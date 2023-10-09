package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmSavePartialSurveyTable")
public class DmSavePartialSurveyTable {
    @PrimaryKey(autoGenerate = true)
    public int uid;

}
