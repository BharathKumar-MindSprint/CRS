package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon;

import com.google.gson.annotations.SerializedName;

public class PolygonPlotHistory {

    @SerializedName("id")
    public int id;

    @SerializedName("plotName")
    public String plotName;

    @SerializedName("positionData")
    public String positionData;

    @SerializedName("centerPlot")
    public String centerPlot;

    @SerializedName("plotArea")
    public Double plotArea;

}
