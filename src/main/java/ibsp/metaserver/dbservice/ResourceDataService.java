package ibsp.metaserver.dbservice;

import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.exception.CRUDException;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceDataService {
	
	private static Logger logger = LoggerFactory.getLogger(ResourceDataService.class);
		
	private static final String SEL_SERVER_LIST =
			"SELECT SERVER_IP, SERVER_NAME FROM t_server WHERE 1=1 ";
	private static final String COUNT_SERVER_LIST =
			"SELECT count(0) count FROM t_server WHERE 1=1 ";
	private static final String ADD_SERVER =
			"INSERT INTO t_server (SERVER_IP) VALUES (?)";
	private static final String UPDATE_SERVER_NAME =
			"UPDATE t_server SET SERVER_NAME=? WHERE SERVER_IP=?";
	private static final String DELETE_SERVER =
			"DELETE FROM t_server WHERE SERVER_IP IN ";
	
	private static final String SEL_SSH_LIST =
			"SELECT SSH_NAME, SSH_PWD, SERV_TYPE FROM t_ssh WHERE SERVER_IP=? ";
	private static final String COUNT_SSH_LIST =
			"SELECT count(0) count FROM t_ssh WHERE SERVER_IP=? ";
	private static final String ADD_SSH =
			"INSERT INTO t_ssh (SSH_PWD, SERV_TYPE, SSH_NAME, SERVER_IP) VALUES (?,?,?,?)";
	private static final String UPDATE_SSH =
			"UPDATE t_ssh SET SSH_PWD=?,SERV_TYPE=? WHERE SSH_NAME=? AND SERVER_IP=?";
	//if attr_id should be put to CONSTS
	private static final String UPDATE_PWD_FOR_INSTANCE =
			"UPDATE t_instance_attr SET ATTR_VALUE=? WHERE ATTR_ID=109 "+
			"AND INST_ID IN (SELECT INST_ID FROM t_instance_attr WHERE ATTR_ID=100 AND ATTR_VALUE=?) "+ 
            "AND INST_ID IN (SELECT INST_ID FROM t_instance_attr WHERE ATTR_ID=108 AND ATTR_VALUE=?)";
	private static final String DELETE_SSH =
			"DELETE FROM t_ssh WHERE SERVER_IP=? AND SSH_NAME IN ";
	private static final String DELETE_SSH_BY_IP =
			"DELETE FROM t_ssh WHERE SERVER_IP IN ";
	
	private static final String GET_BY_SERVICE_TYPE = 
			"SELECT t_server.SERVER_IP, SSH_NAME, SSH_PWD FROM t_server JOIN t_ssh ON "
			+ "t_server.SERVER_IP=t_ssh.SERVER_IP WHERE SERV_TYPE LIKE ?";

	
	public static JsonArray getServerList(Map<String, String> params, ResultBean result) {
		
		StringBuilder sb = new StringBuilder(SEL_SERVER_LIST);
		extendSQLForServerList(params, sb);
		if (params!=null) {
			int pageSize = Integer.parseInt(params.get(FixHeader.HEADER_PAGE_SIZE));
			int pageNumber = Integer.parseInt(params.get(FixHeader.HEADER_PAGE_NUMBER));
			int start = pageSize*(pageNumber-1);
			if(HttpUtils.isNotNull(pageSize)&&HttpUtils.isNotNull(pageNumber)){
				sb.append(" limit "+start+","+pageSize+" ");
			}
		}
		SqlBean sql = new SqlBean(sb.toString());
		CRUD curd = new CRUD();
		curd.putSqlBean(sql);
		
		try {
			return curd.queryForJSONArray();
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		}
		return null;
	}
	
	public static JsonObject getServerCount(Map<String, String> params, ResultBean result) {
		
		StringBuilder sb = new StringBuilder(COUNT_SERVER_LIST);
		extendSQLForServerList(params, sb);
		SqlBean sql = new SqlBean(sb.toString());
		CRUD curd = new CRUD();
		curd.putSqlBean(sql);
		
		try {
			return curd.queryForJSONObject();
		} catch (CRUDException e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		}
		return null;
	}

	private static void extendSQLForServerList(Map<String, String> params, StringBuilder sb) {
		if (params!=null) {
			String serverIP = params.get("SERVER_IP");
			String serverName = params.get("SERVER_NAME");
			if (HttpUtils.isNotNull(serverIP)) {
				sb.append(" and SERVER_IP like '%"+serverIP+"%' ");
			}
			if (HttpUtils.isNotNull(serverName)) {
				sb.append(" and SERVER_NAME like '%"+serverName+"%' ");
			}
		}
	}
	
	public static boolean addServer(Map<String, String> params, ResultBean result) {
		
		if (params != null) {
			String IP = params.get("SERVER_IP");

			if (HttpUtils.isNull(IP)) {
				result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return false;
			}
			
			CRUD curd = new CRUD();
			Object[] sqlParams = new Object[] {IP};
			SqlBean sqlServBean = new SqlBean(ADD_SERVER);
			sqlServBean.addParams(sqlParams);
			curd.putSqlBean(sqlServBean);

			try {
				boolean res = curd.executeUpdate(true, result);
				return res;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.setRetInfo(e.getMessage());
				return false;
			}
		} else {
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return false;
		}
	}
	
	public static boolean deleteServer(Map<String, String> params, ResultBean result) {
		
		if (params != null) {
			String IP = params.get("SERVER_IP");
			IP = IP.substring(1, IP.length()-1);
			IP = IP.replaceAll("\"", "'");
			
			CRUD curd = new CRUD();
			//Delete SSH info on this server first
			SqlBean SSHDeleteBean = new SqlBean(DELETE_SSH_BY_IP+"("+IP+")");
			curd.putSqlBean(SSHDeleteBean);
			SqlBean ServerDeleteBean = new SqlBean(DELETE_SERVER+"("+IP+")");
			curd.putSqlBean(ServerDeleteBean);
			
			try {
				boolean res = curd.executeUpdate(true, result);
				return res;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.setRetInfo(e.getMessage());
				return false;
			}
		} else {
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return false;
		}
	}
	
	
	public static JsonArray getSSHListByIP(Map<String, String> params, ResultBean result) {
		
		StringBuilder sb = new StringBuilder(SEL_SSH_LIST);
		if (params!=null) {
			String IP = params.get("SERVER_IP");
			if (HttpUtils.isNull(IP)) {
				result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
			}
			int pageSize = Integer.parseInt(params.get(FixHeader.HEADER_PAGE_SIZE));
			int pageNumber = Integer.parseInt(params.get(FixHeader.HEADER_PAGE_NUMBER));
			int start = pageSize*(pageNumber-1);
			if(HttpUtils.isNotNull(pageSize)&&HttpUtils.isNotNull(pageNumber)){
				sb.append(" limit "+start+","+pageSize+" ");
			}
			
			SqlBean sql = new SqlBean(sb.toString());
			Object[] sqlParams = new Object[] {IP};
			sql.addParams(sqlParams);
			CRUD curd = new CRUD();
			curd.putSqlBean(sql);
			
			try {
				return curd.queryForJSONArray();
			} catch (CRUDException e) {
				logger.error(e.getMessage(), e);
				result.setRetInfo(e.getMessage());
			}
		} else {
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return null;
		}

		return null;
	}
	
	public static JsonObject getSSHCountByIP(Map<String, String> params, ResultBean result) {
		
		if (params!=null) {
			String IP = params.get("SERVER_IP");
			if (HttpUtils.isNull(IP)) {
				result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
			}
			
			SqlBean sql = new SqlBean(COUNT_SSH_LIST);
			CRUD curd = new CRUD();
			Object[] sqlParams = new Object[] {IP};
			sql.addParams(sqlParams);
			curd.putSqlBean(sql);
			
			try {
				return curd.queryForJSONObject();
			} catch (CRUDException e) {
				logger.error(e.getMessage(), e);
				result.setRetInfo(e.getMessage());
			}
		} else {
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return null;
		}
		return null;
	}
	
	public static boolean addOrModifySSH(Map<String, String> params, ResultBean result) {
		
		if (params != null) {
			boolean isAdd = params.get("TYPE").equals("add") ? true : false;
			String name = params.get("SSH_NAME");
			String password = params.get("SSH_PWD");
			String oldPwd = params.get("OLD_PWD");
			String type = params.get("SERV_TYPE");
			String IP = params.get("SERVER_IP");
			String sName = params.get("SERVER_NAME");
			
			if (HttpUtils.isNull(name) || HttpUtils.isNull(password) || 
					HttpUtils.isNull(type) || HttpUtils.isNull(IP)) {
				result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return false;
			}
			Object[] sqlParams = new Object[] {password, type, name, IP};
			CRUD curd = new CRUD();
			SqlBean sqlServBean = null;
			if (isAdd) {
				sqlServBean = new SqlBean(ADD_SSH);
			} else {
				sqlServBean = new SqlBean(UPDATE_SSH);
			}
			sqlServBean.addParams(sqlParams);
			curd.putSqlBean(sqlServBean);

			try {
				//if password is changed, should check user name and password first
				if (!oldPwd.equals(password)) {
					String hostname = checkSSHUser(IP, name, password);
					if (!hostname.equals(sName)) {
						SqlBean bean = new SqlBean(UPDATE_SERVER_NAME);
						Object[] sParams = new Object[] {hostname, IP};
						bean.addParams(sParams);
						curd.putSqlBean(bean);
					}
					//password of a existing user is changed, we should change the component info under this user too
					if (!isAdd) {
						SqlBean bean = new SqlBean(UPDATE_PWD_FOR_INSTANCE);
						Object[] sParams = new Object[] {password, IP, name};
						bean.addParams(sParams);
						curd.putSqlBean(bean);
					}
				}
				boolean res = curd.executeUpdate(true, result);
				return res;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.setRetInfo(e.getMessage());
				return false;
			}
		} else {
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return false;
		}
	}
	
	public static boolean deleteSSH(Map<String, String> params, ResultBean result) {
		
		if (params != null) {
			String IP = params.get("SERVER_IP");
			String users = params.get("SSH_NAME");
			if (HttpUtils.isNull(IP) || HttpUtils.isNull(users)) {
				result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return false;
			}
			users = users.substring(1, users.length()-1);
			users = users.replaceAll("\"", "'");
			
			CRUD curd = new CRUD();
			SqlBean sqlServBean = new SqlBean(DELETE_SSH+"("+users+")");
			Object[] sqlParams = new Object[] {IP};
			sqlServBean.addParams(sqlParams);
			curd.putSqlBean(sqlServBean);

			try {
				boolean res = curd.executeUpdate(true, result);
				return res;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.setRetInfo(e.getMessage());
				return false;
			}
		} else {
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return false;
		}
	}
	
	public static JsonArray getUserByServiceType(Map<String, String> params, ResultBean result) {
		if (params != null) {
			String type = params.get("SERVICE_TYPE");
			if (HttpUtils.isNull(type)) {
				result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
				return null;
			}
			
			CRUD curd = new CRUD();
			SqlBean sqlServBean = new SqlBean(GET_BY_SERVICE_TYPE);
			Object[] sqlParams = new Object[] {"%"+type+"%"};
			sqlServBean.addParams(sqlParams);
			curd.putSqlBean(sqlServBean);
			
			JsonArray res = new JsonArray();
			JsonArray sshs = new JsonArray();
			String lastIP = "";
			JsonObject user = null;
			
			try {
				JsonArray array = curd.queryForJSONArray();
				for (Object o:array) {
					JsonObject object = (JsonObject) o;
					if (!object.getString("SERVER_IP").equals(lastIP)) {
						if (user!=null) {
							user.put("SSH_LIST", sshs);
							res.add(user);
						}
						user = new JsonObject();
						user.put("SERVER_IP", object.getString("SERVER_IP"));
						sshs = new JsonArray();
					}
					JsonObject ssh = new JsonObject();
					ssh.put("SSH_NAME", object.getString("SSH_NAME"));
					ssh.put("SSH_PWD", object.getString("SSH_PWD"));
					sshs.add(ssh);
				}
				
				user.put("SSH_LIST", sshs);
				res.add(user);
				return res;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.setRetInfo(e.getMessage());
				return null;
			}
		} else {
			result.setRetInfo(CONSTS.ERR_PARAM_INCOMPLETE);
			return null;
		}
	}
	
	//private methods
	private static String checkSSHUser(String IP, String name, String password) throws Exception {
		JschUserInfo ui = new JschUserInfo(name, password, IP, CONSTS.SSH_PORT_DEFAULT);
		SSHExecutor executor = new SSHExecutor(ui);
		executor.connect();
		return executor.getHostname();
	}
}
