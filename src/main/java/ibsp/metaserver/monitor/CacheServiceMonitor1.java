package ibsp.metaserver.monitor;

import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.*;
import ibsp.metaserver.dbservice.CacheService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.MonitorData;
import ibsp.metaserver.utils.*;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheServiceMonitor1 {

    private static Logger logger = LoggerFactory.getLogger(CacheServiceMonitor1.class);
    private static String[] proxyJMXParams = new String[]{"AccessClientConns", "AccessRedisConns", "AccessRequestTps",
            "AccessRequestExcepts", "AccessProcessMaxTime","AccessProcessAvTime"};

    public static void execute(ServiceBean service) {

        //采集proxy数据和监控proxy是不是正常
        List<InstanceDtlBean> cacheProxyList = MetaData.get().getCacheProxysByServId(service.getInstID());

        String servId = service.getInstID();
        ResultBean result = new ResultBean();

        for(InstanceDtlBean cacheProxy : cacheProxyList) {
            if(CONSTS.NOT_DEPLOYED.equals(cacheProxy.getInstance().getIsDeployed()))
                continue;

            checkProxy(cacheProxy, servId, result);
            if(CONSTS.REVOKE_NOK == result.getRetCode()) {
                logger.error(String.format("collect proxy:%s error! %s", cacheProxy.getInstID(), result.getRetInfo()));
            }
        }

        //采集redis数据
        List<InstanceDtlBean> cacheClusterList = new ArrayList<>();
        CacheService.getNodeClustersByServIdOrServiceStub(service.getInstID(), null, cacheClusterList, result);

        for (InstanceDtlBean cluster : cacheClusterList) {
            String masterID = cluster.getAttribute(FixHeader.HEADER_MASTER_ID).getAttrValue();
            if (!HttpUtils.isNotNull(masterID))
                continue;

            //check master
            InstanceDtlBean master = cluster.getSubInstances().get(masterID);

            if (master.getInstance().getIsDeployed().equals(CONSTS.NOT_DEPLOYED))
                continue;

            if (isServerAlive(service.getInstID(), master, EventType.e64, true)) {
                CacheNodeCollectInfo info = collectRedisInfo(master, null, null);
                if (info != null){
                    //保存到monitor
                    MonitorData.get().saveCacheNodeInfo(info);
                }

            } else {
                if (cluster.getSubInstances().size()==1) {
                    logger.info("该主节点没有从节点！尝试拉起该实例！");
                    pullUpInstance(master, null);
                } else {

                    //only one master and one slave is allowed
                    InstanceDtlBean slave = null;
                    for (String slaveID : cluster.getSubInstances().keySet()) {
                        if (slaveID.equals(masterID))
                            continue;
                        slave = cluster.getSubInstances().get(slaveID);
                    }

                    if(slave == null) {
                        continue;
                    }

                    if (!doSwitch(service.getInstID(), cluster, master, slave)) {
                        logger.info("主从切换失败，尝试直接拉起主节点！");
                        pullUpInstance(master, null);
                    } else {
                        masterID = slave.getInstID();
                        master = slave;

                        //pulish e63
                        JsonObject paramsJson = new JsonObject();
                        paramsJson.put(FixHeader.HEADER_CLUSTER_ID, cluster.getInstID());
                        paramsJson.put(FixHeader.HEADER_INSTANCE_ID, cluster.getInstID());
                        paramsJson.put(FixHeader.HEADER_MASTER_ID, masterID);

                        EventBean evBean = new EventBean();
                        evBean.setEvType(EventType.e63);
                        evBean.setServID(service.getInstID());
                        evBean.setUuid(MetaData.get().getUUID());
                        evBean.setJsonStr(paramsJson.toString());
                        EventBusMsg.publishEvent(evBean);
                        MetaDataService.saveAlarm(evBean, null);

                        CacheNodeCollectInfo info = collectRedisInfo(master, null, null);
                        if (info != null){
                            MonitorData.get().saveCacheNodeInfo(info);
                        }
                    }
                }
            }

            //check slave
            for (String slaveID : cluster.getSubInstances().keySet()) {
                if (slaveID.equals(masterID))
                    continue;

                InstanceDtlBean slave = cluster.getSubInstances().get(slaveID);
                if (slave.getInstance().getIsDeployed().equals(CONSTS.NOT_DEPLOYED))
                    continue;

                if (isServerAlive(service.getInstID(), slave, EventType.e64, true)) {
                    CacheNodeCollectInfo info = collectRedisInfo(slave, master, cluster);
                    if (info != null){
                        MonitorData.get().saveCacheNodeInfo(info);
                    }
                } else {
                    pullUpInstance(slave, master);
                }
            }
        }
        CacheService.saveCollectInfo(servId, result);
        syncCollectData(servId);
    }

    private static void checkProxy(InstanceDtlBean cacheProxy, String servId, ResultBean result) {
        CacheProxyCollectInfo proxyCollecInfo = collectProxyInfo(cacheProxy);
        //jmx连接不上
        if(proxyCollecInfo == null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(FixHeader.HEADER_INSTANCE_ID, cacheProxy.getInstID());
            EventBean ev = new EventBean(EventType.e65);
            ev.setServID(servId);
            ev.setJsonStr(jsonObject.toString());

            EventBusMsg.publishEvent(ev);
            MetaDataService.saveAlarm(ev, result);

            logger.warn(String.format("cacheProxy id:%s is down", cacheProxy.getInstID()));

            pullUpProxy(cacheProxy);
        }else {
            MonitorData.get().saveCacheProxyInfo(proxyCollecInfo);
        }

    }

    private static CacheProxyCollectInfo collectProxyInfo(InstanceDtlBean proxy) {
        String proxyId = proxy.getInstID();
        Object[] resArr = JMXUtils.get().getAtrributes(proxyId, proxyJMXParams);
        if(resArr == null) {
            return null;
        }
        long accessClientConns = Long.valueOf(resArr[0].toString());
        long accessRedisConns = Long.valueOf(resArr[1].toString());
        long accessRequestTps = Long.valueOf(resArr[2].toString());
        long accessRequestExcepts = Long.valueOf(resArr[3].toString());

        Double accessProcessMaxTime = Double.valueOf(resArr[4].toString());
        Double accessProcessAvTime = Double.valueOf(resArr[5].toString());
        accessProcessMaxTime = accessProcessMaxTime.equals(Double.NaN) ? 0D: accessProcessMaxTime;
        accessProcessAvTime  = accessProcessAvTime.equals(Double.NaN) ? 0D: accessProcessAvTime;

        CacheProxyCollectInfo proxyCollecInfo = new CacheProxyCollectInfo(proxyId, accessClientConns, accessRedisConns,
                accessRequestTps, accessRequestExcepts, accessProcessMaxTime, accessProcessAvTime);
        return proxyCollecInfo;
    }

    private static void pullUpProxy(InstanceDtlBean proxy) {
        String ip = proxy.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port = proxy.getAttribute(FixHeader.HEADER_PORT).getAttrValue();

        logger.info(String.format("尝试拉起接入机 %s:%s", ip, port));

        String deployPath = String.format("$HOME/cache_proxy_deploy/%s/bin", port);
        execStart(proxy, deployPath, "./" + CONSTS.PROXY_SHELL + " start");

    }

    private static void execStart(InstanceDtlBean bean, String commandPath, String command) {
        String ip = bean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port = bean.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
        String user = bean.getAttribute(FixHeader.HEADER_OS_USER).getAttrValue();
        String pwd = bean.getAttribute(FixHeader.HEADER_OS_PWD).getAttrValue();

        JschUserInfo ui;
        SSHExecutor executor = null;
        boolean connected = false;

        try {
            ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
            executor = new SSHExecutor(ui);
            executor.connect();
            connected = true;
            executor.echo("test");

            executor.cd(commandPath);
            executor.execSingleLine(command, null);

            ResultBean result = new ResultBean();
            if (executor.waitProcessStart(port, null, result)) {
                logger.info("拉起"+bean.getInstID()+"成功！Host:" + ip + ":" + port);
            } else {
                logger.error("拉起"+bean.getInstID()+"失败！Host:" + ip + ":" + port + " " + result.getRetInfo());
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if(connected) {
                executor.close();
            }
        }
    }

    /**
     * 判断实例是否存活
     */
    private static boolean isServerAlive(String servID, InstanceDtlBean instance, EventType type, boolean isRetry) {

        Socket socket = null;
        boolean rstbool = true;
        String host = instance.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port = instance.getAttribute(FixHeader.HEADER_PORT).getAttrValue();

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, Integer.parseInt(port)));
        } catch (IOException e) {
            //如果异常重试3次
            rstbool = false;
            boolean isSuccess = false;

            if(isRetry) {
                for(int i=0;i <3 ;i++) {
                    if(isServerAlive(servID, instance, type, false)) {
                        isSuccess = true;
                        rstbool = true;
                        break;
                    }
                }
            }
            if(isRetry && !isSuccess) {
                logger.warn("进程不存在：" + host+":"+port);
                JsonObject paramsJson = new JsonObject();
                paramsJson.put(FixHeader.HEADER_INSTANCE_ID, instance.getInstID());
                EventBean evBean = new EventBean();
                evBean.setEvType(type);
                evBean.setServID(servID);
                evBean.setJsonStr(paramsJson.toString());
                EventBusMsg.publishEvent(evBean);

                MetaDataService.saveAlarm(evBean, null);
            }
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException ignore) {}
        }
        return rstbool;
    }

    /**
     * 尝试拉起一个redis实例
     */
    private static void pullUpInstance(InstanceDtlBean instance, InstanceDtlBean master) {

        String ip = instance.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port = instance.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
        String user = instance.getAttribute(FixHeader.HEADER_OS_USER).getAttrValue();
        String pwd = instance.getAttribute(FixHeader.HEADER_OS_PWD).getAttrValue();
        String masterIp = "", masterPort = "";
        if (master != null) {
            masterIp = master.getAttribute(FixHeader.HEADER_IP).getAttrValue();
            masterPort = master.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
        }

        logger.info("尝试拉起实例" + ip + ":" + port);
        String deployRootPath = String.format("cache_node_deploy/%s", port);
        JschUserInfo ui;
        SSHExecutor executor = null;
        boolean connected = false;

        try {
            ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
            executor = new SSHExecutor(ui);
            executor.connect();
            connected = true;
            executor.echo("test"); // 有的机器中间加了跳转和管控防止ssh登录"Last login:xxxxx"串到输出一起显示

            if (master != null) {
                if (!RedisUtils.updateConfForAddSlave(ip, port, user, pwd, masterIp, masterPort))
                    return;

                if (!RedisUtils.setConfigForReplication(masterIp, masterPort))
                    return;
            } else {
                if (!RedisUtils.updateConfForRemoveSlave(ip, port, user, pwd))
                    return;
            }

            executor.cd("$HOME/" + deployRootPath);
            executor.execSingleLine("./redis.sh start", null);
            if (executor.waitProcessStart(port, null)) {
                logger.info("拉起节点成功！Host:" + ip + ":" + port);
                if (master != null) {
                    RedisReplicationChecker.get().addReplicationCheckSlave(ip+":"+port);
                    RedisReplicationChecker.get().startChecker();
                }
            } else {
                logger.error("拉起节点失败！Host:" + ip + ":" + port);
            }

        } catch (Exception e) {
            logger.error("Failed to pull up redis instance...", e);
        } finally {
            if (connected) {
                executor.close();
            }
        }
    }

    /**
     * 进行主从切换
     */
    private static boolean doSwitch(String servID, InstanceDtlBean cluster, InstanceDtlBean master, InstanceDtlBean slave) {

        String slaveIP = slave.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String slavePort = slave.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
        String slaveUser = slave.getAttribute(FixHeader.HEADER_OS_USER).getAttrValue();
        String slavePwd = slave.getAttribute(FixHeader.HEADER_OS_PWD).getAttrValue();
        String masterIP = master.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String masterPort = master.getAttribute(FixHeader.HEADER_PORT).getAttrValue();

        if (!isServerAlive(servID, slave, EventType.e64, true))
            return false;

        if (!RedisUtils.removeSlave(slaveIP, slavePort))
            return false;

        try {
            if (!RedisUtils.updateConfForRemoveSlave(slaveIP, slavePort, slaveUser, slavePwd)) {
                //rollback
                RedisUtils.addSlave(slaveIP, slavePort, masterIP, masterPort);
                return false;
            }

            ResultBean result = new ResultBean();
            if (!MetaDataService.modComponentAttribute(cluster.getInstID(),
                    cluster.getAttribute(FixHeader.HEADER_MASTER_ID).getAttrID(),
                    slave.getInstID(), result)) {
                //rollback
                RedisUtils.updateConfForAddSlave(slaveIP, slavePort, slaveUser, slavePwd, masterIP, masterPort);
                RedisUtils.addSlave(slaveIP, slavePort, masterIP, masterPort);
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("Failed to switch redis...", e);
            return false;
        }
    }

    /**
     * 采集redis实例监控信息，同时检测主从角色是否正确
     */
    private static CacheNodeCollectInfo collectRedisInfo(InstanceDtlBean node, InstanceDtlBean master, InstanceDtlBean cluster) {
        String ip = node.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port = node.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
        CacheNodeCollectInfo info = new CacheNodeCollectInfo();
        info.setId(node.getInstID());

        Map<String, String> redisInfo = RedisUtils.getInstanceInfo(ip, port, "");
        Map<String, String> redisConfig = RedisUtils.getInstanceConfig(ip, port, "");
        if (redisInfo == null || redisConfig == null)
            return null;

        try {
            if (master != null) {
                String status = redisInfo.get("master_link_status");
                //主从信息不符（可能发生在断网恢复后）
                if (status == null) {
                    //等待一段时间，防止人工主从切换过程中的误操作
                    Thread.sleep(3000L);

                    if (cluster.getAttribute(FixHeader.HEADER_MASTER_ID).getAttrValue()
                            .equals(master.getInstID())) {
                        String user = node.getAttribute(FixHeader.HEADER_OS_USER).getAttrValue();
                        String pwd = node.getAttribute(FixHeader.HEADER_OS_PWD).getAttrValue();
                        String masterIp = master.getAttribute(FixHeader.HEADER_IP).getAttrValue();
                        String masterPort = master.getAttribute(FixHeader.HEADER_PORT).getAttrValue();

                        if (!RedisUtils.setConfigForReplication(masterIp, masterPort))
                            return null;
                        if (!RedisUtils.updateConfForAddSlave(ip, port, user, pwd, masterIp, masterPort))
                            return null;
                        if (!RedisUtils.addSlave(ip, port, masterIp, masterPort))
                            return null;
                        RedisReplicationChecker.get().addReplicationCheckSlave(ip+":"+port);
                        RedisReplicationChecker.get().startChecker();
                    }
                }
                //从不统计tps
                info.setTotalCommandProcessed(0L);
                info.setLinkStatus(redisInfo.get("master_link_status"));
            }else{
                info.setTotalCommandProcessed(Long.parseLong(redisInfo.get("total_commands_processed")));
            }

            //get db size
            String db0 = redisInfo.get("db0");
            if (db0 == null) {
                info.setDbSize(0);
            } else {
                int dbsizeStart = "keys=".length(), dbsizeEnd = db0.indexOf(",");
                long dbsize = Long.parseLong(db0.substring(dbsizeStart, dbsizeEnd));
                info.setDbSize(dbsize);
            }
            info.setConnectedClients(Integer.parseInt(redisInfo.get("connected_clients")) - 1);
            //get redis memory
            info.setMemoryUsed(Long.parseLong(redisInfo.get("used_memory")));
            info.setMemoryTotal(Long.parseLong(redisConfig.get("maxmemory")));
            info.setTime(System.currentTimeMillis());
            //get persistence policy
           /* if (redisConfig.get("appendonly").equals("yes")) {
                info.setAofPolicy(redisConfig.get("appendfsync"));
                info.setAofSize(Long.parseLong(redisInfo.get("aof_current_size")));
            }
            info.setRdbPolicy(redisConfig.get("save"));*/

            return info;
        } catch (Exception e) {
            logger.error("collectRedisInfo caught error ", e);
            return null;
        }
    }

    private static void syncCollectData(String servId) {
        JsonObject paramsJson = new JsonObject();
        paramsJson.put(FixHeader.HEADER_SERV_TYPE, CONSTS.SERV_TYPE_CACHE);
        paramsJson.put(FixHeader.HEADER_JSONSTR, MonitorData.get().getCacheSyncJson(servId));
        EventBean evBean = new EventBean();
        evBean.setEvType(EventType.e99);
        evBean.setServID(servId);
        evBean.setJsonStr(paramsJson.toString());
        evBean.setUuid(MetaData.get().getUUID());
        EventBusMsg.publishEvent(evBean);
    }
}
