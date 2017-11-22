package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TiDBService {
	
	private static Logger logger = LoggerFactory.getLogger(TiDBService.class);
	
	public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> pdServerList,
			List<InstanceDtlBean> tidbServerList, List<InstanceDtlBean> tikvServerList,
			InstanceDtlBean collectd, ResultBean result) {
		
		Map<Integer, String> serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
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
		
		
		// get tikv server list
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
		
		
		// get pd server list
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
	
}
