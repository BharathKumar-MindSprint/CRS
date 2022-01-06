package com.olam.digital.ofis.multifarm;

public class SubmittedInfo {
    boolean submitted;
    long farmerId;

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
