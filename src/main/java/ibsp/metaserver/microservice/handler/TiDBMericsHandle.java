package ibsp.metaserver.microservice.handler;

import ibsp.metaserver.annotation.App;
import ibsp.metaserver.annotation.Service;

import ibsp.metaserver.bean.Histogram;
import ibsp.metaserver.bean.TiKVMetricsStatus;
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
    private static Logger logger = LoggerFactory.getLogger(TiDBMericsHandle.class);

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
            String[] jobName = system.split("_");
            String instId = jobName[0];

            if(instId == null || jobName.length != 2)
                return;

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

            TiKVMetricsStatus tiKVMetricsStatus = tikvAnalysis(instId, metricFamilyList);
            calc(instId, tiKVMetricsStatus);
        }

        private TiKVMetricsStatus tikvAnalysis(String instId, List<Metrics.MetricFamily> metricFamilyList) {
            TiKVMetricsStatus status = new TiKVMetricsStatus();

            double otherPengdingWorkCount = 0D;
            Histogram snapshotHis = new Histogram();
            Histogram writeHis = new Histogram();
            Histogram schedulerHis = new Histogram();

            for (Metrics.MetricFamily metricFamily : metricFamilyList) {
                String name = metricFamily.getName();
                switch (name) {
                    case "tikv_pd_heartbeat_tick_total" :
                        for(Metrics.Metric metric : metricFamily.getMetricList()) {
                            String typeValue = metric.getLabel(0).getValue();
                            if("leader".equals(typeValue)) {
                                status.setLeaderCount(metric.getGauge().getValue());
                            }
                            if("region".equals(typeValue)) {
                                status.setRegionCount(metric.getGauge().getValue());
                            }
                        }
                        break;
                    case "tikv_channel_full_total" :
                        //没出错误的时候没有样例数据
                        break;

                    case "tikv_engine_write_stall" :
                        for(Metrics.Metric metric : metricFamily.getMetricList()) {
                            for(Metrics.LabelPair labelPair : metric.getLabelList()) {
                                if("write_stall_average".equals(labelPair.getValue())) {
                                    status.setStall(metric.getGauge().getValue());
                                    break;
                                }
                            }
                        }
                        break;

                    case "tikv_worker_pending_task_total" :
                        for(Metrics.Metric metric : metricFamily.getMetricList()) {
                            if(!"pd-worker".equals(metric.getLabel(0).getValue())) {
                                otherPengdingWorkCount += metric.getGauge().getValue();
                            }else {
                                status.setLeaderCount(metric.getGauge().getValue());
                            }
                        }
                        break;

                    case "tikv_coprocessor_request_duration_seconds" :
                        //无样例数据 95% & 99% coprocessor request duration : 95% & 99%  coprocessor 执行时间

                    case "tikv_raftstore_raft_sent_message_total" :
                        //无样例数据  Vote
                        for(Metrics.Metric metric : metricFamily.getMetricList()) {
                            if("vote".equals(metric.getLabel(0).getValue())) {
                                status.setVote(metric.getCounter().getValue());
                                break;
                            }
                        }

                    case "tikv_server_report_failure_msg_total" :
                        //无样例数据  发送失败或者收到了错误的 message

                    case "tikv_storage_engine_async_request_duration_seconds" :
                        for(Metrics.Metric metric : metricFamily.getMetricList()) {
                            Metrics.Histogram histogram = metric.getHistogram();

                            if("snapshot".equals(metric.getLabel(0).getValue())) {
                                snapshotHis.setCount(histogram.getSampleCount());
                                snapshotHis.setSum(histogram.getSampleSum());
                                for(Metrics.Bucket bucket : histogram.getBucketList()){
                                    snapshotHis.setValue(bucket.getUpperBound(), (double) bucket.getCumulativeCount());
                                }
                            }else if("write".equals(metric.getLabel(0).getValue())) {
                                writeHis.setCount(histogram.getSampleCount());
                                writeHis.setSum(histogram.getSampleSum());
                                for(Metrics.Bucket bucket : histogram.getBucketList()){
                                    writeHis.setValue(bucket.getUpperBound(), (double) bucket.getCumulativeCount());
                                }
                            }
                        }
                        break;

                    case "tikv_scheduler_command_duration_seconds" :
                        Metrics.Metric metric = metricFamily.getMetric(0);
                        Metrics.Histogram histogram = metric.getHistogram();
                        schedulerHis.setCount(histogram.getSampleCount());
                        schedulerHis.setSum(histogram.getSampleSum());
                        for(Metrics.Bucket bucket : histogram.getBucketList()){
                            schedulerHis.setValue(bucket.getUpperBound(), (double) bucket.getCumulativeCount());
                        }
                        break;
                }
            }

            status.setOtherPendingTask(otherPengdingWorkCount);
            status.setStorageAsyncRequestDurationSnapshotHis(snapshotHis);
            status.setStorageAsyncRequestDurationWriteHis(writeHis);
            status.setTikvSchedulerContextTotalHis(schedulerHis);

            return status;
        }

        private void calc(String instId, TiKVMetricsStatus tiKVMetricsStatus) {
            TiKVMetricsStatus prevTikvMetricsStatus = MonitorData.get().getTiKVMetricsStatusMap().get(instId);
            if(prevTikvMetricsStatus == null) {
                MonitorData.get().getTiKVMetricsStatusMap().put(instId, tiKVMetricsStatus);
                return;
            }

            Histogram h1ScheduleHis = prevTikvMetricsStatus.getTikvSchedulerContextTotalHis();
            Histogram h2ScheduleHis = tiKVMetricsStatus.getTikvSchedulerContextTotalHis();
            tiKVMetricsStatus.setTikvSchedulerContextTotal(
                    Histogram.calc(h1ScheduleHis, h2ScheduleHis, 0.99));

            Histogram h1SnapshotHis = prevTikvMetricsStatus.getStorageAsyncRequestDurationSnapshotHis();
            Histogram h2SnapshotHis = tiKVMetricsStatus.getStorageAsyncRequestDurationSnapshotHis();
            tiKVMetricsStatus.setStorageAsyncRequestSnapshotDuration(
                    Histogram.calc(h1SnapshotHis, h2SnapshotHis, 1));

            Histogram h1WriteHis = prevTikvMetricsStatus.getStorageAsyncRequestDurationWriteHis();
            Histogram h2WriteHis = tiKVMetricsStatus.getStorageAsyncRequestDurationWriteHis();
            tiKVMetricsStatus.setStorageAsyncRequestWriteDuration(
                    Histogram.calc(h1WriteHis, h2WriteHis, 1));

            tiKVMetricsStatus.setVoteRate(tiKVMetricsStatus.getVote() - prevTikvMetricsStatus.getVote());
            MonitorData.get().getTiKVMetricsStatusMap().put(instId, tiKVMetricsStatus);
        }
    }
}
