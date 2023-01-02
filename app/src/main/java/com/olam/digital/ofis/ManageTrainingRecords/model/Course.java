package com.olam.digital.ofis.ManageTrainingRecords.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Course {
    @SerializedName("statusCode")
    @Expose
    int statusCode;
    @SerializedName("statusMessage")
    @Expose
    String statusMessage;
    @SerializedName("data")
    @Expose
    ArrayList<data> data;

    public Course(ArrayList<Course.data> data, int statusCode, String statusMessage) {
        this.data = data;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public ArrayList<Course.data> getData() {
        return data;
    }

    public void setData(ArrayList<Course.data> data) {
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
        @SerializedName("courseId")
        @Expose
        int courseId;
        @SerializedName("courseName")
        @Expose
        String courseName;

        public data(int courseId, String courseName) {
            this.courseId = courseId;
            this.courseName = courseName;
        }

        public int getCourseId() {
            return courseId;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
    }

}


