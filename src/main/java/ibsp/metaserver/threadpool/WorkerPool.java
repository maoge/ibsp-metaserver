package ibsp.metaserver.threadpool;

import ibsp.metaserver.utils.SysConfig;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class WorkerPool {
	private static WorkerPool pool;
	private static Object mtx = null;
	
	static {
		mtx = new Object();
	}
	
	private final int corePoolSize;
	private final int maxPoolSize;
	private final int keepAliveTime;
	private final int workQueueLen;
	private final ThreadPoolExecutor poolExecutor;
	
	public static WorkerPool get() {
		if (pool != null){
			return pool;
		}
		
		synchronized(mtx) {
			if (pool == null) {
				pool = new WorkerPool();
			}
		}
		
		return WorkerPool.pool;
	}
	
	public WorkerPool() {
		corePoolSize  = SysConfig.get().getThreadPoolCoresize();
		maxPoolSize   = SysConfig.get().getThreadPoolMaxsize();
		keepAliveTime = SysConfig.get().getThreadPoolKeepalivetime();
		workQueueLen  = SysConfig.get().getThreadPoolWorkqueueLen();
		
		poolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
				keepAliveTime, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(workQueueLen));
	}
	
	public void execute(Runnable command) {
		poolExecutor.execute(command);
	}
	
}
