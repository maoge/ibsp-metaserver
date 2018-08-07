package ibsp.metaserver.autodeploy.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import ibsp.metaserver.bean.ResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ibsp.metaserver.utils.CONSTS;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SSHExecutor {
	private static Logger logger = LoggerFactory.getLogger(SSHExecutor.class);

	private static int CONN_TIMEOUT = 3000;
	private static long WAIT_TIMEOUT = 10L;
	private static long WAIT_PORT_TIMEOUT = 30000L;

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
	private static final String CMD_ERL      = "erl";
	private static final String CMD_SED      = "sed";
	private static final String CMD_SET_PS1  = "export PS1=\"[\\u@\\h \\W]\\$\" \n";
	
	private static final String ERL_ROOT_DIR = "ERL_ROOT_DIR";

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
		
		// set PS1 to surpport format
		try {
			generalCommand(CMD_SET_PS1);
		} catch (Exception e) {
		}
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
		if (logger.isTraceEnabled())
			logger.trace(result);

		return result;
	}
	
	public boolean execSingleLine(String command, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s\n", command);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return true;
	}

	public boolean cd(String path, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s %s\n", CMD_CD, path);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return true;
	}
	
	public boolean cd(String path) throws InterruptedException {
		String cmd = String.format("%s %s\n", CMD_CD, path);
		generalCommand(cmd);
		return true;
	}

	public boolean pwd(String sessionKey) throws InterruptedException {
		String cmd = String.format("%s\n", CMD_PWD);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return true;
	}

	public boolean cdHome(String sessionKey) throws InterruptedException {
		cd("$HOME", sessionKey);
		return true;
	}
	
	public String getHome() throws InterruptedException {
		String cmd = String.format("%s %s \n", CMD_ECHO, "$HOME");
		String result = generalCommand(cmd);
//		return result.split(System.lineSeparator())[1];
		return result.split("\r\n")[1];
	}

	public boolean cp(String srcFile, String destDir, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s %s %s\n", CMD_CP, srcFile, destDir);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return true;
	}

	public boolean mv(String oldFile, String newFile) throws InterruptedException {
		String cmd = String.format("%s %s %s\n", CMD_MV, oldFile, newFile);
		String context = generalCommand(cmd);
		return context.indexOf(CONSTS.NO_SUCH_FILE) != -1 ? true : false;
	}

	public boolean rm(String file, boolean recursive, String sessionKey) throws InterruptedException {
		String extend = recursive ? "-rf" : "";
		String cmd = String.format("%s %s %s\n", CMD_RM, extend, file);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return context.indexOf(CONSTS.NO_SUCH_FILE) != -1 ? true : false;
	}

	public boolean isDirExistInCurrPath(String fileDir, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s -d %s\n", CMD_FILE, fileDir);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
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
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return true;
	}
	
	// sed -i "s/%JDK_ROOT_PATH%/home/g" access.sh
	public boolean sed(String src, String des, String fileName, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s -i 's/%s/%s/g' %s %s", CMD_SED, src, des, fileName, CONSTS.LINE_SEP);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return true;
	}
	
	public boolean rmLine(String s, String fileName, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s -i '/^%s/d' %s %s", CMD_SED, s, fileName, CONSTS.LINE_SEP);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return true;
	}
	
	public boolean addLine(String s, String fileName, String sessionKey) throws InterruptedException {
		// sed -i '$a aaa' test.txt
		String cmd = String.format("%s -i '$a %s' %s %s",  CMD_SED, s, fileName, CONSTS.LINE_SEP);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return true;
	}

	public boolean isFileExistInCurrPath(String file, String sessionKey) throws InterruptedException {
		String cmd = String.format("%s -al\n", CMD_LS);
		String context = generalCommand(cmd);

		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);

		return context.indexOf(file) != -1 ? true : false;
	}
	
	public boolean pdctlDeletePdMember(String ip, String port, String name, String sessionKey) throws InterruptedException {
		String cmd = String.format("./bin/pd-ctl -u http://%s:%s -d member delete name %s \n", ip, port, name);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return context.indexOf(CONSTS.PD_DELETE_MEMBER_SUCC) != -1 ? Boolean.TRUE : Boolean.FALSE;
	}
	
	public boolean pdctlDeleteTikvStore(String pdIp,String pdPort, int id,String sessionKey) throws InterruptedException {
		String cmd = String.format("./bin/pd-ctl -u http://%s:%s -d store delete %s \n", pdIp, pdPort,id);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		
		return context.indexOf(CONSTS.PD_DELETE_STORE_SUCC) != -1 ? Boolean.TRUE : Boolean.FALSE;
	}
	
	public JsonArray pdctlGetStore(String ip, String port) throws InterruptedException {
		String cmd = String.format("./bin/pd-ctl -u http://%s:%s -d store \n", ip, port);
		String context = generalCommand(cmd);
		int start = context.indexOf(cmd);
		JsonObject stores = new JsonObject(context.substring(start+cmd.length()));
		return stores.getJsonArray("stores");
	}
	
	public Integer getStoreId(String pdIp,String pdPort ,String ip, String port) throws InterruptedException {
		JsonArray arr = pdctlGetStore(pdIp,pdPort);
		int res = 0;
		for(Object obj : arr) {
			JsonObject json = (JsonObject) obj;
			JsonObject store = json.getJsonObject("store");
			String address = store.getString("address");
			if(address.equals(ip+":"+port)) {
				return store.getInteger("id");
			}
		}
		return res;
	}
	
	public boolean pdctlStoreState(String ip,String port, int id) throws InterruptedException {
		String cmd = String.format("./bin/pd-ctl -u http://%s:%s -d store %s \\n",ip, port, id);
		String context = generalCommand(cmd);
		int start = context.indexOf(cmd);
		JsonObject store = new JsonObject(context.substring(start+cmd.length()));
		String stateName = store.getString("state_name");
		return stateName.equals(CONSTS.TIKV_TOMBSTONE_STATUS);
	}

	public boolean scp(String user, String passwd, String srcHost, String src, String des, String sshPort , String sessionKey)
			throws InterruptedException {
		String cmd = String.format("%s -P %s %s@%s:%s %s\n", CMD_SCP, sshPort, user, srcHost, src, des);
		commander.print(cmd);

		long start = System.currentTimeMillis();

		while (true) {
			Thread.sleep(WAIT_TIMEOUT);

			if (bout.checkYesOrNo()) {
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));
				
				bout.reset();

				cmd = String.format("%s\n", "yes");
				commander.print(cmd);
				do {
					Thread.sleep(WAIT_TIMEOUT);

					long curr = System.currentTimeMillis();
					if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
						if (sessionKey != null)
							DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
						
						throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
					}
				} while (!bout.isPasswd());
			}

			if (bout.inputEof())
				break;

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		}

		String context = new String(bout.toByteArray());
		if (context.indexOf(CONSTS.COMMAND_NOT_FOUND) != -1) {
			String errInfo = String.format(CONSTS.ERR_COMMAND_NOT_FOUND, CMD_SCP);
			throw new InterruptedException(errInfo);
		}
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		bout.reset();

		cmd = String.format("%s\n", passwd);
		commander.print(cmd);
		while (true) {
			Thread.sleep(WAIT_TIMEOUT);

			if (!bout.emputy()) {
				if (logger.isTraceEnabled())
					logger.trace(bout.toString());
				
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));

				if (bout.sshEof()) {
					bout.reset();
					break;
				}

				bout.reset();
			}

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		}

		if (logger.isTraceEnabled())
			logger.trace(bout.toString());
		
		if (sessionKey != null)
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
					if (sessionKey != null)
						DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));
					
					bout.reset();
					break;
				}
				
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, new String(bout.toByteArray()));
				
				bout.reset();
			}

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				if (sessionKey != null)
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
				if (sessionKey != null)
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
					if (sessionKey != null)
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
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshRootEof()); // /etc/hosts文件只能以root用户修改 root用户命令行以"# "结束

		if (logger.isTraceEnabled())
			logger.trace(bout.toString());
		
		if (sessionKey != null)
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
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.catEof());

		cmd = String.format("%s PATH=\\$HOME/%s/bin:$PATH\n", CMD_EXPORT, path);
		commander.print(cmd);
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				if (sessionKey != null)
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
				if (sessionKey != null)
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
		generalCommand(cmd);
		return true;
	}

	public boolean addExecMod(String file) throws InterruptedException {
		String cmd = String.format("%s +x %s\n", CMD_CHMOD, file);
		generalCommand(cmd);
		return true;
	}

	public boolean chmod(String file, String mode) throws InterruptedException {
		String cmd = String.format("%s %s %s\n", CMD_CHMOD, mode, file);
		generalCommand(cmd);
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
		return isPortUsed(String.valueOf(port), null);
	}

	public void echo(String text) throws InterruptedException {
		bout.reset();
		
		String cmd = String.format("%s %s \n", CMD_ECHO, text);
		generalCommand(cmd);
	}

	public boolean isPortUsed(String port, String sessionKey) throws InterruptedException {
		bout.reset();

		String cmd = String.format("%s -an | grep LISTEN | awk '{print $4}' | grep :%s$ | wc -l\n", CMD_NETSTAT, port);
		String context = generalCommand(cmd);
		
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);

		int lastLeftBracket = context.lastIndexOf(CONSTS.SQUARE_BRACKET_LEFT);
		int end = context.lastIndexOf(CONSTS.LINE_SEP, lastLeftBracket);
		int begin = context.lastIndexOf(CONSTS.LINE_SEP, end - CONSTS.LINE_SEP.length());
		String lines = context.substring(begin + CONSTS.LINE_SEP.length(), end);
		
		//int begin = context.indexOf(CONSTS.LINE_SEP);
		//int end = context.indexOf(CONSTS.SQUARE_BRACKET_LEFT, begin + CONSTS.LINE_SEP.length());
		//String lines = context.substring(begin + CONSTS.LINE_SEP.length(), end);

		return !lines.startsWith("0");
	}
	
	public boolean isErlCmdValid(String sessionKey) throws InterruptedException {
		boolean erlExist = false;
		String cmd = String.format("%s --version%s", CMD_ERL, CONSTS.LINE_SEP);
		commander.print(cmd);

		long start = System.currentTimeMillis();

		while (true) {
			Thread.sleep(WAIT_TIMEOUT);

			if (bout.catEof()) {
				erlExist = true;
				break;
			} else if (bout.sshEof()) {
				break;
			}

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		}

		String context = bout.toString();
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);
		bout.reset();

		if (erlExist) {
			cmd = String.format("q().%s", CONSTS.LINE_SEP);
			commander.print(cmd);
			do {
				Thread.sleep(WAIT_TIMEOUT);

				long curr = System.currentTimeMillis();
				if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
					if (sessionKey != null)
						DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
					
					throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
				}
			} while (!bout.dollarEof());
		}

		context = bout.toString();
		bout.reset();
		if (sessionKey != null)
			DeployLog.pubLog(sessionKey, context);

		// erl 进程退出比较慢
		Thread.sleep(3000L);

		return erlExist;
	}
	
	public boolean addErlRootDirEnv(String path, String sessionKey) throws InterruptedException {
		cdHome(sessionKey);

		String cmd = String.format("%s >> %s << %s%s", CMD_CAT, CONSTS.BASH_PROFILE, CMD_CAT_END, CONSTS.LINE_SEP);
		commander.print(cmd);
		long start = System.currentTimeMillis();
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.catEof());

		cmd = String.format("%s %s=\\$HOME/%s%s", CMD_EXPORT, ERL_ROOT_DIR, path, CONSTS.LINE_SEP);
		commander.print(cmd);
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.catEof());

		cmd = String.format("%s%s", CMD_CAT_END, CONSTS.LINE_SEP);
		commander.print(cmd);
		do {
			Thread.sleep(WAIT_TIMEOUT);

			long curr = System.currentTimeMillis();
			if ((curr - start) > CONSTS.SSH_CMD_TIMEOUT) {
				if (sessionKey != null)
					DeployLog.pubLog(sessionKey, CONSTS.SSH_TIMEOUT_INFO);
				
				throw new InterruptedException(CONSTS.SSH_TIMEOUT_INFO);
			}
		} while (!bout.sshEof());

		bout.reset();

		return true;
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
				hostname = context.substring(begin, end - CONSTS.LINE_SEP.length());
			}
		}

		return fund ? hostname : null;
	}
	
	public boolean waitProcessStart(String port, String sessionKey) {
		return waitProcessStart(port, sessionKey, null);
	}

	public boolean waitProcessStart(String port, String sessionKey, ResultBean result) {
		boolean ret = true;
		try {
			long beginTs = System.currentTimeMillis();
			long currTs = beginTs;

			do {
				Thread.sleep(CONSTS.DEPLOY_CHECK_INTERVAL);
				currTs = System.currentTimeMillis();
				if ((currTs - beginTs) > WAIT_PORT_TIMEOUT) {
					ret = false;
					if (result != null) {
						result.setRetCode(CONSTS.REVOKE_NOK);
						result.setRetInfo("Start process timeout...");
					}
					break;
				}
				this.echo("......");
			} while (!this.isPortUsed(port, sessionKey));
		} catch (Exception e) {
			ret = false;
		}

		return ret;
	}
	
	public boolean waitProcessStop(String port, String sessionKey) {
		boolean ret = true;
		try {
			long beginTs = System.currentTimeMillis();
			long currTs = beginTs;
			
			do {
				Thread.sleep(CONSTS.DEPLOY_CHECK_INTERVAL);
				currTs = System.currentTimeMillis();
				if ((currTs - beginTs) > WAIT_PORT_TIMEOUT) {
					ret = false;
					break;
				}
				this.echo("......");
			} while (this.isPortUsed(port, sessionKey));
		} catch (Exception e) {
			ret = false;
		}
		
		return ret;
	}

	public boolean isRabbitRunning(String port, String sessionKey) throws InterruptedException {
		String cmd = String.format("ps -ef | grep erl | grep \"{\\\"auto\\\",%s}\" %s", port, CONSTS.LINE_SEP);
		String context = generalCommand(cmd);

		DeployLog.pubLog(sessionKey, context);

		return  context.indexOf("rabbitmq") != -1;
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
