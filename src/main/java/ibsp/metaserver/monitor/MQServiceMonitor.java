package ibsp.metaserver.monitor;

import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.dbservice.MQService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
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
                "cluster_name,statistics_db_node,object_totals,contexts,message_stats.publish," +
                        "message_stats.publish_details,message_stats.ack,message_stats.ack_details," +
                        "queue_totals.messages");
        paramsMap.put("queues",
                "name,node,vhost,durable,auto_delete,arguments,memory,messages_ready,messages_unacknowledged," +
                        "message_bytes_ram,message_bytes_persistent,message_stats.publish,message_stats.ack," +
                        "message_stats.publish_details,message_stats.ack_details");
        paramsMap.put("connections","recv_oct_details,state");
        paramsMap.put("nodes","disk_free,mem_used,mem_limit,partitions,name");
        paramsMap.put("channels","message_stats,number,name,connection_details,state");
    }

    public static void excute(ServiceBean serviceBean){
        if(CONSTS.NOT_DEPLOYED.equalsIgnoreCase(serviceBean.getDeployed())) {
            return;
        }

        String instID = serviceBean.getInstID();
        List<InstanceDtlBean> vbrokerList = new ArrayList<>();
        ResultBean result = new ResultBean();

        if(! MQService.getVBrokersByServIdOrServiceStub(instID, null, vbrokerList, result)) {
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
                checkRunning(vbroker, instID, masterID, result);
                /*getOverViewInfo(master, servId);
                getNodesInfo(master, servId, vbrokerId, result);
                getChannelsInfo(master, servId, vbrokerId);
                getQueuesInfo(master, vbrokerId);
                // 检查connections是不是flow 并且recv_oct_details底下的rate是不是0 是的话 要重启从节点
                checkConnectionFlowAndRateToZero(master, servId, vbrokerId, result);*/
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }

    }

    private static void checkRunning(InstanceDtlBean vbroker, String instID, String masterID, ResultBean result) {

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
                            evBean.setServID(instID);
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
                evBean.setServID(instID);
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
        int count = iObj.intValue();

        result.setRetCode(CONSTS.REVOKE_OK);
        return count;
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
