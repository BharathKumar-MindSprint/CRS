package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;


public class AssessmentResultTypeItems {

    public AssessmentResultTypeItems(int minRange, int maxRange, String remarks) {
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.remarks = remarks;
    }

    @SerializedName("minRange")
    private int minRange;

    @SerializedName("maxRange")
    private int maxRange;

    @SerializedName("remarks")
    private String remarks;

    public int getMinRange() {
        return minRange;
    }

    public void setMinRange(int minRange) {
        this.minRange = minRange;
    }

    public int getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
