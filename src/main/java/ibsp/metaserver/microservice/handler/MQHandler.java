package ibsp.metaserver.microservice.handler;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.MQService;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.SRandomGenerator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

@App(path = "/mqsvr")
public class MQHandler {
	
	@Service(id = "getQueueList", name = "getQueueList", auth = true, bwswitch = true)
	public static void getQueueList(RoutingContext routeContext) {
		JsonArray jsonarray = null;
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		ResultBean resultBean = new ResultBean();
		JsonObject json = new JsonObject();

		jsonarray = MQService.getQueueList(params, resultBean);
		
		for (int i=0; i<jsonarray.size(); i++) {
			JsonObject queue = jsonarray.getJsonObject(i);
			queue.put("C_SHOW", "0");
			if (queue.getString(FixHeader.HEADER_QUEUE_TYPE).equals(CONSTS.TYPE_TOPIC) &&
					MetaData.get().hasPermnentTopicByQueueId(queue.getString(FixHeader.HEADER_QUEUE_ID))) {
				queue.put("C_SHOW", "1");
			}
		}
		
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
	
	@Service(id = "releaseQueue", name = "releaseQueue", auth = true, bwswitch = true)
	public static void releaseQueue(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		MQService.releaseQueue(params, resultBean);
		
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
		json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "createQueueByClient", name = "createQueueByClient", auth = true, bwswitch = true)
	public static void createQueueByClient(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		MQService.createQueueByClient(params, resultBean);
		
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
		json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "deleteQueueByClient", name = "deleteQueueByClient", auth = true, bwswitch = true)
	public static void deleteQueueByClient(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		MQService.deleteQueueByClient(params, resultBean);
		
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
		json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getPermnentTopicList", name = "getPermnentTopicList", auth = true, bwswitch = true)
	public static void getPermnentTopicList(RoutingContext routeContext){
		ResultBean resultBean = new ResultBean();
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonArray jsonarray = MQService.getPermnentTopicList(params, resultBean);

		if(resultBean.getRetCode() == CONSTS.REVOKE_NOK) {
			json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
			json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		}else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, jsonarray);
		}
		
		HttpUtils.outJsonObject(routeContext, json);
		
	}
	
	@Service(id = "getPermnentTopicCount", name="getPermnentTopicCount", auth = true, bwswitch = true)
	public static void getPermnentTopicCount(RoutingContext routeContext){
		ResultBean resultBean = new ResultBean();
		JsonObject json = new JsonObject();
		
		Map<String,String> params = HttpUtils.getParamForMap(routeContext);
		JsonObject countJson = MQService.getPermnentTopicCount(params, resultBean);

		if(resultBean.getRetCode() == CONSTS.REVOKE_NOK) {
			json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
			json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		}else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, countJson);
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "savePermnentTopic", name = "savePermnentTopic", auth = true, bwswitch = true)
	public static void savePermnentTopic(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		String realQueue = MQService.savePermnentTopic(params,resultBean);
		JsonObject json = new JsonObject();
		
		if (realQueue != null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, realQueue);
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "delPermnentTopic", name = "delPermnentTopic", auth = true, bwswitch = true)
	public static void delPermnentTopic(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		MQService.delPermnentTopic(params, resultBean);
		
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_RET_CODE, resultBean.getRetCode());
		json.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getQueueByName", name = "getQueueByName", auth = true, bwswitch = true)
	public static void getQueueByName(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonObject jsonObject = new JsonObject();
		
		if (params == null) {
			jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			jsonObject.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String queueName = params.get(FixHeader.HEADER_QUEUE_NAME);
			if (!HttpUtils.isNotNull(queueName)) {
				jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				jsonObject.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				JsonObject queue = MQService.getQueueByName(queueName, resultBean);
				if (queue != null) {
					jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					jsonObject.put(FixHeader.HEADER_RET_INFO, queue);
				} else {
					jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					jsonObject.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
				}
			}
		}
		HttpUtils.outJsonObject(routeContext, jsonObject);
	}
	
	@Service(id = "getBrokersByQName", name = "getBrokersByQName", auth = true, bwswitch = true)
	public static void getBrokersByQName(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonObject jsonObject = new JsonObject();
		
		if (params == null) {
			jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			jsonObject.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String queueName = params.get(FixHeader.HEADER_QUEUE_NAME);
			if (!HttpUtils.isNotNull(queueName)) {
				jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				jsonObject.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				JsonObject group = MQService.getBrokersByQName(queueName, resultBean);
				if (group != null) {
					jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					jsonObject.put(FixHeader.HEADER_RET_INFO, group);
				} else {
					jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					jsonObject.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
				}
			}
		}
		HttpUtils.outJsonObject(routeContext, jsonObject);
	}
	
	@Service(id = "getPermnentTopic", name = "getPermnentTopic", auth = true, bwswitch = true)
	public static void getPermnentTopic(RoutingContext routeContext) {
		ResultBean resultBean = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonObject jsonObject = new JsonObject();
		
		if (params == null) {
			jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			jsonObject.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String consumerID = params.get(FixHeader.HEADER_CONSUMER_ID);
			if (!HttpUtils.isNotNull(consumerID)) {
				jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				jsonObject.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				JsonObject topic = MQService.getPermTopicAsJson(consumerID, resultBean);
				if (topic != null) {
					jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
					jsonObject.put(FixHeader.HEADER_RET_INFO, topic);
				} else {
					jsonObject.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
					jsonObject.put(FixHeader.HEADER_RET_INFO, resultBean.getRetInfo());
				}
			}
		}
		HttpUtils.outJsonObject(routeContext, jsonObject);
	}
	
	@Service(id = "genConsumerID", name = "genConsumerID", auth = true, bwswitch = true)
	public static void genConsumerID(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
		json.put(FixHeader.HEADER_RET_INFO, "");
		json.put(FixHeader.HEADER_CONSUMER_ID, SRandomGenerator.genConsumerID());
		HttpUtils.outMessage(routeContext, json.toString());
	}
}
