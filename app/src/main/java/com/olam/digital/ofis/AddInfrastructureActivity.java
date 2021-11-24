package com.olam.digital.ofis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.businesslogic.SipLogic;
import com.olam.digital.ofis.childlabour.dto.VillageDTO;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.model.ModuleDTO;
import com.olam.digital.ofis.model.SubModuleDTO;
import com.olam.digital.ofis.recordpolygon.Utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddInfrastructureActivity extends BaseActivity {
    private final String MATOMOTAG = this.getClass().getSimpleName();
    // update
    // for edit mode
    public static long infrastructureID = 0;
    public static boolean editMode = false;
    private int selectedCountry = 0;
    private int selectedRegion = 0;
    private int selectedDistrict = 0;
    private int selectedProduct = 0;
    private int selectedFarmerGroup = 0;
    private int selectedPlace = 0;
    private int selectedSection=0;

    // New
    private int selectedCountry1 = 0;
    private int selectedRegion1 = 0;
    private int selectedDistrict1 = 0;
    private int selectedPlace1 = 0;


    // for GPS location system
    private EditText gpsField;
    private ImageButton requestLocation;
    private Location poiLocation;
    private String recorded_lat, recorded_lng;
    private String accuracy;



    // location updates interval - 2sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;

    // fastest updates interval - 1 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    // colours for animation
    private static final int COLOUR_SCHOOL = 0xFF924c2e;
    private int currentColour = COLOUR_SCHOOL;
    private static final String CURRENT_LAT = "CURRENT_LAT";
    private static final String CURRENT_LNG = "CURRENT_LNG";
    private static final int PERMISSIONS_ACCESS_LOCATION = 6575;

    //types of POI, we use these to pass into various helper methods
    private static final int SCHOOL = 0;
    private int currentTypeOfPOI = SCHOOL;
    private int currentSubTypeOfPOI = 0;
    private EditText poiName;
    private Context context;
    private Resources res;
    private DataLayer dl;

    private Spinner subModulesSpinner;
    private Spinner modulesSpinner;
    private Spinner villageSpinner;
    private String moduleName;
    List<VillageDTO> villageList;
//    private boolean spinners_ready = false;


    // translatable dropdown values
    private final Hashtable<String, String> poitypes = new Hashtable<>();
    private final Hashtable<String, String> genders = new Hashtable<>();
    private final Hashtable<String, String> educationlevels = new Hashtable<>();
    private final Hashtable<String, String> cleanwatertypes = new Hashtable<>();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private DynamicQuestions dynamicQuestions;
    private Cursor subModules, questions, modules;
    private int moduleId;
    private int subModuleId;

    ArrayList<SubModuleDTO> subModuleDTOS;
    ArrayList<ModuleDTO> moduleDTOS;

    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private ProgressDialog pd;
    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    Spinner spinner_place=null;
    Spinner spinner_place1=null;
    VillageDTO villageDetails=null;
    ArrayAdapter<String> villageSpinnerAdapter=null;
    private int editPosition;
    private boolean isVillage;
    LinearLayout layout_geo_old, layout_geo_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_infrastructure);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        res = getResources();
         spinner_place = (Spinner) findViewById(R.id.spinner_place);

        initWidgets();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                if(mCurrentLocation != null)
                    setPoiGPS(mCurrentLocation);
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(new GPSTracker(this).getLocationMode());

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        dl = new DataLayer(res, context);
        poiName = (EditText) findViewById(R.id.poiName);
        poiName.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
        poiName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        subModulesSpinner = (Spinner) findViewById(R.id.spinner_submodules);
        modulesSpinner = (Spinner) findViewById(R.id.spinner_modules);
        villageSpinner=(Spinner) findViewById(R.id.villageSpinner);
        villageSpinner.setVisibility(View.GONE);

        requestLocation = (ImageButton) findViewById(R.id.requestLocation);
        if(!editMode)
            searchingGPS();

        bindGpsButton();
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }
            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }
            recorded_lat = savedInstanceState.getString(CURRENT_LAT);
            recorded_lng = savedInstanceState.getString(CURRENT_LNG);
        }
        dynamicQuestions = DynamicQuestions.getInstance(this, Utilities.getLocaleSuffix(res), dl);
        dynamicQuestions.questionsSection = (LinearLayout) findViewById(R.id.questionsSection);
        dynamicQuestions.inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        dynamicQuestions.reportingYear = getReportingYear();
        initDynamicQuestions();
        bindModulesSpinner();

        if (editMode) {
            modulesSpinner.setEnabled(false);
            subModulesSpinner.setEnabled(false);


            if (infrastructureID != 0) {
                Cursor existingItem = dl.getNetworkQueueItem((int) infrastructureID);

                if (existingItem.getCount() > 0) {
                    existingItem.moveToFirst();
                    populateExistingValues(existingItem);
                }
            }
        } else {
            dynamicQuestions.hasAnswers = false;
            if (moduleDTOS != null && moduleDTOS.size() > 0) {
                moduleId = moduleDTOS.get(0).getId();
                bindPoiSpinner(moduleId);
                try {
                    subModuleId = subModuleDTOS.get(0).getId();
                }catch(Exception e){e.printStackTrace();}
            }
        }

      //  int selectedPlace = 0;
        dl.createGeographyHierarchySpinners(this, context, null, selectedCountry, selectedRegion, selectedDistrict, selectedPlace, null,false);
        dl.createBusinessHierarchySpinners(this, context, null, selectedFarmerGroup, selectedSection);

        // New
        dl.createGeographyHierarchySpinners1(this, context, null, selectedCountry1, selectedRegion1, selectedDistrict1, selectedPlace1, null,false);


        bindSaveButton();
        dynamicQuestions.SurveyType="other";

        villageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.SPINNER_VILLAGE,  ScreenNameConstants.SPINNER_CHANGED);
                if (position>0) {
                    villageDetails = villageList.get(position-1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (editMode){
                    villageDetails=villageList.get(editPosition);
                }

            }
        });
    }

    private void initWidgets() {

        spinner_place1 = (Spinner) findViewById(R.id.spinner_place1);
        layout_geo_old=(LinearLayout) findViewById(R.id.layout_geo_old);
        layout_geo_new=(LinearLayout) findViewById(R.id.layout_geo_new);


    }


    private void setupSubModuleQuestions() {
        // loop through questions and generate the right control for each
        while (questions.moveToNext()) {
            String displayType = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_TYPE));
            if (displayType.toLowerCase().replaceAll(" ","").equals("checkbox")) {
                dynamicQuestions.renderCheckBoxQuestionForSIP(questions, dynamicQuestions.questionsSection);
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("datepicker")) {
                dynamicQuestions.renderDatePickerQuestionForSip(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("decimal")) {
                dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("dropdown")) {
                dynamicQuestions.renderSipDropDownQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("photocheckbox")) {
                dynamicQuestions.renderPhotoCheckboxQuestionForSIP(questions, res, getPackageName());
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("repeater")) {
                dynamicQuestions.renderRepeaterQuestionForSIP(questions, res);
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("textbox")) {
                dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("yesno") || displayType.toLowerCase().replaceAll(" ","").equals("yes/no")) {
                dynamicQuestions.renderYesNoQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("farmtreedensity")) {
                dynamicQuestions.renderTreeDensityQuestion(questions, res);
            } else if (displayType.toLowerCase().replaceAll(" ","").equals("photoupload")) {
                dynamicQuestions.renderPhotoUploadQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void initDynamicQuestions() {
        DynamicQuestions.getInstance().answerSet = new ArrayList<>();
        DynamicQuestions.getInstance().hundredpercentquestions = new ArrayList<>();
        DynamicQuestions.getInstance().context = context;
        DynamicQuestions.getInstance().updatingHundredPercent = false;
        DynamicQuestions.getInstance().questionColumnSuffix = Utilities.getLocaleSuffix(res);
    }

    private void moveToCurrentSubModule(int subModuleId) {
        while (subModules.moveToNext()) {
            if (subModuleId == subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID))) {
                break;
            }
        }
    }

    private int getReportingYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        savedInstanceState.putParcelable("last_known_location", mCurrentLocation);
        if (poiLocation != null) {
            recorded_lat = String.valueOf(poiLocation.getLatitude());
            recorded_lng = String.valueOf(poiLocation.getLongitude());
        }
        if (recorded_lat != null) {
            savedInstanceState.putString(CURRENT_LAT, recorded_lat);
        }
        if (recorded_lng != null) {
            savedInstanceState.putString(CURRENT_LNG, recorded_lng);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
        super.onStop();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_LOCATION) {
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // Permission Denied
                    Toast.makeText(this, R.string.gps_permission_denial_msg, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.getPackageName())));

                } else {
                    if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        // Permission Allowed
                        startLocationUpdates();
                        return;

                    } else {
                        // Checkbox set to Never Ask Again
                        showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
                    }
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            dynamicQuestions.setFullImageFromFilePath(res);
        }else if (requestCode == 100 && resultCode == RESULT_OK) {
//            Toast.makeText(AddInfrastructureActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
            startLocationUpdates();
        }else if (requestCode == 100 && resultCode == RESULT_CANCELED) {
//            Toast.makeText(AddInfrastructureActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
            stopLocationUpdates();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resuming location updates depending on button state and
        // allowed permissions
       /* if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }*/

//        Toast.makeText(AddInfrastructureActivity.this,"Track Count : "+(new GpsTrackerLogic().getGPSTrackList(context)).size(),Toast.LENGTH_LONG).show();
        if(mCurrentLocation != null) {
            setPoiGPS(mCurrentLocation);
        }
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_HOME_ACTIVITY, ScreenNameConstants.SCREEN_ADD_INFRASTRUCTURE_ACTIVITY, ScreenNameConstants.SCREEN_ADD_SIP);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("SetTextI18n")
    private void populateExistingValues(final Cursor existingItem) {
        gpsField = (EditText) findViewById(R.id.gpsPosition);
        gpsField.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
        gpsField.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        int selectedModulePOI = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE_ID));
        int selectedSubModulePOI = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE_ID));
        recorded_lat = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT));
        recorded_lng = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG));
        gpsField.setText(recorded_lat + "," + recorded_lng);



        String uploadId = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID));
        modulesSpinner.setSelection(selectedModulePOI);
        moduleId = moduleDTOS.get(selectedModulePOI).getId();
        subModuleId = getSubModules(moduleId).get(selectedSubModulePOI).getId();
        bindPoiSpinner(moduleId);

        subModulesSpinner.setSelection(selectedSubModulePOI);
        dynamicQuestions.hasAnswers = true;
        questions = dl.getSIPQuestionsAndAnswersByUploadId(uploadId, subModuleId);
        dynamicQuestions.questionsSection.removeAllViews();
//        dynamicQuestions
        setupSubModuleQuestions();

        if (existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_IS_VILLAGE)).equalsIgnoreCase(ChildLabourUtils.TRUE)){
            isVillage=true;
        }else{
            isVillage=false;
        }
        if (!isVillage) {
            selectedCountry = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID));
            selectedRegion = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID));
            selectedDistrict = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID));
            selectedPlace = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLACE_ID));
        }else{
            selectedCountry1 = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID));
            selectedRegion1 = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID));
            selectedDistrict1 = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID));
            selectedPlace1 = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLACE_ID));
        }

        selectedProduct = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PRODUCT_ID));
        selectedFarmerGroup = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMERGROUP_ID));


        //   String name=getSubModules(moduleId).get(selectedSubModulePOI).getSipName();
        if (isVillage) {
            layout_geo_new.setVisibility(View.VISIBLE);
            layout_geo_old.setVisibility(View.GONE);
//            showVillageDropdown();
//            villageSpinner.setVisibility(View.VISIBLE);
//            String villageName=existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME));
//            if (villageName != null) {
//                editPosition = villageSpinnerAdapter.getPosition(villageName);
//                villageSpinner.setSelection(editPosition);
//            }
        }else {
            poiName.setVisibility(View.VISIBLE);
            poiName.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
        }


    }

    private ContentValues validateValues() {
        ContentValues values = new ContentValues();

        // common
        Spinner spinner_country = (Spinner) findViewById(R.id.spinner_country);
        long country_id = spinner_country.getSelectedItemId();
        //New
        Spinner spinner_country1 = (Spinner) findViewById(R.id.spinner_country1);
        long country_id1 = spinner_country1.getSelectedItemId();

        if (isVillage){
            if (country_id1 < 1) {
                Toast.makeText(context, res.getString(R.string.fill_country), Toast.LENGTH_LONG).show();
                return values;
            }
        }else {
            if (country_id < 1) {
                Toast.makeText(context, res.getString(R.string.fill_country), Toast.LENGTH_LONG).show();
                return values;
            }
        }

        Spinner spinner_region = (Spinner) findViewById(R.id.spinner_region);
        long region_id = spinner_region.getSelectedItemId();

        //New
        Spinner spinner_region1 = (Spinner) findViewById(R.id.spinner_region1);
        long region_id1 = spinner_region1.getSelectedItemId();
        if (isVillage){
            if (region_id1 < 1) {
                Toast.makeText(context, res.getString(R.string.fill_region), Toast.LENGTH_LONG).show();
                return values;
            }
        }else{
            if (region_id < 1) {
                Toast.makeText(context, res.getString(R.string.fill_region), Toast.LENGTH_LONG).show();
                return values;
            }
        }

        Spinner spinner_district = (Spinner) findViewById(R.id.spinner_district);
        long district_id = spinner_district.getSelectedItemId();

        //New
        Spinner spinner_district1 = (Spinner) findViewById(R.id.spinner_district1);
        long district_id1 = spinner_district1.getSelectedItemId();

        if (isVillage){
            if (district_id1 < 1) {
                Toast.makeText(context, res.getString(R.string.fill_district), Toast.LENGTH_LONG).show();
                return values;
            }
        }else{
            if (district_id < 1) {
                Toast.makeText(context, res.getString(R.string.fill_district), Toast.LENGTH_LONG).show();
                return values;
            }
        }


        Spinner spinner_farmer_group = (Spinner) findViewById(R.id.spinner_farmerGroup);
        long farmergroup_id = spinner_farmer_group.getSelectedItemId();
        if (farmergroup_id < 1) {

            Toast.makeText(context, res.getString(R.string.fill_farmergroup_name), Toast.LENGTH_LONG).show();

            return values;
        }

       // Spinner spinner_place = (Spinner) findViewById(R.id.spinner_place);

            long place_id = spinner_place.getSelectedItemId();
       // if (!moduleName.equalsIgnoreCase(ChildLabourUtils.VILLAGE_NAME)) {

        //New
        Spinner spinner_place1 = (Spinner) findViewById(R.id.spinner_place1);
        long place_id1 = spinner_place1.getSelectedItemId();
        if (isVillage){
            if (place_id1 < 1) {
                Toast.makeText(context, res.getString(R.string.please_fill_the_place), Toast.LENGTH_LONG).show();
                return values;
            }
        }else{
            if (place_id < 1) {
                Toast.makeText(context, res.getString(R.string.please_fill_the_place), Toast.LENGTH_LONG).show();
                return values;
            }
        }

        if (poiLocation == null && !editMode) {
            Toast.makeText(context, res.getString(R.string.fill_GPS_position), Toast.LENGTH_LONG).show();

            return values;
        }
        if (poiLocation != null && !editMode) {
            recorded_lat = String.valueOf(poiLocation.getLatitude());
            recorded_lng = String.valueOf(poiLocation.getLongitude());
            accuracy=String.valueOf(poiLocation.getAccuracy());
        }


        try {
            // common
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "infrastructure");
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE_ID, String.valueOf(modulesSpinner.getSelectedItemPosition()));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID, String.valueOf(moduleId));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, String.valueOf(modulesSpinner.getSelectedItem()));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE_ID, String.valueOf(subModulesSpinner.getSelectedItemPosition()));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBMODULE_ID, String.valueOf(subModuleId));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE, String.valueOf(subModulesSpinner.getSelectedItem()));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, recorded_lat);
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, recorded_lng);
            if (isVillage){
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID, country_id1);
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID, region_id1);
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, district_id1);
            }else{
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID, country_id);
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID, region_id);
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, district_id);
            }

            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMERGROUP_ID, farmergroup_id);

            if (isVillage) {
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLACE_ID, place_id1);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, dl.getPlaceName(Integer.parseInt(String.valueOf(place_id1))));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_IS_VILLAGE, ChildLabourUtils.TRUE);
            }else{
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLACE_ID, place_id);
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, String.valueOf(poiName.getText()));
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_IS_VILLAGE, ChildLabourUtils.FALSE);
            }

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("AddInfrastructureActivity: Error creating POI values", e));
            values.clear();
        }

        return values;
    }

    private void bindSaveButton() {
        Button save = (Button) findViewById(R.id.buttonSave);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SAVE_INFRASTRUCTURE,  ScreenNameConstants.BUTTON_CLICKED);
                if (!isVillage) {

                    if (poiName.getText().toString().trim().length() == 0) {
                        Toast.makeText(context, res.getString(R.string.fill_name), Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    Spinner spinner_place1=(Spinner) findViewById(R.id.spinner_place1);
                    long villageNameId=spinner_place1.getSelectedItemId();
                    if(villageNameId < 1){
                        Toast.makeText(context, res.getString(R.string.fill_name), Toast.LENGTH_LONG).show();
                        return;
                    }
                }



                // get the entered values
                ContentValues values = validateValues();
//                ContentValues answerValues = answerValidateValues(dynamicQuestions.answerSet, subModuleId, 0, values);
                String uploadId = null;

                if (editMode) {
                    if (infrastructureID != 0) {
                        Cursor existingItem = dl.getNetworkQueueItem((int) infrastructureID);
                        if (existingItem.getCount() > 0) {
                            existingItem.moveToFirst();
                            uploadId = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID));
                        }
                    }
                } else {
                    uploadId = UUID.randomUUID().toString();
                }

                if (uploadId != null)
                    dl.updateSIPSubModuleAnswers(dynamicQuestions.answerSet, subModuleId, 0, uploadId);

                // check below also
             //
                /*if(!dynamicQuestions.requiredFieldsSIPAnswered(uploadId, moduleId, subModuleId)){
                    Toast.makeText(AddInfrastructureActivity.this,getString(R.string.please_answer_all_questions),Toast.LENGTH_LONG).show();
                    return;
                }*/

                if (values.size() > 0 && uploadId != null ) {
                    if (editMode) {
                        // edit POI call
                        if ( dl.updateNetworkQueueEntry(values, infrastructureID) > 0) {
                            Toast.makeText(context, res.getString(R.string.Successfully_updated), Toast.LENGTH_SHORT).show();
                            // update Gps table
                            String sipName="";
                            if (isVillage){
                                Spinner spinner_place1=(Spinner) findViewById(R.id.spinner_place1);
                                long villageNameId=spinner_place1.getSelectedItemId();
                                sipName=dl.getPlaceName(Integer.parseInt(String.valueOf(villageNameId)));
                            }else{
                                sipName=poiName.getText().toString().trim();
                            }
                            try {
                                if (infrastructureID!=0){
                                    dl.updateSIPNameforGPS(AddInfrastructureActivity.this,sipName,(int)infrastructureID);
                                }
                            }catch (Exception e){}
                            save.setEnabled(false);
                            Intent intent = new Intent(context, HomeActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        save.setEnabled(false);
                        // insert POI
                        long newPoiId = dl.insertNetworkQueueSipEntry(values, uploadId);
                        if (newPoiId != -1) {
                            String sipName = null;
                            try {
                                if (isVillage){
                                    Spinner spinner_place1=(Spinner) findViewById(R.id.spinner_place1);
                                    long villageNameId=spinner_place1.getSelectedItemId();
                                    sipName=dl.getPlaceName(Integer.parseInt(String.valueOf(villageNameId)));
                                }else{
                                    sipName=poiName.getText().toString();
                                }
                            }catch (Exception e){}

                            String sipType=String.valueOf(subModulesSpinner.getSelectedItem());
                            collectGPSPoint(sipName,sipType,newPoiId);
                            Toast.makeText(context, res.getString(R.string.Successfully_added), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, HomeActivity.class);
                            startActivity(intent);

                        }

                    }

                } else {
                   // Toast.makeText(context, res.getString(R.string.Please_fill_all_fields), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void collectGPSPoint(String name,String type,long newPoiId) {
        TrackDetailsDTO trackDetailsDTO=new TrackDetailsDTO();
        trackDetailsDTO.setModule(GPSConstants.MODULE_CAPTURE_SOCIAL_INFRASTRUCTURE_POINT);
        trackDetailsDTO.setLatitude(recorded_lat);
        trackDetailsDTO.setLongitude(recorded_lng);
        trackDetailsDTO.setAccuracy(accuracy);
        trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
        trackDetailsDTO.setSipName(name);
        trackDetailsDTO.setSipType(type);
        trackDetailsDTO.setNetworkQueueId(newPoiId);
        ContentValues geoContentValues=new GPSUtils().getGeoContentValues(trackDetailsDTO);
        new GpsTrackerLogic().insertGeoDataDetails(context,geoContentValues);
    }

    private void setPoiGPS(Location location) {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        try {
            //  poiLocation = (Location) msg.obj;
            poiLocation = location;
            gpsField = (EditText) findViewById(R.id.gpsPosition);
            gpsField.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
            gpsField.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            String locationResult;
            if (poiLocation != null) {
                double latitude = poiLocation.getLatitude();
                double longitude = poiLocation.getLongitude();
                locationResult = latitude + "," + longitude;
                requestLocation.setImageResource(R.drawable.ic_actionbutton_location_found);
            } else {
                locationResult = res.getString(R.string.location_not_available);
                requestLocation.setImageResource(R.drawable.ic_actionbutton_location_searching);
            }
            gpsField.setText(locationResult);

        } catch (Exception e) {
            FirebaseCrash.report(new Exception("AddInfrastructureActivity: Error setting POI location", e));
            e.printStackTrace();
        }
    }
    private void searchingGPS(){
        requestLocation.setImageResource(R.drawable.ic_actionbutton_location_searching);

        if (ContextCompat.checkSelfPermission(AddInfrastructureActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(AddInfrastructureActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddInfrastructureActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_ACCESS_LOCATION);

            return;
        }
        getDeviceLocation();
    }


    private void bindGpsButton() {

        requestLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_GPS,  ScreenNameConstants.BUTTON_CLICKED);
                searchingGPS();
            }
        });

    }

    private ArrayList<ModuleDTO> getModules() {
        modules = dl.getSIPModules();
        moduleDTOS = new ArrayList<ModuleDTO>();
        ModuleDTO moduleDTO;
        while (modules.moveToNext()) {
            moduleDTO = new ModuleDTO();
            moduleDTO.setId(modules.getInt(modules.getColumnIndex(OFISDBContract.SipGroupEntry.COLUMN_NAME_ID)));
            moduleDTO.setModuleName(modules.getString(modules.getColumnIndex(OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME)));
            moduleDTOS.add(moduleDTO);
        }
        return moduleDTOS;
    }

  /*
  Old submodule code
   */
//    private ArrayList<SubModuleDTO> getSubModules(long moduleId) {
//        subModules = dl.getSipSubmodulesByModuleId(moduleId);
//        subModuleDTOS = new ArrayList<SubModuleDTO>();
//        SubModuleDTO subModuleDTO;
//        while (subModules.moveToNext()) {
//            subModuleDTO = new SubModuleDTO();
//            subModuleDTO.setId(subModules.getInt(subModules.getColumnIndex(OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID)));
//            subModuleDTO.setSipName(subModules.getString(subModules.getColumnIndex(OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME)));
//            subModuleDTOS.add(subModuleDTO);
//        }
//        return subModuleDTOS;
//    }

    /*
      Old submodule code - O20HS-57
       */
    private ArrayList<SubModuleDTO> getSubModules(long moduleId) {
        subModules = dl.getSipSubmodulesByModuleId(moduleId);
        subModuleDTOS = new ArrayList<SubModuleDTO>();
        SubModuleDTO subModuleDTO;


        // Code added for support multiple language
        String local_language=OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME+"_"+ Locale.getDefault().getLanguage();
        boolean available=false;
        int length=subModules.getColumnCount();
        for (int i=0;i < length;i++){
            if(subModules.getColumnName(i).trim().toString().equalsIgnoreCase(local_language)){
                available=true;
            }
        }
        if(!available){
            local_language=OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME;
        }

        while (subModules.moveToNext()) {
            subModuleDTO = new SubModuleDTO();
            subModuleDTO.setId(subModules.getInt(subModules.getColumnIndex(OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID)));
            subModuleDTO.setVillage(subModules.getString(subModules.getColumnIndex(OFISDBContract.SipSubGroupEntry.COLUMN_NAME_VILLAGE)));
//            subModuleDTO.setSipName(subModules.getString(subModules.getColumnIndex(OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME)));
            subModuleDTO.setSipName(subModules.getString(subModules.getColumnIndex(local_language)));
            subModuleDTOS.add(subModuleDTO);
        }
        return subModuleDTOS;
    }


    private void getSubModuleQuestions(int subModuleId) {
        if (subModuleId != 0) {
            questions = dl.getQuestionsSipBySubModuleId(subModuleId);
            dynamicQuestions.hasAnswers = false;
            moveToCurrentSubModule(subModuleId);
        }
        setupSubModuleQuestions();
    }

    private void bindModulesSpinner() {
        moduleDTOS = getModules();
        ArrayList<String> moduleNames = new ArrayList<String>();
        if (moduleDTOS.size() > 0) {
            for (ModuleDTO moduleDTO : moduleDTOS) {
                moduleNames.add(moduleDTO.getModuleName());
            }
        }

        ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.add_sip_spinner_item, moduleNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        modulesSpinner.setAdapter(adapter);

        modulesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (!editMode) {
                    moduleId = moduleDTOS.get(position).getId();
                    bindPoiSpinner(moduleId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void showPoiName() {
        poiName.setVisibility(View.VISIBLE);
      //  villageSpinner.setVisibility(View.GONE);
        spinner_place.setVisibility(View.VISIBLE);
    }

    private void showVillageDropdown() {
        spinner_place.setVisibility(View.GONE);
        poiName.setVisibility(View.GONE);
     //   villageSpinner.setVisibility(View.VISIBLE);
        villageList= SipLogic.getAllPlaces(this);
        List<String> villageNameList=new ArrayList<String>();
        villageNameList.add(context.getResources().getString(R.string.Name));
        for (VillageDTO village: villageList) {
            villageNameList.add(village.getVillageName());
        }
        villageSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, villageNameList);
        villageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        villageSpinner.setAdapter(villageSpinnerAdapter);
    }


    private void bindPoiSpinner(int moduleId) {
        subModuleDTOS = getSubModules(moduleId);
        ArrayList<String> subModuleNames = new ArrayList<String>();
        if (subModuleDTOS.size() > 0) {
            for (SubModuleDTO subModuleDTO : subModuleDTOS) {
                subModuleNames.add(subModuleDTO.getSipName());
            }
        }


        ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.spinner_dropdown_item_single_line, subModuleNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        subModulesSpinner.setAdapter(adapter);


        subModulesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                moduleName=subModuleDTOS.get(position).getSipName().trim().toLowerCase();
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.SPINNER_SUB_MODULES,  ScreenNameConstants.SPINNER_CHANGED);
                if (!editMode) {
//                    swapPoiType(i);
//                }
                   if (subModuleDTOS.get(position).getVillage().equalsIgnoreCase(ChildLabourUtils.TRUE)){
                       isVillage=true;
                   }else{
                       isVillage=false;
                   }
                    if (isVillage){
                   // if (moduleName.equalsIgnoreCase(ChildLabourUtils.VILLAGE_NAME)){
                   //     showVillageDropdown();
                        poiName.setVisibility(View.GONE);
                        layout_geo_old.setVisibility(View.GONE);
                        layout_geo_new.setVisibility(View.VISIBLE);
                        dl.createGeographyHierarchySpinners1(AddInfrastructureActivity.this, context, null, selectedCountry1, selectedRegion1, selectedDistrict1,selectedPlace1, null,false);

                    }else{
                        showPoiName();
                        layout_geo_old.setVisibility(View.VISIBLE);
                        layout_geo_new.setVisibility(View.GONE);
                    }
                    dynamicQuestions.questionsSection.removeAllViews();
                    dynamicQuestions.answerSet.clear();
                    subModuleId = subModuleDTOS.get(position).getId();
                    getSubModuleQuestions(subModuleId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    @Override
    protected void onDestroy() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        super.onDestroy();
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        pd = new ProgressDialog(AddInfrastructureActivity.this);
                        pd.setCancelable(false);
                        pd.setIndeterminate(false);
                        pd.setMessage(res.getString(R.string.fetching_location));
                        pd.show();
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if(mCurrentLocation != null) {
                                    setPoiGPS(mCurrentLocation);
                                }else{
                                    if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    Toast.makeText(AddInfrastructureActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                                }
                            }
                        }, 6000);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null && pd.isShowing()) {
                                    pd.dismiss();
                                }
                                Toast.makeText(AddInfrastructureActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                            }
                        }, 1000);

                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(AddInfrastructureActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
//                                Log.e(TAG, errorMessage);

                                Toast.makeText(AddInfrastructureActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                                break;
                            default:
                                Toast.makeText(AddInfrastructureActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    private void getDeviceLocation() {

        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


}
