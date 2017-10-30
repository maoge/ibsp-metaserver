package ibsp.metaserver.utils;

import java.util.UUID;

public class UUIDUtils {
	
	public static String genUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
}
