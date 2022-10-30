package com.olam.digital.ofis.downloadsurvey.dto;

import java.io.Serializable;

// Pojo class for farmer details
public class FarmerDTO implements Serializable {
    private  String id;
    private  String synced;
    private  String farmerId;
    private  String name;
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

    public String getSynced() {
        return synced;
    }

    public void setSynced(String synced) {
        this.synced = synced;
    }



}
