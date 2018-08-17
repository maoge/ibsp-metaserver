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

import ibsp.metaserver.bean.*;
import ibsp.metaserver.exception.CRUDException;
import ibsp.metaserver.global.MonitorData;
import ibsp.metaserver.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.global.MetaData;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class TiDBService {
	
	private static Logger logger = LoggerFactory.getLogger(TiDBService.class);

	private static final String INSERT_TIDB_COLLECT= "INSERT INTO t_mo_tidb_collect (INST_ID,QPS, "+
			"CONNECTION_COUNT,STATEMENT_COUNT,QUERY_DURATION_99PERC,TIME) values (?,?,?,?,?,?)" ;

	private static final String INSERT_PD_COLLECT= "INSERT INTO t_mo_pd_collect (INST_ID,STORAGE_CAPACITY, "+
			"CURRENT_STORAGE_SIZE,COMPLETE_DURATION_SECONDS_99PENC,LEADER_BALANCE_RATIO,REGION_BALANCE_RATIO," +
			"REGIONS, TIME) values (?,?,?,?,?,?,?,?)" ;

	private static final String INSERT_TIKV_COLLECT= "INSERT INTO t_mo_tikv_collect (INST_ID,LEADER_COUNT, "+
			"REGION_COUNT,SCHEEDULER_COMMAND_DURATION,TIME) values (?,?,?,?,?)" ;

	private final static String SEL_TIDB_MONITOR_COLLECT   = "SELECT QPS,TIME from t_mo_tidb_collect " +
			"WHERE TIME BETWEEN ? AND ? AND INST_ID = ?  ORDER BY TIME ASC";

	private final static String SEL_PD_MONITOR_COLLECT   = "SELECT STORAGE_CAPACITY,CURRENT_STORAGE_SIZE," +
			"LEADER_BALANCE_RATIO,REGION_BALANCE_RATIO, REGIONS, TIME from t_mo_pd_collect " +
			"WHERE TIME BETWEEN ? AND ? AND INST_ID = ?  ORDER BY TIME ASC";

	private final static String SEL_TIKV_MONITOR_COLLECT   = "SELECT SCHEEDULER_COMMAND_DURATION,TIME from t_mo_tikv_collect " +
			"WHERE TIME BETWEEN ? AND ? AND INST_ID = ?  ORDER BY TIME ASC";


	public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> pdServerList,
			List<InstanceDtlBean> tidbServerList, List<InstanceDtlBean> tikvServerList,
			InstanceDtlBean collectd, ResultBean result) {
		
		Map<Integer, String> serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
		if (serviceStub == null) {
			return false;
		}
		
		return  getTidbInfoByServIdOrServiceStub(serviceID,serviceStub,tidbServerList,result) && 
				getPDInfoByServIdOrServiceStub(serviceID, serviceStub, pdServerList, result) && 
				getTikvInfoByServIdOrServiceStub(serviceID, serviceStub, tikvServerList, result) &&
				getCollectdInfoByServIdOrServiceStub(serviceID, serviceStub, collectd, result);
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
	
	public static boolean getCollectdInfoByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			InstanceDtlBean collectd, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
		Integer tidbCollectdCmptID = MetaData.get().getComponentID("DB_COLLECTD");
		String id = serviceStub.get(tidbCollectdCmptID);
		InstanceBean collectdInstance = MetaDataService.getInstance(id, result);
		Map<String, InstAttributeBean> collectdAttr = MetaDataService.getAttribute(id, result);
		if (collectdInstance == null || collectdAttr == null) {
			String err = String.format("DB collectd id:%s, info missing ......", id);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		collectd.setInstance(collectdInstance);
		collectd.setAttrMap(collectdAttr);
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
	
	private static String getAddressByServID(String servID, String type) {
		try {
			MetaData data = MetaData.get();
			Topology topo = data.getTopo();
			Set<String> containers = topo.get(servID, CONSTS.TOPO_TYPE_CONTAIN);
			
			for (String containerID : containers) {
				InstanceDtlBean container = data.getInstanceDtlBean(containerID);
				int cmptID = container.getInstance().getCmptID();
				String cmptName = data.getComponentByID(cmptID).getCmptName();
				if (cmptName.equals(type+"_CONTAINER")) {
					Set<String> instances = topo.get(container.getInstID(), CONSTS.TOPO_TYPE_CONTAIN);
					
					for (String instanceID : instances) {
						InstanceDtlBean instance = data.getInstanceDtlBean(instanceID);
						return instance.getAttribute("IP").getAttrValue()+
								":"+instance.getAttribute("PORT").getAttrValue();
					}
				}
			}
			
			return null;
		} catch (Exception e) {
			logger.error("Error get address by service ID...", e);
			return null;
		}
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

	public static JsonArray getTidbInfoByService(String servID, ResultBean result) {
		JsonArray array = new JsonArray();
		try {
			List<InstanceDtlBean> tidbServerList = new ArrayList<InstanceDtlBean>();
			if (!getTidbInfoByServIdOrServiceStub(servID, null, tidbServerList, result))
				return null;
			
			for (InstanceDtlBean instance : tidbServerList) {
				if (instance.getInstance().getIsDeployed().equals(CONSTS.NOT_DEPLOYED))
					continue;
				JsonObject object = new JsonObject();
				object.put("ID", instance.getInstID());
				object.put("ADDRESS", instance.getAttribute("IP").getAttrValue()+":"
						+instance.getAttribute("PORT").getAttrValue());
				array.add(object);
			}
		} catch (Exception e) {
			logger.error("Error get tidb info by service ID...", e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
		return array;
	}

	public static boolean saveCollectInfo(String servId, ResultBean result) {
		boolean res = true;
		if(!saveTiDBCollectInfo(servId, result)){
			res = false;
			logger.error("save tidb collect info fail : {}" , result.getRetInfo());
		}
		if(!savePDCollectInfo(servId, result)){
			res = false;
			logger.error("save pd collect info fail : {}" , result.getRetInfo());
		}
		return res;
	}

	public static boolean saveTiDBCollectInfo(String servId, ResultBean result) {
		boolean res = false;

		List<InstanceDtlBean> tidbs = MetaData.get().getTiDBsByServId(servId);
		if(tidbs == null || tidbs.size() == 0) {
			return true;
		}
		Map<String, TiDBMetricsStatus> tidbCollectInfo = MonitorData.get().getTiDBMetricsStatusMap();

		CRUD crud = new CRUD();
		long currentTime = System.currentTimeMillis();

		for(InstanceDtlBean tidb : tidbs) {
			SqlBean sqlBean = new SqlBean(INSERT_TIDB_COLLECT);
			String instID = tidb.getInstID();

			TiDBMetricsStatus collectInfo = tidbCollectInfo.get(instID);
			if(collectInfo == null)
				continue;
			sqlBean.addParams(new Object[]{
					tidb.getInstID(), collectInfo.getQps(), collectInfo.getConnectionCount(),
					collectInfo.getStatements(), collectInfo.getQueryDurationSeconeds(), currentTime
			});
			crud.putSqlBean(sqlBean);
		}
		res = crud.executeUpdate(result);
		return res;
	}

	public static boolean savePDCollectInfo(String servId, ResultBean result) {
		boolean res = false;

		List<InstanceDtlBean> pds = MetaData.get().getPDsByServId(servId);
		if(pds == null || pds.size() == 0) {
			return true;
		}
		Map<String, PDClusterStatus> pdCollectInfo = MonitorData.get().getPdClusterStatusMap();

		CRUD crud = new CRUD();
		long currentTime = System.currentTimeMillis();

		for(InstanceDtlBean pd : pds) {
			SqlBean sqlBean = new SqlBean(INSERT_PD_COLLECT);
			String instID = pd.getInstID();

			PDClusterStatus collectInfo = pdCollectInfo.get(instID);
			if(collectInfo == null)
				continue;
			sqlBean.addParams(new Object[]{
					pd.getInstID(), collectInfo.getCapacity(), collectInfo.getCurrentSize(),
					collectInfo.getCompletedCmdsDurationSecondsAvg99(), collectInfo.getLeaderBalanceRatio(),
					collectInfo.getRegionBalanceRatio(), collectInfo.getRegions(), currentTime
			});
			crud.putSqlBean(sqlBean);
		}
		res = crud.executeUpdate(result);
		return res;
	}

	public static boolean saveTiKVCollectInfo(String instId, ResultBean result) {
		boolean res = false;

		InstanceDtlBean tikv = MetaData.get().getInstanceDtlBean(instId);
		if(tikv == null) {
			return true;
		}

		Map<String, TiKVMetricsStatus> tikvCollectInfo = MonitorData.get().getTiKVMetricsStatusMap();

		TiKVMetricsStatus collectInfo = tikvCollectInfo.get(instId);
		if(collectInfo == null)
			return true;

		CRUD crud = new CRUD();
		long currentTime = System.currentTimeMillis();

		SqlBean sqlBean = new SqlBean(INSERT_TIKV_COLLECT);

		sqlBean.addParams(new Object[]{
				tikv.getInstID(), collectInfo.getLeaderCount(), collectInfo.getRegionCount(),
				collectInfo.getTikvSchedulerContextTotal(), currentTime
		});
		crud.putSqlBean(sqlBean);

		res = crud.executeUpdate(result);
		return res;
	}

	public static JsonArray getTiDBCollectData(String servId) {
		JsonArray jsonArray = new JsonArray();
		List<InstanceDtlBean> tidbs = MetaData.get().getTiDBsByServId(servId);

		if(tidbs == null)
			return null;

		for(InstanceDtlBean tidb : tidbs) {
			TiDBMetricsStatus collectInfo = MonitorData.get().getTiDBMetricsStatusMap().get(tidb.getInstID());

			if(collectInfo == null)
				return null;

			JsonObject subJson = new JsonObject()
					.put(FixHeader.HEADER_TIDB_ID, tidb.getInstID())
					.put(FixHeader.HEADER_TIDB_NAME, tidb.getAttribute(FixHeader.HEADER_TIDB_NAME).getAttrValue())
					.put(FixHeader.HEADER_TIDB_QPS, collectInfo.getQps())
					.put(FixHeader.HEADER_TIDB_CONNECTION_COUNT, collectInfo.getConnectionCount())
					.put(FixHeader.HEADER_TIDB_STATEMENT_COUNT, collectInfo.getStatements())
					.put(FixHeader.HEADER_TIDB_QUERY_DURATION_99P, collectInfo.getQueryDurationSeconeds());

			jsonArray.add(subJson);
		}

		return jsonArray;
	}

	public static JsonArray getPDCollectData(String servId) {
		JsonArray jsonArray = new JsonArray();
		List<InstanceDtlBean> pds = MetaData.get().getPDsByServId(servId);

		if(pds == null)
			return null;

		for(InstanceDtlBean pd : pds) {

			PDClusterStatus collectInfo = MonitorData.get().getPdClusterStatusMap().get(pd.getInstID());
			if(collectInfo == null)
				continue;

			JsonObject subJson = new JsonObject()
					.put(FixHeader.HEADER_PD_ID, pd.getInstID())
					.put(FixHeader.HEADER_PD_NAME, pd.getAttribute(FixHeader.HEADER_PD_NAME).getAttrValue())
					.put(FixHeader.HEADER_PD_STORAGE_CAPACITY, collectInfo.getCapacity())
					.put(FixHeader.HEADER_PD_CURRENT_STORAGE_SIZE, collectInfo.getCurrentSize())
					.put(FixHeader.HEADER_PD_LEADER_BALANCE_RATIO, collectInfo.getLeaderBalanceRatio())
					.put(FixHeader.HEADER_PD_REGION_BALANCE_RATIO, collectInfo.getRegionBalanceRatio())
					.put(FixHeader.HEADER_PD_REGIONS, collectInfo.getRegions())
					.put("STORE_UP_COUNT", collectInfo.getStoreUpCount())
					.put("STORE_DOWN_COUNT", collectInfo.getStoreDownCount())
					.put("STORE_OFFLINE_COUNT", collectInfo.getStoreOfflineCount())
					.put("STORE_TOMBSTONE_COUNT", collectInfo.getStoreTombstoneCount())
					.put(FixHeader.HEADER_PD_COMPLETE_DURATION_SECONDS_99PENC, collectInfo.getCompletedCmdsDurationSecondsAvg99());

			jsonArray.add(subJson);

			break;
		}

		return jsonArray;
	}

	public static JsonArray getTiKVCollectData(String servId) {
		JsonArray jsonArray = new JsonArray();
		List<InstanceDtlBean> tikvs = MetaData.get().getTiKVsByServId(servId);

		if(tikvs == null)
			return null;

		for(InstanceDtlBean tikv : tikvs) {
			TiKVMetricsStatus collectInfo = MonitorData.get().getTiKVMetricsStatusMap().get(tikv.getInstID());

			if(collectInfo == null)
				return null;

			JsonObject subJson = new JsonObject()
					.put(FixHeader.HEADER_TIKV_ID, tikv.getInstID())
					.put(FixHeader.HEADER_TIKV_NAME, tikv.getAttribute(FixHeader.HEADER_TIKV_NAME).getAttrValue())
					.put(FixHeader.HEADER_TIKV_LEADER_COUNT, collectInfo.getLeaderCount())
					.put(FixHeader.HEADER_TIKV_REGION_COUNT, collectInfo.getRegionCount())
					.put(FixHeader.HEADER_TIKV_SCHEEDULER_COMMAND_DURATION, collectInfo.getTikvSchedulerContextTotal());
			jsonArray.add(subJson);
		}

		return jsonArray;
	}

	public static JsonArray getTiDBHisCollectData(String tidbId, long startTs, long endTs, ResultBean result) {
		JsonArray jsonArray = null;

		CRUD crud = new CRUD();
		SqlBean sqlBean = new SqlBean(SEL_TIDB_MONITOR_COLLECT);
		sqlBean.addParams(new Object[]{
				startTs, endTs, tidbId
		});
		crud.putSqlBean(sqlBean);

		try {
			jsonArray = crud.queryForJSONArray();
		} catch (CRUDException e) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(String.format("get TiDB his data fail : %s", e.getMessage()));
			logger.error(e.getMessage(), e);
		}

		return jsonArray;
	}

	public static JsonArray getPDHisCollectData(String pdId, long startTs, long endTs, ResultBean result) {
		JsonArray jsonArray = null;

		CRUD crud = new CRUD();
		SqlBean sqlBean = new SqlBean(SEL_PD_MONITOR_COLLECT);
		sqlBean.addParams(new Object[]{
				startTs, endTs, pdId
		});
		crud.putSqlBean(sqlBean);

		try {
			jsonArray = crud.queryForJSONArray();
		} catch (CRUDException e) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(String.format("get PD his data fail : %s", e.getMessage()));
			logger.error(e.getMessage(), e);
		}

		return jsonArray;
	}

	public static JsonArray getTiKVHisCollectData(String tikvId, long startTs, long endTs, ResultBean result) {
		JsonArray jsonArray = null;

		CRUD crud = new CRUD();
		SqlBean sqlBean = new SqlBean(SEL_TIKV_MONITOR_COLLECT);
		sqlBean.addParams(new Object[]{
				startTs, endTs, tikvId
		});
		crud.putSqlBean(sqlBean);

		try {
			jsonArray = crud.queryForJSONArray();
		} catch (CRUDException e) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(String.format("get TIKV his data fail : %s", e.getMessage()));
			logger.error(e.getMessage(), e);
		}

		return jsonArray;
	}
}
