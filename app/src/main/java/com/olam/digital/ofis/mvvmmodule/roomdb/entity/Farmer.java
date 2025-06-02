package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Farmer")
public class Farmer {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="farmId")
    public int farmId;
    @ColumnInfo(name="placeId")
    public int placeId;
    @ColumnInfo(name="sectionId")
    public int sectionId;
    @ColumnInfo(name="name")
    public int name;
    @ColumnInfo(name="gender")
    public int gender;
    @ColumnInfo(name="countryId")
    public int countryId;
    @ColumnInfo(name="regionId")
    public int regionId;
    @ColumnInfo(name="farmerGroupId")
    public int farmerGroupId;
    @ColumnInfo(name="districtId")
    public int districtId;
    @ColumnInfo(name="subDistrictId")
    public int subDistrictId;
    @ColumnInfo(name="isConsentAvailable")
    public int isConsentAvailable;
    @ColumnInfo(name="productId")
    public int productId;
    @ColumnInfo(name="partnerId")
    public int partnerId;
    @ColumnInfo(name="programmeId")
    public int programmeId;
    @ColumnInfo(name="odFarmerId")
    public int odFarmerId;
    @ColumnInfo(name="dob")
    public int dob;
    @ColumnInfo(name="olamFarmerId")
    public int olamFarmerId;
    @ColumnInfo(name="photo")
    public int photo;
    @ColumnInfo(name="signature")
    public int signature;
    @ColumnInfo(name="synced")
    public int synced;
    @ColumnInfo(name="submittedModules")
    public int submittedModules;
    @ColumnInfo(name="primaryLat")
    public float primaryLat;
    @ColumnInfo(name="primaryLong")
    public int primaryLong;
    @ColumnInfo(name="householdLat")
    public int householdLat;
    @ColumnInfo(name="householdLong")
    public int householdLong;
    @ColumnInfo(name="householdId")
    public int householdId;
    @ColumnInfo(name="nidModeration")
    public int nidModeration;
    @ColumnInfo(name="consentFarmer")
    public boolean consentFarmer;
    @ColumnInfo(name="consentOther")
    public boolean consentOther;
    @ColumnInfo(name="certificationIds")
    public String certificationIds;
    @ColumnInfo(name="certificationText")
    public String certificationText;
    @ColumnInfo(name="age")
    public int age;
    @ColumnInfo(name="farmerComplaint")
    public boolean farmerComplaint;
    @ColumnInfo(name="conductSurveyForNonComplaint")
    public boolean conductSurveyForNonComplaint;
    @ColumnInfo(name="conductTrainingForNonComplaint")
    public boolean conductTrainingForNonComplaint;
    @ColumnInfo(name="polygonCaptureForNonComplaint")
    public boolean polygonCaptureForNonComplaint;
    @ColumnInfo(name="farmCode")
    public String farmCode;
    @ColumnInfo(name="farmName")
    public String farmName;





}
