package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmTextBoxModuleQuestions")
public class DmTextBoxModuleQuestions {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "answerType")
    public int answerType;

    @ColumnInfo(name = "isAllowNegative")
    public boolean isAllowNegative;

    @ColumnInfo(name = "isAllowDecimal")
    public boolean isAllowDecimal;

    @ColumnInfo(name = "minimumRange")
    public double minimumRange;

    @ColumnInfo(name = "maximumRange")
    public double maximumRange;

    @ColumnInfo(name = "questionId")
    public int questionId;
}
