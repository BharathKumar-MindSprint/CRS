package com.olam.digital.ofis;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class TrainingRecord extends BaseListActivity{

    private Context context;
    private Resources res;
    private DataLayer dl;
    private TrainingRecord me;

    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";
    private final static String EDIT_TRAININGRECORD_ID = "EDIT_TRAININGRECORD_ID";
    private final static String COURSE_ID = "COURSE_ID";
    private final static String TRAINING_MODULE_ID = "TRAINING_MODULE_ID";
    private final static String TRAINING_PHOTO = "TRAINING_PHOTO";
    private final static String EDIT_MODE = "EDIT_MODE";
    private final static String READ_ONLY_MODE = "READ_ONLY_MODE";

    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;

    public long trainingRecordId, courseId, trainingModuleId;
    private JSONObject scoringColumns;
    private boolean editMode, readOnlyMode, firstLoad = true;

    private Spinner courseSpinner, trainingModuleSpinner;
    Button completeTrainingRecord;
    private String columnSuffix;

    private Cursor trainingAttendees, trainingRecord, trainingAttendeesStakeholder;
    private CustomListAdapter_SelectedAttendees customListAdapter_selectedAttendees;
    private CustomListAdapter_SelectedAttendees customListAdapter_selectedAttendeesStakeholder;
    private ImageButton trainingPhotoActionButton;

    // Runtime Permission Request Code
    private final static int MY_PERMISSIONS_REQUEST = 9999;

    private final static int REQUEST_PERMISSIONS_FOR_TIRAMISU = 7777;
    private static final int REQUEST_CHECK_SETTINGS = 100;
   public static HashMap<Long,Integer> farmerAttendeeList =null;
   public static HashMap<Long,Integer> stakeholderAttendeeList =null;
   public static boolean isCaptureAttendee;

    public boolean signatureAllowed;
    String currentPhotoPathBackup;

    private ImageView toggleFarmerIcon, toggleStakeholderIcon;
    private ListView farmerList, stakeholderList;

    private boolean isFarmerOpen = false;
    private boolean isStakeholderOpen = false;
    private LinearLayout farmerSection, stakeholderSection;

    private OFISRoomDB ofisRoomDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_record);
        farmerList=findViewById(R.id.list_farmer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        context = me = this;
        res = getResources();
        ofisRoomDB = OFISRoomDB.getDbInstance(context);

        farmerSection = findViewById(R.id.farmer_section);
        stakeholderSection = findViewById(R.id.stakeholder_section);
        toggleFarmerIcon = findViewById(R.id.toggle_icon_farmer);
        toggleStakeholderIcon = findViewById(R.id.toggle_icon_stakeholder);
        stakeholderList = findViewById(R.id.list_stakeholder);

        dl = new DataLayer(res, context);

        Intent intent = getIntent();
        trainingRecordId = intent.getLongExtra(EDIT_TRAININGRECORD_ID, 0);
        courseId = intent.getLongExtra(COURSE_ID, 0);
        trainingModuleId = intent.getLongExtra(TRAINING_MODULE_ID, 0);
        editMode = intent.getBooleanExtra(EDIT_MODE, false);
        readOnlyMode = intent.getBooleanExtra(READ_ONLY_MODE, false);

        courseSpinner = (Spinner) this.findViewById(R.id.spinner_course);
        trainingModuleSpinner = (Spinner) this.findViewById(R.id.spinner_training_module);
        completeTrainingRecord = (Button) findViewById(R.id.completeTrainingRecord);


        columnSuffix = Utilities.getLocaleSuffix(res);
        if (columnSuffix.equalsIgnoreCase("_en")) {
            columnSuffix = "";
        }

        if (editMode) {
            getTrainingRecord();
        }

        trainingPhotoActionButton = (ImageButton) findViewById(R.id.trainingPhotoActionButton);
        if (readOnlyMode) {
            trainingPhotoActionButton.setVisibility(View.GONE);
        } else {
            trainingPhotoActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        if (ContextCompat.checkSelfPermission(TrainingRecord.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(TrainingRecord.this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    REQUEST_PERMISSIONS_FOR_TIRAMISU);
                        }else {
                            bindTrainingPhotoButton();
                        }
                    }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat
                                .checkSelfPermission(TrainingRecord.this,
                                        android.Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(TrainingRecord.this,
                                    new String[]{android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST);

                        } else {
                            bindTrainingPhotoButton();
                        }
                    } else {
                        bindTrainingPhotoButton();
                    }
                }
            });
        }

        bindSpinners();
        bindButtons();

        farmerList.setVisibility(View.VISIBLE);
        stakeholderList.setVisibility(View.GONE);
        toggleFarmerIcon.setImageResource(R.drawable.ic_arrow_up);
        toggleStakeholderIcon.setImageResource(R.drawable.ic_arrow_down);
        updateWeights(farmerSection, 9);
        updateWeights(stakeholderSection, 1);

        toggleSection(true);
        toggleFarmerIcon.setOnClickListener(v -> toggleSection(true));
        toggleStakeholderIcon.setOnClickListener(v -> toggleSection(false));

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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
        if (mCapturedImageURI != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_URI_KEY, mCapturedImageURI.toString());
        }

        savedInstanceState.putLong(EDIT_TRAININGRECORD_ID, trainingRecordId);
        savedInstanceState.putLong(COURSE_ID, courseId);
        savedInstanceState.putLong(TRAINING_MODULE_ID, trainingModuleId);
        savedInstanceState.putBoolean("CAPTURE_TRAINEE",isCaptureAttendee);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        ImageView trainingPhoto = (ImageView)findViewById(R.id.trainingPhoto);

        if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI_KEY)) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI_KEY));
        }
        if (mCurrentPhotoPath != null) {
            displayBitmap(mCurrentPhotoPath, trainingPhoto);
        }
        if (savedInstanceState.containsKey(EDIT_TRAININGRECORD_ID)) {
            trainingRecordId = savedInstanceState.getLong(EDIT_TRAININGRECORD_ID);
        }
        if (savedInstanceState.containsKey(COURSE_ID)) {
            courseId = savedInstanceState.getLong(COURSE_ID);
            dl.setSpinnerSelectionById((int)courseId, courseSpinner);
        }
        if (savedInstanceState.containsKey(TRAINING_MODULE_ID)) {
            trainingModuleId = savedInstanceState.getLong(TRAINING_MODULE_ID);
            dl.setSpinnerSelectionById((int)trainingModuleId, trainingModuleSpinner);
        }
        if (savedInstanceState.containsKey("CAPTURE_TRAINEE")) {
            isCaptureAttendee = savedInstanceState.getBoolean("CAPTURE_TRAINEE");
        }

        super.onRestoreInstanceState(savedInstanceState);
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

    private void setFullImageFromFilePath(String imagePath, ImageView target) {
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, "trainingphoto");

        if (savedImagePath != null) {
            Uri uri = Uri.parse(savedImagePath);
            setCapturedImageURI(uri);
            setCurrentPhotoPath(savedImagePath);

            displayBitmap(savedImagePath, target);
        }

    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "ofis_training_record_" + timestamp;
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void getTrainingRecord() {
        trainingRecord = dl.getTrainingRecordById(trainingRecordId, columnSuffix);

        if (trainingRecord.moveToFirst()) {
            try {
                courseId = trainingRecord.getLong(trainingRecord.getColumnIndex(OFISDBContract.TrainingModule.COLUMN_NAME_COURSE_ID));
                trainingModuleId = trainingRecord.getLong(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID));
                scoringColumns = new JSONObject(trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.CompletionCriteria.COLUMN_NAME_SCORING_COLUMNS)));
                if ((!trainingRecord.isNull(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO))) && (!trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO)).equals(""))) {
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
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("TrainingRecord: Error getting scoring columns for training record", e));
                e.printStackTrace();
            }
        }
    }

    private void getTrainingAttendees() {
        trainingAttendees = dl.getTrainingAttendeesByTrainingRecordId(trainingRecordId);

        //Get All Training Attendees Counts
         farmerAttendeeList=dl.prepareAttendeeCountList(trainingAttendees,Integer.parseInt(String.valueOf(trainingRecordId)), false);

        if (trainingAttendees.getCount() > 0) {
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
            customListAdapter_selectedAttendees = new CustomListAdapter_SelectedAttendees(context, trainingAttendees, adapterCols, adapterRowViews, res, readOnlyMode, me, scoringColumns, columnSuffix,dl,farmerAttendeeList,trainingRecordId, signatureAllowed, false);
            farmerList.setAdapter(customListAdapter_selectedAttendees);
            //setListAdapter(customListAdapter_selectedAttendees);
        }
    }

    private void getTrainingAttendeesStakeholder() {
        trainingAttendeesStakeholder = dl.getTrainingAttendeesByTrainingRecordIdStakeholder(trainingRecordId);

        //Get All Training Attendees Counts
        stakeholderAttendeeList=dl.prepareAttendeeCountList(trainingAttendeesStakeholder,Integer.parseInt(String.valueOf(trainingRecordId)), true);

        if (trainingAttendeesStakeholder.getCount() > 0) {
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
            customListAdapter_selectedAttendeesStakeholder = new CustomListAdapter_SelectedAttendees(context, trainingAttendeesStakeholder, adapterCols, adapterRowViews, res, readOnlyMode, me, scoringColumns, columnSuffix,dl,stakeholderAttendeeList,trainingRecordId, signatureAllowed, true);
            stakeholderList.setAdapter(customListAdapter_selectedAttendeesStakeholder);
        }
    }

    public void deleteTrainingAttendee(final int attendeeId) {

        // New Approach
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.common_alert_dialog, null);
        builder.setView(dialogView);
        TextView tvMsg = (TextView) dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText(res.getString(R.string.trainingattendee_are_you_sure));
        Button btnNo = (Button) dialogView.findViewById(R.id.btn_no);
        btnNo.setText(res.getString(R.string.app_update_cancel));
        Button btnYes = (Button) dialogView.findViewById(R.id.btn_yes);
        btnYes.setText(res.getString(R.string.app_update_ok));
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

                // Deleting from Relationship table before training attendee removed
                dl.deleteAttendeeByTrainingId(attendeeId,trainingRecordId);

                if (dl.deleteTrainingAttendeeById(attendeeId) > 0) {
                    trainingAttendees.close();
                    trainingAttendees = dl.getTrainingAttendeesByTrainingRecordId(trainingRecordId);
                    customListAdapter_selectedAttendees.swapCursor(trainingAttendees);

                    trainingAttendeesStakeholder.close();
                    trainingAttendeesStakeholder = dl.getTrainingAttendeesByTrainingRecordIdStakeholder(trainingRecordId);
                    customListAdapter_selectedAttendeesStakeholder.swapCursor(trainingAttendeesStakeholder);

                    Toast.makeText(context, res.getString(R.string.trainingattendee_deleted), Toast.LENGTH_LONG).show();
                    alert.dismiss();
                    getTrainingAttendees();
                    getTrainingAttendeesStakeholder();
                    if (signatureAllowed && !dl.areAllSignaturesPresent(trainingRecordId)) {
                        completeTrainingRecord.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_disabled));
                    } else {
                        completeTrainingRecord.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_background));
                    }
                }
            }
        });

        alert.show();


    }

    public void signTrainingAttendee(int attendeeId) {
        final boolean[] isSigned = {false};
        boolean isStakeholder = false;
        long attendeeIdByItem = 0;
        attendeeIdByItem = dl.getAttendeeIdByItem(attendeeId, false);
        final Dialog signatureDialog = new Dialog(TrainingRecord.this, android.R.style.Theme_Material_Dialog_Alert);
        signatureDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        signatureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        signatureDialog.setContentView(R.layout.dialog_signature);
        SignaturePad mSignaturePad = signatureDialog.findViewById(R.id.signature_pad);

        Button bt_clear = (Button)signatureDialog.findViewById(R.id.imgv_clear_signature);
        Button bt_confirm = (Button)signatureDialog.findViewById(R.id.btn_add_signature);
        TextView tv_attendeeName = (TextView) signatureDialog.findViewById(R.id.tv_attendeeName);

        if (attendeeIdByItem == 0) {
            attendeeIdByItem = dl.getAttendeeIdByItem(attendeeId, true);
            isStakeholder = true;
        }

        tv_attendeeName.setVisibility(View.VISIBLE);

        if (isStakeholder) {
            String stakeholderName = ofisRoomDB.multiStakeHolderDao().getStakeHolderById(attendeeIdByItem).msName;
            tv_attendeeName.setText(stakeholderName);
        } else {
            Cursor farmer = dl.getFarmerById(attendeeIdByItem);
            if (farmer != null && farmer.moveToFirst()) {
                String farmerName = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
                tv_attendeeName.setText(farmerName);
                farmer.close();
            }
        }

        bt_confirm.setEnabled(true);
        bt_clear.setEnabled(true);

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignaturePad.clear();
            }
        });

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched
            }

            @Override
            public void onSigned() {
                isSigned[0] = true;
            }

            @Override
            public void onClear() {
                isSigned[0] = false;
            }
        });

        signatureDialog.findViewById(R.id.btn_cancel_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signatureDialog.dismiss();
            }
        });

        long finalAttendee = attendeeIdByItem;
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isSigned[0]) {
                        Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                        String signatureName = finalAttendee + "_" + System.currentTimeMillis() + ".png";
                        saveSignatureImage(signatureBitmap, signatureName);
                        dl.saveAttendeeSignature(attendeeId, signatureName);
                        signatureDialog.dismiss();
                        getTrainingAttendees();
                        getTrainingAttendeesStakeholder();

                        if (dl.areAllSignaturesPresent(trainingRecordId)){
                            completeTrainingRecord.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_background));
                        }
                    } else {
                        Toast.makeText(TrainingRecord.this, "No signature available...", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        signatureDialog.show();
    }

    //------------------------Temp Testing for OW-431 - Start-----------------------------
    private boolean tempTest(String selectedSpinnerItem) {
        String courseId = "";
        boolean multiSelectFlag = false;

        DataLayer dl = new DataLayer(getResources(), TrainingRecord.this);
        Cursor c = dl.getTempData();

        Cursor cCourseData = dl.getTempCourseData();

        if (cCourseData.getCount() > 0) {
            cCourseData.moveToFirst();

            for (int i=0; i < cCourseData.getCount(); i++) {

                String courseName = cCourseData.getString(cCourseData.getColumnIndex(OFISDBContract.Course.COLUMN_NAME_NAME));

                if(courseName.equals(selectedSpinnerItem)) {
                    courseId = cCourseData.getString(cCourseData.getColumnIndex(OFISDBContract.Course.COLUMN_NAME_ID));
                    break;
                }
                cCourseData.moveToNext();
            }
        }

        if (c.getCount() > 0) {
            c.moveToFirst();

            for (int i=0; i < c.getCount(); i++) {
                String completionCriteriaId = c.getString(c.getColumnIndex(OFISDBContract.TrainingModule.COLUMN_COMPLETIONCRITERION_ID));
                String moduleCourseId = c.getString(c.getColumnIndex(OFISDBContract.TrainingModule.COLUMN_NAME_COURSE_ID));
                String COLUMN_NAME_ID = c.getString(c.getColumnIndex(OFISDBContract.TrainingModule.COLUMN_NAME_ID));
                String COLUMN_NAME_NAME = c.getString(c.getColumnIndex(OFISDBContract.TrainingModule.COLUMN_NAME_NAME));

                if(courseId.equals(moduleCourseId) && completionCriteriaId.equals("2")) {
                    //Set flag for enable multi select option...
                    multiSelectFlag = true;
                    break;
                } else {
                    multiSelectFlag = false;
                }
                c.moveToNext();
            }
        }
        
        return multiSelectFlag;
    }

    public void saveSignatureImage(Bitmap bitmap, String signatureName) {
        File directory = new File(getFilesDir(), "signatures");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File imageFile = new File(directory, signatureName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAttendeeSignature(int attendeeId, ImageButton attendeeSignature) {
        String signatureName = dl.getSignatureByAttendeeId(attendeeId);
        if (attendeeId != -1) {
            if (signatureName != null) {
                File directory = new File(getFilesDir(), "signatures");
                File imageFile = new File(directory, signatureName);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    attendeeSignature.setImageBitmap(bitmap);
                }
            } else {
                attendeeSignature.setImageDrawable(getResources().getDrawable(R.drawable.empty_signature));
            }
        }
    }
    //------------------------Temp Testing for OW-431 - End-----------------------------

    private void bindSpinners() {

        Cursor courses = dl.getSimpleSpinnerData(OFISDBContract.Course.TABLE_NAME, false, null, null, true, OFISDBContract.Course.COLUMN_NAME_NAME + columnSuffix, OFISDBContract.Course.COLUMN_NAME_NAME + columnSuffix);
        dl.bindSimpleDataDrivenSpinner(courseSpinner, courses, context, OFISDBContract.Course.COLUMN_NAME_NAME + columnSuffix);

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Cursor trainingModules = dl.getSimpleSpinnerData(OFISDBContract.TrainingModule.TABLE_NAME,
                        true,
                        OFISDBContract.TrainingModule.COLUMN_NAME_COURSE_ID,
                        (int)id,
                        true,
                        OFISDBContract.TrainingModule.COLUMN_NAME_NAME + columnSuffix,
                        OFISDBContract.TrainingModule.COLUMN_NAME_NAME + columnSuffix);

                dl.bindSimpleDataDrivenSpinner(trainingModuleSpinner, trainingModules, context, OFISDBContract.TrainingModule.COLUMN_NAME_NAME + columnSuffix);
                if (trainingModules.getCount() > 0) {
                    trainingModuleSpinner.setEnabled(true);
                }

                if (trainingModuleId != 0) {
                    dl.setSpinnerSelectionById((int)trainingModuleId, trainingModuleSpinner);
                    if (trainingModuleSpinner.getCount() == 1) {
                        trainingModuleSpinner.setEnabled(false);
                    }
                } else {
                    if (trainingModules.getCount() == 1) {
                        trainingModuleSpinner.setSelection(1, false);
                        trainingModuleSpinner.setEnabled(false);
                    }
                }

                if (readOnlyMode) {
                    trainingModuleSpinner.setEnabled(false);
                }

                courseId = id;

                //------------------------Temp Testing for OW-431 - Start-----------------------------
                /*MergeCursor item = (MergeCursor) parent.getItemAtPosition(position);
                String selectedSpinnerItem = String.valueOf(item.getString(0));
                //String selectedSpinnerItem = parent.getItemAtPosition(position).toString();

                if(!selectedSpinnerItem.equalsIgnoreCase("Please Select")) {
                    if(tempTest(selectedSpinnerItem)) {
                        Toast.makeText(TrainingRecord.this, "Multi-Select Called...", Toast.LENGTH_LONG).show();
                        trainingModuleSpinner.setEnabled(false);
                    } else {
                        trainingModuleSpinner.setEnabled(true);
                    }
                }*/
                //------------------------Temp Testing for OW-431 - End-----------------------------
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        if (courseId != 0) {
            dl.setSpinnerSelectionById((int)courseId, courseSpinner);
        }

        if (readOnlyMode) {
            courseSpinner.setEnabled(false);
        }

        trainingModuleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean hasSwapped = (trainingModuleId != id);
                trainingModuleId = id;

                // get the capture training Attendee eligibility
                if (trainingModuleId != 0 ) {
                    isCaptureAttendee = dl.isCaptureAttendeeEligible(trainingModuleId);
                    if (!readOnlyMode) {
                        signatureAllowed = dl.isSignatureAllowed(trainingModuleId);
                        if (signatureAllowed && !dl.areAllSignaturesPresent(trainingRecordId)) {
                            completeTrainingRecord.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_disabled));
                        } else {
                            completeTrainingRecord.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_background));
                        }
                    }
                    getTrainingAttendees();
                    getTrainingAttendeesStakeholder();
                }

                if (editMode && trainingModuleId != 0 && !firstLoad && hasSwapped) {
                    addOrUpdateTrainingRecord();
                    getTrainingRecord();

                    if (trainingAttendees != null && trainingAttendees.getCount() > 0) {
                        dl.wipeAttendeeScores(trainingRecordId);
                        getTrainingAttendees();
                    }

                    if (trainingAttendeesStakeholder != null && trainingAttendeesStakeholder.getCount() > 0) {
                        dl.wipeAttendeeScores(trainingRecordId);
                        getTrainingAttendeesStakeholder();                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (trainingModuleId != 0) {
            dl.setSpinnerSelectionById((int)trainingModuleId, trainingModuleSpinner);
            if (trainingModuleSpinner.getCount() == 1) {
                trainingModuleSpinner.setEnabled(false);
            }
        }

        if (readOnlyMode) {
            trainingModuleSpinner.setEnabled(false);
        }

        new CountDownTimer(1000, 100) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (courseId != 0) {
                    dl.setSpinnerSelectionById((int)courseId, courseSpinner);
                }
                if (trainingModuleId != 0) {
                    dl.setSpinnerSelectionById((int)trainingModuleId, trainingModuleSpinner);
                }
                firstLoad = false;
            }
        }.start();

    }

    private void addOrUpdateTrainingRecord() {
        ContentValues values = new ContentValues();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        values.put(OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID, trainingModuleId);
        values.put(OFISDBContract.TrainingRecord.COLUMN_NAME_COURSE_ID, courseId);
        values.put(OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME, timestamp.toString());
        if (mCurrentPhotoPath != null) {
            values.put(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO, getCurrentPhotoPath());
        }

        if (editMode) {
            // edit
            if (dl.updateTrainingRecord(values, trainingRecordId) != 0) {
                Toast.makeText(context, res.getString(R.string.Training_Record_Edited), Toast.LENGTH_LONG).show();
            }
        } else {
            // insert

//            if (mCurrentLocation==null){
//                gpsTracker.getDeviceLocation();
//                return;
//            }

            trainingRecordId = dl.insertTrainingRecord(values);
            if (trainingRecordId != 0) {
                editMode = true;
                Toast.makeText(context, res.getString(R.string.Training_Record_Added), Toast.LENGTH_LONG).show();
            }

        }
    }


    private void bindButtons() {
        ImageButton addTrainingAttendees = (ImageButton) findViewById(R.id.addTrainingAttendees);
        if (readOnlyMode) {
            addTrainingAttendees.setVisibility(View.GONE);
        } else {
            addTrainingAttendees.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (courseId == 0 || trainingModuleId == 0 || mCurrentPhotoPath == null) {
                        Toast.makeText(context, res.getString(R.string.Training_select_course_module_first), Toast.LENGTH_LONG).show();
                    } else {
                        addOrUpdateTrainingRecord();
                        Intent intent = new Intent(context, TrainingAttendees.class);
                        intent.putExtra(EDIT_TRAININGRECORD_ID, trainingRecordId);
                        intent.putExtra(COURSE_ID, courseId);
                        intent.putExtra(TRAINING_MODULE_ID, trainingModuleId);
                        intent.putExtra(TRAINING_PHOTO, mCurrentPhotoPath);
                        intent.putExtra(EDIT_MODE, editMode);
                        intent.putExtra(Utilities.IS_STAKE_HOLDER , false);
                        context.startActivity(intent);
                    }
                }
            });
        }

        ImageButton addAttendeesStakeholder = (ImageButton) findViewById(R.id.addTrainingAttendeesStakeholder);
        if (readOnlyMode) {
            addAttendeesStakeholder.setVisibility(View.GONE);
        } else {
            addAttendeesStakeholder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (courseId == 0 || trainingModuleId == 0 || mCurrentPhotoPath == null) {
                        Toast.makeText(context, res.getString(R.string.Training_select_course_module_first), Toast.LENGTH_LONG).show();
                    } else {
                        addOrUpdateTrainingRecord();
                        Intent intent = new Intent(context, TrainingAttendees.class);
                        intent.putExtra(EDIT_TRAININGRECORD_ID, trainingRecordId);
                        intent.putExtra(COURSE_ID, courseId);
                        intent.putExtra(TRAINING_MODULE_ID, trainingModuleId);
                        intent.putExtra(TRAINING_PHOTO, mCurrentPhotoPath);
                        intent.putExtra(EDIT_MODE, editMode);
                        intent.putExtra(Utilities.IS_STAKE_HOLDER , true);
                        context.startActivity(intent);
                    }
                }
            });
        }

        ImageButton saveTrainingRecord = (ImageButton) findViewById(R.id.saveTrainingRecord);
        if (readOnlyMode) {
            saveTrainingRecord.setVisibility(View.GONE);
        } else {
            saveTrainingRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (courseId != 0 && trainingModuleId != 0 && mCurrentPhotoPath != null) {
                        addOrUpdateTrainingRecord();
                    } else {
                        Toast.makeText(context, res.getString(R.string.Training_select_course_module_first), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        Button addAttendeeScores = (Button) findViewById(R.id.addAttendeeScores);
        if (readOnlyMode) {
            addAttendeeScores.setVisibility(View.GONE);
        } else {
            if (scoringColumns != null) {
                if (scoringColumns.length() == 1) {
                    if (scoringColumns.keys().next().equalsIgnoreCase("attended")) {
                        addAttendeeScores.setVisibility(View.GONE);
                    }
                }
            }
            addAttendeeScores.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (trainingAttendees == null || trainingAttendees.getCount() == 0) {
                        Toast.makeText(context, res.getString(R.string.Add_one_attendee_to_continue), Toast.LENGTH_LONG).show();
                    } else {
                        addOrUpdateTrainingRecord();
                        Intent intent = new Intent(context, TrainingAttendeeScores.class);
                        intent.putExtra(EDIT_TRAININGRECORD_ID, trainingRecordId);
                        context.startActivity(intent);
                    }
                }
            });
        }

        if (readOnlyMode) {
            completeTrainingRecord.setVisibility(View.GONE);
        } else {
            completeTrainingRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (courseId == 0 || trainingModuleId == 0 || mCurrentPhotoPath == null) {
                        Toast.makeText(context, res.getString(R.string.Training_select_course_module_first), Toast.LENGTH_LONG).show();
                    }else if (trainingAttendees == null || trainingAttendees.getCount() == 0) {
                        Toast.makeText(context, res.getString(R.string.Add_one_attendee_to_continue), Toast.LENGTH_LONG).show();
                    } else if (signatureAllowed) {
                        if (!dl.areAllSignaturesPresent(trainingRecordId)){
                            Toast.makeText(context, getResources().getString(R.string.sign_all_attendee), Toast.LENGTH_LONG).show();
                        } else {
                            addOrUpdateTrainingRecord();
                            Intent intent = new Intent(context, TrainingRecordConfirmation.class);
                            intent.putExtra(EDIT_TRAININGRECORD_ID, trainingRecordId);
                            currentPhotoPathBackup = null;
                            context.startActivity(intent);
                        }
                    }else {
                        addOrUpdateTrainingRecord();
                        Intent intent = new Intent(context, TrainingRecordConfirmation.class);
                        intent.putExtra(EDIT_TRAININGRECORD_ID, trainingRecordId);
                        currentPhotoPathBackup = null;
                        context.startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (editMode) {
            getTrainingAttendees();
            getTrainingAttendeesStakeholder();
            if (signatureAllowed && !dl.areAllSignaturesPresent(trainingRecordId)) {
                completeTrainingRecord.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_disabled));
            } else {
                completeTrainingRecord.setBackgroundColor(getResources().getColor(R.color.ofis_action_button_background));
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        ImageView trainingPhoto = (ImageView) findViewById(R.id.trainingPhoto);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == -1) {
                  //  gpsTracker.getDeviceLocation();
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    setFullImageFromFilePath(getCurrentPhotoPath(), trainingPhoto);
                } else {
                    if (currentPhotoPathBackup != null) {
                        setCurrentPhotoPath(currentPhotoPathBackup);
                    }
                }
        }
    }
    private void bindTrainingPhotoButton() {
        currentPhotoPathBackup = getCurrentPhotoPath();
        // New
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            FirebaseCrash.report(new Exception("training record: Error creating photo image file", e));
            e.printStackTrace();
        }

        if (photoFile != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if ((intent.resolveActivity(getPackageManager()) != null)) {
                Uri photoURI = FileProvider.getUriForFile(TrainingRecord.this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                setCapturedImageURI(photoURI);
                setCurrentPhotoPath(photoFile.getPath());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getCapturedImageURI());
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }else{
                Toast.makeText(TrainingRecord.this, "Gallery is not available in Mobile", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showSettingsDialog(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Permission was permanently denied");
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName())));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(TrainingRecord.this, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
                    }
                });
        dialog.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                for (String permission : permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        Toast.makeText(this, R.string.camera_and_gallery_permission_denial_msg, Toast.LENGTH_LONG).show();
                    } else {
                        if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                            bindTrainingPhotoButton();
                        } else {
                            showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
                        }
                    }
                }
            }
            case REQUEST_PERMISSIONS_FOR_TIRAMISU: {
                try {
                    if (grantResults.length > 0 &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        bindTrainingPhotoButton();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                            && (!shouldShowRequestPermissionRationale(permissions[0])
                    )) {
                        showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
                    }else {
                        Toast.makeText(this, R.string.permission_denial_msg_android_13_above, Toast.LENGTH_LONG).show();
                    }
                    break;
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
