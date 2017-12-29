package ibsp.metaserver.bean;

import java.util.HashMap;

public class CollectQuotaBean extends BeanMapper {
	
	private int quotaCode;
	private String quotaName;
	
	public CollectQuotaBean(int quotaCode, String quotaName) {
		super();
		this.quotaCode = quotaCode;
		this.quotaName = quotaName;
	}

	public int getQuotaCode() {
		return quotaCode;
	}

	public void setQuotaCode(int quotaCode) {
		this.quotaCode = quotaCode;
	}

	public String getQuotaName() {
		return quotaName;
	}

	public void setQuotaName(String quotaName) {
		this.quotaName = quotaName;
	}
	
	public static CollectQuotaBean convert(HashMap<String, Object> mapper) {
		if (mapper == null)
			return null;
		
		int    quotaCode = getFixDataAsInt(mapper, "QUOTA_CODE");
		String quotaName = getFixDataAsString(mapper, "QUOTA_NAME");
		
		return new CollectQuotaBean(quotaCode, quotaName);
	}

}
