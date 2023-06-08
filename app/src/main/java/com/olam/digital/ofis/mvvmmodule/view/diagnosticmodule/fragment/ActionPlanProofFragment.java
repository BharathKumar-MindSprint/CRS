package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import java.util.List;


public class ActionPlanProofFragment extends Fragment {

    private static final int REQ_CODE = 300;
    private final String MATOMOTAG = this.getClass().getSimpleName();
    LinearLayout ll_servey_back_actionplan, ll_servey_next_actionplan, ll_complaint;
    ImageView iv_complaint_dropdown, iv_complaint_delete_file;
    TextView tv_complaint_filename;
    RelativeLayout rl_complaint_captureproof_layout, rl_complaint_fileupload_layout;
    Spinner spinner_complaint_actionplan;
    ConstraintLayout complaint_custom_actionplanlayout;
    ImageView iv_delete_custom_complaint, iv_save_custom_complaint;
    EditText complaiant_custom_text;

    //Non-Complaint
    ImageView iv_noncomplaint_dropdown;
    LinearLayout ll_noncomplaint, ll_image_noncomplaint_layout;
    RelativeLayout rl_noncomplaint_captureproof_layout;
    ConstraintLayout noncomplaint_custom_actionplanlayout;
    ImageView iv_delete_custom_noncomplaint, iv_save_custom_noncomplaint;
    EditText noncomplaiant_custom_text;


    //Satisfactory

    ImageView iv_satisfactory_dropdown;
    LinearLayout ll_satisfactory, ll_image_satisfactory_layout;
    RelativeLayout rl_satisfactory_captureproof_layout;

    ConstraintLayout satisfactory_custom_actionplanlayout;

    ImageView iv_delete_custom_satisfactory, iv_save_custom_satisfactory;

    EditText satisfactory_custom_text;


    public Resources res;
    private Uri mCapturedImageURI = null, mCapturedSignatureImageURI = null;
    private final static int MY_PROFILE_PIC_PERMISSIONS_REQUEST = 9998;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_IMAGE_GALLERY = 2;

    private static final int MY_GALLERYPERMISSION_CODE = 100;
    private static final int MY_CAMERA_PERMISSION_CODE = 500;
    private static final int CAMERA_REQUEST = 1;
    private static final int CAMERA_REQUEST_NONCOMPLAINT = 2;
    private static final int CAMERA_REQUEST_SATISFACTORY = 3;
    private static final int GALLERY_PICK = 4;
    private static final int GALLERY_PICK_NONCOMPLAINT = 5;
    private static final int GALLERY_PICK_SATISFACTORY = 6;
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private String mCurrentPhotoPath;

    private Bitmap resizedBitmapNew;
    LinearLayout image_layout;
    ArrayList<String> image_array = new ArrayList<>();
    ArrayList<Long> file_array_ids = new ArrayList<>();

    ArrayList<String> nonComplaint_image_array = new ArrayList<>();
    ArrayList<Long> nonComplaint_file_array_ids = new ArrayList<>();

    ArrayList<String> satisfactory_image_array = new ArrayList<>();
    ArrayList<Long> satisfactory_file_array_ids = new ArrayList<>();

    int complaint_id, noncomplaint_id, satisfactory_id;

    LinearLayout dm_question_complaint, dm_question_noncomplaint, getDm_question_satisfactory;
    private static final String[] ACCEPTED_MIME_TYPES = {"image/jpeg", "image/png", "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
            "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/csv"};

    //Room DB
    OFISRoomDB db;
    Spinner spinner_dmCurrencies;

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
        dm_question_noncomplaint = view.findViewById(R.id.dm_question_noncomplaint);
        getDm_question_satisfactory = view.findViewById(R.id.getDm_question_satisfactory);
        ll_servey_next_actionplan = view.findViewById(R.id.ll_servey_next_actionplan);
        iv_complaint_dropdown = view.findViewById(R.id.iv_complaint_dropdown);
        ll_complaint = view.findViewById(R.id.ll_complaint);
        rl_complaint_captureproof_layout = view.findViewById(R.id.rl_complaint_captureproof_layout);
        tv_complaint_filename = view.findViewById(R.id.tv_complaint_filename);
        iv_complaint_delete_file = view.findViewById(R.id.iv_complaint_delete_file);
        rl_complaint_fileupload_layout = view.findViewById(R.id.rl_complaint_fileupload_layout);
        image_layout = view.findViewById(R.id.image_layout);
        ll_image_noncomplaint_layout = view.findViewById(R.id.ll_image_noncomplaint_layout);
        ll_image_satisfactory_layout = view.findViewById(R.id.ll_image_satisfactory_layout);
        spinner_dmCurrencies = view.findViewById(R.id.spinner_dmCurrencies);
        spinner_complaint_actionplan = view.findViewById(R.id.spinner_complaint_actionplan);

        //Custom Action Plan
        complaint_custom_actionplanlayout = view.findViewById(R.id.complaint_custom_actionplanlayout);
        iv_delete_custom_complaint = view.findViewById(R.id.iv_delete_custom_complaint);
        iv_save_custom_complaint = view.findViewById(R.id.iv_save_custom_complaint);
        complaiant_custom_text = view.findViewById(R.id.complaiant_custom_text);

        iv_complaint_dropdown.setTag("0");

        //Room DB
        db = OFISRoomDB.getDbInstance(getActivity());
        List<DmCurrenciesItem> dmSectionsItemList = db.dmCurrenciesItemDao().getDmCurrenciesItem();
        showCurrencyDropDown(dmSectionsItemList);

        List<DmActionPlansItem> dmActionPlansItemList = db.dmActionPlansItemDao().getDmActionPlansItem();
        showActionplanDropDown(dmActionPlansItemList);
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
        noncomplaint_custom_actionplanlayout = view.findViewById(R.id.noncomplaint_custom_actionplanlayout);


        //Custom Action Plan
        noncomplaint_custom_actionplanlayout = view.findViewById(R.id.noncomplaint_custom_actionplanlayout);
        iv_delete_custom_noncomplaint = view.findViewById(R.id.iv_delete_custom_noncomplaint);
        iv_save_custom_noncomplaint = view.findViewById(R.id.iv_save_custom_noncomplaint);
        noncomplaiant_custom_text = view.findViewById(R.id.noncomplaiant_custom_text);


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

        rl_noncomplaint_captureproof_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageNonComplaint();

            }
        });

        //Satisfactory
        iv_satisfactory_dropdown = view.findViewById(R.id.iv_satisfactory_dropdown);
        ll_satisfactory = view.findViewById(R.id.ll_satisfactory);
        rl_satisfactory_captureproof_layout = view.findViewById(R.id.rl_satisfactory_captureproof_layout);

        //Custom Action Plan
        satisfactory_custom_actionplanlayout = view.findViewById(R.id.satisfactory_custom_actionplanlayout);
        iv_delete_custom_satisfactory = view.findViewById(R.id.iv_delete_custom_satisfactory);
        iv_save_custom_satisfactory = view.findViewById(R.id.iv_save_custom_satisfactory);
        satisfactory_custom_text = view.findViewById(R.id.satisfactory_custom_text);

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

        rl_satisfactory_captureproof_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageSatisfactory();

            }
        });

        //

        List<DmComplaintFileTable> dmComplaintFileTableList = db.dmComplaintFileTableDao().getDmComplaintFileTable();
        if (dmComplaintFileTableList.size() != 0) {
            for (int i = 0; i < dmComplaintFileTableList.size(); i++) {
                image_array.add(dmComplaintFileTableList.get(i).getFile_name());
                file_array_ids.add(dmComplaintFileTableList.get(i).getUid());
            }

            showImages();
        }

        List<DmNonComplaintFileTable> dmNonComplaintFileTableList = db.dmNonComplaintFileTableDao().getDmNonComplaintFileTable();
        if (dmNonComplaintFileTableList.size() != 0) {
            for (int i = 0; i < dmNonComplaintFileTableList.size(); i++) {
                nonComplaint_image_array.add(dmNonComplaintFileTableList.get(i).getFile_name());
                nonComplaint_file_array_ids.add(dmNonComplaintFileTableList.get(i).getUid());
            }

            showImagesNonComplaint();
        }

        List<DmSatisfactoryFileTable> dmSatisfactoryFileTableList = db.dmSatisfactoryFileTableDao().getDmSatisfactoryFileTable();
        if (dmSatisfactoryFileTableList.size() != 0) {
            for (int i = 0; i < dmSatisfactoryFileTableList.size(); i++) {
                satisfactory_image_array.add(dmSatisfactoryFileTableList.get(i).getFile_name());
                satisfactory_file_array_ids.add(dmSatisfactoryFileTableList.get(i).getUid());
            }

            showImagesSatisfactory();
        }


        if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
            for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).compliance.equalsIgnoreCase("Compliant")) {
                    complaint_id = db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id;
                    displayQuestionsModuleComplaint(String.valueOf(complaint_id));
                } else if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).compliance.equalsIgnoreCase("Satisfactory")) {
                    satisfactory_id = db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id;
                    displayQuestionsModuleNonComplaint(String.valueOf(satisfactory_id));
                } else if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).compliance.equalsIgnoreCase("Non Compliant")) {
                    noncomplaint_id = db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id;
                    displayQuestionsModuleSatisfactory(String.valueOf(noncomplaint_id));
                }
            }

        }


        return view;
    }


    private void displayQuestionsModuleComplaint(String complaint_id) {

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceId(complaint_id);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;

            for (int m = 0; m < dmSurveyAnswerTableList.size(); m++) {
                DmSurveyAnswerTable dmQuestionsModule = dmSurveyAnswerTableList.get(m);
                TextView textView = new TextView(getActivity());
                textView.setText((m + 1) + "." + dmQuestionsModule.question);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setTextSize(14);
                textView.setPadding(0, 15, 0, 0);
                //textView.setTypeface(null, Typeface.BOLD);
                dm_question_complaint.addView(textView);

                renderTextboxQuestionModuleComplaint(String.valueOf(complaint_id));

            }
        }



    }

    private void displayQuestionsModuleNonComplaint(String noncomplaint_id) {

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceId(noncomplaint_id);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;

            for (int m = 0; m < dmSurveyAnswerTableList.size(); m++) {
                DmSurveyAnswerTable dmQuestionsModule = dmSurveyAnswerTableList.get(m);
                TextView textView = new TextView(getActivity());
                textView.setText((m + 1) + "." + dmQuestionsModule.question);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setTextSize(14);
                textView.setPadding(0, 15, 0, 0);
                //textView.setTypeface(null, Typeface.BOLD);
                dm_question_noncomplaint.addView(textView);

                renderTextboxQuestionModuleNonComplaint(String.valueOf(complaint_id));

            }
        }

    }

    private void displayQuestionsModuleSatisfactory(String satisfactory_id) {
        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceId(satisfactory_id);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;

            for (int m = 0; m < dmSurveyAnswerTableList.size(); m++) {
                DmSurveyAnswerTable dmQuestionsModule = dmSurveyAnswerTableList.get(m);
                TextView textView = new TextView(getActivity());
                textView.setText((m + 1) + "." + dmQuestionsModule.question);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setTextSize(14);
                textView.setPadding(0, 15, 0, 0);
                //textView.setTypeface(null, Typeface.BOLD);
                dm_question_noncomplaint.addView(textView);

                renderTextboxQuestionModuleSatisfactory(String.valueOf(complaint_id));

            }
        }

    }

    public void renderTextboxQuestionModuleComplaint(String id) {

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setEnabled(false);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(id);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
            editText.setText(answerIdDb);
        }

        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        // Add Spinner to LinearLayout
        if (dm_question_complaint != null) {
            dm_question_complaint.addView(editText);
        }

    }

    public void renderTextboxQuestionModuleNonComplaint(String id) {

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setEnabled(false);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceId(id);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
            editText.setText(answerIdDb);
        }

        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        // Add Spinner to LinearLayout
        if (dm_question_noncomplaint != null) {
            dm_question_noncomplaint.addView(editText);
        }

    }

    public void renderTextboxQuestionModuleSatisfactory(String id) {

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setEnabled(false);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(id);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
            editText.setText(answerIdDb);
        }

        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        // Add Spinner to LinearLayout
        if (getDm_question_satisfactory != null) {
            getDm_question_satisfactory.addView(editText);
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
                Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();

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

    public void showActionplanDropDown(List<DmActionPlansItem> dmActionPlansItemList) {


        final List<String> list = new ArrayList<String>();
        list.clear();

        for (int c = 0; c < dmActionPlansItemList.size(); c++) {
            list.add(dmActionPlansItemList.get(c).actionPlan);
        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_complaint_actionplan.setAdapter(spinnerAdapter);

        spinner_complaint_actionplan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


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

    private void selectImageNonComplaint() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (isStoragePermissionCameraGranted()) {
                        if (nonComplaint_image_array.size() > 5) {
                            Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
                        } else {
                            openCameraNonComplaint();
                        }

                    }

                } else if (options[item].equals("Choose from Gallery")) {

                    if (isStoragePermissionGalleryGranted()) {
                        if (nonComplaint_image_array.size() > 5) {
                            Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
                        } else {
                            openGalleryNoncomplaint();
                        }

                    }
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void selectImageSatisfactory() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (isStoragePermissionCameraGranted()) {
                        if (satisfactory_image_array.size() > 5) {
                            Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
                        } else {
                            openCameraSatisfactory();
                        }

                    }

                } else if (options[item].equals("Choose from Gallery")) {

                    if (isStoragePermissionGalleryGranted()) {
                        if (satisfactory_image_array.size() > 5) {
                            Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
                        } else {
                            openGallerySatisfactory();
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

    private void openCameraNonComplaint() {
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
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_NONCOMPLAINT);
            }
        }
    }

    private void openCameraSatisfactory() {
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
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_SATISFACTORY);
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

    private void openGalleryNoncomplaint() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        //galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES);
        galleryIntent.setType("*/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK_NONCOMPLAINT);
    }

    private void openGallerySatisfactory() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        //galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES);
        galleryIntent.setType("*/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK_SATISFACTORY);
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
            String filename = mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/") + 1);
            try {
                resizedBitmapNew = ImageOrientation.modifyOrientation(getActivity(), resizedBitmap, uriImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            resizedBitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();

            DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
            dmComplaintFileTable.setFile_name(filename);
            dmComplaintFileTable.setFileData(imageBytes);
            db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

            long id = db.dmComplaintFileTableDao().getLastInsertedId();

            file_array_ids.add(id);
            // profile_pic = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            uploadFileImage(filename, imageBytes, resizedBitmapNew);

        } else if (requestCode == CAMERA_REQUEST_NONCOMPLAINT && resultCode == RESULT_OK) {
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

            DmNonComplaintFileTable dmNonComplaintFileTable = new DmNonComplaintFileTable();
            dmNonComplaintFileTable.setFile_name(filename);
            dmNonComplaintFileTable.setFileData(imageBytes);
            db.dmNonComplaintFileTableDao().insert(dmNonComplaintFileTable);

            long id = db.dmNonComplaintFileTableDao().getLastInsertedId();

            nonComplaint_file_array_ids.add(id);
            // profile_pic = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            uploadFileImageNonComplaint(filename, imageBytes, resizedBitmapNew);
        } else if (requestCode == CAMERA_REQUEST_SATISFACTORY && resultCode == RESULT_OK) {
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

            DmSatisfactoryFileTable dmSatisfactoryFileTable = new DmSatisfactoryFileTable();
            dmSatisfactoryFileTable.setFile_name(filename);
            dmSatisfactoryFileTable.setFileData(imageBytes);
            db.dmSatisfactoryFileTableDao().insert(dmSatisfactoryFileTable);

            long id = db.dmSatisfactoryFileTableDao().getLastInsertedId();

            satisfactory_file_array_ids.add(id);
            // profile_pic = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            uploadFileImageSatisfactory(filename, imageBytes, resizedBitmapNew);
        }


        // if i want just add photo profile //
        if ((requestCode == GALLERY_PICK && resultCode == RESULT_OK) && data != null) {

            Uri uri = data.getData();
            ContentResolver cr = getActivity().getContentResolver();
            String mime = cr.getType(uri);
            Toast.makeText(getActivity(), "" + mime, Toast.LENGTH_SHORT).show();
            //setImage(iv_profile, imageUri);

            if (mime.equalsIgnoreCase("application/vnd.ms-excel")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    || mime.equalsIgnoreCase("application/msword") || mime.equalsIgnoreCase("text/plain")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    || mime.equalsIgnoreCase("text/csv")) {
                String filename = getFileName(uri);

                //Toast.makeText(getActivity(), "" + filename + "....." + uri + "..." + uri.getPath(), Toast.LENGTH_LONG).show();

                byte[] fileByteArray;
                try {
                    fileByteArray = FileUtils.convertFileToByteArray(getActivity(), uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setFileData(fileByteArray);
                db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

                long id = db.dmComplaintFileTableDao().getLastInsertedId();

                file_array_ids.add(id);

                uploadFile(uri, filename, fileByteArray);

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
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();


                if ((1024f * 1024f) < compressedSize) {
                    Toast.makeText(getContext(), "Sizer is greater than 10 MB", Toast.LENGTH_SHORT).show();
                    Uri uri1 = Uri.fromFile(compressPdf(uri));
                    byte[] pdfByteArray1;
                    try {
                        pdfByteArray1 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                    dmComplaintFileTable.setFile_name(filename);
                    dmComplaintFileTable.setFileData(pdfByteArray1);
                    db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

                    long id = db.dmComplaintFileTableDao().getLastInsertedId();

                    file_array_ids.add(id);
                    uploadFile(uri1, filename, pdfByteArray1);
                } else {
                    Uri uri1 = Uri.fromFile(compressPdf(uri));

                    byte[] pdfByteArray2;
                    try {
                        pdfByteArray2 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                    dmComplaintFileTable.setFile_name(filename);
                    dmComplaintFileTable.setFileData(pdfByteArray2);
                    db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

                    long id = db.dmComplaintFileTableDao().getLastInsertedId();

                    file_array_ids.add(id);

                    uploadFile(uri1, filename, pdfByteArray2);
                }

            } else {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inSampleSize = 2;
                options.inJustDecodeBounds = false;
                options.inTempStorage = new byte[16 * 1024];

                String filePathimage = null;

                //Toast.makeText(getActivity(), ""+filePathimage+"..."+uri+"....."+uri.getAuthority(), Toast.LENGTH_LONG).show();

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
                //profile_pic = Base64.encodeToString(imageBytes, Base64.DEFAULT);

//                Toast.makeText(getContext(),imageBytes.length, Toast.LENGTH_SHORT).show();

                DmComplaintFileTable dmComplaintFileTable = new DmComplaintFileTable();
                dmComplaintFileTable.setFile_name(filename);
                dmComplaintFileTable.setFileData(imageBytes);
                db.dmComplaintFileTableDao().insert(dmComplaintFileTable);

                long id = db.dmComplaintFileTableDao().getLastInsertedId();

                file_array_ids.add(id);


                uploadFileImage(filename, imageBytes, resizedBitmapNew);
            }

        }

        if ((requestCode == GALLERY_PICK_NONCOMPLAINT && resultCode == RESULT_OK) && data != null) {

            Uri uri = data.getData();
            ContentResolver cr = getActivity().getContentResolver();
            String mime = cr.getType(uri);
            Toast.makeText(getActivity(), "" + mime, Toast.LENGTH_SHORT).show();
            //setImage(iv_profile, imageUri);

            if (mime.equalsIgnoreCase("application/vnd.ms-excel")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    || mime.equalsIgnoreCase("application/msword") || mime.equalsIgnoreCase("text/plain")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    || mime.equalsIgnoreCase("text/csv")) {
                String filename = getFileName(uri);

                //Toast.makeText(getActivity(), "" + filename + "....." + uri + "..." + uri.getPath(), Toast.LENGTH_LONG).show();

                byte[] fileByteArray;
                try {
                    fileByteArray = FileUtils.convertFileToByteArray(getActivity(), uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                DmNonComplaintFileTable dmNonComplaintFileTable = new DmNonComplaintFileTable();
                dmNonComplaintFileTable.setFile_name(filename);
                dmNonComplaintFileTable.setFileData(fileByteArray);
                db.dmNonComplaintFileTableDao().insert(dmNonComplaintFileTable);

                long id = db.dmNonComplaintFileTableDao().getLastInsertedId();

                nonComplaint_file_array_ids.add(id);

                uploadFileNonComplaint(uri, filename, fileByteArray);

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
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();


                if ((1024f * 1024f) < compressedSize) {
                    Toast.makeText(getContext(), "Sizer is greater than 10 MB", Toast.LENGTH_SHORT).show();
                    Uri uri1 = Uri.fromFile(compressPdf(uri));
                    byte[] pdfByteArray1;
                    try {
                        pdfByteArray1 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    DmNonComplaintFileTable dmNonComplaintFileTable = new DmNonComplaintFileTable();
                    dmNonComplaintFileTable.setFile_name(filename);
                    dmNonComplaintFileTable.setFileData(pdfByteArray1);
                    db.dmNonComplaintFileTableDao().insert(dmNonComplaintFileTable);

                    long id = db.dmNonComplaintFileTableDao().getLastInsertedId();

                    nonComplaint_file_array_ids.add(id);
                    uploadFileNonComplaint(uri1, filename, pdfByteArray1);
                } else {
                    Uri uri1 = Uri.fromFile(compressPdf(uri));

                    byte[] pdfByteArray2;
                    try {
                        pdfByteArray2 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    DmNonComplaintFileTable dmNonComplaintFileTable = new DmNonComplaintFileTable();
                    dmNonComplaintFileTable.setFile_name(filename);
                    dmNonComplaintFileTable.setFileData(pdfByteArray2);
                    db.dmNonComplaintFileTableDao().insert(dmNonComplaintFileTable);

                    long id = db.dmNonComplaintFileTableDao().getLastInsertedId();

                    nonComplaint_file_array_ids.add(id);
                    uploadFileNonComplaint(uri1, filename, pdfByteArray2);
                }

            } else {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inSampleSize = 2;
                options.inJustDecodeBounds = false;
                options.inTempStorage = new byte[16 * 1024];

                String filePathimage = null;

                //Toast.makeText(getActivity(), ""+filePathimage+"..."+uri+"....."+uri.getAuthority(), Toast.LENGTH_LONG).show();

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
                //profile_pic = Base64.encodeToString(imageBytes, Base64.DEFAULT);

//                Toast.makeText(getContext(),imageBytes.length, Toast.LENGTH_SHORT).show();

                uploadFileImage(filename, imageBytes, resizedBitmapNew);

                DmNonComplaintFileTable dmNonComplaintFileTable = new DmNonComplaintFileTable();
                dmNonComplaintFileTable.setFile_name(filename);
                dmNonComplaintFileTable.setFileData(imageBytes);
                db.dmNonComplaintFileTableDao().insert(dmNonComplaintFileTable);

                long id = db.dmNonComplaintFileTableDao().getLastInsertedId();

                nonComplaint_file_array_ids.add(id);
                uploadFileImageNonComplaint(filename, imageBytes, resizedBitmapNew);
            }

        }

        if ((requestCode == GALLERY_PICK_SATISFACTORY && resultCode == RESULT_OK) && data != null) {

            Uri uri = data.getData();
            ContentResolver cr = getActivity().getContentResolver();
            String mime = cr.getType(uri);
            Toast.makeText(getActivity(), "" + mime, Toast.LENGTH_SHORT).show();
            //setImage(iv_profile, imageUri);

            if (mime.equalsIgnoreCase("application/vnd.ms-excel")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    || mime.equalsIgnoreCase("application/msword") || mime.equalsIgnoreCase("text/plain")
                    || mime.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    || mime.equalsIgnoreCase("text/csv")) {
                String filename = getFileName(uri);

                //Toast.makeText(getActivity(), "" + filename + "....." + uri + "..." + uri.getPath(), Toast.LENGTH_LONG).show();

                byte[] fileByteArray;
                try {
                    fileByteArray = FileUtils.convertFileToByteArray(getActivity(), uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                DmSatisfactoryFileTable dmSatisfactoryFileTable = new DmSatisfactoryFileTable();
                dmSatisfactoryFileTable.setFile_name(filename);
                dmSatisfactoryFileTable.setFileData(fileByteArray);
                db.dmSatisfactoryFileTableDao().insert(dmSatisfactoryFileTable);

                long id = db.dmSatisfactoryFileTableDao().getLastInsertedId();

                satisfactory_file_array_ids.add(id);

                uploadFileSatisfactory(uri, filename, fileByteArray);

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
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();


                if ((1024f * 1024f) < compressedSize) {
                    Toast.makeText(getContext(), "Sizer is greater than 10 MB", Toast.LENGTH_SHORT).show();
                    Uri uri1 = Uri.fromFile(compressPdf(uri));
                    byte[] pdfByteArray1;
                    try {
                        pdfByteArray1 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    DmSatisfactoryFileTable dmSatisfactoryFileTable = new DmSatisfactoryFileTable();
                    dmSatisfactoryFileTable.setFile_name(filename);
                    dmSatisfactoryFileTable.setFileData(pdfByteArray1);
                    db.dmSatisfactoryFileTableDao().insert(dmSatisfactoryFileTable);
                    long id = db.dmSatisfactoryFileTableDao().getLastInsertedId();

                    satisfactory_file_array_ids.add(id);
                    uploadFileSatisfactory(uri1, filename, pdfByteArray1);
                } else {
                    Uri uri1 = Uri.fromFile(compressPdf(uri));

                    byte[] pdfByteArray2;
                    try {
                        pdfByteArray2 = PDFUtils.convertPDFToByteArray(uri1.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    DmSatisfactoryFileTable dmSatisfactoryFileTable = new DmSatisfactoryFileTable();
                    dmSatisfactoryFileTable.setFile_name(filename);
                    dmSatisfactoryFileTable.setFileData(pdfByteArray2);
                    db.dmSatisfactoryFileTableDao().insert(dmSatisfactoryFileTable);
                    long id = db.dmSatisfactoryFileTableDao().getLastInsertedId();

                    satisfactory_file_array_ids.add(id);

                    uploadFileSatisfactory(uri1, filename, pdfByteArray2);
                }

            } else {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inSampleSize = 2;
                options.inJustDecodeBounds = false;
                options.inTempStorage = new byte[16 * 1024];

                String filePathimage = null;

                //Toast.makeText(getActivity(), ""+filePathimage+"..."+uri+"....."+uri.getAuthority(), Toast.LENGTH_LONG).show();

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
                //profile_pic = Base64.encodeToString(imageBytes, Base64.DEFAULT);

//                Toast.makeText(getContext(),imageBytes.length, Toast.LENGTH_SHORT).show();

                DmSatisfactoryFileTable dmSatisfactoryFileTable = new DmSatisfactoryFileTable();
                dmSatisfactoryFileTable.setFile_name(filename);
                dmSatisfactoryFileTable.setFileData(imageBytes);
                db.dmSatisfactoryFileTableDao().insert(dmSatisfactoryFileTable);
                long id = db.dmSatisfactoryFileTableDao().getLastInsertedId();

                satisfactory_file_array_ids.add(id);


                uploadFileImageSatisfactory(filename, imageBytes, resizedBitmapNew);
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

    private void uploadFileImage(String filename, byte[] filePath, final Bitmap image) {
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

    private void uploadFileImageNonComplaint(String filename, byte[] filePath, final Bitmap image) {
        //checking if file is available
        ProgressDialog progressDialog;
        try {
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setCancelable(false);
//            progressDialog.setTitle("Please Wait...");
//            progressDialog.show();
            nonComplaint_image_array.add(filename);

            showImagesNonComplaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadFileImageSatisfactory(String filename, byte[] filePath, final Bitmap image) {
        //checking if file is available
        ProgressDialog progressDialog;
        try {
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setCancelable(false);
//            progressDialog.setTitle("Please Wait...");
//            progressDialog.show();
            satisfactory_image_array.add(filename);

            showImagesSatisfactory();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadFile(Uri uri, String fileName, byte[] filedata) {
        try {
            image_array.add(fileName);
            showImages();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void uploadFileNonComplaint(Uri uri, String fileName, byte[] filedata) {
        try {
            nonComplaint_image_array.add(fileName);
            showImagesNonComplaint();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void uploadFileSatisfactory(Uri uri, String fileName, byte[] filedata) {
        try {
            satisfactory_image_array.add(fileName);
            showImagesSatisfactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public void showImagesNonComplaint() {
        ll_image_noncomplaint_layout.removeAllViews();

        for (int i = 0; i < nonComplaint_image_array.size(); i++) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
            final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

            ll_image_noncomplaint_layout.addView(convertView);
            convertView.setTag(i);
            remove.setTag(i);
            selected_imgName.setText(nonComplaint_image_array.get(i));


            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialogNonComplaint(v.getTag().toString());

                }
            });


        }
    }

    public void showImagesSatisfactory() {
        ll_image_satisfactory_layout.removeAllViews();

        for (int i = 0; i < satisfactory_image_array.size(); i++) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
            final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);

            ll_image_satisfactory_layout.addView(convertView);
            convertView.setTag(i);
            remove.setTag(i);
            selected_imgName.setText(satisfactory_image_array.get(i));


            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialogSatisfactory(v.getTag().toString());

                }
            });


        }
    }

    private void showDeleteDialog(String pos) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Do you want to remove this report");
        builder1.setTitle("Alert");
        builder1.setCancelable(false);
        builder1.setPositiveButton(
                "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                        db.dmComplaintFileTableDao().deleteById(file_array_ids.get(Integer.valueOf(pos)));

                        image_array.clear();
                        file_array_ids.clear();

                        List<DmComplaintFileTable> dmComplaintFileTableList = db.dmComplaintFileTableDao().getDmComplaintFileTable();
                        if (dmComplaintFileTableList.size() != 0) {
                            for (int i = 0; i < dmComplaintFileTableList.size(); i++) {
                                image_array.add(dmComplaintFileTableList.get(i).getFile_name());
                                file_array_ids.add(dmComplaintFileTableList.get(i).getUid());
                            }
                        }
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

    private void showDeleteDialogNonComplaint(String pos) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Do you want to remove this report");
        builder1.setTitle("Alert");
        builder1.setCancelable(false);
        builder1.setPositiveButton(
                "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                        db.dmNonComplaintFileTableDao().deleteById(nonComplaint_file_array_ids.get(Integer.valueOf(pos)));

                        nonComplaint_image_array.clear();
                        nonComplaint_file_array_ids.clear();

                        List<DmNonComplaintFileTable> dmNonComplaintFileTables = db.dmNonComplaintFileTableDao().getDmNonComplaintFileTable();
                        if (dmNonComplaintFileTables.size() != 0) {
                            for (int i = 0; i < dmNonComplaintFileTables.size(); i++) {
                                nonComplaint_image_array.add(dmNonComplaintFileTables.get(i).getFile_name());
                                nonComplaint_file_array_ids.add(dmNonComplaintFileTables.get(i).getUid());
                            }
                        }
                        showImagesNonComplaint();

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

    private void showDeleteDialogSatisfactory(String pos) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Do you want to remove this report");
        builder1.setTitle("Alert");
        builder1.setCancelable(false);
        builder1.setPositiveButton(
                "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();

                        db.dmSatisfactoryFileTableDao().deleteById(satisfactory_file_array_ids.get(Integer.valueOf(pos)));

                        satisfactory_image_array.clear();
                        satisfactory_file_array_ids.clear();

                        List<DmSatisfactoryFileTable> dmSatisfactoryFileTableList = db.dmSatisfactoryFileTableDao().getDmSatisfactoryFileTable();
                        if (dmSatisfactoryFileTableList.size() != 0) {
                            for (int i = 0; i < dmSatisfactoryFileTableList.size(); i++) {
                                satisfactory_image_array.add(dmSatisfactoryFileTableList.get(i).getFile_name());
                                satisfactory_file_array_ids.add(dmSatisfactoryFileTableList.get(i).getUid());
                            }
                        }
                        showImagesSatisfactory();

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