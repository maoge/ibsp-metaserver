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
	e8(50008, false, "MetaData.serviceMap del");         // MetaData.serviceMap del

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
		// 得到枚举值代表的字符串。
		return value;
	}
	
	public boolean isAarm() {
		return alarm;
	}
	
	public String getInfo() {
		// 得到枚举值代表的字符串。
		return info;
	}
	
	public boolean equals(EventType e) {
		return this.value == e.value;
	}
	
}
