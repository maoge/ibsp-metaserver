package ibsp.metaserver.bean;

import java.util.HashMap;

import ibsp.metaserver.utils.FixHeader;

public class UserBean extends BeanMapper {
	
	private String userId;
	private String userName;
	private String loginPwd;
	private String userStatus;    // 用户状态  1：正常; 2：注销
	private String onlineStatus;  // 用户在线状态  1：在线; 2：离线
	private String recStatus;     // 数据状态  0：新增; 1：修改; 2：删除
	private String recPerson;
	private long recTime;         // 添加时间
	
	public UserBean(String userId, String userName, String loginPwd, String userStatus, String onlineStatus,
			String recStatus, String recPerson, long recTime) {
		super();
		this.userId = userId;
		this.userName = userName;
		this.loginPwd = loginPwd;
		this.userStatus = userStatus;
		this.onlineStatus = onlineStatus;
		this.recStatus = recStatus;
		this.recPerson = recPerson;
		this.recTime = recTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getLoginPwd() {
		return loginPwd;
	}

	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public String getRecStatus() {
		return recStatus;
	}

	public void setRecStatus(String recStatus) {
		this.recStatus = recStatus;
	}
	
	public String getRecPerson() {
		return recPerson;
	}

	public void setRecPerson(String recPerson) {
		this.recPerson = recPerson;
	}

	public long getRecTime() {
		return recTime;
	}

	public void setRecTime(long recTime) {
		this.recTime = recTime;
	}
	
	public static UserBean convert(HashMap<String, Object> mapper) {
		if (mapper == null)
			return null;

		String userId = getFixDataAsString(mapper, FixHeader.HEADER_USER_ID);
		String userName = getFixDataAsString(mapper, FixHeader.HEADER_USER_NAME);
		String loginPwd = getFixDataAsString(mapper, FixHeader.HEADER_LOGIN_PWD);
		String userStatus = getFixDataAsString(mapper, FixHeader.HEADER_USER_STATUS);
		String onlineStatus = getFixDataAsString(mapper, FixHeader.HEADER_LINE_STATUS);
		String recStatus = getFixDataAsString(mapper, FixHeader.HEADER_REC_STATUS);
		String recPerson = getFixDataAsString(mapper, FixHeader.HEADER_REC_PERSON);
		long recTime = getFixDataAsLong(mapper, FixHeader.HEADER_REC_TIME);

		return new UserBean(userId, userName, loginPwd, userStatus, onlineStatus, recStatus, recPerson, recTime);
	}

	@Override
	public String toString() {
		return "UserBean [userId=" + userId + ", userName=" + userName + ", loginPwd=******, userStatus="
				+ userStatus + ", onlineStatus=" + onlineStatus + ", recStatus=" + recStatus + ", recPerson=" + recPerson + ", recTime="
				+ recTime + "]";
	}

}
