package ibsp.metaserver.bean;

public class CacheProxyCollectInfo {
	private String id;

	public CacheProxyCollectInfo() {}

	public CacheProxyCollectInfo(String id, long accessClientConns, long accessRedisConns, long accessRequestTps,
								 long accessRequestExcepts, double accessProcessMaxTime, double accessProcessAvTime) {
		this.id = id;
		this.accessClientConns = accessClientConns;
		this.accessRedisConns = accessRedisConns;
		this.accessRequestTps = accessRequestTps;
		this.accessRequestExcepts = accessRequestExcepts;
		this.accessProcessMaxTime = accessProcessMaxTime;
		this.accessProcessAvTime = accessProcessAvTime;
	}

	private long accessClientConns;
    private long accessRedisConns;
    private long accessRequestTps;
    private long accessRequestExcepts;
    private double accessProcessMaxTime;
    private double accessProcessAvTime;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getAccessClientConns() {
		return accessClientConns;
	}
	public void setAccessClientConns(long accessClientConns) {
		this.accessClientConns = accessClientConns;
	}
	public long getAccessRedisConns() {
		return accessRedisConns;
	}
	public void setAccessRedisConns(long accessRedisConns) {
		this.accessRedisConns = accessRedisConns;
	}
	public long getAccessRequestTps() {
		return accessRequestTps;
	}
	public void setAccessRequestTps(long accessRequestTps) {
		this.accessRequestTps = accessRequestTps;
	}
	public long getAccessRequestExcepts() {
		return accessRequestExcepts;
	}
	public void setAccessRequestExcepts(long accessRequestExcepts) {
		this.accessRequestExcepts = accessRequestExcepts;
	}
	public double getAccessProcessMaxTime() {
		return accessProcessMaxTime;
	}
	public void setAccessProcessMaxTime(double accessProcessMaxTime) {
		this.accessProcessMaxTime = accessProcessMaxTime;
	}
	public double getAccessProcessAvTime() {
		return accessProcessAvTime;
	}
	public void setAccessProcessAvTime(double accessProcessAvTime) {
		this.accessProcessAvTime = accessProcessAvTime;
	}
    
}
