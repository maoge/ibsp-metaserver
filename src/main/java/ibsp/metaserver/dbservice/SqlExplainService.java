package ibsp.metaserver.dbservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SqlExplainService {
	
	private static Logger logger = LoggerFactory.getLogger(SqlExplainService.class);

	public static JsonObject explainSql(String sql, String servID, 
			String schema, String user, String pwd, ResultBean bean) {
		String addressSql = "SELECT ATTR_NAME, ATTR_VALUE FROM "+ 
				"t_instance_attr att JOIN t_instance ins ON att.INST_ID=ins.INST_ID "+ 
				"JOIN t_meta_cmpt cmpt ON ins.CMPT_ID=cmpt.CMPT_ID "+
				"JOIN t_topology top1 ON ins.INST_ID=top1.INST_ID2 "+ 
				"JOIN t_topology top2 ON top1.INST_ID1=top2.INST_ID2 "+
				"WHERE (attr_name='IP' OR attr_name='PORT') AND "+
				"cmpt.CMPT_NAME=? AND top2.INST_ID1=?";
		sql = "EXPLAIN " + sql;
		
		try {
			SqlBean sqlBean = new SqlBean(addressSql);
			sqlBean.addParams(new Object[] { CONSTS.SERV_DB_TIDB, servID });
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			JsonArray result = c.queryForJSONArray();
			
			String DBAddress = "";
			if (result.size()>0 && result.getJsonObject(0).getString("ATTR_NAME").equals("IP")) {
				DBAddress = result.getJsonObject(0).getString("ATTR_VALUE")+":"+
						result.getJsonObject(1).getString("ATTR_VALUE");
			} else if (result.size()>0 && result.getJsonObject(1).getString("ATTR_NAME").equals("IP")) {
				DBAddress = result.getJsonObject(1).getString("ATTR_VALUE")+":"+
						result.getJsonObject(0).getString("ATTR_VALUE");
			} else {
				bean.setRetCode(CONSTS.REVOKE_NOK);
				bean.setRetInfo(CONSTS.ERR_FIND_TIDB_SERVER_ERROR+servID);
				return null;
			}
			
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			JsonArray plan = null;
			try {
				DBAddress = "jdbc:mysql://"+DBAddress+"/"+schema+"?"+
						"user="+user+"&password="+pwd+
						"&useUnicode=true&characterEncoding=UTF8&useSSL=true";
				conn = DriverManager.getConnection(DBAddress);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				plan = resultSetToJson(rs);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				bean.setRetCode(CONSTS.REVOKE_NOK);
				bean.setRetInfo(CONSTS.ERR_CONNECT_TIDB_SERVER_ERROR+e.getMessage());
				return null;
			} finally {
				if (rs != null) rs.close();
				if (stmt != null) stmt.close();
				if (conn != null) conn.close();
			}
			
			return explainPlanToTree(plan);
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
			name += "\n约"+Math.round(object.getDouble("COUNT"))+"条数据";
			
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
	
}