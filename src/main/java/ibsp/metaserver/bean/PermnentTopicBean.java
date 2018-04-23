package ibsp.metaserver.bean;

import java.util.HashMap;

import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonObject;

public class PermnentTopicBean extends BeanMapper {
	
	private String consumerId;
	private String realQueue;
	private String mainTopic;
	private String subTopic;
	private String queueId;
	
	public PermnentTopicBean(String consumerId, String realQueue, String mainTopic, 
			String subTopic, String queueId) {
		super();
		this.consumerId = consumerId;
		this.realQueue  = realQueue;
		this.mainTopic  = mainTopic;
		this.subTopic   = subTopic;
		this.queueId = queueId;
	}
	
	public static PermnentTopicBean convert(HashMap<String, Object> mapper) {
		if (mapper == null)
			return null;
		
		String consumerId = getFixDataAsString(mapper, FixHeader.HEADER_CONSUMER_ID);
		String realQueue  = getFixDataAsString(mapper, FixHeader.HEADER_REAL_QUEUE);
		String mainTopic  = getFixDataAsString(mapper, FixHeader.HEADER_MAIN_TOPIC);
		String subTopic   = getFixDataAsString(mapper, FixHeader.HEADER_SUB_TOPIC);
		String queueId    = getFixDataAsString(mapper, FixHeader.HEADER_QUEUE_ID);
		
		return new PermnentTopicBean(consumerId, realQueue, mainTopic, subTopic, queueId);
	}
	
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_CONSUMER_ID, this.consumerId);
		json.put(FixHeader.HEADER_REAL_QUEUE,  this.realQueue);
		json.put(FixHeader.HEADER_MAIN_TOPIC,  this.mainTopic);
		json.put(FixHeader.HEADER_SUB_TOPIC,   this.subTopic);
		json.put(FixHeader.HEADER_QUEUE_ID,    this.queueId);
		return json;
	}

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public String getRealQueue() {
		return realQueue;
	}

	public void setRealQueue(String realQueue) {
		this.realQueue = realQueue;
	}

	public String getMainTopic() {
		return mainTopic;
	}

	public void setMainTopic(String mainTopic) {
		this.mainTopic = mainTopic;
	}

	public String getSubTopic() {
		return subTopic;
	}

	public void setSubTopic(String subTopic) {
		this.subTopic = subTopic;
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

}
