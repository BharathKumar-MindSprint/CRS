package com.olam.digital.ofis;

import static com.olam.digital.ofis.BaseActivity.UPDATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.olam.digital.ofis.childlabour.ChildLabourDetailsActivity;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.RegisterChildActivity;
import com.olam.digital.ofis.downloadsurvey.ViewPlotSurveyActivity;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.matoma.multifarm.UpdateMultiFarmActivity;

import com.olam.digital.ofis.multifarm.MultiFarmDTO;
import com.olam.digital.ofis.multifarm.MultiFarmUtils;

import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.FollowUp.FollowUpRecyclerAdapter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils;
import com.olam.digital.ofis.recordpolygon.RecordPolygonActivity;
import com.olam.digital.ofis.recordpolygon.SharedPreferenceUtils;
import com.olam.digital.ofis.recordpolygon.polygonhistory.InternetConnectionHelper;
import com.olam.digital.ofis.recordpolygon.polygonhistory.PolygonHistory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class ViewFarmerActivity extends BaseListActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private final String MATOMOTAG = this.getClass().getSimpleName();
    public static long farmerId;
    private Cursor farmer;
    private Context context;
    private static Context staticContext;
    private Resources res;
    private DataLayer dl;

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
    private static final String VIEW_MODULE = "VIEW_MODULE";
    private static final int PERMISSIONS_ACCESS_LOCATION = 6575;

    private RelativeLayout phoneNumbersHolder;
    private LinearLayout phonenumbers;
    private LinearLayout plotList;
    private LayoutInflater inflater;
    private Animation mSlideBottomUp;
    private Animation mSlideBottomDown;
    private View phoneNumbersListItem, clickedLocationItem;
    private long farmerPlaceId;

    // for GPS location system
//    private GoogleApiClient googleApiClient;
    private ProgressDialog pd;
    private LocationListener locationListener;

    // location last updated time
    private String mLastUpdateTime;

    // location updates interval - 2sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;

    // fastest updates interval - 1 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 7070;


    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    // Unique tag for the error dialog fragment
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    ArrayList<String> phoneNumberList;
    ArrayList<String> plotNameList;

    //JIRA req OW-505
    ImageButton farmerPhotoActionButton;
    // Runtime Permission Request Code
    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
    private final static int REQUEST_PERMISSIONS_FOR_TIRAMISU = 2509;
    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";

    private String farmerNameStr;
    private String olamFarmerIdStr;
    private boolean polygon = false;
    private boolean household = false;
    private ListView listView;

    // Location captured related variable
    private Location mCurrentCapturedLocation = null;
    GPSListener gpsListener = null;
    private GPSTracker gpsTracker = null;

    CustomListAdapter_Modules customListAdapter_modulesAdapter;


    ImageView infoCloseImageView,plotInfoCloseImageView,consentInfoCloseImageView,cert_info_close_btn;
    LinearLayout statusInfoLayout,plotStatusInfoLayout,consentStatusInfoLayout,cert_layout_notification;

    String multiFarmId=null;
    public static boolean farmSurvey;

    //Room Database
    OFISRoomDB db;
    String dmSurveyName;
    String  dmSubTitle,dmSubTitleDescription;
    String followUpCount;
    String moduleID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_farmer);
        //noinspection ConstantConditions
        listView = findViewById(R.id.list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        context = this;
        staticContext=this;
        ChildLabourUtils.isChildEnumerator = Utilities.getChildLabourPref(context, ChildLabourUtils.IS_CHILD_ENUMERATOR);
        MultiFarmUtils.isMultiFarmEnumerator = Utilities.getMultifarmPref(context, MultiFarmUtils.IS_MULTI_FARM_ENUMERATOR);

        db = OFISRoomDB.getDbInstance(context);
        res = getResources();
        dl = new DataLayer(res, context);
        pd = new ProgressDialog(context);





        //farmerId = SharedPreferenceUtils.getInstance(getApplicationContext()).getFarmerID();
        initiaizeInfoWidget();
        setupGPS();
        setupUI();
        setupPhoneNumberDialog();
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        gpsListener = new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location != null) {
                    mCurrentCapturedLocation = location;
                }
            }
        };
        gpsTracker = new GPSTracker(this, gpsListener);
        //   gpsTracker.getDeviceLocation();
//        if (gpsTracker.isGpsEnabled()){
        gpsTracker.startLocationUpdateInitially();
        //      }

        // Show popup OW-2909
        infoCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusInfoLayout.setVisibility(View.GONE);
            }
        });
        plotInfoCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plotStatusInfoLayout.setVisibility(View.GONE);
            }
        });
        consentInfoCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consentStatusInfoLayout.setVisibility(View.GONE);
            }
        });
        cert_info_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cert_layout_notification.setVisibility(View.GONE);
            }
        });
    }



    private void initiaizeInfoWidget() {
        infoCloseImageView = findViewById(R.id.info_close_btn);
        statusInfoLayout = findViewById(R.id.layout_notification);
        plotInfoCloseImageView = findViewById(R.id.plot_info_close_btn);
        plotStatusInfoLayout = findViewById(R.id.plot_layout_notification);
        consentInfoCloseImageView = findViewById(R.id.consent_info_close_btn);
        consentStatusInfoLayout = findViewById(R.id.consent_layout_notification);
        cert_info_close_btn = findViewById(R.id.cert_info_close_btn);
        cert_layout_notification = findViewById(R.id.cert_layout_notification);

    }

    private void showInfo() {
        boolean isNIDAvailable = Utilities.getBoolFromSharedPrefs(context, "NationalID", false);
        if (isNIDAvailable) {
            boolean available = dl.isValueavailable(farmerId);
           if (!available) {
                statusInfoLayout.setVisibility(View.VISIBLE);
            } else {
                statusInfoLayout.setVisibility(View.GONE);
            }
        }
        if(db.certificationsDao().getCertifications().size()>0) {
            boolean isCertification = dl.isCertificationavailable(farmerId);
            if(!isCertification){
                cert_layout_notification.setVisibility(View.VISIBLE);
            }else{
                cert_layout_notification.setVisibility(View.GONE);
            }
        }else{
            cert_layout_notification.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        //JIRA req OW-505
        if (mCurrentPhotoPath != null) {
            outState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
        if (mCapturedImageURI != null) {
            outState.putString(CAPTURED_PHOTO_URI_KEY, mCapturedImageURI.toString());
        }
    }

    private void setupGPS() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
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

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
        try {
            if (gpsTracker != null) {
                gpsTracker.stopLocationUpdates();
            }
        } catch (Exception e) {
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.RESULT_CANCELED) return;
        ImageView farmerPhoto = (ImageView) findViewById(R.id.viewFarmerFarmerPhoto);
        //JIRA req OW-505

        switch (requestCode) {

            case REQUEST_CHECK_SETTINGS:
                if (resultCode == -1) {
                    gpsTracker.getDeviceLocation();
                }
                break;

            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    if (getCurrentPhotoPath() != null) {
                        setFullImageFromFilePath(getCurrentPhotoPath(), farmerPhoto, "portrait");
                    } else {
                        Toast.makeText(ViewFarmerActivity.this, "Something went wrong, please try again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_IMAGE_GALLERY:
                if (resultCode == RESULT_OK) {
                    String realPath = ImageFilePath.getPath(ViewFarmerActivity.this, data.getData());

                    try {
                        if (realPath != null) {
                            setFullImageFromFilePath(realPath, farmerPhoto, "portrait");
                        } else {
                            Toast.makeText(ViewFarmerActivity.this, "Something went wrong, please try again", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_RESOLVE_ERROR:
                mResolvingError = false;
                // Make sure the app is not already connected or attempting to connect
                /*if (!googleApiClient.isConnecting() &&
                        !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }*/
                break;
            default:
        }
    }

    private void setupUI() {
        TextView farmerName = (TextView) findViewById(R.id.viewFarmerFarmerName);
        TextView farmerOlamId = (TextView) findViewById(R.id.viewFarmerOlamFarmerID);
        ImageView farmerPhoto = (ImageView) findViewById(R.id.viewFarmerFarmerPhoto);
//        ImageView infoSectionBackground = (ImageView) findViewById(R.id.infoSectionBackground);
        //View farmer info
        TextView viewFarmerInfo = (TextView) findViewById(R.id.viewFarmerInfo);
        LinearLayout linear_viewInfo = (LinearLayout) findViewById(R.id.linear_viewInfo);
        TextView genderInfo = (TextView) findViewById(R.id.genderInfo);
        TextView ageInfo = (TextView) findViewById(R.id.ageInfo);
        TextView pNumberInfo = (TextView) findViewById(R.id.pNumberInfo);
        TextView partnerInfo = (TextView) findViewById(R.id.partnerInfo);
        TextView programInfo = (TextView) findViewById(R.id.programInfo);
        TextView countryInfo = (TextView) findViewById(R.id.countryInfo);
        TextView regionInfo = (TextView) findViewById(R.id.regionInfo);
        TextView districtInfo = (TextView) findViewById(R.id.districtInfo);
        TextView subDistrictInfo = (TextView) findViewById(R.id.subDistrictInfo);
        TextView placeInfo = (TextView) findViewById(R.id.placeInfo);
        TextView fgInfo = (TextView) findViewById(R.id.fgInfo);
        TextView sectionInfo = (TextView) findViewById(R.id.sectionInfo);

        farmer = dl.getFarmerById(farmerId);

        if (farmer.getCount() > 0) {
            farmer.moveToFirst();
            farmerNameStr = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
            olamFarmerIdStr = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
            farmerName.setText(farmerNameStr);
            farmerOlamId.setText(olamFarmerIdStr);
            farmerPlaceId = farmer.getLong(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID));
            if (farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY)) == 1) {
                String farmerPhotoPath = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
                if (farmerPhotoPath != null && farmerPhotoPath.length() > 0) {
                    // set our desired width and height for display
                    int baseWidth = 100;
                    int baseHeight = 133;

                    Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                    Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                    Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

                    Bitmap existingBitmap = Utilities.loadSizedBitmap(farmerPhotoPath, outputWidth, outputHeight);

                    farmerPhoto.setImageBitmap(existingBitmap);
                }
            }

            //setting farmer info
            Cursor existingNumbers = dl.getFarmerPhoneNumbers(farmerId, false);
            StringBuilder phonenumberinfo = new StringBuilder();
            String ptInfo = null , prInfo=null;

            if (existingNumbers.getCount() > 0) {
                while (existingNumbers.moveToNext()) {
                    if(!phonenumberinfo.toString().equals("")){
                        phonenumberinfo.append("\n");
                    }
                    phonenumberinfo.append(existingNumbers.getString(existingNumbers.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE))).append(" ").append(existingNumbers.getString(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER)));
                }
            }
            genderInfo.setText("- " + farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER)));
            ageInfo.setText("- " +farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_AGE)));
            pNumberInfo.setText("- " +phonenumberinfo.toString());
            if(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID))!=null) {
                ptInfo = db.partnersDao().getPartnerNameById(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID))));
            }
            if(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID))!=null){
                prInfo=db.programmeDao().getProgrammeNameById(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID))));
            }
            partnerInfo.setText("- " +ptInfo);
            programInfo.setText("- " +prInfo);
            countryInfo.setText("- " +dl.getCountryName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID)))));
            regionInfo.setText("- " +dl.getRegionName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID)))));
            districtInfo.setText("- " +dl.getDistrictName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID)))));
            subDistrictInfo.setText("- " +dl.getSubDistrictName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISTRICTID)))));
            placeInfo.setText("- " +dl.getPlaceName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)))));
            fgInfo.setText("- " +dl.getFGName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID)))));
            sectionInfo.setText("- " +dl.getSectionName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID)))));
            JSONObject submittedModules;
            try {

                // fetching Info for Multifarms
                int roleId = 0;
                if (Utilities.getMultifarmPref(context, MultiFarmUtils.IS_MULTI_FARM_ENUMERATOR)) {
                    //Old
                    //  roleId = dl.getMultiFarmRoleInfoByFarmerId(String.valueOf(farmerId));
                    // New
                    String roleIdStr = dl.getMultiFarmRoleInfoByFarmerId1(String.valueOf(farmerId));
                    if (roleIdStr != null) {
                        roleId = Integer.parseInt(roleIdStr);
                    }
                    multiFarmId=dl.getMultifarmfarmId(farmerId);
                    if(multiFarmId!=null){
                        farmSurvey=true;
                    }else{
                        farmSurvey=false;
                    }
//                            ispolygonCaptured=dl.getPolygonInfo1(farmerId);
//                             isLocationCaptured=dl.getLocationInfo1(farmerId);
//                              String farmId= dl.getMultifarmfarmId(farmerId);
//                              if (farmId!=null){
//                                  isAvailableInStakeHolderList=true;
//                              }else {
//                                  isAvailableInStakeHolderList = false;
//                              }
//
//                             farmSubmittedHashMapList=dl.isAvailableinSubmittedModules1(moduleAssignments,farmerId);
                    //                             Log.i("FRANK EVANA", "SomeText: " + new Gson().toJson(farmSubmittedHashMapList));

                }else{
                    farmSurvey=false;
                }


                String submittedModulesJSON = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES));
                String Samplestr = String.valueOf("{\"modules\":null}");
                if (Samplestr.equals(submittedModulesJSON)) {
                    submittedModulesJSON = submittedModulesJSON.replaceAll("\"modules\":null", "");
                }
                submittedModules = (!submittedModulesJSON.equals("null")) ? new JSONObject(submittedModulesJSON) : new JSONObject();
                String moduleNameSuffix = Utilities.getLocaleSuffix(res);
                Cursor moduleAssignments = getFarmerModules(farmerId, this,farmSurvey);
                boolean isAtLeastOnePlotSurveyCaptured = validateBySubmittedModule(submittedModules);
                boolean isPlotSurveyPending = checkPendingPlotSurvey(submittedModules);
                showInfo();
                if (isPlotSurveyPending) {
                    plotStatusInfoLayout.setVisibility(View.VISIBLE);
                } else {
                    plotStatusInfoLayout.setVisibility(View.GONE);
                }
                String isConsentavailable = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ISCONSENTAVAILABLE));
                if (isConsentavailable != null && isConsentavailable.length() > 0 && isConsentavailable.equalsIgnoreCase("true")) {
                    consentStatusInfoLayout.setVisibility(View.GONE);
                }else{
                    consentStatusInfoLayout.setVisibility(View.VISIBLE);
                    plotStatusInfoLayout.setVisibility(View.GONE);
                    statusInfoLayout.setVisibility(View.GONE);
                    cert_layout_notification.setVisibility(View.GONE);
                }
                if (moduleAssignments != null && moduleAssignments.getCount() > 0) {
                    String[] adapterCols = new String[]{
                            (moduleAssignments.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix) != -1) ? OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix : OFISDBContract.ModuleEntry.COLUMN_NAME_NAME,
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID,
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED,
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED,
                            "farmer_module_id",
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LAT,
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LNG,
                            OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_VIEW_MODULE
                    };
                    int[] adapterRowViews = new int[]{R.id.moduleListModuleName, R.id.moduleListCurrentSubModuleId, R.id.moduleListStatusStarted, R.id.moduleListStatusFinished, R.id.moduleListFarmerModuleId, R.id.moduleListRecordedLat, R.id.moduleListRecordedLng, R.id.viewModuleAnswers};

                    customListAdapter_modulesAdapter =
                            new CustomListAdapter_Modules(context, moduleAssignments, adapterCols, adapterRowViews, res, farmer, submittedModules, roleId, farmerId,multiFarmId, isAtLeastOnePlotSurveyCaptured);

                     // customListAdapter_modulesAdapter.setValues(roleId,ispolygonCaptured,isLocationCaptured,isAvailableInStakeHolderList,farmSubmittedHashMapList);
                    listView.setAdapter(customListAdapter_modulesAdapter);
                }
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("ViewFarmerActivity: Error getting module assignments for farmer", e));
                e.printStackTrace();
            }
        }


        //JIRA req OW-505
        farmerPhotoActionButton = (ImageButton) findViewById(R.id.farmerPhotoActionButton);
        farmerPhotoActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request Permission only for Android Version Marshmallow and above
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_UPDATE_PHOTO_BUTTON, ScreenNameConstants.BUTTON_CLICKED);

                if (mCurrentCapturedLocation != null) {

                } else {
                    if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
                        gpsTracker.getDeviceLocation();
                        return;
                    }

                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (ContextCompat.checkSelfPermission(ViewFarmerActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewFarmerActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewFarmerActivity.this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewFarmerActivity.this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewFarmerActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewFarmerActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewFarmerActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewFarmerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ViewFarmerActivity.this,
                                new String[]{
                                        android.Manifest.permission.CAMERA,
                                        android.Manifest.permission.READ_PHONE_STATE,
                                        android.Manifest.permission.READ_MEDIA_AUDIO,
                                        android.Manifest.permission.READ_MEDIA_VIDEO,
                                        android.Manifest.permission.READ_MEDIA_IMAGES,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.RECORD_AUDIO,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION},

                                REQUEST_PERMISSIONS_FOR_TIRAMISU);
                    }else {
                        bindFarmerPhotoButton();
                    }
                }
                // Request Permission only for Android Version Marshmallow and above
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Check Camera & Storage Permission
                    if (ContextCompat.checkSelfPermission(ViewFarmerActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                            .checkSelfPermission(ViewFarmerActivity.this,
                                    android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        //request permission
                        ActivityCompat.requestPermissions(ViewFarmerActivity.this,
                                new String[]{android.Manifest.permission.CAMERA,
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PROFILE_PIC_PERMISSIONS_REQUEST);

                    } else {

                        // Permission already granted
                        bindFarmerPhotoButton();
                    }
                } else {

                    // Call methods directly for Android versions lower than Marshmallow
                    bindFarmerPhotoButton();
                }
            }
        });

        viewFarmerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                infoSectionBackground.setVisibility((infoSectionBackground.getVisibility()==View.GONE) ? View.VISIBLE : View.GONE);
                linear_viewInfo.setVisibility((linear_viewInfo.getVisibility()==View.GONE) ? View.VISIBLE : View.GONE);
                viewFarmerInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0,(linear_viewInfo.getVisibility()==View.GONE) ? R.drawable.ic_drop_down_collapse : R.drawable.ic_task_drop_down_expand, 0);
            }
        });


    }

    private boolean validateBySubmittedModule(JSONObject submittedModules) {
        boolean valid = false;
        try {
            int totalCount = dl.fetchPlotTotalCount(String.valueOf(farmerId));
            if(totalCount > 0) {
                if(submittedModules.length() > 0) {
                    JSONArray submittedModulesArray = submittedModules.getJSONArray("modules");
                    for (int i = 0; i < submittedModulesArray.length(); i++) {
                        JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
                        JSONObject submittedModuleDetails = submittedModule.getJSONObject("module");
                        if (submittedModuleDetails.has("plotId") && submittedModuleDetails.getString("plotId") != "null") {
                            valid = true;
                        }
                    }
                }else{
                    valid = false;
                }
            }else {
                valid = true;
            }
            Cursor moduleAssignments = dl.getFarmerModules(farmerId,farmSurvey);
            int moduleCount = moduleAssignments.getCount();
            if(moduleCount == 0){
                valid = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return valid;
    }

    private boolean checkPendingPlotSurvey(JSONObject submittedModules) {
        boolean valid = false;
        try {
            Cursor moduleAssignments = dl.getFarmerModules(farmerId,farmSurvey);
            int moduleCount = moduleAssignments.getCount();
            int plotCount = dl.fetchPlotTotalCount(String.valueOf(farmerId));
            int expectedSurveySubmission = moduleCount * plotCount;
            int actualSurveySubmission = 0;
            if(moduleCount == 0){
                valid = false;
            }
            while(moduleAssignments.moveToNext()) {
                int moduleId = moduleAssignments.getInt(moduleAssignments.getColumnIndex("_id"));
                if (expectedSurveySubmission > 0) {
                    if(submittedModules.length() > 0) {
                        JSONArray submittedModulesArray = submittedModules.getJSONArray("modules");
                        if (submittedModulesArray.length() > 0) {
                            for (int i = 0; i < submittedModulesArray.length(); i++) {
                                JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
                                JSONObject submittedModuleDetails = submittedModule.getJSONObject("module");
                                if (submittedModuleDetails.has("plotId") && submittedModuleDetails.getString("plotId") != "null") {
                                    if (submittedModuleDetails.has("id") && submittedModuleDetails.getInt("id") == moduleId) {
                                        actualSurveySubmission++;
                                    }
                                }
                            }
                        }
                    }else{
                        valid = true;
                        break;
                    }
                    if (expectedSurveySubmission > actualSurveySubmission) {
                        valid = true;
                    } else {
                        valid = false;
                    }
                }
            }
        } catch (Exception e) {
            valid = true;
            e.printStackTrace();
        }
        return valid;
    }

    @SuppressLint("SetTextI18n")
    private void setFarmGPS(Location location, View v) {
        try {
            // Location farmLocation = (Location) msg.obj;
            Location farmLocation = location;
            int updated = dl.updateFarmerLocation(
                    farmerId,
                    farmLocation,
                    farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                    farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID)),
                    farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME))
            );
            if (updated > 0) {
                updatelistitem(v);
                // capturing the geo point track
                collectGPSPoint(GPSConstants.MODULE_CAPTURE_FARM_LOCATION, farmLocation, farmerId);
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_RECORD_FARM_LOCATION, ScreenNameConstants.BUTTON_CLICKED);
            }
            setupUI();
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("ViewFarmerActivity: Error setting farmer GPS location", e));
            e.printStackTrace();
        }

        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    private void updatelistitem(View v) {
        TextView enabled = (TextView) v.findViewById(R.id.moduleListEnabled);
        enabled.setText("false");
        LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.rowlin);
        ImageView imageView = (ImageView) v.findViewById(R.id.moduleListModuleIcon);
        TextView moduleTitle = (TextView) v.findViewById(R.id.moduleListModuleName);
        TextView textView = (TextView) v.findViewById(R.id.moduleListModuleStatus);
        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_found));
        linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_background));
        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
        textView.setText(res.getString(R.string.record_farm_location_complete));
    }

    @SuppressLint("SetTextI18n")
    private void setHouseholdGPS(Location location, View v) {
        try {
            // Location farmLocation = (Location) msg.obj;
            Location farmLocation = location;
            int updated = dl.updateHouseholdLocation(
                    farmerId,
                    farmLocation,
                    farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                    farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID)),
                    farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME))
            );
            if (updated > 0) {
                updatelistitem(v);
                // capturing the geo point track
                collectGPSPoint(GPSConstants.MODULE_CAPTURE_HOUSEHOLD_LOCATION, farmLocation, farmerId);
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_RECORD_HOUSEHOLD_LOCATION, ScreenNameConstants.BUTTON_CLICKED);
            }
            setupUI();
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("ViewFarmerActivity: Error setting household GPS location", e));
            e.printStackTrace();
        }

        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    private void collectGPSPoint(String module, Location farmLocation, long farmerId) {

        TrackDetailsDTO trackDetailsDTO = new TrackDetailsDTO();
        trackDetailsDTO.setModule(module);
        trackDetailsDTO.setLatitude(String.valueOf(farmLocation.getLatitude()));
        trackDetailsDTO.setLongitude(String.valueOf(farmLocation.getLongitude()));
        trackDetailsDTO.setAccuracy(String.valueOf(farmLocation.getAccuracy()));
        trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
        trackDetailsDTO.setFarmerId(String.valueOf(farmerId));
        trackDetailsDTO.setFarmerName(farmerNameStr);

        ContentValues geoContentValues = new GPSUtils().getGeoContentValues(trackDetailsDTO);
        new GpsTrackerLogic().insertGeoDataDetails(context, geoContentValues);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        //JIRA req OW-505 MY_PROFILE_PIC_PERMISSIONS_REQUEST

        if (requestCode == 6575) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFarmGPS(clickedLocationItem);
            }
        } else if (requestCode == MY_PROFILE_PIC_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                bindFarmerPhotoButton();
            } else if (Build.VERSION.SDK_INT >= 23
                    && (!shouldShowRequestPermissionRationale(permissions[0])
                    || !shouldShowRequestPermissionRationale(permissions[1]))) {
                showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
            } else {
                Toast.makeText(this, R.string.camera_and_gallery_permission_denial_msg, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_PERMISSIONS_FOR_TIRAMISU) {
            try {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[5] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[6] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[7] == PackageManager.PERMISSION_GRANTED ) {
                    // Permissions granted, proceed with your code
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && (!shouldShowRequestPermissionRationale(permissions[0])
                        || !shouldShowRequestPermissionRationale(permissions[1])
                        || !shouldShowRequestPermissionRationale(permissions[2])
                        || !shouldShowRequestPermissionRationale(permissions[3])
                        || !shouldShowRequestPermissionRationale(permissions[4])
                        || !shouldShowRequestPermissionRationale(permissions[5])
                        || !shouldShowRequestPermissionRationale(permissions[6])
                        || !shouldShowRequestPermissionRationale(permissions[7])
                )) {
                    showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));

                }else {
                    Toast.makeText(this, R.string.permission_denial_msg_android_13_above, Toast.LENGTH_LONG).show();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getFarmGPS(final View v) {
        try {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            mRequestingLocationUpdates = true;
                            startLocationUpdates(v);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {

                            if (response.isPermanentlyDenied()) {
                                // open device settings when the permission is
                                // denied permanently
                                if (pd != null && pd.isShowing()) {
                                    pd.dismiss();
                                }
                                openSettings();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

        } catch (Exception e) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            FirebaseCrash.report(new Exception("ViewFarmerActivity: Error getting farmer GPS location - enumerator " + Utilities.getFromSharedPrefs(context), e));
            e.printStackTrace();
        }
    }

    public Cursor getFarmerModules(long id, Context context, boolean farmSurvey) {
        Cursor farmer = null;

        try {
            farmer = dl.getFlattenedFarmer(id);
            Cursor output = null;

            if (farmer.getCount() > 0) {
                farmer.moveToFirst();
                String modulesQuery="";
                if (farmSurvey){
                     modulesQuery = "SELECT distinct modules.id as _id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th,modules.annual_survey, " +
                            "submodules.name as submodule_name, submodules.name_fr as submodule_name_fr, submodules.name_es as submodule_name_es, submodules.name_id as submodule_name_id, submodules.name_pt as submodule_name_pt, submodules.name_tr as submodule_name_tr, submodules.name_lo as submodule_name_lo, submodules.name_vi as submodule_name_vi, submodules.name_th as submodule_name_th, " +
                            "modules.expires, " + "moduleassignments.recurrence_frequency, moduleassignments.recurrence_type, moduleassignments.disabled_months, moduleassignments.startDate, moduleassignments.isFarmSurvey,moduleassignments.view_module,moduleassignments.fkFarmerRoleId,moduleassignments.farmerRoleId, " +
                            " farmer_modules.id as farmer_module_id, farmer_modules.status_started, farmer_modules.status_finished, farmer_modules.current_submodule_id, farmer_modules.recorded_lat, farmer_modules.recorded_lng, moduleassignments.surveyDuration FROM moduleassignments " +
                            " INNER JOIN modules ON moduleassignments.module_id = modules.id " +
                            " LEFT OUTER JOIN farmer_modules ON modules.id = farmer_modules.module_id AND " +
                            "(farmer_modules.farmer_id = ? OR farmer_modules.olam_farmer_id = ?) " +
                            " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_modules.current_submodule_id = submodules.id WHERE " +
                             "moduleassignments.isPlotSurvey != 'true' AND " + "moduleassignments.childLabour != 'true' AND " +
                             "moduleassignments.country_id = ( case when moduleassignments.country_id = ? then ? else 'null' end ) AND " +
                            "((moduleassignment_type = 'Country' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?)) group by modules.id";
                }else {
                     modulesQuery = "SELECT distinct modules.id as _id, modules.name, modules.name_fr, modules.name_es, modules.name_id, modules.name_pt, modules.name_tr, modules.name_lo, modules.name_vi, modules.name_th,modules.annual_survey, " +
                            "submodules.name as submodule_name, submodules.name_fr as submodule_name_fr, submodules.name_es as submodule_name_es, submodules.name_id as submodule_name_id, submodules.name_pt as submodule_name_pt, submodules.name_tr as submodule_name_tr, submodules.name_lo as submodule_name_lo, submodules.name_vi as submodule_name_vi, submodules.name_th as submodule_name_th, " +
                            "modules.expires, " + "moduleassignments.recurrence_frequency, moduleassignments.recurrence_type, moduleassignments.disabled_months, moduleassignments.startDate, moduleassignments.isFarmSurvey,moduleassignments.view_module,moduleassignments.fkFarmerRoleId,moduleassignments.farmerRoleId, " +
                            " farmer_modules.id as farmer_module_id, farmer_modules.status_started, farmer_modules.status_finished, farmer_modules.current_submodule_id, farmer_modules.recorded_lat, farmer_modules.recorded_lng, moduleassignments.surveyDuration FROM moduleassignments " +
                            " INNER JOIN modules ON moduleassignments.module_id = modules.id " +
                            " LEFT OUTER JOIN farmer_modules ON modules.id = farmer_modules.module_id AND " +
                            "(farmer_modules.farmer_id = ? OR farmer_modules.olam_farmer_id = ?) " +
                            " LEFT OUTER JOIN " + OFISDBContract.SubmoduleEntry.TABLE_NAME + " ON farmer_modules.current_submodule_id = submodules.id WHERE " +
                            "moduleassignments.childLabour != 'true' AND " +
                             "moduleassignments.isPlotSurvey != 'true' AND "+
                            "moduleassignments.isFarmSurvey == 'false' AND " +
                             "moduleassignments.country_id = ( case when moduleassignments.country_id = ? then ? else 'null' end ) AND " +
                            "((moduleassignment_type = 'Country' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Region' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'District' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Place' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Farmer Group' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Section' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Programme' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Product' AND moduleassignment_id = ?) OR " +
                            "(moduleassignment_type = 'Partner' AND moduleassignment_id = ?)) group by modules.id";
                }
                String[] modulesQueryValues = new String[]{
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)),
                        farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)),
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("country_id")),
                        farmer.getString(farmer.getColumnIndex("region_id")),
                        farmer.getString(farmer.getColumnIndex("district_id")),
                        farmer.getString(farmer.getColumnIndex("place_id")),
                        farmer.getString(farmer.getColumnIndex("farmergroup_id")),
                        farmer.getString(farmer.getColumnIndex("section_id")),
                        farmer.getString(farmer.getColumnIndex("programme_id")),
                        farmer.getString(farmer.getColumnIndex("product_id")),
                        farmer.getString(farmer.getColumnIndex("partner_id"))
                };
//                @SuppressLint("Recycle") Cursor farmerModules = db.rawQuery(modulesQuery, modulesQueryValues);
                @SuppressLint("Recycle") Cursor farmerModules = dl.getfarmerModules(modulesQuery, modulesQueryValues);

                String[] mcColumns = new String[]{
                        "_id",
                        "name",
                        "name_fr",
                        "name_es",
                        "name_id",
                        "name_pt",
                        "name_tr",
                        "name_lo",
                        "name_vi",
                        "name_th",
                        "annual_survey",
                        "submodule_name",
                        "submodule_name_fr",
                        "submodule_name_es",
                        "submodule_name_id",
                        "submodule_name_pt",
                        "submodule_name_tr",
                        "submodule_name_lo",
                        "submodule_name_vi",
                        "submodule_name_th",
                        "expires",
                        "recurrence_frequency",
                        "recurrence_type",
                        "disabled_months",
                        "startDate",
                        "isFarmSurvey",
                        "view_module",
                        "fkFarmerRoleId",
                        "farmerRoleId",
                        "farmer_module_id",
                        "status_started",
                        "status_finished",
                        "current_submodule_id",
                        "recorded_lat",
                        "recorded_lng",
                        "surveyDuration"
                };
                MatrixCursor mc = new MatrixCursor(mcColumns);
                // add new record farm location task
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        res.getString(R.string.record_farm_location),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        res.getString(R.string.record_farm_location_hint),
                        "",
                        "",
                        "",
                        "",
                        "","",
                        "", "", "",
                        "",
                        "",
                        "",""
                });

                // add new record farm polygon task
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        res.getString(R.string.record_farm_polygon),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        "",
                        "",
                        "",
                        "",
                        "","","",
                        "", "", "",
                        "",
                        "farmpolygon",""
                });

                /*// add new polygon history task
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.recorded_polygon_history),
                        res.getString(R.string.recorded_polygon_history),
                        res.getString(R.string.recorded_polygon_history),
                        res.getString(R.string.recorded_polygon_history),
                        res.getString(R.string.recorded_polygon_history),
                        res.getString(R.string.recorded_polygon_history),
                        res.getString(R.string.recorded_polygon_history),
                        res.getString(R.string.recorded_polygon_history),
                        res.getString(R.string.recorded_polygon_history),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        res.getString(R.string.record_farm_polygon_hint),
                        "",
                        "",
                        "",
                        "",
                        "","",
                        "", "", "",
                        "",
                        "polygon_history"
                });*/

                boolean isHHAvailable = Utilities.getBoolFromSharedPrefs(context, Utilities.HOUSEHOLD_ENUMERATOR, false);
                if (isHHAvailable) {

                    // add new record household location task
                    mc.addRow(new String[]{"0", res.getString(R.string.record_hh_location), res.getString(R.string.record_hh_location), res.getString(R.string.record_hh_location), res.getString(R.string.record_hh_location), res.getString(R.string.record_hh_location), res.getString(R.string.record_hh_location), res.getString(R.string.record_hh_location), res.getString(R.string.record_hh_location), res.getString(R.string.record_hh_location), "0", "", "", "", res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), "","", "", "", "","", "", "","", "", "", res.getString(R.string.household_farmer),""});
                }

                MultiFarmUtils.isMultiFarmEnumerator = Utilities.getChildLabourPref(context, MultiFarmUtils.IS_MULTI_FARM_ENUMERATOR);
                if (MultiFarmUtils.isMultiFarmEnumerator) {
                    // add new record for update multifarm
                    mc.addRow(new String[]{"0", res.getString(R.string.update_farm_information), res.getString(R.string.update_farm_information), res.getString(R.string.update_farm_information), res.getString(R.string.update_farm_information), res.getString(R.string.update_farm_information), res.getString(R.string.update_farm_information), res.getString(R.string.update_farm_information), res.getString(R.string.update_farm_information), res.getString(R.string.update_farm_information), "0", "", "", "", res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), "", "","", "", "","", "",
                            "", "", "", "", "multifarm",""});

                }

                // add new phone number updating task
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        res.getString(R.string.update_phone_numbers),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        res.getString(R.string.update_phone_numbers_hint),
                        "",
                        "",
                        "",
                        "",
                        "", "", "", "",
                        "","",
                        "",
                        "Phone",""
                });

                try {
                    int isSynced = farmer.getInt(farmer.getColumnIndex("synced"));
                    String selectedsubdistrict;
                    if (isSynced == 0) {
                        selectedsubdistrict = farmer.getString(farmer.getColumnIndex("subdistrict_id")).trim();
                    } else {
                        selectedsubdistrict = farmer.getString(farmer.getColumnIndex("subdistrictId")).trim();
                    }
                    String countryid = farmer.getString(farmer.getColumnIndex("country_id")).trim();
                    boolean isSubdistrictCapture = dl.isSubdistrictCapture(Integer.parseInt(countryid));
                    if (isSubdistrictCapture) {
                        if (selectedsubdistrict.equalsIgnoreCase("0")) {
                            // update farmers subdistrict
                            mc.addRow(new String[]{"0", res.getString(R.string.update_farmer_subdistrict), res.getString(R.string.update_farmer_subdistrict), res.getString(R.string.update_farmer_subdistrict), res.getString(R.string.update_farmer_subdistrict), res.getString(R.string.update_farmer_subdistrict), res.getString(R.string.update_farmer_subdistrict), res.getString(R.string.update_farmer_subdistrict), res.getString(R.string.update_farmer_subdistrict), res.getString(R.string.update_farmer_subdistrict), "0", "", "", "", res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), "", "","", "", "","", "", "", "", "", "", "Subdistrict",""});
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // add update consent statement task
                mc.addRow(new String[]{"0", res.getString(R.string.update_consent_statement), res.getString(R.string.update_consent_statement), res.getString(R.string.update_consent_statement), res.getString(R.string.update_consent_statement), res.getString(R.string.update_consent_statement), res.getString(R.string.update_consent_statement), res.getString(R.string.update_consent_statement), res.getString(R.string.update_consent_statement), res.getString(R.string.update_consent_statement), "0", "", "", "", res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), res.getString(R.string.update_tab_hint), "","", "", "", "","", "", "", "", "", "", Utilities.CONSENTSTATEMENT,""});

                // add e-signature for existing farmers
                mc.addRow(new String[]{
                        "0",
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        res.getString(R.string.update_signature),
                        "0",
                        "",
                        "",
                        "",
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        res.getString(R.string.update_signature_hint),
                        "",
                        "",
                        "",
                        "",
                        "","",
                        "", "", "",
                        "","",
                        "Signature",""
                });

                boolean isNIDAvailable = Utilities.getBoolFromSharedPrefs(context, "NationalID", false);
                if (isNIDAvailable) {
//         add national id information
                    mc.addRow(new String[]{"0", res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), res.getString(R.string.add_nationalid), "0", "", "", "", res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), "","", "", "", "","", "", "", "","", "", "National_ID",""});
                }
                // add certification
                if(db.certificationsDao().getCertifications().size()>0) {
                    mc.addRow(new String[]{"0", res.getString(R.string.update_certification), res.getString(R.string.update_certification), res.getString(R.string.update_certification), res.getString(R.string.update_certification),
                            res.getString(R.string.update_certification), res.getString(R.string.update_certification), res.getString(R.string.update_certification), res.getString(R.string.update_certification), res.getString(R.string.update_certification),
                            "0", "", "", "", res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), res.getString(R.string.update_signature_hint), "","", "", "","", "", "", "", "","", "", res.getString(R.string.certification),""});
                }

               /* int pendingSync = dl.fetchPlotCountByFarmerID(String.valueOf(farmerId));
                int totalCount = dl.fetchPlotTotalCount(String.valueOf(farmerId));
                */

                //ticket-OFISDM-39 add new record diagnostic module task

                db = OFISRoomDB.getDbInstance(ViewFarmerActivity.this);
                List<DmMainModule> dmQuestionsModuleList = db.dmMainModuleDao().getDMmodule();

                for (int i = 0; i < dmQuestionsModuleList.size(); i++){
                    dmSurveyName = dmQuestionsModuleList.get(i).name;
                    dmSubTitle = dmQuestionsModuleList.get(i).assessmentType;
                    moduleID = String.valueOf(dmQuestionsModuleList.get(i).id);
                    followUpCount = String.valueOf(dmQuestionsModuleList.get(i).followUpAttempts);
                    String startDate = String.valueOf(dmQuestionsModuleList.get(i).startDate);
                    DmRecurrence dmRecurrence = db.dmRecurrenceDao().getRecurrenceByModuleId(moduleID);
                    mc.addRow(new String[]{
                            "0",
                            dmSurveyName,
                            dmSurveyName,
                            dmSurveyName,
                            dmSurveyName,
                            dmSurveyName,
                            dmSurveyName,
                            dmSurveyName,
                            dmSurveyName,
                            dmSurveyName,
                            "0",
                            "",
                            "",
                            "",
                            res.getString(R.string.dm_diognostic_module_subtitle),
                            res.getString(R.string.dm_diognostic_module_subtitle),
                            res.getString(R.string.dm_diognostic_module_subtitle),
                            res.getString(R.string.dm_diognostic_module_subtitle),
                            res.getString(R.string.dm_diognostic_module_subtitle),
                            res.getString(R.string.dm_diognostic_module_subtitle),
                            res.getString(R.string.dm_diognostic_module_subtitle),
//                            String.valueOf(dmRecurrence.recurrenceFrequency),
//                            dmRecurrence.recurrenceType,
                            res.getString(R.string.dm_diognostic_module_subtitle),
                            res.getString(R.string.dm_diognostic_module_subtitle),
//                            "", "", "", "", "", startDate, "",moduleID , "",
                            "",
                            "",
                            "", "", "","", "", "", "",moduleID , "",
                            "diagnostic",""
                    });

                }



                int farmPlotCount = dl.fetchSyncedPlotCountByFarmerID(String.valueOf(farmerId));
                Cursor moduleAssignments = dl.getFarmerModules(farmerId,farmSurvey);
                if (farmPlotCount > 0 && moduleAssignments.getCount() > 0) {
                    mc.addRow(new String[]{"0", res.getString(R.string.plot_survey), res.getString(R.string.plot_survey), res.getString(R.string.plot_survey), res.getString(R.string.plot_survey), res.getString(R.string.plot_survey), res.getString(R.string.plot_survey), res.getString(R.string.plot_survey), res.getString(R.string.plot_survey), res.getString(R.string.plot_survey), "0", "", "", "", res.getString(R.string.ModuleStatus), res.getString(R.string.ModuleStatus), res.getString(R.string.ModuleStatus), res.getString(R.string.ModuleStatus), res.getString(R.string.ModuleStatus), res.getString(R.string.ModuleStatus), res.getString(R.string.ModuleStatus), res.getString(R.string.ModuleStatus), res.getString(R.string.ModuleStatus), "", "", "", "", "","", "", "", "", "", "", "PlotSurvey",""});
                }
                Cursor fmp = dl.getFmpByFarmerId(id);
                if (fmp.getCount() > 0) {
                    fmp.moveToFirst();
                    int fmpStarted = 0;
                    String fmpMode = "summary";
                    if (dl.hasNqi("fmpreview", fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID)))) {
                        fmpStarted = 1;
                    } else if (dl.hasNqi("fullfmpreview", fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID)))) {
                        fmpStarted = 1;
                        fmpMode = "full";
                    }

                    mc.addRow(new String[]{
                            "-" + fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID)),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            res.getString(R.string.review_farm_management_plan),
                            "0",
                            "",
                            "",
                            "",
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            res.getString(R.string.review_farm_management_plan_hint),
                            "", "", "", "", "","",
                            String.valueOf(fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID))),
                            String.valueOf(fmpStarted),
                            String.valueOf(fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_HAS_FEEDBACK))),
                            String.valueOf(fmp.getInt(fmp.getColumnIndex(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_STARTING_YEAR))),
                            fmpMode,
                            ""
                    });
                }

                MatrixCursor childOptionsCursor = new MatrixCursor(mcColumns);

                // add register child
                if (ChildLabourUtils.isChildEnumerator) {
                    childOptionsCursor.addRow(new String[]{
                            "0",
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            res.getString(R.string.register_child),
                            "0",
                            "",
                            "",
                            "",
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            res.getString(R.string.update_childlabour_hint),
                            "",
                            "",
                            "",
                            "",
                            "","", "", "",
                            "","",
                            "",
                            ChildLabourUtils.REG_CHILD,""
                    });

                    // child labour survey
                    childOptionsCursor.addRow(new String[]{
                            "0",
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            res.getString(R.string.child_labour_survey),
                            "0",
                            "",
                            "",
                            "",
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            res.getString(R.string.ModuleStatus),
                            "",
                            "",
                            "",
                            "", "","", "",
                            "",
                            "","",
                            "",
                            ChildLabourUtils.CHILDLABOUR_SURVEY,""
                    });
                }
                Cursor[] cursors = {mc, farmerModules, childOptionsCursor};
                output = new MergeCursor(cursors);
            }
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            farmer.close();
        }

    }

    private void recordFarmLocation(final View v, String statement) {
        pd = new ProgressDialog(ViewFarmerActivity.this);
        pd.setCancelable(false);
        pd.setIndeterminate(false);
        pd.setMessage(res.getString(R.string.fetching_location));
        pd.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg = (TextView) dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText(statement);
        Button btnNo = (Button) dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button) dialogView.findViewById(R.id.btn_yes);
        AlertDialog alert = builder.create();
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                alert.dismiss();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFarmGPS(v);
                alert.dismiss();
            }
        });

        alert.show();
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

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates(final View v) {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (pd != null && pd.isShowing()) {
                                    pd.dismiss();
                                }
                                if (mCurrentLocation != null && !GPSUtils.isMockLocation(mCurrentLocation)) {
                                    if (polygon) {
                                        recordFarmPolygon(v);
                                    } else {
                                        if (household) {
                                            setHouseholdGPS(mCurrentLocation, v);
                                        } else {
                                            setFarmGPS(mCurrentLocation, v);
                                        }
                                    }
                                } else {
                                    Toast.makeText(ViewFarmerActivity.this, getResources().getString(R.string.record_location_timeout), Toast.LENGTH_LONG).show();
                                }
                            }
                        }, 6000);

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
//                        Handler handler = new Handler(Looper.getMainLooper());
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(mCurrentLocation != null)
//                                    setFarmGPS(mCurrentLocation, v);
//                            }
//                        }, 1000);
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                        Toast.makeText(ViewFarmerActivity.this, getResources().getString(R.string.record_location_timeout), Toast.LENGTH_LONG).show();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(ViewFarmerActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
//                                Log.e(TAG, errorMessage);

                                Toast.makeText(ViewFarmerActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(ViewFarmerActivity.this, getResources().getString(R.string.record_location_timeout), Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void enableFarmPolygon(final View v) {
        pd = new ProgressDialog(ViewFarmerActivity.this);
        pd.setCancelable(false);
        pd.setIndeterminate(false);
        pd.setMessage(res.getString(R.string.fetching_location));
        pd.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg = (TextView) dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText(res.getString(R.string.are_you_ready_to_polygon_record));
        Button btnNo = (Button) dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button) dialogView.findViewById(R.id.btn_yes);
        AlertDialog alert = builder.create();
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                alert.dismiss();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SUBMIT_PLOT_LEVEL_ANALYSIS, ScreenNameConstants.BUTTON_CLICKED);
                alert.dismiss();
                getFarmGPSPolygon(v);
            }
        });

        alert.show();

    }

    private void getFarmGPSPolygon(final View v) {
        try {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            mRequestingLocationUpdates = true;
                            startLocationUpdates(v);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {

                            if (response.isPermanentlyDenied()) {
                                // open device settings when the permission is
                                // denied permanently
                                if (pd != null && pd.isShowing()) {
                                    pd.dismiss();
                                }
                                openSettings();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

        } catch (Exception e) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            FirebaseCrash.report(new Exception("ViewFarmerActivity: Error getting farmer GPS location - enumerator " + Utilities.getFromSharedPrefs(context), e));
            e.printStackTrace();
        }
    }

    private void recordFarmPolygon(final View v) {
        final String strFarmID = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
        DataLayer dl = new DataLayer(getResources(), ViewFarmerActivity.this);
//        dl.fetchPlotData(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
        int count = 0, recordedPlotcount = 0;
        try {
//            count = SharedPreferenceUtils.getInstance(getApplicationContext()).getPlotCount(""+strFarmID);
            recordedPlotcount = dl.fetchPlotData(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
            count = dl.fetchPlotCountByFarmerID(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
        } catch (Exception e) {
            count = 0;
            recordedPlotcount = 0;
        }

        Log.d("ViewFarmerActivity", "Plot Count = " + count);
        if (recordedPlotcount == 0 || count == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.plot_count_alert_dialog, null);
            builder.setView(dialogView);
            TextView tvMsg = (TextView) dialogView.findViewById(R.id.tv_msg);
            TextView tvTitle = (TextView) dialogView.findViewById(R.id.tv_title);
            LinearLayout tvAddPlot = (LinearLayout) dialogView.findViewById(R.id.addPlot);
            plotList = (LinearLayout) dialogView.findViewById(R.id.plotList);
//            EditText edTxtNumberOfPlots = (EditText) dialogView.findViewById(R.id.et_number_plot);
            tvTitle.setText(res.getString(R.string.record_polygon_input_title));
            tvMsg.setText(res.getString(R.string.record_polygon_input_messege));
            Button btnNo = (Button) dialogView.findViewById(R.id.btn_no);
            ImageButton plotClose = (ImageButton) dialogView.findViewById(R.id.plotClose);
            Button btnYes = (Button) dialogView.findViewById(R.id.btn_yes);
            AlertDialog alert = builder.create();
            alert.show();
            alert.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            loadPlotLayout();

            tvAddPlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout plotLayout = (LinearLayout) inflater.inflate(R.layout.add_plot, plotList, false);
                    plotList.addView(plotLayout);
                    TextView plotCount = plotLayout.findViewById(R.id.plotCount);
                    EditText plotName = plotLayout.findViewById(R.id.plotName);
                    LinearLayout plotDelete = plotLayout.findViewById(R.id.deletePlot);
                    Cursor plotIdsSynced = dl.syncedPlotIds(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                    int totalPlotCount = plotIdsSynced.getCount() + plotList.getChildCount();
                    plotCount.setText("Plot " + plotList.getChildCount());
                    bindPlotDelete(plotDelete);
                }
            });

            plotClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                }
            });


            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                }
            });

            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    String lPlotCountString = edTxtNumberOfPlots.getText().toString();
                    String farmerID = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));

                    ArrayList<String> plotNames = getAllPlotValues();
                    dl.insertNewPlot(farmerID, plotNames);

                    /*String lPlotCountString = "1";
                    int lPlotCount = 0;
                    if (!TextUtils.isEmpty(lPlotCountString)) {
                        lPlotCount = Integer.parseInt(lPlotCountString);
                    }*/
                    int lPlotCount = plotNames.size();
                    if (lPlotCount > 0) {
                        Intent recordPolygonIntent = new Intent(context, RecordPolygonActivity.class);
                        recordPolygonIntent.putExtra(FARMER_ID, farmerId);
                        recordPolygonIntent.putExtra(FARM_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                        recordPolygonIntent.putExtra(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                        recordPolygonIntent.putExtra(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                        recordPolygonIntent.putExtra(PLOT_COUNT, lPlotCount);
                        SharedPreferenceUtils.getInstance(getApplicationContext()).setPlotCount("" + strFarmID, lPlotCount);
                        recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
                        startActivity(recordPolygonIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.valid_count_msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Intent recordPolygonIntent = new Intent(context, RecordPolygonActivity.class);
            recordPolygonIntent.putExtra(FARMER_ID, farmerId);
            recordPolygonIntent.putExtra(FARM_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
            recordPolygonIntent.putExtra(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
            recordPolygonIntent.putExtra(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
            int totalCount = dl.fetchPlotTotalCount(String.valueOf(farmerId));
            recordPolygonIntent.putExtra(PLOT_COUNT, totalCount);
            SharedPreferenceUtils.getInstance(getApplicationContext()).setPlotCount("" + strFarmID, totalCount);
            recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
            startActivity(recordPolygonIntent);
        }

    }

    private void loadPlotLayout() {
        plotList.removeAllViews();
        Cursor plotIds = dl.fetchAllPlotIds(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
        Cursor plotIdsSynced = dl.syncedPlotIds(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));

        /*if (plotIdsSynced.getCount() > 0){
            int size = plotIdsSynced.getCount();
            for (int i = 1; i <= size; i++) {
                plotIdsSynced.moveToNext();
                if(position == i){
                    name = plotIdsSynced.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME));
                    selectedPlotId = plotIdsSynced.getInt(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_ID));
                    txtPlotName.setText(name);
                }
            }
        }*/
        int totalPlotCount = plotIdsSynced.getCount();

        if(plotIds.getCount() > 0){
            int size = plotIds.getCount();
            for (int i = 1; i <= size; i++) {
                plotIds.moveToNext();
                LinearLayout plotLayout = (LinearLayout) inflater.inflate(R.layout.add_plot, plotList, false);
                plotList.addView(plotLayout);
                TextView plotCount = plotLayout.findViewById(R.id.plotCount);
                EditText plotName = plotLayout.findViewById(R.id.plotName);
                LinearLayout plotDelete = plotLayout.findViewById(R.id.deletePlot);
                int finalCount = i + totalPlotCount;
                plotCount.setText("Plot " + i);
                String name = plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME));
                String isSynced = plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_IS_SYNCED));
//                if(! name.equalsIgnoreCase("null")){
                    plotName.setText(name);
                    if (isSynced.equalsIgnoreCase("1")){
                        plotName.setEnabled(false);
                        plotName.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    }else {
                        plotName.setEnabled(true);
                    }
//                }
                int count = dl.fetchPlotData(String.valueOf(farmerId));
                if(i == 1 || i <= count){
                    plotDelete.setVisibility(View.INVISIBLE);
                }else{
                    plotDelete.setVisibility(View.VISIBLE);
                }
                if(isSynced.equalsIgnoreCase("1")){
                    plotDelete.setVisibility(View.INVISIBLE);
                }

                plotName.setFocusableInTouchMode(true);
                bindPlotDelete(plotDelete);

            }
        } else {
            LinearLayout plotLayout = (LinearLayout) inflater.inflate(R.layout.add_plot, plotList, false);
            plotList.addView(plotLayout);
            TextView plotCount = plotLayout.findViewById(R.id.plotCount);
            EditText plotName = plotLayout.findViewById(R.id.plotName);
            int finalCount = 1 + totalPlotCount;
            plotCount.setText("Plot " + 1);
            LinearLayout plotDelete = plotLayout.findViewById(R.id.deletePlot);
            plotDelete.setVisibility(View.GONE);
            bindPlotDelete(plotDelete);
        }
    }

    private void bindPlotDelete(LinearLayout trashbutton) {
        trashbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout numberrow = (LinearLayout) v.getParent();
                ((ViewGroup) numberrow.getParent()).removeView(numberrow);
                String farmerID = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
                ArrayList<String> plotNames = getAllPlotValues();
                dl.insertNewPlot(farmerID,plotNames);
                loadPlotLayout();
            }
        });
    }

    private ArrayList<String> getAllPlotValues() {
        plotNameList = new ArrayList<String>();
        int plotRowsCount = plotList.getChildCount();
        for (int j = 0; j < plotRowsCount; j++) {
            LinearLayout plotNameRow = (LinearLayout) plotList.getChildAt(j);
            EditText plotName = (EditText) plotNameRow.findViewById(R.id.plotName);
            String plotNameValue = String.valueOf(plotName.getText());
            plotNameList.add(plotNameValue);
        }
        return plotNameList;
    }

    private void bindnumberdelete(ImageView trashbutton) {
        trashbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout numberrow = (LinearLayout) v.getParent();
                ((ViewGroup) numberrow.getParent()).removeView(numberrow);
            }
        });
    }

    private void bindnumberradio(RadioButton mainnumber) {
        mainnumber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    int phonerowscount = phonenumbers.getChildCount();
                    for (int j = 0; j < phonerowscount; j++) {
                        LinearLayout phonenumberrow = (LinearLayout) phonenumbers.getChildAt(j);
                        RadioButton primary_number = (RadioButton) phonenumberrow.findViewById(R.id.primary_number);

                        primary_number.setChecked(false);
                    }
                    buttonView.setChecked(true);
                }
            }
        });
    }

    private void populatephoneNumberDialog() {
        phonenumbers.removeAllViews();

        Cursor existingNumbers = dl.getFarmerPhoneNumbers(farmerId, false);

        if (existingNumbers.getCount() > 0) {
            boolean firstNumber = true;
            while (existingNumbers.moveToNext()) {
                LinearLayout phonenumberinclude = (LinearLayout) inflater.inflate(R.layout.phonenumber_entry, phonenumbers, false);
                RadioButton numberradio = (RadioButton) phonenumberinclude.findViewById(R.id.primary_number);
                TextView diallingCode = (TextView) phonenumberinclude.findViewById(R.id.dialling_code);
                EditText numbertext = (EditText) phonenumberinclude.findViewById(R.id.phonenumber);
                ImageView numberdelete = (ImageView) phonenumberinclude.findViewById(R.id.deleteNumber);

//                numberradio.setChecked(existingNumbers.getInt(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY)) == 1);
                try {
                    numberradio.setChecked((existingNumbers.getString(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY)).equalsIgnoreCase("true") || (existingNumbers.getInt(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY)) == 1)));
                } catch (Exception e) {
                }

                diallingCode.setText(existingNumbers.getString(existingNumbers.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE)));
                numbertext.setText(existingNumbers.getString(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER)));

                phonenumbers.addView(phonenumberinclude);
                if (firstNumber) {
                    numberdelete.setVisibility(View.GONE);
                    firstNumber = false;
                } else {
                    bindnumberdelete(numberdelete);
                }
                bindnumberradio(numberradio);
            }
        } else {
            Cursor countryDiallingCode = dl.getDiallingCodeByPlaceId(farmerPlaceId);
            String dialling_code = "";
            if (countryDiallingCode.getCount() > 0) {
                countryDiallingCode.moveToFirst();
                dialling_code = countryDiallingCode.getString(countryDiallingCode.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE));
            }

            LinearLayout phonenumberinclude = (LinearLayout) inflater.inflate(R.layout.phonenumber_entry, phonenumbers, false);
            phonenumbers.addView(phonenumberinclude);
            RadioButton numberradio = (RadioButton) phonenumberinclude.findViewById(R.id.primary_number);
            ImageView numberdelete = (ImageView) phonenumberinclude.findViewById(R.id.deleteNumber);
            EditText diallingCode = (EditText) phonenumberinclude.findViewById(R.id.dialling_code);
            diallingCode.setText(dialling_code);
            numberdelete.setVisibility(View.GONE);
            bindnumberradio(numberradio);
            numberradio.setChecked(true);
        }
    }

    private void getAllPhoneNumbers() {
        phoneNumberList = new ArrayList<String>();

        int phonerowscount = phonenumbers.getChildCount();

        for (int j = 0; j < phonerowscount; j++) {
            LinearLayout phonenumberrow = (LinearLayout) phonenumbers.getChildAt(j);

            EditText phonenumber = (EditText) phonenumberrow.findViewById(R.id.phonenumber);
            String phonenumberValue = String.valueOf(phonenumber.getText()).replace(" ", "");
            phoneNumberList.add(phonenumberValue);
        }
    }

    private boolean duplicatePhoneNumberValidation() {
        boolean duplicatePhoneNumberValidationFlag = false;

        getAllPhoneNumbers();

        int phonerowscount = phonenumbers.getChildCount();

        for (int j = 0; j < phonerowscount; j++) {
            LinearLayout phonenumberrow = (LinearLayout) phonenumbers.getChildAt(j);

            EditText phonenumber = (EditText) phonenumberrow.findViewById(R.id.phonenumber);
            String phonenumberValue = String.valueOf(phonenumber.getText()).replace(" ", "");

            if (Collections.frequency(phoneNumberList, phonenumberValue) == 1) {
                duplicatePhoneNumberValidationFlag = true;
            } else {
                duplicatePhoneNumberValidationFlag = false;
                break;
            }
        }

        return duplicatePhoneNumberValidationFlag;
    }

    private void setupPhoneNumberDialog() {
        mSlideBottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up);
        mSlideBottomDown = AnimationUtils.loadAnimation(context, R.anim.bottom_down);
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        phoneNumbersHolder = (RelativeLayout) findViewById(R.id.phoneNumbersHolder);
        phonenumbers = (LinearLayout) findViewById(R.id.phonenumbers);
        ImageButton hidenumbersBox = (ImageButton) findViewById(R.id.phonenumbersCloseButton);
        ImageButton addNumber = (ImageButton) findViewById(R.id.addNumber);
        Button phonenumbersSave = (Button) findViewById(R.id.phonenumbersSave);

        hidenumbersBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_HIDE_NUMBERBOX, ScreenNameConstants.BUTTON_CLICKED);
                phoneNumbersHolder.startAnimation(mSlideBottomDown);
            }
        });

        addNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_ADD_PHONE_NUMBER, ScreenNameConstants.BUTTON_CLICKED);
                LinearLayout phonenumberinclude = (LinearLayout) inflater.inflate(R.layout.phonenumber_entry, phonenumbers, false);
                EditText diallingCode = (EditText) phonenumbers.getChildAt(0).findViewById(R.id.dialling_code);
                if (diallingCode != null) {
                    EditText newDiallingCode = (EditText) phonenumberinclude.findViewById(R.id.dialling_code);
                    newDiallingCode.setText(diallingCode.getText());
                }
                phonenumbers.addView(phonenumberinclude);
                bindnumberdelete((ImageView) phonenumberinclude.findViewById(R.id.deleteNumber));
                bindnumberradio((RadioButton) phonenumberinclude.findViewById(R.id.primary_number));
            }
        });

        phonenumbersSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SAVE_PHONE_NUMBER, ScreenNameConstants.BUTTON_CLICKED);
                if (duplicatePhoneNumberValidation()) { //Duplicate Phone Number Validation....
                    int phonerowscount = phonenumbers.getChildCount();
                    boolean hasNumber = false;
                    boolean hasMainNumber = false;
                    Cursor fullFarmer = dl.getFlattenedFarmer(farmerId);
                    int countryId = 0;
                    JSONArray storeableNumbers = new JSONArray();

                    if (fullFarmer.moveToFirst()) {
                        countryId = fullFarmer.getInt(fullFarmer.getColumnIndex("country_id"));
                    }

                    for (int j = 0; j < phonerowscount; j++) {
                        LinearLayout phonenumberrow = (LinearLayout) phonenumbers.getChildAt(j);

                        EditText phonenumber = (EditText) phonenumberrow.findViewById(R.id.phonenumber);
                        RadioButton primary_number = (RadioButton) phonenumberrow.findViewById(R.id.primary_number);
                        String phonenumberValue = String.valueOf(phonenumber.getText());

                        if (phonenumberValue.length() > 0 && PhoneNumberUtils.isGlobalPhoneNumber(phonenumberValue)) {
                            hasNumber = true;
                            JSONObject storeableNumber = new JSONObject();
                            try {
                                storeableNumber.put("country_id", countryId);
                                storeableNumber.put("primary_number", primary_number.isChecked());
                                storeableNumber.put("phonenumber", phonenumberValue);
                                storeableNumbers.put(storeableNumber);
                            } catch (JSONException e) {
                                FirebaseCrash.report(new Exception("ViewFarmerActivity: Error setting farmer phone number", e));
                                e.printStackTrace();
                            }
                        }

                        if (primary_number.isChecked()) {
                            hasMainNumber = true;
                        }
                    }
                    if (!hasNumber || !hasMainNumber) {
                        Toast.makeText(context, res.getString(R.string.Please_fill_all_fields), Toast.LENGTH_LONG).show();
                    } else {
                        dl.updateFarmerPhoneNumbers(farmerId, storeableNumbers);
                        ContentValues nqiValues = new ContentValues();
                        if (mCurrentCapturedLocation != null) {
                              collectGPSPoint(GPSConstants.MODULE_UPDATE_PHONE_NUMBER, mCurrentCapturedLocation, farmerId);
                        } else if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
                            gpsTracker.getDeviceLocation();
                            return;
                        } else {
                            //
                        }
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "phonenumbers");
                        nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, res.getString(R.string.update_phone_numbers));
                        nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                        nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
                        nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                        nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                        nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
                        dl.insertNetworkQueueEntry(nqiValues);

                        LinearLayout linearLayout = (LinearLayout) phoneNumbersListItem.findViewById(R.id.rowlin);
                        ImageView imageView = (ImageView) phoneNumbersListItem.findViewById(R.id.moduleListModuleIcon);
                        TextView moduleTitle = (TextView) phoneNumbersListItem.findViewById(R.id.moduleListModuleName);
                        TextView textView = (TextView) phoneNumbersListItem.findViewById(R.id.moduleListModuleStatus);
                        TextView enabledItem = (TextView) phoneNumbersListItem.findViewById(R.id.moduleListEnabled);

                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                        enabledItem.setText("false");
                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setText(res.getString(R.string.phone_number_recorded));

                        phoneNumbersHolder.startAnimation(mSlideBottomDown);
                        phonenumbersSave.setEnabled(false);
                        setupUI();
                        Toast.makeText(context, res.getString(R.string.Farmer_Updated), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, res.getString(R.string.duplicate_phone_number), Toast.LENGTH_LONG).show();
                }
            }
        });

        mSlideBottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                phoneNumbersHolder.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        TextView enabled = (TextView) v.findViewById(R.id.moduleListEnabled);
//        if (enabled.getText().toString().equalsIgnoreCase("true")) {
//            if (id > 0) {
//                TextView subModuleTv = (TextView) v.findViewById(R.id.moduleListCurrentSubModuleId);
//                TextView moduleNameTv = (TextView) v.findViewById(R.id.moduleListModuleName);
//                TextView statusStartedTv = (TextView) v.findViewById(R.id.moduleListStatusStarted);
//                TextView statusFinishedTv = (TextView) v.findViewById(R.id.moduleListStatusFinished);
//                TextView farmerModuleIdTv = (TextView) v.findViewById(R.id.moduleListFarmerModuleId);
//                TextView farmerModuleLatTv = (TextView) v.findViewById(R.id.moduleListRecordedLat);
//                TextView farmerModuleLngTv = (TextView) v.findViewById(R.id.moduleListRecordedLng);
//                Intent intent = new Intent(context, CompleteModuleActivity.class);
//                intent.putExtra(FARMER_ID, farmerId);
//                intent.putExtra(MODULE_ID, id);
//                intent.putExtra(FARMER_MODULE_ID, (String.valueOf(farmerModuleIdTv.getText()).equals("")) ? 0 : Long.parseLong(String.valueOf(farmerModuleIdTv.getText())));
//                intent.putExtra(SUB_MODULE_ID, (String.valueOf(subModuleTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(subModuleTv.getText())));
//                intent.putExtra(FARM_ID, farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID)));
//                intent.putExtra(STATUS_STARTED, (String.valueOf(statusStartedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusStartedTv.getText())));
//                intent.putExtra(STATUS_FINISHED, (String.valueOf(statusFinishedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusFinishedTv.getText())));
//                intent.putExtra(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
//                intent.putExtra(MODULE_NAME, String.valueOf(moduleNameTv.getText()));
//                intent.putExtra(RECORDED_LAT, (String.valueOf(farmerModuleLatTv.getText()).equals("")) ? "" : String.valueOf(farmerModuleLatTv.getText()));
//                intent.putExtra(RECORDED_LNG, (String.valueOf(farmerModuleLatTv.getText()).equals("")) ? "" : String.valueOf(farmerModuleLngTv.getText()));
//                intent.putExtra(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
//
//                if (DynamicQuestions.getInstance() != null) {
//                    DynamicQuestions.clearInstance();
//                }
//                context.startActivity(intent);
//            } else {
//                if (id == 0) {
//                    // record farm location if necessary
//                    TextView recordedLng = (TextView) v.findViewById(R.id.moduleListRecordedLng);
//                    if (recordedLng != null && recordedLng.getText().equals("Phone")) {
//                        if (phoneNumbersListItem == null) {
//                            phoneNumbersListItem = v;
//                        }
//                        populatephoneNumberDialog();
//                        phoneNumbersHolder.setVisibility(View.VISIBLE);
//                        phoneNumbersHolder.startAnimation(mSlideBottomUp);
//                    } else if (recordedLng != null && recordedLng.getText().equals("farmpolygon")) {
//                        polygon=true;
//                        enableFarmPolygon(v);
//                    } else if (recordedLng != null && recordedLng.getText().equals(ChildLabourUtils.REG_CHILD)) {
//                       Intent in = new Intent(this, RegisterChildActivity.class);
//                       Bundle bundle=new Bundle();
//                       bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
//                       in.putExtras(bundle);
//                       startActivity(in);
//                    } else if (recordedLng != null && recordedLng.getText().equals(ChildLabourUtils.CHILDLABOUR_SURVEY)) {
//                        Intent in = new Intent(this, ChildLabourDetailsActivity.class);
//                        Bundle bundle=new Bundle();
//                        bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
//                        in.putExtras(bundle);
//                        startActivity(in);
//                    } else {
//                        polygon=false;
//                        recordFarmLocation(v);
//                    }
//                } else if (id < 0) {
//                    // go to fmp review page
//                    TextView startYearTv = (TextView) v.findViewById(R.id.moduleListCurrentSubModuleId);
//                    TextView statusStartedTv = (TextView) v.findViewById(R.id.moduleListStatusStarted);
//                    TextView fmpIdTv = (TextView) v.findViewById(R.id.moduleListFarmerModuleId);
//                    TextView fmpModeTv = (TextView) v.findViewById(R.id.moduleListRecordedLat);
//                    ReviewFarmManagementPlan.fmpId = (String.valueOf(fmpIdTv.getText()).equals("")) ? 0 : Long.parseLong(String.valueOf(fmpIdTv.getText()));
//                    ReviewFarmManagementPlan.fullReviewMode = !String.valueOf(fmpModeTv.getText()).equalsIgnoreCase("summary");
//                    ReviewFarmManagementPlan.farmerId = farmerId;
//                    ReviewFarmManagementPlan.fmpStartYear = (String.valueOf(startYearTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(startYearTv.getText()));
//                    ReviewFarmManagementPlan.statusStarted = (String.valueOf(statusStartedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusStartedTv.getText()));
//                    ReviewFarmManagementPlan.olamFarmerId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
//                    ReviewFarmManagementPlan.farmId = farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID));
//                    Intent intent = new Intent(context, ReviewFarmManagementPlan.class);
//                    context.startActivity(intent);
//                }
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    //JIRA req OW-505
    private void bindFarmerPhotoButton() {
        final CharSequence[] items = {res.getString(R.string.Camera), res.getString(R.string.Gallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(res.getString(R.string.Select_picture_from));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(context, items[item], Toast.LENGTH_SHORT).show();

                if (items[item].equals(res.getString(R.string.Camera))) {
                    // New
                    File photoFile = null;
                    try {
                        photoFile = createImageFile("portrait");
                    } catch (IOException e) {
                        FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating photo image file", e));
                        e.printStackTrace();
                    }

                    if (photoFile != null) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if ((intent.resolveActivity(getPackageManager()) != null)) {
                            //Uri photoURI = FileProvider.getUriForFile(ViewFarmerActivity.this, getApplicationContext().getPackageName() + ".profileimage.fileprovider", photoFile);
                            Uri photoURI = FileProvider.getUriForFile(ViewFarmerActivity.this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);

                            // Samsung Galaxy S3 Fix
                            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            for (ResolveInfo resolveInfo : resInfoList) {
                                String packageName = resolveInfo.activityInfo.packageName;
                                grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }

                            //  tempProfileImageFile=photoFile;
                            setCapturedImageURI(photoURI);
                            setCurrentPhotoPath(photoFile.getPath());
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, getCapturedImageURI());
                            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                        } else {
                            Toast.makeText(ViewFarmerActivity.this, "Gallery is not available in Mobile", Toast.LENGTH_LONG).show();
                        }
                    }

                } else if (items[item].equals(res.getString(R.string.Gallery))) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_IMAGE_GALLERY);

                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private File createImageFile(String portraitOrSignature) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = (portraitOrSignature.equalsIgnoreCase("portrait")) ? "ofis_farmer_portrait_" + timestamp + "_" : "ofis_farmer_signature_" + timestamp + "_";
        File storageDir = getExternalFilesDir("");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        if (portraitOrSignature.equalsIgnoreCase("portrait")) {
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        }
        return image;
    }


    private String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    private void setCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }

    private Uri getCapturedImageURI() {
        return mCapturedImageURI;
    }

    private void setCapturedImageURI(Uri mCapturedImageURI) {
        this.mCapturedImageURI = mCapturedImageURI;
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
                        Toast.makeText(ViewFarmerActivity.this, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
                    }
                });
        dialog.show();
    }


    private void setFullImageFromFilePath(String imagePath, ImageView target, String portraitOrSignature) {

        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, portraitOrSignature);
        if (savedImagePath != null) {
            if (portraitOrSignature.equalsIgnoreCase("portrait")) {
                Uri uri = Uri.parse(savedImagePath);
                setCapturedImageURI(uri);
                setCurrentPhotoPath(savedImagePath);
            }

            displayBitmap(savedImagePath, portraitOrSignature, target);
        }
    }

    private void displayBitmap(String savedImagePath, String portraitOrSignature, ImageView target) {
        // set our desired width and height for display
        int baseWidth = 100;
        int baseHeight = 133;
        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());
        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);
        target.setImageBitmap(bitmap);
        dl.updateFarmerPhoto(farmerId, savedImagePath, olamFarmerIdStr, farmerNameStr);

        // Collect the GPS Point
        if (mCurrentCapturedLocation != null) {
            // check the current location
            collectGPSPoint(GPSConstants.MODULE_CAPTURE_UPDATE_PHOTO, mCurrentCapturedLocation, farmerId);
        } else if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
            gpsTracker.getDeviceLocation();
            return;
        } else {
            //
        }
    }

    private static String getFilePathUri(Context context, Uri uri) {
        Uri filePathUri = uri;
        if (uri.getScheme().compareTo("content") == 0) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            assert cursor != null;
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePathUri = Uri.parse(cursor.getString(column_index));
            }
            cursor.close();
        }
        return filePathUri + "";
    }

    //frank
    @Override
    public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id) {
        // TODO Auto-generated method stub
        TextView enabled = (TextView) v.findViewById(R.id.moduleListEnabled);
        try {
            if (MultiFarmUtils.isMultiFarmEnumerator) {
                if (enabled.getText().toString().equalsIgnoreCase("false") && id > 0) {
                    if (enabled.getTag().toString().equalsIgnoreCase("farmerS")) {
                        LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = li.inflate(R.layout.multifarm_custom_toast_layout, null);
                        TextView tv = layout.findViewById(R.id.alert_msg);
                        tv.setText(res.getString(R.string.survey_can_be_completed_by_other_stakeholders));
                        Toast toast = new Toast(this);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setView(layout);//setting the view of custom toast layout
                        toast.show();

                    }else if(enabled.getTag().toString().equalsIgnoreCase("canfarm")){
                        LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = li.inflate(R.layout.multifarm_custom_toast_layout, null);
                        TextView tv = layout.findViewById(R.id.alert_msg);
                        tv.setText(res.getString(R.string.survey_can_be_completed_by_other_stakeholders));
                        Toast toast = new Toast(this);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setView(layout);//setting the view of custom toast layout
                        toast.show();
                    }else if(enabled.getTag().toString().equalsIgnoreCase("hasfarm")){
                        LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = li.inflate(R.layout.multifarm_custom_toast_layout, null);
                        TextView tv = layout.findViewById(R.id.alert_msg);
                        tv.setText(res.getString(R.string.already_syned_survey_info));
                        Toast toast = new Toast(this);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setView(layout);//setting the view of custom toast layout
                        toast.show();
                    }
                }

            }
        }catch (Exception e){}

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        TextView enabled = (TextView) v.findViewById(R.id.moduleListEnabled);
        if (enabled.getText().toString().equalsIgnoreCase("true")) {
            if (id > 0) {
               String mfFarmId  = dl.getMultifarmfarmId(farmerId);
                if (mfFarmId != null && MultiFarmUtils.isMultiFarmEnumerator) {
                    try {
                        showPopupFarmDetails(v, position, id, mfFarmId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    showSurvey(v, id);
                }

//                showSurvey(v, id);

            }
            else {
                if (id == 0) {
                    // record farm location if necessary
                    TextView recordedLng = (TextView) v.findViewById(R.id.moduleListRecordedLng);
                    TextView subModuleTv = (TextView) v.findViewById(R.id.moduleListCurrentSubModuleId);
                    TextView moduleListModuleName = (TextView) v.findViewById(R.id.moduleListModuleName);
                    if (recordedLng != null && recordedLng.getText().equals("Phone")) {
                        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_UPDATE_PHONE_NUMBER, ScreenNameConstants.BUTTON_CLICKED);
                        if (phoneNumbersListItem == null) {
                            phoneNumbersListItem = v;
                        }
                        populatephoneNumberDialog();
                        phoneNumbersHolder.setVisibility(View.VISIBLE);
                        phoneNumbersHolder.startAnimation(mSlideBottomUp);
                    } else if (recordedLng != null && recordedLng.getText().equals("farmpolygon")) {
                        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_FARM_POLYGON, ScreenNameConstants.BUTTON_CLICKED);
                        polygon = true;
                        enableFarmPolygon(v);
                        
                    } /*else if (recordedLng != null && recordedLng.getText().equals("polygon_history")) {
                        // check internet connection
                        if (InternetConnectionHelper.isInternetAvailable(context)) {
                            if (hasRequiredPermissions(context)) {
                                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_POLYGON_HISTORY, ScreenNameConstants.BUTTON_CLICKED);
                                Intent polygonHistoryIntent = new Intent(this, PolygonHistory.class);
                                polygonHistoryIntent.putExtra(FARMER_ID, farmerId);
                                polygonHistoryIntent.putExtra(FARM_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                                polygonHistoryIntent.putExtra(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                                polygonHistoryIntent.putExtra(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                                startActivity(polygonHistoryIntent);
                            } else {
                                showToast(context, " Location Permission not granted");
                            }
                        } else {
                            showToast(context, "No internet connection");
                        }
                    }*/
                    //Ticket-OFISDM-39 newly added
                    else if(recordedLng != null && recordedLng.getText().equals("diagnostic")){
                        Constants.MODULE_ID = subModuleTv.getText().toString();
                        Constants.MODULE_NAME = moduleListModuleName.getText().toString();
                        Constants.FARMER_ID = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
                        String farmerID = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
                        long farmerGroupId = dl.getFarmerGroupById(farmerId);
                        Constants.FARMER_GROUP_ID = String.valueOf(dl.getFarmerGroupById(farmerId));;

                        Intent in = new Intent(this, DiagnosticModuleActivity.class);
                        in.putExtra("SUB_TITLE",dmSubTitle);
                        in.putExtra("DM_MODULE_ID",moduleID);
                        in.putExtra("DM_FARMER_ID",farmerID);
                        in.putExtra("DM_SURVEY_SUBTITLE",dmSurveyName);
                        in.putExtra("DM_FARMER_GROUP_ID",String.valueOf(farmerGroupId));
//                        startActivity(in);

                        int newCount;
                        if (Objects.equals(followUpCount, "0")){
                            newCount = 0;
                        }else {
                            newCount=Integer.parseInt(followUpCount);
                        }

                        showPopupDialog(newCount , in, farmerID, Constants.MODULE_ID);

                    } else if (recordedLng != null && recordedLng.getText().equals(ChildLabourUtils.REG_CHILD)) {
                        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_REGISTER_CHILD, ScreenNameConstants.BUTTON_CLICKED);
                        Intent in = new Intent(this, RegisterChildActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
                        in.putExtras(bundle);
                        startActivity(in);
                    } else if (recordedLng != null && recordedLng.getText().equals(ChildLabourUtils.CHILDLABOUR_SURVEY)) {
                        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CHILD_LABOUR_SURVEY, ScreenNameConstants.BUTTON_CLICKED);
                        Intent in = new Intent(this, ChildLabourDetailsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
                        in.putExtras(bundle);
                        startActivity(in);
                    } else if (recordedLng != null && recordedLng.getText().equals("PlotSurvey")) {
                        Intent in = new Intent(this, ViewPlotSurveyActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
                        in.putExtras(bundle);
                        startActivity(in);
                    } else if (recordedLng != null && recordedLng.getText().equals("Signature")) {
                        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_UPDATE_SIGNATURE, ScreenNameConstants.BUTTON_CLICKED);
                        showSignatureDialog();
                    } else if (recordedLng != null && recordedLng.getText().equals("National_ID")) {
                        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_ADD_NATIONAL_ID, ScreenNameConstants.BUTTON_CLICKED);
                        Intent in = new Intent(this, AddNationalidActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                        bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
                        boolean status = dl.isValueavailable(farmerId);
                        if (status) {
                            bundle.putBoolean("edit_mode", true);
                        } else {
                            bundle.putBoolean("edit_mode", false);
                        }
                        in.putExtras(bundle);
                        startActivity(in);
                    } else if (recordedLng != null && recordedLng.getText().equals(res.getString(R.string.certification))){
                        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CERTIFICATION, ScreenNameConstants.BUTTON_CLICKED);
                        Intent in = new Intent(this, AddCertificationsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
                        bundle.putString(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                        in.putExtras(bundle);
                        startActivity(in);
                    }else if (recordedLng != null && recordedLng.getText().equals("ConsentStatement")) {
                        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CHILD_LABOUR_SURVEY, ScreenNameConstants.BUTTON_CLICKED);
                        Intent in = new Intent(this, ConsentStatementActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("displayFarmerId", olamFarmerIdStr);
                        bundle.putString("olamfarmername", farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                        bundle.putLong("FarmerId", farmerId);
                        bundle.putString("type", UPDATE);
                        in.putExtras(bundle);
                        startActivity(in);
                    } else if (recordedLng != null && recordedLng.getText().equals("Subdistrict")) {
                        Intent in = new Intent(this, UpdateSubdistrict.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("olamfarmername", farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                        bundle.putInt(ChildLabourUtils.CHILD_FARMER_ID, (int) farmerId);
                        in.putExtras(bundle);
                        startActivity(in);

                    } else if (recordedLng != null && recordedLng.getText().equals(getResources().getString(R.string.household_farmer))) {
                        polygon = false;
                        household = true;
                        recordFarmLocation(v, res.getString(R.string.are_you_at_the_farmers_hh));
                    } else if (recordedLng != null && recordedLng.getText().equals("multifarm")) {
                        Intent in = new Intent(this, UpdateMultiFarmActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong("farmerId", farmerId);
                        bundle.putBoolean("editMode",false);
                        bundle.putBoolean("isSurveyEnabled",false);
                        bundle.putString("olamfarmername", farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                        in.putExtras(bundle);
                        startActivity(in);
                    } else {
                        polygon = false;
                        household = false;
                        recordFarmLocation(v, res.getString(R.string.are_you_at_the_farm));
                    }
                }
                else if (id < 0) {
                    // go to fmp review page
                    TextView startYearTv = (TextView) v.findViewById(R.id.moduleListCurrentSubModuleId);
                    TextView statusStartedTv = (TextView) v.findViewById(R.id.moduleListStatusStarted);
                    TextView fmpIdTv = (TextView) v.findViewById(R.id.moduleListFarmerModuleId);
                    TextView fmpModeTv = (TextView) v.findViewById(R.id.moduleListRecordedLat);
                    ReviewFarmManagementPlan.fmpId = (String.valueOf(fmpIdTv.getText()).equals("")) ? 0 : Long.parseLong(String.valueOf(fmpIdTv.getText()));
                    ReviewFarmManagementPlan.fullReviewMode = !String.valueOf(fmpModeTv.getText()).equalsIgnoreCase("summary");
                    ReviewFarmManagementPlan.farmerId = farmerId;
                    ReviewFarmManagementPlan.fmpStartYear = (String.valueOf(startYearTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(startYearTv.getText()));
                    ReviewFarmManagementPlan.statusStarted = (String.valueOf(statusStartedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusStartedTv.getText()));
                    ReviewFarmManagementPlan.olamFarmerId = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
                    ReviewFarmManagementPlan.farmId = farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID));
                    Intent intent = new Intent(context, ReviewFarmManagementPlan.class);
                    context.startActivity(intent);
                }
            }
        } else {
            // Deal the Multifarm Item Click

            if (MultiFarmUtils.isMultiFarmEnumerator) {
                try {
                    if (enabled.getText().toString().equalsIgnoreCase("false") && id == 0) {

                        if (enabled.getTag().toString().equalsIgnoreCase("poly")) {
                            boolean ispolygonCaptured = dl.getPolygonInfo1(farmerId);
                            if (ispolygonCaptured) {
//
                                LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View layout = li.inflate(R.layout.multifarm_custom_toast_layout, null);
                                TextView tv = layout.findViewById(R.id.alert_msg);
                                tv.setText(res.getString(R.string.recored_polygon_plot_multifarm_info));
                                //Creating the Toast objectn
                                Toast toast = new Toast(this);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                toast.setView(layout);//setting the view of custom toast layout
                                toast.show();
                            }
                        } else if (enabled.getTag().toString().equalsIgnoreCase("loc")) {
                            boolean isLocationCaptured = dl.getLocationInfo1(farmerId);
                            if (isLocationCaptured) {
//
                                LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View layout = li.inflate(R.layout.multifarm_custom_toast_layout, null);
                                TextView tv = layout.findViewById(R.id.alert_msg);
                                tv.setText(res.getString(R.string.recored_polygon_multifarm_info));
                                //Creating the Toast objectn
                                Toast toast = new Toast(this);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                toast.setView(layout);//setting the view of custom toast layout
                                toast.show();
                            }
                        }

                    }
                }catch (Exception e){}


            }
        }
    }



    public void showPopupFarmDetails(View v, int position, long id, String mfFarmId) throws JSONException {
        final Dialog farmDetailDialog = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
        farmDetailDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        farmDetailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        farmDetailDialog.setContentView(R.layout.dialog_farmdetails);
        farmDetailDialog.setCancelable(false);
        farmDetailDialog.setCanceledOnTouchOutside(false);
        LinearLayout farmerList = (LinearLayout) farmDetailDialog.findViewById(R.id.farmerList);

//        fetch multifarm details
            MultiFarmDTO multiFarmDTO = dl.getMultiFarmStakeHolderInfoByFarmId(mfFarmId);
            JSONArray multiFarmArray=multiFarmDTO.getStakeholdersInfo();
            if (multiFarmArray != null) {
                for (int i = 0; i < multiFarmArray.length(); i++) {
                    JSONObject jsonobject = (JSONObject) multiFarmArray.get(i);
//            String farmerName=jsonobject.optString("farmerName");
                    String role = dl.getFarmerRole(jsonobject.optInt("farmerRoleId"));
                    String farmerName = dl.getFarmerNamefromDb(jsonobject.optLong("farmerId"));
                    LinearLayout includeFarmerList = (LinearLayout) inflater.inflate(R.layout.add_farmerroles, farmerList, false);
                    TextView farmer_name_title = includeFarmerList.findViewById(R.id.farmer_name_title);
                    TextView farmer_name = includeFarmerList.findViewById(R.id.farmer_name);
                    TextView farmerRole = includeFarmerList.findViewById(R.id.role);
                    int farmerCount = i + 1;
                    farmer_name_title.setText(getResources().getString(R.string.farmer) + " " + farmerCount + ":");
                    farmer_name.setText(farmerName);
                    farmerRole.setText(role);
                    farmerList.addView(includeFarmerList);
                }

                TextView farmName = (TextView) farmDetailDialog.findViewById(R.id.farm_name);
                TextView farmCode = (TextView) farmDetailDialog.findViewById(R.id.farm_code);
                farmName.setText(multiFarmDTO.getFarmName());
                farmCode.setText(multiFarmDTO.getFarmCode());

            }

        farmDetailDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                farmDetailDialog.dismiss();
                showSurvey(v, id);
            }
        });

        farmDetailDialog.findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                farmDetailDialog.dismiss();
                TextView subModuleTv = (TextView) v.findViewById(R.id.moduleListCurrentSubModuleId);
                TextView moduleNameTv = (TextView) v.findViewById(R.id.moduleListModuleName);
                TextView statusStartedTv = (TextView) v.findViewById(R.id.moduleListStatusStarted);
                TextView statusFinishedTv = (TextView) v.findViewById(R.id.moduleListStatusFinished);
                TextView farmerModuleIdTv = (TextView) v.findViewById(R.id.moduleListFarmerModuleId);
                TextView farmerModuleLatTv = (TextView) v.findViewById(R.id.moduleListRecordedLat);
                TextView farmerModuleLngTv = (TextView) v.findViewById(R.id.moduleListRecordedLng);
//                intent.putString(FARMER_ID, farmerId);

                Intent in = new Intent(ViewFarmerActivity.this, UpdateMultiFarmActivity.class);
                Bundle bundle=new Bundle();
//                bundle.putLong("farmerId", farmerId);
//                bundle.putBoolean("editMode",true);
//                bundle.putString("olamfarmername", farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                bundle.putString("farmerId", String.valueOf(farmerId));
                bundle.putString("farmId", String.valueOf(mfFarmId));
                bundle.putBoolean("editMode",true);
                bundle.putBoolean("isSurveyEnabled",true);
                bundle.putLong(MODULE_ID, id);
                bundle.putLong(FARMER_MODULE_ID, (String.valueOf(farmerModuleIdTv.getText()).equals("")) ? 0 : Long.parseLong(String.valueOf(farmerModuleIdTv.getText())));
                bundle.putLong(SUB_MODULE_ID, (String.valueOf(subModuleTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(subModuleTv.getText())));
                bundle.putLong(FARM_ID, farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID)));
                bundle.putLong(STATUS_STARTED, (String.valueOf(statusStartedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusStartedTv.getText())));
                bundle.putLong(STATUS_FINISHED, (String.valueOf(statusFinishedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusFinishedTv.getText())));
                bundle.putString(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                bundle.putString(MODULE_NAME, String.valueOf(moduleNameTv.getText()));
                bundle.putString(RECORDED_LAT, (String.valueOf(farmerModuleLatTv.getText()).equals("")) ? "" : String.valueOf(farmerModuleLatTv.getText()));
                bundle.putString(RECORDED_LNG, (String.valueOf(farmerModuleLatTv.getText()).equals("")) ? "" : String.valueOf(farmerModuleLngTv.getText()));
                bundle.putString(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));

                in.putExtras(bundle);
                startActivity(in);
            }
        });

        farmDetailDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 farmDetailDialog.dismiss();
            }
        });

        farmDetailDialog.show();
    }

    public void showSurvey(View v, long id) {
        TextView subModuleTv = (TextView) v.findViewById(R.id.moduleListCurrentSubModuleId);
        TextView moduleNameTv = (TextView) v.findViewById(R.id.moduleListModuleName);
        TextView statusStartedTv = (TextView) v.findViewById(R.id.moduleListStatusStarted);
        TextView statusFinishedTv = (TextView) v.findViewById(R.id.moduleListStatusFinished);
        TextView farmerModuleIdTv = (TextView) v.findViewById(R.id.moduleListFarmerModuleId);
        TextView farmerModuleLatTv = (TextView) v.findViewById(R.id.moduleListRecordedLat);
        TextView farmerModuleLngTv = (TextView) v.findViewById(R.id.moduleListRecordedLng);
        TextView viewModuleAns = (TextView) v.findViewById(R.id.viewModuleAnswers);
        MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SURVEY,  ScreenNameConstants.BUTTON_CLICKED);
        Intent intent = new Intent(context, CompleteModuleActivity.class);
        intent.putExtra(FARMER_ID, farmerId);
        intent.putExtra(MODULE_ID, id);
        intent.putExtra(FARMER_MODULE_ID, (String.valueOf(farmerModuleIdTv.getText()).equals("")) ? 0 : Long.parseLong(String.valueOf(farmerModuleIdTv.getText())));
        intent.putExtra(SUB_MODULE_ID, (String.valueOf(subModuleTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(subModuleTv.getText())));
        intent.putExtra(FARM_ID, farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID)));
        intent.putExtra(STATUS_STARTED, (String.valueOf(statusStartedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusStartedTv.getText())));
        intent.putExtra(STATUS_FINISHED, (String.valueOf(statusFinishedTv.getText()).equals("")) ? 0 : Integer.parseInt(String.valueOf(statusFinishedTv.getText())));
        intent.putExtra(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
        intent.putExtra(MODULE_NAME, String.valueOf(moduleNameTv.getText()));
        intent.putExtra(RECORDED_LAT, (String.valueOf(farmerModuleLatTv.getText()).equals("")) ? "" : String.valueOf(farmerModuleLatTv.getText()));
        intent.putExtra(RECORDED_LNG, (String.valueOf(farmerModuleLatTv.getText()).equals("")) ? "" : String.valueOf(farmerModuleLngTv.getText()));
        intent.putExtra(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
        intent.putExtra(VIEW_MODULE, (String.valueOf(viewModuleAns.getText()).equals("")) ? "0" : String.valueOf(viewModuleAns.getText()));
        if (DynamicQuestions.getInstance() != null) {
            DynamicQuestions.clearInstance();
        }
        context.startActivity(intent);
    }


    boolean isSigned = false;
    public void showSignatureDialog() {
        isSigned = false;
        final Dialog signatureDialog = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
        signatureDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        signatureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        signatureDialog.setContentView(R.layout.dialog_signature);
        SignaturePad mSignaturePad = signatureDialog.findViewById(R.id.signature_pad);
        Button bt_clear = (Button)signatureDialog.findViewById(R.id.imgv_clear_signature);
        Button bt_confirm = (Button)signatureDialog.findViewById(R.id.btn_add_signature);
        bt_confirm.setEnabled(true);
        bt_clear.setEnabled(true);

        bt_clear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CLEAR_SIGNATURE,  ScreenNameConstants.BUTTON_CLICKED);
                mSignaturePad.clear();
                return false;
            }
        });
        bt_clear.findViewById(R.id.imgv_clear_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CLEAR_SIGNATURE,  ScreenNameConstants.BUTTON_CLICKED);
                mSignaturePad.clear();
            }
        });
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched
                isSigned = false;
            }

            @Override
            public void onSigned() {
                //Event triggered when the pad is signed
                isSigned = true;
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
                isSigned = false;
            }
        });

        signatureDialog.findViewById(R.id.btn_cancel_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CANCEL_SIGNATURE,  ScreenNameConstants.BUTTON_CLICKED);
                signatureDialog.dismiss();
            }
        });

        bt_confirm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                {
                    MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CONFIRM_SIGNATURE,  ScreenNameConstants.BUTTON_CLICKED);
                    try {
                        if (isSigned) {
//                        farmerSignatureButton.setImageBitmap(mSignaturePad.getTransparentSignatureBitmap());
                            File photoFile = null;
                            try {
                                photoFile = createImageFile("signature");
                            } catch (IOException e) {
                                FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating signature image file", e));
                                e.printStackTrace();
                            }
                            if (photoFile != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    try {
                                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                                        m.invoke(null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                Uri fileUri = Uri.fromFile(photoFile);
//                            setCapturedSignatureImageURI(fileUri);
//                            setCurrentSignaturePhotoPath(fileUri.getPath());
                                dl.updateSignature(context, farmerId, fileUri.getPath(),farmerNameStr);
                                if (mCurrentCapturedLocation != null) {
                                    collectGPSPoint(GPSConstants.MODULE_UPDATE_SIGNATURE,mCurrentCapturedLocation,farmerId);
                                } else if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
                                    gpsTracker.getDeviceLocation();
                                } else {
                                    //
                                }


                                try (FileOutputStream out = new FileOutputStream(photoFile)) {
                                    mSignaturePad.getTransparentSignatureBitmap().compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                    // PNG is a lossless format, the compression factor (100) is ignored
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            signatureDialog.dismiss();
                            customListAdapter_modulesAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ViewFarmerActivity.this, "No signature available...", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        bt_confirm.findViewById(R.id.btn_add_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CONFIRM_SIGNATURE,  ScreenNameConstants.BUTTON_CLICKED);
                try {
                    if (isSigned) {
//                        farmerSignatureButton.setImageBitmap(mSignaturePad.getTransparentSignatureBitmap());
                        File photoFile = null;
                        try {
                            photoFile = createImageFile("signature");
                        } catch (IOException e) {
                            FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating signature image file", e));
                            e.printStackTrace();
                        }
                        if (photoFile != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                try {
                                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                                    m.invoke(null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Uri fileUri = Uri.fromFile(photoFile);
//                            setCapturedSignatureImageURI(fileUri);
//                            setCurrentSignaturePhotoPath(fileUri.getPath());
                            dl.updateSignature(context, farmerId, fileUri.getPath(),farmerNameStr);
                            try (FileOutputStream out = new FileOutputStream(photoFile)) {
                                mSignaturePad.getTransparentSignatureBitmap().compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                // PNG is a lossless format, the compression factor (100) is ignored
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        signatureDialog.dismiss();
                        customListAdapter_modulesAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewFarmerActivity.this, "No signature available...", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        signatureDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setupUI();

        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_FIND_FARMER_ACTIVITY, ScreenNameConstants.SCREEN_VIEW_FARMER_ACTIVITY, ScreenNameConstants.SCREEN_VIEW_FARMER);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            listView.setOnItemLongClickListener(null);
            listView.setOnClickListener(null);
        }catch (Exception e){}

        try {
            if(gpsTracker!=null) {
                gpsTracker.stopLocationUpdates();
            }
        } catch (Exception e) {
        }

    }


    private void showPopupDialog(int newFollowUpCount, Intent in, String farmerID, String moduleID) {
        boolean isCompleted = false;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.followup_diagnostic_popup);
//        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        ArrayList<String> recyclerViewData = new ArrayList<>();

        recyclerViewData.add(Constants.EXISTINGMODULE);
//        recyclerViewData.add("Follow up " + 1);

        if(db.complianceIndicatorDao().hasActionPlan(String.valueOf(moduleID))){

            // getting the seventh day which restricts in generating the followup
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();
            String startDateString = db.followUpItemMainTableDao().getFollowUpStartDate(String.valueOf(1),farmerID);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            boolean followUpDurationDisable = false;
            String followUpDuration = null;
//            Date fakeDate = null;
//            String fakeCurrentDate = null;


            Date endFollowUpDurationDate = null;
            if (startDateString != null){
                followUpDuration = RecurrenceUtils.calculateEndDate(startDateString, db.dmMainModuleDao().getFollowUpDurationVal(moduleID) , "days");
//                fakeCurrentDate = RecurrenceUtils.calculateEndDate(startDateString,8,"days");
            }


            try {
                if (followUpDuration != null) {
//                    fakeDate = dateFormat.parse(fakeCurrentDate);
                    endFollowUpDurationDate = dateFormat.parse(followUpDuration);
                }
            } catch (android.net.ParseException | java.text.ParseException e) {
                e.printStackTrace();
            }

            // creating the followUp for surveys
            List<FollowUpItemMainTable> followupList = db.followUpItemMainTableDao().getFollowupByFarmerId(Long.parseLong(farmerID), Integer.parseInt(moduleID));
            if(followupList.size() == 0){
                List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByModuleId(String.valueOf(moduleID), String.valueOf(farmerId), true);
                if(dmSurveyAnswerTableList.size() != 0) {
                    recyclerViewData.add("Follow-up survey " + 1);
                }
            }else {
                List<Integer> followupAttempts = db.followUpItemMainTableDao().getFollowupByAttempts(Long.parseLong(farmerID), Integer.parseInt(moduleID));
                for (int i = 1; i <= followupAttempts.size(); i++) {
                    recyclerViewData.add("Follow-up survey " + i);
                }
                List<FollowUpItemMainTable> followupUnSyncedItems = db.followUpItemMainTableDao().getFollowupUnSyncedItems(farmerID, moduleID, false);
                if(followupUnSyncedItems.size() == 0) {
                    List<FollowUpItemMainTable> followupUnResolvedItems = db.followUpItemMainTableDao().getFollowupUnResolvedItems(farmerID, moduleID, true, Constants.NOTRESOLVED, followupAttempts.size());
                    if(followupUnResolvedItems.size() > 0) {
                        int val = followupAttempts.size() + 1;

                        if (endFollowUpDurationDate!=null){
                            if (currentDate.before(endFollowUpDurationDate)){
                                followUpDurationDisable = true;
                            }
                        }

                        String count = db.dmMainModuleDao().getFollowUpAttemptsVal(moduleID);
                        if (!Objects.equals(count, "0") && !Objects.equals(count, null)){
//                            count = db.dmMainModuleDao().getFollowUpAttempts(moduleID);
                            if (count.replaceAll(" ", "").toLowerCase().equals("max")){
                                if (followUpDurationDisable){
                                    recyclerViewData.add("Follow-up survey " + val);
                                    isCompleted = false;

                                }else {
                                    recyclerViewData.add("Follow-up survey " + val);
                                    isCompleted = true;
                                    Toast.makeText(context, "Recurrence Follow-up date crossed", Toast.LENGTH_SHORT).show();
                                }
                            }else if (val < Integer.parseInt(count) + 1 ){

                                if (followUpDurationDisable){
                                    recyclerViewData.add("Follow-up survey " + val);
                                    isCompleted = false;

                                }else {
                                    recyclerViewData.add("Follow-up survey " + val);
                                    isCompleted = true;
                                    Toast.makeText(context, "Recurrence Follow-up date crossed", Toast.LENGTH_SHORT).show();
                                }

                            }else {
                                if (followUpDurationDisable){
                                    isCompleted = false;

                                }else {
                                    isCompleted = true;
                                    Toast.makeText(context, "Recurrence Follow-up date crossed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            recyclerViewData.add("Follow-up survey " + val);
                            if (!followUpDurationDisable){
                                isCompleted = true;
                                Toast.makeText(context, "Recurrence Follow-up date crossed", Toast.LENGTH_SHORT).show();
                            }
                        }



                    }else{
                        isCompleted = true;
                    }
                }
            }

        }else if (db.complianceIndicatorDao().hasNoActionPlan(String.valueOf(moduleID))){
            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByModuleId(String.valueOf(moduleID), String.valueOf(farmerId), true);
            if(dmSurveyAnswerTableList.size() != 0) {
                isCompleted = true;
            }
        }

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        FollowUpRecyclerAdapter adapter = new FollowUpRecyclerAdapter(recyclerViewData,getApplicationContext(),newFollowUpCount , in, isCompleted , moduleID);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        ImageView closeButton = dialog.findViewById(R.id.imageView);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private static boolean hasRequiredPermissions(Context context) {
        // Replace with the actual permission you need to check
        String permission = "android.permission.ACCESS_FINE_LOCATION";
//        int result = context.checkCallingOrSelfPermission(permission);
//        return result == PackageManager.PERMISSION_GRANTED;

        // Check if the location permission is not granted
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Request location permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{permission},
                    LOCATION_PERMISSION_REQUEST_CODE);

            // Return false because the permission is not yet granted
            return false;
        }

        // Location permission is already granted
        return true;
    }

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Call this method in your Activity's onRequestPermissionsResult
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                  @NonNull int[] grantResults, Context context) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, you can proceed with your logic here
            } else {
                // Location permission denied, show a toast or handle the denial gracefully
                showToast(context, "Location permission denied");
            }
        }
    }


}
