package com.olam.digital.ofis.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.gps.utils.GPSUtils;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class GPSTracker {

    // Component related to UI
    Activity mActivity;
    private ProgressDialog progressDialog;

    // bunch of location related apis
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;

    // App handle time
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;

    private GPSListener gpsListener;
    private boolean showDialog=false;

    public static final int PRIORITY_HIGH_ACCURACY = 100;
    public static final int PRIORITY_BALANCED_POWER_ACCURACY = 102;
    public static final int PRIORITY_LOW_POWER = 104;
    public static final int PRIORITY_NO_POWER = 105;

    public GPSTracker(AppCompatActivity mActivity){
        this.mActivity=mActivity;
    }

    public GPSTracker(AppCompatActivity mActivity, GPSListener gpsListener){
        this.mActivity= mActivity;
        this.gpsListener = gpsListener;
        progressDialog=new ProgressDialog(mActivity);
        init();
    }
    // OW-4574
    public GPSTracker(FragmentActivity mActivity, GPSListener gpsListener){
        this.mActivity= mActivity;
        this.gpsListener = gpsListener;
        progressDialog=new ProgressDialog(mActivity);
        init();
    }

    public GPSTracker(AppCompatActivity mActivity, GPSListener gpsListener, boolean showDialog){
        this.mActivity= mActivity;
        this.gpsListener = gpsListener;
        progressDialog=new ProgressDialog(mActivity);
        this.showDialog=showDialog;
        init();
    }

    /**
     * Initialize the location call back and pass the current locations to activity
     * build the location request to periodic location update
     */
    public void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        mSettingsClient = LocationServices.getSettingsClient(mActivity);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                if (mCurrentLocation != null && !GPSUtils.isMockLocation(mCurrentLocation)) {
                    if (gpsListener!=null) {
                        gpsListener.onGPSClick(mCurrentLocation);
                    }
                }
                if (progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        };


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(getLocationMode());
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    public int getLocationMode() {
         int locationType=PRIORITY_HIGH_ACCURACY;
         int locationMode=3;
        try {
             locationMode = Settings.Secure.getInt(mActivity.getContentResolver(),Settings.Secure.LOCATION_MODE);
         } catch (Settings.SettingNotFoundException e) {
             e.printStackTrace();
         }
         try {
             if(locationMode == 3) {
                 locationType = PRIORITY_HIGH_ACCURACY;
             }else if(locationMode == 2) {
                 locationType = PRIORITY_BALANCED_POWER_ACCURACY;
             }
         }catch (Exception e){}
           return locationType;
    }


    /**
     * method to identify the GPS status in device. Check if enabled and if not send user to the GPS settings
     * @return
     * true - yes, GPS is turned on
     * false - No, GPS is turned off
     */
    public boolean isGpsEnabled(){
        boolean gpsEnabled=false;
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationManager service = (LocationManager) mActivity.getSystemService(LOCATION_SERVICE);
            boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (enabled) {
                gpsEnabled=true;
            }
        }
        return gpsEnabled;
    }


    /**
     * get Device location, Dexter library initially checked the permissions
     * Location update method would be called if permission accepted already
     *If permission denied, settings page will open.
     */
    public void getDeviceLocation(){
        Dexter.withActivity(mActivity)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            startLocationUpdates();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            if (progressDialog != null && progressDialog.getWindow().getDecorView().getWindowToken() != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(mActivity, "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }


    /**
     * Starting location updates only gps is turned off.
     * No negative scenario handled. it will run in background without user knowlwdge.
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    public void startLocationUpdateInitially() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        progressDialog = new ProgressDialog(mActivity);
                        progressDialog.setCancelable(false);
                        progressDialog.setIndeterminate(false);
                        progressDialog.setMessage(mActivity.getResources().getString(R.string.fetching_location));
                        if (showDialog) {
                            progressDialog.show();
                        }
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if(mCurrentLocation != null && !GPSUtils.isMockLocation(mCurrentLocation)) {
                                    if (progressDialog!=null && progressDialog.isShowing()){
                                        progressDialog.dismiss();
                                    }
                                    if(gpsListener!=null) {
                                        gpsListener.onGPSClick(mCurrentLocation);
                                    }
                                }else{
                                    if (progressDialog != null && progressDialog.getWindow().getDecorView().getWindowToken() != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
//                                    Toast.makeText(mActivity,mActivity.getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                                }
                            }
                        }, 3000);
                    }
                });
    }


    /**
     * Starting location updates. handled negative scenario also while save/submit for each activity if GPS not enabled.
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    public void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        progressDialog = new ProgressDialog(mActivity);
                        progressDialog.setCancelable(false);
                        progressDialog.setIndeterminate(false);
                        progressDialog.setMessage(mActivity.getResources().getString(R.string.fetching_location));
                        if (showDialog) {
                            progressDialog.show();
                        }
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if(mCurrentLocation != null && !GPSUtils.isMockLocation(mCurrentLocation)) {
                                    if (progressDialog!=null && progressDialog.isShowing()){
                                        progressDialog.dismiss();
                                    }
                                    if(gpsListener!=null) {
                                        gpsListener.onGPSClick(mCurrentLocation);
                                    }
                                }else{
                                    if (progressDialog != null && progressDialog.getWindow().getDecorView().getWindowToken() != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    Toast.makeText(mActivity,mActivity.getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                                }
                            }
                        }, 3000);
                    }
                })
                .addOnFailureListener(mActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.getWindow().getDecorView().getWindowToken() != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mActivity,mActivity.getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();
                            }
                        }, 1000);

                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
//                                Log.e(TAG, errorMessage);

                                Toast.makeText(mActivity, errorMessage, Toast.LENGTH_LONG).show();

                                break;
                            default:
                                Toast.makeText(mActivity,mActivity.getResources().getString(R.string.record_location_timeout),Toast.LENGTH_LONG).show();

                        }
                    }
                }
                );
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
    }


    /**
     * Remove the location updates
     * calling this method in every activities onStop() method.
     */
    public void stopLocationUpdates() {
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(mActivity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

}
