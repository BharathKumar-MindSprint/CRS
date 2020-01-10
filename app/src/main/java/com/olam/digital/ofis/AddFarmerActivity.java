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
import android.media.Image;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.logging.FabricLogsWriter;
import com.olam.digital.ofis.model.NationalIdDTO;
import com.olam.digital.ofis.model.NationalIdTypeDTO;
import com.olam.digital.ofis.recordpolygon.SharedPreferenceUtils;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.tasks.NonScrollListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// update
public class AddFarmerActivity extends BaseActivity implements View.OnClickListener{

    private Context context;
    private Resources res;
    private long editFarmerId;
    private boolean editMode;
    private int selectedCountry = 0, selectedRegion = 0, selectedDistrict = 0, selectedPlace = 0, selectedFarmerGroup = 0, selectedSection = 0;

    private Spinner genderspinner, countrySpinner, regionSpinner, districtSpinner, placeSpinner, farmerGroupSpinner, sectionSpinner;
    ImageButton farmerPhotoActionButton;
    EditText newPlaceName;

    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";
    private final static String CAPTURED_SIGNATURE_PHOTO_PATH_KEY = "mCurrentSignaturePhotoPath";
    private final static String CAPTURED_SIGNATURE_PHOTO_URI_KEY = "mCapturedSignatureImageURI";
    private final static String CAPTURED_NATIONALID_PHOTO_URI_KEY = "mCapturedNationalIdImageURI";
    private final static String CAPTURED_NATIONALID_PHOTO_PATH_KEY = "mCapturedNationalIdImageURI";
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
    private String mCurrentPhotoPath = null, mCurrentSignaturePhotoPath = null, mCurrentNationalIdPhotoPath = null;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null, mCapturedNationalIdImageURI = null;

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
    private final static int MY_NATIONALID_PIC_PERMISSIONS_REQUEST = 9999;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    ArrayList<String> phoneNumberList;
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

// National ID releated variable
    NonScrollListView nationalidslist;
    ArrayList<String> selectednationalidList = new ArrayList<String> ();
    ArrayList<String> nidtype;
    ArrayList<NationalIdTypeDTO> nidtypelist;
    ImageButton farmerSignatureButton;
    ImageButton addNationId;
    Map<Integer, Integer> typeMap = new HashMap<Integer, Integer>();
    Map<Integer, String> nidvalueMap = new HashMap<Integer, String>();
    private final static int REQUEST_IMAGE_CAPTURE_NATIONALID = 4;
    ArrayList<NationalIdDTO> nidDtoList = new ArrayList<NationalIdDTO> ();;
    ImageButton showNIDImage;
    int currentcapture_position = 0;
    CustomListAdapter_NationalId adapterNationalId;
    int capture__position;
    NationalIdDTO nidDTO;
    HashMap<Integer, String> hm_path = new HashMap<>();
    private final static int REQUEST_NEW_ITEM = 1;
    private final static int REQUEST_ADDING_ITEM = 2;
    boolean edit_signature = false;
    private final static String LIMIT_XED = "limit";
    private final static String DUPLICATE = "duplicate";
    private final static String ENTER_NID = "novalues";
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
        nationalidslist = (NonScrollListView) findViewById(R.id.list_nationalids);
        farmerSignatureButton = (ImageButton) findViewById(R.id.farmerSignature);
        refreshNationalidtype(setNationalidtype());
        bindphonenumbers();
        bindnationaiddetails();

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
        addNationId = (ImageButton) findViewById(R.id.addnid);
        addNationId.setOnClickListener(this);
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

    private void bindnationaiddetails() {
        if(editMode){
            bindnationaidvaluesfromdb();
        }
        if(nidDtoList.size() == 0) {
            addingEmptyValue(REQUEST_NEW_ITEM);
        }

        adapterNationalId = new CustomListAdapter_NationalId(this, res, nidtypelist, nidDtoList, refreshNationalidtype(setNationalidtype()),typeMap, nidvalueMap , 1);
        nationalidslist.setAdapter(adapterNationalId);
        Utilities.setListViewHeightBasedOnChildren(nationalidslist);

//        adapterNationalId.getValues();
       /* nationalidslist.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long viewId = view.getId();

                if(viewId == R.id.deleteNationalid){
                    nidDtoList.remove(position);
                    adapterNationalId.notifyDataSetChanged();
                }

            }
        });*/
    }

    private void bindnationaidvaluesfromdb() {
        Cursor nids = dl.getnationalidsbyfarmerid(editFarmerId);

        nidDtoList = new ArrayList<>();
        int i=0;
        while (nids.moveToNext()) {
            NationalIdDTO nationalIdDTO = new NationalIdDTO();
            nationalIdDTO.setNationalidtype(String.valueOf(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE)));
            nationalIdDTO.setNationalidNumber(nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER)));
            nidvalueMap.put(i,nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER)));
            typeMap.put(i,nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE)));

            String farmerNIDPath = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO));
            if (farmerNIDPath != null && farmerNIDPath.length() > 0) {
                nationalIdDTO.setPhoto(farmerNIDPath);
            }
            nidDtoList.add(nationalIdDTO);
            i++;
        }

    }

    private void addingEmptyValue(int status) {
        NationalIdDTO idDTO = new NationalIdDTO();
        idDTO.setNationalidNumber("");
        idDTO.setNationalidtype(String.valueOf(0));
        idDTO.setPhoto("");
        nidDtoList.add(idDTO);
        if(status == REQUEST_ADDING_ITEM) {
            adapterNationalId.notifyDataSetChanged();
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
        if (mCurrentNationalIdPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_NATIONALID_PHOTO_PATH_KEY, mCurrentNationalIdPhotoPath);
        }
        if (mCapturedNationalIdImageURI != null) {
            savedInstanceState.putString(CAPTURED_NATIONALID_PHOTO_URI_KEY, mCapturedNationalIdImageURI.toString());
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
        if (savedInstanceState.containsKey(CAPTURED_NATIONALID_PHOTO_PATH_KEY)) {
            mCurrentNationalIdPhotoPath = savedInstanceState.getString(CAPTURED_NATIONALID_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_NATIONALID_PHOTO_URI_KEY)) {
            mCapturedNationalIdImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_NATIONALID_PHOTO_URI_KEY));
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
        if (mCurrentNationalIdPhotoPath != null) {
            displayBitmap(mCurrentNationalIdPhotoPath, "NationalId", showNIDImage);
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

    private String getCurrentNationalIdPhotoPath() {
        return mCurrentNationalIdPhotoPath;
    }

    private void setCurrentNationalIdPhotoPath(String mCurrentNationalIdPhotoPath) {
        this.mCurrentNationalIdPhotoPath = mCurrentNationalIdPhotoPath;
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

    private Uri getCapturedNationalIdImageURI() {
        return mCapturedNationalIdImageURI;
    }

    private void setCapturedNationalIdImageURI(Uri mCapturedNationalIdImageURI) {
        this.mCapturedNationalIdImageURI = mCapturedNationalIdImageURI;
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
//        fetchListValues();
        int phonerowscount = phonenumbers.getChildCount();
        /*boolean hasNumber = false;
        boolean hasMainNumber = false;*/
        long countryId = countrySpinner.getSelectedItemId();
        long regionId = regionSpinner.getSelectedItemId();
        long districtId = districtSpinner.getSelectedItemId();
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

                                    dl.updateFarmerNationalids(context, editFarmerId, fetchListValues(),"1","name");
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
                                        dl.addFarmerPhoneNumbers(newFarmerId, storeableNumbers);
                                        dl.addFarmerNationalids(context, newFarmerId, fetchListValues(), "1","name");
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
        try {
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
            if(nidDTO != null) {
                nidDTO.setPhoto(getCurrentNationalIdPhotoPath());
            }
            target.setImageBitmap(bitmap);
        }catch (Exception e){
        }
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

    public void addTypeintoSelectionList(String value){
        selectednationalidList.add(value);

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
        return  photostatus;
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

//                        setFullImageFromFilePath(getCurrentNationalIdPhotoPath(), showNIDImage, "portrait");
                        setNIDImageFromFilePath(getCurrentNationalIdPhotoPath(), "NationalId");
                    } else {
                        showNIDImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_capture_id_default));
                        setCurrentNationalIdPhotoPath(null);
                    }
                    break;


                default:
            }
    }

    private void setNIDImageFromFilePath(String imagePath, String nid) {
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, nid);
        if (savedImagePath != null) {
            Uri uri = Uri.parse(savedImagePath);
            setCapturedNationalIdImageURI(uri);
            setCurrentNationalIdPhotoPath(savedImagePath);
            int baseWidth = 100;
            int baseHeight = 133;

            Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
            Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
            Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

            Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);
            nidDTO.setPhoto(getCurrentNationalIdPhotoPath());
            hm_path.put(capture__position, getCurrentNationalIdPhotoPath());
            nidDtoList.set(capture__position, nidDTO);
            adapterNationalId.notifyDataSetChanged();
//            showNIDImage.setImageBitmap(bitmap);

        }else{
            Toast.makeText(this,"not saving", Toast.LENGTH_SHORT).show();
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
                isSigned = false;
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


    /*public void showNationalIDDialog(View v){
        final Dialog nidDialog = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
        nidDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        nidDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nidDialog.setContentView(R.layout.dialog_nationalid);
        ImageView cancel = nidDialog.findViewById(R.id.btn_cancel_nid);
        Button save = nidDialog.findViewById(R.id.addnid);
        showNIDImage = (ImageButton) nidDialog.findViewById(R.id.nationalidphoto);
        ImageButton captureimg = nidDialog.findViewById(R.id.nidphotoActionButton);
        Spinner idtypespinner = nidDialog.findViewById(R.id.idtypespinner);
        EditText nidnumber = nidDialog.findViewById(R.id.nidnumber);

        ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(AddFarmerActivity.this, R.layout.spinner_item, refreshitem);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        idtypespinner.setAdapter(spinnerArrayAdapter);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nidDialog.dismiss();
            }
        });

        captureimg.setOnClickListener(new View.OnClickListener() {
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
                                MY_NATIONALID_PIC_PERMISSIONS_REQUEST);

                    } else {
                        currentcapture_position ++;
                        // Permission already granted
                        bindFarmerNationalIDButton();
                    }
                } else {
                    currentcapture_position ++;
                    // Call methods directly for Android versions lower than Marshmallow
                    bindFarmerNationalIDButton();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nid = nidnumber.getText().toString();
                if(nid.trim() == "" || nid.trim().equalsIgnoreCase("")){
                    nidnumber.setSelected(true);
                    Toast.makeText(AddFarmerActivity.this,getResources().getString(R.string.nid_value_enter),Toast.LENGTH_SHORT).show();
                    return;
                }

                String value = idtypespinner.getSelectedItem().toString();
                if(value.equalsIgnoreCase(SELECT_NATIONALID)){
                    Toast.makeText(AddFarmerActivity.this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getNidCaptureStatus(value).equalsIgnoreCase("true")) {
                    if(getCurrentNationalIdPhotoPath() == null){
                        Toast.makeText(AddFarmerActivity.this,getResources().getString(R.string.nid_photo_select),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

//              saving nid data
                int type = getNidType(value);
                NationalIdDTO idDTO = new NationalIdDTO();
                idDTO.setNationalidNumber(nid);
                idDTO.setNationalidtype(String.valueOf(type));
                idDTO.setPhoto(getCurrentNationalIdPhotoPath());
                nidDtoList.add(idDTO);
                selectednationalidList.add(value);
                adapterNationalId.notifyDataSetChanged();
                nidDialog.dismiss();
            }
        });

        nidDialog.show();
    }*/

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

    public void showtoast(int val){
        Toast.makeText(this,""+val,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.addnid){
//            fetchListValues();
           /* boolean typeadded = false;
            ArrayList<String> names = new ArrayList<>();
            for (int i = 0; i < adapterNationalId.getCount(); i++) {
                View view = nationalidslist.getChildAt(i);
                Spinner idtypespinner = view.findViewById(R.id.idtypespinner);
                EditText nidnumber = view.findViewById(R.id.nidnumber);
                String value = idtypespinner.getSelectedItem().toString();
                int type = getNidType(value);
                String nid = nidnumber.getText().toString();
                nidvalueMap.put(i, nid);
                typeMap.put(i,type);
                names.add(String.valueOf(type));
            }
            Set<String> store = new HashSet<>();
            for (String name : names) {
                if (store.add(name) == false) {
                    typeadded = true;
                }
            }*/
            if(! getSubmitStatus()) {
                addingEmptyValue(REQUEST_ADDING_ITEM);
            }else{
                Toast.makeText(this,getResources().getString(R.string.duplicate_nationalids),Toast.LENGTH_SHORT).show();
            }
        }
//        getSubmitStatus();
    }

    private boolean getSubmitStatus() {
        boolean typeadded = false;
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < adapterNationalId.getCount(); i++) {
            View view = nationalidslist.getChildAt(i);
            Spinner idtypespinner = view.findViewById(R.id.idtypespinner);
            EditText nidnumber = view.findViewById(R.id.nidnumber);
            String value = idtypespinner.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            nidvalueMap.put(i, nid);
            typeMap.put(i,type);
            names.add(String.valueOf(type));
        }
        Set<String> store = new HashSet<>();
        for (String name : names) {
            if (store.add(name) == false) {
                typeadded = true;
            }
        }
        return typeadded;
    }

    private String getnationalidSubmitStatus() {
        String typeadded = "false";
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < adapterNationalId.getCount(); i++) {
            View view = nationalidslist.getChildAt(i);
            Spinner idtypespinner = view.findViewById(R.id.idtypespinner);
            EditText nidnumber = view.findViewById(R.id.nidnumber);
            String value = idtypespinner.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            int limit = getNidLimit(idtypespinner.getSelectedItem().toString());
            if(limit < nid.length()){
                typeadded = LIMIT_XED;
            }
            if(adapterNationalId.getCount() == 1 && i==0){
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

    private JSONArray fetchListValues() {
        JSONArray storeableNationalids = new JSONArray();
        for (int i = 0; i < adapterNationalId.getCount(); i++) {
            View view = nationalidslist.getChildAt(i);
            Spinner idtypespinner = view.findViewById(R.id.idtypespinner);
            EditText nidnumber = view.findViewById(R.id.nidnumber);
            String value = idtypespinner.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            JSONObject storeableNids = new JSONObject();
            try {
                storeableNids.put("nationalidtype", type);
                storeableNids.put("nationalidNumber", nid);
                storeableNids.put("photo", nidDtoList.get(i).getPhoto());
                storeableNationalids.put(storeableNids);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating storable phone number", e));
                e.printStackTrace();
            }

        }

        return storeableNationalids;
    }

    private void bindFarmerNationalIDButton() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        File photoFile = null;

        try {
            photoFile = createImageFile("nationalidimage" +currentcapture_position );
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
            setCapturedNationalIdImageURI(fileUri);
            setCurrentNationalIdPhotoPath(fileUri.getPath());
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, getCapturedNationalIdImageURI());
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE_NATIONALID);
        }
    }

    public void captureSetImage(int position, NationalIdDTO idDTO, int type, String value) {
        capture__position = position;
        nidDTO = idDTO;
        typeMap.put(position, type);
        nidvalueMap.put(position, value);
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
                currentcapture_position ++;
                // Permission already granted
                bindFarmerNationalIDButton();
            }
        } else {
            currentcapture_position ++;
            // Call methods directly for Android versions lower than Marshmallow
            bindFarmerNationalIDButton();
        }
    }

    public void deleteValuesfromMap(int position,  int type, String value) {
        capture__position = position;
        if(typeMap.containsKey(position)) {
            typeMap.remove(position);
        }
        if(nidvalueMap.containsKey(position)) {
            nidvalueMap.remove(position);
        }
    }
}
