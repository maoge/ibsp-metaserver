package ibsp.metaserver.autodeploy.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LogBean {
	private static final String HTML_LIEN_END = "<br/>";
	
	private long lastTimestamp;
	private ConcurrentLinkedQueue<String> longQueue;

	public LogBean() {
		lastTimestamp = System.currentTimeMillis();
		longQueue = new ConcurrentLinkedQueue<String>();
	}
	
	public void putLog(String log) {
		longQueue.offer(log);
		lastTimestamp = System.currentTimeMillis();
	}
	
	public String getLog() {
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = longQueue.poll()) != null) {
			sb.append(line).append(HTML_LIEN_END);
		}
		
		return sb.toString();
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}
	
	public void clear() {
		if (longQueue != null) {
			longQueue.clear();
		}
	}
	
}
