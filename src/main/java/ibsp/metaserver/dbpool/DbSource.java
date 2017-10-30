package ibsp.metaserver.dbpool;

import ibsp.metaserver.exception.CRUDException;
import ibsp.metaserver.exception.CRUDException.CRUDERRINFO;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.SysConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSource {
	
	private static Logger logger = LoggerFactory.getLogger(DbSource.class);

	private static DbSource theInstance = null;
	
	private static Object mtx = new Object();
	private ConcurrentHashMap<String, ConnectionPool> validDBMap = null;
	private ConcurrentHashMap<String, ConnectionPool> invalidDBMap = null;
	private List<String> validIdList;
	private List<String> invalidIdList;
	private long index = 0L;
	
	private static Thread checkThread = null;
	private static DBPoolRecoveryChecker checker = null;
	private static volatile boolean isCheckerRunning = false;
	
	public DbSource() {
		validDBMap = new ConcurrentHashMap<String, ConnectionPool>();
		invalidDBMap = new ConcurrentHashMap<String, ConnectionPool>();
		validIdList = new ArrayList<String>();
		invalidIdList = new ArrayList<String>();
	}
	
	public static DbSource get() {
		if (theInstance != null) {
			return theInstance;
		}
		
		synchronized(mtx) {
			if (theInstance == null) {
				theInstance = new DbSource();
				
				String dbSourceIDs = SysConfig.get().getDbsourceId();
				String[] ids = dbSourceIDs.split(CONSTS.PATH_COMMA);
				if (ids == null || ids.length == 0) {
					logger.error("datasource is null!");
				} else {
					Set<String> idSet = new HashSet<String>();
					for (String id : ids) {
						idSet.add(id);
					}
					
					for (String id : idSet) {
						DbPoolImpl connPool = new DbPoolImpl(id);
						
						if (connPool.check()) {
							theInstance.validDBMap.put(id, connPool);
							theInstance.validIdList.add(id);
						} else {
							theInstance.invalidDBMap.put(id, connPool);
							theInstance.invalidIdList.add(id);
						}
					}
				}
			}
		}
		
		return theInstance;
	}
	
	public static ConnectionPool getPool() throws CRUDException {
		DbSource dbsource = DbSource.get();
		if (dbsource.validIdList.size() == 0) {
			throw new CRUDException("db source is empty", new Throwable(), CRUDERRINFO.e1);
		}
		
		ConnectionPool connPool = null;
		synchronized(mtx) {
			int seed = (int) (dbsource.index++ % dbsource.validIdList.size());
			String id = dbsource.validIdList.get(seed);
			connPool = dbsource.validDBMap.get(id);
		}
		
		return connPool;
	}
	
	public static void close() {
		isCheckerRunning = false;
		stopChecker();
		
		DbSource dbsource = DbSource.get();
		synchronized(mtx) {
			if (dbsource.validDBMap != null) {
				Set<Entry<String, ConnectionPool>> entrySet = dbsource.validDBMap.entrySet();
				for (Entry<String, ConnectionPool> entry : entrySet) {
					ConnectionPool connPool = entry.getValue();
					if (connPool == null) {
						continue;
					}
					try {
						connPool.close();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			
			if (dbsource.invalidDBMap != null) {
				Set<Entry<String, ConnectionPool>> entrySet = dbsource.invalidDBMap.entrySet();
				for (Entry<String, ConnectionPool> entry : entrySet) {
					ConnectionPool connPool = entry.getValue();
					if (connPool == null) {
						continue;
					}
					try {
						connPool.close();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			
			dbsource.validIdList.clear();
			dbsource.validDBMap.clear();
			
			dbsource.invalidIdList.clear();
			dbsource.invalidDBMap.clear();
		}
	}
	
	public static void removeBrokenPool(String id) {
		DbSource dbsource = DbSource.get();
		synchronized(mtx) {
			if (dbsource.validIdList.contains(id)) {
				dbsource.validIdList.remove(id);
				ConnectionPool connPool = dbsource.validDBMap.remove(id);
				if (connPool != null) {
					logger.info("db pool:{} broken ......", id);
					
					dbsource.invalidDBMap.put(id, connPool);
					
					if (!dbsource.invalidIdList.contains(id)) {
						dbsource.invalidIdList.add(id);
					}
					
					if (dbsource.invalidDBMap.size() > 0) {
						startChecker();
					}
				}
			}
		}
	}
	
	public static void mergeRecoveredPool(String id) {
		DbSource dbsource = DbSource.get();
		synchronized(mtx) {
			if (dbsource.invalidIdList.contains(id)) {
				dbsource.invalidIdList.remove(id);
				ConnectionPool connPool = dbsource.invalidDBMap.remove(id);
				if (connPool != null) {
					logger.info("db pool:{} recovered ......", id);
					
					dbsource.validDBMap.put(id, connPool);
					
					if (!dbsource.validIdList.contains(id)) {
						dbsource.validIdList.add(id);
					}
					
					if (dbsource.invalidDBMap.size() == 0) {
						stopChecker();
					}
				}
			}
		}
	}
	
	public static void startChecker() {
		if (isCheckerRunning)
			return;
		
		isCheckerRunning = true;
		checker = new DBPoolRecoveryChecker();
		checkThread = new Thread(checker);
		checkThread.start();
	}
	
	public static void stopChecker() {
		if (!isCheckerRunning)
			return;
		
		isCheckerRunning = false;
		if (checker != null) {
			checker.stopRunning();
			
			checker = null;
			checkThread = null;
		}
	}
	
	private static class DBPoolRecoveryChecker implements Runnable {
		
		private volatile boolean running = false;
		
		public DBPoolRecoveryChecker() {
			
		}

		@Override
		public void run() {
			running = true;
			
			while (running) {
				DbSource dbsource = DbSource.get();
				int size = dbsource.invalidDBMap.size();
				if (size > 0) {
					String id = dbsource.invalidIdList.get(0);
					logger.info("DBSource Checking:{} ......", id);
					DbPoolImpl connPool = (DbPoolImpl)dbsource.invalidDBMap.get(id);
					if (connPool.check()) {
						DbSource.mergeRecoveredPool(id);
					}
				}
				
				try {
					Thread.sleep(CONSTS.DBPOOL_CHECK_INTERVAL);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		
		public void stopRunning() {
			running = false;
		}
		
	}
	
}
