package ibsp.metaserver.global;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.QueueBean;
import ibsp.metaserver.dbservice.MQService;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.UUIDUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.shareddata.SharedData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceData {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceData.class);
	
	private HttpServer httpServer;
	private EventBus eventBus;
	private MessageConsumer<String> sysEvMsgConsumer;
	private String uuid;
	
	private SharedData sharedData;
	
	private static ServiceData theInstance = null;
	private static ReentrantLock intanceLock = null;
	private static Object mtx = null;
	
	private Map<String, QueueBean>   queueMap;
	private Map<String, String>      queueName2IdMap;
	
	static {
		intanceLock = new ReentrantLock();
	}
	
	public ServiceData() {
		mtx = new Object();
		uuid = UUIDUtils.genUUID();
		
		queueMap   = new ConcurrentHashMap<String, QueueBean>();
		queueName2IdMap   = new ConcurrentHashMap<String, String>();
	}
	
	public void initData() {
		loadQueue();
	}
	
	public static ServiceData get() {
		try {
			intanceLock.lock();
			if (theInstance != null){
				return theInstance;
			} else {
				theInstance = new ServiceData();
				theInstance.initData();
			}
		} finally {
			intanceLock.unlock();
		}
		
		return theInstance;
	}
	
	public HttpServer getHttpServer() {
		return httpServer;
	}

	public void setHttpServer(HttpServer httpServer) {
		this.httpServer = httpServer;
	}
	
	public EventBus getEventBus() {
		return eventBus;
	}

	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	public MessageConsumer<String> getSysEvMsgConsumer() {
		return sysEvMsgConsumer;
	}

	public void setSysEvMsgConsumer(MessageConsumer<String> msgConsumer) {
		this.sysEvMsgConsumer = msgConsumer;
	}
	
	public SharedData getSharedData() {
		return sharedData;
	}

	public void setSharedData(SharedData sharedData) {
		this.sharedData = sharedData;
	}

	private void loadQueue() {
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
		
		genQueueName2IdMap();
	}
	
	private void genQueueName2IdMap() {
		if (queueMap == null)
			return;
		
		synchronized(mtx) {
			queueName2IdMap.clear();
			
			Set<Entry<String, QueueBean>> queueEntrySet = queueMap.entrySet();
			for (Entry<String, QueueBean> queueEntry : queueEntrySet) {
				QueueBean queueBean = queueEntry.getValue();
				if (queueBean == null)
					continue;
				
				queueName2IdMap.put(queueBean.getQueueName(), queueBean.getQueueId());
			}
		}
	}
	
	public boolean isQueueNameExistsByName(String queueName) {
		boolean exists = false;
		
		synchronized(mtx) {
			if (queueName2IdMap == null) {
				return false;
			}
			exists = queueName2IdMap.containsKey(queueName);
		}
		
		return exists;
	}
	
	public boolean isQueueNameExistsById(String queueId) {
		boolean exists = false;
		
		synchronized(mtx) {
			if (queueName2IdMap == null) {
				return false;
			}
			exists = queueMap.containsKey(queueId);
		}
		
		return exists;
	}
	
	public boolean saveQueue(String queueId, QueueBean queueBean) {
		if (queueMap != null) {
			synchronized(mtx) {
				QueueBean oldQueue = queueMap.get(queueId);
				queueMap.put(queueId, queueBean);
				
				if(oldQueue != null) {
					queueName2IdMap.remove(oldQueue.getQueueName());
				}
				
				queueName2IdMap.put(queueBean.getQueueName(), queueId);
			}
			return true;
		}
		
		return false;
	}
	
	public boolean delQueue(String queueId) {
		if (queueMap != null) {
			synchronized(mtx) {
				QueueBean qb = queueMap.get(queueId);
				if(qb != null) {
					queueName2IdMap.remove(qb.getQueueName());
					queueMap.remove(queueId);	
					return true;
				}		
			}
		}
		return false;
	}
	
	public QueueBean getQueueBeanByName(String queueName) {
		if (queueMap != null) {
			synchronized(mtx) {
				String queueId = queueName2IdMap.get(queueName);
				if(HttpUtils.isNotNull(queueId)) {
					return queueMap.get(queueId);
				}
			}
		}
		return null;
	}
	
	public QueueBean getQueueBeanById(String queueId) {
		if (queueMap != null) {
			synchronized(mtx) {
				if(HttpUtils.isNotNull(queueId)) {
					return queueMap.get(queueId);
				}
			}
		}
		return null;
	}
	
	public boolean isServContainSingleVBroker(String servId) {
		List<InstanceDtlBean> list = MetaData.get().getVbrokerByServId(servId);
		if(list != null && list.size() > 1) {
			return false;
		}
		return true;
	}
}
