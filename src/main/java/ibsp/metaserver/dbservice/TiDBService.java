package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.HttpUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TiDBService {
	
	private static Logger logger = LoggerFactory.getLogger(TiDBService.class);
	
	private static final String SEL_TOE = "SELECT t1.INST_ID2 as INST_ID, t2.CMPT_ID as CMPT_ID FROM t_topology t1, t_instance t2 "
	                                    +  "WHERE t1.INST_ID1 = ? AND t1.INST_ID2 = t2.INST_ID";
	
	private static final String SEL_INSTANCE = "SELECT INST_ID,CMPT_ID,IS_DEPLOYED,POS_X,POS_Y,WIDTH,HEIGHT,ROW,COL "
	                                    + "FROM t_instance WHERE INST_ID = ?";
	
	private static final String SEL_ATTRIBUTE = "SELECT INST_ID,ATTR_ID,ATTR_NAME,ATTR_VALUE "
	                                    + "FROM t_instance_attr WHERE INST_ID = ?";
	
	public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> pdServerList,
			List<InstanceDtlBean> tidbServerList, List<InstanceDtlBean> tikvServerList,
			InstanceDtlBean collectd, ResultBean result) {
		
		Map<Integer, String> serviceStub = getSubNodesWithType(serviceID, result);
		if (serviceStub == null) {
			return false;
		}
		
		// get db collectd info
		/*Integer dbCollectdCmptID = MetaData.get().getComponentID("DB_COLLECTD");
		String collectdID = serviceStub.get(dbCollectdCmptID);
		if (HttpUtils.isNull(collectdID)) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("db collectd id is null ......");
			return false;
		}
		InstanceBean collectdInstance = getInstance(collectdID, result);
		Map<String, InstAttributeBean> collectdAttr = getAttribute(collectdID, result);
		if (collectdInstance == null || collectdAttr == null) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("db collectd info error ......");
			return false;
		}
		collectd.setInstance(collectdInstance);
		collectd.setAttrMap(collectdAttr);*/
		
		
		// get tidb server list
		Integer tidbContainerCmptID = MetaData.get().getComponentID("DB_TIDB_CONTAINER");
		String tidbContainerID = serviceStub.get(tidbContainerCmptID);
		Set<String> tidbStub = getSubNodes(tidbContainerID, result);
		if (tidbStub == null || tidbStub.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("tidb container subnode is null ......");
			return false;
		}
		for (String id : tidbStub) {
			InstanceBean tidbInstance = getInstance(id, result);
			Map<String, InstAttributeBean> tidbAttr = getAttribute(id, result);
			if (tidbInstance == null || tidbAttr == null) {
				String err = String.format("tidb id:%s, info missing ......", id);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(err);
				return false;
			}
			InstanceDtlBean tidb = new InstanceDtlBean(tidbInstance, tidbAttr);
			tidbServerList.add(tidb);
		}
		
		
		// get tikv server list
		Integer tikvContainerCmptID = MetaData.get().getComponentID("DB_TIKV_CONTAINER");
		String tikvContainerID = serviceStub.get(tikvContainerCmptID);
		Set<String> tikvStub = getSubNodes(tikvContainerID, result);
		if (tikvStub == null || tikvStub.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("tikv container subnode is null ......");
			return false;
		}
		for (String id : tikvStub) {
			InstanceBean tikvInstance = getInstance(id, result);
			Map<String, InstAttributeBean> tikvAttr = getAttribute(id, result);
			if (tikvInstance == null || tikvAttr == null) {
				String err = String.format("tikv id:%s, info missing ......", id);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(err);
				return false;
			}
			InstanceDtlBean tikv = new InstanceDtlBean(tikvInstance, tikvAttr);
			tikvServerList.add(tikv);
		}
		
		
		// get pd server list
		Integer pdContainerCmptID = MetaData.get().getComponentID("DB_PD_CONTAINER");
		String pdContainerID = serviceStub.get(pdContainerCmptID);
		Set<String> pdStub = getSubNodes(pdContainerID, result);
		if (pdStub == null || pdStub.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("pd container subnode is null ......");
			return false;
		}
		for (String id : pdStub) {
			InstanceBean pdInstance = getInstance(id, result);
			Map<String, InstAttributeBean> pdAttr = getAttribute(id, result);
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
	
}
