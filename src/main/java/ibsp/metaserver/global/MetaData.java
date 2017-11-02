package ibsp.metaserver.global;

import ibsp.metaserver.bean.IdSetBean;
import ibsp.metaserver.bean.MetaAttributeBean;
import ibsp.metaserver.bean.MetaComponentBean;
import ibsp.metaserver.bean.RelationBean;
import ibsp.metaserver.dbservice.MetaDataService;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaData {
	
	private static Logger logger = LoggerFactory.getLogger(MetaData.class);
	
	private Map<Integer, MetaAttributeBean> metaAttrMap;
	private Map<Integer, MetaComponentBean> metaCmptMap;
	private Map<String,  Integer> metaCmptName2IDMap;
	private Map<Integer, IdSetBean<Integer>> metaCmpt2AttrMap;
	
	private static MetaData theInstance = null;
	private static ReentrantLock intanceLock = null;
	
	static {
		intanceLock = new ReentrantLock();
	}
	
	public MetaData() {
		metaAttrMap = new ConcurrentHashMap<Integer, MetaAttributeBean>();
		metaCmptMap = new ConcurrentHashMap<Integer, MetaComponentBean>();
		metaCmptName2IDMap = new ConcurrentHashMap<String,  Integer>();
		metaCmpt2AttrMap = new ConcurrentHashMap<Integer, IdSetBean<Integer>>();
	}
	
	public static MetaData get() {
		try {
			intanceLock.lock();
			if (theInstance != null){
				return theInstance;
			} else {
				theInstance = new MetaData();
				theInstance.initData();
			}
		} finally {
			intanceLock.unlock();
		}
		
		return theInstance;
	}
	
	private void initData() {
		LoadMetaAttr();
		LoadMetaCmpt();
		LoadMetaCmpt2Attr();
	}
	
	private void LoadMetaAttr() {
		try {
			intanceLock.lock();
			
			List<MetaAttributeBean> metaAttrList = MetaDataService.getAllMetaAttribute();
			if (metaAttrList == null) {
				return;
			}
			
			metaAttrMap.clear();
			
			for (MetaAttributeBean metaAttr : metaAttrList) {
				if (metaAttr == null)
					continue;
				
				metaAttrMap.put(metaAttr.getAttrID(), metaAttr);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadMetaCmpt() {
		try {
			intanceLock.lock();
			
			List<MetaComponentBean> metaCmptList = MetaDataService.getAllMetaComponent();
			if (metaCmptList == null) {
				return;
			}
			
			metaCmptMap.clear();
			metaCmptName2IDMap.clear();
			
			for (MetaComponentBean metaCmpt : metaCmptList) {
				if (metaCmpt == null)
					continue;
				
				metaCmptMap.put(metaCmpt.getCmptID(), metaCmpt);
				metaCmptName2IDMap.put(metaCmpt.getCmptName(), metaCmpt.getCmptID());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	private void LoadMetaCmpt2Attr() {
		try {
			intanceLock.lock();
			
			List<RelationBean> cmpt2AttrList = MetaDataService.getAllCmpt2Attr();
			if (cmpt2AttrList == null) {
				return;
			}
			
			metaCmpt2AttrMap.clear();
			
			for (RelationBean relationBean : cmpt2AttrList) {
				if (relationBean == null)
					continue;
				
				Integer masterID = (Integer) relationBean.getMasterID();
				Integer slaveID = (Integer) relationBean.getSlaveID();
				
				IdSetBean<Integer> idSet = metaCmpt2AttrMap.get(masterID);
				if (idSet == null) {
					idSet = new IdSetBean<Integer>();
					idSet.addId(slaveID);
					
					metaCmpt2AttrMap.put(masterID, idSet);
				} else {
					idSet.addId(slaveID);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			intanceLock.unlock();
		}
	}
	
	public MetaComponentBean getComponentByName(String cmptName) {
		Integer cmptID = metaCmptName2IDMap.get(cmptName);
		if (cmptID == null)
			return null;
		
		return metaCmptMap.get(cmptID);
	}
	
	public MetaComponentBean getComponentByID(int cmptID) {
		return metaCmptMap.get(cmptID);
	}
	
	public List<MetaAttributeBean> getCmptAttrByName(String cmptName) {
		Integer cmptID = metaCmptName2IDMap.get(cmptName);
		if (cmptID == null)
			return null;
		
		IdSetBean<Integer> idSet = metaCmpt2AttrMap.get(cmptID);
		if (idSet == null)
			return null;
		
		List<MetaAttributeBean> attrs = new LinkedList<MetaAttributeBean>();
		Iterator<Integer> it = idSet.iterator();
		while (it.hasNext()) {
			Integer attrID = it.next();
			MetaAttributeBean attr = metaAttrMap.get(attrID);
			attrs.add(attr);
		}
		
		return attrs;
	}
	
	public List<MetaAttributeBean> getCmptAttrByID(Integer cmptID) {
		IdSetBean<Integer> idSet = metaCmpt2AttrMap.get(cmptID);
		if (idSet == null)
			return null;
		
		List<MetaAttributeBean> attrs = new LinkedList<MetaAttributeBean>();
		Iterator<Integer> it = idSet.iterator();
		while (it.hasNext()) {
			Integer attrID = it.next();
			MetaAttributeBean attr = metaAttrMap.get(attrID);
			attrs.add(attr);
		}
		
		return attrs;
	}
	
	public IdSetBean<Integer> getAttrIdSet(Integer cmptID) {
		return metaCmpt2AttrMap.get(cmptID);
	}
	
	public MetaAttributeBean getAttributeByID(Integer attrID) {
		return metaAttrMap.get(attrID);
	}
	
}
