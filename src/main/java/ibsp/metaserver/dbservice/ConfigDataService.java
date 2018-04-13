package ibsp.metaserver.dbservice;

import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.IdSetBean;
import ibsp.metaserver.bean.MetaAttributeBean;
import ibsp.metaserver.bean.MetaComponentBean;
import ibsp.metaserver.bean.PosBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public class ConfigDataService {
	
	private static Logger logger = LoggerFactory.getLogger(ConfigDataService.class);
	
	private static Map<String, String> SKELETON_SCHEMA_MAPPER = null;
	private static Map<String, String> SERV_TYPE_NAME_MAPPER  = null;
	
	private static final String NAME_INDEX        = "_NAME";
	private static final String ID_INDEX          = "_ID";
	private static final String CONTAINER_INDEX   = "_CONTAINER";
	
	private static final String INS_INSTANCE      = "insert into t_instance(INST_ID,CMPT_ID,IS_DEPLOYED,POS_X,POS_Y,WIDTH,HEIGHT,ROW,COL) "
	                                              + "values(?,?,?,?,?,?,?,?,?)";

	private static final String INS_INSTANCE_ATTR = "insert into t_instance_attr(INST_ID,ATTR_ID,ATTR_NAME,ATTR_VALUE) "
	                                              + "values(?,?,?,?)";
	private static final String MOD_INSTANCE_ATTR = "update t_instance_attr set ATTR_VALUE = ? "
	                                              + "where INST_ID = ? and ATTR_ID = ?";
	
	private static final String INS_TOPOLOGY      = "insert into t_topology(INST_ID1,INST_ID2,TOPO_TYPE) "
	                                              + "values(?,?,?)";
	
	private static final String CNT_SERVICE       = "SELECT COUNT(INST_ID) AS CNT FROM t_service where INST_ID=?";
	private static final String UPDATE_POS        = "update t_instance set POS_X=?,POS_Y=?, WIDTH=?, HEIGHT=?,ROW=?,COL=? "
	                                              + "where INST_ID = ?";
	
	private static final String SEL_DEPLOY_FILE   = "SELECT FILE_TYPE,FILE_NAME,FILE_DIR,IP_ADDRESS,USER_NAME,USER_PWD,FTP_PORT "
	                                              + "FROM t_file_deploy t1, t_ftp_host t2 "
	                                              + "WHERE t1.SERV_CLAZZ = ? AND t1.HOST_ID = t2.HOST_ID";
	
	private static final String MOD_INSTANCE_DEP  = "UPDATE t_instance SET IS_DEPLOYED = ? WHERE INST_ID = ?";
	private static final String MOD_SERVICE_DEP   = "UPDATE t_service SET IS_DEPLOYED = ? WHERE INST_ID = ?";
	
	private static final String SEL_SERVICE_LIST  = "SELECT INST_ID, SERV_NAME, SERV_TYPE, IS_DEPLOYED FROM t_service WHERE 1=1 ";
	private static final String COUNT_SERVICE_LIST= "SELECT count(0) count FROM t_service WHERE 1=1 ";
	private static final String GET_IS_PRODUCT    = "SELECT IS_PRODUCT FROM t_service WHERE INST_ID=?";
	
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
		
		ResultBean servIDBean = new ResultBean();
		ResultBean servNameBean = new ResultBean();
		if (!getServiceIdAndName(topoJson, sServType, servIDBean, servNameBean, result))
			return false;
		
		String serviceID = servIDBean.getRetInfo();
		List<EventBean> events = new LinkedList<EventBean>();
		
		if (MetaDataService.getInstance(serviceID, result) != null) {
			// do move container operation after save service topo
			if (!enumJsonPos(topoJson, curd, result))
				return false;
		} else {
			// init containers
			if (!enumJson(topoJson, curd, result, events))
				return false;
		}
		
		boolean res = curd.executeUpdate(true, result);
		if (res) {
			Iterator<EventBean> it = events.iterator();
			while (it.hasNext()) {
				EventBean ev = it.next();
				if (ev == null)
					continue;
				
				EventBusMsg.publishEvent(ev);
			}
		}
		
		return res;
	}
	
	public static boolean saveServiceNode(String sParentID, String sOperType, String sNodeJson, ResultBean result) {
		JsonObject nodeJson = new JsonObject(sNodeJson);
		
		CRUD curd = new CRUD();
		List<EventBean> events = new LinkedList<EventBean>();
		
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

				if (isPortUsed(instanceNode, result)) {
					return false;
				}
				
				if (sOperType.equals(CONSTS.OP_TYPE_ADD)) {
					// add instance
					addInstance(instID, cmptID, curd, instanceNode, result, events);
					
					// add component attribute
					addComponentAttribute(instID, cmptID, curd, instanceNode, result);
					
					// add relation
					addRelation(sParentID, cmptID, curd, instanceNode, result, events);
				} else if (sOperType.equals(CONSTS.OP_TYPE_MOD)) {
					// mod component attribute
					modComponentAttribute(instID, cmptID, curd, instanceNode, result, events);
				} else {
					String err = String.format("OP_TYPE:%s error ......", sOperType);
					result.setRetCode(CONSTS.REVOKE_NOK);
					result.setRetInfo(err);
					return false;
				}
			}
		}
		
		boolean res = curd.executeUpdate(true, result);
		if (res) {
			Iterator<EventBean> it = events.iterator();
			while (it.hasNext()) {
				EventBean ev = it.next();
				if (ev == null)
					continue;
				
				EventBusMsg.publishEvent(ev);
			}
		}
		
		return res;
	}
	
	public static boolean delServiceNode(String sParentID, String sInstID, ResultBean result) {
		return MetaDataService.deleteInstance(sParentID, sInstID, result);
	}
	
	public static JsonArray getTreeMetaDataByInstId(String sInstID, ResultBean result) {
		return MetaData.get().getMetaTreeByInstId(sInstID);
	}
	
	public static JsonObject getMetaDataByInstId(String sInstID, ResultBean result) {
		return MetaData.get().getMetaDataByInstId(sInstID);
	}
	
	private static boolean isPortUsed(JsonObject instance, ResultBean result) {
		
		if (!instance.containsKey("IP") || !instance.containsKey("PORT")) return false;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			String ip = instance.getString("IP");
			String user  = instance.getString("OS_USER");
			String pwd   = instance.getString("OS_PWD");
			JschUserInfo ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			for (String name : instance.fieldNames()) {
				String port = null;
				if (name.equals("PORT") || name.indexOf("_PORT")!=-1) {
					port = instance.getString(name);
					if (executor.isPortUsed(Integer.parseInt(port))) {
						result.setRetCode(CONSTS.REVOKE_NOK);
						result.setRetInfo(name+" "+port+" is already in use......");
						return true;
					}
				}
			}
		} catch (Exception e) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return true;
		} finally {
			if (connected) {
				executor.close();
			}
		}
		return false;
	}
	
	private static boolean modComponentAttribute(String instID, Integer cmptID,
			CRUD curd, JsonObject nodeJson, ResultBean result, List<EventBean> events) {
		
		IdSetBean<Integer> attrIdSet = MetaData.get().getAttrIdSet(cmptID);
		Iterator<Integer> it = attrIdSet.iterator();
		while (it.hasNext()) {
			Integer attrID = it.next();
			MetaAttributeBean metaAttr = MetaData.get().getAttributeByID(attrID);
			String attrName = metaAttr.getAttrName();
			String attrValue = nodeJson.getString(attrName);
			
			SqlBean sqlAttr = new SqlBean(MOD_INSTANCE_ATTR);
			sqlAttr.addParams(new Object[]{attrValue, instID, attrID});
			curd.putSqlBean(sqlAttr);
		}
		
		JsonObject evJson = new JsonObject();
		evJson.put("INST_ID", instID);
		
		EventBean ev = new EventBean(EventType.e4);
		ev.setUuid(MetaData.get().getUUID());
		ev.setJsonStr(evJson.toString());
		events.add(ev);
		
		return true;
	}
	
	public static boolean modComponentAttribute(String instID, int attrID,
			String attrValue, ResultBean result) {
		
		CRUD curd = new CRUD();
		SqlBean sqlAttr = new SqlBean(MOD_INSTANCE_ATTR);
		sqlAttr.addParams(new Object[]{attrValue, instID, attrID});
		curd.putSqlBean(sqlAttr);
		boolean res = curd.executeUpdate(true, result);
		
		if (res) {
			JsonObject evJson = new JsonObject();
			evJson.put("INST_ID", instID);
			
			EventBean ev = new EventBean(EventType.e4);
			ev.setUuid(MetaData.get().getUUID());
			ev.setJsonStr(evJson.toString());
			
			EventBusMsg.publishEvent(ev);
		}
		
		return res;
	}
	
	
	private static boolean addInstance(String instID, Integer cmptID,
			CRUD curd, JsonObject nodeJson, ResultBean result, List<EventBean> events) {
		
		PosBean pos = new PosBean();
		JsonObject jsonPos = nodeJson.getJsonObject(FixHeader.HEADER_POS);
		if (jsonPos != null)
			getPos(jsonPos, pos);
		
		MetaComponentBean component = MetaData.get().getComponentByID(cmptID);
		String isNeedDeploy = component.getIsNeedDeploy();
		String deployed = isNeedDeploy.equals(CONSTS.NOT_NEED_DEPLOY) ? CONSTS.DEPLOYED : CONSTS.NOT_DEPLOYED;
		
		// add instance
		SqlBean sqlInst = new SqlBean(INS_INSTANCE);
		sqlInst.addParams(new Object[]{instID, cmptID, deployed, pos.getX(), pos.getY(),
				pos.getWidth(), pos.getHeight(), pos.getRow(), pos.getCol()});
		curd.putSqlBean(sqlInst);
		
		JsonObject evJson = new JsonObject();
		evJson.put("INST_ID", instID);
		
		EventBean ev = new EventBean(EventType.e3);
		ev.setUuid(MetaData.get().getUUID());
		ev.setJsonStr(evJson.toString());
		events.add(ev);
		
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
			CRUD curd, JsonObject nodeJson, ResultBean result, List<EventBean> events) {
		
		String subCmptIDAttrName = MetaData.get().getCmptIDAttrNameByID(cmptID);
		String instID2 = nodeJson.getString(subCmptIDAttrName);
		
		if (HttpUtils.isNotNull(instID2)) {
			SqlBean sqlTopo = new SqlBean(INS_TOPOLOGY);
			sqlTopo.addParams(new Object[] { instID1, instID2, CONSTS.TOPO_TYPE_CONTAIN });
			curd.putSqlBean(sqlTopo);
			
			JsonObject evJson = new JsonObject();
			evJson.put("INST_ID1", instID1);
			evJson.put("INST_ID2", instID2);
			evJson.put("TOPO_TYPE", CONSTS.TOPO_TYPE_CONTAIN);
			
			EventBean ev = new EventBean(EventType.e1);
			ev.setUuid(MetaData.get().getUUID());
			ev.setJsonStr(evJson.toString());
			events.add(ev);
		}
		
		return true;
	}
	
	private static boolean enumJsonPos(JsonObject json, CRUD curd, ResultBean result) {
		Iterator<Entry<String, Object>> it = json.iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			String cmptName = entry.getKey();
			Object val = entry.getValue();
			
			if (val instanceof JsonObject) {
				JsonObject subJson = (JsonObject) val;
				JsonObject posJson = subJson.getJsonObject(FixHeader.HEADER_POS);
				if (posJson != null) {
					PosBean pos = new PosBean();
					getPos(posJson, pos);
					
					String idAttrName = MetaData.get().getCmptIDAttrNameByName(cmptName);
					if (HttpUtils.isNull(idAttrName)) {
						String info = String.format("compoent:%s ID attribute not found ......", cmptName);
						result.setRetCode(CONSTS.REVOKE_NOK);
						result.setRetInfo(info);
						return false;
					}
					
					String instID = subJson.getString(idAttrName);
					SqlBean sqlPos = new SqlBean(UPDATE_POS);
					sqlPos.addParams(new Object[] { pos.getX(), pos.getY(),
							pos.getWidth(), pos.getHeight(), pos.getRow(),
							pos.getCol(), instID });
					curd.putSqlBean(sqlPos);
				}
				
				if (!enumJsonPos(subJson, curd, result))
					return false;
			}
		}
		
		return true;
	}
	
	private static boolean enumJson(Object json, CRUD curd,
			ResultBean result, List<EventBean> events) {
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
					if (((JsonObject) val).fieldNames().size() == 0)
						return true;
					
					if (!addComponentAttrbuteAndRelation((JsonObject) val, curd, key, result, events)) {
						return false;
					}
				}
				
				if (!enumJson(val, curd, result, events))
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
				
				if (!enumJson(val, curd, result, events))
					return false;
			}
		}
		
		return true;
	}
	
	private static boolean addComponentAttrbuteAndRelation(JsonObject json, CRUD curd, String cmptName, ResultBean result, List<EventBean> events) {
		MetaComponentBean component = MetaData.get().getComponentByName(cmptName);
		if (component == null) {
			String info = String.format("compoent:%s not found ......", cmptName);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(info);
			return false;
		}
		int cmptID = component.getCmptID();
		
		String idAttrName = MetaData.get().getCmptIDAttrNameByName(cmptName);
		if (HttpUtils.isNull(idAttrName)) {
			String info = String.format("compoent:%s ID attribute not found ......", cmptName);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(info);
			return false;
		}
		
		String instID = json.getString(idAttrName);
		if (HttpUtils.isNull(instID)) {
			// json {}
			return true;
		}
		PosBean pos = new PosBean();
		JsonObject posJson = json.getJsonObject(FixHeader.HEADER_POS);
		if (posJson != null)
			getPos(posJson, pos);
		
		String isNeedDeploy = component.getIsNeedDeploy();
		String deployed = isNeedDeploy.equals(CONSTS.NOT_NEED_DEPLOY) ? CONSTS.DEPLOYED : CONSTS.NOT_DEPLOYED;
		
		// add instance
		SqlBean sqlInst = new SqlBean(INS_INSTANCE);
		sqlInst.addParams(new Object[] { instID, cmptID, deployed, pos.getX(),
				pos.getY(), pos.getWidth(), pos.getHeight(), pos.getRow(),
				pos.getCol() });
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
					
					JsonObject evJson = new JsonObject();
					evJson.put("INST_ID1", instID);
					evJson.put("INST_ID2", subCmptID);
					evJson.put("TOPO_TYPE", CONSTS.TOPO_TYPE_CONTAIN);
					
					EventBean ev = new EventBean(EventType.e1);
					ev.setUuid(MetaData.get().getUUID());
					ev.setJsonStr(evJson.toString());
					
					events.add(ev);
				}
			}
		}
		
		return true;
	}
	
	private static boolean getServiceIdAndName(JsonObject json,
			String sServType, ResultBean servIDBean, ResultBean servNameBean,
			ResultBean result) {
		
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
				servIDBean.setRetInfo(serviceID);
				continue;
			}
			
			if (key.indexOf(NAME_INDEX) != -1) {
				serviceName = (String) entry.getValue();
				servNameBean.setRetInfo(serviceName);
				continue;
			}
		}
		
		if (serviceID == null || serviceName == null) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("service id or name is null ......");
			return false;
		}
		
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
	
	public static boolean isServiceExist(String serviceID, ResultBean result) {
		int cnt = 0;
		CRUD curd = new CRUD();
		
		SqlBean sql = new SqlBean(CNT_SERVICE);
		sql.addParams(new Object[] { serviceID });
		curd.putSqlBean(sql);
		try {
			cnt = curd.queryForCount();
		} catch (CRUDException e) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		}
		
		return cnt > 0;
	}
	
	public static boolean loadDeployFile(String servClazz, Map<String, DeployFileBean> deployFileMap, ResultBean result) {
		SqlBean sqlInst = new SqlBean(SEL_DEPLOY_FILE);
		sqlInst.addParams(new Object[] { servClazz });
		
		CRUD curd = new CRUD();
		curd.putSqlBean(sqlInst);
		
		try {
			List<HashMap<String, Object>> queryList = curd.queryForList();
			if (queryList == null || queryList.isEmpty())
				return false;
			
			Iterator<HashMap<String, Object>> it = queryList.iterator();
			while (it.hasNext()) {
				HashMap<String, Object> mapper = it.next();
				DeployFileBean deployFile = DeployFileBean.convert(mapper);
				deployFileMap.put(deployFile.getFileType(), deployFile);
			}
		} catch (CRUDException e) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public static boolean modInstanceDeployFlag(String instID, String deployFlag, ResultBean result) {
		SqlBean sqlInst = new SqlBean(MOD_INSTANCE_DEP);
		sqlInst.addParams(new Object[] { deployFlag, instID });
		
		CRUD curd = new CRUD();
		curd.putSqlBean(sqlInst);
		
		return curd.executeUpdate(result);
	}
	
	public static boolean modServiceDeployFlag(String instID, String deployFlag, ResultBean result) {
		SqlBean sqlInst = new SqlBean(MOD_SERVICE_DEP);
		sqlInst.addParams(new Object[] { deployFlag, instID });
		
		CRUD curd = new CRUD();
		curd.putSqlBean(sqlInst);
		
		return curd.executeUpdate(result);
	}

	public static JsonArray getServiceList(Map<String, String> params, ResultBean result) {
		
		StringBuilder sb = new StringBuilder(SEL_SERVICE_LIST);
		extendSQLForServiceList(params, sb);
		if (params!=null) {
			int pageSize = Integer.parseInt(params.get(FixHeader.HEADER_PAGE_SIZE));
			int pageNumber = Integer.parseInt(params.get(FixHeader.HEADER_PAGE_NUMBER));
			int start = pageSize*(pageNumber-1);
			if(HttpUtils.isNotNull(pageSize)&&HttpUtils.isNotNull(pageNumber)){
				sb.append(" limit "+start+","+pageSize+" ");
			}
		}
		SqlBean sql = new SqlBean(sb.toString());
		CRUD curd = new CRUD();
		curd.putSqlBean(sql);
		
		try {
			return curd.queryForJSONArray();
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		}
		return null;
	}
	
	public static JsonObject getServiceCount(Map<String, String> params, ResultBean result) {
		
		StringBuilder sb = new StringBuilder(COUNT_SERVICE_LIST);
		extendSQLForServiceList(params, sb);
		SqlBean sql = new SqlBean(sb.toString());
		CRUD curd = new CRUD();
		curd.putSqlBean(sql);
		
		try {
			return curd.queryForJSONObject();
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		}
		return null;
	}

	private static void extendSQLForServiceList(Map<String, String> params, StringBuilder sb) {
		if (params!=null) {
			String serviceName = params.get("SERVICE_NAME");
			String serviceType = params.get("SERVICE_TYPE");
			if (HttpUtils.isNotNull(serviceName)) {
				sb.append(" and serv_name like '%"+serviceName+"%' ");
			}
			if (HttpUtils.isNotNull(serviceType)) {
				sb.append(" and serv_type = '"+serviceType+"' ");
			}
		}
	}
	
	public static Boolean getIsProductByServId(String serviceID, ResultBean result) {
		
		SqlBean sql = new SqlBean(GET_IS_PRODUCT);
		sql.addParams(new Object[] {serviceID});
		CRUD curd = new CRUD();
		curd.putSqlBean(sql);
		
		try {
			return curd.queryForCount()==1;
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		}
		return null;
	}
}
