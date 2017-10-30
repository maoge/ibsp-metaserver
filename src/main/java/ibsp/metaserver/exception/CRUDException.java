package ibsp.metaserver.exception;

public class CRUDException extends Exception {
	private static final long serialVersionUID = 5230083280341862101L;

	public static enum CRUDERRINFO {
		DEFAULT(80010000),
		e1(80010001),        // 连接池异常
		e2(80010002),        // 获取连接异常
		e3(80010003),        // 查询
		e4(80010004),        // 更新
		e5(80010005);        // setAutoCommit(false) error
		
		private int value;
		
		private CRUDERRINFO(int s) {
			value = s;
		}
		
		public int getValue() {
			// 得到枚举值代表的字符串。
			return value;
		}
	}
	
	private int errorCode;
	
	public CRUDException(String message, Throwable cause, CRUDERRINFO errorInfo) {
		super(message, cause);
		this.errorCode = errorInfo.value;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
}
