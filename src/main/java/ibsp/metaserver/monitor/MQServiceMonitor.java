package ibsp.metaserver.monitor;

import ibsp.metaserver.bean.*;
import ibsp.metaserver.dbservice.MQService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.MonitorData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.SysConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MQServiceMonitor {

    private static Logger logger = LoggerFactory.getLogger(MQServiceMonitor.class);
    private static Map<String, String> paramsMap = new HashMap<>();

    static {
        paramsMap.put("overview",
                "cluster_name,object_totals,contexts,message_stats.publish," +
                        "message_stats.publish_details,message_stats.ack,message_stats.ack_details," +
                        "queue_totals.messages");
        paramsMap.put("queues",
                "name,node,vhost,durable,auto_delete,arguments,memory,messages_ready,messages_unacknowledged," +
                        "message_bytes_ram,message_bytes_persistent,message_stats.publish,message_stats.ack," +
                        "message_stats.publish_details,message_stats.ack_details");
        paramsMap.put("connections","recv_oct_details,state");
        paramsMap.put("nodes","disk_free,disk_free_limit,mem_used,mem_limit,partitions,name");
        paramsMap.put("channels","message_stats,number,name,connection_details,state");
    }

    public static void excute(ServiceBean serviceBean){
        if(CONSTS.NOT_DEPLOYED.equalsIgnoreCase(serviceBean.getDeployed())) {
            return;
        }

        String servId = serviceBean.getInstID();
        List<InstanceDtlBean> vbrokerList = new ArrayList<>();
        ResultBean result = new ResultBean();

        if(! MQService.getVBrokersByServIdOrServiceStub(servId, null, vbrokerList, result)) {
            return;
        }

        for(InstanceDtlBean vbroker : vbrokerList) {
            String masterID = vbroker.getAttribute("MASTER_ID").getAttrValue();
            if(CONSTS.NOT_DEPLOYED.equalsIgnoreCase(vbroker.getInstance().getIsDeployed()) ||
                    HttpUtils.isNull(masterID)) {
                continue;
            }

            InstanceDtlBean master = vbroker.getSubInstances().get(masterID);
            String vbrokerId = vbroker.getInstID();

            try {
                checkRunningAndHASwitch(vbroker, servId, masterID, result);
                getOverViewInfo(master, servId, result);
                getNodesInfo(master, servId, vbrokerId, result);
                getChannelsInfo(master, vbrokerId);
                getQueuesInfo(master, vbrokerId);
                /*// 检查connections是不是flow 并且recv_oct_details底下的rate是不是0 是的话 要重启从节点
                checkConnectionFlowAndRateToZero(master, servId, vbrokerId, result);*/
                //TODO 发送事件同步采集数据
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }

    }

    //检测vbroker下的broker是不是挂了，检测是不是发生了主从切换
    private static void checkRunningAndHASwitch(InstanceDtlBean vbroker, String servId, String masterID, ResultBean result) {

        boolean needCheckSwitch = true;

        for (InstanceDtlBean broker : vbroker.getSubInstances().values()) {

            if(CONSTS.NOT_DEPLOYED.equalsIgnoreCase(broker.getInstance().getIsDeployed()))
                continue;

            if (MQService.checkBrokerRunning(broker)) {
                //检查主从切换，一个VBroker只要检查一次
                if (needCheckSwitch) {

                    String realMasterID = getMasterIdFromRabbit(broker.getInstID(), result);
                    //获取masterID失败，可能是正好上面检查broker的时候正常，但是检查是否主从切换的时候挂了，
                    // ，等另外一个broker进行主从切换检查，或者只有一个节点等下一个轮回检查broker是不是正常。
                    if(HttpUtils.isNull(realMasterID)){
                        continue;
                    }
                    if (!masterID.equals(realMasterID)) {
                        boolean modMasterID =
                                MetaDataService.modComponentAttribute(vbroker.getInstID(),
                                        vbroker.getAttribute(FixHeader.HEADER_MASTER_ID).getAttrID(),
                                        realMasterID, result);
                        if (modMasterID) {
                            JsonObject paramsJson = new JsonObject();
                            paramsJson.put(FixHeader.HEADER_VBROKER_ID, vbroker.getInstID());
                            paramsJson.put(FixHeader.HEADER_INSTANCE_ID, vbroker.getInstID());
                            paramsJson.put(FixHeader.HEADER_BROKER_ID, realMasterID);
                            EventBean evBean = new EventBean();
                            evBean.setEvType(EventType.e56);
                            evBean.setServID(servId);
                            evBean.setJsonStr(paramsJson.toString());
                            evBean.setUuid(MetaData.get().getUUID());
                            EventBusMsg.publishEvent(evBean);
                            MetaDataService.saveAlarm(evBean, result);
                        }
                    }
                    needCheckSwitch = false;
                }
            } else {
                JsonObject paramsJson = new JsonObject();
                paramsJson.put(FixHeader.HEADER_VBROKER_ID, vbroker.getInstID());
                paramsJson.put(FixHeader.HEADER_INSTANCE_ID, broker.getInstID());
                EventBean evBean = new EventBean();
                evBean.setEvType(EventType.e54);
                evBean.setServID(servId);
                evBean.setJsonStr(paramsJson.toString());
                EventBusMsg.publishEvent(evBean);
                MetaDataService.saveAlarm(evBean, result);

                //拉起从节点时，需要判断当前的数据量，如果积压很多数据的情况下重启从节点，将导致整个VBroker卡住，无法收发数据
                boolean needStart = true;
                if (!broker.getInstID().equals(masterID)) {
                    try {
                        int total = getVBrokerTotalMessage(vbroker.getInstID(), result);
                        if (total>CONSTS.STUCK_NO_OPER_MESSAGE)
                            needStart = false;
                    } catch (Exception e) {
                        //如果主节点也有问题，暂时不拉起从节点，等待主节点恢复
                        needStart = false;
                    }
                }

                if (needStart) {
                    // rabbitmq down 主动拉起
                    MQService.startBroker(broker.getInstID(), "", result);
                    if (result.getRetCode() == CONSTS.REVOKE_OK) {
                        logger.info("broker id:{} down and restart by metaserver.", broker.getInstID());
                    } else {
                        logger.error("broker id:{} down and restart fail: {}.", broker.getInstID(), result.getRetInfo());
                    }
                }
            }
        }
    }

    //检查是不是消息堆积了
    private static void getOverViewInfo(InstanceDtlBean master, String servId, ResultBean result) {
        JsonObject json = getJsonByInst(master, "overview");
        if(json == null) {
            return;
        }

        logger.debug("overview : " + json.toString());
        if(HttpUtils.isNotNull(json)) {
            Long accumulate      = json.getJsonObject("queue_totals").getLong("messages");
            //TODO 后期改为从数据库获取这个service的message accumulate limit数量
            long accumulateLimit = SysConfig.get().getMsgAccumulateHighWaterMark();

            if(accumulate > accumulateLimit){
                JsonObject alarmMess = new JsonObject();
                EventBean ev = new EventBean();
                ev.setEvType(EventType.e47);
                ev.setServID(servId);

                alarmMess.put("accumulate", accumulate);
                alarmMess.put(FixHeader.HEADER_INSTANCE_ID, master.getInstID());

                ev.setJsonStr(alarmMess.toString());
                EventBusMsg.publishEvent(ev);

                /* 保存到数据库 */
                boolean success = MetaDataService.saveAlarm(ev, result);
                if(!success) {
                    logger.error("broker id:{} save alarm message accumulate fail : {}" ,master.getInstID(), result.getRetInfo());
                }
            }
        }
    }

    //检测内存、磁盘高水位和脑裂 ，保存数据到内存
    private static void getNodesInfo(InstanceDtlBean master, String servId, String vbrokerId, ResultBean result){
        JsonArray jsonArray = getJsonArrayByInst(master, "nodes");

        if(jsonArray == null || jsonArray.size() == 0){
            return;
        }

        logger.debug("nodes : " + jsonArray.toString());
        boolean stopPartionNode = false;

        if(HttpUtils.isNotNull(jsonArray)){
            for(int i=0,len=jsonArray.size(); i < len; i++) {
                JsonObject json = jsonArray.getJsonObject(i);
                if (json == null)
                    continue;

                Long diskFree = json.getLong("disk_free");
                Long diskFreeLimit = json.getLong("disk_free_limit");

                Long memUse = json.getLong("mem_used");
                Long memLimit = json.getLong("mem_limit");

                String nodeName = json.getString("name");
                String instId = nodeName.split(CONSTS.NAME_SPLIT)[0];

                //磁盘使用高水位
                if (diskFree != null && diskFreeLimit != null && diskFree <= diskFreeLimit) {
                    EventBean ev = new EventBean();
                    ev.setEvType(EventType.e49);
                    ev.setServID(servId);

                    JsonObject alarmMess = new JsonObject();
                    alarmMess.put(FixHeader.HEADER_INSTANCE_ID, master.getInstID());
                    ev.setJsonStr(alarmMess.toString());
                    EventBusMsg.publishEvent(ev);
                    boolean success = MetaDataService.saveAlarm(ev, result);
                    if (!success) {
                        logger.error("broker id:{} save alarm disk highwater mark fail : {}", master.getInstID(), result.getRetInfo());
                    }
                }

                //内存使用高水位
                if (memUse != null && memLimit != null && memUse > memLimit) {
                    EventBean ev = new EventBean();
                    ev.setEvType(EventType.e48);
                    ev.setServID(servId);

                    JsonObject alarmMess = new JsonObject();
                    alarmMess.put(FixHeader.HEADER_INSTANCE_ID, master.getInstID());
                    ev.setJsonStr(alarmMess.toString());
                    EventBusMsg.publishEvent(ev);
                    boolean success = MetaDataService.saveAlarm(ev, result);
                    if (!success) {
                        logger.error("broker id:{} save alarm memory highwater mark fail : {}", master.getInstID(), result.getRetInfo());
                    }
                }

                //脑裂检测
                JsonArray partitions = json.getJsonArray("partitions");
                if (partitions != null && partitions.size() > 0) {
                    //集群中检测到脑裂只要重启一次
                    if (!stopPartionNode) {
                        EventBean ev = new EventBean();
                        ev.setEvType(EventType.e46);
                        ev.setServID(servId);

                        JsonObject alarmMess = new JsonObject();
                        alarmMess.put(FixHeader.HEADER_INSTANCE_ID, instId);
                        ev.setJsonStr(alarmMess.toString());
                        EventBusMsg.publishEvent(ev);
                        boolean success = MetaDataService.saveAlarm(ev, result);
                        if (!success) {
                            logger.error("save alarm network partition fail : " + result.getRetInfo());
                        }

                        // 停止从节点，恢复脑裂
                        InstanceDtlBean slaveBean = MetaData.get().getSlaveBrokersByVrokerId(vbrokerId);
                        MQService.stopBroker(slaveBean.getInstID(), "", result);
                        if (result.getRetCode() != CONSTS.REVOKE_OK) {
                            logger.error("broker id:{} stop fail: {}.", slaveBean.getInstID(), result.getRetInfo());
                        }
                        stopPartionNode = true;
                    }
                }

                //保存数据到内存
                MQNodeInfoBean nodeInfoBean = new MQNodeInfoBean();
                nodeInfoBean.setInstId(instId);
                nodeInfoBean.setDiskFree(diskFree);
                nodeInfoBean.setDiskFreeLimit(diskFreeLimit);
                nodeInfoBean.setMemUse(memUse);
                nodeInfoBean.setMemLimit(memLimit);
                MonitorData.get().saveMQNodeInfo(vbrokerId, nodeInfoBean);

            }
        }
    }

    //保存vbroker连接信息
    private static void getChannelsInfo(InstanceDtlBean master, String vbrokerId) {
        JsonArray jsonArray = getJsonArrayByInst(master, "channels");

        if(jsonArray == null || jsonArray.size() == 0){
            return;
        }

        logger.debug("channels : " + jsonArray.toString());

        for(int i=0,len=jsonArray.size();i<len;i++){
            JsonObject json = jsonArray.getJsonObject(i);
            if(json == null)
                continue;

            int num = json.getInteger("number");
            if (num == CONSTS.CMD_CHANNEL_ID)
                continue;


            String sourceAddr = json.getJsonObject("connection_details").getString("name");

            MQConnectionInfoBean connectionInfo = new MQConnectionInfoBean();
            connectionInfo.setSourceAddress(sourceAddr);

            JsonObject messageStates = json.getJsonObject("message_stats");

            //只有连接，一直没有开始生产或者消费的连接
            if(messageStates == null || messageStates.size() == 0) {
                int type     = connectionInfo.getConnType().getValue() | ConnType.SEND.getValue();
                connectionInfo.setConnType(ConnType.get(type));
                continue;
            }

            if (num == CONSTS.SEND_CHANNEL_ID) {
                long publish = messageStates.getLong("publish");
                long rate    = messageStates.getJsonObject("publish_details").getLong("rate");
                int type     = connectionInfo.getConnType().getValue() | ConnType.SEND.getValue();
                connectionInfo.setProduceCounts(publish);
                connectionInfo.setProduceRate(rate);
                connectionInfo.setConnType(ConnType.get(type));
            }else if(num >= CONSTS.REV_CHANNEL_START) {
                long ack     = messageStates.getLong("ack");
                long rate    = messageStates.getJsonObject("ack_details").getLong("rate");
                int type     = connectionInfo.getConnType().getValue() | ConnType.RECEIVE.getValue();
                connectionInfo.setConsumerCounts(ack);
                connectionInfo.setConsumerRate(rate);
                connectionInfo.setConnType(ConnType.get(type));
            }

            MonitorData.get().saveMQConnInfo(vbrokerId, connectionInfo);
        }
    }

    //统计队列信息,并且统计所有队列的速率和总数的总和，加到vbroker的统计信息中
    private static void getQueuesInfo(InstanceDtlBean master, String vbrokerId) {
        JsonArray jsonArray = getJsonArrayByInst(master, "queues");

        if(jsonArray == null)
            return;

        logger.debug("queues : " + jsonArray.toString());

        //统计vbroker下的速率和总数
        long vbrokerProduceCounts  = 0L;
        long vbrokerProduceRate   = 0L;
        long vbrokerConsumerCounts = 0L;
        long vbrokerConsumerRate  = 0L;

        for(int i=0,len=jsonArray.size();i<len;i++) {
            //queue队列统计的元数据
            MQQueueInfoBean mqQueueInfoBean = new MQQueueInfoBean();

            JsonObject json = jsonArray.getJsonObject(i);
            JsonObject messageState = json.getJsonObject("message_stats");

            if(messageState != null){

                Long publish      = messageState.getLong("publish");
                Long ack          = messageState.getLong("ack");
                Long publishRate  = 0L;
                Long consumerRate = 0L;

                if(publish != null) {
                    publishRate = messageState.getJsonObject("publish_details").getLong("rate");
                    publishRate = publishRate == null? 0L : publishRate;

                }else{
                    consumerRate = messageState.getJsonObject("ack_details").getLong("rate");
                    consumerRate = consumerRate == null? 0L : consumerRate;
                }

                publish = publish == null? 0L : publish;
                ack = ack == null? 0L : ack;

                mqQueueInfoBean.setProduceRate(publishRate);
                mqQueueInfoBean.setProduceCounts(publish);
                mqQueueInfoBean.setConsumerRate(consumerRate);
                mqQueueInfoBean.setConsumerCounts(ack);

                vbrokerProduceCounts += publish;
                vbrokerProduceRate += publishRate;
                vbrokerConsumerCounts += ack;
                vbrokerConsumerRate += consumerRate;
            }

            String queueName   = json.getString("name");
            String nodeName    = json.getString("node");
            String vhost       = json.getString("vhost");
            String durable     = json.getBoolean("durable").toString();
            String autoDelete  = json.getBoolean("auto_delete").toString();
            String arguments   = json.getJsonObject("arguments").toString();

            long memory        = json.getLong("memory");
            long msgRam        = json.getLong("message_bytes_ram");
            long msgPersistent = json.getLong("message_bytes_persistent");

            long msgReady      = json.getLong("messages_ready");
            long msgUnAck      = json.getLong("messages_unacknowledged");

            mqQueueInfoBean.setVbrokerId(vbrokerId);
            mqQueueInfoBean.setQueueName(queueName);
            mqQueueInfoBean.setNode(nodeName);
            mqQueueInfoBean.setVhost(vhost);
            mqQueueInfoBean.setDurable(durable);
            mqQueueInfoBean.setAutoDelete(autoDelete);
            mqQueueInfoBean.setArguments(arguments);
            mqQueueInfoBean.setMemory(memory);
            mqQueueInfoBean.setMsgRam(msgRam);
            mqQueueInfoBean.setMsgPersistent(msgPersistent);
            mqQueueInfoBean.setMsgReady(msgReady);
            mqQueueInfoBean.setMsgUnAck(msgUnAck);
            mqQueueInfoBean.setTimestamp(System.currentTimeMillis());

            MonitorData.get().saveQueueInfo(mqQueueInfoBean);
        }

        MonitorData.get().saveMQVbrokerCollectInfo(vbrokerId, vbrokerProduceRate, vbrokerProduceCounts,
                vbrokerConsumerRate, vbrokerConsumerCounts);
    }

    private static String getMasterIdFromRabbit(String brokerId, ResultBean result) {
        InstanceDtlBean broker = MetaData.get().getInstanceDtlBean(brokerId);
        if(broker == null) {
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo("no broker info find !");
            return null;
        }

        try {
            JsonArray jsonArray = getJsonArrayByInst(broker, "nodes", "name,uptime");
            if(jsonArray == null) {
                //代表查询失败，可能是broker挂了。等下一次检查broker running的时候启动
                result.setRetCode(CONSTS.REVOKE_NOK);
                result.setRetInfo("get master id from rabbitmq error !");
                return null;
            }
            Long maxTime = 0L;
            String masterNode = "";
            for (int i = 0, len = jsonArray.size(); i < len; i++) {
                JsonObject json = jsonArray.getJsonObject(i);
                String nodeName = json.getString("name");
                Long upTime = json.getLong("uptime");
                upTime = upTime == null ? 0L : upTime;
                if(upTime >= maxTime) {
                    maxTime = upTime;
                    masterNode = nodeName;
                }
            }

            result.setRetCode(CONSTS.REVOKE_OK);

            return masterNode.split("@")[0];

        }catch (Exception e) {
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo("get master id from rabbitmq error !");
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private static int getVBrokerTotalMessage(String vbrokerId, ResultBean result) {
        InstanceDtlBean vbroker = MetaData.get().getInstanceDtlBean(vbrokerId);
        if(vbroker == null) {
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo("No vbroker info found !");
            return -1;
        }

        String masterId = vbroker.getAttribute(FixHeader.HEADER_MASTER_ID).getAttrValue();
        InstanceDtlBean masterBroker = MetaData.get().getInstanceDtlBean(masterId);

        JsonObject json = getJsonByInst(masterBroker, "overview", "queue_totals.messages");

        if(json == null){
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo("mq get message count error !");
            return -1;
        }
        Integer iObj = json.getJsonObject(FixHeader.HEADER_QUEUE_TOTALS).getInteger(FixHeader.HEADER_MESSAGES);


        result.setRetCode(CONSTS.REVOKE_OK);
        return iObj;
    }

    private static JsonObject getJsonByInst(InstanceDtlBean instanceDtlBean, String api){
        return getJsonByInst(instanceDtlBean, api, null);
    }

    private static JsonObject getJsonByInst(InstanceDtlBean instanceDtlBean, String api, String clomuns){
        String host = instanceDtlBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String mgrPort = instanceDtlBean.getAttribute(FixHeader.HEADER_MGR_PORT).getAttrValue();
        String urlString;
        if(clomuns == null){
            urlString = makeUrl(host, mgrPort, api);
        }else{
            urlString = makeUrl(host, mgrPort, api, clomuns);
        }

        String res;
        JsonObject json = null;
        try {
            res = HttpUtils.getUrlData(urlString, CONSTS.MQ_DEFAULT_USER, CONSTS.MQ_DEFAULT_PWD);
            json = new JsonObject(res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return  json;
    }

    private static JsonArray getJsonArrayByInst(InstanceDtlBean instanceDtlBean, String api){
        return getJsonArrayByInst(instanceDtlBean, api, null);
    }

    private static JsonArray getJsonArrayByInst(InstanceDtlBean instanceDtlBean, String api, String columns){
        String host = instanceDtlBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String mgrPort = instanceDtlBean.getAttribute(FixHeader.HEADER_MGR_PORT).getAttrValue();
        String urlString;
        if(columns == null){
            urlString = makeUrl(host, mgrPort, api);
        }else{
            urlString = makeUrl(host, mgrPort, api, columns);
        }

        String res;
        JsonArray json = null;
        try {
            res = HttpUtils.getUrlData(urlString, CONSTS.MQ_DEFAULT_USER, CONSTS.MQ_DEFAULT_PWD);
            json = new JsonArray(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return  json;
    }

    private static String makeUrl(String host, String mgrPort, String api) {
        String columns = paramsMap.get(api);
        return makeUrl(host, mgrPort, api, columns);
    }

    private static String makeUrl(String host, String mgrPort, String api, String columns) {
        StringBuilder https = new StringBuilder();

        https.append(CONSTS.HTTP_STR);
        https.append(host);
        https.append(CONSTS.COLON);
        https.append(mgrPort);
        https.append(CONSTS.PATH_SPLIT);
        https.append(CONSTS.RABBIT_MRG_API);
        https.append(CONSTS.PATH_SPLIT);
        https.append(api);
        https.append("?columns=");
        https.append(columns);

        return https.toString();
    }
}
