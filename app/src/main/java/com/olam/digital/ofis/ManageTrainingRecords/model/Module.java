package com.olam.digital.ofis.ManageTrainingRecords.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Module {
    @SerializedName("statusCode")
    @Expose
    int statusCode;
    @SerializedName("statusMessage")
    @Expose
    String statusMessage;
    @SerializedName("data")
    @Expose
    ArrayList<data> data;

    public Module(ArrayList<Module.data> data, int statusCode, String statusMessage) {
        this.data = data;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public ArrayList<Module.data> getData() {
        return data;
    }

    public void setData(ArrayList<Module.data> data) {
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

    public static class data{
        @SerializedName("courseTrainingModuleId")
        @Expose
        int courseTrainingModuleId;
        @SerializedName("courseId")
        @Expose
        int courseId;
        @SerializedName("moduleId")
        @Expose
        int moduleId;
        @SerializedName("moduleName")
        @Expose
        String moduleName;

        public data(int courseTrainingModuleId, int courseId, int moduleId, String moduleName) {
            this.courseTrainingModuleId = courseTrainingModuleId;
            this.courseId = courseId;
            this.moduleId = moduleId;
            this.moduleName = moduleName;
        }

        public int getCourseTrainingModuleId() {
            return courseTrainingModuleId;
        }

        public void setCourseTrainingModuleId(int courseTrainingModuleId) {
            this.courseTrainingModuleId = courseTrainingModuleId;
        }

        public int getCourseId() {
            return courseId;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }

        public int getModuleId() {
            return moduleId;
        }

        public void setModuleId(int moduleId) {
            this.moduleId = moduleId;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }
    }
}
