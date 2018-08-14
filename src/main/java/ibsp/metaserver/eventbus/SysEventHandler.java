package ibsp.metaserver.eventbus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

import ibsp.metaserver.global.MonitorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.CacheService;
import ibsp.metaserver.global.ClientStatisticData;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.threadpool.WorkerPool;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class SysEventHandler implements Handler<Message<String>> {
	
	private static Logger logger = LoggerFactory.getLogger(SysEventHandler.class);

	@Override
	public void handle(Message<String> message) {
		String msgBody = message.body();
		logger.debug("have received message:{}", msgBody);
		
		if (msgBody != null && !msgBody.equals("")) {
			SysEventRunner runner = new SysEventRunner(msgBody);
			WorkerPool.get().execute(runner);
		} else {
			logger.error("Event Message body null ......");
		}
	}
	
	private static class SysEventRunner implements Runnable {
		
		String msg;
		
		public SysEventRunner(String msg) {
			this.msg = msg;
		}

		@Override
		public void run() {
			JsonObject jsonObj = new JsonObject(msg);
			if (jsonObj.isEmpty()) {
				return;
			}
			
			int eventCode       = jsonObj.getInteger(FixHeader.HEADER_EVENT_CODE);
			String servId       = jsonObj.getString(FixHeader.HEADER_SERV_ID);
			String uuid         = jsonObj.getString(FixHeader.HEADER_UUID);
			String jsonStr      = jsonObj.getString(FixHeader.HEADER_JSONSTR);
			EventType type      = EventType.get(eventCode);
			JsonObject json     = new JsonObject(jsonStr);

			/*if (eventCode != EventType.e98.getValue())
				logger.info("Received event type " + eventCode);*/
			
			switch(type) {
			case e1:
			case e2:
				MetaData.get().doTopo(json, type);
				break;
			case e3:
			case e4:
			case e5:
				MetaData.get().doInstance(json, type);
				break;
			case e6:
			case e7:
			case e8:
				MetaData.get().doService(json, type);
				break;
			case e9:
			case e10:
			case e11:
				MetaData.get().doQueue(json, type);
				break;
			case e12:
			case e13:
				MetaData.get().doPermnentTopic(json, type);
				break;

			case e14:
			case e15:
				MetaData.get().doServer(json, type);
				break;

				case e21:
			case e22:
				MetaData.get().doServiceDeploy(json, type);
				break;
			case e23:
			case e24:
				MetaData.get().doInstanceDeploy(json, type);
				break;
			
			//卸载MQSerivce时候，把所有队列和topic删除
			case e31:
				if (uuid.equals(MetaData.get().getUUID())) {
					MetaData.get().doMQServiceUndeploy(json, type);
				}
				break;
			case e46:
			case e47:
			case e48:
			case e49:
			case e50:
				break;
			//broker down
			case e54:
				break;

			//主从切换
			case e56:
				if (uuid.equals(MetaData.get().getUUID())) {
					generalNotify(type, servId, jsonStr,
							ClientStatisticData.get().getMqClients());
				}
				break;
				
			//接入机扩缩容
			case e61:		
			case e62:
				if (uuid.equals(MetaData.get().getUUID())) {
					JsonObject proxyInfo = CacheService.getProxyInfoByID(
							json.getString(FixHeader.HEADER_INSTANCE_ID), new ResultBean());
					generalNotify(type, servId, proxyInfo.toString(),
							ClientStatisticData.get().getCacheClients());
				}
				break;				
			
			//redis故障切换
			case e63:
				if (uuid.equals(MetaData.get().getUUID())) {
					generalNotify(type, servId, jsonStr,
							ClientStatisticData.get().getCacheProxies(servId));
				}
				//update metadata
				InstanceDtlBean cluster =
						MetaData.get().getInstanceDtlBean(json.getString(FixHeader.HEADER_CLUSTER_ID));
				cluster.setAttribute(FixHeader.HEADER_MASTER_ID, 
						json.getString(FixHeader.HEADER_NEW_MASTER_ID));
				break;
				
			//redis节点down
			case e64:
				break;
				
			//Tidb Server扩缩容
			case e71:
			case e72:
				if (uuid.equals(MetaData.get().getUUID())) {
					generalNotify(type, servId, jsonStr, 
							ClientStatisticData.get().getDbClients());
				}
				break;	
				
			//客户端上报事件
			case e98:
				JsonObject obj = new JsonObject(jsonStr);
				String clientType = obj.getString(FixHeader.HEADER_CLIENT_TYPE);
				String lsnrAddr = obj.getString(FixHeader.HEADER_LSNR_ADDR);
				//TODO deal client info
				ClientStatisticData.get().put(clientType, lsnrAddr);
				
				break;
			case e99:
				if (!uuid.equals(MetaData.get().getUUID())) {
					JsonObject jsonObject = new JsonObject(jsonStr);
					if(jsonObject != null){
						String servType = jsonObject.getString(FixHeader.HEADER_SERV_TYPE);
						JsonObject syncJson = jsonObject.getJsonObject(FixHeader.HEADER_JSONSTR);

						if(CONSTS.SERV_TYPE_MQ.equalsIgnoreCase(servType)) {
							MonitorData.get().syncMqJson(syncJson, servId);
						}else if(CONSTS.SERV_TYPE_CACHE.equalsIgnoreCase(servType)) {
							MonitorData.get().syncCacheJson(syncJson, servId);
						}else if(CONSTS.SERV_TYPE_DB.equalsIgnoreCase(servType)) {
							MonitorData.get().syncTiDBJson(syncJson, servId);
						}
					}
				}
				break;
			default:
				break;
			}
		}
		
		private void generalNotify(EventType type, String servId, String jsonStr, Set<String> clients) {
			EventBean evBean = new EventBean();
			evBean.setEvType(type);
			evBean.setServID(servId);
			evBean.setJsonStr(jsonStr);
			String msg = evBean.asJsonString();
			
			for (String addr : clients) {
				String arr[] = addr.split(":");
				String ip   = arr[0];
				int    port = Integer.valueOf(arr[1]);
					
				EventNotifier notifier = new EventNotifier(ip, port, msg);
				String info = String.format("notify cache ha switch event %s:%d %s", ip, port, msg);
				logger.info(info);
				WorkerPool.get().execute(notifier);
			}
		}

	}
	
	private static class EventNotifier implements Runnable {
		
		private String ip;
		private int    port;
		private String msg;
		
		public EventNotifier(String ip, int port, String msg) {
			this.ip   = ip;
			this.port = port;
			this.msg  = msg;
		}

		@Override
		public void run() {
			try {
				Socket socket = new Socket(ip, port);

				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();

				DataOutputStream dout = new DataOutputStream(out);
				DataInputStream din = new DataInputStream(in);

				int bodyLen = msg.length();
				int len = CONSTS.FIX_HEAD_LEN + bodyLen;
				byte[] sendData = new byte[len];
				
				prepareData(sendData, msg);

				dout.write(sendData, 0, len);
				dout.flush();

				dout.close();
				out.close();

				din.close();
				in.close();

				socket.close();

			} catch (UnknownHostException e) {
				logger.error("EventNotifier " + ip + ":" + port, e);
			} catch (IOException e) {
				logger.error("EventNotifier " + ip + ":" + port, e);
			}
		}
		
		void GetIntBytes(byte[] bs, int i) {
			int idx = CONSTS.FIX_PREHEAD_LEN;
			bs[idx++] = (byte) (i & 0xff);
			bs[idx++] = (byte) ((i >>> 8) & 0xff);
			bs[idx++] = (byte) ((i >>> 16) & 0xff);
			bs[idx++] = (byte) ((i >>> 24) & 0xff);
		}
		
		void prepareData(byte[] sendData, String body) {
			System.arraycopy(CONSTS.PRE_HEAD, 0, sendData, 0, CONSTS.FIX_PREHEAD_LEN);
		    
		    GetIntBytes(sendData, body.length());
		    
		    byte[] bodyBytes = body.getBytes();
		    System.arraycopy(bodyBytes, 0, sendData, CONSTS.FIX_HEAD_LEN, bodyBytes.length);
		}
		
	}

}
