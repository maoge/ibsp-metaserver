package ibsp.metaserver.eventbus;

import ibsp.metaserver.global.ServiceData;
import ibsp.metaserver.utils.CONSTS;
import io.vertx.core.eventbus.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBusMsg {
	
	private static Logger logger = LoggerFactory.getLogger(EventBusMsg.class.getName());
	
	public static void publishEvent(EventBean evBean) {
		if (evBean == null) {
			logger.error("evBean is null ......");
			return;
		}
		
		String msg = evBean.asJsonString();
		
		logger.debug("EventBus public messages:{}", msg);
		EventBus evBus = ServiceData.get().getEventBus();
		if (evBus != null) {
			evBus.publish(CONSTS.SYS_EVENT_QUEUE, msg);
		} else {
			logger.info("event bus not ready!");
		}
	}

}
