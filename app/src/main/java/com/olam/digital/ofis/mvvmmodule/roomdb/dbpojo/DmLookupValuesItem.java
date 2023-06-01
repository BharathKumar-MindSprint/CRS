package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmLookupValuesItem")
public class DmLookupValuesItem {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "valueId")
    public int valueId;

    @ColumnInfo(name = "valueTr")
    public String valueTr;

    @ColumnInfo(name = "valueVi")
    public String valueVi;

    @ColumnInfo(name = "valueTh")
    public String valueTh;

    @ColumnInfo(name = "valueEs")
    public String valueEs;

    @ColumnInfo(name = "valueFr")
    public String valueFr;

    @ColumnInfo(name = "valueLo")
    public String valueLo;

    @ColumnInfo(name = "valuePt")
    public String valuePt;

    @ColumnInfo(name = "value")
    public String value;

    @ColumnInfo(name = "position")
    public int position;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "idQuestionsMudule")
    public int idQuestionsMudule;

    @ColumnInfo(name = "allowCustomActionPlan")
    public boolean allowCustomActionPlan;

    @ColumnInfo(name = "compliance")
    public String compliance;

    @ColumnInfo(name = "complianceId")
    public int complianceId;

    @ColumnInfo(name = "isCaptureProof")
    public boolean isCaptureProof;

    @ColumnInfo(name = "proofFormat")
    public String proofFormat;

}
