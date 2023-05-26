package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmRecurrence")
public class DmRecurrence {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "recurrenceFrequency")
    public int recurrenceFrequency;

    @ColumnInfo(name = "isCustomRecurrence")
    public boolean isCustomRecurrence;

    @ColumnInfo(name = "recurrenceType")
    public String recurrenceType;
}
