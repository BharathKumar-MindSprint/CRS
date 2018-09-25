package com.webpoint0.ofiscompanion.authentication.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RefreshTokenDTO {

    @SerializedName("client_secret")
    @Expose
    private String clientSecret;
    @SerializedName("client_id")
    @Expose
    private Long clientId;
    @SerializedName("refresh_token")
    @Expose
    private String refreshToken;
    @SerializedName("grant_type")
    @Expose
    private String grantType;
    @SerializedName("application")
    @Expose
    private String application;

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

}