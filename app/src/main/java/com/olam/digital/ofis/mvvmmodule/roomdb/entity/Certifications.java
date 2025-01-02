package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Certifications")
public class Certifications {

    @PrimaryKey
    @ColumnInfo(name = "certificationId")
    public int certificationId;

    @ColumnInfo(name = "certificationName")
    public String certificationName;

    @ColumnInfo(name = "certificationDesc")
    public String certificationDesc;

    @ColumnInfo(name = "createdAt")
    public long createdAt;

    @ColumnInfo(name = "modifiedAt")
    public long modifiedAt;

    @ColumnInfo(name = "appId")
    public int appId;

    @ColumnInfo(name = "deletedFlag")
    public boolean deletedFlag;

    @ColumnInfo(name = "isActive")
    public boolean isActive;
}
