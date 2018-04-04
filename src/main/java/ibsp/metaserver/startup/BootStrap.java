package ibsp.metaserver.startup;

import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.PropertiesUtils;
import ibsp.metaserver.utils.SysConfig;
import ibsp.metaserver.microservice.Server;
import ibsp.metaserver.monitor.ActiveCollect;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.dbpool.DbSource;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.ServiceData;
import ibsp.metaserver.threadpool.WorkerPool;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;

public class BootStrap {
	
	private static Logger logger = LoggerFactory.getLogger(BootStrap.class);

	public static void main(String[] args) {
		bootstrap();
	}
	
	public static void bootstrap() {
		bootLoggerConfig();
		bootSingleInstance();
		bootMicroService();
	}
	
	private static void bootLoggerConfig() {
		PropertyConfigurator.configure(PropertiesUtils.getInstance(CONSTS.LOG4J_CONF).getProperties());
	}
	
	private static void bootSingleInstance() {
		SysConfig.get();
		WorkerPool.get();
		DbSource.get();
		MetaData.get();
		ServiceData.get();
		ActiveCollect.get();
	}
	
	private static void bootMicroService() {
		int nEvLoopPoolSize = SysConfig.get().getVertxEvLoopSize();
		int nWorkerPoolSize = SysConfig.get().getVertxWorkerPoolSize();
		
		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setMaxEventLoopExecuteTime(SysConfig.get().getMaxEventLoopExecuteTime());
		vertxOptions.setEventLoopPoolSize(nEvLoopPoolSize);
		vertxOptions.setWorkerPoolSize(nWorkerPoolSize);
		
		DeploymentOptions deployOptions = new DeploymentOptions();
		deployOptions.setWorker(true);
		
		if (SysConfig.get().isVertxClustered()) {
			vertxOptions.setClustered(true);
			vertxOptions.setClusterHost(SysConfig.get().getVertxClusterHost());
			vertxOptions.setClusterPort(SysConfig.get().getVertxClusterPort());
						
			Config cfg = null;
			
			cfg = new XmlConfigBuilder(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(CONSTS.HAZELCAST_CONF_FILE)).build();

			ClusterManager mgr = new HazelcastClusterManager(cfg);
			vertxOptions.setClusterManager(mgr);
			
			Vertx.clusteredVertx(vertxOptions, res -> {
				if (res.succeeded()) {
					Vertx vertx = res.result();
					vertx.deployVerticle(new Server(), deployOptions);  // new DeploymentOptions().setWorker(true)
				} else {
					logger.error("create cluster vert.x error, cause:{}", res.cause());
					System.exit(0);
				}
			});
		} else {
			Vertx vertx = Vertx.vertx(vertxOptions);
			vertx.deployVerticle("com.ffcs.mq.startup.Server", deployOptions);  // new Server()
		}
	}

}
