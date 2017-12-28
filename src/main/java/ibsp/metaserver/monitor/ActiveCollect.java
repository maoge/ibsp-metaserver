package ibsp.metaserver.monitor;

import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.SysConfig;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveCollect {
	
	private static Logger logger = LoggerFactory.getLogger(ActiveCollect.class);
	
	private ScheduledExecutorService taskInventor;
	private ActiveCollectTaskInventor activeCollectTaskInventor;
	
	private static Object mtx = null;
	private static ActiveCollect theInstance;
	
	static {
		mtx = new Object();
	}
	
	public ActiveCollect() {
		if (SysConfig.get().isActiveCollect()) {
			int execInterval = SysConfig.get().getActiveCollectInterval();
			
			activeCollectTaskInventor = new ActiveCollectTaskInventor();
			taskInventor = Executors.newSingleThreadScheduledExecutor();
			taskInventor.scheduleAtFixedRate(activeCollectTaskInventor, execInterval, execInterval, TimeUnit.MILLISECONDS);
		}
	}
	
	public static ActiveCollect get() {
		if (theInstance != null){
			return theInstance;
		}
		
		synchronized(mtx) {
			if (theInstance == null) {
				theInstance = new ActiveCollect();
			}
		}
		
		return theInstance;
	}
	
	private class ActiveCollectTaskInventor implements Runnable {
		
		public ActiveCollectTaskInventor() {
			super();
		}
		
		@Override
		public void run() {
			Map<String, ServiceBean> serviceMap = MetaData.get().getServiceMap();
			Set<Entry<String, ServiceBean>> entrySet = serviceMap.entrySet();
			for (Entry<String, ServiceBean> entry : entrySet) {
				if (entry == null)
					continue;
				
				ServiceBean service = entry.getValue();
				if (service == null)
					continue;
				
				if (service.getDeployed().equals(CONSTS.NOT_DEPLOYED))
					continue;
				
				
			}
			
		}
		
	}

}
