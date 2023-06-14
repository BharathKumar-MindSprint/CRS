package com.olam.digital.ofis;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import com.crashlytics.android.answers.Answers;
//import com.crashlytics.android.answers.LoginEvent;
//import com.dynatrace.android.agent.Dynatrace;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.olam.digital.ofis.ManageTrainingRecords.Records;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.authentication.AccessTokenTask;
import com.olam.digital.ofis.authentication.AuthenticationClient;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.authentication.AuthenticationInterface;
import com.olam.digital.ofis.authentication.KeyCloakInfo;
import com.olam.digital.ofis.authentication.OnAccessTokenInterface;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.olam.digital.ofis.childlabour.AddNewChildActivity;
import com.olam.digital.ofis.childlabour.ChildLabourCompleteModuleActivity;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.ChildVisitDetailActivity;
import com.olam.digital.ofis.childlabour.ViewChildLabourSurveyActivity;
import com.olam.digital.ofis.downloadsurvey.ListDownSurveys;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;
import com.olam.digital.ofis.dynatrace.DynatraceHelper;
import com.olam.digital.ofis.farmplandownload.DownloadFarmPlanActivity;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenReqBody;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenResp;
import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.logging.FabricLogsWriter;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.multifarm.MultiFarmUtils;
import com.olam.digital.ofis.multifarm.UpdateMultiFarmActivity;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.AnswerTypesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ComplianceIndicatorItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.CurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.CurrencyItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DiagnosticModulesList;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DmRangeTextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.LookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionTypesItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionsItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.QuestionsSectionItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.Recurrence;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionActionPlansItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionLookupValuesItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionsItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.TextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmAnswerTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrency;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesISubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRangeTextBoxModuleQuestionsTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmTextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;
import com.olam.digital.ofis.recordpolygon.RecordPolygonActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends BaseListActivity implements Callback<AuthenticationDTO>,OnAccessTokenInterface, AdapterView.OnItemClickListener{

    // update
    public static final String TAG = "HomeActivity";
    private final String MATOMOTAG =this.getClass().getSimpleName();;
    private Context context;
    private static Context staticContext;
    private static Resources staticRes;
    private Resources res;
    private HomeActivity me;
    private static DataLayer dl;
    private NetworkLayer nl;
    private ProgressDialog m_ProgressDialog = null;
    private ProgressDialog mDialogLoading=null;
    int checkvalue = 0;
    private boolean farmerSynced = true, autotimeEnabled = false;
    private boolean multifarmSynced = true;
    private boolean farmerMultifarmSynced=true;
    private Cursor networkQueue;
    private CustomListAdapter_NetworkQueue customListAdapter_networkQueue;
    private static LinearLayout topSection;
    private static RelativeLayout mainSection;
    private static ScrollView firstRunSection;
    private static TextView queueCount;
    private static CardView captureMyLocationWrapper;
    private static CardView recordTrainingWrapper;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_NAME = "com.olam.digital.ofis.app_prefs";
    private static final String SHARED_FIREBASE_TOKEN_UPDATED = "firebase_token_updated";
    private static MenuItem settingsButton, tasksButton, notificationButton;
    private Handler mainHandler;

    private final static String EDIT_FARMER_ID = "EDIT_FARMER_ID";
    private final static String EDIT_TRANSACTION_ITEM_ID = "EDIT_TRANSACTION_ITEM_ID";
    private final static String EDIT_TRAININGRECORD_ID = "EDIT_TRAININGRECORD_ID";
    private final static String READ_ONLY_MODE = "READ_ONLY_MODE";
    private final static String EDIT_MODE = "EDIT_MODE";

    private static final String FARMER_ID = "FARMER_ID";
    private static final String FARM_ID = "FARM_ID";
    private static final String PLOT_COUNT = "PLOT_COUNT";
    private static final String OLAM_FARMER_ID = "OLAM_FARMER_ID";
    private static final String FARMER_NAME = "FARMER_NAME";

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static final String ACCOUNT_TYPE = BuildConfig.ACCOUNT_TYPE;
    private static final String ACCOUNT = "ofis_companion";
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SYNC_INTERVAL_IN_MINUTES = 1L;
    private static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;

    private EditText enumeratorID;
//    Passcode added - Penetration test
    private EditText passcode;
    private Button saveUpdateButton;

    // Runtime Permission Request Code
    private final static int MY_PERMISSIONS_REQUEST = 9999;

    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
    private final static int MY_SIGNATURE_PIC_PERMISSIONS_REQUEST = 9997;
    private static final int PERMISSIONS_ACCESS_LOCATION = 6575;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private boolean syncOptionAll;
    private int itemId;
    public static int totalItems;
    public static int progressItem;
    private long lastClickTime = 0;
    private long lastitemClickTime = 0;
    private boolean isChildSynced=true;
    ListView listView;
    private Boolean existingUser = false;
    private Location mCurrentLocation=null;
    GPSListener gpsListener=null;
    private GPSTracker gpsTracker=null;
    private Boolean newGps;
    private boolean islocationenabled = false;
    static String ipAddress,date;
    ImageButton syncAll;
    static CardView downloadSurveyWrapper;
    private static CardView cardview_downloadFarmplan;
   static String counrtyCodeNew;

    static String password = "Olam123+";
    static String system = "od";
    private static ProgressDialog progressDialog;
    static String odAccessToken;

    static boolean isdownloadfarmplan;
    static boolean isEnableDM = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        listView=findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        checkTls();
        staticContext = this;
        context = me = this;
        staticRes = getResources();
        res = getResources();
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
//        setTitle(R.string.applicationName);

        dl = new DataLayer(res, context);
        nl = new NetworkLayer(context, res);
        mainHandler = new Handler(Looper.getMainLooper());
        sharedPreferences = getSharedPreferences("com.olam.digital.ofis.app_prefs", MODE_PRIVATE);

        LinearLayout itemsHeader = (LinearLayout) ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.home_header_buttons, listView, false);
        listView.addHeaderView(itemsHeader);

        firstRunSection = (ScrollView) findViewById(R.id.firstRunWrapper);
        mainSection = (RelativeLayout) findViewById(R.id.mainWrapper);
        captureMyLocationWrapper = (CardView) findViewById(R.id.captureLocationWrapper);
        recordTrainingWrapper = (CardView) findViewById(R.id.recordTrainingWrapper);


        enumeratorID = (EditText) findViewById(R.id.enumeratorID);
        //

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.PleaseWait));
        progressDialog.setMessage(getString(R.string.Loading));


        downloadSurveyWrap();
        String enumeratorId=Utilities.getFromSharedPrefs(context);
        if(!enumeratorId.equalsIgnoreCase("")) {
            if (DynatraceHelper.isAppClosed) {
//                Dynatrace.identifyUser(enumeratorId);
//                DynatraceHelper.isAppClosed=false;
//                DynatraceHelper.findNetworkSpeed(this);
            }
        }
//        Passcode added - Penetration testing
        passcode = (EditText) findViewById(R.id.passcode);
        saveUpdateButton = (Button) findViewById(R.id.buttonSaveUpdate);
        topSection = (LinearLayout) findViewById(R.id.topSectionWrapper);
        downloadSurveyWrapper = (CardView) findViewById(R.id.downloadSurveyWrapper);
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // checkTime();
        bindNetworkQueue(true);
      //  if (Utilities.getKeyCloakClientDetails(context, KeyCloakInfo.Client_Id).equalsIgnoreCase("")){
        // Removed the check migrate existing enumerator while update Manna version
            String environment=BuildConfig.FLAVOR;
            setKeycloakCredentials(environment);
            setOperatingUnit(environment);
            setCommodity();
      //  }
        if (sharedPreferences.getBoolean("FIRST_RUN",true)) {
            firstRunSection.setVisibility(View.VISIBLE);
            if(!enumeratorId.equalsIgnoreCase("")) {
                existingUser = true;
                enumeratorID.setText(enumeratorId);
                enumeratorID.setFocusable(false);
                passcode.setFocusable(true);
                enumeratorID.setFocusableInTouchMode(false);
                enumeratorID.setClickable(false);
            }
        }

            Account mAccount = CreateSyncAcount();

            ContentResolver.setSyncAutomatically(
                    mAccount,
                    AUTHORITY,
                    true
            );

            ContentResolver.addPeriodicSync(
                    mAccount,
                    AUTHORITY,
                    Bundle.EMPTY,
                    SYNC_INTERVAL
            );

        // Request Permission only for Android Version Marshmallow and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check Phone Permission
            if (ContextCompat.checkSelfPermission(HomeActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                    .checkSelfPermission(HomeActivity.this,
                            android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.READ_PHONE_STATE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.RECORD_AUDIO,
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_SIGNATURE_PIC_PERMISSIONS_REQUEST);


                //request permission
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{android.Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST);

            }
        }

        cardview_downloadFarmplan =findViewById(R.id.cardview_downloadFarmplan);
       boolean isCropAvailable =  Utilities.getBoolFromSharedPrefs(staticContext, "DISPLAY_CROPPLAN", false);
        if(isCropAvailable){
            cardview_downloadFarmplan.setVisibility(View.VISIBLE);
        }else{
            cardview_downloadFarmplan.setVisibility(View.GONE);
        }

        Button downloadFarmplan = (Button) findViewById(R.id.DownloadFarmPlan);
        downloadFarmplan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_ADD_NEW_FARMER,  ScreenNameConstants.BUTTON_CLICKED);
                Intent intent = new Intent(context, DownloadFarmPlanActivity.class);
                context.startActivity(intent);

            }
        });

        saveUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(HomeActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(HomeActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_ACCESS_LOCATION);

                    return;
                }else {
                    doingAuthRequest();
                }
            }
        });

//        if (sharedPreferences.getBoolean("FIRST_RUN",true)) {
//
//
//            saveUpdateButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Request Permission only for Android Version Marshmallow and above
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        // Check Phone Permission
//                        if (ContextCompat.checkSelfPermission(HomeActivity.this,
//                                android.Manifest.permission.READ_PHONE_STATE)
//                                != PackageManager.PERMISSION_GRANTED) {
//
//                    //request permission
//                    ActivityCompat.requestPermissions(HomeActivity.this,
//                            new String[]{android.Manifest.permission.READ_PHONE_STATE},
//                            MY_PERMISSIONS_REQUEST);
//
//                } else {
//
//                    // Permission already granted
//                    firstTimeSetup();
//                }
//            } else {
//
//                        // Call methods directly for Android versions lower than Marshmallow
//                        firstTimeSetup();
//                    }
//                }
//            });
//        } else {
//            checkForUpdate();
//            mainSection.removeView(firstRunSection);
//            long currentVersionCode = sharedPreferences.getLong("VERSION_NUM", 0);
//            if (BuildConfig.VERSION_CODE > currentVersionCode) {
//                Toast.makeText(context, res.getString(R.string.upgrade_message), Toast.LENGTH_LONG).show();
//                Utilities.saveLongToSharedPrefs(context, "VERSION_NUM", BuildConfig.VERSION_CODE);
//                nl.syncData();
//            }
//        }

        gpsListener=new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location!=null){
                    mCurrentLocation=location;
                    TrackDetailsDTO trackDetailsDTO=new TrackDetailsDTO();
                    trackDetailsDTO.setModule(GPSConstants.MODULE_CAPTURE_CAPTURE_MY_LOCATION);
                    trackDetailsDTO.setLatitude(String.valueOf(mCurrentLocation.getLatitude()));
                    trackDetailsDTO.setLongitude(String.valueOf(mCurrentLocation.getLongitude()));
                    trackDetailsDTO.setAccuracy(String.valueOf(mCurrentLocation.getAccuracy()));
                    trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
                    ContentValues geoContentValues=new GPSUtils().getGeoContentValues(trackDetailsDTO);
                    if (newGps) {
                        long newGpsQueueId = new GpsTrackerLogic().insertGPSDetailsToQueue(context, geoContentValues);
                        if (newGpsQueueId != -1) {
                           enableSync();
                        }
                        newGps=false;
                    }
                }
            }
        };
        gpsTracker=new GPSTracker(this,gpsListener,true);
        try {
            MultiFarmUtils.isMultiFarmEnumerator = Utilities.getMultifarmPref(context, MultiFarmUtils.IS_MULTI_FARM_ENUMERATOR);
        }catch (Exception e){}


    }
    private void setKeycloakCredentials(String environment) {
        String clientId = null;
        String clientSecret=null;
        switch (environment){
            case "dev":
                clientId=KeyCloakInfo.dev_client_Id;
                clientSecret=KeyCloakInfo.dev_Client_Secret;

                // fake
//                clientId=KeyCloakInfo.dev_ofi_client_Id;
//                clientSecret=KeyCloakInfo.dev_ofi_Client_Secret;

                break;
            case "devofi":
                clientId=KeyCloakInfo.dev_ofi_client_Id;
                clientSecret=KeyCloakInfo.dev_ofi_Client_Secret;
                break;
            case "sitofi":
                clientId=KeyCloakInfo.sit_ofi_client_Id;
                clientSecret=KeyCloakInfo.sit_ofi_Client_Secret;
                break;
            case "sit":
                clientId=KeyCloakInfo.sit_non_ofi_client_Id;
                clientSecret=KeyCloakInfo.sit_non_ofi_Client_Secret;
                break;
            case "uat":
                clientId=KeyCloakInfo.uat_non_ofi_client_Id;
                clientSecret=KeyCloakInfo.uat_non_ofi_Client_Secret;
                break;
            case "uatofi":
                clientId=KeyCloakInfo.uat_ofi_client_Id;
                clientSecret=KeyCloakInfo.uat_ofi_Client_Secret;
                break;
            case "prod":
                clientId=KeyCloakInfo.prod_client_Id;
                clientSecret=KeyCloakInfo.prod_Client_Secret;
                break;

            case "prodofi":
                clientId=KeyCloakInfo.prod_ofi_client_Id;
                clientSecret=KeyCloakInfo.prod_ofi_Client_Secret;
                break;

                // OGA
            case "devoga":
                clientId=KeyCloakInfo.dev_oga_client_Id;
                clientSecret=KeyCloakInfo.dev_oga_Client_Secret;
                break;
            case "sitoga":
                clientId=KeyCloakInfo.sit_oga_client_Id;
                clientSecret=KeyCloakInfo.sit_oga_Client_Secret;
                break;
            case "uatoga":
                clientId=KeyCloakInfo.uat_oga_client_Id;
                clientSecret=KeyCloakInfo.uat_oga_Client_Secret;
                break;
            case "prodoga":
                clientId=KeyCloakInfo.prod_oga_client_Id;
                clientSecret=KeyCloakInfo.prod_oga_Client_Secret;
                break;

        }
        Utilities.setKeycloakClientDetails(context,KeyCloakInfo.Client_Id,clientId);
        Utilities.setKeycloakClientDetails(context,KeyCloakInfo.Client_Secret,clientSecret);
    }

    private void setOperatingUnit(String environment){
        String OpUnit="nil";
        if (environment.equalsIgnoreCase("dev") || environment.equalsIgnoreCase("sit") ||
        environment.equalsIgnoreCase("uat") || environment.equalsIgnoreCase("prod")){
            OpUnit="NON OFI";
        }else if (environment.equalsIgnoreCase("devofi") || environment.equalsIgnoreCase("sitofi") ||
                environment.equalsIgnoreCase("uatofi") || environment.equalsIgnoreCase("prodofi")){
            OpUnit="OFI";
        }else if (environment.equalsIgnoreCase("devoga") || environment.equalsIgnoreCase("sitoga") ||
                environment.equalsIgnoreCase("uatoga") || environment.equalsIgnoreCase("prodoga")){
            OpUnit="OGA";
        }else{
             OpUnit="nil";
        }
        Utilities.setOperatingUnitSharedPrefs(context,OpUnit);
    }

    private void setCommodity(){
        String commodity=dl.getProductInfo();
        Utilities.setCommoditySharedPrefs(context,commodity);
    }



    private void doingAuthRequest() {
        String userEnumerator = enumeratorID.getText().toString().trim();
//        Passcode validation - Penetration testing
        String userpasscode = passcode.getText().toString().trim();
        if (userEnumerator!=null && !userEnumerator.equalsIgnoreCase("") &&
                userpasscode!=null && !userpasscode.equalsIgnoreCase("")){
                showProgress(true);
                // Current OFIS Authentication
                // doAuthentication(userEnumerator,userpasscode);

                // KeyCloak Authentication
                 doKeyCloakAuthentication(userEnumerator,passcode.getText().toString().trim());

        }else{
            Toast.makeText(HomeActivity.this,getString(R.string.enumerator_empty),Toast.LENGTH_LONG).show();
        }

    }

    private void doKeyCloakAuthentication(String userEnumerator, String passcode) {

        String clientId=Utilities.getKeyCloakClientDetails(context,KeyCloakInfo.Client_Id);
        String clientSecret=Utilities.getKeyCloakClientDetails(context,KeyCloakInfo.Client_Secret);
        String grantType=Utilities.PASSWORD;
        String scope=Utilities.SCOPE;

        // Check the need of DTO
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername(userEnumerator);
        authRequestDTO.setPasscode(passcode);
        authRequestDTO.setGrantType(grantType);
        authRequestDTO.setClientId(clientId);
        authRequestDTO.setClientSecret(clientSecret);
        authRequestDTO.setScope(scope);

        AuthenticationInterface authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, HomeActivity.this);
        Call<AuthenticationDTO> authCall = authenticationInterface.getKeyCloakAuthToken(clientId, userEnumerator,passcode,grantType,clientSecret,scope);
        authCall.enqueue(HomeActivity.this);
    }


    protected void checkTls() {
        if (android.os.Build.VERSION.SDK_INT < 21) {
            try {
                ProviderInstaller.installIfNeeded(HomeActivity.this);

//                ProviderInstaller.installIfNeededAsync(this, new ProviderInstaller.ProviderInstallListener() {
//                    @Override
//                    public void onProviderInstalled() {
//                        SSLContext sslContext = null;
//                        try {
//                            sslContext = SSLContext.getInstance("TLSv1.2");
//                            sslContext.init(null, null, null);
//                            SSLEngine engine = sslContext.createSSLEngine();
//                        } catch (NoSuchAlgorithmException e) {
//                            e.printStackTrace();
//                        } catch (KeyManagementException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onProviderInstallFailed(int i, Intent intent) {
//                    }
//                });
              SSLContext  sslContext = SSLContext.getInstance("TLSv1.2");
              sslContext.init(null, null, null);
               SSLEngine engine = sslContext.createSSLEngine();
               engine.setEnabledProtocols(new String[]{"TLSv1.2"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        checkTime();
    }

    private static Account CreateSyncAcount() {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) staticContext.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            return newAccount;
        }

        return newAccount;
    }

    private void checkTime() {
        try {
            int auto_time_enabled = 0;
            int auto_time_zone_enabled = 0;
            if (Build.VERSION.SDK_INT <= 16) {
                auto_time_enabled = Settings.System.getInt(getContentResolver(), Settings.System.AUTO_TIME, 0);
                auto_time_zone_enabled = Settings.System.getInt(getContentResolver(), Settings.System.AUTO_TIME_ZONE);
            } else {
                auto_time_enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
                auto_time_zone_enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
            }

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
                autotimeEnabled = true;
                bindButtons();
            }
        } catch (Settings.SettingNotFoundException e) {
            FirebaseCrash.report(new Exception("HomeActivity: Error getting phone auto time settings", e));
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == -1) {
                    gpsTracker.getDeviceLocation();
                }
                break;
            default:
                if (requestCode == 6575) {
                    checkTime();
                }
        }
    }

    private void checkForUpdate() {
        if (nl.isPhoneOnline()) {
            @SuppressLint("HandlerLeak") Handler checkVersionHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 4) {
                        final JSONObject response = (JSONObject) msg.obj;
                        String columnname = "responseMessage" + Utilities.getLocaleSuffix(res);
                        if (columnname.equalsIgnoreCase("responseMessage_en")) {
                            columnname = "responseMessage";
                        }
                        if (response.has("downloadUrl")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setCancelable(true);
                            builder.setNegativeButton(res.getString(R.string.app_update_cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                            if (networkQueue.getCount() == 0) {
                                builder.setMessage(response.optString(columnname, res.getString(R.string.app_updated_generic_message)))
                                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.optString("downloadUrl", "https://otstgprd1001.blob.core.windows.net/mobileapp-download/OFIS-2.0-release.apk")));
                                                startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                               /* long daysLeftToUpdate = Utilities.daysToUpdate(context);
                                                if (daysLeftToUpdate >= 0) {
                                                    String reminder = res.getQuantityString(R.plurals.days_left_to_update, (int) daysLeftToUpdate, (int) daysLeftToUpdate);
                                                    Toast.makeText(context, reminder, Toast.LENGTH_LONG).show();
                                                }*/
                                            }
                                        });
                            } else {
                                builder.setMessage(response.optString(columnname, res.getString(R.string.app_updated_upload_first_message)))
                                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                if (SystemClock.elapsedRealtime() - lastClickTime < 3000){
                                                    return;
                                                }else {
                                                    syncOptionAll = true;
                                                    checkAuthenticate();
                                                }
                                                lastClickTime=SystemClock.elapsedRealtime();
                                            }
                                        })
                                        .setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                               /* long daysLeftToUpdate = Utilities.daysToUpdate(context);
                                                if (daysLeftToUpdate >= 0) {
                                                    String reminder = res.getQuantityString(R.plurals.days_left_to_update, (int) daysLeftToUpdate, (int) daysLeftToUpdate);
                                                    Toast.makeText(context, reminder, Toast.LENGTH_LONG).show();
                                                }*/
                                               dialog.cancel();
                                            }
                                        });

                            }
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                }
            };

            try {
                nl.checkVersion(checkVersionHandler);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("HomeActivity: Error requesting phone version check", e));
                e.printStackTrace();
            }
        }
    }

    private void firstTimeSetup() {

        if (enumeratorID.getText().length() > 0 && passcode.getText().length() >0) {
            Utilities.saveLongToSharedPrefs(context, "VERSION_NUM", BuildConfig.VERSION_CODE);
            Utilities.saveToSharedPrefs(context, "ENUMERATOR_ID", enumeratorID.getText().toString(), null);
            Utilities.saveToSharedPrefs(context, "PASSCODE", passcode.getText().toString().trim(), null);
            if(!existingUser) {
                nl.syncData();
            }else {
                completeFirstSetup();
            }
            //nl.postFirebaseToken(FirebaseInstanceId.getInstance().getToken());
        } else {
            Toast.makeText(context, R.string.Please_fill_all_fields, Toast.LENGTH_SHORT).show();
        }


    }

    public static void completeFirstSetup() {
        Utilities.saveToSharedPrefs(staticContext, "FIRST_RUN", "", false);
        Animation mSlideInTop = AnimationUtils.loadAnimation(staticContext, R.anim.in_top);
        Animation mSlideOutBottom = AnimationUtils.loadAnimation(staticContext, R.anim.out_bottom);
        if (settingsButton != null) {
            settingsButton.setVisible(true);
        }
        if (tasksButton != null) {
            tasksButton.setVisible(true);
        }
        if (notificationButton != null) {
            notificationButton.setVisible(true);
        }

//        boolean can_transact = Utilities.getBoolFromSharedPrefs(staticContext, "can_transact", false);
//        if (!can_transact) {
//            recordTransactionWrapper.setVisibility(View.GONE);
//            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) recordTrainingWrapper.getLayoutParams();
//            lp.setMargins(0,0,0,0);
//        }

        firstRunSection.startAnimation(mSlideOutBottom);
        topSection.startAnimation(mSlideInTop);

        mSlideOutBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainSection.removeView(firstRunSection);
                new viewSurveyWrapper().execute();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }



    private void bindNetworkQueue(boolean isClickable) {
        TextView nomodulestosubmittext = (TextView) findViewById(R.id.nomodulestosubmittext);
        networkQueue = dl.getNetworkQueue();

        if (networkQueue.getCount() > 0) {
            queueCount = (TextView) findViewById(R.id.networkQueueCount);
            totalItems = networkQueue.getCount();
            String queueTotal = Integer.toString(totalItems);
            queueCount.setText(queueTotal);

            nomodulestosubmittext.setVisibility(View.GONE);
            String[] adapterCols = new String[]{"_id", OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID};
            int[] adapterRowViews = new int[]{R.id.networkQueueId, R.id.networkQueueName, R.id.networkQueuePoiType, R.id.networkQueueOlamFarmerId};
            customListAdapter_networkQueue = new CustomListAdapter_NetworkQueue(context, networkQueue, adapterCols, adapterRowViews, me , isClickable);
            listView.setAdapter(customListAdapter_networkQueue);
            //setListAdapter(customListAdapter_networkQueue);

            if (Utilities.daysToUpdate(context) <= 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setMessage(res.getString(R.string.ofis_update_overdue))
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }

        } else {
            listView.setAdapter(null);
            queueCount = (TextView) findViewById(R.id.networkQueueCount);
            queueCount.setText("0");
            if (Utilities.daysToUpdate(context) <= 0) {
                nomodulestosubmittext.setText(R.string.ofis_update_overdue);
                nomodulestosubmittext.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

    }

    private void checkAuthenticate(){

        if (nl.isPhoneOnline()) {
            AccessTokenTask accessTokenTask = new AccessTokenTask(context, getAuthRequestDTO(), HomeActivity.this);
            accessTokenTask.execute();
        }else{
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    private AuthRequestDTO getAuthRequestDTO(){
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername(Utilities.getFromSharedPrefs(HomeActivity.this));
        authRequestDTO.setPasscode(Utilities.getPasscodeFromSharedPrefs(HomeActivity.this));
        return authRequestDTO;
    }

    public void enableSync(){
        try{
            setupSyncAllBtn(true);
            bindNetworkQueue(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startAllItemSync() {
        if (nl.isPhoneOnline()) {


            final String message = res.getString(R.string.Please_wait) + " " + res.getString(R.string.Submitting_data);
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    m_ProgressDialog = Utilities.openProgressDialog(context, message, true);
                    if (!farmerSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.farmer_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    }else if(!isChildSynced){
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.child_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    }else if(!multifarmSynced){
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.multifarm_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    }else if(!farmerMultifarmSynced){
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.sync_farmer_and_multifarm_stakeholders));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    }
                    else{
                        if(totalItems == 0)
                            totalItems = networkQueue.getCount();

                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, totalItems, 0, message);

                    }
                }
            });

            Handler syncHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                            String pdUpdate = msg.obj.toString();
                            int storedCount=Utilities.minProgressCount;
                            int min = storedCount+1;
                            int max = networkQueue.getCount();
                            Log.d("HomeActivity", "min : " + min  + ",  max : " + max);
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, max, min, pdUpdate);
                          //  m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, msg.arg1, msg.arg2, pdUpdate);
                        }
                    } else if (msg.what == 2) {
                        finishItemSync(msg);
                    } else if (msg.what == 3) {
                        farmerSynced = false;
                    }else if (msg.what == 5){
                        isChildSynced=false;
                    } else if (msg.what == 7){
                        multifarmSynced=false;
                    }else if (msg.what == 8){
                        farmerMultifarmSynced=false;
                    }else if(msg.what ==9) {
                        closePopup(msg);
                    }
                }
            };
            farmerSynced = true;
            isChildSynced=true;
            multifarmSynced=true;
            farmerMultifarmSynced=true;
            nl.getNetworkQueue(null);
            nl.syncNetworkQueue(null, syncHandler);
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    public void setupSyncAllBtn(boolean isClickable){
        if(isClickable){
            syncAll.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_all_white));
            syncAll.setEnabled(true);
        }else{
            syncAll.setEnabled(false);
            syncAll.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_gray));
        }
    }

    public void startItemSyncAuth(int itemId){

       /* if (SystemClock.elapsedRealtime() - lastitemClickTime < 3000){
            return;
        }else {*/
       /* syncAll.setEnabled(false);
        syncAll.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_sync_black));*/
        setupSyncAllBtn(false);
        bindNetworkQueue(false);
        this.itemId = itemId;
        syncOptionAll = false;
        checkAuthenticate();
       /* final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                syncAll.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_list));
                syncAll.setEnabled(true);
            }
        }, 5000);*/

//        }
//        lastitemClickTime = SystemClock.elapsedRealtime();
    }

    public void startItemSync(int itemId) {
        if (nl.isPhoneOnline()) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    m_ProgressDialog = Utilities.openProgressDialog(context, res.getString(R.string.Please_wait) + " " + res.getString(R.string.Submitting_data), false);
                    if (!farmerSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.farmer_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    }else if (!isChildSynced){
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.child_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    }
                    else if (!multifarmSynced){
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.multifarm_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    }else if (!farmerMultifarmSynced){
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.sync_farmer_and_multifarm_stakeholders));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    }
                }
            });

            Handler syncHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {

                    if (msg.what == 1) {
                        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                            String pdUpdate = msg.obj.toString();
                            Log.d("HomeActivity", "msg.arg1 : " + msg.arg1 + ",msg.arg2 : " + ",networkQueue.getCount() : " + networkQueue.getCount());
//                            int storedCount=Utilities.minProgressID;
//                            int min = storedCount+1;
//                            int max = networkQueue.getCount();
//                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, max, min, pdUpdate);
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, msg.arg1, msg.arg2, pdUpdate);

                        }
                    } else if (msg.what == 2) {
                        finishItemSync(msg);
                    } else if (msg.what == 3) {
                        farmerSynced = false;
                    } else if (msg.what ==5){
                        isChildSynced=false;
                    }else if (msg.what ==7){
                        multifarmSynced=false;
                    }else if (msg.what ==8){
                        farmerMultifarmSynced=false;
                    }else if(msg.what ==9) {
                        closePopup(msg);
                    }
                    else {
                        finishItemSync(msg);
                    }
                }
            };
            farmerSynced = true;
            isChildSynced=true;
            multifarmSynced=true;
            farmerMultifarmSynced=true;
            nl.getNetworkQueue(itemId);
            nl.syncNetworkQueueFromHome(itemId, syncHandler);
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
       enableSync();
    }

    private void finishItemSync(final Message msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (m_ProgressDialog!=null && m_ProgressDialog.isShowing())
                { String pdUpdate = msg.obj.toString();
                 m_ProgressDialog.setMessage(pdUpdate);
                  //  String pdUpdate = msg.obj.toString();
                //    m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, totalItems/*msg.arg1*/, /*progressItem*/msg.arg2, pdUpdate);
                    Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                }
                networkQueue.close();
//                customListAdapter_networkQueue.swapCursor(networkQueue);
                enableSync();
                networkQueue = dl.getNetworkQueue();
                String queueTotal = Integer.toString(networkQueue.getCount());
                queueCount.setText(queueTotal);
                totalItems = networkQueue.getCount();
                if (networkQueue.getCount() == 0) {
                    TextView nomodulestosubmittext = (TextView) findViewById(R.id.nomodulestosubmittext);
                    nomodulestosubmittext.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void closePopup(final Message msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (m_ProgressDialog!=null && m_ProgressDialog.isShowing())
                {
                    Utilities.closeProgressDialog(m_ProgressDialog, 200);
                }
                networkQueue.close();
//                customListAdapter_networkQueue.swapCursor(networkQueue);
                enableSync();
                networkQueue = dl.getNetworkQueue();
                String queueTotal = Integer.toString(networkQueue.getCount());
                queueCount.setText(queueTotal);
                totalItems = networkQueue.getCount();
                if (networkQueue.getCount() == 0) {
                    TextView nomodulestosubmittext = (TextView) findViewById(R.id.nomodulestosubmittext);
                    nomodulestosubmittext.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void downloadSurveyWrap(){
       try {
            ArrayList<ModuleDTO> duplicateList = dl.viewModuleList();
            ArrayList<ModuleDTO> moduleList = dl.removeDuplicateModule(duplicateList);
            if (moduleList == null || moduleList.size() == 0) {
                downloadSurveyWrapper.setVisibility(View.GONE);
            }else{
                downloadSurveyWrapper.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void downloadSurvey(){

        try {
            DataLayer dl = new DataLayer(staticContext.getResources(), staticContext);
            ArrayList<ModuleDTO> duplicateList = dl.viewModuleList();
            ArrayList<ModuleDTO> moduleList = dl.removeDuplicateModule(duplicateList);
            if (moduleList == null || moduleList.size() == 0) {
                downloadSurveyWrapper.setVisibility(View.GONE);
            }else{
                downloadSurveyWrapper.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void bindButtons() {
        Button addNewFarmer = (Button) findViewById(R.id.AddNewFarmer);
        Button completeModule = (Button) findViewById(R.id.CompleteQuestionnaireModule);
        Button addSocialInfra = (Button) findViewById(R.id.AddSocialInfrastructure);
        Button captureLocation = (Button) findViewById(R.id.captureLocation);
        Button recordTraining = (Button) findViewById(R.id.RecordFarmerTraining);
        Button downloadSurvey = (Button) findViewById(R.id.downloadSurveyData);

        syncAll = (ImageButton) findViewById(R.id.syncAll);
        boolean can_transact = Utilities.getBoolFromSharedPrefs(context, "can_transact", false);
        downloadSurveyWrap();
        downloadSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(context, ListDownSurveys.class);
                startActivity(in);
            }
        });
        syncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncAll.setEnabled(false);
                syncAll.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_gray));
//                final Handler handler = new Handler(Looper.getMainLooper());
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SYNC_ALL_ITEM,  ScreenNameConstants.BUTTON_CLICKED);
                if (SystemClock.elapsedRealtime() - lastClickTime < 3000){
                    return;
                }else{
                    if (networkQueue.getCount() > 0) {
                        syncOptionAll=true;
                        setupSyncAllBtn(false);
                        bindNetworkQueue(false);
                        checkAuthenticate();
                    }
                }
                lastClickTime=SystemClock.elapsedRealtime();
               /* handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        syncAll.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_list));
                        syncAll.setEnabled(true);
                    }
                }, 5000);*/
            }
        });

        if (Utilities.daysToUpdate(context) > 0 && autotimeEnabled) {

            addNewFarmer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_ADD_NEW_FARMER,  ScreenNameConstants.BUTTON_CLICKED);
                    Intent intent = new Intent(context, AddFarmerActivity.class);
                    intent.putExtra(EDIT_FARMER_ID, (long) 0);
                    intent.putExtra(EDIT_MODE, false);
                    context.startActivity(intent);


                }
            });

            completeModule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_COMPLETE_QUESTIONNAIRE_MODULE,  ScreenNameConstants.BUTTON_CLICKED);
                    Intent intent = new Intent(context, FindFarmerActivity.class);
                    context.startActivity(intent);
                }
            });


            addSocialInfra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_ADD_NEW_SIP,  ScreenNameConstants.BUTTON_CLICKED);
                    AddInfrastructureActivity.editMode = false;
                    AddInfrastructureActivity.infrastructureID = 0;
                    Intent intent = new Intent(context, AddInfrastructureActivity.class);
                    context.startActivity(intent);
//                    GetAllDiagnosticModule();
                }
            });

            // Hiding the record transaction section
//            if (can_transact || sharedPreferences.getBoolean("FIRST_RUN",true)) {
//                recordTransaction.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(context, TransactionItems.class);
//                        context.startActivity(intent);
//                    }
//                });
//            } else {
//                recordTransactionWrapper.setVisibility(View.GONE);
//                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) recordTrainingWrapper.getLayoutParams();
//                lp.setMargins(0,0,0,0);
//            }

            captureLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // nl.syncGPSData();
                    MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CAPTURE_MY_LOCATION,  ScreenNameConstants.BUTTON_CLICKED);
                    if (SystemClock.elapsedRealtime() - lastClickTime < 3000){
                        return;
                    }else {
                        newGps=true;
                        gpsTracker.getDeviceLocation();
                    }
                    lastClickTime=SystemClock.elapsedRealtime();
                }
            });

            recordTraining.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_RECORD_TRAINING,  ScreenNameConstants.BUTTON_CLICKED);
                    Intent intent = new Intent(context, Records.class);
                    context.startActivity(intent);
                }
            });

        } else {
            addNewFarmer.setAlpha(.5f);
            addNewFarmer.setClickable(false);
            completeModule.setAlpha(.5f);
            completeModule.setClickable(false);
            addSocialInfra.setAlpha(.5f);
            addSocialInfra.setClickable(false);
            captureLocation.setAlpha(.5f);
            captureLocation.setClickable(false);
            recordTraining.setAlpha(.5f);
            recordTraining.setClickable(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_HOME_ACTIVITY, ScreenNameConstants.SCREEN_HOME_ACTIVITY, ScreenNameConstants.SCREEN_HOME);
        checkTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (autotimeEnabled) {
            getMenuInflater().inflate(R.menu.menu_home, menu);

            if (sharedPreferences.getBoolean("FIRST_RUN", true)) {
                settingsButton = menu.findItem(R.id.action_settings);
                settingsButton.setVisible(false);
                tasksButton = menu.findItem(R.id.action_tasks);
                tasksButton.setVisible(false);
                notificationButton = menu.findItem(R.id.action_notification);
                notificationButton.setVisible(false);
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        if (!networkQueue.isClosed()) {
            networkQueue.close();
        }
        if (m_ProgressDialog!=null && m_ProgressDialog.isShowing()) {
            m_ProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    private void showSettingsDialog(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Permission was permanently denied");
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName())));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(HomeActivity.this, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
                    }
                });
        dialog.show();
    }


    @Override
    public void onResponse(Call<AuthenticationDTO> call, Response<AuthenticationDTO> response) {
        showProgress(false);
        if (response.isSuccessful()) {
            AuthenticationDTO authenticationDTO = response.body();
            authenticationDTO.setWillExpireAt((System.currentTimeMillis() / 1000) + authenticationDTO.getExpiresIn() / 1000);
            new AuthenticationHelper(this).setAuthenticationDTO(authenticationDTO);
            new AuthenticationHelper(this).setUserName(enumeratorID.getText().toString());
            new AuthenticationHelper(this).setPassode(passcode.getText().toString().trim());


//            Dynatrace.identifyUser(enumeratorID.getText().toString());
            DynatraceHelper.isAppClosed=false;
            DynatraceHelper.findNetworkSpeed(this);
            doInitialCheck();
        }else if(response.code() == 423){
            enableSync();
            Toast.makeText(HomeActivity.this,getString(R.string.authentication_attempt),Toast.LENGTH_LONG).show();
        }
        else {
            try {
                FabricLogsWriter.printCrashLog(TAG,"oauth/authenticate",response.code(),"Request : :  "+new Gson().toJson(getAuthRequestDTO()).toString() +"   Response :  "+response.raw().networkResponse().toString() + "user Id :" +enumeratorID.getText().toString().trim(),context);
            }catch (Exception e){}
            enableSync();
            Toast.makeText(HomeActivity.this,getString(R.string.authentication_failed),Toast.LENGTH_LONG).show();
//            Answers.getInstance().logLogin(new LoginEvent()
//                    .putMethod("first")
//                    .putCustomAttribute("reason", "wrong credentials")
//                    .putSuccess(false));
        }
    }

    @Override
    public void onFailure(Call<AuthenticationDTO> call, Throwable t) {
        showProgress(false);
        Toast.makeText(HomeActivity.this,getString(R.string.authentication_failed),Toast.LENGTH_LONG).show();
//        Answers.getInstance().logLogin(new LoginEvent()
//                .putMethod("first")
//                .putCustomAttribute("reason", "network error")
//                .putSuccess(false));
    }

    private void doInitialCheck() {

        if (sharedPreferences.getBoolean("FIRST_RUN", true)) {
            // Request Permission only for Android Version Marshmallow and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check Phone Permission
                if (ContextCompat.checkSelfPermission(HomeActivity.this,
                        android.Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {

                    //request permission
                   /* ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{android.Manifest.permission.READ_PHONE_STATE},
                            MY_PERMISSIONS_REQUEST);*/

                } else {

                    // Permission already granted
                    firstTimeSetup();
                }
            } else {

                // Call methods directly for Android versions lower than Marshmallow
                firstTimeSetup();
            }
        } else {
            checkForUpdate();
            mainSection.removeView(firstRunSection);
            long currentVersionCode = sharedPreferences.getLong("VERSION_NUM", 0);
            if (BuildConfig.VERSION_CODE > currentVersionCode) {
                Toast.makeText(context, res.getString(R.string.upgrade_message), Toast.LENGTH_LONG).show();
                Utilities.saveLongToSharedPrefs(context, "VERSION_NUM", BuildConfig.VERSION_CODE);
                nl.syncData();

            }else{Utilities.isAppAlive=true;}
        }

    }

    private void showProgress(boolean show) {
        if (show && mDialogLoading == null) {
            mDialogLoading = ProgressDialog.show(this, "", getString(R.string.loggin_In), true);
            mDialogLoading.setCancelable(false);
            mDialogLoading.show();
        } else if (mDialogLoading != null) {
            mDialogLoading.dismiss();
            mDialogLoading = null;
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_LOCATION: {
                try {
                    for (String permission : permissions) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            // Permission Denied
                            Toast.makeText(this, R.string.gps_permission_denial_msg, Toast.LENGTH_LONG).show();
                            String userEnumerator = enumeratorID.getText().toString().trim();
                            if (userEnumerator!=null && !userEnumerator.equalsIgnoreCase("")){
                                doingAuthRequest();
                            }

                        } else {
                            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                                // Permission Allowed
                                if(checkvalue == 0) {
                                    checkvalue++;
                                    doingAuthRequest();
                                }
                            } else {
                                // Checkbox set to Never Ask Again
                                showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
                            }
                        }
                    }
                    break;
            }catch (Exception e ){}

            }

            case MY_PROFILE_PIC_PERMISSIONS_REQUEST: {
                try{
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {



                    } else if (Build.VERSION.SDK_INT >= 23
                            && (!shouldShowRequestPermissionRationale(permissions[0])
                            || !shouldShowRequestPermissionRationale(permissions[1]))) {
                        showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));

                    } else {
                        Toast.makeText(this, R.string.camera_and_gallery_permission_denial_msg, Toast.LENGTH_LONG).show();
                    }
                    break;
                }catch (Exception e){}

            }
            case MY_SIGNATURE_PIC_PERMISSIONS_REQUEST: {

                try {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {



                    } else if (Build.VERSION.SDK_INT >= 23
                            && (!shouldShowRequestPermissionRationale(permissions[0])
                            || !shouldShowRequestPermissionRationale(permissions[1]))) {
                        showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));

                    } else {
                        Toast.makeText(this, R.string.camera_and_gallery_permission_denial_msg, Toast.LENGTH_LONG).show();
                    }
                    break;

                }catch (Exception e){}
            }



        }
    }

    @Override
    public void onTokenCompleted(String status) {
        enableSync();
        if(!status.equalsIgnoreCase("false")){
            Utilities.minProgressCount=0;
            DynatraceHelper.findNetworkSpeed(this);
               if(syncOptionAll){
                  startAllItemSync();
              }else{
                   startItemSync(itemId);
              }
        }else{
            Toast.makeText(HomeActivity.this,getString(R.string.authentication_failed),Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        // carry out relevant action based on item clicked
        try {

            if (networkQueue.getCount() > 1) {
                networkQueue.moveToPosition(position - 1);
            }
            String itemType = networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE));
            if (itemType.equalsIgnoreCase("infrastructure")) {
                Intent intent = new Intent(this, AddInfrastructureActivity.class);
                AddInfrastructureActivity.infrastructureID = id;
                AddInfrastructureActivity.editMode = true;
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("farmer")) {
                Intent intent = new Intent(this, AddFarmerActivity.class);
                intent.putExtra(EDIT_FARMER_ID, (long) networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                intent.putExtra("farmId", (long) networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID)));
                intent.putExtra(EDIT_MODE, true);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("farmlocation")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //long farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("biometric_update")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //long farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("farmpolygon")) {
                Intent recordPolygonIntent = new Intent(this, RecordPolygonActivity.class);
                final String lFarmID = networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID));
//            int count = SharedPreferenceUtils.getInstance(getApplicationContext()).getPlotCount(""+lFarmID);
                DataLayer dl = new DataLayer(getResources(), HomeActivity.this);
                dl.fetchPlotData(networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                int count = 0;
//        if(strFarmID != "0"){
                try {
//            count = SharedPreferenceUtils.getInstance(getApplicationContext()).getPlotCount(""+strFarmID);
                    count = dl.fetchPlotData(networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                } catch (Exception e) {
                    count = 0;
                }
                recordPolygonIntent.putExtra(FARMER_ID, Long.valueOf(networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID))));
                recordPolygonIntent.putExtra(FARM_ID, lFarmID);
                recordPolygonIntent.putExtra(OLAM_FARMER_ID, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                recordPolygonIntent.putExtra(FARMER_NAME, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
                recordPolygonIntent.putExtra(PLOT_COUNT, count);
                recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
                startActivity(recordPolygonIntent);
            } else if (itemType.equalsIgnoreCase("module")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                CompleteModuleActivity.editMode = true;
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //long farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("childmodule")) {
                Intent intent = new Intent(this, ViewChildLabourSurveyActivity.class);
                ChildLabourCompleteModuleActivity.editMode = true;
                ViewChildLabourSurveyActivity.farmerID = Long.valueOf(networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                ViewChildLabourSurveyActivity.childID = Long.valueOf(networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID)));
                //long farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("fmpreview") || itemType.equalsIgnoreCase("fullfmpreview")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //long farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("transaction")) {
                Intent intent = new Intent(context, TransactionItem.class);
                intent.putExtra(EDIT_TRANSACTION_ITEM_ID, networkQueue.getLong(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRANSACTION_ITEM_ID)));
                intent.putExtra(EDIT_MODE, true);
                intent.putExtra(READ_ONLY_MODE, true);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("trainingrecord")) {
                Intent intent = new Intent(context, TrainingRecord.class);
                intent.putExtra(EDIT_TRAININGRECORD_ID, networkQueue.getLong(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRAINING_RECORD_ID)));
                intent.putExtra(EDIT_MODE, true);
                intent.putExtra(READ_ONLY_MODE, true);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("updatesubdistrict")) {
                Intent in = new Intent(this, UpdateSubdistrict.class);
                Bundle bundle = new Bundle();
                bundle.putString("olamfarmername", networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
                bundle.putInt(ChildLabourUtils.CHILD_FARMER_ID, networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                bundle.putBoolean(EDIT_MODE, true);
                bundle.putBoolean(READ_ONLY_MODE, true);
                in.putExtras(bundle);
                startActivity(in);
            } else if (itemType.equalsIgnoreCase("farmer_photo")) {
                //JIRA req OW-505
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("children")) {
                Intent intent = new Intent(this, AddNewChildActivity.class);
                intent.putExtra("action", "sync_edit");
                intent.putExtra(ChildLabourUtils.CHILD_FARMER_ID, String.valueOf(networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID))));
                intent.putExtra(ChildLabourUtils.CHILD_ID, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID)));
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("child_visit")) {
                Bundle childBundle = new Bundle();
                childBundle.putString(ChildLabourUtils.CHILD_FARMER_ID, String.valueOf(networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID))));
                Intent intent = new Intent(this, ChildVisitDetailActivity.class);
                intent.putExtras(childBundle);
//            intent.putExtra(ChildLabourUtils.CHILD_FARMER_ID, String.valueOf(networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID))));
//            intent.putExtra(ChildLabourUtils.CHILDLABOUR_SURVEY, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_SURVEY_CODE)));
                startActivity(intent);

            } else if (itemType.equalsIgnoreCase("Multifarm Update")) {
                Bundle bundle = new Bundle();
                bundle.putString("farmerId", String.valueOf(networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID))));
                bundle.putString("farmId", String.valueOf(networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID))));
                bundle.putBoolean("editMode", true);
                bundle.putBoolean("isSurveyEnabled",false);
                Intent intent = new Intent(this, UpdateMultiFarmActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        }catch (CursorIndexOutOfBoundsException e){}
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
      //  Toast.makeText(this,"Disable the back press",Toast.LENGTH_LONG).show();
    }

    private static class viewSurveyWrapper extends AsyncTask<Object, Boolean, Boolean> {
        ArrayList<ModuleDTO> moduleList = new ArrayList<>();
        @Override
        protected Boolean doInBackground(Object... objects) {
            try {
                DataLayer dl = new DataLayer(staticContext.getResources(), staticContext);
                ArrayList<ModuleDTO> duplicateList = dl.viewModuleList();
                moduleList = dl.removeDuplicateModule(duplicateList);

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (moduleList == null || moduleList.size() == 0) {
                downloadSurveyWrapper.setVisibility(View.GONE);
            }else{
                downloadSurveyWrapper.setVisibility(View.VISIBLE);
            }

            GetAccessToken("home");

        }
    }


    //
    public static void GetAccessToken(String value) {
        isEnableDM = Utilities.getBoolFromSharedPrefs(staticContext, Utilities.DM_ENUMERATOR,false);
        String isoCode = Utilities.getdatafromSharedPrefs(staticContext,"CountryUserID");
        Log.d("COUNTRY_CODENEWPREFFF", isoCode);
        counrtyCodeNew =isoCode+"_OFIS_DO_INTERFACE";

        progressDialog.show();
        final AccessTokenReqBody accessTokenReqBody = new AccessTokenReqBody(counrtyCodeNew,password,system);

        ManageRecords GetAccessToken= RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<AccessTokenResp> call=GetAccessToken.getAccessToken(accessTokenReqBody);
        call.enqueue(new Callback<AccessTokenResp>() {
            @Override
            public void onResponse(Call<AccessTokenResp> call, Response<AccessTokenResp> response) {
                if(response.code()==200){
                    progressDialog.hide();

                    odAccessToken = response.body().getAccessToken();

                    //Toast.makeText(DownloadFarmPlanActivity.this,odAccessToken, Toast.LENGTH_SHORT).show();
                    Log.e("OD_AccessTOKEN",odAccessToken);

                        DownloadFormList("home");

                }else if(response.code()==400){
                    progressDialog.hide();
                    Utilities.saveToSharedPrefs(staticContext, "DISPLAY_CROPPLAN", "", false);
                    if(value.equalsIgnoreCase("home")){
                        cardview_downloadFarmplan.setVisibility(View.GONE);
                        if(isEnableDM) {
                            GetAllDiagnosticModule();
                        }
                    }
                   // showAlert("Unable to fetch country name for the ISOCode"+response.message());
                }else if (response.code()==500){
                    progressDialog.hide();
                    Utilities.saveToSharedPrefs(staticContext, "DISPLAY_CROPPLAN", "", false);
                    if(value.equalsIgnoreCase("home")){
                        cardview_downloadFarmplan.setVisibility(View.GONE);
                        if(isEnableDM) {
                            GetAllDiagnosticModule();
                        }
                    }
                   // showAlert("Internal Server Error, Please Contact Admin!!"+response.message());
                }else{

                }
            }
            @Override
            public void onFailure(Call<AccessTokenResp> call, Throwable t) {
                progressDialog.hide();
                Toast.makeText(staticContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    /*Get DownLoad Farm Form List*/
    public static void DownloadFormList(String value) {
        //progressDialog.show();
       // String userEnumeratorID = enumeratorID.getText().toString().trim();
        ManageRecords AllFarmFormList= RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<DownloadFormList> call=AllFarmFormList.getAllFarmFormList(1,counrtyCodeNew, Integer.valueOf(Utilities.getFromSharedPrefs(staticContext)),odAccessToken);
        call.enqueue(new Callback<DownloadFormList>() {

            @Override
            public void onResponse(Call<DownloadFormList> call, Response<DownloadFormList> response) {
                Log.e("ListOnResponse",response.message());
                Log.e("ListOnRequest",call.toString());

                if(response.code()==200){

                    //Toast.makeText(DownloadFarmPlanActivity.this, "Farm Plan is available to download", Toast.LENGTH_SHORT).show();
                   // progressDialog.hide();
                    if(response.body()!=null && response.body().getStatusCode()==200){
                        if(response.body().getData()!=null){
                            //data1 =response.body().getData();

                            ArrayList < DownloadFormList.data > dataArrayList = response.body().getData();;
                           // dataArrayList = response.body().getData();
                            //String[] names = new String[dataArrayList.size()];
                            for (int i = 0; i < dataArrayList.size(); i++) {
                                 isdownloadfarmplan = dataArrayList.get(i).isIsdownloadfarmplan();
                                 Log.d("FARMPLAN_Status", String.valueOf(isdownloadfarmplan));

                                 if(isdownloadfarmplan && dataArrayList.size()>0){
                                     Utilities.saveToSharedPrefs(staticContext, "DISPLAY_CROPPLAN", "", true);
                                     if(value.equalsIgnoreCase("home")){
                                         cardview_downloadFarmplan.setVisibility(View.VISIBLE);
                                     }

                                }else{
                                     Utilities.saveToSharedPrefs(staticContext, "DISPLAY_CROPPLAN", "", false);
                                     if(value.equalsIgnoreCase("home")){
                                         cardview_downloadFarmplan.setVisibility(View.GONE);
                                     }
                                 }
                            }
                            Log.e("ListRespValue",response.body().getStatusMessage());

                        }else{
                            //Toast.makeText(DownloadFarmPlanActivity.this,response.body().getStatusMessage(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(DownloadFarmPlanActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                            //showAlert(response.body().getStatusMessage());
                        }
                    }else{
                       // showAlert(response.body().getStatusMessage());
                        // Toast.makeText(DownloadFarmPlanActivity.this, R.string.no_data_found, Toast.LENGTH_SHORT).show();
                    }
                }else if(response.code()==400){
                   // progressDialog.hide();
                    //showAlert(response.message().toString());
                    //Toast.makeText(staticContext,"Bad Request", Toast.LENGTH_SHORT).show();
                    Log.d("BadRequest-",response.message());
                }else if(response.code()==500){
                    //progressDialog.hide();
                    //showAlert("Internal Server Error, Please Contact Admin!!-"+response.message().toString());
                }else{
                   // showAlert("Server Issue!!"+response.message().toString());
                }
               if(isEnableDM) {
                    GetAllDiagnosticModule();
                }
            }
            @Override
            public void onFailure(Call<DownloadFormList> call, Throwable t) {
                //progressDialog.hide();
                //Toast.makeText(staticContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
               // GetAllDiagnosticModuleHome();
            }
        });

    }

    private static void showAlert(String msg) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(staticContext);
        builder.setMessage(msg);
        builder.setTitle(R.string.Alert_txt);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public boolean isOnline(Context c){
        ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo ni=cm.getActiveNetworkInfo();
        if(ni !=null && ni.isConnectedOrConnecting()){
            return true;
        }else{
            return false;
        }
    }


    public static void GetAllDiagnosticModule() {

        ManageRecords AllDiagnosticModuleData = RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<ResponseModel> call = AllDiagnosticModuleData.getAllDiagnosticModuleData(Integer.valueOf(Utilities.getFromSharedPrefs(staticContext)));
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                OFISRoomDB db = OFISRoomDB.getDbInstance(staticContext);

                try {
                    if (response.code() == 200) {

                        db.dmSectionsItemDao().deleteAll();
                        db.dmQuestionsSubModuleDao().deleteAll();
                        db.dmLookupValuesISubmoduleDao().deleteAll();
                        db.dmActionPlansItemSubmoduleDao().deleteAll();
                        db.dmQuestionsModuleDao().deleteAll();
                        db.dmLookupValuesItemDao().deleteAll();
                        db.dmActionPlansItemDao().deleteAll();
                        db.dmMainModuleDao().deleteAll();
                        db.dmAnswerTypesDao().deleteAll();
                        db.dmQuestionTypesDao().deleteAll();
                        db.dmCurrenciesItemDao().deleteAll();
                        db.dmTextBoxModuleQuestionsDao().deleteAll();
                        db.dmRangeTextBoxModuleQuestionsTableDao().deleteAll();
                        db.dmTextBoxModuleQuestionsDao().deleteAll();
                        db.dmRangeTextBoxModuleQuestionsTableDao().deleteAll();
                        db.complianceIndicatorDao().deleteAll();


                        if (response.body().getDiagnosticModules() != null) {
                            ArrayList<DiagnosticModulesList> mainResponseList = new ArrayList<>();
                            mainResponseList.clear();
                            mainResponseList = response.body().getDiagnosticModules();


                            for (int i = 0; i < mainResponseList.size(); i++) {
                                int id = mainResponseList.get(i).getId();
                                int followUpDuration = mainResponseList.get(i).getFollowUpDuration();
                                String assessmentType = mainResponseList.get(i).getAssessmentType();
                                int surveyType = mainResponseList.get(i).getSurveyType();
                                int followUpAttempts = mainResponseList.get(i).getFollowUpAttempts();
                                String name = mainResponseList.get(i).getName();
                                String nameFr = mainResponseList.get(i).getNameFr();
                                String nameIdn = mainResponseList.get(i).getNameIdn();
                                String namePt = mainResponseList.get(i).getNamePt();
                                String nameLo = mainResponseList.get(i).getNameLo();
                                String nameTr = mainResponseList.get(i).getNameTr();
                                String nameTh = mainResponseList.get(i).getNameTh();
                                String nameVi = mainResponseList.get(i).getNameVi();
                                String nameEs = mainResponseList.get(i).getNameEs();
                                String descriptionPt = mainResponseList.get(i).getDescriptionPt();
                                String description = mainResponseList.get(i).getDescription();
                                String descriptionVi = mainResponseList.get(i).getDescriptionVi();
                                String descriptionEs = mainResponseList.get(i).getDescriptionEs();
                                String descriptionTh = mainResponseList.get(i).getDescriptionTh();
                                String descriptionLo = mainResponseList.get(i).getDescriptionLo();
                                String descriptionIdn = mainResponseList.get(i).getDescriptionIdn();
                                String descriptionTr = mainResponseList.get(i).getDescriptionTr();
                                String descriptionFr = mainResponseList.get(i).getDescriptionFr();

                                List<ComplianceIndicatorItem> complianceIndicatorItemList = new ArrayList<>();
                                complianceIndicatorItemList.clear();
                                complianceIndicatorItemList = mainResponseList.get(i).getComplianceIndicator();
                                List<CurrencyItem> currencyItemList = new ArrayList<>();
                                currencyItemList.clear();
                                currencyItemList = mainResponseList.get(i).getCurrency();
                                List<QuestionsItem> questionsItemList = new ArrayList<>();
                                questionsItemList.clear();
                                questionsItemList = mainResponseList.get(i).getQuestions();

                                List<SectionsItem> sectionsItemList = mainResponseList.get(i).getSections();
                                Recurrence recurrences = mainResponseList.get(i).getRecurrence();

                                DmMainModule user = new DmMainModule();
                                user.id = id;
                                user.name = name;
                                user.nameFr = nameFr;
                                user.nameIdn = nameIdn;
                                user.namePt = namePt;
                                user.nameLo = nameLo;
                                user.nameTr = nameTr;
                                user.nameTh = nameTh;
                                user.nameVi = nameVi;
                                user.nameEs = nameEs;
                                user.descriptionPt = descriptionPt;
                                user.description = description;
                                user.descriptionVi = descriptionVi;
                                user.descriptionEs = descriptionEs;
                                user.descriptionTh = descriptionTh;
                                user.descriptionLo = descriptionLo;
                                user.descriptionIdn = descriptionIdn;
                                user.descriptionTr = descriptionTr;
                                user.descriptionFr = descriptionFr;

                                user.surveyType = surveyType;
                                user.followUpDuration = followUpDuration;
                                user.assessmentType = assessmentType;
                                user.followUpAttempts = followUpAttempts;

                                db.dmMainModuleDao().insert(user);

                                for (int j = 0; j < complianceIndicatorItemList.size(); j++) {


                                    int moduleDraftId = complianceIndicatorItemList.get(j).getModuleDraftId();
                                    int id_Compliance = complianceIndicatorItemList.get(j).getId();
                                    String compliance = complianceIndicatorItemList.get(j).getCompliance();
                                    String label = complianceIndicatorItemList.get(j).getLabel();
                                    String labelTh = complianceIndicatorItemList.get(j).getLabelTh();
                                    String labelPt = complianceIndicatorItemList.get(j).getLabelPt();
                                    String labelIdn = complianceIndicatorItemList.get(j).getLabelIdn();
                                    String labelEs = complianceIndicatorItemList.get(j).getLabelEs();
                                    String labelTr = complianceIndicatorItemList.get(j).getLabelTr();
                                    String labelFr = complianceIndicatorItemList.get(j).getLabelFr();
                                    String labelLo = complianceIndicatorItemList.get(j).getLabelLo();
                                    String labelVi = complianceIndicatorItemList.get(j).getLabelVi();


                                    ComplianceIndicator complianceIndicator = new ComplianceIndicator();
                                    complianceIndicator.moduleDraftId = moduleDraftId;
                                    complianceIndicator.id = id_Compliance;
                                    complianceIndicator.compliance = compliance;
                                    complianceIndicator.label = label;
                                    complianceIndicator.labelTh = labelTh;
                                    complianceIndicator.labelPt = labelPt;
                                    complianceIndicator.labelIdn = labelIdn;
                                    complianceIndicator.labelEs = labelEs;
                                    complianceIndicator.labelTr = labelTr;
                                    complianceIndicator.labelFr = labelFr;
                                    complianceIndicator.labelLo = labelLo;
                                    complianceIndicator.labelVi = labelVi;

                                    db.complianceIndicatorDao().insert(complianceIndicator);
                                }


                                for (int k = 0; k < currencyItemList.size(); k++) {

                                    int moduleDraftId = currencyItemList.get(k).getModuleDraftId();
                                    int currencyDraftId = currencyItemList.get(k).getCurrencyDraftId();
                                    int currencyId = currencyItemList.get(k).getCurrencyId();
                                    boolean isDefault = currencyItemList.get(k).isIsDefault();

                                    DmCurrency dmCurrency = new DmCurrency();
                                    dmCurrency.moduleDraftId = moduleDraftId;
                                    dmCurrency.currencyDraftId = currencyDraftId;
                                    dmCurrency.currencyId = currencyId;
                                    dmCurrency.isDefault = isDefault;

                                    db.dmCurrencyDao().insert(dmCurrency);

                                }

                                for (int l = 0; l < questionsItemList.size(); l++) {

                                    boolean mandate = questionsItemList.get(l).isMandate();
                                    String question = questionsItemList.get(l).getQuestion();
                                    String questionTr = questionsItemList.get(l).getQuestionTr();
                                    String questionPt = questionsItemList.get(l).getQuestionPt();
                                    String questionLo = questionsItemList.get(l).getQuestionLo();
                                    String questionFr = questionsItemList.get(l).getQuestionFr();
                                    String questionIdn = questionsItemList.get(l).getQuestionIdn();
                                    String questionEs = questionsItemList.get(l).getQuestionEs();
                                    String questionTh = questionsItemList.get(l).getQuestionTh();
                                    String questionVi = questionsItemList.get(l).getQuestionVi();
                                    int displaytypeId = questionsItemList.get(l).getDisplaytypeId();
                                    int datatypeId = questionsItemList.get(l).getDatatypeId();
                                    boolean isCriticalQuest = questionsItemList.get(l).isCriticalQuest();
                                    String displayType = questionsItemList.get(l).getDisplayType();
                                    int id_questionModule = questionsItemList.get(l).getId();
                                    int position = questionsItemList.get(l).getPosition();
                                    int moduleId = questionsItemList.get(l).getModuleId();
                                    int submoduleId = questionsItemList.get(l).getSubmoduleId();

                                    DmQuestionsModule dmQuestionsModule = new DmQuestionsModule();
                                    dmQuestionsModule.mandate = mandate;
                                    dmQuestionsModule.question = question;
                                    dmQuestionsModule.questionTr = questionTr;
                                    dmQuestionsModule.questionPt = questionPt;
                                    dmQuestionsModule.questionLo = questionLo;
                                    dmQuestionsModule.questionFr = questionFr;
                                    dmQuestionsModule.questionIdn = questionIdn;
                                    dmQuestionsModule.questionEs = questionEs;
                                    dmQuestionsModule.questionTh = questionTh;
                                    dmQuestionsModule.questionVi = questionVi;
                                    dmQuestionsModule.displaytypeId = displaytypeId;
                                    dmQuestionsModule.datatypeId = datatypeId;
                                    dmQuestionsModule.isCriticalQuest = isCriticalQuest;
                                    dmQuestionsModule.displayType = displayType;
                                    dmQuestionsModule.id = id_questionModule;
                                    dmQuestionsModule.position = position;
                                    dmQuestionsModule.moduleId = moduleId;
                                    dmQuestionsModule.submoduleId = submoduleId;
                                    db.dmQuestionsModuleDao().insert(dmQuestionsModule);


                                    List<LookupValuesItem> lookupValuesItemList = new ArrayList<>();
                                    lookupValuesItemList.clear();
                                    lookupValuesItemList = questionsItemList.get(l).getLookupValues();

                                    TextBoxModuleQuestions textBoxModuleQuestions = questionsItemList.get(l).getTextBoxModuleQuestions();


                                    if (lookupValuesItemList != null) {
                                        for (int m = 0; m < lookupValuesItemList.size(); m++) {

                                            String valueId = lookupValuesItemList.get(m).getValueId();
                                            String valueTr = lookupValuesItemList.get(m).getValueTr();
                                            String valueVi = lookupValuesItemList.get(m).getValueVi();
                                            String valueTh = lookupValuesItemList.get(m).getValueTh();
                                            String valueEs = lookupValuesItemList.get(m).getValueEs();
                                            String valueFr = lookupValuesItemList.get(m).getValueFr();
                                            String valueLo = lookupValuesItemList.get(m).getValueLo();
                                            String valuePt = lookupValuesItemList.get(m).getValuePt();
                                            String value = lookupValuesItemList.get(m).getValue();
                                            int position_lookupValues = lookupValuesItemList.get(m).getPosition();
                                            int id_lookupValues = lookupValuesItemList.get(m).getId();
                                            boolean allowCustomActionPlan = lookupValuesItemList.get(m).isAllowCustomActionPlan();
                                            String compliance = lookupValuesItemList.get(m).getCompliance();
                                            int complianceId = lookupValuesItemList.get(m).getComplianceId();
                                            boolean isCaptureProof = lookupValuesItemList.get(m).isCaptureProof();
                                            String proofFormat = lookupValuesItemList.get(m).getProofFormat();

                                            DmLookupValuesItem dmLookupValuesItem = new DmLookupValuesItem();
                                            dmLookupValuesItem.valueId = valueId;
                                            dmLookupValuesItem.valueTr = valueTr;
                                            dmLookupValuesItem.valueVi = valueVi;
                                            dmLookupValuesItem.valueTh = valueTh;
                                            dmLookupValuesItem.valueEs = valueEs;
                                            dmLookupValuesItem.valueFr = valueFr;
                                            dmLookupValuesItem.valueLo = valueLo;
                                            dmLookupValuesItem.valuePt = valuePt;
                                            dmLookupValuesItem.value = value;
                                            dmLookupValuesItem.position = position_lookupValues;
                                            dmLookupValuesItem.id = id_lookupValues;
                                            dmLookupValuesItem.idQuestionsMudule = id_questionModule;
                                            dmLookupValuesItem.allowCustomActionPlan = allowCustomActionPlan;
                                            dmLookupValuesItem.compliance = compliance;
                                            dmLookupValuesItem.complianceId = complianceId;
                                            dmLookupValuesItem.isCaptureProof = isCaptureProof;
                                            dmLookupValuesItem.proofFormat = proofFormat;

                                            db.dmLookupValuesItemDao().insert(dmLookupValuesItem);


                                            List<ActionPlansItem> actionPlansItemList = new ArrayList<>();
                                            actionPlansItemList.clear();
                                            actionPlansItemList = lookupValuesItemList.get(m).getActionPlans();

                                            if (actionPlansItemList != null) {
                                                for (int n = 0; n < actionPlansItemList.size(); n++) {
                                                    int id_actionPlan = actionPlansItemList.get(n).getId();
                                                    boolean isDefault = actionPlansItemList.get(n).isIsDefault();
                                                    String actionPlan = actionPlansItemList.get(n).getActionPlan();

                                                    DmActionPlansItem dmActionPlansItem = new DmActionPlansItem();
                                                    dmActionPlansItem.id = id_actionPlan;
                                                    dmActionPlansItem.id_lookupValue = id_lookupValues;
                                                    dmActionPlansItem.isDefault = isDefault;
                                                    dmActionPlansItem.actionPlan = actionPlan;

                                                    db.dmActionPlansItemDao().insert(dmActionPlansItem);
                                                }
                                            }


                                        }
                                    }

                                    if (textBoxModuleQuestions != null) {
                                        int answerType = textBoxModuleQuestions.getAnswerType();
                                        boolean isAllowNegative = textBoxModuleQuestions.isAllowNegative();
                                        boolean isAllowDecimal = textBoxModuleQuestions.isAllowDecimal();
                                        double minimumRange = textBoxModuleQuestions.getMinimumRange();
                                        double maximumRange = textBoxModuleQuestions.getMaximumRange();

                                        DmTextBoxModuleQuestions dmTextBoxModuleQuestions = new DmTextBoxModuleQuestions();
                                        dmTextBoxModuleQuestions.answerType = answerType;
                                        dmTextBoxModuleQuestions.isAllowNegative = isAllowNegative;
                                        dmTextBoxModuleQuestions.isAllowDecimal = isAllowDecimal;
                                        dmTextBoxModuleQuestions.minimumRange = minimumRange;
                                        dmTextBoxModuleQuestions.maximumRange = maximumRange;
                                        dmTextBoxModuleQuestions.questionId = id_questionModule;
                                        db.dmTextBoxModuleQuestionsDao().insert(dmTextBoxModuleQuestions);

                                        List<DmRangeTextBoxModuleQuestions> dmRangeTextBoxModuleQuestionsList = new ArrayList<>();
                                        dmRangeTextBoxModuleQuestionsList.clear();
                                        dmRangeTextBoxModuleQuestionsList = textBoxModuleQuestions.getDmRangeQuestionsList();


                                        if (dmRangeTextBoxModuleQuestionsList != null) {
                                            for (int r = 0; r < dmRangeTextBoxModuleQuestionsList.size(); r++) {
                                                double minimumRange_range = dmRangeTextBoxModuleQuestionsList.get(r).getMinimumRange();
                                                double maximumRange_range = dmRangeTextBoxModuleQuestionsList.get(r).getMaximumRange();
                                                String compliance = dmRangeTextBoxModuleQuestionsList.get(r).getCompliance();
                                                int complianceId = dmRangeTextBoxModuleQuestionsList.get(r).getComplianceId();
                                                boolean allowCustomActionPlan = dmRangeTextBoxModuleQuestionsList.get(r).isAllowCustomActionPlan();
                                                boolean isCaptureProof = dmRangeTextBoxModuleQuestionsList.get(r).isCaptureProof();
                                                String proofFormat = dmRangeTextBoxModuleQuestionsList.get(r).getProofFormat();

                                                DmRangeTextBoxModuleQuestionsTable dmRangeTextBoxModuleQuestionsTable = new DmRangeTextBoxModuleQuestionsTable();
                                                dmRangeTextBoxModuleQuestionsTable.minimumRange = minimumRange_range;
                                                dmRangeTextBoxModuleQuestionsTable.maximumRange = maximumRange_range;
                                                dmRangeTextBoxModuleQuestionsTable.compliance = compliance;
                                                dmRangeTextBoxModuleQuestionsTable.complianceId = complianceId;
                                                dmRangeTextBoxModuleQuestionsTable.allowCustomActionPlan = allowCustomActionPlan;
                                                dmRangeTextBoxModuleQuestionsTable.isCaptureProof = isCaptureProof;
                                                dmRangeTextBoxModuleQuestionsTable.proofFormat = proofFormat;
                                                dmRangeTextBoxModuleQuestionsTable.questionId = id_questionModule;
                                                db.dmRangeTextBoxModuleQuestionsTableDao().insert(dmRangeTextBoxModuleQuestionsTable);



                                            }

                                        }


                                    }


                                }

                                for (int s = 0; s < sectionsItemList.size(); s++) {
                                    String nameTh_Sec = sectionsItemList.get(s).getNameTh();
                                    String nameVi_Sec = sectionsItemList.get(s).getNameVi();
                                    String namePt_Sec = sectionsItemList.get(s).getNamePt();
                                    String nameLo_Sec = sectionsItemList.get(s).getNameLo();
                                    String nameEs_Sec = sectionsItemList.get(s).getNameEs();
                                    String nameFr_Sec = sectionsItemList.get(s).getNameFr();
                                    String nameTr_Sec = sectionsItemList.get(s).getNameTr();
                                    String name_Sec = sectionsItemList.get(s).getName();
                                    String nameId_Sec = sectionsItemList.get(s).getNameId();
                                    String description_Sec = sectionsItemList.get(s).getDescription();
                                    String descriptionPt_Sec = sectionsItemList.get(s).getDescriptionPt();
                                    String descriptionVi_Sec = sectionsItemList.get(s).getDescriptionVi();
                                    String descriptionFr_Sec = sectionsItemList.get(s).getDescriptionFr();
                                    String descriptionEs_Sec = sectionsItemList.get(s).getDescriptionEs();
                                    String descriptionTh_Sec = sectionsItemList.get(s).getDescriptionTh();
                                    String descriptionLo_Sec = sectionsItemList.get(s).getDescriptionLo();
                                    String descriptionIdn_Sec = sectionsItemList.get(s).getDescriptionIdn();
                                    String descriptionTr_Sec = sectionsItemList.get(s).getDescriptionTr();
                                    boolean isDefault_Sec = sectionsItemList.get(s).isDefault();
                                    boolean showDescription_Sec = sectionsItemList.get(s).isShowDescription();
                                    int id_Sec = sectionsItemList.get(s).getId();
                                    int position_Sec = sectionsItemList.get(s).getPosition();
                                    int moduleId_Sec = sectionsItemList.get(s).getModuleId();

                                    DmSectionsItem dmSectionsItem = new DmSectionsItem();
                                    dmSectionsItem.nameTh = nameTh_Sec;
                                    dmSectionsItem.nameVi = nameVi_Sec;
                                    dmSectionsItem.namePt = namePt_Sec;
                                    dmSectionsItem.nameLo = nameLo_Sec;
                                    dmSectionsItem.nameEs = nameEs_Sec;
                                    dmSectionsItem.nameFr = nameFr_Sec;
                                    dmSectionsItem.nameTr = nameTr_Sec;
                                    dmSectionsItem.name = name_Sec;
                                    dmSectionsItem.nameId = nameId_Sec;
                                    dmSectionsItem.description = description_Sec;
                                    dmSectionsItem.descriptionPt = descriptionPt_Sec;
                                    dmSectionsItem.descriptionVi = descriptionVi_Sec;
                                    dmSectionsItem.descriptionFr = descriptionFr_Sec;
                                    dmSectionsItem.descriptionEs = descriptionEs_Sec;
                                    dmSectionsItem.descriptionTh = descriptionTh_Sec;
                                    dmSectionsItem.descriptionLo = descriptionLo_Sec;
                                    dmSectionsItem.descriptionIdn = descriptionIdn_Sec;
                                    dmSectionsItem.descriptionTr = descriptionTr_Sec;
                                    dmSectionsItem.isDefault = isDefault_Sec;
                                    dmSectionsItem.showDescription = showDescription_Sec;
                                    dmSectionsItem.id = id_Sec;
                                    dmSectionsItem.position = position_Sec;
                                    dmSectionsItem.moduleId = moduleId_Sec;
                                    db.dmSectionsItemDao().insert(dmSectionsItem);

                                    List<QuestionsSectionItemSubmodule> questionsSectionItemSubmoduleList = new ArrayList<>();
                                    questionsSectionItemSubmoduleList.clear();
                                    questionsSectionItemSubmoduleList = sectionsItemList.get(s).getQuestions();

                                    Log.d("secquestiionnsss", ".." + questionsSectionItemSubmoduleList.size());

                                    for (int t = 0; t < questionsSectionItemSubmoduleList.size(); t++) {

                                        boolean mandate = questionsSectionItemSubmoduleList.get(t).isMandate();
                                        String textBox = questionsSectionItemSubmoduleList.get(t).getTextBox();
                                        String question = questionsSectionItemSubmoduleList.get(t).getQuestion();
                                        String questionTr = questionsSectionItemSubmoduleList.get(t).getQuestionTr();
                                        String questionPt = questionsSectionItemSubmoduleList.get(t).getQuestionPt();
                                        String questionLo = questionsSectionItemSubmoduleList.get(t).getQuestionLo();
                                        String questionFr = questionsSectionItemSubmoduleList.get(t).getQuestionFr();
                                        String questionIdn = questionsSectionItemSubmoduleList.get(t).getQuestionIdn();
                                        String questionEs = questionsSectionItemSubmoduleList.get(t).getQuestionEs();
                                        String questionTh = questionsSectionItemSubmoduleList.get(t).getQuestionTh();
                                        String questionVi = questionsSectionItemSubmoduleList.get(t).getQuestionVi();
                                        int displaytypeId = questionsSectionItemSubmoduleList.get(t).getDisplaytypeId();
                                        int datatypeId = questionsSectionItemSubmoduleList.get(t).getDatatypeId();
                                        boolean isCriticalQuest = questionsSectionItemSubmoduleList.get(t).isCriticalQuest();
                                        String displayType = questionsSectionItemSubmoduleList.get(t).getDisplayType();
                                        int id_questionSubModule = questionsSectionItemSubmoduleList.get(t).getId();
                                        int position = questionsSectionItemSubmoduleList.get(t).getPosition();
                                        int moduleId = questionsSectionItemSubmoduleList.get(t).getModuleId();
                                        int submoduleId = questionsSectionItemSubmoduleList.get(t).getSubmoduleId();

                                        DmQuestionsSubModule dmQuestionsSubModule = new DmQuestionsSubModule();
                                        dmQuestionsSubModule.mandate = mandate;
                                        dmQuestionsSubModule.textBox = textBox;
                                        dmQuestionsSubModule.question = question;
                                        dmQuestionsSubModule.questionTr = questionTr;
                                        dmQuestionsSubModule.questionPt = questionPt;
                                        dmQuestionsSubModule.questionLo = questionLo;
                                        dmQuestionsSubModule.questionFr = questionFr;
                                        dmQuestionsSubModule.sectionId = id_Sec;
                                        dmQuestionsSubModule.questionIdn = questionIdn;
                                        dmQuestionsSubModule.questionEs = questionEs;
                                        dmQuestionsSubModule.questionTh = questionTh;
                                        dmQuestionsSubModule.questionVi = questionVi;
                                        dmQuestionsSubModule.displaytypeId = displaytypeId;
                                        dmQuestionsSubModule.datatypeId = datatypeId;
                                        dmQuestionsSubModule.isCriticalQuest = isCriticalQuest;
                                        dmQuestionsSubModule.displayType = displayType;
                                        dmQuestionsSubModule.id = id_questionSubModule;
                                        dmQuestionsSubModule.position = position;
                                        dmQuestionsSubModule.moduleId = moduleId;
                                        dmQuestionsSubModule.submoduleId = submoduleId;
                                        db.dmQuestionsSubModuleDao().insert(dmQuestionsSubModule);

                                        List<SectionLookupValuesItemSubmodule> sectionLookupValuesItemSubmoduleList = new ArrayList<>();
                                        sectionLookupValuesItemSubmoduleList.clear();
                                        sectionLookupValuesItemSubmoduleList = questionsSectionItemSubmoduleList.get(t).getLookupValues();

                                        if (sectionLookupValuesItemSubmoduleList != null) {
                                            for (int u = 0; u < sectionLookupValuesItemSubmoduleList.size(); u++) {

                                                String valueId = sectionLookupValuesItemSubmoduleList.get(u).getValueId();
                                                String valueTr = sectionLookupValuesItemSubmoduleList.get(u).getValueTr();
                                                String valueVi = sectionLookupValuesItemSubmoduleList.get(u).getValueVi();
                                                String valueTh = sectionLookupValuesItemSubmoduleList.get(u).getValueTh();
                                                String valueEs = sectionLookupValuesItemSubmoduleList.get(u).getValueEs();
                                                String valueFr = sectionLookupValuesItemSubmoduleList.get(u).getValueFr();
                                                String valueLo = sectionLookupValuesItemSubmoduleList.get(u).getValueLo();
                                                String valuePt = sectionLookupValuesItemSubmoduleList.get(u).getValuePt();
                                                String value = sectionLookupValuesItemSubmoduleList.get(u).getValue();
                                                int position_lookupValues = sectionLookupValuesItemSubmoduleList.get(u).getPosition();
                                                int id_lookupValues = sectionLookupValuesItemSubmoduleList.get(u).getId();
                                                boolean allowCustomActionPlan = sectionLookupValuesItemSubmoduleList.get(u).isAllowCustomActionPlan();
                                                String compliance = sectionLookupValuesItemSubmoduleList.get(u).getCompliance();
                                                int complianceId = sectionLookupValuesItemSubmoduleList.get(u).getComplianceId();
                                                boolean isCaptureProof = sectionLookupValuesItemSubmoduleList.get(u).isCaptureProof();
                                                String proofFormat = sectionLookupValuesItemSubmoduleList.get(u).getProofFormat();

                                                DmLookupValuesISubmodule dmLookupValuesISubmodule = new DmLookupValuesISubmodule();
                                                dmLookupValuesISubmodule.valueId = valueId;
                                                dmLookupValuesISubmodule.valueTr = valueTr;
                                                dmLookupValuesISubmodule.valueVi = valueVi;
                                                dmLookupValuesISubmodule.valueTh = valueTh;
                                                dmLookupValuesISubmodule.valueEs = valueEs;
                                                dmLookupValuesISubmodule.valueFr = valueFr;
                                                dmLookupValuesISubmodule.valueLo = valueLo;
                                                dmLookupValuesISubmodule.valuePt = valuePt;
                                                dmLookupValuesISubmodule.value = value;
                                                dmLookupValuesISubmodule.position = position_lookupValues;
                                                dmLookupValuesISubmodule.id = id_lookupValues;
                                                dmLookupValuesISubmodule.idQuestionSubModule = id_questionSubModule;
                                                dmLookupValuesISubmodule.allowCustomActionPlan = allowCustomActionPlan;
                                                dmLookupValuesISubmodule.compliance = compliance;
                                                dmLookupValuesISubmodule.complianceId = complianceId;
                                                dmLookupValuesISubmodule.isCaptureProof = isCaptureProof;
                                                dmLookupValuesISubmodule.proofFormat = proofFormat;

                                                db.dmLookupValuesISubmoduleDao().insert(dmLookupValuesISubmodule);


                                                List<SectionActionPlansItemSubmodule> sectionActionPlansItemSubmoduleList = new ArrayList<>();
                                                sectionActionPlansItemSubmoduleList.clear();
                                                sectionActionPlansItemSubmoduleList = sectionLookupValuesItemSubmoduleList.get(u).getActionPlans();

                                                if (sectionActionPlansItemSubmoduleList != null) {
                                                    for (int v = 0; v < sectionActionPlansItemSubmoduleList.size(); v++) {
                                                        int id_actionPlan = sectionActionPlansItemSubmoduleList.get(v).getId();
                                                        boolean isDefault = sectionActionPlansItemSubmoduleList.get(v).isIsDefault();
                                                        String actionPlan = sectionActionPlansItemSubmoduleList.get(v).getActionPlan();

                                                        DmActionPlansItemSubmodule dmActionPlansItemSubmodule = new DmActionPlansItemSubmodule();
                                                        dmActionPlansItemSubmodule.id = id_actionPlan;
                                                        dmActionPlansItemSubmodule.isDefault = isDefault;
                                                        dmActionPlansItemSubmodule.actionPlan = actionPlan;

                                                        db.dmActionPlansItemSubmoduleDao().insert(dmActionPlansItemSubmodule);
                                                    }
                                                }

                                            }
                                        }
                                    }

                                }

                                if (recurrences != null) {
                                    int recurrenceFrequency = recurrences.getRecurrenceFrequency();
                                    boolean isCustomRecurrence = recurrences.isIsCustomRecurrence();
                                    String recurrenceType = recurrences.getRecurrenceType();

                                    DmRecurrence dmRecurrence = new DmRecurrence();
                                    dmRecurrence.recurrenceFrequency = recurrenceFrequency;
                                    dmRecurrence.isCustomRecurrence = isCustomRecurrence;
                                    dmRecurrence.recurrenceType = recurrenceType;
                                    db.dmRecurrenceDao().insert(dmRecurrence);
                                }


                            }
                        }

                        if (response.body().getAnswerTypes() != null) {
                            ArrayList<AnswerTypesItem> answerTypesItemArrayList = response.body().getAnswerTypes();

                            for (int p = 0; p < answerTypesItemArrayList.size(); p++) {

                                int id = answerTypesItemArrayList.get(p).getId();
                                String value = answerTypesItemArrayList.get(p).getValue();

                                DmAnswerTypes dmAnswerTypes = new DmAnswerTypes();
                                dmAnswerTypes.id = id;
                                dmAnswerTypes.value = value;
                                db.dmAnswerTypesDao().insert(dmAnswerTypes);

                            }

                        }

                        if (response.body().getQuestionTypes() != null) {
                            ArrayList<QuestionTypesItem> questionTypesItemArrayList = response.body().getQuestionTypes();

                            for (int q = 0; q < questionTypesItemArrayList.size(); q++) {

                                int id = questionTypesItemArrayList.get(q).getId();
                                String value = questionTypesItemArrayList.get(q).getValue();

                                DmQuestionTypes dmQuestionTypes = new DmQuestionTypes();
                                dmQuestionTypes.id = id;
                                dmQuestionTypes.value = value;
                                db.dmQuestionTypesDao().insert(dmQuestionTypes);


                            }

                        }

                        db.dmCurrenciesItemDao().deleteAll();

                        if (response.body().getCurrencies() != null) {
                            ArrayList<CurrenciesItem> currenciesItemArrayList = response.body().getCurrencies();

                            for (int r = 0; r < currenciesItemArrayList.size(); r++) {

                                int currencyId = currenciesItemArrayList.get(r).getCurrencyId();
                                String notation = currenciesItemArrayList.get(r).getNotation();
                                String code = currenciesItemArrayList.get(r).getCode();
                                String name = currenciesItemArrayList.get(r).getName();

                                DmCurrenciesItem dmCurrenciesItem = new DmCurrenciesItem();
                                dmCurrenciesItem.currencyId = currencyId;
                                dmCurrenciesItem.code = code;
                                dmCurrenciesItem.notation = notation;
                                dmCurrenciesItem.name = name;
                                db.dmCurrenciesItemDao().insert(dmCurrenciesItem);

                            }


                        }


//                       Log.d("DbData-", db.dmMainModuleDao().getDMmodule().get(0).name);
//                       Log.d("DbDataCompliance-", db.complianceIndicatorDao().getComplianceIndicator().get(0).compliance);
//                       Log.d("DbDataCurrency-", String.valueOf(db.dmCurrencyDao().getDMCurrency().get(0).currencyId));
//                       Log.d("DbAnswerTypes-", db.dmAnswerTypesDao().getDmAnswerTypes().get(0).value);
//                       Log.d("DbQuestionsType-", db.dmQuestionTypesDao().getDmQuestionTypes().get(0).value);
//                       Log.d("DbCurrenciesItem-", db.dmCurrenciesItemDao().getDmCurrenciesItem().get(0).notation);
//                       Log.d("DbModuleQuestionsItem-", db.dmQuestionsModuleDao().getDmQuestionsModule().get(0).question);
//                       Log.d("DbModuleLookupValues-", db.dmLookupValuesItemDao().getDmLookupValuesItem().get(0).value);
//                       Log.d("DbModuleActionPlan-", db.dmActionPlansItemDao().getDmActionPlansItem().get(1).actionPlan);
//                       Log.d("DbSectionValues-", db.dmSectionsItemDao().getDmSectionsItem().get(0).description);
//                       Log.d("DbSubModuleQuestions-", db.dmQuestionsSubModuleDao().getDmQuestionsSubModule().get(0).displayType);
//                       Log.d("DbSubModuleLookup-", db.dmLookupValuesISubmoduleDao().getDmLookupValuesISubmodule().get(0).proofFormat);
//                       Log.d("DbSubModuleActionPlan-", db.dmActionPlansItemSubmoduleDao().getDmActionPlansItemSubmodule().get(0).actionPlan);
//                       Log.d("DbRecurranceData-", db.dmRecurrenceDao().getDmRecurrence().recurrenceType);

                    } else if (response.code() == 400) {

                        Log.d("BadRequest-", response.message());
                    } else if (response.code() == 500) {

                    } else {
                        // showAlert("Server Issue!!"+response.message().toString());
                    }
                } catch (Exception e) {
                    Log.d("hdjhashdksa", e.toString());
                    throw new RuntimeException(e);

                }

            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {

            }
        });

    }

}
