package ibsp.metaserver.bean;

import java.util.HashMap;

import ibsp.metaserver.utils.FixHeader;

public class QueueBean extends BeanMapper {
	
	private String queueId;    // "QUEUE_ID"
	private String queueName;  // "QUEUE_NAME"
	private String durable;    // "IS_DURABLE"  0: not durable;    1: durable
	private String ordered;    // "IS_ORDERED"  0: not ordered;    1: global ordered
	private String queueType;  // "QUEUE_TYPE"  1: queue;          2: topic
	private String deploy;     // "IS_DEPLOY"   0: not deployed;   1: deployed
	
	private String serviceId;    // 冗余队列所在组的id
	private String serviceName;  // 冗余队列所在组的name

	public QueueBean() {
		queueId   = "";
		queueName = "";
		durable   = "";
		ordered   = "";
		queueType = "";
		deploy    = "";
		
		serviceId   = "";
		serviceName = "";
	}
	
	public QueueBean(String queueId, String queueName, String durable, String ordered,
			String queueType, String deploy, String serviceId, String serviceName) {
		super();
		
		this.queueId = queueId;
		this.queueName = queueName;
		this.durable = durable;
		this.ordered = ordered;
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
		String queueType   = getFixDataAsString(mapper, FixHeader.HEADER_QUEUE_TYPE);
		String deploy      = getFixDataAsString(mapper, FixHeader.HEADER_IS_DEPLOY);
		String serviceId   = getFixDataAsString(mapper, FixHeader.HEADER_SERVICE_ID);
		String serviceName = getFixDataAsString(mapper, FixHeader.HEADER_SERVICE_NAME);
		
		return new QueueBean(queueId, queueName, durable, ordered, queueType, deploy, serviceId, serviceName);
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
