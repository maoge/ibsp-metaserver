package ibsp.metaserver.eventbus;

import ibsp.metaserver.global.GlobalRes;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
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
		
		Producer<byte[]> evBusSender = GlobalRes.get().getProducer();
		try {
			if (evBusSender != null) {
				evBusSender.send(msg.getBytes());
			} else {
				logger.info("event bus not ready!");
			}
		} catch (PulsarClientException e) {
			logger.error("event bus send error:" + e.getMessage());
		}
	}

}
