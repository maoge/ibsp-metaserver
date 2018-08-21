package ibsp.metaserver.global;

import ibsp.metaserver.bean.*;
import ibsp.metaserver.monitor.ConnType;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.Json;
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

    private Map<String, PDClusterStatus> pdClusterStatusMap;//pdID -> PDClusterStatus
    private Map<String, TiDBMetricsStatus> tiDBMetricsStatusMap;//pdID -> PDClusterStatus
    private Map<String, TiKVMetricsStatus> tiKVMetricsStatusMap;//pdID -> PDClusterStatus

    private MonitorData() {
        mqVbrokerCollectInfoMap  = new ConcurrentHashMap<>();
        mqQueueCollectInfoMap    = new ConcurrentHashMap<>();
        cacheProxyCollectInfoMap = new ConcurrentHashMap<>();
        cacheNodeCollectInfoMap  = new ConcurrentHashMap<>();
        pdClusterStatusMap       = new ConcurrentHashMap<>();
        tiDBMetricsStatusMap     = new ConcurrentHashMap<>();
        tiKVMetricsStatusMap     = new ConcurrentHashMap<>();
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
        String instId = cacheNodeCollectInfo.getId();

        synchronized (MonitorData.class) {
            CacheNodeCollectInfo prevCollectInfo = cacheNodeCollectInfoMap.get(instId);
            if(prevCollectInfo == null) {
                cacheNodeCollectInfo.setProcessTps(0);
            }else {
                long intervalProcessCount = cacheNodeCollectInfo.getTotalCommandProcessed() -
                        prevCollectInfo.getTotalCommandProcessed();
                long intervalTime = cacheNodeCollectInfo.getTime() - prevCollectInfo.getTime();
                if(intervalProcessCount<=0L || (int) intervalTime / 1000L <= 0) {
                    cacheNodeCollectInfo.setProcessTps(0);
                }else {
                    cacheNodeCollectInfo.setProcessTps( (int) (intervalProcessCount / (intervalTime / 1000L)));
                }
            }
            cacheNodeCollectInfoMap.put(cacheNodeCollectInfo.getId(), cacheNodeCollectInfo);
        }
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

        if(queues == null)
            return queueJsonObject;

        for(QueueBean queue : queues) {
            queueJsonObject.put(queue.getQueueId(), Json.encode(mqQueueCollectInfoMap.get(queue.getQueueId())));
        }
        jsonObject.put("queues", queueJsonObject);
        return jsonObject;
    }

    public JsonObject getTiDBSyncJson(String servId) {
        JsonObject jsonObject = new JsonObject();
        List<InstanceDtlBean> tidbs = MetaData.get().getTiDBsByServId(servId);
        JsonObject tidbJson = new JsonObject();
        for(InstanceDtlBean tidb : tidbs) {
            tidbJson.put(tidb.getInstID(), Json.encode(tiDBMetricsStatusMap.get(tidb.getInstID())));
        }
        jsonObject.put("tidb",tidbJson);

        List<InstanceDtlBean> pds = MetaData.get().getPDsByServId(servId);
        JsonObject pdJson = new JsonObject();
        for(InstanceDtlBean pd : pds) {
            pdJson.put(pd.getInstID(), Json.encode(pdClusterStatusMap.get(pd.getInstID())));
        }
        jsonObject.put("pd",pdJson);
        return jsonObject;
    }

    public JsonObject getTiKVSyncJson(String instId) {
        JsonObject jsonObject = new JsonObject();

        JsonObject tikvJson = new JsonObject();
        tikvJson.put(instId, Json.encode(tiKVMetricsStatusMap.get(instId)));

        jsonObject.put("tikv",tikvJson);

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
            if(json == null)
                continue;
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
            if(json == null)
                continue;
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
            if(json == null)
                continue;
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
            if(json == null)
                continue;
            try {
                cacheNodeCollectInfoMap.put(key, Json.decodeValue(json.toString(), CacheNodeCollectInfo.class));
            }catch (Exception e) {
                logger.error("sync queue data fail : {}", e.getMessage());
            }
        }
    }

    public void syncTiDBJson(JsonObject jsonObject, String servId) {

        if(jsonObject == null)
            return;

        JsonObject tidbJson = jsonObject.getJsonObject("tidb");
        if(tidbJson != null) {
            Iterator<Map.Entry<String, Object>> tidbIter = tidbJson.iterator();
            while(tidbIter.hasNext()) {
                Map.Entry<String, Object> entry = tidbIter.next();
                String key = entry.getKey();
                JsonObject json = new JsonObject(entry.getValue().toString());
                if(json != null && json.size() > 0){
                    try {
                        tiDBMetricsStatusMap.put(key, Json.decodeValue(json.toString(), TiDBMetricsStatus.class));
                    }catch (Exception e) {
                        logger.error("sync tidb data fail : {}", e.getMessage());
                    }
                }
            }
        }

        JsonObject pdJson = jsonObject.getJsonObject("pd");
        if(pdJson != null) {
            Iterator<Map.Entry<String, Object>> pdIter = pdJson.iterator();
            while(pdIter.hasNext()) {
                Map.Entry<String, Object> entry = pdIter.next();
                String key = entry.getKey();
                if(entry.getValue() == null || "".equals(entry.getValue().toString()) || "null".equals(entry.getValue().toString()))
                    continue;

                try {
                    PDClusterStatus status = Json.decodeValue(entry.getValue().toString(), PDClusterStatus.class);
                    if(status != null)
                        pdClusterStatusMap.put(key, status);
                }catch (Exception e) {
                    logger.error("sync pd data fail : {}", e.getMessage());
                }

            }
        }

        JsonObject tikvJson = jsonObject.getJsonObject("tikv");
        if(tikvJson != null) {
            Iterator<Map.Entry<String, Object>> tikvIter = tikvJson.iterator();
            while(tikvIter.hasNext()) {
                Map.Entry<String, Object> entry = tikvIter.next();
                String key = entry.getKey();
                JsonObject json = new JsonObject(entry.getValue().toString());
                if(json != null && json.size() > 0) {
                    try {
                        tiKVMetricsStatusMap.put(key, Json.decodeValue(json.toString(), TiKVMetricsStatus.class));
                    }catch (Exception e) {
                        logger.error("sync tikv data fail : {}", e.getMessage());
                    }
                }
            }
        }
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

    public Map<String, PDClusterStatus> getPdClusterStatusMap() {
        return pdClusterStatusMap;
    }

    public Map<String, TiDBMetricsStatus> getTiDBMetricsStatusMap() {
        return tiDBMetricsStatusMap;
    }

    public Map<String, TiKVMetricsStatus> getTiKVMetricsStatusMap() {
        return tiKVMetricsStatusMap;
    }

    public JsonObject toJson() {
        return new JsonObject().put("mqVbrokerCollectInfoMap" , HttpUtils.mapToJson(mqVbrokerCollectInfoMap))
                .put("mqQueueCollectInfoMap", HttpUtils.mapToJson(mqQueueCollectInfoMap))
                .put("cacheProxyCollecInfoMap", HttpUtils.mapToJson(cacheProxyCollectInfoMap))
                .put("cacheNodeCollectInfoMap", HttpUtils.mapToJson(cacheNodeCollectInfoMap))
                .put("pdClusterStatusMap", HttpUtils.mapToJson(pdClusterStatusMap))
                .put("tiDBMetricsStatusMap", HttpUtils.mapToJson(tiDBMetricsStatusMap))
                .put("tiKVMetricsStatusMap", HttpUtils.mapToJson(tiKVMetricsStatusMap));
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
        m.pdClusterStatusMap       = HttpUtils.jsonToMap(json.getJsonObject("pdClusterStatusMap"),
                PDClusterStatus.class);
        m.tiDBMetricsStatusMap       = HttpUtils.jsonToMap(json.getJsonObject("tiDBMetricsStatusMap"),
                TiDBMetricsStatus.class);
        m.tiKVMetricsStatusMap       = HttpUtils.jsonToMap(json.getJsonObject("tiDBMetricsStatusMap"),
                TiKVMetricsStatus.class);
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
