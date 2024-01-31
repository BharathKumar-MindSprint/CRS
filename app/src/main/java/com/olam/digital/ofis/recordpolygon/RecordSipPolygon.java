package com.olam.digital.ofis.recordpolygon;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.recordpolygon.polygonhistory.PlotDataParser;

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
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

public class RecordSipPolygon extends AppCompatActivity {

    private String category_name;

    private ImageView recordingStatus;
    private String sip_name;

    private int category_sip_id;

    private static final String CATEGORY_NAME = "MODULE_NAME";
    private static final String SIP_NAME = "SIP_NAME";
    private static final String SIP_CATEGORY_ID = "SIP_CATEGORY_ID";

    TextView category_name_sip, name_sip;
    Button start_stop_recording_sip, pause_recording_sip, bttnSave;
    static int currentLoggingTrackSpinnerPosition = 0;

    protected LocationManager mLocationManager;
    OFISRoomDB db;

    protected GeoPoint startPoint, destinationPoint;
    protected ArrayList<GeoPoint> viaPoints;
    protected FolderOverlay mItineraryMarkers;
    TextView areaInfo;
    Logger logger;
    protected MapView map;
    Polygon polygon;
    OnlineTileSourceBase MAPBOXSATELLITELABELLED;
    protected DirectedLocationOverlay myLocationOverlay;
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
    private Location mCapturedLocation=null;
    private final NetworkLocationIgnorer mNetworkLocationIgnorer = new NetworkLocationIgnorer();

    long mLastTime = 0; // milliseconds
    double mSpeed = 0.0; // km/h

    String currentTrackname = "OFIS Track";
    float mAzimuthAngleSpeed = 0.0f;

    protected boolean mTrackingMode;

    boolean mIsLoggingStopped;
    boolean mIsLoggingStarted;
    boolean mIsLoggingPaused;
    boolean mIsGPSLocationAvailable;

    private double areaWithoutRounded=0;

    protected FolderOverlay mRoadNodeMarkers;
    static final int OSRM = 0;

    protected FolderOverlay mKmlOverlay; //root container of overlays from KML reading
    public static KmlDocument mKmlDocument; //made static to pass between activities
    public static Stack<KmlFeature> mKmlStack; //passed between activities, top is the current KmlFeature to edit.
    public static KmlFolder mKmlClipboard; //passed between activities. Folder for multiple items selection.

    boolean mIsRecordingTrack = false;
    ImageButton mTrackingModeButton;
    private String plotsToGps;
    private String plotsRecentGps;

    public static final int GRANTED = 0;
    public static final int DENIED = 1;
    public static final int BLOCKED_OR_NEVER_ASKED = 2;

    private enum RecordingState {
        RECORDING, PAUSED, STOPPED
    }
    List<plot> mLocationBeanList = new ArrayList<>();
    private  final String MATOMOTAG = this.getClass().getSimpleName();
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sip_polygon);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Configuration.getInstance().setOsmdroidBasePath(new File(Environment.getExternalStorageDirectory(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(Environment.getExternalStorageDirectory(), "osmdroid/tiles"));

        boolean hwAccelerationOK = true;
        Configuration.getInstance().setMapViewHardwareAccelerated(hwAccelerationOK);

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



        category_name_sip = (TextView) findViewById(R.id.category_name_sip);
        name_sip = (TextView) findViewById(R.id.name_sip);
        bttnSave = (Button) findViewById(R.id.save_recording_sip);
        recordingStatus = (ImageView) findViewById(R.id.recording_status_sip);
        areaInfo = (TextView) findViewById(R.id.areaInfo_sip);

        category_name = getIntent().getStringExtra(CATEGORY_NAME);
        sip_name = getIntent().getStringExtra(SIP_NAME);
        category_sip_id = getIntent().getIntExtra(SIP_CATEGORY_ID,0);


        category_name_sip.setText(category_name);
        name_sip.setText(sip_name);


        SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        MAPBOXSATELLITELABELLED = new MapBoxTileSource("MapBoxSatelliteLabelled", 1, 19, 256, ".png");
        ((MapBoxTileSource) MAPBOXSATELLITELABELLED).retrieveAccessToken(this);
        ((MapBoxTileSource) MAPBOXSATELLITELABELLED).retrieveMapBoxMapId(this);
        TileSourceFactory.addTileSource(MAPBOXSATELLITELABELLED);


        map = (MapView) findViewById(R.id.map_sip);

        map.setTilesScaledToDpi(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getOverlayManager().getTilesOverlay().setColorFilter(null);
        mapController.setZoom(17);


        start_stop_recording_sip = (Button) findViewById(R.id.start_stop_recording_sip);
        pause_recording_sip = (Button) findViewById(R.id.pause_recording_sip);
        bttnSave = (Button) findViewById(R.id.save_recording_sip);

        pause_recording_sip.setEnabled(false);
        bttnSave.setEnabled(false);
        disableBtn(pause_recording_sip);
        disableBtn(bttnSave);
        start_stop_recording_sip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.addRecordToLog("mStartLoggingButton");
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_START_LOGGING,  ScreenNameConstants.BUTTON_CLICKED);
                if(pause_recording_sip.getText().toString().equalsIgnoreCase("resume")){


                    Toast.makeText(RecordSipPolygon.this, "Please remove pause state first", Toast.LENGTH_SHORT).show();

                }
                else{
                    handleStartStopLoggingButton();
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
                    if (calculateArea() >= 0.01){
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

        /*//To use MapEventsReceiver methods, we add a MapEventsOverlay:
        MapEventsOverlay overlay = new MapEventsOverlay(this);
        map.getOverlays().add(overlay);*/

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
        mTrackingModeButton = (ImageButton) findViewById(R.id.buttonTrackingMode_sip);
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

        View expander = findViewById(R.id.expander_sip);

        expander.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                View topSection = findViewById(R.id.topSectionSip);
                View saveRecording = findViewById(R.id.save_recording_sip);
                if (topSection.getVisibility() == View.VISIBLE) {
                    topSection.setVisibility(View.GONE);
                    saveRecording.setVisibility(View.GONE);
                } else {
                    topSection.setVisibility(View.VISIBLE);
                    saveRecording.setVisibility(View.VISIBLE);
                }
            }
        });

        mRoadNodeMarkers = new FolderOverlay();
        mRoadNodeMarkers.setName("Route Steps");
        map.getOverlays().add(mRoadNodeMarkers);

        mKmlOverlay = null;
        if (savedInstanceState != null) {
            //STATIC - mKmlDocument = savedInstanceState.getParcelable("kml");
            updateUIWithKml();
        }
        setStdTileProvider();

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

        try {
            DataLayer dl = new DataLayer(getResources(), RecordSipPolygon.this);
            if (dl.getSipPlot(category_sip_id) > 0){
                plotGraphInMap(category_sip_id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void plotGraphInMap(int id){
        DataLayer dl = new DataLayer(getResources(), RecordSipPolygon.this);
        String jsonString = dl.getPLotDataFromCategoryId(String.valueOf(id));
        ArrayList<GeoPoint> plotPoints = PlotDataParser.parsePlotData(jsonString);

        // Set the default map center and zoom level
        IMapController mapController = map.getController();
        mapController.setZoom(21);

        if (!plotPoints.isEmpty()) {
            if (polygon != null) {
                map.getOverlays().remove(polygon);
            }

            polygon = new Polygon();
            polygon.setFillColor(0x15FF0080);
            polygon.setStrokeColor(0x800000FF);
            polygon.setStrokeWidth(5.0f);
            polygon.setPoints(plotPoints);
            map.getOverlays().add(polygon);

            // Set map center to the first point
            mapController.setCenter(plotPoints.get(0));
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

    void setStdTileProvider() {
        if (!(map.getTileProvider() instanceof MapTileProviderBasic)) {
            MapTileProviderBasic bitmapProvider = new MapTileProviderBasic(this);
            map.setTileProvider(bitmapProvider);
        }
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

//                String plotId = currentTrackname.toLowerCase().replace(" ", "").toString().concat("_" + mOlamFarmerId);
//                String plotName = txtPlotName.getText().toString();
                saveSipPlotGeoDataJsonToDatabase(plotGeoJson, category_sip_id, category_name,sip_name);
                JSONObject jObj=new JSONObject();
                try {
                    String encodedPlotsJson = Base64.encodeToString(plotGeoJson.getBytes(), Base64.NO_WRAP);
                    jObj.put(currentTrackname,encodedPlotsJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                try {
//                    collectGPSPoint(jObj.toString());
//                }catch (Exception e){}
//                try {
//                    String plotallLocations = prepareJsonServerSync();
//                    saveToSyncDatabase(plotallLocations, plotId);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

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
//    void saveToSyncDatabase(String plotsJson , String plotId) {
//        DataLayer dl = new DataLayer(getResources(), RecordSipPolygon.this);
//        if (plotsJson != null) {
//            plotsJson = Base64.encodeToString(plotsJson.getBytes(), Base64.NO_WRAP);
//            dl.updateSipPolygon(category_sip_id, plotsJson, category_name, sip_name);
//        }
//    }

    void initializeLoggingVariables() {
        mIsLoggingStopped = false;
        mIsLoggingStarted = false;
        mIsLoggingPaused = false;
        mIsGPSLocationAvailable = false;
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
        /*if (lats.size() > 0) {
            double lTotalArea = Utils.getArea(lats, lons);
            if (lTotalArea >= 0) {
                lTotalArea = lTotalArea / 10000;
                // lTotalArea = Math.round(lTotalArea * 100.0) / 100.0;
                lTotalArea = roundArea(lTotalArea,2);
                areaInfo.setText(getResources().getString(R.string.area_info, String.valueOf(lTotalArea)));
            }
        }*/
        updateUIWithPolygon(geoPoints, trackId);
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

    private void collectGPSPoint(String plotDetails) {
        if (mCapturedLocation!=null) {
            TrackDetailsDTO trackDetailsDTO = new TrackDetailsDTO();
            trackDetailsDTO.setModule(GPSConstants.MODULE_CAPTURE_FARM_POLYGON);
            trackDetailsDTO.setLatitude(String.valueOf(mCapturedLocation.getLatitude()));
            trackDetailsDTO.setLongitude(String.valueOf(mCapturedLocation.getLongitude()));
            trackDetailsDTO.setAccuracy(String.valueOf(mCapturedLocation.getAccuracy()));
            trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
            trackDetailsDTO.setSipCategoryId(category_sip_id);
            trackDetailsDTO.setSipCategoryName(category_name);
            trackDetailsDTO.setSipName(sip_name);
            trackDetailsDTO.setPolygonPlotDetails(plotDetails);

            ContentValues geoContentValues = new GPSUtils().getGeoContentValues(trackDetailsDTO);
            GpsTrackerLogic gpsTrackerLogic = new GpsTrackerLogic();
            gpsTrackerLogic.insertGeoDataDetails(RecordSipPolygon.this, geoContentValues);
        }

    }

    void saveSipPlotGeoDataJsonToDatabase(String plotGeoJson, int category_sip_id, String category_name, String sip_name) {
        DataLayer dl = new DataLayer(getResources(), RecordSipPolygon.this);
        if (plotGeoJson != null) {
            //plotGeoJson = Base64.encodeToString(plotGeoJson.getBytes(), Base64.NO_WRAP);

            if (plotGeoJson != null) {
                dl.updateSipPlotGeoData(category_sip_id, category_name, plotGeoJson,sip_name);
            }
        }
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
                showToastMessage(getResources().getString(R.string.already_logged_and_saved, currentTrackname));
//                editPlotAlertDialog();
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

    boolean isGPSProviderEnabled() {
        if (mLocationManager != null) {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return true;
            }
        }
        return false;
    }
    void showToastMessage(String aMessage) {
        Toast.makeText(getApplicationContext(), aMessage,
                Toast.LENGTH_SHORT).show();
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


    void initializeKMLDocument() {
        mKmlDocument = new KmlDocument();
        mKmlStack = new Stack<KmlFeature>();
        mKmlClipboard = new KmlFolder();
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
                            DataLayer dl = new DataLayer(getResources(), RecordSipPolygon.this);
                            int farmPlotCount = dl.fetchSyncedSipPlotCountById(String.valueOf(category_sip_id));
                            if(farmPlotCount == 0) {
                                showPlotLocationConfirmDialog(currentTrackname);

                            }else{

                                // delete and re-record
                                /*int loggedPlotCount = getTotalLoggedPlotCount() ;
                                int actualPlot = Integer.parseInt(StringUtils.split(currentTrackname)[1]);
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
                                }*/
                            }
                        } else {
                            if (mLocationBeanList.size() > 0) {
                                showToastMessage(getResources().getString(R.string.ready_to_save, currentTrackname).trim().toUpperCase());
                                pause_recording_sip.setEnabled(false);
                                disableBtn(pause_recording_sip);
                                bttnSave.setEnabled(true);
                                enableSaveBtn(bttnSave);
                                setButtonsLoggingState(false);
                            } else {




                                showToastMessage(getResources().getString(R.string.location_not_yet_logged));
                                pause_recording_sip.setEnabled(false);
                                bttnSave.setEnabled(false);
                                disableBtn(pause_recording_sip);
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
//                currentLoggingTrackSpinnerPosition = spinnerPlotList.getSelectedItemPosition();
                alert.dismiss();
            }
        });

        alert.show();
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

    private void setButtonsLoggingState(boolean aIsLoggingStarted) {

        if (aIsLoggingStarted) {

            start_stop_recording_sip.setText(R.string.stop_logging_button_text);
            setLoggingStarted(true);
            setRecordingIcon(RecordingState.RECORDING);
            pause_recording_sip.setEnabled(true);
            enablePauseBtn(pause_recording_sip);

        } else {


            setLoggingStarted(false);
            setRecordingIcon(RecordingState.STOPPED);
            start_stop_recording_sip.setText(R.string.start_logging_button_text);
            pause_recording_sip.setText(R.string.pause_button_text);
            pause_recording_sip.setEnabled(false);
            disableBtn(pause_recording_sip);

        }
    }

    void setLoggingStarted(boolean isStarted) {
        mIsLoggingStarted = isStarted;
        mIsLoggingStopped = !isStarted;
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

    private void enablePauseBtn(Button btn){
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_secondary_background));
        btn.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void disableBtn(Button btn){
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_primary_disabled));
        btn.setTextColor(ContextCompat.getColor(this, R.color.btn_disabled_text));
    }

    private  void enableSaveBtn(Button btn){
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btnColorPrimary));
        btn.setTextColor(ContextCompat.getColor(this, R.color.white));
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

    boolean checkPlotEntryExistInPreference() {
        LocationBeanList LocationBeanList = SharedPreferenceUtils.getInstance(getApplicationContext()).getLocationBeanList(currentTrackname);
        if (LocationBeanList != null) {
            return true;
        }
        return false;
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
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

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

    void setGPSLocationAvailable(boolean isAvailable) {
        mIsGPSLocationAvailable = isAvailable;
        if (isAvailable) {
            //currentLocation.setImageResource(R.drawable.ic_actionbutton_location_found);
        } else {
            //currentLocation.setImageResource(R.drawable.ic_actionbutton_location_searching);
        }

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
}