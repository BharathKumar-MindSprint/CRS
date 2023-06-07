package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DmRangeTextBoxModuleQuestions {

    public DmRangeTextBoxModuleQuestions(double minimumRange, double maximumRange, String compliance, int complianceId, boolean allowCustomActionPlan, boolean isCaptureProof, String proofFormat, List<DmRangeTextBoxActionPlans> dmRangeTextBoxActionPlansList) {
        this.minimumRange = minimumRange;
        this.maximumRange = maximumRange;
        this.compliance = compliance;
        this.complianceId = complianceId;
        this.allowCustomActionPlan = allowCustomActionPlan;
        this.isCaptureProof = isCaptureProof;
        this.proofFormat = proofFormat;
        this.dmRangeTextBoxActionPlansList = dmRangeTextBoxActionPlansList;
    }

    public double getMinimumRange() {
        return minimumRange;
    }

    public void setMinimumRange(double minimumRange) {
        this.minimumRange = minimumRange;
    }

    public double getMaximumRange() {
        return maximumRange;
    }

    public void setMaximumRange(double maximumRange) {
        this.maximumRange = maximumRange;
    }

    public String getCompliance() {
        return compliance;
    }

    public void setCompliance(String compliance) {
        this.compliance = compliance;
    }

    public int getComplianceId() {
        return complianceId;
    }

    public void setComplianceId(int complianceId) {
        this.complianceId = complianceId;
    }

    public boolean isAllowCustomActionPlan() {
        return allowCustomActionPlan;
    }

    public void setAllowCustomActionPlan(boolean allowCustomActionPlan) {
        this.allowCustomActionPlan = allowCustomActionPlan;
    }

    public boolean isCaptureProof() {
        return isCaptureProof;
    }

    public void setCaptureProof(boolean captureProof) {
        isCaptureProof = captureProof;
    }

    public String getProofFormat() {
        return proofFormat;
    }

    public void setProofFormat(String proofFormat) {
        this.proofFormat = proofFormat;
    }

    public List<DmRangeTextBoxActionPlans> getDmRangeTextBoxActionPlansList() {
        return dmRangeTextBoxActionPlansList;
    }

    public void setDmRangeTextBoxActionPlansList(List<DmRangeTextBoxActionPlans> dmRangeTextBoxActionPlansList) {
        this.dmRangeTextBoxActionPlansList = dmRangeTextBoxActionPlansList;
    }

    @SerializedName("minimumRange")
    private double minimumRange;

    @SerializedName("maximumRange")
    private double maximumRange;

    @SerializedName("compliance")
    private String compliance;

    @SerializedName("complianceId")
    private int complianceId;

    @SerializedName("allowCustomActionPlan")
    private boolean allowCustomActionPlan;

    @SerializedName("isCaptureProof")
    private boolean isCaptureProof;

    @SerializedName("proofFormat")
    private String proofFormat;

    @SerializedName("actionPlans")
    private List<DmRangeTextBoxActionPlans> dmRangeTextBoxActionPlansList;
}
