package com.olam.digital.ofis.farmplandownload.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccessTokenResp {
    public AccessTokenResp(String accessToken) {
        this.accessToken = accessToken;
    }

    @SerializedName("accessToken")
    @Expose
    String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }



}
