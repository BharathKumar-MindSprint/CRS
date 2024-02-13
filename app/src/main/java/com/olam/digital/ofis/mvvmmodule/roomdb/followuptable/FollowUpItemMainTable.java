package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FollowUpItemMainTable")
public class FollowUpItemMainTable {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "questionId")
    public int questionId;

    @ColumnInfo(name = "updatedBy")
    public String updatedBy;

    @ColumnInfo(name = "isPortal")
    public boolean isPortal;

    @ColumnInfo(name = "attempt")
    public int attempt;

    @ColumnInfo(name = "createdAt")
    public String createdAt;

    @ColumnInfo(name = "complianceId")
    public int complianceId;

    @ColumnInfo(name = "createdBy")
    public String createdBy;


    @ColumnInfo(name = "actionPlanId", defaultValue = "0", index = true)
    public String actionPlanId;


    @ColumnInfo(name = "actionPlanText", defaultValue = "0", index = true)
    public String actionPlanText;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "moduleId")
    public int moduleId;

    @ColumnInfo(name = "dmSubModId")
    public int dmSubModId;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "updatedAt")
    public String updatedAt;

    @ColumnInfo(name = "farmerId")
    public long farmerId;

    @ColumnInfo(name = "dmCapturedProof")
    public int dmCapturedProof;

    @ColumnInfo(name = "isSync")
    public Boolean isSync;

}
