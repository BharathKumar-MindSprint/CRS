package com.olam.digital.ofis.mvvmmodule.model.followup;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FollowUpResponseModel {
    @SerializedName("diagnosticModuleData")
    private ArrayList<FollowUpResponseItem> followUpResponseItemList;

    @SerializedName("blobContainer")
    private String blobContainer;

    @SerializedName("baseBlobURL")
    private String baseBlobURL;

    @SerializedName("errorMessage")
    private String errorMessage;

    public ArrayList<FollowUpResponseItem> getFollowUpResponseItemList() {
        return followUpResponseItemList;
    }

    public void setFollowUpResponseItemList(ArrayList<FollowUpResponseItem> followUpResponseItemList) {
        this.followUpResponseItemList = followUpResponseItemList;
    }

    public String getBlobContainer() {
        return blobContainer;
    }

    public void setBlobContainer(String blobContainer) {
        this.blobContainer = blobContainer;
    }

    public String getBaseBlobURL() {
        return baseBlobURL;
    }

    public void setBaseBlobURL(String baseBlobURL) {
        this.baseBlobURL = baseBlobURL;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }



}
