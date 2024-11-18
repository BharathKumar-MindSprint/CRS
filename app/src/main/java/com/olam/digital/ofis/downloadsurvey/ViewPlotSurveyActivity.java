package com.olam.digital.ofis.downloadsurvey;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.CompleteModuleActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.downloadsurvey.adapter.CustomExpandableListAdapter;
import com.olam.digital.ofis.downloadsurvey.dto.FarmerDTO;
import com.olam.digital.ofis.downloadsurvey.dto.PlotDTO;
import com.olam.digital.ofis.multifarm.MultiFarmUtils;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.FollowUp.FollowUpRecyclerAdapter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
// Handles and shows the child list in the screen before taking the survey

public class ViewPlotSurveyActivity extends BaseActivity {

    private FarmerDTO farmerDTO;
    public static Long farmerID;

    private String moduleName;
    private List<ChildModuleDto> childModuleDtoList;
//    TextView childNameTv;

    private static final String FARMER_ID = "FARMER_ID";
    private static final String MODULE_ID = "MODULE_ID";
    private static final String FARMER_MODULE_ID = "FARMER_MODULE_ID";
    private static final String SUB_MODULE_ID = "SUB_MODULE";
    private static final String FARM_ID = "FARM_ID";
    private static final String STATUS_STARTED = "STATUS_STARTED";
    private static final String STATUS_FINISHED = "STATUS_FINISHED";
    private static final String OLAM_FARMER_ID = "OLAM_FARMER_ID";
    private static final String MODULE_NAME = "MODULE_NAME";
    private static final String RECORDED_LAT = "RECORDED_LAT";
    private static final String RECORDED_LNG = "RECORDED_LNG";
    private static final String FARMER_NAME = "FARMER_NAME";
    private static final String CHILD_ID="CHILD_ID";
    private static final String VIEW_MODULE = "VIEW_MODULE";
    private static final String ISPLOTSURVEY = "ISPLOTSURVEY";
    private static final String PLOTID = "PLOTID";

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<PlotDTO> expandableListTitle;
    LinkedHashMap<PlotDTO, List<ChildModuleDto>> expandableListDetail;
    public static boolean farmSurvey;
    String multiFarmId=null;

    private boolean isDiagnosticsPlotSurvey;
    OFISRoomDB db;
    String dmSurveyName;
    String  dmSubTitle;
    String followUpCount;
    String moduleID;

    private Cursor farmer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_plot_survey);
        initComponents();
        db = OFISRoomDB.getDbInstance(context);
        try {
            farmerID=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
            isDiagnosticsPlotSurvey = getIntent().getBooleanExtra(res.getString(R.string.dm_plot_survey), false);
        }catch (Exception e){e.printStackTrace();}
        getChildDetailsById();
        try {
            populateSurveyList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DynamicQuestions.farmerGeo=dl.getFarmerInfo(this,farmerID);

        if(isDiagnosticsPlotSurvey) {
            setTitle(R.string.dm_plot_survey);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // method to get the child details by child Id
    private void getChildDetailsById() {
        if (farmerID != null) {
            farmerDTO = dl.getFarmerDTO(String.valueOf(farmerID));
        }

    }

    // method to populate the survey list
    private void populateSurveyList() throws JSONException {
        childModuleDtoList = getChildSurveyModuleList();
//        Collections.sort(childModuleDtoList, new SortbySurveyCode());
        JSONObject submittedModules = null;
        JSONArray modulesArray=null;
        try {
            String submittedModulesJSON = farmerDTO.getSubmittedModules();
            String Samplestr = String.valueOf("{\"modules\":null}");
            if (Samplestr.equals(submittedModulesJSON)) {
                submittedModulesJSON = submittedModulesJSON.replaceAll("\"modules\":null", "");
            }
            submittedModules = (!submittedModulesJSON.equals("null")) ? new JSONObject(submittedModulesJSON) : new JSONObject();

//            submittedModules=new JSONObject(submittedModulesJSON);
//            modulesArray= submittedModules.getJSONArray("modules");

        }catch (Exception e){e.printStackTrace();}
        int roleId = 0;
        if (Utilities.getMultifarmPref(this, MultiFarmUtils.IS_MULTI_FARM_ENUMERATOR)) {
            //Old
            //  roleId = dl.getMultiFarmRoleInfoByFarmerId(String.valueOf(farmerId));
            // New
            String roleIdStr = dl.getMultiFarmRoleInfoByFarmerId1(String.valueOf(farmerID));
            if (roleIdStr != null) {
                roleId = Integer.parseInt(roleIdStr);
            }
            multiFarmId=dl.getMultifarmfarmId(farmerID);
            if(multiFarmId!=null){
                farmSurvey=true;
            }else{
                farmSurvey=false;
            }

        }else{
            farmSurvey=false;
        }
        if (childModuleDtoList != null && childModuleDtoList.size() > 0) {

//            expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
            expandableListDetail = loadListWithPlots(childModuleDtoList);
            expandableListTitle = new ArrayList<PlotDTO>(expandableListDetail.keySet());
            expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail,
                    submittedModules,farmerDTO,getResources(),
                    roleId, multiFarmId);
            expandableListView.setAdapter(expandableListAdapter);

            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                @Override
                public void onGroupExpand(int groupPosition) {
                    /*Toast.makeText(getApplicationContext(),
                            expandableListTitle.get(groupPosition) + " List Expanded.",
                            Toast.LENGTH_SHORT).show();*/
                }
            });

            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                @Override
                public void onGroupCollapse(int groupPosition) {
                    /*Toast.makeText(getApplicationContext(),
                            expandableListTitle.get(groupPosition) + " List Collapsed.",
                            Toast.LENGTH_SHORT).show();*/

                }
            });

            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {

                    TextView enabled = (TextView) v.findViewById(R.id.moduleListEnabled);
                    if (enabled.getText().toString().equalsIgnoreCase("true")) {
                        PlotDTO plotDTO = expandableListTitle.get(groupPosition);
                        ChildModuleDto childModuleDto = expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition);
                        moduleName = childModuleDto.getName();
                        Bundle bundle = new Bundle();
                        bundle.putString(ChildLabourUtils.CHILD_FARMER_ID, String.valueOf(farmerID));
                        bundle.putSerializable("childModule", childModuleDto);
                        bundle.putSerializable("farmerDTO", farmerDTO);

                        if (childModuleDto.getRecorded_lng() != null && childModuleDto.getRecorded_lng().equalsIgnoreCase(res.getString(R.string.dm_plot_survey))) {
                            Constants.MODULE_ID = childModuleDto.getCurrent_submodule_id();
                            Constants.MODULE_NAME = childModuleDto.getName();
                            Constants.FARMER_ID = String.valueOf(farmerID);
                            Constants.FARMER_NAME = farmerDTO.getName();
                            Constants.FARMER_GROUP_ID = String.valueOf(dl.getFarmerGroupById(farmerID));;
                            Constants.PLOT_ID = plotDTO.getPlotId();

                            showPopupDialog(plotDTO, Constants.MODULE_ID, childModuleDto.getAnnual_survey().equalsIgnoreCase("true"));

                        }
                        else {
                            Intent intent = new Intent(ViewPlotSurveyActivity.this, CompleteModuleActivity.class);
                            intent.putExtras(bundle);
                            setRequiredData(intent, childModuleDto, plotDTO);
                            // show the list of surveys
                            if (DynamicQuestions.getInstance() != null) {
                                DynamicQuestions.clearInstance();
                            }
                            startActivity(intent);
                        }
                    }
                    return false;
                }
            });
        }
    }

    private LinkedHashMap<PlotDTO, List<ChildModuleDto>> loadListWithPlots(List<ChildModuleDto> childModuleDtoList) {
        LinkedHashMap<PlotDTO, List<ChildModuleDto>> hmModuleList = new LinkedHashMap<PlotDTO, List<ChildModuleDto>>();
        try {
            ArrayList<PlotDTO> plotList = dl.fetchSyncedPlotIds(String.valueOf(farmerID));
            for (PlotDTO plotDTO : plotList) {
                if (!isDiagnosticsPlotSurvey || isPlotDisplayed(plotDTO)) {
                    hmModuleList.put(plotDTO,childModuleDtoList);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

       return hmModuleList;
    }

    private boolean isPlotDisplayed(PlotDTO plotDTO) {
        return (plotDTO.getCleansedStatus().equalsIgnoreCase("updated") || plotDTO.getCleansedStatus().equalsIgnoreCase("repaired"))  && (plotDTO.isPALegalitySurveyCompleted() || plotDTO.isEUDRSurveyCompleted() || !plotDTO.isPALegalityPlot() || !plotDTO.isDeforestationPlot());
    }


    private List<ChildModuleDto> generateModuleList(ArrayList<PlotDTO> plotList, List<ChildModuleDto> finalModuleDtoList) {
        List<ChildModuleDto> moduleList = new ArrayList<>();
//        for (int j = 0; j < childModuleDtoList.size(); j++) {
        int size = childModuleDtoList.size();
        for (PlotDTO plotDTO : plotList) {
            int i = 0;
            for (int j = 0; j < size; j++) {
                int plotId = plotDTO.getPlotId();
                String plotName = plotDTO.getPlotName();
                ChildModuleDto childModuleDto = finalModuleDtoList.get(i);
                childModuleDto.setPlotId(plotId);
                childModuleDto.setPlotName(plotName);
                finalModuleDtoList.add(childModuleDto);
            }
        }
        return childModuleDtoList;
    }

    public List<ChildModuleDto> getChildSurveyModuleList() {

        List<ChildModuleDto> childModuleDtoList = new ArrayList<ChildModuleDto>();
        Cursor moduleAssignments = null;
        if (!isDiagnosticsPlotSurvey) {
            moduleAssignments = dl.getFarmerModules(farmerID,farmSurvey);
        }
        else {
            moduleAssignments = getDiagnosticPlotSurveyModules();
        }

        String moduleNameSuffix = Utilities.getLocaleSuffix(this.getResources());

        if (moduleAssignments != null && moduleAssignments.getCount() > 0) {
            try {
                while (moduleAssignments.moveToNext()) {
                    String isPlotSurvey = moduleAssignments.getString(moduleAssignments.getColumnIndex("isPlotSurvey"));
                    if (isPlotSurvey.equalsIgnoreCase("true")) {
                        ChildModuleDto childModuleDto = new ChildModuleDto();
                        childModuleDto.set_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("_id")));
                        childModuleDto.setStartDate(moduleAssignments.getString(moduleAssignments.getColumnIndex("startDate")));

                        childModuleDto.setName(moduleAssignments.getString(moduleAssignments.getColumnIndex((moduleAssignments.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix) != -1) ? OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix : OFISDBContract.ModuleEntry.COLUMN_NAME_NAME)));
                        childModuleDto.setSubmodule_name(moduleAssignments.getString(moduleAssignments.getColumnIndex((moduleAssignments.getColumnIndex("submodule_name" + moduleNameSuffix) != -1) ? "submodule_name" + moduleNameSuffix : "submodule_name")));

                        childModuleDto.setStatus_started(moduleAssignments.getString(moduleAssignments.getColumnIndex("status_started")));
                        childModuleDto.setStatus_finished(moduleAssignments.getString(moduleAssignments.getColumnIndex("status_finished")));
                        childModuleDto.setFarmer_module_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("farmer_module_id")));
                        childModuleDto.setCurrent_submodule_id(moduleAssignments.getString(moduleAssignments.getColumnIndex("current_submodule_id")));
                        childModuleDto.setIsFarmSurvey(moduleAssignments.getString(moduleAssignments.getColumnIndex("isFarmSurvey")));
                        childModuleDto.setFkFarmerRoleId(moduleAssignments.getString(moduleAssignments.getColumnIndex("fkFarmerRoleId")));
                        childModuleDto.setDisabled_months(moduleAssignments.getString(moduleAssignments.getColumnIndex("disabled_months")));
                        childModuleDto.setRecurrence_frequency(moduleAssignments.getString(moduleAssignments.getColumnIndex("recurrence_frequency")));
                        childModuleDto.setRecurrence_type(moduleAssignments.getString(moduleAssignments.getColumnIndex("recurrence_type")));
//                        childModuleDto.setPlot_id(moduleAssignments.getInt(moduleAssignments.getColumnIndex("plot_id")));

                        childModuleDto.setRecorded_lat(moduleAssignments.getString(moduleAssignments.getColumnIndex("recorded_lat")));
                        childModuleDto.setRecorded_lng(moduleAssignments.getString(moduleAssignments.getColumnIndex("recorded_lng")));
                        childModuleDto.setIsPlotSurvey(moduleAssignments.getString(moduleAssignments.getColumnIndex("isPlotSurvey")));
                        childModuleDto.setView_module(moduleAssignments.getString(moduleAssignments.getColumnIndex("view_module")));
                        childModuleDto.setSurveyDuration(moduleAssignments.getString(moduleAssignments.getColumnIndex("surveyDuration")));
                        childModuleDto.setAnnual_survey(moduleAssignments.getString(moduleAssignments.getColumnIndex("annual_survey")));
                        childModuleDtoList.add(childModuleDto);
                    }
                }
            } finally {
                moduleAssignments.close();
            }
        }

        return childModuleDtoList;
    }




    // method to set the required collected data from child survey module.
    private void setRequiredData(Intent intent, ChildModuleDto childModuleDto, PlotDTO plotDTO) {
        DataLayer dataLayer=new DataLayer(this.getResources(),this);

        intent.putExtra(FARMER_ID, farmerID);
        intent.putExtra(MODULE_ID, Long.parseLong(childModuleDto.get_id()));
        try {
            String farmerModuleId = dl.getFarmerModuleIdByPlotId(Long.parseLong(childModuleDto.get_id()),farmerID,plotDTO.getPlotId());
            intent.putExtra(FARMER_MODULE_ID, Long.parseLong(farmerModuleId));
//            intent.putExtra(FARMER_MODULE_ID, Long.parseLong(childModuleDto.getFarmer_module_id()));
        }catch (Exception e){}

        try {
            intent.putExtra(SUB_MODULE_ID, Integer.parseInt(childModuleDto.getCurrent_submodule_id()));
        }catch (Exception e){}

        try {
            intent.putExtra(STATUS_STARTED,Integer.parseInt(childModuleDto.getStatus_started()));
        }catch (Exception e){}

        try {
            intent.putExtra(STATUS_FINISHED, Integer.parseInt(childModuleDto.getStatus_finished()));
        }catch (Exception e){}

        intent.putExtra(RECORDED_LAT, childModuleDto.getRecorded_lat());
        intent.putExtra(RECORDED_LNG, childModuleDto.getRecorded_lng());
        intent.putExtra(ISPLOTSURVEY, childModuleDto.getIsPlotSurvey());
        intent.putExtra(VIEW_MODULE, childModuleDto.getView_module());
        intent.putExtra(PLOTID, plotDTO.getPlotId());

        Cursor farmer=dataLayer.getFarmerById(farmerID);
        if(farmer!=null && farmer.getCount()>0){
            farmer.moveToNext();
            try {
                intent.putExtra(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                intent.putExtra(FARM_ID, farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID)));
                intent.putExtra(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
            }finally {
                farmer.close();
            }
        }

        intent.putExtra(MODULE_NAME, moduleName);

    }



    private void initComponents() {
        expandableListView =findViewById(R.id.surveyChildLabourRecycleView);
        /*childNameTv=findViewById(R.id.childnameTv);
        childNameTv.setVisibility(View.GONE);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    public Cursor getDiagnosticPlotSurveyModules() {

        String countryId = "";
        String regionId = "";
        String districtId = "";
        String placeId = "";
        String farmerGroupId = "";
        String sectionId = "";
        String programmeId = "";
        String productId = "";
        String partnerId = "";

        try {
            farmer = dl.getFarmerById(farmerID);
            if (farmer.getCount() > 0) {
                farmer.moveToFirst();
                countryId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID));
                regionId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID));
                districtId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID));
                placeId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID));
                farmerGroupId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID));
                sectionId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID));
                programmeId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID));
                productId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID));
                partnerId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID));
            }


            String[] mcColumns = new String[]{
                    "_id",
                    "name",
                    "name_fr",
                    "name_es",
                    "name_id",
                    "name_pt",
                    "name_tr",
                    "name_lo",
                    "name_vi",
                    "name_th",
                    "annual_survey",
                    "submodule_name",
                    "submodule_name_fr",
                    "submodule_name_es",
                    "submodule_name_id",
                    "submodule_name_pt",
                    "submodule_name_tr",
                    "submodule_name_lo",
                    "submodule_name_vi",
                    "submodule_name_th",
                    "expires",
                    "recurrence_frequency",
                    "recurrence_type",
                    "disabled_months",
                    "startDate",
                    "isFarmSurvey",
                    "isPlotSurvey",
                    "view_module",
                    "fkFarmerRoleId",
                    "farmer_module_id",
                    "status_started",
                    "status_finished",
                    "current_submodule_id",
                    "recorded_lat",
                    "recorded_lng",
                    "surveyDuration"
            };

            MatrixCursor mc = new MatrixCursor(mcColumns);

            int diagnosticTypeId = db.dmMasterDataDao().getDiagnosticPlotTypeId("Plot");
            int EUDRGroundVerificationSurveyId = db.dmMasterDataDao().getDiagnosticPlotTypeId(Utilities.EUDR_GROUND_VERIFICATION_SURVEY);

            List<DmMainModule> dmPlotModuleData = db.dmMainModuleDao().getDmMainModuleByAssignmentsByDiagnosticTypeId(countryId,regionId,farmerGroupId,districtId,sectionId,placeId,partnerId,programmeId,productId,diagnosticTypeId);

            for (int i = 0; i < dmPlotModuleData.size(); i++){
                dmSurveyName = Utilities.getNamesByLanguage(dmPlotModuleData.get(i));
                dmSubTitle = dmPlotModuleData.get(i).assessmentType;
                moduleID = String.valueOf(dmPlotModuleData.get(i).id);
                followUpCount = String.valueOf(dmPlotModuleData.get(i).followUpAttempts);
                String isEUDRGroundVerificationSurvey = String.valueOf(EUDRGroundVerificationSurveyId == dmPlotModuleData.get(i).plotSurveyType);

                mc.addRow(new String[]{
                        "0",
                        dmSurveyName,
                        dmSurveyName,
                        dmSurveyName,
                        dmSurveyName,
                        dmSurveyName,
                        dmSurveyName,
                        dmSurveyName,
                        dmSurveyName,
                        dmSurveyName,
                        isEUDRGroundVerificationSurvey,
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        res.getString(R.string.dm_diognostic_module_subtitle),
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "true",
                        "",
                        "",
                        "",
                        "", "", moduleID,
                        "",
                        res.getString(R.string.dm_plot_survey),""
                });

            }

            return new MergeCursor(new Cursor[]{mc});

        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showPopupDialog(PlotDTO plot, String moduleID, boolean isDeforestationSurvey) {
        Intent intent = new Intent(this, DiagnosticModuleActivity.class);

        boolean isCompleted = false;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.followup_diagnostic_popup);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        ArrayList<String> recyclerViewData = new ArrayList<>();
        recyclerViewData.add(getResources().getString(R.string.existing_module));
        String count = db.dmMainModuleDao().getFollowUpAttemptsVal(moduleID);
        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDmPlotDataByModuleIdAndPlotId(String.valueOf(moduleID), String.valueOf(farmerID), true, plot.getPlotId());

        if(count != null && db.complianceIndicatorDao().hasActionPlan(String.valueOf(moduleID))) {
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();

            String startDateString = db.followUpItemMainTableDao().getFollowUpStartDate(String.valueOf(1), String.valueOf(farmerID));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            boolean followUpDurationDisable = false;
            String followUpDuration = null;


            Date endFollowUpDurationDate = null;
            if (startDateString != null){
                followUpDuration = RecurrenceUtils.calculateEndDate(startDateString, db.dmMainModuleDao().getFollowUpDurationVal(moduleID) , "days");
            }


            try {
                if (followUpDuration != null) {
                    endFollowUpDurationDate = dateFormat.parse(followUpDuration);
                }
            } catch (android.net.ParseException | java.text.ParseException e) {
                e.printStackTrace();
            }

            // creating the followUp for surveys
            List<FollowUpItemMainTable> followupList = db.followUpItemMainTableDao().getFollowupByFarmerId(farmerID, Integer.parseInt(moduleID));
            if(followupList.isEmpty()){
                if(!dmSurveyAnswerTableList.isEmpty()) {
                    recyclerViewData.add(getResources().getString(R.string.follow_up_survey)+" " + 1);
                }
            }else {
                List<Integer> followupAttempts = db.followUpItemMainTableDao().getFollowupByAttempts(farmerID, Integer.parseInt(moduleID));
                for (int i = 1; i <= followupAttempts.size(); i++) {
                    recyclerViewData.add(getResources().getString(R.string.follow_up_survey)+" " + i);
                }

                List<FollowUpItemMainTable> followupUnSyncedItems = db.followUpItemMainTableDao().getFollowupUnSyncedItems(String.valueOf(farmerID), moduleID, false);
                if(followupUnSyncedItems.isEmpty()) {
                    List<FollowUpItemMainTable> followupUnResolvedItems = db.followUpItemMainTableDao().getFollowupUnResolvedItems(String.valueOf(farmerID), moduleID, true, Constants.NOTRESOLVED, followupAttempts.size());
                    if(!followupUnResolvedItems.isEmpty()) {
                        int val = followupAttempts.size() + 1;

                        if (endFollowUpDurationDate!=null){
                            if (currentDate.before(endFollowUpDurationDate)){
                                followUpDurationDisable = true;
                            }
                        }

                        if (!Objects.equals(count, "0") && !Objects.equals(count, null)){
                            if (count.replaceAll(" ", "").equalsIgnoreCase("max")){
                                recyclerViewData.add(getResources().getString(R.string.follow_up_survey)+" " + val);
                                if (followUpDurationDisable){
                                    isCompleted = false;
                                }else {
                                    isCompleted = true;
                                    Toast.makeText(context, "Recurrence Follow-up date crossed", Toast.LENGTH_SHORT).show();
                                }
                            }else if (val < Integer.parseInt(count) + 1 ){

                                if (followUpDurationDisable){
                                    recyclerViewData.add(getResources().getString(R.string.follow_up_survey)+" " + val);
                                    isCompleted = false;

                                }else {
                                    recyclerViewData.add(getResources().getString(R.string.follow_up_survey)+" " + val);
                                    isCompleted = true;
                                    Toast.makeText(context, "Recurrence Follow-up date crossed", Toast.LENGTH_SHORT).show();
                                }

                            }else {
                                if (followUpDurationDisable){
                                    isCompleted = false;

                                }else {
                                    isCompleted = true;
                                    Toast.makeText(context, "Recurrence Follow-up date crossed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            recyclerViewData.add(getResources().getString(R.string.follow_up_survey)+" " + val);
                            if (!followUpDurationDisable){
                                isCompleted = true;
                                Toast.makeText(context, "Recurrence Follow-up date crossed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else{
                        isCompleted = true;
                    }
                }
            }
        }
        else if (count == null || db.complianceIndicatorDao().hasNoActionPlan(String.valueOf(moduleID))){
            if((isDeforestationSurvey && plot.isEUDRSurveyCompleted()) || (!isDeforestationSurvey && plot.isPALegalitySurveyCompleted())) {
                isCompleted = true;
            }
        }

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        FollowUpRecyclerAdapter adapter = new FollowUpRecyclerAdapter(recyclerViewData,getApplicationContext(),0 , intent, isCompleted , moduleID);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        ImageView closeButton = dialog.findViewById(R.id.imageView);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
