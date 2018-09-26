package ibsp.metaserver.autodeploy;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.ErlUtils;
import ibsp.metaserver.utils.Topology;

public class MQDeployer implements Deployer {
	
	private static Logger logger = LoggerFactory.getLogger(MQDeployer.class);

	@Override
	public boolean deployService(String serviceID, String user, String pwd, String sessionKey, ResultBean result) {
		List<InstanceDtlBean> vbrokerList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		
		if (!MQService.loadServiceInfo(serviceID, vbrokerList, collectd, result))
			return false;
		
		boolean isServDeployed = MetaData.get().isServDepplyed(serviceID);
		
		// deploy vbroker
		if (!deployVBrokerList(serviceID, vbrokerList, sessionKey, result))
			return false;
		
		// deploy collectd
		if (!DeployUtils.deployCollectd(serviceID, collectd, sessionKey, result))
			return false;
		
		if (!isServDeployed) {
			// mod t_service.IS_DEPLOYED = 1
			if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.DEPLOYED, result))
				return false;
			DeployUtils.publishDeployEvent(EventType.e21, serviceID);
		}
		
		return true;
	}
	
	@Override
	public boolean undeployService(String serviceID, String sessionKey, ResultBean result) {
		List<InstanceDtlBean> vbrokerList = new LinkedList<InstanceDtlBean>();
		InstanceDtlBean collectd = new InstanceDtlBean();
		
		if (!MQService.loadServiceInfo(serviceID, vbrokerList, collectd, result))
			return false;
		
		// undeploy collectd
		if (!DeployUtils.undeployCollectd(collectd, sessionKey, true, result))
			return false;
		
		// undeploy vbroker list
		if (!undeployVBrokerList(serviceID, vbrokerList, sessionKey, result))
			return false;
		
		//  t_service.IS_DEPLOYED = 0
		if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.NOT_DEPLOYED, result))
			return false;
		
		// delete all queue
		if (!MQService.delQueueByServId(serviceID, true, result)) {
			return false;
		}
		
		DeployUtils.publishDeployEvent(EventType.e22, serviceID);
		// TODO e31 delete global data queue data
		DeployUtils.publishDeployEvent(EventType.e31, serviceID);
	
		return true;
	}

	@Override
	public boolean deployInstance(String serviceID, String instID,
			String sessionKey, ResultBean result) {
		
		InstanceDtlBean instDtl = MetaDataService.getInstanceDtlWithSubInfo(instID, result);
		if (instDtl == null) {
			String err = String.format("instance id:%s not found!", instID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		int cmptID = instDtl.getInstance().getCmptID();
		boolean deployRet = false;
		switch (cmptID) {
		case 103:  // MQ_VBROKER
			deployRet = deployVBroker(serviceID, instDtl, sessionKey, result);
			if(deployRet) {
				deployRet = MQService.copyQueueToVbroker(serviceID, instDtl, result);
			}
			break;
		case 104:  // MQ_BROKER
			// can't deploy broker alone
			deployRet = true;
			break;
		case 105:  // MQ_SWITCH
			// TODO
			deployRet = true;
			break;
		case 106:  // MQ_COLLECTD
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
		
		InstanceDtlBean instDtl = MetaDataService.getInstanceDtlWithSubInfo(instID, result);
		if (instDtl == null) {
			String err = String.format("instance id:%s not found!", instID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		int cmptID = instDtl.getInstance().getCmptID();
		boolean undeployRet = false;
		switch (cmptID) {
		case 103:  // MQ_VBROKER
			undeployRet = undeployVBroker(serviceID, instDtl, sessionKey, result);
			break;
		case 104:  // MQ_BROKER
			// can't deploy broker alone
			undeployRet = true;
			break;
		case 105:  // MQ_SWITCH
			// TODO
			undeployRet = true;
			break;
		case 106:  // MQ_COLLECTD
			undeployRet = DeployUtils.undeployCollectd(instDtl, sessionKey, false, result);
			break;
		default:
			break;
		}
		
		return undeployRet;
	}

	@Override
	public boolean forceUndeployInstance(String serviceID, String instID, ResultBean result) {
		InstanceDtlBean instDtl = MetaDataService.getInstanceDtlWithSubInfo(instID, result);
		if (instDtl == null) {
			String err = String.format("instance id:%s not found!", instID);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}

		List<InstanceDtlBean> vbrokers = MetaData.get().getVbrokerByServId(serviceID);
		int len = 0;
		for(InstanceDtlBean vb : vbrokers) {
			if(vb.getInstance().getIsDeployed() == CONSTS.DEPLOYED)
				len++;
		}

		if(len <=1) {
			String err = String.format("this service has only one deploy vbroker!");
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}

		int cmptID = instDtl.getInstance().getCmptID();
		boolean undeployRet = false;
		switch (cmptID) {
			case 103:  // MQ_VBROKER
				undeployRet = forceUndeployVBroker(serviceID, instDtl, result);
				break;
			case 104:  // MQ_BROKER
				// can't undeploy broker alone
				undeployRet = true;
				break;
			case 105:  // MQ_SWITCH
				// TODO
				undeployRet = true;
				break;
			case 106:  // MQ_COLLECTD
				/*undeployRet = DeployUtils.undeployCollectd(instDtl, "", false, result);*/
				break;
			default:
				break;
		}

		return undeployRet;
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
		
		return true;
	}

	@Override
	public boolean deleteInstance(String serviceID, String instID,
			String sessionKey, ResultBean result) {
		return MetaDataService.deleteInstance(serviceID, instID, result);
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
	
	private boolean undeployVBrokerList(String serviceID, List<InstanceDtlBean> vbrokerList,
			String sessionKey, ResultBean result) {
		
		for (int i = 0; i < vbrokerList.size(); i++) {
			InstanceDtlBean vbrokerInstanceDtl = vbrokerList.get(i);
			
			if (!undeployVBroker(serviceID, vbrokerInstanceDtl, sessionKey, result))
				return false;
		}
		
		return true;
	}
	
	private boolean undeployVBroker(String serviceID, InstanceDtlBean vbrokerInstanceDtl,
									 String sessionKey, ResultBean result) {
		String vbrokerId = vbrokerInstanceDtl.getAttribute("VBROKER_ID").getAttrValue();

		if (vbrokerInstanceDtl.getInstance().getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("mq vbroker id:%s is deployed ......", vbrokerId);
			DeployLog.pubSuccessLog(sessionKey, info);

			return true;
		}

		boolean allOk = true;

		Map<String, InstanceDtlBean> brokers = vbrokerInstanceDtl.getSubInstances();
		Set<Entry<String, InstanceDtlBean>> entrySet = brokers.entrySet();
		for (Entry<String, InstanceDtlBean> entry : entrySet) {
			InstanceDtlBean brokerInstanceDtl = entry.getValue();

			allOk &= undeployRabbit(brokerInstanceDtl, sessionKey, result);
			if (!allOk)
				break;

			// write back deploy flag
			if (!ConfigDataService.modInstanceDeployFlag(vbrokerId, CONSTS.NOT_DEPLOYED, result)) {
				return false;
			}
			DeployUtils.publishDeployEvent(EventType.e24, vbrokerId);
		}

		return allOk;
	}

	private boolean forceUndeployVBroker(String serviceID, InstanceDtlBean vbrokerInstanceDtl, ResultBean result) {
		String vbrokerId = vbrokerInstanceDtl.getAttribute("VBROKER_ID").getAttrValue();

		if (vbrokerInstanceDtl.getInstance().getIsDeployed().equals(CONSTS.NOT_DEPLOYED)) {
			String info = String.format("mq vbroker id:%s is deployed ......", vbrokerId);

			return true;
		}

		boolean allOk = true;

		Map<String, InstanceDtlBean> brokers = vbrokerInstanceDtl.getSubInstances();
		Set<Entry<String, InstanceDtlBean>> entrySet = brokers.entrySet();
		for (Entry<String, InstanceDtlBean> entry : entrySet) {
			InstanceDtlBean brokerInstanceDtl = entry.getValue();
			ConfigDataService.modInstanceDeployFlag(brokerInstanceDtl.getInstID(), CONSTS.NOT_DEPLOYED, result);
			DeployUtils.publishDeployEvent(EventType.e24, brokerInstanceDtl.getInstID());
		}

		if (!ConfigDataService.modInstanceDeployFlag(vbrokerId, CONSTS.NOT_DEPLOYED, result)) {
			return false;
		}
		DeployUtils.publishDeployEvent(EventType.e24, vbrokerId);

		return allOk;
	}
	
	private boolean deployVBroker(String serviceID, InstanceDtlBean vbrokerInstanceDtl,
			String sessionKey, ResultBean result) {
		
		String vbrokerId = vbrokerInstanceDtl.getAttribute("VBROKER_ID").getAttrValue();
		
		if (vbrokerInstanceDtl.getInstance().getIsDeployed().equals(CONSTS.DEPLOYED)) {
			String info = String.format("mq vbroker id:%s is deployed ......", vbrokerId);
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
		
		// start with erlang node with -name, no need to set hosts
		// set host name
		//if (isCluster) {
		//	DeployUtils.setHostName(brokers, sessionKey, result);
		//}
		
		Set<Entry<String, InstanceDtlBean>> entrySet = brokers.entrySet();
		for (Entry<String, InstanceDtlBean> entry : entrySet) {
			InstanceDtlBean brokerInstanceDtl = entry.getValue();
			
			String brokerId = brokerInstanceDtl.getAttribute("BROKER_ID").getAttrValue();
			String ip       = brokerInstanceDtl.getAttribute("IP").getAttrValue();
			String port     = brokerInstanceDtl.getAttribute("PORT").getAttrValue();
			String user     = brokerInstanceDtl.getAttribute("OS_USER").getAttrValue();
			String pwd      = brokerInstanceDtl.getAttribute("OS_PWD").getAttrValue();
			
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
				
				if (cnt == 1) {
					masterID = brokerId;
					firstNode = String.format("%s@%s", brokerId, ip);
				}
				
				allOk &= deployRabbit(executor, brokerInstanceDtl, needJoinCluster, needSetHaPlicy,
										erlCookie, firstNode, sessionKey, result);
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
			vbrokerInstanceDtl.setAttribute("MASTER_ID", masterID);
			ConfigDataService.modComponentAttribute(vbrokerId, attrID, masterID, result);
			
			ConfigDataService.modInstanceDeployFlag(vbrokerId, CONSTS.DEPLOYED, result);
			DeployUtils.publishDeployEvent(EventType.e23, vbrokerId);
			DeployUtils.publishInstanceEvent(EventType.e4, vbrokerId);
		} else {
			// uninstall the success
			for (InstanceDtlBean brokerInstanceDtl : success) {
				undeployRabbit(brokerInstanceDtl, sessionKey, result);
			}
		}
		
		success.clear();
		
		return allOk;
	}
	
	private boolean deployErlang(SSHExecutor executor, 
			String sessionKey, ResultBean result) throws InterruptedException {
		
		String deployErlPath = "erl_otp";
		//String otpFile = "otp_R15B";
		String otpFile = "otp_R20.3";
		
		DeployFileBean erlangFile = MetaData.get().getDeployFile(CONSTS.SERV_MQ_ERLANG);
		
		// check erlang
		if (!executor.isFileExistInCurrPath(deployErlPath, null)) {
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
			boolean needJoinCluster, boolean needSetHaPlicy, String erlCookie,
			String firstNode, String sessionKey, ResultBean result) throws InterruptedException {
		
		String ip      = brokerInstanceDtl.getAttribute("IP").getAttrValue();
		String port    = brokerInstanceDtl.getAttribute("PORT").getAttrValue();
		String mgrPort = brokerInstanceDtl.getAttribute("MGR_PORT").getAttrValue();
		String id      = brokerInstanceDtl.getAttribute("BROKER_ID").getAttrValue();
		String mqUser  = CONSTS.MQ_DEFAULT_USER;
		String mqPwd   = CONSTS.MQ_DEFAULT_PWD;
		String mqVHost = CONSTS.MQ_DEFAULT_VHOST;
		
		String mqName = String.format("%s@%s", id, ip);
		
		DeployFileBean rabbitMQFile = MetaData.get().getDeployFile(CONSTS.SERV_MQ_RABBIT);
		String deployMQPath = String.format("%s/%s", CONSTS.MQ_DEPLOY_ROOT_PATH, port);
		
		if (executor.isPortUsed(port,null)) {
			String info = String.format("port %s is already in use ......", port);
			DeployLog.pubLog(sessionKey, info);
			return false;
		}
		
		if (executor.isPortUsed(mgrPort,null)) {
			DeployLog.pubLog(sessionKey, "port "+mgrPort+" is already in use......");
			return false;
		}
		
		executor.cd("$HOME");
		
		boolean isDestPathExist = executor.isFileExistInCurrPath(deployMQPath, null);
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
		
		executor.tgzUnpack(rabbitMQFile.getFileName(), sessionKey);
		executor.rm(rabbitMQFile.getFileName(), false, sessionKey);
		
		String startContext = String.format(
				"ERL_COOKIE=%s RABBITMQ_NODE_PORT=%s RABBITMQ_NODENAME=%s RABBITMQ_USE_LONGNAME=true DISK_FREE_LIMIT=%s VM_MEMORY_HIGH_WATERMARK=%s MANAGEMENT_LISTEN_PORT=%s ./%s/sbin/rabbitmq-server -detached ",
				erlCookie, port, mqName, "" + CONSTS.DISK_FREE_LIMIT, "" + CONSTS.VM_MEMORY_HIGH_WATERMARK,
				mgrPort, CONSTS.MQ_DEPLOY_PATH);
		executor.createStartShell(startContext);

		String stopContext = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s stop ",
				CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie);
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
			
			String addMqUser = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s add_user %s %s",
					CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie, mqUser, mqPwd);
			executor.execSingleLine(addMqUser, sessionKey);
			
			String permission = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s set_permissions -p %s %s '.*' '.*' '.*'",
					CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie, mqVHost, mqUser);
			executor.execSingleLine(permission, sessionKey);
			
			String addRole = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s set_user_tags %s administrator",
					CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie, mqUser);
			executor.execSingleLine(addRole, sessionKey);
			
			String addPluginMgt = String.format("./%s/sbin/rabbitmq-plugins -n %s -l --erlang-cookie %s enable rabbitmq_management --online",
					CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie);
			executor.execSingleLine(addPluginMgt, sessionKey);
			
			String addPluginMgtAgent = String.format("./%s/sbin/rabbitmq-plugins -n %s -l --erlang-cookie %s enable rabbitmq_management_agent --online",
					CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie);
			executor.execSingleLine(addPluginMgtAgent, sessionKey);
			
			if (needJoinCluster) {
				String stopApp = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s stop_app",
												CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie);
				executor.execSingleLine(stopApp, sessionKey);
				
				String resetApp = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s reset",
												CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie);
				executor.execSingleLine(resetApp, sessionKey);

				String joinCluster = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s join_cluster %s",
												CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie, firstNode);
				executor.execSingleLine(joinCluster, sessionKey);

				String startApp = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s start_app",
												CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie);
				executor.execSingleLine(startApp, sessionKey);
			}
			
			// first set mirror group, then set ha.mode policy
			if (needSetHaPlicy) {
				// String setHaPolicy = String.format("ERL_COOKIE=%s ./%s/sbin/rabbitmqctl -n
				// %s@%s set_policy ha-all \".\" '{\"ha-mode\":\"all\",
				// \"ha-sync-mode\":\"automatic\", \"ha-sync-batch-size\":%d}'", erlCookie,
				// deployPath, mqSName, mqHost, CONSTS.HA_SYNC_BATCH_SIZE);
				String setHaPolicy = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s set_policy ha-all \".\" '{\"ha-mode\":\"all\", \"ha-sync-mode\":\"automatic\", \"ha-sync-batch-size\":%d}'",
													CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie, CONSTS.MQ_HA_SYNC_BATCH_SIZE);
				executor.execSingleLine(setHaPolicy, sessionKey);
			}
			
			// rabbitmq 3.7 vhost add new properties: max-connections,max-queues
			String vhostLimits = String.format("./%s/sbin/rabbitmqctl -n %s -l --erlang-cookie %s set_vhost_limits -p %s '{\"max-connections\":%d, \"max-queues\":%d}'",
													CONSTS.MQ_DEPLOY_PATH, mqName, erlCookie, mqVHost, CONSTS.MQ_VHOST_MAX_CONNS, CONSTS.MQ_VHOST_MAX_QUEUES);
			executor.execSingleLine(vhostLimits, sessionKey);
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
		DeployUtils.publishDeployEvent(EventType.e23, id);
		
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
			
			executor.cd("$HOME", sessionKey);
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
		DeployUtils.publishDeployEvent(EventType.e24, id);
		
		return true;
	}

}
