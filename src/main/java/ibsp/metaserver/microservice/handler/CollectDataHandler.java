package ibsp.metaserver.microservice.handler;

import java.util.Map;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.MQService;
import ibsp.metaserver.dbservice.QuotaDataService;
import ibsp.metaserver.global.MonitorData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@App(path = "/collectdata")
public class CollectDataHandler {

	@Service(id = "getCurrCollectData", name = "getCurrCollectData", auth = false, bwswitch = false)
	public static void getCurrCollectData(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String instID  = params.get(FixHeader.HEADER_INSTANCE_ID);
			if (HttpUtils.isNull(instID)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				JsonObject collectDataJson = QuotaDataService.getCurrCollectData(instID);
				if (collectDataJson != null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, collectDataJson);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					json.put(FixHeader.HEADER_RET_INFO, "get current collect data error!");
				}
			}
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getHisCollectData", name = "getHisCollectData", auth = false, bwswitch = false)
	public static void getHisCollectData(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String instID  = params.get(FixHeader.HEADER_INSTANCE_ID);
			String sStartTS = params.get(FixHeader.HEADER_START_TS);
			String sEndTS = params.get(FixHeader.HEADER_END_TS);
			if (HttpUtils.isNull(instID) || HttpUtils.isNull(sStartTS)
					|| HttpUtils.isNull(sEndTS)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				Long startTS = Long.valueOf(sStartTS);
				Long endTS = Long.valueOf(sEndTS);
				
				JsonArray hisDataArr = QuotaDataService.getHisCollectData(instID, startTS, endTS);
				if (hisDataArr != null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, hisDataArr);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, "get history collect data error!");
				}
			}
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}

	@Service(id = "getVbrokerCurrentData", name = "getVbrokerCurrentData", auth = false, bwswitch = false)
	public static void getVbrokerCurrentData(RoutingContext routeContext) {
		JsonObject json = new JsonObject();

		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String servId  = params.get(FixHeader.HEADER_SERV_ID);
			if (HttpUtils.isNull(servId)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				JsonArray collectData = MonitorData.get().getVbrokerCollectData(servId);
				if (collectData != null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, collectData);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, "get vbroker collect data error!");
				}
			}
		}

		HttpUtils.outJsonObject(routeContext, json);
	}

	@Service(id = "getQueueCurrentData", name = "getQueueCurrentData", auth = false, bwswitch = false)
	public static void getQueueCurrentData(RoutingContext routeContext) {
		JsonObject json = new JsonObject();

		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String servId  = params.get(FixHeader.HEADER_SERV_ID);
			if (HttpUtils.isNull(servId)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				JsonArray collectData = MonitorData.get().getQueueCollectData(servId);
				if (collectData != null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, collectData);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, "get vbroker collect data error!");
				}
			}
		}

		HttpUtils.outJsonObject(routeContext, json);
	}

	@Service(id = "getVbrokerHisData", name = "getVbrokerHisData", auth = false, bwswitch = false)
	public static void getVbrokerHisData(RoutingContext routeContext) {
		JsonObject json = new JsonObject();

		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String instId  = params.get(FixHeader.HEADER_INSTANCE_ID);
			String sStartTS = params.get(FixHeader.HEADER_START_TS);
			String sEndTS = params.get(FixHeader.HEADER_END_TS);
			if (HttpUtils.isNull(instId)  || HttpUtils.isNull(sStartTS)
					|| HttpUtils.isNull(sEndTS)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				JsonArray collectData = MQService.getVbrokerHisData(instId, Long.valueOf(sStartTS), Long.valueOf(sEndTS), result);
				if (collectData != null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, collectData);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
				}
			}
		}

		HttpUtils.outJsonObject(routeContext, json);
	}

	@Service(id = "getQueueHisData", name = "getQueueHisData", auth = false, bwswitch = false)
	public static void getQueueHisData(RoutingContext routeContext) {
		JsonObject json = new JsonObject();

		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String queueId  = params.get(FixHeader.HEADER_QUEUE_ID);
			String sStartTS = params.get(FixHeader.HEADER_START_TS);
			String sEndTS = params.get(FixHeader.HEADER_END_TS);
			if (HttpUtils.isNull(queueId)  || HttpUtils.isNull(sStartTS)
					|| HttpUtils.isNull(sEndTS)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				JsonArray collectData = MQService.getQueueHisData(queueId, Long.valueOf(sStartTS), Long.valueOf(sEndTS), result);
				if (collectData != null) {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, collectData);
				} else {
					json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
				}
			}
		}

		HttpUtils.outJsonObject(routeContext, json);
	}
}
