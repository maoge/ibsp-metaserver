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

    @Service(id = "getCLInfo", name = "getCLInfo", auth = false, bwswitch = false)
    public static void getCLInfo(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        JsonObject res = SequoiaDBService.getCLInfo("172.20.0.81","sdbadmin","sdbadmin");
        if(res != null) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
            json.put(FixHeader.HEADER_RET_INFO, res);
        }else {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, new JsonArray());
        }



        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "getCSInfo", name = "getCSInfo", auth = false, bwswitch = false)
    public static void getCSInfo(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);

        JsonObject res = SequoiaDBService.getCSInfo("172.20.0.81","sdbadmin","sdbadmin");
        if(res != null) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
            json.put(FixHeader.HEADER_RET_INFO, res);
        }else {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, new JsonArray());
        }

        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "addCS", name = "addCS", auth = false, bwswitch = false)
    public static void addCS(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);
        String csName = params.get("CS_NAME");
        String domainName = params.get("DOMAIN_NAME");
        if(HttpUtils.isNull(csName) || HttpUtils.isNull(domainName)) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
        }else {
            ResultBean resultBean = new ResultBean();
            boolean res = SequoiaDBService.addCS("172.20.0.81","sdbadmin","sdbadmin", csName,
                    domainName, resultBean);
            if(res) {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
                json.put(FixHeader.HEADER_RET_INFO, new JsonObject());
            }else {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
                json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
            }

        }

        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "delCS", name = "delCS", auth = false, bwswitch = false)
    public static void delCS(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);
        String csName = params.get("CS_NAME");
        if(HttpUtils.isNull(csName)) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
        }else {
            ResultBean resultBean = new ResultBean();
            boolean res = SequoiaDBService.delCS("172.20.0.81","sdbadmin","sdbadmin", csName, resultBean);
            if(res) {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
                json.put(FixHeader.HEADER_RET_INFO, new JsonObject());
            }else {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
                json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
            }

        }

        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "addCL", name = "addCL", auth = false, bwswitch = false)
    public static void addCL(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);
        String csName = params.get("CS_NAME");
        String clName = params.get("CL_NAME");
        String sharedingKey = params.get("SHAREDING_KEY");
        String clType = params.get("CL_TYPE");
        String replicaNum = params.get("RP_NUM");
        if(HttpUtils.isNull(csName) || HttpUtils.isNull(clName) || HttpUtils.isNull(clType) ||
                HttpUtils.isNull(replicaNum)) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
        }else {
            ResultBean resultBean = new ResultBean();
            boolean res = SequoiaDBService.addCL("172.20.0.81","sdbadmin","sdbadmin", csName, clName,
                    clType, sharedingKey, replicaNum, resultBean);
            if(res) {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
                json.put(FixHeader.HEADER_RET_INFO, new JsonObject());
            }else {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
                json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
            }

        }

        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "delCL", name = "delCL", auth = false, bwswitch = false)
    public static void delCL(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);
        String clName = params.get("CL_NAME");
        if(HttpUtils.isNull(clName)) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
        }else {
            ResultBean resultBean = new ResultBean();
            String[] clNames = clName.split("\\.");
            boolean res = SequoiaDBService.delCL("172.20.0.81","sdbadmin","sdbadmin", clNames[0],
                    clNames[1], resultBean);
            if(res) {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
                json.put(FixHeader.HEADER_RET_INFO, new JsonObject());
            }else {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
                json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
            }

        }

        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "attachCL", name = "attachCL", auth = false, bwswitch = false)
    public static void attachCL(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);
        String mainCLName = params.get("MAIN_CL");
        String clName = params.get("CL_NAME");
        String field = params.get("FIELD");
        String lowBound = params.get("LOW_BOUND");
        String upBound  = params.get("UP_BOUND");

        if(HttpUtils.isNull(mainCLName) && HttpUtils.isNull(clName) && HttpUtils.isNull(field) &&
                HttpUtils.isNull(lowBound) && HttpUtils.isNull(upBound)) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
        }else {
            ResultBean resultBean = new ResultBean();
            boolean res = SequoiaDBService.attachCL("172.20.0.81","sdbadmin","sdbadmin",
                    mainCLName, clName, field, lowBound, upBound, resultBean);
            if(res) {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
                json.put(FixHeader.HEADER_RET_INFO, new JsonObject());
            }else {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
                json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
            }
        }

        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "detachCL", name = "detachCL", auth = false, bwswitch = false)
    public static void detachCL(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);
        String mainCLName = params.get("MAIN_CL");
        String clName = params.get("CL_NAME");
        if(HttpUtils.isNull(mainCLName) && HttpUtils.isNull(clName)) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
        }else {
            ResultBean resultBean = new ResultBean();
            boolean res = SequoiaDBService.detachCL("172.20.0.81","sdbadmin","sdbadmin",
                    mainCLName, clName, resultBean);
            if(res) {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
                json.put(FixHeader.HEADER_RET_INFO, new JsonObject());
            }else {
                json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
                json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
            }
        }

        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "listDomains", name = "listDomains", auth = false, bwswitch = false)
    public static void listDomains(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);

        JsonArray res = SequoiaDBService.listDomains("172.20.0.81","sdbadmin","sdbadmin");
        if(res != null) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
            json.put(FixHeader.HEADER_RET_INFO, res);
        }else {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, new JsonArray());
        }


        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "addDomain", name = "addDomain", auth = false, bwswitch = false)
    public static void addDomain(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);

        String domainName = params.get("DOMAIN_NAME");
        ResultBean resultBean = new ResultBean();

        boolean res = SequoiaDBService.createDomain("172.20.0.81","sdbadmin","sdbadmin", domainName, resultBean);
        if(res) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
            json.put(FixHeader.HEADER_RET_INFO, "");
        }else {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
        }

        HttpUtils.outJsonObject(routeContext, json);
    }

    @Service(id = "dropDomain", name = "dropDomain", auth = false, bwswitch = false)
    public static void dropDomain(RoutingContext routeContext) throws Exception {
        JsonObject json = new JsonObject();

        Map<String, String> params = HttpUtils.getParamForMap(routeContext);

        String domainName = params.get("DOMAIN_NAME");
        ResultBean resultBean = new ResultBean();

        boolean res = SequoiaDBService.dropDomain("172.20.0.81","sdbadmin","sdbadmin", domainName, resultBean);
        if(res) {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
            json.put(FixHeader.HEADER_RET_INFO, "");
        }else {
            json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
            json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
        }

        HttpUtils.outJsonObject(routeContext, json);
    }


}
