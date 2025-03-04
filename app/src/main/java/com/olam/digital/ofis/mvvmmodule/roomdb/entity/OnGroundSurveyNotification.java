package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "OnGroundSurveyNotification")
public class OnGroundSurveyNotification {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    public Integer id;

    @ColumnInfo(name = "farmerGroupId")
    public Integer farmerGroupId;

    @ColumnInfo(name = "farmerId")
    public Long farmerId;

    @ColumnInfo(name = "farmerName")
    public String farmerName;

    @ColumnInfo(name = "photo")
    public String photo;

    @ColumnInfo(name = "olamFarmerId")
    public String olamFarmerId;

    @ColumnInfo(name = "createdOnPhone")
    public String createdOnPhone;

    @ColumnInfo(name = "countryId")
    public Integer countryId;

    @ColumnInfo(name = "odFarmerId")
    public String odFarmerId;

    @ColumnInfo(name = "plotId")
    public Long plotId;

    @ColumnInfo(name = "surveyType")
    public String surveyType;

    @ColumnInfo(name = "sectionId")
    public Long sectionId;

    public OnGroundSurveyNotification() {

    }

}
