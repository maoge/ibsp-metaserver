package ibsp.metaserver.bean;

public class CacheNodeCollectInfo {
	private String id;
	
    private String linkStatus;
    private long memoryUsed;
    private long memoryTotal;
    private long dbSize;
    private int connectedClients;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLinkStatus() {
		return linkStatus;
	}

	public void setLinkStatus(String linkStatus) {
		this.linkStatus = linkStatus;
	}

	public long getMemoryUsed() {
		return memoryUsed;
	}

	public void setMemoryUsed(long memoryUsed) {
		this.memoryUsed = memoryUsed;
	}

	public long getMemoryTotal() {
		return memoryTotal;
	}

	public void setMemoryTotal(long memoryTotal) {
		this.memoryTotal = memoryTotal;
	}

	public long getDbSize() {
		return dbSize;
	}

	public void setDbSize(long dbSize) {
		this.dbSize = dbSize;
	}

	public int getConnectedClients() {
		return connectedClients;
	}

	public void setConnectedClients(int connectedClients) {
		this.connectedClients = connectedClients;
	}
}
