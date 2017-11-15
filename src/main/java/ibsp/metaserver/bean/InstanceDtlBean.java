package ibsp.metaserver.bean;

import java.util.Map;

public class InstanceDtlBean {
	
	private InstanceBean instance;
	private Map<String, InstAttributeBean> attrMap;
	
	public InstanceDtlBean() {
		this.instance = null;
		this.attrMap = null;
	}

	public InstanceDtlBean(InstanceBean instance,
			Map<String, InstAttributeBean> attrMap) {
		super();
		this.instance = instance;
		this.attrMap = attrMap;
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

}
