package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmRangeTextBoxModuleQuestionsTable")
public class DmRangeTextBoxModuleQuestionsTable {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "id")
    public Integer id;

    @ColumnInfo(name = "minimumRange")
    public double minimumRange;

    @ColumnInfo(name = "maximumRange")
    public double maximumRange;

    @ColumnInfo(name = "compliance")
    public String compliance;

    @ColumnInfo(name = "complianceId")
    public int complianceId;

    @ColumnInfo(name = "allowCustomActionPlan")
    public boolean allowCustomActionPlan;

    @ColumnInfo(name = "isCaptureProof")
    public boolean isCaptureProof;

    @ColumnInfo(name = "proofFormat")
    public String proofFormat;


    @ColumnInfo(name = "questionId")
    public int questionId;
}
