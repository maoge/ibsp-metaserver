package ibsp.metaserver.autodeploy;

import java.util.Set;

import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.Topology;

public class CacheDeployer implements Deployer {

	@Override
	public boolean deployService(String serviceID, String user, String pwd, String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean undeployService(String serviceID, String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deployInstance(String serviceID, String instID,
			String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean undeployInstance(String serviceID, String instID,
			String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteService(String serviceID, String sessionKey,
			ResultBean result) {
		Topology topo = MetaData.get().getTopo();
		if (topo == null) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("MetaData topo is null!");
			return false;
		}
		
		// delete t_instance,t_instance_attr,t_topology
		Set<String> sub = topo.get(serviceID, CONSTS.TOPO_TYPE_CONTAIN);
		if (sub != null) {
			for (String subID : sub) {
				Set<String> subsub = topo.get(subID, CONSTS.TOPO_TYPE_CONTAIN);
				if (subsub != null) {
					for (String subsubID : subsub) {
						if (!MetaDataService.deleteInstance(subID, subsubID, result))
							return false;
					}
				}
				
				if (!MetaDataService.deleteInstance(serviceID, subID, result))
					return false;
			}
		}
		
		// delete t_instance INST_ID = serviceID
		if (!MetaDataService.deleteInstance(serviceID, serviceID, result))
			return false;
		
		// delete t_service
		if (!MetaDataService.deleteService(serviceID, result))
			return false;
		
		// TODO MetaData clear using event:serviceMap,instanceDtlMap,topo
		
		return true;
	}

	@Override
	public boolean deleteInstance(String serviceID, String instID,
			String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

}
