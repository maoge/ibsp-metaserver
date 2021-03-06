package ibsp.metaserver.bean;

import java.util.HashMap;

import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonObject;

public class QueueBean extends BeanMapper {
	
	private String queueId;    // "QUEUE_ID"
	private String queueName;  // "QUEUE_NAME"
	private String durable;    // "IS_DURABLE"  0: not durable;    1: durable
	private String ordered;    // "IS_ORDERED"  0: not ordered;    1: global ordered
	private String priority;   // "IS_PRIORITY" 0: not priority;   1: priority queue
	private String queueType;  // "QUEUE_TYPE"  1: queue;          2: topic
	private String deploy;     // "IS_DEPLOY"   0: not deployed;   1: deployed

	private String serviceId;    // 冗余队列所在组的id
	private String serviceName;  // 冗余队列所在组的name

	public QueueBean() {
		queueId   = "";
		queueName = "";
		durable   = "";
		ordered   = "";
		priority  = "";
		queueType = "";
		deploy    = "";
		
		serviceId   = "";
		serviceName = "";
	}
	
	public QueueBean(String queueId, String queueName, String durable, String ordered,
			String priority, String queueType, String deploy, String serviceId, String serviceName) {
		super();
		
		this.queueId = queueId;
		this.queueName = queueName;
		this.durable = durable;
		this.ordered = ordered;
		this.priority = priority;
		this.queueType = queueType;
		this.deploy = deploy;
		
		this.serviceId = serviceId;
		this.serviceName = serviceName;
	}

	public static QueueBean convert(HashMap<String, Object> mapper) {
		if (mapper == null)
			return null;
		
		String queueId     = getFixDataAsString(mapper, FixHeader.HEADER_QUEUE_ID);
		String queueName   = getFixDataAsString(mapper, FixHeader.HEADER_QUEUE_NAME);
		String durable     = getFixDataAsString(mapper, FixHeader.HEADER_IS_DURABLE);
		String ordered     = getFixDataAsString(mapper, FixHeader.HEADER_GLOBAL_ORDERED);
		String priority    = getFixDataAsString(mapper, FixHeader.HEADER_IS_PRIORITY);
		String queueType   = getFixDataAsString(mapper, FixHeader.HEADER_QUEUE_TYPE);
		String deploy      = getFixDataAsString(mapper, FixHeader.HEADER_IS_DEPLOY);
		String serviceId   = getFixDataAsString(mapper, FixHeader.HEADER_SERV_ID);
		String serviceName = getFixDataAsString(mapper, FixHeader.HEADER_SERV_NAME);
		
		return new QueueBean(queueId, queueName, durable, ordered, priority, queueType, deploy, serviceId, serviceName);
	}
	
	public JsonObject toJsonObject() {
		JsonObject result = new JsonObject();
		
		result.put(FixHeader.HEADER_QUEUE_ID, this.queueId);
		result.put(FixHeader.HEADER_QUEUE_NAME, this.queueName);
		result.put(FixHeader.HEADER_IS_DURABLE, this.durable);
		result.put(FixHeader.HEADER_GLOBAL_ORDERED, this.ordered);
		result.put(FixHeader.HEADER_IS_PRIORITY, this.priority);
		result.put(FixHeader.HEADER_QUEUE_TYPE, this.queueType);
		result.put(FixHeader.HEADER_IS_DEPLOY, this.deploy);
		result.put(FixHeader.HEADER_SERV_ID, this.serviceId);
		result.put(FixHeader.HEADER_SERV_NAME, this.serviceName);
		return result;
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getDurable() {
		return durable;
	}

	public void setDurable(String durable) {
		this.durable = durable;
	}
	
	public String getOrdered() {
		return ordered;
	}

	public void setOrdered(String ordered) {
		this.ordered = ordered;
	}
	
	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getQueueType() {
		return queueType;
	}

	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}

	public String getDeploy() {
		return deploy;
	}

	public void setDeploy(String deploy) {
		this.deploy = deploy;
	}
	
	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public String toString() {
		return toJsonString(this);
	}
}
