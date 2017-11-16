package ibsp.metaserver.autodeploy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import ibsp.metaserver.dbservice.TiDBService;
import ibsp.metaserver.utils.CONSTS;

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

		Map<String, DeployFileBean> deployFileMap = new HashMap<String, DeployFileBean>();
		if (!loadDeployFileInfo(deployFileMap, result))
			return false;

		// deploy pd-server
		if (!deployPDServerList(pdServerList, deployFileMap, sessionKey, result))
			return false;

		// deploy tikv-server
		String pdList = getPDList(pdServerList);
		if (!deployTiKVServerList(tikvServerList, deployFileMap, pdList, sessionKey, result))
			return false;

		// deploy tidb-server
		if (!deployTiDBServerList(tidbServerList, deployFileMap, pdList, sessionKey, result))
			return false;

		// deploy collectd
		if (!deployCollectd(collectd, deployFileMap, sessionKey, result))
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
		
		// undeploy collectd
		// undeployCollectd(InstanceDtlBean collectd, String sessionKey, ResultBean result)
		if (!undeployCollectd(collectd, sessionKey, result))
			return false;
		
		// undeploy tidb-server
		if (!undeployTiDBServerList(tidbServerList, sessionKey, result))
			return false;
		
		// undeploy tikv-server
		if (!undeployTiKVServerList(tikvServerList, sessionKey, result))
			return false;
		
		// undeploy pd-server
		if (!undeployPDServerList(pdServerList, sessionKey, result))
			return false;
		
		// mod t_service.IS_DEPLOYED = 0
		if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.NOT_DEPLOYED, result))
			return false;
		
		return true;
	}

	@Override
	public boolean loadDeployFileInfo(Map<String, DeployFileBean> deployFileMap,
			ResultBean result) {
		return ConfigDataService.loadDeployFile(CONSTS.SERV_TYPE_DB,
				deployFileMap, result);
	}

	private boolean deployPDServerList(List<InstanceDtlBean> pdServerList,
			Map<String, DeployFileBean> deployFileMap, String sessionKey,
			ResultBean result) {
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

		String initCluster = getPDInitCluster(pdServerList);
		InstanceDtlBean firstPD = pdServerList.get(0);
		String join = getClusterString(firstPD);
		
		DeployFileBean pdFile = deployFileMap.get(CONSTS.SERV_DB_PD);

		for (int i = 0; i < pdServerList.size(); i++) {
			InstanceDtlBean pdInfo = pdServerList.get(i);
			InstanceBean pdInstance = pdInfo.getInstance();

			String id    = pdInfo.getAttribute("PD_ID").getAttrValue();
			String ip    = pdInfo.getAttribute("IP").getAttrValue();
			String port  = pdInfo.getAttribute("PORT").getAttrValue();
			String cPort = pdInfo.getAttribute("CLUSTER_PORT").getAttrValue();
			String user  = pdInfo.getAttribute("OS_USER").getAttrValue();
			String pwd   = pdInfo.getAttribute("OS_PWD").getAttrValue();
			
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

				continue;
			}

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
				if (i > 0) {
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
		}
		
		return true;
	}

	private boolean deployTiKVServerList(List<InstanceDtlBean> tikvServerList,
			Map<String, DeployFileBean> deployFileMap, String pdList,
			String sessionKey, ResultBean result) {
		
		// tikv_deploy/service_port
		//                      |----- start.sh
		//                      |----- stop.sh
		//                      |----- bin
		//                               |----- tikv-server
		//                               |----- tikv-ctl
		//                      |----- data
		//                      |----- log
		
		DeployFileBean tikvFile = deployFileMap.get(CONSTS.SERV_DB_TIKV);
		
		for (int i = 0; i < tikvServerList.size(); i++) {
			InstanceDtlBean tikvInfo = tikvServerList.get(i);
			InstanceBean tikvInstance = tikvInfo.getInstance();
			
			String id   = tikvInfo.getAttribute("TIKV_ID").getAttrValue();
			String ip   = tikvInfo.getAttribute("IP").getAttrValue();
			String port = tikvInfo.getAttribute("PORT").getAttrValue();
			String user = tikvInfo.getAttribute("OS_USER").getAttrValue();
			String pwd  = tikvInfo.getAttribute("OS_PWD").getAttrValue();
			
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

				continue;
			}
			
			String deployRootPath = String.format("tikv_deploy/%s", port);
			JschUserInfo ui = null;
			SSHExecutor executor = null;
			boolean connected = false;
			
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
		}
		
		return true;
	}

	private boolean deployTiDBServerList(List<InstanceDtlBean> tidbServerList,
			Map<String, DeployFileBean> deployFileMap, String pdList,
			String sessionKey, ResultBean result) {
		
		// tidb_deploy/service_port
		//                      |----- start.sh
		//                      |----- stop.sh
		//                      |----- bin
		//                               |----- tidb-server
		//                               |----- goyacc
		//                      |----- log
		
		DeployFileBean tidbFile = deployFileMap.get(CONSTS.SERV_DB_TIDB);
		
		for (int i = 0; i < tidbServerList.size(); i++) {
			
			InstanceDtlBean tidbInfo = tidbServerList.get(i);
			InstanceBean tidbInstance = tidbInfo.getInstance();
			
			String id   = tidbInfo.getAttribute("TIDB_ID").getAttrValue();
			String ip   = tidbInfo.getAttribute("IP").getAttrValue();
			String port = tidbInfo.getAttribute("PORT").getAttrValue();
			String statPort = tidbInfo.getAttribute("STAT_PORT").getAttrValue();
			String user = tidbInfo.getAttribute("OS_USER").getAttrValue();
			String pwd  = tidbInfo.getAttribute("OS_PWD").getAttrValue();
			
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

				continue;
			}
			
			String deployRootPath = String.format("tidb_deploy/%s", port);
			JschUserInfo ui = null;
			SSHExecutor executor = null;
			boolean connected = false;
			
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
				if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result))
					return false;
				
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
		}
		
		return true;
	}

	private boolean deployCollectd(InstanceDtlBean collectd,
			Map<String, DeployFileBean> deployFileMap, String sessionKey,
			ResultBean result) {
		// TODO
		return true;
	}
	
	private boolean undeployPDServerList(List<InstanceDtlBean> pdServerList,
			String sessionKey, ResultBean result) {

		for (int i = 0; i < pdServerList.size(); i++) {
			InstanceDtlBean pdInfo = pdServerList.get(i);
			InstanceBean pdInstance = pdInfo.getInstance();

			String id    = pdInfo.getAttribute("PD_ID").getAttrValue();
			String ip    = pdInfo.getAttribute("IP").getAttrValue();
			String port  = pdInfo.getAttribute("PORT").getAttrValue();
			String cPort = pdInfo.getAttribute("CLUSTER_PORT").getAttrValue();
			String user  = pdInfo.getAttribute("OS_USER").getAttrValue();
			String pwd   = pdInfo.getAttribute("OS_PWD").getAttrValue();
			
			if (pdInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
				String info = String.format("pd id:%s %s:%s is not deployed ......", id, ip, port);
				StringBuffer deploySuccessLog = new StringBuffer();
				deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
				deploySuccessLog.append(info);
				deploySuccessLog.append(CONSTS.END_STYLE);
				DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

				continue;
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
				if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.NOT_DEPLOYED, result))
					return false;
				
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
		}
		
		return true;
	}
	
	private boolean undeployTiKVServerList(List<InstanceDtlBean> tikvServerList,
			String sessionKey, ResultBean result) {
		
		for (int i = 0; i < tikvServerList.size(); i++) {
			InstanceDtlBean tikvInfo = tikvServerList.get(i);
			InstanceBean tikvInstance = tikvInfo.getInstance();
			
			String id   = tikvInfo.getAttribute("TIKV_ID").getAttrValue();
			String ip   = tikvInfo.getAttribute("IP").getAttrValue();
			String port = tikvInfo.getAttribute("PORT").getAttrValue();
			String user = tikvInfo.getAttribute("OS_USER").getAttrValue();
			String pwd  = tikvInfo.getAttribute("OS_PWD").getAttrValue();
			
			if (tikvInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
				String info = String.format("tikv id:%s %s:%s is not deployed ......", id, ip, port);
				StringBuffer deploySuccessLog = new StringBuffer();
				deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
				deploySuccessLog.append(info);
				deploySuccessLog.append(CONSTS.END_STYLE);
				DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

				continue;
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
		}
		
		return true;
	}
	
	private boolean undeployTiDBServerList(List<InstanceDtlBean> tidbServerList,
			String sessionKey, ResultBean result) {
		
		for (int i = 0; i < tidbServerList.size(); i++) {
			
			InstanceDtlBean tidbInfo = tidbServerList.get(i);
			InstanceBean tidbInstance = tidbInfo.getInstance();
			
			String id   = tidbInfo.getAttribute("TIDB_ID").getAttrValue();
			String ip   = tidbInfo.getAttribute("IP").getAttrValue();
			String port = tidbInfo.getAttribute("PORT").getAttrValue();
			String user = tidbInfo.getAttribute("OS_USER").getAttrValue();
			String pwd  = tidbInfo.getAttribute("OS_PWD").getAttrValue();
			
			if (tidbInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
				String info = String.format("tidb id:%s %s:%s is not deployed ......", id, ip, port);
				StringBuffer deploySuccessLog = new StringBuffer();
				deploySuccessLog.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
				deploySuccessLog.append(info);
				deploySuccessLog.append(CONSTS.END_STYLE);
				DeployLog.pubLog(sessionKey, deploySuccessLog.toString());

				continue;
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
		}
		
		return true;
	}
	
	private boolean undeployCollectd(InstanceDtlBean collectd, String sessionKey, ResultBean result) {
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
				Thread.sleep(3L);
	
				currTs = System.currentTimeMillis();
				if ((currTs - beginTs) > maxTs) {
					ret = false;
					break;
				}
	
				executor.echo("......");
			} while (!executor.isPortUsed(port, sessionKey));
			Thread.sleep(3L);
			
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
				Thread.sleep(3L);
	
				currTs = System.currentTimeMillis();
				if ((currTs - beginTs) > maxTs) {
					ret = false;
					break;
				}
	
				executor.echo("......");
			} while (executor.isPortUsed(port, sessionKey));
			Thread.sleep(3L);
			
		} catch (Exception e) {
			ret = false;
		}
		
		return ret;
	}

}
