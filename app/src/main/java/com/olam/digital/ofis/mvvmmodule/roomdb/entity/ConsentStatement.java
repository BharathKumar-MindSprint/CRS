package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ConsentStatement")
public class ConsentStatement {

    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="farmerId")
    public int farmerId;
    @ColumnInfo(name="msId")
    public int msId;
    @ColumnInfo(name="displayFarmerId")
    public String displayFarmerId;
    @ColumnInfo(name="farmerName")
    public String farmerName;
    @ColumnInfo(name="farmerConfirmation")
    public String farmerConfirmation;
    @ColumnInfo(name="childName")
    public String childName;
    @ColumnInfo(name="occupation")
    public String occupation;
    @ColumnInfo(name="farmerConsentSignature")
    public String farmerConsentSignature;
    @ColumnInfo(name="confirmedDate")
    public String confirmedDate;
    @ColumnInfo(name="enumeratorConsentSignature")
    public String enumeratorConsentSignature;
    @ColumnInfo(name="maritalStatus")
    public int maritalStatus;
    @ColumnInfo(name="profession")
    public String profession;
    @ColumnInfo(name="cpfNumber")
    public String cpfNumber;
    @ColumnInfo(name="regNumber")
    public String regNumber;
    @ColumnInfo(name="fullAddress")
    public String fullAddress;
    @ColumnInfo(name="city")
    public String city;
    @ColumnInfo(name="state")
    public String state;
    @ColumnInfo(name="subName")
    public String subName;
    @ColumnInfo(name="subNationality")
    public String subNationality;
    @ColumnInfo(name="submaritalStatus")
    public String submaritalStatus;
    @ColumnInfo(name="subProfession")
    public String subProfession;
    @ColumnInfo(name="subCpfNumber")
    public String subCpfNumber;
    @ColumnInfo(name="subRgNumber")
    public String subRgNumber;
    @ColumnInfo(name="subCity")
    public String subCity;
    @ColumnInfo(name="subState")
    public String subState;
    @ColumnInfo(name="contact")
    public String contact;
    @ColumnInfo(name="witness1Name")
    public String witness1Name;
    @ColumnInfo(name="witness1CPF")
    public String witness1CPF;
    @ColumnInfo(name="witness1RG")
    public String witness1RG;
    @ColumnInfo(name="witness1Sign")
    public String witness1Sign;
    @ColumnInfo(name="witness2Name")
    public String witness2Name;
    @ColumnInfo(name="witness2CPF")
    public String witness2CPF;
    @ColumnInfo(name="witness2RG")
    public String witness2RG;
    @ColumnInfo(name="witness2Sign")
    public String witness2Sign;
}
