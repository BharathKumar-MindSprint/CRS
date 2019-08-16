package com.olam.digital.ofis.childlabour.businesslogic;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.database.Cursor;
import android.widget.Toast;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;

import java.util.ArrayList;
import java.util.List;


public class ChildLabourLogic {

    DataLayer dataLayer;


    public long insertVisitDetails(Context context, ContentValues contentValues) {
        dataLayer = new DataLayer(context.getResources(), context);
        long newVisitDetaisId = dataLayer.insertChildVisitDetails(contentValues);
        return newVisitDetaisId;
    }

    public long insertChildDetails(Context context, ContentValues contentValues) {
        dataLayer = new DataLayer(context.getResources(), context);
        long newChildId = dataLayer.insertChild(contentValues);
        return newChildId;
    }

    public List<ChildrenDTO> getChildDetailsById(Context context, Long farmerId) {
        dataLayer = new DataLayer(context.getResources(), context);
        List<ChildrenDTO> clildrenList = new ArrayList<ChildrenDTO>();
        Cursor cursor = dataLayer.getChildDetailsById(farmerId);
        ChildrenDTO childrenDTO;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                childrenDTO = new ChildrenDTO();
                childrenDTO.setId(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_ID)));
                childrenDTO.setName(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME)));
                childrenDTO.setGender(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER)));
                childrenDTO.setAge(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE)));
                childrenDTO.setStatus(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_STATUS)));

                childrenDTO.setSubmittedModules(cursor.getString(cursor.getColumnIndex(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SUBMITTED_MODULES)));
                clildrenList.add(childrenDTO);
            }
        }
        return clildrenList;
    }

    public List<ChildModuleDto> getChildSurveyModuleList(Context context, Long id) {
        dataLayer = new DataLayer(context.getResources(), context);
        List<ChildModuleDto> childModuleDtoList = new ArrayList<ChildModuleDto>();
        Cursor moduleAssignments = dataLayer.getChildModulesByFarmer(id);
        String moduleNameSuffix = Utilities.getLocaleSuffix(context.getResources());

        if (moduleAssignments != null && moduleAssignments.getCount() > 0) {
            try {
                while (moduleAssignments.moveToNext()) {
                    String moduleCode = moduleAssignments.getString(moduleAssignments.getColumnIndex("moduleCode"));
                    if (moduleCode.equalsIgnoreCase("01") || moduleCode.equalsIgnoreCase("02") || moduleCode.equalsIgnoreCase("03")) {
                        ChildModuleDto childModuleDto = new ChildModuleDto();
                        childModuleDto.set_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("_id")));
                        childModuleDto.setName(moduleAssignments.getString(moduleAssignments.getColumnIndex("name")));
                        childModuleDto.setStatus_started(moduleAssignments.getString(moduleAssignments.getColumnIndex("status_started")));
                        childModuleDto.setStatus_finished(moduleAssignments.getString(moduleAssignments.getColumnIndex("status_finished")));
                        childModuleDto.setFarmer_module_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("farmer_module_id")));
                        childModuleDto.setCurrent_submodule_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("current_submodule_id")));

                        childModuleDto.setRecorded_lat(moduleAssignments.getString(moduleAssignments.getColumnIndex("recorded_lat")));
                        childModuleDto.setRecorded_lng(moduleAssignments.getString(moduleAssignments.getColumnIndex("recorded_lng")));
                        childModuleDto.setModuleCode(moduleCode);
                        childModuleDtoList.add(childModuleDto);
                    }
                }
            } finally {
                moduleAssignments.close();
            }
        }
//
//            String[] adapterCols = new String[]{
//                    (moduleAssignments.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix) != -1) ? OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix : OFISDBContract.ModuleEntry.COLUMN_NAME_NAME,
//                    OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID,
//                    OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED,
//                    OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED,
//                    "farmer_module_id",
//                    OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LAT,
//                    OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LNG
//            };



            return childModuleDtoList;
        }


}
