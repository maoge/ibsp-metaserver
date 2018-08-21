package ibsp.metaserver.autodeploy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.autodeploy.utils.DeployLog;
import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.ConfigDataService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.dbservice.TiDBService;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.Topology;
import io.vertx.core.json.JsonObject;

public class TiDBDeployer implements Deployer {

	private static Logger logger = LoggerFactory.getLogger(TiDBDeployer.class);

	@Override
	public boolean deployService(String serviceID, String user, String pwd, String sessionKey,
			ResultBean result) {
		
		List<InstanceDtlBean> pdServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tidbServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tikvServerList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		if (!TiDBService.loadServiceInfo(serviceID, pdServerList,
				tidbServerList, tikvServerList, collectd, result))
			return false;
		
		boolean isServDeployed = MetaData.get().isServDepplyed(serviceID);
		
		//check if the service matches minimum condition of TiDB product
		Boolean isProduct = ConfigDataService.getIsProductByServId(serviceID, result);
		if (isProduct == null) {
			return false;
		}
		if (isProduct) {
			if (pdServerList.size()<CONSTS.MIN_PD_NUMBER) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo("Number of PD is "+pdServerList.size()+", should be at least "+CONSTS.MIN_PD_NUMBER);
				return false;
			}
			if (tikvServerList.size()<CONSTS.MIN_TIKV_NUMBER) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo("Number of Tikv is "+tikvServerList.size()+", should be at least "+CONSTS.MIN_TIKV_NUMBER);
				return false;
			}
			if (tidbServerList.size()<CONSTS.MIN_TIDB_NUMBER) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo("Number of Tidb is "+tidbServerList.size()+", should be at least "+CONSTS.MIN_TIDB_NUMBER);
				return false;
			}
		}
		
		String pdList = getPDList(pdServerList);

		// deploy pd-server
		if (!deployPDServerList(serviceID, pdServerList, sessionKey, result))
			return false;

		// deploy tikv-server
		if (!deployTiKVServerList(serviceID, tikvServerList, pdList, sessionKey, result))
			return false;

		// deploy tidb-server
		if (!deployTiDBServerList(serviceID, tidbServerList, pdList, sessionKey, result))
			return false;

		// deploy collectd
		if (!DeployUtils.deployCollectd(serviceID, collectd, sessionKey, result))
			return false;
			
		if (!isServDeployed) {
			// deploy tidb root password
			if (!DeployUtils.resetDBPwd(serviceID, tidbServerList.get(0), pwd, sessionKey, result))
				return false;
			
			// mod t_service.IS_DEPLOYED = 1
			if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.DEPLOYED, result))
				return false;
			DeployUtils.publishDeployEvent(EventType.e21, serviceID);
		}
		
		return true;
	}

	@Override
	public boolean undeployService(String serviceID, String sessionKey,
			ResultBean result) {
		
		List<InstanceDtlBean> pdServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tidbServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tikvServerList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		if (!TiDBService.loadServiceInfo(serviceID, pdServerList,
				tidbServerList, tikvServerList, collectd, result))
			return false;
		
		if (!DeployUtils.undeployCollectd(collectd, sessionKey, true, result))
			return false;
		
		// undeploy tidb-server
		if (!undeployTiDBServerList(serviceID, tidbServerList, sessionKey, result))
			return false;
		
		// undeploy tikv-server
		if (!undeployTiKVServerList(serviceID, tikvServerList, sessionKey, result))
			return false;
		
		// undeploy pd-server
		if (!undeployPDServerList(serviceID, pdServerList, sessionKey, result))
			return false;
		
		// mod t_service.IS_DEPLOYED = 0
		if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.NOT_DEPLOYED, result))
			return false;
		DeployUtils.publishDeployEvent(EventType.e22, serviceID);
		
		return true;
	}
	
	@Override
	public boolean deployInstance(String serviceID, String instID,
			String sessionKey, ResultBean result) {
		
		List<InstanceDtlBean> pdServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tidbServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tikvServerList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		if (!TiDBService.loadServiceInfo(serviceID, pdServerList,
				tidbServerList, tikvServerList, collectd, result))
			return false;
		
		InstanceDtlBean instDtl = MetaDataService.getInstanceDtl(instID, result);
		if (instDtl == null) {
			String err = String.format("instance id:%s not found!", instID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		String pdList = getPDList(pdServerList);
		int cmptID = instDtl.getInstance().getCmptID();
		boolean deployRet = false;
		
		switch (cmptID) {
		case 118:    // DB_PD
			String initCluster = getPDInitCluster(pdServerList); //in --initial-cluster=xxx form
			String join = getPDString(pdServerList); //in --join=xxx form
			if (HttpUtils.isNull(join)) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(CONSTS.ERR_NO_PD_TO_JOIN);
				return false;
			}
			//when deploying a new PD server, needJoin must be true
			deployRet = deployPDServer(serviceID, instDtl, true, join, initCluster, sessionKey, result);
			break;
			
		case 119:    // DB_TIDB
			deployRet = deployTiDBServer(serviceID, instDtl, pdList, sessionKey, result);
			JsonObject paramsJson = new JsonObject();
			paramsJson.put(FixHeader.HEADER_INSTANCE_ID, instID);
			paramsJson.put(FixHeader.HEADER_INSTANCE_ADDRESS, 
					instDtl.getAttribute("IP").getAttrValue()+":"+instDtl.getAttribute("PORT").getAttrValue());
			EventBean evBean = new EventBean();
			evBean.setEvType(EventType.e71);
			evBean.setServID(serviceID);
			evBean.setUuid(MetaData.get().getUUID());
			evBean.setJsonStr(paramsJson.toString());
			EventBusMsg.publishEvent(evBean);
			
			break;
		case 120:    // DB_TIKV
			deployRet = deployTiKVServer(serviceID, instDtl, pdList, sessionKey, result);
			break;
		case 121:    // DB_COLLECTD
			deployRet = DeployUtils.deployCollectd(serviceID, instDtl, sessionKey, result);
			break;
		default:
			break;
		}
		
		return deployRet;
	}
	
	@Override
	public boolean undeployInstance(String serviceID, String instID,
			String sessionKey, ResultBean result) {
		
		List<InstanceDtlBean> pdServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tidbServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tikvServerList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		if (!TiDBService.loadServiceInfo(serviceID, pdServerList,
				tidbServerList, tikvServerList, collectd, result))
			return false;
		
		InstanceDtlBean instDtl = MetaDataService.getInstanceDtl(instID, result);
		if (instDtl == null) {
			String err = String.format("instance id:%s not found!", instID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		int cmptID = instDtl.getInstance().getCmptID();
		// if the undeploy instance is pd-server, then need to change the setting of initial-cluster
		if (cmptID == 118) {
			for (int i = 0; i < pdServerList.size(); i++) {
				InstanceDtlBean pdInstDtl = pdServerList.get(i);
				if (instDtl.getInstance().getInstID().equals(pdInstDtl.getInstance().getInstID())) {
					pdServerList.remove(i);
					break;
				}
			}
		}
		
		// String pdList = getPDList(pdServerList);
		String initCluster = getPDInitCluster(pdServerList);
		
		boolean undeployRet = false;
		switch (cmptID) {
		case 118:    // DB_PD
			undeployRet = undeployPDServer(serviceID, instDtl, initCluster, sessionKey, false, result);
			break;
		case 119:    // DB_TIDB
			undeployRet = undeployTiDBServer(serviceID, instDtl, sessionKey, false, result);
			JsonObject paramsJson = new JsonObject();
			paramsJson.put(FixHeader.HEADER_INSTANCE_ID, instID);
			EventBean evBean = new EventBean();
			evBean.setEvType(EventType.e72);
			evBean.setServID(serviceID);
			evBean.setUuid(MetaData.get().getUUID());
			evBean.setJsonStr(paramsJson.toString());
			EventBusMsg.publishEvent(evBean);
			
			break;
		case 120:    // DB_TIKV
			undeployRet = undeployTiKVServer(serviceID, instDtl, sessionKey, false, result);
			break;
		case 121:    // DB_COLLECTD
			undeployRet = DeployUtils.undeployCollectd(instDtl, sessionKey, false, result);
			break;
		default:
			break;
		}
		
		return undeployRet;
	}

	@Override
	public boolean forceUndeployInstance(String serviceID, String instID, ResultBean result) {
		return false;
	}

	@Override
	public boolean deleteService(String serviceID, String sessionKey, ResultBean result) {
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
		
		return true;
	}
	
	@Override
	public boolean deleteInstance(String serviceID, String instID, String sessionKey, ResultBean result) {
		return MetaDataService.deleteInstance(serviceID, instID, result);
	}

	private boolean deployPDServerList(String serviceID, List<InstanceDtlBean> pdServerList,
			String sessionKey, ResultBean result) {

		String initCluster = getPDInitCluster(pdServerList);
		
		for (int i = 0; i < pdServerList.size(); i++) {
			InstanceDtlBean pdInstDtl = pdServerList.get(i);
			
			if (!deployPDServer(serviceID, pdInstDtl, false, "", initCluster, sessionKey, result))
				return false;
		}
		
		return true;
	}

	private boolean deployTiKVServerList(String serviceID, List<InstanceDtlBean> tikvServerList,
			String pdList, String sessionKey, ResultBean result) {
		
		for (int i = 0; i < tikvServerList.size(); i++) {
			InstanceDtlBean tikvInstDtl = tikvServerList.get(i);
			
			if (!deployTiKVServer(serviceID, tikvInstDtl, pdList, sessionKey, result))
				return false;
		}
		
		return true;
	}

	private boolean deployTiDBServerList(String serviceID, List<InstanceDtlBean> tidbServerList,
			String pdList, String sessionKey, ResultBean result) {
		
		for (int i = 0; i < tidbServerList.size(); i++) {
			InstanceDtlBean tidbInstDtl = tidbServerList.get(i);
			
			if (!deployTiDBServer(serviceID, tidbInstDtl, pdList, sessionKey, result))
				return false;
		}
		
		return true;
	}
	
	private boolean undeployPDServerList(String serviceID, List<InstanceDtlBean> pdServerList,
			String sessionKey, ResultBean result) {

		for (int i = 0; i < pdServerList.size(); i++) {
			InstanceDtlBean pdInstBean = pdServerList.get(i);

			if (!undeployPDServer(serviceID, pdInstBean, "", sessionKey, true, result)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean undeployTiKVServerList(String serviceID, List<InstanceDtlBean> tikvServerList,
			String sessionKey, ResultBean result) {
		
		for (int i = 0; i < tikvServerList.size(); i++) {
			InstanceDtlBean tikvInstDtlBean = tikvServerList.get(i);
			if (!undeployTiKVServer(serviceID, tikvInstDtlBean, sessionKey, true, result)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean undeployTiDBServerList(String serviceID, List<InstanceDtlBean> tidbServerList,
			String sessionKey, ResultBean result) {
		
		for (int i = 0; i < tidbServerList.size(); i++) {
			InstanceDtlBean tidbInstDtlBean = tidbServerList.get(i);
			if (!undeployTiDBServer(serviceID, tidbInstDtlBean, sessionKey, true, result)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean deployPDServer(String serviceID, InstanceDtlBean instanceDtl,
			boolean needJoin, String join, String initCluster,
			String sessionKey, ResultBean result) {
		
		// pd_deploy/service_port
		//                      |----- start.sh
		//                      |----- stop.sh
		//                      |----- bin
		//                               |----- pd-server
		//                               |----- pd-ctl
		//                               |----- pd-ctl
		//                               |----- pd-tso-bench
		//                      |----- data
		//                      |----- log
		
		InstanceBean pdInstance = instanceDtl.getInstance();
		
		String id    = instanceDtl.getAttribute("PD_ID").getAttrValue();
		String ip    = instanceDtl.getAttribute("IP").getAttrValue();
		String port  = instanceDtl.getAttribute("PORT").getAttrValue();
		String cPort = instanceDtl.getAttribute("CLUSTER_PORT").getAttrValue();
		String user  = instanceDtl.getAttribute("OS_USER").getAttrValue();
		String pwd   = instanceDtl.getAttribute("OS_PWD").getAttrValue();
		
		String clientUrl = String.format("http://%s:%s", ip, port);
		String peerUrl   = String.format("http://%s:%s", ip, cPort);
		String dataDir   = "data";
		String logFile   = "log/pd.log";

		String startContext = needJoin ? DeployUtils.getPdJoinStartCmd(id, clientUrl, peerUrl, dataDir, logFile, join) :
			DeployUtils.getPdInitStartCmd(id, clientUrl, peerUrl, dataDir, logFile, initCluster);
		
		String stopContext = DeployUtils.getPdStopCmd(id);

		if (pdInstance.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("pd id:%s %s:%s is deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

			return true;
		}
		
		DeployFileBean pdFile = MetaData.get().getDeployFile(CONSTS.SERV_DB_PD);

		String deployRootPath = String.format("pd_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;

		try {
			String infoBegin = String.format("deploy pd id:%s %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, infoBegin);
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			if (executor.isPortUsed(Integer.parseInt(port))) {
				DeployLog.pubLog(sessionKey, "port "+port+" is already in use......");
				return false;
			}
			if (executor.isPortUsed(Integer.parseInt(cPort))) {
				DeployLog.pubLog(sessionKey, "port "+cPort+" is already in use......");
				return false;
			}
			
			// make deploy dir
			if (!executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.mkdir(deployRootPath, sessionKey);
			}

			executor.cd("$HOME/" + deployRootPath, sessionKey);
			executor.mkdir("data", sessionKey);
			executor.mkdir("log", sessionKey);
			
			// fetch deploy file
			String srcFile = String.format("%s%s", pdFile.getFtpDir(), pdFile.getFileName());
			String desPath = ".";
			executor.scp(pdFile.getFtpUser(), pdFile.getFtpPwd(),
					pdFile.getFtpHost(), srcFile, desPath,
					pdFile.getSshPort(), sessionKey);
			
			// unpack deploy file
			executor.tgzUnpack(pdFile.getFileName(), sessionKey);
			executor.rm(pdFile.getFileName(), false, sessionKey);
			
			// create start shell and create stop shell
			if (!executor.createStartShell(startContext)) {
				DeployLog.pubLog(sessionKey, "create pd start shell fail ......");
				return false;
			}
			
			// create stop shell
			if (!executor.createStopShell(stopContext)) {
				DeployLog.pubLog(sessionKey, "create pd stop shell fail ......");
				return false;
			}
			
			// start pd-server
			if (!DeployUtils.execStartShell(executor, port, sessionKey)) {
				DeployLog.pubLog(sessionKey, "exec pd start shell fail ......");
				return false;
			}
			
			//if needJoin, relace --join with --initial-cluster here
			if (needJoin) {
				startContext = startContext.replace("--join="+join, "--initial-cluster="+initCluster);
				if (!executor.createStartShell(startContext)) {
					DeployLog.pubLog(sessionKey, "update pd start shell fail ......");
					return false;
				}
			}
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result))
				return false;
			DeployUtils.publishDeployEvent(EventType.e23, id);
			
			if(needJoin) {
				//然后更新所有的pd脚本、tikv脚本、tidb脚本
				List<InstanceDtlBean> pdServerList   = new ArrayList<InstanceDtlBean>();
				List<InstanceDtlBean> tikvServerList = new ArrayList<InstanceDtlBean>();
				List<InstanceDtlBean> tidbServerList = new ArrayList<InstanceDtlBean>();
				InstanceDtlBean collectd = new InstanceDtlBean();
				TiDBService.loadServiceInfo(serviceID, pdServerList, tidbServerList, tikvServerList, collectd, result);
				refreshDbCompCmd(pdServerList, tikvServerList, tidbServerList, sessionKey);
			}

			String info = String.format("deploy pd id:%s %s:%s success ......", id, ip, port);
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
	
	private boolean deployTiKVServer(String serviceID, InstanceDtlBean instanceDtl,
			String pdList, String sessionKey, ResultBean result) {
		
		// tikv_deploy/service_port
		//                      |----- start.sh
		//                      |----- stop.sh
		//                      |----- bin
		//                               |----- tikv-server
		//                               |----- tikv-ctl
		//                      |----- data
		//                      |----- log

		InstanceBean tikvInstance = instanceDtl.getInstance();
		
		String id   = instanceDtl.getAttribute("TIKV_ID").getAttrValue();
		String ip   = instanceDtl.getAttribute("IP").getAttrValue();
		String port = instanceDtl.getAttribute("PORT").getAttrValue();
		String user = instanceDtl.getAttribute("OS_USER").getAttrValue();
		String pwd  = instanceDtl.getAttribute("OS_PWD").getAttrValue();
		
		String dataDir = "data";
		String logFile = "log/tikv.log";
		
		String startContext = DeployUtils.getTikvStartCmd(ip, port, pdList, dataDir, logFile);
		String stopContext  = DeployUtils.getTikvStopCmd(ip, port);
		
		if (tikvInstance.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("tikv id:%s %s:%s is deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

			return true;
		}
		
		String deployRootPath = String.format("tikv_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		DeployFileBean tikvFile = MetaData.get().getDeployFile(CONSTS.SERV_DB_TIKV);
		
		try {
			String startInfo = String.format("deploy tikv id:%s %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, startInfo);
			
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
			executor.mkdir("data", sessionKey);
			executor.mkdir("log", sessionKey);
		
			// fetch deploy file
			String srcFile = String.format("%s%s", tikvFile.getFtpDir(), tikvFile.getFileName());
			String desPath = ".";
			executor.scp(tikvFile.getFtpUser(), tikvFile.getFtpPwd(),
					tikvFile.getFtpHost(), srcFile, desPath,
					tikvFile.getSshPort(), sessionKey);
			
			// unpack deploy file
			executor.tgzUnpack(tikvFile.getFileName(), sessionKey);
			executor.rm(tikvFile.getFileName(), false, sessionKey);

			String[] metaUrls = MetaData.get().getMetaServUrls().split(",");

			executor.cd("./conf", sessionKey);
			executor.sed(CONSTS.TIKV_METRIC_ADDRESS, metaUrls[0], CONSTS.TIKV_TOML, sessionKey);
			executor.sed(CONSTS.TIKV_METRIC_JOB, id, CONSTS.TIKV_TOML, sessionKey);

			executor.cd("..", sessionKey);
			// create start shell
			if (!executor.createStartShell(startContext)) {
				DeployLog.pubLog(sessionKey, "create tikv start shell fail ......");
				return false;
			}
			
			// create stop shell
			if (!executor.createStopShell(stopContext)) {
				DeployLog.pubLog(sessionKey, "create tikv stop shell fail ......");
				return false;
			}

			// start tikv-server
			if (!DeployUtils.execStartShell(executor, port, sessionKey)) {
				DeployLog.pubLog(sessionKey, "exec tikv start shell fail ......");
				return false;
			}
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result))
				return false;
			DeployUtils.publishDeployEvent(EventType.e23, id);
			
			String info = String.format("deploy tikv id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("deploy tikv id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
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
	
	private boolean deployTiDBServer(String serviceID, InstanceDtlBean instanceDtl,
			String pdList, String sessionKey, ResultBean result) {
		
		// tidb_deploy/service_port
		//                      |----- start.sh
		//                      |----- stop.sh
		//                      |----- bin
		//                               |----- tidb-server
		//                               |----- goyacc
		//                      |----- log
		
		InstanceBean tidbInstance = instanceDtl.getInstance();
		
		String id   = instanceDtl.getAttribute("TIDB_ID").getAttrValue();
		String ip   = instanceDtl.getAttribute("IP").getAttrValue();
		String port = instanceDtl.getAttribute("PORT").getAttrValue();
		String statPort = instanceDtl.getAttribute("STAT_PORT").getAttrValue();
		String user = instanceDtl.getAttribute("OS_USER").getAttrValue();
		String pwd  = instanceDtl.getAttribute("OS_PWD").getAttrValue();
		
		String logFile = "log/tidb.log";
		
		String startContext = DeployUtils.getTidbStartCmd(ip, port, logFile, pdList, statPort);
		
		String stopContext = DeployUtils.getTidbStopCmd(ip, port);
		
		if (tidbInstance.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("tidb id:%s %s:%s is deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

			return true;
		}
		
		String deployRootPath = String.format("tidb_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		DeployFileBean tidbFile = MetaData.get().getDeployFile(CONSTS.SERV_DB_TIDB);
		
		try {
			String startInfo = String.format("deploy tidb id:%s %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, startInfo);
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			if (executor.isPortUsed(Integer.parseInt(port))) {
				DeployLog.pubLog(sessionKey, "port "+port+" is already in use......");
				return false;
			}
			if (executor.isPortUsed(Integer.parseInt(statPort))) {
				DeployLog.pubLog(sessionKey, "port "+statPort+" is already in use......");
				return false;
			}
			
			// make deploy dir
			if (!executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.mkdir(deployRootPath, sessionKey);
			}

			executor.cd("$HOME/" + deployRootPath, sessionKey);
			executor.mkdir("log", sessionKey);
			
			// fetch deploy file
			String srcFile = String.format("%s%s", tidbFile.getFtpDir(), tidbFile.getFileName());
			String desPath = ".";
			executor.scp(tidbFile.getFtpUser(), tidbFile.getFtpPwd(),
					tidbFile.getFtpHost(), srcFile, desPath,
					tidbFile.getSshPort(), sessionKey);
			
			// unpack deploy file
			executor.tgzUnpack(tidbFile.getFileName(), sessionKey);
			executor.rm(tidbFile.getFileName(), false, sessionKey);
			
			// create start shell
			if (!executor.createStartShell(startContext)) {
				DeployLog.pubLog(sessionKey, "create tidb start shell fail ......");
				return false;
			}
			
			// create stop shell
			if (!executor.createStopShell(stopContext)) {
				DeployLog.pubLog(sessionKey, "create tidb stop shell fail ......");
				return false;
			}
			
			// start tidb-server
			if (!DeployUtils.execStartShell(executor, port, sessionKey)) {
				DeployLog.pubLog(sessionKey, "exec tidb start shell fail ......");
				return false;
			}
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result)) {
				return false;
			}
			DeployUtils.publishDeployEvent(EventType.e23, id);
			
			String info = String.format("deploy tidb id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("deploy tidb id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
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
	
	private boolean undeployPDServer(String serviceID, InstanceDtlBean pdInstDtlBean,
			String initCluster, String sessionKey, boolean isUndeployService, ResultBean result) {
		
		InstanceBean pdInstance = pdInstDtlBean.getInstance();

		String id    = pdInstDtlBean.getAttribute("PD_ID").getAttrValue();
		String ip    = pdInstDtlBean.getAttribute("IP").getAttrValue();
		String port  = pdInstDtlBean.getAttribute("PORT").getAttrValue();
		String user  = pdInstDtlBean.getAttribute("OS_USER").getAttrValue();
		String pwd   = pdInstDtlBean.getAttribute("OS_PWD").getAttrValue();
		
		List<InstanceDtlBean> pdServerList = new ArrayList<InstanceDtlBean>();
		
		if (pdInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("pd id:%s %s:%s is not deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

			return true;
		}

		String deployRootPath = String.format("pd_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;

		try {
			String infoBegin = String.format("undeploy pd id:%s %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, infoBegin);
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			executor.cd("$HOME/" + deployRootPath, sessionKey);
			
			if (!isUndeployService) {
				//如果失败，可能是这个pd服务没有起来，尝试其它的pd节点进行pd缩容
				boolean deletePdMember = executor.pdctlDeletePdMember(ip, port, id, sessionKey);
				if(!deletePdMember) {
					TiDBService.getPDInfoByServIdOrServiceStub(serviceID, null, pdServerList, result);
					for(InstanceDtlBean pd : pdServerList) {
						String otherId    = pd.getAttribute("PD_ID").getAttrValue();
						String otherIp    = pd.getAttribute("IP").getAttrValue();
						String otherPort  = pd.getAttribute("PORT").getAttrValue();
						if(executor.pdctlDeletePdMember(otherIp, otherPort, otherId, sessionKey)) {
							deletePdMember = Boolean.TRUE;
							break;
						}
					}
				}
				
				if(!deletePdMember) {
					DeployLog.pubLog(sessionKey, "exec pd delete memeber shell fail ......");
					
					result.setRetCode(CONSTS.REVOKE_NOK);
					result.setRetInfo("exec pd delete memeber shell fail ......");
					
					return false;
				}
			}
				
			// cd deploy dir, exec stop shell, rm deploy dir
			if (executor.isDirExistInCurrPath("$HOME/"+deployRootPath, sessionKey)) {
				executor.cd("$HOME/" + deployRootPath, sessionKey);
				
				// stop pd-server
				if (executor.isPortUsed(port, sessionKey)) {
					if (!DeployUtils.execStopShell(executor, port, sessionKey)) {
						DeployLog.pubLog(sessionKey, "exec pd stop shell fail ......");
						return false;
					}
				}
				
				executor.cd("$HOME", sessionKey);
				executor.rm(deployRootPath, true, sessionKey);
			}
				
			// mod t_instance.IS_DEPLOYED = 0
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.NOT_DEPLOYED, result)) {
				return false;
			}
			DeployUtils.publishDeployEvent(EventType.e24, id);
	
			if (!isUndeployService) {
				// 重新从数据库获取所有的PD的信息，然后刷新PD脚本 再从数据库所有的tikv信息，刷新tikv的脚本
				List<InstanceDtlBean> tikvServerList = new ArrayList<InstanceDtlBean>();
				List<InstanceDtlBean> tidbServerList = new ArrayList<InstanceDtlBean>();
				InstanceDtlBean collectd = new InstanceDtlBean();
				TiDBService.loadServiceInfo(serviceID, pdServerList, tidbServerList, tikvServerList, collectd, result);
				for(InstanceDtlBean pd : pdServerList) {
					if(pd.getAttribute("PD_ID").getAttrValue().equals(id)) {
						pdServerList.remove(pd);
						break;
					}
				}
				refreshDbCompCmd(pdServerList, tikvServerList, tidbServerList, sessionKey);
			}
			
			String info = String.format("undeploy pd id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);

			String error = String.format("undeploy pd id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
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
	
	private boolean undeployTiKVServer(String serviceID, InstanceDtlBean tikvInstDtlBean,
			String sessionKey, boolean isUndeployService, ResultBean result) {
		
		InstanceBean tikvInstance = tikvInstDtlBean.getInstance();
		
		String id   = tikvInstDtlBean.getAttribute("TIKV_ID").getAttrValue();
		String ip   = tikvInstDtlBean.getAttribute("IP").getAttrValue();
		String port = tikvInstDtlBean.getAttribute("PORT").getAttrValue();
		String user = tikvInstDtlBean.getAttribute("OS_USER").getAttrValue();
		String pwd  = tikvInstDtlBean.getAttribute("OS_PWD").getAttrValue();
		
		if (tikvInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("tikv id:%s %s:%s is not deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

			return true;
		}
		
		List<InstanceDtlBean> pdServerList = new ArrayList<InstanceDtlBean>();
		
		//获取所有pd的地址，后面取第一个pd用来执行tikv的remove操作
		TiDBService.getPDInfoByServIdOrServiceStub(serviceID, null, pdServerList, result);
		if(pdServerList==null || pdServerList.size() == 0) {
			DeployLog.pubErrorLog(sessionKey, "this tikv not belong to any pd-server or the data error ...... ");
			return false;
		}
		
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			if (isUndeployService) {
				checkTikvStatus(serviceID, id, ip, port, user, pwd, 0);
			} else {
				String startInfo = String.format("undeploy tikv id:%s %s:%s begin ......", id, ip, port);
				DeployLog.pubSuccessLog(sessionKey, startInfo);
				
				//获取pd集群的第一个pd 
				InstanceDtlBean pd = pdServerList.get(0);
				String pdIp    = pd.getAttribute("IP").getAttrValue();
				String pdPort  = pd.getAttribute("PORT").getAttrValue();
				String pdUser = pd.getAttribute("OS_USER").getAttrValue();
				String pdPwd  = pd.getAttribute("OS_PWD").getAttrValue();
				
				ui = new JschUserInfo(pdUser, pdPwd, pdIp, CONSTS.SSH_PORT_DEFAULT);
				executor = new SSHExecutor(ui);
				executor.connect();
				connected = true;
				String deployPdRootPath = String.format("$HOME/pd_deploy/%s", pdPort);
				executor.cd(deployPdRootPath, sessionKey);
				
				//获取这个tikv的id(tidb中自己生成的id)，根据id来删除
				int tikvId = executor.getStoreId(pdIp, pdPort, ip, port);
				if(tikvId != 0 && !executor.pdctlDeleteTikvStore(pdIp, pdPort, tikvId, sessionKey)) {
					DeployLog.pubErrorLog(sessionKey, "remove tikv from pd-cluster false ...... ");
					return false;
				}
				
				checkTikvStatus(serviceID, id, ip, port, user, pwd, tikvId);
			}
			
			String info = String.format("undeploy tikv id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubErrorLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("undeploy tikv id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
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
	
	private boolean undeployTiDBServer(String serviceID, InstanceDtlBean tidbInstDtlBean,
			String sessionKey, boolean isUndeployService, ResultBean result) {
		
		InstanceBean tidbInstance = tidbInstDtlBean.getInstance();
		
		String id   = tidbInstDtlBean.getAttribute("TIDB_ID").getAttrValue();
		String ip   = tidbInstDtlBean.getAttribute("IP").getAttrValue();
		String port = tidbInstDtlBean.getAttribute("PORT").getAttrValue();
		String user = tidbInstDtlBean.getAttribute("OS_USER").getAttrValue();
		String pwd  = tidbInstDtlBean.getAttribute("OS_PWD").getAttrValue();
		
		if (tidbInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("tidb id:%s %s:%s is not deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			
			return true;
		}
		
		String deployRootPath = String.format("tidb_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			String startInfo = String.format("undeploy tidb id:%s %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, startInfo);
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			// cd deploy dir, exec stop shell, rm deploy dir
			if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.cd("$HOME/" + deployRootPath, sessionKey);
				
				// stop tidb-server
				if (executor.isPortUsed(port, sessionKey)) {
					if (!DeployUtils.execStopShell(executor, port, sessionKey)) {
						DeployLog.pubLog(sessionKey, "exec tidb stop shell fail ......");
						return false;
					}
				}
				
				executor.cd("$HOME", sessionKey);
				executor.rm(deployRootPath, true, sessionKey);
			}
			
			// mod t_instance.IS_DEPLOYED = 0
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.NOT_DEPLOYED, result))
				return false;
			DeployUtils.publishDeployEvent(EventType.e24, id);
			
			String info = String.format("undeploy tidb id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("undeploy tidb id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
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
	
	private String getPDString(List<InstanceDtlBean> pdServerList) {
		//TODO remove dead pd servers
		StringBuilder result = new StringBuilder("");
		for (InstanceDtlBean instance : pdServerList) {
			if (instance.getInstance().getIsDeployed().equals("1")) {
				String ip = instance.getAttribute("IP").getAttrValue();
				String port = instance.getAttribute("PORT").getAttrValue();
				result.append("http://"+ip+":"+port+",");
			}
		}
		result.deleteCharAt(result.length()-1);
		return result.toString();
	}

	private String getPDInitCluster(List<InstanceDtlBean> pdServerList) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pdServerList.size(); i++) {
			InstanceDtlBean pdInfo = pdServerList.get(i);
			String id = pdInfo.getAttribute("PD_ID").getAttrValue();
			String ip = pdInfo.getAttribute("IP").getAttrValue();
			String cPort = pdInfo.getAttribute("CLUSTER_PORT").getAttrValue();

			String s = String.format("%s=http://%s:%s", id, ip, cPort);
			if (i > 0)
				sb.append(CONSTS.PATH_COMMA);

			sb.append(s);
		}

		return sb.toString();
	}
	
	private String getPDList(List<InstanceDtlBean> pdServerList) {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (InstanceDtlBean pd : pdServerList) {
			if(!isFirst) {
				sb.append(CONSTS.PATH_COMMA);
			}
			sb.append(pd.getAttribute("IP").getAttrValue()).append(":")
				.append(pd.getAttribute("PORT").getAttrValue());
			isFirst = false;
		}

		return sb.toString();
	}
	
	//检查tikv的状态，停止tikv 最后删除目录
	private void checkTikvStatus(String servId, String id, String ip, String port, String user, String password, int tikvId) {
		new Thread(new Runnable() {
			public void run() {
				JschUserInfo ui = null;
				SSHExecutor executor = null;
				boolean connected = false;
				String deployRootPath = "$HOME/tikv_deploy/" + port;
				ResultBean result = new ResultBean();

				try {
					ui = new JschUserInfo(user, password, ip, CONSTS.SSH_PORT_DEFAULT);
					executor = new SSHExecutor(ui);
					executor.connect();
					connected = true;
					executor.cd(deployRootPath);
					if(tikvId != 0) {
						while(!TiDBService.getTikvStatusByTikvId(servId, tikvId, result)) {
							Thread.sleep(CONSTS.TIKV_STATE_CHECK_INTERVAL);
						}
					}
					// stop tikv-server
					if (executor.isPortUsed(port,"")) {
						if (!DeployUtils.execStopShell(executor, port, "")) {
							logger.error(String.format("stop tikv[%s:%s] shell faild", ip, port));
						}
					}
					//删除目录
					executor.rm(deployRootPath, true, "");
					// mod t_instance.IS_DEPLOYED = 0
					if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.NOT_DEPLOYED, result)) {
						logger.error(result.getRetInfo());
					}
					DeployUtils.publishDeployEvent(EventType.e24, id);
				}catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}finally {
					if(connected) {
						executor.close();
					}
				}
			}
		}).start();
	}
	
	private boolean refreshDbCompCmd(List<InstanceDtlBean> pdServerList, List<InstanceDtlBean> tikvServerList
			,List<InstanceDtlBean> tidbServerList, String sessionKey) {
		return refreshPdStartCmd(pdServerList,sessionKey) && 
				refreshTikvStartCmd(tikvServerList, getPDList(pdServerList), sessionKey) && 
				refreshTidbCmd(tidbServerList, getPDList(pdServerList), sessionKey);
	}
	
	private boolean refreshPdStartCmd(List<InstanceDtlBean> pdServerList, String sessionKey) {
		String initCluster = getPDInitCluster(pdServerList);
		// ssh all pd and replace start shell
		for(InstanceDtlBean pd : pdServerList) {
			generatePdCmd(pd, initCluster, sessionKey);	
		}
		return Boolean.TRUE;
	}
	
	private boolean refreshTikvStartCmd(List<InstanceDtlBean> tikvServerList, String pdList, String sessionKey) {
		for(InstanceDtlBean tikv : tikvServerList) {
			generateTikvCmd(tikv, pdList, sessionKey);
		}
		return Boolean.TRUE;
	}
	
	private boolean refreshTidbCmd(List<InstanceDtlBean> tidbServerList, String pdList, String sessionKey) {
		for(InstanceDtlBean tidb : tidbServerList) {
			generateTidbCmd(tidb, pdList, sessionKey);
		}
		return Boolean.TRUE;
	}
	
	private boolean generatePdCmd(InstanceDtlBean pd, String initCluster, String sessionKey) {
		String ip        = pd.getAttribute("IP").getAttrValue();
		String user      = pd.getAttribute("OS_USER").getAttrValue();
		String pwd       = pd.getAttribute("OS_PWD").getAttrValue();
		
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			generatePdCmdBySSH(executor, pd, initCluster, sessionKey);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			String error = String.format("update pd shell error:%s", e.getMessage());
			DeployLog.pubErrorLog(sessionKey, error);
			
			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}	
		return true;
	}
	
	private boolean generateTikvCmd(InstanceDtlBean tikv, String pdList, String sessionKey) {
		String ip        = tikv.getAttribute("IP").getAttrValue();
		String user      = tikv.getAttribute("OS_USER").getAttrValue();
		String pwd       = tikv.getAttribute("OS_PWD").getAttrValue();
		
		JschUserInfo ui      = null;
		SSHExecutor executor = null;
		boolean connected    = false;
		
		try {
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			generateTikvCmdBySSH(executor, tikv, pdList, sessionKey);
			
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			String error = String.format("update tikv shell error:%s", e.getMessage());
			DeployLog.pubErrorLog(sessionKey, error);
			
			return Boolean.FALSE;
		} finally {
			if (connected) {
				executor.close();
			}
		}	
		return Boolean.TRUE;
	}
	
	private boolean generateTidbCmd(InstanceDtlBean tidb, String pdList, String sessionKey) {
		String ip   = tidb.getAttribute("IP").getAttrValue();
		String user = tidb.getAttribute("OS_USER").getAttrValue();
		String pwd  = tidb.getAttribute("OS_PWD").getAttrValue();
		
		JschUserInfo ui      = null;
		SSHExecutor executor = null;
		boolean connected    = false;
		
		try {
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			generateTidbCmdBySSH(executor, tidb, pdList, sessionKey);
			
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			String error = String.format("update tidb shell error:%s", e.getMessage());
			DeployLog.pubErrorLog(sessionKey, error);
			
			return Boolean.FALSE;
		} finally {
			if (connected) {
				executor.close();
			}
		}	
		
		return Boolean.TRUE;
	}
	
	private boolean generateTidbCmdBySSH(SSHExecutor executor, InstanceDtlBean tidb, String pdList, String sessionKey) throws InterruptedException {
		String id       = tidb.getAttribute("TIDB_ID").getAttrValue();
		String ip       = tidb.getAttribute("IP").getAttrValue();
		String port     = tidb.getAttribute("PORT").getAttrValue();
		String statPort = tidb.getAttribute("STAT_PORT").getAttrValue();

		String logFile   = "log/tidb.log";
		
		String deployRootPath = String.format("$HOME/tidb_deploy/%s", port);
		String startShell = DeployUtils.getTidbStartCmd(ip, port, logFile, pdList, statPort);	
		String stopShell  = DeployUtils.getTidbStopCmd(ip, port);
		
		if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
			executor.cd(deployRootPath, sessionKey);
			executor.createStartShell(startShell);
			executor.createStopShell(stopShell);
			String log = String.format("tidb[%s:%s]%s create Shell success! ", ip, port, id);
			DeployLog.pubLog(sessionKey, log.toString());
		}
		
		return Boolean.TRUE;
	}
	
	private boolean generatePdCmdBySSH(SSHExecutor executor, InstanceDtlBean pd, String initCluster, String sessionKey) throws InterruptedException {
		String ip        = pd.getAttribute("IP").getAttrValue();
		String id        = pd.getAttribute("PD_ID").getAttrValue();
		String port      = pd.getAttribute("PORT").getAttrValue();
		String cPort     = pd.getAttribute("CLUSTER_PORT").getAttrValue();
		String clientUrl = String.format("http://%s:%s", ip, port);
		String peerUrl   = String.format("http://%s:%s", ip, cPort);
		String dataDir   = "data";
		String logFile   = "log/pd.log";
		
		String deployRootPath = String.format("$HOME/pd_deploy/%s", port);
		String startShell = DeployUtils.getPdInitStartCmd(id, clientUrl, peerUrl, dataDir, logFile, initCluster);
		String stopShell  = DeployUtils.getPdStopCmd(id);
		
		if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
			executor.cd(deployRootPath, sessionKey);
			executor.createStartShell(startShell);
			executor.createStopShell(stopShell);
			String log = String.format("pd[%s:%s]%s create Shell success! ", ip, port, id);
			DeployLog.pubLog(sessionKey, log.toString());
		}
		
		return Boolean.TRUE;
	}
	
	private boolean generateTikvCmdBySSH(SSHExecutor executor, InstanceDtlBean tikv, String pdList, String sessionKey) throws InterruptedException {
		String id   = tikv.getAttribute("TIKV_ID").getAttrValue();
		String ip   = tikv.getAttribute("IP").getAttrValue();
		String port = tikv.getAttribute("PORT").getAttrValue();

		String dataDir   = "data";
		String logFile   = "log/tikv.log";
		
		String deployRootPath = String.format("$HOME/tikv_deploy/%s", port);
		String startShell = DeployUtils.getTikvStartCmd(ip, port, pdList, dataDir, logFile);	
		String stopShell  = DeployUtils.getTikvStopCmd(ip, port);
		
		if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
			executor.cd(deployRootPath, sessionKey);
			executor.createStartShell(startShell);
			executor.createStopShell(stopShell);
			String log = String.format("tikv[%s:%s]%s create Shell success! ", ip, port, id);
			DeployLog.pubLog(sessionKey, log.toString());
		}
		
		return Boolean.TRUE;
	}
	
}
