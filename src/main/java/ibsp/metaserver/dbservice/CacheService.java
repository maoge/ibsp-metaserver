package ibsp.metaserver.dbservice;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import io.vertx.core.json.JsonObject;

public class CacheService {
	
	private static Logger logger = LoggerFactory.getLogger(CacheService.class);
	
	public static JsonObject getProxyInfoByID(String instID, ResultBean result) {
		JsonObject res = new JsonObject();
		
		List<InstAttributeBean> proxy = MetaDataService.getInstanceAttribute(instID);
		if (proxy == null || proxy.size()==0) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("No instance found: "+instID);
			return null;
		}
		
		for (InstAttributeBean attr : proxy) {
			String name = attr.getAttrName();
			switch (name) {
			case "IP":
				res.put("IP", attr.getAttrValue());
				break;
			case "PORT":
				res.put("PORT", attr.getAttrValue());
				break;
			case "STAT_PORT":
				res.put("STAT_PORT", attr.getAttrValue());
				break;
			default:
				break;
			}
			res.put("NAME", instID);
		}
		return res;
	}
	
	public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> nodeClusterList,
			List<InstanceDtlBean> proxyList, InstanceDtlBean collectd, ResultBean result) {
		
		Map<Integer, String> serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
		if (serviceStub == null) {
			return false;
		}
		
		return  getNodeClustersByServIdOrServiceStub(serviceID, serviceStub, nodeClusterList, result) &&
				getCacheProxiesByServIdOrServiceStub(serviceID, serviceStub, proxyList, result) &&
				getCollectdInfoByServIdOrServiceStub(serviceID, serviceStub, collectd, result);
	}
	
	public static boolean getNodeClustersByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			List<InstanceDtlBean> nodeClusterList, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
		
		Integer cacheNodeContainerCmptID = MetaData.get().getComponentID("CACHE_NODE_CONTAINER");
		String cacheNodeContainerID = serviceStub.get(cacheNodeContainerCmptID);
		Set<String> nodeClusters = MetaDataService.getSubNodes(cacheNodeContainerID, result);
		if (nodeClusters == null || nodeClusters.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("cache node container subnode is null ......");
			return false;
		}
		
		for (String nodeClusterId : nodeClusters) {
			InstanceDtlBean nodeClusterInstance = MetaDataService.getInstanceDtl(nodeClusterId, result);
			if (nodeClusterInstance == null) {
				return false;
			}
			
			Set<String> cacheNodeIds = MetaDataService.getSubNodes(nodeClusterId, result);
			for (String cacheNodeId : cacheNodeIds) {
				InstanceDtlBean cacheNodeInstance = MetaDataService.getInstanceDtl(cacheNodeId, result);
				if (cacheNodeInstance == null) {
					return false;
				}
				
				nodeClusterInstance.addSubInstance(cacheNodeInstance);
			}
			
			nodeClusterList.add(nodeClusterInstance);
		}
		
		return true;
	}
	
	public static boolean getCacheProxiesByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			List<InstanceDtlBean> cacheProxyList, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
		
		Integer cacheProxyCmptID = MetaData.get().getComponentID("CACHE_PROXY_CONTAINER");
		String cacheProxyContainerID = serviceStub.get(cacheProxyCmptID);
		Set<String> cacheProxies = MetaDataService.getSubNodes(cacheProxyContainerID, result);
		if (cacheProxies == null || cacheProxies.isEmpty()) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("cache proxy container subnode is null ......");
			return false;
		}
		
		for (String nodeProxyId : cacheProxies) {
			InstanceDtlBean nodeProxyInstance = MetaDataService.getInstanceDtl(nodeProxyId, result);
			if (nodeProxyInstance == null) {
				return false;
			}
			
			cacheProxyList.add(nodeProxyInstance);
		}
		
		return true;
	}
	
	public static boolean getCollectdInfoByServIdOrServiceStub(String serviceID, Map<Integer, String> serviceStub,
			InstanceDtlBean collectd, ResultBean result) {
		
		if (serviceStub == null) {
			serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
			if(serviceStub == null) {
				return false;
			}
		}
		Integer cacheCollectdCmptID = MetaData.get().getComponentID("CACHE_COLLECTD");
		String id = serviceStub.get(cacheCollectdCmptID);
		InstanceDtlBean collectdInstance = MetaDataService.getInstanceDtl(id, result);
		if (collectdInstance == null) {
			String err = String.format("Cache collectd id:%s, info missing ......", id);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(err);
			return false;
		}
		
		return true;
	}
	
}
