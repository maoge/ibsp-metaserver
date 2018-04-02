package ibsp.metaserver.autodeploy.utils;

import java.io.File;
import java.io.IOException;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;

public class SCPFileUtils {
	
	private Connection connection;
	private SCPClient scpClient;
	
	public SCPFileUtils(String ip, String user, String password, int port) throws Exception {
        this.connection = new Connection(ip, port);
        this.connection.connect();
        boolean isAuthenticated = this.connection.authenticateWithPassword(user, password);
        if (!isAuthenticated) {
            throw new RuntimeException("Authentication failed!");
        }
        this.scpClient = connection.createSCPClient();
	}
	
	public void getFile(String file) throws IOException {
		this.scpClient.get(file, "./");
	}
	
	public void putFile(String context, String fileName, String fileDir) throws IOException {
		this.scpClient.put(context.getBytes(), fileName, fileDir);
	}
	
	public void deleteLocalFile(String fileName) {
		File file = new File("./"+fileName);
		if (!file.exists())
			return;
		file.delete();
	}
	
	public void close() {
		this.connection.close();
	}
}
