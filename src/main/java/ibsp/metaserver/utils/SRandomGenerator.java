package ibsp.metaserver.utils;

import java.util.Random;

import ibsp.metaserver.global.MetaData;

public class SRandomGenerator {
	
	private static int ID_LEN     = 16;
	private static int QUEUE_LEN  = 16;
	
	private static final String PERM_CONID_PREFIX   = "ConID";
	private static final String PERM_QUEUE_PREFIX   = "PermQ";
	
	private static char[] DICT_TABLE = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
			'X', 'Y', 'Z' };
	
	public static String genConsumerID() {
		int len = DICT_TABLE.length - 1;
		char[] buf = new char[ID_LEN];
		String ret = "";
		
		while (true) {
			Random random = new Random();
			
			for (int i = 0; i < ID_LEN; i++) {
				int index = random.nextInt(len) % len;
				buf[i] = DICT_TABLE[index];
			}
			
			ret = String.format("%s_%s", PERM_CONID_PREFIX, new String(buf));
			if (!MetaData.get().isPermnentTopicExistsById(ret))
				break;
		}
		
		return ret;
	}
	
	public static String genPermQueue() {
		int len = DICT_TABLE.length - 1;
		char[] buf = new char[QUEUE_LEN];
		
		Random random = new Random();
		
		for (int i = 0; i < QUEUE_LEN; i++) {
			int index = random.nextInt(len) % len;
			buf[i] = DICT_TABLE[index];
		}
		
		return String.format("%s_%s", PERM_QUEUE_PREFIX, new String(buf));
	}

}
