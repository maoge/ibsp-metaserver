package ibsp.metaserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class RedisReplicationChecker {
	private static final Logger log = LoggerFactory.getLogger(RedisReplicationChecker.class);

	private static RedisReplicationChecker instance = new RedisReplicationChecker();
	private Map<String, Integer> replicationCountMap = new ConcurrentHashMap<>(); //check for replication status
	private Checker checker = null;
	
	public static RedisReplicationChecker get() {
		return instance;
	}
	
	public synchronized void startChecker() {
		if (checker == null) {
			checker = new Checker();
			Thread checkerThread = new Thread(checker);
			checkerThread.setName("redis-replication-check");
			checkerThread.start();
		}
	}
	
	public synchronized void stopChecker() {
		checker = null;
	}
	
	public void addReplicationCheckSlave(String address) {
        replicationCountMap.put(address, 0);
    }
	
	private class Checker implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (replicationCountMap.size() == 0 ) {
					stopChecker();
					return;
				}
				
				Set<String> removeAddress = new HashSet<String>();
				//check replication status for new slaves
				for (String address : replicationCountMap.keySet()) {

					try {
						String[] arr = address.split(":");
						String ip = arr[0];
						int port = Integer.valueOf(arr[1]).intValue();
						
						Map<String, String> replicationInfo = RedisUtils.getInstanceInfo(ip, port, "replication");
				
						String status = replicationInfo.get("master_link_status");
						//this address is a master, no need to check replication
						if (status == null) {
							removeAddress.add(address);
							continue;
						}
						
						if (status.equals("up")) {
							int count = replicationCountMap.get(address)+1;
							if (count>=3) {
								removeAddress.add(address);
								String masterIp = replicationInfo.get("master_host");
								int masterPort = Integer.valueOf(replicationInfo.get("master_port")).intValue();
								RedisUtils.recoverConfigForReplication(masterIp, masterPort);
							} else {
								replicationCountMap.put(address, count);
							}
						} else {
							replicationCountMap.put(address, 0);
						}
					} catch (Exception e) {
						log.error("Failed to check redis replication "+address, e);
					}
				}
				
				for (String address : removeAddress) {
					log.info("Check redis replication "+address + " success!");
					replicationCountMap.remove(address);
				}
				
				try {
					Thread.sleep(SysConfig.get().getActiveCollectInterval());
				} catch (InterruptedException e) {
				}
    		}
		}
	}
}
