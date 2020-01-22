package com.olam.digital.ofis;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.model.NationalIdDTO;
import com.olam.digital.ofis.model.NationalIdTypeDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

// update
public class AddFarmerActivity extends BaseActivity implements View.OnClickListener{

    private Context context;
    private Resources res;
    private long editFarmerId;
    private boolean editMode;
    private int selectedCountry = 0, selectedRegion = 0, selectedDistrict = 0, selectedPlace = 0, selectedFarmerGroup = 0, selectedSection = 0;
    ArrayAdapter<String> spinnerArrayAdapter;
    private Spinner genderspinner, countrySpinner, regionSpinner, districtSpinner, placeSpinner, farmerGroupSpinner, sectionSpinner;
    Spinner spinner_nidtype;
    ImageButton farmerPhotoActionButton;
    ImageButton farmerSignatureButton;
    Button captureSignature;
    EditText newPlaceName;
    private final static String LIMIT_XED = "limit";
    private final static String DUPLICATE = "duplicate";
    private final static String ENTER_NID = "novalues";
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;
    private final static int REQUEST_IMAGE_CAPTURE_NATIONALID = 4;
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";
    private final static String CAPTURED_SIGNATURE_PHOTO_PATH_KEY = "mCurrentSignaturePhotoPath";
    private final static String CAPTURED_SIGNATURE_PHOTO_URI_KEY = "mCapturedSignatureImageURI";
    private final static String CAPTURED_DATE_STR_TO_SEND = "dateStrToSend";
    private final static String CAPTURED_SELECTED_COUNTRY = "mCapturedCountry";
    private final static String CAPTURED_SELECTED_REGION = "mCapturedRegion";
    private final static String CAPTURED_SELECTED_DISTRICT = "mCapturedDistrict";
    private final static String CAPTURED_SELECTED_PLACE = "mCapturedPlace";
    private final static String CAPTURED_SELECTED_FARMERGROUP = "mCapturedFarmerGroup";
    private final static String CAPTURED_SELECTED_SECTION = "mCapturedSection";
    private final static String EDIT_FARMER_ID = "EDIT_FARMER_ID";
    private final static String EDIT_MODE = "EDIT_MODE";
    private final static String SELECT_NATIONALID = "ID TYPE";

    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null, mCurrentSignaturePhotoPath = null;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null;

    private String dateStrToSend;
    //    private EditText farmerDOB;
    private TextView farmerDOB;
    private EditText farmerIdPrefix;
    private LinearLayout phonenumbers;
    private LinearLayout nationalids;
    private LayoutInflater inflater;
    private JSONArray storeableNumbers;
    private JSONArray storeableNationalids = new JSONArray();

    private DataLayer dl;

    private final Hashtable<String, String> genders = new Hashtable<>();

    // Runtime Permission Request Code
    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
    private final static int MY_NATIONALID_PIC_PERMISSIONS_REQUEST = 9997;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    ArrayList<String> phoneNumberList;
    ArrayList<String> nationalidList;
    ArrayList<String> selectednationalidList = new ArrayList<String> ();;
    ArrayList<String> nidtype;
    ArrayList<NationalIdTypeDTO> nidtypelist;
    int currentcapture_position;
    HashMap<Integer, Uri> hm_uri = new HashMap<>();
    HashMap<Integer, String> hm_uripath = new HashMap<>();
    private String blockCharacterSet = "~#^|$%&*!'\"";
    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };


    // Location captured related variable
    private Location mCurrentLocation = null;
    GPSListener gpsListener = null;
    private GPSTracker gpsTracker = null;
    private int nidvalue;
    boolean editnidmode = false;
    boolean edit_signature = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farmer);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        res = getResources();
        Intent intent = getIntent();
        editFarmerId = intent.getLongExtra(EDIT_FARMER_ID, 0);
        editMode = intent.getBooleanExtra(EDIT_MODE, false);

        String localeSuffix = Utilities.getLocaleSuffix(res);

        genders.put("Male" + localeSuffix, res.getString(R.string.gender_male));
        genders.put("Female" + localeSuffix, res.getString(R.string.gender_female));

        dl = new DataLayer(res, context);
        farmerIdPrefix = (EditText) findViewById(R.id.olamFarmerIdStart);
        phonenumbers = (LinearLayout) findViewById(R.id.phonenumbers);
        nationalids = (LinearLayout) findViewById(R.id.nationalids);
        farmerSignatureButton = (ImageButton) findViewById(R.id.farmerSignature);

        bindphonenumbers();
        dl.clearlocalniddbvalue();
        if(!editMode) {
            bindnationalid();
        }else {
            bindNationalidValues();
        }
        if (editMode) {
            if (editFarmerId != 0) {
                Cursor existingItem = dl.getFlattenedFarmer(editFarmerId);
                if (existingItem.getCount() > 0) {
                    existingItem.moveToFirst();
                    populateExistingValues(existingItem);
                }
            }
        }

        countrySpinner = (Spinner) this.findViewById(R.id.spinner_country);
        regionSpinner = (Spinner) this.findViewById(R.id.spinner_region);
        districtSpinner = (Spinner) this.findViewById(R.id.spinner_district);
        placeSpinner = (Spinner) this.findViewById(R.id.spinner_place);
        farmerGroupSpinner = (Spinner) this.findViewById(R.id.spinner_farmerGroup);
        sectionSpinner = (Spinner) this.findViewById(R.id.spinner_section);

        dl.createBusinessHierarchySpinners(this, context, farmerIdPrefix, selectedFarmerGroup, selectedSection);
        dl.createGeographyHierarchySpinners(this, context, farmerIdPrefix, selectedCountry, selectedRegion, selectedDistrict, selectedPlace, phonenumbers, true);

        farmerPhotoActionButton = (ImageButton) findViewById(R.id.farmerPhotoActionButton);
        farmerPhotoActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request Permission only for Android Version Marshmallow and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Check Camera & Storage Permission
                    if (ContextCompat.checkSelfPermission(AddFarmerActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                            .checkSelfPermission(AddFarmerActivity.this,
                                    android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        //request permission
                        ActivityCompat.requestPermissions(AddFarmerActivity.this,
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

        bindDateField();
        bindSaveButton();
        bindConsentButton();


        gpsListener = new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location != null) {
                    mCurrentLocation = location;
                }
            }
        };
        gpsTracker = new GPSTracker(this, gpsListener);
        if (gpsTracker.isGpsEnabled()){
            gpsTracker.startLocationUpdateInitially();
        }

    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
        if (mCapturedImageURI != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_URI_KEY, mCapturedImageURI.toString());
        }
        if (mCurrentSignaturePhotoPath != null) {
            savedInstanceState.putString(CAPTURED_SIGNATURE_PHOTO_PATH_KEY, mCurrentSignaturePhotoPath);
        }
        if (mCapturedSignatureImageURI != null) {
            savedInstanceState.putString(CAPTURED_SIGNATURE_PHOTO_URI_KEY, mCapturedSignatureImageURI.toString());
        }
        if (dateStrToSend != null) {
            savedInstanceState.putString(CAPTURED_DATE_STR_TO_SEND, dateStrToSend);
        }

        selectedCountry = (int) countrySpinner.getSelectedItemId();
        selectedRegion = (int) regionSpinner.getSelectedItemId();
        selectedDistrict = (int) districtSpinner.getSelectedItemId();
        selectedPlace = (int) placeSpinner.getSelectedItemId();
        selectedFarmerGroup = (int) farmerGroupSpinner.getSelectedItemId();
        selectedSection = (int) sectionSpinner.getSelectedItemId();


        savedInstanceState.putInt(CAPTURED_SELECTED_COUNTRY, selectedCountry);
        savedInstanceState.putInt(CAPTURED_SELECTED_REGION, selectedRegion);
        savedInstanceState.putInt(CAPTURED_SELECTED_DISTRICT, selectedDistrict);
        savedInstanceState.putInt(CAPTURED_SELECTED_PLACE, selectedPlace);
        savedInstanceState.putInt(CAPTURED_SELECTED_FARMERGROUP, selectedFarmerGroup);
        savedInstanceState.putInt(CAPTURED_SELECTED_SECTION, selectedSection);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        ImageButton farmerPhotoButton = (ImageButton) findViewById(R.id.farmerPhoto);
        ImageButton farmerSignatureButton = (ImageButton) findViewById(R.id.farmerSignature);

        if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI_KEY)) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI_KEY));
        }
        if (savedInstanceState.containsKey(CAPTURED_SIGNATURE_PHOTO_PATH_KEY)) {
            mCurrentSignaturePhotoPath = savedInstanceState.getString(CAPTURED_SIGNATURE_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_SIGNATURE_PHOTO_URI_KEY)) {
            mCapturedSignatureImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_SIGNATURE_PHOTO_URI_KEY));
        }
        if (savedInstanceState.containsKey(CAPTURED_DATE_STR_TO_SEND)) {
            dateStrToSend = savedInstanceState.getString(CAPTURED_DATE_STR_TO_SEND);
        }
        if (mCurrentPhotoPath != null) {
            displayBitmap(mCurrentPhotoPath, "portrait", farmerPhotoButton);
        }
        if (mCurrentSignaturePhotoPath != null) {
            displayBitmap(mCurrentSignaturePhotoPath, "signature", farmerSignatureButton);
        }
        selectedCountry = savedInstanceState.getInt(CAPTURED_SELECTED_COUNTRY, 0);
        selectedRegion = savedInstanceState.getInt(CAPTURED_SELECTED_REGION, 0);
        selectedDistrict = savedInstanceState.getInt(CAPTURED_SELECTED_DISTRICT, 0);
        selectedPlace = savedInstanceState.getInt(CAPTURED_SELECTED_PLACE, 0);
        selectedFarmerGroup = savedInstanceState.getInt(CAPTURED_SELECTED_FARMERGROUP, 0);
        selectedSection = savedInstanceState.getInt(CAPTURED_SELECTED_SECTION, 0);

        dl.createBusinessHierarchySpinners(this, context, farmerIdPrefix, selectedFarmerGroup, selectedSection);
        dl.createGeographyHierarchySpinners(this, context, farmerIdPrefix, selectedCountry, selectedRegion, selectedDistrict, selectedPlace, phonenumbers, true);

        super.onRestoreInstanceState(savedInstanceState);
    }

    private String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    private void setCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }

    private String getCurrentSignaturePhotoPath() {
        return mCurrentSignaturePhotoPath;
    }

    private void setCurrentSignaturePhotoPath(String mCurrentSignaturePhotoPath) {
        this.mCurrentSignaturePhotoPath = mCurrentSignaturePhotoPath;
    }

    private void setCurrentNIDPhotoPath(HashMap<Integer, String> hm_uripath) {
        this.hm_uripath = hm_uripath;
    }

    private HashMap<Integer, String> getCurrentNIDPhotoPath() {
        return hm_uripath;
    }

    private Uri getCapturedImageURI() {
        return mCapturedImageURI;
    }

    private void setCapturedImageURI(Uri mCapturedImageURI) {
        this.mCapturedImageURI = mCapturedImageURI;
    }

    private Uri getCapturedSignatureImageURI() {
        return mCapturedSignatureImageURI;
    }

    private void setCapturedSignatureImageURI(Uri mCapturedSignatureImageURI) {
        this.mCapturedSignatureImageURI = mCapturedSignatureImageURI;
    }


    private void setCapturedNIDImageURI(HashMap<Integer,Uri> hm_uri) {
        this.hm_uri = hm_uri;
    }

    private HashMap<Integer,Uri> getCapturedNIDImageURI() {
        return hm_uri;
    }

    private void populateExistingValues(final Cursor existingItem) {
        EditText farmerName = (EditText) findViewById(R.id.farmerName);
        farmerName.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
        farmerName.setFilters(new InputFilter[]{filter});
        farmerDOB = (TextView) findViewById(R.id.farmerDOB);
        dateStrToSend = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB));
        farmerDOB.setText(dateStrToSend);

        selectedCountry = existingItem.getInt(existingItem.getColumnIndex("country_id"));

        selectedRegion = existingItem.getInt(existingItem.getColumnIndex("region_id"));

        selectedDistrict = existingItem.getInt(existingItem.getColumnIndex("district_id"));

        selectedPlace = existingItem.getInt(existingItem.getColumnIndex("place_id"));

        selectedFarmerGroup = existingItem.getInt(existingItem.getColumnIndex("farmergroup_id"));

        selectedSection = existingItem.getInt(existingItem.getColumnIndex("section_id"));

        genderspinner = (Spinner) findViewById(R.id.farmerGender);
        new CountDownTimer(1000, 100) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                String existingGender = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER));
                String existingGenderKey = existingGender + Utilities.getLocaleSuffix(res);
                if (genders.containsKey(existingGenderKey)) {
                    existingGender = genders.get(existingGenderKey);
                }

                Utilities.setSpinnerSelectionByValue(existingGender, genderspinner);
            }
        }.start();

        farmerIdPrefix.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)).substring(0, 13));
        EditText farmerIdSuffix = (EditText) findViewById(R.id.olamFarmerIdEnd);
        farmerIdSuffix.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)).substring(13));

        String farmerPhotoPath = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
        if (farmerPhotoPath != null) {
            ImageButton farmerPhoto = (ImageButton) findViewById(R.id.farmerPhoto);
            if (farmerPhotoPath.length() > 0) {
                Uri uri = Uri.parse(farmerPhotoPath);
                setCapturedImageURI(uri);
                setCurrentPhotoPath(uri.getPath());

                // set our desired width and height for display
                int baseWidth = 100;
                int baseHeight = 133;

                Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

                Bitmap existingBitmap = Utilities.loadSizedBitmap(getCurrentPhotoPath(), outputWidth, outputHeight);

                farmerPhoto.setImageBitmap(existingBitmap);
            }
        }
        ImageButton farmerSignature = (ImageButton) findViewById(R.id.farmerSignature);
        String farmerSignaturePhotoPath = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE));
        if (farmerSignaturePhotoPath != null) {
            if (farmerSignaturePhotoPath.length() > 0) {
                Uri uri = Uri.parse(farmerSignaturePhotoPath);
                setCapturedSignatureImageURI(uri);
                setCurrentSignaturePhotoPath(uri.getPath());

                // set our desired width and height for display
                int baseWidth = 82;
                int baseHeight = 28;

                Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

                Bitmap existingSignatureBitmap = Utilities.loadSizedBitmap(farmerSignaturePhotoPath, outputWidth, outputHeight);

                farmerSignature.setImageBitmap(existingSignatureBitmap);
            }else {
                farmerSignature.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.empty_signature));
            }
        }else{
            farmerSignature.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.empty_signature));
        }

        Switch consentFarmer = (Switch) findViewById(R.id.consentFarmer);
        consentFarmer.setChecked(true);
        consentFarmer.setEnabled(false);

        Switch consentOthers = (Switch) findViewById(R.id.consentOthers);
        consentOthers.setChecked(true);
        consentOthers.setEnabled(false);

    }

    private void getAllPhoneNumbers() {
        phoneNumberList = new ArrayList<String> ();

        int phonerowscount = phonenumbers.getChildCount();

        for (int j = 0; j < phonerowscount; j++) {
            LinearLayout phonenumberrow = (LinearLayout) phonenumbers.getChildAt(j);

            EditText phonenumber = (EditText) phonenumberrow.findViewById(R.id.phonenumber);
            String phonenumberValue = String.valueOf(phonenumber.getText()).replace(" ", "");
            if(phonenumberValue.trim().length() > 0){
                phoneNumberList.add(phonenumberValue);
            }
        }
    }

    private void getAllNationalIds() {
        nationalidList = new ArrayList<String> ();

        int nidrowscount = nationalids.getChildCount();

        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);

            EditText nidnumber = (EditText) nidrow.findViewById(R.id.nidnumber);
            String nidnumberValue = String.valueOf(nidnumber.getText()).replace(" ", "");
            if(nidnumberValue.trim().length() > 0){
                nationalidList.add(nidnumberValue);
            }
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


            if (phonenumberValue.length() == 0) {

                duplicatePhoneNumberValidationFlag = true;
            } else if (Collections.frequency(phoneNumberList, phonenumberValue.trim()) == 1) {
                duplicatePhoneNumberValidationFlag = true;
            } else {
                duplicatePhoneNumberValidationFlag = false;
                break;
            }
        }

        return duplicatePhoneNumberValidationFlag;
    }


    private boolean duplicateNationalidsValidation() {
        boolean duplicateNationalidsValidationFlag = false;

        getAllNationalIds();

        int rowscount = nationalids.getChildCount();

        for (int j = 0; j < rowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);

            EditText nidnumber = (EditText) nidrow.findViewById(R.id.nidnumber);
            String nidnumberValue = String.valueOf(nidnumber.getText()).replace(" ", "");

            if(nidnumberValue.length()==0){

                duplicateNationalidsValidationFlag = true;
            }



            else if (Collections.frequency(nationalidList,nidnumberValue.trim()) == 1) {
                duplicateNationalidsValidationFlag = true;
            } else {
                duplicateNationalidsValidationFlag = false;
                break;
            }
        }

        return duplicateNationalidsValidationFlag;
    }

    private ContentValues validateValues() {

        ContentValues values = new ContentValues();


        Long username = Long.parseLong(new AuthenticationHelper(AddFarmerActivity.this).getuserName());
        Long time = System.currentTimeMillis();
        String olamFarmerId = String.valueOf(username + time);

//        String olamFarmerId = farmerIdPrefix.getText().toString() + farmerIdSuffixValue;

        EditText farmerName = (EditText) findViewById(R.id.farmerName);
        String farmerNameValue = String.valueOf(farmerName.getText()).trim();
        if (farmerNameValue.equals("")) {
            farmerName.setText("");
            Toast.makeText(context, res.getString(R.string.please_fill_farmer_name), Toast.LENGTH_LONG).show();
            return values;


        }

        if (dateStrToSend == null || dateStrToSend.equals("")) {

            Toast.makeText(context, res.getString(R.string.please_fill_dob), Toast.LENGTH_LONG).show();
            return values;

        }
        genderspinner = (Spinner) findViewById(R.id.farmerGender);
        String genderValue = Utilities.getDefaultTextByLocalValue(genders, String.valueOf(genderspinner.getSelectedItem()), res);
        if (genderValue.equals("")) {
            return values;
        }
        Spinner spinner_section = (Spinner) findViewById(R.id.spinner_section);
        long section_id = spinner_section.getSelectedItemId();


        if (section_id < 1) {

            selectedCountry = (int) countrySpinner.getSelectedItemId();
            selectedRegion = (int) regionSpinner.getSelectedItemId();
            selectedDistrict = (int) districtSpinner.getSelectedItemId();
            selectedPlace = (int) placeSpinner.getSelectedItemId();
            selectedFarmerGroup = (int) farmerGroupSpinner.getSelectedItemId();
            selectedSection = (int) sectionSpinner.getSelectedItemId();

            String message = "";
            if (selectedCountry == 0) {
                message = "Country";
            } else if (selectedRegion == 0) {

                message = "Region";

            } else if (selectedDistrict == 0) {
                message = "District";
            } else if (selectedPlace == 0) {

                message = "Place";

            } else if (selectedFarmerGroup == 0) {

                message = "farmer group";

            } else if (selectedSection == 0) {
                message = "section";

            }


            Toast.makeText(context, "Please select the " + message, Toast.LENGTH_LONG).show();
            return values;
        }

        Spinner districtSpinner = (Spinner) findViewById(R.id.spinner_district);
        long district_id = districtSpinner.getSelectedItemId();

        newPlaceName = (EditText) findViewById(R.id.newPlaceName);
        Spinner spinner_place = (Spinner) findViewById(R.id.spinner_place);
        long place_id = spinner_place.getSelectedItemId();
        if (place_id == 0) {

            Toast.makeText(context, res.getString(R.string.please_fill_the_place), Toast.LENGTH_LONG).show();
            return values;

        }
        String newPlaceNameValue = (place_id == -1) ? String.valueOf(newPlaceName.getText()) : "";
        if (place_id == -1) {
            if (newPlaceNameValue.length() == 0) {

                Toast.makeText(context, res.getString(R.string.please_fill_the_place), Toast.LENGTH_LONG).show();
                return values;
            } else {
                if (dl.placeAlreadyExists(newPlaceNameValue, district_id)) {
                    Toast.makeText(context, res.getString(R.string.place_name_not_unique), Toast.LENGTH_LONG).show();


                    return values;
                }
            }
        }

        if (place_id == -1 && newPlaceName.length() > 0) {
            place_id = dl.insertNewPlace(newPlaceNameValue, district_id);
        }

        Switch consentFarmer = (Switch) findViewById(R.id.consentFarmer);
        if (!consentFarmer.isChecked()) {
            Toast.makeText(context, res.getString(R.string.Please_accept_both_consents), Toast.LENGTH_LONG).show();
            return values;
        }

        Switch consentOthers = (Switch) findViewById(R.id.consentOthers);
        if (!consentOthers.isChecked()) {
            Toast.makeText(context, res.getString(R.string.Please_accept_both_consents), Toast.LENGTH_LONG).show();
            return values;
        }

//        fetch national id values
       /* int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);
            EditText nidnumber = (EditText) nidrow.findViewById(R.id.nidnumber);
            Spinner nid_type = (Spinner) nidrow.findViewById(R.id.idtypespinner);

            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            JSONObject storeableNids = new JSONObject();
            try {
                storeableNids.put("nationalidtype", type);
                storeableNids.put("nationalidNumber", nid);
                storeableNids.put("photo", hm_uripath.get(j+1));
                storeableNationalids.put(storeableNids);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating storable phone number", e));
                e.printStackTrace();
            }

        }*/
        if(getnationalidSubmitStatus().equalsIgnoreCase(DUPLICATE)) {
            Toast.makeText(this,getResources().getString(R.string.duplicate_nationalids),Toast.LENGTH_SHORT).show();
            return values;
        }else if(getnationalidSubmitStatus().equalsIgnoreCase(LIMIT_XED)){
            Toast.makeText(this,getResources().getString(R.string.duplicate_nationalids),Toast.LENGTH_SHORT).show();
            return values;
        }else if(getnationalidSubmitStatus().equalsIgnoreCase(ENTER_NID)){
            Toast.makeText(this,getResources().getString(R.string.enter_nationalids),Toast.LENGTH_SHORT).show();
            return values;
        }
       if(!ValidateNIDValues()) {
           return values;
       }
       storeableNationalids = fetchnationalidvalues();

        int phonerowscount = phonenumbers.getChildCount();
        /*boolean hasNumber = false;
        boolean hasMainNumber = false;*/
        long countryId = countrySpinner.getSelectedItemId();
        long regionId = regionSpinner.getSelectedItemId();
        long districtId = districtSpinner.getSelectedItemId();
        long farmerGroupId = farmerGroupSpinner.getSelectedItemId();
        storeableNumbers = new JSONArray();
        for (int j = 0; j < phonerowscount; j++) {
            LinearLayout phonenumberrow = (LinearLayout) phonenumbers.getChildAt(j);

            EditText phonenumber = (EditText) phonenumberrow.findViewById(R.id.phonenumber);
            RadioButton primary_number = (RadioButton) phonenumberrow.findViewById(R.id.primary_number);
            String phonenumberValue = String.valueOf(phonenumber.getText()).replace(" ", "");

            if (phonenumberValue.length() > 0 && PhoneNumberUtils.isGlobalPhoneNumber(phonenumberValue)) {
                //hasNumber = true;
                JSONObject storeableNumber = new JSONObject();
                try {
                    storeableNumber.put("country_id", countryId);
                    storeableNumber.put("primary_number", primary_number.isChecked());
                    storeableNumber.put("phonenumber", phonenumberValue);
                    storeableNumbers.put(storeableNumber);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating storable phone number", e));
                    e.printStackTrace();
                }
            }
        }

        /**
         * Here we used SharedPreference to map OLAM Farmer ID with UUID. It is used to find the plot count value in ViewFarmerActivity class.
         * Plot count value is important to showing alert to enter plots number of farmer at first time.
         */

//        SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(olamFarmerId, String.valueOf(generateUniqueLongNumber()));

        try {
            if (editMode) {
                if (edit_signature)
                    values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE, getCurrentSignaturePhotoPath());
            }else {
                values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE, getCurrentSignaturePhotoPath());
            }
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME, farmerNameValue);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY, 1);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB, dateStrToSend);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER, genderValue);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO, getCurrentPhotoPath());
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID, place_id);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID, section_id);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED, 0);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES, "{}");
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, district_id);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_FARMER, 1);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_OTHERS, 1);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID, String.valueOf(countryId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID, String.valueOf(regionId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID, String.valueOf(districtId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID, String.valueOf(farmerGroupId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID,String.valueOf(countryId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID,String.valueOf(regionId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID,String.valueOf(districtId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID,String.valueOf(farmerGroupId));
            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String registereddate = format.format(today);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_REGISTER_DATE, registereddate);

        } catch (Exception e) {
            FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating farmer content values", e));
            values.clear();
        }

        return values;
    }

    private JSONArray fetchnationalidvalues() {
        Cursor nids = dl.getnidsfromlocaldb();
        JSONArray storeableNationalids = new JSONArray();
        try {
            while (nids.moveToNext()) {
                JSONObject storeableNids = new JSONObject();
                    storeableNids.put("nationalIdType", new Long(nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE))));
                    storeableNids.put("nationalIdNumber", nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER)));
                    storeableNids.put("photo", nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO)));

                    storeableNationalids.put(storeableNids);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return storeableNationalids;
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

    private void bindniddelete(LinearLayout trashbutton) {
        trashbutton.setTag(nationalids.getChildCount() -1);
        trashbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showtoast((Integer) trashbutton.getTag());
                LinearLayout numberrow = (LinearLayout) v.getParent();
                removeSpinnerItem(numberrow);
                ((ViewGroup) numberrow.getParent()).removeView(numberrow);

                removehmvalue(hm_uripath, (int) trashbutton.getTag());
                removehmURIvalue(hm_uri, (int) trashbutton.getTag());
                refreshTags();
            }
        });
    }



    private void removeSpinnerItem(LinearLayout numberrow) {
        try {
            Spinner spinner = (Spinner) numberrow.findViewById(R.id.idtypespinner);
            String val = spinner.getSelectedItem().toString();

            for (int i = 0; i < selectednationalidList.size(); i++) {
                if (val.equalsIgnoreCase(selectednationalidList.get(i))) {
                    selectednationalidList.remove(i);
                }
            }
            int type = getNidType(val);
            dl.removeNIDPhoto(type);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void removehmvalue(HashMap<Integer, String> hm_uripath, int val) {
        Iterator<Integer> iterator = hm_uripath.keySet().iterator();
        while(iterator.hasNext()){
            Integer nid = iterator.next();
            if(nid == val){
                iterator.remove();
            }
        }
    }

    private void removehmURIvalue(HashMap<Integer, Uri> hm_uripath, int val) {
        Iterator<Integer> iterator = hm_uripath.keySet().iterator();
        while(iterator.hasNext()){
            Integer nid = iterator.next();
            if(nid == val){
                iterator.remove();
            }
        }
    }

    private void refreshTags() {
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            if(j > 0) {
                LinearLayout nationalidinclude = (LinearLayout) nationalids.getChildAt(j);
                ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
                actionbtn.setTag(j+1);
                nationalidphoto.setTag(j+1);

            }
        }
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

    private void bindphonenumbers() {
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        if (editMode) {
            Cursor existingNumbers = dl.getFarmerPhoneNumbers(editFarmerId, false);
            boolean firstNumber = true;

            while (existingNumbers.moveToNext()) {
                LinearLayout phonenumberinclude = (LinearLayout) inflater.inflate(R.layout.phonenumber_entry, phonenumbers, false);
                RadioButton numberradio = (RadioButton) phonenumberinclude.findViewById(R.id.primary_number);
                EditText numbertext = (EditText) phonenumberinclude.findViewById(R.id.phonenumber);
                ImageView numberdelete = (ImageView) phonenumberinclude.findViewById(R.id.deleteNumber);

                numberradio.setChecked(existingNumbers.getInt(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY)) == 1);
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

            if (existingNumbers.getCount() == 0) {

                LinearLayout phonenumberinclude = (LinearLayout) inflater.inflate(R.layout.phonenumber_entry, phonenumbers, false);
                phonenumbers.addView(phonenumberinclude);
                RadioButton numberradio = (RadioButton) phonenumberinclude.findViewById(R.id.primary_number);
                ImageView numberdelete = (ImageView) phonenumberinclude.findViewById(R.id.deleteNumber);
                numberdelete.setVisibility(View.GONE);
                bindnumberradio(numberradio);
                numberradio.setChecked(true);
            }

        } else {
            LinearLayout phonenumberinclude = (LinearLayout) inflater.inflate(R.layout.phonenumber_entry, phonenumbers, false);
            phonenumbers.addView(phonenumberinclude);
            RadioButton numberradio = (RadioButton) phonenumberinclude.findViewById(R.id.primary_number);
            ImageView numberdelete = (ImageView) phonenumberinclude.findViewById(R.id.deleteNumber);
            numberdelete.setVisibility(View.GONE);
            bindnumberradio(numberradio);
            numberradio.setChecked(true);
        }

        ImageButton addNumber = (ImageButton) findViewById(R.id.addNumber);
       /* if(!editMode){
            addNumber.setVisibility(View.INVISIBLE);
        }*/
        addNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout phonenumberinclude = (LinearLayout) inflater.inflate(R.layout.phonenumber_entry, phonenumbers, false);
                EditText diallingCode = null;
                try {
                    diallingCode = (EditText) phonenumbers.getChildAt(0).findViewById(R.id.dialling_code);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (diallingCode != null) {
                    EditText newDiallingCode = (EditText) phonenumberinclude.findViewById(R.id.dialling_code);
                    newDiallingCode.setText(diallingCode.getText());
                }
                phonenumbers.addView(phonenumberinclude);
                bindnumberdelete((ImageView) phonenumberinclude.findViewById(R.id.deleteNumber));
                bindnumberradio((RadioButton) phonenumberinclude.findViewById(R.id.primary_number));
            }
        });
    }

    private void bindnationalid() {
//        if (! editMode) {
        ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
        LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
        nationalids.addView(nationalidinclude);

        LinearLayout niddelete = (LinearLayout) nationalidinclude.findViewById(R.id.deleteNationalid);
        niddelete.setVisibility(View.GONE);
        spinner_nidtype = (Spinner) nationalidinclude.findViewById(R.id.idtypespinner);
        LinearLayout ll_capturephoto = (LinearLayout) nationalidinclude.findViewById(R.id.ll_capturephoto);
        spinnerArrayAdapter = new ArrayAdapter<String>(AddFarmerActivity.this, R.layout.spinner_item, refreshitem);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_nidtype.setAdapter(spinnerArrayAdapter);

        EditText nidnumber = (EditText) nationalidinclude.findViewById(R.id.nidnumber);
        ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
        ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
        actionbtn.setTag(nationalids.getChildCount());
        nationalidphoto.setTag(nationalids.getChildCount());

        String selected_item = spinner_nidtype.getSelectedItem().toString();
        spinner_nidtype.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddFarmerActivity.this, R.layout.spinner_item, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);
                    nidnumber.setText("");
                }
                return false;
            }
        });
        spinner_nidtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinner_nidtype.getSelectedItem().toString().equalsIgnoreCase(SELECT_NATIONALID)) {
//                    selectednationalidList.add(spinner_nidtype.getSelectedItem().toString());

                    int limit = getNidLimit(spinner_nidtype.getSelectedItem().toString());
                    InputFilter[] filters = new InputFilter[1];
                    filters[0] = new InputFilter.LengthFilter(limit); //Filter to 10 characters
                    nidnumber.setFilters(filters);
                    if (getNidCaptureStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                        ll_capturephoto.setVisibility(View.GONE);
                    } else {
                        ll_capturephoto.setVisibility(View.VISIBLE);
                    }
                    ValidateSpinnerTypes();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bindnationlidcapturephoto((Spinner) nationalidinclude.findViewById(R.id.idtypespinner), (ImageButton) nationalidinclude.findViewById(R.id.nidphotoActionButton));

        ImageButton addNationId = (ImageButton) findViewById(R.id.addnid);
        addNationId.setOnClickListener(this);

    }

    private void bindNationalidValues() {
        if(editFarmerId != 0) {
            Cursor nids = dl.getnationalidsbyfarmerid(editFarmerId);
//            JSONArray nidlist = new JSONArray();
            ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
            try {
                int i = 0;
                while (nids.moveToNext()) {
                    LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
                    nationalids.addView(nationalidinclude);
                    Spinner spinner_nidtype = (Spinner) nationalidinclude.findViewById(R.id.idtypespinner);
                    LinearLayout niddelete = (LinearLayout) nationalidinclude.findViewById(R.id.deleteNationalid);
                    ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                    ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
                    LinearLayout ll_capturephoto = (LinearLayout) nationalidinclude.findViewById(R.id.ll_capturephoto);
                    EditText nidnumber = (EditText) nationalidinclude.findViewById(R.id.nidnumber);
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddFarmerActivity.this, R.layout.nidtype_spinner, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.nidtype_spinner);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);
                    actionbtn.setClickable(false);
                    spinner_nidtype.setEnabled(false);
//                    nidnumber.setEnabled(false);
                    if(i ==0) {
                        niddelete.setVisibility(View.INVISIBLE);
                    }else{
                        niddelete.setVisibility(View.VISIBLE);
                    }
                    i++;
//                    JSONObject nationalids = new JSONObject();

//                    nationalids.put("farmerId", new Long(nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID))));
//                    nationalids.put("nationalIdType", new Long(nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE))));
//                    nationalids.put("nationalIdNumber", nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER)));
                    nidnumber.setText(nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER)));
                    String selected_item = getNidTypebyid(nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE)));
                    int spinnerPosition = spinnerArrayAdapter.getPosition(selected_item);
                    spinner_nidtype.setSelection(spinnerPosition);
                    /*if (spinnerPosition != 0) {
//
                        if (getNidCaptureStatus(selected_item).equalsIgnoreCase("false")) {
                            ll_capturephoto.setVisibility(View.GONE);
                        } else {
                            ll_capturephoto.setVisibility(View.VISIBLE);
                        }
                    }*/
                    String farmerNIDPath = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO));
                    if (farmerNIDPath != null && farmerNIDPath.length() > 0) {
                        int baseWidth = 100;
                        int baseHeight = 133;


                        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

                        Bitmap bitmap = Utilities.loadSizedBitmap(farmerNIDPath, outputWidth, outputHeight);

                        nationalidphoto.setImageBitmap(bitmap);
                       /* try {
                            String encodedImage = Utilities.base64ConversionForImage(farmerNIDPath);
                            nationalids.put("photo", encodedImage);
//                   jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE, signature.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }*/
                        nationalidphoto.setTag(nationalids.getChildCount());
                    }
                    actionbtn.setTag(nationalids.getChildCount());


                    spinner_nidtype.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                                spinnerArrayAdapter = new ArrayAdapter<String>(AddFarmerActivity.this, R.layout.spinner_item, refreshitem);
                                spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                spinner_nidtype.setAdapter(spinnerArrayAdapter);
                                nidnumber.setText("");
                            }
                            return false;
                        }
                    });
                    spinner_nidtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (!spinner_nidtype.getSelectedItem().toString().equalsIgnoreCase(SELECT_NATIONALID)) {
//                    selectednationalidList.add(spinner_nidtype.getSelectedItem().toString());

                                int limit = getNidLimit(spinner_nidtype.getSelectedItem().toString());
                                InputFilter[] filters = new InputFilter[1];
                                filters[0] = new InputFilter.LengthFilter(limit); //Filter to 10 characters
                                nidnumber.setFilters(filters);
                                if (getNidCaptureStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                                    ll_capturephoto.setVisibility(View.GONE);
                                } else {
                                    ll_capturephoto.setVisibility(View.VISIBLE);
                                }
                                ValidateSpinnerTypes();
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
//                    nidlist.put(nationalids);
                    bindnationlidcapturephoto((Spinner) nationalidinclude.findViewById(R.id.idtypespinner), (ImageButton) nationalidinclude.findViewById(R.id.nidphotoActionButton));
                    bindniddelete((LinearLayout) nationalidinclude.findViewById(R.id.deleteNationalid));
                }

            } catch (Exception e) {
                FirebaseCrash.report(new Exception("NetworkLayer: Error creating farmer phone numbers object", e));
                e.printStackTrace();
            }

            ImageButton addNationId = (ImageButton) findViewById(R.id.addnid);
            addNationId.setOnClickListener(this);

        }
    }

    private boolean validateSpinnerValues(String selected_item) {
        boolean status = true;
        if(editMode && ! editnidmode) {
           return true;
        }
        if(selected_item.equalsIgnoreCase(SELECT_NATIONALID)){
            return false;
        }
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nationalidinclude = (LinearLayout) nationalids.getChildAt(j);
            Spinner spinner = nationalidinclude.findViewById(R.id.idtypespinner);
            String val = spinner.getSelectedItem().toString();
            if(val.equalsIgnoreCase(SELECT_NATIONALID)){

                return false;
            }
        }
        return status;
    }

    private void ValidateSpinnerTypes() {
        ArrayList<String> nidtype_list = new ArrayList<>();
        ArrayList<Integer> selected_types = new ArrayList<>();
        selectednationalidList = new ArrayList<String> ();
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);
            Spinner nid_type = (Spinner) nidrow.findViewById(R.id.idtypespinner);
            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
            if(! nidtype_list.contains(value)) {
                    nidtype_list.add(value);
                    selectednationalidList.add(value);
                    selected_types.add(type);
                 }
            }
        compareTypes(selected_types,getAllphotoTypes());
    }

    private void compareTypes(ArrayList<Integer> selected_types, ArrayList<Integer> allphotoTypes) {
         for (int type2 : allphotoTypes) {
            boolean found = false;
            for (int type1 : selected_types) {
                if (type2 == type1) {
                    found = true;
                }
            }
            if (!found) {
                dl.removeNIDPhoto(type2);
            }
        }
    }

    private boolean validateSpinnerSelection() {
        boolean status = true;
        ArrayList<String> values = refreshNationalidtype(setNationalidtype());
        if(values.size() == 1){
            return false;
        }
        return status;
    }

    /*private boolean validateSelectedSpinnerValue(String selectedValue) {
        boolean status = false;
        for(int i=0; i < selectednationalidList.size(); i++){
            if(selectedValue.equalsIgnoreCase(selectednationalidList.get(i))) {
                return true;
            }
        }
        return status;
    }*/


    private void bindnationlidcapturephoto(Spinner spinner, ImageButton actionbutton) {

        actionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = spinner.getSelectedItem().toString();
                if (value.equalsIgnoreCase(SELECT_NATIONALID)) {
                    Toast.makeText(AddFarmerActivity.this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_SHORT).show();
                    return;
                }

                nidvalue = getNidType(value);
//                showtoast((int) actionbutton.getTag());
//                currentcapture_position = nationalids.getChildCount();
                currentcapture_position = (int) actionbutton.getTag();
                // Request Permission only for Android Version Marshmallow and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Check Camera & Storage Permission
                    if (ContextCompat.checkSelfPermission(AddFarmerActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                            .checkSelfPermission(AddFarmerActivity.this,
                                    android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        //request permission
                        ActivityCompat.requestPermissions(AddFarmerActivity.this,
                                new String[]{android.Manifest.permission.CAMERA,
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_NATIONALID_PIC_PERMISSIONS_REQUEST);

                    } else {

                        // Permission already granted
                        bindFarmerNationalIDButton();
                    }
                } else {

                    // Call methods directly for Android versions lower than Marshmallow
                    bindFarmerNationalIDButton();
                }
            }
        });
    }

    private void bindConsentButton() {
        Button showConsent = (Button) findViewById(R.id.consentLaunchButton);
        ImageButton hideConsent = (ImageButton) findViewById(R.id.consentCloseButton);
        final LinearLayout consentHolder = (LinearLayout) findViewById(R.id.consentStatementHolder);
        final Animation mSlideBottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up);
        final Animation mSlideBottomDown = AnimationUtils.loadAnimation(context, R.anim.bottom_down);


        showConsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consentHolder.setVisibility(View.VISIBLE);
                consentHolder.startAnimation(mSlideBottomUp);
            }
        });

        hideConsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consentHolder.startAnimation(mSlideBottomDown);
            }
        });

        mSlideBottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                consentHolder.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void bindSaveButton() {
        Button saveButton = (Button) findViewById(R.id.saveFarmer);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Switch consentFarmer = (Switch) findViewById(R.id.consentFarmer);
                Switch consentOthers = (Switch) findViewById(R.id.consentOthers);

                if (! duplicateNationalidsValidation()) { //Duplicate nationalids Validation....
                    Toast.makeText(context, res.getString(R.string.duplicate_nationalids), Toast.LENGTH_LONG).show();
                    return;
                }

                if (duplicatePhoneNumberValidation()) { //Duplicate Phone Number Validation....

                    // get the entered values
                    ContentValues values = validateValues();

                    if (values.size() > 0) {
                        if (editMode) {
                            long updFarmerId = dl.updateFarmer(values, editFarmerId);
                            dl.updateFarmerPhoneNumbers(editFarmerId, storeableNumbers);
                            dl.updateFarmerNationalids(context, editFarmerId, storeableNationalids,"1","name");
                            if (updFarmerId > 0) {
                                Toast.makeText(context, res.getString(R.string.Farmer_Updated), Toast.LENGTH_LONG).show();
                                ViewFarmerActivity.farmerId = editFarmerId;
                                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(editFarmerId);
                                finish();
                                Intent intent = new Intent(context, ViewFarmerActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            // check the current location
                            if (mCurrentLocation == null || !gpsTracker.isGpsEnabled()) {
                                gpsTracker.getDeviceLocation();
                                return;
                            }

                            long newFarmerId = dl.insertFarmer(values);
                            if (newFarmerId != -1) {
                                dl.updateFarmerPhoneNumbers(newFarmerId, storeableNumbers);
                                dl.updateFarmerNationalids(context, newFarmerId, storeableNationalids, "1","name");
                                Toast.makeText(context, res.getString(R.string.Farmer_Added), Toast.LENGTH_LONG).show();

                                if (mCurrentLocation != null) {
                                    collectGPSPoint();
                                }
                                ViewFarmerActivity.farmerId = newFarmerId;
                                //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(newFarmerId);
                                finish();
                                Intent intent = new Intent(context, ViewFarmerActivity.class);
                                startActivity(intent);
                            }
                        }

                    } else {
                        if ((!consentFarmer.isChecked()) || (!consentOthers.isChecked())) {
                            //Toast.makeText(context, res.getString(R.string.Please_accept_both_consents), Toast.LENGTH_LONG).show();
                        }else {
//                                    Toast.makeText(context, res.getString(R.string.Please_fill_all_fields), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(context, res.getString(R.string.duplicate_phone_number), Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    private void collectGPSPoint() {
        ContentValues geoContentValues = new GPSUtils().getGeoContentValues(GPSConstants.MODULE_CAPTURE_ADD_FARMER, String.valueOf(mCurrentLocation.getLatitude()), String.valueOf(mCurrentLocation.getLongitude()), new Timestamp(System.currentTimeMillis()).toString());
        Long newTrackId = new GpsTrackerLogic().insertGeoDataDetails(context, geoContentValues);
//        if (newTrackId==-1){
//            try {
//                FabricLogsWriter.printGpsCrashLog(GPSConstants.MODULE_CAPTURE_ADD_FARMER ,context);
//            }catch (Exception e){}
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            gpsTracker.stopLocationUpdates();
        }catch (Exception e){}

    }

    /**
     * Generate unique ID from UUID.
     * @return long value representing UUID
     */
    private static Long generateUniqueLongNumber() {
        long val = -1;
        do {
            final UUID uid = UUID.randomUUID();
            final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
            buffer.putLong(uid.getLeastSignificantBits());
            buffer.putLong(uid.getMostSignificantBits());
            final BigInteger bi = new BigInteger(buffer.array());
            val = bi.longValue();
        } while (val < 0);
        return val;
    }

    private final DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {

            // get the date
            Calendar cal = Calendar.getInstance();
            cal.set(selectedYear, selectedMonth, selectedDay);

            SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            dateStrToSend = serverDateFormat.format(cal.getTime());

            String humanDate = DateFormat.getMediumDateFormat(context).format(cal.getTime());

            farmerDOB.setText(humanDate);
        }
    };

    private void bindDateField() {
        farmerDOB = (TextView) findViewById(R.id.farmerDOB);

        farmerDOB.setKeyListener(null);
        farmerDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, -16);
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Max Age: 110 Years
                long minYearMilliseconds = (long) (365.2425 * 24 * 60 * 60 * 1000 * 110);
                // formula used = no. of days in a year * hrs in a day * minutes * seconds * milliseconds * total years
                //Log.d("AddFarmerActivity", "minYearMilliSeconds: " + minYearMilliseconds);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, pickerListener, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - minYearMilliseconds);
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
                datePickerDialog.show();
            }
        });
    }

    private void displayBitmap(String savedImagePath, String portraitOrSignature, ImageButton target) {
        // set our desired width and height for display
        int baseWidth = 100;
        int baseHeight = 133;

        if (portraitOrSignature.equalsIgnoreCase("signature")) {
            baseWidth = 82;
            baseHeight = 28;
        }

        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);

        target.setImageBitmap(bitmap);
    }

    private void displayBitmapforNID(int nidvalue, String savedImagePath) {
        // set our desired width and height for display
        int baseWidth = 100;
        int baseHeight = 133;



        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);

//        target.setImageBitmap(bitmap);

        int nidrowscount = nationalids.getChildCount();

        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);
            Spinner nid_type = (Spinner) nidrow.findViewById(R.id.idtypespinner);
            ImageButton imageButton = (ImageButton) nidrow.findViewById(R.id.nationalidphoto);
            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
           /* if(imageButton.getTag() == captureSignature){
                imageButton.setImageBitmap(bitmap);
            }*/
            if(type == nidvalue) {
                imageButton.setImageBitmap(bitmap);
            }
        }


    }

    private void setFullImageFromFilePath(String imagePath, ImageButton target, String portraitOrSignature) {
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

    private void setNIDImageFromFilePath(String imagePath, String portraitOrSignature) {
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, portraitOrSignature);
        if (savedImagePath != null) {
            Uri uri = Uri.parse(savedImagePath);
            setCapturedImageURI(uri);
//            setCurrentPhotoPath(savedImagePath);
            int baseWidth = 100;
            int baseHeight = 133;
            Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
            Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
            Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

           dl.updateNIDPhoto(nidvalue, savedImagePath);

//            showNIDImage.setImageBitmap(bitmap);

            displayBitmapforNID(nidvalue,savedImagePath);

        }else{
            Toast.makeText(this,"Image not saved ! Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile(String portraitOrSignature) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName;
        if(! portraitOrSignature.contains("nationalidimage")) {
            imageFileName = (portraitOrSignature.equalsIgnoreCase("portrait")) ? "ofis_farmer_portrait_" + timestamp + "_" : "ofis_farmer_signature_" + timestamp + "_";
        }else{
            imageFileName = portraitOrSignature + "_" + timestamp + "_";
        }
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        if (portraitOrSignature.equalsIgnoreCase("portrait")) {
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        } else {
            mCurrentSignaturePhotoPath = "file:" + image.getAbsolutePath();
        }
        return image;
    }



    private void bindFarmerNationalIDButton() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        File photoFile = null;

        try {
            photoFile = createImageFile("nationalidimage_" +currentcapture_position );
        } catch (IOException e) {
            FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating nationalid image file", e));
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
            removehmURIvalue(hm_uri,currentcapture_position);
            removehmvalue(hm_uripath,currentcapture_position);
            hm_uri.put(currentcapture_position,fileUri);
            hm_uripath.put(currentcapture_position,fileUri.getPath());
            setCapturedNIDImageURI(hm_uri);
            setCurrentNIDPhotoPath(hm_uripath);
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, hm_uri.get(currentcapture_position));
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE_NATIONALID);
        }
    }



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
                                Toast.makeText(AddFarmerActivity.this,"Gallery is not available in Mobile",Toast.LENGTH_LONG).show();
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
                            Toast.makeText(AddFarmerActivity.this,"Gallery is not available in Mobile",Toast.LENGTH_LONG).show();
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

    private ArrayList<NationalIdTypeDTO> getModules(){
//        nidtype = new ArrayList<NationalIdTypeDTO>();
        nidtypelist = new ArrayList<NationalIdTypeDTO>();

        Cursor cursor = dl.getnationalids();
        NationalIdTypeDTO typeDTO;
        if (cursor.moveToFirst()) {
            do {
                typeDTO = new NationalIdTypeDTO();
                typeDTO.setId(cursor.getInt(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_ID)));
                typeDTO.setType(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE)));
                typeDTO.setNationalIdLimit(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_LIMIT)));
                typeDTO.setPhotoRequired(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_PHOTO_REQUIRED)));
                typeDTO.setActive(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_ACTIVE)));
                typeDTO.setTypeFr(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR)));
                typeDTO.setTypeEs(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES)));
                typeDTO.setTypePt(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT)));
                typeDTO.setTypeId(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID)));
                typeDTO.setTypeTr(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR)));
                typeDTO.setTypeLo(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO)));
                typeDTO.setTypeVi(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI)));
                typeDTO.setTypeTh(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TH)));
                nidtypelist.add(typeDTO);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return nidtypelist;
    }

    private ArrayList<String> setNationalidtype(){
        nidtypelist = getModules();
        nidtype = new ArrayList<String>();
        nidtype.add(SELECT_NATIONALID);
        if (nidtypelist.size() > 0) {
            for (NationalIdTypeDTO moduleDTO : nidtypelist) {
                nidtype.add(moduleDTO.getType());
            }
        }

        return nidtype;
    }


    private ArrayList<String> refreshNationalidtype(ArrayList<String> value){
        for(int i=0; i < selectednationalidList.size(); i++){
            value.remove(selectednationalidList.get(i));
        }

        return value;
    }
    // getting nationalids type id values
    public int getNidType(String type) {
        int id=0;
        Cursor  nid=dl.getNIDByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            id= nid.getInt(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_ID));
        }
        return  id;
    }

    // getting nationalids type id values
    public String getNidTypebyid(int id) {
        String type=null;
        Cursor  nid=dl.getNIDById(id);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE));
        }
        return  type;
    }

    // getting nationalids type id values
    public String getPhotobytype(int type) {
        String photo=null;
        Cursor  nid=dl.getPhotoByType(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            photo= nid.getString(nid.getColumnIndex(OFISDBContract.LocalNationalIDImages.COLUMN_NID_PHOTO));
        }
        return photo;
    }

    // getting nationalids type id values
    public int getNidLimit(String type) {
        int id=0;
        Cursor  nid=dl.getNIDByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            id= nid.getInt(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_LIMIT));
        }
        return  id;
    }

    // getting nationalids type id values
    public String getNidCaptureStatus(String type) {
        String photostatus="true";
        Cursor  nid=dl.getNIDPhotostatusByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            photostatus= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_PHOTO_REQUIRED));
        }
        Boolean status = (photostatus == "true")? true : false;
        return  photostatus;
    }

    // getting nationalids type id values
    public boolean getPhotostatusByType(String type) {
        boolean photostatus= false;
        Cursor  nid=dl.getPhotostatusByType(type);
        if (nid.getCount() > 0) {
            photostatus= true;
        }
        return  photostatus;
    }

    public boolean getNIDPhotostatusByType(String type, long editFarmerId) {
        boolean photostatus= false;
        Cursor  nid=dl.getNIDPhotostatusByType(type,editFarmerId);
        if (nid.getCount() > 0) {
            photostatus= true;
        }
        return  photostatus;
    }

    public ArrayList<Integer> getAllphotoTypes() {
        ArrayList<Integer> types = new ArrayList<>();
        Cursor  nid=dl.getAllPhotos();
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            int type= nid.getInt(nid.getColumnIndex(OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE));
            types.add(type);
        }
        return types;
    }

    public String getNIDPhotoByType(String type, long farmerid) {
        String photo=null;
        Cursor  nid=dl.getNIDPhotostatusByType(type, farmerid);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            photo= nid.getString(nid.getColumnIndex(OFISDBContract.LocalNationalIDImages.COLUMN_NID_PHOTO));
        }
        return photo;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == Activity.RESULT_CANCELED) return;
        ImageButton farmerPhoto = (ImageButton) findViewById(R.id.farmerPhoto);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == -1) {
                    gpsTracker.getDeviceLocation();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    setFullImageFromFilePath(getCurrentPhotoPath(), farmerPhoto, "portrait");
                } else {
                    farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.empty_farmer_photo));
                    setCurrentPhotoPath(null);
                }
                break;
            case REQUEST_IMAGE_GALLERY:
                if (resultCode == RESULT_OK) {
                    setCapturedImageURI(imageReturnedIntent.getData());
                    setFullImageFromFilePath(getFilePathUri(context, getCapturedImageURI()), farmerPhoto, "portrait");
                } else {
                    farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.empty_farmer_photo));
                    setCurrentPhotoPath(null);
                }
                break;

            case REQUEST_IMAGE_CAPTURE_NATIONALID:
                if (resultCode == RESULT_OK) {
                    setNIDImageFromFilePath(hm_uripath.get(currentcapture_position), "signature");
                }
                break;
            default:
        }
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
                        Toast.makeText(AddFarmerActivity.this, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
                    }
                });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PROFILE_PIC_PERMISSIONS_REQUEST: {
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
                break;
            }

            case MY_NATIONALID_PIC_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    bindFarmerNationalIDButton();

                } else if (Build.VERSION.SDK_INT >= 23
                        && (!shouldShowRequestPermissionRationale(permissions[0])
                        || !shouldShowRequestPermissionRationale(permissions[1]))) {
                    showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));

                } else {
                    Toast.makeText(this, R.string.camera_and_gallery_permission_denial_msg, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    boolean isSigned = false;

    public void showSignatureDialog(View v) {

        isSigned = false;

        final Dialog signatureDialog = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
        signatureDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        signatureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        signatureDialog.setContentView(R.layout.dialog_signature);
        SignaturePad mSignaturePad = signatureDialog.findViewById(R.id.signature_pad);



        signatureDialog.findViewById(R.id.imgv_clear_signature).setOnClickListener(new View.OnClickListener() {
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
                //Event triggered when the pad is signed
                isSigned = true;
                edit_signature = true;
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
                isSigned = false;
            }
        });

        signatureDialog.findViewById(R.id.btn_cancel_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signatureDialog.dismiss();


            }
        });

        signatureDialog.findViewById(R.id.btn_add_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    if (isSigned) {


                        farmerSignatureButton.setImageBitmap(mSignaturePad.getTransparentSignatureBitmap());

                        File photoFile = null;

                        try {
                            photoFile = createImageFile("signature");
                        } catch (IOException e) {
                            FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating signature image file", e));
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
                            setCapturedSignatureImageURI(fileUri);
                            setCurrentSignaturePhotoPath(fileUri.getPath());

                            try (FileOutputStream out = new FileOutputStream(photoFile)) {
                                mSignaturePad.getTransparentSignatureBitmap().compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                // PNG is a lossless format, the compression factor (100) is ignored
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        signatureDialog.dismiss();
                    } else {

                        Toast.makeText(AddFarmerActivity.this, "No signature available...", Toast.LENGTH_SHORT).show();
                    }


                } catch (Exception e) {

                }


            }
        });

        signatureDialog.show();
    }

    @Override
    protected void onResume() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        super.onPause();
    }

    /*public void showtoast(int val){
        Toast.makeText(this,""+val,Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.addnid) {
            if (!ValidateNIDValues()) return;
            try {
                if (! validateSpinnerValues(spinner_nidtype.getSelectedItem().toString())) {
                    Toast.makeText(AddFarmerActivity.this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_LONG).show();
                    return;
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
//            if (validateSpinnerValues(spinner_nidtype.getSelectedItem().toString())) {
                if (validateSpinnerSelection()) {
                    ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                    if (refreshitem.size() != 0) {
                        editnidmode = true;
                        LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
                        nationalids.addView(nationalidinclude);
                        LinearLayout ll_capturephoto = (LinearLayout) nationalidinclude.findViewById(R.id.ll_capturephoto);
                        Spinner spinner_nidtype = (Spinner) nationalidinclude.findViewById(R.id.idtypespinner);

                        ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                        ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
                        actionbtn.setTag(nationalids.getChildCount());
                        nationalidphoto.setTag(nationalids.getChildCount());
                        EditText nidnumber = (EditText) nationalidinclude.findViewById(R.id.nidnumber);
                        spinnerArrayAdapter = new ArrayAdapter<String>(AddFarmerActivity.this, R.layout.spinner_item, refreshitem);
                        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spinner_nidtype.setAdapter(spinnerArrayAdapter);
                        spinner_nidtype.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                    ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                                    spinnerArrayAdapter = new ArrayAdapter<String>(AddFarmerActivity.this, R.layout.spinner_item, refreshitem);
                                    spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                    spinner_nidtype.setAdapter(spinnerArrayAdapter);
                                    nidnumber.setText("");
                                }
                                return false;
                            }
                        });
                        spinner_nidtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (!spinner_nidtype.getSelectedItem().toString().equalsIgnoreCase(SELECT_NATIONALID)) {

//                                    selectednationalidList.add(spinner_nidtype.getSelectedItem().toString());

                                    int limit = getNidLimit(spinner_nidtype.getSelectedItem().toString());
                                    InputFilter[] filters = new InputFilter[1];
                                    filters[0] = new InputFilter.LengthFilter(limit); //Filter to 10 characters
                                    nidnumber.setFilters(filters);
                                    if (getNidCaptureStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                                        ll_capturephoto.setVisibility(View.GONE);
                                    } else {
                                        ll_capturephoto.setVisibility(View.VISIBLE);
                                    }
                                    ValidateSpinnerTypes();
                                }
                           /* if(selectednationalidList.contains(spinner_nidtype.getSelectedItem().toString())){
                                Toast.makeText(AddFarmerActivity.this,"Already selected",Toast.LENGTH_LONG).show();
                                int spinnerPosition = spinnerArrayAdapter.getPosition(selected_item);
                                spinner_nidtype.setSelection(spinnerPosition);
                            }*/
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        bindniddelete((LinearLayout) nationalidinclude.findViewById(R.id.deleteNationalid));
//                        String value = spinner_nidtype.getSelectedItem().toString();
//                        if (!value.equalsIgnoreCase(SELECT_NATIONALID)) {
//                            nidvalue = getNidType(value);
                            bindnationlidcapturephoto((Spinner) nationalidinclude.findViewById(R.id.idtypespinner), (ImageButton) nationalidinclude.findViewById(R.id.nidphotoActionButton));
//                        }else{
//                            Toast.makeText(AddFarmerActivity.this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_LONG).show();
//                        }

                    }
                }
//            }

            /*else{
                Toast.makeText(AddFarmerActivity.this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_LONG).show();

            }*/
        }



    }

    private String getnationalidSubmitStatus() {
        String typeadded = "false";
        int nidrowscount = nationalids.getChildCount();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < nidrowscount; i++) {
            LinearLayout view = (LinearLayout) nationalids.getChildAt(i);

            Spinner idtypespinner = view.findViewById(R.id.idtypespinner);
            EditText nidnumber = view.findViewById(R.id.nidnumber);
            String value = idtypespinner.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            int limit = getNidLimit(idtypespinner.getSelectedItem().toString());
            if(limit < nid.length()){
                typeadded = LIMIT_XED;
            }
            if(nidrowscount == 1 && i==0){
                if(type == 0){
                    typeadded = ENTER_NID;
                }
            }
            names.add(String.valueOf(type));
        }
        Set<String> store = new HashSet<>();
        for (String name : names) {
            if (store.add(name) == false) {
                typeadded = DUPLICATE;
            }
        }

        return typeadded;
    }

    private boolean ValidateNIDValues() {
        boolean status = true;
        ArrayList<String> nidtype_list = new ArrayList<>();
        selectednationalidList = new ArrayList<String> ();;
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);
            EditText nidnumber = (EditText) nidrow.findViewById(R.id.nidnumber);
            Spinner nid_type = (Spinner) nidrow.findViewById(R.id.idtypespinner);
            LinearLayout ll_nidview = (LinearLayout) nidrow.findViewById(R.id.ll_nidview);
            LinearLayout deleteNationalid = (LinearLayout) nidrow.findViewById(R.id.deleteNationalid);
            ImageButton nidphotoActionButton = (ImageButton) nidrow.findViewById(R.id.nidphotoActionButton);
            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            /*if(j == nidrowscount-1){
                nid_type.setEnabled(true);
            }else {
                nid_type.setEnabled(false);
            }*/
            if(nid.trim() == "" || nid.trim().equalsIgnoreCase("")){
//                nidnumber.setEnabled(false);
                Toast.makeText(this,getResources().getString(R.string.nid_value_enter),Toast.LENGTH_SHORT).show();
                return false;
            }
            if(value.equalsIgnoreCase(SELECT_NATIONALID)){
                Toast.makeText(this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_SHORT).show();
                return false;
            }
            if(nidtype_list.size() != 0 && nidtype_list.contains(value)) {
                nid_type.setEnabled(true);
                Toast.makeText(this,getResources().getString(R.string.nationalidtype_alreadyselected),Toast.LENGTH_SHORT).show();
                return false;
            }else {
                nidtype_list.add(value);
                selectednationalidList.add(value);
            }
            if (getNidCaptureStatus(value).equalsIgnoreCase("true")) {
//                if(! hm_uripath.containsKey(j+1)){
                if(! editMode) {
                    if (!getPhotostatusByType(String.valueOf(type))) {
                        nidphotoActionButton.setClickable(true);
                        Toast.makeText(this, getResources().getString(R.string.nid_photo_select), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                /*else {
                    if(! hm_uripath.containsKey(j+1)){
                        nidphotoActionButton.setClickable(true);
                        Toast.makeText(this, getResources().getString(R.string.nid_photo_select), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }*/

            }

            if(status){
                NationalIdDTO idDTO = new NationalIdDTO();
                idDTO.setNationalidNumber(nid);
                idDTO.setNationalidtype(String.valueOf(type));
                if (getNidCaptureStatus(value).equalsIgnoreCase("true")) {
                    String photopath = null;
                    if(! editMode) {
                         photopath = getPhotobytype(type);
                    }else{
                        if(getNIDPhotostatusByType(String.valueOf(type),editFarmerId)) {
                            photopath = getNIDPhotoByType(String.valueOf(type), editFarmerId);
                        }else {
                            photopath = getPhotobytype(type);
                        }
                    }
                    idDTO.setPhoto(photopath);
                }else{
                    idDTO.setPhoto("");
                }

                dl.addnewLocalFarmerNationalids(idDTO);
                nid_type.setEnabled(false);
               /* nid_type.setEnabled(false);
                nidnumber.setEnabled(false);
                nidphotoActionButton.setClickable(false);
//                deleteNationalid.setClickable(false);
                ll_nidview.setBackgroundColor(getResources().getColor(R.color.cardview_light_background));*/
            }
        }
        return status;
    }

   /* private void removelastrow() {
        try {
            int nidrowscount = nationalids.getChildCount();
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(nidrowscount - 1);
            Spinner nid_type = (Spinner) nidrow.findViewById(R.id.idtypespinner);
            String value = nid_type.getSelectedItem().toString();
            if (value.equalsIgnoreCase(SELECT_NATIONALID)) {
                nationalids.removeViewAt(nidrowscount - 1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/


}
