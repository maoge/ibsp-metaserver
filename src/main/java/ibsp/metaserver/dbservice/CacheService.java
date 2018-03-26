package ibsp.metaserver.dbservice;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.utils.CONSTS;
import io.vertx.core.json.JsonObject;

public class CacheService {
	
	private static Logger logger = LoggerFactory.getLogger(CacheService.class);
	
	public static JsonObject getProxyInfoByID(String instID, ResultBean result) {
		JsonObject res = new JsonObject();
		
		List<InstAttributeBean> proxy = MetaDataService.getInstanceAttribute(instID);
		if (proxy == null || proxy.size()==0) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("No instance found: "+instID);
			return null;
		}
		
		for (InstAttributeBean attr : proxy) {
			String name = attr.getAttrName();
			switch (name) {
			case "IP":
				res.put("IP", attr.getAttrValue());
				break;
			case "PORT":
				res.put("PORT", attr.getAttrValue());
				break;
			case "STAT_PORT":
				res.put("STAT_PORT", attr.getAttrValue());
				break;
			default:
				break;
			}
			res.put("NAME", instID);
		}
		return res;
	}
	
}
