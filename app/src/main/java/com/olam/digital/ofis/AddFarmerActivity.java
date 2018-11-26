package com.olam.digital.ofis;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.recordpolygon.SharedPreferenceUtils;
import com.olam.digital.ofis.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.UUID;

// update
public class AddFarmerActivity extends BaseActivity {

    private Context context;
    private Resources res;
    private long editFarmerId;
    private boolean editMode;
    private int selectedCountry = 0, selectedRegion = 0, selectedDistrict = 0, selectedPlace = 0, selectedFarmerGroup = 0, selectedSection = 0;

    private Spinner genderspinner, countrySpinner, regionSpinner, districtSpinner, placeSpinner, farmerGroupSpinner, sectionSpinner;

    ImageButton farmerPhotoActionButton;
    Button captureSignature;
    EditText newPlaceName;

    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;
    private final static int REQUEST_IMAGE_CAPTURE_SIGNATURE = 3;
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

    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null, mCurrentSignaturePhotoPath = null;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null;

    private String dateStrToSend;
//    private EditText farmerDOB;
    private TextView farmerDOB;
    private EditText farmerIdPrefix;
    private LinearLayout phonenumbers;
    private LayoutInflater inflater;
    private final JSONArray storeableNumbers = new JSONArray();

    private DataLayer dl;

    private final Hashtable<String, String> genders = new Hashtable<>();

    // Runtime Permission Request Code
    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
    private final static int MY_SIGNATURE_PIC_PERMISSIONS_REQUEST = 9999;

    ArrayList<String> phoneNumberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farmer);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

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
        bindphonenumbers();

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
        dl.createGeographyHierarchySpinners(this, context, farmerIdPrefix, selectedCountry, selectedRegion, selectedDistrict, selectedPlace, phonenumbers);

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

        captureSignature = (Button) findViewById(R.id.captureSignature);
        captureSignature.setOnClickListener(new View.OnClickListener() {
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
                                MY_SIGNATURE_PIC_PERMISSIONS_REQUEST);

                    } else {

                        // Permission already granted
                        bindFarmerSignatureButton();
                    }
                } else {

                    // Call methods directly for Android versions lower than Marshmallow
                    bindFarmerSignatureButton();
                }
            }
        });


        bindDateField();
        bindSaveButton();
        bindConsentButton();

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
        dl.createGeographyHierarchySpinners(this, context, farmerIdPrefix, selectedCountry, selectedRegion, selectedDistrict, selectedPlace, phonenumbers);

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

    private void populateExistingValues(final Cursor existingItem) {
        EditText farmerName = (EditText) findViewById(R.id.farmerName);
        farmerName.setText(existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));

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

        String farmerSignaturePhotoPath = existingItem.getString(existingItem.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE));
        if (farmerSignaturePhotoPath != null) {
            ImageButton farmerSignature = (ImageButton) findViewById(R.id.farmerSignature);

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

                Bitmap existingSignatureBitmap = Utilities.loadSizedBitmap(getCurrentSignaturePhotoPath(), outputWidth, outputHeight);

                farmerSignature.setImageBitmap(existingSignatureBitmap);
            }
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

    private boolean duplicatePhoneNumberValidation() {
        boolean duplicatePhoneNumberValidationFlag = false;

        getAllPhoneNumbers();

        int phonerowscount = phonenumbers.getChildCount();

        for (int j = 0; j < phonerowscount; j++) {
            LinearLayout phonenumberrow = (LinearLayout) phonenumbers.getChildAt(j);

            EditText phonenumber = (EditText) phonenumberrow.findViewById(R.id.phonenumber);
            String phonenumberValue = String.valueOf(phonenumber.getText()).replace(" ", "");


            if(phonenumberValue.length()==0){

                duplicatePhoneNumberValidationFlag = true;
            }



           else if (Collections.frequency(phoneNumberList,phonenumberValue.trim()) == 1) {
                duplicatePhoneNumberValidationFlag = true;
            } else {
                duplicatePhoneNumberValidationFlag = false;
                break;
            }
        }

        return duplicatePhoneNumberValidationFlag;
    }

    private ContentValues validateValues() {

        ContentValues values = new ContentValues();


//        EditText farmerIdSuffix = (EditText) findViewById(R.id.olamFarmerIdEnd);
//        String farmerIdSuffixValue = String.valueOf(farmerIdSuffix.getText());
//        if (farmerIdSuffixValue.equals("") || farmerIdSuffix.length() < 4) {
//            Toast.makeText(context, "Please fill the Farmer id 4 digit", Toast.LENGTH_LONG).show();
//            return values;
//        }
        Long username = Long.parseLong(new AuthenticationHelper(AddFarmerActivity.this).getuserName());
        Long time = System.currentTimeMillis();
        String  olamFarmerId = String.valueOf(username+time);

//        String olamFarmerId = farmerIdPrefix.getText().toString() + farmerIdSuffixValue;

        EditText farmerName = (EditText) findViewById(R.id.farmerName);
        String farmerNameValue = String.valueOf(farmerName.getText());
        if (farmerNameValue.equals("")) {

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
            }

            else if (selectedRegion == 0) {

                message = "Region";

            }
            else if (selectedDistrict == 0) {
                message = "District";
            }

            else if (selectedPlace == 0) {

                message = "Place";

            }
            else if (selectedFarmerGroup == 0) {

                message = "farmer group";

            }

            else if (selectedSection == 0) {
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

        int phonerowscount = phonenumbers.getChildCount();
        /*boolean hasNumber = false;
        boolean hasMainNumber = false;*/
        long countryId = countrySpinner.getSelectedItemId();
        long regionId = regionSpinner.getSelectedItemId();
        long districtId=districtSpinner.getSelectedItemId();
        long farmerGroupId = farmerGroupSpinner.getSelectedItemId();
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

        SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(olamFarmerId, generateUniqueLongNumber());

        try {
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME, farmerNameValue);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID,olamFarmerId);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY, 1);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB, dateStrToSend);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER, genderValue);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO, getCurrentPhotoPath());
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE, getCurrentSignaturePhotoPath());
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID, place_id);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID, section_id);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED, 0);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES, "{}");
            values.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, district_id);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_FARMER, 1);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_OTHERS, 1);
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRYID,String.valueOf(countryId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID,String.valueOf(regionId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID,String.valueOf(districtId));
            values.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID,String.valueOf(farmerGroupId));
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating farmer content values", e));
            values.clear();
        }

        return values;
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

            if(existingNumbers.getCount()==0){

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

        addNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout phonenumberinclude = (LinearLayout) inflater.inflate(R.layout.phonenumber_entry, phonenumbers, false);
                EditText diallingCode=null;
                try {
                     diallingCode = (EditText) phonenumbers.getChildAt(0).findViewById(R.id.dialling_code);
                }catch (Exception e){
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

                if (duplicatePhoneNumberValidation()) { //Duplicate Phone Number Validation....

                            // get the entered values
                            ContentValues values = validateValues();

                            if (values.size() > 0) {
                                if (editMode) {
                                    long updFarmerId = dl.updateFarmer(values, editFarmerId);
                                    dl.updateFarmerPhoneNumbers(editFarmerId, storeableNumbers);
                                    if (updFarmerId > 0) {
                                        Toast.makeText(context, res.getString(R.string.Farmer_Updated), Toast.LENGTH_LONG).show();
                                        ViewFarmerActivity.farmerId = editFarmerId;
                                        //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(editFarmerId);
                                        finish();
                                        Intent intent = new Intent(context, ViewFarmerActivity.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    long newFarmerId = dl.insertFarmer(values);
                                    if (newFarmerId != -1) {
                                        dl.addFarmerPhoneNumbers(newFarmerId, storeableNumbers);
                                        Toast.makeText(context, res.getString(R.string.Farmer_Added), Toast.LENGTH_LONG).show();

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

    private void setFullImageFromFilePath(String imagePath, ImageButton target, String portraitOrSignature) {
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, portraitOrSignature);
        if (savedImagePath != null) {
            if (portraitOrSignature.equalsIgnoreCase("portrait")) {
                Uri uri = Uri.parse(savedImagePath);
                setCapturedImageURI(uri);
                setCurrentPhotoPath(savedImagePath);
            } else {
                Uri uri = Uri.parse(savedImagePath);
                setCapturedSignatureImageURI(uri);
                setCurrentSignaturePhotoPath(savedImagePath);
            }

            displayBitmap(savedImagePath, portraitOrSignature, target);
        }
    }

    private File createImageFile(String portraitOrSignature) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = (portraitOrSignature.equalsIgnoreCase("portrait")) ? "ofis_farmer_portrait_" + timestamp + "_" : "ofis_farmer_signature_" + timestamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        if (portraitOrSignature.equalsIgnoreCase("portrait")) {
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        } else {
            mCurrentSignaturePhotoPath = "file:" + image.getAbsolutePath();
        }
        return image;
    }

    private void bindFarmerSignatureButton() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, getCapturedSignatureImageURI());
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE_SIGNATURE);
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
                        startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                    }

                } else if (items[item].equals(res.getString(R.string.Gallery))) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        ImageButton farmerPhoto = (ImageButton) findViewById(R.id.farmerPhoto);
        ImageButton farmerSignature = (ImageButton) findViewById(R.id.farmerSignature);

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    setFullImageFromFilePath(getCurrentPhotoPath(), farmerPhoto, "portrait");
                }else{
                    farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.empty_farmer_photo));
                    setCurrentPhotoPath(null);
                }
                break;
            case REQUEST_IMAGE_GALLERY:
                if (resultCode == RESULT_OK) {
                    setCapturedImageURI(imageReturnedIntent.getData());
                    setFullImageFromFilePath(getFilePathUri(context, getCapturedImageURI()), farmerPhoto, "portrait");
                }else{
                    farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.empty_farmer_photo));
                    setCurrentPhotoPath(null);
                }
                break;
            case REQUEST_IMAGE_CAPTURE_SIGNATURE:
                if (resultCode == RESULT_OK) {
                    setFullImageFromFilePath(getCurrentSignaturePhotoPath(), farmerSignature, "signature");
                }else{
                    farmerSignature.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.empty_signature));
                    setCurrentSignaturePhotoPath(null);
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
            case MY_SIGNATURE_PIC_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    bindFarmerSignatureButton();

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
}
