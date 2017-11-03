package ibsp.metaserver.dbservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import ibsp.metaserver.bean.IdSetBean;
import ibsp.metaserver.bean.MetaAttributeBean;
import ibsp.metaserver.bean.PosBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.schema.Validator;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ConfigDataService {
	
	private static Logger logger = LoggerFactory.getLogger(ConfigDataService.class);
	
	private static Map<String, String> SKELETON_SCHEMA_MAPPER = null;
	private static Map<String, String> SERV_TYPE_NAME_MAPPER  = null;
	
	private static final String NAME_INDEX        = "_NAME";
	private static final String ID_INDEX          = "_ID";
	private static final String CONTAINER_INDEX   = "_CONTAINER";
	
	private static final String INS_INSTANCE      = "insert into t_instance(INST_ID,CMPT_ID,POS_X,POS_Y,WIDTH,HEIGHT,ROW,COL) "
	                                              + "values(?,?,?,?,?,?,?,?)";

	private static final String INS_INSTANCE_ATTR = "insert into t_instance_attr(INST_ID,ATTR_ID,ATTR_NAME,ATTR_VALUE) "
	                                              + "values(?,?,?,?)";
	
	private static final String INS_SERVICE       = "insert into t_service(INST_ID,SERV_NAME,SERV_TYPE,CREATE_TIME) "
	                                              + "values(?,?,?,?)";
	
	private static final String INS_TOPOLOGY      = "insert into t_topology(INST_ID1,INST_ID2,TOPO_TYPE) "
	                                              + "values(?,?,?)";
	
	static {
		SKELETON_SCHEMA_MAPPER = new HashMap<String, String>();
		SKELETON_SCHEMA_MAPPER.put(CONSTS.SERV_TYPE_MQ,    "mq_skeleton");
		SKELETON_SCHEMA_MAPPER.put(CONSTS.SERV_TYPE_CACHE, "cache_skeleton");
		SKELETON_SCHEMA_MAPPER.put(CONSTS.SERV_TYPE_DB,    "tidb_skeleton");
		
		SERV_TYPE_NAME_MAPPER = new HashMap<String, String>();
		SERV_TYPE_NAME_MAPPER.put(CONSTS.SERV_TYPE_MQ,    "MQ_SERV_CONTAINER");
		SERV_TYPE_NAME_MAPPER.put(CONSTS.SERV_TYPE_CACHE, "CACHE_SERV_CONTAINER");
		SERV_TYPE_NAME_MAPPER.put(CONSTS.SERV_TYPE_DB,    "DB_SERV_CONTAINER");
	}
	
	public static boolean saveServiceTopoSkeleton(String sTopoJson, String sServType, ResultBean result) {
		if (!checkSchema(sTopoJson, sServType, result))
			return false;
		
		CRUD curd = new CRUD();
		JsonObject topoJson = new JsonObject(sTopoJson);
		
		if (!addService(topoJson, curd, sServType, result))
			return false;
		
		if (!enumJson(topoJson, curd, result))
			return false;
		
		return curd.executeUpdate(true, result);
	}
	
	public static boolean saveServiceNode(String sParentID, String sNodeJson, ResultBean result) {
		JsonObject nodeJson = new JsonObject(sNodeJson);
		
		CRUD curd = new CRUD();
		
		Iterator<Entry<String, Object>> itNode = nodeJson.iterator();
		while (itNode.hasNext()) {
			Entry<String, Object> entry = itNode.next();
			
			String cmptName = entry.getKey();
			Object subNode = entry.getValue();
			
			Integer cmptID = MetaData.get().getComponentID(cmptName);
			if (cmptID == null) {
				String info = String.format("compoent:%s ID not found ......", cmptName);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(info);
				return false;
			}
			
			if (!(subNode instanceof JsonArray)) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(CONSTS.ERR_JSONNODE_NOT_COMPLETE);
				return false;
			}
			
			JsonArray subJsonArr = (JsonArray) subNode;
			if (subJsonArr.size() == 0)
				continue;
			
			int size = subJsonArr.size();
			for (int i = 0; i < size; i++) {
				JsonObject instanceNode = subJsonArr.getJsonObject(i);
				
				String idAttrName = MetaData.get().getCmptIDAttrNameByName(cmptName);
				if (HttpUtils.isNull(idAttrName)) {
					String info = String.format("compoent:%s ID attribute not found ......", cmptName);
					result.setRetCode(CONSTS.REVOKE_NOK);
					result.setRetInfo(info);
					return false;
				}
				
				String instID = instanceNode.getString(idAttrName);
				
				// add instance
				addInstance(instID, cmptID, curd, instanceNode, result);
				
				// add component attrbute
				addComponentAttribute(instID, cmptID, curd, instanceNode, result);
				
				// add relation
				addRelation(sParentID, cmptID, curd, instanceNode, result);
			}
		}
		
		return curd.executeUpdate(true, result);
	}
	
	private static boolean addInstance(String instID, Integer cmptID,
			CRUD curd, JsonObject nodeJson, ResultBean result) {
		
		PosBean pos = new PosBean();
		getPos(nodeJson, pos);
		
		// add instance
		SqlBean sqlInst = new SqlBean(INS_INSTANCE);
		sqlInst.addParams(new Object[]{instID, cmptID, pos.getX(), pos.getY(),
				pos.getWidth(), pos.getHeight(), pos.getRow(), pos.getCol()});
		curd.putSqlBean(sqlInst);
		
		return true;
	}
	
	private static boolean addComponentAttribute(String instID, Integer cmptID,
			CRUD curd, JsonObject nodeJson, ResultBean result) {
		
		IdSetBean<Integer> attrIdSet = MetaData.get().getAttrIdSet(cmptID);
		Iterator<Integer> it = attrIdSet.iterator();
		while (it.hasNext()) {
			Integer attrID = it.next();
			MetaAttributeBean metaAttr = MetaData.get().getAttributeByID(attrID);
			String attrName = metaAttr.getAttrName();
			String attrValue = nodeJson.getString(attrName);
			
			SqlBean sqlAttr = new SqlBean(INS_INSTANCE_ATTR);
			sqlAttr.addParams(new Object[]{instID, attrID, attrName, attrValue});
			curd.putSqlBean(sqlAttr);
		}
		
		return true;
	}
	
	private static boolean addRelation(String instID1, Integer cmptID,
			CRUD curd, JsonObject nodeJson, ResultBean result) {
		
		String subCmptIDAttrName = MetaData.get().getCmptIDAttrNameByID(cmptID);
		String instID2 = nodeJson.getString(subCmptIDAttrName);
		
		if (HttpUtils.isNotNull(instID2)) {
			SqlBean sqlTopo = new SqlBean(INS_TOPOLOGY);
			sqlTopo.addParams(new Object[] { instID1, instID2, CONSTS.TOPO_TYPE_CONTAIN });
			curd.putSqlBean(sqlTopo);
		}
		
		return true;
	}
	
	private static boolean enumJson(Object json, CRUD curd, ResultBean result) {
		if (json instanceof JsonObject) {
			Iterator<Entry<String, Object>> it = ((JsonObject) json).iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				if (entry == null)
					return false;
				
				String key = entry.getKey();
				Object val = entry.getValue();
				if (val == null)
					return false;
				 
				if (val instanceof JsonObject && !key.equals(FixHeader.HEADER_POS)) {
					if (!addComponentAttrbuteAndRelation((JsonObject) val, curd, key, result)) {
						return false;
					}
				}
				
				if (!enumJson(val, curd, result))
					return false;
			}
		} else if (json instanceof JsonArray) {
			if (((JsonArray) json).size() == 0)
				return true;
			
			Iterator<Object> it = ((JsonArray) json).iterator();
			while (it.hasNext()) {
				Object val = it.next();
				if (val == null)
					return false;
				
				if (!enumJson(val, curd, result))
					return false;
			}
		}
		
		return true;
	}
	
	private static boolean addComponentAttrbuteAndRelation(JsonObject json, CRUD curd, String cmptName, ResultBean result) {
		Integer cmptID = MetaData.get().getComponentID(cmptName);
		if (cmptID == null) {
			String info = String.format("compoent:%s ID not found ......", cmptName);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(info);
			return false;
		}
		
		String idAttrName = MetaData.get().getCmptIDAttrNameByName(cmptName);
		if (HttpUtils.isNull(idAttrName)) {
			String info = String.format("compoent:%s ID attribute not found ......", cmptName);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(info);
			return false;
		}
		
		String instID = json.getString(idAttrName);
		PosBean pos = new PosBean();
		getPos(json, pos);
		
		// add instance
		SqlBean sqlInst = new SqlBean(INS_INSTANCE);
		sqlInst.addParams(new Object[]{instID, cmptID, pos.getX(), pos.getY(),
				pos.getWidth(), pos.getHeight(), pos.getRow(), pos.getCol()});
		curd.putSqlBean(sqlInst);
		
		// add component attrbute
		IdSetBean<Integer> attrIdSet = MetaData.get().getAttrIdSet(cmptID);
		Iterator<Integer> it = attrIdSet.iterator();
		while (it.hasNext()) {
			Integer attrID = it.next();
			MetaAttributeBean metaAttr = MetaData.get().getAttributeByID(attrID);
			String attrName = metaAttr.getAttrName();
			String attrValue = json.getString(attrName);
			
			SqlBean sqlAttr = new SqlBean(INS_INSTANCE_ATTR);
			sqlAttr.addParams(new Object[]{instID, attrID, attrName, attrValue});
			curd.putSqlBean(sqlAttr);
		}
		
		// add relation
		Iterator<Entry<String, Object>> itJson = ((JsonObject) json).iterator();
		while (itJson.hasNext()) {
			Entry<String, Object> entry = itJson.next();
			String key = entry.getKey();
			Object val = entry.getValue();
			
			if (key.indexOf(CONTAINER_INDEX) != -1 && val instanceof JsonObject) {
				String subCmptIDAttrName = MetaData.get().getCmptIDAttrNameByName(key);
				String subCmptID = ((JsonObject) val).getString(subCmptIDAttrName);
				
				if (HttpUtils.isNotNull(subCmptID)) {
					SqlBean sqlTopo = new SqlBean(INS_TOPOLOGY);
					sqlTopo.addParams(new Object[] { instID, subCmptID, CONSTS.TOPO_TYPE_CONTAIN });
					curd.putSqlBean(sqlTopo);
				}
			}
		}
		
		return true;
	}
	
	private static boolean addService(JsonObject json, CRUD curd, String sServType, ResultBean result) {
		long dt = System.currentTimeMillis();
		String sServTypeName = SERV_TYPE_NAME_MAPPER.get(sServType);
		if (HttpUtils.isNull(sServTypeName)) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(CONSTS.ERR_SERV_TYPE_NOT_FOUND);
			return false;
		}
		
		JsonObject subJson = json.getJsonObject(sServTypeName);
		
		String serviceID = null;
		String serviceName = null;
		Iterator<Entry<String, Object>> it = subJson.iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			
			if (key.indexOf(ID_INDEX) != -1) {
				serviceID = (String) entry.getValue();
				continue;
			}
			
			if (key.indexOf(NAME_INDEX) != -1) {
				serviceName = (String) entry.getValue();
				continue;
			}
		}
		
		SqlBean sqlServBean = new SqlBean(INS_SERVICE);
		sqlServBean.addParams(new Object[] { serviceID, serviceName, sServType, dt });
		curd.putSqlBean(sqlServBean);
		
		return true;
	}
	
	private static void getPos(JsonObject posJson, PosBean pos) {
		Integer x = posJson.getInteger(FixHeader.HEADER_X);
		Integer y = posJson.getInteger(FixHeader.HEADER_Y);
		
		Integer width  = posJson.getInteger(FixHeader.HEADER_WIDTH);
		Integer height = posJson.getInteger(FixHeader.HEADER_HEIGHT);
		
		Integer row = posJson.getInteger(FixHeader.HEADER_ROW);
		Integer col = posJson.getInteger(FixHeader.HEADER_COL);
		
		pos.setX(x != null ? x : 0);
		pos.setY(y != null ? y : 0);
		pos.setWidth(width != null ? width : CONSTS.POS_DEFAULT_VALUE);
		pos.setHeight(height != null ? height : CONSTS.POS_DEFAULT_VALUE);
		pos.setRow(row != null ? row : CONSTS.POS_DEFAULT_VALUE);
		pos.setCol(col != null ? col : CONSTS.POS_DEFAULT_VALUE);
	}
	
	private static boolean checkSchema(String sTopoJson, String sServType, ResultBean result) {
		String schemaName = SKELETON_SCHEMA_MAPPER.get(sServType);
		if (HttpUtils.isNull(schemaName)) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(CONSTS.ERR_SCHEMA_FILE_NOT_EXIST);
			return false;
		}
		
		boolean ret = false;
		try {
			ProcessingReport report = Validator.validator(schemaName, sTopoJson);
			ret = report.isSuccess();
		} catch (IOException | ProcessingException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(CONSTS.ERR_JSON_SCHEME_VALI_ERR);
		}
		
		return ret;
	}

}
