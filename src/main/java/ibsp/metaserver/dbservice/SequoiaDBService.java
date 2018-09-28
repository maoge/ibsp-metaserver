package ibsp.metaserver.dbservice;

import ibsp.metaserver.bean.InstAttributeBean;
import ibsp.metaserver.bean.InstanceBean;
import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SequoiaDBService {

    private static Logger logger = LoggerFactory.getLogger(SequoiaDBService.class);

    public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> pgList, ResultBean result) {

        Map<Integer, String> serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
        if (serviceStub == null) {
            return false;
        }

        Integer cmptID = MetaData.get().getComponentID("SDB_PG_CONTAINER");
        String containerId = serviceStub.get(cmptID);
        Set<String> pgStub = MetaDataService.getSubNodes(containerId, result);

        if (pgStub == null || pgStub.isEmpty()) {
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo("pg container subnode is null ......");
            return false;
        }
        for (String id : pgStub) {
            InstanceBean pg = MetaDataService.getInstance(id, result);
            Map<String, InstAttributeBean> pgAttr = MetaDataService.getAttribute(id, result);

            if (pg == null || pgAttr == null) {
                String err = String.format("tidb id:%s, info missing ......", id);
                result.setRetCode(CONSTS.REVOKE_NOK);
                result.setRetInfo(err);
                return false;
            }

            InstanceDtlBean instance = new InstanceDtlBean(pg, pgAttr);
            pgList.add(instance);
        }
        return true;
    }

    public static JsonArray getSdbInfoByService(String servID, ResultBean result) {
        JsonArray array = new JsonArray();
        try {
            List<InstanceDtlBean> pgList = new ArrayList<>();
            if (!loadServiceInfo(servID,  pgList, result))
                return null;

            for (InstanceDtlBean instance : pgList) {
                if (instance.getInstance().getIsDeployed().equals(CONSTS.NOT_DEPLOYED))
                    continue;
                JsonObject object = new JsonObject();
                object.put("ID", instance.getInstID());
                object.put("ADDRESS", instance.getAttribute("IP").getAttrValue()+":"
                        +instance.getAttribute("PORT").getAttrValue());
                array.add(object);
            }
        } catch (Exception e) {
            logger.error("Error get PG info by service ID...", e);
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo(e.getMessage());
            return null;
        }
        return array;
    }
}
