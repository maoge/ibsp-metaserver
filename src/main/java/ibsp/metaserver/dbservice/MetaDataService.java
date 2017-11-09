package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceRelationBean;
import ibsp.metaserver.bean.MetaAttributeBean;
import ibsp.metaserver.bean.MetaComponentBean;
import ibsp.metaserver.bean.RelationBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.bean.SqlBean;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaDataService {
	
	private static Logger logger = LoggerFactory.getLogger(MetaDataService.class);
	private static Map<String, String> SERVICE_TYPE_MAPPER = null;
	
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
		String sql = "select CMPT_ID,CMPT_NAME,CMPT_NAME_CN,SERV_CLAZZ,SERV_TYPE,SUB_SERV_TYPE from t_meta_cmpt";
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
	
	public static InstanceBean getInstance(String instID) {
		String sql = "select INST_ID, CMPT_ID, POS_X, POS_Y, WIDTH, HEIGHT, ROW, COL from t_instance where INST_ID=?";
		InstanceBean instance = null;
		
		try {
			SqlBean sqlBean = new SqlBean(sql);
			sqlBean.addParams(new Object[] { instID });
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			Map<String, Object> result = c.queryForMap();
			if (result == null) {
				return null;
			}
			
			instance = InstanceBean.convert(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return instance;
	}
	
	private static ServiceBean getService(String instID) {
		String sql = "select INST_ID, SERV_NAME, SERV_TYPE, CREATE_TIME from t_service where INST_ID = ?";
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
	
	private static boolean addInstanceAttribute(String instID, Object json, Map<String, String> skeleton) {
		InstanceBean instBean = getInstance(instID);
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
		
		if (!addCurrAttr(instID, instBean, innerJson)) {
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
			if (!addInstanceAttribute(toeID, innerJson, skeleton)) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean addCurrAttr(String instID, InstanceBean instBean, Object json) {
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
			if (!addInstanceAttribute(instID, topoJson, skeleton)) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(CONSTS.ERR_METADATA_NOT_FOUND);
				return null;
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
		
		return topoJson;
	}
	
}
