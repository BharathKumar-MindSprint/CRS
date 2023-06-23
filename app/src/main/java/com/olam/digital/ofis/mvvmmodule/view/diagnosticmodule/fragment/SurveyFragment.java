package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.olam.digital.ofis.FindFarmerActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DecimalFilter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.NegativeDecimalFilter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.NumberInputFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SurveyFragment extends Fragment {
    LinearLayout ll_servey_next, ll_servey_back, dm_Section, dm_Question;
    OFISRoomDB db;
    public LayoutInflater inflater;
    Resources resources;

//    private DataRefreshListener dataRefreshListener;

    public SurveyFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_survey, container, false);

        ll_servey_next = view.findViewById(R.id.ll_servey_next);
        ll_servey_back = view.findViewById(R.id.ll_servey_back);
        dm_Section = view.findViewById(R.id.dm_Section);
        dm_Question = view.findViewById(R.id.dm_Question);


        db = OFISRoomDB.getDbInstance(getActivity());
        //db.dmSurveyAnswerTableDao().deleteAll();

        List<DmSectionsItem> dmSectionsItemList = db.dmSectionsItemDao().getDmSectionsByModuleId(Integer.parseInt(Constants.MODULE_ID));
        displaySectionItems(dmSectionsItemList);

        List<DmQuestionsModule> dmQuestionsModuleList = db.dmQuestionsModuleDao().getDmQuestionsModulebyModuleId(Integer.parseInt(Constants.MODULE_ID));
        // List<DmQuestionsModule> dmQuestionsModuleList = db.dmQuestionsModuleDao().getDmQuestionsModulebyModuleId(262);
        displayQuestionsModuleItems(dmQuestionsModuleList);



        ll_servey_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindFarmerActivity.class);
                startActivity(intent);
            }
        });

        ll_servey_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<String> qId = Constants.questionIds;

                ArrayList<String> qIdSections = Constants.questionIdsSection;

                if (db.dmSurveyAnswerTableDao().getDataByModuleId(Constants.MODULE_ID, Constants.FARMER_ID).size() ==
                        Constants.questionIds.size() + Constants.questionIdsSection.size()) {
                    ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                    viewPager.setCurrentItem(1);
                    for (int i = 0; i < Constants.questionIds.size(); i++) {
                        View desiredView = dm_Question.findViewWithTag(qId.get(i));
                        TextView textView = (TextView) desiredView;
                        textView.setVisibility(View.GONE);
                    }

                    for (int j = 0; j < Constants.questionIdsSection.size(); j++) {
                        View desiredView = dm_Section.findViewWithTag(qIdSections.get(j));
                        TextView textView = (TextView) desiredView;
                        textView.setVisibility(View.GONE);
                    }


                } else {
                    for (int i = 0; i < Constants.questionIds.size(); i++) {

                        String type = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qId.get(i))).get(0).displayType;

                        if (type.equalsIgnoreCase("text box")) {

                            View desiredView = dm_Question.findViewWithTag(qId.get(i));
                            if (desiredView != null) {
                                TextView textView = (TextView) desiredView;
                                if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID).size() == 0) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                // View not found
                            }

                        } else if (type.equalsIgnoreCase("dropdown")) {
                            View desiredView = dm_Question.findViewWithTag(qId.get(i));
                            if (desiredView != null) {
                                TextView textView = (TextView) desiredView;
                                if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID).size() == 0) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                // View not found
                            }

                        }

                    }

                    for (int j = 0; j < Constants.questionIdsSection.size(); j++) {

                        String type = db.dmQuestionsSubModuleDao().getDmQuestionType(Integer.parseInt(qIdSections.get(j))).get(0).displayType;

                        if (type.equalsIgnoreCase("text box")) {

                            View desiredView = dm_Section.findViewWithTag(qIdSections.get(j));
                            if (desiredView != null) {
                                TextView textView = (TextView) desiredView;
                                if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qIdSections.get(j), Constants.FARMER_ID).size() == 0) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                // View not found
                            }

                        } else if (type.equalsIgnoreCase("dropdown")) {
                            View desiredView = dm_Section.findViewWithTag(qIdSections.get(j));
                            if (desiredView != null) {
                                TextView textView = (TextView) desiredView;
                                if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qIdSections.get(j), Constants.FARMER_ID).size() == 0) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                // View not found
                            }

                        }

                    }
                }

            }
        });


        return view;
    }

    private void displaySectionQuestionItems(int section, List<DmQuestionsModule> dmQuestionsSubModuleList) {
        Constants.questionIdsSection.clear();
        for (int j = 0; j < dmQuestionsSubModuleList.size(); j++) {

            DmQuestionsModule dmQuestionsSubModule = dmQuestionsSubModuleList.get(j);
            TextView textView = new TextView(getActivity());
            textView.setText(section + "." + (j + 1) + "." + dmQuestionsSubModule.question);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(14);
            //textView.setTypeface(null, Typeface.BOLD);
            dm_Section.addView(textView);

            Constants.questionIdsSection.add(String.valueOf(dmQuestionsSubModule.id));

            if (dmQuestionsSubModule.displayType.equalsIgnoreCase("dropdown")) {
                List<DmLookupValuesItem> dmLookupValuesISubmoduleList = new ArrayList<>();
                dmLookupValuesISubmoduleList.clear();
                dmLookupValuesISubmoduleList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(dmQuestionsSubModule.id);
                renderDropDownQuestionSections(String.valueOf(dmQuestionsSubModule.id), dmLookupValuesISubmoduleList, dmQuestionsSubModule.question);
            } else if (dmQuestionsSubModule.displayType.equalsIgnoreCase("text box")) {
                renderTextboxQuestionSections(String.valueOf(dmQuestionsSubModule.id), dmQuestionsSubModule.question);
            }

        }

    }

    private void displaySectionItems(List<DmSectionsItem> dmSectionsItemList) {
        for (int i = 0; i < dmSectionsItemList.size(); i++) {
            DmSectionsItem dmSectionsItem = dmSectionsItemList.get(i);
            TextView textView = new TextView(getActivity());
            textView.setText(dmSectionsItem.name);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(14);
            //textView.setPadding(0, res.getDimensionPixelSize(R.dimen.ofis_large_pad), 0, 0);
            textView.setTypeface(null, Typeface.BOLD);
            dm_Section.addView(textView);
            List<DmQuestionsModule> dmQuestionsSubModuleList = new ArrayList<>();
            dmQuestionsSubModuleList.clear();
            dmQuestionsSubModuleList = db.dmQuestionsModuleDao().getDmQuestionsSubModulebySectionId(dmSectionsItem.id);
            displaySectionQuestionItems(i + 1, dmQuestionsSubModuleList);
//            Toast.makeText(getActivity(), "OK" + dmQuestionsSubModuleList.size(), Toast.LENGTH_SHORT).show();

        }
    }

    private void displayQuestionsModuleItems(List<DmQuestionsModule> dmQuestionsModuleList) {
        Constants.questionIds.clear();
        for (int m = 0; m < dmQuestionsModuleList.size(); m++) {
            DmQuestionsModule dmQuestionsModule = dmQuestionsModuleList.get(m);
            TextView textView = new TextView(getActivity());

            String star = "*";
            SpannableStringBuilder builder = new SpannableStringBuilder();

            // Append the first string (s1)
            builder.append((m + 1) + "." + dmQuestionsModule.question);

            // Append the second string (s2) with red color
            int start = builder.length();
            builder.append(" " + star);  // Add space for separation

            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(colorSpan, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (dmQuestionsModule.isCriticalQuest || dmQuestionsModule.mandate){
                textView.setText(builder);
            }else{
                textView.setText((m + 1) + "." + dmQuestionsModule.question);
            }
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(14);
            textView.setPadding(0, 15, 0, 0);
            textView.setTypeface(null, Typeface.BOLD);
            dm_Question.addView(textView);
            Constants.questionIds.add(String.valueOf(dmQuestionsModule.id));

            if (dmQuestionsModule.displayType.equalsIgnoreCase("dropdown")) {
                List<DmLookupValuesItem> dmLookupValuesItemList = new ArrayList<>();
                dmLookupValuesItemList.clear();
                dmLookupValuesItemList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(dmQuestionsModule.id);
                renderDropDownQuestionModule(String.valueOf(dmQuestionsModule.id), dmLookupValuesItemList, dmQuestionsModule.question);
            } else if (dmQuestionsModule.displayType.equalsIgnoreCase("text box")) {
                renderTextboxQuestionModule(String.valueOf(dmQuestionsModule.id), dmQuestionsModule.question);
            }

        }

    }

    public void renderDropDownQuestionSections(String questionId, List<DmLookupValuesItem> dmLookupValuesISubmoduleList, String question) {


        final List<String> list = new ArrayList<String>();
        list.clear();


        final List<String> ids = new ArrayList<String>();
        ids.clear();


        final List<Boolean> allowCustomActionPlan = new ArrayList<Boolean>();
        allowCustomActionPlan.clear();

        final List<Boolean> isCaptureProof = new ArrayList<Boolean>();
        isCaptureProof.clear();

        final List<String> complianceIds = new ArrayList<String>();
        complianceIds.clear();

        list.add("Answer");
        ids.add("0");
        complianceIds.add("0");
        allowCustomActionPlan.add(false);
        isCaptureProof.add(false);

        for (int k = 0; k < dmLookupValuesISubmoduleList.size(); k++) {
            list.add(dmLookupValuesISubmoduleList.get(k).value);
            ids.add(String.valueOf(dmLookupValuesISubmoduleList.get(k).id));
            complianceIds.add(String.valueOf(dmLookupValuesISubmoduleList.get(k).complianceId));
            allowCustomActionPlan.add(dmLookupValuesISubmoduleList.get(k).allowCustomActionPlan);
            isCaptureProof.add(dmLookupValuesISubmoduleList.get(k).isCaptureProof);
        }


        final Spinner spinner = new Spinner(getActivity());


//        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner,relativeLayout,false);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        TextView textView = new TextView(getActivity());
        textView.setText("Please enter any drop down value");
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);
        textView.setTag(questionId);

        String answerIdDb = "0";

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID);
        if (dmSurveyAnswerTableList.size() != 0) {
            answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
        }

        int selectedPosition = 0;

        for (int i = 0; i < ids.size(); i++) {
            if (answerIdDb.equalsIgnoreCase(ids.get(i))) {
                selectedPosition = i;
            }

        }

        spinner.setSelection(selectedPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();

                if (position == 0) {
                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                } else {
                    String answerId = ids.get(position);

                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);

                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                    dmSurveyAnswerTable1.questionID = questionId;
                    dmSurveyAnswerTable1.answerID = ids.get(position);
                    dmSurveyAnswerTable1.answer = list.get(position);
                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                    dmSurveyAnswerTable1.question = question;
                    dmSurveyAnswerTable1.complinceTypeId = complianceIds.get(position);
                    dmSurveyAnswerTable1.actionplanId = "";
                    dmSurveyAnswerTable1.actionPlanText = "";
                    dmSurveyAnswerTable1.amount = 0;
                    dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                    dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan.get(position);
                    dmSurveyAnswerTable1.isCaptureProof = isCaptureProof.get(position);
                    db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);


                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Add Spinner to LinearLayout
        if (dm_Section != null) {
            dm_Section.addView(spinner);
            dm_Section.addView(textView);
        }


    }

    public void renderTextboxQuestionSections(String questionId, String question) {

        EditText editText = new EditText(getActivity());
        editText.setHint("Answer");
        editText.setTextSize(14);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);


        TextView textView = new TextView(getActivity());
        textView.setText("Please enter any value!");
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);

        textView.setTag(questionId);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answer;
            editText.setText(answerIdDb);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                } else {
                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                    dmSurveyAnswerTable1.questionID = questionId;
                    dmSurveyAnswerTable1.answer = charSequence.toString();
                    dmSurveyAnswerTable1.answerID = charSequence.toString();
                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                    dmSurveyAnswerTable1.question = question;
                    int complianceId = 0;
                    boolean allowCustomActionPlan = false;
                    boolean isCaptureProof = false;
                    if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size() != 0) {
                        if (editText.getText().toString() != null) {
                            String value = editText.getText().toString().trim();
                            for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++)
                                if (Double.parseDouble(value) < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange) {
                                    complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;
                                    allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                                    isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                                }
                        }
                    }


                    dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
                    dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
                    dmSurveyAnswerTable1.actionPlanText = "";
                    dmSurveyAnswerTable1.actionplanId = "";
                    dmSurveyAnswerTable1.amount = 0;
                    dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
                    dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                    db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        // Add Spinner to LinearLayout
        if (dm_Section != null) {
            dm_Section.addView(editText);
            dm_Section.addView(textView);
        }

    }


    public void renderDropDownQuestionModule(String questionId, List<DmLookupValuesItem> dmLookupValuesItemList, String question) {


        final List<String> list = new ArrayList<String>();
        list.clear();


        final List<String> ids = new ArrayList<String>();
        ids.clear();


        final List<String> complianceIds = new ArrayList<String>();
        complianceIds.clear();

        final List<Boolean> allowCustomActionPlan = new ArrayList<Boolean>();
        allowCustomActionPlan.clear();

        final List<Boolean> isCaptureProof = new ArrayList<Boolean>();
        isCaptureProof.clear();

        list.add("Answer");
        ids.add("0");
        complianceIds.add("0");
        allowCustomActionPlan.add(false);
        isCaptureProof.add(false);

        for (int k = 0; k < dmLookupValuesItemList.size(); k++) {
            list.add(dmLookupValuesItemList.get(k).value);
            ids.add(String.valueOf(dmLookupValuesItemList.get(k).id));
            complianceIds.add(String.valueOf(dmLookupValuesItemList.get(k).complianceId));
            allowCustomActionPlan.add(dmLookupValuesItemList.get(k).allowCustomActionPlan);
            isCaptureProof.add(dmLookupValuesItemList.get(k).isCaptureProof);
        }


        final Spinner spinner = new Spinner(getActivity());


//        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner,relativeLayout,false);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        TextView textView = new TextView(getActivity());
        textView.setText("Please enter any drop down value");
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);
        textView.setTag(questionId);

        String answerIdDb = "0";

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID);
        if (dmSurveyAnswerTableList.size() != 0) {
            answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
        }

        int selectedPosition = 0;

        for (int i = 0; i < ids.size(); i++) {
            if (answerIdDb.equalsIgnoreCase(ids.get(i))) {
                selectedPosition = i;
            }

        }

        spinner.setSelection(selectedPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(), "Selected Item " + list.get(position), Toast.LENGTH_SHORT).show();

                if (position == 0) {
                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                } else {
                    String answerId = ids.get(position);


                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);

                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                    dmSurveyAnswerTable1.questionID = questionId;
                    dmSurveyAnswerTable1.answerID = ids.get(position);
                    dmSurveyAnswerTable1.answer = list.get(position);
                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                    dmSurveyAnswerTable1.question = question;
                    dmSurveyAnswerTable1.complinceTypeId = complianceIds.get(position);
                    dmSurveyAnswerTable1.actionplanId = "";
                    dmSurveyAnswerTable1.actionPlanText = "";
                    dmSurveyAnswerTable1.amount = 0;
                    dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                    dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan.get(position);
                    dmSurveyAnswerTable1.isCaptureProof = isCaptureProof.get(position);
                    db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Add Spinner to LinearLayout
        if (dm_Question != null) {
            dm_Question.addView(spinner);
            dm_Question.addView(textView);
        }


    }

    public void renderTextboxQuestionModule(String questionId, String question) {

        EditText editText = new EditText(getActivity());
//        editText.setHint("Answer");
        editText.setTextSize(14);
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        double minValue = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).minimumRange;
        double maxValue = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).maximumRange;
//        double maxValue = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange;

        if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowNegative && db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowDecimal) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
            NegativeDecimalFilter negativeDecimalFilter = new NegativeDecimalFilter(minValue, maxValue);
            editText.setHint("Range from " + minValue + " to " + maxValue);
            editText.setFilters(new InputFilter[] { negativeDecimalFilter });
            /*editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() == 0) {
                        db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                    } else {
                        db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                        DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                        dmSurveyAnswerTable1.questionID = questionId;
                        dmSurveyAnswerTable1.answer = charSequence.toString();
                        dmSurveyAnswerTable1.answerID = charSequence.toString();
                        dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                        dmSurveyAnswerTable1.question = question;
                        int complianceId = 0;
                        boolean allowCustomActionPlan = false;
                        boolean isCaptureProof = false;
                        if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size() != 0) {
                            if (editText.getText().toString() != null) {
                                String value = editText.getText().toString().trim();


                                for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++) {

                                allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                                isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                                complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;

                                }

                            }
                        }


                        dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
                        dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
                        dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
                        dmSurveyAnswerTable1.actionPlanText = "";
                        dmSurveyAnswerTable1.actionplanId = "";
                        dmSurveyAnswerTable1.amount = 0;
                        dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                    }


                }

                @Override
                public void afterTextChanged(Editable editable) {


                }
            });*/
        }else if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowNegative) {
                editText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
                editText.setHint("Range from " + minValue + " to " + maxValue);
                NumberInputFilter numberFilter = new NumberInputFilter(minValue, maxValue);
                editText.setFilters(new InputFilter[]{numberFilter});

            /*editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() == 0) {
                        db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                    } else {
                        db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                        DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                        dmSurveyAnswerTable1.questionID = questionId;
                        dmSurveyAnswerTable1.answer = charSequence.toString();
                        dmSurveyAnswerTable1.answerID = charSequence.toString();
                        dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                        dmSurveyAnswerTable1.question = question;
                        int complianceId = 0;
                        boolean allowCustomActionPlan = false;
                        boolean isCaptureProof = false;
                        if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size() != 0) {
                            if (editText.getText().toString() != null) {
                                String value = editText.getText().toString().trim();


                                for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++) {

                                allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                                isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                                complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;

                                }

                            }
                        }


                        dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
                        dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
                        dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
                        dmSurveyAnswerTable1.actionPlanText = "";
                        dmSurveyAnswerTable1.actionplanId = "";
                        dmSurveyAnswerTable1.amount = 0;
                        dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                    }


                }

                @Override
                public void afterTextChanged(Editable editable) {


                }
            });*/
            } else if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowDecimal) {
                editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                DecimalFilter decimalFilter = new DecimalFilter(minValue, maxValue);
                editText.setHint("Range from " + minValue + " to " + maxValue);
                editText.setFilters(new InputFilter[]{decimalFilter});

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Not needed for this implementation
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Not needed for this implementation
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String input = editText.getText().toString();
                    if (!input.isEmpty()) {
                        double number = Double.parseDouble(input);
                        if (number >= minValue) {
                            // Valid input starting from the minimum value
                            if (number <= maxValue) {
                                // Input falls within the specified range
                                // Perform desired actions here
                                editText.setError(null); // Clear any previous error
                                /*if (editable.length() == 0) {
                                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                                } else {
                                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                                    dmSurveyAnswerTable1.questionID = questionId;
                                    dmSurveyAnswerTable1.answer = editable.toString();
                                    dmSurveyAnswerTable1.answerID = editable.toString();
                                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                                    dmSurveyAnswerTable1.question = question;
                                    int complianceId = 0;
                                    boolean allowCustomActionPlan = false;
                                    boolean isCaptureProof = false;
                                    if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size() != 0) {
                                        if (editText.getText().toString() != null) {
                                            String value = editText.getText().toString().trim();


                                            for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++) {
                                                if (Double.parseDouble(value) < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange) {
                                                    complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;
                                                    allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                                                    isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                                                }
                                            }

                                        }
                                    }


                                    dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
                                    dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
                                    dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
                                    dmSurveyAnswerTable1.actionPlanText = "";
                                    dmSurveyAnswerTable1.actionplanId = "";
                                    dmSurveyAnswerTable1.amount = 0;
                                    dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                                    db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                                }*/

                            } else {
                                // Input is greater than the maximum range
//                            editTextRange.setError("Please enter a number within the range");
                            }
                        } else {
                            // Input is less than the minimum value
                            editText.setError("Minimum Range " + minValue);
                        }
                    } else {
                        // Input is empty
//                    editTextRange.setError("Please enter a number");
                    }
                }
            });
            } else {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setHint("Range from " + minValue + " to " + maxValue);
                InputFilter numberFilter = new DecimalFilter(minValue, maxValue);
                editText.setFilters(new InputFilter[]{numberFilter});

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Not needed for this implementation
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Not needed for this implementation
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String input = editText.getText().toString();
                    if (!input.isEmpty()) {
                        int number = Integer.parseInt(input);
                        if (number >= minValue) {
                            // Valid input starting from the minimum value
                            if (number <= maxValue) {
                                // Input falls within the specified range
                                // Perform desired actions here
                                editText.setError(null); // Clear any previous error

                                /*if (editable.length() == 0) {
                                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                                } else {
                                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                                    dmSurveyAnswerTable1.questionID = questionId;
                                    dmSurveyAnswerTable1.answer = editable.toString();
                                    dmSurveyAnswerTable1.answerID = editable.toString();
                                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                                    dmSurveyAnswerTable1.question = question;
                                    int complianceId = 0;
                                    boolean allowCustomActionPlan = false;
                                    boolean isCaptureProof = false;
                                    if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size() != 0) {
                                        if (editText.getText().toString() != null) {
                                            String value = editText.getText().toString().trim();


                                            for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++) {
                                                if (Double.parseDouble(value) < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange) {
                                                    complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;
                                                    allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                                                    isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                                                }
                                            }

                                        }
                                    }


                                    dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
                                    dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
                                    dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
                                    dmSurveyAnswerTable1.actionPlanText = "";
                                    dmSurveyAnswerTable1.actionplanId = "";
                                    dmSurveyAnswerTable1.amount = 0;
                                    dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                                    db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                                }*/


                            } else {
                                // Input is greater than the maximum range
//                            editTextRange.setError("Please enter a number within the range");
                            }
                        } else {
                            // Input is less than the minimum value
                            editText.setError("Minimum Range " + minValue);
                        }
                    } else {
                        // Input is empty
//                    editTextRange.setError("Please enter a number");
                    }
                }
            });

            }





        TextView textView = new TextView(getActivity());
        textView.setText("Please enter any value!");
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);
        textView.setTag(questionId);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID);
        if (dmSurveyAnswerTableList.size() != 0) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answer;
            editText.setText(answerIdDb);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                } else {
                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID);
                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                    dmSurveyAnswerTable1.questionID = questionId;
                    dmSurveyAnswerTable1.answer = charSequence.toString();
                    dmSurveyAnswerTable1.answerID = charSequence.toString();
                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                    dmSurveyAnswerTable1.question = question;
                    int complianceId = 0;
                    boolean allowCustomActionPlan = false;
                    boolean isCaptureProof = false;
                    if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size() != 0) {
                        if (editText.getText().toString() != null) {
                            String value = editText.getText().toString().trim();


                            for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++) {

//                                allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
//                                isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
//                                complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;
                                try {
                                    if (Objects.equals(value, "-")) {
                                        complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;
                                        allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                                        isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                                        dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
                                        dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
                                        dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
                                        dmSurveyAnswerTable1.actionPlanText = "";
                                        dmSurveyAnswerTable1.actionplanId = "";
                                        dmSurveyAnswerTable1.amount = 0;
                                        dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                                        return;
                                    }
                                    else if (Objects.equals(value, "-" + value) && db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange ) {
                                        complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;
                                        allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                                        isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                                        dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
                                        dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
                                        dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
                                        dmSurveyAnswerTable1.actionPlanText = "";
                                        dmSurveyAnswerTable1.actionplanId = "";
                                        dmSurveyAnswerTable1.amount = 0;
                                        dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                                        return;
                                    }
                                    else if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange) {
                                        allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
                                        isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
                                        complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;
                                        dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
                                        dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
                                        dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
                                        dmSurveyAnswerTable1.actionPlanText = "";
                                        dmSurveyAnswerTable1.actionplanId = "";
                                        dmSurveyAnswerTable1.amount = 0;
                                        dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                                        return;
                                    }
                                }catch (Exception e){e.printStackTrace();}
                            }

                        }
                    }



                }


            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        //editText.getLayoutParams().width=40;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
        }

        // Add Spinner to LinearLayout
        if (dm_Question != null) {
            dm_Question.addView(editText);
            dm_Question.addView(textView);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        db = OFISRoomDB.getDbInstance(getActivity());

        dm_Section.removeAllViews();
        dm_Question.removeAllViews();

        List<DmSectionsItem> dmSectionsItemList = new ArrayList<>();
        dmSectionsItemList.clear();
        dmSectionsItemList = db.dmSectionsItemDao().getDmSectionsByModuleId(Integer.parseInt(Constants.MODULE_ID));
        displaySectionItems(dmSectionsItemList);

        List<DmQuestionsModule> dmQuestionsModuleList = new ArrayList<>();
        dmQuestionsModuleList.clear();
        dmQuestionsModuleList = db.dmQuestionsModuleDao().getDmQuestionsModulebyModuleId(Integer.parseInt(Constants.MODULE_ID));
        // List<DmQuestionsModule> dmQuestionsModuleList = db.dmQuestionsModuleDao().getDmQuestionsModulebyModuleId(262);
        displayQuestionsModuleItems(dmQuestionsModuleList);

        ll_servey_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<String> qId = Constants.questionIds;

                ArrayList<String> qIdSections = Constants.questionIdsSection;



                /*for (int m = 0; m < qId.size(); m++){
                    Boolean isMandate = db.dmQuestionsSubModuleDao().getDmQuestionType(Integer.parseInt(qId.get(m))).get(0).mandate;
                    if (isMandate){

                    }
                }

                for (int n = 0; n < qIdSections.size(); n++) {
                    Boolean isMandate = db.dmQuestionsSubModuleDao().getDmQuestionType(Integer.parseInt(qIdSections.get(n))).get(0).mandate;
                }*/


                if (db.dmSurveyAnswerTableDao().getDataByModuleId(Constants.MODULE_ID, Constants.FARMER_ID).size() == Constants.questionIds.size() + Constants.questionIdsSection.size()) {
                    ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                    viewPager.setCurrentItem(1);
                    for (int i = 0; i < Constants.questionIds.size(); i++) {
                        View desiredView = dm_Question.findViewWithTag(qId.get(i));
                        TextView textView = (TextView) desiredView;
                        textView.setVisibility(View.GONE);
                    }

                    for (int j = 0; j < Constants.questionIdsSection.size(); j++) {
                        View desiredView = dm_Section.findViewWithTag(qIdSections.get(j));
                        TextView textView = (TextView) desiredView;
                        textView.setVisibility(View.GONE);
                    }


                } else {
                    for (int i = 0; i < Constants.questionIds.size(); i++) {


                        String type = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qId.get(i))).get(0).displayType;
//                        Boolean isMandate = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qId.get(i))).get(0).mandate;


                        if (type.equalsIgnoreCase("text box")) {

                            View desiredView = dm_Question.findViewWithTag(qId.get(i));
                            if (desiredView != null) {
                                TextView textView = (TextView) desiredView;
                                if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID).size() == 0) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                // View not found
                            }

                        } else if (type.equalsIgnoreCase("dropdown")) {
                            View desiredView = dm_Question.findViewWithTag(qId.get(i));
                            if (desiredView != null) {
                                TextView textView = (TextView) desiredView;
                                if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID).size() == 0) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                // View not found
                            }

                        }

                    }

                    for (int j = 0; j < Constants.questionIdsSection.size(); j++) {

                        String type = db.dmQuestionsSubModuleDao().getDmQuestionType(Integer.parseInt(qIdSections.get(j))).get(0).displayType;
//                        Boolean isMandate = db.dmQuestionsSubModuleDao().getDmQuestionType(Integer.parseInt(qIdSections.get(j))).get(0).mandate;
                        if (type.equalsIgnoreCase("text box")) {

                            View desiredView = dm_Section.findViewWithTag(qIdSections.get(j));
                            if (desiredView != null) {
                                TextView textView = (TextView) desiredView;
                                if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qIdSections.get(j), Constants.FARMER_ID).size() == 0) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                // View not found
                            }

                        } else if (type.equalsIgnoreCase("dropdown")) {
                            View desiredView = dm_Section.findViewWithTag(qIdSections.get(j));
                            if (desiredView != null) {
                                TextView textView = (TextView) desiredView;
                                if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qIdSections.get(j), Constants.FARMER_ID).size() == 0) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    textView.setVisibility(View.GONE);
                                }
                            } else {
                                // View not found
                            }

                        }

                    }
                }

            }
        });
    }
}


