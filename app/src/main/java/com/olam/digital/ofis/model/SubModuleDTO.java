package com.olam.digital.ofis.model;

public class SubModuleDTO {

    int id;
    String sipName;
    String village;

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSipName() {
        return sipName;
    }

    public void setSipName(String sipName) {
        this.sipName = sipName;
    }

}
