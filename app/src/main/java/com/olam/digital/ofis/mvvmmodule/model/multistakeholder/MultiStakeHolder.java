package com.olam.digital.ofis.mvvmmodule.model.multistakeholder;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "MultiStakeHolder")
public class MultiStakeHolder {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "msId")
    @SerializedName("msId")
    public Long msId;

    @ColumnInfo(name = "msTempId")
    public Long msTempId;

    @ColumnInfo(name = "farmerId")
    @SerializedName("farmerId")
    public Long farmerId;

    @ColumnInfo(name = "msName")
    @SerializedName("msName")
    public String msName;

    @ColumnInfo(name = "farmHoldingName")
    public String farmHoldingName;

    @ColumnInfo(name = "farmId")
    @SerializedName("farmId")
    public String farmId;

    @ColumnInfo(name = "msGender")
    @SerializedName("msGender")
    public String msGender;

    @ColumnInfo(name = "age")
    public String age;

    @ColumnInfo(name = "msPhoto")
    @SerializedName("msPhoto")
    public String msPhoto;

    @ColumnInfo(name = "msDOB")
    @SerializedName("msDOB")
    public String msDOB;

    @ColumnInfo(name = "consentFarmer")
    public Integer consentFarmer;

    @ColumnInfo(name = "consentOthers")
    public Integer consentOthers;

    @ColumnInfo(name = "msSignature")
    @SerializedName("msSignature")
    public String msSignature;

    @ColumnInfo(name = "msDisplayId")
    public String msDisplayId;

    @ColumnInfo(name = "olamMultiStakeholderId")
    @SerializedName("olamMultiStakeHolderId")
    public String olamMultiStakeholderId;

    @ColumnInfo(name = "farmHoldingCode")
    public String farmHoldingCode;

    @ColumnInfo(name = "msRoleId")
    @SerializedName("msRoleId")
    public String msRoleId;

    @ColumnInfo(name = "synced")
    @SerializedName("synced")
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
