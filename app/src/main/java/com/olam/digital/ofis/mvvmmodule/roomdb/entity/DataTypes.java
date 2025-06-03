package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DataTypes")
public class DataTypes {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    public String name;
}
