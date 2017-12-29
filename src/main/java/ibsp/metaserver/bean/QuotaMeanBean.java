package ibsp.metaserver.bean;

import java.util.HashMap;

public class QuotaMeanBean extends BeanMapper {
	
	private String instID;
	private long ts;
	private int quotaCode;
	private String quotaMean;
	
	public QuotaMeanBean(String instID, long ts, int quotaCode, String quotaMean) {
		super();
		this.instID = instID;
		this.ts = ts;
		this.quotaCode = quotaCode;
		this.quotaMean = quotaMean;
	}

	public String getInstID() {
		return instID;
	}

	public void setInstID(String instID) {
		this.instID = instID;
	}
	
	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public int getQuotaCode() {
		return quotaCode;
	}

	public void setQuotaCode(int quotaCode) {
		this.quotaCode = quotaCode;
	}

	public String getQuotaMean() {
		return quotaMean;
	}

	public void setQuotaMean(String quotaMean) {
		this.quotaMean = quotaMean;
	}
	
	public static QuotaMeanBean convert(HashMap<String, Object> mapper) {
		if (mapper == null)
			return null;
		
		String instID = getFixDataAsString(mapper, "INST_ID");
		long ts = getFixDataAsLong(mapper, "TS");
		int quotaCode = getFixDataAsInt(mapper, "QUOTA_CODE");
		String quotaMean = getFixDataAsString(mapper, "QUOTA_MEAN");
		
		return new QuotaMeanBean(instID, ts, quotaCode, quotaMean);
	}

}
