package ibsp.metaserver.bean;

public class IBSPClientInfo {
	
	private String address;
	private String servID;
	private String type;
	private long refreshTS;
	
	public IBSPClientInfo(String address, String servID, String type,
			long refreshTS) {
		super();
		this.address = address;
		this.servID = servID;
		this.type = type;
		this.refreshTS = refreshTS;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getServID() {
		return servID;
	}

	public void setServID(String servID) {
		this.servID = servID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getRefreshTS() {
		return refreshTS;
	}

	public void setRefreshTS(long refreshTS) {
		this.refreshTS = refreshTS;
	}
	
	public void refresh() {
		this.refreshTS = System.currentTimeMillis();
	}

}
