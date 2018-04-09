package ibsp.metaserver.global;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ibsp.metaserver.utils.CONSTS;


/**
 * 收集所有客户端上报数据
 */
public class ClientStatisticData {
	
	private static final long EXPIRED_TIME = 60*1000L;   // 一分钟都没有更新过则过期
	
	private Map<String, Long> dbClientMap = null; //db
	private Map<String, Long> cacheClientMap = null, cacheProxyMap = null; //cache
	private static Object mtx = null;
	private static ClientStatisticData theInstance = null;
	
	private ScheduledExecutorService expiredCheckerSESvr;
	private ExpiredDataChecker expiredChecker;

	
	static {
		mtx = new Object();
	}
	
	private ClientStatisticData(){
		//TODO put statistic info in maps
		dbClientMap = new ConcurrentHashMap<String, Long>();
		cacheClientMap = new ConcurrentHashMap<String, Long>();
		cacheProxyMap = new ConcurrentHashMap<String, Long>();
		
		expiredCheckerSESvr = Executors.newSingleThreadScheduledExecutor();
		expiredChecker = new ExpiredDataChecker();
		expiredCheckerSESvr.scheduleAtFixedRate(expiredChecker, 3L, 3L, TimeUnit.SECONDS);
	}
	
	public static ClientStatisticData get() {
		if (theInstance != null){
			return theInstance;
		}
		
		synchronized(mtx) {
			if (theInstance == null) {
				theInstance = new ClientStatisticData();
			}
		}
		return theInstance;
	}
	
	public void put(String type, String address) {
		switch (type) {
		case CONSTS.CLIENT_TYPE_CACHE:
			cacheClientMap.put(address, System.currentTimeMillis());
			break;
		case CONSTS.SERV_CACHE_PROXY:
			cacheProxyMap.put(address, System.currentTimeMillis());
			break;
		case CONSTS.CLIENT_TYPE_DB:
			dbClientMap.put(address, System.currentTimeMillis());
			break;
		default:
			break;
		}
	}

	public Set<String> getCacheClients() {
		return this.cacheClientMap.keySet();
	}
	
	private class ExpiredDataChecker implements Runnable {

		@Override
		public void run() {
			this.removeExpired(cacheClientMap);
			this.removeExpired(cacheProxyMap);
			this.removeExpired(dbClientMap);
		}
		
		private void removeExpired(Map<String, Long> map) {
			List<String> expiredList = new ArrayList<String>();
			
			Set<Entry<String, Long>> entrySet = map.entrySet();
			for (Entry<String, Long> entry : entrySet) {
				String key = entry.getKey();
				
				long currTs = System.currentTimeMillis();
				long diff   = currTs - entry.getValue();
				if (diff > EXPIRED_TIME) {
					expiredList.add(key);
				}
			}
			
			for (String key : expiredList) {
				map.remove(key);
			}
		}
		
	}
}
