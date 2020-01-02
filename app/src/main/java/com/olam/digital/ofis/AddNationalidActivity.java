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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

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
    //    int count_plus = 0;
    private final static int MY_NATIONALID_PIC_PERMISSIONS_REQUEST = 9997;

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
    ArrayAdapter<String> spinnerArrayAdapter;
    AppCompatButton save;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_nationalid);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        dl = new DataLayer(res, context);
        res = getResources();

        initComponents();
        try {
            farmerId=getIntent().getExtras().getLong(ChildLabourUtils.CHILD_FARMER_ID);
        }catch (Exception e){}

        bindnationalid();
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

        LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
        nationalids.addView(nationalidinclude);
        LinearLayout niddelete = (LinearLayout) nationalidinclude.findViewById(R.id.deleteNationalid);
        niddelete.setVisibility(View.GONE);
        Spinner spinner_nidtype = (Spinner) nationalidinclude.findViewById(R.id.idtypespinner);
        LinearLayout ll_capturephoto = (LinearLayout) nationalidinclude.findViewById(R.id.ll_capturephoto);
        spinnerArrayAdapter = new ArrayAdapter<String>
                (AddNationalidActivity.this, R.layout.nidtype_spinner,
                        setNationalidtype());
        spinnerArrayAdapter.setDropDownViewResource(R.layout
                .nidtype_spinner);
        spinner_nidtype.setAdapter(spinnerArrayAdapter);
        count_nidimg ++;
        ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
        actionbtn.setTag(count_nidimg);
        String selected_item = spinner_nidtype.getSelectedItem().toString();
        spinner_nidtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectednationalidList.add(spinner_nidtype.getSelectedItem().toString());
               /* if(getNidCaptureStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")){
                    ll_capturephoto.setVisibility(View.GONE);
                }else{
                    ll_capturephoto.setVisibility(View.VISIBLE);
                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        bindnationlidcapturephoto((ImageButton) nationalidinclude.findViewById(R.id.nationalidphoto),
                (ImageButton) nationalidinclude.findViewById(R.id.nidphotoActionButton));

        ImageButton addNationId = (ImageButton) findViewById(R.id.addnid);
        addNationId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                count_plus ++;
//                addNationId.setTag(count_plus);
//                currentcapture_position = count_nidimg;
                count_nidimg++;
                ArrayList<String> refreshitem = refreshNationalidtype(setNationalidtype());
                if (refreshitem.size() != 0) {
                    LinearLayout nationalidinclude = (LinearLayout) inflater.inflate(R.layout.add_nationalid, nationalids, false);
                    LinearLayout ll_capturephoto = (LinearLayout) nationalidinclude.findViewById(R.id.ll_capturephoto);
                    Spinner spinner_nidtype = (Spinner) nationalidinclude.findViewById(R.id.idtypespinner);
                    ImageButton actionbtn = nationalidinclude.findViewById(R.id.nidphotoActionButton);
                    actionbtn.setTag(count_nidimg);
                    spinnerArrayAdapter = new ArrayAdapter<String>(AddNationalidActivity.this, R.layout.nidtype_spinner, refreshitem);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.nidtype_spinner);
                    spinner_nidtype.setAdapter(spinnerArrayAdapter);
                    String selected_item = spinner_nidtype.getSelectedItem().toString();
                    spinner_nidtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectednationalidList.add(spinner_nidtype.getSelectedItem().toString());
                            /*if(getNidCaptureStatus(spinner_nidtype.getSelectedItem().toString()).trim().equalsIgnoreCase("false")){
                                ll_capturephoto.setVisibility(View.GONE);
                            }else{
                                ll_capturephoto.setVisibility(View.VISIBLE);
                            }*/
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
                    nationalids.addView(nationalidinclude);
                    bindniddelete((LinearLayout) nationalidinclude.findViewById(R.id.deleteNationalid));
                    bindnationlidcapturephoto((ImageButton) nationalidinclude.findViewById(R.id.nationalidphoto), (ImageButton) nationalidinclude.findViewById(R.id.nidphotoActionButton));
                }
            }
        });
    }

    private void bindniddelete(LinearLayout trashbutton) {
        trashbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout numberrow = (LinearLayout) v.getParent();
                ((ViewGroup) numberrow.getParent()).removeView(numberrow);
            }
        });
    }

    private void initComponents() {
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        nationalids = (LinearLayout) findViewById(R.id.nationalids);
        addNationId = (ImageButton) findViewById(R.id.addnid);
        save = (AppCompatButton) findViewById(R.id.save);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save) {
            addNIDValues();

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

    private void addNIDValues() {

        if (! duplicateNationalidsValidation()) { //Duplicate nationalids Validation....
            Toast.makeText(context, res.getString(R.string.duplicate_nationalids), Toast.LENGTH_LONG).show();
            return;
        }



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
                storeableNids.put("nationalidtype", type);
                storeableNids.put("nationalidNumber", nid);
                storeableNids.put("photo", hm_uripath.get(j+1));
                storeableNationalids.put(storeableNids);

                dl.updateFarmerNationalids(context, farmerId, storeableNationalids, "2");
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating storable phone number", e));
                e.printStackTrace();
            }

        }

        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }



    private ArrayList<String> setNationalidtype(){
        nidtype = new ArrayList<String>();
        Cursor cursor = dl.getnationalids();
        if (cursor.moveToFirst()) {
            do {
                nidtype.add(cursor.getString(cursor.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE)));
            } while (cursor.moveToNext());
        }
        cursor.close();
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

    private void bindnationlidcapturephoto(ImageButton imagebutton, ImageButton actionbutton) {
//        count_plus ++;
        actionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                actionbutton.setTag(count_nidimg);
                currentcapture_position = count_nidimg;
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

    private void setNIDImageFromFilePath(String imagePath, String portraitOrSignature) {
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, portraitOrSignature);
        if (savedImagePath != null) {
            Uri uri = Uri.parse(savedImagePath);
            setCapturedImageURI(uri);
            setCurrentPhotoPath(savedImagePath);


            displayBitmapforNID(savedImagePath);
        }
    }

    private void displayBitmapforNID(String savedImagePath) {
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
            ImageButton imageButton = (ImageButton) nidrow.findViewById(R.id.nationalidphoto);
            if(j == currentcapture_position-1) {
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

}
