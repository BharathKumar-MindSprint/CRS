package com.olam.digital.ofis.multifarm;

import org.json.JSONArray;

public class MultiFarmDTO {

    private String farmName;
    private String farmCode;
    private String farmId;

    private String farmerId;
    private String lat;
    private String lng;

    private JSONArray stakeholdersInfo;

    private String farmType;
    private boolean isExistingFarm;

    private int synced;

    public int getSynced() {
        return synced;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }

    public boolean isExistingFarm() {
        return isExistingFarm;
    }

    public void setExistingFarm(boolean existingFarm) {
        isExistingFarm = existingFarm;
    }

    public String getFarmType() {
        return farmType;
    }

    public void setFarmType(String farmType) {
        this.farmType = farmType;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public JSONArray getStakeholdersInfo() {
        return stakeholdersInfo;
    }

    public void setStakeholdersInfo(JSONArray stakeholdersInfo) {
        this.stakeholdersInfo = stakeholdersInfo;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getFarmCode() {
        return farmCode;
    }

    public void setFarmCode(String farmCode) {
        this.farmCode = farmCode;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }
}
