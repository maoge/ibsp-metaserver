package ibsp.metaserver.monitor;

import ibsp.metaserver.bean.*;
import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.MonitorData;
import ibsp.metaserver.utils.CONSTS;
import ibsp.metaserver.utils.FixHeader;
import ibsp.metaserver.utils.HttpUtils;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TiDBServiceMonitor {

    private static Logger logger = LoggerFactory.getLogger(TiDBServiceMonitor.class);
    private static final String PD_API_PREFIX = "pd/api/v1/";

    public static void excute(ServiceBean serviceBean){
        if(CONSTS.NOT_DEPLOYED.equalsIgnoreCase(serviceBean.getDeployed())) {
            return;
        }

        String servId = serviceBean.getInstID();

        List<InstanceDtlBean> pds = MetaData.get().getPDsByServId(servId);
        if(pds == null)
            return;

        for(InstanceDtlBean pd : pds) {
            checkPDStatus(pd);
        }

        List<InstanceDtlBean> tidbs = MetaData.get().getTiDBsByServId(servId);

        if(tidbs == null)
            return;

        for(InstanceDtlBean tidb : tidbs) {
            checkTiDBStatus(tidb);
        }
    }

    private static boolean checkPDStatus(InstanceDtlBean pd) {
        if(pd == null)
            return false;
        return checkPDStatus(pd, true);
    }

    //检测PD状态。
    private static boolean checkPDStatus(InstanceDtlBean pd, boolean recycle) {
        boolean res = false;
        JsonObject json = getPDJsonByInst(pd, "leader");

        res = json != null;
        if(!recycle) {
            return res;
        }

        //可能是网络问题，再重试3次
        if(json == null) {
            for(int i=0; i < 3; i++) {
                if(res = checkPDStatus(pd, false)) {
                    break;
                }
            }
        }

        //TODO 重试3次后还是失败的，通过ssh连接查看进程是不是存在，不存在的就重启这个节点。
        if(!res) {

        }else {
            //如果正好检测的节点是主节点，就检测其他信息。不是主节点的 不采集
            String pdLeader = json.getString("name");
            if(pd.getInstID().equals(pdLeader)) {
                //主动采集PD上的metrics信息
                PDClusterStatus pdClusterStatus = getPDMetricsByInst(pd);
                //计算histogram信息
                pdCalc(pd.getInstID(), pdClusterStatus);

            }
        }
        return res;
    }

    private static boolean checkTiDBStatus(InstanceDtlBean tidb) {
        if(tidb == null)
            return false;
        return checkTiDBStatus(tidb, true);
    }

    private static boolean checkTiDBStatus(InstanceDtlBean tidb, boolean recycle) {
        boolean res = false;
        JsonObject json = getTiDBJsonByInst(tidb, "status");

        res = json != null;

        if(!recycle) {
            return res;
        }

        //可能是网络问题，再重试3次
        if(json == null) {
            for(int i=0; i < 3; i++) {
                if(res = checkTiDBStatus(tidb, false)) {
                    break;
                }
            }
        }

        //TODO 重试3次后还是失败的，通过ssh连接查看进程是不是存在，不存在的就重启这个节点。
        if(!res) {

        }else {
            //节点正常 就采集其他信息
            TiDBMetricsStatus tiDBMetricsStatus = getTIDBMetricsByInst(tidb);
            tidbCalc(tidb.getInstID(), tiDBMetricsStatus);
        }
        return res;
    }

    private static JsonObject getPDJsonByInst(InstanceDtlBean instanceDtlBean, String api){
        String host      = instanceDtlBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port      = instanceDtlBean.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
        String urlString = makeUrl(host, port, PD_API_PREFIX +api);
        JsonObject json  = null;
        String res;

        try {
            res = HttpUtils.getUrlData(urlString);
            json = new JsonObject(res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return  json;
    }

    private static JsonObject getTiDBJsonByInst(InstanceDtlBean instanceDtlBean, String api){
        String host      = instanceDtlBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port      = instanceDtlBean.getAttribute(FixHeader.HEADER_STAT_PORT).getAttrValue();
        String urlString = makeUrl(host, port, api);
        JsonObject json  = null;
        String res;

        try {
            res = HttpUtils.getUrlData(urlString);
            json = new JsonObject(res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return  json;
    }

    private static PDClusterStatus getPDMetricsByInst(InstanceDtlBean instanceDtlBean) {
        String host      = instanceDtlBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port      = instanceDtlBean.getAttribute(FixHeader.HEADER_PORT).getAttrValue();
        String urlString = makeUrl(host, port, "metrics");
        PDClusterStatus status = new PDClusterStatus();

        BufferedReader reader = null;
        Histogram txn = null;

        URL url;
        HttpURLConnection urlcon = null;
        boolean isConn = false;

        try {
            url = new URL(urlString);

            urlcon = (HttpURLConnection) url.openConnection();
            urlcon.setRequestMethod("POST");
            urlcon.setDoOutput(false);
            urlcon.setReadTimeout(50000);
            urlcon.setConnectTimeout(50000);
            urlcon.setRequestProperty("Accept", "text/plain;version=0.0.4;q=1,*/*;q=0.1");
            urlcon.setRequestProperty("User-Agent", "Prometheus/2.2.1");
            urlcon.setRequestProperty("content-type", "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");

            reader = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
            isConn = true;

            String line;
            txn = new Histogram();

            double maxLeaderScore = 0D;
            double minLeaderScore = 0D;

            double maxRegionScore = 0D;
            double minRegionScore = 0D;

            while( (line= reader.readLine()) != null) {
                String pattern = "(\\w+)\\{(\\S*)\\} (\\S*)";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(line);

                String name = null;
                String prop = null;
                String value = null;

                if (m.find()) {
                    name = m.group(1);
                    prop = m.group(2);
                    value = m.group(3);
                }

                String duractionPatten = "grpc_method=\"Txn\",\\S*le\\=\"(\\S*)\"";
                String duractionSumAndCountPatten = ".*grpc_method=\"Txn\".*";
                Pattern duraction = Pattern.compile(duractionPatten);
                Pattern duractionSumAndCount = Pattern.compile(duractionSumAndCountPatten);

                if(name !=null) {
                    switch (name) {
                        case "pd_cluster_status" :
                            if(prop.contains("type=\"storage_capacity\""))
                                status.setCapacity(Double.valueOf(value));

                            if(prop.contains("type=\"storage_size\""))
                                status.setCurrentSize(Double.valueOf(value));

                            if(prop.contains("type=\"store_up_count\""))
                                status.setStoreUpCount(Integer.valueOf(value));

                            if(prop.contains("type=\"store_down_count\""))
                                status.setStoreDownCount(Integer.valueOf(value));

                            if(prop.contains("type=\"store_offline_count\""))
                                status.setStoreOfflineCount(Integer.valueOf(value));

                            if(prop.contains("type=\"store_tombstone_count\""))
                                status.setStoreTombstoneCount(Integer.valueOf(value));

                            if(prop.contains("type=\"region_count\""))
                                status.setRegions(Integer.valueOf(value));

                            break;
                        case "grpc_server_handling_seconds_bucket" :
                            Matcher bucket = duraction.matcher(prop);

                            if(bucket.find()) {
                                String key = bucket.group(1);
                                if("+Inf".equals(key)) {
                                    txn.setValue(Double.NaN, Double.parseDouble(value));
                                }else {
                                    txn.setValue(Double.parseDouble(key), Double.parseDouble(value));
                                }
                            }
                            break;

                        case "grpc_server_handling_seconds_sum" :
                            Matcher sum = duractionSumAndCount.matcher(prop);


                            if(sum.matches()) {
                                txn.setSum(Double.parseDouble(value));
                            }
                            break;

                        case "grpc_server_handling_seconds_count" :
                            Matcher count = duractionSumAndCount.matcher(prop);

                            if(count.matches()) {
                                txn.setCount(Double.parseDouble(value));
                            }
                            break;

                        case "pd_scheduler_store_status" :
                            if(prop.contains("type=\"leader_score\"")){
                                double currentLeaderScore = Double.parseDouble(value);
                                if(currentLeaderScore > maxLeaderScore || maxLeaderScore == 0) {
                                    maxLeaderScore = currentLeaderScore;
                                }
                                if(currentLeaderScore < minLeaderScore || minLeaderScore == 0){
                                    minLeaderScore = currentLeaderScore;
                                }
                            }

                            if(prop.contains("type=\"region_score\"")){
                                double currentRegionScore = Double.parseDouble(value);
                                if(currentRegionScore > maxRegionScore || maxRegionScore == 0) {
                                    maxRegionScore = currentRegionScore;
                                }
                                if(currentRegionScore < minRegionScore || minRegionScore == 0){
                                    minRegionScore = currentRegionScore;
                                }
                            }
                            break;
                    }
                }
            }

            status.setLeaderBalanceRatio(1 - minLeaderScore / maxLeaderScore);
            status.setRegionBalanceRatio(1 - minRegionScore / maxRegionScore);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlcon != null && isConn) {
                urlcon.disconnect();
            }
        }
        status.setTxnHis(txn);

        return status;
    }

    private static TiDBMetricsStatus getTIDBMetricsByInst(InstanceDtlBean instanceDtlBean)  {
        String host = instanceDtlBean.getAttribute(FixHeader.HEADER_IP).getAttrValue();
        String port = instanceDtlBean.getAttribute(FixHeader.HEADER_STAT_PORT).getAttrValue();
        String urlString = makeUrl(host, port, "metrics");

        TiDBMetricsStatus status = new TiDBMetricsStatus();
        BufferedReader reader = null;

        URL url;
        HttpURLConnection urlcon = null;
        boolean isConn = false;

        try {
            url = new URL(urlString);

            urlcon = (HttpURLConnection) url.openConnection();
            urlcon.setRequestMethod("POST");
            urlcon.setDoOutput(false);
            urlcon.setReadTimeout(50000);
            urlcon.setConnectTimeout(50000);
            urlcon.setRequestProperty("Accept", "text/plain;version=0.0.4;q=1,*/*;q=0.1");
            urlcon.setRequestProperty("User-Agent", "Prometheus/2.2.1");
            urlcon.setRequestProperty("content-type", "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");

            reader = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
            isConn = true;
            String line;

            Histogram queryDurationHis = new Histogram();
            Histogram regionHis = new Histogram();
            Histogram storeHis = new Histogram();
            Histogram tsoHis = new Histogram();

            double statementCount = 0D;

            while ((line = reader.readLine()) != null) {

                String pattern = "(\\w+)\\{(\\S*)\\} (\\S*)";
                String pattern1 = "^(\\w+) (\\S*)";
                Pattern r = Pattern.compile(pattern);
                Pattern r1 = Pattern.compile(pattern1);

                Matcher m = r.matcher(line);

                String name = null;
                String prop = null;
                String value = null;

                if (m.find()) {
                    name = m.group(1);
                    prop = m.group(2);
                    value = m.group(3);
                } else {
                    Matcher m1 = r1.matcher(line);
                    if (m1.find()) {
                        name = m1.group(1);
                        value = m1.group(2);
                    }
                }

                String duration = "type=\"(\\S*)\",le=\"(\\S*)\"";
                Pattern durationPattern = Pattern.compile(duration);


                if (name != null) {
                    switch (name) {
                        //qps
                        case "tidb_server_query_total":
                            status.setTidbServerQueryTotal(Double.parseDouble(value));
                            break;
                        case "tidb_server_connections":
                            status.setConnectionCount(Double.parseDouble(value));
                            break;
                        case "tidb_executor_statement_total":
                            statementCount += Double.parseDouble(value);
                            break;
                        case "tidb_server_handle_query_duration_seconds_bucket":
                            String query = "le=\"(\\S*)\"";
                            Pattern queryPattern = Pattern.compile(query);
                            Matcher queryMatcher = queryPattern.matcher(prop);

                            if (queryMatcher.find()) {
                                String le = queryMatcher.group(1);
                                if ("+Inf".equals(le)) {
                                    queryDurationHis.setValue(Double.NaN, Double.parseDouble(value));
                                } else {
                                    queryDurationHis.setValue(Double.parseDouble(le), Double.parseDouble(value));
                                }
                            }
                            break;

                        case "tidb_server_handle_query_duration_seconds_sum":
                            queryDurationHis.setSum(Double.parseDouble(value));
                            break;

                        case "tidb_server_handle_query_duration_seconds_count":
                            queryDurationHis.setCount(Double.parseDouble(value));
                            break;

                        case "pd_client_request_handle_requests_duration_seconds_bucket":

                            Matcher durationMatcher = durationPattern.matcher(prop);
                            if (durationMatcher.find()) {
                                String type = durationMatcher.group(1);
                                String le = durationMatcher.group(2);
                                Double key = null;
                                if ("+Inf".equals(le)) {
                                    key = Double.NaN;
                                } else {
                                    key = Double.parseDouble(le);
                                }

                                switch (type) {
                                    case "get_region":
                                        regionHis.setValue(key, Double.parseDouble(value));
                                        break;

                                    case "get_store":
                                        storeHis.setValue(key, Double.parseDouble(value));
                                        break;

                                    case "tso":
                                        tsoHis.setValue(key, Double.parseDouble(value));
                                        break;
                                }

                            }
                            break;


                        case "pd_client_request_handle_requests_duration_seconds_sum":
                            if (prop.contains("get_region")) {
                                regionHis.setSum(Double.parseDouble(value));
                            } else if (prop.contains("get_store")) {
                                storeHis.setSum(Double.parseDouble(value));
                            } else if (prop.contains("tso")) {
                                tsoHis.setSum(Double.parseDouble(value));
                            }
                            break;

                        case "pd_client_request_handle_requests_duration_seconds_count":
                            if (prop.contains("get_region")) {
                                regionHis.setCount(Double.parseDouble(value));
                            } else if (prop.contains("get_store")) {
                                storeHis.setCount(Double.parseDouble(value));
                            } else if (prop.contains("tso")) {
                                tsoHis.setCount(Double.parseDouble(value));
                            }
                            break;
                        }
                }


            }

            status.setStatementCount(statementCount);
            status.setQueryDuration(queryDurationHis);
            status.setHandleRegionRequestDuraction(regionHis);
            status.setHandleStoreRequestDuraction(storeHis);
            status.setHandleTsoRequestDuraction(tsoHis);
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }finally{
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlcon != null && isConn) {
                urlcon.disconnect();
            }
        }
        return status;
    }

    private static void pdCalc(String id, PDClusterStatus pdClusterStatus) {
        PDClusterStatus prevClusterStatus = MonitorData.get().getPdClusterStatusMap().get(id);
        if(prevClusterStatus == null) {
            MonitorData.get().getPdClusterStatusMap().put(id, pdClusterStatus);
            return;
        }

        Histogram h1 = prevClusterStatus.getTxnHis();
        Histogram h2 = pdClusterStatus.getTxnHis();

        pdClusterStatus.setCompletedCmdsDurationSecondsAvg99(Histogram.calc(h1, h2, 0.99));
        pdClusterStatus.setCompletedCmdsDurationSecondsAvg(Histogram.calc(h1, h2, 1));
        MonitorData.get().getPdClusterStatusMap().put(id, pdClusterStatus);
    }

    private static void tidbCalc(String instId, TiDBMetricsStatus status) {
        TiDBMetricsStatus prevTiDBStatus = MonitorData.get().getTiDBMetricsStatusMap().get(instId);
        if(prevTiDBStatus == null) {
            MonitorData.get().getTiDBMetricsStatusMap().put(instId, status);
            return;
        }

        Histogram h1QueryDuration = prevTiDBStatus.getQueryDuration();
        Histogram h2QueryDuration = status.getQueryDuration();
        status.setQueryDurationSeconeds(Histogram.calc(h1QueryDuration, h2QueryDuration, 0.99));

        Histogram h1Region = prevTiDBStatus.getHandleRegionRequestDuraction();
        Histogram h2Region = status.getHandleRegionRequestDuraction();
        status.setHandleRegionRequestDuractionSeconeds(Histogram.calc(h1Region, h2Region, 1));

        Histogram h1Store = prevTiDBStatus.getHandleStoreRequestDuraction();
        Histogram h2Store = status.getHandleStoreRequestDuraction();
        status.setHandleStoreRequestDuractionSeconeds(Histogram.calc(h1Store, h2Store, 1));

        Histogram h1Tso = prevTiDBStatus.getHandleTsoRequestDuraction();
        Histogram h2Tso = status.getHandleTsoRequestDuraction();
        status.setHandleTsoRequestDuractionSeconeds(Histogram.calc(h1Tso, h2Tso, 1));

        status.setStatements(status.getStatementCount() - prevTiDBStatus.getStatementCount());

        MonitorData.get().getTiDBMetricsStatusMap().put(instId, status);
    }

    private static String makeUrl(String host, String mgrPort, String api) {
        StringBuilder https = new StringBuilder();

        https.append(CONSTS.HTTP_STR);
        https.append(host);
        https.append(CONSTS.COLON);
        https.append(mgrPort);
        https.append(CONSTS.PATH_SPLIT);
        https.append(api);
        return https.toString();
    }
}
