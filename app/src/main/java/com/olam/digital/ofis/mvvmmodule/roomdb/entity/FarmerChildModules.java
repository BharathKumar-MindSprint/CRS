package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FarmerChildModule")
public class FarmerChildModules {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="moduleId")
    public int moduleId;
    @ColumnInfo(name="farmerId")
    public int farmerId;
    @ColumnInfo(name="olamFarmerId")
    public int olamFarmerId;
    @ColumnInfo(name="childId")
    public int childId;
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
    @ColumnInfo(name="startedDate")
    public int startedDate;


}
