package ibsp.metaserver.bean;

import ibsp.metaserver.utils.FixHeader;

import java.util.Map;

public class ServerBean extends BeanMapper {
	
	private String serverIP;
	private String serverName;
	
	public ServerBean(String serverIP, String serverName) {
		super();
		this.serverIP = serverIP;
		this.serverName = serverName;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public static ServerBean convert(Map<String, Object> mapper) {
		if (mapper == null || mapper.isEmpty())
			return null;
		
		String serverIP   = getFixDataAsString(mapper, FixHeader.HEADER_SERVER_IP);
		String serverName = getFixDataAsString(mapper, FixHeader.HEADER_SERVER_NAME);
		
		return new ServerBean(serverIP, serverName);
	}

}
