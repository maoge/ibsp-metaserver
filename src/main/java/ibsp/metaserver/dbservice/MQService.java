package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MQService {
	
	public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> vbrokerList,
			InstanceDtlBean collectd, ResultBean result) {
		
		Map<Integer, String> serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
		if (serviceStub == null) {
			return false;
		}
		
		return  getVBrokersByServIdOrServiceStub(serviceID, serviceStub, vbrokerList, result) &&
				getCollectdInfoByServIdOrServiceStub(serviceID, serviceStub, collectd, result);
	}
	
	public static boolean getVBrokersByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			List<InstanceDtlBean> vbrokerList, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
		
		Integer vbrokerContainerCmptID = MetaData.get().getComponentID("MQ_VBROKER_CONTAINER");
		String vbrokerContainerID = serviceStub.get(vbrokerContainerCmptID);
		Set<String> vbrokers = MetaDataService.getSubNodes(vbrokerContainerID, result);
		if (vbrokers == null || vbrokers.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("vbroker container subnode is null ......");
			return false;
		}
		
		for (String vbrokerId : vbrokers) {
			InstanceDtlBean vbrokerInstance = MetaDataService.getInstanceDtl(vbrokerId, result);
			if (vbrokerInstance == null) {
				return false;
			}
			
			Set<String> brokerIds = MetaDataService.getSubNodes(vbrokerId, result);
			for (String brokerId : brokerIds) {
				InstanceDtlBean brokerInstance = MetaDataService.getInstanceDtl(brokerId, result);
				if (brokerInstance == null) {
					return false;
				}
				
				vbrokerInstance.addSubInstance(brokerInstance);
			}
			
			vbrokerList.add(vbrokerInstance);
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
		Integer mqCollectdCmptID = MetaData.get().getComponentID("MQ_COLLECTD");
		String id = serviceStub.get(mqCollectdCmptID);
		InstanceDtlBean collectdInstance = MetaDataService.getInstanceDtl(id, result);
		if (collectdInstance == null) {
			String err = String.format("MQ collectd id:%s, info missing ......", id);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		return true;
	}

}
