package ibsp.metaserver.autodeploy;

import java.util.LinkedList;
import java.util.List;

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
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;

// TODO PD changes need to modify the configuration of tikv-server and tidb-server

public class TiDBDeployer implements Deployer {

	private static Logger logger = LoggerFactory.getLogger(TiDBDeployer.class);

	@Override
	public boolean deployService(String serviceID, String sessionKey,
			ResultBean result) {
		
		List<InstanceDtlBean> pdServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tidbServerList = new LinkedList<InstanceDtlBean>();
		List<InstanceDtlBean> tikvServerList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		if (!TiDBService.loadServiceInfo(serviceID, pdServerList,
				tidbServerList, tikvServerList, collectd, result))
			return false;
		
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
		if (!deployDBCollectd(serviceID, collectd, pdList, sessionKey, result))
			return false;
			
		// mod t_service.IS_DEPLOYED = 1
		if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.DEPLOYED, result))
			return false;
		
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
		
		if (!undeployDBCollectd(collectd, sessionKey, result))
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
		
		String initCluster = getPDInitCluster(pdServerList);
		InstanceDtlBean firstPD = pdServerList.get(0);
		String join = getClusterString(firstPD);
		boolean needJoin = pdServerList.size() > 0;
		
		int cmptID = instDtl.getInstance().getCmptID();
		boolean deployRet = false;
		switch (cmptID) {
		case 118:    // DB_PD
			deployRet = deployPDServer(serviceID, instDtl, needJoin, join, initCluster, sessionKey, result);
			break;
		case 119:    // DB_TIDB
			deployRet = deployTiDBServer(serviceID, instDtl, pdList, sessionKey, result);
			break;
		case 120:    // DB_TIKV
			deployRet = deployTiKVServer(serviceID, instDtl, pdList, sessionKey, result);
			break;
		case 121:    // DB_COLLECTD
			deployRet = deployDBCollectd(serviceID, instDtl, pdList, sessionKey, result);
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
			undeployRet = undeployPDServer(serviceID, instDtl, initCluster, sessionKey, result);
			break;
		case 119:    // DB_TIDB
			undeployRet = undeployTiDBServer(serviceID, instDtl, sessionKey, result);
			break;
		case 120:    // DB_TIKV
			undeployRet = undeployTiKVServer(serviceID, instDtl, sessionKey, result);
			break;
		case 121:    // DB_COLLECTD
			undeployRet = undeployDBCollectd(instDtl, sessionKey, result);
			break;
		default:
			break;
		}
		
		return undeployRet;
	}

	private boolean deployPDServerList(String serviceID, List<InstanceDtlBean> pdServerList,
			String sessionKey, ResultBean result) {

		String initCluster = getPDInitCluster(pdServerList);
		InstanceDtlBean firstPD = pdServerList.get(0);
		String join = getClusterString(firstPD);
		
		for (int i = 0; i < pdServerList.size(); i++) {
			InstanceDtlBean pdInstDtl = pdServerList.get(i);
			boolean needJoin = i > 0;
			
			if (!deployPDServer(serviceID, pdInstDtl, needJoin, join, initCluster, sessionKey, result))
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

			if (!undeployPDServer(serviceID, pdInstBean, "", sessionKey, result)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean undeployTiKVServerList(String serviceID, List<InstanceDtlBean> tikvServerList,
			String sessionKey, ResultBean result) {
		
		for (int i = 0; i < tikvServerList.size(); i++) {
			InstanceDtlBean tikvInstDtlBean = tikvServerList.get(i);
			if (!undeployTiKVServer(serviceID, tikvInstDtlBean, sessionKey, result)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean undeployTiDBServerList(String serviceID, List<InstanceDtlBean> tidbServerList,
			String sessionKey, ResultBean result) {
		
		for (int i = 0; i < tidbServerList.size(); i++) {
			InstanceDtlBean tidbInstDtlBean = tidbServerList.get(i);
			if (!undeployTiDBServer(serviceID, tidbInstDtlBean, sessionKey, result)) {
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
		
		String startContext = String.format("bin/pd-server --name=%s \\\\\n"
				+ "    --client-urls=%s --peer-urls=%s \\\\\n"
				+ "    --data-dir=%s --initial-cluster=%s \\\\\n"
				+ "    -L info --log-file=%s &",
				id, clientUrl, peerUrl, dataDir, initCluster, logFile);
		
		String stopContext = String.format("var=%s\\n"
				+ "pid=\\`ps -ef | grep \\\"\\${var}\\\" | awk '{print \\$1, \\$2, \\$8}' | grep pd-server | awk '{print \\$2}'\\`\\n"
				+ "if [ \\\"\\${pid}\\\" != \\\"\\\" ]\\n"
				+ "then\\n"
				+ "    kill -9 \\$pid\\n"
				+ "    echo stop pd-server pid:\\$pid\\n"
				+ "else\\n"
				+ "    echo stop pd-server not running\\n"
				+ "fi\\n",
				id);

		if (pdInstance.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("pd id:%s %s:%s is deployed ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return true;
		}
		
		DeployFileBean pdFile = MetaData.get().getDeployFile(CONSTS.SERV_DB_PD);

		String deployRootPath = String.format("pd_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;

		try {
			String infoBegin = String.format("deploy pd id:%s %s:%s begin ......", id, ip, port);
			StringBuffer deployBeginLog = new StringBuffer();
			deployBeginLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deployBeginLog.append(infoBegin);
			deployBeginLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deployBeginLog.toString());
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
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
			
			// create start shell
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
			if (!execStartShell(executor, port, sessionKey)) {
				DeployLog.pubLog(sessionKey, "exec pd start shell fail ......");
				return false;
			}
			
			// subsequent pd node need join to the first start node
			if (needJoin) {
				String cmdJoin = String.format("bin/pd-server --join=%s", join);
				if (!executor.execSingleLine(cmdJoin, sessionKey))
					return false;
				
				// TODO check pd-ctl member
			}
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result))
				return false;
			
			String info = String.format("deploy pd id:%s %s:%s success ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("deploy pd id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_FAIL_BEGIN_STYLE);
			deploySuccessLog.append(error);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

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
		
		String startContext = String.format("bin/tikv-server --addr %s:%s \\\\\n"
				+ "    --pd %s \\\\\n"
				+ "    --data-dir %s \\\\\n"
				+ "    -L info --log-file %s &",
				ip, port, pdList, dataDir, logFile);
		
		
		String stopUniqueFlag = String.format("\\\\--addr %s:%s", ip, port);
		String stopContext = String.format("var=\\\"%s\\\"\\n"
				+ "pid=\\`ps -ef | grep \\\"\\${var}\\\" | awk '{print \\$1, \\$2, \\$8}' | grep tikv-server | awk '{print \\$2}'\\`\\n"
				+ "if [ \\\"\\${pid}\\\" != \\\"\\\" ]\\n"
				+ "then\\n"
				+ "    kill -9 \\$pid\\n"
				+ "    echo stop tikv-server pid:\\$pid\\n"
				+ "else\\n"
				+ "    echo stop tikv-server not running\\n"
				+ "fi\\n",
				stopUniqueFlag);
		
		if (tikvInstance.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("tikv id:%s %s:%s is deployed ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return true;
		}
		
		String deployRootPath = String.format("tikv_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		DeployFileBean tikvFile = MetaData.get().getDeployFile(CONSTS.SERV_DB_TIKV);
		
		try {
			String startInfo = String.format("deploy tikv id:%s %s:%s begin ......", id, ip, port);
			StringBuffer deployBeginLog = new StringBuffer();
			deployBeginLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deployBeginLog.append(startInfo);
			deployBeginLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deployBeginLog.toString());
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
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
			if (!execStartShell(executor, port, sessionKey)) {
				DeployLog.pubLog(sessionKey, "exec tikv start shell fail ......");
				return false;
			}
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result))
				return false;
			
			String info = String.format("deploy tikv id:%s %s:%s success ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("deploy tikv id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_FAIL_BEGIN_STYLE);
			deploySuccessLog.append(error);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

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
		
		String startContext = String.format("bin/tidb-server -host %s -P %s \\\\\n"
				+ "    --store=tikv \\\\\n"
				+ "    --log-file=%s \\\\\n"
				+ "    --path=%s \\\\\n"
				+ "    --status=%s &",
				ip, port, logFile, pdList, statPort);
		
		String stopUniqueFlag = String.format("\\\\-host %s \\\\-P %s", ip, port);
		String stopContext = String.format("var=\\\"%s\\\"\\n"
				+ "pid=\\`ps -ef | grep \\\"\\${var}\\\" | awk '{print \\$1, \\$2, \\$8}' | grep tidb-server | awk '{print \\$2}'\\`\\n"
				+ "if [ \\\"\\${pid}\\\" != \\\"\\\" ]\\n"
				+ "then\\n"
				+ "    kill -9 \\$pid\\n"
				+ "    echo stop tidb-server pid:\\$pid\\n"
				+ "else\\n"
				+ "    echo stop tidb-server not running\\n"
				+ "fi\\n",
				stopUniqueFlag);
		
		if (tidbInstance.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("tidb id:%s %s:%s is deployed ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return true;
		}
		
		String deployRootPath = String.format("tidb_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		DeployFileBean tidbFile = MetaData.get().getDeployFile(CONSTS.SERV_DB_TIDB);
		
		try {
			String startInfo = String.format("deploy tidb id:%s %s:%s begin ......", id, ip, port);
			StringBuffer deployBeginLog = new StringBuffer();
			deployBeginLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deployBeginLog.append(startInfo);
			deployBeginLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deployBeginLog.toString());
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
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
			if (!execStartShell(executor, port, sessionKey)) {
				DeployLog.pubLog(sessionKey, "exec tidb start shell fail ......");
				return false;
			}
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result)) {
				return false;
			}
			
			String info = String.format("deploy tidb id:%s %s:%s success ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String info = String.format("deploy tidb id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_FAIL_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
		
		return true;
	}
	
	private boolean deployDBCollectd(String serviceID, InstanceDtlBean instanceDtl,
			String pdList, String sessionKey, ResultBean result) {
		// TODO
		return true;
	}
	
	private boolean undeployPDServer(String serviceID, InstanceDtlBean pdInstDtlBean,
			String initCluster, String sessionKey, ResultBean result) {
		
		InstanceBean pdInstance = pdInstDtlBean.getInstance();

		String id    = pdInstDtlBean.getAttribute("PD_ID").getAttrValue();
		String ip    = pdInstDtlBean.getAttribute("IP").getAttrValue();
		String port  = pdInstDtlBean.getAttribute("PORT").getAttrValue();
		String user  = pdInstDtlBean.getAttribute("OS_USER").getAttrValue();
		String pwd   = pdInstDtlBean.getAttribute("OS_PWD").getAttrValue();
		
		if (pdInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("pd id:%s %s:%s is not deployed ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return true;
		}

		String deployRootPath = String.format("pd_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;

		try {
			String infoBegin = String.format("undeploy pd id:%s %s:%s begin ......", id, ip, port);
			StringBuffer deployBeginLog = new StringBuffer();
			deployBeginLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deployBeginLog.append(infoBegin);
			deployBeginLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deployBeginLog.toString());
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			// cd deploy dir, exec stop shell, rm deploy dir
			if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.cd("$HOME/" + deployRootPath, sessionKey);
				
				// stop pd-server
				if (executor.isPortUsed(port, sessionKey)) {
					if (!execStopShell(executor, port, sessionKey)) {
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
			
			String info = String.format("undeploy pd id:%s %s:%s success ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("undeploy pd id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_FAIL_BEGIN_STYLE);
			deploySuccessLog.append(error);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
		
		return true;
	}
	
	private boolean undeployTiKVServer(String serviceID, InstanceDtlBean tikvInstDtlBean,
			String sessionKey, ResultBean result) {
		
		InstanceBean tikvInstance = tikvInstDtlBean.getInstance();
		
		String id   = tikvInstDtlBean.getAttribute("TIKV_ID").getAttrValue();
		String ip   = tikvInstDtlBean.getAttribute("IP").getAttrValue();
		String port = tikvInstDtlBean.getAttribute("PORT").getAttrValue();
		String user = tikvInstDtlBean.getAttribute("OS_USER").getAttrValue();
		String pwd  = tikvInstDtlBean.getAttribute("OS_PWD").getAttrValue();
		
		if (tikvInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("tikv id:%s %s:%s is not deployed ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return true;
		}
		
		String deployRootPath = String.format("tikv_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			String startInfo = String.format("undeploy tikv id:%s %s:%s begin ......", id, ip, port);
			StringBuffer deployBeginLog = new StringBuffer();
			deployBeginLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deployBeginLog.append(startInfo);
			deployBeginLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deployBeginLog.toString());
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			// cd deploy dir, exec stop shell, rm deploy dir
			if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.cd("$HOME/" + deployRootPath, sessionKey);
				
				// stop tikv-server
				if (executor.isPortUsed(port, sessionKey)) {
					if (!execStopShell(executor, port, sessionKey)) {
						DeployLog.pubLog(sessionKey, "exec tikv stop shell fail ......");
						return false;
					}
				}
				
				executor.cd("$HOME", sessionKey);
				executor.rm(deployRootPath, true, sessionKey);
			}
			
			// mod t_instance.IS_DEPLOYED = 0
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.NOT_DEPLOYED, result))
				return false;
			
			String info = String.format("undeploy tikv id:%s %s:%s success ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("undeploy tikv id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_FAIL_BEGIN_STYLE);
			deploySuccessLog.append(error);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
		
		return true;
	}
	
	private boolean undeployTiDBServer(String serviceID, InstanceDtlBean tidbInstDtlBean,
			String sessionKey, ResultBean result) {
		
		InstanceBean tidbInstance = tidbInstDtlBean.getInstance();
		
		String id   = tidbInstDtlBean.getAttribute("TIDB_ID").getAttrValue();
		String ip   = tidbInstDtlBean.getAttribute("IP").getAttrValue();
		String port = tidbInstDtlBean.getAttribute("PORT").getAttrValue();
		String user = tidbInstDtlBean.getAttribute("OS_USER").getAttrValue();
		String pwd  = tidbInstDtlBean.getAttribute("OS_PWD").getAttrValue();
		
		if (tidbInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("tidb id:%s %s:%s is not deployed ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return true;
		}
		
		String deployRootPath = String.format("tidb_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			String startInfo = String.format("undeploy tidb id:%s %s:%s begin ......", id, ip, port);
			StringBuffer deployBeginLog = new StringBuffer();
			deployBeginLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deployBeginLog.append(startInfo);
			deployBeginLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deployBeginLog.toString());
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			// cd deploy dir, exec stop shell, rm deploy dir
			if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.cd("$HOME/" + deployRootPath, sessionKey);
				
				// stop tidb-server
				if (executor.isPortUsed(port, sessionKey)) {
					if (!execStopShell(executor, port, sessionKey)) {
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
			
			String info = String.format("undeploy tidb id:%s %s:%s success ......", id, ip, port);
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String info = String.format("undeploy tidb id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			StringBuffer deploySuccessLog = new StringBuffer();
			deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_FAIL_BEGIN_STYLE);
			deploySuccessLog.append(info);
			deploySuccessLog.append(CONSTS.END_STYLE);
			DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
		
		return true;
	}
	
	private boolean undeployDBCollectd(InstanceDtlBean collectd, String sessionKey, ResultBean result) {
		// TODO
		return true;
	}
	
	private String getClusterString(InstanceDtlBean pdInstance) {
		String ip = pdInstance.getAttribute("IP").getAttrValue();
		String cPort = pdInstance.getAttribute("CLUSTER_PORT").getAttrValue();
		return String.format("http://%s:%s", ip, cPort);
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
		for (int i = 0; i < pdServerList.size(); i++) {
			InstanceDtlBean pdInfo = pdServerList.get(i);
			String ip = pdInfo.getAttribute("IP").getAttrValue();
			String port = pdInfo.getAttribute("PORT").getAttrValue();

			String s = String.format("%s:%s", ip, port);
			if (i > 0)
				sb.append(CONSTS.PATH_COMMA);

			sb.append(s);
		}

		return sb.toString();
	}
	
	private boolean execStartShell(SSHExecutor executor, String port, String sessionKey) {
		boolean ret = true;
		try {
			executor.execStartShell(sessionKey);
			
			// start may take some time
			long beginTs = System.currentTimeMillis();
			long currTs = beginTs;
			long maxTs = 60000L;
			
			do {
				Thread.sleep(CONSTS.DEPLOY_CHECK_INTERVAL);
	
				currTs = System.currentTimeMillis();
				if ((currTs - beginTs) > maxTs) {
					ret = false;
					break;
				}
	
				executor.echo("......");
			} while (!executor.isPortUsed(port, sessionKey));
//			Thread.sleep(1L);
			
		} catch (Exception e) {
			ret = false;
		}
		
		return ret;
	}
	
	private boolean execStopShell(SSHExecutor executor, String port, String sessionKey) {
		boolean ret = true;
		try {
			executor.execStopShell(sessionKey);
			
			// start may take some time
			long beginTs = System.currentTimeMillis();
			long currTs = beginTs;
			long maxTs = 60000L;
			
			do {
				Thread.sleep(CONSTS.DEPLOY_CHECK_INTERVAL);
	
				currTs = System.currentTimeMillis();
				if ((currTs - beginTs) > maxTs) {
					ret = false;
					break;
				}
	
				executor.echo("......");
			} while (executor.isPortUsed(port, sessionKey));
//			Thread.sleep(1L);
			
		} catch (Exception e) {
			ret = false;
		}
		
		return ret;
	}

}
