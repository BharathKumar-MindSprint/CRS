package com.olam.digital.ofis.farmstakeholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;

import java.util.ArrayList;
import java.util.List;

public class StakeholderModuleRepository {
    private DataLayer dl;
    private Context context;

    public StakeholderModuleRepository(DataLayer dl, Context context) {
        this.dl = dl;
        this.context = context;
    }

    public List<ModulesDTO> getStakeholderModules(long farmerId, String stakeHolderId) {
        Cursor farmer = null;
        try {
            farmer = dl.getFlattenedFarmer(farmerId);
            List<ModulesDTO> itemList = new ArrayList<>();

            if (farmer.getCount() > 0) {
                farmer.moveToFirst();

                String countryId = farmer.getString(farmer.getColumnIndex("country_id"));
                String regionId = farmer.getString(farmer.getColumnIndex("region_id"));
                String districtId = farmer.getString(farmer.getColumnIndex("district_id"));
                String placeId = farmer.getString(farmer.getColumnIndex("place_id"));
                String farmerGroupId = farmer.getString(farmer.getColumnIndex("farmergroup_id"));
                String sectionId = farmer.getString(farmer.getColumnIndex("section_id"));
                String programmeId = farmer.getString(farmer.getColumnIndex("programme_id"));
                String productId = farmer.getString(farmer.getColumnIndex("product_id"));
                String partnerId = farmer.getString(farmer.getColumnIndex("partner_id"));

                String modulesQuery = "SELECT distinct modules.id as _id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th,modules.annual_survey, " +
                        "submodules.name as submodule_name, submodules.name_fr as submodule_name_fr, submodules.name_es as submodule_name_es, submodules.name_id as submodule_name_id, submodules.name_pt as submodule_name_pt, submodules.name_tr as submodule_name_tr, submodules.name_lo as submodule_name_lo, submodules.name_vi as submodule_name_vi, submodules.name_th as submodule_name_th, " +
                        "modules.expires, " + "moduleassignments.recurrence_frequency, moduleassignments.recurrence_type, moduleassignments.disabled_months, moduleassignments.startDate, moduleassignments.isFarmSurvey,moduleassignments.view_module,moduleassignments.fkFarmerRoleId,moduleassignments.farmerRoleId, " +
                        " farmer_modules.id as farmer_module_id, farmer_modules.status_started, farmer_modules.status_finished, farmer_modules.current_submodule_id, farmer_modules.recorded_lat, farmer_modules.recorded_lng, moduleassignments.surveyDuration, modules.surveyType FROM moduleassignments " +
                        " INNER JOIN modules ON moduleassignments.module_id = modules.id " +
                        " LEFT OUTER JOIN farmer_modules ON modules.id = farmer_modules.module_id AND " +
                        "(farmer_modules.farmer_id = ? OR farmer_modules.olam_farmer_id = ?) AND farmer_modules.stakeholder_id = ?" +
                        " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_modules.current_submodule_id = submodules.id WHERE " +
                        "moduleassignments.childLabour != 'true' AND " +
                        "moduleassignments.isPlotSurvey != 'true' AND " +
                        "modules.surveyType == '8' AND " +
                        "moduleassignments.country_id = ( case when moduleassignments.country_id = ? then ? else 'null' end ) AND " +
                        "((moduleassignment_type = 'Country' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                        "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?)) group by modules.id";


                String[] modulesQueryValues = new String[]{
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)),
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                        stakeHolderId,
                        countryId,
                        countryId,
                        countryId,
                        regionId,
                        districtId,
                        placeId,
                        farmerGroupId,
                        sectionId,
                        programmeId,
                        productId,
                        partnerId
                };

                @SuppressLint("Recycle")
                Cursor farmerModules = dl.getfarmerModules(modulesQuery, modulesQueryValues);

                if (farmerModules != null && farmerModules.getCount() > 0) {
                    itemList.addAll(ModuleCursorDtoMapper.mapCursorToDTOListModule(farmerModules));
                }
                // Add other items to the list
                /*ModulesDTO.Builder moduleDTOBuilder = new ModulesDTO.Builder();
                moduleDTOBuilder.setModuleName("Polygon Record")
                        .setSurveyType("POLYGON");

                itemList.add(moduleDTOBuilder.build());*/
            }
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (farmer != null) {
                farmer.close();
            }
        }
    }

}


