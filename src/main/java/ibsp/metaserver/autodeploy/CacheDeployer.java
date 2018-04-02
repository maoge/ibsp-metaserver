 package ibsp.metaserver.autodeploy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.autodeploy.utils.DeployLog;
import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SCPFileUtils;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.dbservice.CacheService;
import ibsp.metaserver.dbservice.ConfigDataService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.Topology;
 
public class CacheDeployer implements Deployer {

	private static Logger logger = LoggerFactory.getLogger(CacheDeployer.class);
	private static final String UPDATE_CACHE_SLOT = 
			"UPDATE t_instance_attr SET ATTR_VALUE=? "
			+ "WHERE INST_ID=? AND attr_id=239";
	private static final String UPDATE_MASTER_ID = 
			"UPDATE t_instance_attr SET ATTR_VALUE=? "
			+ "WHERE INST_ID=? AND attr_id=208";
	
	@Override
	public boolean deployService(String serviceID, String user, String pwd, String sessionKey, ResultBean result) {
		
		List<InstanceDtlBean> nodeClusterList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> proxyList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		
		if (!CacheService.loadServiceInfo(serviceID, nodeClusterList, proxyList, collectd, result))
			return false;
		
		//check hash slot
		if (!checkHashSlot(serviceID, nodeClusterList, sessionKey, result))
			return false;
		
		// deploy cache node
		if (!deployNodeClusterList(serviceID, nodeClusterList, sessionKey, result))
			return false;

		// deploy proxy
		if (!deployProxyList(serviceID, proxyList, sessionKey, result))
			return false;
		
		// deploy collectd
//		if (!deployCollectd(serviceID, collectd, sessionKey, result))
//			return false;
		
		// mod t_service.IS_DEPLOYED = 1
		if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.DEPLOYED, result))
			return false;
		
		return true;
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
	
	
	private boolean checkHashSlot(String serviceID, List<InstanceDtlBean> nodeClusterList, String sessionKey,
			ResultBean result) {
		boolean slotComplete = true, slotEmpty = true;
		for (int i = 0; i < nodeClusterList.size(); i++) {
			InstanceDtlBean clusterDtl = nodeClusterList.get(i);
			String slot = clusterDtl.getAttribute("CACHE_SLOT").getAttrValue();
			if (HttpUtils.isNull(slot)) {
				slotComplete = false;
			} else {
				slotEmpty = false;
			}
		}
		if (slotEmpty) {
			int key = (CONSTS.MAX_CACHE_SLOT+1) / nodeClusterList.size();

			CRUD c = new CRUD();
			for (int i = 0; i < nodeClusterList.size(); i++) {
				InstanceDtlBean clusterDtl = nodeClusterList.get(i);
				String slot = "["+(key*i)+","+(key*(i+1)-1)+"]";
				clusterDtl.setAttribute("CACHE_SLOT", slot);
				SqlBean sqlBean = new SqlBean(UPDATE_CACHE_SLOT);
				sqlBean.addParams(new Object[] {slot, clusterDtl.getInstID()});
				c.putSqlBean(sqlBean);
			}
			try {
				c.executeUpdate();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(e.getMessage());
				return false;
			}
			return true;
		} else if (slotComplete) {
			return true;
		} else {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("Cache slot info not compelete...");
			return false;
		}
	}
	
	private boolean deployNodeClusterList(String serviceID, List<InstanceDtlBean> nodeClusterList, 
			String sessionKey, ResultBean result) {

		for (int i = 0; i < nodeClusterList.size(); i++) {
			InstanceDtlBean clusterDtl = nodeClusterList.get(i);
			if (!deployNodeCluster(serviceID, clusterDtl, sessionKey, result))
				return false;
		}
		return true;
	}
	
	private boolean deployNodeCluster(String serviceID, InstanceDtlBean clusterDtl, 
			String sessionKey, ResultBean result) {
		
		String masterID = clusterDtl.getAttribute("MASTER_ID").getAttrValue();
		if (HttpUtils.isNull(masterID) || masterID.equals("null")) {
			masterID = (String)clusterDtl.getSubInstances().keySet().toArray()[0];
			CRUD c = new CRUD();
			SqlBean sqlBean = new SqlBean(UPDATE_MASTER_ID);
			sqlBean.addParams(new Object[] {masterID, clusterDtl.getInstID()});
			c.putSqlBean(sqlBean);
			try {
				c.executeUpdate();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(e.getMessage());
				return false;
			}
		}
		
		int maxMemory = Integer.parseInt(clusterDtl.getAttribute("MAX_MEMORY").getAttrValue());
		
		InstanceDtlBean masterDtl = clusterDtl.getSubInstances().get(masterID);
		String masterAddress = masterDtl.getAttribute("IP").getAttrValue()+" "+masterDtl.getAttribute("PORT").getAttrValue();
		if (!deployCacheNode(serviceID, masterDtl, maxMemory, null, sessionKey, result))
			return false;
		
		for (String instID : clusterDtl.getSubInstances().keySet()) {
			if (instID.equals(masterID))
				continue;
			InstanceDtlBean slaveDtl = clusterDtl.getSubInstances().get(instID);
			if (!deployCacheNode(serviceID, slaveDtl, maxMemory, masterAddress, sessionKey, result))
				return false;
		}
		
		if (!ConfigDataService.modInstanceDeployFlag(clusterDtl.getInstID(), CONSTS.DEPLOYED, result))
			return false;
		
		return true;
	}
	
	private boolean deployProxyList(String serviceID, List<InstanceDtlBean> proxyList, 
			String sessionKey, ResultBean result) {

		for (int i = 0; i < proxyList.size(); i++) {
			InstanceDtlBean proxyDtl = proxyList.get(i);
			if (!deployProxy(serviceID, proxyDtl, sessionKey, result))
				return false;
		}
		return true;
	}
	
	private boolean deployCacheNode(String serviceID, InstanceDtlBean instanceDtl, int maxMemory, String master, 
			String sessionKey, ResultBean result) {
		
		InstanceBean node = instanceDtl.getInstance();
		
		String id    = instanceDtl.getAttribute("CACHE_NODE_ID").getAttrValue();
		String ip    = instanceDtl.getAttribute("IP").getAttrValue();
		String port  = instanceDtl.getAttribute("PORT").getAttrValue();
		String user  = instanceDtl.getAttribute("OS_USER").getAttrValue();
		String pwd   = instanceDtl.getAttribute("OS_PWD").getAttrValue();
		
		if (node.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("cache node id:%s %s:%s is deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			return true;
		}
		
		DeployFileBean proxyFile = MetaData.get().getDeployFile(CONSTS.SERV_CACHE_NODE);
		String deployRootPath = String.format("cache_node_deploy/%s", port);;
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			String infoBegin = String.format("deploy cache node id:%s %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, infoBegin);
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			if (executor.isPortUsed(Integer.parseInt(port))) {
				DeployLog.pubLog(sessionKey, "port "+port+" is already in use......");
				return false;
			}
			
			// make deploy dir and make redis source file
			if (!executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.mkdir(deployRootPath, sessionKey);
				executor.cd("$HOME/" + deployRootPath, sessionKey);
			}
				
			// fetch and unpack deploy file
			executor.cd("$HOME/" + deployRootPath, sessionKey);
			String srcFile = String.format("%s%s", proxyFile.getFtpDir(), proxyFile.getFileName());
			String desPath = ".";
			executor.scp(proxyFile.getFtpUser(), proxyFile.getFtpPwd(),
					proxyFile.getFtpHost(), srcFile, desPath,
					proxyFile.getSshPort(), sessionKey);
			executor.tgzUnpack(proxyFile.getFileName(), sessionKey);
			executor.rm(proxyFile.getFileName(), false, sessionKey);
				
			//modify redis.conf
			String homeDir = executor.getHome();
			SCPFileUtils scp = new SCPFileUtils(ip, user, pwd, CONSTS.SSH_PORT_DEFAULT);
				
			scp.getFile(homeDir + "/" + deployRootPath + "/conf/" + CONSTS.REDIS_PROPERTIES);
			BufferedReader reader = new BufferedReader(new FileReader("./"+CONSTS.REDIS_PROPERTIES));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				String header = line.split(" ")[0];
				switch (header) {
				case "maxmemory":
					line = line.substring(0, "maxmemory".length()+1)+maxMemory+"gb";
					break;
				case "logfile":
					line = line.substring(0, "logfile".length()+1)+"log_"+port+".log";
					break;
				case "port":
					line = line.substring(0, "port".length()+1)+port;
					break;
				case "dir":
					line = line.substring(0, "dir".length()+1)+homeDir+"/"+deployRootPath+"/data";
					break;
				case "pidfile":
					line = line.substring(0, "pidfile".length()+1)+homeDir+"/"+deployRootPath+"/data/redis"+port+".pid";
					break;
				default:
					break;
				}
				sb.append(line).append("\n");
			}
			if (master != null) {
				sb.append("slaveof ").append(master).append("\n");
			}
			scp.putFile(sb.toString(), CONSTS.REDIS_PROPERTIES, homeDir + "/" + deployRootPath + "/conf");
			reader.close();
			scp.deleteLocalFile(CONSTS.REDIS_PROPERTIES);
			scp.close();
			
			//start redis instance
			executor.execSingleLine("bin/redis-server conf/redis.conf", sessionKey);
			if (!executor.waitProcessStart(port, sessionKey))
				return false;
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result))
				return false;

			String info = String.format("deploy cache node id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("deploy cache node id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			DeployLog.pubErrorLog(sessionKey, error);

			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(error);

			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
		
		return true;
	}
	
	private boolean deployProxy(String serviceID, InstanceDtlBean instanceDtl,
			String sessionKey, ResultBean result) {
		
		InstanceBean proxy = instanceDtl.getInstance();
		
		String id    = instanceDtl.getAttribute("CACHE_PROXY_ID").getAttrValue();
		String ip    = instanceDtl.getAttribute("IP").getAttrValue();
		String port  = instanceDtl.getAttribute("PORT").getAttrValue();
		String user  = instanceDtl.getAttribute("OS_USER").getAttrValue();
		String pwd   = instanceDtl.getAttribute("OS_PWD").getAttrValue();
		
		if (proxy.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("proxy id:%s %s:%s is deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			return true;
		}
		
		DeployFileBean proxyFile = MetaData.get().getDeployFile(CONSTS.SERV_CACHE_PROXY);
		String deployRootPath = String.format("cache_proxy_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			String infoBegin = String.format("deploy cache proxy id:%s %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, infoBegin);
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			if (executor.isPortUsed(Integer.parseInt(port))) {
				DeployLog.pubLog(sessionKey, "port "+port+" is already in use......");
				return false;
			}
			
			// make deploy dir
			if (!executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.mkdir(deployRootPath, sessionKey);
			}

			executor.cd("$HOME/" + deployRootPath, sessionKey);
			
			// fetch deploy file
			String srcFile = String.format("%s%s", proxyFile.getFtpDir(), proxyFile.getFileName());
			String desPath = ".";
			executor.scp(proxyFile.getFtpUser(), proxyFile.getFtpPwd(),
					proxyFile.getFtpHost(), srcFile, desPath,
					proxyFile.getSshPort(), sessionKey);
			
			// unpack deploy file
			executor.tgzUnpack(proxyFile.getFileName(), sessionKey);
			executor.rm(proxyFile.getFileName(), false, sessionKey);
			
			// modify access.sh and init.properties
			String homeDir = executor.getHome();
			SCPFileUtils scp = new SCPFileUtils(ip, user, pwd, CONSTS.SSH_PORT_DEFAULT);
			
			scp.getFile(homeDir + "/" + deployRootPath + "/bin/" + CONSTS.PROXY_SHELL);
			BufferedReader reader = new BufferedReader(new FileReader("./"+CONSTS.PROXY_SHELL));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("COMMAND=")!=-1) {
					line = line.substring(0, "COMMAND=".length())+id;
				}
				sb.append(line).append("\n");
			}
			scp.putFile(sb.toString(), CONSTS.PROXY_SHELL, homeDir + "/" + deployRootPath + "/bin");
			reader.close();
			scp.deleteLocalFile(CONSTS.PROXY_SHELL);
			
			scp.getFile(homeDir + "/" + deployRootPath + "/conf/" + CONSTS.PROXY_PROPERTIES);
			reader = new BufferedReader(new FileReader("./"+CONSTS.PROXY_PROPERTIES));
			sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("proxy.id=")!=-1) {
					line = line.substring(0, "proxy.id=".length())+id;
				}
				if (line.indexOf("metasvr.rooturl=")!=-1) {
					//TODO metaserver address
					line = line.substring(0, "metasvr.rooturl=".length())+CONSTS.METASVR_URL;
				}
				sb.append(line).append("\n");
			}
			scp.putFile(sb.toString(), CONSTS.PROXY_PROPERTIES, homeDir + "/" + deployRootPath + "/conf");
			reader.close();
			scp.deleteLocalFile(CONSTS.PROXY_PROPERTIES);
			scp.close();
			
			//start cache proxy
			executor.cd("./bin", sessionKey);
			executor.execSingleLine("./"+CONSTS.PROXY_SHELL+" start", sessionKey);
			if (!executor.waitProcessStart(port, sessionKey))
				return false;
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result))
				return false;

			String info = String.format("deploy cache proxy id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("deploy pd id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			DeployLog.pubErrorLog(sessionKey, error);

			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(error);

			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
		
		return true;
	}
	
}
