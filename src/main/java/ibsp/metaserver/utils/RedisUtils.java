package ibsp.metaserver.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
import redis.clients.jedis.Jedis;


public class RedisUtils {
	
	private static Logger logger = LoggerFactory.getLogger(RedisUtils.class);
	
	public static boolean updateConfForRemoveSlave(String ip, int port, String user, String pwd) {
		String deployRootPath = String.format("cache_node_deploy/%d", port);
		
		JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			String confPath = "%HOME" + String.format("/%s/%s", deployRootPath, "conf");
			executor.cd(confPath, null);
			executor.rmLine(CONSTS.REDIS_SLAVEOF, CONSTS.REDIS_PROPERTIES, null);
		} catch (Exception e) {
			logger.error("修改redis配置文件失败！", e);
			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
		
		return true;
    }

    public static boolean updateConfForAddSlave(String ip, int port, String user, String pwd,
    		String targetIp, int targetPort) {
    	String deployRootPath = String.format("cache_node_deploy/%d", port);
    	
    	JschUserInfo ui = null;
		SSHExecutor executor = null;
		boolean connected = false;
		
		try {
			ui = new JschUserInfo(user, pwd, ip, CONSTS.SSH_PORT_DEFAULT);
			executor = new SSHExecutor(ui);
			executor.connect();
			connected = true;
			
			String confPath = "%HOME" + String.format("/%s/%s", deployRootPath, "conf");
			executor.cd(confPath, null);
			executor.rmLine(CONSTS.REDIS_SLAVEOF, CONSTS.REDIS_PROPERTIES, null);
			
			String slaveof = String.format("%s %s %d", CONSTS.REDIS_SLAVEOF, targetIp, targetPort);
			executor.addLine(slaveof, CONSTS.REDIS_PROPERTIES, null);
			
		} catch (Exception e) {
			logger.error("修改redis配置文件失败！", e);
			return false;
		} finally {
			if (connected) {
				executor.close();
			}
		}
    	
    	return true;
    }
	
	public static boolean removeSlave(String ip, int port) {
    	Jedis jedis = null;
		try {
			jedis = new Jedis(ip, port);
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
    
	public static boolean addSlave(String ip, int port, String targetIp, int targetPort) {
    	Jedis jedis = null;
		try {
			jedis = new Jedis(ip, port);
			jedis.slaveof(targetIp, targetPort);
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
    public static boolean setConfigForReplication(String ip, int port) {
    	Jedis jedis = null;
		try {
			jedis = new Jedis(ip, port);
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
    
	public static boolean recoverConfigForReplication(String ip, int port) {
		Jedis jedis = null;
		try {
			jedis = new Jedis(ip, port);
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
	
	public static Map<String, String> getReplicationInfo(String ip, int port) {
		
		Jedis jedis = null;
		try {
			Map<String, String> result = new HashMap<String, String>();
			jedis = new Jedis(ip, port);
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

    public static Map<String, String> getInstanceInfo(String ip, int port, String type) {

        Jedis jedis = null;
        try {
            Map<String, String> result = new HashMap<String, String>();
            jedis = new Jedis(ip, port);

            String[] infoArray = null;
            if (HttpUtils.isNull(type)) {
                infoArray = jedis.info().split("\r\n");
            } else {
                infoArray = jedis.info(type).split("\r\n");
            }
            for (String info : infoArray) {
                if (info.indexOf(":") == -1)
                    continue;
                result.put(info.split(":")[0], info.split(":")[1]);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error getting redis instance info...", e);
            return null;
        } finally {
            if (jedis!=null) {
                jedis.close();
            }
        }
    }

    public static Map<String, String> getInstanceConfig(String ip, int port, String pattern) {

        Jedis jedis = null;
        try {
            Map<String, String> result = new HashMap<String, String>();
            jedis = new Jedis(ip, port);
            List<String> configList = null;
            if (HttpUtils.isNull(pattern)) {
                configList = jedis.configGet("*");
            } else {
                configList = jedis.configGet(pattern);
            }

            String name = "", value = "";
            for (String config : configList) {
                if (name.equals("")) {
                    name = config;
                } else {
                    value = config;
                    result.put(name, value);
                    name = "";
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Error getting redis instance config...", e);
            return null;
        } finally {
            if (jedis!=null) {
                jedis.close();
            }
        }
    }

    public static String getHumanReadable(long number) {
        String result = "";
        long[] list = {1024l*1024l*1024l, 1024l*1024l, 1024l};

        if (number>list[0]) {
            double temp = (double)number/(double)list[0];
            result = String.format("%.2f", temp)+"GB";
        } else if (number>list[1]) {
            double temp = (double)number/(double)list[1];
            result = String.format("%.2f", temp)+"MB";
        } else if (number>list[2]) {
            double temp = (double)number/(double)list[2];
            result = String.format("%.2f", temp)+"KB";
        } else {
            result = number+"B";
        }

        return result;
    }

    /*public static void updateAofPolicy(String ip, String port, String user, String pwd,
                                       String appendonly, String appendfsync) throws Exception {
        Jedis jedis = null;
        SCPFileUtils scp = null;

        try {
            jedis = new Jedis(ip, new Integer(port));
            if (appendonly.equals("no")) {
                jedis.configSet("appendonly", "no");
            } else {
                jedis.configSet("appendonly", "yes");
                jedis.configSet("appendfsync", appendfsync);
            }

            scp = new SCPFileUtils(ip, user, pwd, CONSTS.SSH_PORT_DEFAULT);
            String deployRootPath = String.format("cache_node_deploy/%s", port);
            scp.getFile(deployRootPath + "/conf/" + CONSTS.REDIS_PROPERTIES);
            BufferedReader reader = new BufferedReader(new FileReader("./"+CONSTS.REDIS_PROPERTIES));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.indexOf("appendonly ")!=-1 || line.indexOf("appendfsync ")!=-1)
                    continue;
                sb.append(line).append("\n");
            }
            if (appendonly.equals("no")) {
                sb.append("appendonly ").append("no").append("\n");
            } else {
                sb.append("appendonly ").append("yes").append("\n");
                sb.append("appendfsync ").append(appendfsync).append("\n");
            }
            scp.putFile(sb.toString(), CONSTS.REDIS_PROPERTIES, deployRootPath + "/conf");
            reader.close();
            scp.deleteLocalFile(CONSTS.REDIS_PROPERTIES);

        } finally {
            if (jedis!=null) {
                jedis.close();
            }
            if (scp != null) {
                scp.close();
            }
        }
    }*/

    /*public static void updateRdbPolicy(String ip, String port, String user, String pwd, String save) throws Exception {
        Jedis jedis = null;
        SCPFileUtils scp = null;

        try {
            jedis = new Jedis(ip, new Integer(port));
            if (HttpUtils.isNull(save)) {
                jedis.configSet("save", "");
            } else {
                jedis.configSet("save", save);
            }

            scp = new SCPFileUtils(ip, user, pwd, CONSTS.SSH_PORT_DEFAULT);
            String deployRootPath = String.format("cache_node_deploy/%s", port);
            scp.getFile(deployRootPath + "/conf/" + CONSTS.REDIS_PROPERTIES);
            BufferedReader reader = new BufferedReader(new FileReader("./"+CONSTS.REDIS_PROPERTIES));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.indexOf("save ")!=-1)
                    continue;
                sb.append(line).append("\n");
            }

            if (HttpUtils.isNotNull(save)) {
                String[] rdbPolicies = save.split(" ");
                for (int i=0; i<rdbPolicies.length; i+=2)
                    sb.append("save ").append(rdbPolicies[i]).append(" ").append(rdbPolicies[i+1]).append("\n");
            }
            scp.putFile(sb.toString(), CONSTS.REDIS_PROPERTIES, deployRootPath + "/conf");
            reader.close();
            scp.deleteLocalFile(CONSTS.REDIS_PROPERTIES);

        } finally {
            if (jedis!=null) {
                jedis.close();
            }
            if (scp != null) {
                scp.close();
            }
        }
    }*/

    /*public static void bgRewriteAof(String ip, String port) {
        Jedis jedis = null;

        try {
            jedis = new Jedis(ip, new Integer(port));
            jedis.bgrewriteaof();
        } finally {
            if (jedis!=null) {
                jedis.close();
            }
        }
    }*/

    /*public static void setMaxMemory(String ip, String port, String user, String pwd, long maxMemory) throws Exception {
        long memoryInByte = maxMemory*1024*1024*1024;
        Jedis jedis = null;
        SCPFileUtils scp = null;

        try {
            jedis = new Jedis(ip, new Integer(port));
            jedis.configSet("maxmemory", String.valueOf(memoryInByte));

            scp = new SCPFileUtils(ip, user, pwd, CONSTS.SSH_PORT_DEFAULT);
            String deployRootPath = String.format("cache_node_deploy/%s", port);
            scp.getFile(deployRootPath + "/conf/" + CONSTS.REDIS_PROPERTIES);
            BufferedReader reader = new BufferedReader(new FileReader("./"+CONSTS.REDIS_PROPERTIES));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.indexOf("maxmemory ")!=-1)
                    continue;
                sb.append(line).append("\n");
            }
            sb.append("maxmemory ").append(maxMemory).append("gb").append("\n");

            scp.putFile(sb.toString(), CONSTS.REDIS_PROPERTIES, deployRootPath + "/conf");
            reader.close();
            scp.deleteLocalFile(CONSTS.REDIS_PROPERTIES);
        } finally {
            if (jedis!=null) {
                jedis.close();
            }
            if (scp != null) {
                scp.close();
            }
        }
    }*/

    public static boolean setDataForReplicationCheck(String masterIP, String masterPort,
                                                     String slaveIP, String slavePort) throws Exception {

        final long MAX_TIME_FOR_SYNC = 15000;
        final byte[] key = {0x3, 0x4, 0x5, 0x6}, value = {0x3};
        Jedis masterJedis = null, slaveJedis = null;

        try {
            masterJedis = new Jedis(masterIP, Integer.parseInt(masterPort));
            slaveJedis = new Jedis(slaveIP, Integer.parseInt(slavePort));
            masterJedis.set(key, value);
            byte[] result = null;
            long startTime = System.currentTimeMillis();

            while (true) {
                result = slaveJedis.get(key);
                if (result != null && result[0] == value[0]) break;
                long interval = System.currentTimeMillis() - startTime;
                if (interval > MAX_TIME_FOR_SYNC) {
                    return false;
                }
                Thread.sleep(100);
            }
            return true;
        } finally {
            if (masterJedis!=null) {
                masterJedis.close();
            }
            if (slaveJedis!=null) {
                slaveJedis.close();
            }
        }
    }

}
