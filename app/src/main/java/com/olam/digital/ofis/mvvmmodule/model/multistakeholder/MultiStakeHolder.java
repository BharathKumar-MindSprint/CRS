package com.olam.digital.ofis.mvvmmodule.model.multistakeholder;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "MultiStakeHolder")
public class MultiStakeHolder {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public Long id;

    @ColumnInfo(name = "farmerId")
    public Long farmerId;

    @ColumnInfo(name = "stakeHolderName")
    public String stakeHolderName;

    @ColumnInfo(name = "gender")
    public String gender;

    @ColumnInfo(name = "photoLocation")
    public String photoLocation;

    @ColumnInfo(name = "dob")
    public String dob;

    @ColumnInfo(name = "nationalId")
    public String nationalId ;

    @ColumnInfo(name = "phoneNumber")
    public String phoneNumber;

    @ColumnInfo(name = "consentFarmer")
    public String consentFarmer;

    @ColumnInfo(name = "consentOthers")
    public String consentOthers;

    @ColumnInfo(name = "signature")
    public String signature;

    @ColumnInfo(name = "farmHoldingCode")
    public String farmHoldingCode;

    @ColumnInfo(name = "stakeHolderRole")
    public String stakeHolderRole;

}
