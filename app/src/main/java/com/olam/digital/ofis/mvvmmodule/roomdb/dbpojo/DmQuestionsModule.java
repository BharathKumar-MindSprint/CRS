package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmQuestionsModule")
public class DmQuestionsModule {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "mandate")
    public boolean mandate;

    @ColumnInfo(name = "textBox")
    public String textBox;

    @ColumnInfo(name = "question")
    public String question;

    @ColumnInfo(name = "questionTr")
    public String questionTr;

    @ColumnInfo(name = "questionPt")
    public String questionPt;

    @ColumnInfo(name = "questionLo")
    public String questionLo;

    @ColumnInfo(name = "questionFr")
    public String questionFr;

    @ColumnInfo(name = "questionIdn")
    public String questionIdn;

    @ColumnInfo(name = "questionEs")
    public String questionEs;

    @ColumnInfo(name = "questionTh")
    public String questionTh;

    @ColumnInfo(name = "questionVi")
    public String questionVi;

    @ColumnInfo(name = "questionTypeId")
    public int questionTypeId;

    @ColumnInfo(name = "datatypeId")
    public int datatypeId;

    @ColumnInfo(name = "isCriticalQuest")
    public boolean isCriticalQuest;

    @ColumnInfo(name = "displayType")
    public String displayType;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "position")
    public int position;

    @ColumnInfo(name = "moduleId")
    public int moduleId;

    @ColumnInfo(name = "submoduleId")
    public int submoduleId;

}
