package ibsp.metaserver.bean;

import java.util.Map;

public class MetaServUrl extends BeanMapper {
	
	private int    metaSvrID;    // t_metasvr_url.METASVR_ID
	private String metaSvrAddr;  // t_metasvr_url.METASVR_ADDR
	
	public MetaServUrl(int metaSvrID, String metaSvrAddr) {
		super();
		this.metaSvrID = metaSvrID;
		this.metaSvrAddr = metaSvrAddr;
	}

	public int getMetaSvrID() {
		return metaSvrID;
	}

	public void setMetaSvrID(int metaSvrID) {
		this.metaSvrID = metaSvrID;
	}

	public String getMetaSvrAddr() {
		return metaSvrAddr;
	}

	public void setMetaSvrAddr(String metaSvrAddr) {
		this.metaSvrAddr = metaSvrAddr;
	}
	
	public String getHttpServAddr() {
		return String.format("http://%s", metaSvrAddr);
	}
	
	public static MetaServUrl convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		int    metaServID   = getFixDataAsInt(mapper, "METASVR_ID");
		String metaServAddr = getFixDataAsString(mapper, "METASVR_ADDR");
		
		return new MetaServUrl(metaServID, metaServAddr);
	}

	@Override
	public String toString() {
		return "{\"METASVR_ID\":" + metaSvrID + ", \"METASVR_ADDR\":"
				+ metaSvrAddr + "}";
	}
	
}
