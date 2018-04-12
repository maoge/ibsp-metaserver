package ibsp.metaserver.monitor;

import ibsp.metaserver.bean.CacheCollectDataParser;
import ibsp.metaserver.bean.CollectDataParser;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.MQCollectDataParser;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.bean.TiDBCollectDataParser;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.threadpool.WorkerPool;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.SysConfig;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
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
	private CacheServiceMonitor cacheServiceCollect;
	
	private static Object mtx = null;
	private static ActiveCollect theInstance;
	
	static {
		mtx = new Object();
	}
	
	public ActiveCollect() {
		if (SysConfig.get().isActiveCollect()) {
			int execInterval = SysConfig.get().getActiveCollectInterval();
			
			activeCollectTaskInventor = new ActiveCollectTaskInventor();
			cacheServiceCollect = new CacheServiceMonitor();
			taskInventor = Executors.newScheduledThreadPool(2);
			taskInventor.scheduleAtFixedRate(activeCollectTaskInventor, execInterval, execInterval, TimeUnit.MILLISECONDS);
			taskInventor.scheduleAtFixedRate(cacheServiceCollect, execInterval, execInterval, TimeUnit.MILLISECONDS);
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
	
	public void Stop() {
		if (SysConfig.get().isActiveCollect()) {
			taskInventor.shutdown();
		}
	}
	
	private class ActiveCollectTaskInventor implements Runnable {
		
		public ActiveCollectTaskInventor() {
			super();
		}
		
		@Override
		public void run() {
			
			try {
				
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
					
					String collectdID = MetaData.get().getServiceCollectdID(service.getInstID());
					if (collectdID == null)
						continue;
					
					InstanceDtlBean collectd = MetaData.get().getInstanceDtlBean(collectdID);
					if (collectd == null)
						continue;
					if (collectd.getInstance().getIsDeployed().equals(CONSTS.NOT_DEPLOYED))
						continue;
					
					String collectdName = collectd.getAttribute(FixHeader.HEADER_COLLECTD_NAME).getAttrValue();
					String ip = collectd.getAttribute(FixHeader.HEADER_IP).getAttrValue();
					String port = collectd.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
					String servType = service.getServType();
					
					// COLLECT_DATA_API
					
					CollectWork collectWork = new CollectWork(collectdID, collectdName, ip, port, servType);
					WorkerPool.get().execute(collectWork);
				}
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			
		}
	}
	
	private static class CollectWork implements Runnable {
		
		String collectdID;
		String collectdName;
		String ip;
		String port;
		String servType;
		
		public CollectWork(String collectdID, String collectdName, String ip,
				String port, String servType) {
			super();
			this.collectdID   = collectdID;
			this.collectdName = collectdName;
			this.ip           = ip;
			this.port         = port;
			this.servType     = servType;
		}

		@Override
		public void run() {
			
			String url = String.format("http://%s:%s/%s", ip, port, CONSTS.COLLECT_DATA_API);
			
			try {
				String res = HttpUtils.getUrlData(url);
				
				if (HttpUtils.isNull(res)) {
					String err = String.format("CollectID:%s CollectName:%s ip:%s port:%s error when gathering data.",
							collectdID, collectdName, ip, port);
					logger.error(err);
				}
				
				JsonObject jsonObj = new JsonObject(res);
				CollectDataParser parser = null;
				switch (servType) {
				case CONSTS.SERV_TYPE_MQ:
					parser = new MQCollectDataParser(jsonObj);
					break;
				case CONSTS.SERV_TYPE_CACHE:
					parser = new CacheCollectDataParser(jsonObj);
					break;
				case CONSTS.SERV_TYPE_DB:
					parser = new TiDBCollectDataParser(jsonObj);
					break;
				}
				
				if (parser != null)
					parser.parseAndSave();
				
			} catch (Exception e) {
				String err = String.format("CollectID:%s CollectName:%s ip:%s port:%s error:%s",
						collectdID, collectdName, ip, port, e.getMessage());
				logger.error(err);
				
				// TODO touch off alarm event
				if (e instanceof IOException) {
					// 
				}
			}
			
		}
		
	}

}
