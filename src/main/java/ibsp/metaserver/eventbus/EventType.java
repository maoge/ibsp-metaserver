package ibsp.metaserver.eventbus;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EventType {
	
	e0(50010000,   false, "default"),
	e1(50010001,   false, "add new broker"),               // add broker
	e2(50010002,   false, "broker info update"),           // mod broker
	e3(50010003,   false, "broker removed"),               // del broker
	
	e4(50010004,   false, "add new vbroker"),              // add vbroker
	e5(50010005,   false, "vbroker info update"),          // mod vbroker
	e6(50010006,   false, "vbroker removed"),              // del vbroker
	
	e7(50010007,   false, "add new group"),                // add group
	e8(50010008,   false, "group info update"),            // mod group
	e9(50010009,   false, "group removed"),                // del group
	
	e10(50010010,  false, "add new queue"),                // add queue
	e11(50010011,  false, "queue info update"),            // mod queue
	e12(50010012,  false, "queue removed"),                // del queue
	
	e13(50010013,  false, "add vbroker_broker_relation"),  // vbroker_broker_relation add
	e14(50010014,  false, "mod vbroker_broker_relation"),  // vbroker_broker_relation mod
	e15(50010015,  false, "del vbroker_broker_relation"),  // vbroker_broker_relation del
	
	e16(50010016,  false, "add group_vbroker_relation"),   // group_vbroker_relation add
	e17(50010017,  false, "mod group_vbroker_relation"),   // group_vbroker_relation mod
	e18(50010018,  false, "del group_vbroker_relation"),   // group_vbroker_relation del
	
	e19(50010019,  false, "add queue_group_relation"),     // queue_group_relation add
	e20(50010020,  false, "mod queue_group_relation"),     // queue_group_relation mod
	e21(50010021,  false, "del queue_group_relation"),     // queue_group_relation del
	
	e22(50010022,  false, "del queue for GlobalStatisticData"),       // GlobalStatisticData queue del
	e23(50010023,  false, "del vbroker for GlobalStatisticData"),     // GlobalStatisticData vbroker del
	
	e24(50010024,  false, "add sys_user"),                 // sys_user add
	e25(50010025,  false, "mod sys_user"),                 // sys_user mod
	e26(50010026,  false, "del sys_user"),                 // sys_user del
	
	e27(50010027,  false, "mod user_group_relation"),      // user_group_relation mod
	e28(50010028,  false, "mod sys_roleuser"),             // sys_roleuser mod
	
	e29(50010029,  false, "add black_white_list"),         // add black white list
	e30(50010030,  false, "mod black_white_list"),         // mod black white list
	e31(50010031,  false, "del black_white_list"),         // del black white list

	
	e51(50010051,  false, "stop send/publish msg on vbroker"),        // group缩容前要对即将移除的VBROKER停写
	e52(50010052,  false, "add vbroker to group"),         // group扩容
	e53(50010053,  false, "remove vbroker from group"),    // group缩容
	
	e54(50010054,  true,  "broker down"),                  // broker crashed
	e55(50010055,  false, "broker recovered"),             // broker serice is fixed
	e56(50010056,  true,  "ha cluster swithed"),           // master-slave have switched 
	e57(50010057,  true,  "Message accumulated"),          // 消息发生了堆积
	e58(50010058,  true,  "memory highwtater mark"),       // 内存使用高水位
	e59(50010059,  true,  "disk highwater mark"),          // 磁盘使用高水位
	e60(50010060,  true,  "too many connections"),         // 连接数达到上限
	
	e87(50010087,  true,  "Network partition"),            // 脑裂
	
	e88(50010088,  false, "deploy vbroker"),               // deploy vbroker
	e89(50010089,  false, "undeploy vbroker"),             // undeploy vbroker
	e90(50010090,  false, "modify user passwd"),           // modify user passwd
	e91(50010091,  false, "stop vbroker"),                 // stop vbroker
	e92(50010092,  false, "start vbroker"),                // start vbroker
	e93(50010093,  false, "stop broker"),                  // stop broker
	e94(50010094,  false, "start broker"),                 // start broker
	e95(50010095,  false, "add permnent topic noftify"),   // mq_permnent_topic add
	e96(50010096,  false, "del permnent topic noftify"),   // mq_permnent_topic del
	e97(50010097,  false, "queue sum event"),              // 非采集节点被动接收事件开始统计   
	e98(50010098,  false, "client put collect event"),     // 客户端上传采集信息   
	e99(50010099,  false, "rabbit api collect event");     // 主动扫描事件

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
