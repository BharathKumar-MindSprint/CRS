package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FarmerGroup")
public class FarmerGroup {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="programmeId")
    public int programmeId;
    @ColumnInfo(name="partnerId")
    public int partnerId;
    @ColumnInfo(name="productId")
    public int productId;
    @ColumnInfo(name="code")
    public String code;
    @ColumnInfo(name="name")
    public String name;
}
