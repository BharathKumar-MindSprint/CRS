package com.olam.digital.ofis.mvvmmodule.model.multistakeholder;

import com.google.gson.annotations.SerializedName;

public class MultiStakeHolderDto {

    @SerializedName("msId")
    public Long msId;

    @SerializedName("farmerId")
    public Long farmerId;

    @SerializedName("msName")
    public String msName;

    @SerializedName("farmId")
    public Long farmId;

    @SerializedName("msDisplayId")
    public String msDisplayId;

    @SerializedName("msGender")
    public String msGender;

    @SerializedName("msPhoto")
    public String msPhoto;

    @SerializedName("msDOB")
    public String msDOB;

    @SerializedName("msSignature")
    public String msSignature;

    @SerializedName("msRoleId")
    public String msRoleId;

    @SerializedName("synced")
    public int synced = 1;

}
