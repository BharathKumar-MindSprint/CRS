package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DownloadedSurveys")
public class DownloadedSurveys {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;

    @ColumnInfo(name="moduleId")
    public int moduleId;

    @ColumnInfo(name="moduleName")
    public String moduleName;
    @ColumnInfo(name="farmerGroupId")
    public int farmerGroupId;
    @ColumnInfo(name="sectionId")
    public int sectionId;
    @ColumnInfo(name="savedOn")
    public String savedOn;


}
