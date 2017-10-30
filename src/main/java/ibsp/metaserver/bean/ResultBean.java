package ibsp.metaserver.bean;

import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonObject;

public class ResultBean {
	
	private int retCode;
	private String retInfo;
	
	public ResultBean() {
		this.retCode = CONSTS.REVOKE_OK;
		this.retInfo = "";
	}

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	public String getRetInfo() {
		return retInfo;
	}

	public void setRetInfo(String retInfo) {
		this.retInfo = retInfo;
	}
	
	public String toJSON() {
		JsonObject jsonObj = new JsonObject();

		jsonObj.put(FixHeader.HEADER_RET_CODE, retCode);
		jsonObj.put(FixHeader.HEADER_RET_INFO, retInfo);

		return jsonObj.toString();
	}

	@Override
	public String toString() {
		return "ResultBean [RET_CODE=" + retCode + ", RET_INFO=" + retInfo + "]";
	}
	
}
