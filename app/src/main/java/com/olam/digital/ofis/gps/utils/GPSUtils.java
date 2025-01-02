package com.olam.digital.ofis.gps.utils;

import android.content.ContentValues;
import android.location.Location;

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
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_GENDER, trackDetailsDTO.getFarmerGender());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_GENDER, trackDetailsDTO.getChildGender());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_AGE, trackDetailsDTO.getChildAge());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_POLYGON_PLOTS_DETAILS, trackDetailsDTO.getPolygonPlotDetails());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_NETWORK_QUEUE_ID, String.valueOf(trackDetailsDTO.getNetworkQueueId()));
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_GPS_ACCURACY, String.valueOf(trackDetailsDTO.getAccuracy()));
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_NATIONAL_ID, String.valueOf(trackDetailsDTO.getNationalId()));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        geoContentValues.put(OFISDBContract.GPSDetails.COLUMN_NAME_SUBMITTED_DATE,timestamp.toString());
        return  geoContentValues;
    }

    public static boolean isMockLocation(Location location){
        if (android.os.Build.VERSION.SDK_INT > 31  ) {
            return location.isMock();
        }else {
            return location.isFromMockProvider();
        }
    }
}
