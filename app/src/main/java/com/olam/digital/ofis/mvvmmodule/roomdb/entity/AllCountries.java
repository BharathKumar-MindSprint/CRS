package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AllCountries")
public class AllCountries {

    @PrimaryKey
    @ColumnInfo(name="country_id")
    public int country_id;

    @ColumnInfo(name = "countryName")
    public String countryName;
}
