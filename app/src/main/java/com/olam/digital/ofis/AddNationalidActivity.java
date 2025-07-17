package com.olam.digital.ofis;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.JsonObject;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.businesslogic.GpsTrackerLogic;
import com.olam.digital.ofis.gps.dto.TrackDetailsDTO;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.model.NationalIdDTO;
import com.olam.digital.ofis.model.NationalIdTypeDTO;
import com.olam.digital.ofis.recordpolygon.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class AddNationalidActivity extends BaseActivity implements View.OnClickListener {
    private final static int MY_NATIONALID_PIC_PERMISSIONS_REQUEST = 9997;

    private final static int REQUEST_PERMISSIONS_FOR_TIRAMISU = 9925;
    private final static int REQUEST_IMAGE_CAPTURE_NATIONALID = 4;
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";
    private final static String CAPTURED_SIGNATURE_PHOTO_PATH_KEY = "mCurrentSignaturePhotoPath";
    private final static String CAPTURED_SIGNATURE_PHOTO_URI_KEY = "mCapturedSignatureImageURI";
    Long farmerId;
    ImageButton addNationId;
    ArrayList<String> selectednationalidList = new ArrayList<String>();
    ArrayList<String> nidtype;
    String farmername;
    boolean editnidmode = false;
    ArrayList<NationalIdTypeDTO> nidtypelist;
    int currentcapture_position;
    HashMap<Integer, Uri> hm_uri = new HashMap<>();
    HashMap<Integer, String> hm_uripath = new HashMap<>();
    Spinner spinner_nidtype;
//    TextView expirydate;
    ArrayAdapter<String> spinnerArrayAdapter;
    AppCompatButton save;
    boolean editmode;
    boolean isSynced;
    private JSONArray storeableNationalids = new JSONArray();
    private LayoutInflater inflater;
    private int nidvalue;
    private String mCurrentPhotoPath = null, mCurrentSignaturePhotoPath = null;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null;
    private LinearLayout nationalids;

    private Location mCurrentLocation = null;
    private GPSTracker gpsTracker = null;
    GPSListener gpsListener = null;

    boolean isUpdateNationalIdAvailable;

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private TextView national_id_error_msg;
    private final HashMap<Integer, String> existingNationalId = new HashMap<>();
    private HashMap<Integer, String> enteredNationalId = new HashMap<>();

    private int nationalIdCount;
    private final DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {

            // get the date
            Calendar cal = Calendar.getInstance();
            cal.set(selectedYear, selectedMonth, selectedDay);

            SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            String dateStrToSend = serverDateFormat.format(cal.getTime());
            String value = spinner_nidtype.getSelectedItem().toString();
            if (value.equalsIgnoreCase(SELECT_NATIONALID)) {
                Toast.makeText(AddNationalidActivity.this, getResources().getString(R.string.nationalidtype_selection), Toast.LENGTH_SHORT).show();
                return;
            }
            nidvalue = getNidType(value);
            String humanDate = DateFormat.getMediumDateFormat(context).format(cal.getTime());
            displayExpiryDateforNID(nationalids, nidvalue, humanDate);
//            farmerDOB.setText(humanDate);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_add_nationalid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        national_id_error_msg = (TextView) findViewById(R.id.national_id_error_msg);

        initComponents();
        try {
            farmerId = getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
            editmode = getIntent().getExtras().getBoolean("edit_mode");
            farmername = getIntent().getExtras().getString("FARMER_NAME");
        } catch (Exception e) {
        }
        dl.clearlocalniddbvalue();
        isSynced = dl.getSyncedStatus(farmerId);
        isUpdateNationalIdAvailable = dl.isUpdateNationalIdAvailable(farmerId);

        boolean availstatus = dl.isValueavailable(farmerId);
        this.setTitle(res.getString(availstatus ? R.string.update_nationalid : R.string.add_nationalid));


        if (!editmode) {
            bindnationalid();
        } else {
            bindNationalidValues();
        }
        initLocationSetup();
    }

    private void initLocationSetup() {
        gpsListener = new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location != null && !GPSUtils.isMockLocation(location)) {
                    mCurrentLocation = location;
                }
            }
        };
        gpsTracker = new GPSTracker(this, gpsListener);
//        if (gpsTracker.isGpsEnabled()) {
        gpsTracker.startLocationUpdateInitially();
    }

    @Override
    protected void onResume() {
        super.onResume();
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


        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

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

        super.onRestoreInstanceState(savedInstanceState);
    }


    private void setCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }


    private void setCurrentNIDPhotoPath(HashMap<Integer, String> hm_uripath) {
        this.hm_uripath = hm_uripath;
    }


    private void setCapturedImageURI(Uri mCapturedImageURI) {
        this.mCapturedImageURI = mCapturedImageURI;
    }

    private void setCapturedNIDImageURI(HashMap<Integer, Uri> hm_uri) {
        this.hm_uri = hm_uri;
    }


    private void bindnationalid() {
        ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
        LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);

        LinearLayout niddelete = nationalidinclude.findViewById(R.id.deleteNationalid);
        niddelete.setVisibility(View.GONE);
        spinner_nidtype = nationalidinclude.findViewById(R.id.idtypespinner);
        TextView expiryDate = nationalidinclude.findViewById(R.id.expirydate);
        LinearLayout ll_capturephoto = nationalidinclude.findViewById(R.id.ll_capturephoto);
        LinearLayout ll_captureexpiry = nationalidinclude.findViewById(R.id.ll_captureexpiry);
        spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_nidtype.setAdapter(spinnerArrayAdapter);

        EditText nidnumber = nationalidinclude.findViewById(R.id.nidnumber);
        nidnumber.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
        nidnumber.setFilters(new InputFilter[]{LetterDigitHyphenFilter,blockPiFilter});
        nidnumber.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
        ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
        actionbtn.setTag(nationalids.getChildCount());
        nationalidphoto.setTag(nationalids.getChildCount());
        expiryDate.setTag(nationalids.getChildCount());
//        ll_captureexpiry.setVisibility(View.GONE);
        expiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(AddFarmerActivity.this, "clicked", Toast.LENGTH_LONG).show();
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddNationalidActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                               /* expiryDate.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);*/
                                Calendar cal = Calendar.getInstance();
                                cal.set(year, monthOfYear, dayOfMonth);

                                SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                String humanDate = serverDateFormat.format(cal.getTime());
                                expiryDate.setText(humanDate);

                            }
                        }, mYear, mMonth, mDay);
                setRangeofDatePicker(datePickerDialog,spinner_nidtype.getSelectedItem().toString());
                datePickerDialog.show();
            }
        });

        spinner_nidtype.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    String selected_item = spinner_nidtype.getSelectedItem().toString();
                    int type = getNidType(selected_item);
                    selectednationalidList.remove(selected_item);
                    ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);
                    nidnumber.setText("");
                    expiryDate.setText("");
                    nationalidphoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_capture_id_default));
                    dl.removeNIDPhoto(type);
                }
                return false;
            }
        });
        spinner_nidtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinner_nidtype.getSelectedItem().toString().equalsIgnoreCase(SELECT_NATIONALID)) {
                    int limit = getNidLimit(spinner_nidtype.getSelectedItem().toString());
                    InputFilter[] filters = new InputFilter[1];
                    filters[0] = new InputFilter.LengthFilter(limit); //Filter to 10 characters
                    nidnumber.setFilters(filters);
                    nidnumber.setFilters(new InputFilter[]{LetterDigitHyphenFilter,blockPiFilter});
                    if (getNidCaptureStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                        ll_capturephoto.setVisibility(View.GONE);
                    } else {
                        ll_capturephoto.setVisibility(View.VISIBLE);
                    }
                    if (getNidExpiryDateStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                        ll_captureexpiry.setVisibility(View.GONE);
                    } else {
                        ll_captureexpiry.setVisibility(View.VISIBLE);
                    }
                    ValidateSpinnerTypes();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bindnationlidcapturephoto(nationalidinclude.findViewById(R.id.idtypespinner), nationalidinclude.findViewById(R.id.nidphotoActionButton));
        nationalids.addView(nationalidinclude);

    }

    private void bindNationalidValues() {
        if (farmerId != 0) {
            Cursor nids = dl.getnationalidsbyfarmerids(farmerId, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID);
            ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
            try {
                int i = 0;
                while (nids.moveToNext()) {
                    nationalIdCount = i;
                    LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
                    Spinner spinner_nidtype = nationalidinclude.findViewById(R.id.idtypespinner);
                    LinearLayout niddelete = nationalidinclude.findViewById(R.id.deleteNationalid);
                    ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                    ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
                    LinearLayout ll_capturephoto = nationalidinclude.findViewById(R.id.ll_capturephoto);
                    LinearLayout ll_captureexpiry = nationalidinclude.findViewById(R.id.ll_captureexpiry);
                    EditText nidnumber = nationalidinclude.findViewById(R.id.nidnumber);
                    TextView dbIdStore = nationalidinclude.findViewById(R.id.dbIdStore);
                    TextView expiryDate = nationalidinclude.findViewById(R.id.expirydate);
                    nidnumber.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
                    nidnumber.setFilters(new InputFilter[]{LetterDigitHyphenFilter,blockPiFilter});
                    nidnumber.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//                    ll_captureexpiry.setVisibility(View.GONE);
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.nidtype_spinner, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.nidtype_spinner);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);

                    String farmerNIDPath = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO));
                    String dbId = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_ID));

                    actionbtn.setClickable(Objects.equals(farmerNIDPath, "null"));
                    if (i == 0) {
                        niddelete.setVisibility(View.INVISIBLE);
                    } else {
                        niddelete.setVisibility(View.VISIBLE);
                    }
                    i++;
                    if (dbId != null) {
                       // niddelete.setVisibility(View.GONE);
                        dbIdStore.setTag(dbId);
                    }
                    String nidNumber = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER));
                    nidnumber.setText(nidNumber);
                    try {
                        String expirydatevalue = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_EXPIRY_DATE));
                        long millisecond = Long.parseLong(expirydatevalue);
                        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date(millisecond));
                        expiryDate.setText(dateString);
                    }catch (Exception e){
                        expiryDate.setText(nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_EXPIRY_DATE)));
                    }
                    Integer nidType = nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE));
                    String selected_item = getNidTypebyid(nidType);
                    int spinnerPosition = spinnerArrayAdapter.getPosition(selected_item);
                    spinner_nidtype.setSelection(spinnerPosition);

                    String duplicateNationalId = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_DUPLICATE));
                    if (duplicateNationalId != null) {
                        if (Objects.equals(duplicateNationalId , "true")) {
                            nidnumber.setBackground(getResources().getDrawable(R.drawable.edittext_bg_error));
                            national_id_error_msg.setVisibility(View.VISIBLE);
                            existingNationalId.put(nidType, nidNumber);
                            enteredNationalId.put(nidType,nidNumber);
                        } else {
                            nidnumber.setBackground(getResources().getDrawable(R.drawable.edittext_bg_light));
                            national_id_error_msg.setVisibility(View.GONE);
                        }
                    }
                    nidnumber.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                            String selected_item = spinner_nidtype.getSelectedItem().toString();
                            int type = getNidType(selected_item);
                            String nidNumber = s.toString();
                            enteredNationalId.put(type, nidNumber);

                            boolean duplicateNid = existingNationalId.containsKey(type) && Objects.equals(existingNationalId.get(type), nidNumber);
                            nidnumber.setBackground(getResources().getDrawable(duplicateNid ? R.drawable.edittext_bg_error : R.drawable.edittext_bg_light));

                            boolean isDuplicate = Utilities.isDuplicateNationalId(existingNationalId, enteredNationalId);
                            national_id_error_msg.setVisibility(isDuplicate ? View.VISIBLE : View.GONE);
                        }
                    });


                    if (farmerNIDPath != null && farmerNIDPath.length() > 0) {
                        int baseWidth = 100;
                        int baseHeight = 133;


                        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

                        Bitmap bitmap = Utilities.loadSizedBitmap(farmerNIDPath, outputWidth, outputHeight);

                        nationalidphoto.setImageBitmap(bitmap);

                        nationalidphoto.setTag(nationalids.getChildCount());
                    }
                    actionbtn.setTag(nationalids.getChildCount());

                    expiryDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                Toast.makeText(AddFarmerActivity.this, "clicked", Toast.LENGTH_LONG).show();
                            final Calendar c = Calendar.getInstance();
                            int mYear = c.get(Calendar.YEAR); // current year
                            int mMonth = c.get(Calendar.MONTH); // current month
                            int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                            // date picker dialog
                            DatePickerDialog   datePickerDialog = new DatePickerDialog(AddNationalidActivity.this,
                                    new DatePickerDialog.OnDateSetListener() {

                                        @Override
                                        public void onDateSet(DatePicker view, int year,
                                                              int monthOfYear, int dayOfMonth) {
                                            // set day of month , month and year value in the edit text
                                           /* expiryDate.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);*/
                                            Calendar cal = Calendar.getInstance();
                                            cal.set(year, monthOfYear, dayOfMonth);

                                            SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                            String humanDate = serverDateFormat.format(cal.getTime());
                                            expiryDate.setText(humanDate);
                                        }
                                    }, mYear, mMonth, mDay);
                            setRangeofDatePicker(datePickerDialog,spinner_nidtype.getSelectedItem().toString());
                            datePickerDialog.show();
                        }
                    });
                    spinner_nidtype.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                String selected_item = spinner_nidtype.getSelectedItem().toString();
                                int type = getNidType(selected_item);
                                selectednationalidList.remove(selected_item);
                                ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                                spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
                                spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                spinner_nidtype.setAdapter(spinnerArrayAdapter);
                                nidnumber.setText("");
                                nationalidphoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_capture_id_default));
                                dl.removeNIDPhoto(type);
                            }
                            return false;
                        }
                    });
                    spinner_nidtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (!spinner_nidtype.getSelectedItem().toString().equalsIgnoreCase(SELECT_NATIONALID)) {

                                int limit = getNidLimit(spinner_nidtype.getSelectedItem().toString());
                                InputFilter[] filters = new InputFilter[1];
                                filters[0] = new InputFilter.LengthFilter(limit); //Filter to 10 characters
                                nidnumber.setFilters(filters);
                                nidnumber.setFilters(new InputFilter[]{LetterDigitHyphenFilter,blockPiFilter});
                                if (getNidCaptureStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                                    ll_capturephoto.setVisibility(View.GONE);
                                } else {
                                    ll_capturephoto.setVisibility(View.VISIBLE);
                                }
                                if (getNidExpiryDateStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                                    ll_captureexpiry.setVisibility(View.GONE);
                                } else {
                                    ll_captureexpiry.setVisibility(View.VISIBLE);
                                }
                                ValidateSpinnerTypes();
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    bindnationlidcapturephoto(nationalidinclude.findViewById(R.id.idtypespinner), nationalidinclude.findViewById(R.id.nidphotoActionButton));
                    bindniddelete(nationalidinclude.findViewById(R.id.deleteNationalid));

                    nationalids.addView(nationalidinclude);
                }

            } catch (Exception e) {
                FirebaseCrash.report(new Exception("NetworkLayer: Error creating farmer phone numbers object", e));
                e.printStackTrace();
            }


        }
    }

    private void bindniddelete(LinearLayout trashbutton) {

        trashbutton.setTag(nationalids.getChildCount());
        trashbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nationalIdCount--;
                if(nationalIdCount <= 9){
                    addNationId.setVisibility(View.VISIBLE);
                }else{
                    addNationId.setVisibility(View.GONE);
                }
                LinearLayout numberrow = (LinearLayout) v.getParent();
                removeSpinnerItem(numberrow);
                ((ViewGroup) numberrow.getParent()).removeView(numberrow);
                removehmvalue(hm_uripath, (int) trashbutton.getTag());
                removehmURIvalue(hm_uri, (int) trashbutton.getTag());
                refreshTags(nationalids);
                ValidateNIDValues();

            }
        });
    }

    private boolean ValidateNIDValues() {
        boolean status = true;
        ArrayList<String> nidtype_list = new ArrayList<>();
        selectednationalidList = new ArrayList<String>();

        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            View nidrow = nationalids.getChildAt(j);
            EditText nidnumber = nidrow.findViewById(R.id.nidnumber);
            TextView expiryDate = nidrow.findViewById(R.id.expirydate);
            Spinner nid_type = nidrow.findViewById(R.id.idtypespinner);
            TextView dbIdStore = nidrow.findViewById(R.id.dbIdStore);

            nidnumber.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
            nidnumber.setFilters(new InputFilter[]{LetterDigitHyphenFilter,blockPiFilter});
            nidnumber.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            ImageButton nidphotoActionButton = nidrow.findViewById(R.id.nidphotoActionButton);
            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            String expDate = expiryDate.getText().toString();

            if (nid.trim() == "" || nid.trim().equalsIgnoreCase("")) {
                Toast.makeText(this, getResources().getString(R.string.nid_value_enter), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (value.equalsIgnoreCase(SELECT_NATIONALID)) {
                Toast.makeText(this, getResources().getString(R.string.nationalidtype_selection), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (nidtype_list.size() != 0 && nidtype_list.contains(value)) {
                nid_type.setEnabled(true);
                Toast.makeText(this, getResources().getString(R.string.nationalidtype_alreadyselected), Toast.LENGTH_SHORT).show();
                return false;
            } else {
                nidtype_list.add(value);
                selectednationalidList.add(value);
            }
            if (getNidCaptureStatus(value).equalsIgnoreCase("true")) {
                if (!editmode) {
                    if (!getPhotostatusByType(String.valueOf(type))) {
                        nidphotoActionButton.setClickable(true);
                        Toast.makeText(this, getResources().getString(R.string.nid_photo_select), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                else if (isUpdateNationalIdAvailable) {
                    if (getUpdateNIDPhotoStatusByType(String.valueOf(type), farmerId)) {
                        continue;
                    }

                    if (!getPhotostatusByType(String.valueOf(type))) {
                        nidphotoActionButton.setClickable(true);
                        Toast.makeText(this, getResources().getString(R.string.nid_photo_select), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                else {
                    if (!(getNIDPhotostatusByType(String.valueOf(type), farmerId, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID) || getPhotostatusByType(String.valueOf(type)))) {
                        nidphotoActionButton.setClickable(true);
                        Toast.makeText(this, getResources().getString(R.string.nid_photo_select), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
            if (getNidExpiryDateStatus(value).equalsIgnoreCase("true")) {
                if (expDate.trim() == "" || expDate.trim().equalsIgnoreCase("")) {
                    Toast.makeText(this, getResources().getString(R.string.nid_exp_fill), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            if (status) {
                NationalIdDTO idDTO = new NationalIdDTO();
                idDTO.setNationalidNumber(nid);
                idDTO.setNationalidtype(String.valueOf(type));
                if (dbIdStore.getTag() != null) {
                    idDTO.setBackendDbId(dbIdStore.getTag().toString());
                }
                if (getNidCaptureStatus(value).equalsIgnoreCase("true")) {
                    String photopath = null;
                    if (!editmode || isUpdateNationalIdAvailable) {
                        photopath = getPhotobytype(type);
                    } else {
                        photopath = getNIDPhotoByType(String.valueOf(type), farmerId, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID);
                    }
                    idDTO.setPhoto(photopath);
                } else {
                    idDTO.setPhoto("");
                }
                if (getNidExpiryDateStatus(value).equalsIgnoreCase("true")) {
                    idDTO.setExpiryDate(expDate);
                }
                dl.addnewLocalFarmerNationalids(idDTO);
            }
        }
        return status;
    }


    private void removeSpinnerItem(LinearLayout numberrow) {
        try {
            Spinner spinner = numberrow.findViewById(R.id.idtypespinner);
            String val = spinner.getSelectedItem().toString();

            for (int i = 0; i < selectednationalidList.size(); i++) {
                if (val.equalsIgnoreCase(selectednationalidList.get(i))) {
                    selectednationalidList.remove(i);
                }
            }
            int type = getNidType(val);
            existingNationalId.remove(type);
            enteredNationalId.remove(type);
            boolean isDuplicate = Utilities.isDuplicateNationalId(existingNationalId, enteredNationalId);
            national_id_error_msg.setVisibility(isDuplicate ? View.VISIBLE : View.GONE);
            dl.removeNIDPhoto(type);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void initComponents() {
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        nationalids = findViewById(R.id.nationalids);
        addNationId = findViewById(R.id.addnid);
        save = findViewById(R.id.save);
        if(nationalIdCount == 9){
            addNationId.setVisibility(View.GONE);
        }
        save.setOnClickListener(this);
        addNationId.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.addnid) {
            nationalIdCount++;
            if(nationalIdCount == 9){
                addNationId.setVisibility(View.GONE);
            }else{
                addNationId.setVisibility(View.VISIBLE);
            }
            if (!ValidateNIDValues()) return;
            try {
                if (!validateSpinnerValues(nationalids, spinner_nidtype.getSelectedItem().toString())) {
                    Toast.makeText(AddNationalidActivity.this, getResources().getString(R.string.nationalidtype_selection), Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if (validateSpinnerSelection()) {
                ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                if (refreshitem.size() != 0) {
                    editnidmode = true;
                    LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
                    nationalids.addView(nationalidinclude);
                    LinearLayout ll_capturephoto = nationalidinclude.findViewById(R.id.ll_capturephoto);
                    LinearLayout ll_captureexpiry = nationalidinclude.findViewById(R.id.ll_captureexpiry);
                    Spinner spinner_nidtype = nationalidinclude.findViewById(R.id.idtypespinner);
                    TextView expiryDate = nationalidinclude.findViewById(R.id.expirydate);
                    ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                    ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
                    actionbtn.setTag(nationalids.getChildCount());
                    nationalidphoto.setTag(nationalids.getChildCount());
                    EditText nidnumber = nationalidinclude.findViewById(R.id.nidnumber);
//                    ll_captureexpiry.setVisibility(View.GONE);
                    nidnumber.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
                    nidnumber.setFilters(new InputFilter[]{LetterDigitHyphenFilter,blockPiFilter});
                    nidnumber.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);

                    expiryDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                Toast.makeText(AddFarmerActivity.this, "clicked", Toast.LENGTH_LONG).show();
                            final Calendar c = Calendar.getInstance();
                            int mYear = c.get(Calendar.YEAR); // current year
                            int mMonth = c.get(Calendar.MONTH); // current month
                            int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                            // date picker dialog
                            DatePickerDialog  datePickerDialog = new DatePickerDialog(AddNationalidActivity.this,
                                    new DatePickerDialog.OnDateSetListener() {

                                        @Override
                                        public void onDateSet(DatePicker view, int year,
                                                              int monthOfYear, int dayOfMonth) {
                                            // set day of month , month and year value in the edit text
                                          /* expiryDate.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);*/
                                            Calendar cal = Calendar.getInstance();
                                            cal.set(year, monthOfYear, dayOfMonth);

                                            SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                            String humanDate = serverDateFormat.format(cal.getTime());
                                            expiryDate.setText(humanDate);

                                        }
                                    }, mYear, mMonth, mDay);
                            setRangeofDatePicker(datePickerDialog,spinner_nidtype.getSelectedItem().toString());
                            datePickerDialog.show();
                        }
                    });
                    spinner_nidtype.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                String selected_item = spinner_nidtype.getSelectedItem().toString();
                                int type = getNidType(selected_item);
                                selectednationalidList.remove(selected_item);
                                ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                                spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
                                spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                spinner_nidtype.setAdapter(spinnerArrayAdapter);
                                nidnumber.setText("");
                                nationalidphoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_capture_id_default));
                                dl.removeNIDPhoto(type);
                            }
                            return false;
                        }
                    });
                    spinner_nidtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (!spinner_nidtype.getSelectedItem().toString().equalsIgnoreCase(SELECT_NATIONALID)) {
                                int limit = getNidLimit(spinner_nidtype.getSelectedItem().toString());
                                InputFilter[] filters = new InputFilter[1];
                                filters[0] = new InputFilter.LengthFilter(limit); //Filter to 10 characters
                                nidnumber.setFilters(filters);
                                nidnumber.setFilters(new InputFilter[]{LetterDigitHyphenFilter,blockPiFilter});
                                if (getNidCaptureStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                                    ll_capturephoto.setVisibility(View.GONE);
                                } else {
                                    ll_capturephoto.setVisibility(View.VISIBLE);
                                }
                                if (getNidExpiryDateStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")) {
                                    ll_captureexpiry.setVisibility(View.GONE);
                                } else {
                                    ll_captureexpiry.setVisibility(View.VISIBLE);
                                }
                                ValidateSpinnerTypes();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    bindniddelete(nationalidinclude.findViewById(R.id.deleteNationalid));
                    bindnationlidcapturephoto(nationalidinclude.findViewById(R.id.idtypespinner), nationalidinclude.findViewById(R.id.nidphotoActionButton));

                }
            }

        }
        else if (id == R.id.save) {

            // check the current location
            if(mCurrentLocation != null && !GPSUtils.isMockLocation(mCurrentLocation)){
                // check the current location
            }else {
                if (mCurrentLocation == null || !gpsTracker.isGpsEnabled()) {
                    gpsTracker.getDeviceLocation();
                    return;
                }
            }
            if (!ValidateNIDValues()) return;
            try {
                if (!validateSpinnerValues(nationalids, spinner_nidtype.getSelectedItem().toString())) {
                    Toast.makeText(AddNationalidActivity.this, getResources().getString(R.string.nationalidtype_selection), Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if (getnationalidSubmitStatus(nationalids).equalsIgnoreCase(DUPLICATE)) {
                Toast.makeText(this, getResources().getString(R.string.duplicate_nationalids), Toast.LENGTH_SHORT).show();
                return;
            } else if (getnationalidSubmitStatus(nationalids).equalsIgnoreCase(LIMIT_XED)) {
                Toast.makeText(this, getResources().getString(R.string.duplicate_nationalids), Toast.LENGTH_SHORT).show();
                return;
            } else if (getnationalidSubmitStatus(nationalids).equalsIgnoreCase(ENTER_NID)) {
                Toast.makeText(this, getResources().getString(R.string.enter_nationalids), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isDuplicate = Utilities.isDuplicateNationalId(existingNationalId, enteredNationalId);
            if (isDuplicate) {
                Toast.makeText(this, getResources().getString(R.string.duplicate_nationalids), Toast.LENGTH_SHORT).show();
                return;
            }
            storeableNationalids = fetchnationalidvalues();
            boolean statusmoduration;
            if (isUpdateNationalIdAvailable) {
                statusmoduration = dl.updateFarmerNationalids(context, farmerId, null, storeableNationalids, "updateNationalId", farmername, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID);
            }
            else {
                statusmoduration = dl.updateFarmerNationalids(context, farmerId, null, storeableNationalids, "2", farmername, OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID);
            }

            // collect the national id types
            ArrayList nationalIdTypes= new ArrayList<String>();
            try{
                if(storeableNationalids.length() > 0){

                    int length = storeableNationalids.length();
                    for (int j = 0; j < length; j++) {
                        try {
                            nationalIdTypes.add(storeableNationalids.getJSONObject(j).get("nationalIdType").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }catch (Exception e){}

            collectGPSPoint(nationalIdTypes);

            save.setEnabled(false);
            if (statusmoduration) {
                Toast.makeText(this, getResources().getString(R.string.moderation_note), Toast.LENGTH_LONG).show();
            }
            onBackPressed();
        }
    }

    private void collectGPSPoint(ArrayList nationalIdTypes) {
        GpsTrackerLogic gpsTrackerLogic=new GpsTrackerLogic();
        GPSUtils gpsUtils=new GPSUtils();
        ArrayList<String> nationalIdTypeNames =gpsTrackerLogic.getNationalIdNamesnyIds(this,nationalIdTypes);
        TrackDetailsDTO trackDetailsDTO=new TrackDetailsDTO();
        trackDetailsDTO.setModule(GPSConstants.MODULE_ADD_NATIONAL_ID);
        trackDetailsDTO.setLatitude( String.valueOf(mCurrentLocation.getLatitude()));
        trackDetailsDTO.setLongitude(String.valueOf(mCurrentLocation.getLongitude()));
        trackDetailsDTO.setAccuracy(String.valueOf(mCurrentLocation.getAccuracy()));
        trackDetailsDTO.setRecordDate(new Timestamp(System.currentTimeMillis()).toString());
        trackDetailsDTO.setFarmerId(String.valueOf(farmerId));
        trackDetailsDTO.setFarmerName(farmername);

        // collecting in independent records
        for ( String type : nationalIdTypeNames){
            trackDetailsDTO.setNationalId(type);
            ContentValues geoContentValues = gpsUtils.getGeoContentValues(trackDetailsDTO);
            gpsTrackerLogic.insertGeoDataDetails(context, geoContentValues);
        }

    }



    @Override
    protected void onStop() {
        super.onStop();
        try {
            gpsTracker.stopLocationUpdates();
        } catch (Exception e) {
        }

    }

    private boolean validateSpinnerSelection() {
        boolean status = true;
        ArrayList<String> values = refreshNationalidtype(setNationalidtype());
        if (values.size() == 1) {
            return false;
        }
        return status;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private ArrayList<String> refreshNationalidtype(ArrayList<String> value) {
        for (int i = 0; i < selectednationalidList.size(); i++) {
            value.remove(selectednationalidList.get(i));
        }

        return value;
    }


    private void bindnationlidcapturephoto(Spinner spinner, ImageButton actionbutton) {

        actionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = spinner.getSelectedItem().toString();
                if (value.equalsIgnoreCase(SELECT_NATIONALID)) {
                    Toast.makeText(AddNationalidActivity.this, getResources().getString(R.string.nationalidtype_selection), Toast.LENGTH_SHORT).show();
                    return;
                }
                nidvalue = getNidType(value);
                currentcapture_position = (int) actionbutton.getTag();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(AddNationalidActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(AddNationalidActivity.this,
                                new String[]{android.Manifest.permission.CAMERA}, MY_NATIONALID_PIC_PERMISSIONS_REQUEST);
                    }
                    else {
                        bindFarmerNationalIDButton();
                    }
                }
                else {
                    bindFarmerNationalIDButton();
                }
            }
        });
    }

    private void bindFarmerNationalIDButton() {
        // New Implementation
        File photoFile = null;
        try {
            photoFile = createImageFile("nationalidimage_" + currentcapture_position);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (photoFile != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if ((intent.resolveActivity(getPackageManager()) != null)) {
               // Uri photoURI = FileProvider.getUriForFile(AddNationalidActivity.this, getApplicationContext().getPackageName() + ".profileimage.fileprovider", photoFile);
                Uri photoURI = FileProvider.getUriForFile(AddNationalidActivity.this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);

                // Samsung Galaxy S3 Fix
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                removehmURIvalue(hm_uri, currentcapture_position);
                removehmvalue(hm_uripath, currentcapture_position);
                hm_uri.put(currentcapture_position, photoURI);
                hm_uripath.put(currentcapture_position, photoFile.getPath());
                setCapturedNIDImageURI(hm_uri);
                setCurrentNIDPhotoPath(hm_uripath);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, hm_uri.get(currentcapture_position));
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_NATIONALID);

            }
        }
    }

    private File createImageFile(String portraitOrSignature) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName;
        if (!portraitOrSignature.contains("nationalidimage")) {
            imageFileName = (portraitOrSignature.equalsIgnoreCase("portrait")) ? "ofis_farmer_portrait_" + timestamp + "_" : "ofis_farmer_signature_" + timestamp + "_";
        } else {
            imageFileName = portraitOrSignature + "_" + timestamp + "_";
        }
        File storageDir = getExternalFilesDir("");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        if (portraitOrSignature.equalsIgnoreCase("portrait")) {
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        } else {
            mCurrentSignaturePhotoPath = "file:" + image.getAbsolutePath();
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == Activity.RESULT_CANCELED) return;
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE_NATIONALID:
                if (resultCode == RESULT_OK) {
                    setNIDImageFromFilePath(hm_uripath.get(currentcapture_position), "signature");
                }
                break;
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == -1) {
                    gpsTracker.getDeviceLocation();
                }
                break;
            default:
        }
    }

    private ArrayList<String> setNationalidtype() {
        String language = Locale.getDefault().getLanguage();
        nidtypelist = getModules();
        nidtype = new ArrayList<String>();
        nidtype.add(SELECT_NATIONALID);
        if (nidtypelist.size() > 0) {
            for (NationalIdTypeDTO moduleDTO : nidtypelist) {
                if (language.equalsIgnoreCase(Utilities.FR)) {
                    nidtype.add(moduleDTO.getTypeFr());
                } else if (language.equalsIgnoreCase(Utilities.ES)) {
                    nidtype.add(moduleDTO.getTypeEs());
                } else if (language.equalsIgnoreCase(Utilities.PT)) {
                    nidtype.add(moduleDTO.getTypePt());
                } else if (language.equalsIgnoreCase(Utilities.ID)) {
                    nidtype.add(moduleDTO.getTypeId());
                } else if (language.equalsIgnoreCase(Utilities.TR)) {
                    nidtype.add(moduleDTO.getTypeTr());
                } else if (language.equalsIgnoreCase(Utilities.LO)) {
                    nidtype.add(moduleDTO.getTypeLo());
                } else if (language.equalsIgnoreCase(Utilities.VI)) {
                    nidtype.add(moduleDTO.getTypeVi());
                } else if (language.equalsIgnoreCase(Utilities.TH)) {
                    nidtype.add(moduleDTO.getTypeTh());
                } else {
                    nidtype.add(moduleDTO.getType());
                }
            }
        }

        return nidtype;
    }

    private ArrayList<NationalIdTypeDTO> getModules() {
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

    private void setNIDImageFromFilePath(String imagePath, String portraitOrSignature) {
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, portraitOrSignature);
        if (savedImagePath != null) {
            Uri uri = Uri.parse(savedImagePath);
            setCapturedImageURI(uri);
            setCurrentPhotoPath(savedImagePath);

            dl.updateNIDPhoto(nidvalue, savedImagePath);
            if (editmode && !isUpdateNationalIdAvailable) {
                dl.removeNIDPhotostatusByType(String.valueOf(nidvalue), farmerId);
            }
            displayBitmapforNID(nationalids, nidvalue, savedImagePath);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_NATIONALID_PIC_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                bindFarmerNationalIDButton();
            } else if (Build.VERSION.SDK_INT >= 23 && (!shouldShowRequestPermissionRationale(permissions[0]) || !shouldShowRequestPermissionRationale(permissions[1]))) {
                showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));
            } else {
                Toast.makeText(this, R.string.camera_and_gallery_permission_denial_msg, Toast.LENGTH_LONG).show();
            }
        }
    }


    public InputFilter LetterDigitHyphenFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                char ch = source.charAt(i);
                if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '.') {
                    return "";
                }
            }
            return null; // accept original input
        }
    };

    public InputFilter blockPiFilter = (source, start, end, dest, dstart, dend) -> {
        StringBuilder filtered = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (c != '\u03C0') { // Unicode for π
                filtered.append(c);
            }
        }
        return filtered.toString();
    };



    private void ValidateSpinnerTypes() {
        ArrayList<String> nidtype_list = new ArrayList<>();
        ArrayList<Integer> selected_types = new ArrayList<>();
        selectednationalidList = new ArrayList<String>();
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            View nidrow = nationalids.getChildAt(j);
            Spinner nid_type = nidrow.findViewById(R.id.idtypespinner);
            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
            if (!nidtype_list.contains(value)) {
                nidtype_list.add(value);
                selectednationalidList.add(value);
                selected_types.add(type);
            }
        }
        compareTypes(selected_types, getAllphotoTypes());
    }

}
