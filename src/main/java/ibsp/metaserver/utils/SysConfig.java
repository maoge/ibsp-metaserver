package ibsp.metaserver.utils;

public class SysConfig {
	
	private static SysConfig config = null;
	private static Object mtx = null;
	
	static {
		mtx = new Object();
	}

	public static SysConfig get() {
		if (config != null)
			return config;
		
		synchronized(mtx) {
			if (config == null) {
				config = new SysConfig();
			}
		}
		
		return SysConfig.config;
	}
	
	private int    web_api_port                   = 9990;
	private String dbsource_id                    = "ibsp";
	
	private boolean vertx_clustered               = true;
	private String  vertx_cluster_host            = "127.0.0.1";
	private int     vertx_cluster_port            = 29990;
	private long    max_event_loop_execute_time   = 30000;
	private int     vertx_evloopsize              = 32;
	private int     vertx_workerpoolsize          = 400;
	private final String vertx_sysevent_queuename = "sys.event";
	
	private int thread_pool_coresize              = 20;
	private int thread_pool_maxsize               = 40;
	private int thread_pool_keepalivetime         = 3;
	private int thread_pool_workqueue_len         = 1000;
	
	private boolean active_collect                = false;
	private int active_collect_interval           = 10000;
	private int active_collect_retry              = 3;
	private int active_collect_retry_interval     = 1000;
	private int alarm_time_window                 = 600000;
	
	private String redis_host                     = "127.0.0.1";
	private int redis_port                        = 6379;
	private String redis_auth                     = "";
	private int redis_pool_size                   = 5;
	
	private int  conn_highwater_mark              = 1000;
	private long mem_highwater_mark               = 10000000000L;            // 10G byte
	private	long diskreamin_highwater_mark        = 30000000000L;  // 磁盘剩余不少于 10G byte
	private int  msg_accumulate_highwater_mark    = 100000;        // 按队列, 消息堆积到一定数量触发告警阀值
	
	private long password_expire                  = 7776000; //密码过期时间
	
	private boolean check_blackwhite_list         = false;
	private boolean need_auth                     = true;
	
	private String eventbus_broker_ip             = "127.0.0.1";
	private int    eventbus_broker_port           = 6650;
	private String eventbus_consumer_subscription = "sub_001";
	
	public SysConfig() {
		this.web_api_port                   = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("web.api.port");
		this.dbsource_id                    = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).get("dbsource.id");
		
		this.vertx_clustered                = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getBoolean("vertx.clustered");
		this.vertx_cluster_host             = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).get("vertx.cluster.host");
		this.vertx_cluster_port             = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("vertx.cluster.port");
		this.vertx_evloopsize               = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("vertx.evloopsize");
		this.vertx_workerpoolsize           = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("vertx.workerpoolsize");
		
		this.max_event_loop_execute_time    = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getLong("vertx.option.maxEventLoopExecuteTime");
		
		this.thread_pool_coresize           = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("thread.pool.coresize");
		this.thread_pool_maxsize            = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("thread.pool.maxsize");
		this.thread_pool_keepalivetime      = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("thread.pool.keepalivetime");
		this.thread_pool_workqueue_len      = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("thread.pool.workqueue.len");
		
		this.active_collect                 = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getBoolean("active.collect");
		this.active_collect_interval        = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("active.collect.interval");
		this.active_collect_retry           = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("active.collect.retry");
		this.active_collect_retry_interval  = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("active.collect.retry.interval");
		this.alarm_time_window              = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("alarm.time.window");
		
		this.redis_host                     = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).get("redis.host");
		this.redis_port                     = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("redis.port");
		String authStr                      = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).get("redis.auth");
		this.redis_auth                     = DES3.decrypt(authStr);
		this.redis_pool_size                = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("redis.pool.size");
		
		this.eventbus_broker_ip             = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).get("eventbus.broker.ip", "127.0.0.1");
		this.eventbus_broker_port           = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("eventbus.broker.port", 6650);
		this.eventbus_consumer_subscription = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).get("eventbus.consumer.subscription", "sub_001");
		
		this.conn_highwater_mark            = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("connection.highwater.mark");
		this.mem_highwater_mark             = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getLong("memory.highwater.mark");
		this.diskreamin_highwater_mark      = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getLong("diskremain.highwater.mark");
		this.msg_accumulate_highwater_mark  = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("msg.accumulate.highwater.mark");
	
		this.password_expire                = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getInt("password.expire");
		this.check_blackwhite_list          = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getBoolean("check.blackwhite.list",Boolean.FALSE);
		this.need_auth                      = PropertiesUtils.getInstance(CONSTS.INIT_PROP_FILE).getBoolean("need.auth",Boolean.TRUE);
	}
	
	public int getWebApiPort() {
		return web_api_port;
	}

	public void setWebApiPort(int web_api_port) {
		this.web_api_port = web_api_port;
	}
	
	public String getDbsourceId() {
		return dbsource_id;
	}

	public void setDbsourceId(String dbsource_id) {
		this.dbsource_id = dbsource_id;
	}
	
	public boolean isVertxClustered() {
		return vertx_clustered;
	}

	public void setVertxClustered(boolean vertx_clustered) {
		this.vertx_clustered = vertx_clustered;
	}
	
	public String getVertxClusterHost() {
		return vertx_cluster_host;
	}

	public void setVertxClusterHost(String vertx_cluster_host) {
		this.vertx_cluster_host = vertx_cluster_host;
	}

	public int getVertxClusterPort() {
		return vertx_cluster_port;
	}

	public void setVertxClusterPort(int vertx_cluster_port) {
		this.vertx_cluster_port = vertx_cluster_port;
	}

	public long getMaxEventLoopExecuteTime() {
		return max_event_loop_execute_time;
	}

	public void setMaxEventLoopExecuteTime(long max_event_loop_execute_time) {
		this.max_event_loop_execute_time = max_event_loop_execute_time;
	}
	
	public String getVertxSysEventQueueName() {
		return vertx_sysevent_queuename;
	}
	
	public int getVertxEvLoopSize() {
		return vertx_evloopsize;
	}

	public void setVertxEvLoopSize(int vertx_evloopsize) {
		this.vertx_evloopsize = vertx_evloopsize;
	}

	public int getVertxWorkerPoolSize() {
		return vertx_workerpoolsize;
	}

	public void setVertxWorkerPoolSize(int vertx_workerpoolsize) {
		this.vertx_workerpoolsize = vertx_workerpoolsize;
	}

	//public void setVertxSysEventQueueName(String vertx_sysevent_queuename) {
	//	this.vertx_sysevent_queuename = vertx_sysevent_queuename;
	//}

	public int getThreadPoolCoresize() {
		return thread_pool_coresize;
	}

	public void setThreadPoolCoresize(int thread_pool_coresize) {
		this.thread_pool_coresize = thread_pool_coresize;
	}

	public int getThreadPoolMaxsize() {
		return thread_pool_maxsize;
	}

	public void setThreadPoolMaxsize(int thread_pool_maxsize) {
		this.thread_pool_maxsize = thread_pool_maxsize;
	}

	public int getThreadPoolKeepalivetime() {
		return thread_pool_keepalivetime;
	}

	public void setThreadPoolKeepalivetime(int thread_pool_keepalivetime) {
		this.thread_pool_keepalivetime = thread_pool_keepalivetime;
	}

	public int getThreadPoolWorkqueueLen() {
		return thread_pool_workqueue_len;
	}

	public void setThreadPoolWorkqueueLen(int thread_pool_workqueue_len) {
		this.thread_pool_workqueue_len = thread_pool_workqueue_len;
	}
	
	public String getEventBusBrokerIP() {
		return eventbus_broker_ip;
	}
	
	public int getEventBusBrokerPort() {
		return eventbus_broker_port;
	}
	
	public String getEventBusConsumerSubscription() {
		return eventbus_consumer_subscription;
	}
	
	public boolean isActiveCollect() {
		return active_collect;
	}

	public void setActiveCollect(boolean active_collect) {
		this.active_collect = active_collect;
	}

	public int getActiveCollectInterval() {
		return active_collect_interval;
	}

	public void setActiveCollectInterval(int active_collect_interval) {
		this.active_collect_interval = active_collect_interval;
	}

	public int getActiveCollectRetry() {
		return active_collect_retry;
	}

	public void setActiveCollectRetry(int active_collect_retry) {
		this.active_collect_retry = active_collect_retry;
	}

	public int getActiveCollectRetryInterval() {
		return active_collect_retry_interval;
	}

	public void setActiveCollectRetryInterval(int active_collect_retry_interval) {
		this.active_collect_retry_interval = active_collect_retry_interval;
	}

	public int getAlarmTimeWindow() {
		return alarm_time_window;
	}

	public void setAlarmTimeWindow(int alarm_time_window) {
		this.alarm_time_window = alarm_time_window;
	}

	public int getConnHighWaterMark() {
		return conn_highwater_mark;
	}

	public void setConnHighWaterMark(int conn_highwater_mark) {
		this.conn_highwater_mark = conn_highwater_mark;
	}

	public long getMemHighWaterMark() {
		return mem_highwater_mark;
	}

	public void setMemHighWaterMark(long mem_highwater_mark) {
		this.mem_highwater_mark = mem_highwater_mark;
	}

	public long getDiskRemainHighWaterMark() {
		return diskreamin_highwater_mark;
	}

	public void setDiskRemainHighWaterMark(long disk_highwater_mark) {
		this.diskreamin_highwater_mark = disk_highwater_mark;
	}

	public int getMsgAccumulateHighWaterMark() {
		return msg_accumulate_highwater_mark;
	}

	public void setMsgAccumulateHighWaterMark(int msg_accumulate_highwater_mark) {
		this.msg_accumulate_highwater_mark = msg_accumulate_highwater_mark;
	}

	public long getPassword_expire() {
		return password_expire;
	}

	public void setPassword_expire(long password_expire) {
		this.password_expire = password_expire;
	}

	public boolean isCheck_blackwhite_list() {
		return check_blackwhite_list;
	}

	public boolean isNeed_auth() {
		return need_auth;
	}

	public void setNeed_auth(boolean need_auth) {
		this.need_auth = need_auth;
	}

	public String getRedisHost() {
		return redis_host;
	}

	public void setRedisHost(String redis_host) {
		this.redis_host = redis_host;
	}

	public int getRedisPort() {
		return redis_port;
	}

	public void setRedisPort(int redis_port) {
		this.redis_port = redis_port;
	}

	public String getRedisAuth() {
		return redis_auth;
	}

	public void setRedisAuth(String redis_auth) {
		this.redis_auth = redis_auth;
	}

	public int getRedisPoolSize() {
		return redis_pool_size;
	}

	public void setRedisPoolSize(int redis_pool_size) {
		this.redis_pool_size = redis_pool_size;
	}

}
