package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmQuestionTypes")
public class DmQuestionTypes {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "value")
    public String value;
}
