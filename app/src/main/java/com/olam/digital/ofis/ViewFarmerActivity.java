package com.olam.digital.ofis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.RegisterChildActivity;
import com.olam.digital.ofis.recordpolygon.LocationBeanList;
import com.olam.digital.ofis.recordpolygon.PlotsList;
import com.olam.digital.ofis.recordpolygon.RecordPolygonActivity;
import com.olam.digital.ofis.recordpolygon.SharedPreferenceUtils;
import com.olam.digital.ofis.recordpolygon.location;
import com.olam.digital.ofis.recordpolygon.plot;
import com.olam.digital.ofis.recordpolygon.plots;
import com.olam.digital.ofis.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class ViewFarmerActivity extends BaseListActivity {

    public static long farmerId;
    private Cursor farmer;
    private Context context;
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
    private static final int PERMISSIONS_ACCESS_LOCATION = 6575;

    private RelativeLayout phoneNumbersHolder;
    private LinearLayout phonenumbers;
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

    //JIRA req OW-505
    ImageButton farmerPhotoActionButton;
    // Runtime Permission Request Code
    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_farmer);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        context = this;
        res = getResources();
        dl = new DataLayer(res, context);
        pd = new ProgressDialog(context);

        //farmerId = SharedPreferenceUtils.getInstance(getApplicationContext()).getFarmerID();

        setupGPS();
        setupUI();
        setupPhoneNumberDialog();
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
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.RESULT_CANCELED) return;
        ImageView farmerPhoto = (ImageView) findViewById(R.id.viewFarmerFarmerPhoto);
        //JIRA req OW-505

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    if(getCurrentPhotoPath() != null){
                        setFullImageFromFilePath(getCurrentPhotoPath(), farmerPhoto, "portrait");
                    }else {
                        Toast.makeText(ViewFarmerActivity.this, "Something went wrong, please try again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_IMAGE_GALLERY:
                if (resultCode == RESULT_OK) {

                    setCapturedImageURI(data.getData());
                    try {
                        if (getFilePathUri(context, getCapturedImageURI()) != null) {
                            setFullImageFromFilePath(getFilePathUri(context, getCapturedImageURI()), farmerPhoto, "portrait");
                        } else {
                            Toast.makeText(ViewFarmerActivity.this, "Something went wrong, please try again", Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception e){
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

        farmer = dl.getFarmerById(farmerId);

        if (farmer.getCount() > 0) {
            farmer.moveToFirst();
            farmerNameStr= farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
            olamFarmerIdStr=farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));
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

            JSONObject submittedModules;
            try {

                String submittedModulesJSON = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES));
                String Samplestr = String.valueOf("{\"modules\":null}");
                if(Samplestr.equals(submittedModulesJSON)){
                    submittedModulesJSON =submittedModulesJSON.replaceAll("\"modules\":null","");
                }
                submittedModules = (!submittedModulesJSON.equals("null")) ? new JSONObject(submittedModulesJSON) : new JSONObject();
                String moduleNameSuffix = Utilities.getLocaleSuffix(res);
                Cursor moduleAssignments = dl.getFarmerModules(farmerId);
                if (moduleAssignments != null && moduleAssignments.getCount() > 0) {
                    String[] adapterCols = new String[]{
                            (moduleAssignments.getColumnIndex(OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix) != -1) ? OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + moduleNameSuffix : OFISDBContract.ModuleEntry.COLUMN_NAME_NAME,
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID,
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED,
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED,
                            "farmer_module_id",
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LAT,
                            OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LNG
                    };
                    int[] adapterRowViews = new int[]{R.id.moduleListModuleName, R.id.moduleListCurrentSubModuleId, R.id.moduleListStatusStarted, R.id.moduleListStatusFinished, R.id.moduleListFarmerModuleId, R.id.moduleListRecordedLat, R.id.moduleListRecordedLng};

                    CustomListAdapter_Modules customListAdapter_modulesAdapter =
                            new CustomListAdapter_Modules(context, moduleAssignments, adapterCols, adapterRowViews, res, farmer, submittedModules);
                    setListAdapter(customListAdapter_modulesAdapter);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("ViewFarmerActivity: Error setting farmer GPS location", e));
            e.printStackTrace();
        }

        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

       //JIRA req OW-505 MY_PROFILE_PIC_PERMISSIONS_REQUEST

        if (requestCode == 6575) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFarmGPS(clickedLocationItem);
            }
        }else if(requestCode==MY_PROFILE_PIC_PERMISSIONS_REQUEST){
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

    private void recordFarmLocation(final View v) {
        pd = new ProgressDialog(ViewFarmerActivity.this);
        pd.setCancelable(false);
        pd.setIndeterminate(false);
        pd.setMessage(res.getString(R.string.fetching_location));
        pd.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(res.getString(R.string.are_you_at_the_farm))
                .setCancelable(false)
                .setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getFarmGPS(v);
                    }
                })
                .setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                if (pd != null && pd.isShowing()) {
                                    pd.dismiss();
                                }
                            }
                        }
                );
        AlertDialog alert = builder.create();
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
                                if(mCurrentLocation != null) {
                                    if (polygon) {
                                        recordFarmPolygon(v);
                                    } else {
                                        setFarmGPS(mCurrentLocation, v);
                                    }
                                }else{
                                    Toast.makeText(ViewFarmerActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ViewFarmerActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
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
                                Toast.makeText(ViewFarmerActivity.this,getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    String getPlotsJsonFromLocationBeanList(List<LocationBeanList>  aLocationBeanList) {
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
            lposition_dataList.add(lposition_data);
        }
        lplots.setPlots(lposition_dataList);
        lplotsList.add(lplots);
        lPlotList.setLocation(lplotsList);
        String json = lPlotList == null ? null : new Gson().toJson(lPlotList);
        return json;
    }

    private void enableFarmPolygon(final View v){
        pd = new ProgressDialog(ViewFarmerActivity.this);
        pd.setCancelable(false);
        pd.setIndeterminate(false);
        pd.setMessage(res.getString(R.string.fetching_location));
        pd.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(res.getString(R.string.are_you_ready_to_polygon_record))
                .setCancelable(false)
                .setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getFarmGPSPolygon(v);
                    }
                })
                .setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                if (pd != null && pd.isShowing()) {
                                    pd.dismiss();
                                }
                            }
                        }
                );
        AlertDialog alert = builder.create();
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
        /*final int lFarmID = (int) SharedPreferenceUtils.getInstance(getApplicationContext()).getFarmerID();
        final int lFarmID = (int) ViewFarmerActivity.farmerId;
        final int lFarmID = farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID));*/

        /**
         * Here we used SharedPreference to map OLAM Farmer ID with UUID.
         * Plot count value is important to showing alert to enter plots number of farmer at first time.
         */
        final int strFarmID = (int) SharedPreferenceUtils
                .getInstance(getApplicationContext())
                .getFarmerID(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
        //final int strFarmID = farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID));

       // Some farm Id is getting null.
        int count =0;
        if(strFarmID != 0){
            count = SharedPreferenceUtils.getInstance(getApplicationContext()).getPlotCount(""+strFarmID);
        }


        Log.d("ViewFarmerActivity", "Plot Count = " + count);
        if (count == 0) {
            final EditText edTxtNumberOfPlots = new EditText(context);
            edTxtNumberOfPlots.setInputType(InputType.TYPE_CLASS_NUMBER);
            edTxtNumberOfPlots.setHint(res.getString(R.string.record_polygon_input_hint));

            edTxtNumberOfPlots.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String str = String.valueOf(s);
                    if(str.length()>3){
                        int num = Integer.parseInt(str);
                        if(num >= 1001){
                            edTxtNumberOfPlots.setError("We are supporting up to max 1000 plots");
                            edTxtNumberOfPlots.findFocus();
                            edTxtNumberOfPlots.setText("");
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            new AlertDialog.Builder(context)
                    .setTitle(res.getString(R.string.record_polygon_input_title))
                    .setMessage(res.getString(R.string.record_polygon_input_messege))
                    .setView(edTxtNumberOfPlots)
                    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String lPlotCountString = edTxtNumberOfPlots.getText().toString();
                            int lPlotCount = 0;
                            if (!TextUtils.isEmpty(lPlotCountString)) {
                                lPlotCount = Integer.parseInt(lPlotCountString);
                            }
                            if (lPlotCount > 0) {
                                Intent recordPolygonIntent = new Intent(context, RecordPolygonActivity.class);
                                recordPolygonIntent.putExtra(FARMER_ID, farmerId);
                                recordPolygonIntent.putExtra(FARM_ID, strFarmID);
                                recordPolygonIntent.putExtra(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                                recordPolygonIntent.putExtra(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                                recordPolygonIntent.putExtra(PLOT_COUNT, lPlotCount);
                                SharedPreferenceUtils.getInstance(getApplicationContext()).setPlotCount(""+strFarmID, lPlotCount);
                                recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
                                startActivity(recordPolygonIntent);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.valid_count_msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();
        } else {
            Intent recordPolygonIntent = new Intent(context, RecordPolygonActivity.class);
            recordPolygonIntent.putExtra(FARMER_ID, farmerId);
            recordPolygonIntent.putExtra(FARM_ID, strFarmID);
            recordPolygonIntent.putExtra(OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
            recordPolygonIntent.putExtra(FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
            recordPolygonIntent.putExtra(PLOT_COUNT, count);
            recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
            recordPolygonIntent.setAction(Intent.ACTION_DEFAULT);
            startActivity(recordPolygonIntent);
        }

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
                    numberradio.setChecked((existingNumbers.getString(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY)).equalsIgnoreCase( "true") ||(existingNumbers.getInt(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY))==1)));
                }catch (Exception e){}

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
        phoneNumberList = new ArrayList<String> ();

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

            if (Collections.frequency(phoneNumberList,phonenumberValue) == 1) {
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
                phoneNumbersHolder.startAnimation(mSlideBottomDown);
            }
        });

        addNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        TextView enabled = (TextView) v.findViewById(R.id.moduleListEnabled);
        if (enabled.getText().toString().equalsIgnoreCase("true")) {
            if (id > 0) {
                TextView subModuleTv = (TextView) v.findViewById(R.id.moduleListCurrentSubModuleId);
                TextView moduleNameTv = (TextView) v.findViewById(R.id.moduleListModuleName);
                TextView statusStartedTv = (TextView) v.findViewById(R.id.moduleListStatusStarted);
                TextView statusFinishedTv = (TextView) v.findViewById(R.id.moduleListStatusFinished);
                TextView farmerModuleIdTv = (TextView) v.findViewById(R.id.moduleListFarmerModuleId);
                TextView farmerModuleLatTv = (TextView) v.findViewById(R.id.moduleListRecordedLat);
                TextView farmerModuleLngTv = (TextView) v.findViewById(R.id.moduleListRecordedLng);
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

                if (DynamicQuestions.getInstance() != null) {
                    DynamicQuestions.clearInstance();
                }
                context.startActivity(intent);
            } else {
                if (id == 0) {
                    // record farm location if necessary
                    TextView recordedLng = (TextView) v.findViewById(R.id.moduleListRecordedLng);
                    if (recordedLng != null && recordedLng.getText().equals("Phone")) {
                        if (phoneNumbersListItem == null) {
                            phoneNumbersListItem = v;
                        }
                        populatephoneNumberDialog();
                        phoneNumbersHolder.setVisibility(View.VISIBLE);
                        phoneNumbersHolder.startAnimation(mSlideBottomUp);
                    } else if (recordedLng != null && recordedLng.getText().equals("farmpolygon")) {
                        polygon=true;
                        enableFarmPolygon(v);
                    } else if (recordedLng != null && recordedLng.getText().equals(ChildLabourUtils.REG_CHILD)) {
                       Intent in = new Intent(this, RegisterChildActivity.class);
                       Bundle bundle=new Bundle();
                       bundle.putLong(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
                       in.putExtras(bundle);
                       startActivity(in);
                    } else if (recordedLng != null && recordedLng.getText().equals(ChildLabourUtils.CHILDLABOUR_SURVEY)) {
                        Intent in = new Intent(this, RegisterChildActivity.class);
                        in.putExtra(ChildLabourUtils.CHILD_FARMER_ID, farmerId);
                        startActivity(in);
                    } else {
                        polygon=false;
                        recordFarmLocation(v);
                    }
                } else if (id < 0) {
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
        }
    }

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
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    File photoFile = null;

                    try {
                        photoFile = createImageFile("portrait");
                    } catch (IOException e) {
                        FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating photo image file", e));
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
                        setCapturedImageURI(fileUri);
                        setCurrentPhotoPath(fileUri.getPath());
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, getCapturedImageURI());
                        try {
                            if(takePicture.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                            }else {
                                Toast.makeText(ViewFarmerActivity.this,"Gallery is not available in Mobile",Toast.LENGTH_LONG).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                } else if (items[item].equals(res.getString(R.string.Gallery))) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    try {
                        if(pickPhoto.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);
                        }else {
                            Toast.makeText(ViewFarmerActivity.this,"Gallery is not available in Mobile",Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private File createImageFile(String portraitOrSignature) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = (portraitOrSignature.equalsIgnoreCase("portrait")) ? "ofis_farmer_portrait_" + timestamp + "_" : "ofis_farmer_signature_" + timestamp + "_";
        File storageDir = getExternalFilesDir(null);
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
        dl.updateFarmerPhoto(farmerId,savedImagePath,olamFarmerIdStr,farmerNameStr);
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
}
