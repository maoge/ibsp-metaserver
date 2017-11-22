package ibsp.metaserver.microservice.handler;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.SqlExplainService;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

@App(path = "/explain")
public class SqlExplainHandler {

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
				JsonObject object = SqlExplainService.explainSql(sql, servID, name, user, pwd, result);
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
}
