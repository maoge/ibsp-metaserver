package ibsp.metaserver.bean;

import java.util.Map;

public class TopologyBean extends BeanMapper {
	
	private String instID1;  // t_topology.INST_ID1
	private String instID2;  // t_topology.INST_ID2
	private int topoType;    // t_topology.TOPO_TYPE
	
	public TopologyBean(String id1, String id2, int type) {
		super();
		
		this.instID1 = id1;
		this.instID2 = id2;
		this.topoType = type;
	}
	
	public String getInstID1() {
		return instID1;
	}

	public void setInstID1(String instID1) {
		this.instID1 = instID1;
	}

	public String getInstID2() {
		return instID2;
	}

	public void setInstID2(String instID2) {
		this.instID2 = instID2;
	}

	public int getTopoType() {
		return topoType;
	}

	public void setTopoType(int topoType) {
		this.topoType = topoType;
	}

	public static TopologyBean convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		
		String instID1 = getFixDataAsString(mapper, "INST_ID1");
		String instID2 = getFixDataAsString(mapper, "INST_ID2");
		int topoType   = getFixDataAsInt(mapper, "TOPO_TYPE");
		
		return new TopologyBean(instID1, instID2, topoType);
	}
	
}
