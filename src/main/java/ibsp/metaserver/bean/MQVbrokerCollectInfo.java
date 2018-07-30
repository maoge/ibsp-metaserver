package ibsp.metaserver.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MQVbrokerCollectInfo {
    private Map<String, MQNodeInfoBean> nodeInfoMap; //inst_id -> MQNodeInfoBean
    private Map<String, MQConnectionInfoBean> connInfoMap;//sourceAddress -> MQConnectionInfoBean

    //vbroker所有的连接的速率和总数，从api/channels获取
    private long connProduceRate;
    private long connProduceCounts;
    private long connConsumerRate;
    private long connConsumerCounts;

    //vroker的速率和总数，从api/queues获取统计
    private long produceRate;
    private long produceCounts;
    private long consumerRate;
    private long consumerCounts;

    private long timestamp;

    public MQVbrokerCollectInfo() {
        nodeInfoMap = new ConcurrentHashMap<>();
        connInfoMap = new ConcurrentHashMap<>();
        connProduceRate = 0L;
        connProduceCounts = 0L;
        connConsumerRate = 0L;
        connConsumerCounts = 0L;

        produceRate = 0L;
        produceCounts = 0L;
        consumerRate = 0L;
        consumerCounts = 0L;
    }

    public void addNodeInfo(MQNodeInfoBean nodeInfoBean) {
        if(nodeInfoBean == null)
            return;
        nodeInfoMap.put(nodeInfoBean.getInstId(), nodeInfoBean);
    }

    public void addConnInfo(MQConnectionInfoBean connectionInfoBean) {
        if(connectionInfoBean == null)
            return;

        String sourceAddress = connectionInfoBean.getSourceAddress();
        MQConnectionInfoBean old = connInfoMap.get(sourceAddress);
        synchronized (MQVbrokerCollectInfo.class) {
            if(old != null) {
                connProduceRate -= old.getProduceRate();
                connProduceCounts -= old.getProduceCounts();
                connConsumerRate -= old.getConsumerRate();
                connConsumerCounts -= old.getConsumerCounts();
            }
            connInfoMap.put(connectionInfoBean.getSourceAddress(), connectionInfoBean);

            connProduceRate += connectionInfoBean.getProduceRate();
            connProduceCounts += connectionInfoBean.getProduceCounts();
            connConsumerRate += connectionInfoBean.getConsumerRate();
            connConsumerCounts += connectionInfoBean.getConsumerCounts();
        }
    }

    public Map<String, MQNodeInfoBean> getNodeInfoMap() {
        return nodeInfoMap;
    }

    public Map<String, MQConnectionInfoBean> getConnInfoMap() {
        return connInfoMap;
    }

    public long getConnProduceRate() {
        return connProduceRate;
    }

    public long getConnProduceCounts() {
        return connProduceCounts;
    }

    public long getConnConsumerRate() {
        return connConsumerRate;
    }

    public long getConnConsumerCounts() {
        return connConsumerCounts;
    }

    public long getProduceRate() {
        return produceRate;
    }

    public long getProduceCounts() {
        return produceCounts;
    }

    public long getConsumerRate() {
        return consumerRate;
    }

    public long getConsumerCounts() {
        return consumerCounts;
    }

    public void setConnProduceRate(long connProduceRate) {
        this.connProduceRate = connProduceRate;
    }

    public void setConnProduceCounts(long connProduceCounts) {
        this.connProduceCounts = connProduceCounts;
    }

    public void setConnConsumerRate(long connConsumerRate) {
        this.connConsumerRate = connConsumerRate;
    }

    public void setConnConsumerCounts(long connConsumerCounts) {
        this.connConsumerCounts = connConsumerCounts;
    }

    public void setProduceRate(long produceRate) {
        this.produceRate = produceRate;
    }

    public void setProduceCounts(long produceCounts) {
        this.produceCounts = produceCounts;
    }

    public void setConsumerRate(long consumerRate) {
        this.consumerRate = consumerRate;
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
