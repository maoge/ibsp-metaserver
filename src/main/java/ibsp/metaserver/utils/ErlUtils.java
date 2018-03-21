package ibsp.metaserver.utils;

import java.util.Random;

public class ErlUtils {
	
	private static int COOKIE_LEN = 20;
	
	private static char[] DICT_TABLE = {'A', 'B', 'C', 'D', 'E', 'F', 'G',
										'H', 'I', 'J', 'K', 'L', 'M', 'N',
										'O', 'P', 'Q', 'R', 'S', 'T',
										'U', 'V', 'W', 'X', 'Y', 'Z'}; 
	
	public static String genErlCookie() {
		char[] buf = new char[COOKIE_LEN];
		Random random = new Random();
		
		for (int i = 0; i < COOKIE_LEN; i++) {
			int index = random.nextInt(COOKIE_LEN) % COOKIE_LEN;
			buf[i] = DICT_TABLE[index];
		}
		
		return new String(buf);
	}
	
}
