package ibsp.metaserver.eventbus;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.annotation.KVPair;
import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonObject;

public class EventBean {
	
	private static Logger logger = LoggerFactory.getLogger(EventBean.class.getName());
	
	@KVPair(key = FixHeader.HEADER_EVENT_CODE, val = "")
	private EventType evType;
	
	@KVPair(key = FixHeader.HEADER_SERV_ID, val = "")
	private String servID;
	
	@KVPair(key = FixHeader.HEADER_UUID, val = "")
	private String uuid;
	
	@KVPair(key = FixHeader.HEADER_JSONSTR, val = "")
	private String jsonStr;

	private static Class<?> CLAZZ;
	
	static {
		CLAZZ = EventBean.class;
	}
	
	public EventBean() {
		evType  = EventType.e0;
		servID  = "";
		uuid    = "";
		jsonStr = "";
	}
	
	public EventBean(EventType evType) {
		this.evType = evType;
	}

	public EventType getEvType() {
		return evType;
	}

	public void setEvType(EventType evType) {
		this.evType = evType;
	}
	
	public String getServID() {
		return servID;
	}

	public void setServID(String servID) {
		this.servID = servID;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getJsonStr() {
		return jsonStr;
	}

	public void setJsonStr(String jsonStr) {
		this.jsonStr = jsonStr;
	}

	public String asJsonString() {
		JsonObject jsonObj = new JsonObject();
		
		Field[] fields = CLAZZ.getDeclaredFields();
		for (Field field : fields) {
			KVPair kv = field.getAnnotation(KVPair.class);
			if (kv == null)
				continue;
			
			boolean error = false;
			Object obj = null;
			
			try {
				field.setAccessible(true);
				obj = field.get(this);
				if (obj == null)
					continue;
				
				if (obj instanceof EventType) {
					obj = ((EventType) obj).getValue();
				} else if (obj instanceof String) {
					if (obj.equals(""))
						continue;
				}
				
				if (obj != null) {
					String key = kv.key();
					jsonObj.put(key, obj);
				}
			} catch(IllegalArgumentException e) {
				logger.error(e.getMessage(), e);
				error = true;
			} catch(IllegalAccessException e) {
				logger.error(e.getMessage(), e);
				error = true;
			} finally {
				if (error)
					continue;
			}
		}
		
		return jsonObj.toString();
	}

}
