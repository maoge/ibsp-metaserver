package json.schema.enumtest;

import java.util.Iterator;
import java.util.Map.Entry;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonEnumTest {
	
	private static boolean EnumJson(Object json) {
		if (json instanceof JsonObject) {
			Iterator<Entry<String, Object>> it = ((JsonObject) json).iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				if (entry == null)
					return false;
				
				String key = entry.getKey();
				Object val = entry.getValue();
				if (val == null)
					return false;
				
				String info = String.format("%s:%s", key, val.getClass().getName());
				System.out.println(info);
				
				if (!EnumJson(val))
					return false;
			}
		} else if (json instanceof JsonArray) {
			if (((JsonArray) json).size() == 0)
				return true;
			
			Iterator<Object> it = ((JsonArray) json).iterator();
			while (it.hasNext()) {
				Object val = it.next();
				if (val == null)
					return false;
				
				if (!EnumJson(val))
					return false;
			}
		}
		
		return true;
	}

	public static void main(String[] args) {
		JsonObject json = new JsonObject();
		JsonObject subJson = new JsonObject();
		json.put("DB_SERV_CONTAINER", subJson);
		
		subJson.put("DB_SVC_CONTAINER_ID", "b83df121-94fd-c646-9c41-038deeeed90a");
		subJson.put("DB_SVC_CONTAINER_NAME", "cureuprapapa");
		
		JsonArray subJsonArr = new JsonArray();
		json.put("DB_TIDB", subJsonArr);
		
		EnumJson(json);
	}

}
