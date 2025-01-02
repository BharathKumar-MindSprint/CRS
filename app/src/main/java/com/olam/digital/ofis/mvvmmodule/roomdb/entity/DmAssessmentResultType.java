package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AssessmentResultType")
public class DmAssessmentResultType {


    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "moduleId")
    public int moduleId;

    @ColumnInfo(name = "minRange")
    public int minRange;

    @ColumnInfo(name = "maxRange")
    public int maxRange;

    @ColumnInfo(name = "remarks")
    public String remarks;


}
