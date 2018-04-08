package ibsp.metaserver.microservice.handler;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.MQService;
import ibsp.metaserver.dbservice.TiDBService;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

@App(path = "/mqsvr")
public class MQHandle {

	@Service(id = "sqlExplainService", name = "sqlExplainService", auth = true, bwswitch = true)
	public static void sqlExplainService(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String sql = params.get(FixHeader.HEADER_SQL_STR);
			String servID = params.get(FixHeader.HEADER_SERV_ID);
			String name = params.get(FixHeader.HEADER_SCHEMA_NAME);
			String user = params.get(FixHeader.HEADER_USER_NAME);
			String pwd = params.get(FixHeader.HEADER_USER_PWD);
			if (!HttpUtils.isNotNull(sql) || !HttpUtils.isNotNull(servID) ||
					!HttpUtils.isNotNull(name) || !HttpUtils.isNotNull(user)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				JsonObject object = TiDBService.explainSql(sql, servID, name, user, pwd, result);
				if (object!=null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, object);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
				}
			}
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getQueueList", name = "getQueueList", auth = true, bwswitch = true)
	public static void getQueueList(RoutingContext routeContext) {
		JsonArray jsonarray = null;
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		ResultBean resultBean = new ResultBean();
		JsonObject json = new JsonObject();

		jsonarray = MQService.getQueueList(params, resultBean);
		
		if(resultBean.getRetCode() == CONSTS.REVOKE_NOK) {
			json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
			json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		}else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, jsonarray);
		}
		
		HttpUtils.outJsonObject(routeContext, json);
		
	}
	
	@Service(id = "getQueueListCount", name = "getQueueListCount", auth = true, bwswitch = true)
	public static void getQueueListCount(RoutingContext routeContext) {
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		ResultBean resultBean = new ResultBean();
		JsonObject json = new JsonObject();

		JsonObject countJson = MQService.getQueueListCount(params, resultBean);
		
		if(resultBean.getRetCode() == CONSTS.REVOKE_NOK) {
			json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
			json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		}else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, countJson);
		}
		
		HttpUtils.outJsonObject(routeContext, json);
		
	}
	
	@Service(id = "saveQueue", name = "saveQueue", auth = true, bwswitch = true)
	public static void saveQueue(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		MQService.saveQueue(params,resultBean);
		
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
		json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "delQueue", name = "delQueue", auth = true, bwswitch = true)
	public static void delQueue(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		MQService.delQueue(params, resultBean);
		
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
		json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "releaseQueue", name = "releaseQueue")
	public static void releaseQueue(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		MQService.releaseQueue(params, resultBean);
		
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
		json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		HttpUtils.outJsonObject(routeContext, json);
	}
}
