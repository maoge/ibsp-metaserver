package ibsp.metaserver.eventbus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			String uuid         = jsonObj.getString(FixHeader.HEADER_UUID);
			String jsonStr      = jsonObj.getString(FixHeader.HEADER_JSONSTR);
			EventType type      = EventType.get(eventCode);
			JsonObject json     = new JsonObject(jsonStr);
			
//			if (uuid.equals(MetaData.get().getUUID())) {
//				return;
//			}
			
			switch(type) {
			case e1:
			case e2:
				System.out.println(msg);
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
			//客户端上报事件
			case e98:
				JsonObject obj = new JsonObject(jsonStr);
				String clientType = obj.getString(FixHeader.HEADER_CLIENT_TYPE);
				String clientInfo = obj.getString(FixHeader.HEADER_CLIENT_INFO);
				String lsnrAddr = obj.getString(FixHeader.HEADER_LSNR_ADDR);
				
				//TODO deal client info
				ClientStatisticData.get().put(clientType, lsnrAddr);
				
				break;
				
			default:
				break;
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
