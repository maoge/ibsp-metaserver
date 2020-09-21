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

//import java.text.ParseException;
//import java.text.SimpleDateFormat;
import java.util.*;

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

    public static JsonObject getCLInfo(String ip, String sdbUser, String sdbPwd) {
        return connSdbAndExec(ip, sdbUser, sdbPwd, (executor) -> {
            String clSql = "SELECT " +
                "t1.Name, " +
                "t1.Details.GroupName, " +
                "t1.Details.Indexes, " +
                "t1.Details.TotalRecords, " +
                "t1.Details.TotalLobs, " +
                "t1.Details.TotalDataPages, " +
                "t1.Details.TotalIndexPages, " +
                "t1.Details.TotalLobPages, " +
                "t1.Details.TotalDataFreeSpace/1048576, " +
                "t1.Details.TotalIndexFreeSpace/1048576 " +
                "FROM (SELECT * FROM $SNAPSHOT_CL WHERE NodeSelect=\\\"master\\\" split BY Details) AS t1 ORDER BY t1.Name";
            String cataSql = "SELECT " +
                    "Name, ShardingKey, ShardingType, IsMainCL, MainCLName, AutoSplit, CataInfo " +
                    "from $SNAPSHOT_CATA Order by Name";

            StringBuffer res =  new StringBuffer()
                    .append("{\"clInfo\" : [")
                    .append(executor.execSdbSql(clSql).replaceAll("\r\n", ","))
                    .append("],\"cataInfo\" : [")
                    .append(executor.execSdbSql(cataSql).replaceAll("\r\n", ","))
                    .append("]}");

            return new JsonObject(res.toString());
        });
    }

    public static JsonObject getCSInfo(String ip, String sdbUser, String sdbPwd) {

        return connSdbAndExec(ip, sdbUser, sdbPwd, executor -> {
            String sql = "SELECT " +
                    "Name, " +
                    "PageSize/1024, " +
                    "LobPageSize/1024, " +
                    "GroupName, " +
                    "TotalRecords, " +
                    "FreeDataSize/1048576, " +
                    "FreeIndexSize/1048576, " +
                    "FreeSize/1048576, " +
                    "MaxDataCapSize/1073741824, " +
                    "MaxIndexCapSize/1073741824, " +
                    "MaxLobCapSize/1073741824, " +
                    "TotalDataSize/1048576, " +
                    "TotalIndexSize/1048576, " +
                    "TotalSize/1048576 " +
                    "FROM $SNAPSHOT_CS WHERE NodeSelect=\\\"master\\\" ORDER BY Name";

            String cmd = "db.listCollectionSpaces()";

            StringBuffer res =  new StringBuffer()
                    .append("{\"csInfo\" : [")
                    .append(executor.execSdbSql(sql).replaceAll("\r\n", ","))
                    .append("],\"listCS\" : [")
                    .append(executor.execSdbCmd(cmd).replaceAll("\r\n", ","))
                    .append("]}");

            return new JsonObject(res.toString());

        });

    }

    public static boolean addCS(String ip, String sdbUser, String sdbPwd, String csName, String domainName,
                                ResultBean resultBean) {

        String cmd = String.format("db.createCS(\"%s\" ,{Domain:\"%s\"})", csName, domainName);
        return execSingleCmdBool(ip, sdbUser, sdbPwd, cmd, resultBean);
    }

    public static boolean delCS(String ip, String sdbUser, String sdbPwd, String csName, ResultBean resultBean) {

        String cmd = String.format("db.dropCS(\"%s\")", csName);
        return execSingleCmdBool(ip, sdbUser, sdbPwd, cmd, resultBean);
    }

    public static boolean addCL(String ip, String sdbUser, String sdbPwd, String csName, String clName, String clType,
                                String sharedingKey, String replicaNum, ResultBean resultBean) {

        String cmd = String.format("db.%s.createCL(\"%s\" ,{ShardingKey:{%s:1},AutoSplit:true," +
                "ShardingType:\"%s\", Partition:4096, Compressed:false,ReplSize:%s,EnsureShardingIndex:false})",
                csName, clName, sharedingKey, clType, replicaNum);
        return execSingleCmdBool(ip, sdbUser, sdbPwd, cmd, resultBean);
    }

    public static boolean delCL(String ip, String sdbUser, String sdbPwd, String csName, String clName,
                                ResultBean resultBean) {

        String cmd = String.format("db.%s.dropCL(\"%s\")", csName , clName);
        return execSingleCmdBool(ip, sdbUser, sdbPwd, cmd, resultBean);
    }

    public static boolean detachCL(String ip, String sdbUser, String sdbPwd, String mainCL, String clName,
                                ResultBean resultBean) {

        String cmd = String.format("db.%s.detachCL(\"%s\")", mainCL , clName);
        return execSingleCmdBool(ip, sdbUser, sdbPwd, cmd, resultBean);
    }

    public static boolean attachCL(String ip, String sdbUser, String sdbPwd, String mainCL, String clName, String field,
                                   String lowBound, String upBound, ResultBean resultBean) {

        String cmd = String.format("db.%s.attachCL(\"%s\", {LowBound: { %s: %s }, UpBound: { %s: %s }})", mainCL ,
                clName, field, lowBound, field, upBound);
        return execSingleCmdBool(ip, sdbUser, sdbPwd, cmd, resultBean);
    }

    /*public static boolean autoAttachCL (String ip, String sdbUser, String sdbPwd, String mainCL, String domain,
                                        String field, String shardingKey, String beginDate, String endDate,
                                        ResultBean resultBean) {
        return connSdbAndExec(ip, sdbUser, sdbPwd, executor -> {
            String createCSCmd = "db.createCS(\"%s\" ,{Domain:\"%s\"})";
            String createCLCmd = "db.%s.createCL(\"%s\" ,{ShardingKey:{%s:1},AutoSplit:true," +
                            "ShardingType:\"%s\", Partition:4096, Compressed:false,ReplSize:%s,EnsureShardingIndex:false})";

            SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
            Calendar calendar = Calendar.getInstance();
            Date begin = null;
            Date end = null;
            try {
                begin = daySdf.parse(beginDate);
                end   = daySdf.parse(endDate);

                int days = (int) ((end.getTime() - begin.getTime()) / (24*60*60*1000)) + 1;
                for(int i = 0 ;i< days; i++) {
                    calendar.clear();
                    calendar.setTime(begin);
                    calendar.set(Calendar.DAYOF, 0);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    String lowBound = sdf.format(calendar.getTime());

                    calendar.clear();
                    calendar.setTime(end);
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999999);
                    String upBound = sdf.format(calendar.getTime());
                    executor.execSdbCmd(String.format(createCSCmd, "poc" + daySdf.format(begin)))
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return true;

        });
    }*/

    public static JsonArray listDomains(String ip, String sdbUser, String sdbPwd) {
        String cmd = "db.listDomains()";
        return execSingleCmdJsonArr(ip, sdbUser, sdbPwd, cmd);
    }

    public static boolean createDomain(String ip, String sdbUser, String sdbPwd, String domainName, ResultBean resultBean) {
        //获取group组
        String listGroupCmd = "db.listReplicaGroups()";
        JsonArray groupArr = execSingleCmdJsonArr(ip, sdbUser, sdbPwd, listGroupCmd);

        JsonArray groups = new JsonArray();

        for(int i =0,len = groupArr.size();i<len ;i++) {
            JsonObject json = groupArr.getJsonObject(i);
            int roleId = json.getInteger("Role");
            //- 0：数据节点 - 1：协调节点 - 2：编目节点
            if(roleId == 0) {
                groups.add(json.getString("GroupName"));
            }
        }

        //添加domain
        String cmd = String.format("db.createDomain(\"%s\", %s, {AutoSplit: true })", domainName, groups.toString());
        return execSingleCmdBool(ip, sdbUser, sdbPwd, cmd, resultBean);
    }

    public static boolean dropDomain(String ip, String sdbUser, String sdbPwd, String domainName, ResultBean resultBean) {
        //添加domain
        String cmd = String.format("db.dropDomain(\"%s\")", domainName);
        return execSingleCmdBool(ip, sdbUser, sdbPwd, cmd, resultBean);
    }


    //------------------------------------private method-----------------------------------
    @SuppressWarnings("unused")
	private static JsonArray execSingleSql(String ip, String sdbUser, String sdbPwd, String sql) {
        return connSdbAndExec(ip, sdbUser, sdbPwd, executor -> {
            String res = executor.execSdbSql(sql);
            res = "[" + res.replaceAll("\r\n", ",") + "]";
            return new JsonArray(res);
        });
    }

    private static JsonArray execSingleCmdJsonArr(String ip, String sdbUser, String sdbPwd, String cmd) {
        return connSdbAndExec(ip, sdbUser, sdbPwd, executor -> {
            String res = executor.execSdbCmd(cmd);
            res = "[" + res.replaceAll("\r\n", ",") + "]";
            return new JsonArray(res);
        });
    }

    private static boolean execSingleCmdBool(String ip, String sdbUser, String sdbPwd, String cmd, ResultBean resultBean) {
        JsonObject jsonObject = connSdbAndExec(ip, sdbUser, sdbPwd, executor -> {
            String res = executor.execSdbCmd(cmd);
            JsonObject json = new JsonObject();
            if(res.contains("error") || res.contains("exception")) {
                json.put("error", res);
                logger.error("exec sdb command err : " + res);
            }
            return json;
        });

        boolean isSuccess = jsonObject != null && jsonObject.size() == 0;
        if(!isSuccess) {
            resultBean.setRetCode(CONSTS.REVOKE_NOK);
            resultBean.setRetInfo(jsonObject == null ? "null" : jsonObject.toString());
        }
        return isSuccess;
    }

    private static <T> T connSdbAndExec(String ip, String sdbUser, String sdbPwd, SdbHandle<T> handle) {
        JschUserInfo ui;
        SSHExecutor executor = null;
        T t = null;
        boolean connected = false;
        try {
            ui = new JschUserInfo(sdbUser, sdbPwd, ip, CONSTS.SSH_PORT_DEFAULT);
            executor = new SSHExecutor(ui);
            executor.connect();
            connected = true;
            executor.echo("test"); // 有的机器中间加了跳转和管控防止ssh登录"Last login:xxxxx"串到输出一起显示

            t = handle.handle(executor);
        } catch (Exception e) {
            logger.error("Failed to execute sdb command", e);
        } finally {
            if (connected) {
                executor.close();
            }
        }

        return t;
    }

    @FunctionalInterface
    private interface SdbHandle<T> {
        T handle(SSHExecutor executor) throws InterruptedException;
    }
}
