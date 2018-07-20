package ibsp.metaserver.global;

import ibsp.metaserver.bean.MQConnectionInfoBean;
import ibsp.metaserver.bean.MQNodeInfoBean;
import ibsp.metaserver.bean.MQVbrokerCollectInfo;
import ibsp.metaserver.monitor.ConnType;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorData {
    private static MonitorData monitorData = new MonitorData();
    private Map<String, MQVbrokerCollectInfo> mqVbrokerCollectInfoMap;

    private MonitorData() {
        mqVbrokerCollectInfoMap = new ConcurrentHashMap<>();
    }

    public static MonitorData get() {
        return monitorData;
    }

    public void saveMQNodeInfo(String vbrokerId, MQNodeInfoBean nodeInfoBean) {
        if(nodeInfoBean == null)
            return;

        MQVbrokerCollectInfo collectInfo = mqVbrokerCollectInfoMap.get(vbrokerId);
        if(collectInfo == null) {
            collectInfo = new MQVbrokerCollectInfo();
            mqVbrokerCollectInfoMap.put(vbrokerId, collectInfo);
        }

        collectInfo.addNodeInfo(nodeInfoBean);
    }

    public void saveMQConnInfo(String vbrokerId, MQConnectionInfoBean connInfoBean) {
        if(connInfoBean == null)
            return;

        MQVbrokerCollectInfo collectInfo = mqVbrokerCollectInfoMap.get(vbrokerId);
        if(collectInfo == null) {
            collectInfo = new MQVbrokerCollectInfo();
            mqVbrokerCollectInfoMap.put(vbrokerId, collectInfo);
        }

        collectInfo.addConnInfo(connInfoBean);
    }

    public void saveMQVbrokerCollectInfo (String vbrokerId, long produceRate, long produceCounts, long consumerRate,
                                          long consumerCounts) {
        MQVbrokerCollectInfo collectInfo = mqVbrokerCollectInfoMap.get(vbrokerId);
        if(collectInfo == null) {
            collectInfo = new MQVbrokerCollectInfo();
            mqVbrokerCollectInfoMap.put(vbrokerId, collectInfo);
        }
        collectInfo.setProduceRate(produceRate);
        collectInfo.setProduceCounts(produceCounts);
        collectInfo.setConsumerRate(consumerRate);
        collectInfo.setConsumerCounts(consumerCounts);
    }

    public Map<String, MQVbrokerCollectInfo> getMqVbrokerCollectInfoMap() {
        return mqVbrokerCollectInfoMap;
    }


    public JsonObject toJson() {
        return new JsonObject().put("mqVbrokerCollectInfoMap" , HttpUtils.mapToJson(mqVbrokerCollectInfoMap));
    }

    public static MonitorData fromJson(JsonObject json) {
        MonitorData m = new MonitorData();
        m.mqVbrokerCollectInfoMap = HttpUtils.jsonToMap(json.getJsonObject("mqVbrokerCollectInfoMap"), MQVbrokerCollectInfo.class);
        return m;
    }

    public static void main(String[] args) {
        MQVbrokerCollectInfo m = new MQVbrokerCollectInfo();
        MQNodeInfoBean node1 = new MQNodeInfoBean();
        node1.setInstId("broker-a");
        node1.setDiskFree(1);
        node1.setDiskFreeLimit(2);
        node1.setMemUse(1);
        node1.setMemLimit(2);
        m.getNodeInfoMap().put(node1.getInstId(), node1);

        MQNodeInfoBean node2 = new MQNodeInfoBean();
        node2.setInstId("broker-b");
        node2.setDiskFree(1);
        node2.setDiskFreeLimit(2);
        node2.setMemUse(1);
        node2.setMemLimit(2);
        m.getNodeInfoMap().put(node2.getInstId(), node2);

        MQConnectionInfoBean conn1 = new MQConnectionInfoBean();
        conn1.setSourceAddress("127.0.0.1:9500 -> 172.20.0.82:9390");
        conn1.setProduceRate(1);
        conn1.setProduceCounts(2);
        conn1.setConsumerRate(1);
        conn1.setConsumerCounts(2);
        conn1.setConnType(ConnType.SEND);

        m.getConnInfoMap().put(conn1.getSourceAddress(), conn1);
        MonitorData monitor = new MonitorData();
        monitor.mqVbrokerCollectInfoMap.put("vbroker-all-1" , m);
        System.out.println(MonitorData.fromJson(monitor.toJson()).toJson());
    }
}
