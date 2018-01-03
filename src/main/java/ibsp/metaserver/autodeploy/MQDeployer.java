package ibsp.metaserver.autodeploy;

import ibsp.metaserver.bean.ResultBean;

public class MQDeployer implements Deployer {

	@Override
	public boolean deployService(String serviceID, String user, String pwd, String sessionKey, ResultBean result) {
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
	public boolean undeployService(String serviceID, String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteService(String serviceID, String sessionKey,
			ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteInstance(String serviceID, String instID,
			String sessionKey, ResultBean result) {
		// TODO Auto-generated method stub
		return false;
	}

}
