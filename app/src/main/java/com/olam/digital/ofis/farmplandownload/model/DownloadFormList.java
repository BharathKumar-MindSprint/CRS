package com.olam.digital.ofis.farmplandownload.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class DownloadFormList {
    @SerializedName("statusCode")
    @Expose
    int statusCode;
    @SerializedName("statusMessage")
    @Expose
    String statusMessage;

    @SerializedName("data")
    @Expose
    ArrayList<data> data;

    public DownloadFormList(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }
    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    public DownloadFormList(ArrayList<data> data) {
        this.data = data;
    }

    public ArrayList<data> getData() {
        return data;
    }

    public void setData(ArrayList<data> data) {
        this.data = data;
    }

    public static class data implements Serializable {

        public data(String productName, int productId, String country, String fileUrl, String baseBlobURL, String filename, boolean isdownloadfarmplan) {
            this.productName = productName;
            this.productId = productId;
            this.country = country;
            this.fileUrl = fileUrl;
            this.baseBlobURL = baseBlobURL;
            this.filename = filename;
            this.isdownloadfarmplan = isdownloadfarmplan;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getBaseBlobURL() {
            return baseBlobURL;
        }

        public void setBaseBlobURL(String baseBlobURL) {
            this.baseBlobURL = baseBlobURL;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }



        public boolean isIsdownloadfarmplan() {
            return isdownloadfarmplan;
        }

        public void setIsdownloadfarmplan(boolean isdownloadfarmplan) {
            this.isdownloadfarmplan = isdownloadfarmplan;
        }

        @SerializedName("productName")
        @Expose
        String productName;
        @SerializedName("productId")
        @Expose
        int productId;

        @SerializedName("country")
        @Expose
        String country;

        @SerializedName("fileUrl")
        @Expose
        String fileUrl;

        @SerializedName("baseBlobURL")
        @Expose
        String baseBlobURL;

        @SerializedName("filename")
        @Expose
        String filename;


        @SerializedName("isdownloadfarmplan")
        @Expose
        boolean isdownloadfarmplan;



    }
}
