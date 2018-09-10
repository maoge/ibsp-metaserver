package ibsp.metaserver.microservice.handler;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.MQQueueCollectInfo;
import ibsp.metaserver.bean.QueueBean;
import ibsp.metaserver.global.ClientStatisticData;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.MonitorData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@App(path = "/meta")
public class MetaHandle {
    private static Logger logger = LoggerFactory.getLogger(MetaHandle.class);

    @Service(id = "getMQClients", name = "getMQClients", auth = false, bwswitch = false)
    public static void getMQClients(RoutingContext routeContext) {
        Set<String> clients = ClientStatisticData.get().getMqClients();
        JsonArray json = null;
        if(clients != null && clients.size() > 0){
            json = new JsonArray();
            for(String s : clients){
                json.add(s);
            }
        }
        HttpUtils.outJsonArray(routeContext, json);
    }

    @Service(id = "getCacheClients", name = "getCacheClients", auth = false, bwswitch = false)
    public static void getCacheClients(RoutingContext routeContext) {
        Set<String> clients = ClientStatisticData.get().getCacheClients();
        JsonArray json = null;
        if(clients != null && clients.size() > 0){
            json = new JsonArray();
            for(String s : clients){
                json.add(s);
            }
        }
        HttpUtils.outJsonArray(routeContext, json);
    }

    @Service(id = "getMonitorData", name = "getMonitorData", auth = true, bwswitch = false)
    public static void getMonitorData(RoutingContext routeContext) {
        MonitorData monitorData = MonitorData.get();
        JsonObject json = JsonObject.mapFrom(monitorData.toJson());
        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "getMetaServerMap", name = "getMetaServerMap", auth = false, bwswitch = false)
    public static void getMetaServerMap(RoutingContext routeContext) {
        MetaData metaData = MetaData.get();
        JsonObject json = JsonObject.mapFrom(HttpUtils.mapToJson(metaData.getServerMap()));
        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "getInstanceDtlMap", name = "getInstanceDtlMap", auth = false, bwswitch = false)
    public static void getInstanceDtlMap(RoutingContext routeContext) {
        MetaData metaData = MetaData.get();
        JsonObject json = JsonObject.mapFrom(HttpUtils.mapToJson(metaData.getInstanceDtlMap()));
        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "getTopo", name = "getTopo", auth = false, bwswitch = false)
    public static void getTopo(RoutingContext routeContext) {
        MetaData metaData = MetaData.get();
        JsonObject json = JsonObject.mapFrom(Json.encode(metaData.getTopo()));
        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "getMqQueuesMonitor", name = "getMqQueueMonitor", auth = false, bwswitch = false)
    public static void getMqQueuesMonitor(RoutingContext routeContext) {
        Map<String, String> params = HttpUtils.getParamForMap(routeContext);
        JsonObject json = new JsonObject();
        String queueNames = null;

        if(params == null || (queueNames = params.get("QUEUE_NAMES")) == null) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
            HttpUtils.outJsonObject(routeContext, json);
            return;
        }

        String[] queueNameArr = queueNames.split(",");

        json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
        json.put(FixHeader.HEADER_RET_INFO, getMoQueue(queueNameArr));

        HttpUtils.outJsonObject(routeContext, json);
    }

    private static JsonObject getMoQueue(String... queueNames) {
        JsonObject jsonObject = new JsonObject();
        QueueBean queue = null;
        Map<String, MQQueueCollectInfo> collectInfoMap = MonitorData.get().getMqQueueCollectInfoMap();

        for(String queueName : queueNames) {
            if(HttpUtils.isNull(queueName) || (queue = MetaData.get().getQueueBeanByName(queueName)) == null) {
                continue;
            }

            MQQueueCollectInfo collectInfo = collectInfoMap.get(queue.getQueueId());
            if(collectInfo == null) {
                continue;
            }

            jsonObject.put(queueName, JsonObject.mapFrom(collectInfo));
        }
        return jsonObject;
    }
}
