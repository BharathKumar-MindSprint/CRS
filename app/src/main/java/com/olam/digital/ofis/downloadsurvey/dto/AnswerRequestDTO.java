package com.olam.digital.ofis.downloadsurvey.dto;

import java.io.Serializable;

// Pojo class for village details
public class AnswerRequestDTO implements Serializable {

    private String module_id;
    private String module_name;
    private String farmerGroupId;
    private String sectionId;
    private String listedName;
    private String dateTime;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getListedName() {
        return listedName;
    }

    public void setListedName(String listedName) {
        this.listedName = listedName;
    }

    public String getModule_id() {
        return module_id;
    }

    public void setModule_id(String module_id) {
        this.module_id = module_id;
    }

    public String getModule_name() {
        return module_name;
    }

    public void setModule_name(String module_name) {
        this.module_name = module_name;
    }

    public String getFarmerGroupId() {
        return farmerGroupId;
    }

    public void setFarmerGroupId(String farmerGroupId) {
        this.farmerGroupId = farmerGroupId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }
}
