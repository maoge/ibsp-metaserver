package ibsp.metaserver.autodeploy;

import java.util.Map;

import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.ResultBean;

public class MQDeployer implements Deployer {

	@Override
	public boolean deployService(String serviceID, String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean undeployService(String serviceID, String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadDeployFileInfo(Map<String, DeployFileBean> deployFileMap, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

}
