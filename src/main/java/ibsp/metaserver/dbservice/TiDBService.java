package ibsp.metaserver.dbservice;

import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import ibsp.metaserver.bean.IdSetBean;
import ibsp.metaserver.bean.MetaAttributeBean;
import ibsp.metaserver.bean.MetaComponentBean;
import ibsp.metaserver.bean.PosBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.bean.SqlBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.schema.Validator;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.CRUD;
import ibsp.metaserver.utils.FixHeader;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class TiDBService {
	
	private static Logger logger = LoggerFactory.getLogger(TiDBService.class);
	
	private static final String INS_INSTANCE      = "insert into t_instance(INST_ID,CMPT_ID,POS_X,POS_Y,WIDTH,HEIGHT,ROW,COL) "
	                                              + "values(?,?,?,?,?,?,?,?)";
	
	private static final String INS_INSTANCE_ATTR = "insert into t_instance_attr(INST_ID,ATTR_ID,ATTR_NAME,ATTR_VALUE) "
	                                              + "values(?,?,?,?)";
	
	private static final String INS_SERVICE       = "insert into t_service(INST_ID,SERV_NAME,SERV_TYPE,CREATE_TIME) "
	                                              + "values(?,?,?,?)";
	
	private static final String INS_TOPOLOGY      = "insert into t_topology(INST_ID1,INST_ID2,TOPO_TYPE) "
	                                              + "values(?,?,?)";
	
	
	public static boolean saveTiDBTopo(String sTiDBJson, ResultBean result) {
		if (!checkTiDBJson(sTiDBJson, result)) {
			return false;
		}
		
		JsonObject tidbJson = new JsonObject(sTiDBJson);
		JsonObject dbServContainer = tidbJson.getJsonObject(FixHeader.HEADER_DB_SERV_CONTAINER);
		String dbServContainerID = dbServContainer.getString(FixHeader.HEADER_DB_SVC_CONTAINER_ID);
		String dbServContainerName = dbServContainer.getString(FixHeader.HEADER_DB_SVC_CONTAINER_NAME);
		
		JsonObject tidbContainer = dbServContainer.getJsonObject(FixHeader.HEADER_DB_TIDB_CONTAINER);
		JsonObject tikvContainer = dbServContainer.getJsonObject(FixHeader.HEADER_DB_TIKV_CONTAINER);
		JsonObject pdContainer = dbServContainer.getJsonObject(FixHeader.HEADER_DB_PD_CONTAINER);
		JsonObject collectd = dbServContainer.getJsonObject(FixHeader.HEADER_DB_COLLECTD);
		
		CRUD curd = new CRUD();
		
		MetaComponentBean dbServContainerComponent = MetaData.get().getComponentByName(FixHeader.HEADER_DB_SERV_CONTAINER);
		Integer dbServContainerCmptID = dbServContainerComponent.getCmptID();
		
		// add db service container
		addService(curd, dbServContainerID, dbServContainerName, CONSTS.SERV_TYPE_DB);
		
		// insert db service container instance
		PosBean posDBContainer = new PosBean();
		addComponentInstance(curd, dbServContainerID, dbServContainerCmptID, posDBContainer);
					
		// insert db service container attribute
		addComponentAttrbute(curd, dbServContainerID, dbServContainer, FixHeader.HEADER_DB_SERV_CONTAINER);
		
		// tidb container
		if (tidbContainer != null) {
			String tidbContainerID = tidbContainer.getString(FixHeader.HEADER_TIDB_CONTAINER_ID);
			JsonObject tidbContainerPos = tidbContainer.getJsonObject(FixHeader.HEADER_POS);
			PosBean posTidbContainer = new PosBean();
			getPos(tidbContainerPos, posTidbContainer);
			
			MetaComponentBean tidbContainerComponent = MetaData.get().getComponentByName(FixHeader.HEADER_DB_TIDB_CONTAINER);
			Integer tidbContainerCmptID = tidbContainerComponent.getCmptID();
			
			// insert tidb container instance
			addComponentInstance(curd, tidbContainerID, tidbContainerCmptID, posTidbContainer);
			
			// insert tidb container attribute
			addComponentAttrbute(curd, tidbContainerID, tidbContainer, FixHeader.HEADER_DB_TIDB_CONTAINER);
			
			// insert tidb contain relation
			addRelation(curd, dbServContainerID, tidbContainerID, CONSTS.TOPO_TYPE_CONTAIN);
			
			// tidb-server list
			JsonArray tidbServerList = tidbContainer.getJsonArray(FixHeader.HEADER_DB_TIDB);
			for (int i = 0; i < tidbServerList.size(); i++) {
				JsonObject tidbServer = tidbServerList.getJsonObject(i);
				String tidbID = tidbServer.getString(FixHeader.HEADER_TIDB_ID);
				
				MetaComponentBean tidbComponent = MetaData.get().getComponentByName(FixHeader.HEADER_DB_TIDB);
				Integer tidbCmptID = tidbComponent.getCmptID();
				
				PosBean posTidbServer = new PosBean();
				
				// insert tidb-server instance
				addComponentInstance(curd, tidbID, tidbCmptID, posTidbServer);
				
				// insert tidb-server attribute
				addComponentAttrbute(curd, tidbID, tidbServer, FixHeader.HEADER_DB_TIDB);
				
				// insert tidb-server contain relation
				addRelation(curd, tidbContainerID, tidbID, CONSTS.TOPO_TYPE_CONTAIN);
			}
		}
		
		// tikv container
		if (tikvContainer != null) {
			String tikvContainerID = tikvContainer.getString(FixHeader.HEADER_TIKV_CONTAINER_ID);
			JsonObject tikvContainerPos = tikvContainer.getJsonObject(FixHeader.HEADER_POS);
			PosBean posTikvContainer = new PosBean();
			getPos(tikvContainerPos, posTikvContainer);
			
			MetaComponentBean tikvContainerComponent = MetaData.get().getComponentByName(FixHeader.HEADER_DB_TIKV_CONTAINER);
			Integer tikvContainerCmptID = tikvContainerComponent.getCmptID();
			
			// insert tikv container instance
			addComponentInstance(curd, tikvContainerID, tikvContainerCmptID, posTikvContainer);
			
			// insert tikv container attribute
			addComponentAttrbute(curd, tikvContainerID, tikvContainer, FixHeader.HEADER_DB_TIKV_CONTAINER);
			
			// insert tikv contain relation
			addRelation(curd, dbServContainerID, tikvContainerID, CONSTS.TOPO_TYPE_CONTAIN);
			
			// tikv-server list
			JsonArray tikvServerList = tikvContainer.getJsonArray(FixHeader.HEADER_DB_TIKV);
			for (int i = 0; i < tikvServerList.size(); i++) {
				JsonObject tikvServer = tikvServerList.getJsonObject(i);
				String tikvID = tikvServer.getString(FixHeader.HEADER_TIKV_ID);
				
				MetaComponentBean tikvComponent = MetaData.get().getComponentByName(FixHeader.HEADER_DB_TIKV);
				Integer tikvCmptID = tikvComponent.getCmptID();
				
				PosBean posTikvServer = new PosBean();
				
				// insert tikv-server instance
				addComponentInstance(curd, tikvID, tikvCmptID, posTikvServer);
				
				// insert tikv-server attribute
				addComponentAttrbute(curd, tikvID, tikvServer, FixHeader.HEADER_DB_TIKV);
				
				// insert tikv-server contain relation
				addRelation(curd, tikvContainerID, tikvID, CONSTS.TOPO_TYPE_CONTAIN);
			}
		}
		
		// pd container
		if (pdContainer != null) {
			String pdContainerID = pdContainer.getString(FixHeader.HEADER_PD_CONTAINER_ID);
			JsonObject pdContainerPos = pdContainer.getJsonObject(FixHeader.HEADER_POS);
			PosBean posPDContainer = new PosBean();
			getPos(pdContainerPos, posPDContainer);
			
			MetaComponentBean pdContainerComponent = MetaData.get().getComponentByName(FixHeader.HEADER_DB_PD_CONTAINER);
			Integer pdContainerCmptID = pdContainerComponent.getCmptID();
			
			// insert pd container instance
			addComponentInstance(curd, pdContainerID, pdContainerCmptID, posPDContainer);
			
			// insert pd container attribute
			addComponentAttrbute(curd, pdContainerID, pdContainer, FixHeader.HEADER_DB_PD_CONTAINER);
			
			// insert pd contain relation
			addRelation(curd, dbServContainerID, pdContainerID, CONSTS.TOPO_TYPE_CONTAIN);
			
			// pd-server list
			JsonArray pdServerList = pdContainer.getJsonArray(FixHeader.HEADER_DB_PD);
			for (int i = 0; i < pdServerList.size(); i++) {
				JsonObject pdServer = pdServerList.getJsonObject(i);
				String pdID = pdServer.getString(FixHeader.HEADER_PD_ID);
				
				MetaComponentBean pdComponent = MetaData.get().getComponentByName(FixHeader.HEADER_DB_PD);
				Integer pdCmptID = pdComponent.getCmptID();
				
				PosBean posPDServer = new PosBean();
				
				// insert pd-server instance
				addComponentInstance(curd, pdID, pdCmptID, posPDServer);
				
				// insert pd-server attribute
				addComponentAttrbute(curd, pdID, pdServer, FixHeader.HEADER_DB_PD);
				
				// insert pd-server contain relation
				addRelation(curd, pdContainerID, pdID, CONSTS.TOPO_TYPE_CONTAIN);
			}
		}
		
		// collectd
		if (collectd != null) {
			String collectdID = collectd.getString(FixHeader.HEADER_COLLECTD_ID);
			JsonObject collectdPosJson = pdContainer.getJsonObject(FixHeader.HEADER_POS);
			PosBean posCollectd = new PosBean();
			getPos(collectdPosJson, posCollectd);
			
			MetaComponentBean dbCollectdComponent = MetaData.get().getComponentByName(FixHeader.HEADER_DB_COLLECTD);
			Integer dbCollectdCmptID = dbCollectdComponent.getCmptID();
			
			// insert db collectd
			addComponentInstance(curd, collectdID, dbCollectdCmptID, posCollectd);
			
			// insert db collectd attribute
			addComponentAttrbute(curd, collectdID, collectd, FixHeader.HEADER_DB_COLLECTD);
			
			// insert db collectd relation
			addRelation(curd, dbServContainerID, collectdID, CONSTS.TOPO_TYPE_CONTAIN);
		}
		
		return curd.executeUpdate(true, result);
	}
	
	// dbServContainerID, dbServContainerName, CONSTS.SERV_TYPE_DB, dt
	private static void addService(CRUD curd, String dbServContainerID, String dbServContainerName, String servType) {
		long dt = System.currentTimeMillis();
		SqlBean sqlServBean = new SqlBean(INS_SERVICE);
		sqlServBean.addParams(new Object[]{dbServContainerID, dbServContainerName, servType, dt});
		curd.putSqlBean(sqlServBean);
	}
	
	private static void addRelation(CRUD curd, String id1, String id2, int topoType) {
		SqlBean sqlTopo = new SqlBean(INS_TOPOLOGY);
		sqlTopo.addParams(new Object[]{id1, id2, topoType});
		curd.putSqlBean(sqlTopo);
	}
	
	private static void addComponentAttrbute(CRUD curd, String instanceID, JsonObject cmptJson, String cmptName) {
		MetaComponentBean component = MetaData.get().getComponentByName(cmptName);
		Integer cmptID = component.getCmptID();
		
		try {
			IdSetBean<Integer> attrIdSet = MetaData.get().getAttrIdSet(cmptID);
			Iterator<Integer> it = attrIdSet.iterator();
			while (it.hasNext()) {
				
				Integer attrID = it.next();
				MetaAttributeBean metaAttr = MetaData.get().getAttributeByID(attrID);
				String attrName = metaAttr.getAttrName();
				String attrValue = cmptJson.getString(attrName);
				
				SqlBean sqlAttr = new SqlBean(INS_INSTANCE_ATTR);
				sqlAttr.addParams(new Object[]{instanceID, attrID, attrName, attrValue});
				curd.putSqlBean(sqlAttr);
				
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void addComponentInstance(CRUD curd, String instanceID, Integer cmptID, PosBean pos) {
		SqlBean sqlInsertInstance = new SqlBean(INS_INSTANCE);
		sqlInsertInstance.addParams(new Object[]{instanceID, cmptID, pos.getX(), pos.getY(),
				pos.getWidth(), pos.getHeight(), pos.getRow(), pos.getCol()});
		curd.putSqlBean(sqlInsertInstance);
	}
	
	private static void getPos(JsonObject posJson, PosBean pos) {
		Integer x = posJson.getInteger(FixHeader.HEADER_X);
		Integer y = posJson.getInteger(FixHeader.HEADER_Y);
		
		Integer width  = posJson.getInteger(FixHeader.HEADER_WIDTH);
		Integer height = posJson.getInteger(FixHeader.HEADER_HEIGHT);
		
		Integer row = posJson.getInteger(FixHeader.HEADER_ROW);
		Integer col = posJson.getInteger(FixHeader.HEADER_COL);
		
		pos.setX(x != null ? x : 0);
		pos.setY(y != null ? y : 0);
		pos.setWidth(width != null ? width : CONSTS.POS_DEFAULT_VALUE);
		pos.setHeight(height != null ? height : CONSTS.POS_DEFAULT_VALUE);
		pos.setRow(row != null ? row : CONSTS.POS_DEFAULT_VALUE);
		pos.setCol(col != null ? col : CONSTS.POS_DEFAULT_VALUE);
	}
	
	private static boolean checkTiDBJson(String sTiDBJson, ResultBean result) {
		boolean ret = false;
		try {
			ret = Validator.validateTiDBJson(sTiDBJson);
		} catch (IOException | ProcessingException e) {
			result.setRetCode(CONSTS.REVOKE_NOK);
			result.setRetInfo(CONSTS.ERR_JSON_SCHEME_VALI_ERR);
			logger.error(e.getMessage(), e);
		}
		
		return ret;
	}
	
}
