package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
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

import com.olam.digital.ofis.FindFarmerActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FollowUpSurvey extends Fragment {

    ImageView iv_complaint_dropdown_follow_up_survey , iv_non_complaint_dropdown_follow_up_survey , iv_satisfactory_dropdown_follow_up_survey;
    LinearLayout ll_complaint_follow_up_survey , ll_non_complaint_follow_up_survey, ll_satisfactory_follow_up_survey;
    LinearLayout dm_question_complaint_followup_survey , ll_servey_back_followup_survey;

    OFISRoomDB db;

    public FollowUpSurvey() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_follow_up_survey, container, false);

        dm_question_complaint_followup_survey = view.findViewById(R.id.dm_question_complaint_followup_survey);
        ll_servey_back_followup_survey = view.findViewById(R.id.ll_servey_back_followup_survey);
        ll_servey_back_followup_survey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindFarmerActivity.class);
                startActivity(intent);
            }
        });


/*
//        iv_complaint_dropdown_follow_up_survey = view.findViewById(R.id.iv_complaint_dropdown_follow_up_survey);
//        iv_non_complaint_dropdown_follow_up_survey = view.findViewById(R.id.iv_non_complaint_dropdown_follow_up_survey);
//        iv_satisfactory_dropdown_follow_up_survey = view.findViewById(R.id.iv_satisfactory_dropdown_follow_up_survey);
//        ll_complaint_follow_up_survey = view.findViewById(R.id.ll_complaint_follow_up_survey);
//        ll_non_complaint_follow_up_survey = view.findViewById(R.id.ll_non_complaint_follow_up_survey);
//        ll_satisfactory_follow_up_survey = view.findViewById(R.id.ll_satisfactory_follow_up_survey);


        iv_complaint_dropdown_follow_up_survey.setTag("0");
        iv_complaint_dropdown_follow_up_survey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_complaint_dropdown_follow_up_survey.getTag().equals("0")) {
                    iv_complaint_dropdown_follow_up_survey.setTag("1");
                    iv_complaint_dropdown_follow_up_survey.setRotation(180);
                    ll_complaint_follow_up_survey.setVisibility(View.VISIBLE);
                } else {
                    iv_complaint_dropdown_follow_up_survey.setTag("0");
                    iv_complaint_dropdown_follow_up_survey.setRotation(0);
                    ll_complaint_follow_up_survey.setVisibility(View.GONE);
                }
            }
        });

        iv_non_complaint_dropdown_follow_up_survey.setTag("0");
        iv_non_complaint_dropdown_follow_up_survey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_non_complaint_dropdown_follow_up_survey.getTag().equals("0")) {
                    iv_non_complaint_dropdown_follow_up_survey.setTag("1");
                    iv_non_complaint_dropdown_follow_up_survey.setRotation(180);
                    ll_non_complaint_follow_up_survey.setVisibility(View.VISIBLE);
                } else {
                    iv_non_complaint_dropdown_follow_up_survey.setTag("0");
                    iv_non_complaint_dropdown_follow_up_survey.setRotation(0);
                    ll_non_complaint_follow_up_survey.setVisibility(View.GONE);
                }
            }
        });

        iv_satisfactory_dropdown_follow_up_survey.setTag("0");
        iv_satisfactory_dropdown_follow_up_survey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (iv_satisfactory_dropdown_follow_up_survey.getTag().equals("0")) {
                    iv_satisfactory_dropdown_follow_up_survey.setTag("1");
                    iv_satisfactory_dropdown_follow_up_survey.setRotation(180);
                    ll_satisfactory_follow_up_survey.setVisibility(View.VISIBLE);
                } else {
                    iv_satisfactory_dropdown_follow_up_survey.setTag("0");
                    iv_satisfactory_dropdown_follow_up_survey.setRotation(0);
                    ll_satisfactory_follow_up_survey.setVisibility(View.GONE);
                }
            }
        });*/

        dm_question_complaint_followup_survey.removeAllViews();

        db = OFISRoomDB.getDbInstance(getActivity());

        if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
            for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
            }
        }


        return view;
    }

    @SuppressLint("ResourceAsColor")
    private void displayQuestionsModuleComplaint(String complaint_id) {

        List<ComplianceIndicator> dmComplaintList = new ArrayList<>();
        dmComplaintList.clear();
        dmComplaintList = db.complianceIndicatorDao().getComplianceNameById(complaint_id);

        if (dmComplaintList.size() != 0) {
            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = new ArrayList<>();
            dmSurveyAnswerTableList.clear();
            dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceId(complaint_id,Constants.FARMER_ID);

            String complianceName = dmComplaintList.get(0).compliance;

//            for (int amt = 0; amt < dmSurveyAnswerTableList.size(); amt++) {
//
//                budgetAmount = String.valueOf(dmSurveyAnswerTableList.get(amt).amount);
//
//            }

            if (complianceName.equalsIgnoreCase("Satisfactory")) {
                Constants.satisfactorySize = String.valueOf(dmSurveyAnswerTableList.size());
            } else if (complianceName.equalsIgnoreCase("Compliant")) {
                Constants.complaintSize = String.valueOf(dmSurveyAnswerTableList.size());
            } else if (complianceName.equalsIgnoreCase("Non Compliant")) {
                Constants.noncomplaintSize = String.valueOf(dmSurveyAnswerTableList.size());
            }

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View convertView = inflater.inflate(R.layout.complaint_layout, null, false);
            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint);
            final ImageView complaint_dropdown = convertView.findViewById(R.id.iv_complaint_dropdown);

            dm_question_complaint_followup_survey.addView(convertView);
            convertView.setTag(complaint_id);
            complaint_dropdown.setTag("0" + complaint_id);
            selected_imgName.setText(complianceName);


            List<DmSurveyAnswerTable> finalDmSurveyAnswerTableList = dmSurveyAnswerTableList;
            complaint_dropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = complaint_dropdown.getTag().toString();

                    int tagFirst = Integer.parseInt(String.valueOf(tag.charAt(0)));

                    if (tagFirst == 0) {
                        //Toast.makeText(getActivity(), "Expand" + tag, Toast.LENGTH_SHORT).show();
                        complaint_dropdown.setTag("1" + complaint_id);
                        complaint_dropdown.setRotation(180);
                        if (finalDmSurveyAnswerTableList.size() != 0) {
                            View desiredView = dm_question_complaint_followup_survey.findViewWithTag("item" + complaint_id);
                            LinearLayout linearLayout = (LinearLayout) desiredView;
                            linearLayout.setVisibility(View.VISIBLE);
                        }


                    } else {
                        //Toast.makeText(getActivity(), "Collapse" + tag, Toast.LENGTH_SHORT).show();
                        complaint_dropdown.setTag("0" + complaint_id);
                        complaint_dropdown.setRotation(0);
                        if (finalDmSurveyAnswerTableList.size() != 0) {
                            View desiredView = dm_question_complaint_followup_survey.findViewWithTag("item" + complaint_id);
                            LinearLayout linearLayout = (LinearLayout) desiredView;
                            linearLayout.setVisibility(View.GONE);

                        }

                    }
                }
            });

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
                    linearLayoutItems.addView(textView);


                    renderTextboxQuestionModuleComplaint(String.valueOf(dmQuestionsModule.questionID), complaint_id, linearLayoutItems);

                }
            }
        }
    }


    public void renderTextboxQuestionModuleComplaint(String id, String complaint_id, LinearLayout linearLayoutItems) {


        LinearLayout.LayoutParams layoutParamsQes = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.capture_box)
        );

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setEnabled(false);
        editText.setTextColor(getResources().getColor(R.color.black));
        editText.setPadding(10, 10, 0, 10);
        layoutParamsQes.setMargins(0, 10, 0, 10);
        editText.setLayoutParams(layoutParamsQes);
        editText.setTag("et" + complaint_id);

//
//        TextView textView = new TextView(getActivity());
//        textView.setText("Capture Proof");
//
//        //Important Do not Change the Tag
//        textView.setTag(id);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            textView.setTextColor(getActivity().getColor(R.color.colorPrimary));
//        }
//
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                getResources().getDimensionPixelSize(R.dimen.capture_box)
//        );
//        layoutParams.setMargins(0, 10, 0, 10);
//
//        textView.setLayoutParams(layoutParams);
//        textView.setGravity(Gravity.CENTER);
//        textView.setPadding(0, 15, 0, 15);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            textView.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg));
//        }


//        LinearLayout linearLayoutImage = new LinearLayout(getActivity());
//        linearLayoutImage.setLayoutParams(new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//
//        linearLayoutImage.setTag("1234" + id);
//        linearLayoutImage.setOrientation(LinearLayout.VERTICAL);
//
//        ArrayList<String> retrievedArray = new ArrayList<>();
//        retrievedArray.clear();
//
//        ArrayList<Long> retrievedArrayFile = new ArrayList<>();
//        retrievedArrayFile.clear();
//
//        List<DmComplaintFileTable> dmComplaintFileTableList = new ArrayList<>();
//        dmComplaintFileTableList.clear();
//        dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(Integer.parseInt(id));
//
//        // Log.d("akshdkjashdk", "" + dmComplaintFileTableList.size());
//
//        if (dmComplaintFileTableList.size() != 0) {
//            for (int l = 0; l < dmComplaintFileTableList.size(); l++) {
//                retrievedArray.add(dmComplaintFileTableList.get(l).getFile_name());
//                retrievedArrayFile.add(dmComplaintFileTableList.get(l).getUid());
//
//                LayoutInflater inflater = LayoutInflater.from(getActivity());
//                final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
//                final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
//                final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);
//
//                linearLayoutImage.addView(convertView);
//                convertView.setTag(l);
//                remove.setTag(l);
//                selected_imgName.setText(dmComplaintFileTableList.get(l).getFile_name());
//
//                int finalI = l;
//                ArrayList<Long> finalRetrievedArrayFile = retrievedArrayFile;
//                ArrayList<String> finalRetrievedArray = retrievedArray;
//               /* remove.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
//                        builder1.setMessage("Do you want to remove this report");
//                        builder1.setTitle("Alert");
//                        builder1.setCancelable(false);
//                        builder1.setPositiveButton(
//                                "YES",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        dialog.cancel();
//
//                                        //Toast.makeText(getActivity(), "" + pos, Toast.LENGTH_SHORT).show();
//
//                                        db.dmComplaintFileTableDao().deleteById(finalRetrievedArrayFile.get(finalI));
//
//                                        finalRetrievedArray.clear();
//                                        finalRetrievedArrayFile.clear();
//
//                                        List<DmComplaintFileTable> dmComplaintFileTableList = db.dmComplaintFileTableDao().getFilesById(CAMERA_REQUEST);
//                                        if (dmComplaintFileTableList.size() != 0) {
//                                            for (int k = 0; k < dmComplaintFileTableList.size(); k++) {
//                                                finalRetrievedArray.add(dmComplaintFileTableList.get(k).getFile_name());
//                                                finalRetrievedArrayFile.add(dmComplaintFileTableList.get(k).getUid());
//                                            }
//                                        }
//
//                                        for (int j = 0; j < finalRetrievedArray.size(); j++) {
//                                            LayoutInflater inflater = LayoutInflater.from(getActivity());
//                                            final View convertView = inflater.inflate(R.layout.image_complaint_dynamic, null, false);
//                                            final TextView selected_imgName = convertView.findViewById(R.id.tv_complaint_filename);
//                                            final ImageView remove = convertView.findViewById(R.id.iv_complaint_delete_file);
//
//                                            linearLayoutImage.addView(convertView);
//                                            convertView.setTag(j);
//                                            remove.setTag(j);
//                                            selected_imgName.setText(finalRetrievedArray.get(j));
//                                        }
//
//                                    }
//                                });
//                        builder1.setNegativeButton(
//                                "NO",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        dialog.cancel();
//                                    }
//                                });
//
//                        AlertDialog alert11 = builder1.create();
//                        alert11.show();
//
//                    }
//                });*/
//            }
//        }
//
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("Add Photo!");
//                builder.setItems(options, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//                        if (options[item].equals("Take Photo")) {
//                            if (isStoragePermissionCameraGranted()) {
//                                if (dynamicArray.size() > 5) {
//                                    Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    CAMERA_REQUEST = Integer.parseInt(textView.getTag().toString());
//                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                    // Ensure that there's a camera activity to handle the intent
//                                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                                        // Create the File where the photo should go
//                                        File photoFile = null;
//                                        try {
//                                            photoFile = createImageFile();
//                                        } catch (IOException ex) {
//                                            // Error occurred while creating the File
//
//                                        }
//                                        // Continue only if the File was successfully created
//                                        if (photoFile != null) {
//                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                                                    FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider",
//                                                            new File(mCurrentPhotoPath)));
//                                            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
//                                        }
//                                    }
//                                }
//
//                            }
//
//                        } else if (options[item].equals("Choose from Gallery")) {
//
//                            if (isStoragePermissionGalleryGranted()) {
//                                if (dynamicArray.size() > 5) {
//                                    Toast.makeText(getActivity(), "Maximum 5 files can be uploaded.", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    GALLERY_PICK = Integer.parseInt(textView.getTag().toString() + "0");
//                                    Intent galleryIntent = new Intent();
//                                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                                    galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES);
//                                    galleryIntent.setType("*/*");
//                                    startActivityForResult(Intent.createChooser(galleryIntent, "Choose gallery"), GALLERY_PICK);
//                                }
//
//                            }
//                        } else if (options[item].equals("Cancel")) {
//                            dialog.dismiss();
//                        }
//                    }
//                });
//                builder.show();
//
//            }
//        });
//

//        List<DmActionPlansItem> dmActionPlansItemList = new ArrayList<>();
//        dmActionPlansItemList.clear();
//        dmActionPlansItemList = db.dmActionPlansItemDao().getActionPlanItemById(Integer.parseInt(id));
//
//        final List<String> list = new ArrayList<String>();
//        list.clear();
//
//        final List<String> ids = new ArrayList<String>();
//        ids.clear();
//
//        for (int c = 0; c < dmActionPlansItemList.size(); c++) {
//            list.add(dmActionPlansItemList.get(c).actionPlan);
//            ids.add(String.valueOf(dmActionPlansItemList.get(c).id));
//        }
//
//        LinearLayout linearLayoutSpin = new LinearLayout(getActivity());
//        linearLayoutSpin.setLayoutParams(new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        linearLayoutSpin.setOrientation(LinearLayout.HORIZONTAL);
//        linearLayoutSpin.setWeightSum(10);
//
//        LinearLayout.LayoutParams layoutParamsSpin = new LinearLayout.LayoutParams(
//                getResources().getDimensionPixelSize(R.dimen.forWeight),
//                getResources().getDimensionPixelSize(R.dimen.capture_box),
//                5
//        );
//
//        final Spinner spinner = new Spinner(getActivity());
//
//        spinner.setLayoutParams(layoutParamsSpin);
//        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));
//
//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
//        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        spinner.setAdapter(spinnerAdapter);
//
//        spinner.setTag("spinner" + complaint_id);
//
//        layoutParamsSpin.setMargins(0, 10, 10, 10);
//
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long idS) {
//
//                String Id = ids.get(position);
//
//                db.dmSurveyAnswerTableDao().updateActionPlanId(Id, id);
//
//                // Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//
//        LinearLayout.LayoutParams layoutParamsetCur = new LinearLayout.LayoutParams(
//                getResources().getDimensionPixelSize(R.dimen.forWeight),
//                getResources().getDimensionPixelSize(R.dimen.capture_box),
//                2
//        );
//
////        List<DmCurrenciesItem> currenciesItems = new ArrayList<>();
////        currenciesItems.clear();
////        currenciesItems = db.dmCurrenciesItemDao().getCurrencyItemById(Integer.parseInt(currency_id));
////
////        String currency = "$";
////
////        if (currenciesItems.size() != 0) {
////            currency = currenciesItems.get(0).notation;
////        }
//
//        EditText editTextCurrency = new EditText(getActivity());
////        editTextCurrency.setText(currency);
//        editTextCurrency.setTextSize(14);
//        editTextCurrency.setEnabled(false);
//        editTextCurrency.setTextColor(getResources().getColor(R.color.black));
//        editTextCurrency.setPadding(0, 10, 0, 10);
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            editTextCurrency.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
//        }
//
//        layoutParamsetCur.setMargins(0, 10, 0, 10);
//        editTextCurrency.setLayoutParams(layoutParamsetCur);
//
//
//        LinearLayout.LayoutParams layoutParamsetCurEt = new LinearLayout.LayoutParams(
//                getResources().getDimensionPixelSize(R.dimen.forWeight),
//                getResources().getDimensionPixelSize(R.dimen.capture_box),
//                3
//        );
//
//        EditText editTextCurrencyInput = new EditText(getActivity());
//        editTextCurrencyInput.setHint("Amount");
//        editTextCurrencyInput.setTextSize(14);
//        editTextCurrencyInput.setTextColor(getResources().getColor(R.color.black));
//        editTextCurrencyInput.setPadding(0, 10, 0, 10);
////        editTextCurrencyInput.setText(budgetAmount);
//        editTextCurrencyInput.setInputType(InputType.TYPE_CLASS_NUMBER);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            editTextCurrencyInput.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
//        }
//        layoutParamsetCurEt.setMargins(0, 10, 0, 10);
//        editTextCurrencyInput.setLayoutParams(layoutParamsetCurEt);
//
//        editTextCurrencyInput.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.length() == 0) {
//
//                } else {
//                    db.dmSurveyAnswerTableDao().updateAmount(charSequence.toString(), id);
//                }
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

//
//        linearLayoutSpin.addView(spinner);
//        linearLayoutSpin.addView(editTextCurrency);
//        linearLayoutSpin.addView(editTextCurrencyInput);

//        LinearLayout linearLayoutActionPlanMain = new LinearLayout(getActivity());
//        linearLayoutActionPlanMain.setLayoutParams(new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        linearLayoutActionPlanMain.setOrientation(LinearLayout.VERTICAL);
//
//        LinearLayout linearLayoutActionPlan = new LinearLayout(getActivity());
//
//        EditText actionPlan = new EditText(getActivity());
//        actionPlan.setHint("Edit action plan details");
//        actionPlan.setTextSize(14);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            actionPlan.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
//        }
//
//        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                getResources().getDimensionPixelSize(R.dimen.actionplanbox) // Replace with your desired height in dp
//        );
//        actionPlan.setLayoutParams(layoutParams1);
//
//        actionPlan.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.length() == 0) {
//                    db.dmSurveyAnswerTableDao().updateActionPlanText("", id);
//                } else {
//                    db.dmSurveyAnswerTableDao().updateActionPlanText(charSequence.toString(), id);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

//
//        linearLayoutActionPlan.setGravity(Gravity.END);
//        linearLayoutActionPlan.setOrientation(LinearLayout.HORIZONTAL);
//
//        ImageView imageView1 = new ImageView(getActivity());
//        imageView1.setImageResource(R.drawable.green_tick);
//
//        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(
//                getResources().getDimensionPixelSize(R.dimen.small_button_width_height),
//                getResources().getDimensionPixelSize(R.dimen.small_button_width_height));
//
//        imageView1.setLayoutParams(layoutParams3);
//
//        ImageView imageView2 = new ImageView(getActivity());
//        imageView2.setImageResource(R.drawable.red_cross);
//
//        imageView2.setLayoutParams(layoutParams3);
//
//        linearLayoutActionPlan.addView(imageView1);
//        linearLayoutActionPlan.addView(imageView2);
//
//        linearLayoutActionPlanMain.addView(actionPlan);
//        linearLayoutActionPlanMain.addView(linearLayoutActionPlan);
//

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = new ArrayList<>();
        dmSurveyAnswerTableList.clear();

        dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(id, Constants.FARMER_ID);

        LinearLayout linearLayout1 = new LinearLayout(getContext());
        linearLayout1.setGravity(Gravity.CENTER);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answer;
            editText.setText(answerIdDb);

            // Create TextViews
            TextView notResolveText = new TextView(getContext());
            notResolveText.setText("Not Resolved");
            notResolveText.setTextColor(getResources().getColor(R.color.gray));
            notResolveText.setPadding(70, 25, 70, 25);
            notResolveText.setTextSize(14);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notResolveText.setBackground(getActivity().getDrawable(R.drawable.dm_captureproof_bg));
            }
            TextView resolveText = new TextView(getContext());
            resolveText.setText("Resolved");
            resolveText.setTextColor(getResources().getColor(R.color.white));
            resolveText.setPadding(110, 25, 110, 25);
            resolveText.setTextSize(14);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resolveText.setBackground(getActivity().getDrawable(R.drawable.dm_next_background));
            }

// Create layout parameters for TextViews

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            layoutParams.setMargins(10, 20, 10, 20);

// Add TextViews to LinearLayout
            linearLayout1.addView(notResolveText, layoutParams);
            linearLayout1.addView(resolveText, layoutParams);




        }

        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        if (linearLayoutItems != null) {
            linearLayoutItems.addView(editText);
            linearLayoutItems.addView(linearLayout1);
//            linearLayoutItems.addView(textView);
//            linearLayoutItems.addView(linearLayoutImage);
//            linearLayoutItems.addView(linearLayoutSpin);

//            if (db.dmSurveyAnswerTableDao().getDataByQuestionId(id).get(0).allowCustomActionPlan) {
//                linearLayoutItems.addView(linearLayoutActionPlanMain);
//            }

        }

        linearLayoutItems.setVisibility(View.GONE);

        dm_question_complaint_followup_survey.removeView(linearLayoutItems);
        if (dm_question_complaint_followup_survey != null) {
            dm_question_complaint_followup_survey.addView(linearLayoutItems);

        }

    }

}