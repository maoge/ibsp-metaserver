package ibsp.metaserver.monitor;

//import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.core.ILock;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.global.GlobalRes;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.SysConfig;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClusterActiveCollect implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(ClusterActiveCollect.class);

    private ScheduledExecutorService taskInventor;
    private int interval;

    private static ClusterActiveCollect instance;
    private static Object mtx = null;

    static {
        mtx = new Object();
    }

    public ClusterActiveCollect() {
        if(!SysConfig.get().isActiveCollect())
        	return;
        
        this.taskInventor = Executors.newScheduledThreadPool(1);
        this.interval = SysConfig.get().getActiveCollectInterval();
        taskInventor.scheduleAtFixedRate(this, interval, interval, TimeUnit.MILLISECONDS);
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
            String lockKey = instID + "-lock";
            
            RedissonClient redissonClient = GlobalRes.get().getRedissionClient();
            RLock lock = redissonClient.getLock(lockKey);
            
            try {
            	lock.lock();
            	
                String type = serviceBean.getServType();
                switch (type) {
                case CONSTS.SERV_TYPE_DB:
                	TiDBServiceMonitor.excute(serviceBean);
                	break;
                case CONSTS.SERV_TYPE_MQ:
                	MQServiceMonitor.excute(serviceBean);
                	break;
                case CONSTS.SERV_TYPE_CACHE:
                	CacheServiceMonitor1.execute(serviceBean);
                	break;
                default:
                	break;
                }            	
    		} catch (Exception e) {
    			logger.error(e.getMessage(), e);
    		} finally {
    			lock.unlock();
    		}
        }
    }
}
