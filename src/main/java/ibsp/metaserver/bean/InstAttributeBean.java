package ibsp.metaserver.bean;

import java.util.Map;

public class InstAttributeBean extends BeanMapper {
	
	private String instID;     // t_instance_attr.INST_ID
	private int attrID;        // t_instance_attr.ATTR_ID
	private String attrName;   // t_instance_attr.ATTR_NAME
	private String attrValue;  // t_instance_attr.ATTR_VALUE
	
	public InstAttributeBean(String instID, int attrID, String attrName,
			String attrValue) {
		super();
		this.instID = instID;
		this.attrID = attrID;
		this.attrName = attrName;
		this.attrValue = attrValue;
	}

	public String getInstID() {
		return instID;
	}

	public void setInstID(String instID) {
		this.instID = instID;
	}

	public int getAttrID() {
		return attrID;
	}

	public void setAttrID(int attrID) {
		this.attrID = attrID;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}
	
	public static InstAttributeBean convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		
		String instID    = getFixDataAsString(mapper, "INST_ID");
		int    attrID    = getFixDataAsInt(mapper, "ATTR_ID");
		String attrName  = getFixDataAsString(mapper, "ATTR_NAME");
		String attrValue = getFixDataAsString(mapper, "ATTR_VALUE");
		
		return new InstAttributeBean(instID, attrID, attrName, attrValue);
	}

}
