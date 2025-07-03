package com.olam.digital.ofis.downloadsurvey.dto;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto.FarmerPolygonDto;

import java.util.ArrayList;
import java.util.List;

public class ModuleCursorToDTO {
    public static List<ModuleDTO> cursorToDTOListModule(Cursor moduleList, SQLiteDatabase db) {

        List<ModuleDTO> modulesList = new ArrayList<>();

        if (moduleList != null && moduleList.moveToFirst()) {
            do {
                ModuleDTO.Builder moduleDTOBuilder = new ModuleDTO.Builder();
                String moduleType = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULEASSIGNMENT_TYPE));
                String assignmentId = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULEASSIGNMENT_ID));
                String moduleId = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_ID));
                String moduleName = moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME));

                moduleDTOBuilder.setName_lo(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_LO)))
                .setName_es(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_ES)))
                .setName_fr(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_FR)))
                .setName_id(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_ID)))
                .setName_pt(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_PT)))
                .setName_th(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_TH)))
                .setName_tr(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_TR)))
                .setName_vi(moduleList.getString(moduleList.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_VI)))
                .setModule_id(moduleId)
                .setName(moduleName)
                .setModuleassignment_type(moduleType)
                .setModuleassignment_id(assignmentId)
                .setShowType(Utilities.QWR);

                if (moduleType.equalsIgnoreCase("Country") && isValueExist(assignmentId, OFISDBContract.CountriesEntry.TABLE_NAME, db)) {
                    modulesList.add(moduleDTOBuilder.build());
                } else if (moduleType.equalsIgnoreCase("Region") && isValueExist(assignmentId, OFISDBContract.RegionsEntry.TABLE_NAME, db)) {
                    modulesList.add(moduleDTOBuilder.build());
                } else if (moduleType.equalsIgnoreCase("District") && isValueExist(assignmentId, OFISDBContract.DistrictsEntry.TABLE_NAME, db)) {
                    modulesList.add(moduleDTOBuilder.build());
                } else if (moduleType.equalsIgnoreCase("Place") && isValueExist(assignmentId, OFISDBContract.PlacesEntry.TABLE_NAME, db)) {
                    modulesList.add(moduleDTOBuilder.build());
                } else if (moduleType.equalsIgnoreCase("Farmer Group") && isValueExist(assignmentId, OFISDBContract.FarmerGroupsEntry.TABLE_NAME, db)) {
                    modulesList.add(moduleDTOBuilder.build());
                } else if (moduleType.equalsIgnoreCase("Section") && isValueExist(assignmentId, OFISDBContract.SectionsEntry.TABLE_NAME, db)) {
                    modulesList.add(moduleDTOBuilder.build());
                } else if (moduleType.equalsIgnoreCase("Product") && isValueExist(assignmentId, OFISDBContract.ProductsEntry.TABLE_NAME, db)) {
                    modulesList.add(moduleDTOBuilder.build());
                }

            } while (moduleList.moveToNext());
        }
        if (moduleList != null) {
            moduleList.close();
        }
        return modulesList;
    }

    public static boolean isValueExist(String id, String table, SQLiteDatabase db) {
        Cursor cursor = db.query(table, new String[]{"*"}, "id = ?",
                new String[]{id}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public static List<FarmerPolygonDto> cursorToDtoFarmerList(Cursor cursor) {
        List<FarmerPolygonDto> farmerList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                FarmerPolygonDto.Builder farmerListDto = new FarmerPolygonDto.Builder();
                farmerListDto.setId(cursor.getLong(cursor.getColumnIndexOrThrow("_id")))
                        .setName(cursor.getString(cursor.getColumnIndexOrThrow(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)))
                        .setOlamFarmerId(cursor.getString(cursor.getColumnIndexOrThrow(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));

                farmerList.add(farmerListDto.build());
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return farmerList;
    }
}
