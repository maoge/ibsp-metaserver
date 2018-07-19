package ibsp.metaserver.microservice.handler;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.global.ClientStatisticData;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
