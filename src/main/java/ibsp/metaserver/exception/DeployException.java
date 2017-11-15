package ibsp.metaserver.exception;

public class DeployException extends Exception {
	private static final long serialVersionUID = 5230083280341862101L;

	public static enum DEPLOYERRINFO {
		DEFAULT(90010000),
		e1(90010001),        // 发布文件配置不存在
		e2(90010002),        // 主机信息不完整
		e3(90010003),        // SSH连接失败
		e4(90010004),        // 执行SHELL出错
		e5(90010005),        // 部署erl出错
		e6(90010006),        // mq port 被占用
		e7(90010007),        // 部署mq出错
		e8(90010008),        // 卸载mq出错
		e9(90010009),        // setHosts出错
		e10(90010010);       // no broker fund
		
		private int value;
		
		private DEPLOYERRINFO(int s) {
			value = s;
		}
		
		public int getValue() {
			// 得到枚举值代表的字符串。
			return value;
		}
	}
	
	private int errorCode;
	
	public DeployException(String message, Throwable cause, DEPLOYERRINFO errorInfo) {
		super(message, cause);
		this.errorCode = errorInfo.value;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
}
