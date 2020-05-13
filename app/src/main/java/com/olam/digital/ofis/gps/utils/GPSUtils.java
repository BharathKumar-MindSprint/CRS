package com.olam.digital.ofis.gps.utils;

import android.content.ContentValues;
import android.content.Context;

import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;

import java.sql.Timestamp;

public class GPSUtils {

    public ContentValues getGeoContentValues(TrackDetailsDTO trackDetailsDTO){

        ContentValues geoContentValues=new ContentValues();
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_CAPTURED_MODULE, trackDetailsDTO.getModule());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_LATITUDE, trackDetailsDTO.getLatitude());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_LONGITUDE, trackDetailsDTO.getLongitude());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_RECORDED_DATE, trackDetailsDTO.getRecordDate());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_ID, trackDetailsDTO.getFarmerId());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_NAME, trackDetailsDTO.getFarmerName());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_ID, trackDetailsDTO.getChildId());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_NAME, trackDetailsDTO.getChildName());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_SIP_NAME, trackDetailsDTO.getSipName());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_SIP_TYPE, trackDetailsDTO.getSipType());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_TRAINING_COURSE_NAME, trackDetailsDTO.getTrainingCourseName());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_TRAINING_MODULE_NAME, trackDetailsDTO.getTrainingModuleName());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_SURVEY_NAME, trackDetailsDTO.getSurveyName());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_GENDER, trackDetailsDTO.getGender());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_AGE, trackDetailsDTO.getAge());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_POLYGON_PLOTS_DETAILS, trackDetailsDTO.getPolygonPlotDetails());

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_SUBMITTED_DATE,timestamp.toString());
        return  geoContentValues;
    }
}
