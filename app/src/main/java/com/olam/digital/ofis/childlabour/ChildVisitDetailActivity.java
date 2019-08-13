package com.olam.digital.ofis.childlabour;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.Toast;

import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;


public class ChildVisitDetailActivity  extends AppCompatActivity implements View.OnClickListener{

    private AppCompatButton saveVisitDetailButton;
    private AppCompatCheckBox houseHoldCheckBox, inspectionCheckBox, followUpCheckBox;
    private int household=0;
    private int inspection=0;
    private int followup=0;
    private String farmerId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_visit_detail);
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

        ContentValues contentValues=new ContentValues();
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_NAME_FARMER_ID,farmerId);
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_HOUSEHOLD,household);
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_INSPECTION,inspection);
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_FOLLOWUP,followup);
        contentValues.put(OFISDBContract.ChildVisitDetailsEntry.COLUMN_NAME_VISIT_DETAIL_SYNCED,0);

        long rowId= new ChildLabourLogic().insertVisitDetails(this,contentValues);
        if (rowId != -1){
            Toast.makeText(this, getResources().getString(R.string.child_visit_details_inserted),Toast.LENGTH_LONG).show();
            finish();
        }

    }

    public void getBundleValues() {
        Bundle bundle = getIntent().getExtras();
        farmerId=bundle.getString(ChildLabourUtils.CHILD_FARMER_ID);
    }
}
