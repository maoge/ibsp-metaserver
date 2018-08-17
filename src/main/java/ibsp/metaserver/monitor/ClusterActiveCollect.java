package ibsp.metaserver.monitor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.ServiceData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.SysConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 必须在Hazelcast加载完后初始化
 */
public class ClusterActiveCollect implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(ClusterActiveCollect.class);

    private static final String FIRST_START_TIME_STRING="FIRST_START_TIME_STRING";
    private static final String EXCUTE_TIME_MAP = "EXCUTE_TIME_MAP";

    private ScheduledExecutorService taskInventor;
    private HazelcastInstance hzInstance;
    private int interval;
    private Map<String, Long> excuteTimeMap;

    private static ClusterActiveCollect instance;
    private static Object mtx = null;

    static {
        mtx = new Object();
    }

    public ClusterActiveCollect(){
        SysConfig sysConfig = SysConfig.get();
        if(sysConfig.isActiveCollect()) {

            this.taskInventor = Executors.newScheduledThreadPool(1);
            this.hzInstance = ServiceData.get().getHzInstance();
            this.excuteTimeMap = hzInstance.getMap(EXCUTE_TIME_MAP);
            this.interval = sysConfig.getActiveCollectInterval();

            Long startTime = excuteTimeMap.get(FIRST_START_TIME_STRING);
            long clusterTime = hzInstance.getCluster().getClusterTime();
            long currentTime = System.currentTimeMillis();

            if(startTime == null) {
                startTime = clusterTime + 1;
                excuteTimeMap.put(FIRST_START_TIME_STRING, startTime);
            }

            long delay = currentTime - clusterTime;
            delay = delay < 0 ? -delay : delay;
            delay = delay % interval + interval - ((clusterTime - startTime) % interval);

            logger.debug("delay = {} , interval = {} !" ,delay, interval);
            taskInventor.scheduleAtFixedRate(this, delay, interval, TimeUnit.MILLISECONDS);
        }
    }

    public static ClusterActiveCollect get(){
        if(instance != null) {
            return instance;
        }

        synchronized (mtx) {
            if(instance == null){
                instance = new ClusterActiveCollect();
            }
        }

        return instance;
    }

    @Override
    public void run() {
        Map<String, ServiceBean> serviceMap = MetaData.get().getServiceMap();
        Set<Map.Entry<String, ServiceBean>> entrySet = serviceMap.entrySet();

        for(Map.Entry<String, ServiceBean> entry : entrySet) {
            ServiceBean serviceBean = entry.getValue();
            if(serviceBean == null || CONSTS.NOT_DEPLOYED.equalsIgnoreCase(serviceBean.getDeployed())) {
                continue;
            }

            String instID = serviceBean.getInstID();
            long currentClusterTime = hzInstance.getCluster().getClusterTime();
            int memberSize = hzInstance.getCluster().getMembers().size();
            Long prevExcuteTime = excuteTimeMap.get(instID);
            prevExcuteTime = prevExcuteTime == null ? 0L : prevExcuteTime;

            //如果只有一个节点或者这个service的执行时间超过interval（集群时间可能会有误差，多100ms来消除）执行监控采集
            if(memberSize == 1 || currentClusterTime - prevExcuteTime >= interval-100) {
                ILock lock = hzInstance.getLock(instID + "-lock");
                if(! lock.isLocked()) {
                    try {
                        if(lock.tryLock(1, TimeUnit.MICROSECONDS)) {
                            logger.debug("start monitor : {} " ,serviceBean.getServName());
                            try {

                                String type = serviceBean.getServType();
                                if(CONSTS.SERV_TYPE_DB.equalsIgnoreCase(type)) {
                                    //DB 监控
                                    TiDBServiceMonitor.excute(serviceBean);
                                }else if(CONSTS.SERV_TYPE_MQ.equalsIgnoreCase(type)) {
                                    //MQ 监控
                                    MQServiceMonitor.excute(serviceBean);

                                }else if(CONSTS.SERV_TYPE_CACHE.equalsIgnoreCase(type)) {
                                    //CACHE 监控
                                    CacheServiceMonitor1.execute(serviceBean);
                                }

                                excuteTimeMap.put(instID,currentClusterTime);

                            }catch (Exception e){
                                logger.error(e.getMessage(), e);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
    }
}
