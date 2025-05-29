package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "SubQuestionAnswer")
public class SubQuestionAnswer {

    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;

    @ColumnInfo(name = "parent_id")
    public int parent_id;

    @ColumnInfo(name = "question_id")
    public int question_id;

    @ColumnInfo(name = "module_id")
    public int module_id;

    @ColumnInfo(name = "farmer_id")
    public int farmer_id;

    @ColumnInfo(name = "question_answer")
    public String question_answer;

    @ColumnInfo(name = "report_year")
    public int report_year;

    @ColumnInfo(name = "question_answer_position")
    public int question_answer_position;

}
