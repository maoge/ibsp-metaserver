package ibsp.metaserver.dbservice;

import ibsp.metaserver.autodeploy.utils.JschUserInfo;
import ibsp.metaserver.autodeploy.utils.SSHExecutor;
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

    public static boolean loadServiceInfo(String serviceID, List<InstanceDtlBean> engineList, ResultBean result) {

        Map<Integer, String> serviceStub = MetaDataService.getSubNodesWithType(serviceID, result);
        if (serviceStub == null) {
            return false;
        }

        Integer cmptID = MetaData.get().getComponentID("SDB_ENGINE_CONTAINER");
        String containerId = serviceStub.get(cmptID);
        Set<String> engineStub = MetaDataService.getSubNodes(containerId, result);

        if (engineStub == null || engineStub.isEmpty()) {
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo("ENGINE container subnode is null ......");
            return false;
        }
        for (String id : engineStub) {
            InstanceBean engine = MetaDataService.getInstance(id, result);
            Map<String, InstAttributeBean> engineAttr = MetaDataService.getAttribute(id, result);

            if (engine == null || engineAttr == null) {
                String err = String.format("tidb id:%s, info missing ......", id);
                result.setRetCode(CONSTS.REVOKE_NOK);
                result.setRetInfo(err);
                return false;
            }

            InstanceDtlBean instance = new InstanceDtlBean(engine, engineAttr);
            engineList.add(instance);
        }
        return true;
    }

    public static JsonArray getSdbInfoByService(String servID, ResultBean result) {
        JsonArray array = new JsonArray();
        try {
            List<InstanceDtlBean> engineList = new ArrayList<>();
            if (!loadServiceInfo(servID,  engineList, result))
                return null;

            for (InstanceDtlBean instance : engineList) {
                if (instance.getInstance().getIsDeployed().equals(CONSTS.NOT_DEPLOYED))
                    continue;
                JsonObject object = new JsonObject();
                object.put("ID", instance.getInstID());
                object.put("ADDRESS", instance.getAttribute("IP").getAttrValue()+":"
                        +instance.getAttribute("PORT").getAttrValue());
                array.add(object);
            }
        } catch (Exception e) {
            logger.error("Error get ENGINE info by service ID...", e);
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo(e.getMessage());
            return null;
        }
        return array;
    }

    public static String getSDBInfo(String ip, int port, String sdbUser, String sdbPwd, String command, ResultBean result) {
        JschUserInfo ui = null;
        SSHExecutor executor = null;
        boolean connected = false;

        try {
            ui = new JschUserInfo(sdbUser, sdbPwd, ip, CONSTS.SSH_PORT_DEFAULT);
            executor = new SSHExecutor(ui);
            executor.connect();
            connected = true;
            executor.echo("test"); // 有的机器中间加了跳转和管控防止ssh登录"Last login:xxxxx"串到输出一起显示

            executor.execSingleLine(String.format("sdb -s \"var db = new Sdb();%s\"", command), null);
        } catch (Exception e) {
            logger.error("Failed to execute sdb command", e);
        } finally {
            if (connected) {
                executor.close();
            }
        }

        return null;
    }
}
