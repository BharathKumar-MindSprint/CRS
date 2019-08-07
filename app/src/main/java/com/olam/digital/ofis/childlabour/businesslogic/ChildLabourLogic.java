package com.olam.digital.ofis.childlabour.businesslogic;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.database.Cursor;
import android.widget.Toast;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.ViewFarmerActivity;
import com.olam.digital.ofis.childlabour.AddNewChildActivity;
import com.olam.digital.ofis.childlabour.ChildVisitDetailActivity;
import com.olam.digital.ofis.childlabour.RegisterChildActivity;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.dto.VillageDTO;

import java.util.ArrayList;
import java.util.List;


public class ChildLabourLogic {

    DataLayer dataLayer;


    public long insertVisitDetails(Context context, ContentValues contentValues) {
        dataLayer=new DataLayer(context.getResources(),context);
        long newVisitDetaisId = dataLayer.insertChildVisitDetails(contentValues);
        return newVisitDetaisId;
    }

    public long insertFarmerDetails(Context context, ContentValues contentValues) {
        dataLayer=new DataLayer(context.getResources(),context);
        long newChildId = dataLayer.insertChild(contentValues);
        return  newChildId;
    }

    public List<ChildrenDTO> getChildDetailsById(Context context, int farmerId) {
        dataLayer=new DataLayer(context.getResources(),context);
        List<ChildrenDTO> clildrenList=new ArrayList<ChildrenDTO>();
        Cursor cursor= dataLayer.getChildDetailsById(farmerId);
        ChildrenDTO childrenDTO;
        if(cursor!=null){
        while (cursor.moveToNext()) {
            childrenDTO = new ChildrenDTO();
            childrenDTO.setId(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_ID)));
            childrenDTO.setName(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME)));
            childrenDTO.setGender(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER)));
            childrenDTO.setAge(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE)));
            childrenDTO.setStatus(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_STATUS)));
            clildrenList.add(childrenDTO);
        }
        }
            return  clildrenList;

    }
}
