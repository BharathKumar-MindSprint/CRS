package com.olam.digital.ofis.childlabour;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.adapter.ChildSurveyRecyclerAdapter;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemSurveyListener;

import java.util.List;

public class ViewChildLabourSurveyActivity extends AppCompatActivity{

    private RecyclerView surveyChildLabourRecycleView;
    private ChildSurveyRecyclerAdapter childSurveyRecyclerAdapter;
    private ChildrenDTO children;
    private Long farmerID;
    private String moduleName;
    private List<ChildModuleDto> childModuleDtoList;

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
    private static final String PLOT_COUNT = "PLOT_COUNT";
    private static final int PERMISSIONS_ACCESS_LOCATION = 6575;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_child_labour_survey);
        initComponents();
        initData();
        populateSurveyList();

    }

    private void populateSurveyList() {
        childModuleDtoList=   new ChildLabourLogic().getChildSurveyModuleList(this,farmerID);
        if(childModuleDtoList!=null && childModuleDtoList.size()>0){
            childSurveyRecyclerAdapter = new ChildSurveyRecyclerAdapter(childModuleDtoList,this);
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            surveyChildLabourRecycleView.setLayoutManager(reLayoutManager);
            surveyChildLabourRecycleView.setItemAnimator(new DefaultItemAnimator());
            ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
            surveyChildLabourRecycleView.setAdapter(childSurveyRecyclerAdapter);

            childSurveyRecyclerAdapter.setOnItemSurveyClickListener(new OnItemSurveyListener() {
                @Override
                public void onChildClick(View view, int position, ChildModuleDto childModuleDto) {

                    TextView txtView=view.findViewById(R.id.surveyName);
                    moduleName=txtView.getText().toString();
                    Bundle bundle=new Bundle();
                    bundle.putString(ChildLabourUtils.CHILD_FARMER_ID,String.valueOf(farmerID));
                    bundle.putSerializable("childModule", childModuleDto);
                    bundle.putSerializable("children", children);

                    Intent intent=new Intent(ViewChildLabourSurveyActivity.this,ChildLabourCompleteModuleActivity.class);
                     intent.putExtras(bundle);
                    setRequiredData(intent,childModuleDto);
                    // show the list of surveys
                    if (DynamicQuestions.getInstance() != null) {
                        DynamicQuestions.clearInstance();
                    }
                    startActivity(intent);
                }
            });

        }

    }

    private void setRequiredData(Intent intent,ChildModuleDto childModuleDto) {
        DataLayer dataLayer=new DataLayer(this.getResources(),this);

        intent.putExtra(FARMER_ID, farmerID);
        intent.putExtra(MODULE_ID, Long.parseLong(childModuleDto.get_id()));
        intent.putExtra(FARMER_MODULE_ID, childModuleDto.getFarmer_module_id());
        intent.putExtra(SUB_MODULE_ID, childModuleDto.getCurrent_submodule_id());
        intent.putExtra(STATUS_STARTED,childModuleDto.getStatus_started());
        intent.putExtra(STATUS_FINISHED, childModuleDto.getStatus_finished());
        intent.putExtra(RECORDED_LAT, childModuleDto.getRecorded_lat());
        intent.putExtra(RECORDED_LNG, childModuleDto.getRecorded_lng());

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

//        intent.putExtra(FARMER_MODULE_ID, (String.valueOf(farmerModuleIdTv.getText()).equals("")) ? 0 : Long.parseLong(String.valueOf(farmerModuleIdTv.getText())));
//        intent.putExtra(SUB_MODULE_ID, (String.valueOf(subModuleTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(subModuleTv.getText())));
//        intent.putExtra(STATUS_STARTED, (String.valueOf(statusStartedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusStartedTv.getText())));
//        intent.putExtra(STATUS_FINISHED, (String.valueOf(statusFinishedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusFinishedTv.getText())));
//        intent.putExtra(RECORDED_LAT, (String.valueOf(farmerModuleLatTv.getText()).equals("")) ? "" : String.valueOf(farmerModuleLatTv.getText()));
//        intent.putExtra(RECORDED_LNG, (String.valueOf(farmerModuleLatTv.getText()).equals("")) ? "" : String.valueOf(farmerModuleLngTv.getText()));


    }

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            children = (ChildrenDTO) bundle.getSerializable("children");
        }
        try {
            farmerID=Long.parseLong(bundle.getString(ChildLabourUtils.CHILD_FARMER_ID));
        }catch (Exception e){}
    }

    private void initComponents() {
        surveyChildLabourRecycleView=findViewById(R.id.surveyChildLabourRecycleView);;
    }
}
