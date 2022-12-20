package com.olam.digital.ofis.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DocumentList {
    @SerializedName("statusCode")
    @Expose
    int statusCode;
    @SerializedName("statusMessage")
    @Expose
    String statusMessage;
    @SerializedName("data")
    @Expose
    ArrayList<data> data;

    public DocumentList(int statusCode, String statusMessage, ArrayList<DocumentList.data> data) {
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

    public ArrayList<DocumentList.data> getData() {
        return data;
    }

    public void setData(ArrayList<DocumentList.data> data) {
        this.data = data;
    }

    public static class data{
        @SerializedName("description")
         @Expose
        String description;
        @SerializedName("fileFormat")
        @Expose
        String fileFormat;
        @SerializedName("fileName")
        @Expose
        String fileName;
        @SerializedName("fileOrder")
        @Expose
        int fileOrder;
        @SerializedName("fileSize")
        @Expose
        String fileSize;
        @SerializedName("tDocsId")
        @Expose
        int tDocsId;
        @SerializedName("tDocsLanId")
        @Expose
        int tDocsLanId;
        @SerializedName("updatedFileName")
        @Expose
        String updatedFileName;

        public data(String description, String fileFormat, String fileName, int fileOrder, String fileSize, int tDocsId, int tDocsLanId, String updatedFileName) {
            this.description = description;
            this.fileFormat = fileFormat;
            this.fileName = fileName;
            this.fileOrder = fileOrder;
            this.fileSize = fileSize;
            this.tDocsId = tDocsId;
            this.tDocsLanId = tDocsLanId;
            this.updatedFileName = updatedFileName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getFileFormat() {
            return fileFormat;
        }

        public void setFileFormat(String fileFormat) {
            this.fileFormat = fileFormat;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public int getFileOrder() {
            return fileOrder;
        }

        public void setFileOrder(int fileOrder) {
            this.fileOrder = fileOrder;
        }

        public String getFileSize() {
            return fileSize;
        }

        public void setFileSize(String fileSize) {
            this.fileSize = fileSize;
        }

        public int gettDocsId() {
            return tDocsId;
        }

        public void settDocsId(int tDocsId) {
            this.tDocsId = tDocsId;
        }

        public int gettDocsLanId() {
            return tDocsLanId;
        }

        public void settDocsLanId(int tDocsLanId) {
            this.tDocsLanId = tDocsLanId;
        }

        public String getUpdatedFileName() {
            return updatedFileName;
        }

        public void setUpdatedFileName(String updatedFileName) {
            this.updatedFileName = updatedFileName;
        }
    }
}
