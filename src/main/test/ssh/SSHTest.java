package ssh;

import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.utils.CONSTS;

public class SSHTest {

	public static void main(String[] args) {
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			String ip = "192.168.188.71";
			String user  = "ultravirs";
			String pwd   = "wwwqqq.";
			
			JschUserInfo ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			if (executor.isPortUsed("5880", null)) {
				System.out.println("port is used!");
			} else {
				System.out.println("port is not used!");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connected) {
				executor.close();
			}
		}
	}

}
