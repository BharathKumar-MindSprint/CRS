package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ChildVisit")
public class ChildVisit {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;
    @ColumnInfo(name = "farmerId")
    public int farmerId;
    @ColumnInfo(name = "surveyCode")
    public int surveyCode;
    @ColumnInfo(name = "surveyDescription")
    public String surveyDescription;
    @ColumnInfo(name = "synced")
    public boolean synced;

}
