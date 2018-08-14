package ibsp.metaserver.microservice.handler;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;

import ibsp.metaserver.global.MetaData;
import ibsp.metaserver.global.MonitorData;
import ibsp.metaserver.threadpool.WorkerPool;
import io.prometheus.client.Metrics;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@App(path = "/metrics/job")
public class TiDBMericsHandle {
    /*private static Logger logger = LoggerFactory.getLogger(TiDBMericsHandle.class);

    @Service(id = ":system/instance/:server", name = "tidb/instance/:server", auth = false, bwswitch = false)
    public static void getMetrics(RoutingContext routeContext) {
        Buffer buffer = routeContext.getBody();

        byte[] bytes = buffer.getBytes();
        String system = routeContext.request().getParam("system");
        String server = routeContext.request().getParam("server");

        WorkerPool.get().execute(new MetricsRunner(system, server, bytes));

        routeContext.response().setStatusCode(202).end();
    }


    private static class MetricsRunner implements Runnable{
        private String system;
        private String server;
        private byte[] bytes;

        MetricsRunner(String system, String server, byte[] bytes) {
            this.system = system;
            this.server = server;
            this.bytes  = bytes;
        }

        @Override
        public void run() {
            ByteArrayInputStream bin = new ByteArrayInputStream(bytes);


            Metrics.MetricFamily metricFamily = null;
            StringBuffer sb = new StringBuffer();

            List<Metrics.MetricFamily> metricFamilyList = new ArrayList<>();

            do{
                try {
                    metricFamily = Metrics.MetricFamily.parseDelimitedFrom(bin);
                    if(metricFamily != null){
                        metricFamilyList.add(metricFamily);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }while(metricFamily != null);

            if(metricFamilyList.size() == 0)
                return;
            System.out.println(system + "  " + server);
            if("tidb".equals(system)) {
                String[] hostAndPort = server.split("_");
                String ip = MetaData.get().getIpByHostName(hostAndPort[0]);
                String port = hostAndPort[1];
                tidbAnalysis(metricFamilyList);
            }else {
                String[] instanceId = server.split("_");
                //pd
                if(instanceId.length == 1) {
                    pdAnalysis(metricFamilyList);
                }
                //tikv
                if(instanceId.length == 2) {
                    tikvAnalysis(metricFamilyList);
                }
            }

        }

        private void tidbAnalysis(List<Metrics.MetricFamily> metricFamilyList) {

            TiDBMetricsBean currenttiDBMetricsBean = new TiDBMetricsBean();

            for(Metrics.MetricFamily metricFamily : metricFamilyList) {
                String metricName = metricFamily.getName();
                List<Metrics.Metric> metrics = metricFamily.getMetricList();

                switch (metricName) {
                    //QPS 统计 (是总数)
                    case "tidb_server_query_total" :
                        currenttiDBMetricsBean.setQps(foreachCounter(metrics));
                        break;

                    //连接数 （当前值）
                    case "tidb_server_connections" :
                        currenttiDBMetricsBean.setConnectionCount(foreachGauge(metrics));
                        break;

                    //statement（当前值）
                    case "tidb_executor_statement_total" :
                        currenttiDBMetricsBean.setStatementCount(foreachCounter(metrics));
                        break;

                    //PD获取TSO响应时间 (直方桶图)
                    case "pd_client_request_handle_requests_duration_seconds" :
                        currenttiDBMetricsBean.setTso(foreachHistogramByTypeValue(metrics, "tso"));
                        break;

                    //99% 的query时间
                    case "tidb_server_handle_query_duration_seconds" :
                        currenttiDBMetricsBean.setQueryDuration99thPercentile(foreachHistogramByBucketLimit(metrics, 0.99D));
                        break;
                }
            }
            currenttiDBMetricsBean.setTime(System.currentTimeMillis());
            {
                //TODO 保存数据库
                TiDBMetricsBean prevMetrics = MonitorData.get().getTiDBMetricsBean();
                if(prevMetrics != null) {
                    long inteval = (currenttiDBMetricsBean.getTime() - prevMetrics.getTime() ) / 1000;

                    System.out.println("qps:" + (currenttiDBMetricsBean.getQps() - prevMetrics.getQps())/inteval);
                    System.out.println("conns:" + (currenttiDBMetricsBean.getConnectionCount()));
                    System.out.println("statement:" + (currenttiDBMetricsBean.getStatementCount() -
                            prevMetrics.getStatementCount())/inteval);
                    //ops是每分钟最大的开关(或动作)次数 、60代表一分钟，这边统计的是间隔时间总和/60
                    System.out.println("tso:" + (currenttiDBMetricsBean.getTso() - prevMetrics.getTso())/inteval/60);
                    //同上
                    System.out.println("99%的query时间:" + (currenttiDBMetricsBean.getQueryDuration99thPercentile() -
                            prevMetrics.getQueryDuration99thPercentile())/inteval/60);
                }
            }
            MonitorData.get().setTiDBMetricsBean(currenttiDBMetricsBean);
        }

        private void pdAnalysis(List<Metrics.MetricFamily> metricFamilyList) {

            for(Metrics.MetricFamily metricFamily : metricFamilyList) {
                String metricName = metricFamily.getName();
                List<Metrics.Metric> metrics = metricFamily.getMetricList();

                switch (metricName) {
                    //QPS 统计 (是总数)
                    case "tidb_server_query_total" :
                        foreachCounter(metrics);
                        break;

                    //连接数 （当前值）
                    case "tidb_server_connections" :
                        foreachGauge(metrics);
                        break;

                    //statement（当前值）
                    case "tidb_executor_statement_total" :
                        foreachCounter(metrics);
                        break;

                    //PD获取TSO响应时间 (直方桶图)
                    case "pd_client_request_handle_requests_duration_seconds" :
                        foreachHistogramByTypeValue(metrics, "tso");
                        break;

                    //99% 的query时间
                    case "tidb_server_handle_query_duration_seconds" :
                        foreachHistogramByBucketLimit(metrics, 0.99D);
                        break;
                }
            }
        }

        private void tikvAnalysis(List<Metrics.MetricFamily> metricFamilyList) {

        }

        private double foreachCounter(List<Metrics.Metric> metrics) {
            double res = 0D;
            for(Metrics.Metric metric : metrics) {
                res += metric.getCounter().getValue();
            }
            return res;
        }

        private double foreachGauge(List<Metrics.Metric> metrics) {
            double res = 0D;
            for(Metrics.Metric metric : metrics) {
                res += metric.getGauge().getValue();
            }
            return res;
        }

        *//*private double foreachHistogram(List<Metrics.Metric> metrics) {
            double res = 0D;
            for(Metrics.Metric metric : metrics) {
                res += metric.getGauge().getValue();
            }
            return res;
        }*//*

        private double foreachHistogramByTypeValue(List<Metrics.Metric> metrics, String typeValue) {
            double res = 0D;
            for(Metrics.Metric metric : metrics) {
                Metrics.LabelPair label = metric.getLabel(0);
                if(typeValue.equals(label.getValue())) {
                    Metrics.Histogram histogram = metric.getHistogram();
                    List<Metrics.Bucket> bucketList = histogram.getBucketList();
                    for(Metrics.Bucket bucket : bucketList) {
                        res += bucket.getCumulativeCount();
                    }
                    break;
                }
                continue;
            }
            return res;
        }

        private double foreachHistogramByBucketLimit(List<Metrics.Metric> metrics, double limit) {
            double res = 0D;
            for(Metrics.Metric metric : metrics) {
                Metrics.Histogram histogram = metric.getHistogram();
                List<Metrics.Bucket> bucketList = histogram.getBucketList();
                for(Metrics.Bucket bucket : bucketList) {
                    if(bucket.getUpperBound() > (1-limit))
                        res += bucket.getCumulativeCount();
                }
            }
            return res;
        }
    }*/
}
