package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.R;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class CompleteModuleActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
    private long networkQueueId = 0;

    // for GPS location system
    private GoogleApiClient googleApiClient;
    private LocationListener locationListener;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_module);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

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

        context = this;
        res = getResources();
        dl = new DataLayer(res, context);


        DynamicQuestions.resource(res);
        setupGPS();

        dynamicQuestions = DynamicQuestions.getInstance(this, Utilities.getLocaleSuffix(res), dl);

        dynamicQuestions.questionsSection = (LinearLayout) findViewById(R.id.questionsSection);
        dynamicQuestions.inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        dynamicQuestions.reportingYear = getReportingYear();


        subModules = dl.getSubmodulesByModuleId(moduleId);

        Utilities.subModuleId = subModuleId;
        Utilities.farmerId = farmerId;
        Utilities.olamFarmerId = olamFarmerId;
        Utilities.moduleId = (int)moduleId;

        Utilities.farmerModuleId = farmerModuleId;

        Utilities.packagename=getPackageName();

        if (statusStarted != 0 || statusFinished != 0) {

            dynamicQuestions.hasAnswers = true;
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
            }catch (Exception e){}

        }

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
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (recordedLat.equalsIgnoreCase("") || recordedLng.equalsIgnoreCase("")) {
            recordCurrentGps();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingError) {
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                googleApiClient.connect();
            }
        } else {
            GoogleApiAvailability.getInstance().showErrorDialogFragment(this, connectionResult.getErrorCode(), REQUEST_RESOLVE_ERROR);
            mResolvingError = true;
        }
        FirebaseCrash.report(new Throwable("Location connection failed adding infrastructure : code: " + connectionResult.getErrorCode() + " | message: " + connectionResult.getErrorMessage()));
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

    }

    private void setCurrentGPS(Message msg) {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
        }
        try {
            Location moduleLocation = (Location) msg.obj;
            recordedLat = String.valueOf(moduleLocation.getLatitude());
            recordedLng = String.valueOf(moduleLocation.getLongitude());
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("CompleteModuleActivity: Error setting GPS values", e));
            e.printStackTrace();
        }
    }

    private void recordCurrentGps() {
        @SuppressLint("HandlerLeak") Handler gpsHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                setCurrentGPS(msg);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationListener = new getGPSLocation(gpsHandler);

        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
        }

        gpsHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (googleApiClient.isConnected()) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
                }
            }
        }, 30000);
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
        int columnIndex = (subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + dynamicQuestions.questionColumnSuffix) != -1) ? subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + dynamicQuestions.questionColumnSuffix) : subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME);
        subModuleNameTv.setText(subModules.getString(columnIndex));
    }

    private int getReportingYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!googleApiClient.isConnecting() &&
                        !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }
        }

        if (resultCode == RESULT_OK) {
            dynamicQuestions.setFullImageFromFilePath(res);
        }
    }




    private void setupSubModuleQuestions() {
        // loop through questions and generate the right control for each
        while (questions.moveToNext()) {
            String displayType = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));

            if (displayType.toLowerCase().equals("checkbox")) {
                dynamicQuestions.renderCheckBoxQuestion(questions, dynamicQuestions.questionsSection, farmerId, farmerModuleId);
            } else if (displayType.toLowerCase().equals("datepicker")) {
                dynamicQuestions.renderDatePickerQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().equals("decimal")) {
                dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().equals("dropdown")) {
                dynamicQuestions.renderDropDownQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
            } else if (displayType.toLowerCase().equals("photocheckbox")) {
                dynamicQuestions.renderPhotoCheckboxQuestion(questions, farmerId, farmerModuleId, res, getPackageName());
            } else if (displayType.toLowerCase().equals("repeater")) {
                dynamicQuestions.renderRepeaterQuestion(questions, farmerId, olamFarmerId, farmerModuleId, res);
            } else if (displayType.toLowerCase().equals("textbox")) {
                dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().equals("yesno") || displayType.toLowerCase().equals("yes/no")) {
                dynamicQuestions.renderYesNoQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
            } else if (displayType.toLowerCase().equals("farmtreedensity")) {
                dynamicQuestions.renderTreeDensityQuestion(questions, res);
            } else if (displayType.toLowerCase().equals("photoupload")) {
                dynamicQuestions.renderPhotoUploadQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
            }
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
            }
        });

        nextSaveSubmodule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLastPage) {
                    ContentValues values = new ContentValues();
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "module");
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, moduleName);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, farmerName);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, farmId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID, farmerModuleId);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, recordedLat);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, recordedLng);
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
                    if (statusFinished == 0) {
                        values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                        networkQueueId = dl.insertNetworkQueueEntry(values);
                    } else {
                        dl.updateNetworkQueueFarmerModuleEntry(values, farmerModuleId);
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
                saveSubModule("exit");
            }
        });
    }

    private void saveSubModule(String action) {
        boolean canSaveAndFinish = true;

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
            subModules.moveToNext();
            subModuleId = subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
        } else if (action.equalsIgnoreCase("back")) {
            subModules.moveToPrevious();
            subModuleId = subModules.getInt(subModules.getColumnIndex(OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID));
        }

        if (action.equalsIgnoreCase("exit") || action.equalsIgnoreCase("finish")) {
            if (!dynamicQuestions.requiredFieldsAnswered((int) farmerModuleId, (int) moduleId)) {
                statusFinished = 0;
                canSaveAndFinish = false;
                dl.deleteNetworkQueueItemByFarmerModuleId((int) farmerModuleId);
                Toast.makeText(context, res.getString(R.string.complete_all_required_fields), Toast.LENGTH_LONG).show();
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

        }
    }
}
