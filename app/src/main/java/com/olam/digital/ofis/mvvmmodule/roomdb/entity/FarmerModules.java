package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FarmerModules")
public class FarmerModules {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="moduleId")
    public int moduleId;
    @ColumnInfo(name="farmerId")
    public int farmerId;
    @ColumnInfo(name="olamFarmerId")
    public int olamFarmerId;
    @ColumnInfo(name="farmId")
    public int farmId;
    @ColumnInfo(name="submoduleId")
    public int submoduleId;
    @ColumnInfo(name="statusStarted")
    public int statusStarted;
    @ColumnInfo(name="statusFinished")
    public int statusFinished;
    @ColumnInfo(name="recordedLat")
    public int recordedLat;
    @ColumnInfo(name="recordedLong")
    public int recordedLong;
    @ColumnInfo(name="plot")
    public int plot;
    @ColumnInfo(name="startedDate")
    public int startedDate;
    @ColumnInfo(name="stakeholderId")
    public int stakeholderId;
}
