package ibsp.metaserver.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class Topology {
	
	private Map<String, Set<String>> mapContain;
	private Map<String, Set<String>> mapLink;
	
	private ReentrantLock lock = null;
	
	public Topology() {
		mapContain = new HashMap<String, Set<String>>();
		mapLink = new HashMap<String, Set<String>>();
		
		lock = new ReentrantLock();
	}
	
	public void put(String par, String sub, int topoType) {
		try {
			lock.lock();
			
			Map<String, Set<String>> map = topoType == CONSTS.TOPO_TYPE_CONTAIN ? mapContain : mapLink;
			Set<String> val = map.get(par);
			if (val == null) {
				val = new HashSet<String>();
				map.put(par, val);
			}
			
			val.add(sub);
		} finally {
			lock.unlock();
		}
	}
	
	public final Set<String> get(String par, int topoType) {
		Set<String> result = null;
		
		try {
			lock.lock();
			
			Map<String, Set<String>> map = topoType == CONSTS.TOPO_TYPE_CONTAIN ? mapContain : mapLink;
			result = map.get(par);
			
		} finally {
			lock.unlock();
		}
		
		return result;
	}
	
	public boolean isExist(String par, String sub, int topoType) {
		boolean ret = false;
		
		try {
			lock.lock();
			
			Map<String, Set<String>> map = topoType == CONSTS.TOPO_TYPE_CONTAIN ? mapContain : mapLink;
			Set<String> val = map.get(par);
			if (val != null)
				ret = val.contains(sub);
			
		} finally {
			lock.unlock();
		}
		
		return ret;
	}
	
	public void clear() {
		try {
			lock.lock();
			
			mapContain.clear();
			mapLink.clear();
			
		} finally {
			lock.unlock();
		}	
	}

}
