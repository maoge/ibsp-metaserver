package ibsp.metaserver.autodeploy;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.DES3;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.UUIDUtils;
import io.vertx.core.json.JsonObject;

public class DeployServiceFactory {
	
	protected static final Logger logger = LoggerFactory.getLogger(DeployServiceFactory.class);
	
	public static Map<String, Class<?>> DEPLOY_FACTORY;
	
	private static final String INS_SERVICE       = "insert into t_service(INST_ID,SERV_NAME,SERV_TYPE,IS_DEPLOYED,IS_PRODUCT,CREATE_TIME,USER,PASSWORD) "
            + "values(?,?,?,?,?,?,?,?)";
	private static final String MOD_SERVICE       = "update t_service set SERV_NAME=?, IS_PRODUCT=? where INST_ID=?";
	
	static {
		DEPLOY_FACTORY = new HashMap<String, Class<?>>();
		DEPLOY_FACTORY.put(CONSTS.SERV_TYPE_MQ,    MQDeployer.class);
		DEPLOY_FACTORY.put(CONSTS.SERV_TYPE_CACHE, CacheDeployer.class);
		DEPLOY_FACTORY.put(CONSTS.SERV_TYPE_DB,    TiDBDeployer.class);
		DEPLOY_FACTORY.put(CONSTS.SERV_TYPE_SEQUOIADB, SequoiaDBDeployer.class);
	}
	
	public static boolean deployService(String serviceID, String sessionKey, ResultBean result) {
		ServiceBean service = MetaDataService.getService(serviceID, result);
		if (service == null) {
			String err = String.format("service not found, id:%s", serviceID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		if (service.getDeployed().equals(CONSTS.DEPLOYED)) {
			String err = String.format("service is deployed, id:%s", serviceID);
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
		String user = service.getUser();
		String pwd = service.getPassword();
		
		try {
			// Deployer o = (Deployer) clazz.newInstance();
			Deployer o = (Deployer) clazz.getDeclaredConstructor().newInstance();
			res = o.deployService(serviceID, user, pwd, sessionKey, result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
		
		return res;
	}
	
	public static boolean undeployService(String serviceID, String sessionKey, ResultBean result) {
		ServiceBean service = MetaDataService.getService(serviceID, result);
		if (service == null) {
			String err = String.format("service not found, id:%s", serviceID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		if (service.getDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String err = String.format("service is not deployed, id:%s", serviceID);
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
			// Deployer o = (Deployer) clazz.newInstance();
			Deployer o = (Deployer) clazz.getDeclaredConstructor().newInstance();
			res = o.undeployService(serviceID, sessionKey, result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
		
		return res;
	}
	
	public static boolean deployInstance(String serviceID, String instanceID,
			String sessionKey, ResultBean result) {
		
		ServiceBean service = MetaDataService.getService(serviceID, result);
		if (service == null) {
			String err = String.format("service not found, id:%s", serviceID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		if (service.getDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String err = String.format("service is not deployed, id:%s", serviceID);
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
			// Deployer o = (Deployer) clazz.newInstance();
			Deployer o = (Deployer) clazz.getDeclaredConstructor().newInstance();
			res = o.deployInstance(serviceID, instanceID, sessionKey, result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
		
		return res;
	}
	
	public static boolean undeployInstance(String serviceID, String instanceID,
			String sessionKey, ResultBean result) {
		
		ServiceBean service = MetaDataService.getService(serviceID, result);
		if (service == null) {
			String err = String.format("service not found, id:%s", serviceID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		if (service.getDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String err = String.format("service is not deployed, id:%s", serviceID);
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
			// Deployer o = (Deployer) clazz.newInstance();
			Deployer o = (Deployer) clazz.getDeclaredConstructor().newInstance();
			res = o.undeployInstance(serviceID, instanceID, sessionKey, result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
		
		return res;
	}

	public static boolean forceUndeployInstance(String serviceID, String instanceID, ResultBean result) {

		ServiceBean service = MetaDataService.getService(serviceID, result);
		if (service == null) {
			String err = String.format("service not found, id:%s", serviceID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}

		if (service.getDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String err = String.format("service is not deployed, id:%s", serviceID);
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
			// Deployer o = (Deployer) clazz.newInstance();
			Deployer o = (Deployer) clazz.getDeclaredConstructor().newInstance();
			res = o.forceUndeployInstance(serviceID, instanceID,  result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}

		return res;
	}

	public static boolean addOrModifyService(Map<String, String> params, ResultBean result) {
		
		if (params != null) {
			boolean isAddService = false;
			String serviceID   = params.get("SERVICE_ID");
			String serviceName = params.get("SERVICE_NAME");
			String serviceType = params.get("SERVICE_TYPE");
			String isProduct   = params.get("IS_PRODUCT");

			if (HttpUtils.isNull(serviceName) || HttpUtils.isNull(serviceType) || HttpUtils.isNull(isProduct)) {
				result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return false;
			}
			
			CRUD curd = new CRUD();
			if (HttpUtils.isNull(serviceID)) {
				isAddService = true;
				serviceID = UUIDUtils.genUUID();
				long dt = System.currentTimeMillis();
				Object[] sqlParams = new Object[] {serviceID, serviceName, serviceType, 
						CONSTS.NOT_DEPLOYED, isProduct, dt, null, null};
			
				//set db root password
				switch (serviceType) {
					case CONSTS.SERV_TYPE_DB:
						String pwd = UUIDUtils.genUUID();
						sqlParams[sqlParams.length-1] = DES3.encrypt(pwd.substring(pwd.lastIndexOf("-")+1));
						sqlParams[sqlParams.length-2] = "root";
						break;
					case CONSTS.SERV_TYPE_MQ:
						//TODO
						break;
					case CONSTS.SERV_TYPE_CACHE:
						//TODO
						break;
					default:
						break;

				}

				SqlBean sqlServBean = new SqlBean(INS_SERVICE);
				sqlServBean.addParams(sqlParams);
				curd.putSqlBean(sqlServBean);
			} else {
				Object[] sqlParams = new Object[] {serviceName, isProduct, serviceID};
				SqlBean sqlServBean = new SqlBean(MOD_SERVICE);
				sqlServBean.addParams(sqlParams);
				curd.putSqlBean(sqlServBean);
			}

			try {
				boolean res = curd.executeUpdate(true, result);
				//publish event e6 or e7
				if (res) {
					JsonObject evJson = new JsonObject();
					evJson.put("INST_ID", serviceID);
					
					EventBean ev = null;
					if (isAddService) {
						ev = new EventBean(EventType.e6);
					} else {
						ev = new EventBean(EventType.e7);
					}
					ev.setUuid(MetaData.get().getUUID());
					ev.setJsonStr(evJson.toString());	
					EventBusMsg.publishEvent(ev);
				}
				return res;
			} catch (Exception e) {
				result.setRetInfo(e.getMessage());
				return false;
			}
		} else {
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return false;
		}
	}
	
	public static boolean deleteService(String serviceID, String sessionKey, ResultBean result) {
		ServiceBean service = MetaDataService.getService(serviceID, result);
		if (service == null) {
			String err = String.format("service not found, id:%s", serviceID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		if (service.getDeployed().equals(CONSTS.DEPLOYED)) {
			String err = String.format("service id:%s is deployed, can not delete", serviceID);
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
			// Deployer o = (Deployer) clazz.newInstance();
			Deployer o = (Deployer) clazz.getDeclaredConstructor().newInstance();
			res = o.deleteService(serviceID, sessionKey, result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
		
		return res;
	}

}
