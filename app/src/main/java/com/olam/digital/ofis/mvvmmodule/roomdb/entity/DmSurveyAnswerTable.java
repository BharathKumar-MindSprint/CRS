package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

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

    @ColumnInfo(name = "answer")
    public String answer;

    @ColumnInfo(name = "question")
    public String question;


    @ColumnInfo(name = "moduleId")
    public String moduleId;

    @ColumnInfo(name = "complinceTypeId")
    public String complinceTypeId;

    @ColumnInfo(name = "actionplanId", defaultValue = "0",index = true)
    public String actionplanId;

    @ColumnInfo(name = "actionPlanText",defaultValue = "0",index = true)
    public String actionPlanText;

    @ColumnInfo(name = "allowCustomActionPlan")
    public Boolean allowCustomActionPlan;

    @ColumnInfo(name = "isCaptureProof")
    public Boolean isCaptureProof;

    @ColumnInfo(name = "amount")
    public int amount;

    @ColumnInfo(name = "farmerId")
    public String farmerId;

    @ColumnInfo(name = "resolved", defaultValue = "0", index = true)
    public String resolved;

    @ColumnInfo(name = "isSynced")
    public Boolean isSynced;

    @ColumnInfo(name = "submittedModuleId")
    public int submittedModuleId;

    @ColumnInfo(name = "plotId", defaultValue = "0")
    public Integer plotId;

    @ColumnInfo(name = "displayType")
    public String displayType;

}
