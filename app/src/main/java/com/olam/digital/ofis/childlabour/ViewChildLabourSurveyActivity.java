package com.olam.digital.ofis.childlabour;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.adapter.ChildSurveyRecyclerAdapter;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemSurveyListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
// Handles and shows the child list in the screen before taking the survey

public class ViewChildLabourSurveyActivity extends BaseActivity {

    private RecyclerView surveyChildLabourRecycleView;
    private ChildSurveyRecyclerAdapter childSurveyRecyclerAdapter;
    private ChildrenDTO children;
    public static Long farmerID;
    public static Long childID;

    private String moduleName;
    private List<ChildModuleDto> childModuleDtoList;
    private ChildModuleDto followUpModule = null;
    private Integer followUpCount = null;
    TextView childNameTv;

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
    private static final String PLOT_COUNT = "PLOT_COUNT";
    private static final int PERMISSIONS_ACCESS_LOCATION = 6575;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_child_labour_survey);
        initComponents();
        initData();
        getChildDetailsById();
        populateSurveyList();
        DynamicQuestions.farmerGeo=dl.getFarmerInfo(this,farmerID);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // method to get the child details by child Id
    private void getChildDetailsById() {
        if (childID != null) {
            children = new ChildLabourLogic().getChildrenDTO(this, String.valueOf(childID));
        }

        if (children!=null) {
            childNameTv.setText(children.getName());
        }

    }

    private void populateSurveyList() {
        JSONObject moduleDtoJson=null;
        try {
            String moduleDetails=children.getModuleDTO();
            moduleDtoJson=new JSONObject(moduleDetails);
            followUpCount = Integer.valueOf(moduleDtoJson.optString("followUpCount"));

        }catch (Exception e){}

        Object[] childSurveyModules = new ChildLabourLogic().getChildSurveyModuleList(this, farmerID,String.valueOf(children.getId()), followUpCount);
        followUpModule = (ChildModuleDto) childSurveyModules[0];
        childModuleDtoList = (List<ChildModuleDto>) childSurveyModules[1];
        Collections.sort(childModuleDtoList, new SortbySurveyCode());

        JSONObject submittedModules = null;
        JSONArray modulesArray=null;
        try {
            String submittedModulesJSON = children.getSubmittedModules();
            submittedModules=new JSONObject(submittedModulesJSON);
            modulesArray= submittedModules.getJSONArray("modules");

        }catch (Exception e){}

        if (childModuleDtoList != null && !childModuleDtoList.isEmpty()) {
            try {
                childSurveyRecyclerAdapter = new ChildSurveyRecyclerAdapter(childModuleDtoList,this,modulesArray,children,moduleDtoJson, followUpCount);
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
                    ChildModuleDto module = childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.FOLLOWUP_SURVEY_CODE) ? followUpModule : childModuleDto;
                    bundle.putSerializable("childModule", childModuleDto);
                    bundle.putSerializable("children", children);
                    module.setName(childModuleDto.getName());
                    bundle.putSerializable("childModule", module);
                    bundle.putInt(Utilities.FOLLOW_UP_COUNT, followUpCount == null ? 0 : followUpCount);

                    Intent intent=new Intent(ViewChildLabourSurveyActivity.this,ChildLabourCompleteModuleActivity.class);
                    intent.putExtras(bundle);
                    setRequiredData(intent,module);
                    // show the list of surveys
                    if (DynamicQuestions.getInstance() != null) {
                        DynamicQuestions.clearInstance();
                    }
                    startActivity(intent);
                }
            });

        }

    }

    // method to assemble the survey by order
    private void AssembleSurveyByOrder() {
        List<ChildModuleDto> unOrderedSurveyList=childModuleDtoList;
        for (ChildModuleDto childModuleDto : unOrderedSurveyList){
             if (childModuleDto.getSurveyCode().equalsIgnoreCase("01")){
                 childModuleDtoList.set(0,childModuleDto);
             }else if(childModuleDto.getSurveyCode().equalsIgnoreCase("02")){
                 childModuleDtoList.set(1,childModuleDto);
             }else if(childModuleDto.getSurveyCode().equalsIgnoreCase("03")){
                 childModuleDtoList.set(2,childModuleDto);
             }else {
                 childModuleDtoList.add(childModuleDto);
             }
        }

    }

    // Compare the child survey code
    class SortbySurveyCode implements Comparator<ChildModuleDto> {
        // Used for sorting in ascending order of
        // roll number
        @Override
        public int compare(ChildModuleDto child1, ChildModuleDto child2) {
            return child1.getSurveyCode().compareTo(child2.getSurveyCode());
        }
    }


    // method to set the required collected data from child survey module.
    private void setRequiredData(Intent intent,ChildModuleDto childModuleDto) {
        DataLayer dataLayer=new DataLayer(this.getResources(),this);

        intent.putExtra(FARMER_ID, farmerID);
        intent.putExtra(MODULE_ID, Long.parseLong(childModuleDto.get_id()));
        try {
            intent.putExtra(FARMER_MODULE_ID, Long.parseLong(childModuleDto.getFarmer_module_id()));
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
        intent.putExtra(CHILD_ID,children.getId());

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

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            children = (ChildrenDTO) bundle.getSerializable("children");
        }
        try {
            farmerID=Long.parseLong(bundle.getString(ChildLabourUtils.CHILD_FARMER_ID));
        }catch (Exception e){}

        try {
            childID=Long.parseLong(bundle.getString(ChildLabourUtils.CHILD_ID));
        }catch (Exception e){}
    }

    private void initComponents() {
        childNameTv=findViewById(R.id.childnameTv);
        surveyChildLabourRecycleView=findViewById(R.id.surveyChildLabourRecycleView);;
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
