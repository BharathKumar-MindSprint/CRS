package com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


import java.util.ArrayList;

@Entity(tableName = "PolygonHistoryData")
public class PolygonHistoryData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "farmerName")
    public String farmerName;

    @ColumnInfo(name = "olam_farmer_id")
    public String olamFarmerId;

    @ColumnInfo(name = "primary_lat")
    public double primaryLat;

    @ColumnInfo(name = "primary_lng")
    public double primaryLng;

    @ColumnInfo(name = "farmerId")
    public double farmerId;

    @ColumnInfo(name = "farmerGroupId")
    public double farmerGroupId;

    @ColumnInfo(name = "farm_id")
    public double farmId;

    @ColumnInfo(name = "farmCode")
    public String farmCode;

    @ColumnInfo(name = "farmName")
    public String farmName;

    @Ignore
    @ColumnInfo(name = "plot")
    public ArrayList<PolygonPlotHistory> plot;

}
