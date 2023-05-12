package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.AddFarmerActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;
import com.olam.digital.ofis.mvvmmodule.ImageOrientation;
import com.olam.digital.ofis.mvvmmodule.PathUtil;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ActionPlanProofFragment extends Fragment {

    private static final int REQ_CODE = 300;
    private final String MATOMOTAG = this.getClass().getSimpleName();
    LinearLayout ll_servey_back_actionplan, ll_servey_next_actionplan, ll_complaint;
    ImageView iv_complaint_dropdown, iv_complaint_delete_file;
    TextView tv_complaint_filename;
    RelativeLayout rl_complaint_captureproof_layout, rl_complaint_fileupload_layout;

    //Non-Complaint
    ImageView iv_noncomplaint_dropdown;
    LinearLayout ll_noncomplaint, ll_image_noncomplaint_layout;
    RelativeLayout rl_noncomplaint_captureproof_layout;

    //Satisfactory

    ImageView iv_satisfactory_dropdown;
    LinearLayout ll_satisfactory,ll_image_satisfactory_layout;
    RelativeLayout rl_satisfactory_captureproof_layout;



    public Resources res;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null;
    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;

    private static final int MY_GALLERYPERMISSION_CODE = 100;
    private static final int MY_CAMERA_PERMISSION_CODE = 500;
    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_PICK = 3;
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private String mCurrentPhotoPath;

    private Bitmap resizedBitmapNew;
    LinearLayout image_layout;
    ArrayList<String> image_array = new ArrayList<>();
    private static final String[] ACCEPTED_MIME_TYPES = {"image/jpeg", "image/png", "application/pdf"};

    public ActionPlanProofFragment() {
        // Required empty public constructor
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_action_plan_froof, container, false);
        res = getResources();

        //Complaint
        ll_servey_back_actionplan = view.findViewById(R.id.ll_servey_back_actionplan);
        ll_servey_next_actionplan = view.findViewById(R.id.ll_servey_next_actionplan);
        iv_complaint_dropdown = view.findViewById(R.id.iv_complaint_dropdown);
        ll_complaint = view.findViewById(R.id.ll_complaint);
        rl_complaint_captureproof_layout = view.findViewById(R.id.rl_complaint_captureproof_layout);
        tv_complaint_filename = view.findViewById(R.id.tv_complaint_filename);
        iv_complaint_delete_file = view.findViewById(R.id.iv_complaint_delete_file);
        rl_complaint_fileupload_layout = view.findViewById(R.id.rl_complaint_fileupload_layout);
        image_layout = view.findViewById(R.id.image_layout);
        iv_complaint_dropdown.setTag("0");
        iv_complaint_dropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iv_complaint_dropdown.getTag().equals("0")) {
                    iv_complaint_dropdown.setTag("1");
                    iv_complaint_dropdown.setRotation(180);
                    ll_complaint.setVisibility(View.VISIBLE);
                } else {
                    iv_complaint_dropdown.setTag("0");
                    iv_complaint_dropdown.setRotation(0);
                    ll_complaint.setVisibility(View.GONE);
                }


            }
        });
        rl_complaint_captureproof_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        //Non-Complaint
        iv_noncomplaint_dropdown = view.findViewById(R.id.iv_noncomplaint_dropdown);
        ll_noncomplaint = view.findViewById(R.id.ll_noncomplaint);
        rl_noncomplaint_captureproof_layout = view.findViewById(R.id.rl_noncomplaint_captureproof_layout);

        iv_noncomplaint_dropdown.setTag("0");
        iv_noncomplaint_dropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iv_noncomplaint_dropdown.getTag().equals("0")) {
                    iv_noncomplaint_dropdown.setTag("1");
                    iv_noncomplaint_dropdown.setRotation(180);
                    ll_noncomplaint.setVisibility(View.VISIBLE);
                } else {
                    iv_noncomplaint_dropdown.setTag("0");
                    iv_noncomplaint_dropdown.setRotation(0);
                    ll_noncomplaint.setVisibility(View.GONE);
                }
            }
        });

        //Satisfactory
        iv_satisfactory_dropdown = view.findViewById(R.id.iv_satisfactory_dropdown);
        ll_satisfactory = view.findViewById(R.id.ll_satisfactory);
        rl_satisfactory_captureproof_layout = view.findViewById(R.id.rl_satisfactory_captureproof_layout);

        iv_satisfactory_dropdown.setTag("0");
        iv_satisfactory_dropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iv_satisfactory_dropdown.getTag().equals("0")) {
                    iv_satisfactory_dropdown.setTag("1");
                    iv_satisfactory_dropdown.setRotation(180);
                    ll_satisfactory.setVisibility(View.VISIBLE);
                } else {
                    iv_satisfactory_dropdown.setTag("0");
                    iv_satisfactory_dropdown.setRotation(0);
                    ll_satisfactory.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (isStoragePermissionCameraGranted()) {
                        if (image_array.size() > 5) {
                            Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
                        } else {
                            openCamera();
                        }

                    }

                } else if (options[item].equals("Choose from Gallery")) {

                    if (isStoragePermissionGalleryGranted()) {
                        if (image_array.size() > 5) {
                            Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
                        } else {
                            openGallery();
                        }

                    }
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider",
                                new File(mCurrentPhotoPath)));
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = String.format("JPEG_%s_", ts);
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(filename, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public boolean isStoragePermissionGalleryGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {


                return true;
            } else {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_GALLERYPERMISSION_CODE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }

    public boolean isStoragePermissionCameraGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {


                return true;
            } else {


                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_CAMERA_PERMISSION_CODE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }


    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        //galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES);
        galleryIntent.setType("*/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(PatientReasonActivity.this, R.string.camera_perm_granted, Toast.LENGTH_LONG).show();
                openCamera();
            } else {
                Toast.makeText(getActivity(), "Please give camera permission", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == MY_GALLERYPERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(PatientReasonActivity.this, R.string.cs, Toast.LENGTH_LONG).show();
                openGallery();
            } else {
                Toast.makeText(getActivity(), "Please give gallery permission", Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 2;
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[16 * 1024];

            Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath, options);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 960, 730, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Uri uriImage = Uri.fromFile(new File(mCurrentPhotoPath));
            String filename=mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/")+1);
            try {
                resizedBitmapNew = ImageOrientation.modifyOrientation(getActivity(), resizedBitmap, uriImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            resizedBitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();

            // profile_pic = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            uploadFileImage(filename,imageBytes, resizedBitmapNew);


        }


        // if i want just add photo profile //
        if ((requestCode == GALLERY_PICK && resultCode == RESULT_OK) && data != null) {

            Uri uri = data.getData();
            ContentResolver cr = getActivity().getContentResolver();
            String mime = cr.getType(uri);
            Toast.makeText(getActivity(), "" + mime, Toast.LENGTH_SHORT).show();
            //setImage(iv_profile, imageUri);

            if (mime.equalsIgnoreCase("application/pdf")) {
                String filename=uri.getPath().substring(uri.getPath().lastIndexOf("/")+1);
                uploadFile(uri,filename);
            }
            else {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inSampleSize = 2;
                options.inJustDecodeBounds = false;
                options.inTempStorage = new byte[16 * 1024];

                String filePathimage = null;
                try {
                    filePathimage = PathUtil.getPath(getActivity(), uri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }


                Bitmap bmp = BitmapFactory.decodeFile(filePathimage, options);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                String filename=filePathimage.substring(filePathimage.lastIndexOf("/")+1);

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false);

                try {
                    resizedBitmapNew = ImageOrientation.modifyOrientation(getActivity(), resizedBitmap, uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                resizedBitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                //profile_pic = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                uploadFileImage(filename,imageBytes, resizedBitmapNew);
            }

        }

    }

    private void uploadFileImage(String filename,byte[] filePath, final Bitmap image) {
        //checking if file is available
        ProgressDialog progressDialog;
        try {
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setCancelable(false);
//            progressDialog.setTitle("Please Wait...");
//            progressDialog.show();
            image_array.add(filename);
            showImages();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadFile(Uri uri,String fileName) {
        image_array.add(fileName);
        showImages();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermission() {
        final String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        final List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            // ActivityCompat.requestPermissions(getActivity(), permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CAMERA_PERMISSIONS);
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSIONS);

        }

    }

    public void showImages() {
        image_layout.removeAllViews();

        for (int i = 0; i < image_array.size(); i++) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
            final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

            image_layout.addView(convertView);
            convertView.setTag(i);
            remove.setTag(i);

            selected_imgName.setText(image_array.get(i));

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog(v.getTag().toString());
                }
            });


        }
    }

    private void showDeleteDialog(String pos) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Do you want ro remove this report");
        builder1.setTitle("Alert");
        builder1.setCancelable(false);
        builder1.setPositiveButton(
                "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        image_array.remove(image_array.get(Integer.valueOf(pos)));
                        showImages();
                    }
                });
        builder1.setNegativeButton(
                "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public String getExt(String filePath) {
        int strLength = filePath.lastIndexOf(".");
        if (strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }


}