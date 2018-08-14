package ibsp.metaserver.bean;

public class PDClusterStatus {
    private double capacity;
    private double currentSize;
    private int regions;
    private int storeDownCount;
    private int storeUpCount;
    private int storeOfflineCount;
    private int storeTombstoneCount;
    private double completedCmdsDurationSecondsAvg99;
    private double completedCmdsDurationSecondsAvg;
    private double leaderBalanceRatio;
    private double regionBalanceRatio;
    private Histogram txnHis;

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(double currentSize) {
        this.currentSize = currentSize;
    }

    public int getRegions() {
        return regions;
    }

    public void setRegions(int regions) {
        this.regions = regions;
    }

    public int getStoreDownCount() {
        return storeDownCount;
    }

    public void setStoreDownCount(int storeDownCount) {
        this.storeDownCount = storeDownCount;
    }

    public int getStoreUpCount() {
        return storeUpCount;
    }

    public void setStoreUpCount(int storeUpCount) {
        this.storeUpCount = storeUpCount;
    }

    public int getStoreOfflineCount() {
        return storeOfflineCount;
    }

    public void setStoreOfflineCount(int storeOfflineCount) {
        this.storeOfflineCount = storeOfflineCount;
    }

    public int getStoreTombstoneCount() {
        return storeTombstoneCount;
    }

    public void setStoreTombstoneCount(int storeTombstoneCount) {
        this.storeTombstoneCount = storeTombstoneCount;
    }

    public double getCompletedCmdsDurationSecondsAvg99() {
        return completedCmdsDurationSecondsAvg99;
    }

    public void setCompletedCmdsDurationSecondsAvg99(double completedCmdsDurationSecondsAvg99) {
        this.completedCmdsDurationSecondsAvg99 = completedCmdsDurationSecondsAvg99;
    }

    public double getCompletedCmdsDurationSecondsAvg() {
        return completedCmdsDurationSecondsAvg;
    }

    public void setCompletedCmdsDurationSecondsAvg(double completedCmdsDurationSecondsAvg) {
        this.completedCmdsDurationSecondsAvg = completedCmdsDurationSecondsAvg;
    }

    public double getLeaderBalanceRatio() {
        return leaderBalanceRatio;
    }

    public void setLeaderBalanceRatio(double leaderBalanceRatio) {
        this.leaderBalanceRatio = leaderBalanceRatio;
    }

    public double getRegionBalanceRatio() {
        return regionBalanceRatio;
    }

    public void setRegionBalanceRatio(double regionBalanceRatio) {
        this.regionBalanceRatio = regionBalanceRatio;
    }

    public Histogram getTxnHis() {
        return txnHis;
    }

    public void setTxnHis(Histogram txnHis) {
        this.txnHis = txnHis;
    }
}
