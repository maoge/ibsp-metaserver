package ibsp.metaserver.bean;

import java.util.Map;

public class ServiceBean extends BeanMapper {
	
	private String instID;    // t_service.INST_ID
	private String servName;  // t_service.SERV_NAME
	private String servType;  // t_service.SERV_TYPE
	private String deployed;  // t_service.IS_DEPLOYED
	private String product;   // t_service.IS_PRODUCT
	private long createTime;  // t_service.CREATE_TIME
	private String user;      // t_service.USER
	private String password;  // t_service.PASSWORD
	
	public ServiceBean(String instID, String servName, String servType,
			String deployed, String product, long createTime, String user, String password) {
		super();
		this.instID = instID;
		this.servName = servName;
		this.servType = servType;
		this.deployed = deployed;
		this.product = product;
		this.createTime = createTime;
		this.user = user;
		this.password = password;
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
	
	public String getDeployed() {
		return deployed;
	}

	public void setDeployed(String deployed) {
		this.deployed = deployed;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public static ServiceBean convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		
		String instID   = getFixDataAsString(mapper, "INST_ID");
		String servName = getFixDataAsString(mapper, "SERV_NAME");
		String servType = getFixDataAsString(mapper, "SERV_TYPE");
		String deployed = getFixDataAsString(mapper, "IS_DEPLOYED");
		String product = getFixDataAsString(mapper, "IS_PRODUCT");
		long createTime = getFixDataAsLong(mapper, "CREATE_TIME");
		String user = getFixDataAsString(mapper, "USER");
		String password = getFixDataAsString(mapper, "PASSWORD");
		
		return new ServiceBean(instID, servName, servType, deployed, product, createTime, user, password);
	}

}
