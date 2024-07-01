package com.olam.digital.ofis.downloadsurvey;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
// Handles and shows the child list in the screen before taking the survey

public class ViewPlotSurveyActivity extends BaseActivity {

//    private RecyclerView surveyChildLabourRecycleView;
//    private PlotSurveyRecyclerAdapter childSurveyRecyclerAdapter;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_plot_survey);
        initComponents();
        try {
            farmerID=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
        }catch (Exception e){e.printStackTrace();}
        getChildDetailsById();
        try {
            populateSurveyList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DynamicQuestions.farmerGeo=dl.getFarmerInfo(this,farmerID);
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
                    roleId,multiFarmId);
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
                    /*Toast.makeText(
                            getApplicationContext(),
                            expandableListTitle.get(groupPosition)
                                    + " -> "
                                    + expandableListDetail.get(
                                    expandableListTitle.get(groupPosition)).get(
                                    childPosition), Toast.LENGTH_SHORT
                    ).show();*/
//                    TextView txtView=view.findViewById(R.id.moduleListModuleName);
//                    moduleName=txtView.getText().toString();
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

                        Intent intent = new Intent(ViewPlotSurveyActivity.this, CompleteModuleActivity.class);
                        intent.putExtras(bundle);
                        setRequiredData(intent, childModuleDto, plotDTO);
                        // show the list of surveys
                        if (DynamicQuestions.getInstance() != null) {
                            DynamicQuestions.clearInstance();
                        }
                        startActivity(intent);
                    }
                    return false;
                }
            });

           /* try {
                childSurveyRecyclerAdapter = new PlotSurveyRecyclerAdapter(childModuleDtoList,this,modulesArray,farmerDTO);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            surveyChildLabourRecycleView.setLayoutManager(reLayoutManager);
            surveyChildLabourRecycleView.setItemAnimator(new DefaultItemAnimator());
            ((LinearLayoutManager) reLayoutManager).setReverseLayout(false);
            surveyChildLabourRecycleView.setAdapter(childSurveyRecyclerAdapter);

            childSurveyRecyclerAdapter.setOnItemSurveyClickListener(new OnItemSurveyListener() {
                @Override
                public void onChildClick(View view, int position, ChildModuleDto childModuleDto) {

                    TextView txtView=view.findViewById(R.id.surveyName);
                    moduleName=txtView.getText().toString();
                    Bundle bundle=new Bundle();
                    bundle.putString(ChildLabourUtils.CHILD_FARMER_ID,String.valueOf(farmerID));
                    bundle.putSerializable("childModule", childModuleDto);
                    bundle.putSerializable("farmerDTO", farmerDTO);

                    Intent intent=new Intent(ViewPlotSurveyActivity.this, CompleteModuleActivity.class);
                    intent.putExtras(bundle);
                    setRequiredData(intent,childModuleDto);
                    // show the list of surveys
                    if (DynamicQuestions.getInstance() != null) {
                        DynamicQuestions.clearInstance();
                    }
                    startActivity(intent);
                }
            });*/


        }

    }

    private LinkedHashMap<PlotDTO, List<ChildModuleDto>> loadListWithPlots(List<ChildModuleDto> childModuleDtoList) {
        LinkedHashMap<PlotDTO, List<ChildModuleDto>> hmModuleList = new LinkedHashMap<PlotDTO, List<ChildModuleDto>>();
        try {
            ArrayList<PlotDTO> plotList = dl.fetchSyncedPlotIds(String.valueOf(farmerID));
            for (PlotDTO plotDTO : plotList) {
                hmModuleList.put(plotDTO,childModuleDtoList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

       return hmModuleList;
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
        Cursor moduleAssignments = dl.getFarmerModules(farmerID,farmSurvey);
        String moduleNameSuffix = Utilities.getLocaleSuffix(this.getResources());

        if (moduleAssignments != null && moduleAssignments.getCount() > 0) {
            try {
                while (moduleAssignments.moveToNext()) {
                    String isPlotSurvey=moduleAssignments.getString(moduleAssignments.getColumnIndex("isPlotSurvey"));
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
}
