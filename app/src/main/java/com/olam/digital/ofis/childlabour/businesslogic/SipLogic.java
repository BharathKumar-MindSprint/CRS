package com.olam.digital.ofis.childlabour.businesslogic;

import android.content.Context;
import android.database.Cursor;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.childlabour.dto.VillageDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

        // min sdk version is 19. After API 24 we can use lambda expression
        Collections.sort(villageList, new Comparator<VillageDTO>() {
            @Override
            public int compare(final VillageDTO object1, final VillageDTO object2) {
                return object1.getVillageName().compareTo(object2.getVillageName());
            }
        });

        return  villageList;

    }

}
