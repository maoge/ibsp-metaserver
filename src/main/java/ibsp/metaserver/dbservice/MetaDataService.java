package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.CollectQuotaBean;
import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.InstanceRelationBean;
import ibsp.metaserver.bean.MetaAttributeBean;
import ibsp.metaserver.bean.MetaComponentBean;
import ibsp.metaserver.bean.RelationBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.bean.TopologyBean;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.exception.CRUDException;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.schema.Validator;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaDataService {
	
	private static Logger logger = LoggerFactory.getLogger(MetaDataService.class);
	private static Map<String, String> SERVICE_TYPE_MAPPER = null;
	
	private static final String SEL_TOE = "SELECT t1.INST_ID2 as INST_ID, t2.CMPT_ID as CMPT_ID FROM t_topology t1, t_instance t2 "
            +  "WHERE t1.INST_ID1 = ? AND t1.INST_ID2 = t2.INST_ID";
	
	private static final String SEL_INSTANCE = "SELECT INST_ID,CMPT_ID,IS_DEPLOYED,POS_X,POS_Y,WIDTH,HEIGHT,ROW,COL "
			+ "FROM t_instance WHERE INST_ID = ?";

	private static final String SEL_ATTRIBUTE = "SELECT INST_ID,ATTR_ID,ATTR_NAME,ATTR_VALUE "
			+ "FROM t_instance_attr WHERE INST_ID = ?";
	
	private static final String SEL_DEPLOY_FILE   = "SELECT FILE_TYPE,FILE_NAME,FILE_DIR,IP_ADDRESS,USER_NAME,USER_PWD,FTP_PORT "
            + "FROM t_file_deploy t1, t_ftp_host t2 "
            + "WHERE t1.HOST_ID = t2.HOST_ID";
	
	private static final String SEL_ALL_INTANCE = "SELECT INST_ID,CMPT_ID,IS_DEPLOYED,POS_X,POS_Y,WIDTH,HEIGHT,ROW,COL FROM t_instance";
	private static final String SEL_ALL_INTANCE_ATTR = "SELECT INST_ID, ATTR_ID, ATTR_NAME, ATTR_VALUE from t_instance_attr";
	
	private static final String DEL_INSTANCE = "delete from t_instance where INST_ID = ?";
	private static final String DEL_INSTANCE_ATTR = "delete from t_instance_attr where INST_ID = ?";
	private static final String DEL_TOPOLOGY = "delete from t_topology where INST_ID2 = ?";
	private static final String DEL_SERVICE = "delete from t_service where INST_ID = ?";
	
	static {
		SERVICE_TYPE_MAPPER = new ConcurrentHashMap<String, String>();
		SERVICE_TYPE_MAPPER.put(CONSTS.SERV_TYPE_DB, "tidb");
		SERVICE_TYPE_MAPPER.put(CONSTS.SERV_TYPE_MQ, "mq");
		SERVICE_TYPE_MAPPER.put(CONSTS.SERV_TYPE_CACHE, "cache");
	}
	
	public static boolean testDB() {
		String sql = "select 1 from dual";
		boolean ret = false;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null) {
				ret = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return ret;
	}
	
	public static boolean deleteInstance(String parentID, String instID, ResultBean result) {
		boolean res = false;
		
		try {
			CRUD c = new CRUD();
			
			SqlBean sqlBean1 = new SqlBean(DEL_INSTANCE);
			sqlBean1.addParams(new Object[] { instID });
			c.putSqlBean(sqlBean1);
			
			SqlBean sqlBean2 = new SqlBean(DEL_INSTANCE_ATTR);
			sqlBean2.addParams(new Object[] { instID });
			c.putSqlBean(sqlBean2);
			
			SqlBean sqlBean3 = new SqlBean(DEL_TOPOLOGY);
			sqlBean3.addParams(new Object[] { instID });
			c.putSqlBean(sqlBean3);
			
			res = c.executeUpdate();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		}
		
		if (res) {
			JsonObject ev1Json = new JsonObject();
			ev1Json.put("INST_ID", instID);
			EventBean ev1 = new EventBean(EventType.e5);
			ev1.setUuid(MetaData.get().getUUID());
			ev1.setJsonStr(ev1Json.toString());
			EventBusMsg.publishEvent(ev1);
			
			JsonObject ev2Json = new JsonObject();
			ev2Json.put("INST_ID1", parentID);
			ev2Json.put("INST_ID2", instID);
			EventBean ev2 = new EventBean(EventType.e2);
			ev1.setUuid(MetaData.get().getUUID());
			ev1.setJsonStr(ev2Json.toString());
			EventBusMsg.publishEvent(ev2);
		}
		
		return res;
	}
	
	public static boolean deleteService(String instID, ResultBean result) {
		boolean res = false;
		
		try {
			CRUD c = new CRUD();
			
			SqlBean sqlBean = new SqlBean(DEL_SERVICE);
			sqlBean.addParams(new Object[] { instID });
			c.putSqlBean(sqlBean);
			
			res = c.executeUpdate();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		}
		
		if (res) {
			JsonObject evJson = new JsonObject();
			evJson.put("INST_ID", instID);
			
			EventBean ev = new EventBean(EventType.e8);
			ev.setUuid(MetaData.get().getUUID());
			ev.setJsonStr(evJson.toString());
			EventBusMsg.publishEvent(ev);
		}
		
		return res;
	}
	
	public static List<MetaAttributeBean> getAllMetaAttribute() {
		String sql = "select ATTR_ID, ATTR_NAME, ATTR_NAME_CN, AUTO_GEN from t_meta_attr";
		List<MetaAttributeBean> metaAttrList = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null) {
				metaAttrList = new LinkedList<MetaAttributeBean>();
				
				for (HashMap<String, Object> item : resultList) {
					MetaAttributeBean metaAttr = MetaAttributeBean.convert(item);
					metaAttrList.add(metaAttr);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return metaAttrList;
	}
	
	public static List<MetaComponentBean> getAllMetaComponent() {
		String sql = "select CMPT_ID,CMPT_NAME,CMPT_NAME_CN,IS_NEED_DEPLOY,SERV_CLAZZ,SERV_TYPE,SUB_SERV_TYPE from t_meta_cmpt";
		List<MetaComponentBean> metaCmptList = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null) {
				metaCmptList = new LinkedList<MetaComponentBean>();
				
				for (HashMap<String, Object> item : resultList) {
					MetaComponentBean metaCmpt = MetaComponentBean.convert(item);
					metaCmptList.add(metaCmpt);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return metaCmptList;
	}
	
	public static List<RelationBean> getAllCmpt2Attr() {
		String sql = "select CMPT_ID as MASTER_ID, ATTR_ID as SLAVE_ID from t_meta_cmpt_attr";
		List<RelationBean> cmpt2AttrList = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null) {
				cmpt2AttrList = new LinkedList<RelationBean>();
				
				for (HashMap<String, Object> item : resultList) {
					RelationBean bean = RelationBean.convert(item);
					cmpt2AttrList.add(bean);
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return cmpt2AttrList;
	}
	
	public static Map<Integer, String> getSubNodesWithType(String parentID, ResultBean result) {
		Map<Integer, String> ret = null;
		
		try {
			SqlBean sqlBean = new SqlBean(SEL_TOE);
			sqlBean.addParams(new Object[] { parentID });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList == null || resultList.size() == 0)
				return null;
			
			ret = new HashMap<Integer, String>();
			for (HashMap<String, Object> item : resultList) {
				Integer cmptID = (Integer) item.get("CMPT_ID");
				String instID = (String) item.get("INST_ID");
				ret.put(cmptID, instID);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
		
		return ret;
	}
	
	public static Set<String> getSubNodes(String parentID, ResultBean result) {
		Set<String> ret = null;
		
		try {
			SqlBean sqlBean = new SqlBean(SEL_TOE);
			sqlBean.addParams(new Object[] { parentID });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList == null || resultList.size() == 0)
				return null;
			
			ret = new HashSet<String>();
			for (HashMap<String, Object> item : resultList) {
				String instID = (String) item.get("INST_ID");
				ret.add(instID);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
		
		return ret;
	}
	
	public static InstanceBean getInstance(String instID, ResultBean result) {
		InstanceBean instance = null;
		
		try {
			SqlBean sqlBean = new SqlBean(SEL_INSTANCE);
			sqlBean.addParams(new Object[] { instID });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			Map<String, Object> resultMap = c.queryForMap();
			if (resultMap != null) {
				instance = InstanceBean.convert(resultMap);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
		
		return instance;
	}
	
	public static Map<String, InstAttributeBean> getAttribute(String instID, ResultBean result) {
		Map<String, InstAttributeBean> attrMap = null;
		
		try {
			SqlBean sqlBean = new SqlBean(SEL_ATTRIBUTE);
			sqlBean.addParams(new Object[] { instID });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> resultList = c.queryForList();
			if (resultList != null && resultList.size() > 0) {
				attrMap = new HashMap<String, InstAttributeBean>();
				for (HashMap<String, Object> item : resultList) {
					InstAttributeBean attrBean = InstAttributeBean.convert(item);
					attrMap.put(attrBean.getAttrName(), attrBean);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
		
		return attrMap;
	}
	
	public static InstanceDtlBean getInstanceDtl(String instID, ResultBean result) {
		InstanceBean instanceBean = MetaDataService.getInstance(instID, result);
		Map<String, InstAttributeBean> instanceAttr = MetaDataService.getAttribute(instID, result);
		if (instanceBean == null || instanceAttr == null) {
			return null;
		}
		
		return new InstanceDtlBean(instanceBean, instanceAttr);
	}
	
	public static ServiceBean getService(String instID) {
		String sql = "select INST_ID, SERV_NAME, SERV_TYPE, IS_DEPLOYED, CREATE_TIME, USER, PASSWORD from t_service where INST_ID = ?";
		ServiceBean serviceBean = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			sqlBean.addParams(new Object[] { instID });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			Map<String, Object> result = c.queryForMap();
			if (result == null) {
				return null;
			}
			
			serviceBean = ServiceBean.convert(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return serviceBean;
	}
	
	public static List<CollectQuotaBean> getAllCollectQuotas() {
		String sql = "select QUOTA_CODE, QUOTA_NAME from t_meta_collect_quota";
		List<CollectQuotaBean> quotas = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> result = c.queryForList();
			if (result == null) {
				return null;
			}
			
			quotas = new LinkedList<CollectQuotaBean>();
			
			for (HashMap<String, Object> item : result) {
				if (item == null)
					continue;
				
				CollectQuotaBean quota = CollectQuotaBean.convert(item);
				quotas.add(quota);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return quotas;
	}
	
	public static List<ServiceBean> getAllDeployedServices() {
		String sql = "select INST_ID, SERV_NAME, SERV_TYPE, IS_DEPLOYED, CREATE_TIME, USER, PASSWORD "
				+ "from t_service where IS_DEPLOYED = ?";
		List<ServiceBean> services = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			sqlBean.addParams(new Object[] { CONSTS.DEPLOYED });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> result = c.queryForList();
			if (result == null) {
				return null;
			}
			
			services = new LinkedList<ServiceBean>();
			
			for (HashMap<String, Object> item : result) {
				if (item == null)
					continue;
				
				ServiceBean service = ServiceBean.convert(item);
				services.add(service);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return services;
	}
	
	public static List<TopologyBean> getAllTopology() {
		String sql = "select INST_ID1, INST_ID2, TOPO_TYPE from t_topology";
		List<TopologyBean> topos = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> result = c.queryForList();
			if (result == null) {
				return null;
			}
			
			topos = new LinkedList<TopologyBean>();
			
			for (HashMap<String, Object> item : result) {
				if (item == null)
					continue;
				
				TopologyBean topo = TopologyBean.convert(item);
				topos.add(topo);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return topos;
	}
	
	public static List<InstanceBean> getAllInstance() {
		List<InstanceBean> instances = null;
		try {
			SqlBean sqlBean = new SqlBean(SEL_ALL_INTANCE);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> listMapping = c.queryForList();
			if (listMapping == null)
				return null;
			
			instances = new LinkedList<InstanceBean>();
			for (HashMap<String, Object> mapping : listMapping) {
				InstanceBean instance = InstanceBean.convert(mapping);
				if (instance == null)
					continue;
				
				instances.add(instance);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return instances;
	}
	
	public static List<InstAttributeBean> getAllInstanceAttribute() {
		List<InstAttributeBean> instAttrs = null;
		try {
			SqlBean sqlBean = new SqlBean(SEL_ALL_INTANCE_ATTR);
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> listMapping = c.queryForList();
			if (listMapping == null)
				return null;
			
			instAttrs = new LinkedList<InstAttributeBean>();
			for (HashMap<String, Object> mapping : listMapping) {
				InstAttributeBean instance = InstAttributeBean.convert(mapping);
				if (instance == null)
					continue;
				
				instAttrs.add(instance);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return instAttrs;
	}
	
	public static InstanceDtlBean getInstanceDtl(String instID) {
		InstanceBean instance = getInstance(instID);
		List<InstAttributeBean> attrs = getInstanceAttribute(instID);
		
		if (instance == null)
			return null;
		
		InstanceDtlBean instDtl = new InstanceDtlBean(instance);
		for (InstAttributeBean attr : attrs) {
			instDtl.addAttribute(attr);
		}
		
		return instDtl;
	}
	
	public static InstanceBean getInstance(String instID) {
		String sql = "select INST_ID, CMPT_ID, IS_DEPLOYED, POS_X, POS_Y, WIDTH, HEIGHT, ROW, COL "
				+ "from t_instance where INST_ID = ?";
		
		InstanceBean instance = null;
		try {
			SqlBean sqlBean = new SqlBean(sql);
			sqlBean.addParams(new Object[] { instID });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			Map<String, Object> mapper = c.queryForMap();
			if (mapper == null)
				return null;
			
			instance = InstanceBean.convert(mapper);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return instance;
	}
	
	public static List<InstAttributeBean> getInstanceAttribute(String instID) {
		String sql = "select INST_ID, ATTR_ID, ATTR_NAME, ATTR_VALUE "
				+ "from t_instance_attr where INST_ID = ?";
		
		List<InstAttributeBean> attrs = null;
		try {
			SqlBean sqlBean = new SqlBean(sql);
			sqlBean.addParams(new Object[] { instID });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> listMapping = c.queryForList();
			if (listMapping == null)
				return null;
			
			attrs = new LinkedList<InstAttributeBean>();
			for (HashMap<String, Object> mapping : listMapping) {
				InstAttributeBean attr = InstAttributeBean.convert(mapping);
				if (attr == null)
					continue;
				
				attrs.add(attr);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return attrs;
	}
	
	private static List<InstanceRelationBean> getInstRelations(String instID) {
		String sql = "select INST_ID1, INST_ID2, TOPO_TYPE from t_topology "
				+ "where (INST_ID1 = ?) or (INST_ID2 = ? and TOPO_TYPE = ?)";
		List<InstanceRelationBean> result = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			sqlBean.addParams(new Object[] { instID, instID, CONSTS.TOPO_TYPE_LINK });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> dataList = c.queryForList();
			if (dataList == null)
				return null;
			
			result = new LinkedList<InstanceRelationBean>();
			for (HashMap<String, Object> mapper : dataList) {
				InstanceRelationBean relation = InstanceRelationBean.convert(mapper);
				result.add(relation);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return result;
	}
	
	private static boolean addInstanceAttribute(String instID, Object json, Map<String, String> skeleton, JsonArray deployFlagArr) {
		ResultBean resultBean = new ResultBean();
		InstanceBean instBean = MetaDataService.getInstance(instID, resultBean);
		if (instBean == null)
			return false;
		
		int cmptID = instBean.getCmptID();
		MetaComponentBean component = MetaData.get().getComponentByID(cmptID);
		if (component == null) {
			return false;
		}
		
		String subNodeType = skeleton.get(component.getCmptName());
		if (subNodeType == null) {
			return false;
		}
		
		// first add attributes of it self
		Object innerJson = null;
		if (subNodeType.equals(CONSTS.SCHEMA_ARRAY)) {
			if ((innerJson = ((JsonObject) json).getValue(component.getCmptName())) == null) {
				innerJson = new JsonArray();
			}
		} else {
			innerJson = new JsonObject();
		}
		
		if (json instanceof JsonArray) {
			((JsonArray) json).add(innerJson);
		} else {
			((JsonObject) json).put(component.getCmptName(), innerJson);
		}
		
		if (!addCurrAttr(instID, instBean, innerJson, deployFlagArr)) {
			return false;
		}
		
		List<InstanceRelationBean> relations = getInstRelations(instID);
		if (relations == null || relations.size() == 0) {
			String subServType = component.getSubServType();
			if (HttpUtils.isNotNull(subServType))
				((JsonObject) innerJson).put(subServType, new JsonArray());
			return true;
		}
		
		// then add attribute of it related nodes by recursively traversal
		for (InstanceRelationBean relation : relations) {
			String toeID = (String) relation.getTOE(instID);
			if (!addInstanceAttribute(toeID, innerJson, skeleton, deployFlagArr)) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean addCurrAttr(String instID, InstanceBean instBean,
			Object json, JsonArray deployFlagArr) {
		
		JsonObject tmpJson = json instanceof JsonObject ? (JsonObject) json : new JsonObject();
		
		// instance component attributes
		List<InstAttributeBean> attrs = getInstanceAttribute(instID);
		if (attrs == null || attrs.size() == 0)
			return false;
		
		for (InstAttributeBean attr : attrs) {
			tmpJson.put(attr.getAttrName(), attr.getAttrValue());
		}
		
		// instance POS
		JsonObject posJson = instBean.getPosAsJson();
		if (posJson != null) {
			tmpJson.put(FixHeader.HEADER_POS, posJson);
		}
		
		if (json instanceof JsonArray)
			((JsonArray) json).add(tmpJson);
		
		JsonObject deployJson = new JsonObject();
		deployJson.put(instID, instBean.getIsDeployed());
		deployFlagArr.add(deployJson);
		
		return true;
	}
	
	public static JsonObject loadServiceTopoByInstID(String instID, ResultBean result) {
		ServiceBean serviceBean = getService(instID);
		if (serviceBean == null)
			return null;
		
		String servType = serviceBean.getServType();
		String name = SERVICE_TYPE_MAPPER.get(servType);
		Map<String, String> skeleton = null;
		JsonObject topoJson = null;
		try {
			skeleton = Validator.getSkeleton(name);
			
			topoJson = new JsonObject();
			JsonArray deployFlagArr = new JsonArray();
			if (!addInstanceAttribute(instID, topoJson, skeleton, deployFlagArr)) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(CONSTS.ERR_METADATA_NOT_FOUND);
				return null;
			}
			topoJson.put(FixHeader.HEADER_DEPLOY_FLAG, deployFlagArr);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
		
		return topoJson;
	}
	
	public static List<DeployFileBean> loadDeployFile() {
		SqlBean sqlInst = new SqlBean(SEL_DEPLOY_FILE);
		
		CRUD curd = new CRUD();
		curd.putSqlBean(sqlInst);
		
		List<DeployFileBean> deployFiles = null;
		
		try {
			List<HashMap<String, Object>> queryList = curd.queryForList();
			if (queryList == null || queryList.isEmpty())
				return null;
			
			deployFiles = new LinkedList<DeployFileBean>();
			
			Iterator<HashMap<String, Object>> it = queryList.iterator();
			while (it.hasNext()) {
				HashMap<String, Object> mapper = it.next();
				DeployFileBean deployFile = DeployFileBean.convert(mapper);
				deployFiles.add(deployFile);
			}
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
		}
		
		return deployFiles;
	}
	
}
