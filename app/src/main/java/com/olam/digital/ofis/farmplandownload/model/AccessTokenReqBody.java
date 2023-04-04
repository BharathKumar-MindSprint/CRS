package com.olam.digital.ofis.farmplandownload.model;


import com.google.gson.annotations.SerializedName;

public class AccessTokenReqBody {
    public AccessTokenReqBody(String userId, String password, String system) {
        this.userId = userId;
        this.password = password;
        this.system = system;
    }

    @SerializedName("userId")
    String userId;

    @SerializedName("password")
    String password;

    @SerializedName("system")
    String system;
}
