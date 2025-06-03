package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Answers")
public class Answers {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="question_id")
    public int question_id;
    @ColumnInfo(name="parent_question_id")
    public int parent_question_id;
    @ColumnInfo(name="repeater_item_position")
    public int repeater_item_position;
    @ColumnInfo(name="submodule_id")
    public int submodule_id;
    @ColumnInfo(name="farmer_module_id")
    public int farmer_module_id;
    @ColumnInfo(name="fmp_id")
    public int fmp_id;
    @ColumnInfo(name="farmer_id")
    public int farmer_id;
    @ColumnInfo(name="olam_farmer_id")
    public int olam_farmer_id;
    @ColumnInfo(name="farm_id")
    public int farm_id;
    @ColumnInfo(name="answer_int")
    public int answer_int;
    @ColumnInfo(name="answer_boolean")
    public int answer_boolean;
    @ColumnInfo(name="answer_decimal")
    public int answer_decimal;
    @ColumnInfo(name="answer_date")
    public int answer_date;
    @ColumnInfo(name="answer_text")
    public int answer_text;
    @ColumnInfo(name="reporting_year")
    public int reporting_year;
    @ColumnInfo(name="metric_global_id")
    public int metric_global_id;
    @ColumnInfo(name="metric_local_id")
    public int metric_local_id;

    @ColumnInfo(name="metric_answer")
    public int metric_answer;
    @ColumnInfo(name="stakeholder_id")
    public int stakeholder_id;

}
