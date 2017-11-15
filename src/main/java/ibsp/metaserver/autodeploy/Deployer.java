package ibsp.metaserver.autodeploy;

import java.util.Map;

import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.ResultBean;

public interface Deployer {
	
	public boolean deployService(String serviceID, String sessionKey, ResultBean result);
	
	public boolean undeployService(String serviceID, String sessionKey, ResultBean result);
	
	public boolean loadDeployFileInfo(Map<String, DeployFileBean> deployFileMap, ResultBean result);
	
}
