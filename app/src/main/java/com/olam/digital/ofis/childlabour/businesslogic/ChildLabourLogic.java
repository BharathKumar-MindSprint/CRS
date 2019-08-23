package com.olam.digital.ofis.childlabour.businesslogic;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.database.Cursor;
import android.widget.Toast;

import com.google.gson.Gson;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.AddNewChildActivity;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.ChildVisitDetailActivity;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ChildLabourLogic {

    DataLayer dataLayer;


    public long insertVisitDetails(Context context, ContentValues contentValues) {
        dataLayer = new DataLayer(context.getResources(), context);
        long newVisitDetaisId = dataLayer.insertChildVisitDetails(contentValues);
        return newVisitDetaisId;
    }

    public long insertVisitSurveyDetails(Context context, ContentValues contentValues,String name,String type) {
        dataLayer = new DataLayer(context.getResources(), context);
        long newVisitDetaisId = dataLayer.insertChildVisitSurveyDetails(contentValues,name,type);
        return newVisitDetaisId;
    }

    public long insertChildDetails(Context context, ContentValues contentValues,String name) {
        dataLayer = new DataLayer(context.getResources(), context);
        long newChildId = dataLayer.insertChild(context,contentValues, name);
        return newChildId;
    }

    public long deleteVisitDetails(Context context, String farmerId) {
        dataLayer = new DataLayer(context.getResources(), context);
        return dataLayer.deleteNetworkQueueItemByFarmerId(Integer.parseInt(farmerId), "child_visit");

    }

    public long deleteChildVisitDetails(Context context, String farmerId) {
        dataLayer = new DataLayer(context.getResources(), context);
        return dataLayer.deleteChildVisitDetailsById(Long.valueOf(farmerId));

    }

    public long updateChildDetails(Context context, ContentValues contentValues, String id,String name) {
        dataLayer = new DataLayer(context.getResources(), context);
        return dataLayer.updateChild(context,contentValues,id,name);
    }

    public ChildrenDTO getChildrenDTO(Context context, String childId) {
        dataLayer = new DataLayer(context.getResources(), context);
        Cursor cursor = dataLayer.getChildById(Long.parseLong(childId));
        ChildrenDTO childrenDTO = new ChildrenDTO();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
                childrenDTO = new ChildrenDTO();
                childrenDTO.setId(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_ID)));
                childrenDTO.setName(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME)));
                childrenDTO.setGender(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER)));
                childrenDTO.setAge(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE)));
                childrenDTO.setStatus(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_STATUS)));
                childrenDTO.setSynced(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED)));
                childrenDTO.setCreatedAt(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT)));
                childrenDTO.setSubmittedModules(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SUBMITTED_MODULES)));

            }

        return childrenDTO;
    }



    public List<String> getSurveyCodeDetailsById(Context context, Long farmerId) {
        dataLayer = new DataLayer(context.getResources(), context);
        List<String> visitList = new ArrayList<String>();
        Cursor cursor = dataLayer.getChildVisitDetailsById(farmerId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                visitList.add(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE)));
            }
        }
        return visitList;
    }

    public List<ContentValues> getChildVisitDetailsById(Context context, Long farmerId) {
        dataLayer = new DataLayer(context.getResources(), context);
        List<ContentValues> visitList = new ArrayList<ContentValues>();
        Cursor cursor = dataLayer.getChildVisitDetailsById(farmerId);
        ContentValues contentValues;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                contentValues = new ContentValues();
                contentValues.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_ID,cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildVisitDetails.COLUMN_NAME_ID)));
                contentValues.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID,cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID)));
                contentValues.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED,cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED)));
                contentValues.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE,cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE)));
                contentValues.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_DESCRIPTION,cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_DESCRIPTION)));

                visitList.add(contentValues);
            }
        }
        return visitList;
    }

    public List<ChildrenDTO> getChildDetailsById(Context context, Long farmerId) {
        dataLayer = new DataLayer(context.getResources(), context);
        List<ChildrenDTO> childrenList = new ArrayList<ChildrenDTO>();
        Cursor cursor = dataLayer.getChildDetailsById(farmerId);
        ChildrenDTO childrenDTO;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                childrenDTO = new ChildrenDTO();
                childrenDTO.setId(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_ID)));
                childrenDTO.setServerId(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_ONLINE_ID)));
                childrenDTO.setName(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME)));
                childrenDTO.setGender(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER)));
                childrenDTO.setAge(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE)));
                childrenDTO.setStatus(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_STATUS)));
                childrenDTO.setCreatedAt(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT)));
                childrenDTO.setSynced(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED)));
                childrenDTO.setSubmittedModules(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SUBMITTED_MODULES)));
                childrenList.add(childrenDTO);
            }
        }
        return childrenList;
    }

    public List<ChildModuleDto> getChildSurveyModuleList(Context context, Long id,String childId) {
        dataLayer = new DataLayer(context.getResources(), context);
        List<ChildModuleDto> childModuleDtoList = new ArrayList<ChildModuleDto>();
        Cursor moduleAssignments = dataLayer.getChildModulesByFarmer(id,childId);
        String moduleNameSuffix = Utilities.getLocaleSuffix(context.getResources());

        if (moduleAssignments != null && moduleAssignments.getCount() > 0) {
            try {
                while (moduleAssignments.moveToNext()) {
                    String surveyCode = moduleAssignments.getString(moduleAssignments.getColumnIndex("surveyCode"));
                    String isChildLabour=moduleAssignments.getString(moduleAssignments.getColumnIndex("childLabour"));
                    if ((surveyCode.equalsIgnoreCase(ChildLabourUtils.HOUSEHOLD_SURVEY_CODE) || surveyCode.equalsIgnoreCase(ChildLabourUtils.INSPECTION_SURVEY_CODE) || surveyCode.equalsIgnoreCase(ChildLabourUtils.FOLLOWUP_SURVEY_CODE)) && isChildLabour.equalsIgnoreCase("true")) {
                        ChildModuleDto childModuleDto = new ChildModuleDto();
                        childModuleDto.set_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("_id")));

                        // set language Module & Submodule

                        childModuleDto.setName(moduleAssignments.getString(moduleAssignments.getColumnIndex((moduleAssignments.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix) != -1) ? OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix : OFISDBContract.ModuleEntry.COLUMN_NAME_NAME)));

                        childModuleDto.setStatus_started(moduleAssignments.getString(moduleAssignments.getColumnIndex("status_started")));
                        childModuleDto.setStatus_finished(moduleAssignments.getString(moduleAssignments.getColumnIndex("status_finished")));
                        childModuleDto.setFarmer_module_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("farmer_module_id")));
                        childModuleDto.setCurrent_submodule_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("current_submodule_id")));

                        childModuleDto.setRecorded_lat(moduleAssignments.getString(moduleAssignments.getColumnIndex("recorded_lat")));
                        childModuleDto.setRecorded_lng(moduleAssignments.getString(moduleAssignments.getColumnIndex("recorded_lng")));
                        childModuleDto.setSurveyCode(surveyCode);
                       String startDate=moduleAssignments.getString(moduleAssignments.getColumnIndex("startDate"));
                        if(startDate!=null && !startDate.equalsIgnoreCase("null")) {
                            if (Utilities.currentTimeInMilliSeconds() > Utilities.dateStringToMilliSeconds(startDate)){
                                childModuleDtoList.add(childModuleDto);
                            }
                        }
                    }
                }
            } finally {
                moduleAssignments.close();
            }
        }

            return childModuleDtoList;
        }


    public String getFarmerName(Context context, String farmerId) {
        String farmerName=null;
        dataLayer = new DataLayer(context.getResources(), context);
        Cursor  farmer=dataLayer.getFarmerById(Long.valueOf(farmerId));
        if (farmer.getCount() > 0) {
            farmer.moveToFirst();
            farmerName= farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
        }
        return  farmerName;
    }
}
