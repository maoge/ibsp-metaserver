package ibsp.metaserver.autodeploy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;

import ibsp.metaserver.autodeploy.utils.DeployLog;
import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.ConfigDataService;
import ibsp.metaserver.dbservice.MQService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.ErlUtils;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.Topology;

public class MQDeployer implements Deployer {
	
	private static Logger logger = LoggerFactory.getLogger(MQDeployer.class);

	@Override
	public boolean deployService(String serviceID, String user, String pwd, String sessionKey, ResultBean result) {
		List<InstanceDtlBean> vbrokerList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		if (!MQService.loadServiceInfo(serviceID, vbrokerList, collectd, result))
			return false;
		
		// deploy vbroker
		if (!deployVBrokerList(serviceID, vbrokerList, sessionKey, result))
			return false;
		
		// deploy collectd
		if (!deployCollectd(serviceID, collectd, sessionKey, result))
			return false;
		
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
	
	private boolean deployVBrokerList(String serviceID, List<InstanceDtlBean> vbrokerList,
			String sessionKey, ResultBean result) {
		
		for (int i = 0; i < vbrokerList.size(); i++) {
			InstanceDtlBean vbrokerInstanceDtl = vbrokerList.get(i);
			
			if (!deployVBroker(serviceID, vbrokerInstanceDtl, sessionKey, result))
				return false;
		}
		
		return true;
	}
	
	private boolean deployVBroker(String serviceID, InstanceDtlBean vbrokerInstanceDtl,
			String sessionKey, ResultBean result) {
		
		String vbrokerId = vbrokerInstanceDtl.getAttribute("VBROKER_ID").getAttrValue();
		
		if (vbrokerInstanceDtl.getInstance().getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("mq vbroker id:%s %s:%s is deployed ......", vbrokerId);
			DeployLog.pubSuccessLog(sessionKey, info);

			return true;
		}
		
		Map<String, InstanceDtlBean> brokers = vbrokerInstanceDtl.getSubInstances();
		if (brokers == null || brokers.isEmpty()) {
			String info = String.format("mq vbroker id:%s no sub broker  ......", vbrokerId);
			DeployLog.pubSuccessLog(sessionKey, info);

			return false;
		}
		
		int cnt = 1, size = brokers.size();
		boolean isCluster = size > 1;
		boolean allOk = true;
		Set<InstanceDtlBean> success = new HashSet<InstanceDtlBean>(brokers.size());
		
		String erlCookie = ErlUtils.genErlCookie();
		String masterID = null, firstNode = null;
		
		// set host name
		if (isCluster) {
			setHostName(brokers, sessionKey, result);
		}
		
		Set<Entry<String, InstanceDtlBean>> entrySet = brokers.entrySet();
		for (Entry<String, InstanceDtlBean> entry : entrySet) {
			InstanceDtlBean brokerInstanceDtl = entry.getValue();
			
			String brokerId = brokerInstanceDtl.getAttribute("BROKER_ID").getAttrValue();
			String ip       = brokerInstanceDtl.getAttribute("IP").getAttrValue();
			String port     = brokerInstanceDtl.getAttribute("PORT").getAttrValue();
			String user     = brokerInstanceDtl.getAttribute("OS_USER").getAttrValue();
			String pwd      = brokerInstanceDtl.getAttribute("OS_PWD").getAttrValue();
			String host     = brokerInstanceDtl.getAttribute("HOST_NAME").getAttrValue();
			
			JschUserInfo ui = null;
			SSHExecutor executor = null;
			boolean connected = false;
			
			try {
				String startInfo = String.format("deploy mq: vbroker id:{%s} broker id:{%s} %s:%s begin ......", vbrokerId, brokerId, ip, port);
				DeployLog.pubSuccessLog(sessionKey, startInfo);
				
				ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
				executor = new SSHExecutor(ui);
				executor.connect();
				connected = true;
				
				executor.echo("test"); // 有的机器中间加了跳转和管控防止ssh登录"Last login:xxxxx"串到输出一起显示
				
				allOk &= deployErlang(executor, sessionKey, result);
				if (!allOk) {
					DeployLog.pubLog(sessionKey, "deloy erlang fail ......");
					break;
				}
				
				boolean needJoinCluster = isCluster && (cnt > 1);
				boolean needSetHaPlicy  = isCluster && (size == cnt);
				
				if (HttpUtils.isNull(host)) {
					host = executor.getHostname();
				}
				
				if (cnt == 1) {
					masterID = brokerId;
					firstNode = String.format("%s@%s", brokerId, host);
				}
				
				allOk &= deployRabbit(executor, brokerInstanceDtl, needJoinCluster, needSetHaPlicy,
										erlCookie, host, firstNode, sessionKey, result);
				if (allOk) {
					success.add(brokerInstanceDtl);
				} else {
					DeployLog.pubLog(sessionKey, "deloy rabbit fail ......");
					break;
				}
				
				cnt++;
				
			} catch (Exception e) {
				if (e instanceof JSchException || e instanceof InterruptedException) {
					String errInfo = String.format("%s:%s", CONSTS.ERR_DEPLOY_MQ_FAIL, e.getMessage());

					StringBuffer deployFailLog = new StringBuffer();
					deployFailLog.append(CONSTS.DEPLOY_SINGLE_FAIL_BEGIN_STYLE);
					deployFailLog.append(errInfo);
					deployFailLog.append(CONSTS.END_STYLE);
					DeployLog.pubLog(sessionKey, deployFailLog.toString());
					
					logger.error(errInfo);
					
					allOk = false;
				}
			} finally {
				if (connected) {
					executor.close();
				}
			}
		}
		
		if (allOk) {
			// write back VBROKER attribute: MASTER_ID
			int attrID = vbrokerInstanceDtl.getAttribute("MASTER_ID").getAttrID();
			ConfigDataService.modComponentAttribute(vbrokerId, attrID, masterID, result);
			
			ConfigDataService.modInstanceDeployFlag(vbrokerId, CONSTS.DEPLOYED, result);
		} else {
			// uninstall the success
			for (InstanceDtlBean brokerInstanceDtl : success) {
				undeployRabbit(brokerInstanceDtl, sessionKey, result);
			}
		}
		
		success.clear();
		
		return allOk;
	}
	
	private boolean deployCollectd(String serviceID, InstanceDtlBean collectd,
			String sessionKey, ResultBean result) {
		// TODO
		return true;
	}
	
	private boolean deployErlang(SSHExecutor executor, 
			String sessionKey, ResultBean result) throws InterruptedException {
		
		String deployErlPath = "erl_otp";
		String otpFile = "otp_R15B";
		
		DeployFileBean erlangFile = MetaData.get().getDeployFile(CONSTS.SERV_MQ_ERLANG);
		
		// check erlang
		if (!executor.isFileExistInCurrPath(deployErlPath, sessionKey)) {
			executor.mkdir(deployErlPath, sessionKey);
			executor.cd("$HOME/" + deployErlPath, sessionKey);
			
			String erlSrcFile = String.format("%s%s", erlangFile.getFtpDir(), erlangFile.getFileName());
			String erlDesPath = ".";
			executor.scp(erlangFile.getFtpUser(), erlangFile.getFtpPwd(),
					erlangFile.getFtpHost(), erlSrcFile, erlDesPath,
					erlangFile.getSshPort(), sessionKey);
			
			// unpack deploy file
			executor.tgzUnpack(erlangFile.getFileName(), sessionKey);
			executor.rm(erlangFile.getFileName(), false, sessionKey);
		}
		
		// add erlang path to .bash_profile and validate profile
		if (!executor.isErlCmdValid(sessionKey)) {
			executor.addErlRootDirEnv(deployErlPath + "/" + otpFile, sessionKey);
			executor.addPathToEnv(deployErlPath + "/" + otpFile, sessionKey);
			executor.validateEnv();
		}
		
		executor.cd("$HOME");
		
		return true;
	}
	
	private boolean deployRabbit(SSHExecutor executor, InstanceDtlBean brokerInstanceDtl,
			boolean needJoinCluster, boolean needSetHaPlicy,
			String erlCookie, String host, String firstNode, String sessionKey, ResultBean result) throws InterruptedException {
		
		String port    = brokerInstanceDtl.getAttribute("PORT").getAttrValue();
		String mgrPort = brokerInstanceDtl.getAttribute("MGR_PORT").getAttrValue();
		String id      = brokerInstanceDtl.getAttribute("BROKER_ID").getAttrValue();
		String mqUser  = CONSTS.MQ_DEFAULT_USER;
		String mqPwd   = CONSTS.MQ_DEFAULT_PWD;
		String mqVHost = CONSTS.MQ_DEFAULT_VHOST;
		
		String mqSName = String.format("%s@%s", id, host);
		
		DeployFileBean rabbitMQFile = MetaData.get().getDeployFile(CONSTS.SERV_MQ_RABBIT);
		String deployMQPath = String.format("%s/%s", CONSTS.MQ_DEPLOY_ROOT_PATH, port);
		
		if (executor.isPortUsed(Integer.parseInt(port))) {
			String info = String.format("port %s is already in use ......", port);
			DeployLog.pubLog(sessionKey, info);
			return false;
		}
		
		if (executor.isPortUsed(Integer.parseInt(mgrPort))) {
			DeployLog.pubLog(sessionKey, "port "+mgrPort+" is already in use......");
			return false;
		}
		
		executor.cd("$HOME");
		
		boolean isDestPathExist = executor.isFileExistInCurrPath(deployMQPath, sessionKey);
		if (isDestPathExist) {
			executor.rm(deployMQPath, false, sessionKey);
		}
		
		executor.mkdir(deployMQPath, sessionKey);
		executor.cd("$HOME/" + deployMQPath, sessionKey);
		
		String rabbitSrcFile = String.format("%s%s", rabbitMQFile.getFtpDir(), rabbitMQFile.getFileName());
		String rabbitDesPath = ".";
		executor.scp(rabbitMQFile.getFtpUser(), rabbitMQFile.getFtpPwd(),
				rabbitMQFile.getFtpHost(), rabbitSrcFile, rabbitDesPath,
				rabbitMQFile.getSshPort(), sessionKey);
		
		// make mq deploy dir
		if (!executor.isDirExistInCurrPath(deployMQPath, sessionKey)) {
			executor.mkdir(deployMQPath, sessionKey);
		}
		
		executor.tgzUnpack(rabbitMQFile.getFileName(), sessionKey);
		executor.rm(rabbitMQFile.getFileName(), false, sessionKey);
		
		String startContext = String.format(
				"ERL_COOKIE=%s RABBITMQ_NODE_PORT=%d RABBITMQ_NODENAME=%s DISK_FREE_LIMIT=%s VM_MEMORY_HIGH_WATERMARK=%s MANAGEMENT_LISTEN_PORT=%d ./%s/sbin/rabbitmq-server -detached ",
				erlCookie, port, mqSName, "" + CONSTS.DISK_FREE_LIMIT, "" + CONSTS.VM_MEMORY_HIGH_WATERMARK,
				mgrPort, CONSTS.MQ_DEPLOY_PATH);
		executor.createStartShell(startContext);

		String stopContext = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s stop ", erlCookie,
				CONSTS.MQ_DEPLOY_PATH, mqSName);
		executor.createStopShell(stopContext);
		
		
		if (!executor.isPortUsed(port, sessionKey)) {
			executor.execStartShell(sessionKey);
			
			// rabbit start slowly, wait for ready, then create rabbit accout
			long beginTs = System.currentTimeMillis();
			long currTs = beginTs;
			do {
				Thread.sleep(1000L);

				currTs = System.currentTimeMillis();
				if ((currTs - beginTs) > CONSTS.MQ_DEPLOY_MAXTIME) {
					return false;
				}

				executor.echo("......");
			} while (!executor.isPortUsed(port, sessionKey));
			Thread.sleep(3000L);
			
			String addMqUser = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s add_user %s %s",
					erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName, mqUser, mqPwd);
			executor.execSingleLine(addMqUser, sessionKey);
			
			String permission = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s set_permissions -p %s %s '.*' '.*' '.*'",
					erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName, mqVHost, mqUser);
			executor.execSingleLine(permission, sessionKey);
			
			String addRole = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s set_user_tags %s administrator",
					erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName, mqUser);
			executor.execSingleLine(addRole, sessionKey);
			
			String addPluginMgt = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmq-plugins -n %s enable rabbitmq_management --online",
					erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName);
			executor.execSingleLine(addPluginMgt, sessionKey);
			
			String addPluginMgtAgent = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmq-plugins -n %s enable rabbitmq_management_agent --online",
					erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName);
			executor.execSingleLine(addPluginMgtAgent, sessionKey);
			
			if (needJoinCluster) {
				String stopApp = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s stop_app",
												erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName);
				executor.execSingleLine(stopApp, sessionKey);
				
				String resetApp = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s reset",
												erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName);
				executor.execSingleLine(resetApp, sessionKey);

				String joinCluster = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s join_cluster %s",
												erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName, firstNode);
				executor.execSingleLine(joinCluster, sessionKey);

				String startApp = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s start_app",
												erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName);
				executor.execSingleLine(startApp, sessionKey);
			}
			
			// first set mirror group, then set ha.mode policy
			if (needSetHaPlicy) {
				// String setHaPolicy = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n
				// %s@%s set_policy ha-all \".\" '{\"ha-mode\":\"all\",
				// \"ha-sync-mode\":\"automatic\", \"ha-sync-batch-size\":%d}'", erlCookie,
				// deployPath, mqSName, mqHost, CONSTS.HA_SYNC_BATCH_SIZE);
				String setHaPolicy = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n %s set_policy ha-all \".\" '{\"ha-mode\":\"all\", \"ha-sync-mode\":\"automatic\"}'",
													erlCookie, CONSTS.MQ_DEPLOY_PATH, mqSName);
				executor.execSingleLine(setHaPolicy, sessionKey);
			}
			
		} else {
			String err = String.format("%s, port:%s", CONSTS.ERR_MQ_PORT_ISUSED, port);
			DeployLog.pubLog(sessionKey, err);
			
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);

			return false;
		}
		
		// write back BROKER attribute: ERL_COOKIE
		int cookieAttrID = brokerInstanceDtl.getAttribute("ERL_COOKIE").getAttrID();
		ConfigDataService.modComponentAttribute(id, cookieAttrID, erlCookie, result);
		
		// write back deploy flag
		if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.DEPLOYED, result)) {
			return false;
		}
		
		return true;
	}
	
	private boolean undeployRabbit(InstanceDtlBean brokerInstanceDtl, String sessionKey, ResultBean result) {
		if (brokerInstanceDtl == null)
			return false;
		
		String id      = brokerInstanceDtl.getAttribute("BROKER_ID").getAttrValue();
		String ip      = brokerInstanceDtl.getAttribute("IP").getAttrValue();
		String port    = brokerInstanceDtl.getAttribute("PORT").getAttrValue();
		String user    = brokerInstanceDtl.getAttribute("OS_USER").getAttrValue();
		String pwd     = brokerInstanceDtl.getAttribute("OS_PWD").getAttrValue();
		
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			String startInfo = String.format("undeploy mq: broker id:{%s} %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, startInfo);
			
			executor.cd("$HOME", sessionKey);
			
			String path = String.format("%s/%s", CONSTS.MQ_DEPLOY_ROOT_PATH, port);
			if (executor.isPortUsed(port, sessionKey)) {
				executor.cd(path, sessionKey);
				executor.execStopShell(sessionKey);
				Thread.sleep(3000L);
			}
			
			executor.rm(path, true, sessionKey);
			
			String endInfo = String.format("undeploy mq: broker id:{%s} %s:%s begin ......", id, ip, port);
			DeployLog.pubSuccessLog(sessionKey, endInfo);
		} catch (Exception e) {
			if (e instanceof JSchException || e instanceof InterruptedException) {
				String errInfo = String.format("%s:%s", CONSTS.ERR_UNDEPLOY_MQ_FAIL, e.getMessage());
				logger.error(errInfo);
				
				return false;
			}
		} finally {
			if (connected) {
				executor.close();
			}
		}
		
		// write back deploy flag
		if (!ConfigDataService.modInstanceDeployFlag(id, CONSTS.NOT_DEPLOYED, result)) {
			return false;
		}
		
		return true;
	}
	
	private boolean setHostName(Map<String, InstanceDtlBean> brokers,
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

}
