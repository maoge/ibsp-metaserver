package ibsp.metaserver.bean;

import java.util.Map;

import io.vertx.core.json.JsonObject;

public abstract class BeanMapper {
	
	public static Object getFixDataAsObject(Map<String, Object> mapper, String fixHeader) {
		if (mapper == null)
			return null;
		
		if (fixHeader == null || fixHeader.length() == 0)
			return null;
		
		Object o = mapper.get(fixHeader);
		return o;
	}

	public static String getFixDataAsString(Map<String, Object> mapper, String fixHeader) {
		if (mapper == null)
			return "";
		
		if (fixHeader == null || fixHeader.length() == 0)
			return "";
		
		Object obj = mapper.get(fixHeader);
		String value = obj instanceof String ? (String) obj : String.valueOf(obj);
		return value != null ? value : "";
	}
	
	public static int getFixDataAsInt(Map<String, Object> mapper, String fixHeader) {
		if (mapper == null)
			return 0;
		
		if (fixHeader == null || fixHeader.length() == 0)
			return 0;
		
		Object obj = mapper.get(fixHeader);
		Integer value = obj instanceof Integer ? (Integer) obj : Integer.valueOf(String.valueOf(obj));
		return value != null ? value.intValue() : 0;
	}
	
	public static long getFixDataAsLong(Map<String, Object> mapper, String fixHeader) {
		if (mapper == null)
			return 0L;
		
		if (fixHeader == null || fixHeader.length() == 0)
			return 0L;
		
		Object obj = mapper.get(fixHeader);
		Long value = obj instanceof Long ? (Long) obj : Long.valueOf(String.valueOf(obj));
		return value != null ? value.longValue() : 0L;
	}
	
	public static float getFixDataAsFloat(Map<String, Object> mapper, String fixHeader) {
		if (mapper == null)
			return 0;
		
		if (fixHeader == null || fixHeader.length() == 0)
			return 0;
		
		Object obj = mapper.get(fixHeader);
		Float value = obj instanceof Float ? (Float) obj : Float.valueOf(String.valueOf(obj));
		return value != null ? value.floatValue() : 0;
	}
	
	public static double getFixDataAsDouble(Map<String, Object> mapper, String fixHeader) {
		if (mapper == null)
			return 0;
		
		if (fixHeader == null || fixHeader.length() == 0)
			return 0;
		
		Object obj = mapper.get(fixHeader);
		Double value = obj instanceof Double ? (Double) obj : Double.valueOf(String.valueOf(obj));
		return value != null ? value.doubleValue() : 0;
	}
	
	
	public static String toJsonString(Object obj) {
		return JsonObject.mapFrom(obj).toString();
	}
	
}
