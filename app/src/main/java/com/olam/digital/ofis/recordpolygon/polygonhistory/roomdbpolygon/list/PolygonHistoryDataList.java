package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.list;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;

import java.util.ArrayList;

public class PolygonHistoryDataList {

    @SerializedName("id")
    public int id;

    @SerializedName("farmerName")
    public String farmerName;

    @SerializedName("olam_farmer_id")
    public String olamFarmerId;

    @SerializedName("primary_lat")
    public double primaryLat;

    @SerializedName("primary_lng")
    public double primaryLng;

    @SerializedName("farmerId")
    public double farmerId;

    @SerializedName("farmerGroupId")
    public double farmerGroupId;

    @SerializedName("farm_id")
    public double farmId;

    @SerializedName("farmCode")
    public String farmCode;

    @SerializedName("farmName")
    public String farmName;

    @Ignore
    @SerializedName("plot")
    public ArrayList<PolygonPlotHistory> plot;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getOlamFarmerId() {
        return olamFarmerId;
    }

    public void setOlamFarmerId(String olamFarmerId) {
        this.olamFarmerId = olamFarmerId;
    }

    public double getPrimaryLat() {
        return primaryLat;
    }

    public void setPrimaryLat(double primaryLat) {
        this.primaryLat = primaryLat;
    }

    public double getPrimaryLng() {
        return primaryLng;
    }

    public void setPrimaryLng(double primaryLng) {
        this.primaryLng = primaryLng;
    }

    public double getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(double farmerId) {
        this.farmerId = farmerId;
    }

    public double getFarmerGroupId() {
        return farmerGroupId;
    }

    public void setFarmerGroupId(double farmerGroupId) {
        this.farmerGroupId = farmerGroupId;
    }

    public double getFarmId() {
        return farmId;
    }

    public void setFarmId(double farmId) {
        this.farmId = farmId;
    }

    public String getFarmCode() {
        return farmCode;
    }

    public void setFarmCode(String farmCode) {
        this.farmCode = farmCode;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public ArrayList<PolygonPlotHistory> getPlot() {
        return plot;
    }

    public void setPlot(ArrayList<PolygonPlotHistory> plot) {
        this.plot = plot;
    }
}
