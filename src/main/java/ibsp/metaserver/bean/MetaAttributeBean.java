package ibsp.metaserver.bean;

import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;

public class MetaAttributeBean extends BeanMapper {

	private int     attrID;     // t_meta_attr.ATTR_ID
	private String  attrName;   // t_meta_attr.ATTR_NAME
	private String  attrNameCN; // t_meta_attr.ATTR_NAME_CN
	private boolean autoGen;    // t_meta_attr.AUTO_GEN
	
	public MetaAttributeBean(int attrID, String attrName, String attrNameCN,
			boolean autoGen) {
		super();
		this.attrID = attrID;
		this.attrName = attrName;
		this.attrNameCN = attrNameCN;
		this.autoGen = autoGen;
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

	public String getAttrNameCN() {
		return attrNameCN;
	}

	public void setAttrNameCN(String attrNameCN) {
		this.attrNameCN = attrNameCN;
	}

	public boolean isAutoGen() {
		return autoGen;
	}

	public void setAutoGen(boolean autoGen) {
		this.autoGen = autoGen;
	}

	public String jsonString() {
		JsonObject json = new JsonObject();
		
		json.put(FixHeader.HEADER_ATTR_ID,      attrID);
		json.put(FixHeader.HEADER_ATTR_NAME,    attrName);
		json.put(FixHeader.HEADER_ATTR_NAME_CN, attrNameCN);
		json.put(FixHeader.HEADER_AUTO_GEN,     autoGen ? CONSTS.AUTO_GEN_Y : CONSTS.AUTO_GEN_N);
		
		return json.toString();
	}
	
	public static MetaAttributeBean fromJson(String json) {
		if (HttpUtils.isNull(json) || !HttpUtils.isJson(json))
			return null;
		
		JsonObject jsonObj = new JsonObject(json);
		int     attrID     = jsonObj.getInteger(FixHeader.HEADER_ATTR_ID);
		String  attrName   = jsonObj.getString(FixHeader.HEADER_ATTR_NAME);
		String  attrNameCN = jsonObj.getString(FixHeader.HEADER_ATTR_NAME_CN);
		boolean autoGen    = jsonObj.getString(FixHeader.HEADER_AUTO_GEN).equals(CONSTS.AUTO_GEN_Y) ? true : false;
		
		return new MetaAttributeBean(attrID, attrName, attrNameCN, autoGen);
	}
	
	public static MetaAttributeBean convert(HashMap<String, Object> mapper) {
		if (mapper == null)
			return null;
		
		int     attrID     = getFixDataAsInt(mapper, FixHeader.HEADER_ATTR_ID);
		String  attrName   = getFixDataAsString(mapper, FixHeader.HEADER_ATTR_NAME);
		String  attrNameCN = getFixDataAsString(mapper, FixHeader.HEADER_ATTR_NAME_CN);
		boolean autoGen    = getFixDataAsString(mapper, FixHeader.HEADER_AUTO_GEN).equals(CONSTS.AUTO_GEN_Y) ? true : false;
		
		return new MetaAttributeBean(attrID, attrName, attrNameCN, autoGen);
	}
	
	public JsonObject asJson() {
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_ATTR_ID,      attrID);
		json.put(FixHeader.HEADER_ATTR_NAME,    attrName);
		json.put(FixHeader.HEADER_ATTR_NAME_CN, attrNameCN);
		json.put(FixHeader.HEADER_AUTO_GEN,     autoGen ? CONSTS.AUTO_GEN_Y : CONSTS.AUTO_GEN_N);
		
		return json;
	}
	
	public String asJsonString() {
		JsonObject json = new JsonObject();
		json.put(FixHeader.HEADER_ATTR_ID,      attrID);
		json.put(FixHeader.HEADER_ATTR_NAME,    attrName);
		json.put(FixHeader.HEADER_ATTR_NAME_CN, attrNameCN);
		json.put(FixHeader.HEADER_AUTO_GEN,     autoGen ? CONSTS.AUTO_GEN_Y : CONSTS.AUTO_GEN_N);
		
		return json.toString();
	}
	
}
