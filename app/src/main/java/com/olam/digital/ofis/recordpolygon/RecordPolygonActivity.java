package com.olam.digital.ofis.recordpolygon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.mbms.StreamingServiceInfo;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.HomeActivity;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.SettingsActivity;
import com.olam.digital.ofis.TasksActivity;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.recordpolygon.polygonhistory.InternetConnectionHelper;
import com.olam.digital.ofis.recordpolygon.polygonhistory.PlotDataParser;
import com.olam.digital.ofis.recordpolygon.polygonhistory.PolygonHistory;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.APICall.PolygonHistoryResponseModel;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonHistoryData;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.list.PolygonHistoryDataList;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlTrack;
import org.osmdroid.bonuspack.kml.LineStyle;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import retrofit2.Call;
import retrofit2.Response;

public class RecordPolygonActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, MapEventsReceiver, LocationListener {

    private static final String TAG = RecordPolygonActivity.class.getSimpleName();
    private  final String MATOMOTAG = this.getClass().getSimpleName();

    protected MapView map;

    protected GeoPoint startPoint, destinationPoint;
    protected ArrayList<GeoPoint> viaPoints;
    protected FolderOverlay mItineraryMarkers;
    protected DirectedLocationOverlay myLocationOverlay;
    protected LocationManager mLocationManager;

    protected boolean mTrackingMode;
    ImageButton mTrackingModeButton;
    float mAzimuthAngleSpeed = 0.0f;

    protected FolderOverlay mRoadNodeMarkers;
    static final int OSRM = 0;

    protected FolderOverlay mKmlOverlay; //root container of overlays from KML reading
    public static KmlDocument mKmlDocument; //made static to pass between activities
    public static Stack<KmlFeature> mKmlStack; //passed between activities, top is the current KmlFeature to edit.
    public static KmlFolder mKmlClipboard; //passed between activities. Folder for multiple items selection.

    boolean mIsRecordingTrack = false;
    boolean mIsLoggingStopped;
    boolean mIsLoggingStarted;
    boolean mIsLoggingPaused;
    boolean mIsGPSLocationAvailable;
    private String plotsToGps;
    private String plotsRecentGps;

    private enum RecordingState {
        RECORDING, PAUSED, STOPPED
    }

    OnlineTileSourceBase MAPBOXSATELLITELABELLED;
    boolean mNightMode;

    private Button mStartLoggingButton;

    private TextView farmerName, olamFarmerId, txtPlotName;
    ;
    private ImageView recordingStatus;
    private Button pause, startStop, bttnSave;
    //private ImageButton currentLocation;
    TextView areaInfo;
    Spinner spinnerPlotList;

    private static final String FARMER_ID = "FARMER_ID";
    private static final String FARM_ID = "FARM_ID";
    private static final String PLOT_COUNT = "PLOT_COUNT";
    private static final String OLAM_FARMER_ID = "OLAM_FARMER_ID";
    private static final String FARMER_NAME = "FARMER_NAME";
    long mFarmerId;
    int mFarmId;
    String mOlamFarmerId;
    String mFarmerName;
    String currentTrackname = "OFIS Track";
    String previousTrackname = "Previous OFIS Track";
    static int currentLoggingTrackSpinnerPosition = 0;

    List<plot> mLocationBeanList = new ArrayList<>();
    String spinnerArray[];
    public static final int GRANTED = 0;
    public static final int DENIED = 1;
    public static final int BLOCKED_OR_NEVER_ASKED = 2;

    private int lPlotCount = 0;
    private double areaWithoutRounded=0;

    private Location mCapturedLocation=null;

    ArrayAdapter<String> spinnerArrayAdapter;

    private static final long INTERVAL = 1000 * 3;
    private static final long FASTEST_INTERVAL = 1000 * 2;
    private static final int REQUEST_MSG_PERMISSION = 1004;
    private static final int REQUEST_LOCATION_CODE = 1001;
    private static int DISPLACEMENT = 5;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            onLocationChanged(locationResult.getLastLocation());
        }
    };
    private LocationRequest locationRequest;

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_ANDROID_13 = 1255;
    Logger logger;
    ArrayList<String> plotNameList;
    private LinearLayout plotList;
    int selectedPlotId;

    OFISRoomDB db;

    Polygon polygon;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 7;

    private Marker currentLocationMarker , farmLocationMarker;
    // boolean flag to toggle the ui
//    private Boolean mRequestingLocationUpdates;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Configuration.getInstance().setOsmdroidBasePath(new File(Environment.getExternalStorageDirectory(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(Environment.getExternalStorageDirectory(), "osmdroid/tiles"));

        boolean hwAccelerationOK = true;
        Configuration.getInstance().setMapViewHardwareAccelerated(hwAccelerationOK);

        setContentView(R.layout.activity_record_polygon);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        logger.addRecordToLog("Record polygon activity started");
        db = OFISRoomDB.getDbInstance(getApplicationContext());
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
//                        mRequestingLocationUpdates = true;
                        logger.addRecordToLog("onPermissionGranted");
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
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

        if (!isGPSProviderEnabled()) {
            if (locationMode() !=2){
                showTurnOnGPSDialog();
            }

        } else {
            //Go to the current location with location marker in center of map.
            centerCurrentLocation();
        }

        mFarmerId = getIntent().getLongExtra(FARMER_ID, 0L);
        mFarmId = getIntent().getIntExtra(FARM_ID, 0);
        mOlamFarmerId = getIntent().getStringExtra(OLAM_FARMER_ID);
        mFarmerName = getIntent().getStringExtra(FARMER_NAME);
        lPlotCount = getIntent().getIntExtra(PLOT_COUNT, 0);
        SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmID("" + mOlamFarmerId);

        farmerName = (TextView) findViewById(R.id.farmer_name);
        olamFarmerId = (TextView) findViewById(R.id.olam_farmer_id);
        txtPlotName = (TextView) findViewById(R.id.plot_name);
        areaInfo = (TextView) findViewById(R.id.areaInfo);
        if (!TextUtils.isEmpty(mFarmerName)) {
            farmerName.setText(mFarmerName);
        }
        if (!TextUtils.isEmpty(mOlamFarmerId)) {
            olamFarmerId.setText(mOlamFarmerId);
        }
        loadPlotName(0);

        /*DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        Cursor plotIdsSynced = dl.syncedPlotIds(String.valueOf(mFarmerId));
        if (plotIdsSynced!=null){
            int totalPlotCount = plotIdsSynced.getCount();
            spinnerArray = new String[lPlotCount];
            for (int i = 0; i < lPlotCount; i++) {
                spinnerArray[i] = getResources().getString(R.string.plot, (i + 1 + totalPlotCount));

                //SharedPreferenceUtils.getInstance(getApplicationContext()).removePlotUsingKey("Plot " + (i + 1));
            }
        }else {
            spinnerArray = new String[lPlotCount];
            for (int i = 0; i < lPlotCount; i++) {
                spinnerArray[i] = getResources().getString(R.string.plot, (i + 1));

                //SharedPreferenceUtils.getInstance(getApplicationContext()).removePlotUsingKey("Plot " + (i + 1));
            }
        }*/

        spinnerArray = new String[lPlotCount];
        for (int i = 0; i < lPlotCount; i++) {
            spinnerArray[i] = getResources().getString(R.string.plot, (i + 1));

            //SharedPreferenceUtils.getInstance(getApplicationContext()).removePlotUsingKey("Plot " + (i + 1));
        }
        spinnerPlotList = (Spinner) findViewById(R.id.plot_number);
        recordingStatus = (ImageView) findViewById(R.id.recording_status);
        pause = (Button) findViewById(R.id.pause_recording);
        mStartLoggingButton = (Button) findViewById(R.id.start_stop_recording);
        bttnSave = (Button) findViewById(R.id.save_recording);
        //currentLocation = (ImageButton) findViewById(R.id.current_location);
        pause.setEnabled(false);
        bttnSave.setEnabled(false);
        disableBtn(pause); disableBtn(bttnSave);
        spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinnerPlotList.setAdapter(spinnerArrayAdapter);
        spinnerPlotList.setOnItemSelectedListener(this);
        mStartLoggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.addRecordToLog("mStartLoggingButton");
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_START_LOGGING,  ScreenNameConstants.BUTTON_CLICKED);
                if(pause.getText().toString().equalsIgnoreCase("resume")){


                    Toast.makeText(RecordPolygonActivity.this, "Please remove pause state first", Toast.LENGTH_SHORT).show();

                }
                else{
                    handleStartStopLoggingButton();
                }

            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_PAUSE_POLYGON,  ScreenNameConstants.BUTTON_CLICKED);
                if (mIsLoggingPaused) {
                    setPauseButtonState(false);
                } else {
                    setPauseButtonState(true);
                }
            }
        });

        bttnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SAVE_POLYGON,  ScreenNameConstants.BUTTON_CLICKED);
                setLoggingStarted(false);
                setRecordingIcon(RecordingState.STOPPED);
                if (!checkPlotEntryExistInPreference()) {
                    if (calculateArea() >= 0.0009){
                        savePolygonToSharedPreference();
                    }else{
                    areaInfo.setText(getResources().getString(R.string.area_info, String.valueOf(areaWithoutRounded)));
                        showZeroAreaDialog();
                    }
                } else {
                    showToastMessage(getResources().getString(R.string.plot_already_logged, currentTrackname));
                }
            }
        });

        SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        MAPBOXSATELLITELABELLED = new MapBoxTileSource("MapBoxSatelliteLabelled", 1, 19, 256, ".png");
        ((MapBoxTileSource) MAPBOXSATELLITELABELLED).retrieveAccessToken(this);
        ((MapBoxTileSource) MAPBOXSATELLITELABELLED).retrieveMapBoxMapId(this);
        TileSourceFactory.addTileSource(MAPBOXSATELLITELABELLED);


        map = (MapView) findViewById(R.id.map);

        map.setTilesScaledToDpi(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();

        //To use MapEventsReceiver methods, we add a MapEventsOverlay:
        MapEventsOverlay overlay = new MapEventsOverlay(this);
        map.getOverlays().add(overlay);


        myLocationOverlay = new DirectedLocationOverlay(this);
        map.getOverlays().add(myLocationOverlay);
        CompassOverlay compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        if (savedInstanceState == null) {
            Location location = null;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //  location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                if (location == null)
//                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                try {
                    Task<Location> locationResult = getFusedLocationProviderClient(this).getLastLocation();
                    locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                Location location = task.getResult();
                                if (location != null) {
                                    //location known:
                                    onLocationChanged(location);
                                } else {
                                    //no location known: hide myLocationOverlay
                                    myLocationOverlay.setEnabled(false);
                                }
                            }
                        }
                    });
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

            }

            startPoint = null;
            destinationPoint = null;
            viaPoints = new ArrayList<GeoPoint>();
        } else {
            myLocationOverlay.setLocation((GeoPoint) savedInstanceState.getParcelable("location"));
            //TODO: restore other aspects of myLocationOverlay...
            startPoint = savedInstanceState.getParcelable("start");
            destinationPoint = savedInstanceState.getParcelable("destination");
            viaPoints = savedInstanceState.getParcelableArrayList("viapoints");
            mapController.setCenter(startPoint);
        }

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
        map.getOverlays().add(scaleBarOverlay);

        // Itinerary markers:
        mItineraryMarkers = new FolderOverlay();
        //mItineraryMarkers.setName(getString(R.string.itinerary_markers_title));
        map.getOverlays().add(mItineraryMarkers);

        //Tracking system:
        mTrackingModeButton = (ImageButton) findViewById(R.id.buttonTrackingMode);
        mTrackingModeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mTrackingMode = !mTrackingMode;
                updateUIWithTrackingMode();
            }
        });
        if (savedInstanceState != null) {
            mTrackingMode = savedInstanceState.getBoolean("tracking_mode");
            updateUIWithTrackingMode();
        } else
            mTrackingMode = false;

        View expander = findViewById(R.id.expander);

        expander.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                View topSection = findViewById(R.id.topSection);
                View saveRecording = findViewById(R.id.save_recording);
                if (topSection.getVisibility() == View.VISIBLE) {
                    topSection.setVisibility(View.GONE);
                    saveRecording.setVisibility(View.GONE);
                } else {
                    topSection.setVisibility(View.VISIBLE);
                    saveRecording.setVisibility(View.VISIBLE);
                }
            }
        });
        View topSection = findViewById(R.id.topSection);
        if (prefs.getInt("PANEL_VISIBILITY", View.VISIBLE) == 0) {
            topSection.setVisibility(View.VISIBLE);
        } else {
            topSection.setVisibility(View.GONE);
        }

        mRoadNodeMarkers = new FolderOverlay();
        mRoadNodeMarkers.setName("Route Steps");
        map.getOverlays().add(mRoadNodeMarkers);

        mKmlOverlay = null;
        if (savedInstanceState != null) {
            //STATIC - mKmlDocument = savedInstanceState.getParcelable("kml");
            updateUIWithKml();
        } else { //first launch:
            mKmlDocument = new KmlDocument();
            mKmlStack = new Stack<KmlFeature>();
            mKmlClipboard = new KmlFolder();
            //check if intent has been passed with a kml URI to load (url or file)
            Intent onCreateIntent = getIntent();
            if (onCreateIntent.getAction().equals(Intent.ACTION_VIEW)) {
                String uri = onCreateIntent.getDataString();
                //openFile(uri, true, false);
            }
        }
        setStdTileProvider();
//        map.setTileSource(MAPBOXSATELLITELABELLED);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getOverlayManager().getTilesOverlay().setColorFilter(null);
        mapController.setZoom(17);

        int permissionStatus = getPermissionStatus(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionStatusExtStorage = getPermissionStatus(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStatus == BLOCKED_OR_NEVER_ASKED || permissionStatusExtStorage == BLOCKED_OR_NEVER_ASKED) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                checkPermissionsForAndroid13Above();
            }else {
                checkPermissions();
            }

        }
        initializeLoggingVariables();
    }

    private void enablePauseBtn(Button btn){
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_secondary_background));
        btn.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private  void enableSaveBtn(Button btn){
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btnColorPrimary));
        btn.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void disableBtn(Button btn){
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_primary_disabled));
        btn.setTextColor(ContextCompat.getColor(this, R.color.btn_disabled_text));
    }

    private int locationMode(){
        int locationMode=0;
        try {
            locationMode = Settings.Secure.getInt(this.getContentResolver(),Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return locationMode;
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
     * Whenever user start polygon recording at that time will fetch last known location from Android API and put location marker in center of Map UI.
     * If last known location is null then will request GPS and fetch current location to put location marker in center of Map UI.
     * Last Known location data is quickly to get from API and easily to update new location data.
     */
    private void centerCurrentLocation() {
        if (startLocationUpdates()) {
            Location location = null;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null)
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location != null && map != null) {
                myLocationOverlay.setEnabled(true);
                GeoPoint newLocation = new GeoPoint(location);
                map.getController().animateTo(newLocation);
                map.setMapOrientation(-(location.getBearing()));
                //location known:
                onLocationChanged(location);
            }

            initCenterCurrentLocation();
        }
    }

    private void initCenterCurrentLocation() {
        int permissionStatus = getPermissionStatus(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionStatus == DENIED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                checkPermissionsForAndroid13Above();
            }else {
                checkPermissions();
            }
            return;
        } else if (permissionStatus == BLOCKED_OR_NEVER_ASKED) {
            showChangeSettingsDialog();
            return;
        }
        if (startLocationUpdates()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                int count = 0;

                @Override
                public void run() {
                    if (count > 20) {
                        showToastMessage(getResources().getString(R.string.try_again_later_msg));
                        return;
                    }
                    count++;
                    if (mIsGPSLocationAvailable) {
                        dismissProgressDialog();
                    } else {
                        if (dialog == null) {
                            showProgressDialog();
                        }
                        handler.postDelayed(this, 2000);
                    }
                }
            }, 500);
        } else {
            showToastMessage(getResources().getString(R.string.try_again_later_msg));
        }
    }

    void initializeKMLDocument() {
        mKmlDocument = new KmlDocument();
        mKmlStack = new Stack<KmlFeature>();
        mKmlClipboard = new KmlFolder();
    }

    boolean checkPlotEntryExistInPreference() {
        LocationBeanList LocationBeanList = SharedPreferenceUtils.getInstance(getApplicationContext()).getLocationBeanList(currentTrackname);
        if (LocationBeanList != null) {
            return true;
        }
        return false;
    }

    boolean checkPreviousPlotEntryExistInPreference() {
        LocationBeanList LocationBeanList = SharedPreferenceUtils.getInstance(
                getApplicationContext()).getLocationBeanList(previousTrackname);
        if (LocationBeanList != null) {
            return true;
        }
        return false;
    }

    String getLastLoggedPlotFromPreference() {
        int plot = 0;
        for (int i = 0; i < lPlotCount; i++) {
            String plotName = getResources().getString(R.string.plot, i);
            LocationBeanList LocationBeanList = SharedPreferenceUtils.getInstance(
                    getApplicationContext()).getLocationBeanList(plotName);
            if (LocationBeanList != null) {
                plot = i;
            }
        }
        return getResources().getString(R.string.plot, (plot + 1));
    }

    int getTotalLoggedPlotCount() {
        int count = 0;
        try {
            for (int i = 0; i < lPlotCount; i++) {
                LocationBeanList LocationBeanList = SharedPreferenceUtils.
                        getInstance(getApplicationContext()).getLocationBeanList(spinnerArray[i]);
                if (LocationBeanList != null) {
                    count++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return count;
    }

    void showChangeSettingsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
        TextView tvTitle= dialogView.findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.location_permission_alertdialog_title);
        tvMsg.setText(R.string.open_application_settings_msg);
        Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
        AlertDialog alert = builder.create();

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToastMessage(getResources().getString(R.string.gps_permission_denial_msg));
                alert.dismiss();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                alert.dismiss();
            }
        });

        alert.show();
    }

    void handleStartStopLoggingButton() {

        if (isGPSProviderEnabled() || locationMode()==2) {
            if (!checkPlotEntryExistInPreference()) {
                if (mLocationBeanList.size() > 0 && !mIsLoggingStarted) {
                    //new
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
                    builder.setView(dialogView);
                    TextView tvTitle=(TextView)dialogView.findViewById(R.id.tv_title);
                    tvTitle.setVisibility(View.VISIBLE);
                    tvTitle.setText(getResources().getString(R.string.ready_to_save, currentTrackname));
                    TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
                    tvMsg.setText(R.string.restart_logging);
                    Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
                    Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
                    AlertDialog alert = builder.create();
                    btnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showToastMessage(getResources().getString(R.string.save_existing_logs));
                            alert.dismiss();
                        }
                    });

                    btnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mLocationBeanList = new ArrayList<>();
                            bttnSave.setEnabled(false);
                            disableBtn(bttnSave);
                            initializeKMLDocument();
                            initiateLogging();
                            alert.dismiss();
                        }
                    });
                    alert.show();
                } else {
                    initiateLogging();
                }

            } else {
//                showToastMessage(getResources().getString(R.string.already_logged_and_saved, currentTrackname));
                editPlotAlertDialog();
            }

            /**
             * Whenever user start polygon recording at that time will fetch last known location from Android API and put location marker in center of Map UI.
             * If last known location is null then will request GPS and fetch current location to put location marker in center of Map UI.
             * Last Known location data is quickly to get from API and easily to update new location data.
             */
            if (startLocationUpdates()) {
                Location location = null;
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    if (location == null)
//                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    try {
                        Task<Location> locationResult = getFusedLocationProviderClient(this).getLastLocation();
                        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful()) {
                                    Location location = task.getResult();
                                    if (location != null) {
                                        GeoPoint newLocation = new GeoPoint(location);
                                        map.getController().animateTo(newLocation);
                                        //location known:
                                        onLocationChanged(location);
                                    }
                                }
                            }
                        });
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }


                }
            }

        } else {
            showTurnOnGPSDialog();
        }
    }

    void initiateLogging() {
        int permissionStatus = getPermissionStatus(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionStatus == DENIED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                checkPermissionsForAndroid13Above();
            }else {
                checkPermissions();
            }
            return;
        } else if (permissionStatus == BLOCKED_OR_NEVER_ASKED) {
            showChangeSettingsDialog();
            return;
        }
        if (startUpdate()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                int count = 0;

                @Override
                public void run() {
                    if (count > 10) {
                        showToastMessage(getResources().getString(R.string.try_again_later_msg));
                        return;
                    }
                    count++;
                    if (mIsGPSLocationAvailable) {
                        dismissProgressDialog();
                        if (!mIsLoggingStarted) {
                            DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
                            int farmPlotCount = dl.fetchSyncedPlotCountByFarmerID(String.valueOf(mFarmerId));
                            if(farmPlotCount == 0) {
                                if (checkPreviousPlotEntryExistInPreference() || (currentTrackname.equals(previousTrackname)))
                                    showPlotLocationConfirmDialog(currentTrackname);
                                else
                                    showToastMessage(getResources().getString(R.string.previous_plot_not_recorded_msg, getLastLoggedPlotFromPreference()));
                            }else{
                                int loggedPlotCount = getTotalLoggedPlotCount() ;
//                                int actualPlot = Integer.parseInt(StringUtils.split(currentTrackname)[1]);
                                int actualPlot = extractLastNumber(currentTrackname);
                                if(loggedPlotCount == 0 ){
                                    if(actualPlot == farmPlotCount + 1){
                                        showPlotLocationConfirmDialog(currentTrackname);
                                    }else{
                                        showToastMessage(getResources().getString(R.string.previous_plot_not_recorded_msg, "Plot " + (farmPlotCount+1)));
                                    }
                                }else{
                                    int i = loggedPlotCount + farmPlotCount;
                                    if(i+1 == actualPlot){
                                        showPlotLocationConfirmDialog(currentTrackname);
                                    }else{
                                        showToastMessage(getResources().getString(R.string.previous_plot_not_recorded_msg, "Plot " + (i+1)));
                                    }
                                }
                            }
                        } else {
                            if (mLocationBeanList.size() > 0) {
                                showToastMessage(getResources().getString(R.string.ready_to_save, currentTrackname).trim().toUpperCase());
                                pause.setEnabled(false);
                                disableBtn(pause);
                                bttnSave.setEnabled(true);
                                enableSaveBtn(bttnSave);
                                setButtonsLoggingState(false);
                            } else {




                                    showToastMessage(getResources().getString(R.string.location_not_yet_logged));
                                    pause.setEnabled(false);
                                    bttnSave.setEnabled(false);
                                    disableBtn(pause);
                                    disableBtn(bttnSave);
                                    setButtonsLoggingState(false);

                            }
                        }
                    } else {
                        if (dialog == null) {
                            showProgressDialog();
                        }
                        handler.postDelayed(this, 2000);
                    }
                }
            }, 500);
        } else {
            showToastMessage(getResources().getString(R.string.try_again_later_msg));
        }
    }

    public static Integer extractLastNumber(String trackName) {
        String[] parts = trackName.split("\\s+");  // Split by whitespace

        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                return Integer.parseInt(parts[i]);  // Attempt to parse integer
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    boolean isGPSProviderEnabled() {
        if (mLocationManager != null) {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return true;
            }
        }
        return false;
    }

    private void setRecordingIcon(RecordingState recordingState) {
        if (recordingState == RecordingState.RECORDING) {
            recordingStatus.setImageResource(R.drawable.animation_recording);
            AnimationDrawable frameAnimation = (AnimationDrawable) recordingStatus.getDrawable();
            frameAnimation.start();
        } else if (recordingState == RecordingState.PAUSED) {
            recordingStatus.setImageResource(R.drawable.ic_recording_paused);
        } else if (recordingState == RecordingState.STOPPED) {
            recordingStatus.setImageResource(R.drawable.ic_recording_stopped);
        }
    }

    private ProgressDialog dialog;

    private void showProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.fetch_location_msg));
        dialog.show();
    }

    private void dismissProgressDialog() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void setButtonsLoggingState(boolean aIsLoggingStarted) {

        if (aIsLoggingStarted) {

            mStartLoggingButton.setText(R.string.stop_logging_button_text);
            setLoggingStarted(true);
            setRecordingIcon(RecordingState.RECORDING);
            pause.setEnabled(true);
            enablePauseBtn(pause);

        } else {


            setLoggingStarted(false);
            setRecordingIcon(RecordingState.STOPPED);
            mStartLoggingButton.setText(R.string.start_logging_button_text);
            pause.setText(R.string.pause_button_text);
            pause.setEnabled(false);
            disableBtn(pause);

        }
    }

    private void setPauseButtonState(boolean aIsPaused) {
        if (aIsPaused) {
            pause.setText(R.string.resume_button_text);
            setRecordingIcon(RecordingState.PAUSED);
            setLoggingPaused(true);
        } else {
            pause.setText(R.string.pause_button_text);
            setRecordingIcon(RecordingState.RECORDING);
            setLoggingPaused(false);
        }
    }


    void showTurnOnGPSDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
        TextView tvTitle= dialogView.findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.gps_manager_msg);
        tvMsg.setText(R.string.enable_gps_msg);
        Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
        AlertDialog alert = builder.create();

        AlertDialog finalAlert = alert;
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalAlert.dismiss();
                finish();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalAlert.dismiss();
                showToastMessage(getResources().getString(R.string.gps_turned_on));
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        });

        alert.show();
    }

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    boolean getLocationPermissionStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public static int getPermissionStatus(AppCompatActivity activity, String androidPermissionName) {
        if (ContextCompat.checkSelfPermission(activity, androidPermissionName) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermissionName)) {
                return BLOCKED_OR_NEVER_ASKED;
            }
            return DENIED;
        }
        return GRANTED;
    }

    void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    void checkPermissionsForAndroid13Above(){
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);

        }

        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                //perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }

                try {
                    // Check for WRITE_EXTERNAL_STORAGE
                    Boolean storage = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        storage = perms.get(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                                perms.get(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
                                perms.get(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
                    }else {
                        storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    }
                    if (!storage) {
                        // Permission Denied
                        Toast.makeText(this, R.string.storage_permission_explanation, Toast.LENGTH_LONG).show();
                    } // else: permission was granted, yay!

                    Boolean locationPerm = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    if (!locationPerm) {
                        // Permission Denied
                        Toast.makeText(this, R.string.location_permission_explanation, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case REQUEST_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Location permissions granted, get the last known location
                    getLastLocation();
                } else {
                    // Location permissions not granted, handle accordingly
                    Log.d("Location", "Location permissions not granted.");
                }
            }


        }
    }

    void initializeButtonStates() {
        bttnSave.setEnabled(false);
        pause.setEnabled(false);
        disableBtn(bttnSave);
        disableBtn(pause);
        mStartLoggingButton.setText(R.string.start_logging_button_text);
    }

    void initializeLoggingVariables() {
        mIsLoggingStopped = false;
        mIsLoggingStarted = false;
        mIsLoggingPaused = false;
        mIsGPSLocationAvailable = false;
    }

    void setLoggingPaused(boolean isPaused) {
        mIsLoggingPaused = isPaused;
    }

    void setLoggingStarted(boolean isStarted) {
        mIsLoggingStarted = isStarted;
        mIsLoggingStopped = !isStarted;
    }

    void setLoggingStopped(boolean isStopped) {
        mIsLoggingStopped = isStopped;
        mIsLoggingStarted = !isStopped;
    }

    void setGPSLocationAvailable(boolean isAvailable) {
        mIsGPSLocationAvailable = isAvailable;
        if (isAvailable) {
            //currentLocation.setImageResource(R.drawable.ic_actionbutton_location_found);
        } else {
            //currentLocation.setImageResource(R.drawable.ic_actionbutton_location_searching);
        }

    }

    void savePrefs() {
        SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("MAP_ZOOM_LEVEL", map.getZoomLevel());
        GeoPoint c = (GeoPoint) map.getMapCenter();
        ed.putFloat("MAP_CENTER_LAT", (float) c.getLatitude());
        ed.putFloat("MAP_CENTER_LON", (float) c.getLongitude());
        MapTileProviderBase tileProvider = map.getTileProvider();
        String tileProviderName = tileProvider.getTileSource().name();
        ed.putString("TILE_PROVIDER", tileProviderName);
        ed.putBoolean("NIGHT_MODE", mNightMode);
        ed.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("location", myLocationOverlay.getLocation());
        outState.putBoolean("tracking_mode", mTrackingMode);
        outState.putParcelable("start", startPoint);
        outState.putParcelable("destination", destinationPoint);
        outState.putParcelableArrayList("viapoints", viaPoints);
        savePrefs();
    }

    boolean startLocationUpdates() {
        logger.addRecordToLog("startLocationUpdates");
        boolean result = false;
        for (final String provider : mLocationManager.getProviders(true)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //   mLocationManager.requestLocationUpdates(provider, 0, 0, this);
                startUpdate();
                result = true;
            }
        }
        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        logger.addRecordToLog("onPause checkSelfPermission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
        }
    }

    void updateUIWithTrackingMode() {
        if (mTrackingMode) {
            mTrackingModeButton.setBackgroundResource(R.drawable.btn_tracking_on);
            if (myLocationOverlay.isEnabled() && myLocationOverlay.getLocation() != null) {
                map.getController().animateTo(myLocationOverlay.getLocation());
            }
            map.setMapOrientation(-mAzimuthAngleSpeed);
            mTrackingModeButton.setKeepScreenOn(true);
        } else {
            mTrackingModeButton.setBackgroundResource(R.drawable.btn_tracking_off);
            map.setMapOrientation(0.0f);
            mTrackingModeButton.setKeepScreenOn(false);
        }
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(map);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    void setStdTileProvider() {
        if (!(map.getTileProvider() instanceof MapTileProviderBasic)) {
            MapTileProviderBasic bitmapProvider = new MapTileProviderBasic(this);
            map.setTileProvider(bitmapProvider);
        }
    }

    void updateUIWithKml() {
        try {
            if (mKmlOverlay != null) {
                mKmlOverlay.closeAllInfoWindows();
                map.getOverlays().remove(mKmlOverlay);
            }
            mKmlOverlay = (FolderOverlay) mKmlDocument.mKmlRoot.buildOverlay(map, buildDefaultStyle(), null, mKmlDocument);
            map.getOverlays().add(mKmlOverlay);
            map.invalidate();
        }catch (Exception e){
           e.printStackTrace();
        }
    }

    Style buildDefaultStyle() {
        Drawable defaultKmlMarker = ResourcesCompat.getDrawable(getResources(), R.drawable.record_polygon, null);
        Bitmap bitmap = ((BitmapDrawable) defaultKmlMarker).getBitmap();
        return new Style(bitmap, Color.RED, 5.0f, Color.RED);
    }

    //------------ LocationListener implementation
    private final NetworkLocationIgnorer mNetworkLocationIgnorer = new NetworkLocationIgnorer();
    long mLastTime = 0; // milliseconds
    double mSpeed = 0.0; // km/h

    @Override
    public void onLocationChanged(final Location pLoc) {
        setGPSLocationAvailable(true);
        if (pLoc !=null){
            mCapturedLocation= pLoc;
        }

        long currentTime = System.currentTimeMillis();
        if (mNetworkLocationIgnorer.shouldIgnore(pLoc.getProvider(), currentTime))
            return;
        double dT = currentTime - mLastTime;
        if (dT < 100.0) {
            //Toast.makeText(this, pLoc.getProvider()+" dT="+dT, Toast.LENGTH_SHORT).show();
            return;
        }
        mLastTime = currentTime;

        // Capture the Logger item

        logger.addRecordToLog( currentTrackname +"-- >  Accuracy -->" + pLoc.getAccuracy()  +"\n" +
                "    Latitude  -->"+ pLoc.getLatitude() +"\n" +
                "    Longitude  -->"+ pLoc.getLongitude() +"\n" +
                "    Altitude  -->"+ pLoc.getAltitude() +"\n" +
                "    Bearing  -->"+ pLoc.getBearing()+"\n" );

        GeoPoint newLocation = new GeoPoint(pLoc);
        if (!myLocationOverlay.isEnabled()) {
            //we get the location for the first time:
            myLocationOverlay.setEnabled(true);
            map.getController().animateTo(newLocation);
        }

        GeoPoint prevLocation = myLocationOverlay.getLocation();
        myLocationOverlay.setLocation(newLocation);
        myLocationOverlay.setAccuracy((int) pLoc.getAccuracy());
        if (prevLocation != null && pLoc.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            mSpeed = pLoc.getSpeed() * 3.6;
            long speedInt = Math.round(mSpeed);
            TextView speedTxt = (TextView) findViewById(R.id.speed);
            speedTxt.setText(getResources().getString(R.string.speed_unit_kmph, speedInt));

            //TODO: check if speed is not too small
            mAzimuthAngleSpeed = pLoc.getBearing();
            myLocationOverlay.setBearing(mAzimuthAngleSpeed);
        }

        map.getController().animateTo(newLocation);
        map.setMapOrientation(-mAzimuthAngleSpeed);

        if (mTrackingMode) {
            //keep the map view centered on current location:
            /*map.getController().animateTo(newLocation);
            map.setMapOrientation(-mAzimuthAngleSpeed);*/
        } else {
            //just redraw the location overlay:
            map.invalidate();
        }
        if (mIsLoggingStopped || mIsLoggingPaused) {
            return;
        }
        if (mIsLoggingStarted) {
            recordCurrentLocationInTrack(currentTrackname.toLowerCase().replace(" ", "-"), currentTrackname, newLocation);
        }
    }

    static int[] TrackColor = {
            Color.CYAN - 0x20000000, Color.BLUE - 0x20000000, Color.MAGENTA - 0x20000000, Color.RED - 0x20000000, Color.YELLOW - 0x20000000
    };

    KmlTrack createTrack(String id, String name) {
        KmlTrack t = new KmlTrack();
        KmlPlacemark p = new KmlPlacemark();
        p.mId = id;
        p.mName = name;
        p.mGeometry = t;
        mKmlDocument.mKmlRoot.add(p);
        //set a color to this track by creating a style:
        Style s = new Style();
        int color = Color.RED - 0x20000000;
        s.mLineStyle = new LineStyle(color, 8.0f);
        String styleId = mKmlDocument.addStyle(s);
        p.mStyle = styleId;
        return t;
    }

    void recordCurrentLocationInTrack(String trackId, String trackName, GeoPoint currentLocation) {
        //Find the KML track in the current KML structure - and create it if necessary:
        KmlTrack t;
        KmlFeature f = mKmlDocument.mKmlRoot.findFeatureId(trackId, false);
        if (f == null)
            t = createTrack(trackId, trackName);
        else if (!(f instanceof KmlPlacemark))
            //id already defined but is not a PlaceMark
            return;
        else {
            KmlPlacemark p = (KmlPlacemark) f;
            if (!(p.mGeometry instanceof KmlTrack))
                //id already defined but is not a Track
                return;
            else
                t = (KmlTrack) p.mGeometry;
        }
        //TODO check if current location is really different from last point of the track
        //record in the track the current location at current time:
        t.add(currentLocation, new Date());
        if (currentLocation != null) {
            plot plot = new plot();
            plot.setLat(currentLocation.getLatitude());
            plot.setLng(currentLocation.getLongitude());
            plot.setEle(0.0);
            mLocationBeanList.add(plot);
        }
        //refresh KML:
        updateUIWithKml();
    }

    @Override
    public void onProviderDisabled(String provider) {
        setGPSLocationAvailable(false);
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onProviderEnabled(String provider) {
        setGPSLocationAvailable(true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        int farmPlotCount = dl.fetchSyncedPlotCountByFarmerID(String.valueOf(mFarmerId));
        currentTrackname = getResources().getString(R.string.plot, (position + 1));

        if(farmPlotCount == 0){
            if (position == 0)
                previousTrackname = getResources().getString(R.string.plot, (position + 1));
            else
                previousTrackname = getResources().getString(R.string.plot, position);
        }else if (farmPlotCount > 0){
            if (position == farmPlotCount + 1 )
                previousTrackname = getResources().getString(R.string.plot, (position + 1));
            else
                previousTrackname = getResources().getString(R.string.plot, position);
        }
        loadPlotName(position);
        areaInfo.setText("");
        if (mIsLoggingStarted) {
            String currentPlotName = "";
            try {
                currentPlotName = spinnerArray[currentLoggingTrackSpinnerPosition];
            } catch (Exception e) {

            }

            if (!currentTrackname.equals(currentPlotName)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
                builder.setView(dialogView);
                TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
                TextView tvTitle= dialogView.findViewById(R.id.tv_title);
                tvTitle.setVisibility(View.VISIBLE);
                tvTitle.setText(R.string.logging_progress_msg);
                tvMsg.setText(getResources().getString(R.string.cancel_logging_question) +" "+ currentPlotName + "?");
                Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
                Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
                AlertDialog alert = builder.create();

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                        spinnerPlotList.setSelection(currentLoggingTrackSpinnerPosition);
                        loadPlotName(currentLoggingTrackSpinnerPosition);
                    }
                });
                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                        spinnerPlotList.setSelection(currentLoggingTrackSpinnerPosition);
                        loadPlotName(currentLoggingTrackSpinnerPosition);
                        showToastMessage(getResources().getString(R.string.cancel_logging_msg, currentTrackname));
                        mLocationBeanList = new ArrayList<>();
                        initializeKMLDocument();
                        setButtonsLoggingState(false);
                        initializeButtonStates();
                    }
                });

                alert.show();
            }
            return;
        }
        currentTrackname = getResources().getString(R.string.plot, (position + 1));
        if (position == 0)
            previousTrackname = getResources().getString(R.string.plot, (position + 1));
        else
            previousTrackname = getResources().getString(R.string.plot, position);
        if (polygon != null) {
            map.getOverlays().remove(polygon);
        }
        if(isSyncedPlot(position)){
            if (InternetConnectionHelper.isInternetAvailable(this)) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                } else {
//                    getLastLocation();
                    try {
                        PolygonHistoryApiCall((int) mFarmerId, position);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
//                    getFarmLocation();
                }
            } else {
                showAlert(getString(R.string.no_internet_polygon));
            }
            mStartLoggingButton.setVisibility(View.GONE);
            pause.setVisibility(View.GONE);
            bttnSave.setVisibility(View.GONE);
            recordingStatus.setVisibility(View.GONE);
        }else {
            mStartLoggingButton.setVisibility(View.VISIBLE);
            pause.setVisibility(View.VISIBLE);
            bttnSave.setVisibility(View.VISIBLE);
            recordingStatus.setVisibility(View.VISIBLE);
            if (!checkPlotEntryExistInPreference()) {
                showToastMessage(getResources().getString(R.string.not_logged_yet, currentTrackname));
                initializeKMLDocument();
                loadMapWithPolygon(currentTrackname);
            } else {
                initializeKMLDocument();
                loadMapWithPolygon(currentTrackname);
            }
        }

    }

    private void showAlert(String msg) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle(R.string.Alert_txt);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    protected Polygon mDestinationPolygon;

    public void updateUIWithPolygon(ArrayList<GeoPoint> polygon, String name) {
        if (mKmlOverlay != null) {
            mKmlOverlay.closeAllInfoWindows();
            map.getOverlays().remove(mKmlOverlay);
        }
        List<Overlay> mapOverlays = map.getOverlays();
        int location = -1;
        if (mDestinationPolygon != null)
            location = mapOverlays.indexOf(mDestinationPolygon);
        mDestinationPolygon = new Polygon();
        mDestinationPolygon.setFillColor(0x15FF0080);
        mDestinationPolygon.setStrokeColor(0x800000FF);
        mDestinationPolygon.setStrokeWidth(5.0f);
        mDestinationPolygon.setTitle(name);
        BoundingBox bb = null;
        if (polygon != null) {
            mDestinationPolygon.setPoints(polygon);
            bb = BoundingBox.fromGeoPoints(polygon);
        }
        if (location != -1)
            mapOverlays.set(location, mDestinationPolygon);
        else
            mapOverlays.add(1, mDestinationPolygon); //insert just above the MapEventsOverlay.
        if (polygon.size() > 0) {
            setViewOn(bb);
        }
        map.invalidate();
    }

    void setViewOn(BoundingBox bb) {
        if (bb != null) {
            map.zoomToBoundingBox(bb, true);
        }
    }


    void loadMapWithPolygon(String trackId) {
        //record in the track the current location at current time:
        ArrayList<GeoPoint> geoPoints = new ArrayList<>();
        ArrayList<Double> lats = new ArrayList<>();
        ArrayList<Double> lons = new ArrayList<>();
        LocationBeanList locationBeanList = SharedPreferenceUtils.getInstance(getApplicationContext()).getLocationBeanList(trackId);
        if (locationBeanList != null) {
            for (plot locationBean : locationBeanList.getLocationBean()) {
                GeoPoint currentLocation = new GeoPoint(locationBean.getLat(), locationBean.getLng());
                lats.add(locationBean.getLat());
                lons.add(locationBean.getLng());
                geoPoints.add(currentLocation);
                updateUIWithPolygon(geoPoints, trackId);
            }
        }
        if (lats.size() > 0) {
            double lTotalArea = Utils.getArea(lats, lons);
            if (lTotalArea >= 0) {
                lTotalArea = lTotalArea / 10000;
               // lTotalArea = Math.round(lTotalArea * 100.0) / 100.0;
                lTotalArea = roundArea(lTotalArea,2);
                areaInfo.setText(getResources().getString(R.string.area_info, String.valueOf(lTotalArea)));
            }
        }
        updateUIWithPolygon(geoPoints, trackId);
    }

    void savePolygonToSharedPreference() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
        TextView tvTitle= dialogView.findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.save_polygon_alertdialog_title, currentTrackname));
        tvMsg.setText(getResources().getString(R.string.save_polygon_alertdialog, currentTrackname));
        Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
        btnNo.setText(R.string.close);
        btnYes.setText(R.string.save_button_text);
        AlertDialog alert = builder.create();

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();

                LocationBeanList lLocationBeanList = new LocationBeanList();
                lLocationBeanList.setLocationBean(mLocationBeanList);

                String plotGeoJson = SharedPreferenceUtils.getInstance(getApplicationContext()).setLocationBeanList(currentTrackname, lLocationBeanList);

                String plotId = currentTrackname.toLowerCase().replace(" ", "").toString().concat("_" + mOlamFarmerId);
                String plotName = txtPlotName.getText().toString();
                savePlotGeoDataJsonToDatabase(plotGeoJson, plotId, plotName);
                JSONObject jObj=new JSONObject();
                try {
                    String encodedPlotsJson = Base64.encodeToString(plotGeoJson.getBytes(), Base64.NO_WRAP);
                    jObj.put(currentTrackname,encodedPlotsJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    collectGPSPoint(jObj.toString());
                }catch (Exception e){}
                try {
                    String plotallLocations = prepareJsonServerSync();
                    saveToSyncDatabase(plotallLocations, plotId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //List<LocationBeanList> list = SharedPreferenceUtils.getInstance(getApplicationContext()).getAllLocations();

                showToastMessage(getResources().getString(R.string.saved_successfully_msg, currentTrackname));
                mLocationBeanList = new ArrayList<>();
                bttnSave.setEnabled(false);
                disableBtn(bttnSave);
                if (checkPlotEntryExistInPreference()) {
                    initializeKMLDocument();
                    loadMapWithPolygon(currentTrackname);
                }
            }
        });

        alert.show();

    }


    private void collectGPSPoint(String plotDetails) {
                if (mCapturedLocation!=null) {
                    TrackDetailsDTO trackDetailsDTO = new TrackDetailsDTO();
                    trackDetailsDTO.setModule(GPSConstants.MODULE_CAPTURE_FARM_POLYGON);
                    trackDetailsDTO.setLatitude(String.valueOf(mCapturedLocation.getLatitude()));
                    trackDetailsDTO.setLongitude(String.valueOf(mCapturedLocation.getLongitude()));
                    trackDetailsDTO.setAccuracy(String.valueOf(mCapturedLocation.getAccuracy()));
                    trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
                    trackDetailsDTO.setFarmerId(String.valueOf(mFarmerId));
                    trackDetailsDTO.setFarmerName(mFarmerName);
                    trackDetailsDTO.setPolygonPlotDetails(plotDetails);

                    ContentValues geoContentValues = new GPSUtils().getGeoContentValues(trackDetailsDTO);
                    GpsTrackerLogic gpsTrackerLogic = new GpsTrackerLogic();
                    gpsTrackerLogic.insertGeoDataDetails(RecordPolygonActivity.this, geoContentValues);
                }

    }

    /**
     * Save plot geo data to table with plotgeo json and plot name
     *
     * @param plotGeoJson plot geo data json
     * @param plotName    plot name is unique name for identifying plot
     */
    void savePlotGeoDataJsonToDatabase(String plotGeoJson, String plotName, String enteredPlotName) {
        DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        if (plotGeoJson != null) {
            //plotGeoJson = Base64.encodeToString(plotGeoJson.getBytes(), Base64.NO_WRAP);

            if (plotName != null && plotGeoJson != null) {
                dl.updatePlotGeoData(mOlamFarmerId, plotName, plotGeoJson,enteredPlotName, mFarmerId);
                dl.updatePlottedName(selectedPlotId,plotName,mFarmerId);
            }
        }
    }

    /**
     * prepare JSON to sync with server based on server require format. It is collecting require data from plot geo data table and prepare json.
     * <p>
     * Prepare JSON to sync with server based on server require format.
     * It is collecting require data from plot geo data table and prepare json.
     *
     * @return JSON server require format
     * @throws JSONException
     */
    String prepareJsonServerSync() throws JSONException {

        DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        Cursor c = dl.getPlotGeoData();

        JSONObject location = new JSONObject();

        if (c.getCount() > 0) {
            c.moveToFirst();

            JSONArray plotsArray = new JSONArray();

            for (int i = 0; i < c.getCount(); i++) {
                String farmId = c.getString(c.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_FARM_ID));

                String syncFlag = c.getString(c.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_SYNC_FLAG));

                if (farmId.equalsIgnoreCase(mOlamFarmerId) && syncFlag.equals("false")) {
                    JSONObject plotsObj = new JSONObject(c.getString(c.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_JSON)));
                    plotsObj.putOpt("plotid", c.getString(c.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME)));
                    plotsObj.putOpt("plot_name", c.getString(c.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_ENTEREDPLOT_NAME)));
                    plotsArray.put(plotsObj);
                }

                c.moveToNext();
            }

            JSONObject plotsObj = new JSONObject();
            plotsObj.put("plots", plotsArray);

            JSONArray locArray = new JSONArray();
            locArray.put(plotsObj);

            location.put("location", locArray);
        }

        return location.toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    void saveToSyncDatabase(String plotsJson , String plotId) {
        DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        if (plotsJson != null) {
            plotsJson = Base64.encodeToString(plotsJson.getBytes(), Base64.NO_WRAP);
            dl.updateFarmPolygon(mFarmerId, plotsJson, mOlamFarmerId, mFarmerName, plotId);
        }
    }

    String getPlotsJsonFromLocationBeanList(List<LocationBeanList> aLocationBeanList, String plotId) {
        PlotsList lPlotList = new PlotsList();
        List<location> lplotsList = new ArrayList<>();
        List<plots> lposition_dataList = new ArrayList<>();
        location lplots = new location();
        for (int i = 0; i < aLocationBeanList.size(); i++) {
            List<plot> lplotList = new ArrayList<>();
            plots lposition_data = new plots();
            for (plot bean : aLocationBeanList.get(i).getLocationBean()) {
                lplotList.add(bean);
            }
            lposition_data.setPlot(lplotList);
            lposition_data.setPlotId(plotId);
            lposition_dataList.add(lposition_data);
        }
        lplots.setPlots(lposition_dataList);
        lplotsList.add(lplots);
        lPlotList.setLocation(lplotsList);
        String json = lPlotList == null ? null : new Gson().toJson(lPlotList);
        return json;
    }

    void showPlotLocationConfirmDialog(final String aPlotName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvTitle=(TextView)dialogView.findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.confirm);
        TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText(getResources().getString(R.string.plot_location_confirmation, aPlotName));
        Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
        Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
        AlertDialog alert = builder.create();
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToastMessage(getResources().getString(R.string.logging_not_started_msg));
                alert.dismiss();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bttnSave.setEnabled(false);
                disableBtn(bttnSave);
                setLoggingStarted(true);
                setButtonsLoggingState(true);
                initializeKMLDocument();
                myLocationOverlay.setEnabled(true);
                currentLoggingTrackSpinnerPosition = spinnerPlotList.getSelectedItemPosition();
                alert.dismiss();
            }
        });

        alert.show();
    }

    void showToastMessage(String aMessage) {
        Toast.makeText(getApplicationContext(), aMessage,
                Toast.LENGTH_SHORT).show();
    }

    private void showZeroAreaDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText(getResources().getString(R.string.zero_area_msg));
        Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
        btnNo.setVisibility(View.GONE);
        Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
        btnYes.setText(getResources().getString(R.string.msg_ok));
        AlertDialog alert = builder.create();

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaInfo.setText("");
                alert.dismiss();
            }
        });
        alert.show();



    }

   /* public void showEditPlotsDialog(View view) {
        final Dialog dialog = new Dialog(RecordPolygonActivity.this);
        dialog.setContentView(R.layout.number_picker_dialog);
        dialog.setTitle(R.string.edit_plot_dialog_title);

        final NumberPicker numberPicker = dialog.findViewById(R.id.numberPicker);
        Button buttonSave = dialog.findViewById(R.id.buttonSave);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        TextView existingPlotCount = dialog.findViewById(R.id.existingPlotCount);
        TextView existingPlotsLogged = dialog.findViewById(R.id.existingPlotsLogged);


        existingPlotCount.setText(getResources().getString(R.string.existing_plots, lPlotCount));
        existingPlotsLogged.setText(getResources().getString(
                R.string.existing_plots_logged, getTotalLoggedPlotCount()));
        numberPicker.setMaxValue(1000); // max value 1000
        if (getTotalLoggedPlotCount() == 0)
            numberPicker.setMinValue(getTotalLoggedPlotCount() + 1);
        else
            numberPicker.setMinValue(getTotalLoggedPlotCount());
        numberPicker.setValue(lPlotCount);
        numberPicker.setWrapSelectorWheel(false);
        //numberPicker.setOnValueChangedListener(this);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_SAVE_PLOT_COUNT,  ScreenNameConstants.BUTTON_CLICKED);
                lPlotCount = numberPicker.getValue();
                spinnerArray = (String[]) resizeArray(spinnerArray, lPlotCount);
                for (int i = 0; i < lPlotCount; i++) {
                    spinnerArray[i] = getResources().getString(R.string.plot, (i + 1));
                }
                spinnerArrayAdapter = new ArrayAdapter<String>
                        (RecordPolygonActivity.this, android.R.layout.simple_spinner_item,
                                spinnerArray);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                        .simple_spinner_dropdown_item);
                spinnerPlotList.setAdapter(spinnerArrayAdapter);
                SharedPreferenceUtils.getInstance(getApplicationContext()).setPlotCount("" + mOlamFarmerId, lPlotCount);
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CANCEL_PLOT_COUNT,  ScreenNameConstants.BUTTON_CLICKED);
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/

    public void showEditPlotsDialog(View v) {
//        if(! mIsLoggingStarted) {
            DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);

            AlertDialog.Builder builder = new AlertDialog.Builder(RecordPolygonActivity.this);
            builder.setCancelable(false);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.plot_count_alert_dialog, null);
            builder.setView(dialogView);
            TextView tvMsg = (TextView) dialogView.findViewById(R.id.tv_msg);
            TextView tvTitle = (TextView) dialogView.findViewById(R.id.tv_title);
            LinearLayout tvAddPlot = (LinearLayout) dialogView.findViewById(R.id.addPlot);
            plotList = (LinearLayout) dialogView.findViewById(R.id.plotList);
//            EditText edTxtNumberOfPlots = (EditText) dialogView.findViewById(R.id.et_number_plot);
            tvTitle.setText(this.getResources().getString(R.string.record_polygon_input_title));
            tvMsg.setText(this.getResources().getString(R.string.record_polygon_input_messege));
            Button btnNo = (Button) dialogView.findViewById(R.id.btn_no);
            ImageButton plotClose = (ImageButton) dialogView.findViewById(R.id.plotClose);
            Button btnYes = (Button) dialogView.findViewById(R.id.btn_yes);
            AlertDialog alert = builder.create();
            alert.show();
            alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            loadPlotLayout(inflater);

            tvAddPlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout plotLayout = (LinearLayout) inflater.inflate(R.layout.add_plot, plotList, false);
                    plotList.addView(plotLayout);
                    TextView plotCount = plotLayout.findViewById(R.id.plotCount);
                    EditText plotName = plotLayout.findViewById(R.id.plotName);
                    LinearLayout plotDelete = plotLayout.findViewById(R.id.deletePlot);
                    Cursor plotIdsSynced = dl.syncedPlotIds(String.valueOf(mFarmerId));
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
                    ArrayList<String> plotNames = getAllPlotValues();
                    dl.insertNewPlot(String.valueOf(mFarmerId), plotNames);
                    /*String lPlotCountString = "1";
                    int lPlotCount = 0;
                    if (!TextUtils.isEmpty(lPlotCountString)) {
                        lPlotCount = Integer.parseInt(lPlotCountString);
                    }*/
                    int lPlotCount = plotNames.size();
                    if (lPlotCount > 0) {
                        spinnerArray = (String[]) resizeArray(spinnerArray, lPlotCount);
                        for (int i = 0; i < lPlotCount; i++) {
                            spinnerArray[i] = getResources().getString(R.string.plot, (i + 1));
                        }
                        spinnerArrayAdapter = new ArrayAdapter<String>
                                (RecordPolygonActivity.this, android.R.layout.simple_spinner_item,
                                        spinnerArray);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                                .simple_spinner_dropdown_item);
                        spinnerPlotList.setAdapter(spinnerArrayAdapter);
                        SharedPreferenceUtils.getInstance(getApplicationContext()).setPlotCount("" + mOlamFarmerId, lPlotCount);
                        alert.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.valid_count_msg, Toast.LENGTH_LONG).show();
                    }
                }
            });

//        }else{
//            showToastMessage("Logging in progess! You can't edit plot data");
//        }
    }

    public String loadPlotName(int position){
        String name = "Plot 0";
        position = position + 1;
        DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        Cursor plotIds = dl.fetchAllPlotIds(String.valueOf(mFarmerId));
        if(plotIds.getCount() > 0) {
            int size = plotIds.getCount();
            for (int i = 1; i <= size; i++) {
                plotIds.moveToNext();
                if(position == i){
                    name = plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME));
                    selectedPlotId = plotIds.getInt(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_ID));
                    txtPlotName.setText(name);
                }
            }
        }
        return name;
    }

    public boolean isSyncedPlot(int position){
        boolean status = false;
        position = position + 1;
        DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        Cursor plotIds = dl.fetchAllPlotIds(String.valueOf(mFarmerId));
        if(plotIds.getCount() > 0) {
            int size = plotIds.getCount();
            for (int i = 1; i <= size; i++) {
                plotIds.moveToNext();
                if(position == i){
                   String isSynced = plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_IS_SYNCED));
                   if(isSynced.equalsIgnoreCase("1")){
                       status = true;
                   }
                }
            }
        }
        return status;
    }

    public int getPlotId(int position){
        int status = 0;
        position = position + 1;
        DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        Cursor plotIds = dl.fetchAllPlotIds(String.valueOf(mFarmerId));
        if(plotIds.getCount() > 0) {
            int size = plotIds.getCount();
            for (int i = 1; i <= size; i++) {
                plotIds.moveToNext();
                if(position == i){
                    status = plotIds.getInt(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_ID));

                }
            }
        }
        return status;
    }

    public void loadPlotLayout(LayoutInflater inflater) {
        DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
        plotList.removeAllViews();
        Cursor plotIds = dl.fetchAllPlotIds(String.valueOf(mFarmerId));
        if(plotIds.getCount() > 0){
            int size = plotIds.getCount();
            for (int i = 1; i <= size; i++) {
                plotIds.moveToNext();
                LinearLayout plotLayout = (LinearLayout) inflater.inflate(R.layout.add_plot, plotList, false);
                plotList.addView(plotLayout);
                TextView plotCount = plotLayout.findViewById(R.id.plotCount);
                EditText plotName = plotLayout.findViewById(R.id.plotName);
                LinearLayout plotDelete = plotLayout.findViewById(R.id.deletePlot);
                plotDelete.setTag("Plot "+i);
                Cursor plotIdsSynced = dl.syncedPlotIds(String.valueOf(mFarmerId));
                int totalPlotCount = plotIdsSynced.getCount() + i;
                plotCount.setText("Plot " + i);
                String name = plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME));
                String isSynced = plotIds.getString(plotIds.getColumnIndex(OFISDBContract.PlotEntry.COLUMN_IS_SYNCED));
                if(! name.equalsIgnoreCase("null")){
                    plotName.setText(name);
                    if (isSynced.equalsIgnoreCase("1")){
                        plotName.setEnabled(false);
                        plotName.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ofis_list_item_disabled_text));
                    }else {
                        plotName.setEnabled(true);
                    }
                }
                int count = getTotalLoggedPlotCount() + 1;
                /*if(i == 1 || i <= count){
                    plotDelete.setVisibility(View.INVISIBLE);
                }else{
                    plotDelete.setVisibility(View.VISIBLE);
                }*/
                plotDelete.setVisibility(View.VISIBLE);
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
            LinearLayout plotDelete = plotLayout.findViewById(R.id.deletePlot);
            plotDelete.setTag("Plot "+1);
            plotDelete.setVisibility(View.GONE);
            bindPlotDelete(plotDelete);
        }
    }

    private void bindPlotDelete(LinearLayout trashbutton) {
        trashbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout numberrow = (LinearLayout) v.getParent();
                String plotNumber = (String) v.getTag();
                DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
                String plotName = plotNumber.toLowerCase().replace(" ", "").toString().concat("_" + mOlamFarmerId);

                Matcher matcherPlotNumber = Pattern.compile("Plot\\s+(\\d+)").matcher(plotNumber);
                String plotId = null;
                if (matcherPlotNumber.find()){
                    plotId = matcherPlotNumber.group(1);
                }
//                String plotId = plotNumber.substring(plotNumber.length() - 1);
                Cursor c =dl.getPNamePlotGeoData(mOlamFarmerId);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    for (int i = 1; i <= c.getCount(); i++) {
                        String pName = c.getString(c.getColumnIndex(OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME));
//                        String [] pID = pName.split("_");
//                        String pNameId = pID[0].substring(pID[0].length() - 1);
                        Pattern pattern = Pattern.compile("plot([^_]+)_");
                        Matcher matcher = pattern.matcher(pName);

                        String pNameId = null;
                        if (matcher.find()) {
                            pNameId = matcher.group(1);
                        }

                        if (pNameId.equals(plotId)){
                            dl.deleteDatafromGeoPlot(plotName);
                            SharedPreferenceUtils.getInstance(getApplicationContext()).removePlotUsingKey(plotNumber);
                           if (dl.getPNamePlotGeoData(mOlamFarmerId).getCount() == 0){
                               dl.deleteNetworkQueuePlotItemByFarmerId(mFarmerId);
                           }
                        }

                        if (Integer.parseInt(pNameId) > Integer.parseInt(plotId)){
                            //decrementing saved shared prefrences
                            LocationBeanList beans = SharedPreferenceUtils.getInstance(getApplicationContext()).getLocationBeanList("Plot " + pNameId);
                            SharedPreferenceUtils.getInstance(getApplicationContext()).removePlotUsingKey("Plot " + pNameId);
                            int finalPlot = Integer.parseInt(pNameId) - 1;
                            SharedPreferenceUtils.getInstance(getApplicationContext()).setLocationBeanList("Plot " + finalPlot, beans);

                            // updating db
                            dl.updateGeoPlotDataAfterDelete(pName , Integer.parseInt(pNameId)-1 , mOlamFarmerId);
                        }


                        c.moveToNext();

                    }
                }
                ((ViewGroup) numberrow.getParent()).removeView(numberrow);
                ArrayList<String> plotNames = getAllPlotValues();
                dl.insertNewPlot(String.valueOf(mFarmerId),plotNames);
                LayoutInflater inflater = getLayoutInflater();
                loadPlotLayout(inflater);
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

    private static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(
                elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0)
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        return newArray;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    private void stopUpdate() {
        getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);

    }

    private boolean startUpdate() {
        logger.addRecordToLog("startLocationUpdates");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(new GPSTracker(this).getLocationMode());
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
        logger.addRecordToLog("INTERVAL :" + INTERVAL +"\n" +
                "FASTEST_INTERVAL :" + FASTEST_INTERVAL +"\n" +
                "Priority: " +  new GPSTracker(this).getLocationMode() +"\n" +
                "SmallestDisplacement: " +    DISPLACEMENT );
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            logger.addRecordToLog("getFusedLocationProviderClient");
            getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            return true;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_VIEW_FARMER_ACTIVITY, ScreenNameConstants.SCREEN_RECORD_POLYGON_ACTIVITY, ScreenNameConstants.SCREEN_RECORD_PLOT_POLYGON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_other, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_home) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        if (id == R.id.action_tasks) {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

   private double calculateArea() {
        //record in the track the current location at current time:

       LocationBeanList lLocationBeanList = new LocationBeanList();
       lLocationBeanList.setLocationBean(mLocationBeanList);

        ArrayList<GeoPoint> geoPoints = new ArrayList<>();
        ArrayList<Double> lats = new ArrayList<>();
        ArrayList<Double> lons = new ArrayList<>();
       // LocationBeanList locationBeanList = SharedPreferenceUtils.getInstance(getApplicationContext()).getLocationBeanList(trackId);

        if (mLocationBeanList != null) {
            for (plot locationBean : mLocationBeanList) {
                GeoPoint currentLocation = new GeoPoint(locationBean.getLat(), locationBean.getLng());
                lats.add(locationBean.getLat());
                lons.add(locationBean.getLng());
                geoPoints.add(currentLocation);
            }
        }
        double lTotalArea = 0;
        if (lats.size() > 0) {
             lTotalArea = Utils.getArea(lats, lons);
            if (lTotalArea >= 0) {
                lTotalArea = lTotalArea / 10000;
                // Area rounded in 3 digits
                areaWithoutRounded=roundArea(lTotalArea,3);
                lTotalArea = roundArea(lTotalArea,2);
            }
        }
        return lTotalArea;
    }

    public static double roundArea(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    private void getFarmLocation() {
        double farmLatitude = db.polygonHistoryDataDao().getLattitude(mFarmerId);
        double farmLongitude = db.polygonHistoryDataDao().getLongitude(mFarmerId);

        // Create a GeoPoint from the last known location
        GeoPoint farmLocation = new GeoPoint(farmLatitude, farmLongitude);

        // Create a Marker for the current location
        farmLocationMarker = new Marker(map);
        farmLocationMarker.setPosition(farmLocation);
        farmLocationMarker.setTitle("Farm Location");
        farmLocationMarker.setSnippet("Latitude: " + farmLatitude + "\nLongitude: " + farmLongitude); // Set the snippet with lat and long
        farmLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        farmLocationMarker.setIcon(ContextCompat.getDrawable(RecordPolygonActivity.this, R.drawable.location_mark_purple));
        map.getOverlays().add(farmLocationMarker);

        farmLocationMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
//                            Toast.makeText(PolygonHistory.this, marker.getTitle() + "\n" + marker.getSnippet(), Toast.LENGTH_SHORT).show();
                View dialogView = getLayoutInflater().inflate(R.layout.custom_location_dialog, null);

                // Find views in the dialog layout
                TextView textLatitude = dialogView.findViewById(R.id.textLatitude);
                TextView textLongitude = dialogView.findViewById(R.id.textLongitude);
                TextView farmerName = dialogView.findViewById(R.id.enumName);

                farmerName.setText("Name: " + mFarmerName);
                textLatitude.setText("Latitude: " + farmLatitude);
                textLongitude.setText("Longitude: " + farmLongitude);

                AlertDialog.Builder builder = new AlertDialog.Builder(RecordPolygonActivity.this);
                builder.setView(dialogView)
                        .setTitle(marker.getTitle())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

                 new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Dismiss the dialog after 15 seconds
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }, 7000); // 7 seconds in milliseconds
                return true;
            }
        });
    }

    private void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           return;
        }

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Use the last known location
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);

//                    // Get a reference to the MapView
//                    MapView mapView = (MapView) findViewById(R.id.mapView);

                    // Create a GeoPoint from the last known location
                    GeoPoint currentLocation = new GeoPoint(latitude, longitude);
                    if (currentLocationMarker != null) {
                        map.getOverlays().remove(currentLocationMarker);
                    }
                    // Create a Marker for the current location
                    currentLocationMarker = new Marker(map);

                    currentLocationMarker.setPosition(currentLocation);
                    currentLocationMarker.setTitle("Enumerator Current Location");
                    currentLocationMarker.setSnippet("Latitude: " + latitude + "\nLongitude: " + longitude); // Set the snippet with lat and long
                    currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    currentLocationMarker.setIcon(ContextCompat.getDrawable(RecordPolygonActivity.this, R.drawable.location_mark));
                    map.getOverlays().add(currentLocationMarker);

                    // Set a marker click listener
                    currentLocationMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
//                            Toast.makeText(PolygonHistory.this, marker.getTitle() + "\n" + marker.getSnippet(), Toast.LENGTH_SHORT).show();
                            View dialogView = getLayoutInflater().inflate(R.layout.custom_location_dialog, null);

                            // Find views in the dialog layout
                            TextView textLatitude = dialogView.findViewById(R.id.textLatitude);
                            TextView textLongitude = dialogView.findViewById(R.id.textLongitude);
                            TextView enumName = dialogView.findViewById(R.id.enumName);

                            String enumerator_id = Utilities.getFromSharedPrefs(getApplicationContext());
                            String enumerator_name = Utilities.getEnumeratorNameSharedPrefs(getApplicationContext());
                            // Set the latitude and longitude in the dialog
                            enumName.setText("Name: " + enumerator_name);
                            textLatitude.setText("Latitude: " + latitude);
                            textLongitude.setText("Longitude: " + longitude);

                            // Create and show the dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(RecordPolygonActivity.this);
                            builder.setView(dialogView)
                                    .setTitle(marker.getTitle())
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Dismiss the dialog if OK is clicked
                                            dialog.dismiss();
                                        }
                                    });

                            AlertDialog dialog = builder.create();
                            dialog.show();

                            // Use a Handler to delay the dismissal of the dialog
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Dismiss the dialog after 15 seconds
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                }
                            }, 7000); // 7 seconds in milliseconds
                            return true;
                        }
                    });

                    // Zoom the MapView to the current location
//                    map.getController().animateTo(currentLocation);
//                    map.getController().setZoom(20.0);
                } else {
                    // No last known location is available
                    Log.d("Location", "No last known location available.");
                }
            }
        });
    }

    private void PolygonHistoryApiCall(Integer farmerId, int position) {
        ManageRecords getPolygonHistoryData = RetrofitClient.getRetrofit().create(ManageRecords.class);
        retrofit2.Call<PolygonHistoryResponseModel> call = getPolygonHistoryData.getAllPolygonHistoryResponse(farmerId , 1);

        call.enqueue(new retrofit2.Callback<PolygonHistoryResponseModel>() {

            @Override
            public void onResponse(Call<PolygonHistoryResponseModel> call, Response<PolygonHistoryResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProgressDialog pd = new ProgressDialog(RecordPolygonActivity.this);
                    pd.setCancelable(false);
                    pd.setIndeterminate(false);
                    pd.setMessage(getResources().getString(R.string.fetching_data));
                    pd.show();
                    try {
                        db = OFISRoomDB.getDbInstance(getApplicationContext());
                        if (response.body() != null ) {
                            PolygonHistoryResponseModel outputData = response.body();
                            db.polygonHistoryDataDao().deleteAll();
                            db.polygonPlotHistoryDao().deleteAll();
                            if (outputData != null) {
                                PolygonHistoryDataList polygonHistoryEntity = convertToEntity(outputData);
                                // Insert into Room database
//                                db.polygonHistoryDataDao().insertAll(polygonHistoryEntity);

                                PolygonHistoryData data = new PolygonHistoryData();
                                data.farmerName = polygonHistoryEntity.getFarmerName();
                                data.olamFarmerId = polygonHistoryEntity.getOlamFarmerId();
                                data.primaryLat = polygonHistoryEntity.getPrimaryLat();
                                data.primaryLng = polygonHistoryEntity.getPrimaryLng();
                                data.farmerId = polygonHistoryEntity.getFarmerId();
                                data.farmerGroupId = polygonHistoryEntity.getFarmerGroupId();
                                data.farmId = polygonHistoryEntity.getFarmId();
                                data.farmCode = polygonHistoryEntity.getFarmCode();
                                data.farmName = polygonHistoryEntity.getFarmName();
                                db.polygonHistoryDataDao().insert(data);

                                ArrayList<PolygonPlotHistory> polygonPlotHistories = polygonHistoryEntity.getPlot();
                                if (polygonPlotHistories != null && !polygonPlotHistories.isEmpty()) {
                                    for (PolygonPlotHistory polygonPlotHistories1 : polygonPlotHistories) {
                                        db.polygonPlotHistoryDao().insert(polygonPlotHistories1);
                                    }
                                }

                            }
                            int plotId = getPlotId(position);
                            plotGraphInMap(plotId);
                            getFarmLocation();
                            getLastLocation();
                        }else {
                            try {
                                Log.d("FailureMessage", "Error response body: " + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pd.dismiss();
                        }
                    }, 2000);

                }else {
                    try {
                        Log.d("FailureMessage", "Error response body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<PolygonHistoryResponseModel> call, Throwable t) {
                Log.d("TAGFailure", "Network request failed", t);
            }
        });
    }

    private static PolygonHistoryDataList convertToEntity(PolygonHistoryResponseModel responseModel) {
        PolygonHistoryDataList entity = new PolygonHistoryDataList();
        entity.setFarmerName(responseModel.getFarmerName());
        entity.setOlamFarmerId(responseModel.getOlamFarmerId());
        entity.setPrimaryLat(responseModel.getPrimaryLat());
        entity.setPrimaryLng(responseModel.getPrimaryLng());
        entity.setFarmerId(responseModel.getFarmerId());
        entity.setFarmerGroupId(responseModel.getFarmerGroupId());
        entity.setFarmId(responseModel.getFarmId());
        entity.setFarmCode(responseModel.getFarmCode());
        entity.setFarmName(responseModel.getFarmName());
        entity.setPlot(responseModel.getPlot());

        return entity;
    }

    private void plotGraphInMap(long id){
        String jsonString = db.polygonPlotHistoryDao().getPositionData(id);
        ArrayList<GeoPoint> plotPoints = PlotDataParser.parsePlotData(jsonString);
        IMapController mapController = map.getController();
        mapController.setZoom(21);

        if (!plotPoints.isEmpty()) {
            if (polygon != null) {
                map.getOverlays().remove(polygon);
            }

            polygon = new Polygon();
            polygon.setFillColor(Color.GREEN);
            polygon.setStrokeColor(Color.BLACK);
            polygon.setStrokeWidth(2);
            polygon.setPoints(plotPoints);
            map.getOverlays().add(polygon);

            // Set map center to the first point
            mapController.setCenter(plotPoints.get(0));
        }
    }

    private void editPlotAlertDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText(getResources().getString(R.string.edit_already_logged_plot, currentTrackname));
        Button btnNo = (Button)dialogView.findViewById(R.id.btn_no);
        btnNo.setText(getResources().getString(R.string.no));
        Button btnYes = (Button)dialogView.findViewById(R.id.btn_yes);
        btnYes.setText(getResources().getString(R.string.yes));
        AlertDialog alert = builder.create();

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String plotName = currentTrackname.toLowerCase().replace(" ", "").toString().concat("_" + mOlamFarmerId);
                SharedPreferenceUtils.getInstance(getApplicationContext()).removePlotUsingKey(currentTrackname);
                if (polygon != null) {
                    map.getOverlays().remove(polygon);
                }
                DataLayer dl = new DataLayer(getResources(), RecordPolygonActivity.this);
                dl.deletePlotDataFromNetworkQueue(plotName);
                initiateLogging();

                alert.dismiss();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });
        alert.show();



    }

}