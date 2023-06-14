package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class DmRangeTextBoxActionPlans {
    @SerializedName("actionPlan")
    private String actionPlan;

    @SerializedName("isDefault")
    private boolean isDefault;

    @SerializedName("id")
    private int id;
    public DmRangeTextBoxActionPlans(String actionPlan, boolean isDefault, int id) {
        this.actionPlan = actionPlan;
        this.isDefault = isDefault;
        this.id = id;
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


}
