package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.QuotaMeanBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CRUD;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class QuotaDataService {
	
	private static Logger logger = LoggerFactory.getLogger(QuotaDataService.class);
	
	private static final String INS_MONITOR_COLLECT = "insert into t_monitor_collect(INST_ID,TS,QUOTA_CODE,QUOTA_MEAN) values(?,?,?,?)";
	
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
		
		jedis.hmset(id, quotas);
	}

}
