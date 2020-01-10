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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.olam.digital.ofis.tasks.NonScrollListView;

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
import java.util.Map;
import java.util.Set;

public class AddNationalidActivity extends BaseActivity implements  View.OnClickListener{

    private Context context;
    Long farmerId;
    private Resources res;
    ImageButton addNationId;
    private final JSONArray storeableNationalids = new JSONArray();
    private LayoutInflater inflater;
    private DataLayer dl;
    ArrayList<String> nationalidList;
    ArrayList<String> selectednationalidList = new ArrayList<String> ();;
    ArrayList<String> nidtype;
    private final static int MY_NATIONALID_PIC_PERMISSIONS_REQUEST = 9997;
    ArrayList<NationalIdTypeDTO> nidtypelist;
    private String mCurrentPhotoPath = null, mCurrentSignaturePhotoPath = null, mCurrentNationalIdPhotoPath = null;;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null, mCapturedNationalIdImageURI = null;
    private final static String CAPTURED_NATIONALID_PHOTO_URI_KEY = "mCapturedNationalIdImageURI";
    private final static String CAPTURED_NATIONALID_PHOTO_PATH_KEY = "mCapturedNationalIdImageURI";
    private final static int REQUEST_IMAGE_CAPTURE_NATIONALID = 4;
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";
    private final static String CAPTURED_SIGNATURE_PHOTO_PATH_KEY = "mCurrentSignaturePhotoPath";
    private final static String CAPTURED_SIGNATURE_PHOTO_URI_KEY = "mCapturedSignatureImageURI";

    int currentcapture_position;
    HashMap<Integer, Uri> hm_uri = new HashMap<>();
    HashMap<Integer, String> hm_uripath = new HashMap<>();
    private LinearLayout nationalids;
    Spinner spinner_nidtype;
    private final static String SELECT_NATIONALID = "ID TYPE";
    ArrayAdapter<String> spinnerArrayAdapter;
    AppCompatButton save;
    boolean editmode;

    // National ID releated variable
    ListView nationalidslist;
    Map<Integer, Integer> typeMap = new HashMap<Integer, Integer>();
    Map<Integer, String> nidvalueMap = new HashMap<Integer, String>();
    ArrayList<NationalIdDTO> nidDtoList = new ArrayList<NationalIdDTO> ();;
    ImageButton showNIDImage;
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
    String farmername;

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
        refreshNationalidtype(setNationalidtype());

        bindnationaiddetails();
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

    private void bindnationaiddetails() {
//        if(editmode){
//            bindnationaidvaluesfromdb();
//        }
        if(nidDtoList.size() == 0) {
            addingEmptyValue(REQUEST_NEW_ITEM);
        }
        adapterNationalId = new CustomListAdapter_NationalId(this, res, nidtypelist, nidDtoList, refreshNationalidtype(setNationalidtype()),typeMap, nidvalueMap, 2);
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
        Cursor nids = dl.getnationalidsbyfarmerid(farmerId);

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

    private ArrayList<String> refreshNationalidtype(ArrayList<String> value){
        for(int i=0; i < selectednationalidList.size(); i++){
            value.remove(selectednationalidList.get(i));
        }

        return value;
    }

    public void addTypeintoSelectionList(String value){
        selectednationalidList.add(value);

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
        if (mCurrentNationalIdPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_NATIONALID_PHOTO_PATH_KEY, mCurrentNationalIdPhotoPath);
        }
        if (mCapturedNationalIdImageURI != null) {
            savedInstanceState.putString(CAPTURED_NATIONALID_PHOTO_URI_KEY, mCapturedNationalIdImageURI.toString());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        if (savedInstanceState.containsKey(CAPTURED_NATIONALID_PHOTO_PATH_KEY)) {
            mCurrentNationalIdPhotoPath = savedInstanceState.getString(CAPTURED_NATIONALID_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_NATIONALID_PHOTO_URI_KEY)) {
            mCapturedNationalIdImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_NATIONALID_PHOTO_URI_KEY));
        }
        if (mCurrentNationalIdPhotoPath != null) {
            displayBitmap(mCurrentNationalIdPhotoPath, "NationalId", showNIDImage);
        }

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

    private Uri getCapturedNationalIdImageURI() {
        return mCapturedNationalIdImageURI;
    }

    private void setCapturedNationalIdImageURI(Uri mCapturedNationalIdImageURI) {
        this.mCapturedNationalIdImageURI = mCapturedNationalIdImageURI;
    }

    private String getCurrentNationalIdPhotoPath() {
        return mCurrentNationalIdPhotoPath;
    }

    private void setCurrentNationalIdPhotoPath(String mCurrentNationalIdPhotoPath) {
        this.mCurrentNationalIdPhotoPath = mCurrentNationalIdPhotoPath;
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





    private void removeSpinnerItem(LinearLayout numberrow) {
        Spinner spinner = (Spinner) numberrow.findViewById(R.id.idtypespinner);
        String val = spinner.getSelectedItem().toString();
        for(int i=0; i < selectednationalidList.size(); i++){
            if(val.equalsIgnoreCase(selectednationalidList.get(i))) {
                selectednationalidList.remove(i);
            }
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


    private void initComponents() {
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        nationalidslist = (ListView) findViewById(R.id.list_nationalids);
        addNationId = (ImageButton) findViewById(R.id.addnid);
        save = (AppCompatButton) findViewById(R.id.save);
        save.setOnClickListener(this);
        addNationId.setOnClickListener(this);
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
        if (v.getId() == R.id.save) {

            boolean status = validateValues();
            if(status){
                dl.updateFarmerNationalids(context, farmerId, fetchListValues(),"2",farmername);
                Toast.makeText(this, getResources().getString(R.string.nid_updated_successfully),Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }

        if(v.getId() == R.id.addnid) {
            if(! getSubmitStatus()) {
                addingEmptyValue(REQUEST_ADDING_ITEM);
            }else{
                Toast.makeText(this,getResources().getString(R.string.duplicate_nationalids),Toast.LENGTH_SHORT).show();
            }
        }
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
        Toast.makeText(this,"count:" + storeableNationalids.length(),Toast.LENGTH_SHORT).show();
        return storeableNationalids;
    }

    private boolean validateValues() {
        if(getnationalidSubmitStatus().equalsIgnoreCase(DUPLICATE)) {
            Toast.makeText(this,getResources().getString(R.string.duplicate_nationalids),Toast.LENGTH_SHORT).show();
            return false;
        }else if(getnationalidSubmitStatus().equalsIgnoreCase(LIMIT_XED)){
            Toast.makeText(this,getResources().getString(R.string.duplicate_nationalids),Toast.LENGTH_SHORT).show();
            return false;
        }else if(getnationalidSubmitStatus().equalsIgnoreCase(ENTER_NID)){
            Toast.makeText(this,getResources().getString(R.string.enter_nationalids),Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

    public void captureSetImage(int position, NationalIdDTO idDTO, int type, String value) {
        capture__position = position;
        nidDTO = idDTO;
        typeMap.put(position, type);
        nidvalueMap.put(position, value);
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

}
