package ibsp.metaserver.dbpool;

import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.DES3;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ConnectionModel {
	
	private static final Logger logger = LoggerFactory.getLogger(ConnectionModel.class);
	
	private String configFile = CONSTS.C3P0_PROP_FILE;
	private boolean isOpen = false;
	
	private static final String PREFERRED_TEST_QUERY = "select 1 from dual"; 
	
	private ComboPooledDataSource dataSource = null;
	
	public ConnectionModel(String configFile) {
		this.configFile = configFile;
		initDataSource();
	}
	
	private boolean initDataSource() {
		DbConfig config = new DbConfig(configFile);
		
		dataSource = new ComboPooledDataSource();
		
		try {
			dataSource.setDriverClass(config.getDriver());
		} catch (PropertyVetoException e) {
			logger.error(e.getMessage(), e);
		}
		
		dataSource.setJdbcUrl(config.getUrl());
		dataSource.setUser(config.getUsername());
		dataSource.setPassword(DES3.decrypt(config.getPassword()));
		
		dataSource.setMaxPoolSize(config.getMaxPoolSize());
		dataSource.setMinPoolSize(config.getMinPoolSize());
		dataSource.setInitialPoolSize(config.getInitPoolSize());
		dataSource.setMaxStatements(config.getMaxStatements());
		dataSource.setMaxIdleTime(config.getMaxIdleTime());
		dataSource.setMaxConnectionAge(config.getMaxConnectionAge());
		dataSource.setCheckoutTimeout(config.getCheckoutTimeout());
		dataSource.setIdleConnectionTestPeriod(config.getIdleConnectionTestPeriod());
		dataSource.setUnreturnedConnectionTimeout(config.getUnreturnedConnectionTimeout());
		
		dataSource.setBreakAfterAcquireFailure(config.isBreakAfterAcquireFailure());
		dataSource.setAutoCommitOnClose(config.isAutoCommitOnClose());
		dataSource.setTestConnectionOnCheckout(config.isTestConnectionOnCheckout());
		dataSource.setTestConnectionOnCheckin(config.isTestConnectionOnCheckin());
		dataSource.setDebugUnreturnedConnectionStackTraces(config.isDebugUnreturnedConnectionStackTraces());
		
		dataSource.setAcquireIncrement(config.getAcquireIncrement());
		dataSource.setAcquireRetryAttempts(config.getAcquireRetryAttempts());
		dataSource.setAcquireRetryDelay(config.getAcquireRetryDelay());
		
		dataSource.setPreferredTestQuery(PREFERRED_TEST_QUERY);
		
		isOpen = true;
		
		return isOpen;
	}
	
	public Connection getConnection() throws SQLException, PropertyVetoException {
		if (dataSource == null) {
			initDataSource();
		}
		
		Connection conn = null;
		if (dataSource != null) {
			conn = dataSource.getConnection();
		}
		
		return conn;
	}
	
	public Connection newConnection() {
		Connection conn = null;
		try {
			return getConnection();
		} catch (Exception ex) {
			logger.error("Can not create a connection to " + dataSource.getJdbcUrl(), ex);
		}		
		return conn;
	}
	
	public String getTestQuery() {
		return PREFERRED_TEST_QUERY;
	}
	
	public void close() throws SQLException {
		if (dataSource != null && isOpen) {
			dataSource.close();
			isOpen = false;
		}
	}

}
