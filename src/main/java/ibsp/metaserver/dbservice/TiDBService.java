package ibsp.metaserver.dbservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class TiDBService {
	
	private static Logger logger = LoggerFactory.getLogger(TiDBService.class);
	private static final String GET_ADDRESS_BY_SERV_ID = 
			"SELECT ATTR_NAME, ATTR_VALUE FROM "+ 
			"t_instance_attr att JOIN t_instance ins ON att.INST_ID=ins.INST_ID "+ 
			"JOIN t_meta_cmpt cmpt ON ins.CMPT_ID=cmpt.CMPT_ID "+
			"JOIN t_topology top1 ON ins.INST_ID=top1.INST_ID2 "+ 
			"JOIN t_topology top2 ON top1.INST_ID1=top2.INST_ID2 "+
			"WHERE (attr_name='IP' OR attr_name='PORT') AND ins.IS_DEPLOYED=1 "+
			"AND cmpt.CMPT_NAME=? AND top2.INST_ID1=?";
			
	
	public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> pdServerList,
			List<InstanceDtlBean> tidbServerList, List<InstanceDtlBean> tikvServerList,
			InstanceDtlBean collectd, ResultBean result) {
		
		Map<Integer, String> serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
		if (serviceStub == null) {
			return false;
		}
		
		return  getTidbInfoByServIdOrServiceStub(serviceID,serviceStub,tidbServerList,result) && 
				getPDInfoByServIdOrServiceStub(serviceID, serviceStub, pdServerList, result) && 
				getTikvInfoByServIdOrServiceStub(serviceID, serviceStub, tikvServerList, result);
	}
	
	public static boolean getPDInfoByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			List<InstanceDtlBean> pdServerList, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
				
		Integer pdContainerCmptID = MetaData.get().getComponentID("DB_PD_CONTAINER");
		String pdContainerID = serviceStub.get(pdContainerCmptID);
		Set<String> pdStub = MetaDataService.getSubNodes(pdContainerID, result);
		if (pdStub == null || pdStub.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("pd container subnode is null ......");
			return false;
		}
		for (String id : pdStub) {
			InstanceBean pdInstance = MetaDataService.getInstance(id, result);
			Map<String, InstAttributeBean> pdAttr = MetaDataService.getAttribute(id, result);
			if (pdInstance == null || pdAttr == null) {
				String err = String.format("pd id:%s, info missing ......", id);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(err);
				return false;
			}
			InstanceDtlBean pd = new InstanceDtlBean(pdInstance, pdAttr);
			pdServerList.add(pd);
		}
		
		return true;
	}

	public static boolean getTikvInfoByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			List<InstanceDtlBean> tikvServerList, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
				
		Integer tikvContainerCmptID = MetaData.get().getComponentID("DB_TIKV_CONTAINER");
		String tikvContainerID = serviceStub.get(tikvContainerCmptID);
		Set<String> tikvStub = MetaDataService.getSubNodes(tikvContainerID, result);
		if (tikvStub == null || tikvStub.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("tikv container subnode is null ......");
			return false;
		}
		for (String id : tikvStub) {
			InstanceBean tikvInstance = MetaDataService.getInstance(id, result);
			Map<String, InstAttributeBean> tikvAttr = MetaDataService.getAttribute(id, result);
			if (tikvInstance == null || tikvAttr == null) {
				String err = String.format("tikv id:%s, info missing ......", id);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(err);
				return false;
			}
			InstanceDtlBean tikv = new InstanceDtlBean(tikvInstance, tikvAttr);
			tikvServerList.add(tikv);
		}

		return true;
	}

	public static boolean getTidbInfoByServIdOrServiceStub(String serviceID,Map<Integer, String> serviceStub, 
			List<InstanceDtlBean> tidbServerList, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
		Integer tidbContainerCmptID = MetaData.get().getComponentID("DB_TIDB_CONTAINER");
		String tidbContainerID = serviceStub.get(tidbContainerCmptID);
		Set<String> tidbStub = MetaDataService.getSubNodes(tidbContainerID, result);
		if (tidbStub == null || tidbStub.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("tidb container subnode is null ......");
			return false;
		}
		for (String id : tidbStub) {
			InstanceBean tidbInstance = MetaDataService.getInstance(id, result);
			Map<String, InstAttributeBean> tidbAttr = MetaDataService.getAttribute(id, result);
			if (tidbInstance == null || tidbAttr == null) {
				String err = String.format("tidb id:%s, info missing ......", id);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(err);
				return false;
			}
			InstanceDtlBean tidb = new InstanceDtlBean(tidbInstance, tidbAttr);
			tidbServerList.add(tidb);
		}
		return true;
	}
	
	public static JsonObject explainSql(String sql, String servID, 
			String schema, String user, String pwd, ResultBean bean) {
		sql = "EXPLAIN " + sql;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			String DBAddress = getAddressByServID(servID, CONSTS.SERV_DB_TIDB);
			if (HttpUtils.isNull(DBAddress)) {
				bean.setRetCode(CONSTS.REVOKE_NOK);
				bean.setRetInfo(CONSTS.ERR_FIND_TIDB_SERVER_ERROR+servID);
			}
			
			JsonArray plan = null;
			try {
				DBAddress = "jdbc:mysql://"+DBAddress+"/"+schema+"?"+
						"user="+user+"&password="+pwd+
						"&useUnicode=true&characterEncoding=UTF8&useSSL=true";
				conn = DriverManager.getConnection(DBAddress);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				bean.setRetCode(CONSTS.REVOKE_NOK);
				bean.setRetInfo(CONSTS.ERR_CONNECT_TIDB_SERVER_ERROR+e.getMessage());
				return null;
			}
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			plan = resultSetToJson(rs);
			return explainPlanToTree(plan);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			bean.setRetCode(CONSTS.REVOKE_NOK);
			bean.setRetInfo(e.getMessage());
		} finally {
			try {
				if (rs != null) rs.close();
				if (stmt != null) stmt.close();
				if (conn != null) conn.close();
			} catch (Exception e) {
			}
		}
		
		return null;
	}
	
	/**
	 * 将sql执行计划转换成前台echarts所需的树状json格式
	 * @param plan
	 * @return
	 */
	private static JsonObject explainPlanToTree(JsonArray plan) {
		Map<String, JsonArray> childrenMap = new HashMap<String, JsonArray>();
		
		//根据前序（后根）遍历的顺序将画树需要的信息提取出来
		for (int i=0; i<plan.size(); i++) {
			JsonObject object = plan.getJsonObject(i);
			
			//name信息
			JsonObject resultObject = new JsonObject();
			String name = object.getString("ID");
			
			//children信息
			JsonArray children = new JsonArray();
			if (childrenMap.containsKey("cop") && object.getString("TASK").equals("root")) {
				for (Object o : childrenMap.get("cop")) {
					JsonObject child = (JsonObject) o;
					children.add(child);
					if (child.containsKey("table")) {
						if (!resultObject.containsKey("table")) {
							resultObject.put("table", child.getString("table"));
						}
						child.remove("table");
					}
				}
				childrenMap.remove("cop");
			}
			if (childrenMap.containsKey(name)) {
				for (Object o : childrenMap.get(name)) {
					JsonObject child = (JsonObject) o;
					children.add(child);
					if (child.containsKey("table")) {
						if (object.getString("TASK").equals("cop") && !resultObject.containsKey("table")) {
							resultObject.put("table", child.getString("table"));
						}
						child.remove("table");
					}
				}
				childrenMap.remove(name);
			}
			if (children.size()>0) {
				resultObject.put("children", children);
			}
			
			//value信息
			StringBuilder value = new StringBuilder("");
			if (object.getString("TASK").equals("cop")) {
				value.append("类型: TiKV端<br/>");
			} else {
				value.append("类型: TiDB端<br/>");
			}
			if (HttpUtils.isNotNull(object.getString("OPERATOR INFO"))) {
				for (String s : object.getString("OPERATOR INFO").split(", ")) {
					if (object.getString("TASK").equals("cop") && 
							s.indexOf(":")!=-1 && s.split(":")[0].equals("table")) {
						resultObject.put("table", s.split(":")[1]);
						continue;
					}
					if (s.indexOf(":")!=-1 || (name.indexOf("Join_")!=-1 && s.indexOf(" join")!=1)) {
						if (!value.substring(value.length()-"<br/>".length(), value.length()).equals("<br/>")) {
							value.append("<br/>");
						}
					} else {
						if (!value.substring(value.length()-"<br/>".length(), value.length()).equals("<br/>")) {
							value.append(", ");
						}
					}
					value.append(s);
				}
			}
			if (resultObject.containsKey("table") && object.getString("TASK").equals("root")) {
				name += "\n表:"+resultObject.getString("table");
				resultObject.remove("table");
			}
			name += "\n约"+Math.round(Double.parseDouble(object.getString("COUNT")))+"条数据";
			
			resultObject.put("name", name);
			resultObject.put("value", value);
			resultObject.put("type", object.getString("TASK"));
			
			//将JsonObject存放起来
			String parentID = object.getString("PARENTS");
			if (HttpUtils.isNull(parentID) && object.getString("TASK").equals("cop")) {
				parentID = "cop";
			}
			if (HttpUtils.isNotNull(parentID)) {
				if (childrenMap.containsKey(parentID)) {
					childrenMap.get(parentID).add(resultObject);
				} else {
					JsonArray newArray = new JsonArray();
					newArray.add(resultObject);
					childrenMap.put(parentID, newArray);
				}
			} else {
				return resultObject;
			}
		}
		return null;
	}
	
	public static boolean getTikvStatusByTikvId(String servID, int tikvId, ResultBean bean) {
		boolean res = false;
		try {
			String PDAddress = getAddressByServID(servID, CONSTS.SERV_DB_PD);
			if (HttpUtils.isNull(PDAddress)) {
				bean.setRetCode(CONSTS.REVOKE_NOK);
				bean.setRetInfo(CONSTS.ERR_FIND_PD_SERVER_ERROR+servID);
			}
			String url = PDAddress + "/pd/api/v1/store/" + tikvId;
			JsonObject tikvStatus = callPDApi(url);
			return CONSTS.TIKV_TOMBSTONE_STATUS.equals(tikvStatus.getJsonObject("store").getString("state_name"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			bean.setRetCode(CONSTS.REVOKE_NOK);
			bean.setRetInfo(e.getMessage());
		}
		return res;
	}
	
	public static JsonObject getTikvStatus(String servID, String instAdd, ResultBean bean) {
		try {
			String PDAddress = getAddressByServID(servID, CONSTS.SERV_DB_PD);
			if (HttpUtils.isNull(PDAddress)) {
				bean.setRetCode(CONSTS.REVOKE_NOK);
				bean.setRetInfo(CONSTS.ERR_FIND_PD_SERVER_ERROR+servID);
			}
			
			JsonObject tikvStatus = callPDApi(PDAddress+CONSTS.PD_API_STORES);
			JsonArray stores = tikvStatus.getJsonArray("stores");
			
			for (Object object : stores) {
				JsonObject store = (JsonObject)object;
				if (store.getJsonObject("store").getString("address").equals(instAdd)) {
					return store;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			bean.setRetCode(CONSTS.REVOKE_NOK);
			bean.setRetInfo(e.getMessage());
		}
		
		return null;
	}
	
	
	private static JsonArray resultSetToJson(ResultSet rs) throws Exception {
		JsonArray jsonarray = new JsonArray();
		ResultSetMetaData metaData = rs.getMetaData();
		
		int colnum = metaData.getColumnCount();
		while (rs.next()) {
			JsonObject jsonObj = new JsonObject();
			for (int i = 1; i <= colnum; i++) {
				String listName = metaData.getColumnLabel(i).toUpperCase();
				jsonObj.put(listName, rs.getObject(i));
			}
			jsonarray.add(jsonObj);
		}
		return jsonarray;
	}
	
	private static String getAddressByServID(String servID, String type) throws Exception {
		SqlBean sqlBean = new SqlBean(GET_ADDRESS_BY_SERV_ID);
		sqlBean.addParams(new Object[] { type, servID });
		CRUD c = new CRUD();
		c.putSqlBean(sqlBean);
		JsonArray result = c.queryForJSONArray();
		
		String address = "";
		if (result.size()>0 && result.getJsonObject(0).getString("ATTR_NAME").equals("IP")) {
			address = result.getJsonObject(0).getString("ATTR_VALUE")+":"+
					result.getJsonObject(1).getString("ATTR_VALUE");
		} else if (result.size()>0 && result.getJsonObject(1).getString("ATTR_NAME").equals("IP")) {
			address = result.getJsonObject(1).getString("ATTR_VALUE")+":"+
					result.getJsonObject(0).getString("ATTR_VALUE");
		} else {
			return null;
		}
		return address;
	}
	
	public static JsonObject callPDApi(String address) throws Exception {
		URL url = new URL("http://"+address);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        
        StringBuilder result = new StringBuilder("");
        String temp;
        while ((temp = br.readLine()) != null) {  
            result.append(temp);  
        }
        con.disconnect();
        return new JsonObject(result.toString());
	}
	
	public static void main(String[] args) {
		List<InstanceDtlBean> pdServerList = new ArrayList<InstanceDtlBean>();
		List<InstanceDtlBean> tidbServerList = new ArrayList<InstanceDtlBean>();
		List<InstanceDtlBean> tikvServerList = new ArrayList<InstanceDtlBean>();
		InstanceDtlBean collectd = null;
		ResultBean result = new ResultBean();

		loadServiceInfo("dccb67fc-a799-dbec-8266-cb78c79bc956", pdServerList, tidbServerList, tikvServerList, collectd, result);
		
		System.out.println(pdServerList);
	}
}
