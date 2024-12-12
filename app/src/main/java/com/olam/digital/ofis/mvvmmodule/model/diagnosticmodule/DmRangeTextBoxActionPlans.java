package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import androidx.room.ColumnInfo;

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

    @ColumnInfo(name = "actionPlanFr")
    public String actionPlanFr;

    @ColumnInfo(name = "actionPlanEs")
    public String actionPlanEs;

    @ColumnInfo(name = "actionPlanId")
    public String actionPlanId;

    @ColumnInfo(name = "actionPlanPt")
    public String actionPlanPt;

    @ColumnInfo(name = "actionPlanTr")
    public String actionPlanTr;

    @ColumnInfo(name = "actionPlanLo")
    public String actionPlanLo;

    @ColumnInfo(name = "actionPlanVi")
    public String actionPlanVi;

    @ColumnInfo(name = "actionPlanTh")
    public String actionPlanTh;


    public DmRangeTextBoxActionPlans(String actionPlan, boolean isDefault, int id, Integer actionValueId, String actionPlanFr, String actionPlanEs, String actionPlanId, String actionPlanPt, String actionPlanTr, String actionPlanLo, String actionPlanVi, String actionPlanTh) {
        this.actionPlan = actionPlan;
        this.isDefault = isDefault;
        this.id = id;
        this.actionValueId = actionValueId;
        this.actionPlanFr = actionPlanFr;
        this.actionPlanEs = actionPlanEs;
        this.actionPlanId = actionPlanId;
        this.actionPlanPt = actionPlanPt;
        this.actionPlanTr = actionPlanTr;
        this.actionPlanLo = actionPlanLo;
        this.actionPlanVi = actionPlanVi;
        this.actionPlanTh = actionPlanTh;
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

    public String getActionPlanFr() {
        return actionPlanFr;
    }

    public void setActionPlanFr(String actionPlanFr) {
        this.actionPlanFr = actionPlanFr;
    }

    public String getActionPlanEs() {
        return actionPlanEs;
    }

    public void setActionPlanEs(String actionPlanEs) {
        this.actionPlanEs = actionPlanEs;
    }

    public String getActionPlanId() {
        return actionPlanId;
    }

    public void setActionPlanId(String actionPlanId) {
        this.actionPlanId = actionPlanId;
    }

    public String getActionPlanPt() {
        return actionPlanPt;
    }

    public void setActionPlanPt(String actionPlanPt) {
        this.actionPlanPt = actionPlanPt;
    }

    public String getActionPlanTr() {
        return actionPlanTr;
    }

    public void setActionPlanTr(String actionPlanTr) {
        this.actionPlanTr = actionPlanTr;
    }

    public String getActionPlanLo() {
        return actionPlanLo;
    }

    public void setActionPlanLo(String actionPlanLo) {
        this.actionPlanLo = actionPlanLo;
    }

    public String getActionPlanVi() {
        return actionPlanVi;
    }

    public void setActionPlanVi(String actionPlanVi) {
        this.actionPlanVi = actionPlanVi;
    }

    public String getActionPlanTh() {
        return actionPlanTh;
    }

    public void setActionPlanTh(String actionPlanTh) {
        this.actionPlanTh = actionPlanTh;
    }
}
