package ibsp.metaserver.bean;

public class TiDBMetricsStatus {
    private double tidbServerQueryTotal; //QPS;
    private double connectionCount;
    private double statementCount;

    private Histogram handleRegionRequestDuraction;
    private Histogram handleStoreRequestDuraction;
    private Histogram handleTsoRequestDuraction;
    private Histogram queryDuration;

    private double handleRegionRequestDuractionSeconeds;
    private double handleStoreRequestDuractionSeconeds;
    private double handleTsoRequestDuractionSeconeds;
    private double queryDurationSeconeds;

    private double statements;

    public double getTidbServerQueryTotal() {
        return tidbServerQueryTotal;
    }

    public void setTidbServerQueryTotal(double tidbServerQueryTotal) {
        this.tidbServerQueryTotal = tidbServerQueryTotal;
    }

    public double getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(double connectionCount) {
        this.connectionCount = connectionCount;
    }

    public double getStatementCount() {
        return statementCount;
    }

    public void setStatementCount(double statementCount) {
        this.statementCount = statementCount;
    }

    public Histogram getHandleRegionRequestDuraction() {
        return handleRegionRequestDuraction;
    }

    public void setHandleRegionRequestDuraction(Histogram handleRegionRequestDuraction) {
        this.handleRegionRequestDuraction = handleRegionRequestDuraction;
    }

    public Histogram getHandleStoreRequestDuraction() {
        return handleStoreRequestDuraction;
    }

    public void setHandleStoreRequestDuraction(Histogram handleStoreRequestDuraction) {
        this.handleStoreRequestDuraction = handleStoreRequestDuraction;
    }

    public Histogram getHandleTsoRequestDuraction() {
        return handleTsoRequestDuraction;
    }

    public void setHandleTsoRequestDuraction(Histogram handleTsoRequestDuraction) {
        this.handleTsoRequestDuraction = handleTsoRequestDuraction;
    }

    public Histogram getQueryDuration() {
        return queryDuration;
    }

    public void setQueryDuration(Histogram queryDuration) {
        this.queryDuration = queryDuration;
    }

    public double getHandleRegionRequestDuractionSeconeds() {
        return handleRegionRequestDuractionSeconeds;
    }

    public void setHandleRegionRequestDuractionSeconeds(double handleRegionRequestDuractionSeconeds) {
        this.handleRegionRequestDuractionSeconeds = handleRegionRequestDuractionSeconeds;
    }

    public double getHandleStoreRequestDuractionSeconeds() {
        return handleStoreRequestDuractionSeconeds;
    }

    public void setHandleStoreRequestDuractionSeconeds(double handleStoreRequestDuractionSeconeds) {
        this.handleStoreRequestDuractionSeconeds = handleStoreRequestDuractionSeconeds;
    }

    public double getHandleTsoRequestDuractionSeconeds() {
        return handleTsoRequestDuractionSeconeds;
    }

    public void setHandleTsoRequestDuractionSeconeds(double handleTsoRequestDuractionSeconeds) {
        this.handleTsoRequestDuractionSeconeds = handleTsoRequestDuractionSeconeds;
    }

    public double getQueryDurationSeconeds() {
        return queryDurationSeconeds;
    }

    public void setQueryDurationSeconeds(double queryDurationSeconeds) {
        this.queryDurationSeconeds = queryDurationSeconeds;
    }

    public double getStatements() {
        return statements;
    }

    public void setStatements(double statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "TiDBMetricsStatus{" +
                "tidbServerQueryTotal=" + tidbServerQueryTotal +
                ", connectionCount=" + connectionCount +
                ", statementCount=" + statementCount +
                ", handleRegionRequestDuraction=" + handleRegionRequestDuraction +
                ", handleStoreRequestDuraction=" + handleStoreRequestDuraction +
                ", handleTsoRequestDuraction=" + handleTsoRequestDuraction +
                ", queryDuration=" + queryDuration +
                ", handleRegionRequestDuractionSeconeds=" + handleRegionRequestDuractionSeconeds +
                ", handleStoreRequestDuractionSeconeds=" + handleStoreRequestDuractionSeconeds +
                ", handleTsoRequestDuractionSeconeds=" + handleTsoRequestDuractionSeconeds +
                ", queryDurationSeconeds=" + queryDurationSeconeds +
                ", statements=" + statements +
                '}';
    }
}
