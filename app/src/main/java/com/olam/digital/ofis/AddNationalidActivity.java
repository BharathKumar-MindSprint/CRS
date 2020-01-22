package com.olam.digital.ofis;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.ViewChildLabourSurveyActivity;
import com.olam.digital.ofis.childlabour.adapter.ChildLabourRecyclerAdapter;
import com.olam.digital.ofis.childlabour.businesslogic.ChildLabourLogic;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemChildClickListener;
import com.olam.digital.ofis.model.NationalIdDTO;
import com.olam.digital.ofis.model.NationalIdTypeDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddNationalidActivity extends BaseActivity implements  View.OnClickListener{

    private Context context;
    Long farmerId;
    private Resources res;
    ImageButton addNationId;
    private JSONArray storeableNationalids = new JSONArray();
    private LayoutInflater inflater;
    private DataLayer dl;
    ArrayList<String> nationalidList;
    ArrayList<String> selectednationalidList = new ArrayList<String> ();;
    ArrayList<String> nidtype;
    String farmername;
    boolean editnidmode = false;
    private int nidvalue;
    private final static String LIMIT_XED = "limit";
    private final static String DUPLICATE = "duplicate";
    private final static String ENTER_NID = "novalues";
    //    int count_plus = 0;
    private final static int MY_NATIONALID_PIC_PERMISSIONS_REQUEST = 9997;
    ArrayList<NationalIdTypeDTO> nidtypelist;
    private String mCurrentPhotoPath = null, mCurrentSignaturePhotoPath = null;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;
    private final static int REQUEST_IMAGE_CAPTURE_SIGNATURE = 3;
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
    int count_nidimg = 0;
    int currentcapture_position;
    HashMap<Integer, Uri> hm_uri = new HashMap<>();
    HashMap<Integer, String> hm_uripath = new HashMap<>();
    private LinearLayout nationalids;
    Spinner spinner_nidtype;
    private final static String SELECT_NATIONALID = "ID TYPE";
    ArrayAdapter<String> spinnerArrayAdapter;
    AppCompatButton save;
    boolean editmode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_add_nationalid);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        dl = new DataLayer(res, context);
        res = getResources();

        initComponents();
        try {
            farmerId=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
            editmode=getIntent().getExtras().getBoolean("edit_mode");
            farmername = getIntent().getExtras().getString("FARMER_NAME");
        }catch (Exception e){}
        dl.clearlocalniddbvalue();
        if(!editmode) {
            bindnationalid();
        }else{
            bindNationalidValues();
        }
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

    private void bindnationalid() {
//        if (! editMode) {
        ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
        LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
        nationalids.addView(nationalidinclude);

        LinearLayout niddelete = (LinearLayout) nationalidinclude.findViewById(R.id.deleteNationalid);
        niddelete.setVisibility(View.GONE);
        spinner_nidtype = (Spinner) nationalidinclude.findViewById(R.id.idtypespinner);
        LinearLayout ll_capturephoto = (LinearLayout) nationalidinclude.findViewById(R.id.ll_capturephoto);
        spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
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
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
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
        if(farmerId != 0) {
            Cursor nids = dl.getnationalidsbyfarmerid(farmerId);
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
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.nidtype_spinner, refreshitem);
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
                                spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
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

    private void bindniddelete(LinearLayout trashbutton) {
        trashbutton.setTag(nationalids.getChildCount());
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
                ValidateNIDValues();
            }
        });
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
                if(! editmode) {
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
                    if(! editmode) {
                        photopath = getPhotobytype(type);
                    }else{
                        photopath = getNIDPhotoByType(String.valueOf(type),farmerId);
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

    // getting nationalids type id values
    public boolean getPhotostatusByType(String type) {
        boolean photostatus= false;
        Cursor  nid=dl.getPhotostatusByType(type);
        if (nid.getCount() > 0) {
            photostatus= true;
        }
        return  photostatus;
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


    private void initComponents() {
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        nationalids = (LinearLayout) findViewById(R.id.nationalids);
        addNationId = (ImageButton) findViewById(R.id.addnid);
        save = (AppCompatButton) findViewById(R.id.save);
        save.setOnClickListener(this);
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


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.addnid) {
            if (!ValidateNIDValues()) return;
            try {
                if (! validateSpinnerValues(spinner_nidtype.getSelectedItem().toString())) {
                    Toast.makeText(AddNationalidActivity.this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_LONG).show();
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
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);
                    spinner_nidtype.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                                spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.spinner_item, refreshitem);
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
        }else if(id == R.id.save){
            if (!ValidateNIDValues()) return;
            try {
                if (! validateSpinnerValues(spinner_nidtype.getSelectedItem().toString())) {
                    Toast.makeText(AddNationalidActivity.this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_LONG).show();
                    return;
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            if(getnationalidSubmitStatus().equalsIgnoreCase(DUPLICATE)) {
                Toast.makeText(this,getResources().getString(R.string.duplicate_nationalids),Toast.LENGTH_SHORT).show();
                return ;
            }else if(getnationalidSubmitStatus().equalsIgnoreCase(LIMIT_XED)){
                Toast.makeText(this,getResources().getString(R.string.duplicate_nationalids),Toast.LENGTH_SHORT).show();
                return ;
            }else if(getnationalidSubmitStatus().equalsIgnoreCase(ENTER_NID)){
                Toast.makeText(this,getResources().getString(R.string.enter_nationalids),Toast.LENGTH_SHORT).show();
                return ;
            }

            storeableNationalids = fetchnationalidvalues();
            dl.updateFarmerNationalids(context, farmerId, storeableNationalids,"2",farmername);
            onBackPressed();
//            addNIDValues();
        }



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


    private boolean validateSpinnerValues(String selected_item) {
        boolean status = true;
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

    private boolean validateSpinnerSelection() {
        boolean status = true;
        ArrayList<String> values = refreshNationalidtype(setNationalidtype());
        if(values.size() == 1){
            return false;
        }
        return status;
    }


   /* private void addNIDValues() {

        //        fetch national id values
        int nidrowscount = nationalids.getChildCount();
        for (int j = 0; j < nidrowscount; j++) {
            LinearLayout nidrow = (LinearLayout) nationalids.getChildAt(j);
            EditText nidnumber = (EditText) nidrow.findViewById(R.id.nidnumber);
            Spinner nid_type = (Spinner) nidrow.findViewById(R.id.idtypespinner);

            String value = nid_type.getSelectedItem().toString();
            int type = getNidType(value);
            String nid = nidnumber.getText().toString();
            JSONObject storeableNids = new JSONObject();
            try {
                storeableNids.put("nationalIdType", type);
                storeableNids.put("nationalIdNumber", nid);
                storeableNids.put("photo", hm_uripath.get(j+1));
                storeableNationalids.put(storeableNids);

                dl.updateFarmerNationalids(context, farmerId, storeableNationalids,"2",farmername);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating storable phone number", e));
                e.printStackTrace();
            }

        }

        onBackPressed();
    }*/



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

    private void bindnationlidcapturephoto(Spinner spinner, ImageButton actionbutton) {

        actionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = spinner.getSelectedItem().toString();
                if (value.equalsIgnoreCase(SELECT_NATIONALID)) {
                    Toast.makeText(AddNationalidActivity.this,getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_SHORT).show();
                    return;
                }
                nidvalue = getNidType(value);
//                showtoast((int) actionbutton.getTag());
//                currentcapture_position = nationalids.getChildCount();
                currentcapture_position = (int) actionbutton.getTag();
                // Request Permission only for Android Version Marshmallow and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Check Camera & Storage Permission
                    if (ContextCompat.checkSelfPermission(AddNationalidActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                            .checkSelfPermission(AddNationalidActivity.this,
                                    android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        //request permission
                        ActivityCompat.requestPermissions(AddNationalidActivity.this,
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(resultCode == Activity.RESULT_CANCELED) return;
        ImageButton farmerPhoto = (ImageButton) findViewById(R.id.farmerPhoto);
        ImageButton farmerSignature = (ImageButton) findViewById(R.id.farmerSignature);

        switch (requestCode) {

            case REQUEST_IMAGE_CAPTURE_NATIONALID:
                if (resultCode == RESULT_OK) {
                    setNIDImageFromFilePath(hm_uripath.get(currentcapture_position), "signature");
                } else {
//                        farmerSignature.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.empty_signature));
//                        setCurrentSignaturePhotoPath(null);
                }
                break;
            default:
        }
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

    private void setNIDImageFromFilePath(String imagePath, String portraitOrSignature) {
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, portraitOrSignature);
        if (savedImagePath != null) {
            Uri uri = Uri.parse(savedImagePath);
            setCapturedImageURI(uri);
            setCurrentPhotoPath(savedImagePath);

            dl.updateNIDPhoto(nidvalue, savedImagePath);
            displayBitmapforNID(nidvalue, savedImagePath);
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

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
                        Toast.makeText(AddNationalidActivity.this, R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
                    }
                });
        dialog.show();
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
}
