package com.olam.digital.ofis.gps.dto;

public class TrackDetailsDTO {

    private String latitude;
    private String longitude;
    private String module;
    private String recordDate;
    private String submittedDate;

    private String farmerId;
    private String farmerName;
    private String childId;
    private String childName;
    private String sipName;
    private String sipType;
    private String trainingCourseName;
    private String trainingModuleName;
    private String surveyName;

    private String gender;
    private String age;
    private String polygonPlotDetails;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPolygonPlotDetails() {
        return polygonPlotDetails;
    }

    public void setPolygonPlotDetails(String polygonPlotDetails) {
        this.polygonPlotDetails = polygonPlotDetails;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTrainingCourseName() {
        return trainingCourseName;
    }

    public void setTrainingCourseName(String trainingCourseName) {
        this.trainingCourseName = trainingCourseName;
    }

    public String getTrainingModuleName() {
        return trainingModuleName;
    }

    public void setTrainingModuleName(String trainingModuleName) {
        this.trainingModuleName = trainingModuleName;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getSipName() {
        return sipName;
    }

    public void setSipName(String sipName) {
        this.sipName = sipName;
    }

    public String getSipType() {
        return sipType;
    }

    public void setSipType(String sipType) {
        this.sipType = sipType;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }
}
