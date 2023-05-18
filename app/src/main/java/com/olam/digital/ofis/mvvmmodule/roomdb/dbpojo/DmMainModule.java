package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmMainModule")
public class DmMainModule {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "surveyType")
    public int surveyType;

    @ColumnInfo(name = "name")
    public String name;

}
