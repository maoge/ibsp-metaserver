package ibsp.metaserver.bean;

import java.util.HashMap;
import java.util.Map;

public class InstanceDtlBean {
	
	private InstanceBean instance;
	private Map<String, InstAttributeBean> attrMap;
	private Map<String, InstanceDtlBean> subInsts;
	
	public InstanceDtlBean() {
		this.instance = null;
		this.attrMap = null;
	}
	
	public InstanceDtlBean(InstanceBean inst) {
		this.instance = inst;
		this.attrMap = new HashMap<String, InstAttributeBean>();
	}

	public InstanceDtlBean(InstanceBean instance,
			Map<String, InstAttributeBean> attrMap) {
		super();
		this.instance = instance;
		this.attrMap = attrMap;
	}
	
	public String getInstID() {
		if (instance == null)
			return null;
		
		return instance.getInstID();
	}

	public InstanceBean getInstance() {
		return instance;
	}

	public void setInstance(InstanceBean instance) {
		this.instance = instance;
	}

	public Map<String, InstAttributeBean> getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(Map<String, InstAttributeBean> attrMap) {
		this.attrMap = attrMap;
	}
	
	public InstAttributeBean getAttribute(String attrName) {
		if (attrMap == null)
			return null;
		
		return attrMap.get(attrName);
	}
	
	public void setAttribute(String attrName, String attrValue) {
		if (attrMap == null)
			return;
		
		InstAttributeBean attribute = attrMap.get(attrName);
		if (attribute != null) {
			attribute.setAttrValue(attrValue);
		}
	}
	
	public void addAttribute(InstAttributeBean attr) {
		if (attrMap == null) {
			attrMap = new HashMap<String, InstAttributeBean>();
		}
		
		attrMap.put(attr.getAttrName(), attr);
	}
	
	public void addSubInstance(InstanceDtlBean subInstance) {
		if (subInstance == null)
			return;
		
		if (subInsts == null)
			subInsts = new HashMap<String, InstanceDtlBean>();
		
		subInsts.put(subInstance.getInstID(), subInstance);
	}
	
	public Map<String, InstanceDtlBean> getSubInstances() {
		return subInsts;
	}

}
