package com.olam.digital.ofis.childlabour;

import android.annotation.SuppressLint;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.ViewFarmerActivity;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;

import org.json.JSONException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
// handling all survey Questions for child survey.
//Capture all the collected answers and store in database.
public class ChildLabourCompleteModuleActivity extends BaseActivity {

    private Long farmerID;
    private ChildrenDTO children;
    private ChildModuleDto childModuleDto;
    public static boolean editMode = false;

    private long farmerId;
    private long moduleId;
    private long childId;
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
    private Cursor subModules, questions;
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
    private static final String CHILDPRODUCT="CHILD_PRODUCT";
    private long networkQueueId = 0;

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
    private String childNameQueue;
    private String farmerNameQueue;

    private Location mCurrentCapturedLocation=null;
    GPSListener gpsListener=null;
    private GPSTracker gpsTracker=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_module);
        initData();
        initSetup();
        getChildAndFarmerName();
        dynamicQuestions.SurveyType="child";
        initGPSListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkTime();
    }

    private void initGPSListener() {
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

    private void getChildAndFarmerName() {
        childNameQueue= getResources().getString(R.string.child) + ": "+children.getName();
        farmerNameQueue=getResources().getString(R.string.farmer)+": "+farmerName;
    }

    private void initSetup() {
        Intent intent = getIntent();
        farmerId = intent.getLongExtra(FARMER_ID,0);
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

        context = this;
        res = getResources();
        dl = new DataLayer(res, context);


        DynamicQuestions.resource(res);
        setupGPS();

        dynamicQuestions = DynamicQuestions.getInstance(this, Utilities.getLocaleSuffix(res), dl);

        dynamicQuestions.questionsSection = (LinearLayout) findViewById(R.id.questionsSection);
        dynamicQuestions.inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        dynamicQuestions.reportingYear = getReportingYear();
        LinearLayout viewSurveyAnswer = (LinearLayout) findViewById(R.id.view_surveyans);
        viewSurveyAnswer.setVisibility(View.GONE);
        subModules = dl.getSubmodulesByModuleId(moduleId);

        Utilities.subModuleId = subModuleId;
        Utilities.farmerId = farmerId;
        Utilities.olamFarmerId = olamFarmerId;
        Utilities.moduleId = (int)moduleId;

        Utilities.farmerModuleId = farmerModuleId;

        Utilities.packagename=getPackageName();

        Utilities.productId = dl.getIdByFarmerId(farmerId,"Product");
        Utilities.countryId = dl.getIdByFarmerId(farmerId,"Country");
        Utilities.type = CHILDPRODUCT;
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

        initDynamicQuestions();
        setupSubModuleQuestions();
        try {
            setupTopSection();
        }catch (Exception e){}


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
    }

    @SuppressWarnings("ConstantConditions")
    private void initDynamicQuestions() {
        DynamicQuestions.getInstance().answerSet = new ArrayList<>();
        DynamicQuestions.getInstance().hundredpercentquestions = new ArrayList<>();
        DynamicQuestions.getInstance().context = context;
        DynamicQuestions.getInstance().updatingHundredPercent = false;
        DynamicQuestions.getInstance().questionColumnSuffix = Utilities.getLocaleSuffix(res);
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
            }
        });

        nextSaveSubmodule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLastPage) {

                    try{
                        if (mCurrentCapturedLocation!=null){}else{
                        if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
                            gpsTracker.getDeviceLocation();
                            return;
                        }
                        }
                    }catch (Exception e){}

                    boolean isMandated = false, isMinVal = false;
                    try {
                        isMandated = dynamicQuestions.isAllRequiredSubmoduleAnswered(questions);
                        isMinVal = dynamicQuestions.CheckMinMaxforQuestion(questions);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    if (!isMandated) {
                        Toast.makeText(ChildLabourCompleteModuleActivity.this, res.getString(R.string.restriction_in_submodules), Toast.LENGTH_SHORT).show();
                        return;
                    } else if (!isMinVal) {
                        Toast.makeText(ChildLabourCompleteModuleActivity.this, res.getString(R.string.valid_range), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ContentValues values = new ContentValues();
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "childmodule");
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, moduleName);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, childNameQueue);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, farmerNameQueue);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, farmId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID, 0);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID, farmerModuleId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, recordedLat);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, recordedLng);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID, children.getId());
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_SURVEY_CODE,childModuleDto.getSurveyCode()) ;
                    if (statusFinished == 0) {
                        values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                        networkQueueId = dl.insertNetworkQueueEntry(values);
                    } else {
                        dl.updateNetworkQueueFarmerAndChildModuleEntry(values, farmerModuleId,"childmodule",0);
                    }
                    statusFinished = 1;
                    saveSubModule("finish");
                } else {
                    saveSubModule("next");
                }
            }
        });

    }

    private void saveSubModule(String action) {

        // returning now to avoid the duplicates

        boolean canSaveAndFinish = true;

        // check location
        if (action.equalsIgnoreCase("finish")) {
            if (mCurrentCapturedLocation!=null){}else {
                if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
                    gpsTracker.getDeviceLocation();
                    return;
                }
            }
        }

        // first time saving this module?
        if (farmerModuleId == 0) {
            statusStarted = 1;
            farmerModuleId = dl.insertFarmerChildModule(moduleId, farmerId, olamFarmerId, farmId, subModuleId, statusStarted, recordedLat, recordedLng,children.getId());
            if (isLastPage && networkQueueId > 0) {
                ContentValues values = new ContentValues();
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID, farmerModuleId);
                dl.updateNetworkQueueEntry(values, networkQueueId);
            }
        }


        dl.updateSubModuleAnswers(dynamicQuestions.answerSet, farmerModuleId, subModuleId, farmerId, olamFarmerId, farmId, "module");

        if (action.equalsIgnoreCase("next")) {
            subModules.moveToNext();
            subModuleId = subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
        } else if (action.equalsIgnoreCase("back")) {
            subModules.moveToPrevious();
            subModuleId = subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
        }

        if (action.equalsIgnoreCase("exit") || action.equalsIgnoreCase("finish")) {
            if (!dynamicQuestions.requiredFieldsAnsweredChild((int) farmerModuleId, (int) moduleId)) {
                statusFinished = 0;
                canSaveAndFinish = false;
                dl.deleteNetworkQueueItemByFarmerChildModuleId((int) farmerModuleId);
                Toast.makeText(context, res.getString(R.string.complete_all_required_fields), Toast.LENGTH_LONG).show();
            }
        }

        dl.updateFarmerChildModule(farmerModuleId, subModuleId, statusStarted, statusFinished);

        if (action.equalsIgnoreCase("exit")) {
            ViewFarmerActivity.farmerId = farmerId;
            //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
            Intent intent = new Intent(context, ViewFarmerActivity.class);
            context.startActivity(intent);
        } else if (action.equalsIgnoreCase("finish")) {

            if (canSaveAndFinish) {

                if (dynamicQuestions.requiredFieldsAnsweredChild((int) farmerModuleId, (int) moduleId) && !editMode) {
                    collectGPSPoint();
                    gpsTracker.stopLocationUpdates();
                }

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

        }
    }

    private void collectGPSPoint() {

        TrackDetailsDTO trackDetailsDTO=new TrackDetailsDTO();
        trackDetailsDTO.setModule(GPSConstants.COLUMN_NAME_CHILD_SURVEY);
        trackDetailsDTO.setLatitude(String.valueOf(mCurrentCapturedLocation.getLatitude()));
        trackDetailsDTO.setLongitude(String.valueOf(mCurrentCapturedLocation.getLongitude()));
        trackDetailsDTO.setAccuracy(String.valueOf(mCurrentCapturedLocation.getAccuracy()));
        trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
        trackDetailsDTO.setFarmerId(String.valueOf(farmerId));
        trackDetailsDTO.setFarmerName(farmerName);
        trackDetailsDTO.setChildId(String.valueOf(children.getId()));
        trackDetailsDTO.setChildName(children.getName());
        trackDetailsDTO.setSurveyName(moduleName);


        ContentValues geoContentValues=new GPSUtils().getGeoContentValues(trackDetailsDTO);
        new GpsTrackerLogic().insertGeoDataDetails(this,geoContentValues);
    }

    private void bindSaveButton() {
        ImageButton saveButton = (ImageButton) findViewById(R.id.saveProgress);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSubModule("exit");
            }
        });
    }
    private void moveToCurrentSubModule(int subModuleId) {
        while (subModules.moveToNext()) {
            if (subModuleId == subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID))) {
                break;
            }
        }
    }

    private void setupTopSection() {
        if (moduleName!=null && subModules!=null) {
            try {
                TextView moduleNameTv = (TextView) findViewById(R.id.completeModuleModuleTitle);
                TextView subModuleNameTv = (TextView) findViewById(R.id.completeModuleSubModuleTitle);
                moduleNameTv.setText(moduleName);
                int columnIndex = (subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + dynamicQuestions.questionColumnSuffix) != -1) ? subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + dynamicQuestions.questionColumnSuffix) : subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME);
                subModuleNameTv.setText(subModules.getString(columnIndex));
            } catch (Exception e) {
            }
        }

    }

    // method to setup submodule questions
    private void setupSubModuleQuestions() {
        // loop through questions and generate the right control for each
//        while (questions.moveToNext()) {
        if(questions != null && questions.moveToFirst()) {
            do {
                String displayType = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));
                final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                final String questionAssignmentType = dl.getQuestionAssignmentTypeId(questionId);
                final String questionAssignmentField = dl.getQuestionAssignmentFieldId(questionId);
                long farmerId = Utilities.farmerId;

                if (questionAssignmentField.equals("[]") || questionAssignmentField.equals("null")) {
                    if (displayType.replaceAll(" ", "").toLowerCase().equals("checkbox")) {
                        dynamicQuestions.renderCheckBoxQuestion(questions, dynamicQuestions.questionsSection, farmerId, farmerModuleId, false, false, -1, null, 0);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("datepicker")) {
                        dynamicQuestions.renderDatePickerQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, false);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("decimal")) {
                        dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("dropdown")) {
                        dynamicQuestions.renderDropDownQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photocheckbox")) {
                        dynamicQuestions.renderPhotoCheckboxQuestion(questions, farmerId, farmerModuleId, res, getPackageName(), false);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("repeater")) {
                        dynamicQuestions.renderRepeaterQuestion(questions, farmerId, olamFarmerId, farmerModuleId, res, false);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("textbox")) {
                        dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("yesno") || displayType.replaceAll(" ", "").toLowerCase().equals("yes/no")) {
                        dynamicQuestions.renderYesNoQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("farmtreedensity")) {
                        dynamicQuestions.renderTreeDensityQuestion(questions, res);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photoupload")) {
                        dynamicQuestions.renderPhotoUploadQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                    } else if (displayType.replaceAll(" ", "").toLowerCase().equals("radio")) {
                        dynamicQuestions.renderRadioQuestion(questions, false, dynamicQuestions.questionsSection, farmerId, farmerModuleId, false, -1, null, 0);
                    }

                } else {
                    String[] stringArrayQuestion = questionAssignmentField.substring(1, questionAssignmentField.length() - 1).split(",");

                    // Convert the string array to an integer array
                    int[] intArrayQuestion = new int[stringArrayQuestion.length];
                    for (int m = 0; m < stringArrayQuestion.length; m++) {
                        intArrayQuestion[m] = Integer.parseInt(stringArrayQuestion[m]);
                    }

                    // Iterate through the integer array using a for loop
                    for (int fieldMultipleAssignmentQuestion : intArrayQuestion) {

                        String farmerAssignmentFieldQuestion = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(questionAssignmentType));
                        if (String.valueOf(fieldMultipleAssignmentQuestion).equals(farmerAssignmentFieldQuestion)) {
                            if (displayType.replaceAll(" ", "").toLowerCase().equals("checkbox")) {
                                dynamicQuestions.renderCheckBoxQuestion(questions, dynamicQuestions.questionsSection, farmerId, farmerModuleId, false, false, -1, null, 0);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("datepicker")) {
                                dynamicQuestions.renderDatePickerQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, false);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("decimal")) {
                                dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("dropdown")) {
                                dynamicQuestions.renderDropDownQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photocheckbox")) {
                                dynamicQuestions.renderPhotoCheckboxQuestion(questions, farmerId, farmerModuleId, res, getPackageName(), false);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("repeater")) {
                                dynamicQuestions.renderRepeaterQuestion(questions, farmerId, olamFarmerId, farmerModuleId, res, false);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("textbox")) {
                                dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("yesno") || displayType.replaceAll(" ", "").toLowerCase().equals("yes/no")) {
                                dynamicQuestions.renderYesNoQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("farmtreedensity")) {
                                dynamicQuestions.renderTreeDensityQuestion(questions, res);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photoupload")) {
                                dynamicQuestions.renderPhotoUploadQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                            } else if (displayType.replaceAll(" ", "").toLowerCase().equals("radio")) {
                                dynamicQuestions.renderRadioQuestion(questions, false, dynamicQuestions.questionsSection, farmerId, farmerModuleId, false, -1, null, 0);
                            }
                        }

                    }
                }


            } while (questions.moveToNext());
        }


    }
    private int getReportingYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
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
                if(mCurrentLocation != null && !GPSUtils.isMockLocation(mCurrentLocation))
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

    private void initData() {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null) {
                children = (ChildrenDTO) bundle.getSerializable("children");
                childModuleDto= (ChildModuleDto) bundle.getSerializable("childModule");
                try {
                    farmerID=Long.parseLong(bundle.getString(ChildLabourUtils.CHILD_FARMER_ID));
                }catch (Exception e){}
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
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (requestCode == 100 && resultCode == RESULT_OK) {
//            Toast.makeText(AddInfrastructureActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                    gpsTracker.getDeviceLocation();
                    startLocationUpdates();
                } else if (requestCode == 100 && resultCode == RESULT_CANCELED) {
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

                                if(mCurrentLocation != null && !GPSUtils.isMockLocation(mCurrentLocation)) {
                                    setPoiGPS(mCurrentLocation);
                                }else{
                                   /* if (pd != null && pd.getWindow().getDecorView().getWindowToken() != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }*/
                                    Toast.makeText(ChildLabourCompleteModuleActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
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
                                Toast.makeText(ChildLabourCompleteModuleActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                            }
                        }, 1000);

                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(ChildLabourCompleteModuleActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
//                                Log.e(TAG, errorMessage);

                                Toast.makeText(ChildLabourCompleteModuleActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                                break;
                            default:
                                Toast.makeText(ChildLabourCompleteModuleActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();

                        }
                    }
                });
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private String getColumnName(String assignmentType) {
        String assignmentTypeColumn = "";
        switch (assignmentType) {
            case "1":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID;
                break;
            case "2":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID;
                break;
            case "3":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID;
                break;
            case "4":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID;
                break;
            case "5":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID;
                break;
            case "6":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID;
                break;
            case "7":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID;
                break;
            case "8":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID;
                break;
            case "9":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID;
                break;
        }
        return assignmentTypeColumn;
    }

}
