package ibsp.metaserver.global;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ibsp.metaserver.bean.IBSPClientInfo;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.HttpUtils;


/**
 * 收集所有客户端上报数据
 */
public class ClientStatisticData {
	
	private static final long EXPIRED_TIME = 60*1000L;   // 一分钟都没有更新过则过期
	
	private Map<String, Map<String, IBSPClientInfo>> dbClientMap = null; //db
	private Map<String, Map<String, IBSPClientInfo>> cacheClientMap = null, cacheProxyMap = null; //cache
	private Map<String, Map<String, IBSPClientInfo>> mqClientMap = null; //mq
	private static Object mtx = null;
	private static ClientStatisticData theInstance = null;
	
	private ScheduledExecutorService expiredCheckerSESvr;
	private ExpiredDataChecker expiredChecker;

	
	static {
		mtx = new Object();
	}
	
	private ClientStatisticData(){
		dbClientMap = new ConcurrentHashMap<String, Map<String, IBSPClientInfo>>();
		cacheClientMap = new ConcurrentHashMap<String, Map<String, IBSPClientInfo>>();
		cacheProxyMap = new ConcurrentHashMap<String, Map<String, IBSPClientInfo>>();
		mqClientMap = new ConcurrentHashMap<String, Map<String, IBSPClientInfo>>();
		
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
	
	public void put(String type, String address, String servID) {
		Map<String, Map<String, IBSPClientInfo>> mainMap = null;
		switch (type) {
		case CONSTS.CLIENT_TYPE_CACHE:
			mainMap = cacheClientMap;
			break;
		case CONSTS.SERV_CACHE_PROXY:
			mainMap = cacheProxyMap;
			break;
		case CONSTS.CLIENT_TYPE_DB:
			mainMap = dbClientMap;
			break;
		case CONSTS.CLIENT_TYPE_MQ:
			mainMap = mqClientMap;
			break;
		default:
			break;
		}
		
		if (mainMap == null)
			return;
		
		Map<String, IBSPClientInfo> subMap = mainMap.get(servID);
		if (subMap == null) {
			mainMap.put(servID, new ConcurrentHashMap<String, IBSPClientInfo>());
		}
		
		IBSPClientInfo clientInfo = subMap.get(address);
		if (clientInfo == null) {
			clientInfo = new IBSPClientInfo(address, servID, type, System.currentTimeMillis());
			subMap.put(address, clientInfo);
		} else {
			clientInfo.refresh();
		}
	}

	public Set<String> getCacheClients() {
		return this.cacheClientMap.keySet();
	}
	
	public Set<String> getCacheProxies() {
		return this.cacheProxyMap.keySet();
	}

	public Set<String> getCacheProxies(String servId) {
		List<InstanceDtlBean> proxies = MetaData.get().getCacheProxysByServId(servId);
		Set<String> res = new HashSet<>();
		for(InstanceDtlBean proxy : proxies) {
			if(cacheProxyMap.containsKey(proxy.getInstID())) {
				res.add(proxy.getInstID());
			}
		}
		return res;
	}
	
	public Set<String> getDbClients() {
		return this.dbClientMap.keySet();
	}
	
	public Set<String> getMqClients() {
		return this.mqClientMap.keySet();
	}
	
	public Set<String> getClients(String type, String servId) {
		Set<String> clientSet = new HashSet<String>();
		Map<String, Map<String, IBSPClientInfo>> mainMap = null;
		switch (type) {
		case CONSTS.CLIENT_TYPE_CACHE:
			mainMap = cacheClientMap;
			break;
		case CONSTS.SERV_CACHE_PROXY:
			mainMap = cacheProxyMap;
			break;
		case CONSTS.CLIENT_TYPE_DB:
			mainMap = dbClientMap;
			break;
		case CONSTS.CLIENT_TYPE_MQ:
			mainMap = mqClientMap;
			break;
		default:
			break;
		}
		
		if (mainMap == null)
			return clientSet;
		
		Map<String, IBSPClientInfo> clientInfoMap = mainMap.get(servId);
		if (clientInfoMap == null)
			return clientSet;
		
		Set<Entry<String, IBSPClientInfo>> entrySet = clientInfoMap.entrySet();
		for (Entry<String, IBSPClientInfo> entry : entrySet) {
			IBSPClientInfo clientInfo = entry.getValue();
			if (clientInfo == null || HttpUtils.isNull(clientInfo.getAddress()))
				continue;
			
			clientSet.add(clientInfo.getAddress());
		}
		
		return clientSet;
	}
	
	private class ExpiredDataChecker implements Runnable {

		@Override
		public void run() {
			this.removeExpired(cacheClientMap);
			this.removeExpired(cacheProxyMap);
			this.removeExpired(dbClientMap);
			this.removeExpired(mqClientMap);
		}
		
		private void removeExpired(Map<String, Map<String, IBSPClientInfo>> map) {
			List<String> expiredList = new ArrayList<String>();
			long currTs = System.currentTimeMillis();
			
			Set<Entry<String, Map<String, IBSPClientInfo>>> mainEntrySet = map.entrySet();
			for (Entry<String, Map<String, IBSPClientInfo>> mainEntry : mainEntrySet) {
				Map<String, IBSPClientInfo> subMap = mainEntry.getValue();
				
				Set<Entry<String, IBSPClientInfo>> subEntrySet = subMap.entrySet();
				for (Entry<String, IBSPClientInfo> subEntry : subEntrySet) {
					String key = subEntry.getKey();
					IBSPClientInfo clientInfo = subEntry.getValue();
					long diff   = currTs - clientInfo.getRefreshTS();
					if (diff > EXPIRED_TIME) {
						expiredList.add(key);
					}
				}
				
				for (String key : expiredList) {
					subMap.remove(key);
				}
				expiredList.clear();
			}
		}
		
	}
}
