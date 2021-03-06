package ibsp.metaserver.dbservice;

import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.*;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.exception.CRUDException;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.MonitorData;
import ibsp.metaserver.global.ServiceData;
import ibsp.metaserver.rabbitmq.IMQClient;
import ibsp.metaserver.rabbitmq.MQClientImpl;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.SRandomGenerator;
import ibsp.metaserver.utils.UUIDUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQService {
	
	private static final Logger logger = LoggerFactory.getLogger(MQService.class);
	
	private final static String SEL_ALL_QUEUE = "SELECT q.QUEUE_ID, q.QUEUE_NAME, q.IS_DURABLE, q.IS_ORDERED, q.IS_PRIORITY, q.QUEUE_TYPE, q.IS_DEPLOY, q.SERV_ID, t.SERV_NAME "
											+     "FROM t_mq_queue q left join t_service t "
											+     "on q.SERV_ID = t.INST_ID";
	
	private final static String SEL_QUEUE     = "SELECT q.QUEUE_ID, q.QUEUE_NAME, q.IS_DURABLE, q.IS_ORDERED, q.IS_PRIORITY, q.QUEUE_TYPE, q.IS_DEPLOY, q.SERV_ID, t.SERV_NAME "
											+     "FROM t_mq_queue q left join t_service t "
											+     "on q.SERV_ID = t.INST_ID "
											+     "where q.QUEUE_ID = ?";
	
	private final static String SEL_ALL_PERMNENT_TOPIC = "SELECT t.CONSUMER_ID, t.REAL_QUEUE, t.MAIN_TOPIC, t.SUB_TOPIC, t.QUEUE_ID FROM t_mq_permnent_topic t";
	
	private final static String INSERT_PERMNENT_TOPIC  = "INSERT INTO t_mq_permnent_topic(CONSUMER_ID, REAL_QUEUE, MAIN_TOPIC, SUB_TOPIC, QUEUE_ID) values (?,?,?,?,?)";

	private final static String SELECT_PERMNENT_TOPIC  = "SELECT t.CONSUMER_ID, t.REAL_QUEUE, t.MAIN_TOPIC, t.SUB_TOPIC, t.QUEUE_ID FROM t_mq_permnent_topic t where t.CONSUMER_ID = ?";
	
	private final static String DEL_PERMNENT_TOPIC     = "DELETE FROM t_mq_permnent_topic where CONSUMER_ID = ?";
	
	private final static String DEL_PERMNENT_TOPIC_BY_SERVID       = "DELETE FROM t_mq_permnent_topic where QUEUE_ID in (SELECT QUEUE_ID from t_mq_queue where SERV_ID = ?)";
	private final static String UPDATE_QUEUE_UNDEPLOYED_BY_SERVID  = "UPDATE t_mq_queue SET IS_DEPLOY = ? where SERV_ID = ? ";
	private final static String DEL_QUEUE_BY_SERVID                = "DELETE FROM t_mq_queue WHERE SERV_ID = ? "; 

	private final static String INSERT_VBROKER_COLLECT_INFO        = "INSERT INTO t_mo_mq_collect (VBROKER_ID,PRODUCE_RATE," +
			"PRODUCE_COUNTS,CONSUMER_RATE,CONSUMER_COUNTS,REC_TIME) values (?,?,?,?,?,?)";

	private final static String INSERT_QUEUE_COLLECT_INFO          = "INSERT INTO t_mo_mq_collect (QUEUE_ID,PRODUCE_RATE," +
			"PRODUCE_COUNTS,CONSUMER_RATE,CONSUMER_COUNTS,REC_TIME) values (?,?,?,?,?,?)";

	private final static String INSERT_QUEUE_COLLECT_DETAIL_INFO   = "INSERT INTO t_mo_mq_queue_detail (VBROKER_ID, QUEUE_ID,CONSUMER_ID,PRODUCE_RATE," +
            "PRODUCE_COUNTS,CONSUMER_RATE,CONSUMER_COUNTS,REC_TIME) values (?,?,?,?,?,?,?,?)";

	private final static String SEL_VBROKER_MONITOR_COLLECT = "SELECT PRODUCE_RATE," +
			"CONSUMER_RATE,REC_TIME from t_mo_mq_collect WHERE REC_TIME BETWEEN ? AND ? AND VBROKER_ID = ? ORDER BY REC_TIME ASC";

	private final static String SEL_QUEUE_MONITOR_COLLECT   = "SELECT PRODUCE_RATE," +
			"CONSUMER_RATE,REC_TIME from t_mo_mq_collect WHERE REC_TIME BETWEEN ? AND ? AND QUEUE_ID = ?  ORDER BY REC_TIME ASC";

	public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> vbrokerList,
			InstanceDtlBean collectd, ResultBean result) {
		
		Map<Integer, String> serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
		if (serviceStub == null) {
			return false;
		}
		
		return  getVBrokersByServIdOrServiceStub(serviceID, serviceStub, vbrokerList, result) &&
				getCollectdInfoByServIdOrServiceStub(serviceID, serviceStub, collectd, result);
	}
	
	public static boolean getVBrokersByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			List<InstanceDtlBean> vbrokerList, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
		
		Integer vbrokerContainerCmptID = MetaData.get().getComponentID("MQ_VBROKER_CONTAINER");
		String vbrokerContainerID = serviceStub.get(vbrokerContainerCmptID);
		Set<String> vbrokers = MetaDataService.getSubNodes(vbrokerContainerID, result);
		if (vbrokers == null || vbrokers.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("vbroker container subnode is null ......");
			return false;
		}
		
		for (String vbrokerId : vbrokers) {
			InstanceDtlBean vbrokerInstance = MetaDataService.getInstanceDtl(vbrokerId, result);
			if (vbrokerInstance == null) {
				return false;
			}
			
			Set<String> brokerIds = MetaDataService.getSubNodes(vbrokerId, result);
			for (String brokerId : brokerIds) {
				InstanceDtlBean brokerInstance = MetaDataService.getInstanceDtl(brokerId, result);
				if (brokerInstance == null) {
					return false;
				}
				
				vbrokerInstance.addSubInstance(brokerInstance);
			}
			
			vbrokerList.add(vbrokerInstance);
		}
		
		return true;
	}
	
	public static boolean getCollectdInfoByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			InstanceDtlBean collectd, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
		Integer mqCollectdCmptID = MetaData.get().getComponentID("MQ_COLLECTD");
		String id = serviceStub.get(mqCollectdCmptID);
		InstanceDtlBean collectdInstance = MetaDataService.getInstanceDtl(id, result);
		if (collectdInstance == null) {
			String err = String.format("MQ collectd id:%s, info missing ......", id);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		collectd.setInstance(collectdInstance.getInstance());
		collectd.setAttrMap(collectdInstance.getAttrMap());
		return true;
	}
	
	public static QueueBean getQueue(String queueID) {
		QueueBean queueBean = null;
		try {
			SqlBean sqlBean = new SqlBean(SEL_QUEUE);
			sqlBean.addParams(new Object[]{queueID});
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> queryResult = c.queryForList();
			if (queryResult != null && !queryResult.isEmpty()) {
				HashMap<String, Object> item = queryResult.get(0);
				queueBean = QueueBean.convert(item);
			}
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
		}
		
		return queueBean;
	}

	public static List<QueueBean> getAllQueues() {
		List<QueueBean> list = null;
		try {
			CRUD c = new CRUD();
			List<Object> paramList = new LinkedList<Object>();
			c.putSql(SEL_ALL_QUEUE, paramList);
			List<HashMap<String, Object>> queryResult = c.queryForList();
			
			if (queryResult != null) {
				list = new ArrayList<QueueBean>(queryResult.size());
				
				Iterator<HashMap<String, Object>> iter = queryResult.iterator();
				while (iter.hasNext()) {
					HashMap<String, Object> mateMap = iter.next();
					QueueBean queueBean = QueueBean.convert(mateMap);
					list.add(queueBean);
				}
			}
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
		}
		
		return list;
	}
	
	public static JsonArray getQueueList(Map<String,String> params, ResultBean resultBean) {
		JsonArray jsonArray = null;
		String sql="select q.queue_id, q.queue_name, queue_type, q.is_durable, q.queue_type, "
				+ "q.is_priority, q.is_ordered, q.is_deploy "
				+ "from t_mq_queue q where q.serv_id = ?";
		
		if (params!=null && params.size()>0) {
			String pageSizeString = params.get(CONSTS.PAGE_SIZE);
			String pageNumString = params.get(CONSTS.PAGE_NUMBER);
			String servId = params.get(FixHeader.HEADER_SERV_ID);
			String queueType = params.get(FixHeader.HEADER_QUEUE_TYPE);
			String queueName = params.get(FixHeader.HEADER_QUEUE_NAME);
			
			if(HttpUtils.isNull(servId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
			}
			
			if(HttpUtils.isNotNull(queueName)) {
				sql += " and q.queue_name like '%" + queueName +  "%' ";
			}
			
			if(HttpUtils.isNotNull(queueType)) {
				sql += " and q.queue_type = '"+queueType+"' ";
			}
			
			if(HttpUtils.isNotNull(pageSizeString)&&HttpUtils.isNotNull(pageNumString)){
				
				int pageSize = Integer.parseInt(pageSizeString);
				int pageNum = Integer.parseInt(pageNumString);
				
				int start = (pageNum - 1) * pageSize;
				
				sql += " limit "+start+","+pageSize;
			}

			CRUD crud = new CRUD();
			crud.putSql(sql, new Object[] {servId});
			try {
				jsonArray = crud.queryForJSONArray();
			} catch (CRUDException e) {
				e.printStackTrace();
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(e.getMessage());
			}
			
		}else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo("params is null");
		}

		return jsonArray;
	}
	
	public static JsonObject getQueueListCount (Map<String,String> params, ResultBean resultBean){
		JsonObject countJson = new JsonObject();
		String sql="select count(1) from t_mq_queue q where q.serv_id = ?";
		
		if (params!=null && params.size()>0) {
		
			String servId = params.get(FixHeader.HEADER_SERV_ID);
			String queueType = params.get(FixHeader.HEADER_QUEUE_TYPE);
			String queueName = params.get(FixHeader.HEADER_QUEUE_NAME);

			if(HttpUtils.isNull(servId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
			}

			if(HttpUtils.isNotNull(queueName)) {
				sql += " and q.queue_name like '%" + queueName +  "%' ";
			}

			if(HttpUtils.isNotNull(queueType)) {
				sql += " and q.queue_type = '"+queueType+"' ";
			}

			CRUD crud = new CRUD();
			crud.putSql(sql, new Object[] {servId});
			try {
				int count = crud.queryForCount();
				countJson.put("COUNT", count);
			} catch (CRUDException e) {
				e.printStackTrace();
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(e.getMessage());
			}
			
		}else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo("params is null");
		}

		return countJson;
	}
	
	public static boolean saveQueue(Map<String, String> params, ResultBean resultBean) {
		boolean res = false;
		if (params != null) {
			String _qid = params.get(FixHeader.HEADER_QUEUE_ID);
			String _qname = params.get(FixHeader.HEADER_QUEUE_NAME);
			String _qtype = params.get(FixHeader.HEADER_QUEUE_TYPE);
			String _durable = params.get(FixHeader.HEADER_IS_DURABLE);
			String _servid = params.get(FixHeader.HEADER_SERV_ID);
			
			String ordered = params.get(FixHeader.HEADER_GLOBAL_ORDERED);
			String _ordered = HttpUtils.isNull(ordered) ? CONSTS.NOT_GLOBAL_ORDERED : ordered;
			
			String priority = params.get(FixHeader.HEADER_IS_PRIORITY);
			String _priority = HttpUtils.isNull(priority) ? CONSTS.NOT_PRIORITY : priority;
			
			if(HttpUtils.isNull(_qname) || HttpUtils.isNull(_qtype) || HttpUtils.isNull(_durable) || 
					HttpUtils.isNull(_servid)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return false;
			}
			
			// 参数有效性检查
			if (!(CONSTS.TYPE_QUEUE.equals(_qtype) || CONSTS.TYPE_TOPIC.equals(_qtype))) {
				String err = String.format("%s, request is:%s", CONSTS.ERR_QUEUE_TYPE_ERROR, _qtype);
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(err);
				return false;
			}
			
			if (!(CONSTS.DURABLE.equals(_durable) || CONSTS.NOT_DURABLE.equals(_durable))) {
				String err = String.format("%s, request is:%s", CONSTS.ERR_DURABLE_TYPE_ERROR, _durable);
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(err);
				return false;
			}
			
			if (!(CONSTS.GLOBAL_ORDERED.equals(_ordered) || CONSTS.NOT_GLOBAL_ORDERED.equals(_ordered))) {
				String err = String.format("%s, request is:%s", CONSTS.ERR_ORDERED_TYPE_ERROR, _durable);
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(err);
				return false;
			}
			
			if (!(CONSTS.PRIORITY.equals(_priority) || CONSTS.NOT_PRIORITY.equals(_priority))) {
				String err = String.format("%s, request is:%s", CONSTS.ERR_PRIORITY_TYPE_ERROR, _priority);
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(err);
				return false;
			}
			
			String qid = "";
			QueueBean queueBean = null;
			
			if (HttpUtils.isNull(_qid)) {
				boolean allreadyExist = MetaData.get().isQueueNameExistsByName(_qname);
				if (allreadyExist) {
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(CONSTS.ERR_QUEUE_EXISTS);
					return false;
				}
			}

			// service_id 没送或送空串则返回
			if (!HttpUtils.isNotNull(_servid)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("service_id is null or empty!");
				return false;
			}

			// 全局有序队列不能创建在包含多vbroker的group上
			if (_ordered.equals(CONSTS.GLOBAL_ORDERED)) {
				if (!ServiceData.get().isServContainSingleVBroker(_servid)) {
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo("全局有序队列仅能创建在包含单个vbroker的group上");
					return false;
				}
			}
			CRUD curd = new CRUD();
			
			if (HttpUtils.isNotNull(_qid)) {  //修改
				//修改队列信息
				qid = _qid;
				String updateSql = "update t_mq_queue q set q.queue_name=?,q.queue_type=?,q.is_durable=? where q.queue_id=?";
				SqlBean uSqlBean = new SqlBean(updateSql);
				uSqlBean.addParams(new Object[]{_qname, _qtype, _durable, _qid});
				curd.putSqlBean(uSqlBean);
			} else {//添加
				qid = UUIDUtils.genUUID();
				String vbSql = "insert into t_mq_queue(queue_id,queue_name,queue_type,is_durable,is_ordered,is_priority,serv_id,rec_time)value(?,?,?,?,?,?,?,?)";
				SqlBean iSqlBean = new SqlBean(vbSql);
				iSqlBean.addParams(new Object[]{qid,_qname,_qtype,_durable,_ordered,_priority,_servid,HttpUtils.getCurrTimestamp()});
				curd.putSqlBean(iSqlBean);	
			}
		
			res = curd.executeUpdate(resultBean);
			if (!res) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(resultBean.getRetInfo());
			} else {
				resultBean.setRetCode(CONSTS.REVOKE_OK);
				resultBean.setRetInfo("");
				
				queueBean = new QueueBean(qid, _qname, _durable, _ordered, _priority,
						_qtype, CONSTS.NOT_DEPLOYED, _servid, MetaData.get().getServiceName(_servid));
				MetaData.get().saveQueue(qid, queueBean);
				
				JsonObject evJson = new JsonObject();
				evJson.put("QUEUE_ID", qid);
				
				EventBean ev = new EventBean(EventType.e9);
				ev.setUuid(MetaData.get().getUUID());
				ev.setJsonStr(evJson.toString());
				EventBusMsg.publishEvent(ev);
			}
		} else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo("params is null");
		}
		
		return res;
	}

	public static boolean delQueue(Map<String, String> params, ResultBean resultBean) {
		boolean res = false;
		if(params != null) {
			String queueId = params.get(FixHeader.HEADER_QUEUE_ID);
			String servID = params.get(FixHeader.HEADER_SERV_ID);
			
			if(HttpUtils.isNull(queueId) || HttpUtils.isNull(servID)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return false;
			}
			
			if(!MetaData.get().isQueueNameExistsById(queueId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("this queue is not exist!");
				return false;
			}
			
			QueueBean queueBean = MetaData.get().getQueueBeanById(queueId);
			
			if(queueBean == null) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("rabbitmq delete queue fail");
				return false;
			}
			
			if(queueBean.getQueueType().equals(CONSTS.TYPE_TOPIC) && MetaData.get().hasPermnentTopicByQueueId(queueId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("this topic bind permnent_topic!");
				return false;
			}
				
			//先卸载
			if(queueBean.getDeploy().equals(CONSTS.DEPLOYED)) {
				List<InstanceDtlBean> list = MetaData.get().getMasterBrokersByServId(servID);
				res = deleteRabbitQueue(queueBean, list);
				if(!res) {
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(CONSTS.ERR_QUEUE_NOT_EXISTS);
					return false;
				}
			}
			
			CRUD curd = new CRUD();
			String dSql = "delete from t_mq_queue where queue_id = ?";
			SqlBean sqlBean = new SqlBean(dSql);
			sqlBean.addParams(new Object[]{queueId});
			curd.putSqlBean(sqlBean);
			
			res = curd.executeUpdate(resultBean);
			if (!res) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(resultBean.getRetInfo());
			} else {
				resultBean.setRetCode(CONSTS.REVOKE_OK);
				resultBean.setRetInfo("");
				
				MetaData.get().delQueue(queueId);
				
				JsonObject evJson = new JsonObject();
				evJson.put("QUEUE_ID", queueId);
				
				EventBean ev = new EventBean(EventType.e11);
				ev.setUuid(MetaData.get().getUUID());
				ev.setJsonStr(evJson.toString());
				EventBusMsg.publishEvent(ev);
			}
			
		}else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
		}
		return res;
	}
	
	public static boolean releaseQueue(Map<String, String> params, ResultBean resultBean) {
		boolean res = false;
		if(params != null) {
			
			String queueName = params.get(FixHeader.HEADER_QUEUE_NAME);
			String servId = params.get(FixHeader.HEADER_SERV_ID);
			if (HttpUtils.isNotNull(queueName) && HttpUtils.isNotNull(servId)) {
				
				QueueBean queueBean = MetaData.get().getQueueBeanByName(queueName);
				if(queueBean != null) {
					if(CONSTS.NOT_DEPLOYED.equals(queueBean.getDeploy())) {
						res = releaseQueueToMQByServId(queueBean, servId, resultBean);
						if(res) {
							resultBean.setRetCode(CONSTS.REVOKE_OK);
							resultBean.setRetInfo("");
						}
						
					}else {
						resultBean.setRetCode(CONSTS.REVOKE_NOK);
						resultBean.setRetInfo(CONSTS.ERR_QUEUE_ALLREADY_DEPLOYED);
					}
				}else {
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(CONSTS.ERR_QUEUE_NOT_EXISTS);
				}
			}else {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			}
		}else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo("params is null");
		}
		return res;
	}
	
	private static boolean releaseQueueToMQByServId(QueueBean queueBean, String servId, ResultBean resultBean) {
		boolean createOk = true;
		List<InstanceDtlBean> list = MetaData.get().getMasterBrokersByServId(servId);
		
		if (list != null && !list.isEmpty()) {
			String queueName = queueBean.getQueueName();
			boolean priority = queueBean.getPriority().equals(CONSTS.PRIORITY);
			List<InstanceDtlBean> succList = new ArrayList<>();
			
			for (InstanceDtlBean brokerBean : list) {
				if (!createOk) {
					deleteRabbitQueueByName(queueName, succList);
					break;
				}
				
				createOk &= releaseQueueToMQ(queueName, priority, brokerBean, resultBean);
				if(createOk) {
					succList.add(brokerBean);
				}
			}
			
			if (createOk) {
				CRUD crud = new CRUD();
				String uSql = "update t_mq_queue set is_deploy=? where queue_id=?";
				crud.putSql(uSql, new Object[]{CONSTS.DEPLOYED,queueBean.getQueueId()});
				
				//TODO 往mo_queue 和 mo_queue_accu_dtl表插入数据
				createOk = crud.executeUpdate(resultBean);
				if (createOk) {
					queueBean.setDeploy(CONSTS.DEPLOYED);
					MetaData.get().saveQueue(queueBean.getQueueId(), queueBean);
					
					JsonObject evJson = new JsonObject();
					evJson.put("QUEUE_ID", queueBean.getQueueId());
					
					EventBean ev = new EventBean(EventType.e10);
					ev.setUuid(MetaData.get().getUUID());
					ev.setJsonStr(evJson.toString());
					EventBusMsg.publishEvent(ev);
				} else {
					deleteRabbitQueue(queueBean, succList);
				}
			} else {
				deleteRabbitQueue(queueBean, succList);
			}
		}
		
		return createOk;
	}
	
	private static boolean releaseQueueToMQ(String queueName, boolean priority, InstanceDtlBean brokerBean, ResultBean resultBean) {
		List<String> queues = new ArrayList<String>();
		queues.add(queueName);
		
		List<Boolean> priorites = new ArrayList<Boolean>();
		priorites.add(priority);
		
		return releaseQueuesToMQ(queues, priorites, brokerBean, resultBean);
	}
	
	private static boolean releaseQueuesToMQ(List<String> queues, List<Boolean> priorities, InstanceDtlBean brokerBean, ResultBean resultBean) {
		boolean createOk = true;

		String ip = brokerBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
		String user = CONSTS.MQ_DEFAULT_USER;
		String pwd = CONSTS.MQ_DEFAULT_PWD;
		String brokerId= brokerBean.getInstID();
		String vhost = CONSTS.MQ_DEFAULT_VHOST;
		
		int port = Integer.valueOf(brokerBean.getAttribute(FixHeader.HEADER_PORT).getAttrValue());
		
		IMQClient c = new MQClientImpl();
				
		int cf = c.connect(user, pwd, vhost, ip, port);
		try {
			if (cf == 0) {
				int createRet = -1;
				//for(String queueName : queues) {
				for (int idx = 0; idx < queues.size(); idx++) {
					String queueName = queues.get(idx);
					Boolean priority = priorities.get(idx);
					int maxPriority = priority.booleanValue() ? CONSTS.MQ_MAX_QUEUE_PRIORITY : CONSTS.MQ_DEFAULT_QUEUE_PRIORITY;
					
					if(!createOk) {
						break;
					}
					createRet = c.createQueue(queueName, false, true, maxPriority);
					if (createRet != 0) {
						String err = String.format("release queue:%s error, user:%s pwd:%s vhost:%s %s:%d",
								queueName, user, pwd, vhost, ip, port);
						logger.error(err);
						
						createOk = false;
					} else {
						createOk = true;
					}
				}
			} else {
				String err = String.format("create queue on borker:[BrokerId:%s, IP:%s, port:%d] fail.", brokerId, ip, port);
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(err);
				createOk = false;
			}
		} catch (Exception e) {
			createOk = false;
			
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo(e.getMessage());
			logger.error(e.getMessage(), e);
			
		} finally {
			if(cf==0)
				c.close();
		}
	
		return createOk;
	}
	
	private static boolean releasePermnentToMQ(String queueName, String servId, String subTopic, ResultBean resultBean) {
		List<InstanceDtlBean> list = MetaData.get().getMasterBrokersByServId(servId);
		
		return releasePermnentToMQ(queueName, CONSTS.MQ_DEFAULT_QUEUE_PRIORITY, list, subTopic, resultBean);
	}
	
	private static boolean releasePermnentToMQ(String queueName, int maxPriority, List<InstanceDtlBean> list, String subTopic, ResultBean resultBean) {
		
		boolean createOk = true;
		if (list==null || list.size() == 0) {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo("No broker found in service...");
			return false;
		}
						
		String ip = "";
		String user = "";
		String pwd ="";
		String brokerId="";
		String vhost ="";
		List<InstanceDtlBean> succList = new ArrayList<>();
			
		for (InstanceDtlBean brokerBean : list) {
			if (!createOk)
				break;
			
			ip = brokerBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
			user = CONSTS.MQ_DEFAULT_USER;
			pwd = CONSTS.MQ_DEFAULT_PWD;
			brokerId= brokerBean.getInstID();
			int port = Integer.valueOf(brokerBean.getAttribute(FixHeader.HEADER_PORT).getAttrValue());
			vhost = CONSTS.MQ_DEFAULT_VHOST;
			IMQClient c = new MQClientImpl();
			
			int cf = c.connect(user, pwd, vhost, ip, port);
			try {
				if (cf != 0) {
					String err = String.format("create queue on borker:[BrokerId:%s, IP:%s, port:%d] fail.", brokerId, ip, port);
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(err);
					createOk = false;
					break;
				}
					
				int createRet = -1;
				createRet = c.createQueue(queueName, false, true, maxPriority);
				if (createRet != 0) {
					String err = String.format("release queue:%s error, user:%s pwd:%s vhost:%s %s:%d",
							queueName, user, pwd, vhost, ip, port);
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(err);
					createOk = false;
					break;
				}

				//subTopic and mainTopic are the same under permanent mode
				int retBind = c.queueBind(queueName, "amq.direct", subTopic);
				if (retBind != 0) {
					String err = String.format("queue bind on broker %s:%d %s fail.", ip, port, vhost);
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(err);
					createOk = false;
					break;
				}
					
				succList.add(brokerBean);
				createOk = true;
				
			} catch (Exception e) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(e.getMessage());
				logger.error(e.getMessage(), e);
				createOk = false;
				break;
			} finally {
				if(cf==0)
					c.close();
			}
			
			if (!createOk) {
				deleteRabbitQueueByName(queueName, succList);
			}
		}
		return createOk;
	}
	
	private static boolean deleteRabbitQueue(QueueBean queueBean, List<InstanceDtlBean> list) {
		if (list == null)
			return false;
		boolean res = false;
		String queueName = queueBean.getQueueName();
		res = deleteRabbitQueueByName(queueName, list);
		return res;
	}
	
	private static boolean deleteRabbitQueueByName(String queueName, List<InstanceDtlBean> list) {
		if (list == null)
			return false;
		boolean res = false;
		String ip = "";
		int port = 0;
		String user = CONSTS.MQ_DEFAULT_USER;
		String pwd = CONSTS.MQ_DEFAULT_PWD;
		String vhost = CONSTS.MQ_DEFAULT_VHOST;
		
		for (InstanceDtlBean brokerBean : list) {
			ip = brokerBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
			port = Integer.valueOf(brokerBean.getAttribute(FixHeader.HEADER_PORT).getAttrValue());

			IMQClient c = new MQClientImpl();
			int cf = c.connect(user, pwd, vhost, ip, port);
			try {
				if(cf==0) {
					int qf = -1;
					qf = c.deleteQueue(queueName);
					if (qf != -1) {
						res = true;
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if(c!=null) c.close();
			}
		}
		return res;
	}
	
	public static boolean createQueueByClient(Map<String, String> params, ResultBean resultBean) {
		//TODO 权限控制
		return saveQueue(params, resultBean) && 
				releaseQueue(params, resultBean);
	}
	
	public static boolean deleteQueueByClient(Map<String, String> params, ResultBean resultBean) {
		//TODO 权限控制
		if (params == null) {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return false;
		}
		
		String queueName = params.get(FixHeader.HEADER_QUEUE_NAME);
		String queueId = MetaData.get().getQueueBeanByName(queueName).getQueueId();
		String servId = MetaData.get().getServiceByQueueId(queueId).getInstID();
		params.put(FixHeader.HEADER_QUEUE_ID, queueId);
		params.put(FixHeader.HEADER_SERV_ID, servId);
		
		return delQueue(params, resultBean);
	}
	
	public static List<PermnentTopicBean> getAllPermnentTopics() {
		List<PermnentTopicBean> list = null;
		try {
			CRUD c = new CRUD();
			List<Object> paramList = new LinkedList<Object>();
			c.putSql(SEL_ALL_PERMNENT_TOPIC, paramList);
			List<HashMap<String, Object>> queryResult = c.queryForList();
			
			if (queryResult != null) {
				list = new ArrayList<PermnentTopicBean>(queryResult.size());
				
				Iterator<HashMap<String, Object>> iter = queryResult.iterator();
				while (iter.hasNext()) {
					HashMap<String, Object> mateMap = iter.next();
					PermnentTopicBean permnentTopicBean = PermnentTopicBean.convert(mateMap);
					list.add(permnentTopicBean);
				}
			}
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
		}
		
		return list;
	}
	
	public static JsonArray getPermnentTopicList(Map<String, String> params, ResultBean resultBean) {
		JsonArray jsonArray = null;
		String sql="select t.consumer_id, t.real_queue, t.main_topic, t.sub_topic, t.queue_id "
				+ "from t_mq_permnent_topic t where t.queue_id = ? ";
		
		if (params!=null && params.size()>0) {
			
			String pageSizeString = params.get(CONSTS.PAGE_SIZE);
			String pageNumString = params.get(CONSTS.PAGE_NUMBER);
			String queueId = params.get(FixHeader.HEADER_QUEUE_ID);
			String consumerId = params.get(FixHeader.HEADER_CONSUMER_ID);
			
			if(HttpUtils.isNull(queueId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
			}
			
			if(HttpUtils.isNotNull(consumerId)) {
				sql += " and t.consumer_id like '%" + consumerId +  "%' ";
			}
			
			if(HttpUtils.isNotNull(pageSizeString)&&HttpUtils.isNotNull(pageNumString)){
				
				int pageSize = Integer.parseInt(pageSizeString);
				int pageNum = Integer.parseInt(pageNumString);
				int start = (pageNum - 1) * pageSize;
				
				sql += " limit "+start+","+pageSize;
			}

			CRUD crud = new CRUD();
			crud.putSql(sql, new Object[] {queueId});
			try {
				jsonArray = crud.queryForJSONArray();
			} catch (CRUDException e) {
				e.printStackTrace();
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(e.getMessage());
			}
			
		}else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo("params is null");
		}

		return jsonArray;
	}

	public static JsonObject getPermnentTopicCount(Map<String, String> params, ResultBean resultBean) {
		JsonObject countJson = new JsonObject();
		String sql="select count(1) from t_mq_permnent_topic q where q.queue_id = ?";
		
		if (params!=null && params.size()>0) {
		
			String queueId = params.get(FixHeader.HEADER_QUEUE_ID);
			String consumerId = params.get(FixHeader.HEADER_CONSUMER_ID);
			
			if(HttpUtils.isNull(queueId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
			}
			
			if(HttpUtils.isNotNull(consumerId)) {
				sql += " and t.consumer_id like '%" + consumerId +  "%' ";
			}
			
			CRUD crud = new CRUD();
			crud.putSql(sql, new Object[] {queueId});
			try {
				int count = crud.queryForCount();
				countJson.put("COUNT", count);
			} catch (CRUDException e) {
				e.printStackTrace();
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(e.getMessage());
			}
			
		}else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo("params is null");
		}

		return countJson;
	}
	
	public static PermnentTopicBean getPermnentTopic(String consumerId) {
		PermnentTopicBean bean = null;
		try {
			SqlBean sqlBean = new SqlBean(SELECT_PERMNENT_TOPIC);
			sqlBean.addParams(new Object[]{consumerId});
			
			CRUD c = new CRUD();
			c.putSqlBean(sqlBean);
			
			List<HashMap<String, Object>> queryResult = c.queryForList();
			if (queryResult != null && !queryResult.isEmpty()) {
				HashMap<String, Object> item = queryResult.get(0);
				bean = PermnentTopicBean.convert(item);
			}
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
		}
		
		return bean;
	}
	
	public static String savePermnentTopic(Map<String, String> params, ResultBean resultBean) {

		if (params != null) {
			String queueId = params.get(FixHeader.HEADER_QUEUE_ID);
			String consumerId = params.get(FixHeader.HEADER_CONSUMER_ID);
			String subtopic = params.get(FixHeader.HEADER_SUB_TOPIC);
			String realqueue = SRandomGenerator.genPermQueue();
			
			if(HttpUtils.isNull(queueId) || HttpUtils.isNull(consumerId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
			}
			
			QueueBean queueBean = MetaData.get().getQueueBeanById(queueId);
			PermnentTopicBean permnentTopicBean = null;
			
			if(queueBean == null) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_QUEUE_NOT_EXISTS);
				return null;
			}
			
			if(queueBean.getQueueType().equals(CONSTS.TYPE_QUEUE)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("not a topic type");
				return null;
			}
			
			if(MetaData.get().isPermnentTopicExistsById(consumerId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_QUEUE_EXISTS);
				return null;
			}

			if(HttpUtils.isNull(subtopic)) {
				subtopic = queueBean.getQueueName();
			}
			
			boolean isCreate = releasePermnentToMQ(realqueue, queueBean.getServiceId(), subtopic, resultBean);
			
			if(isCreate) {
				CRUD curd = new CRUD();
				
				SqlBean iSqlBean = new SqlBean(INSERT_PERMNENT_TOPIC);
				iSqlBean.addParams(new Object[]{
					consumerId, realqueue, queueBean.getQueueName(), subtopic, queueId
				});
				
				curd.putSqlBean(iSqlBean);	
				boolean res = curd.executeUpdate(resultBean);
				if (!res) {
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(resultBean.getRetInfo());
					return null;
				} else {
					resultBean.setRetCode(CONSTS.REVOKE_OK);
					resultBean.setRetInfo("");
					
					permnentTopicBean = new PermnentTopicBean(consumerId, realqueue, queueBean.getQueueName(), subtopic, queueId);
					MetaData.get().savePermnentTopic(consumerId, permnentTopicBean);
					
					JsonObject evJson = new JsonObject();
					evJson.put(FixHeader.HEADER_CONSUMER_ID, consumerId);
					
					EventBean ev = new EventBean(EventType.e12);
					ev.setUuid(MetaData.get().getUUID());
					ev.setJsonStr(evJson.toString());
					EventBusMsg.publishEvent(ev);
					
					return realqueue;
				}
			}else {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("rabbitmq create permnent_topic fail");
				return null;
			}
		} else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo("params is null");
			return null;
		}
	}
	
	public static boolean delPermnentTopic(Map<String, String> params, ResultBean resultBean) {
		boolean res = false;
		if(params != null) {
			String queueId = params.get(FixHeader.HEADER_QUEUE_ID);
			String consumerId = params.get(FixHeader.HEADER_CONSUMER_ID);
			
			if (HttpUtils.isNull(consumerId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return false;
			}
			if (HttpUtils.isNull(queueId)) {
				queueId = MetaData.get().getQueueIdByConsumerId(consumerId);
			}
			
			if(!MetaData.get().isPermnentTopicExistsById(consumerId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("this permnnet_topic is not exist!");
				return false;
			}
			
			PermnentTopicBean permnentTopicBean = MetaData.get().getPermnentTopicById(consumerId);
			QueueBean queueBean = MetaData.get().getQueueBeanById(queueId);
			
			if(permnentTopicBean != null && queueBean != null) {
				//先卸载
				if(queueBean.getDeploy().equals(CONSTS.DEPLOYED)) {
					List<InstanceDtlBean> list = MetaData.get().getMasterBrokersByServId(queueBean.getServiceId());
					res = deleteRabbitQueueByName(permnentTopicBean.getRealQueue(), list);
					if(!res) {
						resultBean.setRetCode(CONSTS.REVOKE_NOK);
						resultBean.setRetInfo("permnent_topic not exist");
						return false;
					}
				}
				
				CRUD curd = new CRUD();
				SqlBean sqlBean = new SqlBean(DEL_PERMNENT_TOPIC);
				sqlBean.addParams(new Object[]{consumerId});
				curd.putSqlBean(sqlBean);
				
				res = curd.executeUpdate(resultBean);
				if (!res) {
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(resultBean.getRetInfo());
				} else {
					resultBean.setRetCode(CONSTS.REVOKE_OK);
					resultBean.setRetInfo("");
					
					MetaData.get().delPermnentTopic(consumerId);
					
					JsonObject evJson = new JsonObject();
					evJson.put(FixHeader.HEADER_CONSUMER_ID, consumerId);
					
					EventBean ev = new EventBean(EventType.e13);
					ev.setUuid(MetaData.get().getUUID());
					ev.setJsonStr(evJson.toString());
					EventBusMsg.publishEvent(ev);
				}
			}else {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("rabbitmq delete queue fail");
			}
		}else {
			resultBean.setRetCode(CONSTS.REVOKE_NOK);
			resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
		}
		return res;
	}
	
	public static boolean copyQueueToVbroker(String servId, InstanceDtlBean instDtl, ResultBean result) {
		List<QueueBean> queueList = MetaData.get().getQueueListByServId(servId);
		if(queueList == null || queueList.isEmpty()) {
			return true;
		}
		boolean isAllOk = true;
		List<String> queues = new ArrayList<>();
		List<Boolean> priorities = new ArrayList<>();

		for(QueueBean queueBean : queueList) {
			if(!isAllOk) {
				break;
			}
			if(queueBean != null) {
				if(CONSTS.NOT_DEPLOYED.equals(queueBean.getDeploy())) {
					continue;
				}
				if(CONSTS.TYPE_QUEUE.equals(queueBean.getQueueType())) {
					String queueName = queueBean.getQueueName();
					queues.add(queueName);
				}
				else {
					List<PermnentTopicBean> permnentTopics = MetaData.get().getPermnentTopicsByQueueId(queueBean.getQueueId());
					if(permnentTopics != null) {
						for(PermnentTopicBean permnenttopicBean : permnentTopics) {
							queues.add(permnenttopicBean.getRealQueue());
						}
					}
				}
				
				priorities.add(queueBean.getPriority().equals(CONSTS.PRIORITY) ? true : false);
			}
		}
		
		String masterBrokerId = instDtl.getAttribute(FixHeader.HEADER_MASTER_ID).getAttrValue();
		InstanceDtlBean broker = MetaData.get().getInstanceDtlBean(masterBrokerId);
		
		isAllOk = releaseQueuesToMQ(queues, priorities, broker, result);
		
		return isAllOk;
	}
	
	/**
	 * @param deleteQueue 是否要删除数据库中队列信息，false把队列部署置为未部署，true则队列删除
	 */
	public static boolean delQueueByServId(String servId, boolean deleteQueue, ResultBean result) {
		boolean isOk  = false;
		if(HttpUtils.isNull(servId)) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return false;
		}
		
		CRUD crud = new CRUD();
		crud.putSql(DEL_PERMNENT_TOPIC_BY_SERVID, new Object[] {servId});
		if (deleteQueue) {
			crud.putSql(DEL_QUEUE_BY_SERVID, new Object[] {servId});

		} else {
			crud.putSql(UPDATE_QUEUE_UNDEPLOYED_BY_SERVID, new Object[] {CONSTS.NOT_DEPLOYED,servId});
		}
		isOk = crud.executeUpdate(result);
		
		return isOk;
	}
	
	
	//get data from MetaData
	public static JsonObject getQueueByName(String queueName, ResultBean result) {
		try {
			QueueBean queue = MetaData.get().getQueueBeanByName(queueName);
			if (queue == null) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo("No queue info found");
				return null;
			}
			return queue.toJsonObject();
		} catch (Exception e) {
			logger.error("getQueueByName caught error: ", e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
	}
	
	public static JsonObject getBrokersByQName(String queueName, ResultBean result) {
		
		try {
			QueueBean queue = MetaData.get().getQueueBeanByName(queueName);
			if (queue == null) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo("No queue info found");
				return null;
			}
			String serviceID = queue.getServiceId();
			ServiceBean service = MetaDataService.getService(serviceID, result);
			
			List<InstanceDtlBean> vbrokerList = new ArrayList<InstanceDtlBean>();
			if (!getVBrokersByServIdOrServiceStub(serviceID, null, vbrokerList, result)) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo("No VBroker found in service...");
				return null;
			}
			
			JsonObject vbGroup = new JsonObject();
			vbGroup.put(FixHeader.HEADER_ID, service.getInstID());
			vbGroup.put(FixHeader.HEADER_NAME, service.getServName());
			JsonArray vbrokerArray = new JsonArray();
			
			for (InstanceDtlBean vbroker : vbrokerList) {	
				JsonObject vbrokerObj = new JsonObject();
				vbrokerObj.put(FixHeader.HEADER_ID, vbroker.getInstID());
				vbrokerObj.put(FixHeader.HEADER_NAME, vbroker.getAttribute("VBROKER_NAME").getAttrValue());
				vbrokerObj.put(FixHeader.HEADER_MASTER_ID, vbroker.getAttribute(FixHeader.HEADER_MASTER_ID).getAttrValue());
				//TODO 
				vbrokerObj.put(FixHeader.HEADER_IS_WRITABLE, true);
				
				Map<String, InstanceDtlBean> brokerList = vbroker.getSubInstances();
				JsonArray brokerArray = new JsonArray();
				for (InstanceDtlBean broker : brokerList.values()) {
					JsonObject brokerObj = new JsonObject();
					brokerObj.put(FixHeader.HEADER_ID, broker.getInstID());
					brokerObj.put(FixHeader.HEADER_NAME, broker.getAttribute(FixHeader.HEADER_BROKER_NAME).getAttrValue());
					brokerObj.put(FixHeader.HEADER_IP, broker.getAttribute(FixHeader.HEADER_IP).getAttrValue());
					brokerObj.put(FixHeader.HEADER_PORT, broker.getAttribute(FixHeader.HEADER_PORT).getAttrValue());
					brokerArray.add(brokerObj);
				}
				
				vbrokerObj.put("BROKERS", brokerArray);
				vbrokerArray.add(vbrokerObj);
			}
			vbGroup.put("VBROKERS", vbrokerArray);
			return vbGroup;
			
		} catch (Exception e) {
			logger.error("getBrokersByQName caught error: ", e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
	}
	
	public static JsonObject getPermTopicAsJson(String consumerId, ResultBean result) {
		PermnentTopicBean topicBean = MetaData.get().getPermnentTopicById(consumerId);
		if (topicBean == null) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("No topic info found");
			return null;
		}
		
		JsonObject topic = topicBean.toJson();
		ServiceBean service = MetaData.get().getServiceByQueueId(topicBean.getQueueId());
		topic.put(FixHeader.HEADER_GROUP_ID, service.getInstID());
		return topic;
	}

	public static boolean checkBrokerRunning(InstanceDtlBean broker) {
		return checkBrokerRunning(broker, false);
	}

	public static boolean checkBrokerRunning(InstanceDtlBean broker, boolean isRetry) {
		if (broker == null)
			return false;

		boolean ret = false;
		SSHExecutor executor = null;
		boolean connected = false;

		try {
			JschUserInfo ui = new JschUserInfo(
					broker.getAttribute(FixHeader.HEADER_OS_USER).getAttrValue(),
					broker.getAttribute(FixHeader.HEADER_OS_PWD).getAttrValue(),
					broker.getAttribute(FixHeader.HEADER_IP).getAttrValue(),
					CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			executor.echo("test");  // 有的机器中间加了跳转和管控防止ssh登录"Last login:xxxxx"串到输出一起显示
			ret = executor.isRabbitRunning(broker.getAttribute(FixHeader.HEADER_PORT).getAttrValue(), "");

		} catch (Exception e) {
			if(isRetry) {
				for(int i =0 ; i < 3 ;i++) {
					if(checkBrokerRunning(broker, false)) {
						ret = true;
						break;
					}
				}
				if(!ret) {
					logger.error(e.getMessage(), e);
				}
			}
		} finally {
			if (connected) {
				executor.close();
			}
		}

		return ret;
	}

	public static void startBroker(String brokerId, String sessionKey, ResultBean result) {
		excuteBroker(brokerId, sessionKey, result, "start.sh");
	}

	public static void stopBroker(String brokerId, String sessionKey, ResultBean result) {
		excuteBroker(brokerId, sessionKey, result, "stop.sh");
	}

	public static void excuteBroker(String brokerId, String sessionKey, ResultBean result, String cmd) {
		InstanceDtlBean broker = MetaData.get().getInstanceDtlBean(brokerId);
		if(broker == null) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(String.format("broker id : %s not found", brokerId));
			return;
		}

		SSHExecutor executor = null;
		boolean connected = false;

		try{
			JschUserInfo ui = new JschUserInfo(broker.getAttribute(FixHeader.HEADER_OS_USER).getAttrValue(),
					broker.getAttribute(FixHeader.HEADER_OS_PWD).getAttrValue(),
					broker.getAttribute(FixHeader.HEADER_IP).getAttrValue(),
					CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			executor.echo("test"); //有的机器中间加了跳转和管控防止ssh登录“last login:xxxxxx”串到输出一起显示

			String port = broker.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
			String rootPath = String.format("$HOME/%s/%s", CONSTS.MQ_DEPLOY_ROOT_PATH, port);
			executor.cd(rootPath, sessionKey);

			if(!executor.isFileExistInCurrPath(cmd, sessionKey)){
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(String.format("broker id : %s may not deployed", brokerId));
				return;
			}

			if("start.sh".equalsIgnoreCase(cmd)){
				if(executor.isRabbitRunning(port, sessionKey)) {
					result.setRetCode(CONSTS.REVOKE_OK);
					String info = String.format("broker id:%s is already running, does not need start !", brokerId);
					result.setRetInfo(info);
				}else {
					executor.execStartShell(sessionKey);

					long beginTS = System.currentTimeMillis();
					long currTS = beginTS;
					long maxTS = 60000L;
					do{
						Thread.sleep(1000L);
						currTS = System.currentTimeMillis();
						if(currTS - beginTS > maxTS) {
							result.setRetCode(CONSTS.REVOKE_NOK);
							result.setRetInfo(String.format("broker id:%s execute start.sh time out", brokerId));
							break;
						}
					}while(!executor.isPortUsed(port, sessionKey));

					if(executor.isPortUsed(port, sessionKey)) {
						result.setRetCode(CONSTS.REVOKE_OK);
					}
				}
			}else if ("stop.sh".equalsIgnoreCase(cmd)){
				executor.execStopShell(sessionKey);
			}
		} catch (Exception e) {
			String errorMess = String.format("excute MQ broker id : %s cmd %s faild ",brokerId, cmd );
			logger.error(errorMess, e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(errorMess);
		} finally {
			if(connected) {
				executor.close();
			}
		}
	}

	public static boolean saveCollectInfo(String servId, ResultBean result) {
		boolean res = true;
		if(!saveVbrokersCollectInfo(servId, result)){
		   res = false;
		   logger.error("save vbroker collect info fail : {}" , result.getRetInfo());
		}
		if(!saveQueuesCollectInfo(servId, result)){
			res = false;
			logger.error("save queue collect info fail : {}" , result.getRetInfo());
		}
		if(!saveQueuesCollectDetailInfo(servId, result)){
			res = false;
			logger.error("save queue collect detail info fail : {}" , result.getRetInfo());
		}
		return res;
	}

	public static boolean saveVbrokersCollectInfo (String servId, ResultBean result) {
		boolean res = false;

		List<InstanceDtlBean> vbrokers = MetaData.get().getVbrokerByServId(servId);
		if(vbrokers == null || vbrokers.size() == 0) {
			return true;
		}
		Map<String, MQVbrokerCollectInfo> vbrokerCollectInfoMap = MonitorData.get().getMqVbrokerCollectInfoMap();
		CRUD crud = new CRUD();
		long currentTime = System.currentTimeMillis();
		for(InstanceDtlBean vbroker : vbrokers) {
			SqlBean sqlBean = new SqlBean(INSERT_VBROKER_COLLECT_INFO);

			String vbrokerId = vbroker.getInstID();
			MQVbrokerCollectInfo collectInfo = vbrokerCollectInfoMap.get(vbrokerId);
			if(collectInfo == null)
				continue;
			sqlBean.addParams(new Object[]{
					vbroker.getInstID(), collectInfo.getProduceRate(), collectInfo.getProduceCounts(),
					collectInfo.getConsumerRate(), collectInfo.getConsumerCounts(), currentTime
			});
			crud.putSqlBean(sqlBean);
		}
		res = crud.executeUpdate(result);
		return res;
	}

	public static boolean saveQueuesCollectInfo (String servId, ResultBean result) {
		boolean res = false;
		List<QueueBean> queueBeans = MetaData.get().getQueueListByServId(servId);
		if(queueBeans == null || queueBeans.size() == 0) {
			return true;
		}
		Map<String, MQQueueCollectInfo> mqQueueCollectInfoMap = MonitorData.get().getMqQueueCollectInfoMap();
		CRUD crud = new CRUD();
		long currentTime = System.currentTimeMillis();
		for(QueueBean queueBean : queueBeans) {
			SqlBean sqlBean = new SqlBean(INSERT_QUEUE_COLLECT_INFO);
			MQQueueCollectInfo collectInfo = mqQueueCollectInfoMap.get(queueBean.getQueueId());
			if(collectInfo == null)
				continue;
			sqlBean.addParams(new Object[]{
					queueBean.getQueueId(), collectInfo.getProduceRate(), collectInfo.getProduceCounts(),
					collectInfo.getConsumerRate(), collectInfo.getConsumerCounts(),currentTime
			});
			crud.putSqlBean(sqlBean);
		}
		res = crud.executeUpdate(result);
		return res;
	}

	public static boolean saveQueuesCollectDetailInfo(String servId, ResultBean result) {
	    boolean res = false;
	    List<QueueBean> queueBeans = MetaData.get().getQueueListByServId(servId);
	    if(queueBeans == null || queueBeans.size() == 0) {
	        return true;
        }

        Map<String, MQQueueCollectInfo> mqQueueCollectInfoMap = MonitorData.get().getMqQueueCollectInfoMap();

        CRUD crud = new CRUD();
        for(QueueBean queueBean : queueBeans) {

            MQQueueCollectInfo collectInfo = mqQueueCollectInfoMap.get(queueBean.getQueueId());

			if(collectInfo == null)
				continue;

            if(CONSTS.TYPE_QUEUE.equalsIgnoreCase(queueBean.getQueueType())) {
                for(MQQueueInfoBean queueInfoBean : collectInfo.getQueueInfoBeanMap().values()) {
					SqlBean sqlBean = new SqlBean(INSERT_QUEUE_COLLECT_DETAIL_INFO);
                    sqlBean.addParams(new Object[]{
                            queueInfoBean.getVbrokerId(), queueBean.getQueueId(), "", queueInfoBean.getProduceRate(),
                            queueInfoBean.getProduceCounts(), queueInfoBean.getConsumerRate(),
                            queueInfoBean.getConsumerCounts(), queueInfoBean.getTimestamp()
                    });
					crud.putSqlBean(sqlBean);
                }
            }else {
                Map<String, MQQueueCollectInfo> topicCollectInfoMap = collectInfo.getTopicInfoBeanMap();
                for(MQQueueCollectInfo topicCoolectInfo : topicCollectInfoMap.values()) {
                    for(MQQueueInfoBean queueInfoBean : topicCoolectInfo.getQueueInfoBeanMap().values()) {
						SqlBean sqlBean = new SqlBean(INSERT_QUEUE_COLLECT_DETAIL_INFO);
                        sqlBean.addParams(new Object[]{
                                queueInfoBean.getVbrokerId(), queueBean.getQueueId(), topicCoolectInfo.getQueueId(), queueInfoBean.getProduceRate(),
                                queueInfoBean.getProduceCounts(), queueInfoBean.getConsumerRate(),
                                queueInfoBean.getConsumerCounts(), queueInfoBean.getTimestamp()
                        });
						crud.putSqlBean(sqlBean);
                    }
                }
            }
        }
        res = crud.executeUpdate(result);

        return res;
    }

	public static JsonArray getVbrokerHisData(String vbrokerId, long startTs, long endTs, ResultBean result) {
        JsonArray jsonArray = null;
        InstanceDtlBean vbroker = MetaData.get().getInstanceDtlBean(vbrokerId);
        if(vbroker == null) {
            return null;
        }

        CRUD crud = new CRUD();
        SqlBean sqlBean = new SqlBean(SEL_VBROKER_MONITOR_COLLECT);
        sqlBean.addParams(new Object[]{
                startTs, endTs, vbrokerId
        });
        crud.putSqlBean(sqlBean);
        try {
            jsonArray = crud.queryForJSONArray();
        } catch (CRUDException e) {
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo(String.format("getVBrokerHisData fail : %s", e.getMessage()));
            logger.error(e.getMessage(), e);
        }

        return jsonArray;
    }

	public static JsonObject getAllVbrokerHisData(String servId, long startTs, long endTs, ResultBean result) {
		JsonObject json = new JsonObject();

		JsonArray jsonArray = null;
		List<InstanceDtlBean> vbrokers = MetaData.get().getVbrokerByServId(servId);
		if(vbrokers == null) {
			return null;
		}

		CRUD crud = null;

		for(InstanceDtlBean vbroker : vbrokers) {
			crud = new CRUD();
			SqlBean sqlBean = new SqlBean(SEL_VBROKER_MONITOR_COLLECT);
			sqlBean.addParams(new Object[]{
					startTs, endTs, vbroker.getInstID()
			});
			crud.putSqlBean(sqlBean);
			try {
				jsonArray = crud.queryForJSONArray();
				json.put(vbroker.getAttribute(FixHeader.HEADER_VBROKER_NAME).getAttrValue() + "," + vbroker.getInstID()
						, jsonArray);
			} catch (CRUDException e) {
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(String.format("getVBrokerHisData fail : %s", e.getMessage()));
				logger.error(e.getMessage(), e);
			}
		}

		return json;
	}

    public static JsonArray getQueueHisData(String queueId, long startTs, long endTs, ResultBean result) {
        JsonArray jsonArray = null;
        QueueBean queue = MetaData.get().getQueueBeanById(queueId);
        if(queue == null) {
            return null;
        }

        CRUD crud = new CRUD();
        SqlBean sqlBean = new SqlBean(SEL_QUEUE_MONITOR_COLLECT);
        sqlBean.addParams(new Object[]{
                startTs, endTs, queueId
        });
        crud.putSqlBean(sqlBean);
        try {
            jsonArray = crud.queryForJSONArray();
        } catch (CRUDException e) {
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo(String.format("getVBrokerHisData fail : %s", e.getMessage()));
            logger.error(e.getMessage(), e);
        }

        return jsonArray;
    }

	public static JsonArray getVbrokerCollectData(String servId) {
		JsonArray jsonArray = new JsonArray();
		List<InstanceDtlBean> vbrokers = MetaData.get().getVbrokerByServId(servId);

		for(InstanceDtlBean vbroker : vbrokers) {
			MQVbrokerCollectInfo collectInfo = MonitorData.get().getMqVbrokerCollectInfoMap().get(vbroker.getInstID());

			if(collectInfo == null)
				return null;

			JsonObject subJson = new JsonObject()
					.put(FixHeader.HEADER_VBROKER_ID, vbroker.getInstID())
					.put(FixHeader.HEADER_VBROKER_NAME, vbroker.getAttribute(FixHeader.HEADER_VBROKER_NAME).getAttrValue())
					.put(FixHeader.HEADER_PRODUCE_RATE, collectInfo.getProduceRate())
					.put(FixHeader.HEADER_PRODUCE_COUNTS, collectInfo.getProduceCounts())
					.put(FixHeader.HEADER_CONSUMER_RATE, collectInfo.getConsumerRate())
					.put(FixHeader.HEADER_CONSUMER_COUNTS, collectInfo.getConsumerCounts());

			jsonArray.add(subJson);
		}

		return jsonArray;
	}

	public static JsonArray getQueueCollectData(String servId) {
		JsonArray jsonArray = new JsonArray();
		List<QueueBean> queues= MetaData.get().getQueueListByServId(servId);

		if(queues == null)
			return jsonArray;

		for(QueueBean queue : queues) {
			MQQueueCollectInfo collectInfo = MonitorData.get().getMqQueueCollectInfoMap().get(queue.getQueueId());

			if(collectInfo == null)
				return null;

			JsonObject subJson = new JsonObject()
					.put(FixHeader.HEADER_QUEUE_ID, queue.getQueueId())
					.put(FixHeader.HEADER_QUEUE_NAME, queue.getQueueName())
					.put(FixHeader.HEADER_PRODUCE_RATE, collectInfo.getProduceRate())
					.put(FixHeader.HEADER_PRODUCE_COUNTS, collectInfo.getProduceCounts())
					.put(FixHeader.HEADER_CONSUMER_RATE, collectInfo.getConsumerRate())
					.put(FixHeader.HEADER_CONSUMER_COUNTS, collectInfo.getConsumerCounts());

			jsonArray.add(subJson);
		}

		return jsonArray;
	}
}
