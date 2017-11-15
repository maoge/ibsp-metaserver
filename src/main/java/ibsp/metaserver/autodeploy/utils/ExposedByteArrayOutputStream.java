package ibsp.metaserver.autodeploy.utils;

import java.io.ByteArrayOutputStream;

public class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
	
	private static byte[] YES_NO = { '(', 'y', 'e', 's', '/', 'n', 'o', ')', '?', ' ' };
	private static byte[] PASSWD = { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', ':', ' ' };
	
	public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
		if (index > 0 && index < size() - 1) {
			return buf[index];
		} else {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}
	
	public boolean emputy() {
		return count == 0;
	}
	
	// end with: "$ "
	public boolean dollarEof() {
		if (count < 2)
			return false;
		
		return (buf[count - 2] == 36 && buf[count - 1] == 32)
				|| (buf[count - 2] == 62 && buf[count - 1] == 32)
				|| (buf[count - 2] == 93 && buf[count - 1] == 36);
	}
	
	// end with: "$ " or "> " or "]$"
	public boolean sshEof() {
		if (count < 2)
			return false;
		
		//return (buf[count - 2] == 36 || buf[count - 2] == 62) && buf[count - 1] == 32;
		return (buf[count - 2] == 36 && buf[count - 1] == 32)
				|| (buf[count - 2] == 62 && buf[count - 1] == 32)
				|| (buf[count - 2] == 93 && buf[count - 1] == 36);
		
	}
	
	public boolean sshRootEof() {
		if (count < 2)
			return false;
		
		return (buf[count - 2] == 35 && buf[count - 1] == 32)
				|| (buf[count - 2] == 36 && buf[count - 1] == 32)
				|| (buf[count - 2] == 93 && buf[count - 1] == 36)
				|| (buf[count - 2] == 93 && buf[count - 1] == 35);
	}
	
	public boolean catEof() {
		if (count < 2)
			return false;
		
		return buf[count - 2] == 62 && buf[count - 1] == 32;
	}
	
	public boolean inputEof() {
		if (count < 2)
			return false;
		
		return buf[count - 2] == 58 && buf[count - 1] == 32;
	}
	
	//SSH  Are you sure you want to continue connecting (yes/no)? 
	public boolean checkYesOrNo() {
		int len = YES_NO.length;
		if (count < len)
			return false;
		
		boolean equal = true;
		
		for (int i = 0; i < len; i++) {
			if (YES_NO[i] != buf[count - len + i]) {
				equal = false;
				break;
			}
		}
		
		return equal;
	}
	
	// SSH input passwd
	public boolean isPasswd() {
		int len = PASSWD.length;
		if (count < len)
			return false;
		
		boolean equal = true;
		
		for (int i = 0; i < len; i++) {
			if (i == 0) {
				// in some version of linux, it starts with: 'P'
				if (Character.toUpperCase(PASSWD[i]) != Character.toUpperCase(buf[count - len + i])) {
					equal = false;
					break;
				}
			} else {
				if (PASSWD[i] != buf[count - len + i]) {
					equal = false;
					break;
				}
			}
		}
		
		return equal;
	}
}
