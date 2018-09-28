package ibsp.metaserver.autodeploy;

import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.bean.ResultBean;
import ibsp.metaserver.dbservice.ConfigDataService;
import ibsp.metaserver.dbservice.MetaDataService;
import ibsp.metaserver.dbservice.SequoiaDBService;
import ibsp.metaserver.eventbus.EventType;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SequoiaDBDeployer implements Deployer {

    private static Logger logger = LoggerFactory.getLogger(SequoiaDBDeployer.class);


    @Override
    public boolean deployService(String serviceID, String user, String pwd, String sessionKey, ResultBean result) {
        List<InstanceDtlBean> pgList = new LinkedList<>();

        if (!SequoiaDBService.loadServiceInfo(serviceID, pgList, result))
            return false;

        boolean isServDeployed = MetaData.get().isServDepplyed(serviceID);

        if (!isServDeployed && pgList != null) {
            for(InstanceDtlBean pg : pgList) {
                if(!deployInstance(serviceID, pg.getInstID(), sessionKey, result)) {
                    return false;
                }
            }
            if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.DEPLOYED, result))
                return false;
            DeployUtils.publishDeployEvent(EventType.e21, serviceID);
        }

        return true;
    }

    @Override
    public boolean undeployService(String serviceID, String sessionKey, ResultBean result) {

        List<InstanceDtlBean> pgList = new LinkedList<>();

        if (!SequoiaDBService.loadServiceInfo(serviceID, pgList, result))
            return false;

        boolean isServDeployed = MetaData.get().isServDepplyed(serviceID);

        if (isServDeployed && pgList != null) {
            for(InstanceDtlBean pg : pgList) {
                if(!undeployInstance(serviceID, pg.getInstID(), sessionKey, result)) {
                    return false;
                }
            }
            if (!ConfigDataService.modServiceDeployFlag(serviceID, CONSTS.NOT_DEPLOYED, result))
                return false;
            DeployUtils.publishDeployEvent(EventType.e21, serviceID);
        }

        DeployUtils.publishDeployEvent(EventType.e22, serviceID);
        return true;
    }

    @Override
    public boolean deployInstance(String serviceID, String instID, String sessionKey, ResultBean result) {

        InstanceDtlBean instDtl = MetaDataService.getInstanceDtl(instID, result);

        if (instDtl == null) {
            String err = String.format("instance id:%s not found!", instID);
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo(err);
            return false;
        }

        int cmptID = instDtl.getInstance().getCmptID();
        boolean deployRet = false;

        switch (cmptID) {
            case 124:    // SDB_PG
                deployRet = ConfigDataService.modInstanceDeployFlag(instID, CONSTS.DEPLOYED, result);
                if(deployRet) {
                    DeployUtils.publishDeployEvent(EventType.e23, instID);
                }
                break;
            default:
                break;
        }

        return deployRet;
    }

    @Override
    public boolean undeployInstance(String serviceID, String instID, String sessionKey, ResultBean result) {

        InstanceDtlBean instDtl = MetaDataService.getInstanceDtl(instID, result);

        if (instDtl == null) {
            String err = String.format("instance id:%s not found!", instID);
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo(err);
            return false;
        }

        int cmptID = instDtl.getInstance().getCmptID();

        boolean undeployRet = false;
        switch (cmptID) {
            case 124:    // DB_PD
                undeployRet = ConfigDataService.modInstanceDeployFlag(instID, CONSTS.NOT_DEPLOYED, result);
                if(undeployRet) {
                    DeployUtils.publishDeployEvent(EventType.e24, instID);
                }
                break;
            default:
                break;
        }

        return undeployRet;
    }

    @Override
    public boolean forceUndeployInstance(String serviceID, String instID, ResultBean result) {
        return false;
    }

    @Override
    public boolean deleteService(String serviceID, String sessionKey, ResultBean result) {
        Topology topo = MetaData.get().getTopo();
        if (topo == null) {
            result.setRetCode(CONSTS.REVOKE_NOK);
            result.setRetInfo("MetaData topo is null!");
            return false;
        }

        // delete t_instance,t_instance_attr,t_topology
        Set<String> sub = topo.get(serviceID, CONSTS.TOPO_TYPE_CONTAIN);
        if (sub != null) {
            for (String subID : sub) {
                Set<String> subsub = topo.get(subID, CONSTS.TOPO_TYPE_CONTAIN);
                if (subsub != null) {
                    for (String subsubID : subsub) {
                        if (!MetaDataService.deleteInstance(subID, subsubID, result))
                            return false;
                    }
                }

                if (!MetaDataService.deleteInstance(serviceID, subID, result))
                    return false;
            }
        }

        // delete t_instance INST_ID = serviceID
        if (!MetaDataService.deleteInstance(serviceID, serviceID, result))
            return false;

        // delete t_service
        if (!MetaDataService.deleteService(serviceID, result))
            return false;

        return true;
    }

    @Override
    public boolean deleteInstance(String serviceID, String instID, String sessionKey, ResultBean result) {
        return MetaDataService.deleteInstance(serviceID, instID, result);
    }
}
