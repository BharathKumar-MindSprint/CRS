package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.APICall;

import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;

import java.util.ArrayList;

public class PolygonHistoryResponseModel {

    @SerializedName("plot")
    private ArrayList<PolygonPlotHistory> plot;

    public ArrayList<PolygonPlotHistory> getPlot() {
        return plot;
    }

    public void setPlot(ArrayList<PolygonPlotHistory> plot) {
        this.plot = plot;
    }

    public PolygonHistoryResponseModel(ArrayList<PolygonPlotHistory> plot) {
        this.plot = plot;
    }
}
