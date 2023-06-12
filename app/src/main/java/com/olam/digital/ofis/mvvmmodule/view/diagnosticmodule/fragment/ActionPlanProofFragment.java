package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.room.Query;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.itextpdf.io.codec.Base64;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfReader;
//import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.Line;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.FileUtils;
import com.olam.digital.ofis.mvvmmodule.ImageOrientation;
import com.olam.digital.ofis.mvvmmodule.InputFilterMinMax;
import com.olam.digital.ofis.mvvmmodule.PDFUtils;
import com.olam.digital.ofis.mvvmmodule.PathUtil;
import com.olam.digital.ofis.mvvmmodule.roomdb.DmActionPlanCurrenciesTableDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.DmSurveyAnswerTableDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlanCurrenciesTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmNonComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSatisfactoryFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ActionPlanProofFragment extends Fragment {

    private static final int REQ_CODE = 300;
    private final String MATOMOTAG = this.getClass().getSimpleName();
    LinearLayout ll_servey_back_actionplan, ll_servey_next_actionplan;
    ImageView iv_complaint_delete_file;
    TextView tv_complaint_filename;
    RelativeLayout rl_complaint_captureproof_layout, rl_complaint_fileupload_layout;
    ConstraintLayout complaint_custom_actionplanlayout;
    ImageView iv_delete_custom_complaint, iv_save_custom_complaint;
    EditText complaiant_custom_text;

    //Satisfactory


    public Resources res;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null;
    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;

    private static final int MY_GALLERYPERMISSION_CODE = 100;
    private static final int MY_CAMERA_PERMISSION_CODE = 500;
    private static int CAMERA_REQUEST = 0;
    private static int GALLERY_PICK = 0;
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private String mCurrentPhotoPath;

    private Bitmap resizedBitmapNew;
    LinearLayout image_layout;

    LinearLayout dm_question_complaint;

    HashMap<String, ArrayList<String>> dynamicArrays = new HashMap<>();

    String dynamicArrayName;

    ArrayList<String> dynamicArray = new ArrayList<>();

    HashMap<String, ArrayList<Long>> dynamicArraysFile = new HashMap<>();

    String dynamicArrayNameFile;
    ArrayList<Long> dynamicArrayFile = new ArrayList<>();

    private static final String[] ACCEPTED_MIME_TYPES = {"image/jpeg", "image/png", "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
            "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/csv"};

    //Room DB
    OFISRoomDB db;
    Spinner spinner_dmCurrencies;
    LinearLayout linearLayoutAll;

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
        dm_question_complaint = view.findViewById(R.id.dm_question_complaint);
        ll_servey_next_actionplan = view.findViewById(R.id.ll_servey_next_actionplan);
        rl_complaint_captureproof_layout = view.findViewById(R.id.rl_complaint_captureproof_layout);
        tv_complaint_filename = view.findViewById(R.id.tv_complaint_filename);
        iv_complaint_delete_file = view.findViewById(R.id.iv_complaint_delete_file);
        rl_complaint_fileupload_layout = view.findViewById(R.id.rl_complaint_fileupload_layout);
        spinner_dmCurrencies = view.findViewById(R.id.spinner_dmCurrencies);
        //
        linearLayoutAll = new LinearLayout(getActivity());

        db = OFISRoomDB.getDbInstance(getActivity());
        List<DmCurrenciesItem> dmSectionsItemList = db.dmCurrenciesItemDao().getDmCurrenciesItem();
        showCurrencyDropDown(dmSectionsItemList);

        if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
            for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
            }
        }


        return view;
    }


    private void displayQuestionsModuleComplaint(String complaint_id) {

        List<ComplianceIndicator> dmComplaintList = new ArrayList<>();
        dmComplaintList.clear();
        dmComplaintList = db.complianceIndicatorDao().getComplianceNameById(complaint_id);


        if (dmComplaintList.size() != 0) {
            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = new ArrayList<>();
            dmSurveyAnswerTableList.clear();
            dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceId(complaint_id);

            String complianceName = dmComplaintList.get(0).compliance;

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View convertView = inflater.inflate(R.layout.complaint_layout, null, false);
            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint);
            final ImageView complaint_dropdown = convertView.findViewById(R.id.iv_complaint_dropdown);

            linearLayoutAll.addView(convertView);
            convertView.setTag(complaint_id);
            complaint_dropdown.setTag("0" + complaint_id);
            selected_imgName.setText(complianceName);


            complaint_dropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = complaint_dropdown.getTag().toString();

                    int tagFirst = Integer.parseInt(String.valueOf(tag.charAt(0)));

                    if (tagFirst == 0) {
                        //Toast.makeText(getActivity(), "Expand" + tag, Toast.LENGTH_SHORT).show();
                        complaint_dropdown.setTag("1" + complaint_id);
                       // linearLayoutAll.setVisibility(View.VISIBLE);

                    } else {
                        //Toast.makeText(getActivity(), "Collapse" + tag, Toast.LENGTH_SHORT).show();
                        complaint_dropdown.setTag("0" + complaint_id);
                       // linearLayoutAll.setVisibility(View.GONE);

                    }
                }
            });


           // Constants.complaintSize = String.valueOf(dmSurveyAnswerTableList.size());
            if (dmSurveyAnswerTableList.size() != 0) {
                String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
                for (int m = 0; m < dmSurveyAnswerTableList.size(); m++) {
                    DmSurveyAnswerTable dmQuestionsModule = dmSurveyAnswerTableList.get(m);
                    TextView textView = new TextView(getActivity());
                    textView.setText((m + 1) + "." + dmQuestionsModule.question);
                    textView.setTextColor(getResources().getColor(R.color.black));
                    textView.setTextSize(14);
                    textView.setPadding(0, 15, 0, 0);
                    textView.setTypeface(null, Typeface.BOLD);
                    linearLayoutAll.addView(textView);

                    renderTextboxQuestionModuleComplaint(String.valueOf(dmQuestionsModule.questionID));

                }
            }
        }
    }


    public void renderTextboxQuestionModuleComplaint(String id) {
        dm_question_complaint.removeAllViews();

        linearLayoutAll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        linearLayoutAll.setOrientation(LinearLayout.VERTICAL);


        LinearLayout.LayoutParams layoutParamsQes = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.capture_box)
        );

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setEnabled(false);
        editText.setTextColor(getResources().getColor(R.color.black));
        editText.setPadding(0, 10, 0, 10);
        layoutParamsQes.setMargins(0, 10, 0, 10);
        editText.setLayoutParams(layoutParamsQes);

        //LinearLayout complaintCaptureFiles = new LinearLayout(getActivity());

        TextView textView = new TextView(getActivity());
        textView.setText("Capture Proof");
        textView.setTag(id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextColor(getActivity().getColor(R.color.colorPrimary));
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.capture_box)
        );
        //complaintCaptureFiles.setOrientation(LinearLayout.HORIZONTAL);
        //complaintCaptureFiles.setLayoutParams(layoutParams);
        layoutParams.setMargins(0, 10, 0, 10);

        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 15, 0, 15);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg));
        }


        LinearLayout linearLayoutImage = new LinearLayout(getActivity());
        linearLayoutImage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        linearLayoutImage.setTag("1234" + id);
        linearLayoutImage.setOrientation(LinearLayout.VERTICAL);

        ArrayList<String> retrievedArray = new ArrayList<>();
        retrievedArray.clear();

        ArrayList<Long> retrievedArrayFile = new ArrayList<>();
        retrievedArrayFile.clear();

        List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
        dmComplaintFileTableList.clear();
        dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(Integer.parseInt(id));

        Log.d("akshdkjashdk", "" + dmComplaintFileTableList.size());

        if (dmComplaintFileTableList.size() != 0) {
            for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                linearLayoutImage.addView(convertView);
                convertView.setTag(l);
                remove.setTag(l);
                selected_imgName.setText(dmComplaintFileTableList.get(l).getFile_name());

                int finalI = l;
                ArrayList<Long> finalRetrievedArrayFile = retrievedArrayFile;
                ArrayList<String> finalRetrievedArray = retrievedArray;
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setMessage("Do you want to remove this report");
                        builder1.setTitle("Alert");
                        builder1.setCancelable(false);
                        builder1.setPositiveButton(
                                "YES",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                        //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                        db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI));

                                        finalRetrievedArray.clear();
                                        finalRetrievedArrayFile.clear();

                                        List<DmComplaintFileTable> dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(CAMERA_REQUEST);
                                        if (dmComplaintFileTableList.size() != 0) {
                                            for (int k = 0; k < dmComplaintFileTableList.size(); k++) {
                                                finalRetrievedArray.add(dmComplaintFileTableList.get(k).getFile_name());
                                                finalRetrievedArrayFile.add(dmComplaintFileTableList.get(k).getUid());
                                            }
                                        }

                                        for (int j = 0; j < finalRetrievedArray.size(); j++) {
                                            LayoutInflater inflater = LayoutInflater.from(getActivity());
                                            final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                                            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                                            final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                                            linearLayoutImage.addView(convertView);
                                            convertView.setTag(j);
                                            remove.setTag(j);
                                            selected_imgName.setText(finalRetrievedArray.get(j));
                                        }

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
                });
            }
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getActivity(), "" + textView.getTag(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "dyn====" + linearLayoutImage.getTag(), Toast.LENGTH_SHORT).show();

                final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Add Photo!");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            if (isStoragePermissionCameraGranted()) {
                                if (dynamicArray.size() > 5) {
                                    Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
                                } else {
                                    CAMERA_REQUEST = Integer.parseInt(textView.getTag().toString());
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

                            }

                        } else if (options[item].equals("Choose from Gallery")) {

                            if (isStoragePermissionGalleryGranted()) {
                                if (dynamicArray.size() > 5) {
                                    Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
                                } else {
                                    GALLERY_PICK = Integer.parseInt(textView.getTag().toString() + "0");
                                    Intent galleryIntent = new Intent();
                                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                    galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES);
                                    galleryIntent.setType("*/*");
                                    startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK);
                                }

                            }
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

            }
        });


        List<DmActionPlansItem> dmActionPlansItemList = new ArrayList<>();
        dmActionPlansItemList.clear();
        dmActionPlansItemList = db.dmActionPlansItemDao().getDmActionPlansItem();

        final List<String> list = new ArrayList<String>();
        list.clear();

        for (int c = 0; c < dmActionPlansItemList.size(); c++) {
            list.add(dmActionPlansItemList.get(c).actionPlan);
        }

        final Spinner spinner = new Spinner(getActivity());

        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT , 1));
        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // data for currency sign and textBox START \/
        LinearLayout.LayoutParams monetaryMainLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(getContext(), 45));
        // setting margin for the layout which contain spinner and currency indicator and editText
        monetaryMainLayoutParams.setMargins(0, dpToPx(getContext(), 5), 0, dpToPx(getContext(), 5));

        LinearLayout monetaryMainLayout = new LinearLayout(getContext());
        monetaryMainLayout.setLayoutParams(monetaryMainLayoutParams);
        monetaryMainLayout.setOrientation(LinearLayout.HORIZONTAL);


        // this contain currency indicator and editText
        LinearLayout dollarSignLayout = createDollarSignLayout();

        monetaryMainLayout.addView(spinner);
        monetaryMainLayout.addView(dollarSignLayout);
        // data for currency sign and textBox END /\

        EditText actionPlan = new EditText(getActivity());
        actionPlan.setHint("Edit action plan details");
        actionPlan.setTextSize(14);

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.actionplanbox) // Replace with your desired height in dp
        );
        actionPlan.setLayoutParams(layoutParams1);

        LinearLayout linearLayout = new LinearLayout(getActivity());

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.actionplanbox) // Replace with your desired height in dp
        );


        linearLayout.setLayoutParams(layoutParams2);

        linearLayout.setGravity(Gravity.END);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView imageView1 = new ImageView(getActivity());
        imageView1.setImageResource(R.drawable.green_tick);

        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.small_button_width_height),
                getResources().getDimensionPixelSize(R.dimen.small_button_width_height) // Replace with your desired height in dp
        );

        imageView1.setLayoutParams(layoutParams3);

        ImageView imageView2 = new ImageView(getActivity());
        imageView2.setImageResource(R.drawable.red_cross);

        imageView2.setLayoutParams(layoutParams3);

        linearLayout.addView(imageView1);
        linearLayout.addView(imageView2);


        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = new ArrayList<>();
        dmSurveyAnswerTableList.clear();

        dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(id);

        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answer;
            editText.setText(answerIdDb);
        }

        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        // Add Spinner to LinearLayout
        if (linearLayoutAll != null) {
            linearLayoutAll.addView(editText);
            linearLayoutAll.addView(textView);
//            linearLayoutAll.addView(spinner);
            linearLayoutAll.addView(monetaryMainLayout);
            linearLayoutAll.addView(linearLayoutImage);
        }
        dm_question_complaint.addView(linearLayoutAll);
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
            String filename = mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/") + 1);
            try {
                resizedBitmapNew = ImageOrientation.modifyOrientation(getActivity(), resizedBitmap, uriImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            resizedBitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();

            db.dmComplaintFileTableDao().deleteAll();
            DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
            dmComplaintFileTable.setFile_name(filename);
            dmComplaintFileTable.setFileData(imageBytes);
            dmComplaintFileTable.setQuestionId(String.valueOf(CAMERA_REQUEST));
            db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

            long id = db.dmComplaintFileTableDao().getLastInsertedId();

            dynamicArrayName = "imageArray" + CAMERA_REQUEST;

            dynamicArrayNameFile = "fileArray" + CAMERA_REQUEST;

            dynamicArrayFile.clear();
            dynamicArray.clear();

            ArrayList<String> retrievedArray = new ArrayList<>();
            retrievedArray.clear();

            ArrayList<Long> retrievedArrayFile = new ArrayList<>();

            retrievedArrayFile.clear();

            dynamicArrays.clear();
            dynamicArraysFile.clear();


            View desiredView = dm_question_complaint.findViewWithTag("1234" + String.valueOf(CAMERA_REQUEST));
            LinearLayout linearLayoutImage = (LinearLayout) desiredView;


            List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
            dmComplaintFileTableList.clear();
            dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(CAMERA_REQUEST);
            if (dmComplaintFileTableList.size() != 0) {
                for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                    retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                    retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());

                    dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                    dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                }
            }

            dynamicArrays.put(dynamicArrayName, dynamicArray);

            dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

            //Toast.makeText(getActivity(), "onactivity result === " + linearLayoutImage.getTag() + retrievedArray.size(), Toast.LENGTH_SHORT).show();

            linearLayoutImage.removeAllViews();

            for (int i = 0; i < retrievedArray.size(); i++) {

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                linearLayoutImage.addView(convertView);
                convertView.setTag(i);
                remove.setTag(i);
                selected_imgName.setText(retrievedArray.get(i));


                int finalI = i;
                ArrayList<Long> finalRetrievedArrayFile = retrievedArrayFile;
                ArrayList<String> finalRetrievedArray = retrievedArray;
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setMessage("Do you want to remove this report");
                        builder1.setTitle("Alert");
                        builder1.setCancelable(false);
                        builder1.setPositiveButton(
                                "YES",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                        //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                        linearLayoutImage.removeAllViews();

                                        db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI));

                                        finalRetrievedArray.clear();
                                        finalRetrievedArrayFile.clear();

                                        List<DmComplaintFileTable> dmComplaintFileTableList1 = new ArrayList<>();
                                        dmComplaintFileTableList1.clear();
                                        dmComplaintFileTableList1 = db.dmComplaintFileTableDao().getFilesById(CAMERA_REQUEST);
                                        if (dmComplaintFileTableList1.size() != 0) {
                                            for (int k = 0; k < dmComplaintFileTableList1.size(); k++) {
                                                finalRetrievedArray.add(dmComplaintFileTableList1.get(k).getFile_name());
                                                finalRetrievedArrayFile.add(dmComplaintFileTableList1.get(k).getUid());
                                            }
                                        }

                                        for (int j = 0; j < finalRetrievedArray.size(); j++) {
                                            LayoutInflater inflater = LayoutInflater.from(getActivity());
                                            final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                                            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                                            final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                                            linearLayoutImage.addView(convertView);
                                            convertView.setTag(j);
                                            remove.setTag(j);
                                            selected_imgName.setText(finalRetrievedArray.get(j));
                                        }

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
                });


            }


        }
        if ((requestCode == GALLERY_PICK && resultCode == RESULT_OK) && data != null) {

            Uri uri = data.getData();
            ContentResolver cr = getActivity().getContentResolver();
            String mime = cr.getType(uri);
            Toast.makeText(getActivity(), "" + GALLERY_PICK, Toast.LENGTH_SHORT).show();

            String valueCode = String.valueOf(GALLERY_PICK);


            StringBuffer sb = new StringBuffer(valueCode);
            sb.deleteCharAt(sb.length() - 1);

            int questionId = Integer.parseInt(sb.toString());

            Toast.makeText(getActivity(), "" + questionId, Toast.LENGTH_SHORT).show();


            if (mime.equalsIgnoreCase("application/vnd.ms-excel")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    || mime.equalsIgnoreCase("application/msword") || mime.equalsIgnoreCase("text/plain")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    || mime.equalsIgnoreCase("text/csv")) {
                String filename = getFileName(uri);


                byte[] fileByteArray;
                try {
                    fileByteArray = FileUtils.convertFileToByteArray(getActivity(), uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setFileData(fileByteArray);
                dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
                db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

                long id = db.dmComplaintFileTableDao().getLastInsertedId();

                dynamicArrayName = "imageArray" + questionId;

                dynamicArrayNameFile = "fileArray" + questionId;

                dynamicArrayFile.clear();
                dynamicArray.clear();

                ArrayList<String> retrievedArray = new ArrayList<>();
                retrievedArray.clear();

                ArrayList<Long> retrievedArrayFile = new ArrayList<>();

                retrievedArrayFile.clear();

                dynamicArrays.clear();
                dynamicArraysFile.clear();


                View desiredView = dm_question_complaint.findViewWithTag("1234" + String.valueOf(questionId));
                LinearLayout linearLayoutImage = (LinearLayout) desiredView;


                List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
                dmComplaintFileTableList.clear();
                dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId);
                if (dmComplaintFileTableList.size() != 0) {
                    for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                        retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());

                        dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                    }
                }

                dynamicArrays.put(dynamicArrayName, dynamicArray);

                dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

                //Toast.makeText(getActivity(), "onactivity result === " + linearLayoutImage.getTag() + retrievedArray.size(), Toast.LENGTH_SHORT).show();

                linearLayoutImage.removeAllViews();

                for (int i = 0; i < retrievedArray.size(); i++) {

                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                    final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                    final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                    linearLayoutImage.addView(convertView);
                    convertView.setTag(i);
                    remove.setTag(i);
                    selected_imgName.setText(retrievedArray.get(i));


                    int finalI = i;
                    ArrayList<Long> finalRetrievedArrayFile = retrievedArrayFile;
                    ArrayList<String> finalRetrievedArray = retrievedArray;
                    remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                            builder1.setMessage("Do you want to remove this report");
                            builder1.setTitle("Alert");
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    "YES",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                            //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                            linearLayoutImage.removeAllViews();

                                            db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI));

                                            finalRetrievedArray.clear();
                                            finalRetrievedArrayFile.clear();

                                            List<DmComplaintFileTable> dmComplaintFileTableList1 = new ArrayList<>();
                                            dmComplaintFileTableList1.clear();
                                            dmComplaintFileTableList1 = db.dmComplaintFileTableDao().getFilesById(questionId);
                                            if (dmComplaintFileTableList1.size() != 0) {
                                                for (int k = 0; k < dmComplaintFileTableList1.size(); k++) {
                                                    finalRetrievedArray.add(dmComplaintFileTableList1.get(k).getFile_name());
                                                    finalRetrievedArrayFile.add(dmComplaintFileTableList1.get(k).getUid());
                                                }
                                            }

                                            for (int j = 0; j < finalRetrievedArray.size(); j++) {
                                                LayoutInflater inflater = LayoutInflater.from(getActivity());
                                                final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                                                final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                                                final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                                                linearLayoutImage.addView(convertView);
                                                convertView.setTag(j);
                                                remove.setTag(j);
                                                selected_imgName.setText(finalRetrievedArray.get(j));
                                            }

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
                    });


                }


            } else if (mime.equalsIgnoreCase("application/pdf")) {
                String filename = getFileName(uri);

                // Toast.makeText(getActivity(), "" + filename + "...." + uri, Toast.LENGTH_SHORT).show();

                // Get the compressed file size
                AssetFileDescriptor fileDescriptor = null;
                try {
                    fileDescriptor = getContext().getContentResolver().openAssetFileDescriptor(uri, "r");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                long compressedSize = fileDescriptor.getLength();

                // Display the file size
                String fileSize = getFormattedFileSize(compressedSize);
                String message = "PDF Size: " + fileSize;
                // Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();


                if ((1024f * 1024f) < compressedSize) {
                    Toast.makeText(getContext(), "Sizer is greater than 10 MB", Toast.LENGTH_SHORT).show();
                    Uri uri1 = Uri.fromFile(compressPdf(uri));
                    byte[] pdfByteArray1;
                    try {
                        pdfByteArray1 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    Uri uri1 = Uri.fromFile(compressPdf(uri));

                    byte[] pdfByteArray2;
                    try {
                        pdfByteArray2 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    db.dmComplaintFileTableDao().deleteAll();

                    DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                    dmComplaintFileTable.setFile_name(filename);
                    dmComplaintFileTable.setFileData(pdfByteArray2);
                    dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
                    db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

                    long id = db.dmComplaintFileTableDao().getLastInsertedId();

                    dynamicArrayName = "imageArray" + questionId;

                    dynamicArrayNameFile = "fileArray" + questionId;

                    dynamicArrayFile.clear();
                    dynamicArray.clear();

                    ArrayList<String> retrievedArray = new ArrayList<>();
                    retrievedArray.clear();

                    ArrayList<Long> retrievedArrayFile = new ArrayList<>();

                    retrievedArrayFile.clear();

                    dynamicArrays.clear();
                    dynamicArraysFile.clear();


                    View desiredView = dm_question_complaint.findViewWithTag("1234" + String.valueOf(questionId));
                    LinearLayout linearLayoutImage = (LinearLayout) desiredView;


                    List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
                    dmComplaintFileTableList.clear();
                    dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId);
                    if (dmComplaintFileTableList.size() != 0) {
                        for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                            retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                            retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());

                            dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                            dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                        }
                    }

                    dynamicArrays.put(dynamicArrayName, dynamicArray);

                    dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

                    //Toast.makeText(getActivity(), "onactivity result === " + linearLayoutImage.getTag() + retrievedArray.size(), Toast.LENGTH_SHORT).show();

                    linearLayoutImage.removeAllViews();

                    for (int i = 0; i < retrievedArray.size(); i++) {

                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                        final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                        final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                        linearLayoutImage.addView(convertView);
                        convertView.setTag(i);
                        remove.setTag(i);
                        selected_imgName.setText(retrievedArray.get(i));


                        int finalI = i;
                        ArrayList<Long> finalRetrievedArrayFile = retrievedArrayFile;
                        ArrayList<String> finalRetrievedArray = retrievedArray;
                        remove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                                builder1.setMessage("Do you want to remove this report");
                                builder1.setTitle("Alert");
                                builder1.setCancelable(false);
                                builder1.setPositiveButton(
                                        "YES",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();

                                                //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                                linearLayoutImage.removeAllViews();

                                                db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI));

                                                finalRetrievedArray.clear();
                                                finalRetrievedArrayFile.clear();

                                                List<DmComplaintFileTable> dmComplaintFileTableList1 = new ArrayList<>();
                                                dmComplaintFileTableList1.clear();
                                                dmComplaintFileTableList1 = db.dmComplaintFileTableDao().getFilesById(questionId);
                                                if (dmComplaintFileTableList1.size() != 0) {
                                                    for (int k = 0; k < dmComplaintFileTableList1.size(); k++) {
                                                        finalRetrievedArray.add(dmComplaintFileTableList1.get(k).getFile_name());
                                                        finalRetrievedArrayFile.add(dmComplaintFileTableList1.get(k).getUid());
                                                    }
                                                }

                                                for (int j = 0; j < finalRetrievedArray.size(); j++) {
                                                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                                                    final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                                                    final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                                                    final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                                                    linearLayoutImage.addView(convertView);
                                                    convertView.setTag(j);
                                                    remove.setTag(j);
                                                    selected_imgName.setText(finalRetrievedArray.get(j));
                                                }

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
                        });


                    }

                }


            } else {
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

                String filename = filePathimage.substring(filePathimage.lastIndexOf("/") + 1);

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false);

                try {
                    resizedBitmapNew = ImageOrientation.modifyOrientation(getActivity(), resizedBitmap, uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                resizedBitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                db.dmComplaintFileTableDao().deleteAll();
                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setFileData(imageBytes);
                dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
                db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

                long id = db.dmComplaintFileTableDao().getLastInsertedId();

                dynamicArrayName = "imageArray" + questionId;

                dynamicArrayNameFile = "fileArray" + questionId;

                dynamicArrayFile.clear();
                dynamicArray.clear();

                ArrayList<String> retrievedArray = new ArrayList<>();
                retrievedArray.clear();

                ArrayList<Long> retrievedArrayFile = new ArrayList<>();

                retrievedArrayFile.clear();

                dynamicArrays.clear();
                dynamicArraysFile.clear();


                View desiredView = dm_question_complaint.findViewWithTag("1234" + String.valueOf(questionId));
                LinearLayout linearLayoutImage = (LinearLayout) desiredView;


                List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
                dmComplaintFileTableList.clear();
                dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId);
                if (dmComplaintFileTableList.size() != 0) {
                    for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                        retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());

                        dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                    }
                }

                dynamicArrays.put(dynamicArrayName, dynamicArray);

                dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

                //Toast.makeText(getActivity(), "onactivity result === " + linearLayoutImage.getTag() + retrievedArray.size(), Toast.LENGTH_SHORT).show();

                linearLayoutImage.removeAllViews();

                for (int i = 0; i < retrievedArray.size(); i++) {

                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                    final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                    final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                    linearLayoutImage.addView(convertView);
                    convertView.setTag(i);
                    remove.setTag(i);
                    selected_imgName.setText(retrievedArray.get(i));


                    int finalI = i;
                    ArrayList<Long> finalRetrievedArrayFile = retrievedArrayFile;
                    ArrayList<String> finalRetrievedArray = retrievedArray;
                    remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                            builder1.setMessage("Do you want to remove this report");
                            builder1.setTitle("Alert");
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    "YES",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                            //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                            linearLayoutImage.removeAllViews();

                                            db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI));

                                            finalRetrievedArray.clear();
                                            finalRetrievedArrayFile.clear();

                                            List<DmComplaintFileTable> dmComplaintFileTableList1 = new ArrayList<>();
                                            dmComplaintFileTableList1.clear();
                                            dmComplaintFileTableList1 = db.dmComplaintFileTableDao().getFilesById(questionId);
                                            if (dmComplaintFileTableList1.size() != 0) {
                                                for (int k = 0; k < dmComplaintFileTableList1.size(); k++) {
                                                    finalRetrievedArray.add(dmComplaintFileTableList1.get(k).getFile_name());
                                                    finalRetrievedArrayFile.add(dmComplaintFileTableList1.get(k).getUid());
                                                }
                                            }

                                            for (int j = 0; j < finalRetrievedArray.size(); j++) {
                                                LayoutInflater inflater = LayoutInflater.from(getActivity());
                                                final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
                                                final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
                                                final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

                                                linearLayoutImage.addView(convertView);
                                                convertView.setTag(j);
                                                remove.setTag(j);
                                                selected_imgName.setText(finalRetrievedArray.get(j));
                                            }

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
                    });


                }
            }

        }


    }

    public void showCurrencyDropDown(List<DmCurrenciesItem> dmCurrenciesItemList) {


        final List<String> list = new ArrayList<String>();
        list.clear();

        final List<String> ids = new ArrayList<String>();
        ids.clear();

        for (int c = 0; c < dmCurrenciesItemList.size(); c++) {
            list.add(dmCurrenciesItemList.get(c).code);
            ids.add(String.valueOf(dmCurrenciesItemList.get(c).currencyId));
        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_dmCurrencies.setAdapter(spinnerAdapter);

        String currencyIdDb = "0";

        List<DmActionPlanCurrenciesTable> dmActionPlanCurrenciesTables = db.dmActionPlanCurrenciesTableDao().getDataByModuleId(Constants.MODULE_ID);
        if (dmActionPlanCurrenciesTables.size() != 0) {
            currencyIdDb = String.valueOf(dmActionPlanCurrenciesTables.get(0).currencyId);
        }

        int selectedPosition = 0;

        for (int i = 0; i < ids.size(); i++) {
            if (currencyIdDb.equalsIgnoreCase(ids.get(i))) {
                selectedPosition = i;
            }

        }

        spinner_dmCurrencies.setSelection(selectedPosition);

        spinner_dmCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();

                String answerId = ids.get(position);

                db.dmActionPlanCurrenciesTableDao().deleteByModuleId(Constants.MODULE_ID);

                DmActionPlanCurrenciesTable dmActionPlanCurrenciesTable = new DmActionPlanCurrenciesTable();
                dmActionPlanCurrenciesTable.moduleId = Integer.parseInt(Constants.MODULE_ID);
                dmActionPlanCurrenciesTable.currencyId = Integer.parseInt(ids.get(position));
                dmActionPlanCurrenciesTable.code = list.get(position);
                db.dmActionPlanCurrenciesTableDao().insert(dmActionPlanCurrenciesTable);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(PatientReasonActivity.this, R.string.camera_perm_granted, Toast.LENGTH_LONG).show();
                //openCamera();
            } else {
                Toast.makeText(getActivity(), "Please give camera permission", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == MY_GALLERYPERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(PatientReasonActivity.this, R.string.cs, Toast.LENGTH_LONG).show();
                //openGallery();
            } else {
                Toast.makeText(getActivity(), "Please give gallery permission", Toast.LENGTH_LONG).show();
            }
        }

    }


    private File compressPdf(Uri uri) {
        try {

            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            File outputFile = new File(getActivity().getCacheDir(), getFileName(uri));
            OutputStream outputStream = new FileOutputStream(outputFile);

            // Create a reader and writer
            PdfReader reader = new PdfReader(inputStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);
            stamper.getWriter().setCompressionLevel(9);
            int total = reader.getNumberOfPages() + 1;
            for (int i = 1; i < total; i++) {
                reader.setPageContent(i + 1, reader.getPageContent(i + 1));
            }
            stamper.setFullCompression();
            stamper.close();
            reader.close();

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Get the compressed file size
            long compressedSize = outputFile.length();

            // Display a success message with the compressed file size
            String fileSize = getFormattedFileSize(compressedSize);
            String message = "PDF compressed successfully. Size: " + fileSize;
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

            return outputFile;

        } catch (IOException e) {
            e.printStackTrace();
            // Handle any errors that occur during compression
            Toast.makeText(getContext(), "Error compressing PDF", Toast.LENGTH_SHORT).show();
            return null;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFormattedFileSize(long fileSize) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return decimalFormat.format(fileSize / 1024f) + " KB";
        } else {
            return decimalFormat.format(fileSize / (1024f * 1024f)) + " MB";
        }
    }

    // To get name of the selected file
    private String getFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
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


    public String getExt(String filePath) {
        int strLength = filePath.lastIndexOf(".");
        if (strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }


    private LinearLayout createDollarSignLayout() {
        LinearLayout.LayoutParams dollarSignLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        dollarSignLayoutParams.setMargins(dpToPx(getContext(), 7), 0, 0, 0);
        dollarSignLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;

        LinearLayout dollarSignLayout = new LinearLayout(getContext());
        dollarSignLayout.setLayoutParams(dollarSignLayoutParams);
        dollarSignLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView dollarSignTextView = createDollarSignTextView();

        EditText dollarEditText = createDollarEditText();

        dollarSignLayout.addView(dollarSignTextView);
        dollarSignLayout.addView(dollarEditText);
        return dollarSignLayout;
    }

    private TextView createDollarSignTextView() {
        LinearLayout.LayoutParams dollarSignTextLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        TextView dollarSignTextView = new TextView(getContext());
        dollarSignTextView.setLayoutParams(dollarSignTextLayoutParams);
        dollarSignTextView.setText("$");
        dollarSignTextView.setTextSize(10);
        dollarSignTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.answer_text_survey));
        return dollarSignTextView;
    }

    private EditText createDollarEditText() {
        LinearLayout.LayoutParams dollarEditTextLayoutParams = new LinearLayout.LayoutParams(
                dpToPx(getContext(), 70),
                LinearLayout.LayoutParams.MATCH_PARENT);

        EditText dollarEditText = new EditText(getContext());
        dollarEditText.setLayoutParams(dollarEditTextLayoutParams);
        dollarEditText.setBackground(getActivity().getResources().getDrawable(R.drawable.answer_text_survey));
        dollarEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        dollarEditText.setHint("Amount");
        dollarEditText.setTextSize(10);
        dollarEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        dollarEditText.setId(View.generateViewId());
        return dollarEditText;
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}