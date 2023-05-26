package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmActionPlansItem")
public class DmActionPlansItem {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "isDefault")
    public boolean isDefault;

    @ColumnInfo(name = "actionPlan")
    public String actionPlan;
}
