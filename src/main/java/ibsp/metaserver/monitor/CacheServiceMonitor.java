package ibsp.metaserver.monitor;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.dbservice.CacheService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.RedisUtils;
import io.vertx.core.json.JsonObject;

public class CacheServiceMonitor implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(CacheServiceMonitor.class);
	private static Map<String, Integer> replicationCountMap = new HashMap<String, Integer>(); //check for replication status

	@Override
	public void run() {
		try {
			List<ServiceBean> serviceList = MetaDataService.getAllDeployedServices();
			for (ServiceBean service : serviceList) {
				if (!service.getServType().equals(CONSTS.SERV_TYPE_CACHE))
					continue;
			
				List<InstanceDtlBean> clusterList = new ArrayList<InstanceDtlBean>();
				ResultBean result = new ResultBean();
				CacheService.getNodeClustersByServIdOrServiceStub(service.getInstID(), null, clusterList, result);
				for (InstanceDtlBean cluster : clusterList) {
					String masterID = cluster.getAttribute("MASTER_ID").getAttrValue();
					if (!HttpUtils.isNotNull(masterID))
						continue;
					
					//check master
					InstanceDtlBean master = cluster.getSubInstances().get(masterID);
					
					if (master.getInstance().getIsDeployed().equals(CONSTS.DEPLOYED) &&
							!isServerAlive(service.getInstID(), cluster.getInstID(), master)) {
						
						if (cluster.getSubInstances().size()==1) {
				            logger.info("该主节点没有从节点！尝试拉起该实例！");
							this.pullUpInstance(master, null);
						} else {
							
							//only one master and one slave is allowed
							InstanceDtlBean slave = null;
							for (String slaveID : cluster.getSubInstances().keySet()) {
								if (slaveID.equals(masterID))
									continue;
								slave = cluster.getSubInstances().get(slaveID);
							}
							
							if (!doSwitch(service.getInstID(), cluster.getInstID(), master, slave)) {
								logger.info("主从切换失败，尝试直接拉起主节点！");
								this.pullUpInstance(master, null);
							} else {
								//pulish e63
								JsonObject paramsJson = new JsonObject();
								paramsJson.put(FixHeader.HEADER_CLUSTER_ID, cluster.getInstID());
								paramsJson.put(FixHeader.HEADER_NEW_MASTER_ID, slave.getInstID());
								
								EventBean evBean = new EventBean();
								evBean.setEvType(EventType.e63);
								evBean.setServID(service.getInstID());
								evBean.setUuid(MetaData.get().getUUID());
								evBean.setJsonStr(paramsJson.toString());
								EventBusMsg.publishEvent(evBean);
								//TODO save alarm to DB
								
								//pull up old master as slave
								this.pullUpInstance(master, slave);
							}
						}
					}
					
					//check slave
					for (String slaveID : cluster.getSubInstances().keySet()) {
						if (slaveID.equals(masterID))
							continue;
						
						InstanceDtlBean slave = cluster.getSubInstances().get(slaveID);
						if (slave.getInstance().getIsDeployed().equals(CONSTS.DEPLOYED) &&
								!isServerAlive(service.getInstID(), cluster.getInstID(), slave)) {
							this.pullUpInstance(slave, master);
						}
					}
				}
			}
			
			//check replication status for new slaves
			for (String address : replicationCountMap.keySet()) {
				Map<String, String> replicationInfo = 
						RedisUtils.getReplicationInfo(address.split(":")[0], address.split(":")[1]);
				
				String status = replicationInfo.get("master_link_status");
    			if (status.equals("up")) {
    				int count = replicationCountMap.get(address)+1;
    				if (count>=3) {
    					replicationCountMap.remove(address);
    					String masterIp = replicationInfo.get("master_host");
    					String masterPort = replicationInfo.get("master_port");
						RedisUtils.recoverConfigForReplication(masterIp, masterPort);
    				} else {
    					replicationCountMap.put(address, count);
    				}
    			} else {
    				replicationCountMap.put(address, 0);
    			}
    		}
			
		} catch (Exception e) {
			logger.error("CacheServiceCollect caught error: ", e);
		}
	}
	
	public static void addReplicationCheckSlave(String address) {
		replicationCountMap.put(address, 0);
	}
	
	/**
	 * 判断redis实例是否存活
	 */
	private boolean isServerAlive(String servID, String clusterID, InstanceDtlBean instance) {
		
		Socket socket = null;
		boolean rstbool = true;
		String host = instance.getAttribute("IP").getAttrValue();
		String port = instance.getAttribute("PORT").getAttrValue();
		
		try {
			socket = new Socket(host, Integer.parseInt(port));
		} catch (IOException e) {
			rstbool = false;
			logger.warn("redis进程不存在：" + host+":"+port);
			
			JsonObject paramsJson = new JsonObject();
			paramsJson.put("CLUSTER_ID", clusterID);
			paramsJson.put("INST_ID", instance.getInstID());
			
			EventBean evBean = new EventBean();
			evBean.setEvType(EventType.e64);
			evBean.setServID(servID);
			evBean.setJsonStr(paramsJson.toString());
			EventBusMsg.publishEvent(evBean);
			
			//TODO save alarm to DB
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
    private void pullUpInstance(InstanceDtlBean instance, InstanceDtlBean master) {
		
    	String ip = instance.getAttribute("IP").getAttrValue();
		String port = instance.getAttribute("PORT").getAttrValue();
		String user = instance.getAttribute("OS_USER").getAttrValue();
		String pwd = instance.getAttribute("OS_PWD").getAttrValue();
		String masterIp = "", masterPort = "";
		if (master != null) {
			masterIp = master.getAttribute("IP").getAttrValue();
			masterPort = master.getAttribute("PORT").getAttrValue();
		}
    	
        logger.info("尝试拉起实例" + ip + ":" + port);
    	String deployRootPath = String.format("cache_node_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
        
        try {
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			executor.echo("test"); // 有的机器中间加了跳转和管控防止ssh登录"Last login:xxxxx"串到输出一起显示
        	
        	if (master != null) {
        		if (!RedisUtils.updateConfForAddSlave(executor.getHome(), ip, port, user, pwd, masterIp, masterPort))
        			return;
        	
        		if (!RedisUtils.setConfigForReplication(masterIp, masterPort))
        			return;
        	} else {
        		if (!RedisUtils.updateConfForRemoveSlave(executor.getHome(), ip, port, user, pwd))
        			return;
        	}
        	
        	executor.cd("$HOME/" + deployRootPath);
        	executor.execSingleLine("bin/redis-server conf/redis.conf", null);
        	if (executor.waitProcessStart(port, null)) {
    			logger.info("拉起节点成功！Host:" + ip + ":" + port);
    			if (master != null) {
    				replicationCountMap.put(ip+":"+port, 0);
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
    private boolean doSwitch(String servID, String clusterID, InstanceDtlBean master, InstanceDtlBean slave) {
    	
		String slaveIP = slave.getAttribute("IP").getAttrValue();
		String slavePort = slave.getAttribute("PORT").getAttrValue();
		String slaveUser = slave.getAttribute("OS_USER").getAttrValue();
		String slavePwd = slave.getAttribute("OS_PWD").getAttrValue();
		String masterIP = master.getAttribute("IP").getAttrValue();
		String masterPort = master.getAttribute("PORT").getAttrValue();
		
		if (!isServerAlive(servID, clusterID, slave))
			return false;

			
		if (!RedisUtils.removeSlave(slaveIP, slavePort))
			return false;
		
		SSHExecutor executor = null;
		boolean connected = false;
		try {
			JschUserInfo ui = new JschUserInfo(slaveUser, slavePwd, slaveIP, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			executor.echo("test"); // 有的机器中间加了跳转和管控防止ssh登录"Last login:xxxxx"串到输出一起显示
    	
			if (!RedisUtils.updateConfForRemoveSlave(executor.getHome(), slaveIP, slavePort, slaveUser, slavePwd)) {
				//rollback
				RedisUtils.addSlave(slaveIP, slavePort, masterIP, masterPort);
				return false;
			}
			
			ResultBean result = new ResultBean();
			if (!CacheService.updateMasterID(slave.getInstID(), clusterID, result)) {
				//rollback
				RedisUtils.updateConfForAddSlave(executor.getHome(), slaveIP, slavePort, 
						slaveUser, slavePwd, masterIP, masterPort);
				RedisUtils.addSlave(slaveIP, slavePort, masterIP, masterPort);
				return false;
			}
			
			return true;
		} catch (Exception e) {
			logger.error("Failed to switch redis...", e);
			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
    }
}
