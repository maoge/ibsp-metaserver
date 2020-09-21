package ibsp.metaserver.global;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.hazelcast.core.HazelcastInstance;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.SysConfig;
//import io.vertx.core.eventbus.EventBus;
//import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.shareddata.SharedData;

public class ServiceData {
	
	private static Logger logger = LoggerFactory.getLogger(ServiceData.class);
	
	private HttpServer httpServer = null;
	private SharedData sharedData = null;
	
	// private EventBus eventBus;
	// private HazelcastInstance hzInstance;
	// private MessageConsumer<String> sysEvMsgConsumer;
	
	private PulsarClient pulsarClient = null;
	private Producer<byte[]> producer = null;
	private Consumer<byte[]> consumer = null;
	
	private static ServiceData theInstance = null;
	private static ReentrantLock intanceLock = null;
	
	static {
		intanceLock = new ReentrantLock();
	}
	
	public ServiceData() {
		
	}
	
	public void initData() {
		initEventBusBroker();
	}
	
	private void initEventBusBroker() {
		if (!SysConfig.get().isVertxClustered())
			return;

		try {
			String topic = String.format("persistent://public/default/%s", CONSTS.SYS_EVENT_QUEUE);
			
			String uri = String.format("pulsar://%s:%d", 
					SysConfig.get().getEventBusBrokerIP(), SysConfig.get().getEventBusBrokerPort());
			pulsarClient = PulsarClient.builder().serviceUrl(uri).build();
			
			producer = pulsarClient.newProducer()
					.topic(topic)
					.batchingMaxPublishDelay(1, TimeUnit.MILLISECONDS)
					.sendTimeout(1, TimeUnit.SECONDS)
					.blockIfQueueFull(true).create();
			
			consumer = pulsarClient.newConsumer()
					.topic(topic)
					.subscriptionName(SysConfig.get().getEventBusConsumerSubscription())
					.ackTimeout(1, TimeUnit.SECONDS)
					.subscriptionType(SubscriptionType.Exclusive)
					.subscribe();
		} catch (PulsarClientException e) {
			logger.error("eventbus pulsar borker connect error:" + e.getMessage());
		}
	}
	
	public void closeEventBus() {
		if (!SysConfig.get().isVertxClustered())
			return;
		
		try {
			if (producer != null)
				producer.close();
			
			if (consumer != null)
				consumer.close();
			
			if (pulsarClient != null)
				pulsarClient.close();
		} catch (PulsarClientException e) {
			logger.error(e.getMessage());
		}
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
	
	public Producer<byte[]> getEventBusSender() {
	 	return producer;
	}

	// public void setEventBus(EventBus eventBus) {
	// 	this.eventBus = eventBus;
	// }
	
	public Consumer<byte[]> getSysEvMsgConsumer() {
		return consumer;
	}

	// public void setSysEvMsgConsumer(MessageConsumer<String> msgConsumer) {
	// 	this.sysEvMsgConsumer = msgConsumer;
	// }
	
	public SharedData getSharedData() {
		return sharedData;
	}

	public void setSharedData(SharedData sharedData) {
		this.sharedData = sharedData;
	}

//	public HazelcastInstance getHzInstance() {
//		return hzInstance;
//	}

//	public void setHzInstance(HazelcastInstance hzInstance) {
//		this.hzInstance = hzInstance;
//	}

	public boolean isServContainSingleVBroker(String servId) {
		List<InstanceDtlBean> list = MetaData.get().getVbrokerByServId(servId);
		if(list != null && list.size() > 1) {
			return false;
		}
		return true;
	}
	
}
