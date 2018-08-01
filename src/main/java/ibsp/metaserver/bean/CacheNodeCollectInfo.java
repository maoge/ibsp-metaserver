package ibsp.metaserver.bean;

public class CacheNodeCollectInfo {
	private String id;
	
    private String linkStatus;
    private long memoryUsed;
    private long memoryTotal;
    private long dbSize;
    private long aofSize;
    private String aofPolicy;
    private String rdbPolicy;
    
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
	public long getAofSize() {
		return aofSize;
	}
	public void setAofSize(long aofSize) {
		this.aofSize = aofSize;
	}
	public String getAofPolicy() {
		return aofPolicy;
	}
	public void setAofPolicy(String aofPolicy) {
		this.aofPolicy = aofPolicy;
	}
	public String getRdbPolicy() {
		return rdbPolicy;
	}
	public void setRdbPolicy(String rdbPolicy) {
		this.rdbPolicy = rdbPolicy;
	}
    
}
