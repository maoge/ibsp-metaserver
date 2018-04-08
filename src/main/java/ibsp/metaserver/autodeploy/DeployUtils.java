package ibsp.metaserver.autodeploy;

import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.DES3;
import io.vertx.core.json.JsonObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployUtils {
	
	private static Logger logger = LoggerFactory.getLogger(DeployUtils.class);
	
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
				+ "    -rooturl=http://%s \\\\\n"
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

}
