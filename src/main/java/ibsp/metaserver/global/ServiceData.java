package ibsp.metaserver.global;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.utils.UUIDUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.shareddata.SharedData;

public class ServiceData {
	
	private HttpServer httpServer;
	private EventBus eventBus;
	private MessageConsumer<String> sysEvMsgConsumer;
	
	private SharedData sharedData;
	
	private static ServiceData theInstance = null;
	private static ReentrantLock intanceLock = null;
	
	static {
		intanceLock = new ReentrantLock();
	}
	
	public ServiceData() {
		
	}
	
	public void initData() {
		
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
	
	public boolean isServContainSingleVBroker(String servId) {
		List<InstanceDtlBean> list = MetaData.get().getVbrokerByServId(servId);
		if(list != null && list.size() > 1) {
			return false;
		}
		return true;
	}
	
}
