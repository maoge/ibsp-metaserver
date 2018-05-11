package ibsp.metaserver.dbservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.ServiceBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.eventbus.EventBean;
import ibsp.metaserver.eventbus.EventBusMsg;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import ibsp.metaserver.utils.Topology;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CacheService {
	
	private static Logger logger = LoggerFactory.getLogger(CacheService.class);
	
//	private static final String GET_SERVICE_NAME_BY_PROXY_ID = 
//			"select serv.INST_ID, serv.SERV_NAME FROM "
//			+ "t_service serv JOIN t_topology top1 ON serv.INST_ID=top1.INST_ID1 "
//			+ "JOIN t_topology top2 ON top1.INST_ID2=top2.INST_ID1 "
//			+ "WHERE top2.INST_ID2=?";
//	
//	private static final String GET_DEPLOYED_PROXY_BY_SERVICE = 
//			"SELECT att.INST_ID, ATTR_NAME, ATTR_VALUE FROM "+ 
//			"t_instance_attr att JOIN t_instance ins ON att.INST_ID=ins.INST_ID "+ 
//			"JOIN t_meta_cmpt cmpt ON ins.CMPT_ID=cmpt.CMPT_ID "+
//			"JOIN t_topology top1 ON ins.INST_ID=top1.INST_ID2 "+ 
//			"JOIN t_topology top2 ON top1.INST_ID1=top2.INST_ID2 "+
//			"JOIN t_service serv ON top2.INST_ID1=serv.INST_ID "+
//			"WHERE cmpt.CMPT_NAME='CACHE_PROXY' AND ins.IS_DEPLOYED='1' AND serv.SERV_NAME=?";
	
	private static final String UPDATE_MASTER_ID = 
			"UPDATE t_instance_attr SET ATTR_VALUE=? "
			+ "WHERE INST_ID=? AND attr_id=208";
	
	private static final String UPDATE_CACHE_SLOT = 
			"UPDATE t_instance_attr SET ATTR_VALUE=? "
			+ "WHERE INST_ID=? AND attr_id=239";

	
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
			if (cacheNodeIds == null)
				continue;
			
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
		
		collectd.setInstance(collectdInstance.getInstance());
		collectd.setAttrMap(collectdInstance.getAttrMap());
		return true;
	}
	
	public static JsonObject getProxyInfoByID(String instID, ResultBean result) {
		JsonObject res = new JsonObject();
		
		Map<String, InstAttributeBean> proxy = MetaDataService.getAttribute(instID, result);
		if (proxy == null || proxy.size()==0) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("No instance found: "+instID);
			return null;
		}
		
		res.put("IP", proxy.get(FixHeader.HEADER_IP).getAttrValue());
		res.put("PORT", proxy.get(FixHeader.HEADER_PORT).getAttrValue());
		res.put("STAT_PORT", proxy.get(FixHeader.HEADER_STAT_PORT).getAttrValue());
		res.put("ID", proxy.get("CACHE_PROXY_ID").getAttrValue());
		res.put("NAME", proxy.get("CACHE_PROXY_NAME").getAttrValue());
		if (proxy.containsKey("RW_SEPARATE"))
			res.put("RW_SEPARATE", proxy.get("RW_SEPARATE").getAttrValue());
			
		Topology topo = MetaData.get().getTopo();
		String containerID = topo.getParent(instID, CONSTS.TOPO_TYPE_CONTAIN);
		if (containerID == null) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("No service found for "+instID);
			return null;
		}
		String serviceID = topo.getParent(containerID, CONSTS.TOPO_TYPE_CONTAIN);
		if (serviceID == null) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo("No service found for "+instID);
			return null;
		}
		ServiceBean service = MetaDataService.getService(serviceID, result);
		if (service == null)
			return null;
		res.put("SERV_ID", service.getInstID());
		res.put("SERV_NAME", service.getServName());

		return res;
	}
	
	public static JsonArray getDeployedProxyByServiceID(String servID, ResultBean result) {
		
		MetaData data = MetaData.get();
		try {
			ServiceBean service = data.getServiceByID(servID);
			if (service == null) {
				String info = String.format("service id:%s, meta data not exists!", servID);
				result.setRetCode(CONSTS.REVOKE_NOK);
				result.setRetInfo(info);
				return null;
			}
			
			Topology topo = data.getTopo();
			Set<String> containers = topo.get(service.getInstID(), CONSTS.TOPO_TYPE_CONTAIN);
			
			for (String containerID : containers) {
				InstanceDtlBean container = data.getInstanceDtlBean(containerID);
				if (container.getInstance().getCmptID()==108) {
					Set<String> proxies = topo.get(containerID, CONSTS.TOPO_TYPE_CONTAIN);
					JsonArray res = new JsonArray();
					
					for (String proxyID : proxies) {
						InstanceDtlBean proxy = data.getInstanceDtlBean(proxyID);
						JsonObject obj = new JsonObject();
						obj.put("IP", proxy.getAttribute(FixHeader.HEADER_IP).getAttrValue());
						obj.put("PORT", proxy.getAttribute(FixHeader.HEADER_PORT).getAttrValue());
						obj.put("ID", proxy.getAttribute("CACHE_PROXY_ID").getAttrValue());
						obj.put("NAME", proxy.getAttribute("CACHE_PROXY_NAME").getAttrValue());
						res.add(obj);
					}
					return res;
				}
			}
			return null;
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
	}
	
	public static JsonArray getNodeClusterInfo(String servID, ResultBean result) {
		JsonArray res = new JsonArray();
		JsonObject serviceInfo = MetaDataService.loadServiceTopoByInstID(servID, result);
		if (result.getRetCode() == CONSTS.REVOKE_NOK) {
			return null;
		}
		
		//analyze json
		JsonArray clusters = null;
		try {
			clusters = serviceInfo.getJsonObject("CACHE_SERV_CONTAINER")
					.getJsonObject("CACHE_NODE_CONTAINER")
					.getJsonArray("CACHE_NODE_CLUSTER");
			
			for (int i=0; i<clusters.size(); i++) {
				JsonObject cluster = clusters.getJsonObject(i);
				if (HttpUtils.isNull(cluster.getString("MASTER_ID")) || 
						HttpUtils.isNull(cluster.getString("CACHE_SLOT"))) {
					result.setRetCode(CONSTS.REVOKE_NOK);
					result.setRetInfo("Node cluster is not initialized!");
					return null;
				}
				JsonArray nodes = cluster.getJsonArray("CACHE_NODE");
				JsonArray newNodes = new JsonArray();
				for (int j=0; j<nodes.size(); j++) {
					JsonObject node = nodes.getJsonObject(j);
					node.remove("OS_USER");
					node.remove("OS_PWD");
					if (node.getString("CACHE_NODE_ID").equals(cluster.getString("MASTER_ID"))) {
						node.put("TYPE", "M");
					} else {
						node.put("TYPE", "S");
					}
					newNodes.add(node);
				}
				cluster.remove("MASTER_ID");
				cluster.put("CACHE_NODE", newNodes);
				res.add(cluster);
			}
		} catch (Exception e) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return null;
		}
		return res;
	}
	
	public static boolean updateMasterID(String masterID, String clusterID, ResultBean result) {
		CRUD c = new CRUD();
		SqlBean sqlBean = new SqlBean(UPDATE_MASTER_ID);
		sqlBean.addParams(new Object[] {masterID, clusterID});
		c.putSqlBean(sqlBean);
		try {
			c.executeUpdate();
			
			JsonObject evJson = new JsonObject();
			evJson.put("INST_ID", clusterID);
			EventBean ev = new EventBean(EventType.e4);
			ev.setUuid(MetaData.get().getUUID());
			ev.setJsonStr(evJson.toString());
			EventBusMsg.publishEvent(ev);
			
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
	}
	
	public static boolean updateHashSlotByCluster(Map<String, String> slots, ResultBean result) {
		CRUD c = new CRUD();
		List<EventBean> events = new ArrayList<EventBean>();
		for (String clusterID : slots.keySet()) {
			SqlBean sqlBean = new SqlBean(UPDATE_CACHE_SLOT);
			sqlBean.addParams(new Object[] {slots.get(clusterID), clusterID});
			c.putSqlBean(sqlBean);
			
			JsonObject evJson = new JsonObject();
			evJson.put("INST_ID", clusterID);
			EventBean ev = new EventBean(EventType.e4);
			ev.setUuid(MetaData.get().getUUID());
			ev.setJsonStr(evJson.toString());
			events.add(ev);
		}
		try {
			c.executeUpdate();
			for (EventBean event : events) {
				EventBusMsg.publishEvent(event);
			}
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(e.getMessage());
			return false;
		}
	}
}
