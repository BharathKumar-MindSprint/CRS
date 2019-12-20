package com.olam.digital.ofis.gps.utils;

import android.content.ContentValues;
import android.content.Context;

import com.olam.digital.ofis.OFISDBContract;

import java.sql.Timestamp;

public class GPSUtils {

    public ContentValues getGeoContentValues(String module,String latitude,String longitude,String recordedDate){

        ContentValues geoContentValues=new ContentValues();
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_CAPTURED_MODULE, module);
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_LATITUDE, latitude);
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_LONGITUDE, longitude);
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_RECORDED_DATE, recordedDate);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_SUBMITTED_DATE,timestamp.toString());
        return  geoContentValues;
    }
}
