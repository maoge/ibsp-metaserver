package ibsp.metaserver.microservice.handler;

import java.util.Map;
import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.ResourceDataService;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@App(path = "/resourcesvr")
public class ResourceServerHandler {
	
	@Service(id = "getServerList", name = "getServerList", auth = true, bwswitch = true)
	public static void getServerList(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonArray serverList = ResourceDataService.getServerList(params, result);
		
		if (serverList != null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, serverList);
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getServerCount", name = "getServerCount", auth = true, bwswitch = true)
	public static void getServerCount(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonObject serverCount = ResourceDataService.getServerCount(params, result);
		
		if (serverCount != null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, serverCount);
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "addServer", name = "addServer", auth = true, bwswitch = true)
	public static void addServer(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		
		if (ResourceDataService.addServer(params, result)) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, "");
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "deleteServer", name = "deleteServer", auth = true, bwswitch = true)
	public static void deleteServer(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		
		if (ResourceDataService.deleteServer(params, result)) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, "");
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getSSHListByIP", name = "getSSHListByIP", auth = true, bwswitch = true)
	public static void getSSHListByIP(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonArray SSHList = ResourceDataService.getSSHListByIP(params, result);
		
		if (SSHList != null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, SSHList);
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "getSSHCountByIP", name = "getSSHCountByIP", auth = true, bwswitch = true)
	public static void getSSHCountByIP(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		JsonObject SSHCount = ResourceDataService.getSSHCountByIP(params, result);
		
		if (SSHCount != null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, SSHCount);
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "addOrModifySSH", name = "addOrModifySSH", auth = true, bwswitch = true)
	public static void addOrModifySSH(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		
		if (ResourceDataService.addOrModifySSH(params, result)) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, "");
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "deleteSSH", name = "deleteSSH", auth = true, bwswitch = true)
	public static void deleteSSH(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		ResultBean result = new ResultBean();
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		
		if (ResourceDataService.deleteSSH(params, result)) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO, "");
		} else {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, result.getRetInfo());
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
}
