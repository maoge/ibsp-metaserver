package ibsp.metaserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.dbpool.ConnectionPool;
import ibsp.metaserver.dbpool.DbSource;
import ibsp.metaserver.exception.CRUDException;
import ibsp.metaserver.exception.CRUDException.CRUDERRINFO;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CRUD {

	private static final Logger logger = LoggerFactory.getLogger(CRUD.class.getName());

	private Connection conn = null;
	private ConcurrentLinkedQueue<SqlBean> queue;
	private ConnectionPool pool = null;

	public CRUD(ConcurrentLinkedQueue<SqlBean> queue) throws CRUDException {
		this.queue = queue;
	}

	public CRUD() {
		
	}
	
	public void close(){
		if (null != conn)
			pool.recycle(conn);
	}

	public void putSqlBean(SqlBean bean) {
		if (queue == null)
			queue = new ConcurrentLinkedQueue<SqlBean>();

		queue.add(bean);
	}
	
	@SuppressWarnings("unchecked")
	public void putSql(String sql, Object params) {
		if (params == null) {
			SqlBean bean = new SqlBean(sql);
			putSqlBean(bean);
		} else {
			if (params instanceof List) {
				SqlBean bean = new SqlBean(sql, (List<Object>) params);
				putSqlBean(bean);
			} else if (params instanceof Object[]) {
				SqlBean bean = new SqlBean(sql, Arrays.asList((Object[])params));
				putSqlBean(bean);
			}
		}
	}

	private void getConn() throws CRUDException {
		if (pool == null)
			pool = DbSource.getPool();

		if (pool == null) {
			logger.error("连接池异常", new CRUDException("连接池异常", new Throwable(),
					CRUDERRINFO.e1));
			throw new CRUDException("连接池异常", new Throwable(), CRUDERRINFO.e1);
		}
		conn = pool.getConnection();
		if (conn == null) {
			logger.error("获取连接异常", new CRUDException("获取连接异常", new Throwable(), CRUDERRINFO.e1));
			DbSource.removeBrokenPool(pool.getName());
			throw new CRUDException("获取连接异常", new Throwable(), CRUDERRINFO.e2);
		}
	}
	
	public boolean commit(boolean recycle){
		boolean res = false;
		if(conn!=null){
			try {
				conn.commit();
				res = true;
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			} finally {
				if(recycle){
					if (null != conn)
						pool.recycle(conn);
				}
			}
		}
		return res;
	}
	
	public boolean executeUpdate() {
		return executeUpdate(true);
	}
	
	public boolean executeUpdate(boolean recycle) {
		if (conn == null) {
			try {
				getConn();
			} catch (CRUDException e) {
				logger.error(e.getMessage(), e);
				return false;
			}
		}

		boolean res = true;
		try {
			conn.setAutoCommit(false);
			while (true) {
				SqlBean sb = queue.poll();
				if (sb == null)
					break;
				String sql = sb.getSql();
				
				List<Object> objs = sb.getParams();
				UPDATE(conn, sql, objs != null ? objs.toArray() : null);
			}
			conn.commit();
		} catch (Exception e) {
			res = false;
			try {
				conn.rollback();
			} catch (SQLException e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
		} finally {
			if(recycle){
				if (null != conn) {
					pool.recycle(conn);
					conn = null;
				}
			}
		}
		return res;
	}
	
	public boolean executeUpdate(boolean recycle, ResultBean result) {
		if (conn == null) {
			try {
				getConn();
			} catch (CRUDException e) {
				logger.error(e.getMessage(), e);
				return false;
			}
		}

		boolean res = true;
		try {
			conn.setAutoCommit(false);
			while (true) {
				SqlBean sb = queue.poll();
				if (sb == null)
					break;
				String sql = sb.getSql();
				
				List<Object> objs = sb.getParams();
				UPDATE(conn, sql, objs != null ? objs.toArray() : null);
			}
			conn.commit();
		} catch (Exception e) {
			res = false;
			try {
				conn.rollback();
			} catch (SQLException e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
			
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
		} finally {
			if(recycle){
				if (null != conn) {
					pool.recycle(conn);
					conn = null;
				}
			}
		}
		return res;
	}

	public List<HashMap<String, Object>> queryForList() throws CRUDException {
		if (conn == null) {
			getConn();
		}

		List<HashMap<String, Object>> res = new LinkedList<HashMap<String, Object>>();
		SqlBean sb = null;
		try {
			sb = queue.poll();
			if (sb != null) {
				String sql = sb.getSql();
				
				List<Object> objs = sb.getParams();
				res = queryForList(conn, sql, objs != null ? objs.toArray() : null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new CRUDException(e.getMessage(), e, CRUDERRINFO.e3);
		} finally {
			if (null != conn)
				pool.recycle(conn);
		}
		return res;
	}
	
	public int queryForCount() throws CRUDException {
		if (conn == null) {
			getConn();
		}

		int res = 0;
		SqlBean sb = null;
		try {
			sb = queue.poll();
			if (sb != null) {
				String sql = sb.getSql();
				
				List<Object> objs = sb.getParams();
				res = queryForCount(conn, sql, objs != null ? objs.toArray() : null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new CRUDException(e.getMessage(), e, CRUDERRINFO.e3);
		} finally {
			if (null != conn)
				pool.recycle(conn);
		}
		return res;
	}
	
	/**
	 * 
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 * @throws CRUDException
	 */
	public List<HashMap<String, Object>> queryForList(Connection conn,
			String sql, Object[] params) throws CRUDException {
		List<HashMap<String, Object>> resultList = new LinkedList<HashMap<String, Object>>();
		if (conn != null) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				if (logger.isDebugEnabled())
					logger.debug("executeSql:[" + sql + "]");

				ps = conn.prepareStatement(sql);
				if (params != null && params.length > 0) {
					for (int i = 0; i < params.length; i++) {
						ps.setObject(i + 1, params[i]);

						if (logger.isDebugEnabled())
							logger.debug("executeSql-params:" + params[i]);
					}
				}
				rs = ps.executeQuery();
				ResultSetMetaData metaData = rs.getMetaData();
				int colnum = metaData.getColumnCount();
				while (rs.next()) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					for (int i = 1; i <= colnum; i++) {
						String columnName = metaData.getColumnLabel(i).toUpperCase();
						map.put(columnName, rs.getObject(i));
					}
					resultList.add(map);
				}
				
				if (rs != null)
					rs.close();
				
				if (ps != null)
					ps.close();
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new CRUDException(e.getMessage(), e, CRUDERRINFO.e3);
			} 
		}
		return resultList;
	}
	
	public int queryForCount(Connection conn, String sql, Object[] params)
			throws CRUDException {
		int count = 0;
		if (conn != null) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				if (logger.isDebugEnabled())
					logger.debug("executeSql:[" + sql + "]");

				ps = conn.prepareStatement(sql);
				if (params != null && params.length > 0) {
					for (int i = 0; i < params.length; i++) {
						ps.setObject(i + 1, params[i]);

						if (logger.isDebugEnabled())
							logger.debug("executeSql-params:" + params[i]);
					}
				}
				rs = ps.executeQuery();
				while (rs.next()) {
					count = rs.getInt(1);
				}
				
				if (rs != null)
					rs.close();
				
				if (ps != null)
					ps.close();
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new CRUDException(e.getMessage(), e, CRUDERRINFO.e3);
			} 
		}
		return count;
	}
	
	@SuppressWarnings("unused")
	private void mapping(HashMap<String, Object> resultMap, int column, ResultSet rs, ResultSetMetaData metaData) throws SQLException {
		String columnName = metaData.getColumnLabel(column).toUpperCase();
		int columnType = metaData.getColumnType(column);
		
		switch(columnType) {
		case Types.BIT:
			resultMap.put(columnName, rs.getByte(column));
			break;
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
			resultMap.put(columnName, rs.getInt(column));
			break;
		case Types.DATE:
			resultMap.put(columnName, rs.getDate(column));
			break;
		case Types.TIME:
			resultMap.put(columnName, rs.getTime(column));
			break;
		case Types.DECIMAL:
		case Types.BIGINT:
		case Types.TIMESTAMP:
			resultMap.put(columnName, rs.getLong(column));
			break;
		case Types.FLOAT:
			resultMap.put(columnName, rs.getFloat(column));
			break;
		case Types.REAL:
		case Types.DOUBLE:
		case Types.NUMERIC:
			resultMap.put(columnName, rs.getDouble(column));
			break;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			resultMap.put(columnName, rs.getString(column));
			break;
		case Types.BOOLEAN:
			resultMap.put(columnName, rs.getBoolean(column));
			break;
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
			resultMap.put(columnName, rs.getNString(column));
			break;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			resultMap.put(columnName, rs.getBytes(column));
			break;
		case Types.BLOB:
		case Types.CLOB:
		case Types.NCLOB:
		case Types.NULL:
		case Types.OTHER:
		case Types.JAVA_OBJECT:
		case Types.DISTINCT:
		case Types.STRUCT:
		case Types.ARRAY:
		case Types.REF:
		case Types.DATALINK:
		case Types.SQLXML:
		case Types.REF_CURSOR:
		case Types.TIME_WITH_TIMEZONE:
		case Types.TIMESTAMP_WITH_TIMEZONE:
			logger.error("not surpported type ......");
			break;
		default:
			logger.error("data type undefined ......");
			break;
		}
	}

	public JsonArray queryForJSONArray() throws CRUDException {
		if (conn == null) {
			try {
				getConn();
			} catch (CRUDException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}

		JsonArray res = null;
		SqlBean sb = null;
		try {
			sb = queue.poll();
			if (sb != null) {
				String sql = sb.getSql();
				
				List<Object> objs = sb.getParams();
				res = queryForJSONArray(conn, sql, objs != null ? objs.toArray() : null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new CRUDException("sql:" + sb.getSql(), e, CRUDERRINFO.e3);
		} finally {
			if (null != conn)
				pool.recycle(conn);
		}
		return res;
	}

	public JsonObject queryForJSONObject() throws CRUDException {
		if (conn == null) {
			try {
				getConn();
			} catch (CRUDException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}

		JsonObject res = null;
		SqlBean sb = null;
		try {
			sb = queue.poll();
			if (sb != null) {
				String sql = sb.getSql();
				
				List<Object> objs = sb.getParams();
				res = queryForJSONObject(conn, sql, objs != null ? objs.toArray() : null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new CRUDException("sql:" + sb.getSql(), e, CRUDERRINFO.e3);
		} finally {
			if (null != conn)
				pool.recycle(conn);
		}
		
		return res;
	}

	public Map<String, Object> queryForMap() throws CRUDException {
		if (conn == null) {
			try {
				getConn();
			} catch (CRUDException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}
		Map<String, Object> res = null;
		SqlBean sb = null;
		try {
			sb = queue.poll();
			if (sb != null) {
				String sql = sb.getSql();
				
				List<Object> objs = sb.getParams();
				res = queryForMap(conn, sql, objs != null ? objs.toArray() : null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new CRUDException("sql:" + sb.getSql(), e, CRUDERRINFO.e3);
		} finally {
			if (null != conn)
				pool.recycle(conn);
		}
		return res;
	}

	public JsonArray queryForJSONArray(Connection conn, String sql,
			Object[] params) throws CRUDException {
		JsonArray jsonarray = new JsonArray();
		if (conn != null) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				if (logger.isDebugEnabled())
					logger.debug("executeSql:[" + sql + "]");

				ps = conn.prepareStatement(sql);
				if (params != null && params.length > 0) {
					for (int i = 0; i < params.length; i++) {
						ps.setObject(i + 1, params[i]);

						if (logger.isDebugEnabled())
							logger.debug("executeSql-params:" + params[i]);
					}
				}
				rs = ps.executeQuery();
				ResultSetMetaData metaData = rs.getMetaData();
				int colnum = metaData.getColumnCount();
				while (rs.next()) {
					JsonObject jsonObj = new JsonObject();
					for (int i = 1; i <= colnum; i++) {
						String listName = metaData.getColumnLabel(i).toUpperCase();
						jsonObj.put(listName, rs.getObject(i));
					}
					jsonarray.add(jsonObj);
				}
				
				if (rs != null)
					rs.close();
				
				if (ps != null)
					ps.close();
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new CRUDException("sql:" + sql, e, CRUDERRINFO.e3);
			}
		}
		
		return jsonarray;
	}

	public JsonObject queryForJSONObject(Connection conn, String sql,
			Object[] params) throws CRUDException {
		Map<String, Object> queryResult = queryForMap(conn, sql, params);
		JsonObject obj = new JsonObject();
		Set<Entry<String, Object>> entrySet = queryResult.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			obj.put(entry.getKey(), entry.getValue());
		}

		return obj;
	}

	public Map<String, Object> queryForMap(Connection conn, String sql,
			Object[] params) throws CRUDException {
		Map<String, Object> map = null;
		if (conn != null) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				if (logger.isDebugEnabled())
					logger.debug("executeSql:[" + sql + "]");

				ps = conn.prepareStatement(sql);
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						ps.setObject(i + 1, params[i]);

						if (logger.isDebugEnabled())
							logger.debug("executeSql-params:" + params[i]);
					}
				}
				rs = ps.executeQuery();
				ResultSetMetaData metaData = rs.getMetaData();
				int colnum = metaData.getColumnCount();
				map = new HashMap<String, Object>();
				while (rs.next()) {
					for (int i = 1; i <= colnum; i++) {
						String listName = metaData.getColumnLabel(i)
								.toUpperCase();
						String listValue = rs.getString(i);
						map.put(listName, listValue);
					}
					break;
				}
				
				if (rs != null)
					rs.close();
				
				if (ps != null)
					ps.close();
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new CRUDException("sql:" + sql, e, CRUDERRINFO.DEFAULT);
			} 
		}
		return map;
	}

	/**
	 * 数据库的更新操作
	 * 
	 * @param sql更新语句参数使用"?",类型必须和数据库字段类型匹配           
	 * @param params
	 * @return 出现异常时返回-1
	 * @throws CRUDException
	 */
	public int UPDATE(Connection conn, String sql, Object[] params) throws CRUDException {
		int res = -1;
		
		if (conn != null) {
			PreparedStatement ps = null;
			try {
				if (logger.isDebugEnabled())
					logger.debug("executeSql:[" + sql + "]");

				ps = conn.prepareStatement(sql);
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						ps.setObject(i + 1, params[i]);

						if (logger.isDebugEnabled())
							logger.debug("executeSql-params:" + params[i]);
					}
				}
				
				res = ps.executeUpdate();
				
				if (ps != null)
					ps.close();
				
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				throw new CRUDException("sql:" + sql, e, CRUDERRINFO.e3);
			} 
		}
		
		return res;
	}

}
