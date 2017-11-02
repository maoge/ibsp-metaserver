package ibsp.metaserver.bean;

import java.util.Map;

public class ServiceBean extends BeanMapper {
	
	private String instID;    // t_service.INST_ID
	private String servName;  // t_service.SERV_NAME
	private String servType;  // t_service.SERV_TYPE
	private long createTime;  // t_service.CREATE_TIME
	
	public ServiceBean(String instID, String servName, String servType,
			long createTime) {
		super();
		this.instID = instID;
		this.servName = servName;
		this.servType = servType;
		this.createTime = createTime;
	}

	public String getInstID() {
		return instID;
	}

	public void setInstID(String instID) {
		this.instID = instID;
	}

	public String getServName() {
		return servName;
	}

	public void setServName(String servName) {
		this.servName = servName;
	}

	public String getServType() {
		return servType;
	}

	public void setServType(String servType) {
		this.servType = servType;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public static ServiceBean convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		
		String instID   = getFixDataAsString(mapper, "INST_ID");
		String servName = getFixDataAsString(mapper, "SERV_NAME");
		String servType = getFixDataAsString(mapper, "SERV_TYPE");
		long createTime = getFixDataAsLong(mapper, "CREATE_TIME");
		
		return new ServiceBean(instID, servName, servType, createTime);
	}

}
