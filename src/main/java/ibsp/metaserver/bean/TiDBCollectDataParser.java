package ibsp.metaserver.bean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ibsp.metaserver.dbservice.QuotaDataService;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class TiDBCollectDataParser extends CollectDataParser {
	
	private static final String CPU_USED       = "CPU.Used";
	private static final String MEM_USED       = "MEM.Used";
	private static final String DISK_TOTAL     = "DISK.Total";
	private static final String DISK_USED      = "DISK.Used";
	private static final String DISK_AVAILABLE = "DISK.Available";
	
	public TiDBCollectDataParser(JsonObject jsonObj) {
		super(jsonObj);
	}

	@Override
	public void parseAndSave() {
		if (jsonObj == null)
			return;
		
		List<QuotaMeanBean> quotas = new LinkedList<QuotaMeanBean>();
		long ts = System.currentTimeMillis();
		
		JsonArray tidbArr = jsonObj.getJsonArray(FixHeader.HEADER_DB_TIDB);
		if (tidbArr != null) {
			parse(tidbArr, ts, quotas);
		}
		
		JsonArray pdArr = jsonObj.getJsonArray(FixHeader.HEADER_DB_PD);
		if (pdArr != null) {
			parse(pdArr, ts, quotas);
		}
		
		JsonArray tikvArr = jsonObj.getJsonArray(FixHeader.HEADER_DB_TIKV);
		if (tikvArr != null) {
			parse(tikvArr, ts, quotas);
		}
		
		save(quotas);
		quotas.clear();
	}
	
	private void parse(JsonArray jsonArr, long ts, List<QuotaMeanBean> quotas) {
		for (int i = 0; i < jsonArr.size(); i++) {
			JsonObject json = jsonArr.getJsonObject(i);
			if (json == null)
				continue;
			
			String id = json.getString(FixHeader.HEADER_ID);
			JsonObject jsonCPU = json.getJsonObject(FixHeader.HEADER_CPU);
			JsonObject jsonMEM = json.getJsonObject(FixHeader.HEADER_MEM);
			JsonObject jsonDisk = json.getJsonObject(FixHeader.HEADER_DISK);
			
			String cpuUsed   = jsonCPU.getString(FixHeader.HEADER_USED);
			String memUesd   = jsonMEM.getString(FixHeader.HEADER_USED);
			String diskTotal = jsonDisk.getString(FixHeader.HEADER_TOTAL);
			String diskUsed  = jsonDisk.getString(FixHeader.HEADER_USED);
			String diskAvailable = jsonDisk.getString(FixHeader.HEADER_AVAILABLE);
			
			quotas.add(new QuotaMeanBean(id, ts, MetaData.get().getQuotaCode(CPU_USED), cpuUsed));
			quotas.add(new QuotaMeanBean(id, ts, MetaData.get().getQuotaCode(MEM_USED), memUesd));
			quotas.add(new QuotaMeanBean(id, ts, MetaData.get().getQuotaCode(DISK_TOTAL), diskTotal));
			quotas.add(new QuotaMeanBean(id, ts, MetaData.get().getQuotaCode(DISK_USED), diskUsed));
			quotas.add(new QuotaMeanBean(id, ts, MetaData.get().getQuotaCode(DISK_AVAILABLE), diskAvailable));
			
			Map<String, String> hash = new HashMap<String, String>();
			hash.put(FixHeader.HEADER_TS, String.valueOf(ts));
			hash.put(CPU_USED,       cpuUsed);
			hash.put(MEM_USED,       memUesd);
			hash.put(DISK_TOTAL,     diskTotal);
			hash.put(DISK_USED,      diskUsed);
			hash.put(DISK_AVAILABLE, diskAvailable);
			
			QuotaDataService.saveQuotaToRedis(id, hash);
			
			hash.clear();
		}
	}
	
	private void save(List<QuotaMeanBean> quotas) {
		QuotaDataService.saveQuotaDataToDB(quotas);
	}

}
