package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FollowUpAnswerTable")
public class FollowUpAnswerTable {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "answerId")
    public int answerId;

    @ColumnInfo(name = "complianceStatusId")
    public int complianceStatusId;

    @ColumnInfo(name = "questionId")
    public int questionId;

    @ColumnInfo(name = "answerValue")
    public String answerValue;

    @ColumnInfo(name = "actionPlanId")
    public int actionPlanId;

    @ColumnInfo(name = "actionPlanText")
    public String actionPlanText;

    @ColumnInfo(name = "captureProofs")
    public String captureProofs;

}
