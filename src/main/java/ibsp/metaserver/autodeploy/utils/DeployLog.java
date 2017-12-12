package ibsp.metaserver.autodeploy.utils;

import ibsp.metaserver.utils.CONSTS;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeployLog {
	
	private static final long EXPIRE_TIME = 10*1000*60;    // 过期时间
	private ConcurrentHashMap<String, LogBean> logMap;
	
	private static ThreadPoolExecutor expiredExecutor;
	private static BlockingQueue<Runnable> expiredWorksQueue;
	private Runnable expiredCleaner;
	
	private static DeployLog INSTANCE = null;
	
	static {
		expiredWorksQueue = new ArrayBlockingQueue<Runnable>(10);
		expiredExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, expiredWorksQueue, new ThreadPoolExecutor.DiscardPolicy());
	}
	
	public DeployLog() {
		logMap = new ConcurrentHashMap<String, LogBean>();
		expiredCleaner = new ExpiredCleaner();
		expiredExecutor.execute(expiredCleaner);
	}
	
	public static DeployLog getInstance() {
		if (DeployLog.INSTANCE == null) {
			INSTANCE = new DeployLog();
		}
		
		return DeployLog.INSTANCE;
	}
	
	private void putLogBean(String sessionKey, LogBean bean) {
		logMap.put(sessionKey, bean);
	}
	
	private static ConcurrentHashMap<String, LogBean> getLogMap() {
		DeployLog instance = DeployLog.getInstance();
		return instance.logMap;
	}
	
	private static LogBean getLogBean(String sessionKey) {
		ConcurrentHashMap<String, LogBean> instance = DeployLog.getLogMap();
		return instance.get(sessionKey);
	}
	
	public static void pubLog(String sessionKey, String log) {
		if (sessionKey == null)
			return;
		
		final LogBean logBean = DeployLog.getLogBean(sessionKey);
		
		String newLog = log.replaceAll(CONSTS.LINE_END, CONSTS.HTML_LINE_END);
		
		if (logBean == null) {
			LogBean newLogBean = new LogBean();
			newLogBean.putLog(newLog);
			
			DeployLog instance = DeployLog.getInstance();
			instance.putLogBean(sessionKey, newLogBean);
		} else {
			logBean.putLog(newLog);
		}
	}
	
	public static void pubSuccessLog(String sessionKey, String log) {
		StringBuffer logSB = new StringBuffer();
		logSB.append(CONSTS.DEPLOY_SINGLE_SUCCESS_BEGIN_STYLE);
		logSB.append(log);
		logSB.append(CONSTS.END_STYLE);
		pubLog(sessionKey, logSB.toString());
	}
	
	public static void pubErrorLog(String sessionKey, String log) {
		StringBuffer logSB = new StringBuffer();
		logSB.append(CONSTS.DEPLOY_SINGLE_FAIL_BEGIN_STYLE);
		logSB.append(log);
		logSB.append(CONSTS.END_STYLE);
		pubLog(sessionKey, logSB.toString());
		
	}
	
	public static String getLog(String sessionKey) {
		final LogBean logBean = DeployLog.getLogBean(sessionKey);
		if (logBean == null) {
			return "";
		} else {
			return logBean.getLog();
		}
	}
	
	private static void elimExpired() {
		final ConcurrentHashMap<String, LogBean> logMap = DeployLog.getLogMap();
		Iterator<Map.Entry<String, LogBean>> iter = logMap.entrySet().iterator();
		
		long ts = System.currentTimeMillis();
		List<String> removeList = new LinkedList<String>();
		
		while (iter.hasNext()) {
			Map.Entry<String, LogBean> entry = iter.next();
			String key = entry.getKey();
			LogBean logBean = entry.getValue();
			
			if (ts - logBean.getLastTimestamp() > EXPIRE_TIME) {
				logBean.clear();
				removeList.add(key);
			}
		}
		
		if (!removeList.isEmpty()) {
			Iterator<String> removeIter = removeList.iterator();
			while (removeIter.hasNext()) {
				String key = removeIter.next();
				logMap.remove(key);
			}
		}
		
		removeList.clear();
	}
	
	private static class ExpiredCleaner implements Runnable {
		
		public ExpiredCleaner() {
			super();
		}

		@Override
		public void run() {
			DeployLog.elimExpired();
		}
		
	}
	
}
