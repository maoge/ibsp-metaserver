package ibsp.metaserver.eventbus;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EventType {
	
	e0(50000, false, "default"),
	e1(50001, false, "MetaData.topo add"),               // MetaData.topo add
	e2(50002, false, "MetaData.topo del"),               // MetaData.topo delete
	
	e3(50003, false, "MetaData.instanceDtlMap add"),     // MetaData.instanceDtlMap add
	e4(50004, false, "MetaData.instanceDtlMap mod"),     // MetaData.instanceDtlMap mod
	e5(50005, false, "MetaData.instanceDtlMap del"),     // MetaData.instanceDtlMap del
	
	e6(50006, false, "MetaData.serviceMap add"),         // MetaData.serviceMap add
	e7(50007, false, "MetaData.serviceMap mod"),         // MetaData.serviceMap mod
	e8(50008, false, "MetaData.serviceMap del"),         // MetaData.serviceMap del
	
	e9 (50009,  false, "MetaData.queueMap add"),         // MetaData.queueMap add
	e10(500010, false, "MetaData.queueMap mod"),         // MetaData.queueMap mod
	e11(500011, false, "MetaData.queueMap del"),         // MetaData.queueMap del
	
	e12(500012, false, "MetaData.queueMap add"),         // MetaData.permnentTopic add
	e13(500013, false, "MetaData.queueMap del"),         // MetaData.permnentTopic del
	
	e21(50021, false, "Service deployed"),
	e22(50022, false, "Service undeployed"),
	e23(50023, false, "Instance deployed"),
	e24(50024, false, "Instance undeployed"),
	
	e31(50031, false, "MQService.undeploy"),             //MQService.undeploy
	
	e61(50061, false, "cache proxy deployed"),       // 接入机扩容
	e62(50062, false, "cache proxy undeployed"),       // 接入机缩容
	e63(50063, true, "redis automatic ha switch"),       // redis故障主从切换
	e64(50064, true, "redis instance down"),       // redis实例故障
	
	e71(50071, false, "tidb server deployed"),       // TIDB层扩容
	e72(50072, false, "tidb server undeployed"),       // TIDB层缩容
	
	e98(50098, false, "client put collect event");       // 瀹㈡埛绔笂浼犻噰闆嗕俊鎭� 
	

	private final int     value;
	private final boolean alarm;
	private final String  info;
	
	private static final Map<Integer,EventType> map = new HashMap<Integer,EventType>();

	static {
		for (EventType s : EnumSet.allOf(EventType.class)) {
			map.put(s.value, s);
		}
	}
	
	private EventType(int i, boolean b, String s) {
		value = i;
		alarm = b;
		info  = s;
	}
	
	public static EventType get(int code){
		return map.get(code);
	}
	
	public int getValue() {
		// 寰楀埌鏋氫妇鍊间唬琛ㄧ殑瀛楃涓层��
		return value;
	}
	
	public boolean isAarm() {
		return alarm;
	}
	
	public String getInfo() {
		// 寰楀埌鏋氫妇鍊间唬琛ㄧ殑瀛楃涓层��
		return info;
	}
	
	public boolean equals(EventType e) {
		return this.value == e.value;
	}
	
}
