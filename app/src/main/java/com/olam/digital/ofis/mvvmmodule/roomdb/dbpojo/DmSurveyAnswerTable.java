package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmSurveyAnswerTable")
public class DmSurveyAnswerTable {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "questionID")
    public String questionID;

    @ColumnInfo(name = "answerID")
    public String answerID;
}
