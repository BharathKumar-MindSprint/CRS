package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FollowUpAnswerTable")
public class FollowUpAnswerTable {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "questionId")
    public int questionId;

}
