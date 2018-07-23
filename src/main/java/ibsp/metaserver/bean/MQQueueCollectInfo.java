package ibsp.metaserver.bean;

import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一个MQQueueCollectInfo对应的是 队列名 -> 队列的采集信息
 * 如果是topic的话，对应的是topic的名称 -> topic底下的所有的实际绑定队列的采集信息
 */
public class MQQueueCollectInfo {

    private String queueName;
    private String queueId;
    private String queueType;

    private Map<String, MQQueueInfoBean> queueInfoBeanMap; //队列信息vbrokerId -> MQQueueInfoBean
    private Map<String, MQQueueCollectInfo> topicInfoBeanMap;//topic信息 realQueueName -> MQQueueCollectInfo

    private long produceRate;
    private long produceCounts;
    private long consumerRate;
    private long consumerCounts;

    private long memory;
    private long ready;
    private long unack;

    public MQQueueCollectInfo(String queueName) {
        this.queueName = queueName;
        QueueBean queueBean = MetaData.get().getQueueBeanByName(queueName);
        if(queueBean != null) {
            queueId = queueBean.getQueueId();
            queueType = queueBean.getQueueType();
        }
        queueInfoBeanMap = new ConcurrentHashMap<>();
        produceRate    = 0L;
        produceCounts  = 0L;
        consumerRate   = 0L;
        consumerCounts = 0L;

        memory = 0L;
        ready  = 0L;
        unack  = 0L;
    }

    public void saveQueueInfoBean(MQQueueInfoBean mqQueueInfoBean) {
        if(mqQueueInfoBean == null)
            return;

        String collectQueueName = mqQueueInfoBean.getQueueName();

        if(collectQueueName.equalsIgnoreCase(queueName)) {
            if(CONSTS.TYPE_QUEUE.equalsIgnoreCase(queueType)) {
                statisticsQueueInfo(mqQueueInfoBean);
            }
            queueInfoBeanMap.put(mqQueueInfoBean.getVbrokerId(), mqQueueInfoBean);
        }else {

            boolean isIn = MetaData.get().isPermnentTopicInQueue(collectQueueName, queueId);

            if(isIn) {
                if(topicInfoBeanMap == null) {
                    topicInfoBeanMap = new ConcurrentHashMap<>();
                }

                MQQueueCollectInfo topicCollectInfo = topicInfoBeanMap.get(collectQueueName);
                if(topicCollectInfo == null) {
                    topicCollectInfo = new MQQueueCollectInfo(collectQueueName);
                    topicInfoBeanMap.put(collectQueueName, topicCollectInfo);
                }
                statisticsTopicInfo(mqQueueInfoBean);
                topicCollectInfo.saveQueueInfoBean(mqQueueInfoBean);
            }
        }

    }

    private void statisticsQueueInfo(MQQueueInfoBean mqQueueInfoBean) {
        if(mqQueueInfoBean == null)
            return;

        String vbrokerId = mqQueueInfoBean.getVbrokerId();
        MQQueueInfoBean oldQueueCollectInfo = queueInfoBeanMap.get(vbrokerId);
        if(oldQueueCollectInfo != null) {
            this.produceRate    -= oldQueueCollectInfo.getProduceRate();
            this.produceCounts  -= oldQueueCollectInfo.getProduceCounts();
            this.consumerRate   -= oldQueueCollectInfo.getConsumerRate();
            this.consumerCounts -= oldQueueCollectInfo.getConsumerCounts();

            this.memory -= oldQueueCollectInfo.getMemory();
            this.ready  -= oldQueueCollectInfo.getMsgReady();
            this.unack  -= oldQueueCollectInfo.getMsgUnAck();
        }

        this.produceRate    += mqQueueInfoBean.getProduceRate();
        this.produceCounts  += mqQueueInfoBean.getProduceCounts();
        this.consumerRate   += mqQueueInfoBean.getConsumerRate();
        this.consumerCounts += mqQueueInfoBean.getConsumerCounts();

        this.memory += mqQueueInfoBean.getMemory();
        this.ready  += mqQueueInfoBean.getMsgReady();
        this.unack  += mqQueueInfoBean.getMsgUnAck();
    }

    private void statisticsTopicInfo(MQQueueInfoBean mqQueueInfoBean) {
        if(mqQueueInfoBean == null)
            return;

        if(topicInfoBeanMap != null && topicInfoBeanMap.size() > 0 ) {
            //累计topic的信息
            MQQueueCollectInfo mqQueueCollectInfo = topicInfoBeanMap.get(mqQueueInfoBean.getQueueName());

            if(mqQueueCollectInfo != null) {
                this.produceRate    -= mqQueueCollectInfo.getProduceRate();
                this.produceCounts  -= mqQueueCollectInfo.getProduceCounts();
                this.consumerRate   -= mqQueueCollectInfo.getConsumerRate();
                this.consumerCounts -= mqQueueCollectInfo.getConsumerCounts();

                this.memory -= mqQueueCollectInfo.getMemory();
                this.ready  -= mqQueueCollectInfo.getReady();
                this.unack  -= mqQueueCollectInfo.getUnack();
            }else {
                mqQueueCollectInfo = new MQQueueCollectInfo(mqQueueInfoBean.getQueueName());
                topicInfoBeanMap.put(mqQueueInfoBean.getQueueName(), mqQueueCollectInfo);
            }

            mqQueueCollectInfo.statisticsQueueInfo(mqQueueInfoBean);

            this.produceRate    += mqQueueCollectInfo.getProduceRate();
            this.produceCounts  += mqQueueCollectInfo.getProduceCounts();
            this.consumerRate   += mqQueueCollectInfo.getConsumerRate();
            this.consumerCounts += mqQueueCollectInfo.getConsumerCounts();

            this.memory += mqQueueCollectInfo.getMemory();
            this.ready  += mqQueueCollectInfo.getReady();
            this.unack  += mqQueueCollectInfo.getUnack();
        }
    }

    public String getQueueName() {
        return queueName;
    }

    public String getQueueId() {
        return queueId;
    }

    public Map<String, MQQueueInfoBean> getQueueInfoBeanMap() {
        return queueInfoBeanMap;
    }

    public Map<String, MQQueueCollectInfo> getTopicInfoBeanMap() {
        return topicInfoBeanMap;
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

    public long getMemory() {
        return memory;
    }

    public long getReady() {
        return ready;
    }

    public long getUnack() {
        return unack;
    }

}
