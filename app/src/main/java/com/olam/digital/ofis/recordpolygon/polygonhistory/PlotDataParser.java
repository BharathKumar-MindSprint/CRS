package com.olam.digital.ofis.recordpolygon.polygonhistory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class PlotDataParser {

    public static ArrayList<GeoPoint> parsePlotData(String jsonString) {
        ArrayList<GeoPoint> plotPoints = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray plotArray = jsonObject.getJSONArray("plot");

            for (int i = 0; i < plotArray.length(); i++) {
                JSONObject pointObject = plotArray.getJSONObject(i);
                double lat = pointObject.getDouble("lat");
                double lng = pointObject.getDouble("lng");
                GeoPoint geoPoint = new GeoPoint(lat, lng);
                plotPoints.add(geoPoint);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return plotPoints;
    }
}
