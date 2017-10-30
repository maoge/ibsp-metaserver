package ibsp.metaserver.eventbus;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.annotation.KVPair;
import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonObject;

public class EventBean {
	
	private static Logger logger = LoggerFactory.getLogger(EventBean.class.getName());
	
	@KVPair(key = FixHeader.HEADER_EVENT_CODE, val = "")
	private EventType evType;
	
	@KVPair(key = FixHeader.HEADER_QUEUE_ID, val = "")
	private String queueId;
	
	@KVPair(key = FixHeader.HEADER_QUEUE_NAME, val = "")
	private String queueName;
	
	@KVPair(key = FixHeader.HEADER_GROUP_ID, val = "")
	private String groupId;
	
	@KVPair(key = FixHeader.HEADER_VBROKER_ID, val = "")
	private String vbrokerId;
	
	@KVPair(key = FixHeader.HEADER_USER_ID, val = "")
	private String userId;
	
	@KVPair(key = FixHeader.HEADER_ROLE_IDS, val = "")
	private String roleIds;
	
	@KVPair(key = FixHeader.HEADER_GROUP_IDS, val = "")
	private String groupIds;

	@KVPair(key = FixHeader.HEADER_BROKER_ID, val = "")
	private String brokerId;
	
	@KVPair(key = FixHeader.HEADER_ACTIVE_COLL_INFO, val = "")
	private String activeCollInfo;

	@KVPair(key = FixHeader.HEADER_CLIENT_INFO, val = "")
	private String clientInfo;
	
	@KVPair(key = FixHeader.HEADER_LSNR_ADDR, val = "")
	private String lsnrAddr;
	
	@KVPair(key = FixHeader.HEADER_API_TYPE, val = "")
	private String apiType;
	@KVPair(key = FixHeader.HEADER_UUID, val = "")
	private String uuid;
	
	@KVPair(key = FixHeader.HEADER_JSONSTR, val = "")
	private String jsonStr;
	
	@KVPair(key = FixHeader.HEADER_USER_PWD, val = "")
	private String userPasswd;

	@KVPair(key = FixHeader.HEADER_TIMESTAMP, val = "")
	private String timeStamp;
	
	@KVPair(key = FixHeader.HEADER_BLACK_WHITE_LIST_IP, val = "")
	private String blackWhiteListIp;

	private static Class<?> CLAZZ;
	
	static {
		CLAZZ = EventBean.class;
	}
	
	public EventBean() {
		evType    = EventType.e0;
		queueId   = "";
		queueName = "";
		groupId   = "";
		vbrokerId = "";
		brokerId  = "";
		userId    = "";
		roleIds   = "";
		groupIds  = "";
		
		clientInfo= "";
		lsnrAddr  = "";
		
		activeCollInfo = "";
		
		apiType = "";
		uuid= "";
		
		jsonStr = "";
		userPasswd = "";
		timeStamp = "";
		blackWhiteListIp = "";
	}
	
	public EventBean(EventType evType) {
		this.evType = evType;
	}

	public EventType getEvType() {
		return evType;
	}

	public void setEvType(EventType evType) {
		this.evType = evType;
	}

	public String getQueueId() {
		return queueId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getVbrokerId() {
		return vbrokerId;
	}

	public void setVbrokerId(String vbrokerId) {
		this.vbrokerId = vbrokerId;
	}

	public String getBrokerId() {
		return brokerId;
	}

	public void setBrokerId(String brokerId) {
		this.brokerId = brokerId;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(String roleIds) {
		this.roleIds = roleIds;
	}
	
	public String getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(String groupIds) {
		this.groupIds = groupIds;
	}
	
	public String getActiveCollInfo() {
		return activeCollInfo;
	}

	public void setActiveCollInfo(String activeCollInfo) {
		this.activeCollInfo = activeCollInfo;
	}

	public String getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(String clientInfo) {
		this.clientInfo = clientInfo;
	}

	public String getLsnrAddr() {
		return lsnrAddr;
	}

	public void setLsnrAddr(String lsnrAddr) {
		this.lsnrAddr = lsnrAddr;
	}
	
	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getJsonStr() {
		return jsonStr;
	}

	public void setJsonStr(String jsonStr) {
		this.jsonStr = jsonStr;
	}
	
	public String getUserPasswd() {
		return userPasswd;
	}

	public void setUserPasswd(String userPasswd) {
		this.userPasswd = userPasswd;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getBlackWhiteListIp() {
		return blackWhiteListIp;
	}

	public void setBlackWhiteListIp(String blackWhiteListIp) {
		this.blackWhiteListIp = blackWhiteListIp;
	}

	public String getJsonString() {
		JsonObject jsonObj = new JsonObject();
		
		Field[] fields = CLAZZ.getDeclaredFields();
		for (Field field : fields) {
			KVPair kv = field.getAnnotation(KVPair.class);
			if (kv == null)
				continue;
			
			boolean error = false;
			Object obj = null;
			
			try {
				field.setAccessible(true);
				obj = field.get(this);
				if (obj == null)
					continue;
				
				if (obj instanceof EventType) {
					obj = ((EventType) obj).getValue();
				} else if (obj instanceof String) {
					if (obj.equals(""))
						continue;
				}
				
				if (obj != null) {
					String key = kv.key();
					jsonObj.put(key, obj);
				}
			} catch(IllegalArgumentException e) {
				logger.error(e.getMessage(), e);
				error = true;
			} catch(IllegalAccessException e) {
				logger.error(e.getMessage(), e);
				error = true;
			} finally {
				if (error)
					continue;
			}
		}
		
		return jsonObj.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activeCollInfo == null) ? 0 : activeCollInfo.hashCode());
		result = prime * result + ((apiType == null) ? 0 : apiType.hashCode());
		result = prime * result + ((brokerId == null) ? 0 : brokerId.hashCode());
		result = prime * result + ((clientInfo == null) ? 0 : clientInfo.hashCode());
		result = prime * result + ((evType == null) ? 0 : evType.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((groupIds == null) ? 0 : groupIds.hashCode());
		result = prime * result + ((jsonStr == null) ? 0 : jsonStr.hashCode());
		result = prime * result + ((lsnrAddr == null) ? 0 : lsnrAddr.hashCode());
		result = prime * result + ((queueId == null) ? 0 : queueId.hashCode());
		result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
		result = prime * result + ((roleIds == null) ? 0 : roleIds.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result + ((userPasswd == null) ? 0 : userPasswd.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + ((vbrokerId == null) ? 0 : vbrokerId.hashCode());
		result = prime * result + ((blackWhiteListIp == null) ? 0 : blackWhiteListIp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventBean other = (EventBean) obj;
		if (activeCollInfo == null) {
			if (other.activeCollInfo != null)
				return false;
		} else if (!activeCollInfo.equals(other.activeCollInfo))
			return false;
		if (apiType == null) {
			if (other.apiType != null)
				return false;
		} else if (!apiType.equals(other.apiType))
			return false;
		if (brokerId == null) {
			if (other.brokerId != null)
				return false;
		} else if (!brokerId.equals(other.brokerId))
			return false;
		if (clientInfo == null) {
			if (other.clientInfo != null)
				return false;
		} else if (!clientInfo.equals(other.clientInfo))
			return false;
		if (evType != other.evType)
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (groupIds == null) {
			if (other.groupIds != null)
				return false;
		} else if (!groupIds.equals(other.groupIds))
			return false;
		if (jsonStr == null) {
			if (other.jsonStr != null)
				return false;
		} else if (!jsonStr.equals(other.jsonStr))
			return false;
		if (lsnrAddr == null) {
			if (other.lsnrAddr != null)
				return false;
		} else if (!lsnrAddr.equals(other.lsnrAddr))
			return false;
		if (queueId == null) {
			if (other.queueId != null)
				return false;
		} else if (!queueId.equals(other.queueId))
			return false;
		if (queueName == null) {
			if (other.queueName != null)
				return false;
		} else if (!queueName.equals(other.queueName))
			return false;
		if (roleIds == null) {
			if (other.roleIds != null)
				return false;
		} else if (!roleIds.equals(other.roleIds))
			return false;
		if (timeStamp == null) {
			if (other.timeStamp != null)
				return false;
		} else if (!timeStamp.equals(other.timeStamp))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (userPasswd == null) {
			if (other.userPasswd != null)
				return false;
		} else if (!userPasswd.equals(other.userPasswd))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		if (vbrokerId == null) {
			if (other.vbrokerId != null)
				return false;
		} else if (!vbrokerId.equals(other.vbrokerId))
			return false;
		if (blackWhiteListIp == null) {
			if (other.blackWhiteListIp != null)
				return false;
		} else if (!blackWhiteListIp.equals(other.blackWhiteListIp))
			return false;
		return true;
	}
	
}
