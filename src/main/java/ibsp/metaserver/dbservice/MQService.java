package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.QueueBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.exception.CRUDException;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.ServiceData;
import ibsp.metaserver.rabbitmq.IMQClient;
import ibsp.metaserver.rabbitmq.MQClientImpl;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
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
		
		return true;
	}

	public static List<QueueBean> getAllQueues() {
		String sql = "SELECT q.QUEUE_ID, q.QUEUE_NAME, q.IS_DURABLE, q.IS_ORDERED, q.QUEUE_TYPE, q.IS_DEPLOY, q.SERV_ID, t.SERV_NAME "
				+     "FROM t_mq_queue q left join t_service t "
				+     "on q.SERV_ID = t.INST_ID";
		
		List<QueueBean> list = null;
		try {
			CRUD c = new CRUD();
			List<Object> paramList = new LinkedList<Object>();
			c.putSql(sql, paramList);
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
	
	public static JsonArray getQueueList (Map<String,String> params, ResultBean resultBean){
		JsonArray jsonArray = null;
		String sql="select q.queue_id, q.queue_name, queue_type, q.is_durable, q.queue_type, "
				+ "q.is_durable, q.is_ordered, q.is_deploy "
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
			
			if(HttpUtils.isNull(servId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
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
			String _servid = params.get(FixHeader.HEADER_SERVICE_ID);
			
			String ordered = params.get(FixHeader.HEADER_GLOBAL_ORDERED);
			String _ordered = HttpUtils.isNull(ordered) ? CONSTS.NOT_GLOBAL_ORDERED : ordered;
			
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
			
			String qid = "";
			QueueBean queueBean = null;
			
			if (HttpUtils.isNull(_qid)) {
				boolean allreadyExist = ServiceData.get().isQueueNameExistsByName(_qname);
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
				String vbSql = "insert into t_mq_queue(queue_id,queue_name,queue_type,is_durable,is_ordered,serv_id,rec_time)value(?,?,?,?,?,?,?)";
				SqlBean iSqlBean = new SqlBean(vbSql);
				iSqlBean.addParams(new Object[]{qid,_qname,_qtype,_durable,_ordered,_servid,HttpUtils.getCurrTimestamp()});
				curd.putSqlBean(iSqlBean);	
			}
		
			res = curd.executeUpdate(resultBean);
			if (!res) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo(resultBean.getRetInfo());
			} else {
				resultBean.setRetCode(CONSTS.REVOKE_OK);
				resultBean.setRetInfo("");
				//TODO 保存到集群中
				queueBean = new QueueBean(qid, _qname, _durable, _ordered,
						_qtype, CONSTS.NOT_DEPLOYED, _servid, MetaData.get().getServiceName(_servid));
				ServiceData.get().saveQueue(qid, queueBean);
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
			
			if(!ServiceData.get().isQueueNameExistsById(queueId)) {
				resultBean.setRetCode(CONSTS.REVOKE_NOK);
				resultBean.setRetInfo("this queue is not exist!");
				return false;
			}
			
			QueueBean queueBean = ServiceData.get().getQueueBeanById(queueId);
			if(queueBean != null) {
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
					//TODO 保存修改到集群中
					ServiceData.get().delQueue(queueId);
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
	
	public static boolean releaseQueue(Map<String, String> params, ResultBean resultBean) {
		boolean res = false;
		if(params != null) {
			
			String queueName = params.get(FixHeader.HEADER_QUEUE_NAME);
			String servId = params.get(FixHeader.HEADER_SERV_ID);
			if (HttpUtils.isNotNull(queueName) && HttpUtils.isNotNull(servId)) {
				
				QueueBean queueBean = ServiceData.get().getQueueBeanByName(queueName);
				if(queueBean != null) {
					if(CONSTS.NOT_DEPLOYED.equals(queueBean.getDeploy())) {
						res = releaseQueueToMQ(queueBean, servId, resultBean);
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
	
	private static boolean releaseQueueToMQ(QueueBean queueBean, String servId, ResultBean resultBean) {
		boolean createOk = false;
		List<InstanceDtlBean> list = MetaData.get().getMasterBrokersByServId(servId);
		
		if (list!=null&&list.size()>0) {
			
			String queueName = queueBean.getQueueName();
			String queueType = queueBean.getQueueType();//相对C的实现原理,Topic的区分在于消费端,服务端创建Queue和Topic一样
			
			String ip = "";
			String user = "";
			String pwd ="";
			String brokerId="";
			String vhost ="";
			
			List<InstanceDtlBean> succList = new ArrayList<>();
			
			for (InstanceDtlBean brokerBean : list) {
				if (createOk)
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
					if (cf == 0) {
						int createRet = -1;
						if (HttpUtils.isNotNull(queueType)&&queueType.equals("2")) {
							createRet = c.createTopic(queueName, false, true);
						} else {
							createRet = c.createQueue(queueName, false, true);
						}
						
						if (createRet != 0) {
							String err = String.format("release queue:%s error, user:%s pwd:%s vhost:%s %s:%d",
									queueName, user, pwd, vhost, ip, port);
							logger.error(err);
							
							createOk = false;
							break;
						} else {
							succList.add(brokerBean);
							createOk = true;
						}
					} else {
						String err = String.format("create queue on borker:[BrokerId:%s, IP:%s, port:%d] fail.", brokerId, ip, port);
						resultBean.setRetCode(CONSTS.REVOKE_NOK);
						resultBean.setRetInfo(err);
						createOk = false;
						break;
					}
				} catch (Exception e) {
					createOk = false;
					
					resultBean.setRetCode(CONSTS.REVOKE_NOK);
					resultBean.setRetInfo(e.getMessage());
					logger.error(e.getMessage(), e);
					
					break;
				} finally {
					if(cf==0)
						c.close();
				}
			}
			
			if (createOk) {
				CRUD crud = new CRUD();
				String uSql = "update t_mq_queue set is_deploy=? where queue_id=?";
				crud.putSql(uSql, new Object[]{CONSTS.DEPLOYED,queueBean.getQueueId()});
				
				//TODO 往mo_queue 和 mo_queue_accu_dtl表插入数据
				createOk = crud.executeUpdate(resultBean);
				if(createOk) {
					queueBean.setDeploy(CONSTS.DEPLOYED);
					ServiceData.get().saveQueue(queueBean.getQueueId(), queueBean);
				}else {
					deleteRabbitQueue(queueBean, succList);
				}
			} else {
				deleteRabbitQueue(queueBean, succList);
			}
		}
		
		return createOk;
	}
	
	private static boolean deleteRabbitQueue(QueueBean queueBean, List<InstanceDtlBean> list) {
		if (list == null)
			return false;
		boolean res = false;
		String queueName = queueBean.getQueueName();
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
					if(queueBean.getQueueType().equals(CONSTS.TYPE_TOPIC)) {
						qf = c.deleteTopic(queueName);
					} else {
						qf = c.deleteQueue(queueName);
					}
					if (qf != -1) {
						res = true;
					}
				}
				c.deleteQueue(queueName);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if(c!=null) c.close();
			}
		}
		return res;
	}
}
