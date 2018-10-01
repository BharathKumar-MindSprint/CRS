package com.olam.digital.ofis;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.model.ModuleDTO;
import com.olam.digital.ofis.model.SubModuleDTO;
import com.olam.digital.ofis.R;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.UUID;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class AddInfrastructureActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        // update
        // for edit mode
        public static long infrastructureID = 0;
        public static boolean editMode = false;
        private int selectedCountry = 0;
        private int selectedRegion = 0;
        private int selectedDistrict = 0;
        private int selectedProduct = 0;
        private int selectedFarmerGroup = 0;

        // for GPS location system
     //   private GoogleApiClient googleApiClient;
        private EditText gpsField;
        private ImageButton requestLocation;
        private Location poiLocation;
        private String recorded_lat, recorded_lng;

    // colours for animation
    private static final int COLOUR_SCHOOL = 0xFF924c2e;
    //    private static final int COLOUR_HEALTH_CENTRE = 0xFFbe3a34;
    //    private static final int COLOUR_CLEAN_WATER_SUPPLY = 0xFF253746;
    //    private static final int COLOUR_BUYING_CENTRE = 0xFFb0aa7e;
    private int currentColour = COLOUR_SCHOOL;
    private static final String CURRENT_LAT = "CURRENT_LAT";
    private static final String CURRENT_LNG = "CURRENT_LNG";
    private static final int PERMISSIONS_ACCESS_LOCATION = 6575;

    //types of POI, we use these to pass into various helper methods
    private static final int SCHOOL = 0;
    //    private static final int HEALTH_CENTRE = 1;
    //    private static final int CLEAN_WATER_SUPPLY = 2;
    //    private static final int BUYING_CENTRE = 3;
    private int currentTypeOfPOI = SCHOOL;
    private int currentSubTypeOfPOI = 0;
    //ui poi components
    private Spinner school_genderspinner = null;
    private Spinner school_educationlevelss = null;
    private Spinner school_yearOpened = null;
    private Spinner spinner_typeOfCleanWater;
    private Spinner cleanWater_yearOpened = null;
    private Spinner buyingCentreSpinner_product;
    private Spinner buyingCentreSpinner_farmergroup;
    private Spinner healthCentre_yearOpened;
    private EditText school_editTextNumberOfClassrooms = null;
    private EditText poiName;
    private EditText school_editTextCapacity = null;

    private final String[] since1950 = Utilities.since1950();

    private Context context;
    private Resources res;
    private DataLayer dl;

    private Spinner subModulesSpinner;
    private Spinner modulesSpinner;
    private boolean spinners_ready = false;

    // translatable dropdown values
    private final Hashtable<String, String> poitypes = new Hashtable<>();
    private final Hashtable<String, String> genders = new Hashtable<>();
    private final Hashtable<String, String> educationlevels = new Hashtable<>();
    private final Hashtable<String, String> cleanwatertypes = new Hashtable<>();

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private DynamicQuestions dynamicQuestions;
    private Cursor subModules, questions,modules;
    private int moduleId;
    private int subModuleId;

    ArrayList<SubModuleDTO> subModuleDTOS;
    ArrayList<ModuleDTO> moduleDTOS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_infrastructure);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        res = getResources();

//        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS && googleApiClient == null) {
//            googleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }

        // generate hashtable items
//        String localeSuffix = Utilities.getLocaleSuffix(res);
//        poitypes.put("School"+localeSuffix, res.getString(R.string.poitype_school));
//        poitypes.put("Health centre"+localeSuffix, res.getString(R.string.poitype_healthcentre));
//        poitypes.put("Clean water supply"+localeSuffix, res.getString(R.string.poitype_cleanwatersupply));
//        poitypes.put("Buying centre"+localeSuffix, res.getString(R.string.poitype_buyingcentre));
//
//        genders.put("Male"+localeSuffix, res.getString(R.string.gender_school_male));
//        genders.put("Female"+localeSuffix, res.getString(R.string.gender_school_female));
//        genders.put("Coeducational"+localeSuffix, res.getString(R.string.gender_school_coeducational));
//
//        educationlevels.put("Nursery"+localeSuffix, res.getString(R.string.educationlevel_nursery));
//        educationlevels.put("Primary"+localeSuffix, res.getString(R.string.educationlevel_primary));
//        educationlevels.put("Junior Secondary"+localeSuffix, res.getString(R.string.educationlevel_juniorsecondary));
//        educationlevels.put("Senior Secondary"+localeSuffix, res.getString(R.string.educationlevel_seniorsecondary));
//        educationlevels.put("Graduate"+localeSuffix, res.getString(R.string.educationlevel_graduate));
//
//        cleanwatertypes.put("Borehole"+localeSuffix, res.getString(R.string.cleanwatertype_borehole));
//        cleanwatertypes.put("Well"+localeSuffix, res.getString(R.string.cleanwatertype_well));
//        cleanwatertypes.put("Pipe"+localeSuffix, res.getString(R.string.cleanwatertype_pipe));
//        cleanwatertypes.put("Spring"+localeSuffix, res.getString(R.string.cleanwatertype_spring));
//
//
//
        dl = new DataLayer(res, context);
        poiName = (EditText) findViewById(R.id.poiName);
        subModulesSpinner = (Spinner) findViewById(R.id.spinner_submodules);
        modulesSpinner=(Spinner) findViewById(R.id.spinner_modules);

        requestLocation = (ImageButton) findViewById(R.id.requestLocation);
        bindGpsButton();

        if (savedInstanceState != null) {
            recorded_lat = savedInstanceState.getString(CURRENT_LAT);
            recorded_lng = savedInstanceState.getString(CURRENT_LNG);
        }

        //bindPoiSpinner();
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
               if(moduleDTOS!=null && moduleDTOS.size()>0) {
                   moduleId = moduleDTOS.get(0).getId();
                   bindPoiSpinner(moduleId);
                   subModuleId = subModuleDTOS.get(0).getId();
               }

//            swapPoiType(SCHOOL);
        }


        int selectedPlace = 0;
        dl.createGeographyHierarchySpinners(this, context, null, selectedCountry, selectedRegion, selectedDistrict, selectedPlace, null);

        bindSaveButton();

    }




    private void setupSubModuleQuestions() {
        // loop through questions and generate the right control for each
        while (questions.moveToNext()) {
            String displayType = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_TYPE));
            if (displayType.toLowerCase().equals("checkbox")) {
                dynamicQuestions.renderCheckBoxQuestionForSIP(questions, dynamicQuestions.questionsSection);
            } else if (displayType.toLowerCase().equals("datepicker")) {
                dynamicQuestions.renderDatePickerQuestionForSip(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().equals("decimal")) {
                dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().equals("dropdown")) {
                dynamicQuestions.renderDropDownQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
            } else if (displayType.toLowerCase().equals("photocheckbox")) {
                dynamicQuestions.renderPhotoCheckboxQuestionForSIP(questions, res, getPackageName());
            } else if (displayType.toLowerCase().equals("repeater")) {
                dynamicQuestions.renderRepeaterQuestionForSIP(questions, res);
            } else if (displayType.toLowerCase().equals("textbox")) {
                dynamicQuestions.renderTextBoxQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null);
            } else if (displayType.toLowerCase().equals("yesno")) {
                dynamicQuestions.renderYesNoQuestion(questions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
            } else if (displayType.toLowerCase().equals("farmtreedensity")) {
                dynamicQuestions.renderTreeDensityQuestion(questions, res);
            } else if (displayType.toLowerCase().equals("photoupload")) {
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
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
       // googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
      //  googleApiClient.disconnect();
        super.onStop();
    }

    private void doLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_ACCESS_LOCATION);
            return;
        } else {
//            if (googleApiClient != null && googleApiClient.isConnected()) {
//                poiLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//            }
            if (poiLocation != null) {
                recorded_lat = String.valueOf(poiLocation.getLatitude());
                recorded_lng = String.valueOf(poiLocation.getLongitude());
            }
            requestLocation.callOnClick();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
      //  doLocationUpdate();
    }

    private void showSettingsDialog(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.permission_permanent_denial_title);
        dialog.setMessage(msg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName())));
                    }
                })
                .setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(AddInfrastructureActivity.this, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
                    }
                });
        dialog.show();
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
                      //  doLocationUpdate();
                    } else {
                        // Checkbox set to Never Ask Again
                        showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
                    }
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
      //  googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingError) {
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch(IntentSender.SendIntentException e) {
              //  googleApiClient.connect();
            }
        } else {
            GoogleApiAvailability.getInstance().showErrorDialogFragment(this, connectionResult.getErrorCode(), REQUEST_RESOLVE_ERROR);
            mResolvingError = true;
        }
        FirebaseCrash.report(new Throwable("Location connection failed adding infrastructure : code: " + connectionResult.getErrorCode() + " | message: " + connectionResult.getErrorMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
//                if (!googleApiClient.isConnecting() &&
//                        !googleApiClient.isConnected()) {
//                    googleApiClient.connect();
//                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        new CountDownTimer(100, 100) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                spinners_ready = !editMode;//true;
            }
        }.start();

    }

    @SuppressLint("SetTextI18n")
    private void populateExistingValues(final Cursor existingItem) {
        gpsField = (EditText) findViewById(R.id.gpsPosition);
        int selectedModulePOI = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE_ID));
        int selectedSubModulePOI = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE_ID));
        recorded_lat = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT));
        recorded_lng = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG));
        gpsField.setText(recorded_lat + "," + recorded_lng);

        poiName.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));

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
        /*        String currentPoiType = Utilities.getDefaultTextByLocalValue(poitypes, existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE)), res);

        if (currentPoiType.equalsIgnoreCase("school")) {
            currentTypeOfPOI = SCHOOL;
        } else if (currentPoiType.equalsIgnoreCase("buying centre")) {
            currentTypeOfPOI = BUYING_CENTRE;
        } else if (currentPoiType.equalsIgnoreCase("health centre")) {
            currentTypeOfPOI = HEALTH_CENTRE;
        } else if (currentPoiType.equalsIgnoreCase("clean water supply")) {
            currentTypeOfPOI = CLEAN_WATER_SUPPLY;
        }

        swapPoiType(currentTypeOfPOI);
        subModulesSpinner.setSelection(currentTypeOfPOI);

        if (currentPoiType.equalsIgnoreCase("school")) {
            currentTypeOfPOI = SCHOOL;
            poiName.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
            school_editTextCapacity.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CAPACITY)));
            school_editTextNumberOfClassrooms.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_NUM_CLASSROOMS)));

            new CountDownTimer(1000, 100) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    String existingGenderMix = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_GENDER_MIX));
                    String existingGenderMixKey = existingGenderMix + Utilities.getLocaleSuffix(res);
                    if (genders.containsKey(existingGenderMixKey)) {
                        existingGenderMix = genders.get(existingGenderMixKey);
                    }

                    Utilities.setSpinnerSelectionByValue(existingGenderMix, school_genderspinner);
                    String existingEducationLevel = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SCHOOL_TYPE));
                    String existingEducationLevelKey = existingEducationLevel + Utilities.getLocaleSuffix(res);
                    if (educationlevels.containsKey(existingEducationLevelKey)) {
                        existingEducationLevel = educationlevels.get(existingEducationLevelKey);
                    }

                    Utilities.setSpinnerSelectionByValue(existingEducationLevel, school_educationlevelss);
                    Utilities.setSpinnerSelectionByValue(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED)), school_yearOpened);
                }
            }.start();


        } else if (currentPoiType.equalsIgnoreCase("buying centre")) {
            currentTypeOfPOI = BUYING_CENTRE;
            poiName.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
            selectedProduct = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PRODUCT_ID));
            selectedFarmerGroup = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMERGROUP_ID));

            new CountDownTimer(1000, 100) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    dl.setSpinnerSelectionById(selectedProduct, buyingCentreSpinner_product);
                    dl.setSpinnerSelectionById(selectedFarmerGroup, buyingCentreSpinner_farmergroup);
                }
            }.start();


        } else if (currentPoiType.equalsIgnoreCase("health centre")) {
            currentTypeOfPOI = HEALTH_CENTRE;
            poiName.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));

            new CountDownTimer(1000, 100) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    Utilities.setSpinnerSelectionByValue(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED)), healthCentre_yearOpened);
                }
            }.start();
        } else if (currentPoiType.equalsIgnoreCase("clean water supply")) {
            currentTypeOfPOI = CLEAN_WATER_SUPPLY;
            new CountDownTimer(1000, 100) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    String existingCwsType = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CWS_TYPE));
                    String existingCwsTypeKey = existingCwsType + Utilities.getLocaleSuffix(res);
                    if (cleanwatertypes.containsKey(existingCwsTypeKey)) {
                        existingCwsType = cleanwatertypes.get(existingCwsTypeKey);
                    }

                    Utilities.setSpinnerSelectionByValue(existingCwsType, spinner_typeOfCleanWater);
                    Utilities.setSpinnerSelectionByValue(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED)), cleanWater_yearOpened);
                }
            }.start();
        }

        // set in all cases except buying centres
        if (!currentPoiType.equalsIgnoreCase("buying centre")) {
            Switch olamFunded = (Switch) findViewById(R.id.olamFunded);
            olamFunded.setChecked((existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FUNDED)) != 0));
        }

        recorded_lat = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT));
        recorded_lng = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG));
        gpsField.setText(recorded_lat + "," + recorded_lng);*/

        selectedCountry = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID));
        selectedRegion = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID));
        selectedDistrict = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID));
        selectedProduct = existingItem.getInt(existingItem.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PRODUCT_ID));
    }

    private ContentValues validateValues() {
        ContentValues values = new ContentValues();

        // common
        Spinner spinner_country = (Spinner) findViewById(R.id.spinner_country);
        long country_id = spinner_country.getSelectedItemId();
        if (country_id < 1) {
            return values;
        }

        Spinner spinner_region = (Spinner) findViewById(R.id.spinner_region);
        long region_id = spinner_region.getSelectedItemId();
        if (region_id < 1) {
            return values;
        }

        Spinner spinner_district = (Spinner) findViewById(R.id.spinner_district);
        long district_id = spinner_district.getSelectedItemId();
        if (district_id < 1) {
            return values;
        }

        if (poiLocation == null && !editMode) {
            return values;
        }
        if (poiLocation != null && !editMode) {
            recorded_lat = String.valueOf(poiLocation.getLatitude());
            recorded_lng = String.valueOf(poiLocation.getLongitude());
        }
/*
        int olamFundedValue = 0;
        if (currentTypeOfPOI != BUYING_CENTRE) {
            Switch olamFunded = (Switch) findViewById(R.id.olamFunded);
            olamFundedValue = (olamFunded.isChecked()) ? 1 : 0;
        }

        switch (currentTypeOfPOI) {
            case SCHOOL:
                if (String.valueOf(poiName.getText()).equals("")) {
                    return values;
                }
                school_editTextCapacity = (EditText) findViewById(R.id.editText_capacity);
                if (String.valueOf(school_editTextCapacity.getText()).equals("")) {
                    return values;
                }
                school_editTextNumberOfClassrooms = (EditText) findViewById(R.id.editText_numberOfClassRooms);
                if (String.valueOf(school_editTextNumberOfClassrooms.getText()).equals("")) {
                    return values;
                }
                break;
            case BUYING_CENTRE:
                Spinner spinner_farmergroup = (Spinner) findViewById(R.id.spinner_farmergroup);
                long farmergroup_id = spinner_farmergroup.getSelectedItemId();
                if (farmergroup_id < 1) {
                    return values;
                }
                break;
            case HEALTH_CENTRE:
                if (String.valueOf(poiName.getText()).equals("")) {
                    return values;
                }
                break;
            default:
        }*/

        try {
            // common
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "infrastructure");
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE_ID, String.valueOf(modulesSpinner.getSelectedItemPosition()));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID, String.valueOf(moduleId));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, String.valueOf(modulesSpinner.getSelectedItem()));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE_ID, String.valueOf(subModulesSpinner.getSelectedItemPosition()));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBMODULE_ID, String.valueOf(subModuleId));
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE, String.valueOf(subModulesSpinner.getSelectedItem()));
            if (/*currentTypeOfPOI != CLEAN_WATER_SUPPLY | */!modulesSpinner.getSelectedItem().equals("Clean water supply")) {
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, String.valueOf(poiName.getText()));
            } else {
                values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, String.valueOf(subModulesSpinner.getSelectedItem()));
            }
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT, recorded_lat);
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG, recorded_lng);
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID, country_id);
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID, region_id);
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, district_id);
/*
            switch (currentTypeOfPOI) {
                case SCHOOL:

                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CAPACITY, String.valueOf(school_editTextCapacity.getText()));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_NUM_CLASSROOMS, String.valueOf(school_editTextNumberOfClassrooms.getText()));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_GENDER_MIX, Utilities.getDefaultTextByLocalValue(genders, String.valueOf(school_genderspinner.getSelectedItem()), res));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SCHOOL_TYPE, Utilities.getDefaultTextByLocalValue(educationlevels, String.valueOf(school_educationlevelss.getSelectedItem()), res));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED, Integer.valueOf(school_yearOpened.getSelectedItem().toString()));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FUNDED, olamFundedValue);
                    break;
                case BUYING_CENTRE:
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PRODUCT_ID, buyingCentreSpinner_product.getSelectedItemId());
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMERGROUP_ID, buyingCentreSpinner_farmergroup.getSelectedItemId());
                    break;
                case HEALTH_CENTRE:
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED, Integer.valueOf(healthCentre_yearOpened.getSelectedItem().toString()));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FUNDED, olamFundedValue);
                    break;
                case CLEAN_WATER_SUPPLY:
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CWS_TYPE, Utilities.getDefaultTextByLocalValue(cleanwatertypes, String.valueOf(spinner_typeOfCleanWater.getSelectedItem()), res));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED, Integer.valueOf(cleanWater_yearOpened.getSelectedItem().toString()));
                    values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FUNDED, olamFundedValue);
                    break;
                default:
            }*/

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
                }else{
                    uploadId = UUID.randomUUID().toString();

                }
                if(uploadId != null)
                    dl.updateSIPSubModuleAnswers(dynamicQuestions.answerSet, subModuleId, 0, uploadId);

                if (values.size() > 0 && uploadId != null &&  dynamicQuestions.requiredFieldsSIPAnswered(uploadId, moduleId, subModuleId)) {
                    if (editMode) {
                        // edit POI call
                        if (dl.updateNetworkQueueEntry(values, infrastructureID) > 0) {
                            Toast.makeText(context, res.getString(R.string.Successfully_updated), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, HomeActivity.class);
                            startActivity(intent);
                        }
                    } else {

                        // insert POI
//                        long newPoiId = dl.insertNetworkQueueSipEntry(dynamicQuestions.answerSet, subModuleId, 0, values, uploadId);
                        long newPoiId = dl.insertNetworkQueueSipEntry(values, uploadId);
                        if (newPoiId != -1) {
                            Toast.makeText(context, res.getString(R.string.Successfully_added), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, HomeActivity.class);
                            startActivity(intent);

                        }

                    }

                } else {
                    Toast.makeText(context, res.getString(R.string.Please_fill_all_fields), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private ProgressDialog pd;
    private LocationListener locationListener;

    private void setPoiGPS(Location location) {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
//        if (googleApiClient != null && googleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
//        }
        try {
          //  poiLocation = (Location) msg.obj;
            poiLocation=location;
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
            gpsField.setText(locationResult);

        } catch (Exception e) {
            FirebaseCrash.report(new Exception("AddInfrastructureActivity: Error setting POI location", e));
            e.printStackTrace();
        }
    }

    private void getPoiGPS() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                if (pd != null) {
                    pd = ProgressDialog.show(context, res.getString(R.string.Please_wait), res.getString(R.string.searching_for_loc), true);
                }
            }
        });

        @SuppressLint("HandlerLeak") Handler gpsHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
              //  setPoiGPS(msg);
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

//        if (googleApiClient != null && googleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
//        }

        gpsHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (googleApiClient.isConnected()) {
//                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
//                }
                if (pd != null) {
                    pd.setMessage(res.getString(R.string.record_location_timeout));
                    Utilities.closeProgressDialog(pd, 3000);
                }
            }
        }, 30000);
    }

    private void bindGpsButton() {
        requestLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocation.setImageResource(R.drawable.ic_actionbutton_location_searching);
               // getPoiGPS();
                if (ActivityCompat.checkSelfPermission(AddInfrastructureActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddInfrastructureActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    startLocationPermissionRequest();
                    return;
                }
                getDeviceLocation();
            }
        });

    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_ACCESS_LOCATION);
    }
    private void animateBetweenColours(int from, final int to, final ImageView layout)
    {
        Integer colorFrom = from;
        Integer colorTo = to;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator)
            {
                Integer colour = (Integer)animator.getAnimatedValue();
                layout.setBackgroundColor(colour);
                if (colour==to)
                {
                    currentColour=to;
                }
            }

        });
        colorAnimation.setDuration(690);
        colorAnimation.start();
    }

    private ArrayList<ModuleDTO> getModules(){
        modules = dl.getSIPModules();
        moduleDTOS=new ArrayList<ModuleDTO>();
        ModuleDTO moduleDTO;
        while (modules.moveToNext()) {
            moduleDTO=new ModuleDTO();
            moduleDTO.setId(modules.getInt(modules.getColumnIndex(OFISDBContract.SipGroupEntry.COLUMN_NAME_ID)));
            moduleDTO.setModuleName(modules.getString(modules.getColumnIndex(OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME)));
            moduleDTOS.add(moduleDTO);
        }
        return moduleDTOS;
    }


    private ArrayList<SubModuleDTO> getSubModules(long moduleId){
        subModules = dl.getSipSubmodulesByModuleId(moduleId);
        subModuleDTOS=new ArrayList<SubModuleDTO>();
        SubModuleDTO subModuleDTO;
        while (subModules.moveToNext()) {
            subModuleDTO=new SubModuleDTO();
            subModuleDTO.setId(subModules.getInt(subModules.getColumnIndex(OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID)));
            subModuleDTO.setSipName(subModules.getString(subModules.getColumnIndex(OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME)));
            subModuleDTOS.add(subModuleDTO);
        }
        return subModuleDTOS;
    }

    private void getSubModuleQuestions(int subModuleId){
        if (subModuleId != 0) {
            questions = dl.getQuestionsSipBySubModuleId(subModuleId);
            dynamicQuestions.hasAnswers = false;
            moveToCurrentSubModule(subModuleId);
        }
        setupSubModuleQuestions();
    }

    private void swapPoiType(int type) {
        ImageView topSection = (ImageView) findViewById(R.id.topSectionBackground);

       /*  LinearLayout olamFundedWrapper = (LinearLayout) findViewById(R.id.olamFundedWrapper);
        currentTypeOfPOI=type;

        LinearLayout additional_school          = (LinearLayout) findViewById(R.id.additional_school);
        LinearLayout additional_cleanwater      = (LinearLayout) findViewById(R.id.additional_cleanwater);
        LinearLayout additional_buyingcentre    = (LinearLayout) findViewById(R.id.additional_buyingcentre);
        LinearLayout additional_healthcentre    = (LinearLayout) findViewById(R.id.additional_healthcentre);
        dynamicQuestions.questionsSection.removeAllViews();
        switch(type)
        {
            case SCHOOL:
                animateBetweenColours(currentColour,COLOUR_SCHOOL,topSection);
                additional_school.setVisibility(View.VISIBLE);
                additional_cleanwater.setVisibility(View.GONE);
                additional_buyingcentre.setVisibility(View.GONE);
                additional_healthcentre.setVisibility(View.GONE);
                poiName.setVisibility(View.VISIBLE);
                olamFundedWrapper.setVisibility(View.VISIBLE);

                school_genderspinner = (Spinner) findViewById(R.id.genderspinner);
                ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.spinner_item, res.getStringArray(R.array.genders_2));
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                school_genderspinner.setAdapter(adapter);

                school_educationlevelss = (Spinner) findViewById(R.id.educationlevels);
                adapter = new ArrayAdapter<>(context, R.layout.spinner_item, res.getStringArray(R.array.educationlevels));
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                school_educationlevelss.setAdapter(adapter);

                school_yearOpened = (Spinner) findViewById(R.id.school_yearOpened);
                adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1950);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                school_yearOpened.setAdapter(adapter);

                school_editTextNumberOfClassrooms = (EditText) findViewById(R.id.editText_numberOfClassRooms);
                school_editTextCapacity = (EditText) findViewById(R.id.editText_capacity);

                subModuleId =175;
                break;
            case HEALTH_CENTRE:
                animateBetweenColours(currentColour,COLOUR_HEALTH_CENTRE,topSection);

                additional_healthcentre.setVisibility(View.VISIBLE);
                additional_school.setVisibility(View.GONE);
                additional_cleanwater.setVisibility(View.GONE);
                additional_buyingcentre.setVisibility(View.GONE);
                poiName.setVisibility(View.VISIBLE);
                olamFundedWrapper.setVisibility(View.VISIBLE);

                healthCentre_yearOpened = (Spinner) findViewById(R.id.healthCentre_yearOpened);
                adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1950);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                healthCentre_yearOpened.setAdapter(adapter);

                subModuleId =16;
                break;
            case CLEAN_WATER_SUPPLY:
                animateBetweenColours(currentColour,COLOUR_CLEAN_WATER_SUPPLY,topSection);

                additional_school.setVisibility(View.GONE);
                additional_cleanwater.setVisibility(View.VISIBLE);
                additional_buyingcentre.setVisibility(View.GONE);
                additional_healthcentre.setVisibility(View.GONE);
                poiName.setVisibility(View.GONE);
                olamFundedWrapper.setVisibility(View.VISIBLE);


                //and do any tweaking we need
                spinner_typeOfCleanWater = (Spinner) findViewById(R.id.spinner_typeOfCleanWater);
                adapter = new ArrayAdapter<>(context, R.layout.spinner_item, res.getStringArray(R.array.clean_water_types));
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner_typeOfCleanWater.setAdapter(adapter);

                cleanWater_yearOpened = (Spinner) findViewById(R.id.cleanWater_yearOpened);
                adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1950);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                cleanWater_yearOpened.setAdapter(adapter);

                subModuleId =17;
                break;
            case BUYING_CENTRE:
                animateBetweenColours(currentColour, COLOUR_BUYING_CENTRE, topSection);

                additional_school.setVisibility(View.GONE);
                additional_cleanwater.setVisibility(View.GONE);
                additional_buyingcentre.setVisibility(View.VISIBLE);
                additional_healthcentre.setVisibility(View.GONE);
                poiName.setVisibility(View.VISIBLE);
                olamFundedWrapper.setVisibility(View.GONE);

                buyingCentreSpinner_product = (Spinner) findViewById(R.id.spinner_product);

                dl.createProductSpinner(this, context, selectedProduct);

                buyingCentreSpinner_farmergroup = (Spinner) findViewById(R.id.spinner_farmergroup);
                Cursor farmerGroups = dl.getSpinnerData(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME);
                dl.bindDataDrivenSpinner(buyingCentreSpinner_farmergroup, farmerGroups, context, R.string.spinner_farmergroup_default, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME, false);
                if (farmerGroups.getCount() == 1) {
                    buyingCentreSpinner_farmergroup.setSelection(1, false);
                    buyingCentreSpinner_farmergroup.setEnabled(false);
                }

                subModuleId =18;
                break;
            default:
        }
        subModules = dl.getSubmodulesByModuleId(moduleId);
        if (subModuleId != 0) {
            questions = dl.getQuestionsBySubModuleId(subModuleId);
            dynamicQuestions.hasAnswers = false;

            moveToCurrentSubModule(subModuleId);
        }
        setupSubModuleQuestions();*/
    }

    private void bindModulesSpinner() {
        moduleDTOS=getModules();
        ArrayList<String> moduleNames= new ArrayList<String>();
        if(moduleDTOS.size()>0){
            for(ModuleDTO moduleDTO: moduleDTOS) {
                moduleNames.add(moduleDTO.getModuleName());
            }
        }

        ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.add_sip_spinner_item, moduleNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        modulesSpinner.setAdapter(adapter);

        modulesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (spinners_ready) {
                    /*int color;
                    ImageView topSection = (ImageView) findViewById(R.id.topSectionBackground);
                    if(position == 0){
                        color = currentColour;
                        animateBetweenColours(color,currentColour, topSection);

                    }else if(position > 0){
                       color = currentColour;
                       color +=0x2BEE06;
                       animateBetweenColours(currentColour,color,topSection);

                   }  */
                    moduleId = moduleDTOS.get(position).getId();
                    bindPoiSpinner(moduleId);
//                  subModuleId = subModuleDTOS.get(0).getId();
//                    getSubModuleQuestions(subModuleId);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void bindPoiSpinner(int moduleId) {
        subModuleDTOS=getSubModules(moduleId);
        ArrayList<String> subModuleNames= new ArrayList<String>();
        if(subModuleDTOS.size()>0){
            for(SubModuleDTO subModuleDTO: subModuleDTOS) {
                subModuleNames.add(subModuleDTO.getSipName());
            }
        }



        ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.add_sip_spinner_item, subModuleNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        subModulesSpinner.setAdapter(adapter);

        subModulesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (spinners_ready) {
//                    swapPoiType(i);
//                }
                    dynamicQuestions.questionsSection.removeAllViews();
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


    private void getDeviceLocation() {
        try {
            Task<Location> locationResult = getFusedLocationProviderClient(this).getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        setPoiGPS(location);
                    } else {
                        Toast.makeText(AddInfrastructureActivity.this,"No position logged",Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


}
