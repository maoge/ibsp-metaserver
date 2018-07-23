package ibsp.metaserver.bean;

public class MQQueueInfoBean {
    private String queueName;
    private String vbrokerId;
    private String vhost;
    private String durable;
    private String autoDelete;
    private String arguments;
    private String node;

    private long memory;
    private long msgRam;
    private long msgPersistent;
    private long msgReady;
    private long msgUnAck;

    private long produceRate;
    private long produceCounts;
    private long consumerRate;
    private long consumerCounts;

    private long timestamp;

    public MQQueueInfoBean () {
        produceRate = 0L;
        produceCounts = 0L;
        consumerRate = 0L;
        consumerCounts = 0L;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getVbrokerId() {
        return vbrokerId;
    }

    public void setVbrokerId(String vbrokerId) {
        this.vbrokerId = vbrokerId;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public String getDurable() {
        return durable;
    }

    public void setDurable(String durable) {
        this.durable = durable;
    }

    public String getAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(String autoDelete) {
        this.autoDelete = autoDelete;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public long getMsgRam() {
        return msgRam;
    }

    public void setMsgRam(long msgRam) {
        this.msgRam = msgRam;
    }

    public long getMsgPersistent() {
        return msgPersistent;
    }

    public void setMsgPersistent(long msgPersistent) {
        this.msgPersistent = msgPersistent;
    }

    public long getMsgReady() {
        return msgReady;
    }

    public void setMsgReady(long msgReady) {
        this.msgReady = msgReady;
    }

    public long getMsgUnAck() {
        return msgUnAck;
    }

    public void setMsgUnAck(long msgUnAck) {
        this.msgUnAck = msgUnAck;
    }

    public long getProduceRate() {
        return produceRate;
    }

    public void setProduceRate(long produceRate) {
        this.produceRate = produceRate;
    }

    public long getProduceCounts() {
        return produceCounts;
    }

    public void setProduceCounts(long produceCounts) {
        this.produceCounts = produceCounts;
    }

    public long getConsumerRate() {
        return consumerRate;
    }

    public void setConsumerRate(long consumerRate) {
        this.consumerRate = consumerRate;
    }

    public long getConsumerCounts() {
        return consumerCounts;
    }

    public void setConsumerCounts(long consumerCounts) {
        this.consumerCounts = consumerCounts;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
