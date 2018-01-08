package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.QuotaMeanBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class QuotaDataService {
	
	private static Logger logger = LoggerFactory.getLogger(QuotaDataService.class);
	
	private static final String REDIS_OK = "OK";
	private static final String INS_MONITOR_COLLECT = "insert into t_monitor_collect(INST_ID,TS,QUOTA_CODE,QUOTA_MEAN) values(?,?,?,?)";
	private static final String SEL_MONITOR_COLLECT = "select TS,QUOTA_CODE,QUOTA_MEAN from t_monitor_collect where INST_ID=? and (TS>? and TS<=?)";
	
	
	public static void saveQuotaDataToDB(List<QuotaMeanBean> quotas) {
		if (quotas == null || quotas.size() == 0)
			return;
		
		CRUD c = new CRUD();
		for (QuotaMeanBean quota : quotas) {
			if (quota == null)
				continue;
			
			SqlBean sqlBean = new SqlBean(INS_MONITOR_COLLECT);
			sqlBean.addParams(new Object[] { quota.getInstID(), quota.getTs(), quota.getQuotaCode(), quota.getQuotaMean() });
			
			c.putSqlBean(sqlBean);
		}
		
		c.executeUpdate();
	}
	
	public static void saveQuotaToRedis(String id, Map<String, String> quotas) {
		if (quotas == null || quotas.size() == 0)
			return;
		
		Jedis jedis = MetaData.get().getJedis();
		if (jedis == null) {
			logger.error("jedis pool get resource fail!");
			return;
		}
		
		try {
			if (!jedis.hmset(id, quotas).equals(REDIS_OK)) {
				logger.error("save collect data to redis fail!");
			}
		} finally {
			jedis.close();
		}
	}
	
	public static JsonObject getCurrCollectData(String instID) {
		if (HttpUtils.isNull(instID))
			return null;
		
		Jedis jedis = MetaData.get().getJedis();
		if (jedis == null) {
			logger.error("jedis pool get resource fail!");
			return null;
		}
		
		JsonObject json = null;
		try {
			Map<String, String> map = jedis.hgetAll(instID);
			json = new JsonObject();
			
			Set<Entry<String, String>> entrySet = map.entrySet();
			for (Entry<String, String> entry : entrySet) {
				String key = entry.getKey();
				String val = entry.getValue();
				
				json.put(key, val);
			}
		} finally {
			jedis.close();
		}
		
		return json;
	}
	
	public static JsonArray getHisCollectData(String instID, Long startTS, Long endTS) {
		if (HttpUtils.isNull(instID) || startTS == null || endTS == null)
			return null;
		
		JsonArray hisDataArr = null;
		
		SqlBean sqlBean = new SqlBean(SEL_MONITOR_COLLECT);
		sqlBean.addParams(new Object[] { instID, startTS, endTS });
		
		CRUD c = new CRUD();
		c.putSqlBean(sqlBean);
		
		try {
			long privTS = 0L;
			JsonObject json = null;
			
			List<HashMap<String, Object>> hisDataList = c.queryForList();
			Iterator<HashMap<String, Object>> it = hisDataList.iterator();
			
			hisDataArr = new JsonArray();
			while (it.hasNext()) {
				HashMap<String, Object> map = it.next();
				if (map == null)
					continue;
				
				Object objTS = map.get(FixHeader.HEADER_TS);
				Object objQuotaCode = map.get(FixHeader.HEADER_QUOTA_CODE);
				Object objQuotaMean = map.get(FixHeader.HEADER_QUOTA_MEAN);
				if (objTS == null || objQuotaCode == null || objQuotaMean == null)
					continue;
				
				long ts = objTS instanceof Long ? (Long) objTS : Long.valueOf(String.valueOf(objTS));
				Integer quotaCode = objQuotaCode instanceof Integer ? (Integer) objQuotaCode : Integer.valueOf(String.valueOf(objQuotaCode));
				String quotaMean = objQuotaMean instanceof String ? (String) objQuotaMean : String.valueOf(objQuotaMean);
				String quotaName = MetaData.get().getQuotaName(quotaCode);
				
				if (privTS != ts) {
					json = new JsonObject();
					privTS = ts;
					hisDataArr.add(json);
					json.put(FixHeader.HEADER_TS, ts);
				}
				
				json.put(quotaName, quotaMean);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return hisDataArr;
	}

}
