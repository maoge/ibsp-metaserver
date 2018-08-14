package ibsp.metaserver.bean;

public class TiKVMetricsStatus {
    private double tikvSchedulerContextTotal;
    private Histogram tikvSchedulerContextTotalHis;
    private Histogram storageAsyncRequestDurationSnapshotHis;
    private Histogram storageAsyncRequestDurationWriteHis;
    private double storageAsyncRequestSnapshotDuration;
    private double storageAsyncRequestWriteDuration;
    private double serverReportFailureMessage;
    private double vote;
    private double voteRate;
    private double coprocessorRequestDuration;
    private double otherPendingTask;
    private double pdWorkerPendingTask;
    private double stall;

    private double channelFull;
    private double leaderCount;
    private double regionCount;

    public double getTikvSchedulerContextTotal() {
        return tikvSchedulerContextTotal;
    }

    public void setTikvSchedulerContextTotal(double tikvSchedulerContextTotal) {
        this.tikvSchedulerContextTotal = tikvSchedulerContextTotal;
    }

    public double getStorageAsyncRequestSnapshotDuration() {
        return storageAsyncRequestSnapshotDuration;
    }

    public void setStorageAsyncRequestSnapshotDuration(double storageAsyncRequestSnapshotDuration) {
        this.storageAsyncRequestSnapshotDuration = storageAsyncRequestSnapshotDuration;
    }

    public double getServerReportFailureMessage() {
        return serverReportFailureMessage;
    }

    public double getVoteRate() {
        return voteRate;
    }

    public void setVoteRate(double voteRate) {
        this.voteRate = voteRate;
    }

    public void setServerReportFailureMessage(double serverReportFailureMessage) {
        this.serverReportFailureMessage = serverReportFailureMessage;
    }

    public double getVote() {
        return vote;
    }

    public void setVote(double vote) {
        this.vote = vote;
    }

    public double getCoprocessorRequestDuration() {
        return coprocessorRequestDuration;
    }

    public void setCoprocessorRequestDuration(double coprocessorRequestDuration) {
        this.coprocessorRequestDuration = coprocessorRequestDuration;
    }

    public double getOtherPendingTask() {
        return otherPendingTask;
    }

    public void setOtherPendingTask(double otherPendingTask) {
        this.otherPendingTask = otherPendingTask;
    }

    public double getPdWorkerPendingTask() {
        return pdWorkerPendingTask;
    }

    public void setPdWorkerPendingTask(double pdWorkerPendingTask) {
        this.pdWorkerPendingTask = pdWorkerPendingTask;
    }

    public double getStall() {
        return stall;
    }

    public void setStall(double stall) {
        this.stall = stall;
    }

    public double getChannelFull() {
        return channelFull;
    }

    public void setChannelFull(double channelFull) {
        this.channelFull = channelFull;
    }

    public double getLeaderCount() {
        return leaderCount;
    }

    public void setLeaderCount(double leaderCount) {
        this.leaderCount = leaderCount;
    }

    public double getRegionCount() {
        return regionCount;
    }

    public void setRegionCount(double regionCount) {
        this.regionCount = regionCount;
    }

    public Histogram getStorageAsyncRequestDurationSnapshotHis() {
        return storageAsyncRequestDurationSnapshotHis;
    }

    public void setStorageAsyncRequestDurationSnapshotHis(Histogram storageAsyncRequestDurationSnapshotHis) {
        this.storageAsyncRequestDurationSnapshotHis = storageAsyncRequestDurationSnapshotHis;
    }

    public Histogram getStorageAsyncRequestDurationWriteHis() {
        return storageAsyncRequestDurationWriteHis;
    }

    public void setStorageAsyncRequestDurationWriteHis(Histogram storageAsyncRequestDurationWriteHis) {
        this.storageAsyncRequestDurationWriteHis = storageAsyncRequestDurationWriteHis;
    }

    public double getStorageAsyncRequestWriteDuration() {
        return storageAsyncRequestWriteDuration;
    }

    public void setStorageAsyncRequestWriteDuration(double storageAsyncRequestWriteDuration) {
        this.storageAsyncRequestWriteDuration = storageAsyncRequestWriteDuration;
    }

    public Histogram getTikvSchedulerContextTotalHis() {
        return tikvSchedulerContextTotalHis;
    }

    public void setTikvSchedulerContextTotalHis(Histogram tikvSchedulerContextTotalHis) {
        this.tikvSchedulerContextTotalHis = tikvSchedulerContextTotalHis;
    }
}
