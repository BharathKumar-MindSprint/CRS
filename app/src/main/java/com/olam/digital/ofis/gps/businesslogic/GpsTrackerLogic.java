package com.olam.digital.ofis.gps.businesslogic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.childlabour.AddNewChildActivity;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.recordpolygon.RecordPolygonActivity;

import java.util.ArrayList;
import java.util.List;

public class GpsTrackerLogic {

    DataLayer dataLayer;

    public long insertGeoDataDetails(Context context, ContentValues contentValues) {
        dataLayer = new DataLayer(context.getResources(), context);
        long newGPSId = dataLayer.insertGeoData(context,contentValues);
        return newGPSId;
    }

    public long insertGPSDetailsToQueue(Context context,ContentValues contentValues){
        dataLayer = new DataLayer(context.getResources(), context);
        long newGPSId = dataLayer.insertGPSDetailsNetworkQueue(context,contentValues);
        return newGPSId;
    }

    public List<TrackDetailsDTO> getGPSTrackList(Context context){
        dataLayer = new DataLayer(context.getResources(), context);
        List<TrackDetailsDTO> gpsTrackList=new ArrayList<>();
        Cursor cursor=dataLayer.getGeoTrackList();
        TrackDetailsDTO trackDetailsDTO;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                trackDetailsDTO = new TrackDetailsDTO();
                trackDetailsDTO.setModule(cursor.getString(cursor.getColumnIndex(OFISDBContract.GPSDetails.COLUMN_NAME_CAPTURED_MODULE)));
                trackDetailsDTO.setLatitude(cursor.getString(cursor.getColumnIndex(OFISDBContract.GPSDetails.COLUMN_NAME_LATITUDE)));
                trackDetailsDTO.setLongitude(cursor.getString(cursor.getColumnIndex(OFISDBContract.GPSDetails.COLUMN_NAME_LONGITUDE)));
                trackDetailsDTO.setRecordDate(cursor.getString(cursor.getColumnIndex(OFISDBContract.GPSDetails.COLUMN_NAME_RECORDED_DATE)));

                gpsTrackList.add(trackDetailsDTO);
            }
        }
        return gpsTrackList;
    }

    public static int getGeoPointCount(Context context){
       DataLayer dataLayer = new DataLayer(context.getResources(), context);
        return  dataLayer.getGeoTrackList().getCount();
    }

    public void deleteGeoDetails(Context context) {
       DataLayer dataLayer = new DataLayer(context.getResources(), context);
       dataLayer.deleteGeoPoints();
    }


    public void updateFarmerDetails(Context context, long editFarmerId, String farmerNameValue, String genderValue) {
        dataLayer = new DataLayer(context.getResources(), context);
        dataLayer.updateFarmerDetails(editFarmerId,farmerNameValue,genderValue);

    }

    public void updateChildDetails(Context context, String childId, String name, String gender, String age) {
        dataLayer = new DataLayer(context.getResources(), context);
        dataLayer.updateChildDetails(childId,name,gender,age);
    }
}
