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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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


public class FollowUpSurvey extends Fragment implements View.OnClickListener{

    ImageView iv_complaint_dropdown_follow_up_survey, iv_non_complaint_dropdown_follow_up_survey, iv_satisfactory_dropdown_follow_up_survey;
    LinearLayout ll_complaint_follow_up_survey, ll_non_complaint_follow_up_survey, ll_satisfactory_follow_up_survey;
    LinearLayout dm_question_complaint_followup_survey,ll_servey_back_followup_survey,ll_servey_next_followup_survey;

    OFISRoomDB db;

    public FollowUpSurvey() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_follow_up_survey, container, false);

        dm_question_complaint_followup_survey = view.findViewById(R.id.dm_question_complaint_followup_survey);
        ll_servey_back_followup_survey = view.findViewById(R.id.ll_servey_back_followup_survey);
        ll_servey_next_followup_survey = view.findViewById(R.id.ll_servey_next_followup_survey);
        ll_servey_back_followup_survey.setOnClickListener(this);
//        ll_servey_next_followup_survey.setOnClickListener(this);
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
            dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByComplianceIdFilterByActionPlan(complaint_id, Constants.FARMER_ID);

            String complianceName = dmComplaintList.get(0).compliance;


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

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = new ArrayList<>();
        dmSurveyAnswerTableList.clear();

        dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(id, Constants.FARMER_ID);
        //dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionIdActionPlan(id, Constants.FARMER_ID, Constants.ACTIONPlAN_ID);

        LinearLayout linearLayout1 = new LinearLayout(getContext());
        linearLayout1.setGravity(Gravity.CENTER);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answer;
            String questionIdDb = dmSurveyAnswerTableList.get(0).questionID;
            editText.setText(answerIdDb);

            RadioGroup radioGroup = new RadioGroup(getActivity());
            radioGroup.setOrientation(LinearLayout.HORIZONTAL);
            RadioButton radioButton1 = new RadioButton(getActivity());
            radioButton1.setText("Not Resolved");
            radioButton1.setTag("rb1" + questionIdDb); // Set a unique ID for the radio

            RadioButton radioButton2 = new RadioButton(getActivity());
            radioButton2.setText("Resolved");
            radioButton2.setTag("rb2" + questionIdDb);
            radioGroup.addView(radioButton1);
            radioGroup.addView(radioButton2);
            DmSurveyAnswerTable dmSurveyAnswerTableFollowUp = new DmSurveyAnswerTable();
            //dmSurveyAnswerTableFollowUp.resolved = ;


            // Create layout parameters for TextViews

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            layoutParams.setMargins(10, 20, 10, 20);
            linearLayout1.addView(radioGroup, layoutParams);
            //linearLayout1.addView(resolveText, layoutParams);

            radioButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        String selectedValue = radioButton1.getText().toString();
                        db.dmSurveyAnswerTableDao().updateResolvedStatus("2", id, Constants.FARMER_ID);
                        // Do something with the selected value
                    }
                }
            });

            radioButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        String selectedValue = radioButton2.getText().toString();
                        db.dmSurveyAnswerTableDao().updateResolvedStatus("1", id, Constants.FARMER_ID);
                        // Do something with the selected value
                    }
                }
            });

        }

        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        if (linearLayoutItems != null) {
            linearLayoutItems.addView(editText);
            linearLayoutItems.addView(linearLayout1);

        }

        linearLayoutItems.setVisibility(View.VISIBLE);

        dm_question_complaint_followup_survey.removeView(linearLayoutItems);
        if (dm_question_complaint_followup_survey != null) {
            dm_question_complaint_followup_survey.addView(linearLayoutItems);

        }

    }


    @Override
    public void onResume() {
        dm_question_complaint_followup_survey.removeAllViews();

        db = OFISRoomDB.getDbInstance(getActivity());

        if (db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size() != 0) {
            for (int i = 0; i < db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).size(); i++) {
                displayQuestionsModuleComplaint(String.valueOf(db.complianceIndicatorDao().getComplianceByModuleId(Constants.MODULE_ID).get(i).id));
            }
        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.ll_servey_next_followup_survey){

        }else if (id == R.id.ll_servey_back_followup_survey){
            requireActivity().onBackPressed();
        }
    }
}