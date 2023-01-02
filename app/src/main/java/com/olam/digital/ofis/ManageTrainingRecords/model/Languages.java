package com.olam.digital.ofis.ManageTrainingRecords.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Languages {
    @SerializedName("statusCode")
    @Expose
    int statusCode;
    @SerializedName("statusMessage")
    @Expose
    String statusMessage;
    @SerializedName("data")
    @Expose
    ArrayList<data> data;

    public Languages(ArrayList<Languages.data> data, int statusCode, String statusMessage) {
        this.data = data;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public ArrayList<Languages.data> getData() {
        return data;
    }

    public void setData(ArrayList<Languages.data> data) {
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

    public static class data {
        @SerializedName("languageName")
        @Expose
        String languageName;
        @SerializedName("courseLanguageId")
        @Expose
        int courseLanguageId;
        @SerializedName("languageIsoCode")
        @Expose
        boolean languageIsoCode;

        public data(String languageName, int courseLanguageId, boolean languageIsoCode) {
            this.languageName = languageName;
            this.courseLanguageId = courseLanguageId;
            this.languageIsoCode = languageIsoCode;
        }

        public String getLanguageName() {
            return languageName;
        }

        public void setLanguageName(String languageName) {
            this.languageName = languageName;
        }

        public int getCourseLanguageId() {
            return courseLanguageId;
        }

        public void setCourseLanguageId(int courseLanguageId) {
            this.courseLanguageId = courseLanguageId;
        }

        public boolean isLanguageIsoCode() {
            return languageIsoCode;
        }

        public void setLanguageIsoCode(boolean languageIsoCode) {
            this.languageIsoCode = languageIsoCode;
        }
    }
}
