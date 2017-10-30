package ibsp.metaserver.bean;

import java.util.HashMap;

import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonObject;

public class MetaComponentBean extends BeanMapper {
	
	private int    cmptID;      // t_meta_cmpt.CMPT_ID
	private String cmptName;    // t_meta_cmpt.CMPT_NAME
	private String cmptNameCn;  // t_meta_cmpt.CMPT_NAME_CN
	private String servClazz;   // t_meta_cmpt.SERV_CLAZZ
	private String servType;    // t_meta_cmpt.SERV_TYPE
	private String subServType; // t_meta_cmpt.SUB_SERV_TYPE
	
	public MetaComponentBean(int cmptID, String cmptName, String cmptNameCn,
			String servClazz, String servType, String subServType) {
		super();
		this.cmptID = cmptID;
		this.cmptName = cmptName;
		this.cmptNameCn = cmptNameCn;
		this.servClazz = servClazz;
		this.servType = servType;
		this.subServType = subServType;
	}

	public int getCmptID() {
		return cmptID;
	}

	public void setCmptID(int cmptID) {
		this.cmptID = cmptID;
	}

	public String getCmptName() {
		return cmptName;
	}

	public void setCmptName(String cmptName) {
		this.cmptName = cmptName;
	}

	public String getCmptNameCn() {
		return cmptNameCn;
	}

	public void setCmptNameCn(String cmptNameCn) {
		this.cmptNameCn = cmptNameCn;
	}

	public String getServClazz() {
		return servClazz;
	}

	public void setServClazz(String servClazz) {
		this.servClazz = servClazz;
	}

	public String getServType() {
		return servType;
	}

	public void setServType(String servType) {
		this.servType = servType;
	}

	public String getSubServType() {
		return subServType;
	}

	public void setSubServType(String subServType) {
		this.subServType = subServType;
	}

	public String jsonString() {
		JsonObject json = new JsonObject();
		
		json.put(FixHeader.HEADER_CMPT_ID,       cmptID);
		json.put(FixHeader.HEADER_CMPT_NAME,     cmptName);
		json.put(FixHeader.HEADER_CMPT_NAME_CN,  cmptNameCn);
		json.put(FixHeader.HEADER_SERV_CLAZZ,    servClazz);
		json.put(FixHeader.HEADER_SERV_TYPE,     servType);
		json.put(FixHeader.HEADER_SUB_SERV_TYPE, subServType);
		
		return json.toString();
	}
	
	public static MetaComponentBean fromJson(String json) {
		if (HttpUtils.isNull(json) || !HttpUtils.isJson(json))
			return null;
		
		JsonObject jsonObj = new JsonObject(json);
		int    cmptID      = jsonObj.getInteger(FixHeader.HEADER_CMPT_ID);
		String cmptName    = jsonObj.getString(FixHeader.HEADER_CMPT_NAME);
		String cmptNameCn  = jsonObj.getString(FixHeader.HEADER_CMPT_NAME_CN);
		String servClazz   = jsonObj.getString(FixHeader.HEADER_SERV_CLAZZ);
		String servType    = jsonObj.getString(FixHeader.HEADER_SERV_TYPE);
		String subServType = jsonObj.getString(FixHeader.HEADER_SUB_SERV_TYPE);
		
		return new MetaComponentBean(cmptID, cmptName, cmptNameCn, servClazz, servType, subServType);
	}
	
	public static MetaComponentBean convert(HashMap<String, Object> mapper) {
		if (mapper == null)
			return null;
		
		int    cmptID      = getFixDataAsInt(mapper, FixHeader.HEADER_CMPT_ID);
		String cmptName    = getFixDataAsString(mapper, FixHeader.HEADER_CMPT_NAME);
		String cmptNameCn  = getFixDataAsString(mapper, FixHeader.HEADER_CMPT_NAME_CN);
		String servClazz   = getFixDataAsString(mapper, FixHeader.HEADER_SERV_CLAZZ);
		String servType    = getFixDataAsString(mapper, FixHeader.HEADER_SERV_TYPE);
		String subServType = getFixDataAsString(mapper, FixHeader.HEADER_SUB_SERV_TYPE);
		
		return new MetaComponentBean(cmptID, cmptName, cmptNameCn, servClazz, servType, subServType);
	}

}
