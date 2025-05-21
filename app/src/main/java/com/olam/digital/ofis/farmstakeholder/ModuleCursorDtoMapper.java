package com.olam.digital.ofis.farmstakeholder;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class ModuleCursorDtoMapper {
    public static List<ModulesDTO> mapCursorToDTOListModule(Cursor cursor) {
        List<ModulesDTO> modulesList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ModulesDTO.Builder moduleDTOBuilder = new ModulesDTO.Builder();
                moduleDTOBuilder.setId(cursor.getLong(cursor.getColumnIndexOrThrow("_id")))
                        .setModuleName(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                        .setModuleNameFr(cursor.getString(cursor.getColumnIndexOrThrow("name_fr")))
                        .setModuleNameEs(cursor.getString(cursor.getColumnIndexOrThrow("name_es")))
                        .setModuleNameId(cursor.getString(cursor.getColumnIndexOrThrow("name_id")))
                        .setModuleNamePt(cursor.getString(cursor.getColumnIndexOrThrow("name_pt")))
                        .setModuleNameTr(cursor.getString(cursor.getColumnIndexOrThrow("name_tr")))
                        .setModuleNameLo(cursor.getString(cursor.getColumnIndexOrThrow("name_lo")))
                        .setModuleNameVi(cursor.getString(cursor.getColumnIndexOrThrow("name_vi")))
                        .setModuleNameTh(cursor.getString(cursor.getColumnIndexOrThrow("name_th")))
                        .setModuleAnnualSurvey(cursor.getString(cursor.getColumnIndexOrThrow("annual_survey")))
                        .setSubmoduleName(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name")))
                        .setSubmoduleNameFr(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name_fr")))
                        .setSubmoduleNameEs(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name_es")))
                        .setSubmoduleNameId(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name_id")))
                        .setSubmoduleNamePt(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name_pt")))
                        .setSubmoduleNameTr(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name_tr")))
                        .setSubmoduleNameLo(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name_lo")))
                        .setSubmoduleNameVi(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name_vi")))
                        .setSubmoduleNameTh(cursor.getString(cursor.getColumnIndexOrThrow("submodule_name_th")))
                        .setExpires(cursor.getString(cursor.getColumnIndexOrThrow("expires")))
                        .setRecurrenceFrequency(cursor.getString(cursor.getColumnIndexOrThrow("recurrence_frequency")))
                        .setRecurrenceType(cursor.getString(cursor.getColumnIndexOrThrow("recurrence_type")))
                        .setDisabledMonths(cursor.getString(cursor.getColumnIndexOrThrow("disabled_months")))
                        .setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("startDate")))
                        .setIsFarmSurvey(cursor.getString(cursor.getColumnIndexOrThrow("isFarmSurvey")))
                        .setViewModule(cursor.getString(cursor.getColumnIndexOrThrow("view_module")))
                        .setFkFarmerRoleId(cursor.getString(cursor.getColumnIndexOrThrow("fkFarmerRoleId")))
                        .setFarmerRoleId(cursor.getString(cursor.getColumnIndexOrThrow("farmerRoleId")))
                        .setFarmerModuleId(cursor.getString(cursor.getColumnIndexOrThrow("farmer_module_id")))
                        .setStatusStarted(cursor.getString(cursor.getColumnIndexOrThrow("status_started")))
                        .setStatusFinished(cursor.getString(cursor.getColumnIndexOrThrow("status_finished")))
                        .setCurrentSubmoduleId(cursor.getString(cursor.getColumnIndexOrThrow("current_submodule_id")))
                        .setRecordedLat(cursor.getString(cursor.getColumnIndexOrThrow("recorded_lat")))
                        .setRecordedLng(cursor.getString(cursor.getColumnIndexOrThrow("recorded_lng")))
                        .setSurveyDuration(cursor.getString(cursor.getColumnIndexOrThrow("surveyDuration")))
                        .setSurveyType(cursor.getString(cursor.getColumnIndexOrThrow("surveyType")));

                modulesList.add(moduleDTOBuilder.build());
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return modulesList;
    }
}
