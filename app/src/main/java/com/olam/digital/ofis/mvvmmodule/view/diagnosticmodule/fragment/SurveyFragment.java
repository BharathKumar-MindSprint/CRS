package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DecimalFilter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.NegativeDecimalFilter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.NumberInputFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;


public class SurveyFragment extends Fragment implements View.OnClickListener{
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


        ll_servey_back.setOnClickListener(this);

        ll_servey_next.setOnClickListener(this);


        return view;
    }

    private void displaySectionQuestionItems(int section, List<DmQuestionsModule> dmQuestionsSubModuleList) {
        for (int j = 0; j < dmQuestionsSubModuleList.size(); j++) {

            DmQuestionsModule dmQuestionsSubModule = dmQuestionsSubModuleList.get(j);
            dmQuestionsSubModule.question = Utilities.getNamesByLanguage(dmQuestionsSubModule);
            TextView textView = new TextView(getActivity());

            String star = "*";
            SpannableStringBuilder builder = new SpannableStringBuilder();

            // Append the first string (s1)
            builder.append((j + 1) + "." + dmQuestionsSubModule.question);

            // Append the second string (s2) with red color
            int start = builder.length();
            builder.append(" " + star);  // Add space for separation

            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(colorSpan, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (dmQuestionsSubModule.isCriticalQuest || dmQuestionsSubModule.mandate){
                textView.setText(builder);
            }else{
                textView.setText((j + 1) + "." + dmQuestionsSubModule.question);
            }


            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(14);
            textView.setTypeface(null, Typeface.BOLD);
            dm_Section.addView(textView);

            Constants.questionIdsSection.add(String.valueOf(dmQuestionsSubModule.id));
            if(dmQuestionsSubModule.mandate){
                Constants.mandatedQuestionIds.add(String.valueOf(dmQuestionsSubModule.id));
            }

            if (dmQuestionsSubModule.displayType.equalsIgnoreCase("dropdown")) {
                List<DmLookupValuesItem> dmLookupValuesISubmoduleList = new ArrayList<>();
                dmLookupValuesISubmoduleList.clear();
                dmLookupValuesISubmoduleList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(dmQuestionsSubModule.id);
                renderDropDownQuestionSections(String.valueOf(dmQuestionsSubModule.id), dmLookupValuesISubmoduleList, dmQuestionsSubModule.question);
            } else if (dmQuestionsSubModule.displayType.equalsIgnoreCase("text box")) {
                renderTextboxQuestionSections(String.valueOf(dmQuestionsSubModule.id), dmQuestionsSubModule.question);
            } else if (dmQuestionsSubModule.displayType.equalsIgnoreCase("checkbox")) {
                List<DmLookupValuesItem> dmLookupValuesItemList = new ArrayList<>();
                dmLookupValuesItemList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(dmQuestionsSubModule.id);
                renderCheckboxQuestionModule(String.valueOf(dmQuestionsSubModule.id), dmLookupValuesItemList, dmQuestionsSubModule.question, true);
            }

        }

    }

    private void displaySectionItems(List<DmSectionsItem> dmSectionsItemList) {
        for (int i = 0; i < dmSectionsItemList.size(); i++) {
            DmSectionsItem dmSectionsItem = dmSectionsItemList.get(i);
            TextView textView = new TextView(getActivity());
            textView.setText(Utilities.getModuleSectionByLanguage(dmSectionsItemList.get(i), true));
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(14);
            textView.setPadding(0, 10, 0, 0);
            textView.setTypeface(null, Typeface.BOLD);


            TextView textViewDesc = new TextView(getActivity());
            textViewDesc.setText(Utilities.getModuleSectionByLanguage(dmSectionsItemList.get(i), false));
            textViewDesc.setTextColor(getResources().getColor(R.color.black));
            textViewDesc.setTextSize(14);
            textViewDesc.setTypeface(null, Typeface.ITALIC);



            dm_Section.addView(textView);
            dm_Section.addView(textViewDesc);
            List<DmQuestionsModule> dmQuestionsSubModuleList = new ArrayList<>();
            dmQuestionsSubModuleList.clear();
            dmQuestionsSubModuleList = db.dmQuestionsModuleDao().getDmQuestionsSubModulebySectionId(dmSectionsItem.id);
            displaySectionQuestionItems(i + 1, dmQuestionsSubModuleList);

        }
    }

    private void displayQuestionsModuleItems(List<DmQuestionsModule> dmQuestionsModuleList) {
        Constants.questionIds.clear();
        for (int m = 0; m < dmQuestionsModuleList.size(); m++) {
            DmQuestionsModule dmQuestionsModule = dmQuestionsModuleList.get(m);
            dmQuestionsModule.question = Utilities.getNamesByLanguage(dmQuestionsModule);
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
            if(dmQuestionsModule.mandate){
                Constants.mandatedQuestionIds.add(String.valueOf(dmQuestionsModule.id));
            }

            if (dmQuestionsModule.displayType.equalsIgnoreCase("dropdown")) {
                List<DmLookupValuesItem> dmLookupValuesItemList = new ArrayList<>();
                dmLookupValuesItemList.clear();
                dmLookupValuesItemList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(dmQuestionsModule.id);
                renderDropDownQuestionModule(String.valueOf(dmQuestionsModule.id), dmLookupValuesItemList, dmQuestionsModule.question);
            } else if (dmQuestionsModule.displayType.equalsIgnoreCase("text box")) {
                renderTextboxQuestionModule(String.valueOf(dmQuestionsModule.id), dmQuestionsModule.question);
            } else if (dmQuestionsModule.displayType.equalsIgnoreCase("checkbox")) {
                List<DmLookupValuesItem> dmLookupValuesItemList = new ArrayList<>();
                dmLookupValuesItemList = db.dmLookupValuesItemDao().getDmAnswersModuleByQuestionId(dmQuestionsModule.id);
                renderCheckboxQuestionModule(String.valueOf(dmQuestionsModule.id), dmLookupValuesItemList, dmQuestionsModule.question, false);
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

        list.add(getResources().getString(R.string.ModuleAnswerHint));
        ids.add("0");
        complianceIds.add("0");
        allowCustomActionPlan.add(false);
        isCaptureProof.add(false);

        for (int k = 0; k < dmLookupValuesISubmoduleList.size(); k++) {
            list.add(Utilities.getLookupValueByLanguage(dmLookupValuesISubmoduleList.get(k)));
            ids.add(String.valueOf(dmLookupValuesISubmoduleList.get(k).id));
            complianceIds.add(String.valueOf(dmLookupValuesISubmoduleList.get(k).complianceId));
            allowCustomActionPlan.add(dmLookupValuesISubmoduleList.get(k).allowCustomActionPlan);
            isCaptureProof.add(dmLookupValuesISubmoduleList.get(k).isCaptureProof);
        }


        final Spinner spinner = new Spinner(getActivity());
        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, R.id.ofis_spinner_text_view, list);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dm);
        spinner.setAdapter(spinnerAdapter);
        spinner.setTag(questionId+2);

        TextView textView = new TextView(getActivity());
        textView.setText(getResources().getString(R.string.dm_please_enter_any_drop_down_value));
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);
        textView.setTag(questionId);

        String answerIdDb = "0";

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID, Constants.PLOT_ID);
        if (!dmSurveyAnswerTableList.isEmpty()) {
            answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
        }

        int selectedPosition = 0;

        for (int i = 0; i < ids.size(); i++) {
            if (answerIdDb.equalsIgnoreCase(ids.get(i))) {
                selectedPosition = i;
            }

        }

        final boolean[] isUserInteraction = {false};
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isUserInteraction[0] = true;
                return false;
            }
        });

        spinner.setSelection(selectedPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID, Constants.PLOT_ID);
                } else {
                    String answerId = ids.get(position);
                    List<DmSurveyAnswerTable> dmSurveyAnswers = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID, Constants.PLOT_ID);
                    if(dmSurveyAnswers.isEmpty())
                    {
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
                        dmSurveyAnswerTable1.plotId = Constants.PLOT_ID;
                        dmSurveyAnswerTable1.isSynced = false;
                        dmSurveyAnswerTable1.displayType = "dropdown";
                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);

                        if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionId, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                            db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionId,Constants.FARMER_ID,0, Constants.PLOT_ID);
                        }
                    }
                    else
                    {
                        DmSurveyAnswerTable dmSurveyAnswer = dmSurveyAnswers.get(0);
                        String actionPlanId = dmSurveyAnswer.actionplanId;
                        String actionPlanText = dmSurveyAnswer.actionPlanText;
                        int amount = dmSurveyAnswer.amount;
                        db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID, Constants.PLOT_ID);
                        DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                        dmSurveyAnswerTable1.questionID = questionId;
                        dmSurveyAnswerTable1.answerID = ids.get(position);
                        dmSurveyAnswerTable1.answer = list.get(position);
                        dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                        dmSurveyAnswerTable1.question = question;
                        dmSurveyAnswerTable1.complinceTypeId = complianceIds.get(position);
                        dmSurveyAnswerTable1.amount = amount;
                        dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                        dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan.get(position);
                        dmSurveyAnswerTable1.isCaptureProof = isCaptureProof.get(position);
                        dmSurveyAnswerTable1.plotId = Constants.PLOT_ID;
                        dmSurveyAnswerTable1.isSynced = false;
                        dmSurveyAnswerTable1.displayType = "dropdown";

                        if (isUserInteraction[0]) {
                            if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionId, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                                db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionId,Constants.FARMER_ID,0, Constants.PLOT_ID);
                            }
                            dmSurveyAnswerTable1.actionplanId = "";
                            dmSurveyAnswerTable1.actionPlanText = "";
                        } else {
                            dmSurveyAnswerTable1.actionplanId = actionPlanId;
                            dmSurveyAnswerTable1.actionPlanText = actionPlanText;
                        }
                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                    }
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
        editText.setTextSize(14);
        editText.setTag(questionId + 1);

        double minValue = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).minimumRange;
        double maxValue = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).maximumRange;

        if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowNegative && db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowDecimal) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
            NegativeDecimalFilter negativeDecimalFilter = new NegativeDecimalFilter(minValue, maxValue);
            editText.setHint(String.format(getContext().getString(R.string.dm_textbox_range_decimal), minValue, maxValue));
            editText.setFilters(new InputFilter[] { negativeDecimalFilter });

        }else if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowNegative) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
            editText.setHint(String.format(getContext().getString(R.string.dm_textbox_range_integer), (int) minValue, (int) maxValue));
            NumberInputFilter numberFilter = new NumberInputFilter(minValue, maxValue);
            editText.setFilters(new InputFilter[]{numberFilter});
        }
        else if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowDecimal) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            DecimalFilter decimalFilter = new DecimalFilter(minValue, maxValue, editText, true, getContext());
            editText.setHint(String.format(getContext().getString(R.string.dm_textbox_range_decimal), minValue, maxValue));
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


                            } else {
                                // Input is greater than the maximum range
//                            editTextRange.setError("Please enter a number within the range");
                            }
                        } else {
                            // Input is less than the minimum value
                            editText.setError(String.format(getContext().getString(R.string.minimum_range_error), minValue));
                        }
                    } else {
                        // Input is empty
//                    editTextRange.setError("Please enter a number");
                    }
                }
            });
        }
        else {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setHint(String.format(getContext().getString(R.string.dm_textbox_range_integer), (int) minValue, (int) maxValue));
            InputFilter numberFilter = new DecimalFilter(minValue, maxValue, editText, false, getContext());
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


                            } else {
                                // Input is greater than the maximum range
//                            editTextRange.setError("Please enter a number within the range");
                            }
                        } else {
                            // Input is less than the minimum value
                            editText.setError(String.format(getContext().getString(R.string.minimum_range_error), minValue));
                        }
                    } else {
                        // Input is empty
//                    editTextRange.setError("Please enter a number");
                    }
                }
            });
        }

        TextView textView = new TextView(getActivity());
        textView.setText(getResources().getString(R.string.dm_please_enter_any_value));
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);
        textView.setTag(questionId);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID, Constants.PLOT_ID);

        if (!dmSurveyAnswerTableList.isEmpty()) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answer;
            editText.setText(answerIdDb);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId, Constants.FARMER_ID, Constants.PLOT_ID);
                if (charSequence.length() != 0) {
                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                    dmSurveyAnswerTable1.questionID = questionId;
                    dmSurveyAnswerTable1.answer = charSequence.toString();
                    dmSurveyAnswerTable1.answerID = "";
                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                    dmSurveyAnswerTable1.question = question;
                    dmSurveyAnswerTable1.plotId = Constants.PLOT_ID;
                    if (!db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).isEmpty()) {
                        if (editText.getText().toString() != null) {
                            String value = editText.getText().toString().trim();
                            for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++)
                                try {
                                    if (Objects.equals(value, "-")) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;
                                    }
                                    else if (Objects.equals(value, "-" + value) && db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange ) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;
                                    }
                                    else if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;

                                    }
                                    else if (Objects.equals(value, "-" + value) && db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange > Double.parseDouble(value)) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;
                                    }
                                    else if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange > Double.parseDouble(value)) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1,questionId, j);
                                        return ;
                                    }
                                }catch (Exception e){e.printStackTrace();

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
        if (dm_Section != null) {
            dm_Section.addView(editText);
            dm_Section.addView(textView);
        }

    }


    public void renderDropDownQuestionModule(String questionId, List<DmLookupValuesItem> dmLookupValuesItemList, String question) {
        final List<String> list = new ArrayList<String>();
        final List<String> ids = new ArrayList<String>();
        final List<String> complianceIds = new ArrayList<String>();
        final List<Boolean> allowCustomActionPlan = new ArrayList<Boolean>();
        final List<Boolean> isCaptureProof = new ArrayList<Boolean>();

        list.add(getResources().getString(R.string.ModuleAnswerHint));
        ids.add("0");
        complianceIds.add("0");
        allowCustomActionPlan.add(false);
        isCaptureProof.add(false);

        for (int k = 0; k < dmLookupValuesItemList.size(); k++) {
            list.add(Utilities.getLookupValueByLanguage(dmLookupValuesItemList.get(k)));
            ids.add(String.valueOf(dmLookupValuesItemList.get(k).id));
            complianceIds.add(String.valueOf(dmLookupValuesItemList.get(k).complianceId));
            allowCustomActionPlan.add(dmLookupValuesItemList.get(k).allowCustomActionPlan);
            isCaptureProof.add(dmLookupValuesItemList.get(k).isCaptureProof);
        }


        final Spinner spinner = new Spinner(getActivity());
        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, R.id.ofis_spinner_text_view, list);
         spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dm);
        spinner.setTag(questionId+2);
        spinner.setAdapter(spinnerAdapter);


        TextView textView = new TextView(getActivity());
        textView.setText(getResources().getString(R.string.dm_please_enter_any_drop_down_value));
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);
        textView.setTag(questionId);

        String answerIdDb = "0";

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID, Constants.PLOT_ID);
        if (!dmSurveyAnswerTableList.isEmpty()) {
            answerIdDb = dmSurveyAnswerTableList.get(0).answerID;
        }

        int selectedPosition = 0;

        for (int i = 0; i < ids.size(); i++) {
            if (answerIdDb.equalsIgnoreCase(ids.get(i))) {
                selectedPosition = i;
            }

        }

        final boolean[] isUserInteraction = {false};
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isUserInteraction[0] = true;
                return false;
            }
        });

        spinner.setSelection(selectedPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID, Constants.PLOT_ID);
                } else {
                    String answerId = ids.get(position);
                    List<DmSurveyAnswerTable> dmSurveyData = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId,Constants.FARMER_ID, Constants.PLOT_ID);
                    if(dmSurveyData.isEmpty())
                    {
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
                        dmSurveyAnswerTable1.plotId = Constants.PLOT_ID;
                        dmSurveyAnswerTable1.isSynced = false;
                        dmSurveyAnswerTable1.displayType = "dropdown";
                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                        if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionId, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                            db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionId,Constants.FARMER_ID,0, Constants.PLOT_ID);
                        }
                    }
                    else
                    {
                        DmSurveyAnswerTable dmSurveyAnswer = dmSurveyData.get(0);
                        String actionPlanId = dmSurveyAnswer.actionplanId;
                        String actionPlanText = dmSurveyAnswer.actionPlanText;
                        int amount = dmSurveyAnswer.amount;
                        db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID, Constants.PLOT_ID);
                        DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                        dmSurveyAnswerTable1.questionID = questionId;
                        dmSurveyAnswerTable1.answerID = ids.get(position);
                        dmSurveyAnswerTable1.answer = list.get(position);
                        dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                        dmSurveyAnswerTable1.question = question;
                        dmSurveyAnswerTable1.complinceTypeId = complianceIds.get(position);
                        dmSurveyAnswerTable1.amount = amount;
                        dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                        dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan.get(position);
                        dmSurveyAnswerTable1.isCaptureProof = isCaptureProof.get(position);
                        dmSurveyAnswerTable1.plotId = Constants.PLOT_ID;
                        dmSurveyAnswerTable1.isSynced = false;
                        dmSurveyAnswerTable1.displayType = "dropdown";

                        if (isUserInteraction[0]) {
                            if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionId, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                                db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionId,Constants.FARMER_ID,0, Constants.PLOT_ID);
                            }
                            dmSurveyAnswerTable1.actionplanId = "";
                            dmSurveyAnswerTable1.actionPlanText = "";
                        } else {
                            dmSurveyAnswerTable1.actionplanId = actionPlanId;
                            dmSurveyAnswerTable1.actionPlanText = actionPlanText;
                        }
                        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                    }
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
        editText.setTextSize(14);
        editText.setTag(questionId + 1);

        double minValue = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).minimumRange;
        double maxValue = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).maximumRange;

        if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowNegative && db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowDecimal) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
            NegativeDecimalFilter negativeDecimalFilter = new NegativeDecimalFilter(minValue, maxValue);
            editText.setHint(String.format(getContext().getString(R.string.dm_textbox_range_decimal), minValue, maxValue));
            editText.setFilters(new InputFilter[] { negativeDecimalFilter });

        } else if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowNegative) {
                editText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
                editText.setHint(String.format(getContext().getString(R.string.dm_textbox_range_integer), (int) minValue, (int) maxValue));
                NumberInputFilter numberFilter = new NumberInputFilter(minValue, maxValue);
                editText.setFilters(new InputFilter[]{numberFilter});

        } else if (db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(0).isAllowDecimal) {
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            DecimalFilter decimalFilter = new DecimalFilter(minValue, maxValue, editText, true, getContext());
            editText.setHint(String.format(getContext().getString(R.string.dm_textbox_range_decimal), minValue, maxValue));
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
                            if (number <= maxValue) {
                                editText.setError(null);
                            }
                        } else {
                            // Input is less than the minimum value
                            editText.setError(String.format(getContext().getString(R.string.minimum_range_error), minValue));
                        }
                    }
                }
            });
        } else {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setHint(String.format(getContext().getString(R.string.dm_textbox_range_integer), (int) minValue, (int) maxValue));
            InputFilter numberFilter = new DecimalFilter(minValue, maxValue, editText, false, getContext());
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
                            if (number <= maxValue) {
                                editText.setError(null);
                            }
                        } else {
                            editText.setError(String.format(getContext().getString(R.string.minimum_range_error), minValue));
                        }
                    }
                }
            });
        }

        TextView textView = new TextView(getActivity());
        textView.setText(getResources().getString(R.string.dm_please_enter_any_value));
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);
        textView.setTag(questionId);

        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID, Constants.PLOT_ID);
        if (!dmSurveyAnswerTableList.isEmpty()) {
            String answerIdDb = dmSurveyAnswerTableList.get(0).answer;
            editText.setText(answerIdDb);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                db.dmSurveyAnswerTableDao().deleteByQuestionId(questionId,Constants.FARMER_ID, Constants.PLOT_ID);

                if (charSequence.length() != 0) {
                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                    dmSurveyAnswerTable1.questionID = questionId;
                    dmSurveyAnswerTable1.answer = charSequence.toString();
                    dmSurveyAnswerTable1.answerID = "";
                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                    dmSurveyAnswerTable1.question = question;
                    dmSurveyAnswerTable1.plotId = Constants.PLOT_ID;
                    if (!db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).isEmpty()) {
                        if (editText.getText().toString() != null) {
                            String value = editText.getText().toString().trim();

                            for (int j = 0; j < db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).size(); j++) {
                                try {
                                    if (Objects.equals(value, "-")) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;
                                    }
                                    else if (Objects.equals(value, "-" + value) && db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange ) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;
                                    }
                                    else if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange <= Double.parseDouble(value) && Double.parseDouble(value) <= db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).maximumRange) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;
                                    }
                                    else if (Objects.equals(value, "-" + value) && db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange > Double.parseDouble(value)) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;
                                    } else if (db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).minimumRange > Double.parseDouble(value)) {

                                        saveTextBoxAnswer(dmSurveyAnswerTable1, questionId, j);
                                        return ;
                                    }
                                }catch (Exception e){e.printStackTrace();

                                }
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
    public void renderCheckboxQuestionModule(String questionId, List<DmLookupValuesItem> dmLookupValuesItemList, String question, boolean subSection) {
        final List<String> list = new ArrayList<String>();
        final List<String> ids = new ArrayList<String>();
        final List<String> complianceIds = new ArrayList<String>();
        final List<Boolean> allowCustomActionPlan = new ArrayList<Boolean>();
        final List<Boolean> isCaptureProof = new ArrayList<Boolean>();

        list.add(getResources().getString(R.string.ModuleAnswerHint));
        ids.add("0");
        complianceIds.add("0");
        allowCustomActionPlan.add(false);
        isCaptureProof.add(false);

        Integer maxSelection = db.dmQuestionsModuleDao().getMaxSelectionCount(questionId);
        int[] selectedCheckboxCount = {0};
        for (int k = 0; k < dmLookupValuesItemList.size(); k++) {
            list.add(Utilities.getLookupValueByLanguage(dmLookupValuesItemList.get(k)));
            ids.add(String.valueOf(dmLookupValuesItemList.get(k).id));
            complianceIds.add(String.valueOf(dmLookupValuesItemList.get(k).complianceId));
            allowCustomActionPlan.add(dmLookupValuesItemList.get(k).allowCustomActionPlan);
            isCaptureProof.add(dmLookupValuesItemList.get(k).isCaptureProof);
        }

        LayoutInflater inflaterCheckbox = LayoutInflater.from(getContext());
        final TableLayout tl = (TableLayout) inflaterCheckbox.inflate(R.layout.module_checkbox_table_dm, null);
        tl.setSaveEnabled(true);
        tl.setTag(questionId + 3);

        if (!dmLookupValuesItemList.isEmpty()) {

            // Get all checked answers
            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(questionId, Constants.FARMER_ID, Constants.PLOT_ID);
            Set<Integer> answerIdDbSet = new HashSet<>();
            for (DmSurveyAnswerTable answer : dmSurveyAnswerTableList) {
                selectedCheckboxCount[0]++;
                answerIdDbSet.add(Integer.valueOf(answer.answerID));
            }

            for (DmLookupValuesItem item : dmLookupValuesItemList) {

                TableRow tr = (TableRow) inflaterCheckbox.inflate(R.layout.module_checkbox_table_row, tl, false);

                TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                TextView tv = (TextView) tr.findViewById(R.id.cbTitle);
                tv.setPadding(40,0,0,0);
                CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);
                tvId.setText(String.valueOf(item.id));
                tv.setText(Utilities.getLookupValueByLanguage(item));

                if (answerIdDbSet.contains(item.id)) {
                    cb.setChecked(true);
                }

                tl.addView(tr);

            }

        }
        TextView textView = new TextView(getActivity());
        textView.setText("Please enter any checkbox value");
        textView.setTextColor(getResources().getColor(R.color.red_view_survey));
        textView.setTextSize(12);
        textView.setPadding(0, 15, 0, 0);
        textView.setVisibility(View.GONE);
        textView.setTag(questionId);

        for (int i = 0; i < tl.getChildCount(); i++) {
            View child = tl.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                CheckBox cb = (CheckBox) row.findViewById(R.id.cbCheckBox);
                TextView tvId = (TextView) row.findViewById(R.id.cbId);
                TextView tv = (TextView) row.findViewById(R.id.cbTitle);

                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String id = tvId.getText().toString();
                        String value = tv.getText().toString();
                        DmLookupValuesItem lookupValuesItemsById = db.dmLookupValuesItemDao().getAllValuesByLookupId(Integer.parseInt(id));
                        if (isChecked) {
                            if (selectedCheckboxCount[0] < maxSelection) {
                                selectedCheckboxCount[0]++;
                                Log.d("CheckBox", "Checked ID: " + id);
                                List<DmSurveyAnswerTable> dmSurveyData = db.dmSurveyAnswerTableDao().getDataByAnswerId(id,Constants.FARMER_ID, Constants.PLOT_ID);
                                if(dmSurveyData.isEmpty()) {
                                    DmSurveyAnswerTable dmSurveyAnswerTable1 = new DmSurveyAnswerTable();
                                    dmSurveyAnswerTable1.questionID = questionId;
                                    dmSurveyAnswerTable1.answerID = id;
                                    dmSurveyAnswerTable1.answer = value;
                                    dmSurveyAnswerTable1.moduleId = Constants.MODULE_ID;
                                    dmSurveyAnswerTable1.question = question;
                                    dmSurveyAnswerTable1.complinceTypeId = String.valueOf(lookupValuesItemsById.complianceId);
                                    dmSurveyAnswerTable1.actionplanId = "";
                                    dmSurveyAnswerTable1.actionPlanText = "";
                                    dmSurveyAnswerTable1.amount = 0;
                                    dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
                                    dmSurveyAnswerTable1.allowCustomActionPlan = lookupValuesItemsById.allowCustomActionPlan;
                                    dmSurveyAnswerTable1.isCaptureProof = lookupValuesItemsById.isCaptureProof;
                                    dmSurveyAnswerTable1.plotId = Constants.PLOT_ID;
                                    dmSurveyAnswerTable1.displayType = Constants.CHECKBOX;
                                    dmSurveyAnswerTable1.isSynced = false;
                                    db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
                                    if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionId, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                                        db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionId,Constants.FARMER_ID,0, Constants.PLOT_ID);
                                    }
                                    db.dmSurveyAnswerTableDao().updateActionPlanBudget(questionId, Constants.FARMER_ID, Constants.PLOT_ID);
                                }
                            } else {
                                cb.setChecked(false);
                                Toast.makeText(getContext(), "You can only select " + maxSelection + " options", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            selectedCheckboxCount[0]--;
                            Log.d("CheckBox", "Unchecked ID: " + id);
                            db.dmSurveyAnswerTableDao().deleteByAnswerId(id, Constants.FARMER_ID, Constants.PLOT_ID);
                            db.dmSurveyAnswerTableDao().updateActionPlanBudget(questionId, Constants.FARMER_ID, Constants.PLOT_ID);
                            if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionId, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                                db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionId,Constants.FARMER_ID,0, Constants.PLOT_ID);
                            }
                        }
                    }
                });

            }
        }

        if (subSection) {
            if (dm_Section != null) {
                dm_Section.addView(tl);
                dm_Section.addView(textView);
            }
        } else {
            if (this.dm_Question != null) {
                this.dm_Question.addView(tl);
                this.dm_Question.addView(textView);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        db = OFISRoomDB.getDbInstance(getActivity());

        dm_Section.removeAllViews();
        dm_Question.removeAllViews();

        List<DmSectionsItem> dmSectionsItemList = db.dmSectionsItemDao().getDmSectionsByModuleId(Integer.parseInt(Constants.MODULE_ID));
        Constants.questionIdsSection.clear();
        displaySectionItems(dmSectionsItemList);

        List<DmQuestionsModule> dmQuestionsModuleList = db.dmQuestionsModuleDao().getDmQuestionsModulebyModuleId(Integer.parseInt(Constants.MODULE_ID));

        displayQuestionsModuleItems(dmQuestionsModuleList);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.ll_servey_next) {
            ArrayList<String> qId = Constants.questionIds;
            ArrayList<String> qIdSections = Constants.questionIdsSection;
            ArrayList<String> validRange = new ArrayList<>();

            // check textbox range value
            for (int i = 0; i < qId.size(); i++) {
                String type = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qId.get(i))).get(0).displayType;
                if (type.equalsIgnoreCase("text box")) {
                    inputTextRangeValidation(qId, i, dm_Question, validRange);
                }
            }

            for (int i = 0; i < qIdSections.size(); i++) {
                String type = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qIdSections.get(i))).get(0).displayType;
                if (type.equalsIgnoreCase("text box")) {
                    inputTextRangeValidation(qIdSections, i, dm_Section, validRange);
                }
            }

            boolean valid = true;
            List<DmQuestionsModule> dmQuestionsMandatedModuleList = db.dmQuestionsModuleDao().getDmQuestionsModuleId(Integer.parseInt(Constants.MODULE_ID));
            if(!dmQuestionsMandatedModuleList.isEmpty()) {
                for (int i = 0; i < dmQuestionsMandatedModuleList.size(); i++) {
                    int qnId = dmQuestionsMandatedModuleList.get(i).id;
                    List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByQuestionId(String.valueOf(qnId), Constants.FARMER_ID, Constants.PLOT_ID);
                    if (!dmSurveyAnswerTableList.isEmpty()) {
                        valid = true;
                    } else {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid && validRange.isEmpty()) {
                ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                viewPager.setCurrentItem(1);

                for (int i = 0; i < qId.size(); i++) {
                    View desiredView = dm_Question.findViewWithTag(qId.get(i));
                    TextView textView = (TextView) desiredView;
                    textView.setVisibility(View.GONE);
                }

                for (int j = 0; j < qIdSections.size(); j++) {
                    try {
                         View desiredView = dm_Section.findViewWithTag(qIdSections.get(j));
                         TextView textView = (TextView) desiredView;
                         textView.setVisibility(View.GONE);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            } else {
                for (int i = 0; i < qId.size(); i++) {
                    String type = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qId.get(i))).get(0).displayType;
                    boolean isMandate = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qId.get(i))).get(0).mandate;

                    if(isMandate){
                        if (type.equalsIgnoreCase("text box")) {
                            errorTextDisplayForTextBoxQuestion(qId, i, dm_Question);
                        } else if (type.equalsIgnoreCase("dropdown")) {
                            errorTextDisplayForDropdownQuestion(qId, i, dm_Question);
                        } else if (type.equalsIgnoreCase("checkbox")) {
                            errorTextDisplayForCheckboxQuestion(qId, i, dm_Question);
                        }
                    }
                }

                for (int j = 0; j < qIdSections.size(); j++) {
                    String type = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qIdSections.get(j))).get(0).displayType;
                    boolean isMandate = db.dmQuestionsModuleDao().getDmQuestionType(Integer.parseInt(qIdSections.get(j))).get(0).mandate;

                    if(isMandate){
                        if (type.equalsIgnoreCase("text box")) {
                            errorTextDisplayForTextBoxQuestion(qIdSections, j, dm_Section);
                        } else if (type.equalsIgnoreCase("dropdown")) {
                            errorTextDisplayForDropdownQuestion(qIdSections, j, dm_Section);
                        } else if (type.equalsIgnoreCase("checkbox")) {
                            errorTextDisplayForCheckboxQuestion(qIdSections, j, dm_Section);
                        }
                    }
                }
            }
        }
        else if(id == R.id.ll_servey_back){
            requireActivity().onBackPressed();
        }
    }

    private void inputTextRangeValidation(ArrayList<String> qId, int i, LinearLayout questionLayout, ArrayList<String> validRange) {
        View desiredView = questionLayout.findViewWithTag(qId.get(i));

        if (desiredView != null) {
            List<DmSurveyAnswerTable> dmAnswerById = db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID, Constants.PLOT_ID);
            if (!dmAnswerById.isEmpty()) {
                double submittedAnswer = Double.parseDouble(dmAnswerById.get(0).answer);
                double minimumRange = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(qId.get(i))).get(0).minimumRange;
                double maximumRange = db.dmTextBoxModuleQuestionsDao().getDmTextBoxByQuestionId(Integer.parseInt(qId.get(i))).get(0).maximumRange;
                if (!(submittedAnswer >= minimumRange && submittedAnswer <= maximumRange)) {
                    validRange.add(qId.get(i));
                }
            }
        }
    }


    private void errorTextDisplayForTextBoxQuestion(ArrayList<String> qId, int i, LinearLayout questionLayout) {
        View desiredView = questionLayout.findViewWithTag(qId.get(i));
        View desiredView2 = questionLayout.findViewWithTag(qId.get(i) + 1);

        if (desiredView != null) {
            TextView textView = (TextView) desiredView;
            EditText editText = (EditText) desiredView2;

            if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                textView.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_error));

                }
            } else {
                textView.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    editText.setBackground(getActivity().getDrawable(R.drawable.answer_text_survey));
                }
            }
        }
    }

    private void errorTextDisplayForDropdownQuestion(ArrayList<String> qId, int i, LinearLayout questionLayout) {
        View desiredView = questionLayout.findViewWithTag(qId.get(i));
        View desiredView2 = questionLayout.findViewWithTag(qId.get(i) + 2);

        if (desiredView != null) {
            TextView textView = (TextView) desiredView;
            Spinner spinner = (Spinner) desiredView2;
            if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                textView.setVisibility(View.VISIBLE);
                spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_error));
            } else {
                textView.setVisibility(View.GONE);
                spinner.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.spinner_bg_light));
            }
        }


    }

    private void errorTextDisplayForCheckboxQuestion(ArrayList<String> qId, int i, LinearLayout questionLayout) {
        View desiredView = questionLayout.findViewWithTag(qId.get(i));
        View desiredView2 = questionLayout.findViewWithTag(qId.get(i) + 3);

        if (desiredView != null) {
            TextView textView = (TextView) desiredView;
            TableLayout tl = (TableLayout) desiredView2;
            if (db.dmSurveyAnswerTableDao().getDataByQuestionId(qId.get(i), Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
                textView.setVisibility(View.VISIBLE);
                LayoutInflater inflaterCheckbox = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflaterCheckbox.inflate(R.layout.module_checkbox_table_dm_error, null);
                tl.setBackground(layout.getBackground());
            } else {
                textView.setVisibility(View.GONE);
                LayoutInflater inflaterCheckbox = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflaterCheckbox.inflate(R.layout.module_checkbox_table_dm, null);
                tl.setBackground(layout.getBackground());
            }
        }
    }

    public void saveTextBoxAnswer(DmSurveyAnswerTable dmSurveyAnswerTable1, String questionId, int j) {
        int complianceId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).complianceId;
        boolean allowCustomActionPlan = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).allowCustomActionPlan;
        boolean isCaptureProof = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).isCaptureProof;
        int answerId = db.dmRangeTextBoxModuleQuestionsTableDao().getDmTextBoxByQuestionId(Integer.parseInt(questionId)).get(j).id;

        dmSurveyAnswerTable1.complinceTypeId = String.valueOf(complianceId);
        dmSurveyAnswerTable1.allowCustomActionPlan = allowCustomActionPlan;
        dmSurveyAnswerTable1.isCaptureProof = isCaptureProof;
        dmSurveyAnswerTable1.displayType = "textbox";
        dmSurveyAnswerTable1.actionPlanText = "";
        dmSurveyAnswerTable1.answerID = String.valueOf(answerId);
        dmSurveyAnswerTable1.actionplanId = "";
        dmSurveyAnswerTable1.amount = 0;
        dmSurveyAnswerTable1.farmerId = Constants.FARMER_ID;
        dmSurveyAnswerTable1.isSynced = false;
        db.dmSurveyAnswerTableDao().insert(dmSurveyAnswerTable1);
        if (!db.dmComplaintFileTableDao().getCaptureProofSize(questionId, Constants.FARMER_ID, Constants.PLOT_ID).isEmpty()) {
            db.dmComplaintFileTableDao().deleteRowsByFarmerAndQuestionID(questionId,Constants.FARMER_ID,0, Constants.PLOT_ID);
        }
    }

}


