package com.olam.digital.ofis.farmstakeholder;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.ImageFilePath;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class ViewStakeHolderActivity extends BaseActivity {
    private Cursor farmer;
    private Long farmerId;
    private String stakeHolderId;
    OFISRoomDB db;
    private Resources res;
    private DataLayer dl;

    ImageButton farmerPhotoActionButton;
    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
    private final static int REQUEST_PERMISSIONS_FOR_TIRAMISU = 2509;

    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";
    private List<String> farmerRolesList;
    private List<FarmerRoleDto> farmerRoleDTOList;

    private Spinner farmerRoleSpinner;
    private String localeSuffixColumn;

    private int previousRoleSelectedPosition = 0;
    private final boolean[] isSpinnerClicked = {false};

    private MultiStakeHolder stakeHolder;

    private int countryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stake_holder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        farmerId = getIntent().getExtras().getLong(Utilities.FARMER_ID);
        stakeHolderId = getIntent().getExtras().getString(Utilities.STAKE_HOLDER_ID);
        db = OFISRoomDB.getDbInstance(context);
        res = getResources();
        dl = new DataLayer(res, context);

        localeSuffixColumn = Utilities.getColumnNameByLanguage();
        stakeHolder = db.multiStakeHolderDao().getStakeHolderById(Long.parseLong(stakeHolderId));
        setupUI();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bundle bundle = new Bundle();
        bundle.putLong(Utilities.FARMER_ID, farmerId);
        startActivity(new Intent(ViewStakeHolderActivity.this, AddFarmStakeHolderActivity.class).putExtras(bundle));
    }

    private void setupUI() {

        TextView tvStakeHolderName = findViewById(R.id.stakeHolderName);
        TextView farmerOlamId = findViewById(R.id.viewFarmerOlamFarmerID);
        farmerRoleSpinner = findViewById(R.id.farmerRoleSpinner);
        ImageView farmerPhoto = findViewById(R.id.viewFarmerFarmerPhoto);
        TextView viewFarmerInfo = findViewById(R.id.viewFarmerInfo);
        LinearLayout llViewInfo = findViewById(R.id.llViewInfo);
        TextView genderInfo = findViewById(R.id.genderInfo);
        TextView ageInfo = findViewById(R.id.ageInfo);
        TextView pNumberInfo = findViewById(R.id.pNumberInfo);
        TextView partnerInfo = findViewById(R.id.partnerInfo);
        TextView programInfo = findViewById(R.id.programInfo);
        TextView regionInfo = findViewById(R.id.regionInfo);
        TextView districtInfo = findViewById(R.id.districtInfo);
        TextView subDistrictInfo = findViewById(R.id.subDistrictInfo);
        TextView placeInfo = findViewById(R.id.placeInfo);
        TextView fgInfo = findViewById(R.id.fgInfo);
        TextView sectionInfo = findViewById(R.id.sectionInfo);
        TextView nationalIdInfo = findViewById(R.id.nationalIdInfo);

        farmer = dl.getFarmerById(farmerId);

        if (farmer != null && farmer.getCount() > 0) {
            farmer.moveToFirst();
            countryId = Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID)));

            tvStakeHolderName.setText(stakeHolder.msName);
            farmerOlamId.setText(stakeHolder.olamMultiStakeholderId);
            long farmerPlaceId = farmer.getLong(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID));

            if (farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY)) == 1) {
                String farmerPhotoPath = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
                if (farmerPhotoPath != null && !farmerPhotoPath.isEmpty()) {
                    // set our desired width and height for display
                    int baseWidth = 100;
                    int baseHeight = 133;

                    Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                    Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                    Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

                    Bitmap existingBitmap = Utilities.loadSizedBitmap(farmerPhotoPath, outputWidth, outputHeight);

                    farmerPhoto.setImageBitmap(existingBitmap);
                }
            }

            //setting farmer info
            Cursor existingNumbers = dl.getFarmerPhoneNumbers(stakeHolder.msId, false, OFISDBContract.PhoneNumber.COLUMN_NAME_MULTI_STAKEHOLDER_ID);
            StringBuilder phoneNumberInfo = new StringBuilder();
            String ptInfo = null , prInfo=null;

            if (existingNumbers.getCount() > 0) {
                while (existingNumbers.moveToNext()) {
                    if(!phoneNumberInfo.toString().equals("")){
                        phoneNumberInfo.append("\n");
                    }
                    phoneNumberInfo.append(existingNumbers.getString(existingNumbers.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE))).append(" ").append(existingNumbers.getString(existingNumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER)));
                }
            }

            genderInfo.setText("- " + stakeHolder.msGender);
            ageInfo.setText("- " + stakeHolder.age);
            pNumberInfo.setText("- " + phoneNumberInfo);

            if(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID))!=null) {
                ptInfo = db.partnersDao().getPartnerNameById(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID))));
            }

            if(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID))!=null){
                prInfo = db.programmeDao().getProgrammeNameById(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID))));
            }

            partnerInfo.setText("- " + ptInfo);
            programInfo.setText("- " + prInfo);
            regionInfo.setText("- " + dl.getRegionName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID)))));
            districtInfo.setText("- " + dl.getDistrictName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID)))));
            subDistrictInfo.setText("- " + dl.getSubDistrictName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISTRICTID)))));
            placeInfo.setText("- " + dl.getPlaceName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)))));
            fgInfo.setText("- " + dl.getFGName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID)))));
            sectionInfo.setText("- " + dl.getSectionName(Integer.parseInt(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID)))));


            Cursor nids = dl.getnationalidsbyfarmerids(Long.parseLong(stakeHolderId), OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID);
            try {
                int i = 0;
                StringBuilder nationalID = new StringBuilder();
                while (nids.moveToNext()) {
                    i++;
                    nationalID.append(nids.getString(nids.getColumnIndex(OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER))).append(", ");
                }

                if (nationalID.length() > 0) {
                    nationalIdInfo.setText("- " + nationalID.delete(nationalID.length() - 2, nationalID.length()).toString());
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        farmerPhotoActionButton = findViewById(R.id.farmerPhotoActionButton);
        farmerPhotoActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ViewStakeHolderActivity.this,
                                new String[]{
                                        android.Manifest.permission.CAMERA,
                                        android.Manifest.permission.READ_PHONE_STATE,
                                        android.Manifest.permission.READ_MEDIA_AUDIO,
                                        android.Manifest.permission.READ_MEDIA_VIDEO,
                                        android.Manifest.permission.READ_MEDIA_IMAGES,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.RECORD_AUDIO,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION},

                                REQUEST_PERMISSIONS_FOR_TIRAMISU);
                    }else {
                        bindFarmerPhotoButton();
                    }
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(ViewStakeHolderActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                            .checkSelfPermission(ViewStakeHolderActivity.this,
                                    android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(ViewStakeHolderActivity.this,
                                new String[]{android.Manifest.permission.CAMERA,
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PROFILE_PIC_PERMISSIONS_REQUEST);

                    } else {
                        bindFarmerPhotoButton();
                    }
                } else {
                    bindFarmerPhotoButton();
                }
            }
        });

        viewFarmerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llViewInfo.setVisibility((llViewInfo.getVisibility()==View.GONE) ? View.VISIBLE : View.GONE);
                viewFarmerInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0,(llViewInfo.getVisibility()==View.GONE) ? R.drawable.ic_drop_down_collapse : R.drawable.ic_task_drop_down_expand, 0);
            }
        });

        farmerRolesList = new ArrayList<>();
        farmerRoleDTOList = new ArrayList<>();

        farmerRoleDTOList = dl.getFarmerRoles(localeSuffixColumn);
        farmerRolesList.add(res.getString(R.string.farmer_roles));
        for (FarmerRoleDto farmerRoleDTO : farmerRoleDTOList){
            farmerRolesList.add(farmerRoleDTO.getName());
        }
        bindFarmerRoles();
    }

    private void bindFarmerPhotoButton() {
        final CharSequence[] items = {res.getString(R.string.Camera), res.getString(R.string.Gallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(res.getString(R.string.Select_picture_from));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(context, items[item], Toast.LENGTH_SHORT).show();

                if (items[item].equals(res.getString(R.string.Camera))) {
                    // New
                    File photoFile = null;
                    try {
                        photoFile = createImageFile("portrait");
                    } catch (IOException e) {
                        FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating photo image file", e));
                        e.printStackTrace();
                    }

                    if (photoFile != null) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if ((intent.resolveActivity(getPackageManager()) != null)) {
                            Uri photoURI = FileProvider.getUriForFile(ViewStakeHolderActivity.this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
                            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            for (ResolveInfo resolveInfo : resInfoList) {
                                String packageName = resolveInfo.activityInfo.packageName;
                                grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }

                            setCapturedImageURI(photoURI);
                            setCurrentPhotoPath(photoFile.getPath());
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, getCapturedImageURI());
                            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                        } else {
                            Toast.makeText(ViewStakeHolderActivity.this, "Gallery is not available in Mobile", Toast.LENGTH_LONG).show();
                        }
                    }

                } else if (items[item].equals(res.getString(R.string.Gallery))) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_IMAGE_GALLERY);

                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private File createImageFile(String portraitOrSignature) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = (portraitOrSignature.equalsIgnoreCase("portrait")) ? "ofis_farmer_portrait_" + timestamp + "_" : "ofis_farmer_signature_" + timestamp + "_";
        File storageDir = getExternalFilesDir("");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        if (portraitOrSignature.equalsIgnoreCase("portrait")) {
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        }
        return image;
    }

    private void setCapturedImageURI(Uri mCapturedImageURI) {
        this.mCapturedImageURI = mCapturedImageURI;
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

    private void bindFarmerRoles() {
        String[] farmerRoles = farmerRolesList.toArray(new String[farmerRolesList.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewStakeHolderActivity.this, R.layout.spinner_item, farmerRoles);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        farmerRoleSpinner.setAdapter(adapter);
        previousRoleSelectedPosition = getIndexForFarmerRoleSpinnerList(stakeHolder.msRoleId);
        farmerRoleSpinner.setSelection(previousRoleSelectedPosition);

        farmerRoleSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerClicked[0] = true;
                return false;
            }
        });

        farmerRoleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0){
                    farmerRoleSpinner.setTag("null");
                }
                else {
                    if (isSpinnerClicked[0]) {
                        showConfirmationDialog(farmerRoleDTOList.get(position - 1), position);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showConfirmationDialog(FarmerRoleDto farmerRoleDto, int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.confirmation_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        Button btnYes = dialogView.findViewById(R.id.button_yes);
        Button btnNo = dialogView.findViewById(R.id.button_no);
        AlertDialog dialog = builder.create();

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousRoleSelectedPosition = position;
                farmerRoleSpinner.setTag(farmerRoleDto.getId());
                saveChanges(farmerRoleDto);
                dialog.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSpinnerClicked[0] = false;
                farmerRoleSpinner.setSelection(previousRoleSelectedPosition);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveChanges(FarmerRoleDto farmerRoleDto) {
        if (stakeHolder.synced == 0) {
            db.multiStakeHolderDao().updateMsRoleId(String.valueOf(farmerRoleDto.getId()), stakeHolder.msId);
        }
        else {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.RESULT_CANCELED) return;
        ImageView farmerPhoto = (ImageView) findViewById(R.id.viewFarmerFarmerPhoto);
        switch (requestCode) {

            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    if (getCurrentPhotoPath() != null) {
                        setFullImageFromFilePath(getCurrentPhotoPath(), farmerPhoto, "portrait");
                    } else {
                        Toast.makeText(ViewStakeHolderActivity.this, "Something went wrong, please try again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_IMAGE_GALLERY:
                if (resultCode == RESULT_OK) {
                    String realPath = ImageFilePath.getPath(ViewStakeHolderActivity.this, data.getData());

                    try {
                        if (realPath != null) {
                            setFullImageFromFilePath(realPath, farmerPhoto, "portrait");
                        } else {
                            Toast.makeText(ViewStakeHolderActivity.this, "Something went wrong, please try again", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
        }
    }

    private void setFullImageFromFilePath(String imagePath, ImageView target, String portraitOrSignature) {

        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, portraitOrSignature);
        if (savedImagePath != null) {
            if (portraitOrSignature.equalsIgnoreCase("portrait")) {
                Uri uri = Uri.parse(savedImagePath);
                setCapturedImageURI(uri);
                setCurrentPhotoPath(savedImagePath);
            }

            displayBitmap(savedImagePath, target);
        }
    }

    private void displayBitmap(String savedImagePath, ImageView target) {
        int baseWidth = 100;
        int baseHeight = 133;
        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());
        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);
        target.setImageBitmap(bitmap);
        //dl.updateFarmerPhoto(farmerId, savedImagePath, olamFarmerIdStr, farmerNameStr);

//        // Collect the GPS Point
//        if (mCurrentCapturedLocation != null) {
//            // check the current location
//            collectGPSPoint(GPSConstants.MODULE_CAPTURE_UPDATE_PHOTO, mCurrentCapturedLocation, farmerId);
//        } else if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
//            gpsTracker.getDeviceLocation();
//            return;
//        } else {
//            //
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != android.R.id.home) {
            return super.onOptionsItemSelected(item);
        }

        onBackPressed();
        return true;
    }
}