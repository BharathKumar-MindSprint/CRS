package com.olam.digital.ofis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.model.NationalIdDTO;
import com.olam.digital.ofis.model.NationalIdTypeDTO;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;



public class AddNationalidActivity extends BaseActivity implements View.OnClickListener {

    private final static int MY_NATIONALID_PIC_PERMISSIONS_REQUEST = 9997;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_add_nationalid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initComponents();
        try {
            farmerId = getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
            editmode = getIntent().getExtras().getBoolean("edit_mode");
            farmername = getIntent().getExtras().getString("FARMER_NAME");
        } catch (Exception e) {
        }
        dl.clearlocalniddbvalue();
        isSynced = dl.getSyncedStatus(farmerId);
        if (isSynced) {
            this.setTitle("View National ID");
            addNationId.setVisibility(View.INVISIBLE);
            save.setVisibility(View.INVISIBLE);
        }
        if (!editmode) {
            bindnationalid();
        } else {
            bindNationalidValues();
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
        nationalids.addView(nationalidinclude);

        LinearLayout niddelete = nationalidinclude.findViewById(R.id.deleteNationalid);
        niddelete.setVisibility(View.GONE);
        spinner_nidtype = nationalidinclude.findViewById(R.id.idtypespinner);
        LinearLayout ll_capturephoto = nationalidinclude.findViewById(R.id.ll_capturephoto);
        spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_nidtype.setAdapter(spinnerArrayAdapter);

        EditText nidnumber = nationalidinclude.findViewById(R.id.nidnumber);
        ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
        ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
        actionbtn.setTag(nationalids.getChildCount());
        nationalidphoto.setTag(nationalids.getChildCount());

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

        bindnationlidcapturephoto(nationalidinclude.findViewById(R.id.idtypespinner), nationalidinclude.findViewById(R.id.nidphotoActionButton));

        if (isSynced) {
            niddelete.setVisibility(View.GONE);
            spinner_nidtype.setEnabled(false);
            actionbtn.setVisibility(View.INVISIBLE);
            nidnumber.setEnabled(false);
        }

    }

    private void bindNationalidValues() {
        if (farmerId != 0) {
            Cursor nids = dl.getnationalidsbyfarmerid(farmerId);
            ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
            try {
                int i = 0;
                while (nids.moveToNext()) {
                    LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
                    nationalids.addView(nationalidinclude);
                    Spinner spinner_nidtype = nationalidinclude.findViewById(R.id.idtypespinner);
                    LinearLayout niddelete = nationalidinclude.findViewById(R.id.deleteNationalid);
                    ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                    ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
                    LinearLayout ll_capturephoto = nationalidinclude.findViewById(R.id.ll_capturephoto);
                    EditText nidnumber = nationalidinclude.findViewById(R.id.nidnumber);
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.nidtype_spinner, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.nidtype_spinner);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);
                    actionbtn.setClickable(false);
                    spinner_nidtype.setEnabled(false);
                    if (i == 0) {
                        niddelete.setVisibility(View.INVISIBLE);
                    } else {
                        niddelete.setVisibility(View.VISIBLE);
                    }
                    i++;
                    nidnumber.setText(nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER)));
                    String selected_item = getNidTypebyid(nids.getInt(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE)));
                    int spinnerPosition = spinnerArrayAdapter.getPosition(selected_item);
                    spinner_nidtype.setSelection(spinnerPosition);

                    String farmerNIDPath = nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO));
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
                    bindnationlidcapturephoto(nationalidinclude.findViewById(R.id.idtypespinner), nationalidinclude.findViewById(R.id.nidphotoActionButton));
                    bindniddelete(nationalidinclude.findViewById(R.id.deleteNationalid));

                    if (isSynced) {
                        niddelete.setVisibility(View.GONE);
                        spinner_nidtype.setEnabled(false);
                        actionbtn.setVisibility(View.INVISIBLE);
                        nidnumber.setEnabled(false);
                    }
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
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);
            EditText nidnumber = nidrow.findViewById(R.id.nidnumber);
            Spinner nid_type = nidrow.findViewById(R.id.idtypespinner);
            ImageButton nidphotoActionButton = nidrow.findViewById(R.id.nidphotoActionButton);
            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();

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
//                if(! hm_uripath.containsKey(j+1)){
                if (!editmode) {
                    if (!getPhotostatusByType(String.valueOf(type))) {
                        nidphotoActionButton.setClickable(true);
                        Toast.makeText(this, getResources().getString(R.string.nid_photo_select), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    if (!(getNIDPhotostatusByType(String.valueOf(type), farmerId) || getPhotostatusByType(String.valueOf(type)))) {
                        nidphotoActionButton.setClickable(true);
                        Toast.makeText(this, getResources().getString(R.string.nid_photo_select), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }

            if (status) {
                NationalIdDTO idDTO = new NationalIdDTO();
                idDTO.setNationalidNumber(nid);
                idDTO.setNationalidtype(String.valueOf(type));
                if (getNidCaptureStatus(value).equalsIgnoreCase("true")) {
                    String photopath = null;
                    if (!editmode) {
                        photopath = getPhotobytype(type);
                    } else {
                        photopath = getNIDPhotoByType(String.valueOf(type), farmerId);
                    }
                    idDTO.setPhoto(photopath);
                } else {
                    idDTO.setPhoto("");
                }

                dl.addnewLocalFarmerNationalids(idDTO);
                nid_type.setEnabled(false);
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
        save.setOnClickListener(this);
        addNationId.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.addnid) {
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
                    Spinner spinner_nidtype = nationalidinclude.findViewById(R.id.idtypespinner);

                    ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                    ImageButton nationalidphoto = nationalidinclude.findViewById(R.id.nationalidphoto);
                    actionbtn.setTag(nationalids.getChildCount());
                    nationalidphoto.setTag(nationalids.getChildCount());
                    EditText nidnumber = nationalidinclude.findViewById(R.id.nidnumber);
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);
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

                    bindniddelete(nationalidinclude.findViewById(R.id.deleteNationalid));
                    bindnationlidcapturephoto(nationalidinclude.findViewById(R.id.idtypespinner), nationalidinclude.findViewById(R.id.nidphotoActionButton));

                }
            }

        } else if (id == R.id.save) {
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

            storeableNationalids = fetchnationalidvalues();
            boolean statusmoduration = dl.updateFarmerNationalids(context, farmerId, storeableNationalids, "2", farmername);
            if (statusmoduration) {
                Toast.makeText(this, getResources().getString(R.string.moderation_note), Toast.LENGTH_LONG).show();
            }
            onBackPressed();
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
                // Request Permission only for Android Version Marshmallow and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Check Camera & Storage Permission
                    if (ContextCompat.checkSelfPermission(AddNationalidActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(AddNationalidActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        //request permission
                        ActivityCompat.requestPermissions(AddNationalidActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_NATIONALID_PIC_PERMISSIONS_REQUEST);

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

    private void bindFarmerNationalIDButton() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        File photoFile = null;

        try {
            photoFile = createImageFile("nationalidimage_" + currentcapture_position);
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
            removehmURIvalue(hm_uri, currentcapture_position);
            removehmvalue(hm_uripath, currentcapture_position);
            hm_uri.put(currentcapture_position, fileUri);
            hm_uripath.put(currentcapture_position, fileUri.getPath());
            setCapturedNIDImageURI(hm_uri);
            setCurrentNIDPhotoPath(hm_uripath);
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, hm_uri.get(currentcapture_position));
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE_NATIONALID);
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
        File storageDir = getExternalFilesDir(null);
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
            if (editmode) {
                dl.removeNIDPhotostatusByType(String.valueOf(nidvalue), farmerId);
            }
            displayBitmapforNID(nationalids, nidvalue, savedImagePath);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case MY_NATIONALID_PIC_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    bindFarmerNationalIDButton();

                } else if (Build.VERSION.SDK_INT >= 23 && (!shouldShowRequestPermissionRationale(permissions[0]) || !shouldShowRequestPermissionRationale(permissions[1]))) {
                    showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));

                } else {
                    Toast.makeText(this, R.string.camera_and_gallery_permission_denial_msg, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    private void ValidateSpinnerTypes() {
        ArrayList<String> nidtype_list = new ArrayList<>();
        ArrayList<Integer> selected_types = new ArrayList<>();
        selectednationalidList = new ArrayList<String>();
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);
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
