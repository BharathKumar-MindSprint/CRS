package com.olam.digital.ofis.mvvmmodule;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.olam.digital.ofis.CompleteModuleActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.DynamicQuestions;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.coffeesurveyconstraints.CoffeeConstants;
import com.olam.digital.ofis.coffeesurveyconstraints.CoffeeConstraints;
import com.olam.digital.ofis.coffeesurveyconstraints.CommonPojo;
import com.olam.digital.ofis.coffeesurveyconstraints.Constraint;
import com.olam.digital.ofis.coffeesurveyconstraints.QuestionInfo;
import com.olam.digital.ofis.coffeesurveyconstraints.utils.DecimalDigitsInputFilter;
import com.olam.digital.ofis.model.FarmerGeo;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class DynamicQuestionsRoomDb {
    /*
    static Resources resourcesforyesno;
    public ArrayList<JSONObject> answerSet;
    public ArrayList<EditText> hundredpercentquestions;
    public int reportingYear;
    public boolean updatingHundredPercent;
    public boolean hasAnswers;
    public boolean viewAnswer;
    public LayoutInflater inflater;
    public String questionColumnSuffix;
    public LinearLayout questionsSection;
    public Context context;
    public static String SurveyType="";
    public static FarmerGeo farmerGeo;

    public ArrayList<EditText> et_list;
    private String mCurrentPhotoPath = null, mCurrentPhotoFileName = null;
    private Uri mCapturedImageURI = null;
    private ImageView mCurrentImageView = null;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private JSONObject mCurrentPhotoMetadata = new JSONObject();


    private static DynamicQuestionsRoomDb instance = null;

    DynamicQuestionsRoomDb(Context context, String questionColumnSuffix) {
        this.answerSet = new ArrayList<>();
        this.hundredpercentquestions = new ArrayList<>();
        this.context = context;
        this.updatingHundredPercent = false;
        this.questionColumnSuffix = questionColumnSuffix;
    }


    public static DynamicQuestionsRoomDb getInstance(Context context, String questionColumnSuffix, DataLayer dl) {
        if (instance == null) {
            instance = new DynamicQuestionsRoomDb(context, questionColumnSuffix, dl);
        }
        return instance;
    }

    public static DynamicQuestionsRoomDb getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    public void renderTextBoxQuestion(final Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        int answerIdTemp = 0;
        if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
            } else {
                if (qCursor.getColumnIndex("answer_id") != -1) {
                    answerIdTemp = qCursor.getInt(qCursor.getColumnIndex("answer_id"));
                }
            }
        }
        final int answerId = answerIdTemp;

        final EditText et = (EditText) inflater.inflate(R.layout.module_edit_text, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        et.setFilters(new InputFilter[]{new DynamicQuestionsRoomDb.EmojiExcludeFilter()});
        et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        et.setTag(questionId);
        if (hasAnswers) {
            String currentAnswerValue = null;
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                currentAnswerValue = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
            } else {
                if (qCursor.getColumnIndex("answer_" + datatype) != -1) {
                    currentAnswerValue = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                }
            }

            if (currentAnswerValue != null) {
                et.setText(currentAnswerValue);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, currentAnswerValue);
            }
        }

        if (datatype.equals("decimal")) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if (datatype.equals("int")) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        }

        // add tv to array if this is a 100% group question
        // get display config
        boolean isHPQTemp = false;
        JSONObject displayConfig = new JSONObject();
        try {
            displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));

            if (displayConfig.has("hundredpercentgroup") && (displayConfig.getBoolean("hundredpercentgroup"))) {
                et.setBackgroundResource(R.drawable.percent_edit_text);
                et.setTag(R.string.hpQId, questionId);
                et.setTag(R.string.hpAId, answerId);
                et.setTag(R.string.hpPqId, parentQuestionId);
                et.setTag(R.string.hpRiP, repeaterItemPosition);
                et.setTag(R.string.hpD, datatype);
                hundredpercentquestions.add(et);
                isHPQTemp = true;
                if ((displayConfig.has("hundredpercentgroup_balancefield")) && (displayConfig.optBoolean("hundredpercentgroup_balancefield", false))) {
                    et.setKeyListener(null);
                    et.setAlpha((float) 0.5);
                    hundredpercentbalance = et;
                    hundredpercentquestions.remove(et);
                    isHPQTemp = false;
                }
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting textbox display config :" +e.getMessage());
        }
        final boolean isHundredPercentQuestion = isHPQTemp;

        final JSONObject finalDisplayConfig = displayConfig;

        // Check the calculation tag Availability
        String calculationNode = displayConfig.optString(CoffeeConstants.CALCULATION,"noTag");
        String defaultValue="";
        try {
            JSONObject jsonObject = new JSONObject(calculationNode);
            defaultValue = jsonObject.getString(CoffeeConstants.DEFAULT);
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        if (!defaultValue.equalsIgnoreCase("")){
            // Condition Verification Code
            int farmerModuleId= (int) Utilities.farmerModuleId;
            Cursor answerQuery = dl.getAnswersByFarmerModuleQuestionId(farmerModuleId, questionId);
            String answer = "";
            if (answerQuery.getCount() > 0) {
                while (answerQuery.moveToNext()) {
                    String datatypeQuestion = answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                    if (datatypeQuestion.equalsIgnoreCase("int")) {
                        answer = String.valueOf(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)));
                    } else if (datatypeQuestion.equalsIgnoreCase("decimal")) {
                        answer = String.valueOf(answerQuery.getString(answerQuery.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL)));
                    }
                }
            }
            if (answer.equalsIgnoreCase("")){
                et.setText(defaultValue);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, defaultValue);
            }else{
                et.setText(answer);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, answer);
            }
        }

        // restrict decimel places - AS-Q1, AS-Q2
        int decimelRestrict= 0;
        try {
            decimelRestrict = CoffeeConstraints.isDecimalRestrict(displayConfig);
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
        if (decimelRestrict>0){
            et.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(decimelRestrict)});
        }
        // Decimal 2 digits concept - End

        Drawable originalDrawable = et.getBackground();
        String finalDefaultValue = defaultValue;
        final String[] initialText = {""};
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                initialText[0] =et.getText().toString().trim();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if ((isHundredPercentQuestion) && (!updatingHundredPercent)) {
                    autofill100Percent(et);
                }
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, et.getText().toString());


                //   Check the dependent questions - OFS-44 Check
                String beforeVal=et.getText().toString().trim();
                boolean dotCheck=false;
                if (beforeVal.length()>0){
                    if (beforeVal.substring(beforeVal.length() - 1).equalsIgnoreCase(".")){
                        dotCheck =true;
                    }
                }
                if(!dotCheck) {
                    ArrayList<CommonPojo> commonPojos = new CoffeeConstraints(context, CompleteModuleActivity.allQuestions, answerSet, qCursor, dl).questionDependent(questionId);
                    for (CommonPojo commonPojo : commonPojos) {
                        if (commonPojo.getAnswerValue() != -1) {
                            QuestionInfo questionInfo = new QuestionInfo();
                            questionInfo.setQuestionId(commonPojo.getQuestionId());
                            Constraint constraint = new Constraint();
                            if (commonPojo.getQuestionId() != 0) {
                                Cursor questionCur = dl.getQuestionById(commonPojo.getQuestionId());
                                String displayConfig1 = null;
                                if (questionCur != null && questionCur.moveToFirst()) {
                                    do {
                                        displayConfig1 = questionCur.getString(questionCur.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION));
                                    } while (questionCur.moveToNext());
                                }
                                try {
                                    JSONObject finalJsonObj = new JSONObject(displayConfig1);
                                    constraint = new CoffeeConstraints(context, questionInfo, dl, answerSet, qCursor).constraintType(finalJsonObj, String.valueOf(commonPojo.getAnswerValue()));
                                } catch (JSONException e) {
                                    FirebaseCrashlytics.getInstance().log(e.getMessage());
                                }
                            }

                            if (constraint != null) {
                                if (constraint.getQuestionId() != 0) {
                                    editQuestionSet.add(constraint.getQuestionId());
                                }
                            }

                            //    Toast.makeText(context, " frank solution : " + editQuestionSet, Toast.LENGTH_SHORT).show();

                        } else {
                            // Toast.makeText(context, "Innum 6th question ans pannala", Toast.LENGTH_SHORT).show();
                        }
                    }
                }




                *//* Contstraints Check- Coffee Survey Setup Project franklin*//*
                String value= et.getText().toString().trim();
                if(value.equalsIgnoreCase("")){
                    // if the default value is available, for empty value is given , insert the default value for that question.
                    if (!finalDefaultValue.equalsIgnoreCase("")){
                        et.setText(finalDefaultValue);
                        et.setSelection(et.getText().length());
                        saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, finalDefaultValue);
                    }else {
                        removeAnswerEntry(questionId);
                    }
                }else{
                    if (!finalDefaultValue.equalsIgnoreCase("")) {
                        if (initialText[0].equalsIgnoreCase("0")){
                            et.setText(value.replace("0", ""));
                            et.setSelection(et.getText().length());
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, et.getText().toString());
                        }
                    }
                }

                QuestionInfo questionInfo=new QuestionInfo();
                questionInfo.setQuestionId(questionId);
                questionInfo.setDataType(datatype);
                Constraint constraint = null;
                try {
                    et.setError(null);
                    constraint = new CoffeeConstraints(context,questionInfo,dl,answerSet,qCursor).constraintType(finalDisplayConfig,value);
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().log(e.getMessage());
                }
                if (constraint !=null){
                    if (constraint.getErrorMsg()!=null){
                        et.setError(constraint.getErrorMsg());
                        et.setBackgroundResource(R.drawable.rounder_red_edittext);
                        // New
                        removeAnswerEntry(questionId);
                        if (constraint.isOverLimit()){
                            constraintsSet.add(questionId);
                        }

                        // Old remove Logic
//                            if (et.getText().toString().trim().equalsIgnoreCase("")){
//                                // Remove the data from existing Database if data persists
//                                removeAnswerEntryDatabase((int) Utilities.farmerModuleId,questionId);
//                            }else{
//                                removeAnswerEntry(questionId);
//                            }

                    }else{
                        et.setBackground(originalDrawable);
                        constraintsSet.remove(questionId);
                        editQuestionSet.remove(questionId);
                        // et.setError(null);
                    }
                }
            }
        });

        try {
            View scroll=CompleteModuleActivity.scrollView;
            // Approach -1
            scroll.setFocusable(true);
            scroll.setFocusableInTouchMode(true);
            scroll.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // v.requestFocus();
                    v.clearFocus();
                    //  v.requestFocusFromTouch();
                    // et.clearFocus();
                    return false;
                }
            });

        }catch (Exception e){ FirebaseCrashlytics.getInstance().log(e.getMessage());}






        if (isRepeaterItem) {
            parentLayout.addView(et);
        } else {
            questionWrapper.addView(et);
            parentLayout.addView(questionWrapper);
        }
    }

    public void renderQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout) {
        TextView tv;
        tv = (TextView) inflater.inflate((isRepeaterItem) ? R.layout.module_repeater_item_question : R.layout.module_question, parentLayout, false);

        int columnIndex = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) : qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION);
        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        tv.setText(Html.fromHtml(qCursor.getString(columnIndex)));
        String hh = Html.fromHtml(qCursor.getString(columnIndex)) + "";

        // get disp"lay config to see if question is required
        try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));

            if (displayConfig.has("surveyrange")) {

                if (datatype.equals("decimal")) {
                    tv.setText(Html.fromHtml(tv.getText().toString() + "(Min Value: " + String.valueOf(BigDecimal.valueOf(displayConfig.getJSONObject("surveyrange").getDouble("min")).floatValue()) +
                            "\t Max value: " + String.valueOf(BigDecimal.valueOf(displayConfig.getJSONObject("surveyrange").getDouble("max")).floatValue()) +")" ), TextView.BufferType.SPANNABLE);

                } else if (datatype.equals("int")) {
                    tv.setText(Html.fromHtml(tv.getText().toString() + "(Min Value: " + String.valueOf(displayConfig.getJSONObject("surveyrange").getInt("min") +
                            "\tMax value: " + String.valueOf(displayConfig.getJSONObject("surveyrange").getInt("max")) +")" )), TextView.BufferType.SPANNABLE);

                }



            }
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                if (display.optBoolean("requiredfield", false)) {
                    tv.setText(Html.fromHtml(tv.getText().toString() + "&nbsp;<sup><font color='red'>*</font></sup>"), TextView.BufferType.SPANNABLE);
                }
            }

        } catch (JSONException e) {
            String questionId = "0";
            if ((qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID) != -1)) {
                questionId = qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
            }
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting question display config - question " + questionId);
        }

        parentLayout.addView(tv);
    }


    private class EmojiExcludeFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    }
    */
}
