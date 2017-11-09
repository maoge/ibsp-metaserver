package ibsp.metaserver.autodeploy;

import ibsp.metaserver.bean.ResultBean;

public interface Deployer {
	
	public boolean deployService(String serviceID, ResultBean result);
	
	public boolean undeployService(String serviceID, ResultBean result);
	
}
