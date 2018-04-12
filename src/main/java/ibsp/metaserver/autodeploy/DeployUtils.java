package ibsp.metaserver.autodeploy;

import ibsp.metaserver.autodeploy.utils.DeployLog;
import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.ConfigDataService;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.DES3;
import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployUtils {
	
	private static Logger logger = LoggerFactory.getLogger(DeployUtils.class);
	
	public static boolean deployCollectd(String serviceID, InstanceDtlBean instanceDtl,
			String sessionKey, ResultBean result) {
		
		InstanceBean collectdInstance = instanceDtl.getInstance();
		
		String id   = instanceDtl.getAttribute("COLLECTD_ID").getAttrValue();
		String ip   = instanceDtl.getAttribute("IP").getAttrValue();
		String port = instanceDtl.getAttribute("PORT").getAttrValue();
		String user = instanceDtl.getAttribute("OS_USER").getAttrValue();
		String pwd  = instanceDtl.getAttribute("OS_PWD").getAttrValue();
		
//		String logFile = "log/collectd.log";
		
		//TODO log file and metaserver address
		String startContext = getCollectdStartCmd(id, ip, port, MetaData.get().getMetaServUrls(), serviceID);
		
		String stopContext = getCollectdStopCmd(id);
		
		if (collectdInstance.getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("Collectd id:%s %s:%s is deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			return true;
		}
		
		String deployRootPath = String.format("collectd_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		DeployFileBean collectdFile = MetaData.get().getDeployFile(CONSTS.SERV_COLLECTD);
		
		try {
			String startInfo = String.format("deploy collectd id:%s %s:%s begin ......", id, ip, port);
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
			executor.mkdir("log", sessionKey);
			
			// fetch deploy file
			String srcFile = String.format("%s%s", collectdFile.getFtpDir(), collectdFile.getFileName());
			String desPath = ".";
			executor.scp(collectdFile.getFtpUser(), collectdFile.getFtpPwd(),
					collectdFile.getFtpHost(), srcFile, desPath,
					collectdFile.getSshPort(), sessionKey);
			
			// unpack deploy file
			executor.tgzUnpack(collectdFile.getFileName(), sessionKey);
			executor.rm(collectdFile.getFileName(), false, sessionKey);
			
			// create start shell
			if (!executor.createStartShell(startContext)) {
				DeployLog.pubLog(sessionKey, "create collectd start shell fail ......");
				return false;
			}
			
			// create stop shell
			if (!executor.createStopShell(stopContext)) {
				DeployLog.pubLog(sessionKey, "create collectd stop shell fail ......");
				return false;
			}
			
			// start collectd
			if (!DeployUtils.execStartShell(executor, port, sessionKey)) {
				DeployLog.pubLog(sessionKey, "exec collectd start shell fail ......");
				return false;
			}
			
			// mod t_instance.IS_DEPLOYED = 1
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result)) {
				return false;
			}
			publishDeployEvent(EventType.e23, id);
			
			String info = String.format("deploy collectd id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("deploy collectd id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
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
	
	public static boolean undeployCollectd(InstanceDtlBean collectd, String sessionKey,
			boolean isUndeployService, ResultBean result) {
		
		InstanceBean collectdInstance = collectd.getInstance();
		
		String id   = collectd.getAttribute("COLLECTD_ID").getAttrValue();
		String ip   = collectd.getAttribute("IP").getAttrValue();
		String port = collectd.getAttribute("PORT").getAttrValue();
		String user = collectd.getAttribute("OS_USER").getAttrValue();
		String pwd  = collectd.getAttribute("OS_PWD").getAttrValue();
		
		if (collectdInstance.getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("Collectd id:%s %s:%s is not deployed ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			
			return true;
		}
		
		String deployRootPath = String.format("collectd_deploy/%s", port);
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			String startInfo = String.format("undeploy Collectd id:%s %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, startInfo);
			
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			// cd deploy dir, exec stop shell, rm deploy dir
			if (executor.isDirExistInCurrPath(deployRootPath, sessionKey)) {
				executor.cd("$HOME/" + deployRootPath, sessionKey);
				
				// stop collectd
				if (executor.isPortUsed(port, sessionKey)) {
					if (!DeployUtils.execStopShell(executor, port, sessionKey)) {
						DeployLog.pubLog(sessionKey, "exec collectd stop shell fail ......");
						return false;
					}
				}
				
				executor.cd("$HOME", sessionKey);
				executor.rm(deployRootPath, true, sessionKey);
			}
			
			// mod t_instance.IS_DEPLOYED = 0
			if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.NOT_DEPLOYED, result))
				return false;
			publishDeployEvent(EventType.e24, id);
			
			String info = String.format("undeploy collectd id:%s %s:%s success ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, info);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			String error = String.format("undeploy collectd id:%s %s:%s caught error:%s", id, ip, port, e.getMessage());
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
	
	public static boolean execStartShell(SSHExecutor executor, String port, String sessionKey) {
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
	
	public static boolean execStopShell(SSHExecutor executor, String port, String sessionKey) {
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
	
	public static boolean resetDBPwd(String instID, InstanceDtlBean tidbServer, String pwd,
			String sessionKey, ResultBean result) {
		
		if (!setTiDBPwdProc(tidbServer, pwd, sessionKey, result))
			return false;
		
		MetaData.get().setDBPwd(instID, "root", pwd);
		
		JsonObject evJson = new JsonObject();
		evJson.put("INST_ID", instID);
		
		EventBean ev = new EventBean(EventType.e7);
		ev.setUuid(MetaData.get().getUUID());
		ev.setJsonStr(evJson.toString());
		EventBusMsg.publishEvent(ev);
		
		return true;
	}
	
	private static boolean setTiDBPwdProc(InstanceDtlBean tidbServer, String pwd, 
			String sessionKey, ResultBean result) {
		
		boolean ret = false;
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
			ret = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(CONSTS.ERR_CONNECT_TIDB_SERVER_ERROR+e.getMessage());
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (conn != null) conn.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		return ret;
	}
	
	public static boolean setHostName(Map<String, InstanceDtlBean> brokers,
			String sessionKey, ResultBean result) {
		
		Map<String, String> ip2host = new HashMap<String, String>();
		
		// first loop get all broker hostname
		Set<Entry<String, InstanceDtlBean>> entrySet = brokers.entrySet();
		for (Entry<String, InstanceDtlBean> entry : entrySet) {
			InstanceDtlBean brokerInstanceDtl = entry.getValue();
			
			String id   = brokerInstanceDtl.getAttribute("BROKER_ID").getAttrValue();
			String ip   = brokerInstanceDtl.getAttribute("IP").getAttrValue();
			String user = brokerInstanceDtl.getAttribute("OS_USER").getAttrValue();
			String pwd  = brokerInstanceDtl.getAttribute("OS_PWD").getAttrValue();
			
			JschUserInfo ui = null;
			SSHExecutor executor = null;
			boolean connected = false;
			
			try {
				ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
				executor = new SSHExecutor(ui);
				executor.connect();
				connected = true;
				
				String host = executor.getHostname();
				brokerInstanceDtl.setAttribute("HOST_NAME", host);
				
				int hostAttrID = brokerInstanceDtl.getAttribute("HOST_NAME").getAttrID();
				ConfigDataService.modComponentAttribute(id, hostAttrID, host, result);
				
				ip2host.put(ip, host);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return false;
			} finally {
				if (connected) {
					executor.close();
				}
			}
		}
		
		// second loop set /etc/hosts
		for (Entry<String, InstanceDtlBean> entry : entrySet) {
			InstanceDtlBean brokerInstanceDtl = entry.getValue();
			
			String ip      = brokerInstanceDtl.getAttribute("IP").getAttrValue();
			String rootPwd = brokerInstanceDtl.getAttribute("ROOT_PWD").getAttrValue();
			
			JschUserInfo ui = null;
			SSHExecutor executor = null;
			boolean connected = false;
			
			try {
				ui = new JschUserInfo("root", rootPwd, ip, CONSTS.SSH_PORT_DEFAULT);
				executor = new SSHExecutor(ui);
				executor.connect();
				connected = true;
				
				Vector<String> hostLines = new Vector<String>();
				Set<Entry<String, String>> ipEntrySet = ip2host.entrySet();
				for (Entry<String, String> ipEntry : ipEntrySet) {
					String ip2set = ipEntry.getKey();
					String host2set = ipEntry.getValue();
					
					if (!executor.checkHostExist(host2set)) {
						String line = String.format("%s   %s", ip2set, host2set);
						hostLines.add(line);
					}
				}
				
				if (hostLines.size() != 0) {
					executor.addHosts(hostLines, sessionKey);
				}
			
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return false;
			} finally {
				if (connected) {
					executor.close();
				}
			}
		}
		
		return true;
	}
	
	public static String getTidbStartCmd(String ip, String port, String logFile, String pdList, String statPort) {
		return String.format("bin/tidb-server -host %s -P %s \\\\\n"
				+ "    --store=tikv \\\\\n"
				+ "    --log-file=%s \\\\\n"
				+ "    --path=%s \\\\\n"
				+ "    --config=conf/tidb.toml \\\\\n"
				+ "    --status=%s &",
				ip, port, logFile, pdList, statPort);
	}
	
	public static String getTidbStopCmd(String ip, String port) {
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
	
	public static String getPdInitStartCmd(String id, String clientUrl, String peerUrl, String dataDir, String logFile,String cluster) {
		return String.format("bin/pd-server --name=%s \\\\\n"
				+ "    --client-urls=%s --peer-urls=%s \\\\\n"
				+ "    --advertise-client-urls=%s --advertise-peer-urls=%s \\\\\n"
				+ "    --data-dir=%s -L info \\\\\n" 
				+ "    --log-file=%s \\\\\n"
				+ "    --config=conf/pd.toml \\\\\n"
				+ "    --initial-cluster=%s &",
				id, clientUrl, peerUrl, clientUrl, peerUrl, dataDir, logFile, cluster);
	}
	
	public static String getPdJoinStartCmd(String id, String clientUrl, String peerUrl, String dataDir, String logFile,String join) {
		return String.format("bin/pd-server --name=%s \\\\\n"
				+ "    --client-urls=%s --peer-urls=%s \\\\\n"
				+ "    --advertise-client-urls=%s --advertise-peer-urls=%s \\\\\n"
				+ "    --data-dir=%s -L info \\\\\n" 
				+ "    --log-file=%s \\\\\n"
				+ "    --config=conf/pd.toml \\\\\n"
				+ "    --join=%s &",
				id, clientUrl, peerUrl, clientUrl, peerUrl, dataDir, logFile, join);
	}
	
	public static String getPdStopCmd(String id) {
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
	
	public static String getTikvStartCmd(String ip, String port, String pdList, String dataDir, String logFile) {
		return String.format("bin/tikv-server --addr %s:%s \\\\\n"
				+ "    --pd %s \\\\\n"
				+ "    --data-dir %s \\\\\n"
				+ "    --config conf/tikv.toml \\\\\n"
				+ "    -L info --log-file %s &",
				ip, port, pdList, dataDir, logFile);
	}
	
	public static String getTikvStopCmd(String ip,String port) {
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
	
	public static String getCollectdStartCmd(String id, String ip, String port, String rootUrl, String servID) {
		return String.format("bin/collectd -name=%s \\\\\n"
				+ "    -addr=%s:%s \\\\\n"
				+ "    -compress=false \\\\\n"
				+ "    -rooturl=%s \\\\\n"
				+ "    -servid=%s &",
				id, ip, port, rootUrl, servID);
	}
	
	public static String getCollectdStopCmd(String id) {
		return String.format("var=\\\"name=%s\\\" \\n"
				+ "pid=\\`ps -ef | grep \\\"\\${var}\\\" | awk '{print \\$1, \\$2, \\$8}' | grep collectd | awk '{print \\$2}'\\`\\n"
				+ "if [ \\\"\\${pid}\\\" != \\\"\\\" ]\\n"
				+ "then\\n"
				+ "    kill \\$pid\\n"
				+ "    echo stop collectd pid:\\$pid\\n"
				+ "else\\n"
				+ "    echo stop collectd not running\\n"
				+ "fi\\n",
				id);
	}

	public static void publishDeployEvent(EventType type, String id) {
		JsonObject paramsJson = new JsonObject();
		paramsJson.put(FixHeader.HEADER_INSTANCE_ID, id);
		EventBean evBean = new EventBean();
		evBean.setEvType(type);
		evBean.setJsonStr(paramsJson.toString());
		EventBusMsg.publishEvent(evBean);
	}
}
