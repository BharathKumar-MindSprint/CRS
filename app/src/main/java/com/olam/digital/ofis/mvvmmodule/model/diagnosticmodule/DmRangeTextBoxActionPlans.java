package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class DmRangeTextBoxActionPlans {
    @SerializedName("actionPlan")
    private String actionPlan;

    @SerializedName("isDefault")
    private boolean isDefault;

    @SerializedName("id")
    private int id;

    @SerializedName("actionValueId")
    private Integer actionValueId;

    public DmRangeTextBoxActionPlans(String actionPlan, boolean isDefault, int id, Integer actionValueId) {
        this.actionPlan = actionPlan;
        this.isDefault = isDefault;
        this.id = id;
        this.actionValueId = actionValueId;
    }



    public String getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(String actionPlan) {
        this.actionPlan = actionPlan;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getActionValueId() {
        return actionValueId;
    }

    public void setActionValueId(Integer actionValueId) {
        this.actionValueId = actionValueId;
    }
}
