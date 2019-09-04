package com.olam.digital.ofis.childlabour.businesslogic;

import android.content.Context;
import android.database.Cursor;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.childlabour.dto.VillageDTO;

import java.util.ArrayList;
import java.util.List;

public class SipLogic {

    public static List<VillageDTO> getAllPlaces(Context context){
        DataLayer dataLayer=new DataLayer(context.getResources(),context);
        Cursor cursor= dataLayer.getAllPlaces();
        List<VillageDTO> villageList=new ArrayList<VillageDTO>();
        VillageDTO villageDTO;
        while (cursor.moveToNext()){
            villageDTO=new VillageDTO();
            villageDTO.setId(cursor.getString(cursor.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_ID)));
            villageDTO.setVillageName(cursor.getString(cursor.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME)));
            villageList.add(villageDTO);
        }
        return  villageList;

    }

}
