package com.olam.digital.ofis.recordpolygon.polygonhistory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.APICall.PolygonHistoryResponseModel;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonHistoryData;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.dao.PolygonPlotHistoryDao;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.list.PolygonHistoryDataList;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class PolygonHistory extends AppCompatActivity {

    private TextView farmerName, olamFarmerId , plot_name;
    protected LocationManager mLocationManager;

    private static final String FARMER_ID = "FARMER_ID";
    private static final String FARM_ID = "FARM_ID";
    private static final String OLAM_FARMER_ID = "OLAM_FARMER_ID";
    private static final String FARMER_NAME = "FARMER_NAME";
    long mFarmerId;
    int mFarmId;
    String mOlamFarmerId;
    String mFarmerName;
    Spinner plot_number;
    protected MapView mapView;
    private Marker currentLocationMarker , farmLocationMarker;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 7;

    OFISRoomDB db;

    Polygon polygon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polygon_history);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        boolean hwAccelerationOK = true;
        Configuration.getInstance().setMapViewHardwareAccelerated(hwAccelerationOK);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        db = OFISRoomDB.getDbInstance(getApplicationContext());

        mFarmerId = getIntent().getLongExtra(FARMER_ID, 0);
        mFarmId = getIntent().getIntExtra(FARM_ID, 0);
        mOlamFarmerId = getIntent().getStringExtra(OLAM_FARMER_ID);
        mFarmerName = getIntent().getStringExtra(FARMER_NAME);

        farmerName = (TextView) findViewById(R.id.farmer_name);
        olamFarmerId = (TextView) findViewById(R.id.olam_farmer_id);
        plot_name = (TextView) findViewById(R.id.plot_name);
        plot_number = (Spinner) findViewById(R.id.plot_number);
        if (!TextUtils.isEmpty(mFarmerName)) {
            farmerName.setText(mFarmerName);
        }
        if (!TextUtils.isEmpty(mOlamFarmerId)) {
            olamFarmerId.setText(mOlamFarmerId);
        }



        // Set up the OSMDroid configuration
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setClickable(true);

        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Location permissions are not granted, request them
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            // Location permissions are already granted, get the last known location
//            getLastLocation();
            try {
                PolygonHistoryApiCall((int) mFarmerId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            getFarmLocation();
        }

        List<PolygonPlotHistoryDao.UnitMapping> plotData = db.polygonPlotHistoryDao().getAllPlotCount();

        List<String> spinnerItems = new ArrayList<>();
        for (int i = 1; i <= plotData.size(); i++) {
            spinnerItems.add("Plot " + i);
        }

        // Create an ArrayAdapter using the UnitMapping data
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        plot_number.setAdapter(adapter);

        // Set a listener for item selections
        plot_number.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected UnitMapping from the plotData list
                PolygonPlotHistoryDao.UnitMapping selectedUnitMapping = plotData.get(position);

                // Retrieve the ID from the selected UnitMapping
                long selectedId = selectedUnitMapping.id;
                String farmName = selectedUnitMapping.plotName;

                plot_name.setText(farmName);

                plotGraphInMap(selectedId);

                Log.d("Selected ID", "ID: " + selectedId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here if nothing is selected
            }
        });

        ImageButton buttonTrackingMode = (ImageButton) findViewById(R.id.buttonTrackingMode);
        buttonTrackingMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation();
            }
        });

    }

    private void getFarmLocation() {
        double farmLatitude = db.polygonHistoryDataDao().getLattitude(mFarmerId);
        double farmLongitude = db.polygonHistoryDataDao().getLongitude(mFarmerId);

        // Create a GeoPoint from the last known location
        GeoPoint farmLocation = new GeoPoint(farmLatitude, farmLongitude);

        // Create a Marker for the current location
        farmLocationMarker = new Marker(mapView);
        farmLocationMarker.setPosition(farmLocation);
        farmLocationMarker.setTitle("Farm Location");
        farmLocationMarker.setSnippet("Latitude: " + farmLatitude + "\nLongitude: " + farmLongitude); // Set the snippet with lat and long
        farmLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        farmLocationMarker.setIcon(ContextCompat.getDrawable(PolygonHistory.this, R.drawable.location_mark_purple));
        mapView.getOverlays().add(farmLocationMarker);

        farmLocationMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
//                            Toast.makeText(PolygonHistory.this, marker.getTitle() + "\n" + marker.getSnippet(), Toast.LENGTH_SHORT).show();
                View dialogView = getLayoutInflater().inflate(R.layout.custom_location_dialog, null);

                // Find views in the dialog layout
                TextView textLatitude = dialogView.findViewById(R.id.textLatitude);
                TextView textLongitude = dialogView.findViewById(R.id.textLongitude);
                TextView farmerName = dialogView.findViewById(R.id.enumName);

                String enumerator_id = Utilities.getFromSharedPrefs(getApplicationContext());
                // Set the latitude and longitude in the dialog
                farmerName.setText("Name: " + mFarmerName);
                textLatitude.setText("Latitude: " + farmLatitude);
                textLongitude.setText("Longitude: " + farmLongitude);

                // Create and show the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(PolygonHistory.this);
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Location permissions granted, get the last known location
                getLastLocation();
            } else {
                // Location permissions not granted, handle accordingly
                Log.d("Location", "Location permissions not granted.");
            }
        }
    }

    private void getLastLocation() {

        // Get the last known location from the FusedLocationProviderClient
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        /*// Register the LocationListener for continuous location updates
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1F, (LocationListener) this);
*/
        // Create a FusedLocationProviderClient object
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

                    // Create a Marker for the current location
                    currentLocationMarker = new Marker(mapView);
                    currentLocationMarker.setPosition(currentLocation);
                    currentLocationMarker.setTitle("Enumerator Current Location");
                    currentLocationMarker.setSnippet("Latitude: " + latitude + "\nLongitude: " + longitude); // Set the snippet with lat and long
                    currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    currentLocationMarker.setIcon(ContextCompat.getDrawable(PolygonHistory.this, R.drawable.location_mark));
                    mapView.getOverlays().add(currentLocationMarker);

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
                            AlertDialog.Builder builder = new AlertDialog.Builder(PolygonHistory.this);
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
                    mapView.getController().animateTo(currentLocation);
                    mapView.getController().setZoom(20.0);
                } else {
                    // No last known location is available
                    Log.d("Location", "No last known location available.");
                }
            }
        });
    }

    private void PolygonHistoryApiCall(Integer farmerId) {
        ManageRecords getPolygonHistoryData = RetrofitClient.getRetrofit().create(ManageRecords.class);
        retrofit2.Call<PolygonHistoryResponseModel> call = getPolygonHistoryData.getAllPolygonHistoryResponse(farmerId , 1);

        call.enqueue(new retrofit2.Callback<PolygonHistoryResponseModel>() {

            @Override
            public void onResponse(Call<PolygonHistoryResponseModel> call, Response<PolygonHistoryResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProgressDialog pd = new ProgressDialog(PolygonHistory.this);
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
                        }else {
                            try {
                                Log.d("FailureMessage", "Error response body: " + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        pd.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

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
        // Convert PolygonHistoryResponseModel to PolygonHistoryEntity
        // Implement the conversion logic based on your needs
        // For simplicity, you can manually set the fields
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


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    private void plotGraphInMap(long id){
        String jsonString = db.polygonPlotHistoryDao().getPositionData(id);
        ArrayList<GeoPoint> plotPoints = PlotDataParser.parsePlotData(jsonString);

        // Set the default map center and zoom level
        IMapController mapController = mapView.getController();
        mapController.setZoom(21);

        if (!plotPoints.isEmpty()) {
            if (polygon != null) {
                mapView.getOverlays().remove(polygon);
            }

            polygon = new Polygon();
            polygon.setFillColor(Color.GREEN);
            polygon.setStrokeColor(Color.BLACK);
            polygon.setStrokeWidth(2);
            polygon.setPoints(plotPoints);
            mapView.getOverlays().add(polygon);

            // Set map center to the first point
            mapController.setCenter(plotPoints.get(0));
        }
    }
}