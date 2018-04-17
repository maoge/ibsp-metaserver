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
import io.vertx.core.json.JsonArray;
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
					json.put(FixHeader.HEADER_RET_CODE, result.getRetCode());
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
			String sOperType = params.get(FixHeader.HEADER_OP_TYPE);
			if (!HttpUtils.isNotNull(sNodeJson)
					|| !HttpUtils.isNotNull(sParentID)
					|| !HttpUtils.isNotNull(sOperType)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				if (ConfigDataService.saveServiceNode(sParentID, sOperType, sNodeJson, result)) {
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
	
	@Service(id = "delServiceNode", name = "delServiceNode", auth = true, bwswitch = true)
	public static void delServiceNode(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String sParentID = params.get(FixHeader.HEADER_PARENT_ID);
			String sInstID = params.get(FixHeader.HEADER_INSTANCE_ID);
			if (!HttpUtils.isNotNull(sParentID) || !HttpUtils.isNotNull(sInstID)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				if (ConfigDataService.delServiceNode(sParentID, sInstID, result)) {
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
	
	@Service(id = "getServiceList", name = "getServiceList", auth = true, bwswitch = true)
	public static void getServiceList(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonArray serviceList = ConfigDataService.getServiceList(params, result);
//		HttpUtils.outJsonArray(routeContext, serviceList);
		
		if (serviceList != null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, serviceList);
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getServiceCount", name = "getServiceCount", auth = true, bwswitch = true)
	public static void getServiceCount(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonObject serviceCount = ConfigDataService.getServiceCount(params, result);
//		HttpUtils.outJsonObject(routeContext, serviceCount);
		
		if (serviceCount != null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, serviceCount);
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getTreeMetaDataByInstId", name = "getTreeMetaDataByInstId", auth = true, bwswitch = true)
	public static void getTreeMetaDataByInstId(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String sInstID = params.get(FixHeader.HEADER_INSTANCE_ID);
			if (!HttpUtils.isNotNull(sInstID)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				JsonArray arr = ConfigDataService.getTreeMetaDataByInstId(sInstID, result);
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
				json.put(FixHeader.HEADER_RET_INFO, arr);
			}			
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getMetaDataByInstId", name = "getMetaDataByInstId", auth = true, bwswitch = true)
	public static void getMetaDataByInstId(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String sInstID = params.get(FixHeader.HEADER_INSTANCE_ID);
			if (!HttpUtils.isNotNull(sInstID)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				JsonObject res = ConfigDataService.getMetaDataByInstId(sInstID, result);
				if (res != null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, res);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_METADATA_NOT_FOUND);
				}
			}
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
}
