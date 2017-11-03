package ibsp.metaserver.microservice.handler;

import java.util.Map;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.ConfigDataService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@App(path = "/configsvr")
public class ConfigServerHandler {
	
	@Service(id = "loadServiceTopoByInstID", name = "loadServiceTopoByInstID", auth = true, bwswitch = true)
	public static void loadServiceTopoByInstID(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String instID  = params.get(FixHeader.HEADER_INSTANCE_ID);
			if (!HttpUtils.isNotNull(instID)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				JsonObject topoJson = MetaDataService.loadServiceTopoByInstID(instID, result);
				if (topoJson != null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, topoJson);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
				}
			}
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "saveServiceTopoSkeleton", name = "saveServiceTopoSkeleton", auth = true, bwswitch = true)
	public static void saveServiceTopoSkeleton(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String sTopoJson = params.get(FixHeader.HEADER_TOPO_JSON);
			String sServType = params.get(FixHeader.HEADER_SERV_TYPE);
			if (!HttpUtils.isNotNull(sTopoJson) || !HttpUtils.isNotNull(sServType)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				if (ConfigDataService.saveServiceTopoSkeleton(sTopoJson, sServType, result)) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, "");
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
				}
			}
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "saveServiceNode", name = "saveServiceNode", auth = true, bwswitch = true)
	public static void saveServiceNode(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String sNodeJson = params.get(FixHeader.HEADER_NODE_JSON);
			String sParentID = params.get(FixHeader.HEADER_PARENT_ID);
			if (!HttpUtils.isNotNull(sNodeJson) || !HttpUtils.isNotNull(sParentID)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				if (ConfigDataService.saveServiceNode(sParentID, sNodeJson, result)) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, "");
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
				}
			}
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
}
