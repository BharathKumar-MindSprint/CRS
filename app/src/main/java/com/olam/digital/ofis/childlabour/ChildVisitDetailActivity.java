package com.olam.digital.ofis.childlabour;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;


public class ChildVisitDetailActivity  extends BaseActivity implements View.OnClickListener{

    private AppCompatButton saveVisitDetailButton;
    private CheckBox houseHoldCheckBox, inspectionCheckBox, followUpCheckBox;
    private int household=0;
    private int inspection=0;
    private int followup=0;
    private String farmerId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_visit_detail);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        initComponents();
        initEvents();
        getBundleValues();

    }

    private void initEvents() {
        saveVisitDetailButton.setOnClickListener(this);
    }

    private void initComponents() {
        saveVisitDetailButton=findViewById(R.id.saveVisitDetailButton);
        houseHoldCheckBox=findViewById(R.id.householdCheckBox);
        inspectionCheckBox=findViewById(R.id.inspectionCheckBox);
        followUpCheckBox=findViewById(R.id.followupCheckBox);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.saveVisitDetailButton){
            saveVisitDetails();
        }
    }

    private void saveVisitDetails() {
        if(houseHoldCheckBox.isChecked()){
            household=1;
        }
        if(inspectionCheckBox.isChecked()){
            inspection=1;
        }
        if(followUpCheckBox.isChecked()){
            followup=1;
        }

        /*ContentValues contentValues=new ContentValues();
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_NAME_FARMER_ID,farmerId);
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_HOUSEHOLD,household);
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_INSPECTION,inspection);
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_FOLLOWUP,followup);
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_NAME_VISIT_DETAIL_SYNCED,0);

        long rowId= new ChildLabourLogic().insertVisitDetails(this,contentValues);
        if (rowId != -1){
            Toast.makeText(this, getResources().getString(R.string.child_visit_details_inserted),Toast.LENGTH_LONG).show();
            finish();
        }*/
        ContentValues contentValue=new ContentValues();
        contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID,farmerId);
        contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED,0);
        if(household == 1){
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE,1);
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_DESCRIPTION,"HouseHold Survey");
            long hh_rowId= new ChildLabourLogic().insertVisitSurveyDetails(this,contentValue);
            if (hh_rowId != -1){
                Log.e("HH_RESULT",  getResources().getString(R.string.child_visit_details_inserted));
                if(inspection == 0 && followup == 0){
                    Toast.makeText(this, getResources().getString(R.string.child_visit_details_inserted),Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        if(inspection == 1){
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE,2);
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_DESCRIPTION,"Inspection Survey");
            long hh_rowId= new ChildLabourLogic().insertVisitSurveyDetails(this,contentValue);
            if (hh_rowId != -1){
                Log.e("IN_RESULT",  getResources().getString(R.string.child_visit_details_inserted));
                if(followup == 0){
                    Toast.makeText(this, getResources().getString(R.string.child_visit_details_inserted),Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        if(followup == 1){
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE,3);
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_DESCRIPTION,"Followup Survey");
            long hh_rowId= new ChildLabourLogic().insertVisitSurveyDetails(this,contentValue);
            if (hh_rowId != -1){
                Log.e("FU_RESULT",  getResources().getString(R.string.child_visit_details_inserted));
                Toast.makeText(this, getResources().getString(R.string.child_visit_details_inserted),Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    public void getBundleValues() {
        Bundle bundle = getIntent().getExtras();
        farmerId=bundle.getString(ChildLabourUtils.CHILD_FARMER_ID);
    }
}
