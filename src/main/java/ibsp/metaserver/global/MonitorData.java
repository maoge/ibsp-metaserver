package ibsp.metaserver.global;

import ibsp.metaserver.bean.*;
import ibsp.metaserver.monitor.ConnType;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorData {
    private static final Logger logger = LoggerFactory.getLogger(MonitorData.class);
    private static MonitorData monitorData = new MonitorData();

    private Map<String, MQVbrokerCollectInfo> mqVbrokerCollectInfoMap;//vbrokerID -> MQVbrokerCollectInfo
    private Map<String, MQQueueCollectInfo> mqQueueCollectInfoMap;//queueId -> MQQueueCollectInfo

    private Map<String, CacheProxyCollectInfo> cacheProxyCollectInfoMap;//proxyId -> CacheProxyCollecInfo
    private Map<String, CacheNodeCollectInfo> cacheNodeCollectInfoMap;//cacheId -> CacheNodeCollectInfo

    private MonitorData() {
        mqVbrokerCollectInfoMap  = new ConcurrentHashMap<>();
        mqQueueCollectInfoMap    = new ConcurrentHashMap<>();
        cacheProxyCollectInfoMap = new ConcurrentHashMap<>();
        cacheNodeCollectInfoMap  = new ConcurrentHashMap<>();
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
        collectInfo.setTimestamp(System.currentTimeMillis());
    }

    public void saveQueueInfo(MQQueueInfoBean mqQueueInfoBean) {
        if(mqQueueInfoBean == null)
            return;

        QueueBean queueBean = MetaData.get().getQueueBeanByRealQueueName(mqQueueInfoBean.getQueueName());
        String queueName = queueBean.getQueueName();
        String queueId   = queueBean.getQueueId();

        MQQueueCollectInfo mqQueueCollectInfo = mqQueueCollectInfoMap.get(queueId);
        if(mqQueueCollectInfo == null) {
            mqQueueCollectInfo = new MQQueueCollectInfo(queueName);
            mqQueueCollectInfoMap.put(queueId, mqQueueCollectInfo);
        }
        mqQueueCollectInfo.saveQueueInfoBean(mqQueueInfoBean);
    }

    public void saveCacheProxyInfo(CacheProxyCollectInfo cacheProxyCollectInfo) {
        if(cacheProxyCollectInfo == null)
            return;

        cacheProxyCollectInfoMap.put(cacheProxyCollectInfo.getId(), cacheProxyCollectInfo);
    }

    public void saveCacheNodeInfo(CacheNodeCollectInfo cacheNodeCollectInfo) {
        if(cacheNodeCollectInfo == null)
            return;

        cacheNodeCollectInfoMap.put(cacheNodeCollectInfo.getId(), cacheNodeCollectInfo);
    }

    public JsonObject getMqSyncJson(String servId) {
        JsonObject jsonObject = new JsonObject();
        List<InstanceDtlBean> vbrokers = MetaData.get().getVbrokerByServId(servId);
        JsonObject vbJsonObject = new JsonObject();
        for(InstanceDtlBean vbroker : vbrokers) {
            vbJsonObject.put(vbroker.getInstID(), Json.encode(mqVbrokerCollectInfoMap.get(vbroker.getInstID())));
        }
        jsonObject.put("vbrokers",vbJsonObject);

        List<QueueBean> queues= MetaData.get().getQueueListByServId(servId);
        JsonObject queueJsonObject = new JsonObject();
        for(QueueBean queue : queues) {
            queueJsonObject.put(queue.getQueueId(), Json.encode(mqQueueCollectInfoMap.get(queue.getQueueId())));
        }
        jsonObject.put("queues", queueJsonObject);
        return jsonObject;
    }

    public JsonObject getCacheSyncJson(String servId) {
        JsonObject jsonObject = new JsonObject();
        List<InstanceDtlBean> proxys = MetaData.get().getCacheProxysByServId(servId);
        JsonObject proxyJson = new JsonObject();
        for(InstanceDtlBean proxy : proxys) {
            proxyJson.put(proxy.getInstID(), Json.encode(cacheProxyCollectInfoMap.get(proxy.getInstID())));
        }
        jsonObject.put("proxys",proxyJson);

        List<InstanceDtlBean> cacheNodes= MetaData.get().getCacheNodesByServId(servId);
        JsonObject nodeJson = new JsonObject();
        for(InstanceDtlBean cacheNode : cacheNodes) {
            nodeJson.put(cacheNode.getInstID(), Json.encode(cacheNodeCollectInfoMap.get(cacheNode.getInstID())));
        }
        jsonObject.put("cachenodes", nodeJson);
        return jsonObject;
    }

    public void syncMqJson(JsonObject jsonObject, String servId) {

        if(jsonObject == null)
            return;

        JsonObject vbJsonObject = jsonObject.getJsonObject("vbrokers");
        Iterator<Map.Entry<String, Object>> vbIter = vbJsonObject.iterator();
        while(vbIter.hasNext()) {
            Map.Entry<String, Object> entry = vbIter.next();
            String key = entry.getKey();
            JsonObject json = new JsonObject(entry.getValue().toString());
            try {
                mqVbrokerCollectInfoMap.put(key, Json.decodeValue(json.toString(), MQVbrokerCollectInfo.class));
            }catch (Exception e) {
            }
        }

        JsonObject queueJsonObject = jsonObject.getJsonObject("queues");

        Iterator<Map.Entry<String, Object>> queueIter = queueJsonObject.iterator();
        while(queueIter.hasNext()) {
            Map.Entry<String, Object> entry = queueIter.next();
            String key = entry.getKey();
            JsonObject json = new JsonObject(entry.getValue().toString());
            try {
                mqQueueCollectInfoMap.put(key, Json.decodeValue(json.toString(), MQQueueCollectInfo.class));
            }catch (Exception e) {
                logger.error("sync queue data fail : {}", e.getMessage());
            }
        }
    }

    public void syncCacheJson(JsonObject jsonObject, String servId) {

        if(jsonObject == null)
            return;

        JsonObject proxys = jsonObject.getJsonObject("proxys");
        Iterator<Map.Entry<String, Object>> proxyIter = proxys.iterator();
        while(proxyIter.hasNext()) {
            Map.Entry<String, Object> entry = proxyIter.next();
            String key = entry.getKey();
            JsonObject json = new JsonObject(entry.getValue().toString());
            try {
                cacheProxyCollectInfoMap.put(key, Json.decodeValue(json.toString(), CacheProxyCollectInfo.class));
            }catch (Exception e) {
                logger.error("sync proxy data fail : {}", e.getMessage());
            }
        }

        JsonObject queueJsonObject = jsonObject.getJsonObject("cachenodes");

        Iterator<Map.Entry<String, Object>> cacheNodeIter = queueJsonObject.iterator();
        while(cacheNodeIter.hasNext()) {
            Map.Entry<String, Object> entry = cacheNodeIter.next();
            String key = entry.getKey();
            JsonObject json = new JsonObject(entry.getValue().toString());
            try {
                cacheNodeCollectInfoMap.put(key, Json.decodeValue(json.toString(), CacheNodeCollectInfo.class));
            }catch (Exception e) {
                logger.error("sync queue data fail : {}", e.getMessage());
            }
        }
    }

    public JsonArray getVbrokerCollectData(String servId) {
        JsonArray jsonArray = new JsonArray();
        List<InstanceDtlBean> vbrokers = MetaData.get().getVbrokerByServId(servId);

        for(InstanceDtlBean vbroker : vbrokers) {
            MQVbrokerCollectInfo collectInfo = mqVbrokerCollectInfoMap.get(vbroker.getInstID());

            if(collectInfo == null)
                return null;

            JsonObject subJson = new JsonObject()
                    .put(FixHeader.HEADER_VBROKER_ID, vbroker.getInstID())
                    .put(FixHeader.HEADER_VBROKER_NAME, vbroker.getAttribute(FixHeader.HEADER_VBROKER_NAME).getAttrValue())
                    .put(FixHeader.HEADER_PRODUCE_RATE, collectInfo.getProduceRate())
                    .put(FixHeader.HEADER_PRODUCE_COUNTS, collectInfo.getProduceCounts())
                    .put(FixHeader.HEADER_CONSUMER_RATE, collectInfo.getConsumerRate())
                    .put(FixHeader.HEADER_CONSUMER_COUNTS, collectInfo.getConsumerCounts());

            jsonArray.add(subJson);
        }

        return jsonArray;
    }

    public JsonArray getQueueCollectData(String servId) {
        JsonArray jsonArray = new JsonArray();
        List<QueueBean> queues= MetaData.get().getQueueListByServId(servId);

        for(QueueBean queue : queues) {
            MQQueueCollectInfo collectInfo = mqQueueCollectInfoMap.get(queue.getQueueId());

            if(collectInfo == null)
                return null;

            JsonObject subJson = new JsonObject()
                    .put(FixHeader.HEADER_QUEUE_ID, queue.getQueueId())
                    .put(FixHeader.HEADER_QUEUE_NAME, queue.getQueueName())
                    .put(FixHeader.HEADER_PRODUCE_RATE, collectInfo.getProduceRate())
                    .put(FixHeader.HEADER_PRODUCE_COUNTS, collectInfo.getProduceCounts())
                    .put(FixHeader.HEADER_CONSUMER_RATE, collectInfo.getConsumerRate())
                    .put(FixHeader.HEADER_CONSUMER_COUNTS, collectInfo.getConsumerCounts());

            jsonArray.add(subJson);
        }

        return jsonArray;
    }

    public Map<String, MQVbrokerCollectInfo> getMqVbrokerCollectInfoMap() {
        return mqVbrokerCollectInfoMap;
    }

    public Map<String, MQQueueCollectInfo> getMqQueueCollectInfoMap() {
        return mqQueueCollectInfoMap;
    }

    public Map<String, CacheProxyCollectInfo> getCacheProxyCollectInfoMap() {
        return cacheProxyCollectInfoMap;
    }

    public Map<String, CacheNodeCollectInfo> getCacheNodeCollectInfoMap() {
        return cacheNodeCollectInfoMap;
    }

    public JsonObject toJson() {
        return new JsonObject().put("mqVbrokerCollectInfoMap" , HttpUtils.mapToJson(mqVbrokerCollectInfoMap))
                .put("mqQueueCollectInfoMap", HttpUtils.mapToJson(mqQueueCollectInfoMap))
                .put("cacheProxyCollecInfoMap", HttpUtils.mapToJson(cacheProxyCollectInfoMap))
                .put("cacheNodeCollectInfoMap", HttpUtils.mapToJson(cacheNodeCollectInfoMap));
    }

    public static MonitorData fromJson(JsonObject json) {
        MonitorData m = new MonitorData();
        m.mqVbrokerCollectInfoMap  = HttpUtils.jsonToMap(json.getJsonObject("mqVbrokerCollectInfoMap"),
                MQVbrokerCollectInfo.class);
        m.mqQueueCollectInfoMap    = HttpUtils.jsonToMap(json.getJsonObject("mqQueueCollectInfoMap"),
                MQQueueCollectInfo.class);
        m.cacheProxyCollectInfoMap = HttpUtils.jsonToMap(json.getJsonObject("cacheProxyCollecInfoMap"),
                CacheProxyCollectInfo.class);
        m.cacheNodeCollectInfoMap  = HttpUtils.jsonToMap(json.getJsonObject("cacheProxyCollecInfoMap"),
                CacheNodeCollectInfo.class);
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
