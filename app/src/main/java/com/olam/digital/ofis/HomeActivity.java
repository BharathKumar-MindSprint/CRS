package com.olam.digital.ofis;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.Common.Constants.StringConstants;
import com.olam.digital.ofis.ManageTrainingRecords.Records;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSurveyDto;
import com.olam.digital.ofis.agrisupplier.ui.FindSupplierAndSurveyActivity;
import com.olam.digital.ofis.agrisupplier.ui.SupplierSurveyActivity;
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
import com.olam.digital.ofis.farmplandownload.DownloadFarmPlanActivity;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenReqBody;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenResp;
import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;
import com.olam.digital.ofis.farmstakeholder.ViewStakeHolderActivity;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;

import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.recordpolygon.RecordPolygonActivity;
import com.olam.digital.ofis.recordpolygon.SharedPreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.olam.digital.ofis.Common.Utils.NetworkUtils.isNetworkAvailable;


public class HomeActivity extends BaseListActivity implements Callback<AuthenticationDTO>, OnAccessTokenInterface, AdapterView.OnItemClickListener {

    // update
    public static final String TAG = "HomeActivity";
    private Context context;
    private static Context staticContext;
    private static Resources staticRes;
    private Resources res;
    private HomeActivity me;
    private static DataLayer dl;

    private  static OFISRoomDB db;
    private NetworkLayer nl;
    private ProgressDialog m_ProgressDialog = null;
    private ProgressDialog mDialogLoading = null;
    int checkvalue = 0;
    private boolean farmerSynced = true, autotimeEnabled = false, mandateSurveySynced = true, isConsentSynced = true, stakeholderSynced = true;
    private Cursor networkQueue;
    private CustomListAdapter_NetworkQueue customListAdapter_networkQueue;
    private static LinearLayout topSection;
    private static RelativeLayout mainSection;
    private static ScrollView firstRunSection;
    private static TextView queueCount;
    private SharedPreferences sharedPreferences;
    private static MenuItem settingsButton, tasksButton, notificationButton;
    private Handler mainHandler;

    private final static String EDIT_FARMER_ID = "EDIT_FARMER_ID";
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
    private EditText passcode;
    private Button saveUpdateButton;
    private static final int REQUEST_PERMISSIONS = 2507;
    private static final int PERMISSIONS_ACCESS_LOCATION = 6575;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private boolean syncOptionAll;
    private int itemId;
    public static int totalItems;
    public static int progressItem;
    private long lastClickTime = 0;
    private boolean isChildSynced = true;
    ListView listView;
    private Boolean existingUser = false;
    private Location mCurrentLocation = null;
    GPSListener gpsListener = null;
    private GPSTracker gpsTracker = null;
    private Boolean newGps;
    ImageButton syncAll;
    static CardView downloadSurveyWrapper;
    private static CardView cardview_downloadFarmplan;
    static String counrtyCodeNew;

    static String password = "Olam123+";
    static String system = "od";
    private static ProgressDialog progressDialog;
    static String odAccessToken;

    static boolean isdownloadfarmplan;
    private static CardView cvSupplierAndSurvey;
    private static Button supplierAndSurvey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        listView = findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        staticContext = this;
        context = me = this;
        staticRes = getResources();
        res = getResources();
        db = OFISRoomDB.getDbInstance(context);

        dl = new DataLayer(res, context);
        nl = new NetworkLayer(context, res);
        mainHandler = new Handler(Looper.getMainLooper());
        sharedPreferences = getSharedPreferences("com.olam.digital.ofis.app_prefs", MODE_PRIVATE);

        LinearLayout itemsHeader = (LinearLayout) ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.home_header_buttons, listView, false);
        listView.addHeaderView(itemsHeader);

        firstRunSection = findViewById(R.id.firstRunWrapper);
        mainSection = findViewById(R.id.mainWrapper);
        passcode = findViewById(R.id.passcode);
        saveUpdateButton = findViewById(R.id.buttonSaveUpdate);
        topSection = findViewById(R.id.topSectionWrapper);
        downloadSurveyWrapper = findViewById(R.id.downloadSurveyWrapper);


        enumeratorID = findViewById(R.id.enumeratorID);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.PleaseWait));
        progressDialog.setMessage(getString(R.string.Loading));
        progressDialog.setCancelable(false);

        downloadSurveyWrap();
        String enumeratorId = Utilities.getFromSharedPrefs(context);

        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        bindNetworkQueue(true);
        // Removed the check migrate existing enumerator while update Manna version
        String environment = BuildConfig.FLAVOR;
        setKeycloakCredentials(environment);
        setOperatingUnit(environment);
        setCommodity();
        if (sharedPreferences.getBoolean("FIRST_RUN", true)) {
            firstRunSection.setVisibility(View.VISIBLE);
            if (!enumeratorId.equalsIgnoreCase("")) {
                existingUser = true;
                enumeratorID.setText(enumeratorId);
                enumeratorID.setFocusable(false);
                passcode.setFocusable(true);
                enumeratorID.setFocusableInTouchMode(false);
                enumeratorID.setClickable(false);
            }
        }
        else {
            int onGroundSurveyNotificationCount = db.onGroundSurveyNotificationDao().getPendingEUDRSurveyCount("");
            if (onGroundSurveyNotificationCount > 0 && shouldShowPopup()) {
                showDailyRemainderPopUp(onGroundSurveyNotificationCount);
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
        String[] PERMISSIONS = new String[]{
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (!Utilities.hasPermissions(PERMISSIONS, context)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS);
        }

        cardview_downloadFarmplan = findViewById(R.id.cardview_downloadFarmplan);
        boolean isCropAvailable = Utilities.getBoolFromSharedPrefs(staticContext, "DISPLAY_CROPPLAN", false);
        if (isCropAvailable) {
            cardview_downloadFarmplan.setVisibility(View.VISIBLE);
        } else {
            cardview_downloadFarmplan.setVisibility(View.GONE);
        }

        Button downloadFarmplan = findViewById(R.id.DownloadFarmPlan);
        downloadFarmplan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                } else {
                    doingAuthRequest();
                }
            }
        });

        gpsListener = new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location != null && !GPSUtils.isMockLocation(location)) {
                    mCurrentLocation = location;
                    TrackDetailsDTO trackDetailsDTO = new TrackDetailsDTO();
                    trackDetailsDTO.setModule(GPSConstants.MODULE_CAPTURE_CAPTURE_MY_LOCATION);
                    trackDetailsDTO.setLatitude(String.valueOf(mCurrentLocation.getLatitude()));
                    trackDetailsDTO.setLongitude(String.valueOf(mCurrentLocation.getLongitude()));
                    trackDetailsDTO.setAccuracy(String.valueOf(mCurrentLocation.getAccuracy()));
                    trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
                    ContentValues geoContentValues = new GPSUtils().getGeoContentValues(trackDetailsDTO);
                    if (newGps) {
                        long newGpsQueueId = new GpsTrackerLogic().insertGPSDetailsToQueue(context, geoContentValues);
                        if (newGpsQueueId != -1) {
                            enableSync();
                        }
                        newGps = false;
                    }
                }
            }
        };
        gpsTracker = new GPSTracker(this, gpsListener, true);

    }

    private void setKeycloakCredentials(String environment) {
        String clientId = null;
        String clientSecret = switch (environment) {
            case "devofi" -> {
                clientId = KeyCloakInfo.dev_ofi_client_Id;
                yield KeyCloakInfo.dev_ofi_Client_Secret;
            }
            case "sitofi" -> {
                clientId = KeyCloakInfo.sit_ofi_client_Id;
                yield KeyCloakInfo.sit_ofi_Client_Secret;
            }
            case "uatofi" -> {
                clientId = KeyCloakInfo.uat_ofi_client_Id;
                yield KeyCloakInfo.uat_ofi_Client_Secret;
            }
            case "preprodofi" -> {
                clientId = KeyCloakInfo.preprod_ofi_client_Id;
                yield KeyCloakInfo.preprod_ofi_Client_Secret;
            }
            case "prodofi" -> {
                clientId = KeyCloakInfo.prod_ofi_client_Id;
                yield KeyCloakInfo.prod_ofi_Client_Secret;
            }

            // OGA
            case "devoga" -> {
                clientId = KeyCloakInfo.dev_oga_client_Id;
                yield KeyCloakInfo.dev_oga_Client_Secret;
            }
            case "sitoga" -> {
                clientId = KeyCloakInfo.sit_oga_client_Id;
                yield KeyCloakInfo.sit_oga_Client_Secret;
            }
            case "uatoga" -> {
                clientId = KeyCloakInfo.uat_oga_client_Id;
                yield KeyCloakInfo.uat_oga_Client_Secret;
            }
            case "prodoga" -> {
                clientId = KeyCloakInfo.prod_oga_client_Id;
                yield KeyCloakInfo.prod_oga_Client_Secret;
            }
            default -> null;
        };
        Utilities.setKeycloakClientDetails(context, KeyCloakInfo.Client_Id, clientId);
        Utilities.setKeycloakClientDetails(context, KeyCloakInfo.Client_Secret, clientSecret);
    }

    private void setOperatingUnit(String environment) {
        Utilities.setOperatingUnitSharedPrefs(context, environment.contains("oga") ? "OGA" : "OFI");
    }

    private void setCommodity() {
        String commodity = dl.getProductInfo();
        Utilities.setCommoditySharedPrefs(context, commodity);
    }


    private void doingAuthRequest() {
        String userEnumerator = enumeratorID.getText().toString().trim();
        String userpasscode = passcode.getText().toString().trim();
        if (!userEnumerator.equalsIgnoreCase("") && !userpasscode.equalsIgnoreCase("")) {

            if (isAccountLocked()) {
                Toast.makeText(context, getString(R.string.account_locked), Toast.LENGTH_LONG).show();
                return;
            }
            showProgress(true);
            // KeyCloak Authentication
            doKeyCloakAuthentication(userEnumerator, passcode.getText().toString().trim());

        } else {
            Toast.makeText(HomeActivity.this, getString(R.string.enumerator_empty), Toast.LENGTH_LONG).show();
        }

    }

    private void doKeyCloakAuthentication(String userEnumerator, String passcode) {

        String clientId = Utilities.getKeyCloakClientDetails(context, KeyCloakInfo.Client_Id);
        String clientSecret = Utilities.getKeyCloakClientDetails(context, KeyCloakInfo.Client_Secret);
        String grantType = Utilities.PASSWORD;
        String scope = Utilities.SCOPE;

        // Check the need of DTO
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername(userEnumerator);
        authRequestDTO.setPasscode(passcode);
        authRequestDTO.setGrantType(grantType);
        authRequestDTO.setClientId(clientId);
        authRequestDTO.setClientSecret(clientSecret);
        authRequestDTO.setScope(scope);

        AuthenticationInterface authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, HomeActivity.this);
        Call<AuthenticationDTO> authCall = authenticationInterface.getKeyCloakAuthToken(clientId, userEnumerator, passcode, grantType, clientSecret, scope);
        authCall.enqueue(HomeActivity.this);
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
        int auto_time_enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
        int auto_time_zone_enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);

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
        if (isNetworkAvailable(context)) {
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

                                            }
                                        });
                            } else {
                                builder.setMessage(response.optString(columnname, res.getString(R.string.app_updated_upload_first_message)))
                                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                if (SystemClock.elapsedRealtime() - lastClickTime < 3000) {
                                                    return;
                                                } else {
                                                    syncOptionAll = true;
                                                    checkAuthenticate();
                                                }
                                                lastClickTime = SystemClock.elapsedRealtime();
                                            }
                                        })
                                        .setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
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

        if (enumeratorID.getText().length() > 0 && passcode.getText().length() > 0) {
            Utilities.saveLongToSharedPrefs(context, "VERSION_NUM", BuildConfig.VERSION_CODE);
            Utilities.saveToSharedPrefs(context, "ENUMERATOR_ID", enumeratorID.getText().toString(), null);
            Utilities.saveToSharedPrefs(context, "PASSCODE", passcode.getText().toString().trim(), null);
            if (!existingUser) {
                nl.syncData();
            } else {
                completeFirstSetup();
            }
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
        if (Utilities.getBoolFromSharedPrefs(staticContext, Utilities.IS_SUPPLIER_ENABLED, false)) {
            cvSupplierAndSurvey.setVisibility(View.VISIBLE);
            supplierAndSurvey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(staticContext, FindSupplierAndSurveyActivity.class);
                    staticContext.startActivity(intent);
                }
            });
        }
        else {
            cvSupplierAndSurvey.setVisibility(View.GONE);
        }

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
        TextView nomodulestosubmittext = findViewById(R.id.nomodulestosubmittext);
        networkQueue = dl.getNetworkQueue();

        if (networkQueue.getCount() > 0) {
            queueCount = findViewById(R.id.networkQueueCount);
            totalItems = networkQueue.getCount();
            String queueTotal = Integer.toString(totalItems);
            queueCount.setText(queueTotal);

            nomodulestosubmittext.setVisibility(View.GONE);
            String[] adapterCols = new String[]{"_id", OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID};
            int[] adapterRowViews = new int[]{R.id.networkQueueId, R.id.networkQueueName, R.id.networkQueuePoiType, R.id.networkQueueOlamFarmerId};
            customListAdapter_networkQueue = new CustomListAdapter_NetworkQueue(context, networkQueue, adapterCols, adapterRowViews, me, isClickable);
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
            queueCount = findViewById(R.id.networkQueueCount);
            queueCount.setText("0");
            if (Utilities.daysToUpdate(context) <= 0) {
                nomodulestosubmittext.setText(R.string.ofis_update_overdue);
                nomodulestosubmittext.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

        //Automatic Trigger ASK For Help
        if (networkQueue != null && networkQueue.getCount() > 0) {
            triggerAutomaticAskForHelp();
        }
        else {
            Utilities.saveToSharedPrefs(staticContext, "AUTO_TRIGGER", null, false);
            Utilities.saveToSharedPrefs(staticContext, "AUTO_TRIGGER_SEND_HELP", null, true);
        }

    }

    private void checkAuthenticate() {

        if (isNetworkAvailable(context)) {
            AccessTokenTask accessTokenTask = new AccessTokenTask(context, getAuthRequestDTO(), HomeActivity.this);
            accessTokenTask.execute();
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
            enableSync();
        }
    }

    private AuthRequestDTO getAuthRequestDTO() {
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername(Utilities.getFromSharedPrefs(HomeActivity.this));
        authRequestDTO.setPasscode(Utilities.getPasscodeFromSharedPrefs(HomeActivity.this));
        return authRequestDTO;
    }

    public void enableSync() {
        try {
            setupSyncAllBtn(true);
            bindNetworkQueue(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAllItemSync() {
        if (isNetworkAvailable(context)) {
            final String message = res.getString(R.string.Please_wait) + " " + res.getString(R.string.Submitting_data);
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    m_ProgressDialog = Utilities.openProgressDialog(context, message, true);
                    if (!farmerSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.farmer_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else if (!isChildSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.child_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else if (!mandateSurveySynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.mandate_survey_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else if (!isConsentSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.consent_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else if (!stakeholderSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.stakeholder_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else {
                        if (totalItems == 0)
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
                            int storedCount = Utilities.minProgressCount;
                            int min = storedCount + 1;
                            int max = networkQueue.getCount();
                            Log.d("HomeActivity", "min : " + min + ",  max : " + max);
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, max, min, pdUpdate);
                            //  m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, msg.arg1, msg.arg2, pdUpdate);
                        }
                    } else if (msg.what == 2) {
                        finishItemSync(msg);
                    } else if (msg.what == 3) {
                        farmerSynced = false;
                    } else if (msg.what == 5) {
                        isChildSynced = false;
                    } else if (msg.what == 8) {
                        mandateSurveySynced = false;
                    } else if (msg.what == 10) {
                        isConsentSynced = false;
                    } else if (msg.what == 11) {
                        stakeholderSynced = false;
                    } else if (msg.what == 9) {
                        closePopup(msg);
                    }
                }
            };
            farmerSynced = true;
            isChildSynced = true;
            mandateSurveySynced = true;
            isConsentSynced = true;
            stakeholderSynced = true;
            nl.getNetworkQueue(null);
            nl.syncNetworkQueue(null, syncHandler);
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
            enableSync();
        }
    }

    public void setupSyncAllBtn(boolean isClickable) {
        if (isClickable) {
            syncAll.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_all_white));
            syncAll.setEnabled(true);
        } else {
            syncAll.setEnabled(false);
            syncAll.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_gray));
        }
    }

    public void startItemSyncAuth(int itemId) {
        setupSyncAllBtn(false);
        bindNetworkQueue(false);
        this.itemId = itemId;
        syncOptionAll = false;
        checkAuthenticate();
    }

    public void startItemSync(int itemId) {
        if (isNetworkAvailable(context)) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    m_ProgressDialog = Utilities.openProgressDialog(context, res.getString(R.string.Please_wait) + " " + res.getString(R.string.Submitting_data), false);
                    if (!farmerSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.farmer_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else if (!isChildSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.child_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else if (!mandateSurveySynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.mandate_survey_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else if (!isConsentSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.consent_not_synced));
                        Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        enableSync();
                    } else if (!stakeholderSynced) {
                        m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 0, res.getString(R.string.stakeholder_not_synced));
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
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, msg.arg1, msg.arg2, pdUpdate);
                        }
                    } else if (msg.what == 2) {
                        finishItemSync(msg);
                    } else if (msg.what == 3) {
                        farmerSynced = false;
                    } else if (msg.what == 5) {
                        isChildSynced = false;
                    } else if (msg.what == 11) {
                        stakeholderSynced = false;
                    } else if (msg.what == 8) {
                        mandateSurveySynced = false;
                    } else if (msg.what == 10) {
                        isConsentSynced = false;
                    } else if (msg.what == 9) {
                        closePopup(msg);
                    } else {
                        finishItemSync(msg);
                    }
                }
            };
            farmerSynced = true;
            isChildSynced = true;
            mandateSurveySynced = true;
            isConsentSynced = true;
            stakeholderSynced = true;
            nl.getNetworkQueue(itemId);
            nl.syncNetworkQueueFromHome(itemId, syncHandler);
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
            enableSync();
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
                if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                    String pdUpdate = msg.obj.toString();
                    m_ProgressDialog.setMessage(pdUpdate);
                    Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                }
                networkQueue.close();
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
                if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                    Utilities.closeProgressDialog(m_ProgressDialog, 200);
                }
                networkQueue.close();
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

    private void downloadSurveyWrap() {
        try {
            ArrayList<ModuleDTO> duplicateList = dl.viewModuleList(context);
            ArrayList<ModuleDTO> moduleList = dl.removeDuplicateModule(duplicateList);
            if (moduleList == null || moduleList.size() == 0) {
                downloadSurveyWrapper.setVisibility(View.GONE);
            } else {
                downloadSurveyWrapper.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindButtons() {
        Button addNewFarmer = findViewById(R.id.AddNewFarmer);
        Button completeModule = findViewById(R.id.CompleteQuestionnaireModule);
        Button addSocialInfra = findViewById(R.id.AddSocialInfrastructure);
        Button captureLocation = findViewById(R.id.captureLocation);
        Button recordTraining = findViewById(R.id.RecordFarmerTraining);
        Button downloadSurvey = findViewById(R.id.downloadSurveyData);
        supplierAndSurvey = (Button) findViewById(R.id.SupplierAndSurveys);
        cvSupplierAndSurvey = findViewById(R.id.cvSupplierAndSurvey);

        syncAll = findViewById(R.id.syncAll);
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
                if (SystemClock.elapsedRealtime() - lastClickTime < 3000) {
                    return;
                } else {
                    if (networkQueue.getCount() > 0) {
                        syncOptionAll = true;
                        setupSyncAllBtn(false);
                        bindNetworkQueue(false);
                        checkAuthenticate();
                    }
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });

        if (Utilities.daysToUpdate(context) > 0 && autotimeEnabled) {

            addNewFarmer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AddFarmerActivity.class);
                    intent.putExtra(EDIT_FARMER_ID, (long) 0);
                    intent.putExtra(EDIT_MODE, false);
                    context.startActivity(intent);

                }
            });

            completeModule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FindFarmerActivity.class);
                    context.startActivity(intent);
                }
            });

            if (Utilities.getBoolFromSharedPrefs(context, Utilities.IS_SUPPLIER_ENABLED, false)) {
                cvSupplierAndSurvey.setVisibility(View.VISIBLE);
                supplierAndSurvey.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, FindSupplierAndSurveyActivity.class);
                        context.startActivity(intent);
                    }
                });
            }
            else {
                cvSupplierAndSurvey.setVisibility(View.GONE);
            }

            addSocialInfra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddInfrastructureActivity.editMode = false;
                    AddInfrastructureActivity.infrastructureID = 0;
                    Intent intent = new Intent(context, AddInfrastructureActivity.class);
                    context.startActivity(intent);
                }
            });

            captureLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // nl.syncGPSData();
                    if (SystemClock.elapsedRealtime() - lastClickTime < 3000) {
                        return;
                    } else {
                        newGps = true;
                        gpsTracker.getDeviceLocation();
                    }
                    lastClickTime = SystemClock.elapsedRealtime();
                }
            });

            recordTraining.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
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
            Utilities.removeFromSharedPrefs(context, Utilities.LOGIN_ATTEMPTS);
            Utilities.removeFromSharedPrefs(context, Utilities.PREVIOUS_ENTERED_USERNAME);
            AuthenticationDTO authenticationDTO = response.body();
            authenticationDTO.setWillExpireAt((System.currentTimeMillis() / 1000) + authenticationDTO.getExpiresIn() / 1000);
            new AuthenticationHelper(this).setAuthenticationDTO(authenticationDTO);
            new AuthenticationHelper(this).setUserName(enumeratorID.getText().toString());

            doInitialCheck();
        } else if (response.code() == 423) {
            enableSync();
            Toast.makeText(HomeActivity.this, getString(R.string.authentication_attempt), Toast.LENGTH_LONG).show();
        } else {
            enableSync();
            if (response.code() == 401) {
                validateLoginAttempts();
            }
            Toast.makeText(HomeActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_LONG).show();
        }
    }

    private void validateLoginAttempts() {
        String previousEnteredUsername = Utilities.getSharedPrefs(context, Utilities.PREVIOUS_ENTERED_USERNAME);
        String currentEnteredUsername = enumeratorID.getText().toString().trim();

        if (previousEnteredUsername != null && !previousEnteredUsername.equals(currentEnteredUsername)) {
            Utilities.removeFromSharedPrefs(context, Utilities.LOGIN_ATTEMPTS);
            Utilities.setSharedPrefs(context, currentEnteredUsername, Utilities.PREVIOUS_ENTERED_USERNAME);
        }

        int LoginAttempts = Utilities.getLoginAttemptsFromSharedPrefs(context);

        if (LoginAttempts >= 4) {
            Utilities.setLockoutTimeFromSharedPrefs(context);
            return;
        }

        Utilities.setLoginAttemptsFromSharedPrefs(context, LoginAttempts + 1);
    }

    private boolean isAccountLocked() {
        Long lastLockedOutTime = Utilities.getLockoutTimeFromSharedPrefs(context);

        if (lastLockedOutTime != -1L) {
            Long currentTime = Utilities.currentTimeInMilliSeconds();

            if(currentTime - lastLockedOutTime < 900000 && currentTime - lastLockedOutTime > 0) {
                return true;
            }
            else {
                Utilities.removeFromSharedPrefs(context, Utilities.LOCKOUT_TIME);
            }
        }
        return false;
    }

    @Override
    public void onFailure(Call<AuthenticationDTO> call, Throwable t) {
        showProgress(false);
        Toast.makeText(HomeActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_LONG).show();
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

            } else {
                Utilities.isAppAlive = true;
            }
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_ACCESS_LOCATION: {
                try {
                    for (String permission : permissions) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            // Permission Denied
                            Toast.makeText(this, R.string.gps_permission_denial_msg, Toast.LENGTH_LONG).show();
                            String userEnumerator = enumeratorID.getText().toString().trim();
                            if (!userEnumerator.equalsIgnoreCase("")) {
                                doingAuthRequest();
                            }

                        } else {
                            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                                // Permission Allowed
                                if (checkvalue == 0) {
                                    checkvalue++;
                                    doingAuthRequest();
                                }
                            } else {
                                // Checkbox set to Never Ask Again
                                showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                break;
            }
            case REQUEST_PERMISSIONS: {
                try {
                    if (grantResults.length > 0 &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[2] == PackageManager.PERMISSION_GRANTED ) {
                        // Permissions granted, proceed with your code
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                            && (!shouldShowRequestPermissionRationale(permissions[0])
                            || !shouldShowRequestPermissionRationale(permissions[1])
                            || !shouldShowRequestPermissionRationale(permissions[2])
                    )) {
                        showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));

                    }else {
                        Toast.makeText(this, R.string.permission_denial_msg_android_13_above, Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    public void onTokenCompleted(String status) {
        enableSync();
        if (!status.equalsIgnoreCase("false")) {
            Utilities.minProgressCount = 0;
            if (syncOptionAll) {
                startAllItemSync();
            } else {
                startItemSync(itemId);
            }
        } else {
            Toast.makeText(HomeActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_LONG).show();
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
            }else if (itemType.equalsIgnoreCase("stakeholder")) {
                Intent intent = new Intent(this, AddFarmerActivity.class);
                intent.putExtra(EDIT_FARMER_ID, (long) networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID)));
                intent.putExtra(Utilities.STAKE_HOLDER_REGISTRATION, true);
                intent.putExtra(EDIT_MODE, true);
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("farmlocation")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                startActivity(intent);
            }  else if (itemType.equalsIgnoreCase("farmpolygon")) {
                Intent recordPolygonIntent = new Intent(this, RecordPolygonActivity.class);
                final String lFarmID = networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID));
                DataLayer dl = new DataLayer(getResources(), HomeActivity.this);
                dl.fetchPlotData(networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                recordPolygonIntent.putExtra(FARMER_ID, Long.valueOf(networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID))));
                recordPolygonIntent.putExtra(FARM_ID, lFarmID);
                recordPolygonIntent.putExtra(OLAM_FARMER_ID, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                recordPolygonIntent.putExtra(FARMER_NAME, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
                int totalCount = dl.fetchPlotTotalCount(String.valueOf(Long.valueOf(networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)))));
                recordPolygonIntent.putExtra(PLOT_COUNT, totalCount);
                SharedPreferenceUtils.getInstance(getApplicationContext()).setPlotCount("" + networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID)), totalCount);
                recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
                startActivity(recordPolygonIntent);
            } else if (itemType.equalsIgnoreCase("module") || itemType.equalsIgnoreCase("mandate_module")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                CompleteModuleActivity.editMode = true;
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("FollowUpSurvey") ||
                    itemType.equalsIgnoreCase("DMSurvey")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                CompleteModuleActivity.editMode = true;
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("childmodule")) {
                Intent intent = new Intent(this, ViewChildLabourSurveyActivity.class);
                ChildLabourCompleteModuleActivity.editMode = true;
                ViewChildLabourSurveyActivity.farmerID = (long) networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                ViewChildLabourSurveyActivity.childID = (long) networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID));
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("fmpreview") || itemType.equalsIgnoreCase("fullfmpreview")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
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
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("update stakeholder")) {
                Bundle bundle = new Bundle();
                bundle.putLong(Utilities.FARMER_ID, Long.parseLong(networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID))));
                bundle.putString(Utilities.STAKE_HOLDER_ID, String.valueOf(networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID))));
                startActivity(new Intent(this, ViewStakeHolderActivity.class).putExtras(bundle));
            } else if (itemType.equalsIgnoreCase("phonenumbers")) {
                Intent intent = new Intent(this, ViewFarmerActivity.class);
                intent.putExtra("phoneNumber", true);
                ViewFarmerActivity.farmerId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                startActivity(intent);
            } else if (itemType.equalsIgnoreCase("Update_ID")) {
                Intent in = new Intent(this, AddNationalidActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(FARMER_NAME, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
                bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                bundle.putBoolean("edit_mode", true);
                in.putExtras(bundle);
                startActivity(in);
            } else if (itemType.equalsIgnoreCase("National_ID")) {
                Intent in = new Intent(this, AddNationalidActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(FARMER_NAME, networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
                bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                bundle.putBoolean("edit_mode", true);
                in.putExtras(bundle);
                startActivity(in);
            }
            else if (itemType.equalsIgnoreCase("Supplier Survey")) {
                int moduleId = networkQueue.getInt(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID));
                String supplierId = networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
                String supplierName = networkQueue.getString(networkQueue.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID));
                SupplierSurveyDto supplierSurveyDto = db.dmMainModuleDao().getModuleById(moduleId);

                Intent intent = new Intent(context, SupplierSurveyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Utilities.SUPPLIER, supplierSurveyDto);
                bundle.putString("SupplierId", supplierId);
                bundle.putString("SupplierName", supplierName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        } catch (CursorIndexOutOfBoundsException ignored) {
        }
    }

    private static class viewSurveyWrapper extends AsyncTask<Object, Boolean, Boolean> {
        ArrayList<ModuleDTO> moduleList = new ArrayList<>();

        @Override
        protected Boolean doInBackground(Object... objects) {
            try {
                DataLayer dl = new DataLayer(staticContext.getResources(), staticContext);
                ArrayList<ModuleDTO> duplicateList = dl.viewModuleList(staticContext);
                moduleList = dl.removeDuplicateModule(duplicateList);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (moduleList == null || moduleList.isEmpty()) {
                downloadSurveyWrapper.setVisibility(View.GONE);
            } else {
                downloadSurveyWrapper.setVisibility(View.VISIBLE);
            }
            int onGroundSurveyNotificationCount = db.onGroundSurveyNotificationDao().getPendingEUDRSurveyCount("");
            if (onGroundSurveyNotificationCount > 0 && shouldShowPopup()) {
                showDailyRemainderPopUp(onGroundSurveyNotificationCount);
            }
        }
    }

    /*Get DownLoad Farm Form List*/
    public static void DownloadFormList(String value) {
        ManageRecords AllFarmFormList = RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<DownloadFormList> call = AllFarmFormList.getAllFarmFormList(1, counrtyCodeNew, Long.valueOf(Utilities.getFromSharedPrefs(staticContext)), odAccessToken);
        call.enqueue(new Callback<DownloadFormList>() {

            @Override
            public void onResponse(Call<DownloadFormList> call, Response<DownloadFormList> response) {
                Log.e("ListOnResponse", response.message());
                Log.e("ListOnRequest", call.toString());

                if (response.code() == 200) {

                    //Toast.makeText(DownloadFarmPlanActivity.this, "Farm Plan is available to download", Toast.LENGTH_SHORT).show();
                    // progressDialog.hide();
                    if (response.body() != null && response.body().getStatusCode() == 200) {
                        if (response.body().getData() != null) {
                            //data1 =response.body().getData();

                            ArrayList<DownloadFormList.data> dataArrayList = response.body().getData();
                            ;
                            // dataArrayList = response.body().getData();
                            //String[] names = new String[dataArrayList.size()];
                            for (int i = 0; i < dataArrayList.size(); i++) {
                                isdownloadfarmplan = dataArrayList.get(i).isIsdownloadfarmplan();
                                Log.d("FARMPLAN_Status", String.valueOf(isdownloadfarmplan));

                                if (isdownloadfarmplan && dataArrayList.size() > 0) {
                                    Utilities.saveToSharedPrefs(staticContext, "DISPLAY_CROPPLAN", "", true);
                                    if (value.equalsIgnoreCase("home")) {
                                        cardview_downloadFarmplan.setVisibility(View.VISIBLE);
                                    }

                                } else {
                                    Utilities.saveToSharedPrefs(staticContext, "DISPLAY_CROPPLAN", "", false);
                                    if (value.equalsIgnoreCase("home")) {
                                        cardview_downloadFarmplan.setVisibility(View.GONE);
                                    }
                                }
                            }
                            Log.e("ListRespValue", response.body().getStatusMessage());

                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DownloadFormList> call, Throwable t) {
                //progressDialog.hide();
                //Toast.makeText(staticContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                // GetAllDiagnosticModuleHome();
                /*if (isEnableDM) {
                    GetAllDiagnosticModule();
                }*/
            }
        });

    }

    private void triggerAutomaticAskForHelp() {

        if (!sharedPreferences.getBoolean("FIRST_RUN", true)) {
            boolean isAutomaticTriggerEnabled = sharedPreferences.getBoolean("AUTO_TRIGGER", true);
            boolean isAskForHelpSent = sharedPreferences.getBoolean("AUTO_TRIGGER_SEND_HELP", false);
            String countryCode = Utilities.getdatafromSharedPrefs(context, "COUNTRY_CODE");

            Handler helpHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String toastMsg = "AUTO TRIGGERED SUCCESSFUL";
                    try {
                        if (msg.what == 1) {
                            toastMsg = "AUTO TRIGGERED SUCCESSFUL";
                        } else if (msg.what == 2) {
                            toastMsg = "AUTO TRIGGERED UNSUCCESSFUL";
                        }
                    } catch(ClassCastException e) {
                        toastMsg = "AUTO TRIGGERED UNSUCCESSFUL";
                    }
                    finally {
                        String finalToastMsg = toastMsg;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Toast.makeText(staticContext, finalToastMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            };

            if (isAutomaticTriggerEnabled && !isAskForHelpSent) {
                nl.sendForHelp("AUTO TRIGGER", countryCode, "", "This is Auto triggered", helpHandler);
                Utilities.saveToSharedPrefs(staticContext, "AUTO_TRIGGER", null, false);
                Utilities.saveToSharedPrefs(staticContext, "AUTO_TRIGGER_SEND_HELP", null, true);
            }
        }

    }

    private static boolean shouldShowPopup() {
        String lastShownDate = Utilities.getSharedPrefs(staticContext, Utilities.KEY_LAST_SHOWN_DATE);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (lastShownDate == null || lastShownDate.equalsIgnoreCase("") || !lastShownDate.equals(todayDate)) {
            Utilities.setSharedPrefs(staticContext, todayDate, Utilities.KEY_LAST_SHOWN_DATE);
            return true;
        }
        return false;
    }

    private static void showDailyRemainderPopUp(int totalCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(staticContext);
        View customView = LayoutInflater.from(staticContext).inflate(R.layout.remainder_popup_dialog, null);
        builder.setView(customView);
        builder.setCancelable(false);

        TextView dialogMessage = customView.findViewById(R.id.dialog_message);
        ImageButton buttonClose = customView.findViewById(R.id.button_close);
        dialogMessage.setText(totalCount + staticRes.getString(R.string.home_popup_msg));
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        buttonClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

}
