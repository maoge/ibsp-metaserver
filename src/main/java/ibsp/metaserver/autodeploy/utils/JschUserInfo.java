package ibsp.metaserver.autodeploy.utils;

import ibsp.metaserver.exception.DeployException;
import ibsp.metaserver.exception.DeployException.DEPLOYERRINFO;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.HttpUtils;

import com.jcraft.jsch.UserInfo;

public class JschUserInfo implements UserInfo {

	private String user;
	private String passwd;
	private String host;
	private int sshPort;
	
	public JschUserInfo(String user, String passwd, String host, int sshPort) throws DeployException {
		
		if (HttpUtils.isNull(user) || HttpUtils.isNull(passwd)
				|| HttpUtils.isNull(host) || sshPort < 21) {
			throw new DeployException(CONSTS.ERR_HOSTINFO_NOT_COMPLETE, new Throwable(), DEPLOYERRINFO.e2);
		}
		
		this.user = user;
		this.passwd = passwd;
		this.host = host;
		this.sshPort = sshPort;
	}

	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public String getPassword() {
		return passwd;
	}

	@Override
	public boolean promptPassphrase(String paramString) {
		return true;
	}

	@Override
	public boolean promptPassword(String paramString) {
		return true;
	}

	@Override
	public boolean promptYesNo(String paramString) {
		return true;
	}

	@Override
	public void showMessage(String paramString) {
		
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
	
}
