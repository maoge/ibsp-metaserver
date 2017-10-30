package ibsp.metaserver.microservice.handler;

import java.util.Map;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.TiDBService;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@App(path = "/tidbsvr")
public class TiDBServerHanlder {
	
	@Service(id = "saveTiDBTopo", name = "saveTiDBTopo", auth = true, bwswitch = true)
	public static void saveTiDBTopo(RoutingContext routeContext) {
		JsonObject json = new JsonObject();
		
		Map<String, String> params = HttpUtils.getParamForMap(routeContext);
		if(params == null) {
			json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
		} else {
			String sTiDBJson  = params != null ? params.get(FixHeader.HEADER_TIDB_JSON) : null;
			if (!HttpUtils.isNotNull(sTiDBJson)) {
				json.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
				json.put(FixHeader.HEADER_RET_INFO, CONSTS.ERR_PARAM_INCOMPLETE);
			} else {
				ResultBean result = new ResultBean();
				if (TiDBService.saveTiDBTopo(sTiDBJson, result)) {
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
