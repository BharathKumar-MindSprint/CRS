package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Assignments")
public class Assignments {
    @PrimaryKey
    @ColumnInfo(name="productId")
    public int productId;
    @ColumnInfo(name="countryId")
    public int countryId;
    @ColumnInfo(name="isODOrigin")
    public boolean isODOrigin;
    @ColumnInfo(name="countryISOCode")
    public String countryISOCode;
    @ColumnInfo(name="isCompliance")
    public boolean isCompliance;
}
