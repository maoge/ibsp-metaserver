package ibsp.metaserver.bean;

import ibsp.metaserver.monitor.ConnType;

public class MQConnectionInfoBean {
    private ConnType connType;

    private String sourceAddress;
    private long produceRate;
    private long produceCounts;
    private long consumerRate;
    private long consumerCounts;

    public MQConnectionInfoBean() {
        this.connType = ConnType.DEFAULT;
        produceRate = 0L;
        produceCounts = 0L;
        consumerRate = 0L;
        consumerCounts = 0L;
    }

    public ConnType getConnType() {
        return connType;
    }

    public void setConnType(ConnType connType) {
        this.connType = connType;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
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
}
