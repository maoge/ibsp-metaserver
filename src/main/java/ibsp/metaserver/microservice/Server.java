package ibsp.metaserver.microservice;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;
import ibsp.metaserver.dbpool.DbSource;
import ibsp.metaserver.eventbus.SysEventHandler;
import ibsp.metaserver.global.ServiceData;
import ibsp.metaserver.microservice.handler.*;
import ibsp.metaserver.monitor.ActiveCollect;
import ibsp.metaserver.singleton.AllServiceMap;
import ibsp.metaserver.singleton.ServiceStatInfo;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.SysConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends AbstractVerticle {
	
	private static Logger logger = LoggerFactory.getLogger(Server.class);
	private static JsonObject rejectJson;
	private static JsonObject errJson;
	private static JsonObject ipLimitJosn;
	
	static {
		rejectJson = new JsonObject();
		rejectJson.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_AUTH_FAIL);
		rejectJson.put(FixHeader.HEADER_RET_INFO, "not authorized or session is timeout, service call reject!");
		
		errJson = new JsonObject();
		errJson.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_NOK);
		errJson.put(FixHeader.HEADER_RET_INFO, "internal error!");
		
		ipLimitJosn = new JsonObject();
		ipLimitJosn.put(FixHeader.HEADER_RET_CODE, CONSTS.REVOKE_AUTH_IP_LIMIT);
		ipLimitJosn.put(FixHeader.HEADER_RET_INFO, "ip limit !");
	}
	
	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		
		SharedData sharedData = vertx.sharedData();
		ServiceData.get().setSharedData(sharedData);
		
		Vector<Class<?>> clazzToReg = new Vector<Class<?>>();
		clazzToReg.add(MetaServerHandler.class);
		clazzToReg.add(ConfigServerHandler.class);
		clazzToReg.add(AutoDeployHandler.class);
		clazzToReg.add(TiDBHandler.class);
		clazzToReg.add(CacheHandler.class);
		clazzToReg.add(MQHandler.class);
		clazzToReg.add(CollectDataHandler.class);
		clazzToReg.add(ResourceServerHandler.class);
		clazzToReg.add(MetaHandle.class);
		clazzToReg.add(TiDBMericsHandle.class);
		
		registerRoute(router, clazzToReg);
		
		int port = SysConfig.get().getWebApiPort();
		HttpServer server = vertx.createHttpServer();
		
		server.requestHandler(router::accept).listen(port, res -> {
			ServiceData.get().setHttpServer(server);
			
			if (res.succeeded()) {
				registerEventBus();
				logger.info("http server listen on port:{} succeeded!", port);
			} else {
				Throwable t = res.cause();
				logger.error("http server listen on port:{} failed, reason:{}", port, t);
			}
		});
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
		
		if (SysConfig.get().isVertxClustered()) {
			ServiceData.get().getSysEvMsgConsumer().unregister(res -> {
				if (res.succeeded()) {
					logger.info("unregister msgConsumer success!");
				} else {
					logger.error("unregister msgConsumer fail!");
				}
			});
		}
		
		if (SysConfig.get().isActiveCollect()) {
			ActiveCollect.get().Stop();
		}
		
		ServiceData.get().getHttpServer().close();
		DbSource.close();
		
		logger.info("http server listen on port:{} stopped!", SysConfig.get().getWebApiPort());
	}
	
	private void registerRoute(Router router, Vector<Class<?>> clazzToReg) {

		for (Class<?> clazz : clazzToReg) {
			App app = clazz.getAnnotation(App.class);
			String rootPath = app.path();
			if (!rootPath.endsWith(CONSTS.PATH_SPLIT))
				rootPath += CONSTS.PATH_SPLIT;
			
			AllServiceMap serviceMap = AllServiceMap.get();

			Method[] methods = clazz.getMethods();
			for (Method m : methods) {
				if (!Modifier.isStatic(m.getModifiers())
						|| !Modifier.isPublic(m.getModifiers())) {
					continue;
				}

				if (!m.isAnnotationPresent(Service.class)) {
					continue;
				}

				Service s = m.getAnnotation(Service.class);
				String path = rootPath + s.id();
				serviceMap.add(path, s);
				
				router.route(path).handler(BodyHandler.create());
				router.route(path).handler(routingContext -> {
					try {
						// entLoop 阻塞方式
						//m.invoke(null, routingContext);
						
						// EventLoop 非阻塞, 操作扔到线程池完成
						routingContext.vertx().executeBlocking(future -> {
							try {
								
								if (SysConfig.get().isNeed_auth()) {
									if (!doAuth(routingContext)) {
										rejectServiceCall(routingContext);
										future.complete();
										return;
									}
								}
								if(SysConfig.get().isCheck_blackwhite_list()) {
									if(!doIpCheck(routingContext)) {
										doIpLimitError(routingContext);
										future.complete();
										return;
									}
								}
								
								// service call statistic
								doStatistic(routingContext);
								
								m.invoke(null, routingContext);
								future.complete();
							} catch (Exception e) {
								doError(routingContext);
								future.complete();
								logger.error(e.getMessage(), e);
							}
						}, false, null);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				});

			}
		}

	}
	
	private void registerEventBus() {
		EventBus eb = vertx.eventBus();
		ServiceData.get().setEventBus(eb);
		
		String sysEvQueue = SysConfig.get().getVertxSysEventQueueName();
		MessageConsumer<String> sysEvConsumer = eb.consumer(sysEvQueue);
		SysEventHandler handle = new SysEventHandler();
		sysEvConsumer.handler(handle);
		
		sysEvConsumer.completionHandler(res -> {
			if (res.succeeded()) {
				ServiceData.get().setSysEvMsgConsumer(sysEvConsumer);
				logger.info("eventbus register:{} ok!", sysEvQueue);
			} else {
				logger.info("eventbus register:{} fail!", sysEvQueue);
				
				Throwable t = res.cause();
				logger.error(t.getMessage(), t);
			}
		});
	}
	
	/*private boolean doAuth(RoutingContext routingContext) throws InterruptedException, ExecutionException, TimeoutException {
		HttpServerRequest request = routingContext.request();
		HttpMethod method = request.method();
		
		String path = request.path();
		Service s = AllServiceMap.get().find(path);
		if (s == null)
			return false;
		
		if (!s.auth())
			return true;   // 不需要认证的api直接pass
		
		MultiMap attrMap = method.equals(HttpMethod.POST) ? request.formAttributes() : request.params();
		if (attrMap == null)
			return false;
		
		String key = attrMap.get(FixHeader.HEADER_MAGIC_KEY);
		if (key == null)
			return false;
		
		return GlobalData.get().isMagicKeyExists(key);
	}*/
	private boolean doAuth(RoutingContext routingContext) throws InterruptedException, ExecutionException, TimeoutException {
		return true;
	}
	
	/*private boolean doIpCheck(RoutingContext routingContext) {
		HttpServerRequest request = routingContext.request();
		HttpMethod method = request.method();
		
		MultiMap attrMap = method.equals(HttpMethod.POST) ? request.formAttributes() : request.params();
		
		String key = attrMap.get(FixHeader.HEADER_MAGIC_KEY);
		String userId = attrMap.get(FixHeader.HEADER_USER_ID);
		//路径是test的直接通过
		String path = request.path();
		Service s = AllServiceMap.get().find(path);
		if(!s.bwswitch()) {
			return Boolean.TRUE;
		}
		if(HttpUtils.isNull(key)) {
			//如果url是前台页面的登录url或者是客户端的登录。 就直接判断user_id这个参数是不是系统管理员用户
			if(userId != null && GlobalData.get().isAdmin(userId)) {
				return Boolean.TRUE;
			}
		}
		
		boolean isAdmin =  GlobalData.get().isAdminByKey(key);
		if(isAdmin) {
			return Boolean.TRUE;
		}
		
		String ip = request.remoteAddress().host();
		return GlobalData.get().isIpPass(ip);
	}*/
	private boolean doIpCheck(RoutingContext routingContext) {
		return true;
	}
	
	private void doStatistic(RoutingContext routingContext) {
		ServiceStatInfo.get().inc(routingContext.request().path());
	}
	
	private void rejectServiceCall(RoutingContext routingContext) {
		HttpUtils.outJsonObject(routingContext, rejectJson);
	}
	
	private void doError(RoutingContext routingContext) {
		HttpUtils.outJsonObject(routingContext, errJson);
	}
	
	private void doIpLimitError(RoutingContext routingContext) {
		HttpUtils.outJsonObject(routingContext, ipLimitJosn);
	}

}
