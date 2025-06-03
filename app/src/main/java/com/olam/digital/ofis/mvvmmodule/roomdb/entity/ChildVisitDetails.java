package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ChildVisitDetails")
public class ChildVisitDetails {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="farmer_id")
    public int farmer_id;
    @ColumnInfo(name="household")
    public int household;
    @ColumnInfo(name="inspection")
    public int inspection;
    @ColumnInfo(name="followUp")
    public int followUp;
    @ColumnInfo(name="synced")
    public boolean synced;

}
