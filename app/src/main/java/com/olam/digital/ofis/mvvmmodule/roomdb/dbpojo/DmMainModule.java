package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.JsonArray;

import java.util.ArrayList;

@Entity(tableName = "DmMainModule")
public class DmMainModule {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "descriptionPt")
    public String descriptionPt;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "descriptionVi")
    public String descriptionVi;

    @ColumnInfo(name = "descriptionEs")
    public String descriptionEs;

    @ColumnInfo(name = "descriptionTh")
    public String descriptionTh;

    @ColumnInfo(name = "descriptionLo")
    public String descriptionLo;

    @ColumnInfo(name = "descriptionIdn")
    public String descriptionIdn;

    @ColumnInfo(name = "descriptionTr")
    public String descriptionTr;

    @ColumnInfo(name = "descriptionFr")
    public String descriptionFr;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "nameFr")
    public String nameFr;

    @ColumnInfo(name = "nameIdn")
    public String nameIdn;

    @ColumnInfo(name = "namePt")
    public String namePt;

    @ColumnInfo(name = "nameLo")
    public String nameLo;

    @ColumnInfo(name = "nameTr")
    public String nameTr;

    @ColumnInfo(name = "nameTh")
    public String nameTh;

    @ColumnInfo(name = "nameVi")
    public String nameVi;

    @ColumnInfo(name = "nameEs")
    public String nameEs;

    @ColumnInfo(name = "followUpAttempts")
    public int followUpAttempts;

    @ColumnInfo(name = "surveyType")
    public int surveyType;

    @ColumnInfo(name = "followUpDuration")
    public int followUpDuration;

    @ColumnInfo(name = "assessmentType")
    public String assessmentType;

    @ColumnInfo(name = "startDate")
    public String startDate;

    @ColumnInfo(name = "displayLogic")
    public String displayLogic;

    @ColumnInfo(name = "maxScore")
    public int maxScore;



}
