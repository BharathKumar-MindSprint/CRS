package com.olam.digital.ofis.ManageTrainingRecords.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Assets {
    @SerializedName("statusCode")
    @Expose
    int statusCode;
    @SerializedName("statusMessage")
    @Expose
    String statusMessage;
    @SerializedName("data")
    @Expose
    ArrayList<data> data;

    public Assets(int statusCode, String statusMessage, ArrayList<Assets.data> data) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public ArrayList<Assets.data> getData() {
        return data;
    }

    public void setData(ArrayList<Assets.data> data) {
        this.data = data;
    }

    public static class data {
        @SerializedName("courseAssetTypeId")
        @Expose
        int courseAssetTypeId;
        @SerializedName("isActive")
        @Expose
        boolean isActive;

        @SerializedName("assetType")
        @Expose
        String assetType;

        @SerializedName("assetTypeId")
        @Expose
        int assetTypeId;

        public data(int courseAssetTypeId, boolean isActive, String assetType, int assetTypeId) {
            this.courseAssetTypeId = courseAssetTypeId;
            this.isActive = isActive;
            this.assetType = assetType;
            this.assetTypeId = assetTypeId;
        }

        public int getCourseAssetTypeId() {
            return courseAssetTypeId;
        }

        public void setCourseAssetTypeId(int courseAssetTypeId) {
            this.courseAssetTypeId = courseAssetTypeId;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public String getAssetType() {
            return assetType;
        }

        public void setAssetType(String assetType) {
            this.assetType = assetType;
        }

        public int getAssetTypeId() {
            return assetTypeId;
        }

        public void setAssetTypeId(int assetTypeId) {
            this.assetTypeId = assetTypeId;
        }
    }
}
