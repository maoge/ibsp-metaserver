package ibsp.metaserver.autodeploy;

import ibsp.metaserver.bean.ResultBean;

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

}
