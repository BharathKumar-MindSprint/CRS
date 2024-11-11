package com.olam.digital.ofis.mvvmmodule.model.multistakeholder;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "MultiStakeHolder")
public class MultiStakeHolder {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "msId")
    public Long msId;

    @ColumnInfo(name = "msTempId")
    public Long msTempId;

    @ColumnInfo(name = "farmerId")
    public Long farmerId;

    @ColumnInfo(name = "msName")
    public String msName;

    @ColumnInfo(name = "farmHoldingName")
    public String farmHoldingName;

    @ColumnInfo(name = "farmId")
    public String farmId;

    @ColumnInfo(name = "msGender")
    public String msGender;

    @ColumnInfo(name = "age")
    public String age;

    @ColumnInfo(name = "msPhoto")
    public String msPhoto;

    @ColumnInfo(name = "msDOB")
    public String msDOB;

    @ColumnInfo(name = "consentFarmer")
    public Integer consentFarmer;

    @ColumnInfo(name = "consentOthers")
    public Integer consentOthers;

    @ColumnInfo(name = "msSignature")
    public String msSignature;

    @ColumnInfo(name = "msDisplayId")
    public String msDisplayId;

    @ColumnInfo(name = "olamMultiStakeholderId")
    public String olamMultiStakeholderId;

    @ColumnInfo(name = "farmHoldingCode")
    public String farmHoldingCode;

    @ColumnInfo(name = "msRoleId")
    public String msRoleId;

    @ColumnInfo(name = "synced")
    public Integer synced;

    @ColumnInfo(name = "primary_lat")
    public String primary_lat;

    @ColumnInfo(name = "primary_lng")
    public String primary_lng;

    @ColumnInfo(name = "registeredDate")
    public String registeredDate;

    @ColumnInfo(name = "isConsentAvailable")
    public String isConsentAvailable;

    @ColumnInfo(name = "submitted_modules")
    public String submitted_modules;

    @ColumnInfo(name = "created_locally")
    public Integer created_locally;

}
