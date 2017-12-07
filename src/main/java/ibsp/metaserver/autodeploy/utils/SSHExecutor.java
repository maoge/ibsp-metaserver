package ibsp.metaserver.autodeploy.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ibsp.metaserver.utils.CONSTS;

public class SSHExecutor {
	private static Logger logger = LoggerFactory.getLogger(SSHExecutor.class);

	private static int CONN_TIMEOUT = 3000;
	private static long WAIT_TIMEOUT = 10L;

	private static final String CMD_SETH_PLUS= "set +H";
	private static final String CMD_CD       = "cd";
	private static final String CMD_PWD      = "pwd";
	private static final String CMD_CP       = "cp";
	private static final String CMD_SCP      = "scp";
	private static final String CMD_FILE     = "file";
	private static final String CMD_MKDIR    = "mkdir";
	private static final String CMD_LS       = "ls";
	private static final String CMD_TAR      = "tar";
	private static final String CMD_EXPORT   = "export";
	private static final String CMD_CAT      = "cat";
	private static final String CMD_CAT_END  = "EOF";
	private static final String CMD_SOURCE   = "source";
	private static final String CMD_NETSTAT  = "netstat";
	private static final String CMD_ECHO     = "echo";
	private static final String CMD_MV       = "mv";
	private static final String CMD_RM       = "rm";
	private static final String CMD_CHMOD    = "chmod";
	private static final String CMD_HOSTNAME = "hostname";
	private static final String CMD_HNAME2IP = "hname2ip";

	private JschUserInfo ui;
	private JSch jsch;
	private Session session;
	private Channel channel;

	PrintStream commander;
	ExposedByteArrayOutputStream bout;

	public SSHExecutor(JschUserInfo userInfo) throws JSchException {
		this.ui = userInfo;

		jsch = new JSch();
		session = jsch.getSession(ui.getUser(), ui.getHost(), ui.getSshPort());
		session.setUserInfo(ui);
	}

	public void connect() throws JSchException, IOException {
		if (ui == null) {
			logger.error("JschUserInfo not set ......");
			throw new JSchException("JschUserInfo not set ......");
		}

		session.connect(CONN_TIMEOUT);
		channel = session.openChannel("shell");

		commander = new PrintStream(channel.getOutputStream(), true);

		bout = new ExposedByteArrayOutputStream();
		channel.setOutputStream(bout, true);

		channel.connect(CONN_TIMEOUT);
	}

	public void close() {
		assert (commander != null);
		commander.close();

		assert (session != null);
		session.disconnect();
	}

	private String generalCommand(String command) throws InterruptedException {
		if (command == null || command.isEmpty())
			return "";

		commander.print(command);
		long start = System.currentTimeMillis();

		do {
			Thread.sleep(WAIT_TIMEOUT);
			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());

		String result = bout.toString();
		bout.reset();

		return result;
	}
	
	public boolean execSingleLine(String command, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s\n", command);
		String context = generalCommand(cmd);

		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		return true;
	}

	public boolean cd(String path, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s %s\n", CMD_CD, path);
		String context = generalCommand(cmd);

		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		return true;
	}

	public boolean pwd(String sessionKey) throws InterruptedException {
		String cmd = String.format("%s\n", CMD_PWD);
		String context = generalCommand(cmd);

		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		return true;
	}

	public boolean cdHome(String sessionKey) throws InterruptedException {
		cd("$HOME", sessionKey);
		return true;
	}

	public boolean cp(String srcFile, String destDir, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s %s %s\n", CMD_CP, srcFile, destDir);
		String context = generalCommand(cmd);

		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		return true;
	}

	public boolean mv(String oldFile, String newFile) throws InterruptedException {
		String cmd = String.format("%s %s %s\n", CMD_MV, oldFile, newFile);
		String context = generalCommand(cmd);

		if (logger.isTraceEnabled())
			logger.trace(context);

		return context.indexOf(CONSTS.NO_SUCH_FILE) != -1 ? true : false;
	}

	public boolean rm(String file, boolean recursive, String sessionKey) throws InterruptedException {
		String extend = recursive ? "-rf" : "";
		String cmd = String.format("%s %s %s\n", CMD_RM, extend, file);
		String context = generalCommand(cmd);

		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		return context.indexOf(CONSTS.NO_SUCH_FILE) != -1 ? true : false;
	}

	public boolean isDirExistInCurrPath(String fileDir, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s -d %s\n", CMD_FILE, fileDir);
		String context = generalCommand(cmd);

		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		if (context.indexOf(CONSTS.COMMAND_NOT_FOUND) != -1) {
			String errInfo = String.format(CONSTS.ERR_COMMAND_NOT_FOUND, CMD_FILE);
			throw new InterruptedException(errInfo);
		}

		// 用户权限不高时不能用下面的方式判断
		return context.indexOf(CONSTS.FILE_DIR_NOT_EXISTS) != -1 ? false : true;
	}

	public boolean mkdir(String fileDir, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s -p %s\n", CMD_MKDIR, fileDir);
		String context = generalCommand(cmd);

		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		return true;
	}

	public boolean isFileExistInCurrPath(String file, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s -al\n", CMD_LS);
		String context = generalCommand(cmd);

		DeployLog.pubLog(sessionKey, context);

		return context.indexOf(file) != -1 ? true : false;
	}

	public boolean scp(String user, String passwd, String srcHost, String src, String des, String sshPort , String sessionKey)
			throws InterruptedException {
		String cmd = String.format("%s -P %s %s@%s:%s %s\n", CMD_SCP, sshPort, user, srcHost, src, des);
		commander.print(cmd);

		long start = System.currentTimeMillis();

		while (true) {
			Thread.sleep(WAIT_TIMEOUT);

			if (bout.checkYesOrNo()) {
				DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));
				bout.reset();

				cmd = String.format("%s\n", "yes");
				commander.print(cmd);
				do {
					Thread.sleep(WAIT_TIMEOUT);

					long curr = System.currentTimeMillis();
					if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
						DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
						throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
					}
				} while (!bout.isPasswd());
			}

			if (bout.inputEof())
				break;

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		}

		String context = new String(bout.toByteArray());
		if (context.indexOf(CONSTS.COMMAND_NOT_FOUND) != -1) {
			String errInfo = String.format(CONSTS.ERR_COMMAND_NOT_FOUND, CMD_SCP);
			throw new InterruptedException(errInfo);
		}

		DeployLog.pubLog(sessionKey, context);
		bout.reset();

		cmd = String.format("%s\n", passwd);
		commander.print(cmd);
		while (true) {
			Thread.sleep(WAIT_TIMEOUT);

			if (!bout.emputy()) {
				if (logger.isTraceEnabled())
					logger.trace(bout.toString());

				DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));

				if (bout.sshEof()) {
					bout.reset();
					break;
				}

				bout.reset();
			}

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		}

		if (logger.isTraceEnabled())
			logger.trace(bout.toString());

		DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));

		bout.reset();

		return true;
	}

	public boolean tgzUnpack(String fileName, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s -zxvf %s\n", CMD_TAR, fileName);
		commander.print(cmd);
		long start = System.currentTimeMillis();
		while (true) {
			Thread.sleep(WAIT_TIMEOUT);

			if (!bout.emputy()) {
				if (bout.sshEof()) {
					DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));
					bout.reset();
					break;
				}
				
				DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));
				bout.reset();
			}

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));
				bout.reset();
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		}
		
		return true;
	}

	public boolean addHosts(Vector<String> hostLines, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s >> %s << %s\n", CMD_CAT, CONSTS.HOSTS_FILE, CMD_CAT_END);
		commander.print(cmd);
		long start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.catEof());

		for (int i = 0; i < hostLines.size(); i++) {
			cmd = String.format("%s\n", hostLines.get(i));
			commander.print(cmd);
			do {
				Thread.sleep(WAIT_TIMEOUT);

				long curr = System.currentTimeMillis();
				if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
					throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
				}
			} while (!bout.catEof());
		}

		cmd = String.format("%s\n", CMD_CAT_END);
		commander.print(cmd);
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshRootEof()); // /etc/hosts文件只能以root用户修改 root用户命令行以"# "结束

		if (logger.isTraceEnabled())
			logger.trace(bout.toString());

		DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));
		bout.reset();

		return true;
	}

	public boolean checkHostExist(String hName) throws InterruptedException {
		String cmd = String.format("%s -n %s\n", CMD_HNAME2IP, hName);
		String context = generalCommand(cmd);
		return context.indexOf(CONSTS.NO_MAPPING_IN_HOSTS) == -1;
	}

	public boolean addPathToEnv(String path, String sessionKey) throws InterruptedException {
		cdHome(sessionKey);

		String cmd = String.format("%s >> %s << %s\n", CMD_CAT, CONSTS.BASH_PROFILE, CMD_CAT_END);
		commander.print(cmd);
		long start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.catEof());

		cmd = String.format("%s PATH=$HOME/%s/bin:$PATH\n", CMD_EXPORT, path);
		commander.print(cmd);
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.catEof());

		cmd = String.format("%s\n", CMD_CAT_END);
		commander.print(cmd);
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());

		if (logger.isTraceEnabled())
			logger.trace(bout.toString());
		bout.reset();

		return true;
	}

	public boolean validateEnv() throws InterruptedException {
		String cmd = String.format("%s %s\n", CMD_SOURCE, "$HOME/" + CONSTS.BASH_PROFILE);
		String context = generalCommand(cmd);
		if (logger.isTraceEnabled())
			logger.trace(context);
		return true;
	}

	public boolean addExecMod(String file) throws InterruptedException {
		String cmd = String.format("%s +x %s\n", CMD_CHMOD, file);
		String context = generalCommand(cmd);
		if (logger.isTraceEnabled())
			logger.trace(context);
		return true;
	}

	public boolean chmod(String file, String mode) throws InterruptedException {
		String cmd = String.format("%s %s %s\n", CMD_CHMOD, mode, file);
		String context = generalCommand(cmd);
		if (logger.isTraceEnabled())
			logger.trace(context);
		return true;
	}

	public boolean createStartShell(String shellContext) throws InterruptedException {
		return createShell(shellContext, CONSTS.START_SHELL);
	}

	public boolean createStopShell(String shellContext) throws InterruptedException {
		return createShell(shellContext, CONSTS.STOP_SHELL);
	}
	
	private boolean createShell(String shellContext, String shell) throws InterruptedException {
		
		//if shell exists, delete it
		if (isFileExistInCurrPath(shell, "")) {
			this.rm(shell, false, "");
		}
		
		String cmd = String.format("%s\n", CMD_SETH_PLUS);
		commander.print(cmd);
		long start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());
		
		cmd = String.format("%s -e \"%s\">>%s\n", CMD_ECHO, CONSTS.SHELL_MACRO, shell);
		commander.print(cmd);
		start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());
		
		// new line
		cmd = String.format("%s -e \"\\\n\">>%s\n", CMD_ECHO, shell);
		commander.print(cmd);
		start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());
		
		cmd = String.format("%s -e \"%s\">>%s\n", CMD_ECHO, shellContext, shell);
		commander.print(cmd);
		start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());
		
		if (logger.isTraceEnabled())
			logger.trace(bout.toString());
		bout.reset();

		return addExecMod(shell);
	}
	
	public String readStartShell() throws InterruptedException {
		return readShell(CONSTS.START_SHELL);
	}

	public String readStopShell(String shellContext) throws InterruptedException {
		return readShell(CONSTS.STOP_SHELL);
	}
	
	private String readShell(String shell) throws InterruptedException {
		String cmd = String.format("%s %s\n", CMD_CAT, shell);
		commander.print(cmd);
		long start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());

		if (logger.isTraceEnabled())
			logger.trace(bout.toString());

		String result = bout.toString();
		bout.reset();
		return result.substring(result.indexOf("\r\n")+2, result.lastIndexOf("\r\n"));
	}

	public boolean execStartShell(String sessionKey) throws InterruptedException {
		return execShell(CONSTS.START_SHELL, sessionKey);
	}

	public boolean execStopShell(String sessionKey) throws InterruptedException {
		return execShell(CONSTS.STOP_SHELL, sessionKey);
	}

	private boolean execShell(String shell, String sessionKey) throws InterruptedException {
		String cmd = String.format("./%s\n", shell);
		commander.print(cmd);
		long start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());

		if (logger.isTraceEnabled())
			logger.trace(bout.toString());

		DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));

		bout.reset();

		return true;
	}

	public boolean isCmdValid(String command, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s --version\n", command);
		String context = generalCommand(cmd);
		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		return context.indexOf(CONSTS.COMMAND_NOT_FOUND) != -1 ? false : true;
	}

	public boolean isPortUsed(int port) throws InterruptedException {
		bout.reset();

		String cmd = String.format("%s -an | awk '{print $4}' | grep :%d$ | wc -l\n", CMD_NETSTAT, port);
		String context = generalCommand(cmd);
		if (logger.isTraceEnabled())
			logger.trace(context);

		int begin = context.indexOf(CMD_NETSTAT);
		begin = context.indexOf(CMD_NETSTAT, begin + CMD_NETSTAT.length());
		begin = context.indexOf(CONSTS.LINE_SEP, begin + CMD_NETSTAT.length());
		int end = context.indexOf(CONSTS.SQUARE_BRACKET_LEFT, begin + CONSTS.LINE_SEP.length());
		String lines = context.substring(begin + CONSTS.LINE_SEP.length(), end);

		return lines.startsWith("0") ? false : true;
	}

	public void echo(String text) throws InterruptedException {
		bout.reset();

		String cmd = String.format("%s %s \n", CMD_ECHO, text);
		String context = generalCommand(cmd);
		if (logger.isTraceEnabled())
			logger.trace(context);
	}

	public boolean isPortUsed(String port, String sessionKey) throws InterruptedException {
		bout.reset();

		String cmd = String.format("%s -an | awk '{print $4}' | grep :%s$ | wc -l\n", CMD_NETSTAT, port);
		String context = generalCommand(cmd);
		if (logger.isTraceEnabled())
			logger.trace(context);

		DeployLog.pubLog(sessionKey, context);

		int begin = context.indexOf(CONSTS.LINE_SEP);
		int end = context.indexOf(CONSTS.SQUARE_BRACKET_LEFT, begin + CONSTS.LINE_SEP.length());
		String lines = context.substring(begin + CONSTS.LINE_SEP.length(), end);

		return !lines.startsWith("0");
	}

	public String getHostname() throws InterruptedException {
		boolean fund = false;
		String cmd = String.format("%s\n", CMD_HOSTNAME);
		String context = generalCommand(cmd);
		String hostname = null;

		int begin = context.indexOf(CMD_HOSTNAME);
		begin = context.indexOf(CMD_HOSTNAME, begin + CMD_HOSTNAME.length());
		begin = context.indexOf(CONSTS.LINE_SEP, begin + CMD_HOSTNAME.length());
		if (begin != -1) {
			begin += CONSTS.LINE_SEP.length();
			int end = context.indexOf(CONSTS.LINE_SEP, begin);
			if (end != -1) {
				fund = true;
				hostname = context.substring(begin, end);
			}
		}

		return fund ? hostname : null;
	}

	public JschUserInfo getUserInfo() {
		return ui;
	}

	public void setUserInfo(JschUserInfo userInfo) {
		this.ui = userInfo;
	}

	public void clearBuffer() {
		bout.reset();
	}
	
}
