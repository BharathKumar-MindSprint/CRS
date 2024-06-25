package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "PolygonPlotHistory")
public class PolygonPlotHistory {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "plotNo")
    public int plotNo;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "plotName")
    public String plotName;

    @ColumnInfo(name = "positionData")
    public String positionData;

    @ColumnInfo(name = "centerPlot")
    public String centerPlot;

    @ColumnInfo(name = "plotArea")
    public Double plotArea;

}
