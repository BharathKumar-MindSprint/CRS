package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmRangeActionPlansItems")
public class DmRangeActionPlansItems {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "questionId")
    public int questionId;

    @ColumnInfo(name = "complainceId")
    public int complainceId;

    @ColumnInfo(name = "isDefault")
    public boolean isDefault;

    @ColumnInfo(name = "actionPlan")
    public String actionPlan;

}
