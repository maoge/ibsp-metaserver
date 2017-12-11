package ibsp.metaserver.autodeploy;

import ibsp.metaserver.bean.ResultBean;

public interface Deployer {
	
	public boolean deployService(String serviceID, String user, String pwd, String sessionKey, ResultBean result);
	public boolean undeployService(String serviceID, String sessionKey, ResultBean result);
	
	public boolean deployInstance(String serviceID, String instID, String sessionKey, ResultBean result);
	public boolean undeployInstance(String serviceID, String instID, String sessionKey, ResultBean result);
	
}
