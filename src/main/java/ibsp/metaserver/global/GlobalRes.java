package ibsp.metaserver.global;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.eventbus.EventDispatcher;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.SysConfig;

public class GlobalRes {
	
	private static Logger logger = LoggerFactory.getLogger(GlobalRes.class);
	
    private RedissonClient redissonClient;
    
	private PulsarClient pulsarClient = null;
	private Producer<byte[]> producer = null;
	private Consumer<byte[]> consumer = null;
	
	private EventDispatcher eventDispatcher = null;
	
	private static GlobalRes theInstance = null;
	private static ReentrantLock intanceLock = null;

	static {
		intanceLock = new ReentrantLock();
	}
	
	public GlobalRes() {
		
	}
	
	public static GlobalRes get() {
		try {
			intanceLock.lock();
			if (theInstance != null){
				return theInstance;
			} else {
				theInstance = new GlobalRes();
				theInstance.init();
			}
		} finally {
			intanceLock.unlock();
		}
		
		return theInstance;
	}
	
	public static void release() {
		try {

			intanceLock.lock();
			
			if (theInstance == null)
				return;
			
			if (theInstance.redissonClient != null)
				theInstance.redissonClient.shutdown();
			
			if (theInstance.producer != null)
				theInstance.producer.close();
			
			if (theInstance.consumer != null)
				theInstance.consumer.close();
			
			if (theInstance.pulsarClient != null)
				theInstance.pulsarClient.close();
			
			if (theInstance.eventDispatcher != null)
				theInstance.eventDispatcher.stop();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void init() {
		initRedisPool();
		initEventBusBroker();
		initEventDispatcher();
	}
	
	private void initRedisPool() {
		try {
			Config config = new Config();
			String uri = String.format("redis://%s@%s:%d",
					SysConfig.get().getRedisAuth(),
					SysConfig.get().getRedisHost(), SysConfig.get().getRedisPort());
			SingleServerConfig serverConfig = config.useSingleServer();
			serverConfig.setAddress(uri);
			serverConfig.setConnectionPoolSize(SysConfig.get().getRedisPoolSize());
			serverConfig.setConnectTimeout(3000); // 3s
			serverConfig.setTimeout(3000); // 3s
			redissonClient = Redisson.create(config);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
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
			logger.error("eventbus pulsar borker connect error:" + e.getMessage(), e);
		}
	}
	
	private void initEventDispatcher() {
		eventDispatcher = new EventDispatcher(this.consumer);
	}
	
	public RedissonClient getRedissionClient() {
		RedissonClient client = null;
		try {
			intanceLock.lock();
			
			if (theInstance != null)
				client = theInstance.redissonClient;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		return client;
	}

	public Producer<byte[]> getProducer() {
		Producer<byte[]> pulsarProducer = null;
		try {
			intanceLock.lock();
			
			if (theInstance != null)
				pulsarProducer = theInstance.producer;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		return pulsarProducer;
	}

	public Consumer<byte[]> getConsumer() {
		Consumer<byte[]> pulsarConsumer = null;
		try {
			intanceLock.lock();
			
			if (theInstance != null)
				pulsarConsumer = theInstance.consumer;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
		return pulsarConsumer;
	}

}
