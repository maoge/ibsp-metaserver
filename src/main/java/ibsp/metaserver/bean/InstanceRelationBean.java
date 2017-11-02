package ibsp.metaserver.bean;

import java.util.Map;

public class InstanceRelationBean extends BeanMapper {
	
	private Object masterID;
	private Object slaveID;
	private int topoType;
	
	public InstanceRelationBean(Object masterID, Object slaveID, int topoType) {
		super();
		this.masterID = masterID;
		this.slaveID = slaveID;
		this.topoType = topoType;
	}

	public Object getMasterID() {
		return masterID;
	}

	public void setMasterID(Object masterID) {
		this.masterID = masterID;
	}

	public Object getSlaveID() {
		return slaveID;
	}

	public void setSlaveID(Object slaveID) {
		this.slaveID = slaveID;
	}

	public int getTopoType() {
		return topoType;
	}

	public void setTopoType(int topoType) {
		this.topoType = topoType;
	}
	
	public static InstanceRelationBean convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		
		Object masterID = getFixDataAsObject(mapper, "INST_ID1");
		Object slaveID  = getFixDataAsObject(mapper, "INST_ID2");
		int topoType = getFixDataAsInt(mapper, "TOPO_TYPE");
		
		return new InstanceRelationBean(masterID, slaveID, topoType);
	}
	
	public Object getTOE(Object id) {
		return masterID.equals(id) ? slaveID : masterID;
	}
	
}
