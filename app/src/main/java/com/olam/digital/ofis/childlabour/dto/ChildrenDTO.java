package com.olam.digital.ofis.childlabour.dto;

import java.io.Serializable;

public class ChildrenDTO implements Serializable {

    private  String id;
    private  String farmerId;
    private  String name;
    private  String age;
    private  String gender;
    private String submittedModules;
    private String createdAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSubmittedModules() {
        return submittedModules;
    }

    public void setSubmittedModules(String submittedModules) {
        this.submittedModules = submittedModules;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getActionFlag() {
        return actionFlag;
    }

    public void setActionFlag(String actionFlag) {
        this.actionFlag = actionFlag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAgeOnYear() {
        return ageOnYear;
    }

    public void setAgeOnYear(String ageOnYear) {
        this.ageOnYear = ageOnYear;
    }

    public String getSynced() {
        return synced;
    }

    public void setSynced(String synced) {
        this.synced = synced;
    }

    private  String actionFlag;
    private  String status;
    private  String ageOnYear;
    private  String synced;
}
