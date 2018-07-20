package ibsp.metaserver.bean;

import io.vertx.core.json.Json;

public class MQNodeInfoBean {
    private String instId;
    private long diskFree;
    private long diskFreeLimit;
    private long memUse;
    private long memLimit;

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public long getDiskFree() {
        return diskFree;
    }

    public void setDiskFree(long diskFree) {
        this.diskFree = diskFree;
    }

    public long getDiskFreeLimit() {
        return diskFreeLimit;
    }

    public void setDiskFreeLimit(long diskFreeLimit) {
        this.diskFreeLimit = diskFreeLimit;
    }

    public long getMemUse() {
        return memUse;
    }

    public void setMemUse(long memUse) {
        this.memUse = memUse;
    }

    public long getMemLimit() {
        return memLimit;
    }

    public void setMemLimit(long memLimit) {
        this.memLimit = memLimit;
    }

}
