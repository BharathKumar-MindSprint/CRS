package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.list;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

public class PolygonPlotHistoryList {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("plotName")
    public String plotName;

    @SerializedName("positionData")
    public String positionData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    public String getPositionData() {
        return positionData;
    }

    public void setPositionData(String positionData) {
        this.positionData = positionData;
    }

    public PolygonPlotHistoryList(int id, String plotName, String positionData) {
        this.id = id;
        this.plotName = plotName;
        this.positionData = positionData;
    }
}
