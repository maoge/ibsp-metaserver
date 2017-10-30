package ibsp.metaserver.microservice.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

@App(path = "/metasvr")
public class MetaServerHandler {
	
	private static Logger logger = LoggerFactory.getLogger(MetaServerHandler.class);
	
	@Service(id = "test", name = "test", auth = false, bwswitch = false)
	public static void test(RoutingContext routeContext) {
		HttpServerRequest  req  = routeContext.request();
		
		JsonObject json = new JsonObject();
		
		if (req != null) {
			SocketAddress remoteAddr = req.remoteAddress();
			SocketAddress localAddr  = req.localAddress();
			
			json.put(FixHeader.HEADER_RET_CODE,    CONSTS.REVOKE_OK);
			json.put(FixHeader.HEADER_RET_INFO,    "");
			json.put(FixHeader.HEADER_REMOTE_IP,   remoteAddr.host());
			json.put(FixHeader.HEADER_REMOTE_PORT, remoteAddr.port());
			json.put(FixHeader.HEADER_LOCAL_IP,    localAddr.host());
			json.put(FixHeader.HEADER_LOCAL_PORT,  localAddr.port());
			
			logger.debug("respond:{}", json.toString());
		} else {
			json.put(FixHeader.HEADER_RET_CODE,    CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO,    "HttpServerRequest null.");
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}
	
	@Service(id = "testDB", name = "testDB", auth = false, bwswitch = false)
	public static void testDB(RoutingContext routeContext) {
		HttpServerRequest  req  = routeContext.request();
		JsonObject json = new JsonObject();
		
		if (req != null) {
			boolean ret = MetaDataService.testDB();
			json.put(FixHeader.HEADER_RET_CODE,    ret ? CONSTS.REVOKE_OK : CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO,    ret ? "" : "db query error!");
		} else {
			json.put(FixHeader.HEADER_RET_CODE,    CONSTS.REVOKE_NOK);
			json.put(FixHeader.HEADER_RET_INFO,    "HttpServerRequest null.");
		}
		
		HttpUtils.outJsonObject(routeContext, json);
	}

}
