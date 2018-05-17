package ibsp.metaserver.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;


public class RedisUtils {
	
	private static Logger logger = LoggerFactory.getLogger(RedisUtils.class);
	
	public static boolean updateConfForRemoveSlave(String homeDir, String ip, String port, String user, String pwd) {
		String deployRootPath = String.format("cache_node_deploy/%s", port);
		boolean connected = false;
		SCPFileUtils scp = null;
    	
    	try {
			scp = new SCPFileUtils(ip, user, pwd, CONSTS.SSH_PORT_DEFAULT);
			connected = true;
			
			scp.getFile(homeDir + "/" + deployRootPath + "/conf/" + CONSTS.REDIS_PROPERTIES);
			BufferedReader reader = new BufferedReader(new FileReader("./"+CONSTS.REDIS_PROPERTIES));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("slaveof ")!=-1)
					continue;
				sb.append(line).append("\n");
			}
			scp.putFile(sb.toString(), CONSTS.REDIS_PROPERTIES, homeDir + "/" + deployRootPath + "/conf");
			reader.close();
			scp.deleteLocalFile(CONSTS.REDIS_PROPERTIES);
			
			return true;
    	} catch (Exception e) {
    		logger.error("修改redis配置文件失败！", e);
    		return false;
    	} finally {
			if (connected) {
				scp.close();
			}
    	}
    }
	
	public static boolean removeSlave(String ip, String port) {
    	Jedis jedis = null;
		try {
			jedis = new Jedis(ip, new Integer(port));
			jedis.slaveofNoOne();
			return true;
		} catch (Exception e) {
			logger.error("Remove slave for redis failed.", e);
			return false;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}
    
    public static boolean updateConfForAddSlave(String homeDir, String ip, String port, String user, String pwd, 
    		String targetIp, String targetPort) {
    	String deployRootPath = String.format("cache_node_deploy/%s", port);
		boolean connected = false;
		SCPFileUtils scp = null;
    	
    	try {
			scp = new SCPFileUtils(ip, user, pwd, CONSTS.SSH_PORT_DEFAULT);
			connected = true;
			
			scp.getFile(homeDir + "/" + deployRootPath + "/conf/" + CONSTS.REDIS_PROPERTIES);
			BufferedReader reader = new BufferedReader(new FileReader("./"+CONSTS.REDIS_PROPERTIES));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("slaveof ")!=-1)
					continue;
				sb.append(line).append("\n");
			}
			sb.append("slaveof ").append(targetIp).append(" ").append(targetPort).append("\n");
			scp.putFile(sb.toString(), CONSTS.REDIS_PROPERTIES, homeDir + "/" + deployRootPath + "/conf");
			reader.close();
			scp.deleteLocalFile(CONSTS.REDIS_PROPERTIES);
			scp.close();
			
			return true;
    	} catch (Exception e) {
    		logger.error("修改redis配置文件失败！", e);
    		return false;
    	} finally {
			if (connected) {
				scp.close();
			}
    	}
    }
    
	public static boolean addSlave(String ip, String port, String targetIp, String targetPort) {
    	Jedis jedis = null;
		try {
			jedis = new Jedis(ip, new Integer(port));
			jedis.slaveof(targetIp, new Integer(targetPort));
			return true;
		} catch (Exception e) {
			logger.error("Remove slave for redis failed.", e);
			return false;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}
    
    /**
     * 当有新的从节点挂上来时，需要修改主节点的参数，否则大数据量时主从同步会一直不成功，并导致主节点一直写盘
     */
    public static boolean setConfigForReplication(String ip, String port) {
    	Jedis jedis = null;
		try {
			jedis = new Jedis(ip, new Integer(port));
			jedis.configSet("client-output-buffer-limit", "normal 0 0 0 slave 0 0 0 pubsub 33554432 8388608 60");
			jedis.configSet("repl-timeout", "6000");
			return true;
		} catch (Exception e) {
			logger.error("Set config for replication failed.", e);
			return false;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
    }
    
	public static boolean recoverConfigForReplication(String ip, String port) {
		Jedis jedis = null;
		try {
			jedis = new Jedis(ip, new Integer(port));
			jedis.configSet("client-output-buffer-limit", "normal 0 0 0 slave 268435456 67108864 60 pubsub 33554432 8388608 60");
			jedis.configSet("repl-timeout", "60");
			return true;
		} catch (Exception e) {
			logger.error("Recover config for replication failed.", e);
			return false;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}
	
	public static Map<String, String> getReplicationInfo(String ip, String port) {
		
		Jedis jedis = null;
		try {
			Map<String, String> result = new HashMap<String, String>();
			jedis = new Jedis(ip, new Integer(port));
//			String[] replicationInfo = jedis.info("replication").split(System.lineSeparator());
			String[] replicationInfo = jedis.info("replication").split("\r\n");
			for (String info : replicationInfo) {
				if (info.indexOf(":") == -1)
					continue;
				result.put(info.split(":")[0], info.split(":")[1]);
			}
			return result;
		} catch (Exception e) {
			logger.error("Error getting redis replication info...", e);
			return null;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}
	
}
