package com.olam.digital.ofis.multifarm;

public class SubmittedInfo {
    boolean submitted;
    long farmerId;
    int moduleId;
    boolean networkTable=false;

    public boolean isNetworkTable() {
        return networkTable;
    }

    public void setNetworkTable(boolean networkTable) {
        this.networkTable = networkTable;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(long farmerId) {
        this.farmerId = farmerId;
    }
}
