package ibsp.metaserver.microservice.handler;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.SequoiaDBService;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

@App(path = "/sdbsvr")
public class SequoiaDBHandle {

    @Service(id = "getSdbInfoByService", name = "getSdbInfoByService", auth = true, bwswitch = true)
    public static void getSdbInfoByService(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);
        if(params == null) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
        } else {
            String servID = params.get(FixHeader.HEADER_SERV_ID);
            if (!HttpUtils.isNotNull(servID)) {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
                json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
            } else {
                ResultBean result = new ResultBean();
                JsonArray array = SequoiaDBService.getSdbInfoByService(servID, result);
                if (array!=null) {
                    json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
                    json.put(FixHeader.HEADER_RET_INFO, array);
                } else {
                    json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
                    json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
                }
            }
        }

        HttpUtils.outJsonObject(routeContext, json);
    }
}
