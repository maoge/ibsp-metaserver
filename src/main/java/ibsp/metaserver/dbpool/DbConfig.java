package ibsp.metaserver.dbpool;

import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.PropertiesUtils;

public class DbConfig {
	
	private String configFile = CONSTS.C3P0_PROP_FILE;
	
	public DbConfig(String configFile) {
		this.configFile = configFile;
		
		id = PropertiesUtils.getInstance(configFile).get("id");
		driver = PropertiesUtils.getInstance(configFile).get("driver");
		url = PropertiesUtils.getInstance(configFile).get("url");
		username = PropertiesUtils.getInstance(configFile).get("username");
		password = PropertiesUtils.getInstance(configFile).get("password");
		
		maxPoolSize = PropertiesUtils.getInstance(configFile).getInt("maxPoolSize", 10);
		minPoolSize = PropertiesUtils.getInstance(configFile).getInt("minPoolSize", 1);
		initPoolSize = PropertiesUtils.getInstance(configFile).getInt("initPoolSize", 5);
		maxStatements = PropertiesUtils.getInstance(configFile).getInt("maxStatements", 100);
		maxIdleTime = PropertiesUtils.getInstance(configFile).getInt("maxIdleTime", 60*30);
		maxConnectionAge = PropertiesUtils.getInstance(configFile).getInt("maxConnectionAge", 60*60);  // 连接能存活的绝对时间
		checkoutTimeout = PropertiesUtils.getInstance(configFile).getInt("checkoutTimeout", 60);
		idleConnectionTestPeriod = PropertiesUtils.getInstance(configFile).getInt("idleConnectionTestPeriod", 60);
		unreturnedConnectionTimeout = PropertiesUtils.getInstance(configFile).getInt("unreturnedConnectionTimeout", 60);
	
		breakAfterAcquireFailure = PropertiesUtils.getInstance(configFile).getBoolean("breakAfterAcquireFailure", false);
		autoCommitOnClose = PropertiesUtils.getInstance(configFile).getBoolean("autoCommitOnClose", true);
		testConnectionOnCheckout = PropertiesUtils.getInstance(configFile).getBoolean("testConnectionOnCheckout", false);
		testConnectionOnCheckin = PropertiesUtils.getInstance(configFile).getBoolean("testConnectionOnCheckin", false);
		debugUnreturnedConnectionStackTraces = PropertiesUtils.getInstance(configFile).getBoolean("debugUnreturnedConnectionStackTraces", true);
		
		acquireIncrement = 1;
		acquireRetryAttempts = 10;
		acquireRetryDelay = 1000;
	}

	private String id;
	private String driver;
	private String url;
	private String username;
	private String password;
	
	private int maxPoolSize = 10;
	private int minPoolSize = 1;
	private int initPoolSize = 5;
	private int maxStatements = 100;
	private int maxIdleTime = 60*30;
	private int maxConnectionAge = 60*60;  // 连接能存活的绝对时间
	private int checkoutTimeout = 60;
	private int idleConnectionTestPeriod = 60;
	private int unreturnedConnectionTimeout = 60;
	
	private boolean breakAfterAcquireFailure = false;
	private boolean autoCommitOnClose = true;
	private boolean testConnectionOnCheckout = false;
	private boolean testConnectionOnCheckin = false;
	private boolean debugUnreturnedConnectionStackTraces = true;
	
	private int acquireIncrement = 1;
	private int acquireRetryAttempts = 10;
	private int acquireRetryDelay = 1;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public int getInitPoolSize() {
		return initPoolSize;
	}

	public void setInitPoolSize(int initPoolSize) {
		this.initPoolSize = initPoolSize;
	}

	public int getMaxStatements() {
		return maxStatements;
	}

	public void setMaxStatements(int maxStatements) {
		this.maxStatements = maxStatements;
	}

	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}

	public int getMaxConnectionAge() {
		return maxConnectionAge;
	}

	public void setMaxConnectionAge(int maxConnectionAge) {
		this.maxConnectionAge = maxConnectionAge;
	}

	public int getCheckoutTimeout() {
		return checkoutTimeout;
	}

	public void setCheckoutTimeout(int checkoutTimeout) {
		this.checkoutTimeout = checkoutTimeout;
	}

	public int getIdleConnectionTestPeriod() {
		return idleConnectionTestPeriod;
	}

	public void setIdleConnectionTestPeriod(int idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
	}

	public int getUnreturnedConnectionTimeout() {
		return unreturnedConnectionTimeout;
	}

	public void setUnreturnedConnectionTimeout(int unreturnedConnectionTimeout) {
		this.unreturnedConnectionTimeout = unreturnedConnectionTimeout;
	}

	public boolean isBreakAfterAcquireFailure() {
		return breakAfterAcquireFailure;
	}

	public void setBreakAfterAcquireFailure(boolean breakAfterAcquireFailure) {
		this.breakAfterAcquireFailure = breakAfterAcquireFailure;
	}

	public boolean isAutoCommitOnClose() {
		return autoCommitOnClose;
	}

	public void setAutoCommitOnClose(boolean autoCommitOnClose) {
		this.autoCommitOnClose = autoCommitOnClose;
	}

	public boolean isTestConnectionOnCheckout() {
		return testConnectionOnCheckout;
	}

	public void setTestConnectionOnCheckout(boolean testConnectionOnCheckout) {
		this.testConnectionOnCheckout = testConnectionOnCheckout;
	}

	public boolean isTestConnectionOnCheckin() {
		return testConnectionOnCheckin;
	}

	public void setTestConnectionOnCheckin(boolean testConnectionOnCheckin) {
		this.testConnectionOnCheckin = testConnectionOnCheckin;
	}

	public boolean isDebugUnreturnedConnectionStackTraces() {
		return debugUnreturnedConnectionStackTraces;
	}

	public void setDebugUnreturnedConnectionStackTraces(
			boolean debugUnreturnedConnectionStackTraces) {
		this.debugUnreturnedConnectionStackTraces = debugUnreturnedConnectionStackTraces;
	}

	public int getAcquireIncrement() {
		return acquireIncrement;
	}

	public void setAcquireIncrement(int acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}

	public int getAcquireRetryAttempts() {
		return acquireRetryAttempts;
	}

	public void setAcquireRetryAttempts(int acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}

	public int getAcquireRetryDelay() {
		return acquireRetryDelay;
	}

	public void setAcquireRetryDelay(int acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

}
