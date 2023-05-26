package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SectionLookupValuesItemSubmodule {

    @SerializedName("valueTr")
    private String valueTr;

    @SerializedName("valueId")
    private int valueId;

    @SerializedName("valueVi")
    private String valueVi;

    @SerializedName("proofFormat")
    private String proofFormat;

    @SerializedName("valueTh")
    private String valueTh;

    @SerializedName("isCaptureProof")
    private boolean isCaptureProof;

    @SerializedName("valueEs")
    private String valueEs;

    @SerializedName("valueFr")
    private String valueFr;

    @SerializedName("complianceId")
    private int complianceId;

    @SerializedName("compliance")
    private String compliance;

    @SerializedName("allowCustomActionPlan")
    private boolean allowCustomActionPlan;

    @SerializedName("valueLo")
    private String valueLo;


    @SerializedName("actionPlans")
    private List<SectionActionPlansItemSubmodule> actionPlans;

    @SerializedName("id")
    private int id;

    @SerializedName("position")
    private int position;

    @SerializedName("valuePt")
    private String valuePt;

    @SerializedName("value")
    private String value;

    public SectionLookupValuesItemSubmodule(String valueTr, int valueId, String valueVi, String proofFormat, String valueTh, boolean isCaptureProof, String valueEs, String valueFr, int complianceId, String compliance, boolean allowCustomActionPlan, String valueLo, List<SectionActionPlansItemSubmodule> actionPlans, int id, int position, String valuePt, String value) {
        this.valueTr = valueTr;
        this.valueId = valueId;
        this.valueVi = valueVi;
        this.proofFormat = proofFormat;
        this.valueTh = valueTh;
        this.isCaptureProof = isCaptureProof;
        this.valueEs = valueEs;
        this.valueFr = valueFr;
        this.complianceId = complianceId;
        this.compliance = compliance;
        this.allowCustomActionPlan = allowCustomActionPlan;
        this.valueLo = valueLo;
        this.actionPlans = actionPlans;
        this.id = id;
        this.position = position;
        this.valuePt = valuePt;
        this.value = value;
    }

    public String getValueTr() {
        return valueTr;
    }

    public void setValueTr(String valueTr) {
        this.valueTr = valueTr;
    }

    public int getValueId() {
        return valueId;
    }

    public void setValueId(int valueId) {
        this.valueId = valueId;
    }

    public String getValueVi() {
        return valueVi;
    }

    public void setValueVi(String valueVi) {
        this.valueVi = valueVi;
    }

    public String getProofFormat() {
        return proofFormat;
    }

    public void setProofFormat(String proofFormat) {
        this.proofFormat = proofFormat;
    }

    public String getValueTh() {
        return valueTh;
    }

    public void setValueTh(String valueTh) {
        this.valueTh = valueTh;
    }

    public boolean isCaptureProof() {
        return isCaptureProof;
    }

    public void setCaptureProof(boolean captureProof) {
        isCaptureProof = captureProof;
    }

    public String getValueEs() {
        return valueEs;
    }

    public void setValueEs(String valueEs) {
        this.valueEs = valueEs;
    }

    public String getValueFr() {
        return valueFr;
    }

    public void setValueFr(String valueFr) {
        this.valueFr = valueFr;
    }

    public int getComplianceId() {
        return complianceId;
    }

    public void setComplianceId(int complianceId) {
        this.complianceId = complianceId;
    }

    public String getCompliance() {
        return compliance;
    }

    public void setCompliance(String compliance) {
        this.compliance = compliance;
    }

    public boolean isAllowCustomActionPlan() {
        return allowCustomActionPlan;
    }

    public void setAllowCustomActionPlan(boolean allowCustomActionPlan) {
        this.allowCustomActionPlan = allowCustomActionPlan;
    }

    public String getValueLo() {
        return valueLo;
    }

    public void setValueLo(String valueLo) {
        this.valueLo = valueLo;
    }

    public List<SectionActionPlansItemSubmodule> getActionPlans() {
        return actionPlans;
    }

    public void setActionPlans(List<SectionActionPlansItemSubmodule> actionPlans) {
        this.actionPlans = actionPlans;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getValuePt() {
        return valuePt;
    }

    public void setValuePt(String valuePt) {
        this.valuePt = valuePt;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
