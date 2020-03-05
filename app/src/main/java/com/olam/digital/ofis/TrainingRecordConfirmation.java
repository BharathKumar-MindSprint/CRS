package com.olam.digital.ofis;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.AddNewChildActivity;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Hashtable;

public class TrainingRecordConfirmation extends BaseListActivity {

    private final String MATOMOTAG = this.getClass().getSimpleName();
    private Context context;
    private Resources res;
    private DataLayer dl;

    private final static String EDIT_TRAININGRECORD_ID = "EDIT_TRAININGRECORD_ID";
    private String mCurrentPhotoPath = null;
    private long trainingRecordId;
    private JSONObject scoringColumns;
    private String columnSuffix, courseNameValue, moduleNameValue;
    ListView listView;

    private Location mCurrentLocation=null;
    GPSListener gpsListener=null;
    private GPSTracker gpsTracker=null;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    private Location mCurrentLocation=null;
    GPSListener gpsListener=null;
    private GPSTracker gpsTracker=null;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_record_confirmation);
        listView=findViewById(R.id.list);
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

        if (trainingRecordId > 0) {
            getTrainingRecord();
            getTrainingAttendees();
            bindButton();
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
        if (gpsTracker.isGpsEnabled()){
            gpsTracker.startLocationUpdateInitially();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_TRAINING_RECORD_ACTIVITY, ScreenNameConstants.SCREEN_TRAINING_RECORD_CONFIRMATION_ACTIVITY, ScreenNameConstants.SCREEN_TRAINING_RECORD_CONFIRMATION);
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

    private void getTrainingAttendees() {
        Cursor trainingAttendees = dl.getTrainingAttendeesByTrainingRecordId(trainingRecordId);

        if (trainingAttendees.getCount() > 0) {
            LinearLayout attendeesFooter = (LinearLayout) ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.training_confirmation_footer, listView, false);
            listView.addFooterView(attendeesFooter);

            TextView itemCount = (TextView) findViewById(R.id.trainingAttendeesTotal);
            String itemCountValue = res.getQuantityString(R.plurals.trainingAttendeesCount, trainingAttendees.getCount(), trainingAttendees.getCount());
            itemCount.setText(itemCountValue);

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
            CustomListAdapter_SelectedAttendees customListAdapter_selectedAttendees = new CustomListAdapter_SelectedAttendees(context, trainingAttendees, adapterCols, adapterRowViews, res, false, null, scoringColumns, columnSuffix);

            listView.setAdapter(customListAdapter_selectedAttendees);
            listView.setOnItemClickListener(null);
//            setListAdapter(customListAdapter_selectedAttendees);
//            getListView().setOnItemClickListener(null);
        }
    }

    private void bindButton() {
        Button confirmButton = (Button) findViewById(R.id.confirmTrainingRecord);



        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add transaction to network queue
                MatomoHelper.matomoEventTracker(MATOMOTAG, ScreenNameConstants.BUTTON_CONFIRM_TRAINING_RECORD,  ScreenNameConstants.BUTTON_CLICKED);
                if (mCurrentLocation==null || !gpsTracker.isGpsEnabled()){
                    gpsTracker.getDeviceLocation();
                    return;
                }


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
                    collectGPSPoint(GPSConstants.MODULE_CAPTURE_TRAINING_RECORD,mCurrentLocation);
                    ContentValues trainingRecordValues = new ContentValues();
                    trainingRecordValues.put(OFISDBContract.TrainingRecord.COLUMN_NAME_IN_QUEUE, 1);
                    if (dl.updateTrainingRecord(trainingRecordValues, trainingRecordId) > 0) {
                        Toast.makeText(context, R.string.trainingrecord_added_to_queue, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, TrainingRecords.class);
                        context.startActivity(intent);
                    }
                }
            }
        });
    }

    private void collectGPSPoint(String module, Location farmLocation) {
        String lat=String.valueOf(farmLocation.getLatitude());
        String lon=String.valueOf(farmLocation.getLongitude());
        ContentValues geoContentValues=new GPSUtils().getGeoContentValues(module,lat,lon,new Timestamp(System.currentTimeMillis()).toString());
        Long newTrackId= new GpsTrackerLogic().insertGeoDataDetails(context,geoContentValues);
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
