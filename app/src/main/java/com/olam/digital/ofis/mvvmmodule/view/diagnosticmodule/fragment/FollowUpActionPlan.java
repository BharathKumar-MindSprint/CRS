package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
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
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.ViewFarmerActivity;
import com.olam.digital.ofis.gps.GPSListener;
import com.olam.digital.ofis.gps.GPSTracker;
import com.olam.digital.ofis.gps.utils.GPSUtils;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.FileUtils;
import com.olam.digital.ofis.mvvmmodule.ImageOrientation;
import com.olam.digital.ofis.mvvmmodule.PDFUtils;
import com.olam.digital.ofis.mvvmmodule.PathUtil;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmActionPlanCurrenciesTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmCurrency;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeActionPlansItems;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class FollowUpActionPlan extends Fragment {
    private static final int REQ_CODE = 300;
    private final String MATOMOTAG = this.getClass().getSimpleName();
    LinearLayout ll_servey_back_followup_actionplan, ll_servey_next_followup_actionplan;

    public Resources res;

    private static final int MY_GALLERYPERMISSION_CODE = 100;
    private static final int MY_GALLERYPERMISSION_CODE_TIRAMISU = 222;
    private static final int MY_CAMERA_PERMISSION_CODE = 500;

    private static final int MY_CAMERA_PERMISSION_CODE_TIRAMISU = 333;
    private static final int CAMERA_REQUEST = 2277;
    private static final int GALLERY_PICK = 2233;
    private static final int GALLERY_PICK_DOCX = 4455;

    private static int GALLERY_QUESTION_ID = 0;
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private String mCurrentPhotoPath;

    private Bitmap resizedBitmapNew;

    LinearLayout dm_question_followup_complaint;

    LinearLayout linearLayoutAll;

    HashMap<String, ArrayList<String>> dynamicArrays = new HashMap<>();

    String dynamicArrayName;

    ArrayList<String> dynamicArray = new ArrayList<>();

    HashMap<String, ArrayList<Long>> dynamicArraysFile = new HashMap<>();

    String dynamicArrayNameFile;
    ArrayList<Long> dynamicArrayFile = new ArrayList<>();

    String currency_id = "0";

    private static final String[] ACCEPTED_MIME_TYPES_IMG = {"image/jpeg", "image/png"};

    private static final String[] ACCEPTED_MIME_TYPES_DOC = {"application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
            "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/csv"};

    private static final String[] ACCEPTED_MIME_TYPES_ALL_FORMAT = {"image/jpeg", "image/png","application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
            "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/csv"};


    //Room DB
    OFISRoomDB db;
    Spinner spinner_followup_dmCurrencies;
    String budgetAmount;
    private boolean isCreated = false;

    boolean goNextCaptureProof =false;
    boolean goNextActionPlan = true;

    private final long FILE_SIZE = 5242880;
    private final String errorSizeMessage = "5MB";

    private Location mCurrentCapturedLocation = null;
    GPSListener gpsListener = null;
    private GPSTracker gpsTracker = null;

    public FollowUpActionPlan() {
        // Required empty public constructor
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_follow_up_action_plan, container, false);

        res = getResources();

        gpsListener = new GPSListener() {
            @Override
            public void onGPSClick(Location location) {
                if (location != null && !GPSUtils.isMockLocation(location)) {
                    mCurrentCapturedLocation = location;
                }
            }
        };

        gpsTracker = new GPSTracker(getActivity(), gpsListener);
        gpsTracker.startLocationUpdateInitially();

        ll_servey_back_followup_actionplan = view.findViewById(R.id.ll_servey_back_followup_actionplan);
        ll_servey_next_followup_actionplan = view.findViewById(R.id.ll_servey_next_followup_actionplan);
        spinner_followup_dmCurrencies = view.findViewById(R.id.spinner_followup_dmCurrencies);
        dm_question_followup_complaint = view.findViewById(R.id.dm_question_followup_complaint);
        ll_servey_next_followup_actionplan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mCurrentCapturedLocation == null || !gpsTracker.isGpsEnabled()) {
                    gpsTracker.getDeviceLocation();
                    return;
                }

                if (mCurrentCapturedLocation != null && GPSUtils.isMockLocation(mCurrentCapturedLocation)) {
                    return;
                }

                ArrayList<String> qId = Constants.questionIdsFollowUp;

                if (validateCustomActionPlanAndCaptureProof(qId)){
                    DataLayer dl = new DataLayer(getResources(), getContext());
                    dl.updateNqForDMSurvey(Constants.FARMER_ID, Constants.MODULE_ID, "FollowUpSurvey", Constants.MODULE_NAME, Constants.PLOT_ID, mCurrentCapturedLocation.getLatitude() + "," + mCurrentCapturedLocation.getLongitude());
                    Intent intent = new Intent(getContext(), ViewFarmerActivity.class);
                    getContext().startActivity(intent);
                }
            }
        });

        return view;
    }

    private boolean validateCustomActionPlanAndCaptureProof(ArrayList<String> qId) {

        for (int i = 0; i < qId.size(); i++) {

            String ans = db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID, Constants.PLOT_ID).get(0).answer;
            String displayType = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qId.get(i))).get(0).displayType;
            int ansId = 0;

            try {
                ansId = Integer.parseInt(db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID, Constants.PLOT_ID).get(0).answerID);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

            boolean allowCaptureProof = checkAllowCaptureProof(qId.get(i), ans, ansId, displayType);

            if(allowCaptureProof) {
                if (Objects.equals( db.followUpItemMainTableDao().getResolvedOrNOt(qId.get(i) , Constants.FARMER_ID, Constants.ATTEMPT),"NOT Resolved")){
                    List<DmComplaintFileTable> dmComplaintFiles = db.dmComplaintFileTableDao().getTotalFollowCount(Integer.parseInt(qId.get(i)),Constants.FARMER_ID,Constants.ATTEMPT,"FollowUp");
                    if(dmComplaintFiles.isEmpty()){
                        Toast.makeText(getContext(), getResources().getString(R.string.dm_capture_proof_is_missing), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        }

        for (int j=0; j<qId.size(); j++) {
            if (Objects.equals(db.followUpItemMainTableDao().getActionPlanTextFollowUp(qId.get(j), Constants.FARMER_ID, Constants.NOTRESOLVED), "0")){
                Toast.makeText(getActivity(), getResources().getString(R.string.dm_action_plan_is_missing), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }




    @SuppressLint("ResourceAsColor")
    private void displayQuestionsModuleComplaint(String complaint_id) {

        boolean isActionPlanRequired = db.complianceIndicatorDao().getIsActionPlanRequired(complaint_id);
        List<ComplianceIndicator> dmComplaintList = db.complianceIndicatorDao().getComplianceNameById(complaint_id);

        if (!dmComplaintList.isEmpty()) {
            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceIdFilterByActionPlan(complaint_id,Constants.FARMER_ID, Constants.PLOT_ID);
            String complianceName = dmComplaintList.get(0).compliance;

            for (int amt = 0; amt < dmSurveyAnswerTableList.size(); amt++) {
                budgetAmount = String.valueOf(dmSurveyAnswerTableList.get(amt).amount);
            }

            List<DmSurveyAnswerTable> dmSurveyAnswerTableList1 = db.dmSurveyAnswerTableDao().getDataByComplianceId(complaint_id, Constants.FARMER_ID, Constants.PLOT_ID);

            if (complianceName.equalsIgnoreCase("Satisfactory")) {
                Constants.satisfactorySize = String.valueOf(dmSurveyAnswerTableList1.size());
            } else if (complianceName.equalsIgnoreCase("Compliant")) {
                Constants.complaintSize = String.valueOf(dmSurveyAnswerTableList1.size());
            } else if (complianceName.equalsIgnoreCase("Non Compliant")) {
                Constants.noncomplaintSize = String.valueOf(dmSurveyAnswerTableList1.size());
            }

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View convertView = inflater.inflate(R.layout.complaint_layout, null, false);
            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint);
            final ImageView complaint_dropdown = convertView.findViewById(R.id.iv_complaint_dropdown);

            dm_question_followup_complaint.addView(convertView);
            convertView.setTag(complaint_id);
            complaint_dropdown.setTag("0" + complaint_id);
            if (Objects.equals(complianceName,"Compliant")) {
                selected_imgName.setText(getResources().getString(R.string.dm_compliant));
            } else if (Objects.equals(complianceName,"Non Compliant")) {
                selected_imgName.setText(getResources().getString(R.string.dm_non_compliant));
            } else if (Objects.equals(complianceName,"Satisfactory")) {
                selected_imgName.setText(getResources().getString(R.string.dm_satisfactory));
            }


            List<DmSurveyAnswerTable> finalDmSurveyAnswerTableList = dmSurveyAnswerTableList;

            complaint_dropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = complaint_dropdown.getTag().toString();

                    int tagFirst = Integer.parseInt(String.valueOf(tag.charAt(0)));

                    if (tagFirst == 0) {
                        complaint_dropdown.setTag("1" + complaint_id);
                        complaint_dropdown.setRotation(180);
                        if (finalDmSurveyAnswerTableList.size() != 0) {
                            if(isActionPlanRequired){
                                View desiredView = dm_question_followup_complaint.findViewWithTag("item" + complaint_id);
                                LinearLayout linearLayout = (LinearLayout) desiredView;
                                linearLayout.setVisibility(View.VISIBLE);
                            }

                        }

                    } else {
                        complaint_dropdown.setTag("0" + complaint_id);
                        complaint_dropdown.setRotation(0);
                        if (finalDmSurveyAnswerTableList.size() != 0) {
                            if(isActionPlanRequired){
                                View desiredView = dm_question_followup_complaint.findViewWithTag("item" + complaint_id);
                                LinearLayout linearLayout = (LinearLayout) desiredView;
                                linearLayout.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });


            if(isActionPlanRequired) {

                LinearLayout linearLayoutItems = new LinearLayout(getActivity());
                LinearLayout.LayoutParams layoutParamLastHp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayoutItems.setLayoutParams(layoutParamLastHp);
                linearLayoutItems.setPadding(25, 10, 10, 10);
                linearLayoutItems.setTag("item" + complaint_id);
                linearLayoutItems.setOrientation(LinearLayout.VERTICAL);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    linearLayoutItems.setBackground(getActivity().getDrawable(R.drawable.dm_actionplan_module_bg));
                }

                if (!dmSurveyAnswerTableList.isEmpty()) {
                    String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
                    for (int m = 0; m < dmSurveyAnswerTableList.size(); m++) {
                        DmSurveyAnswerTable dmQuestionsModule = dmSurveyAnswerTableList.get(m);
                        TextView textView = new TextView(getActivity());
                        textView.setText((m + 1) + "." + dmQuestionsModule.question);
                        textView.setTextColor(getResources().getColor(R.color.black));
                        textView.setTextSize(14);
                        textView.setPadding(0, 15, 0, 0);
                        textView.setTypeface(null, Typeface.BOLD);
                        linearLayoutItems.addView(textView);

                        renderTextboxQuestionModuleComplaint(String.valueOf(dmQuestionsModule.questionID), complaint_id, linearLayoutItems, dmQuestionsModule.answerID);

                    }
                }
            }

        }
    }


    @SuppressLint("ResourceAsColor")
    public void renderTextboxQuestionModuleComplaint(String id, String complaint_id, LinearLayout linearLayoutItems, String answerID) {


        LinearLayout linearLayoutQes = new LinearLayout(getActivity());
        LinearLayout.LayoutParams layoutParamsQes = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutQes.setOrientation(LinearLayout.VERTICAL);
        linearLayoutQes.setLayoutParams(layoutParamsQes);
        List<FollowUpItemMainTable> dmSurveyAnswerTableList1 = db.followUpItemMainTableDao().getFollowupByQuestionId1(id, Constants.FARMER_ID, Constants.ATTEMPT);
        TextView notResolveText = new TextView(getContext());
        notResolveText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        notResolveText.setTextSize(12);
        notResolveText.setPadding(7, 3, 7, 3    );
        layoutParamsQes.setMargins(0, 15, 0, 0);

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setEnabled(false);
        editText.setTextColor(getResources().getColor(R.color.black));
        editText.setPadding(10, 10, 0, 10);
        layoutParamsQes.setMargins(0, 10, 0, 10);
        editText.setLayoutParams(layoutParamsQes);
        editText.setTag("et" + complaint_id);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(id, Constants.FARMER_ID, Constants.PLOT_ID);

        if (!dmSurveyAnswerTableList.isEmpty()) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).actionPlanText;
            editText.setText(answerIdDb);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }
        TextView textView = new TextView(getActivity());

        //Important Do not Change the Tag
        textView.setTag(id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextColor(getActivity().getColor(R.color.colorPrimary));
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.capture_box)
        );
        layoutParams.setMargins(0, 10, 0, 10);

        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 15, 0, 15);

        LinearLayout linearLayoutImage = new LinearLayout(getActivity());
        linearLayoutImage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        linearLayoutImage.setTag("1234" + id);
        linearLayoutImage.setOrientation(LinearLayout.VERTICAL);

        ArrayList<String> retrievedArray = new ArrayList<>();
        retrievedArray.clear();

        ArrayList<Long> retrievedArrayFile = new ArrayList<>();
        retrievedArrayFile.clear();

        List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
        dmComplaintFileTableList.clear();
        dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(Integer.parseInt(id), Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);

        boolean isFilesUploaded = false;

        if (dmComplaintFileTableList.size() != 0) {
            for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                if(dmComplaintFileTableList.get(l).getSurveyType().equalsIgnoreCase("FollowUp")){
                    retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                    retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                }

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

                isFilesUploaded = isFilesUploaded || dmComplaintFileTableList.get(l).getIsFileSynced() == 1;
                if (isFilesUploaded) {
                    remove.setVisibility(View.INVISIBLE);
                }
                else {
                    remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                            builder1.setMessage(getResources().getString(R.string.dm_do_you_want_to_remove_this_report));
                            builder1.setTitle(getResources().getString(R.string.Alert_txt));
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    getResources().getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                            //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();
                                            String deleteData = db.dmComplaintFileTableDao().getFilePathToDelete(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT);
                                            deleteFile(deleteData);
                                            db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);

                                            dm_question_followup_complaint.removeAllViews();

                                            if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
                                                for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                                                    displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
                                                }
                                            }

                                        }
                                    });
                            builder1.setNegativeButton(
                                    getResources().getString(R.string.no),
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

        if (isFilesUploaded) {
            textView.setText("Capture Proof Uploaded");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textView.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg_disabled));
            }
        }
        else {
            textView.setText(getResources().getString(R.string.dm_capture_proof));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textView.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg));
            }

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (db.dmComplaintFileTableDao().getFilesById(Integer.parseInt(id),Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID).size()>=5){
                        Toast.makeText(getActivity(), getResources().getString(R.string.dm_maximum_5_files_can_be_uploaded), Toast.LENGTH_SHORT).show();

                    }else {

                        CharSequence[] options = new CharSequence[0];


                        try{
                            String formatValueTextBox = db.dmRangeTextBoxModuleQuestionsTableDao().getProofFormatByQuestionId(id,complaint_id,answerID);
                            String formatValueDropBox = db.dmLookupValuesItemDao().getProofFormatByQuestionIdDropDown(id,complaint_id, answerID);
                            if (Objects.equals(formatValueTextBox, "Both") || Objects.equals(formatValueDropBox, "Both")){
                                options = new CharSequence[]{getResources().getString(R.string.take_photo),  getResources().getString(R.string.dm_choose_from_gallery), getResources().getString(R.string.cancel)};
//                            Toast.makeText(getContext(),formatValue, Toast.LENGTH_LONG).show();
                            } else if (Objects.equals(formatValueTextBox, "Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)") || Objects.equals(formatValueDropBox, "Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)")) {
                                options = new CharSequence[]{ getResources().getString(R.string.dm_choose_from_gallery), getResources().getString(R.string.cancel)};
//                            Toast.makeText(getContext(),formatValue, Toast.LENGTH_LONG).show();
                            } else if (Objects.equals(formatValueTextBox, "Image (.png, .jpg, .jpeg)") || Objects.equals(formatValueDropBox, "Image (.png, .jpg, .jpeg)")) {
                                options = new CharSequence[]{getResources().getString(R.string.take_photo),  getResources().getString(R.string.dm_choose_from_gallery), getResources().getString(R.string.cancel)};
//                            Toast.makeText(getContext(),formatValue, Toast.LENGTH_LONG).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        CharSequence[] finalOptions = options;


//                final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getResources().getString(R.string.dm_add_photo));

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (finalOptions[item].equals(getResources().getString(R.string.take_photo))) {
                                    if (isStoragePermissionCameraGranted()) {
//                                    CAMERA_REQUEST = Integer.parseInt(textView.getTag().toString());
                                        GALLERY_QUESTION_ID = Integer.parseInt(textView.getTag().toString());
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

                                } else if (finalOptions[item].equals(getResources().getString(R.string.dm_choose_from_gallery))) {

                                    if (isStoragePermissionGalleryGranted()) {

                                        String formatValueTextBox = db.dmRangeTextBoxModuleQuestionsTableDao().getProofFormatByQuestionId(id,complaint_id,answerID);
                                        String formatValueDropBox = db.dmLookupValuesItemDao().getProofFormatByQuestionIdDropDown(id,complaint_id, answerID);
                                        if (Objects.equals(formatValueTextBox, "Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)") || Objects.equals(formatValueDropBox,"Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)")){
//                                        GALLERY_PICK_DOCX = Integer.parseInt(textView.getTag().toString() + "0");
                                            Intent galleryIntent = new Intent();
                                            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                            galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES_DOC);
                                            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                            galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                            galleryIntent.setType("*/*");
                                            GALLERY_QUESTION_ID = Integer.parseInt(textView.getTag().toString());
                                            startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK_DOCX);
                                        }else if (Objects.equals(formatValueTextBox, "Image (.png, .jpg, .jpeg)") || Objects.equals(formatValueDropBox, "Image (.png, .jpg, .jpeg)")){
//                                        GALLERY_PICK = Integer.parseInt(textView.getTag().toString() + "0");
                                            Intent galleryIntent = new Intent();
                                            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                            galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES_IMG);
                                            galleryIntent.setType("*/*");
                                            GALLERY_QUESTION_ID = Integer.parseInt(textView.getTag().toString());
                                            startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK);
                                        } else if (Objects.equals(formatValueTextBox, "Both") || Objects.equals(formatValueDropBox, "Both")) {
//                                        GALLERY_PICK = Integer.parseInt(textView.getTag().toString() + "0");
                                            Intent galleryIntent = new Intent();
                                            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                            galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES_ALL_FORMAT);
                                            galleryIntent.setType("*/*");
                                            GALLERY_QUESTION_ID = Integer.parseInt(textView.getTag().toString());
                                            startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK);
                                        }


                                    }
                                } else if (finalOptions[item].equals(getResources().getString(R.string.cancel))) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();

                    }




                }
            });
        }




        List<DmRangeActionPlansItems> dmActionPlansItemList = new ArrayList<>();
        dmActionPlansItemList = db.dmRangeActionPlansItemsDao().getActionPlanItemById(Integer.parseInt(id), Integer.parseInt(complaint_id), answerID);

        final List<String> list = new ArrayList<String>();
        final List<String> ids = new ArrayList<String>();

        list.add(getResources().getString(R.string.dm_select_action_plan));
        ids.add("0");
        String ans = db.dmSurveyAnswerTableDao().getDataByQuestionId(id, Constants.FARMER_ID, Constants.PLOT_ID).get(0).answer;
        String displayType = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(id)).get(0).displayType;
        int ansId = 0;
        try {
            ansId = Integer.parseInt(db.dmSurveyAnswerTableDao().getDataByQuestionId(id, Constants.FARMER_ID, Constants.PLOT_ID).get(0).answerID);
        }catch (NumberFormatException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        boolean allowCustomActionPlan = checkAllowCustomActionPlan(id,ans, ansId,displayType);
        if (allowCustomActionPlan) {
            list.add(getResources().getString(R.string.dm_custom_action_plan));
            ids.add("1");
        }

        for (int c = 0; c < dmActionPlansItemList.size(); c++) {
            list.add(Utilities.getActionPlanByLanguage(dmActionPlansItemList.get(c)));
            ids.add(String.valueOf(dmActionPlansItemList.get(c).id));
        }

        LinearLayout linearLayoutSpin = new LinearLayout(getActivity());
        linearLayoutSpin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayoutSpin.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutSpin.setWeightSum(10);

        LinearLayout.LayoutParams layoutParamsSpin = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.capture_box)
        );
        final Spinner spinner = new Spinner(getActivity());
        layoutParamsSpin.gravity = Gravity.CENTER_VERTICAL;
        spinner.setLayoutParams(layoutParamsSpin);
        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setTag("spinner" + complaint_id);

        layoutParamsSpin.setMargins(0, 0, 0, 0);

        String actionPlanT = "0";
        int actionPlanId = 0;
        if (dmSurveyAnswerTableList1.size() != 0) {
            actionPlanT = dmSurveyAnswerTableList1.get(0).actionPlanText;
            try {
                actionPlanId = Integer.parseInt(dmSurveyAnswerTableList1.get(0).actionPlanId);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        int selectedPosition = 0;

        for (int i = 0; i < list.size(); i++) {
            try {
                if (actionPlanT.equalsIgnoreCase(list.get(i))) {
                    selectedPosition = i;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(actionPlanId == 1){
            selectedPosition = 1;
        }

        spinner.setSelection(selectedPosition);

        TextView tVActionPlanSelectedTxt = new TextView(getActivity());
        LinearLayout.LayoutParams layoutParamsActionTxt = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 15, 0, 15);

        tVActionPlanSelectedTxt.setLayoutParams(layoutParamsActionTxt);
        tVActionPlanSelectedTxt.setGravity(Gravity.CENTER);
        tVActionPlanSelectedTxt.setPadding(0, 15, 0, 15);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tVActionPlanSelectedTxt.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }
        tVActionPlanSelectedTxt.setVisibility(View.GONE);
        try {
            if (!actionPlanT.equalsIgnoreCase("0")) {
                tVActionPlanSelectedTxt.setText(actionPlanT);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }


        LinearLayout linearLayoutActionPlanMain = new LinearLayout(getActivity());
        linearLayoutActionPlanMain.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayoutActionPlanMain.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutActionPlan = new LinearLayout(getActivity());

        EditText actionPlan = new EditText(getActivity());
        actionPlan.setHint(getResources().getString(R.string.dm_edit_action_plan_details));
        actionPlan.setFilters(new InputFilter[] { new InputFilter.LengthFilter(2000) });
        actionPlan.setTextSize(14);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionPlan.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }
        if(actionPlanId == 1){
            actionPlan.setText(actionPlanT);
        }
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.actionplanbox));

        layoutParams1.setMargins(0, 15, 0, 15);
        actionPlan.setLayoutParams(layoutParams1);

        actionPlan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                db.followUpItemMainTableDao().updateActionPlanText(charSequence.toString(), id, Constants.FARMER_ID,Constants.ATTEMPT);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        linearLayoutActionPlan.setGravity(Gravity.END);
        linearLayoutActionPlan.setOrientation(LinearLayout.HORIZONTAL);

        ImageView imageView1 = new ImageView(getActivity());
        imageView1.setImageResource(R.drawable.green_tick);
        imageView1.setPadding(10, 10, 10, 10);

        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.text_button_width_height),
                getResources().getDimensionPixelSize(R.dimen.text_button_width_height));
        layoutParams3.setMargins(0, 10, 10, 10);

        imageView1.setLayoutParams(layoutParams3);

        ImageView imageView2 = new ImageView(getActivity());
        imageView2.setImageResource(R.drawable.red_cross);
        imageView2.setPadding(10, 10, 10, 10);

        imageView2.setLayoutParams(layoutParams3);

        linearLayoutActionPlan.addView(imageView1);
        linearLayoutActionPlan.addView(imageView2);

        linearLayoutActionPlanMain.addView(actionPlan);
        linearLayoutActionPlanMain.addView(linearLayoutActionPlan);

        linearLayoutActionPlanMain.setVisibility(View.GONE);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!actionPlan.getText().toString().isEmpty()){
                    actionPlan.setText("");
                    db.followUpItemMainTableDao().updateActionPlanText("", id, Constants.FARMER_ID, Constants.ATTEMPT);
                    db.followUpItemMainTableDao().updateActionPlanId("0",id,Constants.FARMER_ID, Constants.ATTEMPT);
                }
                actionPlan.setEnabled(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    actionPlan.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
                }
            }
        });
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!actionPlan.getText().toString().isEmpty()) {
                    db.followUpItemMainTableDao().updateActionPlanText(actionPlan.getText().toString(), id, Constants.FARMER_ID, Constants.ATTEMPT);
                    db.followUpItemMainTableDao().updateActionPlanId("1",id,Constants.FARMER_ID, Constants.ATTEMPT);
                }
                actionPlan.setEnabled(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    actionPlan.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
                }

            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long idS) {

                String Id = ids.get(position);
                if (position == 0) {
                    tVActionPlanSelectedTxt.setVisibility(View.GONE);
                    db.followUpItemMainTableDao().updateActionPlanText("0", id, Constants.FARMER_ID, Constants.ATTEMPT);
                    db.followUpItemMainTableDao().updateActionPlanId("0",id,Constants.FARMER_ID, Constants.ATTEMPT);
                } else if (list.get(position).equalsIgnoreCase(getResources().getString(R.string.dm_custom_action_plan))) {
                    db.followUpItemMainTableDao().updateActionPlanId(Id, id, Constants.FARMER_ID, Constants.ATTEMPT);
                    db.followUpItemMainTableDao().updateActionPlanText(actionPlan.getText().toString(), id, Constants.FARMER_ID, Constants.ATTEMPT);
                    tVActionPlanSelectedTxt.setVisibility(View.GONE);
                    linearLayoutActionPlanMain.setVisibility(View.VISIBLE);
                } else {
                    tVActionPlanSelectedTxt.setVisibility(View.VISIBLE);
                    tVActionPlanSelectedTxt.setText(list.get(position));
                    linearLayoutActionPlanMain.setVisibility(View.GONE);
                    db.followUpItemMainTableDao().updateActionPlanText(list.get(position), id, Constants.FARMER_ID, Constants.ATTEMPT);
                    db.followUpItemMainTableDao().updateActionPlanId(Id,id,Constants.FARMER_ID, Constants.ATTEMPT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        linearLayoutSpin.addView(spinner);

        if (linearLayoutItems != null) {
            linearLayoutItems.addView(notResolveText);
            linearLayoutItems.addView(editText);
            if (!dmSurveyAnswerTableList1.isEmpty()) {
                String resolved = dmSurveyAnswerTableList1.get(0).status;
                if(resolved != null) {
                    if (resolved.equalsIgnoreCase(Constants.RESOLVED)) {
                        notResolveText.setText(getResources().getString(R.string.dm_resolved));
                        notResolveText.setTextColor(getResources().getColor(R.color.colorPrimary));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            notResolveText.setBackground(getActivity().getDrawable(R.drawable.follow_up_resolved));
                        }
                    } else if (resolved.equalsIgnoreCase(Constants.NOTRESOLVED)) {
                        notResolveText.setText(getResources().getString(R.string.dm_resolved));
                        notResolveText.setTextColor(getResources().getColor(R.color.notResolved));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            notResolveText.setBackground(getActivity().getDrawable(R.drawable.follow_up_not_resolved));
                        }
                        boolean allowCaptureProof = checkAllowCaptureProof(id, ans, ansId, displayType);
                        if (allowCaptureProof) {
                            linearLayoutItems.addView(textView);
                            linearLayoutItems.addView(linearLayoutImage);
                            linearLayoutItems.addView(linearLayoutSpin);
                            linearLayoutItems.addView(linearLayoutActionPlanMain);
                        } else {
                            linearLayoutItems.addView(linearLayoutSpin);
                            linearLayoutItems.addView(linearLayoutActionPlanMain);
                        }
                    }
                }
            }

        }

        //linearLayoutItems.setVisibility(View.GONE);

        dm_question_followup_complaint.removeView(linearLayoutItems);
        if (dm_question_followup_complaint != null) {
            dm_question_followup_complaint.addView(linearLayoutItems);

        }

    }

    private boolean checkAllowCustomActionPlan(String questionId, String ans, int ansId, String displayType) {
        boolean allowCustomActionPlan = false;
        if(displayType.equalsIgnoreCase("text box")){
            if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size() != 0) {
                if (ans != null) {
                    String value = ans.trim();

                    for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++) {

                        try {
                            if (Objects.equals(value, "-")) {
                                allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                            }
                            else if (Objects.equals(value, "-" + value) && db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange ) {
                                allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                            }
                            else if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange) {
                                allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                            }
                        }catch (Exception e){e.printStackTrace();}
                    }

                }
            }
        }else{
            List<DmLookupValuesItem> dmLookupValuesISubmoduleList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(Integer.parseInt(questionId));
            for (int k = 0; k < dmLookupValuesISubmoduleList.size(); k++) {
                if(ans.equalsIgnoreCase(dmLookupValuesISubmoduleList.get(k).value)) {
                    allowCustomActionPlan = dmLookupValuesISubmoduleList.get(k).allowCustomActionPlan;
                }
            }
        }

        return allowCustomActionPlan;
    }

    private boolean checkAllowCaptureProof(String questionId, String ans, int ansId, String displayType) {
        boolean allowCaptureProof = false;
        if(displayType.equalsIgnoreCase("text box")){
            if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size() != 0) {
                if (ans != null) {
                    String value = ans.trim();

                    for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++) {

                        try {
                            if (Objects.equals(value, "-")) {
                                allowCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                            }
                            else if (Objects.equals(value, "-" + value) && db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange ) {
                                allowCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                            }
                            else if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange) {
                                allowCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                            }
                        }catch (Exception e){e.printStackTrace();}
                    }

                }
            }
        }else{
            List<DmLookupValuesItem> dmLookupValuesISubmoduleList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(Integer.parseInt(questionId));
            for (int k = 0; k < dmLookupValuesISubmoduleList.size(); k++) {
                if(ans.equalsIgnoreCase(dmLookupValuesISubmoduleList.get(k).value)) {
                    allowCaptureProof = dmLookupValuesISubmoduleList.get(k).isCaptureProof;
                }
            }
        }

        return allowCaptureProof;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int totalSizeUntilNow = db.dmComplaintFileTableDao().getTotalUnSyncFileSizeByQuestionId(String.valueOf(GALLERY_QUESTION_ID), Constants.FARMER_ID, "FollowUp", Constants.PLOT_ID);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 2;
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[16 * 1024];
            int questionId = GALLERY_QUESTION_ID;

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

            if (validateFileSize(imageBytes.length, totalSizeUntilNow)) {
                return;
            }

            DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
            dmComplaintFileTable.setFile_name(filename);
            dmComplaintFileTable.setSurveyType("FollowUp");
            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentTime = format.format(today);
            String OFISFileName = "Proof_" + Constants.MODULE_ID + "_" + Constants.FARMER_ID + "_" + currentTime + "." + Bitmap.CompressFormat.JPEG;
            dmComplaintFileTable.setOfisFileName(OFISFileName);
            dmComplaintFileTable.setFileData(imageBytes);
            dmComplaintFileTable.setFarmerId(Constants.FARMER_ID);
            dmComplaintFileTable.setPlotId(Constants.PLOT_ID);
            dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
            dmComplaintFileTable.setAttempt(Constants.ATTEMPT);
            dmComplaintFileTable.setFileSize(imageBytes.length);

            // Get an array of app-specific external storage directories
            File[] externalStorageDirs = getContext().getExternalFilesDirs(null);

            // The first element of the array will be the primary external storage directory
            File primaryExternalStorageDir = externalStorageDirs[0];
            File destinationFolder = new File(primaryExternalStorageDir, "OFISDM_PROOF");
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }
            File destinationFile = new File(destinationFolder, OFISFileName);

            try {
                FileOutputStream outputStream = new FileOutputStream(destinationFile);
                outputStream.write(imageBytes);
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String destinationFilePath = destinationFile.getAbsolutePath();
            dmComplaintFileTable.setFilePath(destinationFilePath);
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


            View desiredView = dm_question_followup_complaint.findViewWithTag("1234" + String.valueOf(questionId));
            LinearLayout linearLayoutImage = (LinearLayout) desiredView;


            List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
            dmComplaintFileTableList.clear();
            dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId, Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);
            if (dmComplaintFileTableList.size() != 0) {
                for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                    if(dmComplaintFileTableList.get(l).getSurveyType().equalsIgnoreCase("FollowUp")){
                        retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                    }


                    dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                    dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                }
            }

            dynamicArrays.put(dynamicArrayName, dynamicArray);
            dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

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
                        builder1.setMessage(getResources().getString(R.string.dm_do_you_want_to_remove_this_report));
                        builder1.setTitle(getResources().getString(R.string.Alert_txt));
                        builder1.setCancelable(false);
                        builder1.setPositiveButton(
                                getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                        //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                        linearLayoutImage.removeAllViews();
                                        String deleteData = db.dmComplaintFileTableDao().getFilePathToDelete(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT);
                                        deleteFile(deleteData);
                                        db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);

                                        dm_question_followup_complaint.removeAllViews();

                                        if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
                                            for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                                                displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
                                            }
                                        }

                                    }
                                });
                        builder1.setNegativeButton(
                                getResources().getString(R.string.no),
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

        //for both files
        if ((requestCode == GALLERY_PICK && resultCode == RESULT_OK) && data != null) {

            Uri uri = data.getData();
            ContentResolver cr = getActivity().getContentResolver();
            String mime = cr.getType(uri);
            String valueCode = String.valueOf(GALLERY_PICK);
            StringBuffer sb = new StringBuffer(valueCode);
            sb.deleteCharAt(sb.length() - 1);
            int questionId = GALLERY_QUESTION_ID;

            if (mime.equalsIgnoreCase("application/vnd.ms-excel")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    || mime.equalsIgnoreCase("application/msword") || mime.equalsIgnoreCase("text/plain")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    || mime.equalsIgnoreCase("text/csv")) {
                String filename = getFileName(uri);
                String fileExtension = getFileExtension(filename);


                byte[] fileByteArray;
                try {
                    fileByteArray = FileUtils.convertFileToByteArray(getActivity(), uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (validateFileSize(fileByteArray.length, totalSizeUntilNow)) {
                    return;
                }

                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setSurveyType("FollowUp");
                dmComplaintFileTable.setFileData(fileByteArray);
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String currentTime = format.format(today);
                String OFISFileName = "Proof_" + Constants.MODULE_ID + "_" + Constants.FARMER_ID + "_" + currentTime + "." + fileExtension;
                dmComplaintFileTable.setOfisFileName(OFISFileName);
                dmComplaintFileTable.setFarmerId(Constants.FARMER_ID);
                dmComplaintFileTable.setPlotId(Constants.PLOT_ID);
                dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
                dmComplaintFileTable.setAttempt(Constants.ATTEMPT);
                dmComplaintFileTable.setFileSize(fileByteArray.length);

                File[] externalStorageDirs = getContext().getExternalFilesDirs(null);

                // The first element of the array will be the primary external storage directory
                File primaryExternalStorageDir = externalStorageDirs[0];
                File destinationFolder = new File(primaryExternalStorageDir, "OFISDM_PROOF");
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                }

                File destinationFile = new File(destinationFolder, OFISFileName);
                try {
                    FileOutputStream outputStream = new FileOutputStream(destinationFile);
                    outputStream.write(fileByteArray);
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String destinationFilePath = destinationFile.getAbsolutePath();
                dmComplaintFileTable.setFilePath(destinationFilePath);
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

                View desiredView = dm_question_followup_complaint.findViewWithTag("1234" + String.valueOf(questionId));
                LinearLayout linearLayoutImage = (LinearLayout) desiredView;

                List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
                dmComplaintFileTableList.clear();
                dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId, Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);
                if (dmComplaintFileTableList.size() != 0) {
                    for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                        if(dmComplaintFileTableList.get(l).getSurveyType().equalsIgnoreCase("FollowUp")){
                            retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                            retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                        }

                        dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                    }
                }

                dynamicArrays.put(dynamicArrayName, dynamicArray);
                dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

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
                            builder1.setMessage(getResources().getString(R.string.dm_do_you_want_to_remove_this_report));
                            builder1.setTitle(getResources().getString(R.string.Alert_txt));
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    getResources().getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                            //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                            linearLayoutImage.removeAllViews();
                                            String deleteData = db.dmComplaintFileTableDao().getFilePathToDelete(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT);
                                            deleteFile(deleteData);
                                            db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);

                                            dm_question_followup_complaint.removeAllViews();

                                            if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
                                                for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                                                    displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
                                                }
                                            }

                                        }
                                    });
                            builder1.setNegativeButton(
                                    getResources().getString(R.string.no),
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
            else if (mime.equalsIgnoreCase("application/pdf")) {
                String filename = getFileName(uri);
                String fileExtension = getFileExtension(filename);

                Uri uri1 = Uri.fromFile(compressPdf(uri));
                byte[] pdfByteArray2;
                try {
                    pdfByteArray2 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (validateFileSize(pdfByteArray2.length, totalSizeUntilNow)) {
                    return;
                }

                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setSurveyType("FollowUp");
                dmComplaintFileTable.setFileData(pdfByteArray2);
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String currentTime = format.format(today);
                String OFISFileName = "Proof_" + Constants.MODULE_ID + "_" + Constants.FARMER_ID + "_" + currentTime + "." + fileExtension;
                dmComplaintFileTable.setOfisFileName(OFISFileName);
                dmComplaintFileTable.setFarmerId(Constants.FARMER_ID);
                dmComplaintFileTable.setPlotId(Constants.PLOT_ID);
                dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
                dmComplaintFileTable.setAttempt(Constants.ATTEMPT);
                dmComplaintFileTable.setFileSize(pdfByteArray2.length);

                // Get an array of app-specific external storage directories
                File[] externalStorageDirs = getContext().getExternalFilesDirs(null);

                // The first element of the array will be the primary external storage directory
                File primaryExternalStorageDir = externalStorageDirs[0];
                File destinationFolder = new File(primaryExternalStorageDir, "OFISDM_PROOF");
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                }
                File destinationFile = new File(destinationFolder, OFISFileName);

                try {
                    FileOutputStream outputStream = new FileOutputStream(destinationFile);
                    outputStream.write(pdfByteArray2);
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String destinationFilePath = destinationFile.getAbsolutePath();
                dmComplaintFileTable.setFilePath(destinationFilePath);
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

                View desiredView = dm_question_followup_complaint.findViewWithTag("1234" + String.valueOf(questionId));
                LinearLayout linearLayoutImage = (LinearLayout) desiredView;

                List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
                dmComplaintFileTableList.clear();
                dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId, Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);
                if (dmComplaintFileTableList.size() != 0) {
                    for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                        if(dmComplaintFileTableList.get(l).getSurveyType().equalsIgnoreCase("FollowUp")){
                            retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                            retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                        }
                        dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                    }
                }

                dynamicArrays.put(dynamicArrayName, dynamicArray);
                dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

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
                            builder1.setMessage(getResources().getString(R.string.dm_do_you_want_to_remove_this_report));
                            builder1.setTitle(getResources().getString(R.string.Alert_txt));
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    getResources().getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                            linearLayoutImage.removeAllViews();
                                            String deleteData = db.dmComplaintFileTableDao().getFilePathToDelete(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT);
                                            deleteFile(deleteData);
                                            db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);

                                            dm_question_followup_complaint.removeAllViews();

                                            if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
                                                for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                                                    displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
                                                }
                                            }
                                        }
                                    });
                            builder1.setNegativeButton(
                                    getResources().getString(R.string.no),
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
            else if (mime.equalsIgnoreCase("image/jpeg")
                    || mime.equalsIgnoreCase("image/png")){
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
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 750, 650, false);

                try {
                    resizedBitmapNew = ImageOrientation.modifyOrientation(getActivity(), resizedBitmap, uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                resizedBitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                if (validateFileSize(imageBytes.length, totalSizeUntilNow)) {
                    return;
                }

                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setSurveyType("FollowUp");
                dmComplaintFileTable.setFileData(imageBytes);
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String currentTime = format.format(today);
                String OFISFileName = "Proof_" + Constants.MODULE_ID + "_" + Constants.FARMER_ID + "_" + currentTime + "." + Bitmap.CompressFormat.JPEG;
                dmComplaintFileTable.setOfisFileName(OFISFileName);
                dmComplaintFileTable.setFarmerId(Constants.FARMER_ID);
                dmComplaintFileTable.setPlotId(Constants.PLOT_ID);
                dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
                dmComplaintFileTable.setAttempt(Constants.ATTEMPT);
                dmComplaintFileTable.setFileSize(imageBytes.length);

                // Get an array of app-specific external storage directories
                File[] externalStorageDirs = getContext().getExternalFilesDirs(null);

                // The first element of the array will be the primary external storage directory
                File primaryExternalStorageDir = externalStorageDirs[0];
                File destinationFolder = new File(primaryExternalStorageDir, "OFISDM_PROOF");
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                }

                File destinationFile = new File(destinationFolder, OFISFileName);
                try {
                    FileOutputStream outputStream = new FileOutputStream(destinationFile);
                    outputStream.write(imageBytes);
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String destinationFilePath = destinationFile.getAbsolutePath();
                dmComplaintFileTable.setFilePath(destinationFilePath);
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

                View desiredView = dm_question_followup_complaint.findViewWithTag("1234" + String.valueOf(questionId));
                LinearLayout linearLayoutImage = (LinearLayout) desiredView;

                List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
                dmComplaintFileTableList.clear();
                dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId, Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);
                if (dmComplaintFileTableList.size() != 0) {
                    for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                        if(dmComplaintFileTableList.get(l).getSurveyType().equalsIgnoreCase("FollowUp")){
                            retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                            retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                        }

                        dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                    }
                }

                dynamicArrays.put(dynamicArrayName, dynamicArray);
                dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

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
                            builder1.setMessage(getResources().getString(R.string.dm_do_you_want_to_remove_this_report));
                            builder1.setTitle(getResources().getString(R.string.Alert_txt));
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    getResources().getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                            //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                            linearLayoutImage.removeAllViews();
                                            String deleteData = db.dmComplaintFileTableDao().getFilePathToDelete(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT);
                                            deleteFile(deleteData);
                                            db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);

                                            dm_question_followup_complaint.removeAllViews();

                                            if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
                                                for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                                                    displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
                                                }
                                            }

                                        }
                                    });
                            builder1.setNegativeButton(
                                    getResources().getString(R.string.no),
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

        // for document
        if ((requestCode == GALLERY_PICK_DOCX && resultCode == RESULT_OK) && data != null) {
            Uri uri = data.getData();
            ContentResolver cr = getActivity().getContentResolver();
            String mime = cr.getType(uri);
            String valueCode = String.valueOf(GALLERY_PICK_DOCX);
            StringBuffer sb = new StringBuffer(valueCode);
            sb.deleteCharAt(sb.length() - 1);
            int questionId = GALLERY_QUESTION_ID;

            if (mime.equalsIgnoreCase("application/vnd.ms-excel")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    || mime.equalsIgnoreCase("application/msword") || mime.equalsIgnoreCase("text/plain")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    || mime.equalsIgnoreCase("text/csv")) {
                String filename = getFileName(uri);
                String fileExtension = getFileExtension(filename);

                byte[] fileByteArray;
                try {
                    fileByteArray = FileUtils.convertFileToByteArray(getActivity(), uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (validateFileSize(fileByteArray.length, totalSizeUntilNow)) {
                    return;
                }

                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setSurveyType("FollowUp");
                dmComplaintFileTable.setFileData(fileByteArray);
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String currentTime = format.format(today);
                String OFISFileName = "Proof_" + Constants.MODULE_ID + "_" + Constants.FARMER_ID + "_" + currentTime + "." + fileExtension;
                dmComplaintFileTable.setOfisFileName(OFISFileName);
                dmComplaintFileTable.setFarmerId(Constants.FARMER_ID);
                dmComplaintFileTable.setPlotId(Constants.PLOT_ID);
                dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
                dmComplaintFileTable.setAttempt(Constants.ATTEMPT);
                dmComplaintFileTable.setFileSize(fileByteArray.length);

                // Get an array of app-specific external storage directories
                File[] externalStorageDirs = getContext().getExternalFilesDirs(null);

                // The first element of the array will be the primary external storage directory
                File primaryExternalStorageDir = externalStorageDirs[0];
                File destinationFolder = new File(primaryExternalStorageDir, "OFISDM_PROOF");
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                }

                File destinationFile = new File(destinationFolder, OFISFileName);
                try {
                    FileOutputStream outputStream = new FileOutputStream(destinationFile);
                    outputStream.write(fileByteArray);
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String destinationFilePath = destinationFile.getAbsolutePath();
                dmComplaintFileTable.setFilePath(destinationFilePath);
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

                View desiredView = dm_question_followup_complaint.findViewWithTag("1234" + String.valueOf(questionId));
                LinearLayout linearLayoutImage = (LinearLayout) desiredView;

                List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
                dmComplaintFileTableList.clear();
                dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId, Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);
                if (dmComplaintFileTableList.size() != 0) {
                    for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                        if(dmComplaintFileTableList.get(l).getSurveyType().equalsIgnoreCase("FollowUp")){
                            retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                            retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                        }

                        dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                    }
                }

                dynamicArrays.put(dynamicArrayName, dynamicArray);
                dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

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
                            builder1.setMessage(getResources().getString(R.string.dm_do_you_want_to_remove_this_report));
                            builder1.setTitle(getResources().getString(R.string.Alert_txt));
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    getResources().getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                            //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                                            linearLayoutImage.removeAllViews();
                                            String deleteData = db.dmComplaintFileTableDao().getFilePathToDelete(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT);
                                            deleteFile(deleteData);
                                            db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);

                                            dm_question_followup_complaint.removeAllViews();

                                            if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
                                                for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                                                    displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
                                                }
                                            }

                                        }
                                    });
                            builder1.setNegativeButton(
                                    getResources().getString(R.string.no),
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
            else if (mime.equalsIgnoreCase("application/pdf")) {
                String filename = getFileName(uri);
                String fileExtension = getFileExtension(filename);

                Uri uri1 = Uri.fromFile(compressPdf(uri));
                byte[] pdfByteArray2;
                try {
                    pdfByteArray2 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (validateFileSize(pdfByteArray2.length, totalSizeUntilNow)) {
                    return;
                }

                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setSurveyType("FollowUp");
                dmComplaintFileTable.setFileData(pdfByteArray2);
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String currentTime = format.format(today);
                String OFISFileName = "Proof_" + Constants.MODULE_ID + "_" + Constants.FARMER_ID + "_" + currentTime + "." + fileExtension;
                dmComplaintFileTable.setOfisFileName(OFISFileName);
                dmComplaintFileTable.setFarmerId(Constants.FARMER_ID);
                dmComplaintFileTable.setPlotId(Constants.PLOT_ID);
                dmComplaintFileTable.setQuestionId(String.valueOf(questionId));
                dmComplaintFileTable.setAttempt(Constants.ATTEMPT);
                dmComplaintFileTable.setFileSize(pdfByteArray2.length);

                // Get an array of app-specific external storage directories
                File[] externalStorageDirs = getContext().getExternalFilesDirs(null);

                // The first element of the array will be the primary external storage directory
                File primaryExternalStorageDir = externalStorageDirs[0];
                File destinationFolder = new File(primaryExternalStorageDir, "OFISDM_PROOF");
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                }

                File destinationFile = new File(destinationFolder, OFISFileName);
                try {
                    FileOutputStream outputStream = new FileOutputStream(destinationFile);
                    outputStream.write(pdfByteArray2);
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String destinationFilePath = destinationFile.getAbsolutePath();
                dmComplaintFileTable.setFilePath(destinationFilePath);
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

                View desiredView = dm_question_followup_complaint.findViewWithTag("1234" + String.valueOf(questionId));
                LinearLayout linearLayoutImage = (LinearLayout) desiredView;

                List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
                dmComplaintFileTableList.clear();
                dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(questionId, Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);
                if (dmComplaintFileTableList.size() != 0) {
                    for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
                        if(dmComplaintFileTableList.get(l).getSurveyType().equalsIgnoreCase("FollowUp")){
                            retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
                            retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                        }
                        dynamicArray.add(dmComplaintFileTableList.get(l).getFile_name());
                        dynamicArrayFile.add(dmComplaintFileTableList.get(l).getUid());
                    }
                }

                dynamicArrays.put(dynamicArrayName, dynamicArray);
                dynamicArraysFile.put(dynamicArrayNameFile, dynamicArrayFile);

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
                            builder1.setMessage(getResources().getString(R.string.dm_do_you_want_to_remove_this_report));
                            builder1.setTitle(getResources().getString(R.string.Alert_txt));
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(
                                    getResources().getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                            linearLayoutImage.removeAllViews();
                                            String deleteData = db.dmComplaintFileTableDao().getFilePathToDelete(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT);
                                            deleteFile(deleteData);
                                            db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI), Constants.FARMER_ID, Constants.ATTEMPT, Constants.PLOT_ID);

                                            dm_question_followup_complaint.removeAllViews();

                                            if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
                                                for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                                                    displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
                                                }
                                            }
                                        }
                                    });
                            builder1.setNegativeButton(
                                    getResources().getString(R.string.no),
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
            else {
                Toast.makeText(getContext(), "Required Format:(.doc, .docx, .xls, .xlsx, .pdf, .csv)", Toast.LENGTH_SHORT).show();
            }


        }

    }

    public void showCurrencyDropDown(List<DmCurrency> dmCurrenciesItemList) {


        final List<String> list = new ArrayList<String>();
        list.clear();

        final List<String> ids = new ArrayList<String>();
        ids.clear();

        final List<String> isDefaultCurrency = new ArrayList<String>();
        isDefaultCurrency.clear();

        for (int c = 0; c < dmCurrenciesItemList.size(); c++) {
            int currencyId = dmCurrenciesItemList.get(c).currencyId;

            list.add(db.dmCurrenciesItemDao().getCurrencyCodeItemById(currencyId));
            ids.add(String.valueOf(dmCurrenciesItemList.get(c).currencyId));

        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_followup_dmCurrencies.setAdapter(spinnerAdapter);

        String currencyIdDb = "0";

        List<DmActionPlanCurrenciesTable> dmActionPlanCurrenciesTables = db.dmActionPlanCurrenciesTableDao().getDataByModuleId(Constants.MODULE_ID, Constants.FARMER_ID, Constants.PLOT_ID);

        if (dmActionPlanCurrenciesTables.size() != 0) {
            currencyIdDb = String.valueOf(dmActionPlanCurrenciesTables.get(0).currencyId);
            currency_id = currencyIdDb;
        }



        int selectedPosition = 0;

        try {
            int currencyDefault = db.dmCurrencyDao().isCurrencyDefault(dmActionPlanCurrenciesTables.get(0).moduleId , 1);
            for (int i = 0; i < ids.size(); i++) {
                if (String.valueOf(currencyDefault).equalsIgnoreCase(ids.get(i)) && isCreated) {
                    selectedPosition = i;
                    isCreated = false;
                } else if (currencyIdDb.equalsIgnoreCase(ids.get(i))) {
                    selectedPosition = i;
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        spinner_followup_dmCurrencies.setSelection(selectedPosition);

        spinner_followup_dmCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();

                String answerId = ids.get(position);

                currency_id = ids.get(position);

                db.dmActionPlanCurrenciesTableDao().deleteByModuleId(Constants.MODULE_ID, Constants.FARMER_ID, Constants.PLOT_ID);

                DmActionPlanCurrenciesTable dmActionPlanCurrenciesTable = new DmActionPlanCurrenciesTable();
                dmActionPlanCurrenciesTable.moduleId = Integer.parseInt(Constants.MODULE_ID);
                dmActionPlanCurrenciesTable.currencyId = Integer.parseInt(ids.get(position));
                dmActionPlanCurrenciesTable.code = list.get(position);
                dmActionPlanCurrenciesTable.farmerId = Constants.FARMER_ID;
                dmActionPlanCurrenciesTable.plotId = Constants.PLOT_ID;
                db.dmActionPlanCurrenciesTableDao().insert(dmActionPlanCurrenciesTable);

                dm_question_followup_complaint.removeAllViews();

                if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
                    for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                        displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
                    }
                }


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return  true;
            }else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.READ_PHONE_STATE,
                                android.Manifest.permission.READ_MEDIA_AUDIO,
                                android.Manifest.permission.READ_MEDIA_VIDEO,
                                android.Manifest.permission.READ_MEDIA_IMAGES,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.RECORD_AUDIO,
                                android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_GALLERYPERMISSION_CODE_TIRAMISU);
                return false;
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return  true;
            }else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.READ_PHONE_STATE,
                                android.Manifest.permission.READ_MEDIA_AUDIO,
                                android.Manifest.permission.READ_MEDIA_VIDEO,
                                android.Manifest.permission.READ_MEDIA_IMAGES,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.RECORD_AUDIO,
                                android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_CAMERA_PERMISSION_CODE_TIRAMISU);
                return false;
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        if (requestCode == MY_GALLERYPERMISSION_CODE_TIRAMISU){
            try {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[5] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[6] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[7] == PackageManager.PERMISSION_GRANTED ) {
                    // Permissions granted, proceed with your code
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && (!shouldShowRequestPermissionRationale(permissions[0])
                        || !shouldShowRequestPermissionRationale(permissions[1])
                        || !shouldShowRequestPermissionRationale(permissions[2])
                        || !shouldShowRequestPermissionRationale(permissions[3])
                        || !shouldShowRequestPermissionRationale(permissions[4])
                        || !shouldShowRequestPermissionRationale(permissions[5])
                        || !shouldShowRequestPermissionRationale(permissions[6])
                        || !shouldShowRequestPermissionRationale(permissions[7])
                )) {
                    showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));

                }else {
                    Toast.makeText(getContext(), R.string.permission_denial_msg_android_13_above, Toast.LENGTH_LONG).show();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == MY_CAMERA_PERMISSION_CODE_TIRAMISU){
            try {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[5] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[6] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[7] == PackageManager.PERMISSION_GRANTED ) {
                    // Permissions granted, proceed with your code
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && (!shouldShowRequestPermissionRationale(permissions[0])
                        || !shouldShowRequestPermissionRationale(permissions[1])
                        || !shouldShowRequestPermissionRationale(permissions[2])
                        || !shouldShowRequestPermissionRationale(permissions[3])
                        || !shouldShowRequestPermissionRationale(permissions[4])
                        || !shouldShowRequestPermissionRationale(permissions[5])
                        || !shouldShowRequestPermissionRationale(permissions[6])
                        || !shouldShowRequestPermissionRationale(permissions[7])
                )) {
                    showSettingsDialog(getResources().getString(R.string.go_to_settings_msg));

                }else {
                    Toast.makeText(getContext(), R.string.permission_denial_msg_android_13_above, Toast.LENGTH_LONG).show();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(PatientReasonActivity.this, R.string.camera_perm_granted, Toast.LENGTH_LONG).show();
                //openCamera();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.dm_please_give_camera_permission), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == MY_GALLERYPERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(PatientReasonActivity.this, R.string.cs, Toast.LENGTH_LONG).show();
                //openGallery();
            } else {
                Toast.makeText(getActivity(),getResources().getString(R.string.dm_please_give_gallery_permission), Toast.LENGTH_LONG).show();
            }
        }

    }

    public void showSettingsDialog(String msg) {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.permission_permanent_denial_title);
        dialog.setMessage(msg).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName())));
            }
        }).setNegativeButton(R.string.app_update_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Toast.makeText(getContext(), R.string.permission_permanent_denial_msg, Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
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
            //Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

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


    @Override
    public void onResume() {
        super.onResume();
        goNextCaptureProof = false;
        goNextActionPlan = true;
        db = OFISRoomDB.getDbInstance(getActivity());
        List<DmCurrency> dmSectionsItemList = db.dmCurrencyDao().getDMCurrencyByModuleId(Constants.MODULE_ID);
        showCurrencyDropDown(dmSectionsItemList);
        isCreated = false;

    }

    private String getFileExtension(String fileName) {
        String extension = null;
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            extension = fileName.substring(dotIndex + 1);
        }
        return extension;
    }

    private void deleteFile(String filePath) {
        File fileToDelete = new File(filePath);

        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                Toast.makeText(getContext(), "File deleted successfully.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean validateFileSize(int size, int totalSizeUntilNow) {

        if (size > FILE_SIZE) {
            Toast.makeText(getContext(), getString(R.string.file_exceeds) + errorSizeMessage, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (totalSizeUntilNow + size > FILE_SIZE) {
            String sizeInString = getFormattedFileSize(FILE_SIZE - totalSizeUntilNow);
            Toast.makeText(getContext(), getString(R.string.total_file_size) + sizeInString, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;

    }


}