package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Hashtable;

public class TrainingRecordConfirmation extends BaseListActivity {

    private Context context;
    private Resources res;
    private DataLayer dl;

    private final static String EDIT_TRAININGRECORD_ID = "EDIT_TRAININGRECORD_ID";
    private String mCurrentPhotoPath = null;
    private long trainingRecordId;
    private JSONObject scoringColumns;
    private String columnSuffix, courseNameValue, moduleNameValue;
    private ListView farmerList, stakeholderList;
    private boolean isFarmerOpen = false;
    private boolean isStakeholderOpen = false;
    private LinearLayout farmerSection, stakeholderSection;
    private ImageView toggleFarmerIcon, toggleStakeholderIcon;

    private Location mCurrentLocation=null;
    GPSListener gpsListener=null;
    private GPSTracker gpsTracker=null;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    private int attendeeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_record_confirmation);
        farmerList=findViewById(R.id.list_farmer);
        stakeholderList=findViewById(R.id.list_stakeholder);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        res = getResources();

        dl = new DataLayer(res, context);

        Intent intent = getIntent();
        trainingRecordId = intent.getLongExtra(EDIT_TRAININGRECORD_ID, 0);

        columnSuffix = Utilities.getLocaleSuffix(res);
        if (columnSuffix.equalsIgnoreCase("_en")) {
            columnSuffix = "";
        }

        farmerSection = findViewById(R.id.farmer_section);
        stakeholderSection = findViewById(R.id.stakeholder_section);
        toggleFarmerIcon = findViewById(R.id.toggle_icon_farmer);
        toggleStakeholderIcon = findViewById(R.id.toggle_icon_stakeholder);

        farmerList.setVisibility(View.VISIBLE);
        stakeholderList.setVisibility(View.GONE);
        toggleFarmerIcon.setImageResource(R.drawable.ic_arrow_up);
        toggleStakeholderIcon.setImageResource(R.drawable.ic_arrow_down);
        updateWeights(farmerSection, 9);
        updateWeights(stakeholderSection, 1);

        toggleSection(true);
        toggleFarmerIcon.setOnClickListener(v -> toggleSection(true));
        toggleStakeholderIcon.setOnClickListener(v -> toggleSection(false));

        if (trainingRecordId > 0) {
            getTrainingRecord();

            // To load Farmer
            getTrainingAttendees(false);
            // To load Stakeholder
            getTrainingAttendees(true);

            bindButton();

            TextView itemCount = (TextView) findViewById(R.id.trainingAttendeesTotal);
            String itemCountValue = res.getQuantityString(R.plurals.trainingAttendeesCount, attendeeCount, attendeeCount);
            itemCount.setText(itemCountValue);
        }

        gpsListener=new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location!=null){
                    mCurrentLocation=location;
                }
            }
        };
        gpsTracker=new GPSTracker(this,gpsListener);
        //gpsTracker.getDeviceLocation();
//        if (gpsTracker.isGpsEnabled()){
            gpsTracker.startLocationUpdateInitially();
//        }
    }

    private void toggleSection(boolean isOpeningFarmer) {
        boolean isCurrentlyOpen = isOpeningFarmer ? isFarmerOpen : isStakeholderOpen;

        if (!isCurrentlyOpen) {
            farmerList.setVisibility(isOpeningFarmer ? View.VISIBLE : View.GONE);
            stakeholderList.setVisibility(isOpeningFarmer ? View.GONE : View.VISIBLE);

            toggleFarmerIcon.setImageResource(isOpeningFarmer ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
            toggleStakeholderIcon.setImageResource(isOpeningFarmer ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);

            updateWeights(farmerSection, isOpeningFarmer ? 9 : 1);
            updateWeights(stakeholderSection, isOpeningFarmer ? 1 : 9);

            isFarmerOpen = isOpeningFarmer;
            isStakeholderOpen = !isOpeningFarmer;
        } else {
            if (isOpeningFarmer) {
                farmerList.setVisibility(View.GONE);
                toggleFarmerIcon.setImageResource(R.drawable.ic_arrow_down);
            } else {
                stakeholderList.setVisibility(View.GONE);
                toggleStakeholderIcon.setImageResource(R.drawable.ic_arrow_down);
            }

            updateWeights(farmerSection, 1);
            updateWeights(stakeholderSection, 1);

            isFarmerOpen = false;
            isStakeholderOpen = false;
        }
    }

    private void updateWeights(LinearLayout layout, int weight) {
        ViewGroup.MarginLayoutParams oldParams = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();

        LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                weight
        );

        newParams.setMargins(
                oldParams.leftMargin,
                oldParams.topMargin,
                oldParams.rightMargin,
                oldParams.bottomMargin
        );

        layout.setLayoutParams(newParams);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getTrainingRecord() {
        Cursor trainingRecord = dl.getTrainingRecordById(trainingRecordId, columnSuffix);

        if (trainingRecord.moveToFirst()) {
            try {
                TextView courseName = (TextView) findViewById(R.id.courseName);
                courseNameValue = trainingRecord.getString(trainingRecord.getColumnIndex("course_name"));
                courseName.setText(courseNameValue);
                TextView trainingModuleName = (TextView) findViewById(R.id.trainingModuleName);
                moduleNameValue = trainingRecord.getString(trainingRecord.getColumnIndex("module_name"));
                trainingModuleName.setText(moduleNameValue);
                scoringColumns = new JSONObject(trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.CompletionCriteria.COLUMN_NAME_SCORING_COLUMNS)));
                if ( (!trainingRecord.isNull(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO))) && (!trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO)).equals("")) ) {
                    final ImageView trainingPhoto = (ImageView) findViewById(R.id.trainingPhoto);
                    mCurrentPhotoPath = trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO));

                    new CountDownTimer(1000, 100) {

                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            if (mCurrentPhotoPath != null) {
                                displayBitmap(mCurrentPhotoPath, trainingPhoto);
                            }
                        }
                    }.start();


                }
            } catch(JSONException e) {
                FirebaseCrash.report(new Exception("TrainingRecordConfirmation: Error getting scoring columns from training record", e));
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == -1) {
                    gpsTracker.getDeviceLocation();
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gpsTracker!=null){
            gpsTracker.stopLocationUpdates();
        }
    }
    private void getTrainingAttendees(boolean isStakeholder) {
        Cursor trainingAttendees;
        HashMap<Long,Integer> attendeeList;
        ListView listView;

        if (isStakeholder) {
            trainingAttendees = dl.getTrainingAttendeesByTrainingRecordIdStakeholder(trainingRecordId);
            attendeeList = TrainingRecord.stakeholderAttendeeList;
            listView = stakeholderList;
        } else {
            trainingAttendees = dl.getTrainingAttendeesByTrainingRecordId(trainingRecordId);
            attendeeList = TrainingRecord.farmerAttendeeList;
            listView = farmerList;
        }

        if (trainingAttendees.getCount() > 0) {
            attendeeCount += trainingAttendees.getCount();

            String[] adapterCols = new String[]{
                    "_id",
                    OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME,
                    OFISDBContract.TrainingAttendee.COLUMN_NAME_ATTENDED,
                    OFISDBContract.TrainingAttendee.COLUMN_NAME_PASSED,
                    OFISDBContract.TrainingAttendee.COLUMN_NAME_PRESCORE,
                    OFISDBContract.TrainingAttendee.COLUMN_NAME_POSTSCORE,
                    OFISDBContract.TrainingAttendee.COLUMN_NAME_SCORE,
                    OFISDBContract.TrainingAttendee.COLUMN_NAME_PERCENTAGE,
                    OFISDBContract.TrainingAttendee.COLUMN_NAME_GRADE
            };
            int[] adapterRowViews = new int[]{
                    R.id.trainingAttendeeId,
                    R.id.attendeeName,
                    R.id.criterion_attended,
                    R.id.criterion_passed,
                    R.id.criterion_prescore,
                    R.id.criterion_postscore,
                    R.id.criterion_score,
                    R.id.criterion_percentage,
                    R.id.criterion_grade
            };

            CustomListAdapter_SelectedAttendees customListAdapter = new CustomListAdapter_SelectedAttendees(context, trainingAttendees, adapterCols, adapterRowViews, res, false, null, scoringColumns, columnSuffix, dl, attendeeList, trainingRecordId, false, isStakeholder);

            listView.setAdapter(customListAdapter);
            listView.setOnItemClickListener(null);
        }
    }


    private void bindButton() {
        Button confirmButton = (Button) findViewById(R.id.confirmTrainingRecord);



        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmButton.setEnabled(false);
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        confirmButton.setEnabled(true);
                    }
                }, 3000);
                // add transaction to network queue

                if (mCurrentLocation!=null && !GPSUtils.isMockLocation(mCurrentLocation)){}else {
                    if (mCurrentLocation == null || !gpsTracker.isGpsEnabled()) {
                        gpsTracker.getDeviceLocation();
                        return;
                    }

                }

                dl.retainOrDeleteSignatureBasedOnAvailability(trainingRecordId);

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                ContentValues networkQueueValues = new ContentValues();
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "trainingrecord");
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, res.getString(R.string.TrainingRecordConfirmationTitle));
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, courseNameValue);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, moduleNameValue);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRAINING_RECORD_ID, trainingRecordId);
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());

                if (dl.insertNetworkQueueEntry(networkQueueValues) != 0) {
                    // collect GPS Record
                    collectGPSPoint(GPSConstants.MODULE_CAPTURE_TRAINING_RECORD,mCurrentLocation,courseNameValue,moduleNameValue);
                    ContentValues trainingRecordValues = new ContentValues();
                    trainingRecordValues.put(OFISDBContract.TrainingRecord.COLUMN_NAME_IN_QUEUE, 1);
                    if (dl.updateTrainingRecord(trainingRecordValues, trainingRecordId) > 0) {
                        Toast.makeText(context, R.string.trainingrecord_added_to_queue, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, TrainingRecords.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            }
        });
    }

    private void collectGPSPoint(String module, Location farmLocation, String courseName,String moduleName) {

        TrackDetailsDTO trackDetailsDTO=new TrackDetailsDTO();
        trackDetailsDTO.setModule(module);
        trackDetailsDTO.setLatitude(String.valueOf(farmLocation.getLatitude()));
        trackDetailsDTO.setLongitude(String.valueOf(farmLocation.getLongitude()));
        trackDetailsDTO.setAccuracy(String.valueOf(farmLocation.getAccuracy()));
        trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
        trackDetailsDTO.setTrainingCourseName(courseName);
        trackDetailsDTO.setTrainingModuleName(moduleName);
        ContentValues geoContentValues=new GPSUtils().getGeoContentValues(trackDetailsDTO);
        new GpsTrackerLogic().insertGeoDataDetails(context,geoContentValues);
    }

    private void displayBitmap(String savedImagePath, ImageView target) {
        // set our desired width and height for display
        int baseWidth = target.getWidth();
        int baseHeight = target.getHeight();

        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);

        target.setImageBitmap(bitmap);
    }
}
