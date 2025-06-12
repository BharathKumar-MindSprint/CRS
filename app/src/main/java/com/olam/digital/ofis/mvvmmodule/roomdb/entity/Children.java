package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Children")
public class Children {
    @PrimaryKey
    @ColumnInfo(name="tempId")
    public int tempId;
    @ColumnInfo(name = "id")
    public int id;
    @ColumnInfo(name = "farmerId")
    public int farmerId;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "age")
    public String age;
    @ColumnInfo(name = "gender")
    public String gender;
    @ColumnInfo(name = "actionFlag")
    public String actionFlag;
    @ColumnInfo(name = "status")
    public String status;
    @ColumnInfo(name = "moduleDTO")
    public String moduleDTO;
    @ColumnInfo(name="createdAt")
    public String createdAt;
    @ColumnInfo(name = "submittedModule")
    public String submittedModule;
    @ColumnInfo(name = "actionTicked")
    public String actionTicked;
    @ColumnInfo(name = "ageOnYear")
    public String ageOnYear;
    @ColumnInfo(name="synced")
    public String synced;

}
