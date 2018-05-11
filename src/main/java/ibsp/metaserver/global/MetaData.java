package ibsp.metaserver.global;

import ibsp.metaserver.bean.CollectQuotaBean;
import ibsp.metaserver.bean.DeployFileBean;
import ibsp.metaserver.bean.IdSetBean;
import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.MetaAttributeBean;
import ibsp.metaserver.bean.MetaComponentBean;
import ibsp.metaserver.bean.MetaServUrl;
import ibsp.metaserver.bean.PermnentTopicBean;
import ibsp.metaserver.bean.QueueBean;
import ibsp.metaserver.bean.RelationBean;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.bean.TopologyBean;
import ibsp.metaserver.dbservice.MQService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.SysConfig;
import ibsp.metaserver.utils.Topology;
import ibsp.metaserver.utils.UUIDUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class MetaData {
	
	private static Logger logger = LoggerFactory.getLogger(MetaData.class);
	
	private static String ID_INDEX = "_ID";
	
	private String uuid;
	
	private Map<Integer, MetaAttributeBean> metaAttrMap;
	private Map<Integer, MetaComponentBean> metaCmptMap;
	private Map<String,  Integer> metaCmptName2IDMap;
	private Map<Integer, IdSetBean<Integer>> metaCmpt2AttrMap;
	private Map<String, DeployFileBean> deployFileMap;
	private Map<String, ServiceBean> serviceMap;
	private Map<String, InstanceDtlBean> instanceDtlMap;
	private Map<String, Integer> quotaName2Code;
	private Map<Integer, String> quotaCode2Name;
	private Topology topo;
	
	private Map<Integer, MetaServUrl> metaServMap;
	private String metaServUrls;
	
	private Map<String, IdSetBean<String>> servId2QueueIdMap;
	private Map<String, QueueBean> queueMap;
	private Map<String, String> queueName2IdMap;
	private Map<String, PermnentTopicBean> permTopicMap;
	private Map<String, IdSetBean<String>> queueId2ConsumerIdMap;
	
	private JedisPool jedisPool;
	
	private static MetaData theInstance = null;
	private static ReentrantLock intanceLock = null;
	
	static {
		intanceLock = new ReentrantLock();
	}
	
	public MetaData() {
		uuid               = UUIDUtils.genUUID();
		
		metaAttrMap        = new ConcurrentHashMap<Integer, MetaAttributeBean>();
		metaCmptMap        = new ConcurrentHashMap<Integer, MetaComponentBean>();
		metaCmptName2IDMap = new ConcurrentHashMap<String,  Integer>();
		metaCmpt2AttrMap   = new ConcurrentHashMap<Integer, IdSetBean<Integer>>();
		deployFileMap      = new ConcurrentHashMap<String,  DeployFileBean>();
		serviceMap         = new ConcurrentHashMap<String,  ServiceBean>();
		instanceDtlMap     = new ConcurrentHashMap<String,  InstanceDtlBean>();
		quotaName2Code     = new ConcurrentHashMap<String,  Integer>();
		quotaCode2Name     = new ConcurrentHashMap<Integer, String>();
		topo               = new Topology();
		
		metaServMap        = new ConcurrentHashMap<Integer, MetaServUrl>();
		metaServUrls       = "";
		
		servId2QueueIdMap      = new ConcurrentHashMap<String, IdSetBean<String>>();
		queueMap               = new ConcurrentHashMap<String, QueueBean>();
		queueName2IdMap        = new ConcurrentHashMap<String, String>();
		permTopicMap           = new ConcurrentHashMap<String, PermnentTopicBean>();
		queueId2ConsumerIdMap  = new ConcurrentHashMap<String, IdSetBean<String>>();
	}
	
	public static MetaData get() {
		try {
			intanceLock.lock();
			if (theInstance != null){
				return theInstance;
			} else {
				theInstance = new MetaData();
				theInstance.init();
			}
		} finally {
			intanceLock.unlock();
		}
		
		return theInstance;
	}
	
	private void init() {
		initData();
		initJedisPool();
	}
	
	private void initData() {
		LoadMetaAttr();
		LoadMetaCmpt();
		LoadMetaCmpt2Attr();
		LoadDeployFile();
		LoadServices();
		LoadInstances();
		LoadCollectQuota();
		LoadTopo();
		LoadMetaServUrl();
		LoadQueue();
		LoadPermnentTopic();
	}
	
	public void reloadMetaData() {
		initData();
	}
	
	private void initJedisPool() {
		JedisPoolConfig jedisConfig = new JedisPoolConfig();
		jedisConfig.setMaxIdle(SysConfig.get().getRedisPoolSize());
		jedisConfig.setMaxWaitMillis(10000);
		jedisConfig.setTestOnBorrow(true);
		
		jedisPool = new JedisPool(jedisConfig, SysConfig.get().getRedisHost(), SysConfig.get().getRedisPort(), 3000, SysConfig.get().getRedisAuth());
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public Jedis getJedis() {
		Jedis jedis = null;
		try {
			intanceLock.lock();
			
			jedis = jedisPool.getResource();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		return jedis;
	}
	
	private void LoadCollectQuota() {
		try {
			intanceLock.lock();
			
			List<CollectQuotaBean> list = MetaDataService.getAllCollectQuotas();
			if (list == null || list.isEmpty())
				return;
			
			quotaName2Code.clear();
			quotaCode2Name.clear();
			
			for (CollectQuotaBean quota : list) {
				if (quota == null)
					continue;
				
				quotaName2Code.put(quota.getQuotaName(), quota.getQuotaCode());
				quotaCode2Name.put(quota.getQuotaCode(), quota.getQuotaName());
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadServices() {
		try {
			intanceLock.lock();
			
			List<ServiceBean> list = MetaDataService.getAllServices();
			if (list == null || list.isEmpty())
				return;
			
			serviceMap.clear();
			
			for (ServiceBean service : list) {
				if (service == null)
					continue;
				
				serviceMap.put(service.getInstID(), service);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadInstances() {
		try {
			intanceLock.lock();
			
			List<InstanceBean> instances = MetaDataService.getAllInstance();
			List<InstAttributeBean> instAttrs = MetaDataService.getAllInstanceAttribute();
			
			if (instances == null || instances.isEmpty())
				return;
			
			if (instAttrs == null || instAttrs.isEmpty())
				return;
			
			instanceDtlMap.clear();
			
			for (InstanceBean instance : instances) {
				if (instance == null)
					continue;
				
				InstanceDtlBean instDtl = new InstanceDtlBean(instance);
				instanceDtlMap.put(instDtl.getInstID(), instDtl);
			}
			
			for (InstAttributeBean instAttr : instAttrs) {
				if (instAttr == null)
					continue;
				
				String instID = instAttr.getInstID();
				InstanceDtlBean instDtl = instanceDtlMap.get(instID);
				if (instDtl == null) {
					String err = String.format("instID:%s not found!", instID);
					logger.error(err);
					continue;
				}
				
				instDtl.addAttribute(instAttr);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadQueue() {
		try {
			intanceLock.lock();
			
			List<QueueBean> queueList = MQService.getAllQueues();
			if (queueList == null) {
				logger.info("LoadQueue: no data loaded ......");
				return;
			}
			
			Iterator<QueueBean> iter = queueList.iterator();
			while (iter.hasNext()) {
				QueueBean queue = iter.next();
				queueMap.put(queue.getQueueId(), queue);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		
		genQueueName2IdMap();
		genServId2QueueIdMap();
	}
	
	private void LoadPermnentTopic() {
		try {
			intanceLock.lock();
			
			List<PermnentTopicBean> permnentTopicList = MQService.getAllPermnentTopics();
			if (permnentTopicList == null) {
				logger.info("LoadPermnentTopic: no data loaded ......");
				return;
			}
			
			Iterator<PermnentTopicBean> iter = permnentTopicList.iterator();
			while (iter.hasNext()) {
				PermnentTopicBean bean = iter.next();
				permTopicMap.put(bean.getConsumerId(), bean);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		
		genQueueId2ConsumerIdMap();
	}
	
	private void genQueueId2ConsumerIdMap() {
		if (permTopicMap == null)
			return;
		
		queueId2ConsumerIdMap.clear();
		
		Set<Entry<String, PermnentTopicBean>> entrySet = permTopicMap.entrySet();
		for (Entry<String, PermnentTopicBean> entry : entrySet) {
			PermnentTopicBean bean = entry.getValue();
			if (bean == null)
				continue;
			
			IdSetBean<String> idSet = queueId2ConsumerIdMap.get(bean.getQueueId());
			
			if (idSet == null) {
				idSet = new IdSetBean<String>();
				idSet.addId(bean.getConsumerId());
				
				queueId2ConsumerIdMap.put(bean.getQueueId(), idSet);
			} else {
				idSet.addId(bean.getConsumerId());
			}
		}
	}
	
	private void LoadMetaServUrl() {
		try {
			intanceLock.lock();
			
			List<MetaServUrl> list = MetaDataService.getAllMetaServUrl();
			if (list == null || list.isEmpty())
				return;
			
			metaServMap.clear();
			metaServUrls = "";
			StringBuilder sb = new StringBuilder();
			int cnt = 0;
			
			for (MetaServUrl metaServUrl : list) {
				if (metaServUrl == null)
					continue;
				
				metaServMap.put(metaServUrl.getMetaSvrID(), metaServUrl);
				
				if (cnt++ > 0) sb.append(CONSTS.PATH_COMMA);
				sb.append(metaServUrl.getHttpServAddr());
			}
			
			metaServUrls = sb.toString();
		
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadTopo() {
		try {
			intanceLock.lock();
			
			List<TopologyBean> list = MetaDataService.getAllTopology();
			if (list == null || list.isEmpty())
				return;
			
			topo.clear();
			
			for (TopologyBean topoBean : list) {
				if (topoBean == null)
					continue;
				
				topo.put(topoBean.getInstID1(), topoBean.getInstID2(), topoBean.getTopoType());
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadMetaAttr() {
		try {
			intanceLock.lock();
			
			List<MetaAttributeBean> metaAttrList = MetaDataService.getAllMetaAttribute();
			if (metaAttrList == null) {
				return;
			}
			
			metaAttrMap.clear();
			
			for (MetaAttributeBean metaAttr : metaAttrList) {
				if (metaAttr == null)
					continue;
				
				metaAttrMap.put(metaAttr.getAttrID(), metaAttr);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadMetaCmpt() {
		try {
			intanceLock.lock();
			
			List<MetaComponentBean> metaCmptList = MetaDataService.getAllMetaComponent();
			if (metaCmptList == null) {
				return;
			}
			
			metaCmptMap.clear();
			metaCmptName2IDMap.clear();
			
			for (MetaComponentBean metaCmpt : metaCmptList) {
				if (metaCmpt == null)
					continue;
				
				metaCmptMap.put(metaCmpt.getCmptID(), metaCmpt);
				metaCmptName2IDMap.put(metaCmpt.getCmptName(), metaCmpt.getCmptID());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadMetaCmpt2Attr() {
		try {
			intanceLock.lock();
			
			List<RelationBean> cmpt2AttrList = MetaDataService.getAllCmpt2Attr();
			if (cmpt2AttrList == null) {
				return;
			}
			
			metaCmpt2AttrMap.clear();
			
			for (RelationBean relationBean : cmpt2AttrList) {
				if (relationBean == null)
					continue;
				
				Integer masterID = (Integer) relationBean.getMasterID();
				Integer slaveID = (Integer) relationBean.getSlaveID();
				
				IdSetBean<Integer> idSet = metaCmpt2AttrMap.get(masterID);
				if (idSet == null) {
					idSet = new IdSetBean<Integer>();
					idSet.addId(slaveID);
					
					metaCmpt2AttrMap.put(masterID, idSet);
				} else {
					idSet.addId(slaveID);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadDeployFile() {
		try {
			intanceLock.lock();
			
			List<DeployFileBean> deployFileList = MetaDataService.loadDeployFile();
			if (deployFileList == null) {
				return;
			}
			
			deployFileMap.clear();
			
			for (DeployFileBean deployFile : deployFileList) {
				deployFileMap.put(deployFile.getFileType(), deployFile);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	public boolean doTopo(JsonObject json, EventType type) {
		boolean res = true;
		
		if (topo == null)
			return false;

		String instID1 = json.getString("INST_ID1");
		String instID2 = json.getString("INST_ID2");
		Integer topoType = json.getInteger("TOPO_TYPE");

		if (HttpUtils.isNull(instID1) || HttpUtils.isNull(instID2)
				|| topoType == null)
			return false;

		switch (type) {
		case e1:
			topo.put(instID1, instID2, topoType);
			break;
		case e2:
			topo.remove(instID1, instID2, topoType);
			break;
		default:
			res = false;
			break;
		}

		return res;
	}
	
	public boolean doInstance(JsonObject json, EventType type) {
		boolean res = true;
		
		if (instanceDtlMap == null)
			return false;
		
		String instID = json.getString("INST_ID");
		if (HttpUtils.isNull(instID))
			return false;
		
		switch (type) {
		case e3:
		case e4:
			InstanceDtlBean instDtl = MetaDataService.getInstanceDtlFromDB(instID);
			if (instDtl != null) {
				instanceDtlMap.put(instID, instDtl);
			}
			break;
		case e5:
			instanceDtlMap.remove(instID);
			break;
		default:
			res = false;
			break;
		}
		
		return res;
	}
	
	public boolean doService(JsonObject json, EventType type) {
		boolean res = true;
		
		if (serviceMap == null)
			return false;
		
		String instID = json.getString("INST_ID");
		if (HttpUtils.isNull(instID))
			return false;
		
		switch (type) {
		case e6:
		case e7:
			ServiceBean service = MetaDataService.getServiceFromDB(instID);
			if (service != null) {
				serviceMap.put(instID, service);
			}
			break;
		case e8:
			serviceMap.remove(instID);
			break;
		default:
			res = false;
			break;
		}
		
		return res;
	}
	
	public boolean doQueue(JsonObject json, EventType type) {
		if (queueMap == null || queueName2IdMap == null)
			return false;
		
		String queueID = json.getString("QUEUE_ID");
		if (HttpUtils.isNull(queueID))
			return false;
		
		boolean res = false;
		
		switch (type) {
		case e9:
		case e10:
			QueueBean queueBean = MQService.getQueue(queueID);
			if (queueBean != null) {
				saveQueue(queueID, queueBean);
			} else {
				res = false;
			}
			break;
		case e11:
			res = delQueue(queueID);
			break;
		default:
			break;
		}
		
		return res;
	}
	
	public boolean doServiceDeploy(JsonObject json, EventType type) {
		if (serviceMap == null)
			return false;

		String instID = json.getString(FixHeader.HEADER_INSTANCE_ID);
		if (HttpUtils.isNull(instID))
			return false;

		switch (type) {
		case e21:
			serviceMap.get(instID).setDeployed(CONSTS.DEPLOYED);
			break;
		case e22:
			serviceMap.get(instID).setDeployed(CONSTS.NOT_DEPLOYED);
			break;
		default:
			return false;
		}
		return true;
	}
	
	public boolean doInstanceDeploy(JsonObject json, EventType type) {
		if (this.instanceDtlMap == null)
			return false;

		String instID = json.getString(FixHeader.HEADER_INSTANCE_ID);
		if (HttpUtils.isNull(instID))
			return false;

		switch (type) {
		case e23:
			instanceDtlMap.get(instID).getInstance().setIsDeployed(CONSTS.DEPLOYED);
			break;
		case e24:
			instanceDtlMap.get(instID).getInstance().setIsDeployed(CONSTS.NOT_DEPLOYED);
			break;
		default:
			return false;
		}
		return true;
	}
	
	private void genQueueName2IdMap() {
		if (queueMap == null)
			return;
		
		queueName2IdMap.clear();
		
		Set<Entry<String, QueueBean>> queueEntrySet = queueMap.entrySet();
		for (Entry<String, QueueBean> queueEntry : queueEntrySet) {
			QueueBean queueBean = queueEntry.getValue();
			if (queueBean == null)
				continue;
			
			queueName2IdMap.put(queueBean.getQueueName(), queueBean.getQueueId());
		}
	}
	
	private void genServId2QueueIdMap() {
		if (queueMap == null)
			return;
		
		servId2QueueIdMap.clear();
		
		Set<Entry<String, QueueBean>> queueEntrySet = queueMap.entrySet();
		for (Entry<String, QueueBean> queueEntry : queueEntrySet) {
			QueueBean queueBean = queueEntry.getValue();
			if (queueBean == null)
				continue;
			
			IdSetBean<String> idSet = servId2QueueIdMap.get(queueBean.getServiceId());
			if(idSet == null) {
				idSet = new IdSetBean<>();
				idSet.addId(queueBean.getQueueId());
				servId2QueueIdMap.put(queueBean.getServiceId(), idSet);
			}else {
				idSet.addId(queueBean.getQueueId());
			}
		}
	}
	
	public ServiceBean getServiceByQueueId(String queueId) {
		for (Entry<String, IdSetBean<String>> entry : servId2QueueIdMap.entrySet()) {
			if (entry.getValue().contains(queueId)) {
				return this.serviceMap.get(entry.getKey());
			}
		}
		return null;
	}
	
	public boolean isQueueNameExistsByName(String queueName) {
		if (queueName2IdMap == null)
			return false;
		
		return queueName2IdMap.containsKey(queueName);
	}
	
	public boolean isQueueNameExistsById(String queueId) {
		if (queueName2IdMap == null)
			return false;
		
		return queueMap.containsKey(queueId);
	}
	
	public boolean saveQueue(String queueId, QueueBean queueBean) {
		if (queueMap == null)
			return false;
		
		try {
			intanceLock.lock();
			
			QueueBean oldQueue = queueMap.get(queueId);
			queueMap.put(queueId, queueBean);
			
			if(oldQueue != null) {
				queueName2IdMap.remove(oldQueue.getQueueName());
			}
			
			queueName2IdMap.put(queueBean.getQueueName(), queueId);
			
			IdSetBean<String> idSet = servId2QueueIdMap.get(queueBean.getServiceId());
			if(idSet == null) {
				idSet = new IdSetBean<>();
				idSet.addId(queueId);
				servId2QueueIdMap.put(queueBean.getServiceId(), idSet);
			}else {
				idSet.addId(queueId);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		
		return false;
	}
	
	public boolean delQueue(String queueId) {
		if (queueMap == null)
			return false;
		
		QueueBean qb = queueMap.get(queueId);
		if (qb == null)
			return false;
		
		String servId = qb.getServiceId();
		
		try {
			intanceLock.lock();
			
			queueName2IdMap.remove(qb.getQueueName());
			queueMap.remove(queueId);
			
			IdSetBean<String> idSet = servId2QueueIdMap.get(servId);
			if(idSet != null) {
				idSet.removeId(queueId);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		
		return true;
	}
	
	public List<QueueBean> getQueueListByServId (String servId){
		if(servId2QueueIdMap == null) {
			return null;
		}
		IdSetBean<String> idSet = servId2QueueIdMap.get(servId);
		if(idSet == null || idSet.isEmpty()) {
			return null;
		}
		List<QueueBean> list = new ArrayList<>();
		Iterator<String> it = idSet.iterator();
		while (it.hasNext()) {
			String queueId = it.next();
			QueueBean queueBean = getQueueBeanById(queueId);
			if(queueBean != null)
				list.add(queueBean);
		}
		return list;
	}
	
	public QueueBean getQueueBeanByName(String queueName) {
		if (queueMap == null || queueName2IdMap == null)
			return null;
		
		String queueId = queueName2IdMap.get(queueName);
		if (HttpUtils.isNull(queueId))
			return null;
		
		return queueMap.get(queueId);
	}
	
	public QueueBean getQueueBeanById(String queueId) {
		if (queueMap == null)
			return null;
		
		return queueMap.get(queueId);
	}
	
	public List<PermnentTopicBean> getPermnentTopicsByQueueId(String queueId){
		if(queueId2ConsumerIdMap == null) {
			return null;
		}
		List<PermnentTopicBean> list = new ArrayList<>();

		IdSetBean<String> idset = queueId2ConsumerIdMap.get(queueId);
		Iterator<String> it = idset.iterator();
		while(it.hasNext()) {
			String consumerId = it.next();
			PermnentTopicBean bean = permTopicMap.get(consumerId);
			if(bean != null) {
				list.add(bean);
			}
		}
		return list;
	}
	
	public PermnentTopicBean getPermnentTopicById(String consumerId) {
		if(permTopicMap == null)
			return null;
		
		return permTopicMap.get(consumerId);
	}
	
	public String getQueueIdByConsumerId(String comsumerId) {
		if (queueId2ConsumerIdMap == null)
			return null;
		Set<Entry<String, IdSetBean<String>>> entrySet = queueId2ConsumerIdMap.entrySet();
		for (Entry<String, IdSetBean<String>> entry : entrySet) {
			if (entry.getValue().contains(comsumerId))
				return entry.getKey();
		}
		return null;
	}
	
	public boolean isPermnentTopicExistsById(String consumerId) {
		if (permTopicMap == null)
			return false;
		
		return permTopicMap.containsKey(consumerId);
	}
	
	public boolean hasPermnentTopicByQueueId(String queueId) {
		if (queueId2ConsumerIdMap == null)
			return false;
		IdSetBean<String> idset = queueId2ConsumerIdMap.get(queueId);
		if(idset == null || idset.isEmpty()) {
			return false;
		}
		return true;
	}
	
	public boolean savePermnentTopic(String consumerId, PermnentTopicBean bean) {
		if (permTopicMap == null)
			return false;
		
		try {
			intanceLock.lock();
			permTopicMap.put(consumerId, bean);
			
			IdSetBean<String> idSet = queueId2ConsumerIdMap.get(bean.getQueueId());
			if(idSet == null) {
				idSet = new IdSetBean<>();
				idSet.addId(consumerId);
				queueId2ConsumerIdMap.put(bean.getQueueId(), idSet);
			}else {
				idSet.addId(consumerId);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		
		return false;
	}
	
	public boolean delPermnentTopic(String consumerId) {
		if (permTopicMap == null)
			return false;
		
		PermnentTopicBean bean = permTopicMap.get(consumerId);
		if (bean == null)
			return false;
		
		try {
			intanceLock.lock();
			IdSetBean<String> idSet = queueId2ConsumerIdMap.get(bean.getQueueId());
			idSet.removeId(consumerId);
			permTopicMap.remove(consumerId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		
		return true;
	}
	
	public boolean doPermnentTopic(JsonObject json, EventType type) {
		if (permTopicMap == null || queueId2ConsumerIdMap == null)
			return false;
		
		String consumerId = json.getString(FixHeader.HEADER_CONSUMER_ID);
		if (HttpUtils.isNull(consumerId))
			return false;
		
		boolean res = false;
		
		switch (type) {
		case e12:
			PermnentTopicBean bean = MQService.getPermnentTopic(consumerId);
			if (bean != null) {
				savePermnentTopic(consumerId, bean);
			} else {
				res = false;
			}
			break;
		case e13:
			res = delPermnentTopic(consumerId);
			break;
		default:
			break;
		}
		
		return res;
	}
	
	public Map<String, ServiceBean> getServiceMap() {
		return serviceMap;
	}
	
	public ServiceBean getService(String servID) {
		return serviceMap.get(servID);
	}
	
	public String getServiceName(String servId) {
		if(HttpUtils.isNotNull(servId)) {
			ServiceBean service = serviceMap.get(servId);
			if(service != null) {
				return service.getServName();
			}
		}
		return "";
	}
	
	public ServiceBean getServiceByID(String servID) {
		if (serviceMap == null)
			return null;
		
		return serviceMap.get(servID);
	}
	
	public ServiceBean getServiceByName(String servName) {
		if (serviceMap == null)
			return null;
		
		for (ServiceBean servBean : serviceMap.values()) {
			if (servBean.getServName().equals(servName)) {
				return servBean;
			}
		}
		
		return null;
	}
	
	public String getServiceCollectdID(String servID) {
		ServiceBean servBean = serviceMap.get(servID);
		if (servBean == null)
			return null;
		
		String servType = servBean.getServType();
		String collectdCmptName = String.format("%s_COLLECTD", servType);
		Set<String> subs = topo.get(servID, CONSTS.TOPO_TYPE_CONTAIN);
		if (subs == null)
			return null;
		
		String collectdID = null;
		
		for (String subID : subs) {
			InstanceDtlBean instDtlBean = instanceDtlMap.get(subID);
			if (instDtlBean == null)
				continue;
			
			InstanceBean instBean = instDtlBean.getInstance();
			int cmptID = instBean.getCmptID();
			MetaComponentBean component = metaCmptMap.get(cmptID);
			if (component.getCmptName().equals(collectdCmptName)) {
				collectdID = instBean.getInstID();
				break;
			}
		}
		
		return collectdID;
	}
	
	public InstanceDtlBean getInstanceDtlBean(String instID) {
		if (instanceDtlMap == null)
			return null;
		
		return instanceDtlMap.get(instID);
	}
	
	public DeployFileBean getDeployFile(String type) {
		if (deployFileMap == null)
			return null;
		
		return deployFileMap.get(type);
	}
	
	public MetaComponentBean getComponentByName(String cmptName) {
		Integer cmptID = metaCmptName2IDMap.get(cmptName);
		if (cmptID == null)
			return null;
		
		return metaCmptMap.get(cmptID);
	}
	
	public MetaComponentBean getComponentByID(int cmptID) {
		return metaCmptMap.get(cmptID);
	}
	
	public List<MetaAttributeBean> getCmptAttrByName(String cmptName) {
		Integer cmptID = metaCmptName2IDMap.get(cmptName);
		if (cmptID == null)
			return null;
		
		IdSetBean<Integer> idSet = metaCmpt2AttrMap.get(cmptID);
		if (idSet == null)
			return null;
		
		List<MetaAttributeBean> attrs = new LinkedList<MetaAttributeBean>();
		Iterator<Integer> it = idSet.iterator();
		while (it.hasNext()) {
			Integer attrID = it.next();
			MetaAttributeBean attr = metaAttrMap.get(attrID);
			attrs.add(attr);
		}
		
		return attrs;
	}
	
	public List<MetaAttributeBean> getCmptAttrByID(Integer cmptID) {
		IdSetBean<Integer> idSet = metaCmpt2AttrMap.get(cmptID);
		if (idSet == null)
			return null;
		
		List<MetaAttributeBean> attrs = new LinkedList<MetaAttributeBean>();
		Iterator<Integer> it = idSet.iterator();
		while (it.hasNext()) {
			Integer attrID = it.next();
			MetaAttributeBean attr = metaAttrMap.get(attrID);
			attrs.add(attr);
		}
		
		return attrs;
	}
	
	public String getCmptIDAttrNameByID(Integer cmptID) {
		IdSetBean<Integer> idSet = metaCmpt2AttrMap.get(cmptID);
		if (idSet == null)
			return null;
		
		String idAttrName = null;
		Iterator<Integer> it = idSet.iterator();
		while (it.hasNext()) {
			Integer attrID = it.next();
			
			MetaAttributeBean attribute = metaAttrMap.get(attrID);
			if (attribute.getAttrName().indexOf(ID_INDEX) != -1
					&& !attribute.getAttrName().equals("MASTER_ID")) {
				idAttrName = attribute.getAttrName();
				break;
			}
		}
		
		return idAttrName;
	}
	
	public String getCmptIDAttrNameByName(String cmptName) {
		Integer cmptID = metaCmptName2IDMap.get(cmptName);
		if (cmptID == null)
			return null;
		
		return getCmptIDAttrNameByID(cmptID);
	}
	
	public Integer getComponentID(String cmptName) {
		return metaCmptName2IDMap.get(cmptName);
	}
	
	public IdSetBean<Integer> getAttrIdSet(Integer cmptID) {
		return metaCmpt2AttrMap.get(cmptID);
	}
	
	public MetaAttributeBean getAttributeByID(Integer attrID) {
		return metaAttrMap.get(attrID);
	}
	
	public Integer getQuotaCode(String quotaName) {
		if (quotaName2Code == null)
			return null;
		
		return quotaName2Code.get(quotaName);
	}
	
	public String getQuotaName(Integer quotaCode) {
		if (quotaCode == null)
			return null;
		
		return quotaCode2Name.get(quotaCode);
	}
	
	public Topology getTopo() {
		return topo;
	}
	
	public Set<String> getSubNodes(String parentID) {
		return this.topo.get(parentID, CONSTS.TOPO_TYPE_CONTAIN);
	}
	
	public JsonArray getMetaTreeByInstId(String instId) {
		JsonArray arr = new JsonArray();
		getTreeChild(arr,instId);
		return arr;
	}
	
	public JsonObject getMetaDataByInstId(String instId) {
		InstanceDtlBean insDtlBean = instanceDtlMap.get(instId);
		if (insDtlBean == null)
			return null;
		
		JsonObject json = new JsonObject();
		JsonArray arr = new JsonArray();
		
		int cmptId = insDtlBean.getInstance().getCmptID();
		Map<String, InstAttributeBean> attrmap = insDtlBean.getAttrMap();
		attrmap.forEach((attrName,attrValue) -> {
			JsonObject row = new JsonObject();
			row.put("ATTR_NAME", attrName);
			row.put("ATTR_VALUE", attrValue.getAttrValue());
			MetaAttributeBean maBean = metaAttrMap.get(attrValue.getAttrID());
			row.put("ATTR_VALUE_CN", maBean.getAttrNameCN());
			arr.add(row);
		});
		
		MetaComponentBean mcBean = metaCmptMap.get(cmptId);
		String name = mcBean.getCmptNameCn();
		
		json.put(name, arr);
		
		return json;
	}
	
	private boolean getTreeChild(JsonArray arr, String instId) {
		Set<String> childs = topo.get(instId, CONSTS.TOPO_TYPE_CONTAIN);
		boolean hasChilds = childs != null && !childs.isEmpty();
		if(hasChilds) {
			for(String child : childs) {
				JsonObject json = new JsonObject();
				json.put("text", child);
				JsonArray cArr = new JsonArray();
				if(getTreeChild(cArr,child)) {
					json.put("nodes", cArr);
				}
				arr.add(json);
			}
		}
		return hasChilds;
	}
	

	
	public List<InstanceDtlBean> getVbrokerByServId(String servId) {
		ServiceBean servBean = serviceMap.get(servId);
		int vbrokerContainerCmptID = MetaData.get().getComponentID("MQ_VBROKER_CONTAINER");
		
		if(servBean == null ||  !CONSTS.SERV_TYPE_MQ.equals(servBean.getServType())) {
			return null;
		}
		
		Set<String> childs = topo.get(servId, CONSTS.TOPO_TYPE_CONTAIN);
		if(childs == null || childs.isEmpty()) {
			return null;
		}	

		List<InstanceDtlBean> list = new ArrayList<>();
		
		for(String child : childs) {
			InstanceDtlBean insDtlBean = instanceDtlMap.get(child);
			int cmptId = insDtlBean.getInstance().getCmptID();
			if(cmptId == vbrokerContainerCmptID) {
				Set<String> vbrokers = topo.get(child, CONSTS.TOPO_TYPE_CONTAIN);
				for(String vbroker : vbrokers) {
					list.add(instanceDtlMap.get(vbroker));
				}
				break;
			}
		}
		
		return list;
	}
	
	public List<InstanceDtlBean> getMasterBrokersByServId(String servId){
		List<InstanceDtlBean> brokers = new ArrayList<>();
		List<InstanceDtlBean> vbrokers = getVbrokerByServId(servId);
		
		for(InstanceDtlBean vbroker : vbrokers) {
			String deployed = vbroker.getInstance().getIsDeployed();
			if(deployed.equals(CONSTS.NOT_DEPLOYED)) {
				continue;
			}
			String masterId = vbroker.getAttribute(FixHeader.HEADER_MASTER_ID).getAttrValue();
			InstanceDtlBean broker = instanceDtlMap.get(masterId);
			brokers.add(broker);
		}
		
		return brokers;
	}
	
	public boolean doMQServiceUndeploy(JsonObject json, EventType type) {
		if (this.queueMap == null)
			return true;

		String servId = json.getString(FixHeader.HEADER_INSTANCE_ID);
		if (HttpUtils.isNull(servId))
			return false;

		if (type == EventType.e31) {
			IdSetBean<String> idSet = servId2QueueIdMap.get(servId);
			if (idSet == null || idSet.isEmpty())
				return true;
			
			Iterator<String> ids = idSet.iterator();
			while(ids.hasNext()) {
				String queueId = ids.next();
				QueueBean bean = queueMap.get(queueId);
				if(bean != null) {
					if(bean.getQueueType().equals(CONSTS.TYPE_TOPIC)){
						IdSetBean<String> permnentTopicIdSet = queueId2ConsumerIdMap.get(queueId);
						Iterator<String> permnentTopicIds = permnentTopicIdSet.iterator();
						while(permnentTopicIds.hasNext()) {
							String consumerId = permnentTopicIds.next();
							permTopicMap.remove(consumerId);
						}
						queueId2ConsumerIdMap.remove(queueId);
					}
				}
				
				queueMap.remove(queueId);
			}
		}
		
		return true;
	}
	
	public String getMetaServUrls() {
		return metaServUrls;
	}
	
	public boolean isServDepplyed(String instId) {
		if (serviceMap == null || HttpUtils.isNull(instId))
			return false;

		ServiceBean seviceBean = serviceMap.get(instId);
		if (seviceBean == null)
			return false;
		
		return seviceBean.getDeployed().equals(CONSTS.DEPLOYED);
	}
	
	public void setDBPwd(String instId, String user, String pwd) {
		if (serviceMap == null || HttpUtils.isNull(instId))
			return;

		ServiceBean seviceBean = serviceMap.get(instId);
		if (seviceBean == null)
			return;
		
		seviceBean.setUser(user);
		seviceBean.setPassword(pwd);
	}
	
}
