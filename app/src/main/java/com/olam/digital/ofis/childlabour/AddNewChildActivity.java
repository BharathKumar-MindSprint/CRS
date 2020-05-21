package com.olam.digital.ofis.childlabour;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.recordpolygon.Utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddNewChildActivity extends BaseActivity implements  View.OnClickListener{

    private final String MATOMOTAG = this.getClass().getSimpleName();
    private EditText fullnameEditText;
    private Button maleButton, femaleButton;
    private Spinner ageSpinner;
    private Button addNewChildButton;
    private String gender;
    private String age;
    private String farmerName;
    private ArrayList<String> ageList;
    DataLayer dataLayer;
    String farmerId;
    String action;
    String childId;
    ChildrenDTO childrenDTO;
    ArrayAdapter<String> adapter;
    private long lastClickTime = 0;

    private Location mCurrentLocation=null;
    GPSListener gpsListener=null;
    private GPSTracker gpsTracker=null;

    private static final int REQUEST_CHECK_SETTINGS = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_child);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dataLayer = new DataLayer(getResources(), this);
        initComponents();
        initEvents();
        setAgeSpinner();
        getBundleValues();
        if(farmerId!=null){
            farmerName=new ChildLabourLogic().getFarmerName(this,farmerId);
        }
        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.SPINNER_AGE,  ScreenNameConstants.SPINNER_CHANGED);
                age=ageList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        gpsListener=new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location!=null){
                    mCurrentLocation=location;
                }
            }
        };
        gpsTracker=new GPSTracker(this,gpsListener);
       // gpsTracker.getDeviceLocation();
//        if (gpsTracker.isGpsEnabled()){
            gpsTracker.startLocationUpdateInitially();
 //       }


    }

    private void initEvents() {
        femaleButton.setOnClickListener(this);
        maleButton.setOnClickListener(this);
        addNewChildButton.setOnClickListener(this);
        maleButton.performClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_REGISTER_CHILD_ACTIVITY, ScreenNameConstants.SCREEN_ADD_NEW_CHILD_ACTIVITY, ScreenNameConstants.SCREEN_ADD_NEW_CHILD);
    }

    private void initComponents() {
        fullnameEditText=findViewById(R.id.fullnameEditText);
        fullnameEditText.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
        fullnameEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        maleButton=findViewById(R.id.maleButton);
        femaleButton=findViewById(R.id.femaleButton);
        ageSpinner=findViewById(R.id.ageSpinner);
        addNewChildButton=findViewById(R.id.addNewChildButton);
        TextView titlename = findViewById(R.id.title_name);
        TextView titleage = findViewById(R.id.title_age);
        TextView titlegender = findViewById(R.id.title_gender);
        titlename.setText(Html.fromHtml(titlename.getText().toString() + "&nbsp;<sup><font color='red'>*</font></sup>"), TextView.BufferType.SPANNABLE);
        titleage.setText(Html.fromHtml(titleage.getText().toString() + "&nbsp;<sup><font color='red'>*</font></sup>"), TextView.BufferType.SPANNABLE);
        titlegender.setText(Html.fromHtml(titlegender.getText().toString() + "&nbsp;<sup><font color='red'>*</font></sup>"), TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addNewChildButton){
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_ADD_NEW_CHILD_NAME,  ScreenNameConstants.BUTTON_CLICKED);
            if (SystemClock.elapsedRealtime() - lastClickTime < 3000){
                return;
            }else{
                addNewChild();
            }
            lastClickTime=SystemClock.elapsedRealtime();
        }else if (v.getId() == R.id.maleButton) {
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_MALE_CHILD,  ScreenNameConstants.BUTTON_CLICKED);
             gender=ChildLabourUtils.MALE;
            femaleButton.setBackgroundColor(getResources().getColor(R.color.gender_selected));
            maleButton.setBackgroundResource(R.drawable.gender_male_selected);

            femaleButton.setTextColor(getResources().getColor(R.color.gender_selected_text));
            maleButton.setTextColor(getResources().getColor(android.R.color.black));

        } else if (v.getId() == R.id.femaleButton) {
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_FEMALE_CHILD,  ScreenNameConstants.BUTTON_CLICKED);
             gender=ChildLabourUtils.FEMALE;
    //        gender = getResources().getString(R.string.gender_female);

            femaleButton.setBackgroundResource(R.drawable.gender_female_selected);
            femaleButton.setTextColor(getResources().getColor(android.R.color.black));

            maleButton.setBackgroundColor(getResources().getColor(R.color.gender_selected));
            maleButton.setTextColor(getResources().getColor(R.color.gender_selected_text));

        }
    }

    // Age shows from 0 to 17.
    private void setAgeSpinner() {
        ageList = new ArrayList<String>();
        ageList.add(getResources().getString(R.string.please_select));
        for (int i = 0; i <= 17; i++) {
            ageList.add(Integer.toString(i));
        }
        adapter = new ArrayAdapter<String>(this, R.layout.rowview_age_spinner, ageList);
        adapter.setDropDownViewResource(R.layout.rowview_age_spinner);

        ageSpinner.setAdapter(adapter);
    }

    // Method to enroll the new child
    private void addNewChild() {

        String name=fullnameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            Toast.makeText(AddNewChildActivity.this,getResources().getString(R.string.fill_child_name),Toast.LENGTH_LONG).show();
            return;
        }
       // String age = ageSpinner.getSelectedItem().toString();

        if (ageSpinner.getSelectedItemId()==0){
            Toast.makeText(AddNewChildActivity.this,getResources().getString(R.string.please_select_Age),Toast.LENGTH_LONG).show();
            return;
        }

        if(getIntent().getStringExtra(ChildLabourUtils.ACTION).equalsIgnoreCase(ChildLabourUtils.NEW)){

            if (mCurrentLocation!=null){}else {
                if (mCurrentLocation == null || !gpsTracker.isGpsEnabled()) {
                    gpsTracker.getDeviceLocation();
                    return;
                }
            }
            submitChild(name);
        }else {
            UpdateChild(name);
        }
    }

    // Method to update the new child before sync
    private void UpdateChild(String name) {
        ContentValues contentValues=new ContentValues();
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID, farmerId);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME, name);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE, Integer.parseInt(age));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER, gender);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE_ON_YEAR, Calendar.getInstance().get(Calendar.YEAR));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED, 0);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_ID, childId);

        long newChildId= new ChildLabourLogic().updateChildDetails(this,contentValues,childrenDTO.getId(),farmerName,name);
        new GpsTrackerLogic().updateChildDetails(this,childId,name,gender,age);
        if (newChildId != -1){
            if(action.equalsIgnoreCase(ChildLabourUtils.NEW)){
                Toast.makeText(this,getResources().getString(R.string.child_register_successfully),Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,getResources().getString(R.string.child_updated_successfully),Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }


    private void submitChild(String name) {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String registereddate = format.format(today);

        ContentValues contentValues=new ContentValues();
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID, farmerId);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME, name);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE, Integer.parseInt(age));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER, gender);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE_ON_YEAR, Calendar.getInstance().get(Calendar.YEAR));
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED, 0);
        contentValues.put(OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT, registereddate);

        long newChildId= new ChildLabourLogic().insertChildDetails(this,contentValues,farmerName);
        if (newChildId != -1){
            if(action.equalsIgnoreCase(ChildLabourUtils.NEW)){
                collectGPSPoint(String.valueOf(newChildId),name);
                Toast.makeText(this,getResources().getString(R.string.child_register_successfully),Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,getResources().getString(R.string.child_updated_successfully),Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

        private void collectGPSPoint(String childId, String childName) {
            TrackDetailsDTO trackDetailsDTO=new TrackDetailsDTO();
            trackDetailsDTO.setModule(GPSConstants.MODULE_CAPTURE_ADD_CHILD);
            trackDetailsDTO.setLatitude(String.valueOf(mCurrentLocation.getLatitude()));
            trackDetailsDTO.setLongitude(String.valueOf(mCurrentLocation.getLongitude()));
            trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
            trackDetailsDTO.setFarmerId(String.valueOf(farmerId));
            trackDetailsDTO.setFarmerName(farmerName);
            trackDetailsDTO.setChildId(childId);
            trackDetailsDTO.setChildName(childName);
            trackDetailsDTO.setChildGender(gender);
            trackDetailsDTO.setChildAge(age);

            ContentValues geoContentValues=new GPSUtils().getGeoContentValues(trackDetailsDTO);
            new GpsTrackerLogic().insertGeoDataDetails(this,geoContentValues);
        }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            gpsTracker.stopLocationUpdates();
        }catch (Exception e){}
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

    public void getBundleValues() {
        try{
            Bundle bundle = this.getIntent().getExtras();
            action=bundle.getString(ChildLabourUtils.ACTION);
            if (action.equalsIgnoreCase(ChildLabourUtils.NEW)){
                addNewChildButton.setText(getResources().getString(R.string.add_child));
                farmerId=bundle.getString(ChildLabourUtils.CHILD_FARMER_ID);
            }else if(action.equalsIgnoreCase(ChildLabourUtils.EDIT)){
                addNewChildButton.setText(getResources().getString(R.string.update_child));
                farmerId=bundle.getString(ChildLabourUtils.CHILD_FARMER_ID);
                childrenDTO= (ChildrenDTO) bundle.getSerializable("children");
                setValue(childrenDTO);
            }else if(action.equalsIgnoreCase(ChildLabourUtils.SYNC_EDIT)){
                addNewChildButton.setText(getResources().getString(R.string.update_child));
                farmerId = getIntent().getStringExtra(ChildLabourUtils.CHILD_FARMER_ID);
                childId = getIntent().getStringExtra(ChildLabourUtils.CHILD_ID);
                childrenDTO= new ChildLabourLogic().getChildrenDTO(this,childId);
                setValue(childrenDTO);


            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // Method to set the value from db to edit the child
    private void setValue(ChildrenDTO childrenDTO) {
        fullnameEditText.setText(childrenDTO.getName());

        if(childrenDTO.getGender().equalsIgnoreCase(ChildLabourUtils.MALE)){
            maleButton.performClick();
        }else if(childrenDTO.getGender().equalsIgnoreCase(ChildLabourUtils.FEMALE)){
            femaleButton.performClick();
        }
        childId = childrenDTO.getId();
        if (childrenDTO.getAge() != null) {
            int spinnerPosition = adapter.getPosition(childrenDTO.getAge());
            ageSpinner.setSelection(spinnerPosition);
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
