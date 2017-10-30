package ibsp.metaserver.global;

import java.util.concurrent.locks.ReentrantLock;

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
	
	static {
		intanceLock = new ReentrantLock();
	}
	
	public ServiceData() {
		mtx = new Object();
		uuid = UUIDUtils.genUUID();
	}
	
	public static ServiceData get() {
		try {
			intanceLock.lock();
			if (theInstance != null){
				return theInstance;
			} else {
				theInstance = new ServiceData();
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

}
