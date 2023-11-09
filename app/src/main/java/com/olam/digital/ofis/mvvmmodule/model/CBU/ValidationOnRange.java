package com.olam.digital.ofis.mvvmmodule.model.CBU;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ValidationOnRange")
public class ValidationOnRange {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "value")
    public String value;

    @ColumnInfo(name = "active")
    public boolean active;
}
