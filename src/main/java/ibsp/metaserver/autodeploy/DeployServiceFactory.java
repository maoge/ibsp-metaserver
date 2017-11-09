package ibsp.metaserver.autodeploy;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.utils.CONSTS;

public class DeployServiceFactory {
	
	protected static final Logger logger = LoggerFactory.getLogger(DeployServiceFactory.class);
	
	public static Map<String, Class<?>> DEPLOY_FACTORY;
	
	static {
		DEPLOY_FACTORY = new HashMap<String, Class<?>>();
		DEPLOY_FACTORY.put(CONSTS.SERV_TYPE_MQ,    MQDeployer.class);
		DEPLOY_FACTORY.put(CONSTS.SERV_TYPE_CACHE, CacheDeployer.class);
		DEPLOY_FACTORY.put(CONSTS.SERV_TYPE_DB,    TiDBDeployer.class);
	}
	
	public static boolean deploy(String serviceID, ResultBean result) {
		ServiceBean service = MetaDataService.getService(serviceID);
		if (service == null) {
			String err = String.format("service not found, id:%s", serviceID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		String servType = service.getServType();
		Class<?> clazz = DEPLOY_FACTORY.get(servType);
		if (clazz == null) {
			String err = String.format("service type not found, id:%s", serviceID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		boolean res = false;
		try {
			Deployer o = (Deployer) clazz.newInstance();
			res = o.deployService(serviceID, result);
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
		
		return res;
	}

}
