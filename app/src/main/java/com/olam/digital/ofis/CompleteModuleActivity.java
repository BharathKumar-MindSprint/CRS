package com.olam.digital.ofis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
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
import com.olam.digital.ofis.coffeesurveyconstraints.CoffeeConstants;
import com.olam.digital.ofis.coffeesurveyconstraints.CoffeeConstraints;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.multifarm.FindMultiFarmFarmerAdapter;
import com.olam.digital.ofis.multifarm.MultiFarmDTO;
import com.olam.digital.ofis.multifarm.MultiFarmUtils;

import org.json.JSONException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class CompleteModuleActivity extends BaseActivity {

    private  final String MATOMOTAG = this.getClass().getSimpleName();
    private long farmerId;
    private long moduleId;
    private long farmerModuleId;
    private int subModuleId;
    private int farmId;
    private int statusStarted;
    private int statusFinished;
    private String olamFarmerId;
    private String moduleName;
    private String recordedLat;
    private String recordedLng;
    private String farmerName;
    private String viewModuleAns;
    private Cursor subModules, questions;
    public static Cursor allQuestions;
    private Context context;
    private Resources res;
    private DataLayer dl;
    private boolean isLastPage;
    private DynamicQuestions dynamicQuestions;
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
    private static final String VIEW_MODULE = "VIEW_MODULE";
    private static final String ISPLOTSURVEY = "ISPLOTSURVEY";
    private static final String PLOTID = "PLOTID";
    private long networkQueueId = 0;
    public static boolean editMode = false;

    // for GPS location system
//    private GoogleApiClient googleApiClient;
//    private LocationListener locationListener;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    // location updates interval - 2sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;

    // fastest updates interval - 1 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    private static final int REQUEST_CHECK_SETTINGS = 100;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private Location mCurrentCapturedLocation=null;
    GPSListener gpsListener=null;
    private GPSTracker gpsTracker=null;

    public static ScrollView scrollView;
    LinearLayout viewSurveyAnswer;
    int plotId = 0;
    String isPlotSurvey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_module);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        farmerId = intent.getLongExtra(FARMER_ID, 0);
        moduleId = intent.getLongExtra(MODULE_ID, 0);
        farmerModuleId = intent.getLongExtra(FARMER_MODULE_ID, 0);
        subModuleId = intent.getIntExtra(SUB_MODULE_ID, 0);
        farmId = intent.getIntExtra(FARM_ID, 0);
        statusStarted = intent.getIntExtra(STATUS_STARTED, 0);
        statusFinished = intent.getIntExtra(STATUS_FINISHED, 0);
        olamFarmerId = intent.getStringExtra(OLAM_FARMER_ID);
        moduleName = intent.getStringExtra(MODULE_NAME);
        recordedLat = intent.getStringExtra(RECORDED_LAT);
        recordedLng = intent.getStringExtra(RECORDED_LNG);
        farmerName = intent.getStringExtra(FARMER_NAME);
        viewModuleAns = intent.getStringExtra(VIEW_MODULE);
        isPlotSurvey = intent.getStringExtra(ISPLOTSURVEY);
        if(isPlotSurvey.equalsIgnoreCase("true")) {
            plotId = intent.getIntExtra(PLOTID, 0);
        }
        Toast.makeText(this, "PlotId:" + plotId,Toast.LENGTH_SHORT).show();
        scrollView= findViewById(R.id.scrollView);
        context = this;
        res = getResources();
        dl = new DataLayer(res, context);

        MultiFarmUtils.isMultiFarmEnumerator = Utilities.getMultifarmPref(context, MultiFarmUtils.IS_MULTI_FARM_ENUMERATOR);

        DynamicQuestions.resource(res);
        setupGPS();

        dynamicQuestions = DynamicQuestions.getInstance(this, Utilities.getLocaleSuffix(res), dl);

        dynamicQuestions.questionsSection = (LinearLayout) findViewById(R.id.questionsSection);
        viewSurveyAnswer = (LinearLayout) findViewById(R.id.view_surveyans);
        dynamicQuestions.inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        dynamicQuestions.reportingYear = getReportingYear();


        subModules = dl.getSubmodulesByModuleId(moduleId);

        Utilities.subModuleId = subModuleId;
        Utilities.farmerId = farmerId;
        Utilities.olamFarmerId = olamFarmerId;
        Utilities.moduleId = (int)moduleId;

        Utilities.farmerModuleId = farmerModuleId;

        Utilities.packagename=getPackageName();

        // all questions
        allQuestions = dl.getAllQuestionIds((int) moduleId);

        if (statusStarted != 0 || statusFinished != 0) {

            dynamicQuestions.hasAnswers = true;
            dynamicQuestions.viewAnswer = false;
            questions = dl.getQuestionsAndAnswersBySubModuleId(subModuleId, farmerId, farmerModuleId);

            moveToCurrentSubModule(subModuleId);
        } else if (subModuleId != 0) {
            questions = dl.getQuestionsBySubModuleId(subModuleId);
            dynamicQuestions.hasAnswers = false;
            moveToCurrentSubModule(subModuleId);
        } else {
            dynamicQuestions.hasAnswers = false;
            try {
                subModules.moveToFirst();
                subModuleId = subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
                questions = dl.getQuestionsBySubModuleId(subModuleId);
            }catch (Exception e){

            }

        }




        viewSurveyAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupSurveyAnswers();
            }
        });
        initDynamicQuestions();
        setupSubModuleQuestions();

        setupTopSection();

        final ScrollView mainScroll = (ScrollView) findViewById(R.id.scrollView);
        mainScroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainScroll.scrollTo(0, 0);
                if (Build.VERSION.SDK_INT < 16) {
                    //noinspection deprecation
                    mainScroll.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mainScroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        bindSaveButton();
        setupFooter();
        dynamicQuestions.SurveyType="other";

        gpsListener=new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location!=null){
                    mCurrentCapturedLocation=location;
                }
            }
        };
        gpsTracker=new GPSTracker(this,gpsListener);
       // gpsTracker.getDeviceLocation();
//        if (gpsTracker.isGpsEnabled()){
            gpsTracker.startLocationUpdateInitially();

//        }
}



    private void checkTime() {
        try {
            int auto_time_enabled = 0;
            int auto_time_zone_enabled = 0;

                auto_time_enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
                auto_time_zone_enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
//
            if (auto_time_enabled == 0 || auto_time_zone_enabled == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setMessage(res.getString(R.string.set_auto_time_message))
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                                startActivityForResult(intent, 6575);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
               // autotimeEnabled = true;
            }
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("HomeActivity: Error getting phone auto time settings", e));
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_VIEW_FARMER_ACTIVITY, ScreenNameConstants.SCREEN_COMPLETE_MODULE_ACTIVITY, ScreenNameConstants.SCREEN_COMPLETE_QUESTIONARE);
        checkTime();
    }

    @SuppressWarnings("ConstantConditions")
    private void initDynamicQuestions() {
        DynamicQuestions.getInstance().answerSet = new ArrayList<>();
        DynamicQuestions.getInstance().hundredpercentquestions = new ArrayList<>();
        DynamicQuestions.getInstance().context = context;
        DynamicQuestions.getInstance().updatingHundredPercent = false;
        DynamicQuestions.getInstance().questionColumnSuffix = Utilities.getLocaleSuffix(res);
    }

    private void setupGPS() {
        /*if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/
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
    }


    private void setPoiGPS(Location location) {
        try {/*

            poiLocation = location;
            gpsField = (EditText) findViewById(R.id.gpsPosition);
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
            gpsField.setText(locationResult);*/
            Location moduleLocation = location;
            recordedLat = String.valueOf(moduleLocation.getLatitude());
            recordedLng = String.valueOf(moduleLocation.getLongitude());

        } catch (Exception e) {
            FirebaseCrash.report(new Exception("AddInfrastructureActivity: Error setting POI location", e));
            e.printStackTrace();
        }
    }


    protected void onStart() {
//        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
//        googleApiClient.disconnect();
        super.onStop();
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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        dynamicQuestions.questionsSection = (LinearLayout) findViewById(R.id.questionsSection);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        LinearLayout questionsSection;

        LinearLayout bottomSection = (LinearLayout) findViewById(R.id.bottomSection);

        questionsSection = dynamicQuestions.questionsSection;

        ((ViewGroup) questionsSection.getParent()).removeView(questionsSection);
        bottomSection.addView(questionsSection, 0, questionsSection.getLayoutParams());
        checkTime();
    }



   
    private void moveToCurrentSubModule(int subModuleId) {
        while (subModules.moveToNext()) {
            if (subModuleId == subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID))) {
                break;
            }
        }
    }

    private void setupTopSection() {
        TextView moduleNameTv = (TextView) findViewById(R.id.completeModuleModuleTitle);
        TextView subModuleNameTv = (TextView) findViewById(R.id.completeModuleSubModuleTitle);

        moduleNameTv.setText(moduleName);

        moduleNameTv.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (moduleNameTv.getLineCount()>4){
                        moduleNameTv.setMaxLines(4);
                        moduleNameTv.setEllipsize(TextUtils.TruncateAt.END);}
                }catch (Exception e){}
            }
        });


            int columnIndex = (subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + dynamicQuestions.questionColumnSuffix) != -1) ? subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + dynamicQuestions.questionColumnSuffix) : subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME);
            subModuleNameTv.setText(subModules.getString(columnIndex));

        subModuleNameTv.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (subModuleNameTv.getLineCount()>4){
                        subModuleNameTv.setMaxLines(4);
                        subModuleNameTv.setEllipsize(TextUtils.TruncateAt.END);}
                }catch (Exception e){}
            }
        });
        long farmerGroupId = dl.getFarmerGroupById(farmerId);
        long sectionId = dl.getSectionByFarmerId(farmerId);
        boolean value = dl.farmerExistByfarmerGroupSection(farmerGroupId,sectionId,moduleId);
        if(value){
            boolean viewExistingdata = dl.isSurveyAnsDownloaded(subModuleId, farmerId);
            if(viewExistingdata){
                viewSurveyAnswer.setVisibility(View.VISIBLE);
            }else{
                viewSurveyAnswer.setVisibility(View.GONE);
            }
        }else{
            viewSurveyAnswer.setVisibility(View.GONE);
        }
    }

    private int getReportingYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (requestCode == 100 && resultCode == RESULT_OK) {
//            Toast.makeText(AddInfrastructureActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                    gpsTracker.getDeviceLocation();
                    startLocationUpdates();
                }else if (requestCode == 100 && resultCode == RESULT_CANCELED) {
//            Toast.makeText(AddInfrastructureActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                    stopLocationUpdates();
                }
                break;
            case 6575:
                checkTime();
                break;
            default:
                if (resultCode == RESULT_OK) {
                    dynamicQuestions.setFullImageFromFilePath(res);
                }
        }
    }




    private void setupSubModuleQuestions() {
        // loop through questions and generate the right control for each
//        while (questions.moveToNext()) {
        if(questions != null && questions.moveToFirst()) {
            do {
                String displayType = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));

                if (displayType.replaceAll(" ", "").toLowerCase().equals("checkbox")) {
                    dynamicQuestions.renderCheckBoxQuestion(questions, dynamicQuestions.questionsSection, farmerId, farmerModuleId);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("datepicker")) {
                    dynamicQuestions.renderDatePickerQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("decimal")) {
                    dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("dropdown")) {
                    dynamicQuestions.renderDropDownQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photocheckbox")) {
                    dynamicQuestions.renderPhotoCheckboxQuestion(questions, farmerId, farmerModuleId, res, getPackageName());
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("repeater")) {
                    dynamicQuestions.renderRepeaterQuestion(questions, farmerId, olamFarmerId, farmerModuleId, res);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("textbox")) {
                    dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("yesno") || displayType.replaceAll(" ", "").toLowerCase().equals("yes/no")) {
                    dynamicQuestions.renderYesNoQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("farmtreedensity")) {
                    dynamicQuestions.renderTreeDensityQuestion(questions, res);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photoupload")) {
                    dynamicQuestions.renderPhotoUploadQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                }
            } while (questions.moveToNext());
        }


    }

    private void setupFooter() {
        Button prevSubmodule = (Button) findViewById(R.id.prevSubmodule);
        Button nextSaveSubmodule = (Button) findViewById(R.id.nextSaveSubmodule);
        isLastPage = false;

        if (subModules.isLast()) {
            isLastPage = true;
            nextSaveSubmodule.setText(res.getString(R.string.ModuleSaveButton));
        }

        if (subModules.isFirst()) {
            prevSubmodule.setVisibility(View.GONE);
        } else {
            prevSubmodule.setVisibility(View.VISIBLE);
        }

        prevSubmodule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLastPage = false;
                saveSubModule("back");
                DynamicQuestions.getInstance().constraintsSet.clear();
                DynamicQuestions.getInstance().surveySet.clear();
            }
        });

        nextSaveSubmodule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // validate all the submodules question is answered
                boolean isMandated = false;
                try {
//                    if (DynamicQuestions.getInstance().surveySet.size()>0) {
//                        Toast.makeText(CompleteModuleActivity.this,res.getString(R.string.restriction_in_submodules),Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                    if( DynamicQuestions.getInstance().constraintsSet.size() > 0 || DynamicQuestions.getInstance().surveySet.size() > 0){
                        String constraintMsg= new CoffeeConstraints().getConstraintExceedMsg(context,dl);
                        Toast.makeText(CompleteModuleActivity.this,constraintMsg,Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check the editQuestionSet in each submodule

                    if (DynamicQuestions.getInstance().editQuestionSet.size()>0){
                        ArrayList<Integer> AllSubmodulesQuestions=dl.getAllSubmodulesQuestionsId(questions);
                        ArrayList<Integer> affectedQuestions = new ArrayList<Integer>(AllSubmodulesQuestions);
                        // use common.retainAll

                        affectedQuestions.retainAll(DynamicQuestions.getInstance().editQuestionSet);
                        if (affectedQuestions.size()>0) {
                            String constraintMsg = new CoffeeConstraints().getAffectedQuestionMsg(context, dl, affectedQuestions);
                            Toast.makeText(CompleteModuleActivity.this, constraintMsg, Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }



                    isMandated = dynamicQuestions.isAllRequiredSubmoduleAnswered(questions);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(! isMandated){
                    Toast.makeText(CompleteModuleActivity.this,res.getString(R.string.restriction_in_submodules),Toast.LENGTH_SHORT).show();
                    return;
                }
                // End
                if (isLastPage) {

                    try{
                        if (mCurrentCapturedLocation!=null){}else {
                            if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
                                gpsTracker.getDeviceLocation();
                                return;
                            }
                        }
                    }catch (Exception e){}

                    ContentValues values = new ContentValues();
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "module");
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, moduleName);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, farmerName);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID, plotId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
                    if (MultiFarmUtils.isMultiFarmEnumerator){
                        String multifarmId=dl.getMultiFarmId(farmerId);
                        if (multifarmId!=null){
                            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, multifarmId);
                        }else{
                            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, farmId);
                        }

                    }else{
                        values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, farmId);
                    }

                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID, farmerModuleId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, recordedLat);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, recordedLng);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID, moduleId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
                    if (statusFinished == 0) {
                        values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                        networkQueueId = dl.insertNetworkQueueEntry(values);
                    } else {
                        dl.updateNetworkQueueFarmerAndChildModuleEntry(values, farmerModuleId,"module");
                    }
                    statusFinished = 1;
                    saveSubModule("finish");
                } else {
                    saveSubModule("next");
                }
            }
        });

    }

    private void bindSaveButton() {
        ImageButton saveButton = (ImageButton) findViewById(R.id.saveProgress);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SAVE_SURVEY,  ScreenNameConstants.BUTTON_CLICKED);
                saveSubModule("exit");
            }
        });
    }

    private void saveSubModule(String action) {
        /*boolean status = dynamicQuestions.CheckMinMaxforQuestion(questions);
        if(! status){
            return;
        }*/
        boolean canSaveAndFinish = true;

        // check location
        if (action.equalsIgnoreCase("finish")) {
            if (mCurrentCapturedLocation!=null){

            }else {
                if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
                    gpsTracker.getDeviceLocation();
                    return;
                }
            }
        }

        // first time saving this module?
        if (farmerModuleId == 0) {
            statusStarted = 1;
            farmerModuleId = dl.insertFarmerModule(moduleId, farmerId, olamFarmerId, farmId, subModuleId, statusStarted, recordedLat, recordedLng);
            if (isLastPage && networkQueueId > 0) {
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID, farmerModuleId);
                dl.updateNetworkQueueEntry(values, networkQueueId);
            }
        }


        dl.updateSubModuleAnswers(dynamicQuestions.answerSet, farmerModuleId, subModuleId, farmerId, olamFarmerId, farmId, "module");


        if (action.equalsIgnoreCase("next")) {
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_NEXT_SUBMODULE,  ScreenNameConstants.BUTTON_CLICKED);
            subModules.moveToNext();
            subModuleId = subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
        } else if (action.equalsIgnoreCase("back")) {
            MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_PREVIOUS_SUBMODULE,  ScreenNameConstants.BUTTON_CLICKED);
            subModules.moveToPrevious();
            subModuleId = subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
        }

        /*if (action.equalsIgnoreCase("exit") || action.equalsIgnoreCase("finish")) {
//            if (!dynamicQuestions.requiredFieldsAnswered((int) farmerModuleId, (int) moduleId) || ! dynamicQuestions.requiredFieldsMinMaxAnswered((int) farmerModuleId, (int) moduleId)) {
            if (!dynamicQuestions.requiredFieldsAnswered((int) farmerModuleId, (int) moduleId)) {
                statusFinished = 0;
                canSaveAndFinish = false;
                dl.deleteNetworkQueueItemByFarmerModuleId((int) farmerModuleId);
                Toast.makeText(context, res.getString(R.string.complete_all_required_fields), Toast.LENGTH_LONG).show();
            }
            if(! dynamicQuestions.requiredFieldsMinMaxAnswered((int) farmerModuleId, (int) moduleId)){
                statusFinished = 0;
                canSaveAndFinish = false;
                dl.deleteNetworkQueueItemByFarmerModuleId((int) farmerModuleId);
                Toast.makeText(context, res.getString(R.string.valid_range), Toast.LENGTH_LONG).show();
            }
        }

        dl.updateFarmerModule(farmerModuleId, subModuleId, statusStarted, statusFinished);

        if (action.equalsIgnoreCase("exit")) {
            ViewFarmerActivity.farmerId = farmerId;
            //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
            Intent intent = new Intent(context, ViewFarmerActivity.class);
            context.startActivity(intent);
        } else if (action.equalsIgnoreCase("finish")) {
            if (canSaveAndFinish) {
                ViewFarmerActivity.farmerId = farmerId;
                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
                Intent intent = new Intent(context, ViewFarmerActivity.class);
                context.startActivity(intent);
            }
        } else {
            Intent intent = getIntent();
            intent.putExtra(FARMER_ID, farmerId);
            intent.putExtra(MODULE_ID, moduleId);
            intent.putExtra(FARMER_MODULE_ID, farmerModuleId);
            intent.putExtra(SUB_MODULE_ID, subModuleId);
            intent.putExtra(FARM_ID, farmId);
            intent.putExtra(STATUS_STARTED, statusStarted);
            intent.putExtra(STATUS_FINISHED, statusFinished);
            intent.putExtra(OLAM_FARMER_ID, olamFarmerId);
            intent.putExtra(MODULE_NAME, moduleName);
            intent.putExtra(RECORDED_LAT, recordedLat);
            intent.putExtra(RECORDED_LNG, recordedLng);
            intent.putExtra(FARMER_NAME, farmerName);

            startActivity(intent);

            finish();

        }*/
        boolean bool=dynamicQuestions.checkDisplayStatus((int) moduleId);
        if (action.equalsIgnoreCase("exit") || action.equalsIgnoreCase("finish")) {
            if(bool){
                if (!dynamicQuestions.requiredFieldsAnswered((int) farmerModuleId, (int) moduleId)) {
                    statusFinished = 0;
                    canSaveAndFinish = false;
                    dl.deleteNetworkQueueItemByFarmerModuleId((int) farmerModuleId);
                    Toast.makeText(context, res.getString(R.string.complete_all_required_fields), Toast.LENGTH_LONG).show();
                }
            }else{
                statusFinished = 0;
                canSaveAndFinish = false;
                dl.deleteNetworkQueueItemByFarmerModuleId((int) farmerModuleId);
                Toast.makeText(context, res.getString(R.string.complete_all_required_fields_mandatry), Toast.LENGTH_LONG).show();
            }
        }
        dl.updateFarmerModule(farmerModuleId, subModuleId, statusStarted, statusFinished);
        if (action.equalsIgnoreCase("exit")) {
            if(bool){
                ViewFarmerActivity.farmerId = farmerId;

                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
                Intent intent = new Intent(context, ViewFarmerActivity.class);
                context.startActivity(intent);
            }
        } else if (action.equalsIgnoreCase("finish")) {
            if(bool){
                if (canSaveAndFinish) {

                    if (dynamicQuestions.requiredFieldsAnswered((int) farmerModuleId, (int) moduleId) && !editMode) {
                        collectGPSPoint(mCurrentCapturedLocation);
                        gpsTracker.stopLocationUpdates();
                    }

                    ViewFarmerActivity.farmerId = farmerId;
                    //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
                    Intent intent = new Intent(context, ViewFarmerActivity.class);
                    context.startActivity(intent);
                }
            }
        } else {
            Intent intent = getIntent();
            intent.putExtra(FARMER_ID, farmerId);
            intent.putExtra(MODULE_ID, moduleId);
            intent.putExtra(FARMER_MODULE_ID, farmerModuleId);
            intent.putExtra(SUB_MODULE_ID, subModuleId);
            intent.putExtra(FARM_ID, farmId);
            intent.putExtra(STATUS_STARTED, statusStarted);
            intent.putExtra(STATUS_FINISHED, statusFinished);
            intent.putExtra(OLAM_FARMER_ID, olamFarmerId);
            intent.putExtra(MODULE_NAME, moduleName);
            intent.putExtra(RECORDED_LAT, recordedLat);
            intent.putExtra(RECORDED_LNG, recordedLng);
            intent.putExtra(FARMER_NAME, farmerName);
            startActivity(intent);
            finish();
        }
    }



    private void collectGPSPoint(Location farmLocation) {
        TrackDetailsDTO trackDetailsDTO=new TrackDetailsDTO();
        trackDetailsDTO.setModule(GPSConstants.MODULE_CAPTURE_FARMER_SURVEY);
        trackDetailsDTO.setLatitude(String.valueOf(farmLocation.getLatitude()));
        trackDetailsDTO.setLongitude(String.valueOf(farmLocation.getLongitude()));
        trackDetailsDTO.setAccuracy(String.valueOf(farmLocation.getAccuracy()));
        trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
        trackDetailsDTO.setFarmerId(String.valueOf(farmerId));
        trackDetailsDTO.setFarmerName(farmerName);
        trackDetailsDTO.setSurveyName(moduleName);

        ContentValues geoContentValues=new GPSUtils().getGeoContentValues(trackDetailsDTO);
        new GpsTrackerLogic().insertGeoDataDetails(context,geoContentValues);
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
/*
                        pd = new ProgressDialog(AddInfrastructureActivity.this);
                        pd.setCancelable(false);
                        pd.setIndeterminate(false);
                        pd.setMessage(res.getString(R.string.fetching_location));
                        pd.show();*/
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if(mCurrentLocation != null) {
                                    setPoiGPS(mCurrentLocation);
                                }else{
                                   /* if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }*/
                                    Toast.makeText(CompleteModuleActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
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
                               /* if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null && pd.isShowing()) {
                                    pd.dismiss();
                                }*/
                                Toast.makeText(CompleteModuleActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                            }
                        }, 1000);

                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(CompleteModuleActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
//                                Log.e(TAG, errorMessage);

                                Toast.makeText(CompleteModuleActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                                break;
                            default:
                                Toast.makeText(CompleteModuleActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();

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
                        /*if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null && pd.isShowing()) {
                            pd.dismiss();
                        }*/
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

    private void showPopupSurveyAnswers() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_existingsurveyans, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(findViewById(R.id.layout_complete_module), Gravity.CENTER, 0, 0);
        dimBehind(popupWindow);

        final Dialog surveyDialog = new Dialog(CompleteModuleActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        surveyDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        surveyDialog.setCancelable(false);
        surveyDialog.setCanceledOnTouchOutside(false);
        surveyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        surveyDialog.setContentView(R.layout.activity_existingsurveyans);
        dynamicQuestions.questionsSection = (LinearLayout) popupView.findViewById(R.id.questionsSection);

        Cursor questions = dl.retrieveQuestionsAndAnswersBySubModuleId(subModuleId, farmerId, farmerModuleId);
        try {
            if(questions != null){
             while (questions.moveToNext()) {
                 /*LinearLayout questionInclude = (LinearLayout) inflaterQuestions.inflate(R.layout.question_answer_row, questionsSection, false);
                 questionsSection.addView(questionInclude);
                 TextView questionText = (TextView) questionInclude.findViewById(R.id.questionText);
                 TextView answerText = (TextView) questionInclude.findViewById(R.id.answerText);
                 String questionColumnSuffix = Utilities.getLocaleSuffix(res);
                 int columnIndex = (questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) : questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION);
                 questionText.setText(questions.getString(columnIndex));
                 answerText.setText("Answer Here");*/
                 dynamicQuestions.hasAnswers = true;
                 dynamicQuestions.viewAnswer = true;
                 String displayType = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));
                 if (displayType.replaceAll(" ", "").toLowerCase().equals("checkbox")) {
                     dynamicQuestions.renderCheckBoxQuestion(questions, dynamicQuestions.questionsSection, farmerId, farmerModuleId);
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("datepicker")) {
                     dynamicQuestions.renderDatePickerQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("decimal")) {
                     dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("dropdown")) {
                     dynamicQuestions.renderDropDownQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photocheckbox")) {
                     dynamicQuestions.renderPhotoCheckboxQuestion(questions, farmerId, farmerModuleId, res, getPackageName());
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("repeater")) {
                     dynamicQuestions.renderRepeaterQuestion(questions, farmerId, olamFarmerId, farmerModuleId, res);
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("textbox")) {
                     dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("yesno") || displayType.replaceAll(" ", "").toLowerCase().equals("yes/no")) {
                     dynamicQuestions.renderYesNoQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("farmtreedensity")) {
                     dynamicQuestions.renderTreeDensityQuestion(questions, res);
                 } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photoupload")) {
                     dynamicQuestions.renderPhotoUploadQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                 }
             }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        TextView moduleText = (TextView) popupView.findViewById(R.id.completeModuleSubModule);
        int columnIndex = (subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + dynamicQuestions.questionColumnSuffix) != -1) ? subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + dynamicQuestions.questionColumnSuffix) : subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME);
        moduleText.setText(moduleName + " - " + subModules.getString(columnIndex));
        popupView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                dynamicQuestions.viewAnswer = false;
            }
        });
    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
       WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.7f;
        wm.updateViewLayout(container, p);
    }

}
