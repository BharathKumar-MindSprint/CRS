package com.olam.digital.ofis.mvvmmodule.roomdb.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ComplianceIndicator")
public class ComplianceIndicator {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "compliance")
    public String compliance;

    @ColumnInfo(name = "label")
    public String label;

    @ColumnInfo(name = "moduleDraftId")
    public int moduleDraftId;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "labelTh")
    public String labelTh;

    @ColumnInfo(name = "labelPt")
    public String labelPt;

    @ColumnInfo(name = "labelIdn")
    public String labelIdn;

    @ColumnInfo(name = "labelEs")
    public String labelEs;

    @ColumnInfo(name = "labelTr")
    public String labelTr;

    @ColumnInfo(name = "labelFr")
    public String labelFr;

    @ColumnInfo(name = "labelLo")
    public String labelLo;

    @ColumnInfo(name = "labelVi")
    public String labelVi;

    @ColumnInfo(name = "score")
    public int score;

    @ColumnInfo(name = "isActionPlanRequired")
    public boolean isActionPlanRequired;

    @ColumnInfo(name = "allowCustomActionPlan")
    public boolean allowCustomActionPlan;



}
