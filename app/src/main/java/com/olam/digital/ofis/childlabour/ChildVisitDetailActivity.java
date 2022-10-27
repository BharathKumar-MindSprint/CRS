package com.olam.digital.ofis.childlabour;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;

import java.sql.Timestamp;
import java.util.List;

// Handles the child visit details in screen.
public class ChildVisitDetailActivity  extends BaseActivity implements View.OnClickListener{

    private final String MATOMOTAG = this.getClass().getSimpleName();
    private AppCompatButton saveVisitDetailButton;
    private CheckBox houseHoldCheckBox, inspectionCheckBox, followUpCheckBox;
    private int household=0;
    private int inspection=0;
    private int followup=0;
    private String farmerId;
    private boolean b_household = true;
    private boolean b_inspection = true;
    private String farmerName;
    private String name="";

    private Location mCurrentLocation=null;
    GPSListener gpsListener=null;
    private GPSTracker gpsTracker=null;

    private static final int REQUEST_CHECK_SETTINGS = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_visit_detail);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initComponents();
        initEvents();
        getBundleValues();
        if (farmerId!=null){
            name=new ChildLabourLogic().getFarmerName(this,farmerId);
            farmerName=getResources().getString(R.string.farmer)+": "+name;

        }
        getSubmittedSurveyDetails();

        gpsListener=new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location!=null){
                    mCurrentLocation=location;
                }
            }
        };
        gpsTracker=new GPSTracker(this,gpsListener);
     //   gpsTracker.getDeviceLocation();
//        if (gpsTracker.isGpsEnabled()){
            gpsTracker.startLocationUpdateInitially();
 //       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) return;

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == -1) {
                    gpsTracker.getDeviceLocation();
                }
                break;
            default:
        }
    }


    // method to collect the submitted child module list
    private void getSubmittedSurveyDetails() {
        List<String>  submittedModuleCodes = new ChildLabourLogic().getAllChildSubmittedModulesDetails(this, Long.valueOf(farmerId));
        if (submittedModuleCodes.contains(ChildLabourUtils.HOUSEHOLD_SURVEY_CODE)){
            houseHoldCheckBox.setEnabled(false);
            b_household = false;
        }
        if (submittedModuleCodes.contains(ChildLabourUtils.INSPECTION_SURVEY_CODE)){
            inspectionCheckBox.setEnabled(false);
            b_inspection=false;
        }
        if (submittedModuleCodes.contains(ChildLabourUtils.FOLLOWUP_SURVEY_CODE)){
            followUpCheckBox.setEnabled(false);
        }

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
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SAVE_VISIT_DETAIL,  ScreenNameConstants.BUTTON_CLICKED);
            saveVisitDetails();
        }
    }

    // method to save the child survey visit details
    private void saveVisitDetails() {
        if (mCurrentLocation!=null){}else {
            if (mCurrentLocation == null || !gpsTracker.isGpsEnabled()) {
                gpsTracker.getDeviceLocation();
                return;
            }
        }

        if(houseHoldCheckBox.isChecked()){
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.CHECKBOX_HOUSEHOLD,  ScreenNameConstants.CHECKBOX_CHECKED);
            household=1;
        }
        if(inspectionCheckBox.isChecked()){
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.CHECKBOX_INSPECTION,  ScreenNameConstants.CHECKBOX_CHECKED);
            inspection=1;
        }
        if(followUpCheckBox.isChecked()){
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.CHECKBOX_FOLLOWUP,  ScreenNameConstants.CHECKBOX_CHECKED);
            followup=1;
        }
        if(! b_household){
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.CHECKBOX_HOUSEHOLD,  ScreenNameConstants.CHECKBOX_UNCHECKED);
            household = 0;
        }
        if(! b_inspection){
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.CHECKBOX_INSPECTION,  ScreenNameConstants.CHECKBOX_UNCHECKED);
            inspection = 0;
        }
        long value =  new ChildLabourLogic().deleteVisitDetails(this, farmerId);
        Log.e("IsValueDeleted? ", String.valueOf(value));
        long val =  new ChildLabourLogic().deleteChildVisitDetails(this, farmerId);
        Log.e("IsValueDeleted? ", String.valueOf(val));

        ContentValues contentValue=new ContentValues();
        contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID,farmerId);
        contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED,0);
        if(household == 1){
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE,1);
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_DESCRIPTION,"HouseHold Survey");
            long hh_rowId= new ChildLabourLogic().insertVisitSurveyDetails(this,contentValue,farmerName, getResources().getString(R.string.household));

            if (hh_rowId != -1){
                collectGPSPoint(GPSConstants.MODULE_CAPTURE_HOUSEHOLD_VISIT,mCurrentLocation,farmerId,"HouseHold Survey Visit");
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
            long hh_rowId= new ChildLabourLogic().insertVisitSurveyDetails(this,contentValue,farmerName,getResources().getString(R.string.inspection));
            if (hh_rowId != -1){
                Log.e("IN_RESULT",  getResources().getString(R.string.child_visit_details_inserted));
                collectGPSPoint(GPSConstants.MODULE_CAPTURE_INSPECTION_VISIT,mCurrentLocation,farmerId,"Inspection Survey Visit");
                if(followup == 0){
                    Toast.makeText(this, getResources().getString(R.string.child_visit_details_inserted),Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        if(followup == 1){
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE,3);
            contentValue.put(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_DESCRIPTION,"Followup Survey");
            long hh_rowId= new ChildLabourLogic().insertVisitSurveyDetails(this,contentValue,farmerName,getResources().getString(R.string.followup));
            if (hh_rowId != -1){
                collectGPSPoint(GPSConstants.MODULE_CAPTURE_FOLLOWUP_VISIT,mCurrentLocation,farmerId,"Followup Survey Visit");
                Log.e("FU_RESULT",  getResources().getString(R.string.child_visit_details_inserted));
                Toast.makeText(this, getResources().getString(R.string.child_visit_details_inserted),Toast.LENGTH_LONG).show();
                finish();
            }
        }

        if(household == 0 && inspection == 0){
            finish();
        }

    }

    private void collectGPSPoint(String module,Location location,String farmerId,String visitDetail) {

        TrackDetailsDTO trackDetailsDTO=new TrackDetailsDTO();
        trackDetailsDTO.setModule(module);
        trackDetailsDTO.setLatitude(String.valueOf(location.getLatitude()));
        trackDetailsDTO.setLongitude(String.valueOf(location.getLongitude()));
        trackDetailsDTO.setAccuracy(String.valueOf(location.getAccuracy()));
        trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
        trackDetailsDTO.setFarmerId(String.valueOf(farmerId));
        trackDetailsDTO.setFarmerName(name);
        // set visit detail in new column for further
        trackDetailsDTO.setSurveyName(visitDetail);

        ContentValues geoContentValues=new GPSUtils().getGeoContentValues(trackDetailsDTO);
        Long newTrackId= new GpsTrackerLogic().insertGeoDataDetails(this,geoContentValues);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            gpsTracker.stopLocationUpdates();
        }catch (Exception e){}

    }

    public void getBundleValues() {

        Bundle bundle = getIntent().getExtras();
        farmerId = bundle.getString(ChildLabourUtils.CHILD_FARMER_ID);
        ChildLabourLogic childLabourLogic = new ChildLabourLogic();
        /*List<String> scList = childLabourLogic.getSurveyCodeDetailsById(this, Long.valueOf(farmerId));
        if (scList.size() > 0) {
            for (String value : scList) {
                if (value.equalsIgnoreCase("1")) {
                    houseHoldCheckBox.setChecked(true);
                } else if (value.equalsIgnoreCase("2")) {
                    inspectionCheckBox.setChecked(true);
                }
            }

        }*/
        List<ContentValues> scList = childLabourLogic.getChildVisitDetailsById(this, Long.valueOf(farmerId));
        if (scList.size() > 0) {
            for (ContentValues value : scList) {
                if (value.getAsString(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE).equalsIgnoreCase("1")) {
                    houseHoldCheckBox.setChecked(true);
                    if(value.getAsString(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED).equalsIgnoreCase("1")){
                        houseHoldCheckBox.setEnabled(false);
                        b_household = false;
                    }else {
                        houseHoldCheckBox.setEnabled(true);
                    }
                } else if (value.getAsString(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE).equalsIgnoreCase("2")) {
                    inspectionCheckBox.setChecked(true);
                    if(value.getAsString(OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED).equalsIgnoreCase("1")){
                        inspectionCheckBox.setEnabled(false);
                        b_inspection = false;
                    }else {
                        inspectionCheckBox.setEnabled(true);
                    }
                }


            }
        }




    }


    @Override
    protected void onResume() {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_REGISTER_CHILD_ACTIVITY, ScreenNameConstants.SCREEN_CHILD_VISIT_DETAIL_ACTIVITY, ScreenNameConstants.SCREEN_CHILD_VISIT_DETAIL);
        getBundleValues();
        getSubmittedSurveyDetails();

        try {
            //  hide the household for Turkey/Hazelnut
            Cursor mCursor= dl.getFarmerById(Long.valueOf(farmerId));
            String countryId = null;
            String productId = null;
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                countryId = mCursor.getString(mCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID));
                productId = mCursor.getString(mCursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID));
                if (countryId!=null){
                    if (countryId.equalsIgnoreCase("32") ||
                            (countryId.equalsIgnoreCase("16") && productId.equalsIgnoreCase("6"))){
                        houseHoldCheckBox.setEnabled(false);
                        houseHoldCheckBox.setBackgroundColor(ContextCompat.getColor(this, R.color.clmrs_bg_gray));
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
