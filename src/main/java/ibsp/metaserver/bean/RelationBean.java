package ibsp.metaserver.bean;

import ibsp.metaserver.utils.FixHeader;

import java.util.Map;

public class RelationBean extends BeanMapper {

	private Object masterID;
	private Object slaveID;
	
	public RelationBean() {
		masterID = null;
		slaveID  = null;
	}

	public RelationBean(Object masterID, Object slaveID) {
		super();
		this.masterID = masterID;
		this.slaveID = slaveID;
	}
	
	public static RelationBean convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		
		Object master = getFixDataAsObject(mapper, FixHeader.HEADER_MASTER_ID);
		Object slave  = getFixDataAsObject(mapper, FixHeader.HEADER_SLAVE_ID);
		
		return new RelationBean(master, slave);
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
	
	public Object getTOE(Object id) {
		return masterID.equals(id) ? slaveID : masterID;
	}
	
}
