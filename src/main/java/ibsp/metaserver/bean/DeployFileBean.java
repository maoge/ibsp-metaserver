package ibsp.metaserver.bean;

import java.util.HashMap;

public class DeployFileBean extends BeanMapper {

	private String fileType;
	private String fileName;
	private String ftpDir;
	
	private String ftpHost;
	private String ftpUser;
	private String ftpPwd;
	private String sshPort;

	public DeployFileBean(String fileType, String fileName, String ftpDir,
			String ftpHost, String ftpUser, String ftpPwd, String sshPort) {
		super();
		this.fileType = fileType;
		this.fileName = fileName;
		this.ftpDir = ftpDir;
		this.ftpHost = ftpHost;
		this.ftpUser = ftpUser;
		this.ftpPwd = ftpPwd;
		this.sshPort = sshPort;
	}
	
	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFtpDir() {
		return ftpDir;
	}

	public void setFtpDir(String ftpDir) {
		this.ftpDir = ftpDir;
	}

	public String getFtpHost() {
		return ftpHost;
	}

	public void setFtpHost(String ftpHost) {
		this.ftpHost = ftpHost;
	}

	public String getFtpUser() {
		return ftpUser;
	}

	public void setFtpUser(String ftpUser) {
		this.ftpUser = ftpUser;
	}

	public String getFtpPwd() {
		return ftpPwd;
	}

	public void setFtpPwd(String ftpPwd) {
		this.ftpPwd = ftpPwd;
	}

	public String getSshPort() {
		return sshPort;
	}

	public void setSshPort(String sshPort) {
		this.sshPort = sshPort;
	}

	public static DeployFileBean convert(HashMap<String, Object> mapper) {
		if (mapper == null)
			return null;
		
		String fileType = getFixDataAsString(mapper, "FILE_TYPE");
		String fileName = getFixDataAsString(mapper, "FILE_NAME");
		String ftpDir   = getFixDataAsString(mapper, "FILE_DIR");
		String ftpHost  = getFixDataAsString(mapper, "IP_ADDRESS");
		String ftpUser  = getFixDataAsString(mapper, "USER_NAME");
		String ftpPwd   = getFixDataAsString(mapper, "USER_PWD");
		String sshPort  = getFixDataAsString(mapper, "FTP_PORT");
		
		return new DeployFileBean(fileType, fileName, ftpDir, ftpHost, ftpUser, ftpPwd, sshPort);
	}

}
