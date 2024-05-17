package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.APICall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DiagnosticModulesList;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonHistoryData;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;

import java.util.ArrayList;
import java.util.List;

public class PolygonHistoryResponseModel {

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

    @SerializedName("plot")
    public ArrayList<PolygonPlotHistory> plot;

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

    public PolygonHistoryResponseModel(String farmerName, String olamFarmerId, double primaryLat, double primaryLng, double farmerId, double farmerGroupId, double farmId, String farmCode, String farmName, ArrayList<PolygonPlotHistory> plot) {
        this.farmerName = farmerName;
        this.olamFarmerId = olamFarmerId;
        this.primaryLat = primaryLat;
        this.primaryLng = primaryLng;
        this.farmerId = farmerId;
        this.farmerGroupId = farmerGroupId;
        this.farmId = farmId;
        this.farmCode = farmCode;
        this.farmName = farmName;
        this.plot = plot;
    }
}
