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

	e14(500014, false, "MetaData.serverMap add"),         // MetaData.serverMap add
	e15(500015, false, "MetaData.serverMap del"),         // MetaData.serverMap del

	e21(50021, false, "Service deployed"),
	e22(50022, false, "Service undeployed"),
	e23(50023, false, "Instance deployed"),
	e24(50024, false, "Instance undeployed"),
	
	e31(50031, false, "MQService.undeploy"),             //MQService.undeploy

	e46(50046, true, "Network partition"),               //脑裂
	e47(50047, true, "Message accumlated"),              //消息发生了堆积
	e48(50048, true, "memory highwater mark"),           //内存使用高水位
	e49(50049, true, "disk highwater mark"),             //磁盘使用高水位
	e50(50050, true, "too many connections"),            //连接数太多
	e51(50051, false, "stop send/publish msg on vbroker"), //缩容前的停写
	e52(50052, false, "add vbroker to group"),           //vbroker扩容
	e53(50053, false, "remove vbroker from group"),      //vbroker 缩容
	e54(50054, true, "broker down"),
	e55(50055, false, "broker recovered"),
	e56(50056, true, "mq cluster ha switch"),
	e57(50057, false, "vbroker connection stuck"),       //sync 队列数据时候连接卡住
	
	e61(50061, false, "cache proxy deployed"),           // 接入机扩容
	e62(50062, false, "cache proxy undeployed"),         // 接入机缩容
	e63(50063, true, "redis automatic ha switch"),       // redis故障主从切换
	e64(50064, true, "redis instance down"),             // redis实例故障
	e65(50065, true, "cache proxy down"),                // 接入机故障
	
	e71(50071, false, "tidb server deployed"),           // TIDB层扩容
	e72(50072, false, "tidb server undeployed"),         // TIDB层缩容

	e81(50081, true, "tidb-server down"),
	e82(50082, true, "pd-server down"),
	e83(50083, true, "tikv-server down"),

	e98(50098, false, "client put collect event"),       // Client端定时上传统计信息
	e99(50099, false, "sync memroy data");               // 内存数据同步

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
		return value;
	}
	
	public boolean isAarm() {
		return alarm;
	}
	
	public String getInfo() {
		return info;
	}
	
	public boolean equals(EventType e) {
		return this.value == e.value;
	}
	
}
