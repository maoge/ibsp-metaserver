package ibsp.metaserver.autodeploy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
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
import ibsp.metaserver.utils.DES3;
import ibsp.metaserver.utils.HttpUtils;

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
		
		// deploy tidb root password
		if (!setTiDBPassword(tidbServerList.get(0), pwd, sessionKey, result))
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
	
	private boolean setTiDBPassword(InstanceDtlBean tidbServer, String pwd, 
			String sessionKey, ResultBean result) {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			String DBAddress = "jdbc:mysql://"+tidbServer.getAttribute("IP").getAttrValue()
					+":"+tidbServer.getAttribute("PORT").getAttrValue()+"?"+
					"user=root&useUnicode=true&characterEncoding=UTF8&useSSL=true";
			conn = DriverManager.getConnection(DBAddress);
			
			stmt = conn.prepareStatement("SET PASSWORD FOR 'root'@'%' = ?");
			stmt.setString(1, DES3.decrypt(pwd));
			stmt.execute();
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(CONSTS.ERR_CONNECT_TIDB_SERVER_ERROR+e.getMessage());
			return false;
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (conn != null) conn.close();
			} catch (Exception e) {
			}
		}
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

		String startContext = needJoin ? getPdJoinStartCmd(id, clientUrl, peerUrl, dataDir, logFile, join) :
			getPdInitStartCmd(id, clientUrl, peerUrl, dataDir, logFile, initCluster);
		
		String stopContext = getPdStopCmd(id);

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
			if (!execStartShell(executor, port, sessionKey)) {
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
		
		String startContext = getTikvStartCmd(ip, port, pdList, dataDir, logFile);
		String stopContext  = getTikvStopCmd(ip, port);
		
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
			DeployLog.pubSuccessLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("deploy tikv id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			DeployLog.pubErrorLog(sessionKey, error);

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
		
		String startContext = getTidbStartCmd(ip, port, logFile, pdList, statPort);
		
		String stopContext = getTidbStopCmd(ip, port);
		
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
			if (!execStartShell(executor, port, sessionKey)) {
				DeployLog.pubLog(sessionKey, "exec tidb start shell fail ......");
				return false;
			}
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result)) {
				return false;
			}
			
			String info = String.format("deploy tidb id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String info = String.format("deploy tidb id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			DeployLog.pubErrorLog(sessionKey, info);

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
			
			// cd deploy dir, exec stop shell, rm deploy dir
			if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.cd("$HOME/" + deployRootPath, sessionKey);
				
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
					return false;
				}
				
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
			
			// 重新从数据库获取所有的PD的信息，然后刷新PD脚本 再从数据库所有的tikv信息，刷新tikv的脚本
			List<InstanceDtlBean> tikvServerList = new ArrayList<InstanceDtlBean>();
			List<InstanceDtlBean> tidbServerList = new ArrayList<InstanceDtlBean>();
			InstanceDtlBean collectd = new InstanceDtlBean();
			TiDBService.loadServiceInfo(serviceID, pdServerList, tidbServerList, tikvServerList, collectd, result);
			for(InstanceDtlBean pd : pdServerList) {
				if(pd.getAttribute("PD_ID").getAttrValue().equals(id)) {
					pdServerList.remove(pd);
				}
			}
			refreshDbCompCmd(pdServerList, tikvServerList, tidbServerList, sessionKey);

			
			String info = String.format("undeploy pd id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("undeploy pd id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			DeployLog.pubErrorLog(sessionKey, error);

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
			executor.close();
			checkTikvStatus(serviceID, id, ip, port, user, pwd, tikvId);
			//TODO 另外一个线程来做，最后更新数据库 ，前台根据数据库来查看是否下线
			/*ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
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
				return false;*/
			
			String info = String.format("undeploy tikv id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubErrorLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("undeploy tikv id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			DeployLog.pubErrorLog(sessionKey, error);

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
			DeployLog.pubSuccessLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String info = String.format("undeploy tidb id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
			DeployLog.pubErrorLog(sessionKey, info);

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
	
	private String getPDString(List<InstanceDtlBean> pdServerList) {
		//TODO remove dead pd servers
		StringBuilder result = new StringBuilder("");
		for (InstanceDtlBean instance : pdServerList) {
			if (instance.getInstance().getIsDeployed().equals("1")) {
				String ip = instance.getAttribute("IP").getAttrValue();
				String port = instance.getAttribute("PORT").getAttrValue();
				result.append(ip+":"+port+",");
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
						if (!execStopShell(executor, port, "")) {
							logger.error(String.format("stop tikv[%s:%s] shell faild", ip, port));
						}
					}
					//删除目录
					executor.rm(deployRootPath, true, "");
					// mod t_instance.IS_DEPLOYED = 0
					if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.NOT_DEPLOYED, result)) {
						logger.error(result.getRetInfo());
					}
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
		String startShell = getTidbStartCmd(ip, port, logFile, pdList, statPort);	
		String stopShell  = getTidbStopCmd(ip, port);
		
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
		String startShell = getPdInitStartCmd(id, clientUrl, peerUrl, dataDir, logFile, initCluster);
		String stopShell  = getPdStopCmd(id);
		
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
		String startShell = getTikvStartCmd(ip, port, pdList, dataDir, logFile);	
		String stopShell  = getTikvStopCmd(ip, port);
		
		if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
			executor.cd(deployRootPath, sessionKey);
			executor.createStartShell(startShell);
			executor.createStopShell(stopShell);
			String log = String.format("tikv[%s:%s]%s create Shell success! ", ip, port, id);
			DeployLog.pubLog(sessionKey, log.toString());
		}
		
		return Boolean.TRUE;
	}
	
	private String getTidbStartCmd(String ip, String port, String logFile, String pdList, String statPort) {
		return String.format("bin/tidb-server -host %s -P %s \\\\\n"
				+ "    --store=tikv \\\\\n"
				+ "    --log-file=%s \\\\\n"
				+ "    --path=%s \\\\\n"
				+ "    --status=%s &",
				ip, port, logFile, pdList, statPort);
	}
	
	private String getTidbStopCmd(String ip, String port) {
		return String.format("var=\\\"\\\\-host %s \\\\-P %s\\\" \\n"
				+ "pid=\\`ps -ef | grep \\\"\\${var}\\\" | awk '{print \\$1, \\$2, \\$8}' | grep tidb-server | awk '{print \\$2}'\\`\\n"
				+ "if [ \\\"\\${pid}\\\" != \\\"\\\" ]\\n"
				+ "then\\n"
				+ "    kill -9 \\$pid\\n"
				+ "    echo stop tidb-server pid:\\$pid\\n"
				+ "else\\n"
				+ "    echo stop tidb-server not running\\n"
				+ "fi\\n",
				ip, port);
	}
	
	private String getPdInitStartCmd(String id, String clientUrl, String peerUrl, String dataDir, String logFile,String cluster) {
		return String.format("bin/pd-server --name=%s \\\\\n"
				+ "    --client-urls=%s --peer-urls=%s \\\\\n"
				+ "    --advertise-client-urls=%s --advertise-peer-urls=%s \\\\\n"
				+ "    --data-dir=%s -L info \\\\\n" 
				+ "    --log-file=%s \\\\\n"
				+ "    --initial-cluster=%s &",
				id, clientUrl, peerUrl, clientUrl, peerUrl, dataDir, logFile, cluster);
	}
	
	private String getPdJoinStartCmd(String id, String clientUrl, String peerUrl, String dataDir, String logFile,String join) {
		return String.format("bin/pd-server --name=%s \\\\\n"
				+ "    --client-urls=%s --peer-urls=%s \\\\\n"
				+ "    --advertise-client-urls=%s --advertise-peer-urls=%s \\\\\n"
				+ "    --data-dir=%s -L info \\\\\n" 
				+ "    --log-file=%s \\\\\n"
				+ "    --join=%s &",
				id, clientUrl, peerUrl, clientUrl, peerUrl, dataDir, logFile, join);
	}
	
	private String getPdStopCmd(String id) {
		return String.format("var=\\\"name=%s\\\" \\n"
				+ "pid=\\`ps -ef | grep \\\"\\${var}\\\" | awk '{print \\$1, \\$2, \\$8}' | grep pd-server | awk '{print \\$2}'\\`\\n"
				+ "if [ \\\"\\${pid}\\\" != \\\"\\\" ]\\n"
				+ "then\\n"
				+ "    kill \\$pid\\n"
				+ "    echo stop pd-server pid:\\$pid\\n"
				+ "else\\n"
				+ "    echo stop pd-server not running\\n"
				+ "fi\\n",
				id);
	}
	
	private String getTikvStartCmd(String ip, String port, String pdList, String dataDir, String logFile) {
		return String.format("bin/tikv-server --addr %s:%s \\\\\n"
				+ "    --pd %s \\\\\n"
				+ "    --data-dir %s \\\\\n"
				+ "    -L info --log-file %s &",
				ip, port, pdList, dataDir, logFile);
	}
	
	private String getTikvStopCmd(String ip,String port) {
		return String.format("var=\\\"\\\\--addr %s:%s\\\" \\n"
				+ "pid=\\`ps -ef | grep \\\"\\${var}\\\" | awk '{print \\$1, \\$2, \\$8}' | grep tikv-server | awk '{print \\$2}'\\`\\n"
				+ "if [ \\\"\\${pid}\\\" != \\\"\\\" ]\\n"
				+ "then\\n"
				+ "    kill \\$pid\\n"
				+ "    echo stop tikv-server pid:\\$pid\\n"
				+ "else\\n"
				+ "    echo stop tikv-server not running\\n"
				+ "fi\\n",
				ip, port);
	}
}
