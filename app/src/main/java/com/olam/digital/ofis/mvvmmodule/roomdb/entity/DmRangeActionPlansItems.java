package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmRangeActionPlansItems")
public class DmRangeActionPlansItems {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "actionValueId")
    public Integer actionValueId;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "questionId")
    public int questionId;

    @ColumnInfo(name = "complainceId")
    public int complainceId;

    @ColumnInfo(name = "isDefault")
    public boolean isDefault;

    @ColumnInfo(name = "actionPlan")
    public String actionPlan;

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

}
