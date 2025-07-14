package com.olam.digital.ofis;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;

import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.olam.digital.ofis.childlabour.ChildLabourCompleteModuleActivity;
import com.olam.digital.ofis.coffeesurveyconstraints.CoffeeConstants;
import com.olam.digital.ofis.coffeesurveyconstraints.CoffeeConstraints;
import com.olam.digital.ofis.coffeesurveyconstraints.CommonPojo;
import com.olam.digital.ofis.coffeesurveyconstraints.Constraint;
import com.olam.digital.ofis.coffeesurveyconstraints.QuestionInfo;
import com.olam.digital.ofis.coffeesurveyconstraints.utils.CommonUtilities;
import com.olam.digital.ofis.coffeesurveyconstraints.utils.DecimalDigitsInputFilter;
import com.olam.digital.ofis.model.FarmerGeo;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.Dao.GlobalUnitDao;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.Dao.LocalUnitDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.recordpolygon.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ben on 02/06/2015.
 */
@SuppressWarnings("DefaultFileTemplate")
public class DynamicQuestions {
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
    private final DataLayer dl;
    public static String SurveyType = "";
    public static FarmerGeo farmerGeo;

    public ArrayList<EditText> et_list;
    private String mCurrentPhotoPath = null, mCurrentPhotoFileName = null;
    private Uri mCapturedImageURI = null;
    private ImageView mCurrentImageView = null;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private JSONObject mCurrentPhotoMetadata = new JSONObject();
    Resources resources;
    private LinearLayout densityMainSection, densityPlotSection;
    private EditText hundredpercentbalance;
    private String[] densitySubAnswers;
    private int densityQuestionId, densityAnswerId = 0;
    private TextView plotsHeader, densityResult;
    private Hashtable<String, String> plotvariants;
    public Set<Integer> surveySet = new HashSet<>();
    public Set<Integer> constraintsSet = new HashSet<>();
    public Set<Integer> editQuestionSet = new HashSet<>();
    private static DynamicQuestions instance = null;
    public OFISRoomDB roomDB;
    private String mCurrentSignaturePhotoPath = null;
    private final static int REQUEST_IMAGE_GALLERY = 2;
    private static final String[] ACCEPTED_MIME_TYPES_IMG = {"image/jpeg", "image/png"};
    private String imageURL = "";
    //    private ImageView imageViewInfo;

    DynamicQuestions(Context context, String questionColumnSuffix, DataLayer dl) {
        this.answerSet = new ArrayList<>();
        this.hundredpercentquestions = new ArrayList<>();
        this.context = context;
        this.dl = dl;
        this.updatingHundredPercent = false;
        this.questionColumnSuffix = questionColumnSuffix;
        this.roomDB = OFISRoomDB.getDbInstance(context);
    }

    public static DynamicQuestions getInstance(Context context, String questionColumnSuffix, DataLayer dl) {
        if (instance == null) {
            instance = new DynamicQuestions(context, questionColumnSuffix, dl);
        }
        return instance;
    }

    public static DynamicQuestions getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public static void clearInstance() {

        instance = null;
    }

    private void setupPlotVariants(Resources res) {
        // generate hashtable items
        plotvariants = new Hashtable<>();

        plotvariants.put("The same" + questionColumnSuffix, res.getString(R.string.plotvariant_thesame));
        plotvariants.put("More" + questionColumnSuffix, res.getString(R.string.plotvariant_more));
        plotvariants.put("Less" + questionColumnSuffix, res.getString(R.string.plotvariant_less));

        // reset densitySubAnswers
        densitySubAnswers = new String[]{};
    }

    public boolean checkDisplayStatus(int module) {
        boolean value = true;
        Cursor requiredQuestions = dl.getRequiredQuestionIds(module);
        try {
            if (requiredQuestions != null) {
                if (requiredQuestions.moveToFirst()) {
                    do {
                        JSONObject displayConfig = new JSONObject(requiredQuestions.getString(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                        if (displayConfig.has("display")) {
                            JSONObject display = displayConfig.getJSONObject("display");
                            if (display.optBoolean("requiredfield", false)) {
                                String type = requiredQuestions.getString(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));
                                if (type.equalsIgnoreCase("DropDown") || type.equalsIgnoreCase("checkbox") || type.equalsIgnoreCase("radio") || type.equalsIgnoreCase("datepicker")) {
                                    int question_id = requiredQuestions.getInt(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                                    if (!dl.getLookupValuesForQuestion(question_id)) {
                                        value = false;
                                        break;
                                    }
                                }
                            }
                        }
                    } while (requiredQuestions.moveToNext());
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        } finally {
            requiredQuestions.close();
        }
        return value;
    }

    public boolean requiredFieldsAnswered(int farmerModuleId, int moduleId) {
        Cursor completedAnswers = dl.getAllAnswersByFarmerModuleId(farmerModuleId);
        Cursor requiredQuestions = dl.getRequiredQuestionIds(moduleId);
        Set<Integer> completedQuestionIds = new HashSet<>();
        Set<Integer> requiredQuestionIds = new HashSet<>();

        try {
            while (completedAnswers.moveToNext()) {
                completedQuestionIds.add(completedAnswers.getInt(completedAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
            }
        } finally {
            completedAnswers.close();
        }
        try {
            while (requiredQuestions.moveToNext()) {

                // START
                do {
                    try {
                        JSONObject displayConfig = new JSONObject(requiredQuestions.getString(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                        if (displayConfig.has("display")) {
                            JSONObject display = displayConfig.getJSONObject("display");
                            if (display.optBoolean("requiredfield", false)) {
                                String questionsString = requiredQuestions.getString(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                                String type = requiredQuestions.getString(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));
                                // check the type only for calcualted question.
                                if (!type.equalsIgnoreCase("Calculated") && !type.equalsIgnoreCase("Repeater Item") && !type.equalsIgnoreCase("repeateritem")) {
                                    requiredQuestionIds.add(Integer.parseInt(questionsString));
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                } while (requiredQuestions.moveToNext());
                //END
            }
        } finally {
            requiredQuestions.close();
        }
        return completedQuestionIds.containsAll(requiredQuestionIds);
    }

    public boolean requiredFieldsMinMaxAnswered(int farmerModuleId, int moduleId) {
        boolean status = true;

        Cursor requiredQuestions = dl.getRequiredSurveyRangeQuestionIds(moduleId);
//        CheckMinMaxforQuestion(requiredQuestions);
        try {
            if (requiredQuestions != null && requiredQuestions.moveToFirst()) {
                do {
                    JSONObject displayConfig = new JSONObject(requiredQuestions.getString(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                    final String datatype = requiredQuestions.getString(requiredQuestions.getColumnIndex("datatype_id"));
                    final String displaytype = requiredQuestions.getString(requiredQuestions.getColumnIndex("display_type"));
                    final int questionId = requiredQuestions.getInt(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                    Cursor completedAnswers = dl.getAllAnswersByFarmerModuleId(farmerModuleId);
//                    int  answerId = requiredQuestions.getInt(requiredQuestions.getColumnIndex("answer_id"));
//                    if(displaytype.equalsIgnoreCase("textbox")){
                    if (datatype.equalsIgnoreCase("1") || datatype.equalsIgnoreCase("3")) {
                        if (displayConfig.has("surveyrange")) {

//                        if (getAnswerMinMaxvalidation(questionId, answerId) != "noval") {
//                            float answer = Float.parseFloat(getAnswerMinMaxvalidation(questionId,answerId));
//                            String ans = (getAnswer(completedAnswers, datatype, questionId));
//                            if (String.valueOf(ans) != "noval") {
                            String ans = (getAnswer(completedAnswers, datatype, questionId));
                            if (String.valueOf(ans) != "null") {
                                float answer = Float.valueOf(ans);
                                if (fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX) != 0 || fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN) != 0 ? true : false) {
                                    if ((answer > fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX)) || (answer < fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN))) {
                                        status = false;

//                            Toast.makeText(context, R.string.valid_range, Toast.LENGTH_LONG).show();
                                        break;
                                    }
                                }
//                            }
                            }
                        }
                    }

                } while (requiredQuestions.moveToNext());
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        return status;
    }

    private String getAnswer(Cursor completedAnswers, String datatype, int questionId) {
        String ans = "null";
        try {
            while (completedAnswers.moveToNext()) {
                int question_id = completedAnswers.getInt(completedAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID));
                if (question_id == questionId) {
                    if (datatype.equalsIgnoreCase("3")) {

                        ans = String.valueOf(BigDecimal.valueOf(completedAnswers.getDouble(completedAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL))).floatValue());
                        break;
                    } else if (datatype.equalsIgnoreCase("1")) {
                        ans = String.valueOf(completedAnswers.getInt(completedAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        } finally {
//            completedAnswers.close();
        }
        return ans;
    }

    public boolean requiredFieldsAnsweredChild(int farmerModuleId, int moduleId) {
        Cursor completedAnswers = dl.getAllAnswersByFarmerChildModuleId(farmerModuleId);
        Cursor requiredQuestions = dl.getRequiredQuestionIds(moduleId);
        Set<Integer> completedQuestionIds = new HashSet<>();
        Set<Integer> requiredQuestionIds = new HashSet<>();

        try {
            while (completedAnswers.moveToNext()) {
                completedQuestionIds.add(completedAnswers.getInt(completedAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
            }
        } finally {
            completedAnswers.close();
        }
        try {
            while (requiredQuestions.moveToNext()) {
                int questionId = requiredQuestions.getInt(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                if (isMandateQuestionOriginMatched(questionId)) {
                    requiredQuestionIds.add(questionId);
                }
            }
        } finally {
            requiredQuestions.close();
        }
        return completedQuestionIds.containsAll(requiredQuestionIds);
    }

    private void saveMultipleAnswerEntry(int questionId, @SuppressWarnings("SameParameterValue") int parentQuestionId, String dataType, TableLayout questionTable, Integer globalMetricId, Integer localMetricId, int metricsValue, final int repeaterItemPosition) {

        // New
        Iterator<JSONObject> iterator = answerSet.iterator();
        /*while (iterator.hasNext()) {
            JSONObject currentAnswer = iterator.next();
            if (currentAnswer != null) {
                int currentQuestionId = currentAnswer.optInt("question_id");
                if (currentQuestionId == questionId) {
                    iterator.remove();
                }
            }
        }*/

        // Old
//        for (int i = 0; i < answerSet.size(); i++) {
//            JSONObject currentAnswer = answerSet.get(i);
//            if (currentAnswer != null) {
//                int currentQuestionId = currentAnswer.optInt("question_id");
//                if (currentQuestionId == questionId) {
//                    answerSet.remove(i);
//                }
//            }
//        }

        if (repeaterItemPosition == -1) {
            while (iterator.hasNext()) {
                JSONObject currentAnswer = iterator.next();
                if (currentAnswer != null) {
                    int currentQuestionId = currentAnswer.optInt("question_id");
                    if (currentQuestionId == questionId) {
                        iterator.remove();
                    }
                }
            }

            int tableRows = questionTable.getChildCount();
            for (int j = 0; j < tableRows; j++) {
                TableRow tr = (TableRow) questionTable.getChildAt(j);
                TextView cbId = (TextView) tr.findViewById(R.id.cbId);
                CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                if (cb.isChecked()) {
                    saveAnswerEntry(questionId, j, parentQuestionId, repeaterItemPosition, dataType, String.valueOf(cbId.getText()), globalMetricId, localMetricId, 0);
                }
            }
        } else {
            int count = 1;
            if (answerSet != null) {
                for (int i = 0; i < answerSet.size(); i++) {
                    JSONObject currentAnswer = answerSet.get(i);
                    if (currentAnswer != null) {
                        if (currentAnswer.optInt("question_id") == questionId && currentAnswer.optInt("repeater_item_position") == repeaterItemPosition) {
                            count++;
                        }
                    }
                }
            }

            if (count > 1) {
                while (iterator.hasNext()) {
                    JSONObject currentAnswer = iterator.next();
                    if (currentAnswer != null) {
                        int currentQuestionId = currentAnswer.optInt("question_id");
                        int repeaterItemPositionSet = currentAnswer.optInt("repeater_item_position");
                        if (currentQuestionId == questionId && repeaterItemPositionSet == repeaterItemPosition ) {
                            iterator.remove();
                        }
                    }
                }
            }

            int tableRows = questionTable.getChildCount();
            for (int j = 0; j < tableRows; j++) {
                TableRow tr = (TableRow) questionTable.getChildAt(j);
                TextView cbId = (TextView) tr.findViewById(R.id.cbId);
                CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                if (cb.isChecked()) {
                    saveAnswerEntry(questionId, Integer.parseInt((String) cbId.getText()), parentQuestionId, repeaterItemPosition, dataType, String.valueOf(cbId.getText()), globalMetricId, localMetricId, 0);
                }
            }

        }

        /*int tableRows = questionTable.getChildCount();
        for (int j = 0; j < tableRows; j++) {
            TableRow tr = (TableRow) questionTable.getChildAt(j);
            TextView cbId = (TextView) tr.findViewById(R.id.cbId);
            CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

            if (cb.isChecked()) {
                saveAnswerEntry(questionId, j, parentQuestionId, repeaterItemPosition, dataType, String.valueOf(cbId.getText()), globalMetricId, localMetricId, 0);
            }
        }*/
    }

    private void saveMultipleAnswerEntryRadio(int questionId, int parentQuestionId, String dataType,
                                              LinearLayout linearLayout, int repeaterItemPosition, Integer globalMetricId, Integer localMetricId, int metricsValue,
                                              Spinner spinner, Map<Integer, String> spinnerData, ArrayAdapter<String> spinnerAdapter,
                                              LinearLayout previewLayout, TextView contentTextView, LinearLayout questionWrapper,
                                              int finalSwitchId, TextView finalYesNoConstraint, Cursor questions ) {

        final Integer[] radioGlobalMetrics = {globalMetricId};
        final Integer[] radioLocalMetrics = {localMetricId};


        if (globalMetricId != 0) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // Get the selected item from the adapter
                    String selectedAbbreviation = spinnerAdapter.getItem(position);

                    if (spinnerData != null) {
                        for (Map.Entry<Integer, String> entry : spinnerData.entrySet()) {
                            if (entry.getValue().equals(selectedAbbreviation)) {
                                int selectedLocalUnitId = entry.getKey();

                                if (entry.getKey() == Integer.parseInt(String.valueOf(globalMetricId))) {
                                    radioGlobalMetrics[0] = entry.getKey();
                                    radioLocalMetrics[0] = 0;
                                } else {
                                    radioLocalMetrics[0] = entry.getKey();
                                    radioGlobalMetrics[0] = 0;
                                }
                                // Display a Toast with the selected localUnitId
//                                Toast.makeText(context, "Selected localUnitId: " + selectedLocalUnitId, Toast.LENGTH_SHORT).show();

                                if (answerSet != null) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");

                                            if (currentQuestionId == questionId) {
                                                try {
                                                    currentAnswer.put("globalMetricId", radioGlobalMetrics[0]);
                                                    currentAnswer.put("localMetricId", radioLocalMetrics[0]);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                        }
                                    }
                                }

                                if (answerSet != null) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");
                                            int answerJsonVal = currentAnswer.optInt("answer");

                                            if (currentQuestionId == questionId) {
                                                Integer globalMetricIdAnswer = null;
                                                Integer localMetricIdAnswer = null;
                                                if (currentAnswer.has("globalMetricId")) {
                                                    globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                }
                                                if (currentAnswer.has("localMetricId")) {
                                                    localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                }
                                                if (localMetricIdAnswer != 0) {
                                                    previewLayout.setVisibility(View.VISIBLE);
//                                                    titleTextView.setVisibility(View.VISIBLE);
//                                                    contentTextView.setVisibility(View.VISIBLE);
                                                    String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(globalMetricId));
                                                    String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                    String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                    String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                                        contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                                    if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                        try {
                                                            // Perform your calculations and set the text
                                                            double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                            contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                        } catch (NumberFormatException e) {
                                                            // Handle the case where parsing fails
                                                            e.printStackTrace();
                                                            previewLayout.setVisibility(View.GONE);
                                                        }
                                                    } else {
                                                        // Handle the case where any of the variables is null
                                                        contentTextView.setText("Some values are null");
                                                    }
                                                } else {
                                                    previewLayout.setVisibility(View.GONE);
                                                }
                                            }

                                        }
                                    }
                                }

                                Log.d("AnswerNewSet", answerSet.toString());

                                break;
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // Do nothing here if needed
                }
            });
        }

        RadioGroup radioGroup = (RadioGroup) linearLayout.findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < group.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) group.getChildAt(i);
                    radioButton.setChecked(radioButton.getId() == checkedId);
                }

                // Get the value (text) of the checked RadioButton
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                String selectedText = checkedRadioButton.getText().toString();
                int j = checkedRadioButton.getId();
//                Toast.makeText(context, selectedText, Toast.LENGTH_SHORT).show();
//                Toast.makeText(context, j + "", Toast.LENGTH_SHORT).show();

                // If id =2 , survey-completion-check
                String getNoValue = dl.getValueByLookUpID(String.valueOf(checkedId), questions, "_en");
                int surveyCompletion = -1;
                if (Objects.equals(getNoValue.toLowerCase(),"no")){
                    surveyCompletion = 2;

                } else if (Objects.equals(getNoValue.toLowerCase(),"yes")) {
                    surveyCompletion = 1;
                }
                if (surveyCompletion == finalSwitchId) {
                    questionWrapper.addView(finalYesNoConstraint);
                    surveySet.add(questionId);
                    finalYesNoConstraint.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.RIGHT);
                } else {
                    surveySet.remove(questionId);
                    questionWrapper.removeView(finalYesNoConstraint);
                }

                if (repeaterItemPosition == -1) {
                    if (answerSet != null) {
                        for (int i = 0; i < answerSet.size(); i++) {
                            JSONObject currentAnswer = answerSet.get(i);
                            if (currentAnswer != null) {
                                if (currentAnswer.optInt("question_id") == questionId) {
                                    answerSet.remove(i);
                                }
                            }
                        }
                    }
                    saveAnswerEntry(questionId, j, parentQuestionId, repeaterItemPosition, dataType, String.valueOf(checkedId), radioGlobalMetrics[0], radioLocalMetrics[0], 0);
                } else {
                    int count = 1;
                    if (answerSet != null) {
                        for (int i = 0; i < answerSet.size(); i++) {
                            JSONObject currentAnswer = answerSet.get(i);
                            if (currentAnswer != null) {
                                if (currentAnswer.optInt("question_id") == questionId && currentAnswer.optInt("repeater_item_position") == repeaterItemPosition) {
                                    count++;
                                }
                            }
                        }
                    }

                    if (count > 1) {
                        if (answerSet != null) {
                            for (int i = 0; i < answerSet.size(); i++) {
                                JSONObject currentAnswer = answerSet.get(i);
                                if (currentAnswer != null) {
                                    if (currentAnswer.optInt("question_id") == questionId && currentAnswer.optInt("repeater_item_position") == repeaterItemPosition) {
                                        answerSet.remove(i);
                                    }
                                }
                            }
                        }
                    }

                    saveAnswerEntry(questionId, j, parentQuestionId, repeaterItemPosition, dataType, String.valueOf(checkedId), radioGlobalMetrics[0], radioLocalMetrics[0], 0);

                }

                if (globalMetricId != null && !globalMetricId.equals(0)) {
                    if (answerSet.size() != 0) {
                        for (int i = 0; i < answerSet.size(); i++) {
                            JSONObject currentAnswer = answerSet.get(i);

                            if (currentAnswer != null) {
                                int currentQuestionId = currentAnswer.optInt("question_id");
                                int answerJsonVal = currentAnswer.optInt("answer");

                                if (currentQuestionId == questionId) {
                                    Integer globalMetricIdAnswer = null;
                                    Integer localMetricIdAnswer = null;
                                    if (currentAnswer.has("globalMetricId")) {
                                        globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                    }
                                    if (currentAnswer.has("localMetricId")) {
                                        localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                    }
                                    if (localMetricIdAnswer != 0) {
                                        previewLayout.setVisibility(View.VISIBLE);
                                        String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(globalMetricId));
                                        String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                        String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                        String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                        contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                        if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                            try {
                                                // Perform your calculations and set the text
                                                double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                            } catch (NumberFormatException e) {
                                                // Handle the case where parsing fails
                                                e.printStackTrace();
                                                previewLayout.setVisibility(View.GONE);
                                            }
                                        } else {
                                            // Handle the case where any of the variables is null
                                            contentTextView.setText("Some values are null");
                                        }
                                    } else {
                                        previewLayout.setVisibility(View.GONE);
                                    }

                                } else {
                                    previewLayout.setVisibility(View.GONE);
                                }

                            }
                        }
                    } else {
                        previewLayout.setVisibility(View.GONE);
                    }
                }

            }
        });

    }

    private void saveMultipleAnswerEntryForSUbQuestionAnswerSave(int questionId, @SuppressWarnings("SameParameterValue") int parentQuestionId, String dataType, TableLayout questionTable) {
        /*for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentQuestionId = currentAnswer.optInt("question_id");
                if (currentQuestionId == questionId) {
                    answerSet.remove(i);
                }
            }
        }*/
        int tableRows = questionTable.getChildCount();
        dl.prentidAnswerDelete(parentQuestionId, questionId);
        for (int j = 0; j < tableRows; j++) {
            TableRow tr = (TableRow) questionTable.getChildAt(j);
            TextView cbId = (TextView) tr.findViewById(R.id.cbId);
            CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

            if (cb.isChecked()) {


                dl.prentidAnswer(Utilities.subModuleId, parentQuestionId, questionId, String.valueOf(cbId.getText()), 0 + "", Utilities.olamFarmerId, Utilities.moduleId + "");

            }
        }
    }

    private void saveCalculatedAnswerEntry(int questionId, int answerId, int repeaterItemPosition, String answer, String subAnswers) {
        try {
            if (viewAnswer) return;
            if (answer != null && !answer.isEmpty()) {
                boolean isNew = true;
                for (int i = 0; i < answerSet.size(); i++) {
                    JSONObject currentAnswer = answerSet.get(i);
                    if (currentAnswer != null) {
                        int currentQuestionId = currentAnswer.optInt("question_id");
                        int currentAnswerId = currentAnswer.optInt("answer_id");
                        if (currentQuestionId == questionId && currentAnswerId == answerId) {
                            if (repeaterItemPosition != -1) {
                                int currentRepeaterItemPosition = currentAnswer.optInt("repeater_item_position");
                                if (currentRepeaterItemPosition != repeaterItemPosition) {
                                    continue;
                                }
                            }
                            currentAnswer.put("answer", answer);
                            currentAnswer.put("sub_answers", subAnswers);
                            currentAnswer.put("datatype", "decimal");
                            currentAnswer.put("parent_question_id", 0);
                            currentAnswer.put("repeater_item_position", repeaterItemPosition);
                            currentAnswer.put("reporting_year", reportingYear);
                            currentAnswer.put("globalMetricId", 0);
                            currentAnswer.put("localMetricId", 0);
                            currentAnswer.put("metricAnswer", 0);
                            isNew = false;
                            break;
                        }
                    }
                }
                if (isNew) {
                    JSONObject newAnswer = new JSONObject();
                    newAnswer.put("question_id", questionId);
                    newAnswer.put("answer_id", answerId);
                    newAnswer.put("parent_question_id", 0);
                    newAnswer.put("repeater_item_position", repeaterItemPosition);
                    newAnswer.put("datatype", "decimal");
                    newAnswer.put("answer", answer);
                    newAnswer.put("sub_answers", subAnswers);
                    newAnswer.put("reporting_year", reportingYear);
                    newAnswer.put("globalMetricId", 0);
                    newAnswer.put("localMetricId", 0);
                    newAnswer.put("metricAnswer", 0);
                    answerSet.add(newAnswer);
                }
            }
            Log.d("AnswerSetCheck", String.valueOf(answerSet));
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
    }

    public void saveAnswerEntry(int questionId, int answerId, int parentQuestionId, int repeaterItemPosition, String datatype, String answer, Integer globalMetricId, Integer localMetricId, double metricAnswer) {
        try {
            if (viewAnswer) return;
            if (answer != null && !answer.isEmpty()) {
                boolean isNew = true;
                for (int i = 0; i < answerSet.size(); i++) {
                    JSONObject currentAnswer = answerSet.get(i);
                    if (currentAnswer != null) {
                        int currentQuestionId = currentAnswer.optInt("question_id");
                        int currentAnswerId = currentAnswer.optInt("answer_id");

                        if (currentQuestionId == questionId && currentAnswerId == answerId) {
                            if (repeaterItemPosition != -1) {
                                int currentRepeaterItemPosition = currentAnswer.optInt("repeater_item_position");
                                if (currentRepeaterItemPosition != repeaterItemPosition) {
                                    continue;
                                }
                            }
                            currentAnswer.put("answer", answer);
                            currentAnswer.put("datatype", datatype);
                            currentAnswer.put("parent_question_id", parentQuestionId);
                            currentAnswer.put("repeater_item_position", repeaterItemPosition);
                            currentAnswer.put("reporting_year", reportingYear);
                            currentAnswer.put("globalMetricId", globalMetricId);
                            currentAnswer.put("localMetricId", localMetricId);
                            currentAnswer.put("metricAnswer", metricAnswer);
                            isNew = false;
                            break;
                        }

                    }
                }
                if (isNew) {
                    JSONObject newAnswer = new JSONObject();
                    newAnswer.put("question_id", questionId);
                    newAnswer.put("answer_id", answerId);
                    newAnswer.put("parent_question_id", parentQuestionId);
                    newAnswer.put("repeater_item_position", repeaterItemPosition);
                    newAnswer.put("datatype", datatype);
                    newAnswer.put("answer", answer);
                    newAnswer.put("reporting_year", reportingYear);
                    newAnswer.put("globalMetricId", globalMetricId);
                    newAnswer.put("localMetricId", localMetricId);
                    newAnswer.put("metricAnswer", metricAnswer);
                    answerSet.add(newAnswer);
                }
            } else if (answerId > 0) {
                removeAnswerEntry(questionId, answerId);
            } else if (answerId == 0 && repeaterItemPosition != -1) {
                removeAnswerEntry(questionId, answerId, repeaterItemPosition);
            }

            Log.d("AnswerSetCheck", String.valueOf(answerSet));
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
    }

    private void removeAnswerEntry(int questionId, int answerId, int repeaterItemPosition) {
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentQuestionId = currentAnswer.optInt("question_id");
                int currentAnswerId = currentAnswer.optInt("answer_id");
                int currentAnswerRepeaterPosition = currentAnswer.optInt("repeater_item_position");
                if (currentQuestionId == questionId && currentAnswerId == answerId && currentAnswerRepeaterPosition == repeaterItemPosition) {
                    answerSet.remove(i);
                    break;
                }
            }
        }
    }
    private boolean checkvalues(float min, float max, String answer) {
        boolean val = true;
        try {

            if (max != 0 || min != 0 ? true : false) {
                if ((Float.parseFloat(answer) > max) || (Float.parseFloat(answer) < min)) {
                    val = false;

                }
            }

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
            return false;
        }
        return val;
    }


    private void removeAnswerEntry(int questionId, int answerId) {
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentQuestionId = currentAnswer.optInt("question_id");
                int currentAnswerId = currentAnswer.optInt("answer_id");
                if (currentQuestionId == questionId && currentAnswerId == answerId) {
                    answerSet.remove(i);
                    break;
                }

            }
        }

    }


    private void removeAnswerEntry(int questionId) {
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentQuestionId = currentAnswer.optInt("question_id");
                int currentAnswerRepeaterPosition = currentAnswer.optInt("repeater_item_position");
                if (currentQuestionId == questionId && currentAnswerRepeaterPosition == -1) {
                    answerSet.remove(i);
                    break;
                }
            }
        }
    }

    public boolean isAllRequiredSubmoduleAnswered(Cursor questions) throws JSONException {
        boolean match = false;
        Boolean repeaterItemAnswered = false;

        // get List from Answered List
        ArrayList<Integer> answeredQuestionList = new ArrayList<>();
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentQuestionId = currentAnswer.optInt("question_id");
                answeredQuestionList.add(currentQuestionId);
            }
        }

        //For repeater Items mandate check
        List<RepeaterItems> questionRepeaterSet = new ArrayList<>();
        List<RepeaterItems> answerRepeaterSet = new ArrayList<>();

        // get the list of mandated by submodules
        ArrayList<Integer> questionList = new ArrayList<>();
        if (questions != null && questions.moveToFirst()) {
            do {

                JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                if (displayConfig.has("display")) {
                    JSONObject display = displayConfig.getJSONObject("display");
                    if (display.optBoolean("requiredfield", false)) {
                        String questionsString = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                        String type = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));

                        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));

//                        check the type only for calcualted question. Calculated question FRANKLIN
                        if (!type.equalsIgnoreCase("Calculated") && !type.toLowerCase().replaceAll(" ","").equals("repeateritem") && isMandateQuestionOriginMatched(questionId)) {
                            questionList.add(Integer.parseInt(questionsString));
                        }

                        // OCOM-176 - remove the Repeater Item If the parent question is 0
                        if (type.equalsIgnoreCase("Repeater Item") || type.equalsIgnoreCase("repeateritem")) {
                            int repeaterParentId = display.getInt("repeater_id");
                            String answerRepetaerParent = "";
                            for (int i = 0; i < answerSet.size(); i++) {
                                JSONObject currentAnswer = answerSet.get(i);
                                if (currentAnswer != null) {
                                    if (repeaterParentId == currentAnswer.optInt("question_id")) {
                                        answerRepetaerParent = currentAnswer.getString("answer");
                                        if (answerRepetaerParent.equalsIgnoreCase("0")) {
                                            int index = questionList.indexOf(Integer.parseInt(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID))));
                                            if(questionList.contains(Integer.parseInt(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID))))){
                                                questionList.remove(index);
                                            }
                                        } else {

                                            // for Repeater Item Question Mandate
                                            Cursor repeaterItemsForQuestion = dl.getRepeaterItemsForQuestion(repeaterParentId);
                                            if (repeaterItemsForQuestion != null && repeaterItemsForQuestion.moveToFirst()){
                                                do {
                                                    JSONObject displayConfigRepeater = new JSONObject(repeaterItemsForQuestion.getString(repeaterItemsForQuestion.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                                                    if (displayConfigRepeater.has("display")){
                                                        JSONObject displayRepeater = displayConfigRepeater.getJSONObject("display");
                                                        if (displayRepeater.optBoolean("requiredfield", false)) {
                                                            final int questionIdRepeater = repeaterItemsForQuestion.getInt(repeaterItemsForQuestion.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));

                                                            for (int k = 1; k <=Integer.parseInt(answerRepetaerParent); k++) {
                                                                RepeaterItems newRepeaterItem = new RepeaterItems(questionIdRepeater, k);
                                                                boolean isDuplicate = false;

                                                                for (RepeaterItems existingItem : questionRepeaterSet) {
                                                                    if (newRepeaterItem.equals(existingItem)) {
                                                                        isDuplicate = true;
                                                                        break;
                                                                    }
                                                                }

                                                                if (!isDuplicate && isMandateQuestionOriginMatched(questionIdRepeater)) {
                                                                    questionRepeaterSet.add(newRepeaterItem);
                                                                }
                                                            }
                                                        }
                                                    }

                                                } while (repeaterItemsForQuestion.moveToNext());
                                            }

                                            // For Repeater Item Answer Mandate
                                            for (int m = 0; m < answerSet.size(); m++) {
                                                JSONObject currentAnswerRepeater = answerSet.get(m);
                                                if (currentAnswerRepeater != null) {
                                                    if (repeaterParentId == currentAnswerRepeater.optInt("parent_question_id")){
                                                        RepeaterItems newRepeaterItem = new RepeaterItems(currentAnswerRepeater.optInt("question_id"), currentAnswerRepeater.optInt("repeater_item_position"));
                                                        boolean isDuplicate = false;

                                                        for (RepeaterItems existingItem : answerRepeaterSet) {
                                                            if (newRepeaterItem.equals(existingItem)) {
                                                                isDuplicate = true;
                                                                break;
                                                            }
                                                        }

                                                        if (!isDuplicate) {
                                                            answerRepeaterSet.add(newRepeaterItem);
                                                        }

                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }

                    }
                }


            } while (questions.moveToNext());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            List<String> ques = questionRepeaterSet.stream().map(it-> it.questionId+""+it.repeaterPosition).collect(Collectors.toList());
            List<String> ans = answerRepeaterSet.stream().map(it-> it.questionId+""+it.repeaterPosition).collect(Collectors.toList());
            repeaterItemAnswered = ans.containsAll(ques);

        }

        // Compare the list
        if (questionList.isEmpty()) {
            if (repeaterItemAnswered){
                match = true;
            } else {
                match = false;
            }
        } else {
            if (answeredQuestionList.containsAll(questionList) && repeaterItemAnswered ) {
                match = true;
            }
        }
        return match;
    }

    public boolean isMandateQuestionOriginMatched(int questionId){
        boolean questionFound = false;
        final String questionAssignmentType = dl.getQuestionAssignmentTypeId(questionId);
        final String questionAssignmentField = dl.getQuestionAssignmentFieldId(questionId);
        long farmerId = Utilities.farmerId;
        if (questionAssignmentField.equals("[]") || questionAssignmentField.equals("null")) {
            questionFound = true;
        } else {
            String[] stringArrayQuestion = questionAssignmentField.substring(1, questionAssignmentField.length() - 1).split(",");

            // Convert the string array to an integer array
            int[] intArrayQuestion = new int[stringArrayQuestion.length];
            for (int m = 0; m < stringArrayQuestion.length; m++) {
                intArrayQuestion[m] = Integer.parseInt(stringArrayQuestion[m]);
            }

            for (int fieldMultipleAssignmentQuestion : intArrayQuestion) {
                String farmerAssignmentFieldQuestion = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(questionAssignmentType));
                if (String.valueOf(fieldMultipleAssignmentQuestion).equals(farmerAssignmentFieldQuestion)) {
                    questionFound =true;
                }
            }
        }

        return questionFound;
    }

    private String getAnswerMinMaxvalidation(int questionId) {
        String ans = "noval";
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentQuestionId = currentAnswer.optInt("question_id");
                int currentAnswerId = currentAnswer.optInt("answer_id");
//                if (currentQuestionId == questionId && currentAnswerId == answerId) {
                if (currentQuestionId == questionId) {
                    ans = currentAnswer.optString("answer");

                    break;
                } else
                    ans = "noval";
            }
        }
        return ans;
    }

    private void removeRepeaterAnswers(int questionId) {
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentParentQuestionId = currentAnswer.optInt("parent_question_id");
                if (currentParentQuestionId == questionId) {
                    answerSet.remove(i);
                }
            }
        }
    }

    private void removeRepeaterAnswer(int parentQuestionId, int repeaterItemPosition) {
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentParentQuestionId = currentAnswer.optInt("parent_question_id");
                int currentRepeaterItemPosition = currentAnswer.optInt("repeater_item_position");
                if (currentParentQuestionId == parentQuestionId && currentRepeaterItemPosition == repeaterItemPosition) {
                    answerSet.remove(i);
                }
            }
        }
    }

    public void renderQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout) {
        LinearLayout combinedLayout = new LinearLayout(getInstance().context);
        combinedLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        combinedLayout.setOrientation(LinearLayout.VERTICAL);
//        combinedLayout.setPadding(16, 16, 16, 16);

        LinearLayout textImgLayout = new LinearLayout(getInstance().context);
        textImgLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textImgLayout.setOrientation(LinearLayout.HORIZONTAL);
        textImgLayout.setWeightSum(2);
//        combinedLayout.setPadding(16, 16, 16, 16);


        TextView tv;
        tv = (TextView) inflater.inflate((isRepeaterItem) ? R.layout.module_repeater_item_question : R.layout.module_question, parentLayout, false);
        PopupWindow popupWindow = new PopupWindow(context);
        ;
        // for info
        ImageView imageViewInfo = new ImageView(context);
        imageViewInfo.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f
        ));
        imageViewInfo.setPadding(3, 0, 0, 0);
        imageViewInfo.setImageResource(R.drawable.info_icon);


        int columnIndex = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) : qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION);
        int columnInfo = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO + questionColumnSuffix) != -1) ? qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO + questionColumnSuffix) : qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO);
        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        tv.setText(Html.fromHtml(qCursor.getString(columnIndex)));
        String hh = Html.fromHtml(qCursor.getString(columnIndex)) + "";
        String infoVal = null;
        try {
            if (!viewAnswer){
                infoVal = String.valueOf(Html.fromHtml(qCursor.getString(columnInfo)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get disp"lay config to see if question is required
        try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));

            if (displayConfig.has("surveyrange")) {

                if (datatype.equals("decimal")) {
                    tv.setText(Html.fromHtml(tv.getText().toString() + "(Min Value: " + String.valueOf(BigDecimal.valueOf(displayConfig.getJSONObject("surveyrange").getDouble("min")).floatValue()) +
                            "\t Max value: " + String.valueOf(BigDecimal.valueOf(displayConfig.getJSONObject("surveyrange").getDouble("max")).floatValue()) + ")"), TextView.BufferType.SPANNABLE);

                } else if (datatype.equals("int")) {
                    tv.setText(Html.fromHtml(tv.getText().toString() + "(Min Value: " + String.valueOf(displayConfig.getJSONObject("surveyrange").getInt("min") +
                            "\tMax value: " + String.valueOf(displayConfig.getJSONObject("surveyrange").getInt("max")) + ")")), TextView.BufferType.SPANNABLE);

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

        textImgLayout.addView(tv);

        combinedLayout.addView(textImgLayout);

        if (infoVal != null && !infoVal.equals("")) {
            String finalInfoVal = infoVal;
            imageViewInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (popupWindow == null || !popupWindow.isShowing()) {
                        // Create a new popup window only if it's null or not showing
                        // Inflate the custom popup content layout
                        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_layout, null);

                        // Set the popup content view and dimensions
                        popupWindow.setContentView(popupView);
                        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

                        // Find the TextView in the popup layout
                        try {
                            TextView popupTextView = popupView.findViewById(R.id.popupTextView);
                            // Set the text for the TextView
                            popupTextView.setText(finalInfoVal);

                            // Show the popup below the infoButton
                            popupWindow.showAsDropDown(imageViewInfo, 0, 0, Gravity.CENTER);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    } else {
                        // Dismiss the popup if it's already showing
                        if (popupWindow != null && popupWindow.isShowing()) {
                            popupWindow.dismiss();
                        }
                    }

                }
            });

            textImgLayout.addView(imageViewInfo);
        }


        parentLayout.addView(combinedLayout);
    }

    public boolean CheckMinMaxforQuestion(Cursor qCursor) {
        boolean status = true;
        roomDB = OFISRoomDB.getDbInstance(context);

        try {

            if (qCursor != null && qCursor.moveToFirst()) {
                do {
                    JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
//                    final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
                    final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
//                    int answerId = qCursor.getInt(qCursor.getColumnIndex("answer_id"));
                    JSONObject display = displayConfig.getJSONObject("display");
                    int validationOnRange = display.optInt("validationOnRangeId");

                    String globally;
                    String assignmentType = "";
                    String assignmentField = "";
                    String assignmentFields = "";
                    if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrict() || validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                        if (display.has("range")) {
                            JSONArray range = display.getJSONArray("range");
//                            JSONObject rangeArray = range.getJSONObject(0);

                            int[] farmerOrderList = new int[]{6, 5, 8, 9, 4, 3, 2, 1, 7, 0};
                            boolean valueFound = false;
                            for (int orderList : farmerOrderList) {

                                for (int i = 0; i < range.length(); i++) {
                                    JSONObject rangeArray = range.getJSONObject(i);

                                    globally = rangeArray.getString("globally");
                                    assignmentType = rangeArray.getString("assignmentType");
                                    if (assignmentType.equals("null")) {
                                        assignmentType = "0";
                                    }
                                    assignmentField = rangeArray.getString("assignmentField");
                                    assignmentFields = rangeArray.getString("assignmentFields");

                                    if (Integer.parseInt(assignmentType) == orderList && Integer.parseInt(assignmentType) != 0) {

                                        if (!assignmentField.equals("null")) {
                                            long farmerId = Utilities.farmerId;
                                            roomDB = OFISRoomDB.getDbInstance(context);
                                            String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));
                                            if (assignmentField.equals(farmerAssignmentField) && globally.equals("false") && !valueFound) {
                                                double min = rangeArray.optDouble(AppConstants.RANGE_MIN);
                                                int max = rangeArray.optInt(AppConstants.RANGE_MAX);

                                                if (getAnswerMinMaxvalidation(questionId) != "noval") {
                                                    String wrongValue = getAnswerMinMaxvalidation(questionId);
                                                    if (wrongValue.equals("-")) {
                                                        status = false;
                                                        break;
                                                    }else {
                                                        double answer = Double.parseDouble(getAnswerMinMaxvalidation(questionId));
//                                if (fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX) != 0
//                                        || fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN) != 0 ? true : false) {
//                                    if ((answer > fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX)) || (answer < fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN))) {

                                                        if (answer < min || answer > max) {
                                                            status = false;
//                            Toast.makeText(context, R.string.valid_range, Toast.LENGTH_LONG).show();
                                                            break;
                                                        }
                                                    }

                                                }


                                                valueFound = true;
                                                break;
                                            } else if (assignmentType.equals("null") && assignmentField.equals("null") && globally.equals("true") && !valueFound) {
                                                double min = rangeArray.optDouble(AppConstants.RANGE_MIN);
                                                int max = rangeArray.optInt(AppConstants.RANGE_MAX);

                                                if (getAnswerMinMaxvalidation(questionId) != "noval") {
                                                    String wrongValue = getAnswerMinMaxvalidation(questionId);
                                                    if (wrongValue.equals("-")) {
                                                        status = false;
                                                        break;
                                                    }else {
                                                        double answer = Double.parseDouble(getAnswerMinMaxvalidation(questionId));
//                                if (fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX) != 0
//                                        || fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN) != 0 ? true : false) {
//                                    if ((answer > fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX)) || (answer < fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN))) {

                                                        if (answer < min || answer > max) {
                                                            status = false;
//                            Toast.makeText(context, R.string.valid_range, Toast.LENGTH_LONG).show();
                                                            break;
                                                        }
                                                    }

                                                }


                                            }
                                        } else if (!assignmentFields.equals("null") && !valueFound) {

                                            // Remove square brackets and split the string into an array of strings
                                            String[] stringArray = assignmentFields.substring(1, assignmentFields.length() - 1).split(",");

                                            // Convert the string array to an integer array
                                            int[] intArray = new int[stringArray.length];
                                            for (int m = 0; m < stringArray.length; m++) {
                                                intArray[m] = Integer.parseInt(stringArray[m]);
                                            }

                                            for (int fieldMultipleAssignment : intArray) {
                                                long farmerId = Utilities.farmerId;
                                                roomDB = OFISRoomDB.getDbInstance(context);
                                                String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                                if (String.valueOf(fieldMultipleAssignment).equals(farmerAssignmentField)) {
                                                    double min = rangeArray.optDouble(AppConstants.RANGE_MIN);
                                                    int max = rangeArray.optInt(AppConstants.RANGE_MAX);

                                                    if (getAnswerMinMaxvalidation(questionId) != "noval") {
                                                        String wrongValue = getAnswerMinMaxvalidation(questionId);
                                                        if (wrongValue.equals("-")) {
                                                            status = false;
                                                            break;
                                                        }else {
                                                            double answer = Double.parseDouble(getAnswerMinMaxvalidation(questionId));
//                                if (fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX) != 0
//                                        || fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN) != 0 ? true : false) {
//                                    if ((answer > fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX)) || (answer < fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN))) {

                                                            if (answer < min || answer > max) {
                                                                status = false;
//                            Toast.makeText(context, R.string.valid_range, Toast.LENGTH_LONG).show();
                                                                break;
                                                            }
                                                        }

                                                    }


                                                    valueFound = true;
                                                    break;
                                                }

                                            }

                                        }

                                    } else if (Integer.parseInt(assignmentType) == 0 && orderList == 0 && !valueFound) {
                                        double min = rangeArray.optDouble(AppConstants.RANGE_MIN);
                                        int max = rangeArray.optInt(AppConstants.RANGE_MAX);

                                        if (getAnswerMinMaxvalidation(questionId) != "noval") {
                                            String wrongValue = getAnswerMinMaxvalidation(questionId);
                                            if (wrongValue.equals("-")) {
                                                status = false;
                                                break;
                                            }else {
                                                double answer = Double.parseDouble(getAnswerMinMaxvalidation(questionId));
//                                if (fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX) != 0
//                                        || fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN) != 0 ? true : false) {
//                                    if ((answer > fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX)) || (answer < fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN))) {

                                                if (answer < min || answer > max) {
                                                    status = false;
//                            Toast.makeText(context, R.string.valid_range, Toast.LENGTH_LONG).show();
                                                    break;
                                                }
                                            }

                                        }

                                    }

                                }

                            }
                            /*double min = rangeArray.optDouble(AppConstants.RANGE_MIN);
                            int max = rangeArray.optInt(AppConstants.RANGE_MAX);

                            if (getAnswerMinMaxvalidation(questionId) != "noval") {
                                String wrongValue = getAnswerMinMaxvalidation(questionId);
                                if (wrongValue.equals("-")) {
                                    status = false;
                                    break;
                                }else {
                                    double answer = Double.parseDouble(getAnswerMinMaxvalidation(questionId));
//                                if (fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX) != 0
//                                        || fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN) != 0 ? true : false) {
//                                    if ((answer > fetchMinMaxValue(displayConfig, AppConstants.RANGE_MAX)) || (answer < fetchMinMaxValue(displayConfig, AppConstants.RANGE_MIN))) {

                                    if (answer < min || answer > max) {
                                        status = false;
//                            Toast.makeText(context, R.string.valid_range, Toast.LENGTH_LONG).show();
                                        break;
                                    }
                                }

                            }*/
//                            }

                        }
                    }


                } while (qCursor.moveToNext());
            }
        } catch (JSONException e) {
            String questionId = "0";
            if ((qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID) != -1)) {
                questionId = qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
            }
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting question display config - question " + questionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    private void renderQuestiontest(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout) {
        TextView tv;
        tv = (TextView) inflater.inflate((isRepeaterItem) ? R.layout.module_repeater_item_question : R.layout.module_question, parentLayout, false);

        // int columnIndex = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) : qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION);
        if (qCursor != null) {
            if (qCursor.moveToFirst()) {
                do {
                    String dir = qCursor.getString(qCursor.getColumnIndex("question"));
                    tv.setText(dir);
                } while (qCursor.moveToNext());
            }
        }

        // String hh = Html.fromHtml(qCursor.getString(columnIndex)) + "";

        // get disp"lay config to see if question is required
        /*try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting question display config - question " + questionId, e));
            e.printStackTrace();
        }*/

        parentLayout.addView(tv);
    }


    public void renderPhotoUploadQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

//        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));

        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        }

        if (oldDatatype != null) {
            if (oldDatatype.equals("Photo Upload")) {
                datatype = "text";
            }
        }


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

        final RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.module_photo_upload_question, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        final ImageView iv = (ImageView) rl.findViewById(R.id.modulePhotoQuestion);

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
                if (currentAnswerValue.contains("/")) {
                    String externalPath = (context.getExternalFilesDir(null) != null) ? context.getExternalFilesDir(null).getPath() : "";
                    //final String finalCurrentAnswerValue = externalPath + "/" + currentAnswerValue;
                    final String finalCurrentAnswerValue = currentAnswerValue;
                    new CountDownTimer(1000, 100) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            displayBitmap(finalCurrentAnswerValue, iv, res);
                        }
                    }.start();
                } else {
                    String externalPath = (context.getExternalFilesDir(null) != null) ? context.getExternalFilesDir(null).getPath() : "";
                    final String finalCurrentAnswerValue = externalPath + "/" + currentAnswerValue;
                    new CountDownTimer(1000, 100) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            displayBitmap(finalCurrentAnswerValue, iv, res);
                        }
                    }.start();
                }
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, currentAnswerValue, 0, 0, 0);
            }
        }

        Button modulePhotoUploadButton = (Button) rl.findViewById(R.id.modulePhotoUpload);

        String finalDatatype = datatype;
        modulePhotoUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA},
                                Utilities.REQUEST_PERMISSIONS_FOR_TIRAMISU);

                    } else {
                        bindFarmerPhotoButton(iv, questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) +
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.CAMERA},
                                Utilities.REQUEST_PERMISSIONS_FOR_M);

                    } else {
                        bindFarmerPhotoButton(iv, questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype);
                    }
                } else {
                    bindFarmerPhotoButton(iv, questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype);
                }
            }
        });

        if (isRepeaterItem) {
            parentLayout.addView(rl);
        } else {
            questionWrapper.addView(rl);
            parentLayout.addView(questionWrapper);
        }
    }


    private void bindFarmerPhotoButton(ImageView mCurrentImageView, int questionId, int answerId, int parentQuestionId, int repeaterItemPosition, String finalDatatype) {
        final CharSequence[] items = {context.getString(R.string.Camera), context.getString(R.string.Gallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.Select_picture_from));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(context.getString(R.string.Camera))) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (photoFile != null) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if ((intent.resolveActivity(context.getPackageManager()) != null)) {
                            //Uri photoURI = FileProvider.getUriForFile(AddFarmerActivity.this, getApplicationContext().getPackageName() + ".profileimage.fileprovider", photoFile);
                            Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", photoFile);

                            // Samsung Galaxy S3 Fix
                            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            for (ResolveInfo resolveInfo : resInfoList) {
                                String packageName = resolveInfo.activityInfo.packageName;
                                context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }

                            setCapturedImageURI(photoURI);
                            setCurrentPhotoPath(photoFile.getPath());
                            setCurrentImageView(mCurrentImageView);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, getCapturedImageURI());
                            setCurrentPhotoMetaData(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype);
                            if (context.getClass().getSimpleName().equalsIgnoreCase("CompleteModuleActivity")) {
                                /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                StrictMode.setVmPolicy(builder.build());*/

                                CompleteModuleActivity completeModuleActivity = (CompleteModuleActivity) context;
                                completeModuleActivity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                            } else if (context.getClass().getSimpleName().equalsIgnoreCase("AddInfrastructureActivity")) {
                                /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                StrictMode.setVmPolicy(builder.build());*/

                                AddInfrastructureActivity addInfrastructureActivity = (AddInfrastructureActivity) context;
                                addInfrastructureActivity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                            } else if (context.getClass().getSimpleName().equalsIgnoreCase(Utilities.CHILD_LABOUR_ACTIVITY)) {
                                ChildLabourCompleteModuleActivity childLabourCompleteModuleActivity = (ChildLabourCompleteModuleActivity) context;
                                childLabourCompleteModuleActivity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                            } else {
                                /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                StrictMode.setVmPolicy(builder.build());*/
                                try {
                                    ReviewFarmManagementPlan reviewFarmManagementPlan = (ReviewFarmManagementPlan) context;
                                    reviewFarmManagementPlan.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                                } catch (Exception e) {
                                    FirebaseCrashlytics.getInstance().log(e.getMessage());
                                }
                            }
                        } else {
                            Toast.makeText(context,context.getResources().getString(R.string.gallery_is_not_available_in_mobile), Toast.LENGTH_LONG).show();
                        }
                    }

                } else if (items[item].equals(context.getString(R.string.Gallery))) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (photoFile != null) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPTED_MIME_TYPES_IMG);
                        Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", photoFile);
                        setCurrentPhotoMetaData(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype);
                        setCapturedImageURI(photoURI);
                        setCurrentPhotoPath(photoFile.getPath());
                        setCurrentImageView(mCurrentImageView);
                        if (context.getClass().getSimpleName().equalsIgnoreCase("CompleteModuleActivity")) {
                            /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());*/
                            CompleteModuleActivity completeModuleActivity = (CompleteModuleActivity) context;
                            completeModuleActivity.startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
                        } else if (context.getClass().getSimpleName().equalsIgnoreCase("AddInfrastructureActivity")) {
                            /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());*/
                            AddInfrastructureActivity addInfrastructureActivity = (AddInfrastructureActivity) context;
                            addInfrastructureActivity.startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
                        } else if (context.getClass().getSimpleName().equalsIgnoreCase(Utilities.CHILD_LABOUR_ACTIVITY)) {
                            ChildLabourCompleteModuleActivity childLabourCompleteModuleActivity = (ChildLabourCompleteModuleActivity) context;
                            childLabourCompleteModuleActivity.startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
                        } else {
                            /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());*/
                            try {
                                ReviewFarmManagementPlan reviewFarmManagementPlan = (ReviewFarmManagementPlan) context;
                                reviewFarmManagementPlan.startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().log(e.getMessage());
                            }
                        }
                    }

                }
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void setFullImageFromFilePath(String imagePath, String portraitOrSignature, String requestType) {
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, portraitOrSignature);
        if (savedImagePath != null) {
            if (portraitOrSignature.equalsIgnoreCase("portrait")) {
                Uri uri = Uri.parse(savedImagePath);
                setCapturedImageURI(uri);
                setCurrentPhotoPath(savedImagePath);

            }

            displayBitmap(savedImagePath, portraitOrSignature, getCurrentImageView());
            try {
                JSONObject currentMetaData = getCurrentPhotoMetadata();
                if (requestType.equalsIgnoreCase("IMAGE_CAPTURE")) {
                    saveAnswerEntry(
                            currentMetaData.getInt("question_id"),
                            currentMetaData.optInt("answer_id", 0),
                            currentMetaData.optInt("parent_id", 0),
                            currentMetaData.optInt("repeater_position", -1),
                            currentMetaData.getString("data_type"),
                            getCurrentPhotoFileName(), 0, 0, 0
                    );
                } else {
                    saveAnswerEntry(
                            currentMetaData.getInt("question_id"),
                            currentMetaData.optInt("answer_id", 0),
                            currentMetaData.optInt("parent_id", 0),
                            currentMetaData.optInt("repeater_position", -1),
                            currentMetaData.getString("data_type"),
                            getCurrentPhotoFileName(), 0, 0, 0
                    );
                }
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().log("Dynamic Questions: Error getting photo question image :" + e.getMessage());
            }
        }
    }


    private void displayBitmap(String savedImagePath, String portraitOrSignature, ImageView target) {
        // set our desired width and height for display
        int baseWidth = 50;
        int baseHeight = 50;

        if (portraitOrSignature.equalsIgnoreCase("signature")) {
            baseWidth = 82;
            baseHeight = 28;
        }

        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, context.getResources());
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);

        target.setImageBitmap(bitmap);
    }


    void renderPhotoUploadQuestionSubQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        String datatypeduplicate = "";
        int questionIddouplicate = 0;
        if (qCursor != null) {
            if (qCursor.moveToFirst()) {
                do {
                    datatypeduplicate = qCursor.getString(qCursor.getColumnIndex("datatype_name"));

                    questionIddouplicate = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                } while (qCursor.moveToNext());
            }
        }

        final int questionId = questionIddouplicate;
        final String datatype = datatypeduplicate;


        int answerIdTemp = 0;

        /*if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
            } else {
                if (qCursor.getColumnIndex("answer_id") != -1) {
                    answerIdTemp = qCursor.getInt(qCursor.getColumnIndex("answer_id"));
                }
            }
        }*/
        final int answerId = answerIdTemp;

        final RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.module_photo_upload_question, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        final ImageView iv = (ImageView) rl.findViewById(R.id.modulePhotoQuestion);

        /*if (hasAnswers) {
            String currentAnswerValue = null;
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                currentAnswerValue = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
            } else {
                if (qCursor.getColumnIndex("answer_" + datatype) != -1) {
                    currentAnswerValue = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                }
            }

            if (currentAnswerValue != null) {
                String externalPath = (context.getExternalFilesDir(null) != null) ? context.getExternalFilesDir(null).getPath() : "";
                final String finalCurrentAnswerValue = externalPath + "/" + currentAnswerValue;
                new CountDownTimer(1000, 100) {

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        displayBitmap(finalCurrentAnswerValue, iv, res);
                    }
                }.start();

                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, currentAnswerValue);
            }
        }*/

        Button modulePhotoUploadButton = (Button) rl.findViewById(R.id.modulePhotoUpload);

        modulePhotoUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;

                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error creating photo image file");
                }

                if (photoFile != null) {
                    Uri fileUri = Uri.fromFile(photoFile);
                    setCapturedImageURI(fileUri);
                    setCurrentPhotoPath(fileUri.getPath());
                    setCurrentImageView(iv);
                    dl.prentidAnswerDelete(parentQuestionId, questionId);

                    long f = dl.prentidAnswer(Utilities.subModuleId, parentQuestionId, questionId, fileUri.getPath(), "photo", Utilities.olamFarmerId, Utilities.moduleId + "");
                    // setCurrentPhotoMetaData(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype);

                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, getCapturedImageURI());
                    if (context.getClass().getSimpleName().equalsIgnoreCase("CompleteModuleActivity")) {
                        CompleteModuleActivity completeModuleActivity = (CompleteModuleActivity) context;
                        completeModuleActivity.startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                    } else {
                        ReviewFarmManagementPlan reviewFarmManagementPlan = (ReviewFarmManagementPlan) context;
                        reviewFarmManagementPlan.startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                    }

                }
            }
        });

        if (isRepeaterItem) {
            parentLayout.addView(rl);
        } else {
            questionWrapper.addView(rl);
            parentLayout.addView(questionWrapper);
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "ofis_module_answer_" + timestamp;
        File storageDir = context.getExternalFilesDir("");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        setCurrentPhotoPath("file:" + image.getAbsolutePath());
        setCurrentPhotoFileName(image.getName());
        return image;
    }


    private void setCurrentImageView(ImageView iv) {
        this.mCurrentImageView = iv;
    }

    private ImageView getCurrentImageView() {
        return this.mCurrentImageView;
    }

    private void setCurrentPhotoMetaData(int questionId, int answerId, int parentQuestionId, int repeaterItemPosition, String datatype) {
        try {
            this.mCurrentPhotoMetadata.put("question_id", questionId);
            this.mCurrentPhotoMetadata.put("answer_id", answerId);
            this.mCurrentPhotoMetadata.put("parent_id", parentQuestionId);
            this.mCurrentPhotoMetadata.put("repeater_position", repeaterItemPosition);
            this.mCurrentPhotoMetadata.put("data_type", datatype);
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error creating photo metadata");
        }
    }

    private JSONObject getCurrentPhotoMetadata() {
        return this.mCurrentPhotoMetadata;
    }

    private String getCurrentPhotoFileName() {
        return this.mCurrentPhotoFileName;
    }

    private void setCurrentPhotoFileName(String mCurrentPhotoFileName) {
        this.mCurrentPhotoFileName = mCurrentPhotoFileName;
    }

    public String getCurrentPhotoPath() {
        return this.mCurrentPhotoPath;
    }

    private void setCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }

    private Uri getCapturedImageURI() {
        return this.mCapturedImageURI;
    }

    private void setCapturedImageURI(Uri mCapturedImageURI) {
        this.mCapturedImageURI = mCapturedImageURI;
    }

    public void setFullImageFromFilePath(Resources res) {
        String imagePath = getCurrentPhotoPath();
        ImageView target = getCurrentImageView();
        String savedImagePath = Utilities.scaleRotateAndSaveImage(imagePath, "modulephoto");

        if (savedImagePath != null) {
            Uri uri = Uri.parse(savedImagePath);
            setCapturedImageURI(uri);
            setCurrentPhotoPath(savedImagePath);

            displayBitmap(savedImagePath, target, res);

            try {
                JSONObject currentMetaData = getCurrentPhotoMetadata();
                saveAnswerEntry(
                        currentMetaData.getInt("question_id"),
                        currentMetaData.optInt("answer_id", 0),
                        currentMetaData.optInt("parent_id", 0),
                        currentMetaData.optInt("repeater_position", -1),
                        currentMetaData.getString("data_type"),
                        getCurrentPhotoFileName(), 0, 0, 0
                );
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().log("Dynamic Questions: Error getting photo question image :" + e.getMessage());
            }
        }
    }

    private void displayBitmap(String savedImagePath, ImageView target, Resources res) {
        // set our desired width and height for display
        int baseWidth = 50;
        int baseHeight = 50;

        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);

        target.setImageBitmap(bitmap);
    }

    public void renderDatePickerQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, boolean isSavedAns) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

//        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));

        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        }

        if (oldDatatype != null) {
            if (oldDatatype.equals("Date Picker")) {
                datatype = "date";
            }
        }


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
        // get display config
        try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                boolean showDay = display.getBoolean("show_day");
                boolean showMonth = display.getBoolean("show_month");
                boolean showyear = display.getBoolean("show_year");

                if ((!showDay) && (!showMonth) && (showyear)) {
                    // just the year, show the fifty year dropdown instead
                    final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
                    //  String[] since1950 = Utilities.since1950();
                    String[] since1900 = Utilities.since1900();

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1900);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner.setAdapter(adapter);

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
                            for (int i = 0; i < spinner.getCount(); i++) {
                                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(currentAnswerValue.substring(0, 4))) {
                                    spinner.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    String finalDatatype = datatype;
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, spinner.getSelectedItem().toString() + "-01-01", 0, 0, 0);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    if (isRepeaterItem) {
                        parentLayout.addView(spinner);
                    } else {
                        questionWrapper.addView(spinner);
                        parentLayout.addView(questionWrapper);
                    }
                } else {
                    Calendar today = Calendar.getInstance();
                    DatePicker dp = (DatePicker) inflater.inflate(R.layout.module_datepicker, questionWrapper, false);
                    final SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

                    if (hasAnswers) {
                        String answerDateString = null;
                        if (isRepeaterItem && repeaterAnswers != null) {
                            if (repeaterAnswers.getCount() > 0) {
                                answerDateString = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
                            }
                        } else {
                            answerDateString = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                        }
                        if (answerDateString != null) {
                            today.setTime(serverDateFormat.parse(answerDateString));
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, answerDateString, 0, 0, 0);
                        }
                    } else {
                        /* OCA-59 - Date Picker Question */
                        // Solution : Update the current data during inflate the view
                        Calendar cal = Calendar.getInstance();
                        saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, serverDateFormat.format(cal.getTime()), 0, 0, 0);
                    }

                    String finalDatatype1 = datatype;
                    dp.init(
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH),
                            new DatePicker.OnDateChangedListener() {
                                @Override
                                public void onDateChanged(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                                    // get the date
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(selectedYear, selectedMonth, selectedDay);
                                    saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype1, serverDateFormat.format(cal.getTime()), 0, 0, 0);
                                }
                            }
                    );

                    if (isRepeaterItem) {
                        parentLayout.addView(dp);
                    } else {
                        questionWrapper.addView(dp);
                        parentLayout.addView(questionWrapper);
                    }
                }
            }


        } catch (JSONException | ParseException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error creating date picker display :" + e.getMessage());
        }
    }

    public void renderCaptureLocationQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, Location mCurrentCapturedLocation) {
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

        try {
            final TextView textView = (TextView) inflater.inflate(R.layout.module_location_upload_question, (isRepeaterItem) ? parentLayout : questionWrapper, false);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                            String locationValue = mCurrentCapturedLocation.getLatitude() +","+mCurrentCapturedLocation.getLongitude();
                    saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, "locationValue", 0, 0, 0);
                    textView.setText(context.getResources().getString(R.string.record_farm_location_complete));
                    textView.setBackgroundResource(R.drawable.list_item_disabled);
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setEnabled(false);
                }
            });

            questionWrapper.addView(textView);
            parentLayout.addView(questionWrapper);

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error creating date picker display :" + e.getMessage());
        }
    }

    void renderDatePickerQuestiontest(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        // renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

        String datatypeduplicate = "";
        int questionIddouplicate = 0;
        if (qCursor != null) {
            if (qCursor.moveToFirst()) {
                do {
                    datatypeduplicate = qCursor.getString(qCursor.getColumnIndex("datatype_name"));

                    questionIddouplicate = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                } while (qCursor.moveToNext());
            }
        }

        final int questionId = questionIddouplicate;
        final String datatype = datatypeduplicate;
        int answerIdTemp = 0;
        /*if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
            } else {
                if (qCursor.getColumnIndex("answer_id") != -1) {
                    answerIdTemp = qCursor.getInt(qCursor.getColumnIndex("answer_id"));
                }
            }
        }*/
        final int answerId = answerIdTemp;


        // get display config
        try {


            int lookupvalue = 0;

            JSONObject displayConfig = new JSONObject();
            String columnNameduplicate = "";
            if (qCursor != null) {
                if (qCursor.moveToFirst()) {
                    do {
                        displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                        columnNameduplicate = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;

                        lookupvalue = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                    } while (qCursor.moveToNext());
                }
            }


            final String columnName = columnNameduplicate;


            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                boolean showDay = display.getBoolean("show_day");
                boolean showMonth = display.getBoolean("show_month");
                boolean showyear = display.getBoolean("show_year");

                if ((!showDay) && (!showMonth) && (showyear)) {
                    // just the year, show the fifty year dropdown instead
                    final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
                    //  String[] since1950 = Utilities.since1950();
                    String[] since1900 = Utilities.since1900();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1900);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    /*if (hasAnswers) {
                        String currentAnswerValue = null;
                        if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                            currentAnswerValue = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
                        } else {
                            if (qCursor.getColumnIndex("answer_" + datatype) != -1) {
                                currentAnswerValue = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                            }
                        }
                        if (currentAnswerValue != null) {
                            for (int i = 0; i < spinner.getCount(); i++) {
                                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(currentAnswerValue.substring(0, 4))) {
                                    spinner.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }*/

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                            // saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, spinner.getSelectedItem().toString() + "-01-01");
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    if (isRepeaterItem) {
                        parentLayout.addView(spinner);
                    } else {
                        questionWrapper.addView(spinner);
                        parentLayout.addView(questionWrapper);
                    }
                } else {
                    Calendar today = Calendar.getInstance();
                    DatePicker dp = (DatePicker) inflater.inflate(R.layout.module_datepicker, questionWrapper, false);
                    final SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

                   /* if (hasAnswers) {
                        String answerDateString = null;
                        if (isRepeaterItem && repeaterAnswers != null) {
                            if (repeaterAnswers.getCount() > 0) {
                                answerDateString = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
                            }
                        } else {
                            answerDateString = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                        }
                        if (answerDateString != null) {
                            today.setTime(serverDateFormat.parse(answerDateString));
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, answerDateString);
                        }
                    }*/


                    Cursor cursor = dl.getParentSubquestionAnswer(Utilities.subModuleId, parentQuestionId, questionId);


                    int position = 0;
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {

                                today.setTime(serverDateFormat.parse(cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ANSWER))));

                            } while (qCursor.moveToNext());
                        }
                    }


                    dp.init(
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH),
                            new DatePicker.OnDateChangedListener() {
                                @Override
                                public void onDateChanged(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                                    // get the date
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(selectedYear, selectedMonth, selectedDay);

                                    dl.prentidAnswerDelete(parentQuestionId, questionId);

                                    dl.prentidAnswer(Utilities.subModuleId, parentQuestionId, questionId, serverDateFormat.format(cal.getTime()), 0 + "", Utilities.olamFarmerId, Utilities.moduleId + "");
                                    //saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, serverDateFormat.format(cal.getTime()));
                                }
                            }
                    );

                    if (isRepeaterItem) {
                        parentLayout.addView(dp);
                    } else {
                        questionWrapper.addView(dp);
                        parentLayout.addView(questionWrapper);
                    }
                }
            }


        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error creating date picker display :" + e.getMessage());
        }
    }

    public void renderTextBoxQuestion(final Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        int columnIndex = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) : qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION);

//        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));

        /* // for textbox getting datatype from answerType table
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
        final String datatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));*/

        final int dataTypeID = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));

        final String answerTypeForDatatype;
        String datatypeOld = null;
        try {
            answerTypeForDatatype = dl.getAnswerTypeByQuestionID(questionId);
            datatypeOld = dl.getAnswerTypeValue(Integer.parseInt(answerTypeForDatatype));
        } catch (Exception e) {
            e.printStackTrace();
        }


        String datatype = "";
        if (dataTypeID == 6) {
            if (datatypeOld != null) {
                if (datatypeOld.equals("Alphanumeric") || datatypeOld.equals("Alphabetical")) {
                    datatype = "text";
                } else if (datatypeOld.equals("Numbers Only") || datatypeOld.equals("Percentage")) {
                    datatype = "int";
                } else if (datatypeOld.equals("Decimal")) {
                    datatype = "decimal";
                }
            }
        } else {
            datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        }

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

        String metricGlobalId = dl.getGlobalIdByQuestionId(questionId);
//        String metricGlobalId = "2";
        final Integer[] selectedLocalID = {0};
        final Integer[] selectedGlobalID = {0};
        final boolean[] unCheckAllBox = {false};
        boolean isLocalUnit = false;
        // Create an inner wrapper
        LinearLayout innerWrapper = new LinearLayout(context);
        innerWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        innerWrapper.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout previewLayout = new LinearLayout(context);
        previewLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        previewLayout.setOrientation(LinearLayout.VERTICAL);
        previewLayout.setPadding(16, 16, 16, 16);

        TextView titleTextView = new TextView(context);
        titleTextView.setText(context.getResources().getString(R.string.converted_metrics));
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        titleTextView.setTextColor(context.getResources().getColor(R.color.weightage_bar));

        // Create the second TextView
        TextView contentTextView = new TextView(context);
        contentTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Add TextViews to the LinearLayout
        previewLayout.addView(titleTextView);
        previewLayout.addView(contentTextView);
        previewLayout.setVisibility(View.GONE);


        final Spinner spinner = (Spinner) inflater.inflate(R.layout.metric_spinner, innerWrapper, false);
        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
            List<LocalUnitDao.UnitMapping> unitMappings = roomDB.localUnitDao().getValueByGlobalId(Integer.parseInt(metricGlobalId));

            if (unitMappings.isEmpty()) {

                List<String> getGlobalUnit = roomDB.globalUnitDao().getGlobalAbbreviation(Integer.parseInt(metricGlobalId));
                selectedGlobalID[0] = Integer.parseInt(metricGlobalId);
                LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                        0, // Width set to 0
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2 // Weight set to 2 (20% of the total weight)
                );
                spinnerParams.setMargins(7, 0, 0, 0);
                spinner.setLayoutParams(spinnerParams);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, getGlobalUnit);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                /*if (hasAnswers){
                    int localAnswer = Integer.valueOf(dl.getGlobalIdByAnswerID(answerId));
                    spinner.setSelection(localAnswer);
                }else {
//                    spinner.setSelection(Integer.parseInt(metricGlobalId));
                    spinner.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), getGlobalUnit.keySet().toArray(new Integer[0])));
                }*/
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item from the adapter
                        String selectedAbbreviation = spinnerAdapter.getItem(position);

//                        Toast.makeText(context, "Selected localUnitId: " + selectedAbbreviation, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing here if needed
                    }
                });


            } else {
                isLocalUnit = true;
                Map<Integer, String> spinnerData = new HashMap<>();
                List<GlobalUnitDao.UnitMapping> globalMappings = roomDB.globalUnitDao().getGlobalId(Integer.parseInt(metricGlobalId));
                for (GlobalUnitDao.UnitMapping unitMapping : globalMappings) {
                    spinnerData.put(unitMapping.globalUnitId, unitMapping.globalAbbreviation);
                }

                for (LocalUnitDao.UnitMapping unitMapping : unitMappings) {
                    spinnerData.put(unitMapping.localUnitId, unitMapping.localUnitAbbreviation);
                }

                List<String> spinnerItemList = new ArrayList<>(spinnerData.values());
                LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                        0, // Width set to 0
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2 // Weight set to 2 (20% of the total weight)
                );
                spinnerParams.setMargins(7, 0, 0, 0);
                spinner.setLayoutParams(spinnerParams);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, spinnerItemList);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                if (answerId != 0) {
                    int localAnswer = Integer.valueOf(dl.getLocalIdByAnswerID(answerId));
                    if (localAnswer == 0) {
                        spinner.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), spinnerData.keySet().toArray(new Integer[0])));
                    } else {
                        spinner.setSelection(getIndexForValue(localAnswer, spinnerData.keySet().toArray(new Integer[0])));
                    }

                } else {
//                    spinner.setSelection(Integer.parseInt(metricGlobalId));
                    spinner.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), spinnerData.keySet().toArray(new Integer[0])));
                }
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item from the adapter
                        String selectedAbbreviation = spinnerAdapter.getItem(position);

                        // Find the corresponding localUnitId in the spinnerData map
                        for (Map.Entry<Integer, String> entry : spinnerData.entrySet()) {
                            if (entry.getValue().equals(selectedAbbreviation)) {
                                int selectedLocalUnitId = entry.getKey();

                                if (entry.getKey() == Integer.parseInt(metricGlobalId)) {
                                    selectedGlobalID[0] = entry.getKey();
                                    selectedLocalID[0] = 0;
                                } else {
                                    selectedLocalID[0] = entry.getKey();
                                    selectedGlobalID[0] = 0;
                                }

                                unCheckAllBox[0] = true;

                                // Display a Toast with the selected localUnitId
//                                Toast.makeText(context, "Selected localUnitId: " + selectedLocalUnitId, Toast.LENGTH_SHORT).show();
                                if (answerSet != null) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");

                                            if (currentQuestionId == questionId) {
                                                try {
                                                    currentAnswer.put("globalMetricId", selectedGlobalID[0]);
                                                    currentAnswer.put("localMetricId", selectedLocalID[0]);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                        }
                                    }
                                }

                                if (answerSet != null) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");
                                            int answerJsonVal = currentAnswer.optInt("answer");

                                            if (currentQuestionId == questionId) {
                                                Integer globalMetricIdAnswer = null;
                                                Integer localMetricIdAnswer = null;
                                                if (currentAnswer.has("globalMetricId")) {
                                                    globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                }
                                                if (currentAnswer.has("localMetricId")) {
                                                    localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                }
                                                if (localMetricIdAnswer != 0) {
                                                    previewLayout.setVisibility(View.VISIBLE);
//                                                    titleTextView.setVisibility(View.VISIBLE);
//                                                    contentTextView.setVisibility(View.VISIBLE);
                                                    String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                                    String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                    String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                    String answerValue = et.getText().toString();
                                                    contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(String.valueOf(answerJsonVal)) * Double.parseDouble(localConversion)) + " " + localAbb);
                                                } else {
                                                    previewLayout.setVisibility(View.GONE);
                                                }


                                            }

                                        }
                                    }
                                }
                                Log.d("AnswerNewSet", answerSet.toString());
                                break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing here if needed
                    }
                });
            }
        }

        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
            LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                    0, // Width set to 0
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    8 // Weight set to 8 (80% of the total weight)
            );
            et.setLayoutParams(editTextParams);
        }
        et.setFilters(new InputFilter[]{new EmojiExcludeFilter()});
        et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        et.setTag(questionId);
        if (hasAnswers) {
            String currentAnswerValue = null;
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                currentAnswerValue = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
            } else {
                if (qCursor.getColumnIndex("answer_" + datatype) != -1) {
                    if (Objects.equals(datatype, "decimal")) {
                        currentAnswerValue = qCursor.getString(qCursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL))!=null ? String.valueOf(BigDecimal.valueOf(qCursor.getDouble(qCursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL)))) : null;
                    }
                    else {
                        currentAnswerValue = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                    }

                }
            }

            if (currentAnswerValue != null) {
                et.setText(currentAnswerValue);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, currentAnswerValue, selectedGlobalID[0], selectedLocalID[0], 0);
            }
        }

        if (datatype.equals("decimal")) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if (datatype.equals("int")) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        }

        String answerValue = null;
        String answerType = "";
        String display_configuration = "";
        try {
//            displayTypeID = qCursor.getInt(qCursor.getColumnIndex("displaytypeId"));
            answerType = dl.getAnswerTypeByQuestionID(questionId);
            answerValue = dl.getAnswerTypeValue(Integer.parseInt(answerType));
            display_configuration = dl.getDisplayConfigurationByQuestionID(questionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String globally;
        String assignmentType = "";
        String assignmentField = "";
        String assignmentFields = "";
        String finalDisplay_configuration = display_configuration;


        try {
            if (answerValue.equals("Alphanumeric")) {
                try {
                    JSONObject displayConfig = new JSONObject();
                    displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                    if (displayConfig.has("display")) {
                        JSONObject display = displayConfig.getJSONObject("display");
                        int answerLimit = display.getInt("answerLimit");
                        setAlphanumericFilter(et, answerLimit);
                        et.setHint("Character Limit : " + answerLimit);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (answerValue.equals("Numbers Only")) {
//                et.setInputType(InputType.TYPE_CLASS_NUMBER);
                try {
                    int min = 0, max = 0;
                    JSONObject displayConfig = new JSONObject();
                    displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                    if (displayConfig.has("display")) {
                        JSONObject display = displayConfig.getJSONObject("display");
                        String answerLimit = display.get("answerLimit").toString();

                        if (!answerLimit.equals("null")) {
                            et.setInputType(InputType.TYPE_CLASS_NUMBER);
                            et.setHint("Character limit : " + answerLimit);
                            setIntegerWithCharacterLimit(et, Integer.parseInt(answerLimit));

                        } else {


                            JSONArray range = display.getJSONArray("range");
                            int validationOnRange = display.optInt("validationOnRangeId");

                            int[] farmerOrderList = new int[]{6, 5, 8, 9, 4, 3, 2, 1, 7, 0};
                            boolean valueFound = false;
                            for (int orderList : farmerOrderList) {

                                for (int i = 0; i < range.length(); i++) {
                                    JSONObject rangeArray = range.getJSONObject(i);

                                    globally = rangeArray.getString("globally");
                                    assignmentType = rangeArray.getString("assignmentType");
                                    if (assignmentType.equals("null")) {
                                        assignmentType = "0";
                                    }
                                    assignmentField = rangeArray.getString("assignmentField");
                                    assignmentFields = rangeArray.getString("assignmentFields");

                                    if (Integer.parseInt(assignmentType) == orderList && Integer.parseInt(assignmentType) != 0) {

                                        if (!assignmentField.equals("null")) {
                                            long farmerId = Utilities.farmerId;
                                            roomDB = OFISRoomDB.getDbInstance(context);
                                            String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));
                                            if (assignmentField.equals(farmerAssignmentField) && globally.equals("false") && !valueFound) {
                                                min = rangeArray.getInt("min");
                                                max = rangeArray.getInt("max");
                                                if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                                    et.setHint(String.format(context.getString(R.string.range_hint_A_R), min, max));
                                                } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                                    et.setHint(String.format(context.getString(R.string.range_hint_A_R), min, max));
                                                } else {
                                                    et.setHint(String.format(context.getResources().getString(R.string.alert_review_hint), min, max));
                                                }
                                                if (min < 0) {
                                                    setIntegerNegativeRangeFilter(et, min, max, validationOnRange, rangeArray);
                                                } else {
                                                    setIntegerPositiveRangeFilter(et, min, max, validationOnRange, rangeArray);
                                                }
                                                valueFound = true;
                                                break;
                                            } else if (assignmentType.equals("null") && assignmentField.equals("null") && globally.equals("true") && !valueFound) {
                                                min = rangeArray.getInt("min");
                                                max = rangeArray.getInt("max");
                                                if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                                    et.setHint(String.format(context.getString(R.string.range_hint_A_R), min, max));
                                                } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                                    et.setHint(String.format(context.getString(R.string.range_hint_A_R), min, max));
                                                } else {
                                                    et.setHint(String.format(context.getResources().getString(R.string.alert_review_hint), min, max));
                                                }
                                                if (min < 0) {
                                                    setIntegerNegativeRangeFilter(et, min, max, validationOnRange, rangeArray);
                                                } else {
                                                    setIntegerPositiveRangeFilter(et, min, max, validationOnRange, rangeArray);
                                                }
                                            }
                                        } else if (!assignmentFields.equals("null") && !valueFound) {

                                            // Remove square brackets and split the string into an array of strings
                                            String[] stringArray = assignmentFields.substring(1, assignmentFields.length() - 1).split(",");

                                            // Convert the string array to an integer array
                                            int[] intArray = new int[stringArray.length];
                                            for (int m = 0; m < stringArray.length; m++) {
                                                intArray[m] = Integer.parseInt(stringArray[m]);
                                            }

                                            for (int fieldMultipleAssignment : intArray) {
                                                long farmerId = Utilities.farmerId;
                                                roomDB = OFISRoomDB.getDbInstance(context);
                                                String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                                if (String.valueOf(fieldMultipleAssignment).equals(farmerAssignmentField)) {
                                                    min = rangeArray.getInt("min");
                                                    max = rangeArray.getInt("max");
                                                    if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                                        et.setHint(String.format(context.getString(R.string.range_hint_A_R), min, max));
                                                    } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                                        et.setHint(String.format(context.getString(R.string.range_hint_A_R), min, max));
                                                    } else {
                                                        et.setHint(String.format(context.getResources().getString(R.string.alert_review_hint), min, max));
                                                    }
                                                    if (min < 0) {
                                                        et.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
                                                        setIntegerNegativeRangeFilter(et, min, max, validationOnRange, rangeArray);
                                                    } else {
                                                        et.setInputType(InputType.TYPE_CLASS_NUMBER);
                                                        setIntegerPositiveRangeFilter(et, min, max, validationOnRange, rangeArray);
                                                    }

                                                    valueFound = true;
                                                    break;
                                                }

                                            }

                                        }

                                    } else if (Integer.parseInt(assignmentType) == 0 && orderList == 0 && !valueFound) {
                                        min = rangeArray.getInt("min");
                                        max = rangeArray.getInt("max");
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                            et.setHint(String.format(context.getString(R.string.range_hint_A_R), min, max));
                                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            et.setHint(String.format(context.getString(R.string.range_hint_A_R), min, max));
                                        } else {
                                            et.setHint(String.format(context.getResources().getString(R.string.alert_review_hint), min, max));
                                        }
                                        if (min < 0) {
                                            et.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
                                            setIntegerNegativeRangeFilter(et, min, max, validationOnRange, rangeArray);
                                        } else {
                                            et.setInputType(InputType.TYPE_CLASS_NUMBER);
                                            setIntegerPositiveRangeFilter(et, min, max, validationOnRange, rangeArray);
                                        }
                                    }

                                }

                            }

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (answerValue.equals("Decimal")) {
                try {
                    double min = 0, max = 0;
                    JSONObject displayConfig = new JSONObject();
                    displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                    if (displayConfig.has("display")) {
                        JSONObject display = displayConfig.getJSONObject("display");
                        String answerLimit = display.get("answerLimit").toString();
                        int maxDecimalLimit = display.getInt("maxDecimalLimit");

                        if (!answerLimit.equals("null")) {
                            et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                            et.setHint("Character limit : " + answerLimit);
                            setDecimalWithCharacterLimit(et, Integer.parseInt(answerLimit), maxDecimalLimit);

                        } else {

                            JSONArray range = display.getJSONArray("range");
                            int validationOnRange = display.optInt("validationOnRangeId");

                            int[] farmerOrderList = new int[]{6, 5, 8, 9, 4, 3, 2, 1, 7, 0};
                            boolean valueFound = false;
                            for (int orderList : farmerOrderList) {

                                for (int i = 0; i < range.length(); i++) {
                                    JSONObject rangeArray = range.getJSONObject(i);

                                    globally = rangeArray.getString("globally");
                                    assignmentType = rangeArray.getString("assignmentType");
                                    if (assignmentType.equals("null")) {
                                        assignmentType = "0";
                                    }
                                    assignmentField = rangeArray.getString("assignmentField");
                                    assignmentFields = rangeArray.getString("assignmentFields");

                                    if (Integer.parseInt(assignmentType) == orderList && Integer.parseInt(assignmentType) != 0) {
                                        if (!assignmentField.equals("null")) {
                                            long farmerId = Utilities.farmerId;
                                            roomDB = OFISRoomDB.getDbInstance(context);
                                            String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));
                                            if (assignmentField.equals(farmerAssignmentField) && globally.equals("false") && !valueFound) {
                                                min = rangeArray.getDouble("min");
                                                max = rangeArray.getDouble("max");
                                                if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                                    et.setHint(String.format(context.getString(R.string.range_hint_A_R_decimal), min, max));
                                                } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                                    et.setHint(String.format(context.getString(R.string.range_hint_A_R_decimal), min, max));
                                                } else {
                                                    et.setHint(String.format(context.getResources().getString(R.string.alert_review_hint_decimal), min, max));
                                                }
                                                if (min < 0) {
                                                    et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
                                                    setNegativeDecimalFilter(et, min, max, validationOnRange, maxDecimalLimit, rangeArray);
                                                } else {
                                                    et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                                                    setPositiveDecimalFilter(et, min, max, validationOnRange, maxDecimalLimit, rangeArray);
                                                }

                                                break;
                                            } else if (assignmentType.equals("null") && assignmentField.equals("null") && globally.equals("true") && !valueFound) {
                                                min = rangeArray.getDouble("min");
                                                max = rangeArray.getDouble("max");
                                                if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                                    et.setHint(String.format(context.getString(R.string.range_hint_A_R_decimal), min, max));
                                                } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                                    et.setHint(String.format(context.getString(R.string.range_hint_A_R_decimal), min, max));
                                                } else {
                                                    et.setHint(String.format(context.getResources().getString(R.string.alert_review_hint_decimal), min, max));
                                                }

                                                if (min < 0) {
                                                    et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
                                                    setNegativeDecimalFilter(et, min, max, validationOnRange, maxDecimalLimit, rangeArray);
                                                } else {
                                                    et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                                                    setPositiveDecimalFilter(et, min, max, validationOnRange, maxDecimalLimit, rangeArray);

                                                }
                                            }

                                        } else if (!assignmentFields.equals("null") && !valueFound) {

                                            // Remove square brackets and split the string into an array of strings
                                            String[] stringArray = assignmentFields.substring(1, assignmentFields.length() - 1).split(",");

                                            // Convert the string array to an integer array
                                            int[] intArray = new int[stringArray.length];
                                            for (int m = 0; m < stringArray.length; m++) {
                                                intArray[m] = Integer.parseInt(stringArray[m]);
                                            }

                                            for (int fieldMultipleAssignment : intArray) {

                                                long farmerId = Utilities.farmerId;
                                                roomDB = OFISRoomDB.getDbInstance(context);
                                                String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                                if (String.valueOf(fieldMultipleAssignment).equals(farmerAssignmentField)) {
                                                    min = rangeArray.getDouble("min");
                                                    max = rangeArray.getDouble("max");
                                                    if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                                        et.setHint(String.format(context.getString(R.string.range_hint_A_R_decimal), min, max));
                                                    } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                                        et.setHint(String.format(context.getString(R.string.range_hint_A_R_decimal), min, max));
                                                    } else {
                                                        et.setHint(String.format(context.getResources().getString(R.string.alert_review_hint_decimal), min, max));
                                                    }
                                                    if (min < 0) {
                                                        et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
                                                        setNegativeDecimalFilter(et, min, max, validationOnRange, maxDecimalLimit, rangeArray);
                                                    } else {
                                                        et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                                                        setPositiveDecimalFilter(et, min, max, validationOnRange, maxDecimalLimit, rangeArray);
                                                    }

                                                    valueFound = true;
                                                    break;

                                                }


                                            }

                                        }
                                    } else if (Integer.parseInt(assignmentType) == 0 && orderList == 0 && !valueFound) {
                                        min = rangeArray.getDouble("min");
                                        max = rangeArray.getDouble("max");
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                            et.setHint(String.format(context.getString(R.string.range_hint_A_R_decimal), min, max));
                                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            et.setHint(String.format(context.getString(R.string.range_hint_A_R_decimal), min, max));
                                        } else {
                                            et.setHint(String.format(context.getResources().getString(R.string.alert_review_hint_decimal), min, max));
                                        }
                                        if (min < 0) {
                                            et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
                                            setNegativeDecimalFilter(et, min, max, validationOnRange, maxDecimalLimit, rangeArray);
                                        } else {
                                            et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                                            setPositiveDecimalFilter(et, min, max, validationOnRange, maxDecimalLimit, rangeArray);
                                        }
                                    }
                                }
                            }

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (answerValue.equals("Alphabetical")) {
                try {
                    JSONObject displayConfig = new JSONObject();
                    displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                    if (displayConfig.has("display")) {
                        JSONObject display = displayConfig.getJSONObject("display");
                        int answerLimit = display.getInt("answerLimit");
                        if (!String.valueOf(answerLimit).equals(null)) {
                            et.setHint("Character limit : 0 to " + answerLimit);
                            setAlphabetWithCharacterLimit(et, answerLimit);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (answerValue.equals("Percentage")) {
                InputFilter filter = new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String filteredString = source.toString();
                        boolean isValid = filteredString.matches("^[0-9.%]*$");
                        if (!isValid) {
                            return "";
                        }
                        return null;
                    }
                };
                et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                et.setFilters(new InputFilter[]{filter});
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting textbox display config :" + e.getMessage());
        }
        final boolean isHundredPercentQuestion = isHPQTemp;

        final JSONObject finalDisplayConfig = displayConfig;

        // Check the calculation tag Availability
        String calculationNode = displayConfig.optString(CoffeeConstants.CALCULATION, "noTag");
        String defaultValue = "";
        try {
            JSONObject jsonObject = new JSONObject(calculationNode);
            defaultValue = jsonObject.getString(CoffeeConstants.DEFAULT);
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        if (!defaultValue.equalsIgnoreCase("")) {
            // Condition Verification Code
            int farmerModuleId = (int) Utilities.farmerModuleId;
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
            if (answer.equalsIgnoreCase("")) {
                et.setText(defaultValue);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, defaultValue, selectedGlobalID[0], selectedLocalID[0], 0);
            } else {
                et.setText(answer);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, answer, selectedGlobalID[0], selectedLocalID[0], 0);
            }
        }

        // restrict decimel places - AS-Q1, AS-Q2
        int decimelRestrict = 0;
        try {
            decimelRestrict = CoffeeConstraints.isDecimalRestrict(displayConfig);
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
        if (decimelRestrict > 0 && datatype.equals("decimal") ) {
            et.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(decimelRestrict)});
        }
        // Decimal 2 digits concept - End

        Drawable originalDrawable = et.getBackground();
        String finalDefaultValue = defaultValue;
        final String[] initialText = {""};
        String finalDatatype = datatype;
        boolean finalIsLocalUnit = isLocalUnit;
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                initialText[0] = et.getText().toString().trim();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.d("intial", charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = et.getText().length();

                if (finalDatatype.equals("int") ) {
                    if (length > 9) {
                        et.getText().delete(length - 1, length);
                    }
                }else if (finalDatatype.equals("decimal") ) {
                    if (length > 12) {
                        et.getText().delete(length - 1, length);
                    }
                }
                if ((isHundredPercentQuestion) && (!updatingHundredPercent)) {
                    autofill100Percent(et);
                }
                double metricConversionValue = 0;
                /*if (finalIsLocalUnit){
                    if (finalDatatype.equals("int")){
                        double conversionFactor = roomDB.localUnitDao().getConversionFactorById(selectedLocalID[0]);
                        metricConversionValue = conversionFactor * Double.parseDouble(et.getText().toString());
                    }
                }*/
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, et.getText().toString(), selectedGlobalID[0], selectedLocalID[0], metricConversionValue);


                //   Check the dependent questions - OFS-44 Check
                String beforeVal = et.getText().toString().trim();
                boolean dotCheck = false;
                if (beforeVal.length() > 0) {
                    if (beforeVal.substring(beforeVal.length() - 1).equalsIgnoreCase(".")) {
                        dotCheck = true;
                    }
                }
                if (!dotCheck) {
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




                /* Contstraints Check- Coffee Survey Setup Project franklin*/
                String value = et.getText().toString().trim();
                if (value.equalsIgnoreCase("")) {
                    // if the default value is available, for empty value is given , insert the default value for that question.
                    if (!finalDefaultValue.equalsIgnoreCase("")) {
                        et.setText(finalDefaultValue);
                        et.setSelection(et.getText().length());
                        saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, finalDefaultValue, selectedGlobalID[0], selectedLocalID[0], 0);
                    } else {
                        removeAnswerEntry(questionId);
                    }
                } else {
                    if (!finalDefaultValue.equalsIgnoreCase("")) {
                        if (initialText[0].equalsIgnoreCase("0")) {
                            et.setText(value.replace("0", ""));
                            et.setSelection(et.getText().length());
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, et.getText().toString(), selectedGlobalID[0], selectedLocalID[0], 0);
                        }
                    }
                }

                QuestionInfo questionInfo = new QuestionInfo();
                questionInfo.setQuestionId(questionId);
                questionInfo.setDataType(finalDatatype);
                Constraint constraint = null;
                try {
                    et.setError(null);
                    constraint = new CoffeeConstraints(context, questionInfo, dl, answerSet, qCursor).constraintType(finalDisplayConfig, value);
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().log(e.getMessage());
                }
                if (constraint != null) {
                    if (constraint.getErrorMsg() != null) {
                        et.setError(constraint.getErrorMsg());
                        et.setBackgroundResource(R.drawable.rounder_red_edittext);
                        // New
                        removeAnswerEntry(questionId);
                        if (constraint.isOverLimit()) {
                            constraintsSet.add(questionId);
                        }

                        // Old remove Logic
//                            if (et.getText().toString().trim().equalsIgnoreCase("")){
//                                // Remove the data from existing Database if data persists
//                                removeAnswerEntryDatabase((int) Utilities.farmerModuleId,questionId);
//                            }else{
//                                removeAnswerEntry(questionId);
//                            }

                    } else {
                        et.setBackground(originalDrawable);
                        constraintsSet.remove(questionId);
                        editQuestionSet.remove(questionId);
                        // et.setError(null);
                    }
                }

                if (et.getText().toString() != null && !TextUtils.isEmpty(et.getText().toString())) {
                    if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                        if (answerSet != null) {
                            for (int i = 0; i < answerSet.size(); i++) {
                                JSONObject currentAnswer = answerSet.get(i);

                                if (currentAnswer != null) {
                                    int currentQuestionId = currentAnswer.optInt("question_id");
                                    int answerJsonVal = currentAnswer.optInt("answer");

                                    if (currentQuestionId == questionId) {
                                        Integer globalMetricIdAnswer = null;
                                        Integer localMetricIdAnswer = null;
                                        if (currentAnswer.has("globalMetricId")) {
                                            globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                        }
                                        if (currentAnswer.has("localMetricId")) {
                                            localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                        }
                                        if (localMetricIdAnswer != 0) {
                                            previewLayout.setVisibility(View.VISIBLE);
//                                                    titleTextView.setVisibility(View.VISIBLE);
//                                                    contentTextView.setVisibility(View.VISIBLE);
                                            String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                            String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                            String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                            String answerValue = et.getText().toString();
                                            contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(String.valueOf(answerJsonVal)) * Double.parseDouble(localConversion)) + " " + localAbb);
                                        } else {
                                            previewLayout.setVisibility(View.GONE);
                                        }

                                    }

                                }
                            }
                        }
                    }
                } else {
                    previewLayout.setVisibility(View.GONE);
                }

            }
        });

        try {
            View scroll = CompleteModuleActivity.scrollView;
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

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }


        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
            if (isRepeaterItem) {
                innerWrapper.addView(et);
                innerWrapper.addView(spinner);
//                parentLayout.addView(previewLayout);
                parentLayout.addView(innerWrapper);

            } else {
                innerWrapper.addView(et);
                innerWrapper.addView(spinner);
                questionWrapper.addView(innerWrapper);
//                questionWrapper.addView(previewLayout);
                parentLayout.addView(questionWrapper);
            }
        } else {
            if (isRepeaterItem) {
                parentLayout.addView(et);
            } else {
                questionWrapper.addView(et);
                parentLayout.addView(questionWrapper);
            }
        }
    }

    private void setAlphanumericFilter(final EditText editText, final int maxCharacterLimit) {
        InputFilter alphaNumericFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                                Spanned dest, int dstart, int dend) {
                StringBuilder filteredStringBuilder = new StringBuilder();
                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (Character.isLetterOrDigit(character) || Character.isWhitespace(character) || isSymbol(character)) {
                        if (filteredStringBuilder.length() < maxCharacterLimit) {
                            if (character == ' ') {
                                if (dest.length() == 0 || dest.charAt(dest.length() - 1) != ' ' || filteredStringBuilder.length() > 0 && filteredStringBuilder.charAt(filteredStringBuilder.length() - 1) != ' ') {
                                    filteredStringBuilder.append(character);
                                }
                            } else {
                                filteredStringBuilder.append(character);
                            }
                        }
                    }
                }
                int remainingCharacters = maxCharacterLimit - (dest.length() - (dend - dstart));
                if (remainingCharacters < filteredStringBuilder.length()) {
                    filteredStringBuilder.setLength(remainingCharacters);
                }
                if(dest.length() + filteredStringBuilder.length() - (dend - dstart) > maxCharacterLimit){
                    editText.setError("Character limit reached");
                    new Handler().postDelayed(() -> editText.setError(null), 3000);
                } else {
                    editText.setError(null);
                }
                return filteredStringBuilder.toString();
            }
        };
        editText.setFilters(new InputFilter[]{alphaNumericFilter});
    }

    private boolean isSymbol(char character) {
        String symbolRegex = "[\\p{Punct}]";
        return Character.toString(character).matches(symbolRegex);
    }
    private void setIntegerPositiveRangeFilter(final EditText editText, final int startLimit, final int endLimit, int validationOnRange, JSONObject rangeArray) {
        InputFilter integerRangeFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    roomDB = OFISRoomDB.getDbInstance(context);
                    // Combine the current text with the new input
                    String newValue = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

                    int inputNumber = Integer.parseInt(newValue);

                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String input = editText.getText().toString();
                            if (!input.isEmpty()) {
                                double number = Double.parseDouble(input);
                                if (number >= startLimit) {
                                    // Valid input starting from the minimum value
                                    if (number <= endLimit) {
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            int vr_min = rangeArray.optInt("vr_min");
                                            int vr_max = rangeArray.optInt("vr_max");
                                            if (vr_min > number || number > vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));
                                            }
                                        } else {
                                            editText.setError(null);
                                        }
                                    } else {
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                            editText.setError(context.getString(R.string.alert_review));
                                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            int vr_min = rangeArray.optInt("vr_min");
                                            int vr_max = rangeArray.optInt("vr_max");
                                            if (vr_min > number || number > vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));

                                            }
                                        }
                                    }
                                } else {
                                    if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                        editText.setError(context.getString(R.string.alert_review));
                                    } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                        int vr_min = rangeArray.optInt("vr_min");
                                        int vr_max = rangeArray.optInt("vr_max");
                                        if(number < startLimit){
                                            editText.setError(String.format(context.getString(R.string.alert_review_hint), startLimit, endLimit));
                                        }else {
                                            if (vr_min > number || number > vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));
                                            }
                                        }
                                    } else {
                                        // Input is less than the minimum value
                                        editText.setError(String.format(context.getString(R.string.alert_review_hint), startLimit, endLimit));
                                    }
                                }
                            }
                        }
                    });

                    if (inputNumber > endLimit) {
                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                            editText.setError(context.getString(R.string.alert_review));
                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                            editText.setError(String.format(context.getString(R.string.alert_review_hint), startLimit, endLimit));

                            if (inputNumber >= startLimit && inputNumber <= endLimit){
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        int vr_min = rangeArray.optInt("vr_min");
                                        int vr_max = rangeArray.optInt("vr_max");
                                        if (vr_min > inputNumber || inputNumber > vr_max) {
                                            editText.setError(context.getString(R.string.alert_review));
                                        }
                                    }
                                }, 1000);
                            }

                        } else {
                            editText.setError(String.format(context.getString(R.string.alert_review_hint), startLimit, endLimit));
                        }
                        return (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) ? source : "";

                    } else {
                        if (inputNumber < startLimit) {
                            return source;
                        } else {
                            return source;
                        }

                    }

                } catch (NumberFormatException e) {
                    // Ignore non-numeric input
                    return "";
                }
            }
        };

        editText.setFilters(new InputFilter[]{integerRangeFilter});
    }


    private void setIntegerNegativeRangeFilter(final EditText editText, final int minLimit, final int maxLimit, int validationOnRange, JSONObject rangeArray) {
        InputFilter negativeIntegerRangeFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    roomDB = OFISRoomDB.getDbInstance(context);
                    // Combine the current text with the new input
                    String newValue = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

                    if (newValue.equals("-")) {
                        return source; // Allow negative sign
                    }

                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String input = editText.getText().toString();
                            if (!input.isEmpty()) {
                                double number = Double.parseDouble(input);
                                if (number >= minLimit) {
                                    // Valid input starting from the minimum value
                                    if (number <= maxLimit) {
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            int vr_min = rangeArray.optInt("vr_min");
                                            int vr_max = rangeArray.optInt("vr_max");
                                            if (vr_min <= number && number <= vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));
                                            }
                                        } else {
                                            editText.setError(null);
                                        }
                                    } else {
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                            editText.setError(context.getString(R.string.alert_review));
                                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            int vr_min = rangeArray.optInt("vr_min");
                                            int vr_max = rangeArray.optInt("vr_max");
                                            if (vr_min > number || number > vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));
                                            }
                                        }
                                    }
                                }else {
                                    if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                        editText.setError(context.getString(R.string.alert_review));
                                    } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                        int vr_min = rangeArray.optInt("vr_min");
                                        int vr_max = rangeArray.optInt("vr_max");
                                        if(number < minLimit){
                                            editText.setError(String.format(context.getString(R.string.alert_review_hint), minLimit, maxLimit));
                                        }else {
                                            if (vr_min > number || number > vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));
                                            }
                                        }
                                    } else {
                                        // Input is less than the minimum value
                                        editText.setError(String.format(context.getString(R.string.alert_review_hint), minLimit, maxLimit));
                                    }
                                }
                            }
                        }
                    });
                    int inputNumber = Integer.parseInt(newValue);

                    if (inputNumber >= minLimit && inputNumber <= maxLimit) {
                        editText.setError(null); // Clear any previous errors
                        return source;
                    } else {
                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                            editText.setError(context.getString(R.string.alert_review));
                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()){
                            editText.setError(String.format(context.getString(R.string.alert_review_hint), minLimit, maxLimit));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int vr_min = rangeArray.optInt("vr_min");
                                    int vr_max = rangeArray.optInt("vr_max");
                                    if (vr_min > inputNumber || inputNumber > vr_max) {
                                        editText.setError(context.getString(R.string.alert_review));
                                    }
                                }
                            }, 1000);
                        }else {
                            editText.setError(String.format(context.getString(R.string.alert_review_hint), minLimit, maxLimit));

                        }
                        return (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) ? source : "";
                    }
                } catch (NumberFormatException e) {
                    // Ignore non-numeric input
                    return "";
                }
            }
        };

        editText.setFilters(new InputFilter[]{negativeIntegerRangeFilter});
    }


    private void setIntegerWithCharacterLimit(final EditText editText, final int maxCharacterLimit) {

        InputFilter characterLimitFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                String newValue = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

                if (newValue.length() > maxCharacterLimit) {
                    editText.setError("Character limit exceeded");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            editText.setError(null); // Clear the error message after delay
                        }
                    }, 3000); // Delay for 3 seconds
                    return "";
                }

                editText.setError(null); // Clear any previous errors
                return null;
            }
        };

        editText.setFilters(new InputFilter[]{characterLimitFilter});
    }

    private void setDecimalWithCharacterLimit(final EditText editText, final int maxCharacterLimit, final int maxDecimalLimit) {

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                String newValue = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

                int dotIndex = newValue.indexOf(".");

                // Enforce character limit before and after decimal separately
                if (dotIndex == -1) { // No decimal yet
                    if (newValue.length() > maxCharacterLimit) {
                        editText.setError("Character limit exceeded before decimal");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                editText.setError(null);
                            }
                        }, 3000);
                        return "";
                    }
                } else { // Decimal exists
                    String beforeDecimal = newValue.substring(0, dotIndex);
                    if (beforeDecimal.length() > maxCharacterLimit) {
                        editText.setError("Character limit exceeded before decimal");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                editText.setError(null);
                            }
                        }, 3000);
                        return "";
                    }

                    String afterDecimal = newValue.substring(dotIndex + 1);
                    if (afterDecimal.length() > maxDecimalLimit) {
                        editText.setError("Decimal limit exceeded");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                editText.setError(null);
                            }
                        }, 3000);
                        return "";
                    }
                }

                // Allow only one decimal point
                if (newValue.lastIndexOf(".") != dotIndex && source.toString().equals(".")) {
                    return "";
                }




                // Prevent multiple leading zeros if no decimal is present
                if (!newValue.contains(".")  && newValue.length()==1 && newValue.startsWith("0") && source.toString().equals("0")) {
                    return newValue;
                }

                editText.setError(null);
                return null;
            }
        };

        editText.setFilters(new InputFilter[]{filter});
    }


    /*
    private void setIntegerWithCharacterLimit(final EditText editText , final int maxCharacterLimit) {

        InputFilter numericFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }

                return null;
            }
        };

        InputFilter characterLimitFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                String newValue = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

                if (newValue.length() > maxCharacterLimit) {
                    editText.setError("Character limit exceeded");
                    return "";
                }

                editText.setError(null); // Clear any previous errors
                return null;
            }
        };

        editText.setFilters(new InputFilter[] { numericFilter, characterLimitFilter });
    }
*/

    private void setAlphabetWithCharacterLimit(final EditText editText, final int maxCharacterLimit) {
        InputFilter alphabetFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder) source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar) && !Character.isSpaceChar(currentChar)) {
                            sourceAsSpannableBuilder.delete(i, i + 1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char character = source.charAt(i);
                        if (Character.isLetter(character) || (character == ' ' && (filteredStringBuilder.length() > 0 || dest.length() > 0))) {
                            if (filteredStringBuilder.length() < maxCharacterLimit) {
                                filteredStringBuilder.append(character);
                            }
                        }
                    }
                    int remainingCharacters = maxCharacterLimit - dest.length() + dend - dstart;
                    if (remainingCharacters <= 0) {
                        return ""; // Limit reached, discard the input
                    }

                    return filteredStringBuilder.toString();
                }
            }
        };

        editText.setFilters(new InputFilter[]{alphabetFilter});
    }


    public void setPositiveDecimalFilter(EditText editText, final double minValue, final double maxValue, int validationOnRange, int maxDecimalLimit, JSONObject rangeArray) {

        InputFilter decimalRangeFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    // Create the resulting string and parse it to double
//                    String resultingText = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
//                    double input = Double.parseDouble(resultingText);

                    String resultingText = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
                    boolean addingDecimalPoint = source.toString().equals(".") && !dest.toString().contains(".");

                    // Check for exceeding decimal limit
                    String[] parts = resultingText.split("\\.");
                    if (parts.length > 1 && parts[1].length() > maxDecimalLimit) {
                        return ""; // Reject if decimal places exceed limit
                    }

                    // Allow adding a decimal point even if below minimum value
                    if (addingDecimalPoint) {
                        return null; // Allow decimal point to be entered
                    }

                    // Parse input as double only if not just adding a decimal point
                    double input = Double.parseDouble(resultingText);

                    // Set text watcher for dynamic error handling
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String input = editText.getText().toString();
                            if (!input.isEmpty()) {
                                double number = Double.parseDouble(input);
                                if (number >= minValue) {
                                    // Valid input starting from minimum value
                                    if (number <= maxValue) {
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            double vr_min = rangeArray.optDouble("vr_min");
                                            double vr_max = rangeArray.optDouble("vr_max");
                                            if (vr_min > number || number > vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));

                                            }
                                        } else {
                                            editText.setError(null);
                                        }
                                    } else {
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                            editText.setError(context.getString(R.string.alert_review));
                                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            double vr_min = rangeArray.optDouble("vr_min");
                                            double vr_max = rangeArray.optDouble("vr_max");
                                            if (vr_min > number || number > vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));

                                            }
                                        }
                                    }
                                } else {
                                    if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                        editText.setError(context.getString(R.string.alert_review));
                                    } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                        double vr_min = rangeArray.optDouble("vr_min");
                                        double vr_max = rangeArray.optDouble("vr_max");
                                        if(number < minValue){
                                            editText.setError(String.format(context.getString(R.string.alert_review_hint_decimal), minValue, maxValue));
                                        }else {
                                            if (vr_min <= number || number <= vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));
                                            }
                                        }

                                    } else {
                                        // Input less than minimum value
                                        editText.setError(String.format(context.getString(R.string.alert_review_hint_decimal), minValue, maxValue));
                                    }
                                }
                            }
                        }
                    });

                    // Check if input is within valid range
                    if (input >= minValue && input <= maxValue) {
                        return null; // Allow valid input
                    }

                    // Handle input below minimum value
                    if (resultingText.length() > 0) {
                        double inputValue = Double.parseDouble(resultingText);
                        if (inputValue < minValue) {
                            return null; // Allow input but prevent further input until minimum value is reached
                        }
                    }

                    // Handle input exceeding maximum value
                    if (input > maxValue) {
                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                            editText.setError(context.getString(R.string.alert_review));
                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                            editText.setError(String.format(context.getString(R.string.alert_review_hint_decimal), minValue, maxValue));

                            if (input >= minValue && input <= maxValue){
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        double vr_min = rangeArray.optDouble("vr_min");
                                        double vr_max = rangeArray.optDouble("vr_max");
                                        if (vr_min > input || input > vr_max) {
                                            editText.setError(context.getString(R.string.alert_review));

                                        }
                                    }
                                }, 3000);
                            }


                        } else {
                            editText.setError(String.format(context.getString(R.string.alert_review_hint_decimal), minValue, maxValue));
                        }

                        return (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) ? source : "";
                    } else {
                        if (input < minValue) {
                            return source;
                        } else {
                            return source;
                        }
                    }

                } catch (NumberFormatException ignored) {
                    // Ignore invalid input and let it through
                }

                return ""; // Reject invalid input
            }
        };

        editText.setFilters(new InputFilter[]{decimalRangeFilter});
    }


    private void setNegativeDecimalFilter(EditText editText, final double minValue, final double maxValue, int validationOnRange, int maxDecimalLimit, JSONObject rangeArray) {

        InputFilter negativeDecimalRangeFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    String resultingText = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

                    if (resultingText.equals("-") && minValue < 0) {
                        editText.setError("Please enter a valid number.");
                        return null;
                    }

                    // Parse the resulting string
                    String[] parts = resultingText.split("\\.");
                    if (parts.length > maxDecimalLimit) {
                        // Restrict multiple decimal points
                        return "";
                    }

                    // Check for excessive decimal digits
                    if (parts.length == 2 && parts[1].length() > maxDecimalLimit) {
                        return "";
                    }

                    double input = Double.parseDouble(resultingText);

                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String input = editText.getText().toString();

                            if (!input.isEmpty() && input.matches("-?\\d+(\\.\\d+)?")) {
                                try {
                                    double number = Double.parseDouble(input);
                                    if (number >= minValue) {
                                        // Valid input starting from the minimum value
                                        if (number <= maxValue) {
                                            if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                                double vr_min = rangeArray.optDouble("vr_min");
                                                double vr_max = rangeArray.optDouble("vr_max");
                                                if (vr_min > number || number > vr_max) {
                                                    editText.setError(context.getString(R.string.alert_review));

                                                }
                                            } else {
                                                editText.setError(null);
                                            }
                                        } else {
                                            if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                                editText.setError(context.getString(R.string.alert_review));
                                            } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                                double vr_min = rangeArray.optDouble("vr_min");
                                                double vr_max = rangeArray.optDouble("vr_max");
                                                if (vr_min > number || number > vr_max) {
                                                    editText.setError(context.getString(R.string.alert_review));

                                                }
                                            }else {
                                                editText.setError(String.format(context.getString(R.string.alert_review_hint_decimal), minValue, maxValue));
                                            }
                                        }
                                    } else {
                                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                                            editText.setError(context.getString(R.string.alert_review));
                                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                                            double vr_min = rangeArray.optDouble("vr_min");
                                            double vr_max = rangeArray.optDouble("vr_max");
                                            if (vr_min > number || number < vr_max) {
                                                editText.setError(context.getString(R.string.alert_review));

                                            }
                                        } else {
                                            // Input is less than the minimum value
                                            editText.setError(String.format(context.getString(R.string.alert_review_hint_decimal), minValue, maxValue));
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (!input.isEmpty()) {
                                    editText.setError("Please enter a valid number.");
                                }
                            }
                        }
                    });

                    if (input >= minValue && input <= maxValue) {
                        return null; // Input is valid
                    }

                    if (input < minValue) {
                        return ""; // Reject input below minValue
                    }


                    if (input > maxValue) {
                        if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) {
                            editText.setError(context.getString(R.string.alert_review));
                        } else if (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeIdRestrictandReview()) {
                            editText.setError(String.format(context.getString(R.string.alert_review_hint_decimal), minValue, maxValue));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    double vr_min = rangeArray.optDouble("vr_min");
                                    double vr_max = rangeArray.optDouble("vr_max");
                                    if (vr_min >input || input > vr_max) {
                                        editText.setError(context.getString(R.string.alert_review));

                                    }
                                }
                            }, 3000);
                        } else {
                            editText.setError(String.format(context.getString(R.string.alert_review_hint_decimal), minValue, maxValue));
                        }
                        if (maxValue<0){
                            if (input > maxValue) {
                                return source;
                            }
                        }
                        return (validationOnRange == roomDB.validationOnRangeDao().getValidationOnRangeId()) ? source : "";

                    } else {
                        if (input < minValue) {
                            return source;
                        } else {
                            return source;
                        }

                    }

                } catch (NumberFormatException ignored) {
                }

                return ""; // Reject invalid input
            }
        };

        editText.setFilters(new InputFilter[]{negativeDecimalRangeFilter});
    }


    private float fetchMinMaxValue(JSONObject displayConfig, String rangeMinMax) {
        // get disp"lay config to see if question has display range
        try {
            if (displayConfig.has("surveyrange")) {
                return BigDecimal.valueOf(displayConfig.getJSONObject("surveyrange").getDouble(rangeMinMax)).floatValue();

            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
        return 0;
    }

    void renderTextBoxQuestionSubQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        // renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

        String datatypeduplicate = "";
        int questionIddouplicate = 0;
        if (qCursor != null) {
            if (qCursor.moveToFirst()) {
                do {
                    datatypeduplicate = qCursor.getString(qCursor.getColumnIndex("datatype_name"));

                    questionIddouplicate = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                } while (qCursor.moveToNext());
            }
        }

        final int questionId = questionIddouplicate;
        final String datatype = datatypeduplicate;
        int answerIdTemp = 0;

       /* if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
            } else {
                if (qCursor.getColumnIndex("answer_id") != -1) {
                    answerIdTemp = qCursor.getInt(qCursor.getColumnIndex("answer_id"));
                }
            }
        }*/
        final int answerId = answerIdTemp;

        final EditText et = (EditText) inflater.inflate(R.layout.module_edit_text, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        et.setFilters(new InputFilter[]{new EmojiExcludeFilter()});
        et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
       /* if (hasAnswers) {
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
        }*/

        if (datatype.equals("decimal")) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if (datatype.equals("int")) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        // add tv to array if this is a 100% group question
        // get display config
        boolean isHPQTemp = false;

        try {


            JSONObject displayConfig = new JSONObject();
            String columnNameduplicate = "";
            if (qCursor != null) {
                if (qCursor.moveToFirst()) {
                    do {
                        displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                        columnNameduplicate = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;


                    } while (qCursor.moveToNext());
                }
            }


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
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting textbox display config " + e.getMessage());
        }
        final boolean isHundredPercentQuestion = isHPQTemp;


        Cursor cursor = dl.getParentSubquestionAnswer(Utilities.subModuleId, parentQuestionId, questionId);


        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    et.setText(cursor.getString(cursor.getColumnIndex("question_answer")));


                } while (qCursor.moveToNext());
            }
        }


        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if ((isHundredPercentQuestion) && (!updatingHundredPercent)) {
                    autofill100Percent(et);
                }
                dl.prentidAnswerDelete(parentQuestionId, questionId);

                dl.prentidAnswer(Utilities.subModuleId, parentQuestionId, questionId, et.getText().toString(), 0 + "", Utilities.olamFarmerId, Utilities.moduleId + "");
            }
        });

        if (isRepeaterItem) {
            parentLayout.addView(et);
        } else {
            questionWrapper.addView(et);
            parentLayout.addView(questionWrapper);
        }
    }

    public void renderDropDownQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
        final LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

        resources = res;
//        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));
        final String displayType = qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));
        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        }

        if (oldDatatype != null) {
            if (oldDatatype.equals("Dropdown") || oldDatatype.replaceAll(" ", "").toLowerCase().equals("farmtreedensitydropdown")) {
                datatype = "int";
            }
        }


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

        //Local unit
        String metricGlobalId = dl.getGlobalIdByQuestionId(questionId);
//        String metricGlobalId = "37";
        final Integer[] selectedLocalID = {0};
        final Integer[] selectedGlobalID = {0};
        boolean isLocalUnit = false;
        // Create an inner wrapper
        LinearLayout innerWrapper = new LinearLayout(context);
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        innerParams.setMargins(0, 7, 0, 0);
        innerWrapper.setLayoutParams(innerParams);

        innerWrapper.setOrientation(LinearLayout.HORIZONTAL);


        /*TextView text = new TextView(getInstance().context);
        text.setText("Metrics:");
        text.setTextColor(context.getResources().getColor(R.color.weightage_bar));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, // Width set to 0
                LinearLayout.LayoutParams.MATCH_PARENT,2
        );
        text.setGravity(Gravity.CENTER);
        text.setLayoutParams(textParams);
        innerWrapper.addView(text);*/

        LinearLayout previewLayout = new LinearLayout(context);
        previewLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        previewLayout.setOrientation(LinearLayout.VERTICAL);
        previewLayout.setPadding(16, 16, 16, 16);

        TextView titleTextView = new TextView(context);
        titleTextView.setText(context.getResources().getString(R.string.converted_metrics));
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        titleTextView.setTextColor(context.getResources().getColor(R.color.weightage_bar));

        // Create the second TextView
        TextView contentTextView = new TextView(context);
        contentTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Add TextViews to the LinearLayout
        previewLayout.addView(titleTextView);
        previewLayout.addView(contentTextView);
        previewLayout.setVisibility(View.GONE);

        final Spinner spinnerLocal = (Spinner) inflater.inflate(R.layout.metric_spinner, innerWrapper, false);
        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
            List<LocalUnitDao.UnitMapping> unitMappings = roomDB.localUnitDao().getValueByGlobalId(Integer.parseInt(metricGlobalId));

            if (unitMappings.isEmpty()) {

                List<String> getGlobalUnit = roomDB.globalUnitDao().getGlobalAbbreviation(Integer.parseInt(metricGlobalId));
                selectedGlobalID[0] = Integer.parseInt(metricGlobalId);
                LinearLayout.LayoutParams spinnerLocalParams = new LinearLayout.LayoutParams(
                        0, // Width set to 0
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2 // Weight set to 2 (20% of the total weight)
                );
                spinnerLocalParams.setMargins(7, 0, 0, 0);
                spinnerLocal.setLayoutParams(spinnerLocalParams);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, getGlobalUnit);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerLocal.setAdapter(spinnerAdapter);


                /*if (hasAnswers){
                    int localAnswer = Integer.valueOf(dl.getGlobalIdByAnswerID(answerId));
                    spinnerLocal.setSelection(localAnswer);
                }else {
                    spinnerLocal.setSelection(Integer.parseInt(metricGlobalId));
                }*/

                spinnerLocal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item from the adapter
                        String selectedAbbreviation = spinnerAdapter.getItem(position);

//                        Toast.makeText(context, "Selected localUnitId: " + selectedAbbreviation, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing here if needed
                    }
                });


            } else {
                isLocalUnit = true;
                Map<Integer, String> spinnerData = new HashMap<>();
                List<GlobalUnitDao.UnitMapping> globalMappings = roomDB.globalUnitDao().getGlobalId(Integer.parseInt(metricGlobalId));
                for (GlobalUnitDao.UnitMapping unitMapping : globalMappings) {
                    spinnerData.put(unitMapping.globalUnitId, unitMapping.globalAbbreviation);
                }

                for (LocalUnitDao.UnitMapping unitMapping : unitMappings) {
                    spinnerData.put(unitMapping.localUnitId, unitMapping.localUnitAbbreviation);
                }

                List<String> spinnerItemList = new ArrayList<>(spinnerData.values());
                LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                        0, // Width set to 0
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2 // Weight set to 2 (20% of the total weight)
                );
                spinnerParams.setMargins(7, 0, 0, 0);
                spinnerLocal.setLayoutParams(spinnerParams);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, spinnerItemList);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerLocal.setAdapter(spinnerAdapter);
                if (answerId != 0) {
                    int localAnswer = Integer.valueOf(dl.getLocalIdByAnswerID(answerId));
                    if (localAnswer == 0) {
                        spinnerLocal.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), spinnerData.keySet().toArray(new Integer[0])));
                    } else {
                        spinnerLocal.setSelection(getIndexForValue(localAnswer, spinnerData.keySet().toArray(new Integer[0])));
                    }

                } else {
//                    spinner.setSelection(Integer.parseInt(metricGlobalId));
                    spinnerLocal.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), spinnerData.keySet().toArray(new Integer[0])));
                }
                spinnerLocal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item from the adapter
                        String selectedAbbreviation = spinnerAdapter.getItem(position);

                        // Find the corresponding localUnitId in the spinnerData map
                        for (Map.Entry<Integer, String> entry : spinnerData.entrySet()) {
                            if (entry.getValue().equals(selectedAbbreviation)) {
                                int selectedLocalUnitId = entry.getKey();

                                if (entry.getKey() == Integer.parseInt(metricGlobalId)) {
                                    selectedGlobalID[0] = entry.getKey();
                                    selectedLocalID[0] = 0;
                                } else {
                                    selectedLocalID[0] = entry.getKey();
                                    selectedGlobalID[0] = 0;
                                }
                                // Display a Toast with the selected localUnitId
//                                Toast.makeText(context, "Selected localUnitId: " + selectedLocalUnitId, Toast.LENGTH_SHORT).show();

                                if (answerSet != null) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");

                                            if (currentQuestionId == questionId) {
                                                try {
                                                    currentAnswer.put("globalMetricId", selectedGlobalID[0]);
                                                    currentAnswer.put("localMetricId", selectedLocalID[0]);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                        }
                                    }
                                }
                                if (answerSet != null) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");
                                            int answerJsonVal = currentAnswer.optInt("answer");

                                            if (currentQuestionId == questionId) {
                                                Integer globalMetricIdAnswer = null;
                                                Integer localMetricIdAnswer = null;
                                                if (currentAnswer.has("globalMetricId")) {
                                                    globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                }
                                                if (currentAnswer.has("localMetricId")) {
                                                    localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                }
                                                if (localMetricIdAnswer != 0) {
                                                    previewLayout.setVisibility(View.VISIBLE);
//                                                    titleTextView.setVisibility(View.VISIBLE);
//                                                    contentTextView.setVisibility(View.VISIBLE);
                                                    String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                                    String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                    String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                    String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                                    contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                                    if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                        try {
                                                            // Perform your calculations and set the text
                                                            double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                            contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                        } catch (NumberFormatException e) {
                                                            // Handle the case where parsing fails
                                                            e.printStackTrace();
                                                            previewLayout.setVisibility(View.GONE);
                                                        }
                                                    } else {
                                                        // Handle the case where any of the variables is null
                                                        contentTextView.setText("Some values are null");
                                                    }
                                                } else {
                                                    previewLayout.setVisibility(View.GONE);
                                                }


                                            }

                                        }
                                    }
                                }

                                Log.d("AnswerNewSet", answerSet.toString());
                                break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing here if needed
                    }
                });
            }
        }

        // code end

//        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, questionWrapper, false);
        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
            LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                    0, // Width set to 0
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    8 // Weight set to 8 (80% of the total weight)
            );
            spinner.setLayoutParams(spinnerParams);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, res.getStringArray(R.array.plotvariants));
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        Cursor spinnervalues;

        // get display config
        try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            final String columnName = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;

            if (displayConfig.has("lookuptable")) {
                if (OFISDBContract.countryLookUpValuesEntry.TABLE_NAME.trim().toLowerCase().equals(displayConfig.getString("lookuptable").trim().toLowerCase())) {
                    spinnervalues = dl.getSpinnerData(OFISDBContract.countryLookUpValuesEntry.TABLE_NAME, false, null, null, true, "CountryName");
                } else if (OFISDBContract.productSipEntry.TABLE_NAME.trim().toLowerCase().equals(displayConfig.getString("lookuptable").trim().toLowerCase())) {
                    spinnervalues = dl.getSpinnerData(OFISDBContract.productSipEntry.TABLE_NAME, false, null, null, true, "productName");

                } else {
                    spinnervalues = dl.getSpinnerData(displayConfig.getString("lookuptable"), false, null, null, true, "name");
                }
            } else {
                spinnervalues = dl.getLookupValuesForQuestion(qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), columnName, dataTypeID);
            }

//            MatrixCursor newSpinnerValue = new MatrixCursor(new String[]{"value"});

            String[] columnNames = spinnervalues.getColumnNames();
            MatrixCursor newCursor = new MatrixCursor(columnNames);

            // adding validation
            String assignmentType = "";
            String assignmentField = "";
            String assignmentFields = "";
            String isDefault = "";
            String isActive = "";
            JSONArray countryAndProductAssign = null;
            try {
                JSONObject displayConfig1 = new JSONObject();
                displayConfig1 = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                if (displayConfig1.has("display")) {
                    JSONObject display = displayConfig1.getJSONObject("display");
                    if (display.has("lookupProperties")) {
                        JSONArray range = display.getJSONArray("lookupProperties");
                        for (int i = 0; i < range.length(); i++) {
                            JSONObject rangeArray = range.getJSONObject(i);
                            String lookupID = rangeArray.getString("lookupValueId");
                            assignmentType = rangeArray.getString("assignmentType");
                            assignmentField = rangeArray.getString("assignmentField");
                            assignmentFields = rangeArray.getString("assignmentFields");
                            isDefault = rangeArray.getString("isDefault");
                            if (rangeArray.has("active")) {
                                isActive = rangeArray.getString("active");
                            }
                            if (rangeArray.has("countryAndProductAssign")){
                                countryAndProductAssign = rangeArray.getJSONArray("countryAndProductAssign");
                            }
                            long farmerId = Utilities.farmerId;

                            if (!assignmentField.equals("null")) {

                                String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                if (assignmentField.equals(farmerAssignmentField) || assignmentType.equals("null") && assignmentField.equals("null")) {

                                    if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                        String checkBoxVal = dl.getValueByLookUpID(lookupID, qCursor, questionColumnSuffix);

                                        if (spinnervalues.moveToFirst()) {
                                            do {
                                                String value = spinnervalues.getString(spinnervalues.getColumnIndex(columnName));
                                                int id = spinnervalues.getInt(spinnervalues.getColumnIndex("_id"));

                                                if (value.equals(checkBoxVal)) {
                                                    boolean idExists = false;
                                                    newCursor.moveToPosition(-1);

                                                    while (newCursor.moveToNext()) {
                                                        int existingId = newCursor.getInt(newCursor.getColumnIndex("_id"));
                                                        if (existingId == id) {
                                                            idExists = true;
                                                            break;
                                                        }
                                                    }

                                                    if (!idExists) {
                                                        Object[] rowData = new Object[columnNames.length];
                                                        for (int j = 0; j < columnNames.length; j++) {
                                                            rowData[j] = spinnervalues.getString(spinnervalues.getColumnIndex(columnNames[j]));
                                                        }
                                                        newCursor.addRow(rowData);
                                                    }

                                                }

                                            } while (spinnervalues.moveToNext());
                                        }
                                    }
                                }
                            } else if (assignmentFields.equals("null") && countryAndProductAssign != null && countryAndProductAssign.length() != 0){
                                String checkBoxVal = dl.getValueByLookUpID(lookupID, qCursor, questionColumnSuffix);
                                boolean isFound = countryAndProductAssignLookup(countryAndProductAssign, farmerId);

                                if (isFound) {
                                   if (spinnervalues.moveToFirst()) {
                                       do {
                                           String value = spinnervalues.getString(spinnervalues.getColumnIndex(columnName));
                                           int id = spinnervalues.getInt(spinnervalues.getColumnIndex("_id"));

                                           boolean idExists = false;
                                           newCursor.moveToPosition(-1);

                                           while (newCursor.moveToNext()) {
                                               int existingId = newCursor.getInt(newCursor.getColumnIndex("_id"));
                                               if (existingId == id) {
                                                   idExists = true;
                                                   break;
                                               }
                                           }

                                           if (!idExists && Integer.parseInt(lookupID) == id) {
                                               Object[] rowData = new Object[columnNames.length];
                                               for (int j = 0; j < columnNames.length; j++) {
                                                   rowData[j] = spinnervalues.getString(spinnervalues.getColumnIndex(columnNames[j]));
                                               }
                                               newCursor.addRow(rowData);
                                           }

                                       } while (spinnervalues.moveToNext());
                                   }
                               }

                            } else {

                                // Remove square brackets and split the string into an array of strings
                                String[] stringArray = assignmentFields.substring(1, assignmentFields.length() - 1).split(",");


                                if (assignmentFields.equals("null") || assignmentFields.equals("[]")) {
                                    if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                        String checkBoxVal = dl.getValueByLookUpID(lookupID, qCursor, questionColumnSuffix);

                                        if (spinnervalues.moveToFirst()) {
                                            do {
                                                String value = spinnervalues.getString(spinnervalues.getColumnIndex(columnName));
                                                int id = spinnervalues.getInt(spinnervalues.getColumnIndex("_id"));

                                                if (value.equals(checkBoxVal)) {
                                                    boolean idExists = false;
                                                    newCursor.moveToPosition(-1);

                                                    while (newCursor.moveToNext()) {
                                                        int existingId = newCursor.getInt(newCursor.getColumnIndex("_id"));
                                                        if (existingId == id) {
                                                            idExists = true;
                                                            break;
                                                        }
                                                    }

                                                    if (!idExists) {
                                                        Object[] rowData = new Object[columnNames.length];
                                                        for (int j = 0; j < columnNames.length; j++) {
                                                            rowData[j] = spinnervalues.getString(spinnervalues.getColumnIndex(columnNames[j]));
                                                        }
                                                        newCursor.addRow(rowData);
                                                    }

                                                }

                                            } while (spinnervalues.moveToNext());
                                        }

                                    }
                                } else {
                                    // Convert the string array to an integer array
                                    int[] intArray = new int[stringArray.length];
                                    for (int m = 0; m < stringArray.length; m++) {
                                        intArray[m] = Integer.parseInt(stringArray[m]);
                                    }

                                    // Iterate through the integer array using a for loop
                                    for (int fieldMultipleAssignment : intArray) {

                                        String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                        if (String.valueOf(fieldMultipleAssignment).equals(farmerAssignmentField) || assignmentType.equals("null") && String.valueOf(fieldMultipleAssignment).equals("null")) {

                                            if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                                String checkBoxVal = dl.getValueByLookUpID(lookupID, qCursor, questionColumnSuffix);

                                                if (spinnervalues.moveToFirst()) {
                                                    do {
                                                        String value = spinnervalues.getString(spinnervalues.getColumnIndex(columnName));
                                                        int id = spinnervalues.getInt(spinnervalues.getColumnIndex("_id"));

                                                        if (value.equals(checkBoxVal)) {
                                                            boolean idExists = false;
                                                            newCursor.moveToPosition(-1);

                                                            while (newCursor.moveToNext()) {
                                                                int existingId = newCursor.getInt(newCursor.getColumnIndex("_id"));
                                                                if (existingId == id) {
                                                                    idExists = true;
                                                                    break;
                                                                }
                                                            }

                                                            if (!idExists) {
                                                                Object[] rowData = new Object[columnNames.length];
                                                                for (int j = 0; j < columnNames.length; j++) {
                                                                    rowData[j] = spinnervalues.getString(spinnervalues.getColumnIndex(columnNames[j]));
                                                                }
                                                                newCursor.addRow(rowData);
                                                            }

                                                        }

                                                    } while (spinnervalues.moveToNext());
                                                }

                                            }
                                        }
                                    }
                                }

                            }


                        }

                        dl.bindDataDrivenSpinner(spinner, newCursor, context, R.string.ModuleAnswerHint, columnName, false);
                    } else {
                        dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, columnName, false);
                    }

                } else {
                    dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, columnName, false);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            final boolean isDensityQuestion = (dataTypeID == 6 && displayType.replaceAll(" ", "").toLowerCase().equals("farmtreedensitydropdown")) ? true : (displayConfig.has("densityquestionparent") && (displayConfig.getBoolean("densityquestionparent")));

            if (hasAnswers) {
                if (datatype.equals("int")) {
                    Integer currentAnswerInt = -1;
                    if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                        currentAnswerInt = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT));
                    } else {
                        if (qCursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT) != -1) {
                            currentAnswerInt = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT));
                        }
                    }

                    for (int i = 0; i < spinner.getCount(); i++) {
                        if (spinner.getItemIdAtPosition(i) == currentAnswerInt) {
                            spinner.setSelection(i);
                            break;
                        }
                    }

                } else {
                    String currentAnswerValue = null;
                    if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                        currentAnswerValue = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
                    } else {
                        if (qCursor.getColumnIndex("answer_" + datatype) != -1) {
                            currentAnswerValue = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                        }
                    }

                    for (int i = 0; i < spinner.getCount(); i++) {
                        Cursor currentItem = (Cursor) spinner.getItemAtPosition(i);
                        if (currentItem.getString(currentItem.getColumnIndex(columnName)).equalsIgnoreCase(currentAnswerValue)) {
                            spinner.setSelection(i);
                            break;
                        }
                    }
                }
            }


            //   Prepopulate Yes/No If not answered
            boolean isChildDefault = false;
            try {
                JSONObject jsonObject = new JSONObject(displayConfig.toString()).getJSONObject("calculation");
                JSONArray countryIDs = (JSONArray) jsonObject.get("countryId");
                JSONArray productIDs = (JSONArray) jsonObject.get("productId");
                if (countryIDs.length() > 0 && productIDs.length() > 0) {
                    ArrayList<Integer> countryLdList = new ArrayList<>();
                    for (int i = 0; i < countryIDs.length(); i++) {
                        countryLdList.add(Integer.parseInt(countryIDs.getString(i)));
                    }
                    ArrayList<Integer> productIdList = new ArrayList<>();
                    for (int i = 0; i < productIDs.length(); i++) {
                        productIdList.add(Integer.parseInt(productIDs.getString(i)));
                    }
                    if (countryLdList.contains(farmerGeo.getCountryId()) && productIdList.contains(farmerGeo.getProductId())) {
                        isChildDefault = true;
                    }
                }

            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().log(e.getMessage());

            }


            // N/A for LookUp Value franklin
            int defaultLookUpValue = 0;
            if (displayConfig.has("calculation")) {
                String calculationNode = displayConfig.optString(CoffeeConstants.CALCULATION);
                JSONObject jsonObject = new JSONObject(calculationNode);
                defaultLookUpValue = jsonObject.optInt("defaultLookUpValue", 0);
                if (defaultLookUpValue != 0) {
                    // find the question is answered or not
                    Cursor cbAnswers = null;
                    if (isChildDefault) {
                        cbAnswers = dl.getAnswersForQuestionByChild(questionId, Utilities.farmerId, Utilities.stakeholderId, Utilities.farmerModuleId);
                    } else {
                        cbAnswers = dl.getAnswersForQuestionByFarmer(questionId, Utilities.farmerId, Utilities.stakeholderId, Utilities.farmerModuleId);
                    }
                    if (cbAnswers != null) {
                        if (!(cbAnswers.getCount() > 0)) {
                            for (int i = 0; i < spinner.getCount(); i++) {
                                if (spinner.getItemIdAtPosition(i) == defaultLookUpValue) {
                                    spinner.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                    //   saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, String.valueOf(defaultLookUpValue));
                    // }
                }
            }


            int finalDefaultLookUpValue = defaultLookUpValue;
            String finalDatatype = datatype;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {


                    //renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, olamFarmerId, farmerModuleId, res);


                    //renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, olamFarmerId, farmerModuleId, res);
                    if (id > 0) {
                        if (finalDatatype.equals("int")) {
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, String.valueOf(id), selectedGlobalID[0], selectedLocalID[0], 0);

                            long h = Long.parseLong("0026");


                        } else {
                            Cursor selecteditem = (Cursor) spinner.getSelectedItem();
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, selecteditem.getString(selecteditem.getColumnIndex(columnName)), selectedGlobalID[0], selectedLocalID[0], 0);
                        }
                    } else {
                        removeAnswerEntry(questionId, answerId);
                        // Remove the old value and update the Not applicable value if Answer chosen for default value
                        if (finalDefaultLookUpValue != 0) {
                            for (int i = 0; i < spinner.getCount(); i++) {
                                if (spinner.getItemIdAtPosition(i) == finalDefaultLookUpValue) {
                                    spinner.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                    if (isDensityQuestion) {
                        renderTreeDensityPlots(position, res);
                    }


                    if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                        if (answerSet.size() != 0) {
                            for (int i = 0; i < answerSet.size(); i++) {
                                JSONObject currentAnswer = answerSet.get(i);

                                if (currentAnswer != null) {
                                    int currentQuestionId = currentAnswer.optInt("question_id");
                                    int answerJsonVal = currentAnswer.optInt("answer");

                                    if (currentQuestionId == questionId) {
                                        Integer globalMetricIdAnswer = null;
                                        Integer localMetricIdAnswer = null;
                                        if (currentAnswer.has("globalMetricId")) {
                                            globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                        }
                                        if (currentAnswer.has("localMetricId")) {
                                            localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                        }
                                        if (localMetricIdAnswer != 0) {
                                            previewLayout.setVisibility(View.VISIBLE);
                                            String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                            String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                            String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                            String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                            contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                            if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                try {
                                                    // Perform your calculations and set the text
                                                    double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                    contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                } catch (NumberFormatException e) {
                                                    // Handle the case where parsing fails
                                                    e.printStackTrace();
                                                    previewLayout.setVisibility(View.GONE);
                                                }
                                            } else {
                                                // Handle the case where any of the variables is null
                                                contentTextView.setText("Some values are null");
                                            }
                                        } else {
                                            previewLayout.setVisibility(View.GONE);
                                        }

                                    } else {
                                        previewLayout.setVisibility(View.GONE);
                                    }

                                }
                            }
                        } else {
                            previewLayout.setVisibility(View.GONE);
                        }
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    if (isDensityQuestion) {
                        renderTreeDensityPlots(0, res);
                    }
                }
            });


            if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                if (isRepeaterItem) {
                    innerWrapper.addView(spinner);
                    innerWrapper.addView(spinnerLocal);
                    parentLayout.addView(innerWrapper);
                    parentLayout.addView(previewLayout);
//                parentLayout.addView(spinner);
                } else {
                    innerWrapper.addView(spinner);
                    innerWrapper.addView(spinnerLocal);
                    questionWrapper.addView(innerWrapper);
//                questionWrapper.addView(innerWrapper);
                    questionWrapper.addView(previewLayout);
                    parentLayout.addView(questionWrapper);
                }
            } else {
                if (isRepeaterItem) {
                    parentLayout.addView(spinner);
                } else {
                    questionWrapper.addView(spinner);
                    parentLayout.addView(questionWrapper);
                }
            }


        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error gettign dropdown display config " + e.getMessage());
        }

    }

    public void renderRadioQuestion(Cursor questions, boolean isRepeaterItem, LinearLayout parentLayout, long farmerId, String stakeholderId, long farmerModuleId, boolean isSavedAns, final int repeaterItemPosition, Cursor repeaterAnswers, final int parentQuestionId) {

        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(questions, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

//        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));
        Utilities.datatypeIdCLMRS =dataTypeID;

        final String otherChoice = questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_OTHER_CHOICE));
        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = questions.getString(questions.getColumnIndex("datatype_name"));

        }

        if (oldDatatype != null) {
            if (oldDatatype.equals("Radio")) {
                datatype = "int";
            }
        }


        final boolean singleSelect;
        boolean tempSingleSelect = false;

        Cursor radio = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);
        Cursor rbAnswers;

        if (isSavedAns) {
            rbAnswers = dl.getSavedAnswersForQuestionByFarmer(questionId, farmerId, stakeholderId, farmerModuleId);
        } else {
            rbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId, stakeholderId, farmerModuleId);
        }

        int answerIdTemp = 0;
        if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
            } else {
                if (questions.getColumnIndex("answer_id") != -1) {
                    answerIdTemp = questions.getInt(questions.getColumnIndex("answer_id"));
                }
            }
        }
        final int answerId = answerIdTemp;

        // is this a question we can only select one answer to?
        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting radio button display config " + e.getMessage());
        }

        singleSelect = tempSingleSelect;

        // Parse N/A Question
        int defaultLookUpValue = 0;

        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("calculation")) {
                String calculationNode = displayConfig.optString(CoffeeConstants.CALCULATION);
                JSONObject jsonObject = new JSONObject(calculationNode);
                defaultLookUpValue = jsonObject.optInt("defaultLookUpValue", 0);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        //code start
        String metricGlobalId = dl.getGlobalIdByQuestionId(questionId);
        ArrayAdapter<String> spinnerAdapter = null;
        Map<Integer, String> spinnerData = null;
//        String metricGlobalId = "43";
        final Integer[] selectedLocalID = {0};
        final Integer[] selectedGlobalID = {0};
        boolean isLocalUnit = false;
        // Create an inner wrapper
        LinearLayout innerWrapper = new LinearLayout(context);
        innerWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        innerWrapper.setOrientation(LinearLayout.HORIZONTAL);


        TextView text = new TextView(getInstance().context);
        text.setText(context.getResources().getString(R.string.metrics));
        text.setTextColor(context.getResources().getColor(R.color.weightage_bar));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, // Width set to 0
                LinearLayout.LayoutParams.MATCH_PARENT, 2
        );
        text.setGravity(Gravity.CENTER);
        text.setLayoutParams(textParams);

        LinearLayout previewLayout = new LinearLayout(context);
        previewLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        previewLayout.setOrientation(LinearLayout.VERTICAL);
        previewLayout.setPadding(16, 16, 16, 16);

        TextView titleTextView = new TextView(context);
        titleTextView.setText(context.getResources().getString(R.string.converted_metrics));
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        titleTextView.setTextColor(context.getResources().getColor(R.color.weightage_bar));

        // Create the second TextView
        TextView contentTextView = new TextView(context);
        contentTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Add TextViews to the LinearLayout
        previewLayout.addView(titleTextView);
        previewLayout.addView(contentTextView);
        previewLayout.setVisibility(View.GONE);

        final Spinner spinner = (Spinner) inflater.inflate(R.layout.metric_spinner, innerWrapper, false);
        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
            List<LocalUnitDao.UnitMapping> unitMappings = roomDB.localUnitDao().getValueByGlobalId(Integer.parseInt(metricGlobalId));

            if (unitMappings.isEmpty()) {

                List<String> getGlobalUnit = roomDB.globalUnitDao().getGlobalAbbreviation(Integer.parseInt(metricGlobalId));
                selectedGlobalID[0] = Integer.parseInt(metricGlobalId);
                LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                        0, // Width set to 0
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        8 // Weight set to 2 (20% of the total weight)
                );
                spinnerParams.setMargins(7, 0, 0, 0);
                spinner.setLayoutParams(spinnerParams);
                spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, getGlobalUnit);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                /*if (hasAnswers){
                    int

                    spinner.setSelection(localAnswer);
                }else {
                    spinner.setSelection(getGlobalUnit.indexOf(metricGlobalId));

                }*/
                ArrayAdapter<String> finalSpinnerAdapter1 = spinnerAdapter;
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item from the adapter
                        String selectedAbbreviation = finalSpinnerAdapter1.getItem(position);

//                        Toast.makeText(context, "Selected localUnitId: " + selectedAbbreviation, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing here if needed
                    }
                });


            } else {
                isLocalUnit = true;
                spinnerData = new HashMap<>();
                List<GlobalUnitDao.UnitMapping> globalMappings = roomDB.globalUnitDao().getGlobalId(Integer.parseInt(metricGlobalId));

                for (LocalUnitDao.UnitMapping unitMapping : unitMappings) {
                    spinnerData.put(unitMapping.localUnitId, unitMapping.localUnitAbbreviation);
                }

                for (GlobalUnitDao.UnitMapping unitMapping : globalMappings) {
                    spinnerData.put(unitMapping.globalUnitId, unitMapping.globalAbbreviation);
                }

                List<String> spinnerItemList = new ArrayList<>(spinnerData.values());
                LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                        0, // Width set to 0
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        8 // Weight set to 2 (20% of the total weight)
                );
                spinnerParams.setMargins(7, 0, 0, 0);
                spinner.setLayoutParams(spinnerParams);
                spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, spinnerItemList);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                if (answerId != 0) {
                    int localAnswer = Integer.valueOf(dl.getLocalIdByAnswerID(answerId));
                    if (localAnswer == 0) {
                        spinner.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), spinnerData.keySet().toArray(new Integer[0])));
                    } else {
                        spinner.setSelection(getIndexForValue(localAnswer, spinnerData.keySet().toArray(new Integer[0])));
                    }

                } else {
//                    spinner.setSelection(Integer.parseInt(metricGlobalId));
                    spinner.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), spinnerData.keySet().toArray(new Integer[0])));
                }
                Map<Integer, String> finalSpinnerData = spinnerData;
                ArrayAdapter<String> finalSpinnerAdapter = spinnerAdapter;
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item from the adapter
                        String selectedAbbreviation = finalSpinnerAdapter.getItem(position);

                        // Find the corresponding localUnitId in the spinnerData map
                        for (Map.Entry<Integer, String> entry : finalSpinnerData.entrySet()) {
                            if (entry.getValue().equals(selectedAbbreviation)) {
                                int selectedLocalUnitId = entry.getKey();

                                if (entry.getKey() == Integer.parseInt(metricGlobalId)) {
                                    selectedGlobalID[0] = entry.getKey();
                                    selectedLocalID[0] = 0;
                                } else {
                                    selectedLocalID[0] = entry.getKey();
                                    selectedGlobalID[0] = 0;
                                }
                                // Display a Toast with the selected localUnitId
//                                Toast.makeText(context, "Selected localUnitId: " + selectedLocalUnitId, Toast.LENGTH_SHORT).show();

                                if (answerSet != null) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");

                                            if (currentQuestionId == questionId) {
                                                try {
                                                    currentAnswer.put("globalMetricId", selectedGlobalID[0]);
                                                    currentAnswer.put("localMetricId", selectedLocalID[0]);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                        }
                                    }
                                }

                                if (answerSet.size() != 0) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");
                                            int answerJsonVal = currentAnswer.optInt("answer");

                                            if (currentQuestionId == questionId) {
                                                Integer globalMetricIdAnswer = 0;
                                                Integer localMetricIdAnswer = 0;
                                                if (currentAnswer.has("globalMetricId")) {
                                                    globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                }
                                                if (currentAnswer.has("localMetricId")) {
                                                    localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                }
                                                if (localMetricIdAnswer != 0) {
                                                    previewLayout.setVisibility(View.VISIBLE);
                                                    String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                                    String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                    String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                    String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                        contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                                    if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                        try {
                                                            // Perform your calculations and set the text
                                                            double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                            contentTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                        } catch (NumberFormatException e) {
                                                            // Handle the case where parsing fails
                                                            e.printStackTrace();
                                                            previewLayout.setVisibility(View.GONE);
                                                        }
                                                    } else {
                                                        // Handle the case where any of the variables is null
                                                        contentTextView.setText("Some values are null");
                                                    }
                                                } else {
                                                    previewLayout.setVisibility(View.GONE);
                                                }

                                            } else {
                                                previewLayout.setVisibility(View.GONE);
                                            }

                                        }
                                    }
                                } else {
                                    previewLayout.setVisibility(View.GONE);
                                }


                                Log.d("AnswerNewSet", answerSet.toString());

                                break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing here if needed
                    }
                });
            }
        }

        // code end

        int finalSwitchId = 0;
        TextView finalYesNoConstraint = null;
        try {
            JSONObject displayConfigCheck = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfigCheck.has("display")) {
                JSONObject displayCheck = displayConfigCheck.getJSONObject("display");
                // Error Message for constraints
                String constraintType = "", errorMsg = "";
                int switchId = 0;
                try {
                    if(displayConfigCheck.has("calculation")) {
                        JSONObject jsonObject = displayConfigCheck.getJSONObject(CoffeeConstants.CALCULATION);
                        switchId = jsonObject.getInt("switch");
                        constraintType = jsonObject.getString("type");
                        errorMsg = jsonObject.getJSONObject("errorMsg").getString("error" + CommonUtilities.getLocaleSuffix(context.getResources()));
                    }
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().log(e.getMessage());

                }
                TextView yesNoConstraint = null;
                if (constraintType.equalsIgnoreCase(CoffeeConstants.SURVEY_COMPLETION_CHECK)) {
                    yesNoConstraint = new TextView(context);
                    yesNoConstraint.setText(errorMsg);
                    yesNoConstraint.setTextColor(ContextCompat.getColor(context, R.color.ofis_red));

                }
                finalSwitchId = switchId;
                finalYesNoConstraint = yesNoConstraint;

            }
        } catch (Exception e){
            e.printStackTrace();
        }

        if (radio != null && radio.getCount() > 0) {
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.activity_radio_group, null);
            RadioGroup radioGroup = (RadioGroup) linearLayout.findViewById(R.id.radioGroup);

            String assignmentType = "";
            String assignmentField = "";
            String assignmentFields = "";
            String isDefault = "";
            String isActive = "";
            JSONArray countryAndProductAssign = null;

            try {
                JSONObject displayConfig = new JSONObject();
                displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                if (displayConfig.has("display")) {
                    JSONObject display = displayConfig.getJSONObject("display");
                    JSONArray range = display.getJSONArray("lookupProperties");

                    for (int i = 0; i < range.length(); i++) {
                        JSONObject rangeArray = range.getJSONObject(i);
                        String lookupID = rangeArray.getString("lookupValueId");
                        assignmentType = rangeArray.getString("assignmentType");
                        assignmentField = rangeArray.getString("assignmentField");
                        assignmentFields = rangeArray.getString("assignmentFields");
                        isDefault = rangeArray.getString("isDefault");
                        if (rangeArray.has("active")) {
                            isActive = rangeArray.getString("active");
                        }
                        if (rangeArray.has("countryAndProductAssign")){
                            countryAndProductAssign = rangeArray.getJSONArray("countryAndProductAssign");
                        }

                        if (!assignmentField.equals("null")) {

                            String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                            if (assignmentField.equals(farmerAssignmentField) || assignmentType.equals("null") && assignmentField.equals("null")) {

                                if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                    //                            final int rbId = radio.getInt(radio.getColumnIndex("_id"));
//                            int columnIndex = (radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);

                                    String radioName = dl.getValueByLookUpID(lookupID, questions, questionColumnSuffix);
                                    RadioButton radioButton = (RadioButton) LayoutInflater.from(context).inflate(R.drawable.custom_radio_button, radioGroup, false);
                                    if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                        radioButton.setText(radioName + " " + roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId)));
                                    } else {
                                        radioButton.setText(radioName);
                                    }
                                    radioButton.setId(Integer.parseInt(lookupID));

                                    if (rbAnswers.getCount() == 0) {
                                        if (isDefault.equalsIgnoreCase("true")) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                            String finalDatatype = datatype;
                                            saveAnswerEntry(questionId, Integer.parseInt(lookupID), 0, -1, finalDatatype, lookupID, selectedGlobalID[0], selectedLocalID[0], 0);
                                        }
                                    }


                                    if (answerId != 0) {
                                        if (Integer.parseInt(lookupID) == Integer.parseInt(!Objects.equals(dl.radioAnswerValueFromAnswersTable(answerId), "") ? dl.radioAnswerValueFromAnswersTable(answerId) : dl.radioAnswerValueFromSavedAnswersTable(answerId))) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                            saveAnswerEntry(questionId, Integer.parseInt(lookupID), parentQuestionId, repeaterItemPosition, datatype, lookupID, selectedGlobalID[0], selectedLocalID[0], 0);
                                        }


                                    }
                                /*if (rbAnswers.getCount() > 0) {
                                    // disable while loading the "Not Applicable"
                                    if(Integer.parseInt(lookupID) == defaultLookUpValue){
                                        radioButton.setEnabled(false);
                                    }
                                    //End
                                    while (rbAnswers.moveToNext()) {
                                        if (rbAnswers.getInt(rbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                        }
                                    }
                                    rbAnswers.moveToPosition(-1);
                                }else {
                                    // N/A Question for franklin
                                    if (defaultLookUpValue != 0) {
                                        if (defaultLookUpValue == Integer.parseInt(lookupID)) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                        }
                                    }
                                }*/


                                    radioGroup.addView(radioButton);
                                }
                            }
                        }
                        else if (assignmentFields.equals("null") && countryAndProductAssign != null && countryAndProductAssign.length() != 0){
                            boolean isFound = countryAndProductAssignLookup(countryAndProductAssign, farmerId);

                            if (isFound) {
                                if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                    //                            final int rbId = radio.getInt(radio.getColumnIndex("_id"));
//                            int columnIndex = (radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);

                                    String radioName = dl.getValueByLookUpID(lookupID, questions, questionColumnSuffix);
                                    RadioButton radioButton = (RadioButton) LayoutInflater.from(context).inflate(R.drawable.custom_radio_button, radioGroup, false);
                                    if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                        radioButton.setText(radioName + " " + roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId)));
                                    } else {
                                        radioButton.setText(radioName);
                                    }
                                    radioButton.setId(Integer.parseInt(lookupID));

                                    if (rbAnswers.getCount() == 0) {
                                        if (isDefault.equalsIgnoreCase("true")) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                            String finalDatatype = datatype;
                                            saveAnswerEntry(questionId, Integer.parseInt(lookupID), 0, -1, finalDatatype, lookupID, selectedGlobalID[0], selectedLocalID[0], 0);
                                        }
                                    }


                                    if (answerId != 0) {
                                        if (Integer.parseInt(lookupID) == Integer.parseInt(!Objects.equals(dl.radioAnswerValueFromAnswersTable(answerId), "") ? dl.radioAnswerValueFromAnswersTable(answerId) : dl.radioAnswerValueFromSavedAnswersTable(answerId))) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                            saveAnswerEntry(questionId, Integer.parseInt(lookupID), parentQuestionId, repeaterItemPosition, datatype, lookupID, selectedGlobalID[0], selectedLocalID[0], 0);
                                        }


                                    }
                                /*if (rbAnswers.getCount() > 0) {
                                    // disable while loading the "Not Applicable"
                                    if(Integer.parseInt(lookupID) == defaultLookUpValue){
                                        radioButton.setEnabled(false);
                                    }
                                    //End
                                    while (rbAnswers.moveToNext()) {
                                        if (rbAnswers.getInt(rbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                        }
                                    }
                                    rbAnswers.moveToPosition(-1);
                                }else {
                                    // N/A Question for franklin
                                    if (defaultLookUpValue != 0) {
                                        if (defaultLookUpValue == Integer.parseInt(lookupID)) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                        }
                                    }
                                }*/


                                    radioGroup.addView(radioButton);
                                }
                            }

                        }
                        else {
                            // Remove square brackets and split the string into an array of strings
                            String[] stringArray = assignmentFields.substring(1, assignmentFields.length() - 1).split(",");


                            if (assignmentFields.equals("null") || assignmentFields.equals("[]")) {
                                if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                    //                            final int rbId = radio.getInt(radio.getColumnIndex("_id"));
//                            int columnIndex = (radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);

                                    String radioName = dl.getValueByLookUpID(lookupID, questions, questionColumnSuffix);
                                    RadioButton radioButton = (RadioButton) LayoutInflater.from(context).inflate(R.drawable.custom_radio_button, radioGroup, false);
                                    if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                        radioButton.setText(radioName + " " + roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId)));
                                    } else {
                                        radioButton.setText(radioName);
                                    }
                                    radioButton.setId(Integer.parseInt(lookupID));

                                    if (rbAnswers.getCount() == 0) {
                                        if (isDefault.equalsIgnoreCase("true")) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                            String finalDatatype = datatype;
                                            saveAnswerEntry(questionId, Integer.parseInt(lookupID), 0, -1, finalDatatype, lookupID, selectedGlobalID[0], selectedLocalID[0], 0);
                                        }
                                    }


                                    if (answerId != 0) {
                                        if (Integer.parseInt(lookupID) == Integer.parseInt(!Objects.equals(dl.radioAnswerValueFromAnswersTable(answerId), "") ? dl.radioAnswerValueFromAnswersTable(answerId) : dl.radioAnswerValueFromSavedAnswersTable(answerId))) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                            saveAnswerEntry(questionId, Integer.parseInt(lookupID), parentQuestionId, repeaterItemPosition, datatype, lookupID, selectedGlobalID[0], selectedLocalID[0], 0);
                                        }


                                    }
                                /*if (rbAnswers.getCount() > 0) {
                                    // disable while loading the "Not Applicable"
                                    if(Integer.parseInt(lookupID) == defaultLookUpValue){
                                        radioButton.setEnabled(false);
                                    }
                                    //End
                                    while (rbAnswers.moveToNext()) {
                                        if (rbAnswers.getInt(rbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                        }
                                    }
                                    rbAnswers.moveToPosition(-1);
                                }else {
                                    // N/A Question for franklin
                                    if (defaultLookUpValue != 0) {
                                        if (defaultLookUpValue == Integer.parseInt(lookupID)) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                        }
                                    }
                                }*/


                                    radioGroup.addView(radioButton);
                                }
                            } else {
                                // Convert the string array to an integer array
                                int[] intArray = new int[stringArray.length];
                                for (int m = 0; m < stringArray.length; m++) {
                                    intArray[m] = Integer.parseInt(stringArray[m]);
                                }

                                // Iterate through the integer array using a for loop
                                for (int fieldMultipleAssignment : intArray) {

                                    String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                    if (String.valueOf(fieldMultipleAssignment).equals(farmerAssignmentField) || assignmentType.equals("null") && String.valueOf(fieldMultipleAssignment).equals("null")) {

                                        if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                            //                            final int rbId = radio.getInt(radio.getColumnIndex("_id"));
//                            int columnIndex = (radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : radio.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);

                                            String radioName = dl.getValueByLookUpID(lookupID, questions, questionColumnSuffix);
                                            RadioButton radioButton = (RadioButton) LayoutInflater.from(context).inflate(R.drawable.custom_radio_button, radioGroup, false);
                                            if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                                radioButton.setText(radioName + " " + roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId)));
                                            } else {
                                                radioButton.setText(radioName);
                                            }
                                            radioButton.setId(Integer.parseInt(lookupID));

                                            if (rbAnswers.getCount() == 0) {
                                                if (isDefault.equalsIgnoreCase("true")) {
                                                    radioButton.setChecked(true);
                                                    radioButton.setEnabled(true);
                                                    String finalDatatype = datatype;
                                                    saveAnswerEntry(questionId, Integer.parseInt(lookupID), 0, -1, finalDatatype, lookupID, selectedGlobalID[0], selectedLocalID[0], 0);
                                                }
                                            }


                                            if (answerId != 0) {
                                                if (Integer.parseInt(lookupID) == Integer.parseInt(!Objects.equals(dl.radioAnswerValueFromAnswersTable(answerId), "") ? dl.radioAnswerValueFromAnswersTable(answerId) : dl.radioAnswerValueFromSavedAnswersTable(answerId))) {
                                                    radioButton.setChecked(true);
                                                    radioButton.setEnabled(true);
                                                    saveAnswerEntry(questionId, Integer.parseInt(lookupID), parentQuestionId, repeaterItemPosition, datatype, lookupID, selectedGlobalID[0], selectedLocalID[0], 0);
                                                }


                                            }
                                /*if (rbAnswers.getCount() > 0) {
                                    // disable while loading the "Not Applicable"
                                    if(Integer.parseInt(lookupID) == defaultLookUpValue){
                                        radioButton.setEnabled(false);
                                    }
                                    //End
                                    while (rbAnswers.moveToNext()) {
                                        if (rbAnswers.getInt(rbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                        }
                                    }
                                    rbAnswers.moveToPosition(-1);
                                }else {
                                    // N/A Question for franklin
                                    if (defaultLookUpValue != 0) {
                                        if (defaultLookUpValue == Integer.parseInt(lookupID)) {
                                            radioButton.setChecked(true);
                                            radioButton.setEnabled(true);
                                        }
                                    }
                                }*/


                                            radioGroup.addView(radioButton);
                                        }
                                    }

                                }
                            }

                        }

                    }

                    if (otherChoice.equals("true")) {
                        RadioButton radioButton = (RadioButton) LayoutInflater.from(context).inflate(R.drawable.custom_radio_button, radioGroup, false);
                        radioButton.setText("Others");
                        radioGroup.addView(radioButton);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            int finalGlobalMetrics = 0;
            if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                finalGlobalMetrics = Integer.parseInt(metricGlobalId);
                innerWrapper.addView(text);
                innerWrapper.addView(spinner);
                questionWrapper.addView(linearLayout);
                questionWrapper.addView(innerWrapper);
                questionWrapper.addView(previewLayout);
                parentLayout.addView(questionWrapper);

            } else {
                questionWrapper.addView(linearLayout);
                parentLayout.addView(questionWrapper);
            }

            saveMultipleAnswerEntryRadio(questionId, parentQuestionId, datatype, linearLayout, repeaterItemPosition, finalGlobalMetrics, selectedLocalID[0], 0, spinner, spinnerData, spinnerAdapter, previewLayout, contentTextView, questionWrapper, finalSwitchId, finalYesNoConstraint,questions);

        }

    }


    public void renderNextPage(LinearLayout parentLayout) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        ConstraintLayout linearLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.go_next_layout, null);
        questionWrapper.addView(linearLayout);
        parentLayout.addView(questionWrapper);
    }

    void renderSipDropDownQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
        final LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);
        resources = res;
//        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));


        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        }
        if (oldDatatype != null) {
            if (oldDatatype.equals("Dropdown")) {
                datatype = "int";
            }
        }


        int answerIdTemp = 0;
        if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerSipEntry.COLUMN_NAME_ID));
            } else {
                if (qCursor.getColumnIndex("answer_id") != -1) {
                    answerIdTemp = qCursor.getInt(qCursor.getColumnIndex("answer_id"));
                }
            }
        }
        final int answerId = answerIdTemp;

        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        Cursor spinnervalues;

        // get display config
        try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            final String columnName = (qCursor.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE;

            if (displayConfig.has("lookuptable")) {
                spinnervalues = dl.getSpinnerData(displayConfig.getString("lookuptable"), false, null, null, true, "name");
            } else {
                spinnervalues = dl.getSipLookupValuesForQuestion(qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID)), columnName);
            }

            final boolean isDensityQuestion = (displayConfig.has("densityquestionparent") && (displayConfig.getBoolean("densityquestionparent")));

            dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, columnName, false);

            if (hasAnswers) {
                if (datatype.equals("int") && (!isDensityQuestion)) {
                    Integer currentAnswerInt = -1;
                    if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                        currentAnswerInt = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_INT));
                    } else {
                        if (qCursor.getColumnIndex(OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_INT) != -1) {
                            currentAnswerInt = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_INT));
                        }
                    }

                    for (int i = 0; i < spinner.getCount(); i++) {
                        if (spinner.getItemIdAtPosition(i) == currentAnswerInt) {
                            spinner.setSelection(i);
                            break;
                        }
                    }

                } else {
                    String currentAnswerValue = null;
                    if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                        currentAnswerValue = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
                    } else {
                        if (qCursor.getColumnIndex("answer_" + datatype) != -1) {
                            currentAnswerValue = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                        }
                    }

                    for (int i = 0; i < spinner.getCount(); i++) {
                        Cursor currentItem = (Cursor) spinner.getItemAtPosition(i);
                        if (currentItem.getString(currentItem.getColumnIndex(columnName)).equalsIgnoreCase(currentAnswerValue)) {
                            spinner.setSelection(i);
                            break;
                        }
                    }
                }
            }


            String finalDatatype = datatype;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {


                    //renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, olamFarmerId, farmerModuleId, res);


                    //renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, olamFarmerId, farmerModuleId, res);
                    if (id > 0) {
                        if (finalDatatype.equals("int") && (!isDensityQuestion)) {
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, String.valueOf(id), 0, 0, 0);

                            long h = Long.parseLong("0026");


                        } else {
                            Cursor selecteditem = (Cursor) spinner.getSelectedItem();
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, selecteditem.getString(selecteditem.getColumnIndex(columnName)), 0, 0, 0);
                        }
                    } else {
                        removeAnswerEntry(questionId, answerId);
                    }
                    if (isDensityQuestion) {
                        renderTreeDensityPlots(position, res);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    if (isDensityQuestion) {
                        renderTreeDensityPlots(0, res);
                    }
                }
            });

            if (isRepeaterItem) {
                parentLayout.addView(spinner);
            } else {
                questionWrapper.addView(spinner);
                parentLayout.addView(questionWrapper);
            }


        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error gettign dropdown display config " + e.getMessage());
        }

    }

    void renderDropDownQuestionSubQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
        final LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        // renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);
        resources = res;
       /* final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
*/


        String datatypeduplicate = "";
        int questionIddouplicate = 0;
        if (qCursor != null) {
            if (qCursor.moveToFirst()) {
                do {
                    datatypeduplicate = qCursor.getString(qCursor.getColumnIndex("datatype_name"));

                    questionIddouplicate = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                } while (qCursor.moveToNext());
            }
        }

        final int questionId = questionIddouplicate;
        final String datatype = datatypeduplicate;
        final int dataTypeID = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));


        int answerIdTemp = 0;

        // answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));

        /*if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
            } else {
                if (qCursor.getColumnIndex("answer_id") != -1) {
                    answerIdTemp = qCursor.getInt(qCursor.getColumnIndex("answer_id"));
                }
            }
        }*/


        //  answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
        final int answerId = answerIdTemp;

//        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, questionWrapper, false);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, res.getStringArray(R.array.plotvariants));
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        Cursor spinnervalues;

        // get display config
        try {
            int lookupvalue = 0;

            JSONObject displayConfig = new JSONObject();
            String columnNameduplicate = "";
            if (qCursor != null) {
                if (qCursor.moveToFirst()) {
                    do {
                        displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                        columnNameduplicate = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;

                        lookupvalue = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                    } while (qCursor.moveToNext());
                }
            }


            final String columnName = columnNameduplicate;

            if (displayConfig.has("lookuptable")) {
                spinnervalues = dl.getSpinnerData(displayConfig.getString("lookuptable"), false, null, null, true, "name");
            } else {
                spinnervalues = dl.getLookupValuesForQuestion(lookupvalue, columnName,dataTypeID);
            }

            final boolean isDensityQuestion = (displayConfig.has("densityquestionparent") && (displayConfig.getBoolean("densityquestionparent")));

            dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, columnName, false);

            /*if (hasAnswers) {
                if (datatype.equals("int") && (!isDensityQuestion)) {
                    Integer currentAnswerInt = -1;
                    if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                        currentAnswerInt = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT));
                    } else {
                        if (qCursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT) != -1) {
                            currentAnswerInt = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT));
                        }
                    }

                    for (int i = 0; i < spinner.getCount(); i++) {
                        if (spinner.getItemIdAtPosition(i) == currentAnswerInt) {
                            spinner.setSelection(i);
                            break;
                        }
                    }

                } else {
                    String currentAnswerValue;
                    if (isRepeaterItem && repeaterAnswers != null) {
                        currentAnswerValue = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
                    } else {
                        currentAnswerValue = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                    }

                    for (int i = 0; i < spinner.getCount(); i++) {
                        Cursor currentItem = (Cursor) spinner.getItemAtPosition(i);
                        if (currentItem.getString(currentItem.getColumnIndex(columnName)).equalsIgnoreCase(currentAnswerValue)) {
                            spinner.setSelection(i);
                            break;
                        }
                    }
                }
            }

*/


            Cursor cursor = dl.getParentSubquestionAnswer(Utilities.subModuleId, parentQuestionId, questionId);


            int position = 0;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        position = Integer.parseInt(cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_POSITION)));
                        spinner.setSelection(position);

                    } while (qCursor.moveToNext());
                }
            }


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {


                    if (id > 0) {


                        int delete = dl.prentidAnswerDelete(parentQuestionId, questionId);
                        long l = id;
                        int i = (int) l;
                        dl.prentidAnswer(Utilities.subModuleId, parentQuestionId, questionId, i + "", position + "", Utilities.olamFarmerId, Utilities.moduleId + "");

                        if (datatype.equals("int") && (!isDensityQuestion)) {

                            long h = Long.parseLong("0026");


                        } else {
                            Cursor selecteditem = (Cursor) spinner.getSelectedItem();


                        }
                    } else {
                        int delete = dl.prentidAnswerDelete(parentQuestionId, questionId);
                    }
                    if (isDensityQuestion) {
                        renderTreeDensityPlots(position, res);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    if (isDensityQuestion) {
                        renderTreeDensityPlots(0, res);
                    }
                }
            });

            if (isRepeaterItem) {
                parentLayout.addView(spinner);
            } else {
                questionWrapper.addView(spinner);
                parentLayout.addView(questionWrapper);
            }


        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error gettign dropdown display config " + e.getMessage());
        }

    }


    public void renderCheckBoxQuestion(Cursor questions, LinearLayout parentLayout, long farmerId, String stakeholderId, long farmerModuleId, boolean isSavedAns, boolean isRepeaterItem, final int repeaterItemPosition, Cursor repeaterAnswers, final int parentQuestionId) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(questions, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);


//        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));
        Utilities.datatypeIdCLMRS =dataTypeID;

        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        }
        if (oldDatatype != null) {
            if (oldDatatype.equals("Checkbox")) {
                datatype = "int";
            }
        }


        int answerIdTemp = 0;
        final boolean singleSelect;
        boolean tempSingleSelect = false;
        String metricGlobalId = dl.getGlobalIdByQuestionId(questionId);

        Cursor checkboxes = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);
        Cursor cbAnswers;
        if (isSavedAns) {
            cbAnswers = dl.getSavedAnswersForQuestionByFarmer(questionId, farmerId, stakeholderId, farmerModuleId);
            if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                answerIdTemp = questions.getInt(questions.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
            }
        } else {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                if (viewAnswer){
                    cbAnswers = dl.getSavedAnswersForQuestionByFarmer(questionId, farmerId, stakeholderId, farmerModuleId);
                } else {
                    cbAnswers = dl.getAnswersForQuestionByFarmerRepeaterCheckbox(questionId, farmerId, stakeholderId, farmerModuleId, repeaterItemPosition);
                }
            } else {
                if (viewAnswer){
                    cbAnswers = dl.getSavedAnswersForQuestionByFarmer(questionId, farmerId, stakeholderId, farmerModuleId);
                } else {
                    cbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId, stakeholderId, farmerModuleId);
                }
            }
            if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                try {
                    answerIdTemp = questions.getInt(questions.getColumnIndex("answer_id"));
                } catch (Exception e) {
                    e.printStackTrace();
                    answerIdTemp = 0;
                }

            }
        }
        final int answerId = answerIdTemp;

        // is this a question we can only select one answer to?
        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting checkbox display config " + e.getMessage());
        }

        singleSelect = tempSingleSelect;

        // Parse N/A Question
        int defaultLookUpValue = 0;
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("calculation")) {
                String calculationNode = displayConfig.optString(CoffeeConstants.CALCULATION);
                JSONObject jsonObject = new JSONObject(calculationNode);
                defaultLookUpValue = jsonObject.optInt("defaultLookUpValue", 0);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }


        //code start
//        String metricGlobalId = dl.getGlobalIdByQuestionId(questionId);
//        String metricGlobalId = "2";
        final Integer[] selectedLocalID = {0};
        final Integer[] selectedGlobalID = {0};
        final boolean[] unCheckAllBox = {false};
        boolean isLocalUnit = false;
        // Create an inner wrapper
        LinearLayout innerWrapper = new LinearLayout(context);
        innerWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        innerWrapper.setOrientation(LinearLayout.HORIZONTAL);


        TextView text = new TextView(getInstance().context);
        text.setText(context.getResources().getString(R.string.metrics));
        text.setTextColor(context.getResources().getColor(R.color.weightage_bar));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, // Width set to 0
                LinearLayout.LayoutParams.MATCH_PARENT, 2
        );
        text.setGravity(Gravity.CENTER);
        text.setLayoutParams(textParams);

        LinearLayout previewLayout = new LinearLayout(context);
        previewLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        previewLayout.setOrientation(LinearLayout.VERTICAL);
        previewLayout.setPadding(16, 16, 16, 16);

        TextView titleTextView = new TextView(context);
        titleTextView.setText(context.getResources().getString(R.string.converted_metrics));
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        titleTextView.setTextColor(context.getResources().getColor(R.color.weightage_bar));

        // Create the second TextView
        TextView contentTextView = new TextView(context);
        contentTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Add TextViews to the LinearLayout
//        previewLayout.addView(titleTextView);
//        previewLayout.addView(contentTextView);
        previewLayout.setVisibility(View.GONE);


        final Spinner spinner = (Spinner) inflater.inflate(R.layout.metric_spinner, innerWrapper, false);
        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
            List<LocalUnitDao.UnitMapping> unitMappings = roomDB.localUnitDao().getValueByGlobalId(Integer.parseInt(metricGlobalId));

            if (unitMappings.isEmpty()) {

                List<String> getGlobalUnit = roomDB.globalUnitDao().getGlobalAbbreviation(Integer.parseInt(metricGlobalId));
                selectedGlobalID[0] = Integer.parseInt(metricGlobalId);
                LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                        0, // Width set to 0
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        8 // Weight set to 2 (20% of the total weight)
                );
                spinnerParams.setMargins(7, 0, 0, 0);
                spinner.setLayoutParams(spinnerParams);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, getGlobalUnit);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                /*if (hasAnswers){
                    int localAnswer = Integer.valueOf(dl.getGlobalIdByAnswerID(answerId));
                    spinner.setSelection(localAnswer);
                }else {
                    spinner.setSelection(Integer.parseInt(metricGlobalId));
                }*/
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item from the adapter
                        String selectedAbbreviation = spinnerAdapter.getItem(position);


//                        Toast.makeText(context, "Selected localUnitId: " + selectedAbbreviation, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing here if needed
                    }
                });


            } else {
                isLocalUnit = true;
                Map<Integer, String> spinnerData = new HashMap<>();
                List<GlobalUnitDao.UnitMapping> globalMappings = roomDB.globalUnitDao().getGlobalId(Integer.parseInt(metricGlobalId));

                for (LocalUnitDao.UnitMapping unitMapping : unitMappings) {
                    spinnerData.put(unitMapping.localUnitId, unitMapping.localUnitAbbreviation);
                }

                for (GlobalUnitDao.UnitMapping unitMapping : globalMappings) {
                    spinnerData.put(unitMapping.globalUnitId, unitMapping.globalAbbreviation);
                }

                List<String> spinnerItemList = new ArrayList<>(spinnerData.values());
                LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                        0, // Width set to 0
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        8 // Weight set to 2 (20% of the total weight)
                );
                spinnerParams.setMargins(7, 0, 0, 0);
                spinner.setLayoutParams(spinnerParams);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, spinnerItemList);
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                if (answerId != 0) {
                    int localAnswer = Integer.valueOf(dl.getLocalIdByAnswerID(answerId));
                    if (localAnswer == 0) {
                        spinner.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), spinnerData.keySet().toArray(new Integer[0])));
                    } else {
                        spinner.setSelection(getIndexForValue(localAnswer, spinnerData.keySet().toArray(new Integer[0])));
                    }

                } else {
//                    spinner.setSelection(Integer.parseInt(metricGlobalId));
                    spinner.setSelection(getIndexForValue(Integer.parseInt(metricGlobalId), spinnerData.keySet().toArray(new Integer[0])));
                }
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Get the selected item from the adapter
                        String selectedAbbreviation = spinnerAdapter.getItem(position);

                        // Find the corresponding localUnitId in the spinnerData map
                        for (Map.Entry<Integer, String> entry : spinnerData.entrySet()) {
                            if (entry.getValue().equals(selectedAbbreviation)) {
                                int selectedLocalUnitId = entry.getKey();

                                if (entry.getKey() == Integer.parseInt(metricGlobalId)) {
                                    selectedGlobalID[0] = entry.getKey();
                                    selectedLocalID[0] = 0;
                                } else {
                                    selectedLocalID[0] = entry.getKey();
                                    selectedGlobalID[0] = 0;
                                }

//                                uncheckAllCheckbox(questionWrapper, checkboxes, questions, farmerId,cbAnswers, defaultLookUpValue);
                                unCheckAllBox[0] = true;
                                // Display a Toast with the selected localUnitId
//                                Toast.makeText(context, "Selected localUnitId: " + selectedLocalUnitId, Toast.LENGTH_SHORT).show();
                                if (answerSet != null) {
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");

                                            if (currentQuestionId == questionId) {
                                                try {
                                                    currentAnswer.put("globalMetricId", selectedGlobalID[0]);
                                                    currentAnswer.put("localMetricId", selectedLocalID[0]);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                        }
                                    }
                                }

                                if (answerSet != null) {
                                    previewLayout.removeAllViews();
                                    previewLayout.addView(titleTextView);
                                    for (int i = 0; i < answerSet.size(); i++) {
                                        JSONObject currentAnswer = answerSet.get(i);

                                        if (currentAnswer != null) {
                                            int currentQuestionId = currentAnswer.optInt("question_id");
                                            int answerJsonVal = currentAnswer.optInt("answer");

                                            if (currentQuestionId == questionId) {
                                                Integer globalMetricIdAnswer = null;
                                                Integer localMetricIdAnswer = null;
                                                if (currentAnswer.has("globalMetricId")) {
                                                    globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                }
                                                if (currentAnswer.has("localMetricId")) {
                                                    localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                }
                                                if (localMetricIdAnswer != 0) {
                                                    TextView answerTextView = new TextView(context);
                                                    answerTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                                    ));
//                                                    titleTextView.setVisibility(View.VISIBLE);
//                                                    contentTextView.setVisibility(View.VISIBLE);
                                                    String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                                    String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                    String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                    String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                                    answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                                    if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                        try {
                                                            // Perform your calculations and set the text
                                                            if (canParseToDouble(answerValue)) {
                                                                double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                                answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                                previewLayout.addView(answerTextView);
                                                                previewLayout.setVisibility(View.VISIBLE);
                                                            }

                                                        } catch (NumberFormatException e) {
                                                            // Handle the case where parsing fails
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        // Handle the case where any of the variables is null
                                                        contentTextView.setText("Some values are null");
                                                    }
                                                } else {
                                                    previewLayout.setVisibility(View.GONE);
                                                    previewLayout.removeAllViews();
                                                }
                                            } else {
                                                previewLayout.setVisibility(View.GONE);
                                            }

                                        }
                                    }
                                }

                                Log.d("AnswerNewSet", answerSet.toString());
                                break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing here if needed
                    }
                });
            }
        }

        // code end

        final int[] checkboxCount = {0};
        if (checkboxes != null && checkboxes.getCount() > 0) {
            // add the table layout to the screen
            final TableLayout tl = (TableLayout) inflater.inflate(R.layout.module_checkbox_table, questionWrapper, false);
            tl.setSaveEnabled(true);

            // adding validation
            String assignmentType = "";
            String assignmentField = "";
            String assignmentFields = "";
            String isDefault = "";
            String maxSelection = "";
            String isActive = "";
            JSONArray countryAndProductAssign = null;
            try {
                JSONObject displayConfig = new JSONObject();
                displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                if (displayConfig.has("display")) {
                    JSONObject display = displayConfig.getJSONObject("display");
                    if (display.has("lookupProperties")) {
                        maxSelection = display.getString("maxSelection");
                        JSONArray range = display.getJSONArray("lookupProperties");

                        for (int i = 0; i < range.length(); i++) {
                            JSONObject rangeArray = range.getJSONObject(i);
                            String lookupID = rangeArray.getString("lookupValueId");
                            assignmentType = rangeArray.getString("assignmentType");
                            assignmentField = rangeArray.getString("assignmentField");
                            assignmentFields = rangeArray.getString("assignmentFields");
                            if (rangeArray.has("countryAndProductAssign")){
                                countryAndProductAssign = rangeArray.getJSONArray("countryAndProductAssign");
                            }
                            isDefault = rangeArray.getString("isDefault");
                            if (rangeArray.has("active")) {
                                isActive = rangeArray.getString("active");
                            }

                            if (!assignmentField.equals("null")) {

                                String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                if (assignmentField.equals(farmerAssignmentField) || assignmentType.equals("null") && assignmentField.equals("null")) {

                                    if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                        // generate row item
                                        TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                                        TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                                        TextView tv = (TextView) tr.findViewById(R.id.cbTitle);
                                        CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                                        // last line

//                                      final int cbId = checkboxes.getInt(checkboxes.getColumnIndex("_id"));

//                                      int columnIndex = (checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);


                                        String checkBoxName = dl.getValueByLookUpID(lookupID, questions, questionColumnSuffix);
                                        tvId.setText(lookupID);
                                        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                            tv.setText(checkBoxName + " " + roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId)));
                                        } else {
                                            tv.setText(checkBoxName);
                                        }
                                        tl.addView(tr);

                                        /*if (cbAnswers.getCount() == 0) {
                                            if (isDefault.equalsIgnoreCase("true")) {
                                                cb.setChecked(true);
                                                cb.setEnabled(true);
                                            }
                                        }*/

                                        // initilaize default look up globally
                                        // pre-fill checkboxes that have been previously ticked
                                        if (cbAnswers.getCount() > 0) {
                                            // disable while loading the "Not Applicable"
                                            if (Integer.parseInt(lookupID) == defaultLookUpValue) {
                                                cb.setEnabled(false);
                                            }
                                            //End
                                            while (cbAnswers.moveToNext()) {
                                                if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                                    cb.setChecked(true);
                                                    cb.setEnabled(true);
                                                    checkboxCount[0]++;
                                                }
                                            }
                                            cbAnswers.moveToPosition(-1);
                                        } else {
                                            // N/A Question for franklin
                                            if (defaultLookUpValue != 0) {
                                                if (defaultLookUpValue == Integer.parseInt(lookupID)) {
                                                    cb.setChecked(true);
                                                    cb.setEnabled(true);
                                                }
                                            }
                                        }
                                        tr.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                                                checkbox.setChecked(!checkbox.isChecked());
                                                checkbox.callOnClick();
                                            }

                                        });

                                        //stick the answer in the question itself
                                        int finalDefaultLookUpValue = defaultLookUpValue;
                                        String finalDatatype = datatype;
                                        String finalMaxSelection = maxSelection;
                                        cb.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);
                                                if (checkbox.isChecked()) {
                                                    checkboxCount[0]++;
                                                } else {
                                                    checkboxCount[0]--;
                                                }

                                                if (singleSelect) {
                                                    int ssTableRows = tl.getChildCount();
                                                    for (int j = 0; j < ssTableRows; j++) {
                                                        TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                        CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                                        ssTcb.setChecked(false);
                                                    }
                                                    CheckBox clickedCb = (CheckBox) view;
                                                    clickedCb.setChecked(true);
                                                }

                                                // Update the Not applicable field if none of the value is selected
                                                if (finalDefaultLookUpValue != 0) {
                                                    int ssTableRows = tl.getChildCount();
                                                    boolean checkboxTicked = false;
                                                    int count = 0;
                                                    for (int j = 0; j < ssTableRows; j++) {
                                                        TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                        TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                        CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                        if (ssTcb.isChecked()) {
                                                            checkboxTicked = true;
                                                        }
                                                        if (Integer.parseInt(cbId.getText().toString()) != finalDefaultLookUpValue) {
                                                            if (ssTcb.isChecked()) {
                                                                count++;
                                                            }
                                                        }

                                                    }
                                                    if (!checkboxTicked) {
                                                        for (int j = 0; j < ssTableRows; j++) {
                                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                            if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                                ssTcb.setChecked(true);
                                                                ssTcb.setEnabled(true);
                                                            }
                                                        }
                                                    }

                                                    // Remove the Not applicable when other selected.
                                                    if (count > 0) {
                                                        for (int j = 0; j < ssTableRows; j++) {
                                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                            if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                                ssTcb.setChecked(false);
                                                                ssTcb.setEnabled(false);
                                                            }
                                                        }
                                                    }

                                                }
                                                if (finalMaxSelection != "null") {
                                                    if (checkboxCount[0] > Integer.parseInt(finalMaxSelection)) {
                                                        cb.setChecked(false);
                                                        checkboxCount[0]--;
                                                        Toast.makeText(context, "You can only select " + finalMaxSelection + " options", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                if (unCheckAllBox[0]) {
                                                    while (cbAnswers.moveToNext()) {
                                                        if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                                            cb.setChecked(false);
                                                            cb.setEnabled(false);
                                                            checkboxCount[0]++;
                                                        }
                                                    }
                                                }


                                                saveMultipleAnswerEntry(questionId, parentQuestionId, finalDatatype, tl, selectedGlobalID[0], selectedLocalID[0], 0, repeaterItemPosition);

                                                if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                                    if (answerSet.size() != 0) {
                                                        ;
                                                        previewLayout.removeAllViews();
                                                        previewLayout.addView(titleTextView);
                                                        for (int i = 0; i < answerSet.size(); i++) {
                                                            JSONObject currentAnswer = answerSet.get(i);

                                                            if (currentAnswer != null) {
                                                                int currentQuestionId = currentAnswer.optInt("question_id");
                                                                int answerJsonVal = currentAnswer.optInt("answer");

                                                                if (currentQuestionId == questionId) {
                                                                    Integer globalMetricIdAnswer = null;
                                                                    Integer localMetricIdAnswer = null;
                                                                    if (currentAnswer.has("globalMetricId")) {
                                                                        globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                                    }
                                                                    if (currentAnswer.has("localMetricId")) {
                                                                        localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                                    }
                                                                    if (localMetricIdAnswer != 0) {
                                                                        TextView answerTextView = new TextView(context);
                                                                        answerTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                                                        ));
                                                                        String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                                                        String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                                        String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                                        String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                                                    answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                                                        if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                                            try {
                                                                                // Perform your calculations and set the text

                                                                                if (canParseToDouble(answerValue)) {
                                                                                    double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                                                    answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                                                    previewLayout.addView(answerTextView);
                                                                                    previewLayout.setVisibility(View.VISIBLE);
                                                                                }
                                                                            } catch (
                                                                                    NumberFormatException e) {
                                                                                // Handle the case where parsing fails
                                                                                e.printStackTrace();
                                                                            }
                                                                        } else {
                                                                            // Handle the case where any of the variables is null
                                                                            contentTextView.setText("Some values are null");
                                                                        }
                                                                    } else {
                                                                        previewLayout.setVisibility(View.GONE);
                                                                    }

                                                                } else {
                                                                    previewLayout.setVisibility(View.GONE);
                                                                }

                                                            }
                                                        }
                                                    } else {
                                                        previewLayout.setVisibility(View.GONE);
                                                        previewLayout.removeAllViews();
                                                    }
                                                }

                                            }
                                        });

                                    }
                                }
                            }
                            else if (assignmentFields.equals("null") && countryAndProductAssign != null && countryAndProductAssign.length() != 0){
                                boolean isFound = countryAndProductAssignLookup(countryAndProductAssign, farmerId);

                                if (isFound) {
                                    if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                        // generate row item
                                        TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                                        TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                                        TextView tv = (TextView) tr.findViewById(R.id.cbTitle);
                                        CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                                        // last line

//                                      final int cbId = checkboxes.getInt(checkboxes.getColumnIndex("_id"));

//                                      int columnIndex = (checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);


                                        String checkBoxName = dl.getValueByLookUpID(lookupID, questions, questionColumnSuffix);
                                        tvId.setText(lookupID);
                                        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                            tv.setText(checkBoxName + " " + roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId)));
                                        } else {
                                            tv.setText(checkBoxName);
                                        }
                                        tl.addView(tr);

                                        /*if (cbAnswers.getCount() == 0) {
                                            if (isDefault.equalsIgnoreCase("true")) {
                                                cb.setChecked(true);
                                                cb.setEnabled(true);
                                            }
                                        }*/

                                        // initilaize default look up globally
                                        // pre-fill checkboxes that have been previously ticked
                                        if (cbAnswers.getCount() > 0) {
                                            // disable while loading the "Not Applicable"
                                            if (Integer.parseInt(lookupID) == defaultLookUpValue) {
                                                cb.setEnabled(false);
                                            }
                                            //End
                                            while (cbAnswers.moveToNext()) {
                                                if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                                    cb.setChecked(true);
                                                    cb.setEnabled(true);
                                                    checkboxCount[0]++;
                                                }
                                            }
                                            cbAnswers.moveToPosition(-1);
                                        } else {
                                            // N/A Question for franklin
                                            if (defaultLookUpValue != 0) {
                                                if (defaultLookUpValue == Integer.parseInt(lookupID)) {
                                                    cb.setChecked(true);
                                                    cb.setEnabled(true);
                                                }
                                            }
                                        }
                                        tr.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                                                checkbox.setChecked(!checkbox.isChecked());
                                                checkbox.callOnClick();
                                            }

                                        });

                                        //stick the answer in the question itself
                                        int finalDefaultLookUpValue = defaultLookUpValue;
                                        String finalDatatype = datatype;
                                        String finalMaxSelection = maxSelection;
                                        cb.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);
                                                if (checkbox.isChecked()) {
                                                    checkboxCount[0]++;
                                                } else {
                                                    checkboxCount[0]--;
                                                }

                                                if (singleSelect) {
                                                    int ssTableRows = tl.getChildCount();
                                                    for (int j = 0; j < ssTableRows; j++) {
                                                        TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                        CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                                        ssTcb.setChecked(false);
                                                    }
                                                    CheckBox clickedCb = (CheckBox) view;
                                                    clickedCb.setChecked(true);
                                                }

                                                // Update the Not applicable field if none of the value is selected
                                                if (finalDefaultLookUpValue != 0) {
                                                    int ssTableRows = tl.getChildCount();
                                                    boolean checkboxTicked = false;
                                                    int count = 0;
                                                    for (int j = 0; j < ssTableRows; j++) {
                                                        TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                        TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                        CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                        if (ssTcb.isChecked()) {
                                                            checkboxTicked = true;
                                                        }
                                                        if (Integer.parseInt(cbId.getText().toString()) != finalDefaultLookUpValue) {
                                                            if (ssTcb.isChecked()) {
                                                                count++;
                                                            }
                                                        }

                                                    }
                                                    if (!checkboxTicked) {
                                                        for (int j = 0; j < ssTableRows; j++) {
                                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                            if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                                ssTcb.setChecked(true);
                                                                ssTcb.setEnabled(true);
                                                            }
                                                        }
                                                    }

                                                    // Remove the Not applicable when other selected.
                                                    if (count > 0) {
                                                        for (int j = 0; j < ssTableRows; j++) {
                                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                            if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                                ssTcb.setChecked(false);
                                                                ssTcb.setEnabled(false);
                                                            }
                                                        }
                                                    }

                                                }
                                                if (finalMaxSelection != "null") {
                                                    if (checkboxCount[0] > Integer.parseInt(finalMaxSelection)) {
                                                        cb.setChecked(false);
                                                        checkboxCount[0]--;
                                                        Toast.makeText(context, "You can only select " + finalMaxSelection + " options", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                if (unCheckAllBox[0]) {
                                                    while (cbAnswers.moveToNext()) {
                                                        if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                                            cb.setChecked(false);
                                                            cb.setEnabled(false);
                                                            checkboxCount[0]++;
                                                        }
                                                    }
                                                }


                                                saveMultipleAnswerEntry(questionId, parentQuestionId, finalDatatype, tl, selectedGlobalID[0], selectedLocalID[0], 0, repeaterItemPosition);

                                                if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                                    if (answerSet.size() != 0) {
                                                        ;
                                                        previewLayout.removeAllViews();
                                                        previewLayout.addView(titleTextView);
                                                        for (int i = 0; i < answerSet.size(); i++) {
                                                            JSONObject currentAnswer = answerSet.get(i);

                                                            if (currentAnswer != null) {
                                                                int currentQuestionId = currentAnswer.optInt("question_id");
                                                                int answerJsonVal = currentAnswer.optInt("answer");

                                                                if (currentQuestionId == questionId) {
                                                                    Integer globalMetricIdAnswer = null;
                                                                    Integer localMetricIdAnswer = null;
                                                                    if (currentAnswer.has("globalMetricId")) {
                                                                        globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                                    }
                                                                    if (currentAnswer.has("localMetricId")) {
                                                                        localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                                    }
                                                                    if (localMetricIdAnswer != 0) {
                                                                        TextView answerTextView = new TextView(context);
                                                                        answerTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                                                        ));
                                                                        String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                                                        String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                                        String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                                        String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                                                    answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                                                        if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                                            try {
                                                                                // Perform your calculations and set the text

                                                                                if (canParseToDouble(answerValue)) {
                                                                                    double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                                                    answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                                                    previewLayout.addView(answerTextView);
                                                                                    previewLayout.setVisibility(View.VISIBLE);
                                                                                }
                                                                            } catch (
                                                                                    NumberFormatException e) {
                                                                                // Handle the case where parsing fails
                                                                                e.printStackTrace();
                                                                            }
                                                                        } else {
                                                                            // Handle the case where any of the variables is null
                                                                            contentTextView.setText("Some values are null");
                                                                        }
                                                                    } else {
                                                                        previewLayout.setVisibility(View.GONE);
                                                                    }

                                                                } else {
                                                                    previewLayout.setVisibility(View.GONE);
                                                                }

                                                            }
                                                        }
                                                    } else {
                                                        previewLayout.setVisibility(View.GONE);
                                                        previewLayout.removeAllViews();
                                                    }
                                                }

                                            }
                                        });

                                    }
                                }

                            } else {

                                // Remove square brackets and split the string into an array of strings
                                String[] stringArray = assignmentFields.substring(1, assignmentFields.length() - 1).split(",");

                                if (assignmentFields.equals("null") || assignmentFields.equals("[]")) {
                                    if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                        // generate row item
                                        TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                                        TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                                        TextView tv = (TextView) tr.findViewById(R.id.cbTitle);
                                        CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                                        String checkBoxName = dl.getValueByLookUpID(lookupID, questions, questionColumnSuffix);
                                        tvId.setText(lookupID);
                                        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                            tv.setText(checkBoxName + " " + roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId)));
                                        } else {
                                            tv.setText(checkBoxName);
                                        }
                                        tl.addView(tr);


                                        // initilaize default look up globally
                                        // pre-fill checkboxes that have been previously ticked
                                        if (cbAnswers.getCount() > 0) {
                                            // disable while loading the "Not Applicable"
                                            if (Integer.parseInt(lookupID) == defaultLookUpValue) {
                                                cb.setEnabled(false);
                                            }
                                            //End
                                            while (cbAnswers.moveToNext()) {
                                                if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                                    cb.setChecked(true);
                                                    cb.setEnabled(true);
                                                    checkboxCount[0]++;
                                                }
                                            }
                                            cbAnswers.moveToPosition(-1);
                                        } else {
                                            // N/A Question for franklin
                                            if (defaultLookUpValue != 0) {
                                                if (defaultLookUpValue == Integer.parseInt(lookupID)) {
                                                    cb.setChecked(true);
                                                    cb.setEnabled(true);
                                                }
                                            }
                                        }
                                        tr.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                                                checkbox.setChecked(!checkbox.isChecked());
                                                checkbox.callOnClick();
                                            }

                                        });

                                        //stick the answer in the question itself
                                        int finalDefaultLookUpValue = defaultLookUpValue;
                                        String finalDatatype = datatype;
                                        String finalMaxSelection = maxSelection;
                                        cb.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);
                                                if (checkbox.isChecked()) {
                                                    checkboxCount[0]++;
                                                } else {
                                                    checkboxCount[0]--;
                                                }

                                                if (singleSelect) {
                                                    int ssTableRows = tl.getChildCount();
                                                    for (int j = 0; j < ssTableRows; j++) {
                                                        TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                        CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                                        ssTcb.setChecked(false);
                                                    }
                                                    CheckBox clickedCb = (CheckBox) view;
                                                    clickedCb.setChecked(true);
                                                }

                                                // Update the Not applicable field if none of the value is selected
                                                if (finalDefaultLookUpValue != 0) {
                                                    int ssTableRows = tl.getChildCount();
                                                    boolean checkboxTicked = false;
                                                    int count = 0;
                                                    for (int j = 0; j < ssTableRows; j++) {
                                                        TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                        TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                        CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                        if (ssTcb.isChecked()) {
                                                            checkboxTicked = true;
                                                        }
                                                        if (Integer.parseInt(cbId.getText().toString()) != finalDefaultLookUpValue) {
                                                            if (ssTcb.isChecked()) {
                                                                count++;
                                                            }
                                                        }

                                                    }
                                                    if (!checkboxTicked) {
                                                        for (int j = 0; j < ssTableRows; j++) {
                                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                            if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                                ssTcb.setChecked(true);
                                                                ssTcb.setEnabled(true);
                                                            }
                                                        }
                                                    }

                                                    // Remove the Not applicable when other selected.
                                                    if (count > 0) {
                                                        for (int j = 0; j < ssTableRows; j++) {
                                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                            if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                                ssTcb.setChecked(false);
                                                                ssTcb.setEnabled(false);
                                                            }
                                                        }
                                                    }

                                                }
                                                if (finalMaxSelection != "null") {
                                                    if (checkboxCount[0] > Integer.parseInt(finalMaxSelection)) {
                                                        cb.setChecked(false);
                                                        checkboxCount[0]--;
                                                        Toast.makeText(context, "You can only select " + finalMaxSelection + " options", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                if (unCheckAllBox[0]) {
                                                    while (cbAnswers.moveToNext()) {
                                                        if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                                            cb.setChecked(false);
                                                            cb.setEnabled(false);
                                                            checkboxCount[0]++;
                                                        }
                                                    }
                                                }


                                                saveMultipleAnswerEntry(questionId, parentQuestionId, finalDatatype, tl, selectedGlobalID[0], selectedLocalID[0], 0, repeaterItemPosition);

                                                if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                                    if (answerSet.size() != 0) {
                                                        ;
                                                        previewLayout.removeAllViews();
                                                        previewLayout.addView(titleTextView);
                                                        for (int i = 0; i < answerSet.size(); i++) {
                                                            JSONObject currentAnswer = answerSet.get(i);

                                                            if (currentAnswer != null) {
                                                                int currentQuestionId = currentAnswer.optInt("question_id");
                                                                int answerJsonVal = currentAnswer.optInt("answer");

                                                                if (currentQuestionId == questionId) {
                                                                    Integer globalMetricIdAnswer = null;
                                                                    Integer localMetricIdAnswer = null;
                                                                    if (currentAnswer.has("globalMetricId")) {
                                                                        globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                                    }
                                                                    if (currentAnswer.has("localMetricId")) {
                                                                        localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                                    }
                                                                    if (localMetricIdAnswer != 0) {
                                                                        TextView answerTextView = new TextView(context);
                                                                        answerTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                                                        ));
                                                                        String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                                                        String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                                        String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                                        String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                                                    answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                                                        if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                                            try {
                                                                                // Perform your calculations and set the text

                                                                                if (canParseToDouble(answerValue)) {
                                                                                    double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                                                    answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                                                    previewLayout.addView(answerTextView);
                                                                                    previewLayout.setVisibility(View.VISIBLE);
                                                                                }
                                                                            } catch (
                                                                                    NumberFormatException e) {
                                                                                // Handle the case where parsing fails
                                                                                e.printStackTrace();
                                                                            }
                                                                        } else {
                                                                            // Handle the case where any of the variables is null
                                                                            contentTextView.setText("Some values are null");
                                                                        }
                                                                    } else {
                                                                        previewLayout.setVisibility(View.GONE);
                                                                    }

                                                                } else {
                                                                    previewLayout.setVisibility(View.GONE);
                                                                }

                                                            }
                                                        }
                                                    } else {
                                                        previewLayout.setVisibility(View.GONE);
                                                        previewLayout.removeAllViews();
                                                    }
                                                }

                                            }
                                        });

                                    }
                                } else {
                                    // Convert the string array to an integer array
                                    int[] intArray = new int[stringArray.length];
                                    for (int m = 0; m < stringArray.length; m++) {
                                        intArray[m] = Integer.parseInt(stringArray[m]);
                                    }

                                    // Iterate through the integer array using a for loop
                                    for (int fieldMultipleAssignment : intArray) {

                                        String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                        if (String.valueOf(fieldMultipleAssignment).equals(farmerAssignmentField) || assignmentType.equals("null") && String.valueOf(fieldMultipleAssignment).equals("null")) {

                                            if (isActive.toLowerCase().equals("true") || isActive.equals("")) {
                                                // generate row item
                                                TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                                                TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                                                TextView tv = (TextView) tr.findViewById(R.id.cbTitle);
                                                CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                                                String checkBoxName = dl.getValueByLookUpID(lookupID, questions, questionColumnSuffix);
                                                tvId.setText(lookupID);
                                                if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                                    tv.setText(checkBoxName + " " + roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId)));
                                                } else {
                                                    tv.setText(checkBoxName);
                                                }
                                                tl.addView(tr);


                                                // initilaize default look up globally
                                                // pre-fill checkboxes that have been previously ticked
                                                if (cbAnswers.getCount() > 0) {
                                                    // disable while loading the "Not Applicable"
                                                    if (Integer.parseInt(lookupID) == defaultLookUpValue) {
                                                        cb.setEnabled(false);
                                                    }
                                                    //End
                                                    while (cbAnswers.moveToNext()) {
                                                        if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                                            cb.setChecked(true);
                                                            cb.setEnabled(true);
                                                            checkboxCount[0]++;
                                                        }
                                                    }
                                                    cbAnswers.moveToPosition(-1);
                                                } else {
                                                    // N/A Question for franklin
                                                    if (defaultLookUpValue != 0) {
                                                        if (defaultLookUpValue == Integer.parseInt(lookupID)) {
                                                            cb.setChecked(true);
                                                            cb.setEnabled(true);
                                                        }
                                                    }
                                                }
                                                tr.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                                                        checkbox.setChecked(!checkbox.isChecked());
                                                        checkbox.callOnClick();
                                                    }

                                                });

                                                //stick the answer in the question itself
                                                int finalDefaultLookUpValue = defaultLookUpValue;
                                                String finalDatatype = datatype;
                                                String finalMaxSelection = maxSelection;
                                                cb.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);
                                                        if (checkbox.isChecked()) {
                                                            checkboxCount[0]++;
                                                        } else {
                                                            checkboxCount[0]--;
                                                        }

                                                        if (singleSelect) {
                                                            int ssTableRows = tl.getChildCount();
                                                            for (int j = 0; j < ssTableRows; j++) {
                                                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                                                ssTcb.setChecked(false);
                                                            }
                                                            CheckBox clickedCb = (CheckBox) view;
                                                            clickedCb.setChecked(true);
                                                        }

                                                        // Update the Not applicable field if none of the value is selected
                                                        if (finalDefaultLookUpValue != 0) {
                                                            int ssTableRows = tl.getChildCount();
                                                            boolean checkboxTicked = false;
                                                            int count = 0;
                                                            for (int j = 0; j < ssTableRows; j++) {
                                                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                                TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                                if (ssTcb.isChecked()) {
                                                                    checkboxTicked = true;
                                                                }
                                                                if (Integer.parseInt(cbId.getText().toString()) != finalDefaultLookUpValue) {
                                                                    if (ssTcb.isChecked()) {
                                                                        count++;
                                                                    }
                                                                }

                                                            }
                                                            if (!checkboxTicked) {
                                                                for (int j = 0; j < ssTableRows; j++) {
                                                                    TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                                    CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                                    TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                                    if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                                        ssTcb.setChecked(true);
                                                                        ssTcb.setEnabled(true);
                                                                    }
                                                                }
                                                            }

                                                            // Remove the Not applicable when other selected.
                                                            if (count > 0) {
                                                                for (int j = 0; j < ssTableRows; j++) {
                                                                    TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                                    CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                                    TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                                    if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                                        ssTcb.setChecked(false);
                                                                        ssTcb.setEnabled(false);
                                                                    }
                                                                }
                                                            }

                                                        }
                                                        if (finalMaxSelection != "null") {
                                                            if (checkboxCount[0] > Integer.parseInt(finalMaxSelection)) {
                                                                cb.setChecked(false);
                                                                checkboxCount[0]--;
                                                                Toast.makeText(context, "You can only select " + finalMaxSelection + " options", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        if (unCheckAllBox[0]) {
                                                            while (cbAnswers.moveToNext()) {
                                                                if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupID)) {
                                                                    cb.setChecked(false);
                                                                    cb.setEnabled(false);
                                                                    checkboxCount[0]++;
                                                                }
                                                            }
                                                        }


                                                        saveMultipleAnswerEntry(questionId, parentQuestionId, finalDatatype, tl, selectedGlobalID[0], selectedLocalID[0], 0, repeaterItemPosition);

                                                        if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                                                            if (answerSet.size() != 0) {
                                                                ;
                                                                previewLayout.removeAllViews();
                                                                previewLayout.addView(titleTextView);
                                                                for (int i = 0; i < answerSet.size(); i++) {
                                                                    JSONObject currentAnswer = answerSet.get(i);

                                                                    if (currentAnswer != null) {
                                                                        int currentQuestionId = currentAnswer.optInt("question_id");
                                                                        int answerJsonVal = currentAnswer.optInt("answer");

                                                                        if (currentQuestionId == questionId) {
                                                                            Integer globalMetricIdAnswer = null;
                                                                            Integer localMetricIdAnswer = null;
                                                                            if (currentAnswer.has("globalMetricId")) {
                                                                                globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                                                            }
                                                                            if (currentAnswer.has("localMetricId")) {
                                                                                localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                                                            }
                                                                            if (localMetricIdAnswer != 0) {
                                                                                TextView answerTextView = new TextView(context);
                                                                                answerTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                                                ));
                                                                                String globalAbb = roomDB.globalUnitDao().getAbbByGlobalID(Integer.valueOf(metricGlobalId));
                                                                                String localAbb = roomDB.localUnitDao().getAbbByLocalID(localMetricIdAnswer);
                                                                                String localConversion = roomDB.localUnitDao().getFactorByLocalID(localMetricIdAnswer);
                                                                                String answerValue = dl.getAnswerValuebylookUpId(answerJsonVal);
//                                                                    answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(Double.parseDouble(answerValue) * Double.parseDouble(localConversion))  + " " + localAbb);
                                                                                if (answerValue != null && globalAbb != null && localConversion != null && localAbb != null) {
                                                                                    try {
                                                                                        // Perform your calculations and set the text

                                                                                        if (canParseToDouble(answerValue)) {
                                                                                            double result = Double.parseDouble(answerValue) * Double.parseDouble(localConversion);
                                                                                            answerTextView.setText(answerValue + " " + globalAbb + " = " + String.valueOf(result) + " " + localAbb);
                                                                                            previewLayout.addView(answerTextView);
                                                                                            previewLayout.setVisibility(View.VISIBLE);
                                                                                        }
                                                                                    } catch (
                                                                                            NumberFormatException e) {
                                                                                        // Handle the case where parsing fails
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                } else {
                                                                                    // Handle the case where any of the variables is null
                                                                                    contentTextView.setText("Some values are null");
                                                                                }
                                                                            } else {
                                                                                previewLayout.setVisibility(View.GONE);
                                                                            }

                                                                        } else {
                                                                            previewLayout.setVisibility(View.GONE);
                                                                        }

                                                                    }
                                                                }
                                                            } else {
                                                                previewLayout.setVisibility(View.GONE);
                                                                previewLayout.removeAllViews();
                                                            }
                                                        }

                                                    }
                                                });

                                            }
                                        }
                                    }
                                }

                                // end of multiple assignment
                            }


                        }
                    } else {
                        while (checkboxes.moveToNext()) {
                            // generate row item
                            TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                            TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                            TextView tv = (TextView) tr.findViewById(R.id.cbTitle);

                            final int cbId = checkboxes.getInt(checkboxes.getColumnIndex("_id"));

                            tvId.setText(String.valueOf(cbId));

                            int columnIndex = (checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);
                            tv.setText(checkboxes.getString(columnIndex));

                            CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);
                            tl.addView(tr);
                            // initilaize default look up globally
                            // pre-fill checkboxes that have been previously ticked
                            if (cbAnswers.getCount() > 0) {
                                // disable while loading the "Not Applicable"
                                if (cbId == defaultLookUpValue) {
                                    cb.setEnabled(false);
                                }
                                //End
                                while (cbAnswers.moveToNext()) {
                                    if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == cbId) {
                                        cb.setChecked(true);
                                        cb.setEnabled(true);
                                    }
                                }
                                cbAnswers.moveToPosition(-1);
                            } else {
                                // N/A Question for franklin
                                if (defaultLookUpValue != 0) {
                                    if (defaultLookUpValue == cbId) {
                                        cb.setChecked(true);
                                        cb.setEnabled(true);
                                    }
                                }
                            }


                            tr.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                                    checkbox.setChecked(!checkbox.isChecked());
                                    checkbox.callOnClick();
                                }

                            });

                            //stick the answer in the question itself
                            int finalDefaultLookUpValue = defaultLookUpValue;
                            String finalDatatype1 = datatype;
                            cb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (singleSelect) {
                                        int ssTableRows = tl.getChildCount();
                                        for (int j = 0; j < ssTableRows; j++) {
                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                            ssTcb.setChecked(false);
                                        }
                                        CheckBox clickedCb = (CheckBox) view;
                                        clickedCb.setChecked(true);
                                    }

                                    // Update the Not applicable field if none of the value is selected
                                    if (finalDefaultLookUpValue != 0) {
                                        int ssTableRows = tl.getChildCount();
                                        boolean checkboxTicked = false;
                                        int count = 0;
                                        for (int j = 0; j < ssTableRows; j++) {
                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                            if (ssTcb.isChecked()) {
                                                checkboxTicked = true;
                                            }
                                            if (Integer.parseInt(cbId.getText().toString()) != finalDefaultLookUpValue) {
                                                if (ssTcb.isChecked()) {
                                                    count++;
                                                }
                                            }

                                        }
                                        if (!checkboxTicked) {
                                            for (int j = 0; j < ssTableRows; j++) {
                                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                    ssTcb.setChecked(true);
                                                    ssTcb.setEnabled(true);
                                                }
                                            }
                                        }

                                        // Remove the Not applicable when other selected.
                                        if (count > 0) {
                                            for (int j = 0; j < ssTableRows; j++) {
                                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                                TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                                if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                    ssTcb.setChecked(false);
                                                    ssTcb.setEnabled(false);
                                                }
                                            }
                                        }

                                    }

                                    saveMultipleAnswerEntry(questionId, parentQuestionId, finalDatatype1, tl, selectedGlobalID[0], selectedLocalID[0], 0, repeaterItemPosition);
                                }
                            });


                        }
                    }

                } else {

                    while (checkboxes.moveToNext()) {
                        // generate row item
                        TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                        TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                        TextView tv = (TextView) tr.findViewById(R.id.cbTitle);

                        final int cbId = checkboxes.getInt(checkboxes.getColumnIndex("_id"));

                        tvId.setText(String.valueOf(cbId));

                        int columnIndex = (checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);
                        tv.setText(checkboxes.getString(columnIndex));

                        CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);
                        tl.addView(tr);
                        // initilaize default look up globally
                        // pre-fill checkboxes that have been previously ticked
                        if (cbAnswers.getCount() > 0) {
                            // disable while loading the "Not Applicable"
                            if (cbId == defaultLookUpValue) {
                                cb.setEnabled(false);
                            }
                            //End
                            while (cbAnswers.moveToNext()) {
                                if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == cbId) {
                                    cb.setChecked(true);
                                    cb.setEnabled(true);
                                }
                            }
                            cbAnswers.moveToPosition(-1);
                        } else {
                            // N/A Question for franklin
                            if (defaultLookUpValue != 0) {
                                if (defaultLookUpValue == cbId) {
                                    cb.setChecked(true);
                                    cb.setEnabled(true);
                                }
                            }
                        }


                        tr.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                                checkbox.setChecked(!checkbox.isChecked());
                                checkbox.callOnClick();
                            }

                        });

                        //stick the answer in the question itself
                        int finalDefaultLookUpValue = defaultLookUpValue;
                        String finalDatatype1 = datatype;
                        cb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (singleSelect) {
                                    int ssTableRows = tl.getChildCount();
                                    for (int j = 0; j < ssTableRows; j++) {
                                        TableRow ssTr = (TableRow) tl.getChildAt(j);
                                        CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                        ssTcb.setChecked(false);
                                    }
                                    CheckBox clickedCb = (CheckBox) view;
                                    clickedCb.setChecked(true);
                                }

                                // Update the Not applicable field if none of the value is selected
                                if (finalDefaultLookUpValue != 0) {
                                    int ssTableRows = tl.getChildCount();
                                    boolean checkboxTicked = false;
                                    int count = 0;
                                    for (int j = 0; j < ssTableRows; j++) {
                                        TableRow ssTr = (TableRow) tl.getChildAt(j);
                                        TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                        CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                        if (ssTcb.isChecked()) {
                                            checkboxTicked = true;
                                        }
                                        if (Integer.parseInt(cbId.getText().toString()) != finalDefaultLookUpValue) {
                                            if (ssTcb.isChecked()) {
                                                count++;
                                            }
                                        }

                                    }
                                    if (!checkboxTicked) {
                                        for (int j = 0; j < ssTableRows; j++) {
                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                            if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                ssTcb.setChecked(true);
                                                ssTcb.setEnabled(true);
                                            }
                                        }
                                    }

                                    // Remove the Not applicable when other selected.
                                    if (count > 0) {
                                        for (int j = 0; j < ssTableRows; j++) {
                                            TableRow ssTr = (TableRow) tl.getChildAt(j);
                                            CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);
                                            TextView cbId = (TextView) ssTr.findViewById(R.id.cbId);
                                            if (Integer.parseInt(cbId.getText().toString()) == finalDefaultLookUpValue) {
                                                ssTcb.setChecked(false);
                                                ssTcb.setEnabled(false);
                                            }
                                        }
                                    }

                                }

                                saveMultipleAnswerEntry(questionId, parentQuestionId, finalDatatype1, tl, selectedGlobalID[0], selectedLocalID[0], 0, repeaterItemPosition);
                            }
                        });


                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                if (answerSet != null) {
                    for (int i = 0; i < answerSet.size(); i++) {
                        JSONObject currentAnswer = answerSet.get(i);

                        if (currentAnswer != null) {
                            int currentQuestionId = currentAnswer.optInt("question_id");

                            if (currentQuestionId == questionId) {
                                int globalMetricIdAnswer = currentAnswer.optInt("globalMetricId");
                                int localMetricIdAnswer = currentAnswer.optInt("localMetricId");
                                contentTextView.setText(globalMetricIdAnswer);

                            }

                        }
                    }
                }
            }

            if (metricGlobalId != null && !metricGlobalId.equals("") && !metricGlobalId.equals("0")) {
                innerWrapper.addView(text);
                innerWrapper.addView(spinner);
                questionWrapper.addView(tl);
                questionWrapper.addView(innerWrapper);
                questionWrapper.addView(previewLayout);
                parentLayout.addView(questionWrapper);
            } else {
                questionWrapper.addView(tl);
                parentLayout.addView(questionWrapper);
            }


            saveMultipleAnswerEntry(questionId, parentQuestionId, datatype, tl, selectedGlobalID[0], selectedLocalID[0], 0, repeaterItemPosition);
        }
    }


    // populate the child answers from dropdown and show
    public void renderCheckBoxQuestionChild(Cursor questions, LinearLayout parentLayout, long farmerId, long farmerModuleId) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(questions, false, questionWrapper);

//        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));


        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        }
        if (oldDatatype != null) {
            if (oldDatatype.equals("Checkbox")) {
                datatype = "int";
            }
        }


        final boolean singleSelect;
        boolean tempSingleSelect = false;

        Cursor checkboxes = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);

        Cursor cbAnswers = dl.getAnswersForQuestionByChild(questionId, farmerId, Utilities.stakeholderId, farmerModuleId);

        // is this a question we can only select one answer to?
        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting checkbox display config " + e.getMessage());
        }

        singleSelect = tempSingleSelect;

        if (checkboxes != null && checkboxes.getCount() > 0) {
            // add the table layout to the screen
            final TableLayout tl = (TableLayout) inflater.inflate(R.layout.module_checkbox_table, questionWrapper, false);
            tl.setSaveEnabled(true);

            while (checkboxes.moveToNext()) {
                // generate row item
                TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                TextView tv = (TextView) tr.findViewById(R.id.cbTitle);

                final int cbId = checkboxes.getInt(checkboxes.getColumnIndex("_id"));

                tvId.setText(String.valueOf(cbId));

                int columnIndex = (checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);
                tv.setText(checkboxes.getString(columnIndex));

                CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);
                tl.addView(tr);

                // pre-fill checkboxes that have been previously ticked
                if (hasAnswers && cbAnswers.getCount() > 0) {
                    while (cbAnswers.moveToNext()) {
                        if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == cbId) {
                            cb.setChecked(true);
                        }
                    }
                    cbAnswers.moveToPosition(-1);
                }

                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                        checkbox.setChecked(!checkbox.isChecked());
                        checkbox.callOnClick();
                    }

                });

                //stick the answer in the question itself
                String finalDatatype = datatype;
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (singleSelect) {
                            int ssTableRows = tl.getChildCount();
                            for (int j = 0; j < ssTableRows; j++) {
                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                ssTcb.setChecked(false);
                            }
                            CheckBox clickedCb = (CheckBox) view;
                            clickedCb.setChecked(true);
                        }
                        saveMultipleAnswerEntry(questionId, 0, finalDatatype, tl, 0, 0, 0,-1);
                    }
                });


            }

            questionWrapper.addView(tl);
            parentLayout.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl, 0, 0, 0,-1);
        }
    }


    void renderCheckBoxQuestionSubQuestion(Cursor questions, LinearLayout parentLayout, long farmerId, String stakeholderId, long farmerModuleId, final int parentid) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        // renderQuestion(questions, false, questionWrapper);

        String datatypeduplicate = "";
        int questionIddouplicate = 0;
        if (questions != null) {
            if (questions.moveToFirst()) {
                do {

                    datatypeduplicate = questions.getString(questions.getColumnIndex("datatype_name"));

                    questionIddouplicate = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                } while (questions.moveToNext());
            }
        }

        final int questionId = questionIddouplicate;
        final String datatype = datatypeduplicate;
        final boolean singleSelect;
        boolean tempSingleSelect = false;

        Cursor checkboxes = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);

        // is this a question we can only select one answer to?
        // get display config


        try {
            JSONObject displayConfig = new JSONObject();
            if (questions != null) {
                if (questions.moveToFirst()) {
                    do {
                        displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                    } while (questions.moveToNext());
                }
            }

            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting checkbox display config " + e.getMessage());
        }

        singleSelect = tempSingleSelect;

        if (checkboxes.getCount() > 0) {
            // add the table layout to the screen
            final TableLayout tl = (TableLayout) inflater.inflate(R.layout.module_checkbox_table, questionWrapper, false);
            tl.setSaveEnabled(true);
            Cursor cursor = dl.getParentSubquestionAnswer(Utilities.subModuleId, parentid, questionId);

            while (checkboxes.moveToNext()) {
                // generate row item
                TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                TextView tvId = tr.findViewById(R.id.cbId);
                TextView tv = tr.findViewById(R.id.cbTitle);

                final int cbId = checkboxes.getInt(checkboxes.getColumnIndex("_id"));


                tvId.setText(String.valueOf(cbId));

                int columnIndex = (checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);
                tv.setText(checkboxes.getString(columnIndex));

                CheckBox cb = tr.findViewById(R.id.cbCheckBox);
                tl.addView(tr);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            if (cbId == Integer.parseInt(cursor.getString(cursor.getColumnIndex("question_answer")))) {
                                cb.setChecked(true);
                            }
                        } while (cursor.moveToNext());
                    }
                }

                // pre-fill checkboxes that have been previously ticked
                /*if (hasAnswers && cbAnswers.getCount() > 0) {
                    while (cbAnswers.moveToNext()) {
                        if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == cbId) {
                            cb.setChecked(true);
                        }
                    }
                    cbAnswers.moveToPosition(-1);
                }*/


                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                        checkbox.setChecked(!checkbox.isChecked());
                        checkbox.callOnClick();
                    }

                });

                //stick the answer in the question itself
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (singleSelect) {
                            int ssTableRows = tl.getChildCount();
                            for (int j = 0; j < ssTableRows; j++) {
                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                ssTcb.setChecked(false);
                            }
                            CheckBox clickedCb = (CheckBox) view;
                            clickedCb.setChecked(true);
                        }
                        saveMultipleAnswerEntryForSUbQuestionAnswerSave(questionId, parentid, datatype, tl);
                    }
                });


            }

            questionWrapper.addView(tl);
            parentLayout.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl, 0, 0, 0, -1);
        }
    }

    void renderPhotoCheckboxQuestiontest(Cursor questions, final LinearLayout parentLayout, long farmerId, String stakeholderId, long farmerModuleId, Resources res, String packageName) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);


        String datatypeduplicate = "";
        int questionIddouplicate = 0;
        if (questions != null) {
            if (questions.moveToFirst()) {
                do {
                    datatypeduplicate = questions.getString(questions.getColumnIndex("datatype_name"));

                    questionIddouplicate = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                } while (questions.moveToNext());
            }
        }

        final int questionId = questionIddouplicate;
        final String datatype = datatypeduplicate;

        final boolean singleSelect;
        boolean tempSingleSelect = false;

        Cursor photoCheckboxes = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);
        Cursor photoCbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId, stakeholderId, farmerModuleId);

        // is this a question we can only select one answer to?
        // get display config
        try {


            JSONObject displayConfig = new JSONObject();

            if (questions != null) {
                if (questions.moveToFirst()) {
                    do {
                        displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));

                    } while (questions.moveToNext());
                }
            }
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting photo checkbox display config " + e.getMessage());
        }

        singleSelect = tempSingleSelect;

        if (photoCheckboxes.getCount() > 0) {
            // add the table layout to the screen
            final TableLayout tl = (TableLayout) inflater.inflate(R.layout.module_photo_table, questionWrapper, false);

            while (photoCheckboxes.moveToNext()) {
                // generate row item
                TableRow tr = (TableRow) inflater.inflate(R.layout.module_photo_row, tl, false);

                TextView tvId = tr.findViewById(R.id.cbId);
                TextView tv = tr.findViewById(R.id.cbTitle);
                ImageView iv = tr.findViewById(R.id.photo);

                int cbId = photoCheckboxes.getInt(photoCheckboxes.getColumnIndex("_id"));

                tvId.setText(String.valueOf(cbId));

                int columnIndex = (photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);
                tv.setText(photoCheckboxes.getString(columnIndex));

                int imageId = res.getIdentifier(photoCheckboxes.getString(photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_PHOTO)), "drawable", packageName);
                if (imageId != 0) {
                    iv.setImageDrawable(ContextCompat.getDrawable(context, imageId));
                } else {
                    iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.photo_checkbox_no_image));
                }

                final CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                // pre-fill checkboxes that have been previously ticked
                if (photoCbAnswers.getCount() > 0) {
                    while (photoCbAnswers.moveToNext()) {
                        if (photoCbAnswers.getInt(photoCbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == cbId) {
                            cb.setChecked(true);
                        }
                    }
                    photoCbAnswers.moveToPosition(-1);
                }

                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cb.setChecked(!cb.isChecked());
                        cb.callOnClick();
                    }

                });

                //stick the answer in the question itself
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (singleSelect) {
                            int ssTableRows = tl.getChildCount();
                            for (int j = 0; j < ssTableRows; j++) {
                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                ssTcb.setChecked(false);
                            }
                            CheckBox clickedCb = (CheckBox) view;
                            clickedCb.setChecked(true);
                        }

                        saveMultipleAnswerEntry(questionId, 0, datatype, tl, 0, 0, 0, -1);
                    }
                });

                tl.addView(tr);
            }

            questionWrapper.addView(tl);
            parentLayout.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl, 0, 0, 0, -1);
        }

    }


    public void renderPhotoCheckboxQuestion(Cursor questions, long farmerId) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        String datatype = "int";
        Cursor photoCheckboxes = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);
        Cursor photoCbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId);
        final int[] checkboxCount = {0};
        if (photoCheckboxes.getCount() > 0) {
            // add the table layout to the screen
            final TableLayout tl = (TableLayout) inflater.inflate(R.layout.module_photo_table, questionWrapper, false);
            try {
                JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                if (displayConfig.has("display")) {
                    JSONObject display = displayConfig.getJSONObject("display");
                    boolean singleSelect = display.optBoolean("single_select");
                    if (display.has("lookupProperties")) {
                        int maxSelection = display.optInt("maxSelection");
                        JSONArray range = display.getJSONArray("lookupProperties");
                        for (int i = 0; i < range.length(); i++) {
                            JSONObject rangeArray = range.getJSONObject(i);
                            String lookupID = rangeArray.getString("lookupValueId");
                            String assignmentType = rangeArray.getString("assignmentType");
                            String assignmentField = rangeArray.getString("assignmentField");
                            String assignmentFields = rangeArray.getString("assignmentFields");
                            String isActive = rangeArray.optString("active");
                            if (!assignmentField.equals("null")) {

                                String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));

                                if (assignmentField.equals(farmerAssignmentField)) {
                                    if (isActive.equalsIgnoreCase("true") || isActive.isEmpty()) {
                                        TableRow tr = addPhotoCheckboxTableRow(tl, lookupID, questions, questionId);
                                        addPhotoCheckboxView(tl, tr, checkboxCount, datatype, photoCbAnswers, lookupID, maxSelection, singleSelect, questionId);
                                    }
                                }

                            } else {
                                // Remove square brackets and split the string into an array of strings
                                String[] stringArray = assignmentFields.substring(1, assignmentFields.length() - 1).split(",");
                                if (assignmentFields.equals("null") || assignmentFields.equals("[]")) {
                                    if (isActive.equalsIgnoreCase("true") || isActive.isEmpty()) {
                                        TableRow tr = addPhotoCheckboxTableRow(tl, lookupID, questions, questionId);
                                        addPhotoCheckboxView(tl, tr, checkboxCount, datatype,photoCbAnswers, lookupID, maxSelection, singleSelect, questionId);
                                    }
                                } else {
                                    // Convert the string array to an integer array
                                    int[] intArray = new int[stringArray.length];
                                    for (int m = 0; m < stringArray.length; m++) {
                                        intArray[m] = Integer.parseInt(stringArray[m]);
                                    }
                                    // Iterate through the integer array using a for loop
                                    for (int fieldMultipleAssignment : intArray) {
                                        String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(assignmentType));
                                        if (String.valueOf(fieldMultipleAssignment).equals(farmerAssignmentField) || assignmentType.equals("null") && String.valueOf(fieldMultipleAssignment).equals("null")) {
                                            if (isActive.equalsIgnoreCase("true") || isActive.isEmpty()) {
                                                TableRow tr = addPhotoCheckboxTableRow(tl, lookupID, questions, questionId);
                                                addPhotoCheckboxView(tl, tr, checkboxCount, datatype,photoCbAnswers, lookupID, maxSelection, singleSelect, questionId);
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            questionWrapper.addView(tl);
            questionsSection.addView(questionWrapper);
            saveMultipleAnswerEntry(questionId, 0, datatype, tl, 0, 0, 0, -1);
        }

    }

    /**
     * Add the Table Row containing Checkbox and its related view to the Table
     */
    private TableRow addPhotoCheckboxTableRow(TableLayout tl, String lookupId , Cursor questions, int questionId ){
            String checkBoxName = dl.getValueByLookUpID(lookupId, questions, questionColumnSuffix);
            String photoLocation = roomDB.photoCheckBoxDataDao().getPhotoLocation(lookupId, questionId);

            TableRow tr = (TableRow) inflater.inflate(R.layout.module_photo_row, tl, false);
            TextView tvId = tr.findViewById(R.id.cbId);
            TextView tv = tr.findViewById(R.id.cbTitle);
            ImageView iv = tr.findViewById(R.id.photo);
            tvId.setText(lookupId);
            tv.setText(checkBoxName);
            if (photoLocation != null) {
                File imageFile = new File(photoLocation);
                if (imageFile.exists()) {
                    iv.setImageBitmap(bitmapImageResize(imageFile));
                }
            }
            tl.addView(tr);
            return tr;
    }

    /**
     * Add the checkbox checked change event listener and the row item click event listener
     */
    private void addPhotoCheckboxView(TableLayout tl, TableRow tr, int[] checkboxCount, String dataType, Cursor photoCbAnswers, String lookupId, int maxSelection, boolean singleSelect, int questionId) {
        CheckBox cb = tr.findViewById(R.id.cbCheckBox);
        if (photoCbAnswers.getCount() > 0) {
            while (photoCbAnswers.moveToNext()) {
                if (photoCbAnswers.getInt(photoCbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == Integer.parseInt(lookupId)) {
                    cb.setChecked(true);
                }
            }
            photoCbAnswers.moveToPosition(-1);
        }

        tr.setOnClickListener(view -> {
            cb.setChecked(!cb.isChecked());
        });

        cb.setOnCheckedChangeListener((compoundButton, b) -> {
           onPhotoCheckboxCheckChanged(compoundButton, checkboxCount, singleSelect, tl, cb, maxSelection);
           saveMultipleAnswerEntry(questionId, 0,dataType, tl, 0,0,0,-1);

        });
    }

    /**
     * On Photocheckbox Checked or unchecked
     */
    private void onPhotoCheckboxCheckChanged(CompoundButton compoundButton, int[] checkboxCount, boolean singleSelect, TableLayout tl, CheckBox cb, int maxSelection){
        if (compoundButton.isChecked()) {
            checkboxCount[0]++;
        } else {
            checkboxCount[0]--;
        }
        if(singleSelect) {
            for (int j = 0; j < tl.getChildCount(); j++) {
                TableRow ssTr = (TableRow) tl.getChildAt(j);
                CheckBox ssTcb = ssTr.findViewById(R.id.cbCheckBox);
                ssTcb.setChecked(false);
            }
            cb.setChecked(true);
        }
        if (checkboxCount[0] > maxSelection) {
            cb.setChecked(false);
            checkboxCount[0]--;
            Toast.makeText(context, "You can only select " + maxSelection + " options", Toast.LENGTH_SHORT).show();
        }

    }

    private Bitmap bitmapImageResize(File imageFile){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        int reqWidth = 500;
        int reqHeight = 500;

        Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 200, 200, false);

        float aspectRatio = (float) resizedBitmap.getWidth() / (float) resizedBitmap.getHeight();
        int newWidth;
        int newHeight;

        if (aspectRatio > 1) {
            newWidth = reqWidth;
            newHeight = Math.round(reqWidth / aspectRatio);
        } else {
            newHeight = reqHeight;
            newWidth = Math.round(reqHeight * aspectRatio);
        }
        return Utilities.loadSizedBitmap(imageFile.getAbsolutePath(),newWidth,newHeight);
    }

    public void renderRepeaterQuestion(Cursor questions, final long farmerId, String stakeholderId, final String olamFarmerId, final long farmerModuleId, final Resources res, boolean isSavedAns) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);
        resources = res;
//        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));

        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        }
        if (oldDatatype != null) {
            if (oldDatatype.equals("Repeater")) {
                datatype = "int";
            }
        }


        final int answerId = (hasAnswers) ? questions.getInt(questions.getColumnIndex("answer_id")) : 0;
        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, questionWrapper, false);

        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                int maxrepeats = display.getInt("maxrepeats");
                final boolean zeroAcceptable = (display.has("zeroacceptable") && (display.getBoolean("zeroacceptable")));

                ArrayList<String> repeatOptions = new ArrayList<>(maxrepeats);
                if (zeroAcceptable){
                    for (int i = 0; i <= maxrepeats; i++) {
                        repeatOptions.add(String.valueOf(i));
                    }
                } else {
                    for (int i = 1; i <= maxrepeats; i++) {
                        repeatOptions.add(String.valueOf(i));
                    }
                }
                /*for (int i = 1; i <= maxrepeats; i++) {
                 *//*if (zeroAcceptable){
                        if (!repeatOptions.contains("0")){
                            repeatOptions.add("0");
                        }
                    }*//*
                    repeatOptions.add(String.valueOf(i));
                }*/
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, repeatOptions);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(adapter);

                if (hasAnswers) {
                    int answer = (!questions.isNull(questions.getColumnIndex("answer_" + datatype))) ? questions.getInt(questions.getColumnIndex("answer_" + datatype)) : 0;
                    spinner.setSelection(zeroAcceptable ? answer : answer - 1);
                }

                questionWrapper.addView(spinner);
                final LinearLayout repeaterItemsWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_container, questionWrapper, false);
                questionWrapper.addView(repeaterItemsWrapper);
                questionsSection.addView(questionWrapper);


                String finalDatatype = datatype;
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(!zeroAcceptable){
                            id +=1;
                        }
                        if (id > 0 || zeroAcceptable) {
                            int existingItemsSize = repeaterItemsWrapper.getChildCount();
                            removeRepeaterAnswers(questionId);
                            for (int j = 0; j < existingItemsSize; j++) {
                                removeRepeaterAnswer(questionId, j);
                            }
                            repeaterItemsWrapper.removeAllViews();
                            removeAnswerEntry(questionId, answerId);
                            saveAnswerEntry(questionId, answerId, 0, -1, finalDatatype, spinner.getSelectedItem().toString(), 0, 0, 0);
                            renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, stakeholderId, olamFarmerId, farmerModuleId, res, isSavedAns);
                            if (id > 0) {

                            } else {
                                removeRepeaterAnswers(questionId);
                                repeaterItemsWrapper.removeAllViews();
                            }
                        } else {
                            removeAnswerEntry(questionId, answerId);
                            removeRepeaterAnswers(questionId);
                            repeaterItemsWrapper.removeAllViews();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting repeater display config " + e.getMessage());

        }
    }

    void renderRepeaterQuestionSubQuestion(Cursor questions, final long farmerId, String stakeholderId, final String olamFarmerId, final long farmerModuleId, final Resources res, LinearLayout parentLayout) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        //renderQuestion(questions, false, questionWrapper);
        resources = res;
        String datatypeduplicate = "";
        int questionIddouplicate = 0;
        if (questions != null) {
            if (questions.moveToFirst()) {
                do {
                    datatypeduplicate = questions.getString(questions.getColumnIndex("datatype_name"));

                    questionIddouplicate = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                } while (questions.moveToNext());
            }
        }

        final int questionId = questionIddouplicate;
        final String datatype = datatypeduplicate;


        final int answerId = 0;
        // final int answerId = (hasAnswers) ? questions.getInt(questions.getColumnIndex("answer_id")) : 0;
        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, questionWrapper, false);

        // get display config
        try {


            int lookupvalue = 0;

            JSONObject displayConfig = new JSONObject();
            String columnNameduplicate = "";
            if (questions != null) {
                if (questions.moveToFirst()) {
                    do {
                        displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                        columnNameduplicate = (questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;

                        lookupvalue = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                    } while (questions.moveToNext());
                }
            }


            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                int maxrepeats = display.getInt("maxrepeats");
                ArrayList<String> repeatOptions = new ArrayList<>(maxrepeats);
                for (int i = 0; i < maxrepeats; i++) {
                    repeatOptions.add(String.valueOf(i));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, repeatOptions);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(adapter);

                final boolean zeroAcceptable = (display.has("zeroacceptable") && (display.getBoolean("zeroacceptable")));

                /*if (hasAnswers) {
                    int answer = (!questions.isNull(questions.getColumnIndex("answer_" + datatype))) ? questions.getInt(questions.getColumnIndex("answer_" + datatype)) : 0;
                    spinner.setSelection(answer);
                }*/

                questionWrapper.addView(spinner);
                final LinearLayout repeaterItemsWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_container, questionWrapper, false);
                questionWrapper.addView(repeaterItemsWrapper);
                parentLayout.addView(questionWrapper);


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (id > 0 || zeroAcceptable) {
                            saveAnswerEntry(questionId, answerId, 0, -1, datatype, spinner.getSelectedItem().toString(), 0, 0, 0);
                            if (id > 0) {
                                renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, stakeholderId, olamFarmerId, farmerModuleId, res, false);
                            } else {
                                removeRepeaterAnswers(questionId);
                                repeaterItemsWrapper.removeAllViews();
                            }
                        } else {
                            removeAnswerEntry(questionId, answerId);
                            removeRepeaterAnswers(questionId);
                            repeaterItemsWrapper.removeAllViews();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting repeater display config " + e.getMessage());
        }
    }

    private String repeaterItemDisplayType(Cursor repeaterQuestions) {
        String displayType = "";
        try {
            JSONObject displayConfig = new JSONObject(repeaterQuestions.getString(repeaterQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                if (display.has("repeateritem_display_type")) {
                    displayType = display.getString("repeateritem_display_type");
                }
            }
        } catch (JSONException e) {

            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting repeater item type display config " + e.getMessage());

        }
        return displayType;
    }

    private boolean repeaterItemMobileVisibility(Cursor repeaterQuestions) {
        boolean displayType = true;
        try {
            JSONObject displayConfig = new JSONObject(repeaterQuestions.getString(repeaterQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                if (display.has("mobile_visibility")) {
                    displayType = display.getBoolean("mobile_visibility");
                }
            }
        } catch (JSONException e) {

            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting repeater item type display config " + e.getMessage());


        }
        return displayType;
    }

    private void renderRepeaterItems(int questionId, long repetitions, LinearLayout repeaterItemsWrapper, long farmerId, String stakeholderId, String olamFarmerId, long farmerModuleId, Resources res, boolean isSavedAns) {
        Cursor repeaterQuestions = dl.getRepeaterItemsForQuestion(questionId);
        // Cursor repeaterQuestions = dl.getRepeaterItemsForQuestion1(questionId);

        if (repetitions > 0) {
            final int existingItemsSize = repeaterItemsWrapper.getChildCount();
            if (repeaterQuestions.getCount() > 0 && repetitions > existingItemsSize) {
                for (int i = 1; i <= repetitions; i++) {

                    if (i > existingItemsSize) {
                        LinearLayout repeateritemWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_item, repeaterItemsWrapper, false);
                        LinearLayout repeaterItemsInner = (LinearLayout) repeateritemWrapper.findViewById(R.id.repeater_item);

                        // add a text view with the item number in it
                        TextView tv = (TextView) repeateritemWrapper.findViewById(R.id.repeater_item_number);
                        tv.setText(String.valueOf(i));
                        Boolean questionPresent = false;
                        while (repeaterQuestions.moveToNext()) {
                            if (repeaterItemMobileVisibility(repeaterQuestions)) {
                                String displayType = repeaterItemDisplayType(repeaterQuestions);
                                Cursor repeaterAnswers = null;
                                try {
                                    if (isSavedAns) {
                                        repeaterAnswers = dl.getRepeaterSavedAnswersForQuestionByPosition(repeaterQuestions.getInt(repeaterQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), i, farmerId, stakeholderId, farmerModuleId);
                                    } else {
                                        repeaterAnswers = dl.getRepeaterAnswersForQuestionByPosition(repeaterQuestions.getInt(repeaterQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), i, farmerId, stakeholderId, farmerModuleId);
                                    }
                                    if (repeaterAnswers.getCount() > 0) {
                                        repeaterAnswers.moveToFirst();
                                    }

                                    final int repeaterItemQuestionId = repeaterQuestions.getInt(repeaterQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                                    final String questionAssignmentType = dl.getQuestionAssignmentTypeId(repeaterItemQuestionId);
                                    final String questionAssignmentField = dl.getQuestionAssignmentFieldId(repeaterItemQuestionId);
                                    if (questionAssignmentField.equals("[]") || questionAssignmentField.equals("null")) {
                                        if (displayType.replaceAll(" ", "").toLowerCase().equals("datepicker")) {
                                            renderDatePickerQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, true);
                                            questionPresent = true;
                                        } else if (displayType.replaceAll(" ", "").toLowerCase().equals("decimal")) {
                                            renderTextBoxQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                                            questionPresent = true;
                                        } else if (displayType.replaceAll(" ", "").toLowerCase().equals("dropdown")) {
                                            renderDropDownQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                            questionPresent = true;
                                        } else if (displayType.replaceAll(" ", "").toLowerCase().equals("textbox")) {
                                            renderTextBoxQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                                            questionPresent = true;
                                        } else if (displayType.replaceAll(" ", "").toLowerCase().equals("yesno")) {
                                            renderYesNoQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                            questionPresent = true;
                                        } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photoupload")) {
                                            renderPhotoUploadQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                            questionPresent = true;
                                        } else if (displayType.replaceAll(" ", "").toLowerCase().equals("checkbox")) {
                                            renderCheckBoxQuestion(repeaterQuestions, repeaterItemsInner, farmerId, stakeholderId, farmerModuleId, false, true , i, repeaterAnswers, questionId);
                                            questionPresent = true;
                                        } else if (displayType.replace(" ", "").toLowerCase().equals("radio")) {
                                            renderRadioQuestion(repeaterQuestions, true, repeaterItemsInner, farmerId, stakeholderId, farmerModuleId, false, i, repeaterAnswers, questionId);
                                            questionPresent = true;
                                        } else {
                                            questionPresent = false;
                                        }
                                    } else {
                                        String[] stringArrayQuestion = questionAssignmentField.substring(1, questionAssignmentField.length() - 1).split(",");

                                        // Convert the string array to an integer array
                                        int[] intArrayQuestion = new int[stringArrayQuestion.length];
                                        for (int m = 0; m < stringArrayQuestion.length; m++) {
                                            intArrayQuestion[m] = Integer.parseInt(stringArrayQuestion[m]);
                                        }

                                        // Iterate through the integer array using a for loop
                                        for (int fieldMultipleAssignmentQuestion : intArrayQuestion) {
                                            String farmerAssignmentFieldQuestion = dl.getAssignmentByFarmerTypeID(farmerId, getColumnName(questionAssignmentType));

                                            if (String.valueOf(fieldMultipleAssignmentQuestion).equals(farmerAssignmentFieldQuestion)) {
                                                if (displayType.replaceAll(" ", "").toLowerCase().equals("datepicker")) {
                                                    renderDatePickerQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, true);
                                                    questionPresent = true;
                                                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("decimal")) {
                                                    renderTextBoxQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                                                    questionPresent = true;
                                                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("dropdown")) {
                                                    renderDropDownQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                                    questionPresent = true;
                                                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("textbox")) {
                                                    renderTextBoxQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                                                    questionPresent = true;
                                                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("yesno")) {
                                                    renderYesNoQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                                    questionPresent = true;
                                                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photoupload")) {
                                                    renderPhotoUploadQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                                    questionPresent = true;
                                                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("checkbox")) {
                                                    renderCheckBoxQuestion(repeaterQuestions, repeaterItemsInner, farmerId, stakeholderId, farmerModuleId, false, true , i, repeaterAnswers, questionId);
                                                    questionPresent = true;
                                                } else if (displayType.replace(" ", "").toLowerCase().equals("radio")) {
                                                    renderRadioQuestion(repeaterQuestions, true, repeaterItemsInner, farmerId, stakeholderId, farmerModuleId, false, i, repeaterAnswers, questionId);
                                                    questionPresent = true;
                                                } else {
                                                    questionPresent = false;
                                                }
                                            }
                                        }
                                    }

                                } finally {
                                    if (repeaterAnswers != null) repeaterAnswers.close();
                                }
                            }
                        }

                        if (questionPresent){
                            repeaterItemsWrapper.addView(repeateritemWrapper);
                        }
                        repeaterQuestions.moveToPosition(-1);
                    }
                }
            } else {
                if (repetitions > 0 && repetitions < existingItemsSize) {
                    for (int j = existingItemsSize; j > repetitions; j--) {
                        repeaterItemsWrapper.removeViewAt(j - 1);
                        removeRepeaterAnswer(questionId, j);
                    }
                }
            }
        }
    }

    private void renderRepeaterItemstest(String questionId, int parentid, LinearLayout repeaterItemsWrapper, long farmerId, String stakeholderId, String olamFarmerId, long farmerModuleId, Resources res, boolean selectCondition) {
        //Cursor repeaterQuestions = dl.getRepeaterItemsForQuestion(questionId);

        int i = repeaterItemsWrapper.getChildCount();
        for (; i > 2; i--) {
            repeaterItemsWrapper.removeViewAt(i - 1);

        }

        if (selectCondition) {
            for (String retval : questionId.split(",")) {
                int question_id = Integer.parseInt(retval);

                //logic for yes or no question
                // Cursor repeaterQuestions = dl.getRepeaterItemsForQuestion1(Integer.parseInt(retval));

                //logic for drop down question
                // Cursor repeaterQuestions = dl.getQuestionsAndAnswersBySubModuleId1(Utilities.subModuleId, Utilities.farmerId, Utilities.farmerModuleId, question_id);
                Cursor repeaterQuestions = dl.getQuestionsBySubModuleId1(question_id);


                LinearLayout repeateritemWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_item, repeaterItemsWrapper, false);
                //  repeateritemWrapper.removeAllViews();
                LinearLayout repeaterItemsInner = (LinearLayout) repeateritemWrapper.findViewById(R.id.repeater_item);

                // add a text view with the item number in it
                TextView tv = (TextView) repeateritemWrapper.findViewById(R.id.repeater_item_number);
                String displayType = "";

                if (repeaterQuestions != null) {
                    if (repeaterQuestions.moveToFirst()) {
                        do {
                            String dir = repeaterQuestions.getString(repeaterQuestions.getColumnIndex("question"));
                            tv.setText(dir);
                            displayType = repeaterQuestions.getString(repeaterQuestions.getColumnIndex("display_type"));
                        } while (repeaterQuestions.moveToNext());
                    }
                }
                repeaterItemsWrapper.addView(repeateritemWrapper);
                repeaterQuestions.moveToPosition(-1);

              /*  repeaterQuestions = dl.getRepeaterItemsForQuestion1(question_id);
                renderYesNoQuestiontest(repeaterQuestions, true, repeaterItemsWrapper, Integer.parseInt(retval), 1, null, res);
*/


                if (displayType.replaceAll(" ", "").toLowerCase().equals("datepicker")) {
                    renderDatePickerQuestiontest(repeaterQuestions, false, repeaterItemsWrapper, parentid, i, null);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("decimal")) {

                    renderTextBoxQuestionSubQuestion(repeaterQuestions, false, repeaterItemsWrapper, parentid, -1, null);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("dropdown")) {
                    repeaterQuestions = dl.getQuestionsAndAnswersBySubModuleId1(Utilities.subModuleId, Utilities.farmerId, stakeholderId, Utilities.farmerModuleId, question_id);

                    renderDropDownQuestionSubQuestion(repeaterQuestions, true, repeaterItemsWrapper, parentid, 1, null, res);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("textbox")) {
                    renderTextBoxQuestionSubQuestion(repeaterQuestions, false, repeaterItemsWrapper, parentid, -1, null);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("yesno") || displayType.replaceAll(" ", "").toLowerCase().equals("Yes/No")) {
                    repeaterQuestions = dl.getRepeaterItemsForQuestion1(question_id);
                    renderYesNoQuestionSubQuestion(repeaterQuestions, true, repeaterItemsWrapper, Integer.parseInt(retval), parentid, null, res);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photoupload")) {

                    renderPhotoUploadQuestionSubQuestion(repeaterQuestions, true, repeaterItemsWrapper, parentid, 0, null, res);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("repeater")) {

                    renderRepeaterQuestionSubQuestion(repeaterQuestions, Utilities.farmerId, stakeholderId, Utilities.olamFarmerId, Utilities.farmerModuleId, res, repeaterItemsWrapper);
                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("checkbox")) {
                    renderCheckBoxQuestionSubQuestion(repeaterQuestions, repeaterItemsWrapper, Utilities.farmerId, stakeholderId, Utilities.farmerModuleId, parentid);

                } else if (displayType.replaceAll(" ", "").toLowerCase().equals("photocheckbox")) {
                    renderPhotoCheckboxQuestiontest(repeaterQuestions, repeaterItemsWrapper, farmerId, stakeholderId, farmerModuleId, resourcesforyesno, Utilities.packagename);
                }

                // renderRepeaterQuestionSubQuestion(repeaterQuestions, Utilities.farmerId, Utilities.olamFarmerId, Utilities.farmerModuleId, res,repeaterItemsWrapper);
                // renderPhotoUploadQuestiontest(repeaterQuestions, true, repeaterItemsWrapper, 0, 0, null, res);
                //renderTextBoxQuestiontest(repeaterQuestions, false, repeaterItemsWrapper, 0, -1, null);

                //renderDatePickerQuestiontest(repeaterQuestions, false, repeaterItemsWrapper, 0, i, null);
                //renderCheckBoxQuestionSubQuestion(repeaterQuestions, repeaterItemsWrapper, Utilities.farmerId, Utilities.farmerModuleId);
                //renderDropDownQuestiontest(repeaterQuestions, true, repeaterItemsWrapper, Integer.parseInt(retval), 1, null, res);
                //renderYesNoQuestiontest(repeaterQuestions, true, repeaterItemsWrapper, Integer.parseInt(retval), 1, null, res);
            /*if (repetitions > 0) {
                final int existingItemsSize = repeaterItemsWrapper.getChildCount();
                if (repeaterQuestions.getCount() > 0 && repetitions > existingItemsSize) {
                    for (int i = 1; i <= repetitions; i++) {

                        if (i > existingItemsSize) {
                            LinearLayout repeateritemWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_item, repeaterItemsWrapper, false);
                            LinearLayout repeaterItemsInner = (LinearLayout) repeateritemWrapper.findViewById(R.id.repeater_item);

                            // add a text view with the item number in it
                            TextView tv = (TextView) repeateritemWrapper.findViewById(R.id.repeater_item_number);
                            tv.setText(String.valueOf(i));

                            while (repeaterQuestions.moveToNext()) {
                                String displayType = repeaterItemDisplayType(repeaterQuestions);

                                Cursor repeaterAnswers = dl.getRepeaterAnswersForQuestionByPosition(repeaterQuestions.getInt(repeaterQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), i, farmerId, farmerModuleId);
                                if (repeaterAnswers.getCount() > 0) {
                                    repeaterAnswers.moveToFirst();
                                }

                                if (displayType.toLowerCase().equals("datepicker")) {
                                    renderDatePickerQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                                } else if (displayType.toLowerCase().equals("decimal")) {
                                    renderTextBoxQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                                } else if (displayType.toLowerCase().equals("dropdown")) {
                                    renderDropDownQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                } else if (displayType.toLowerCase().equals("textbox")) {
                                    renderTextBoxQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                                } else if (displayType.toLowerCase().equals("yesno")) {
                                    renderYesNoQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                } else if (displayType.toLowerCase().equals("photoupload")) {
                                    renderPhotoUploadQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                                }
                            }

                            repeaterItemsWrapper.addView(repeateritemWrapper);
                            repeaterQuestions.moveToPosition(-1);
                        }
                    }
                } else {
                    if (repetitions > 0 && repetitions < existingItemsSize) {
                        for (int j = existingItemsSize; j > repetitions; j--) {
                            repeaterItemsWrapper.removeViewAt(j - 1);
                            removeRepeaterAnswer(questionId, j);
                        }
                    }
                }
            }*/
            }
        } else {
        }


    }

    public void renderYesNoQuestion(final Cursor qCursor, final boolean isRepeaterItem, final LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
        final LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);
        String question1 = "", question2 = "";
        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
        /*final String datatype;
        if (Integer.parseInt(displayTypeID) == 6){
            datatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        }else{
            datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        }
*/
        String[] coloumnames = qCursor.getColumnNames();

        List<String> nameList = Arrays.asList(coloumnames);

        String is_subset_questions_requiredduplicate = "";
        String condition = "";
        if (nameList.contains("is_subset_questions_required")) {


            is_subset_questions_requiredduplicate = qCursor.getString(qCursor.getColumnIndex("is_subset_questions_required"));


            //final String is_subset_questions_required = "true";
            condition = qCursor.getString(qCursor.getColumnIndex("conditional_questions"));

            // final String condition = "[{\"question\":\"125,109,162\",\"conditionCheck\":\"==NO\"},{\"question\":\"117,1173,119,67\",\"conditionCheck\":\"==YES\"}]";

        }
        int answerIdTemp = 0;

        try {
            if (is_subset_questions_requiredduplicate == null) {

                is_subset_questions_requiredduplicate = "";
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());

        }
        final String is_subset_questions_required = is_subset_questions_requiredduplicate;
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

        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        String[] columns = new String[]{"name", "_id", "code"};
        MatrixCursor spinnervalues = new MatrixCursor(columns);
        spinnervalues.addRow(new String[]{res.getString(R.string.ofis_switch_text_yes), "1", "1"});
        spinnervalues.addRow(new String[]{res.getString(R.string.ofis_switch_text_no), "2", "0"});
        //
        // spinner explaining

        // Error Message for constraints franklin
        final String displayConfig = qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION));
        String constraintType = "", errorMsg = "";
        int switchId = 0;
        try {
            JSONObject jsonObject = new JSONObject(displayConfig).getJSONObject(CoffeeConstants.CALCULATION);
            switchId = jsonObject.getInt("switch");
            constraintType = jsonObject.getString("type");
            errorMsg = jsonObject.getJSONObject("errorMsg").getString("error" + CommonUtilities.getLocaleSuffix(context.getResources()));
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());

        }
        TextView yesNoConstraint = null;
        if (constraintType.equalsIgnoreCase(CoffeeConstants.SURVEY_COMPLETION_CHECK)) {
            yesNoConstraint = new TextView(context);
            yesNoConstraint.setText(errorMsg);
            yesNoConstraint.setTextColor(ContextCompat.getColor(context, R.color.ofis_red));

        }

        // End

        dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, null, false);
        Integer currentAnswer = -1;
        if (hasAnswers) {
            String currentAnswerString = null;

            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                currentAnswerString = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
            } else {
                if (qCursor.getColumnIndex("answer_" + datatype) != -1) {
                    currentAnswerString = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                }
            }

            if (currentAnswerString != null) {
                if (currentAnswerString.equalsIgnoreCase("true")) {
                    currentAnswerString = "1";
                } else if (currentAnswerString.equalsIgnoreCase("false")) {
                    currentAnswerString = "0";
                }
                currentAnswer = Integer.parseInt(currentAnswerString);
            }

            if (currentAnswer == 1) {
                spinner.setSelection(1, false);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, String.valueOf(currentAnswer), 0, 0, 0);
            } else if (currentAnswer == 0) {
                spinner.setSelection(2, false);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, String.valueOf(currentAnswer), 0, 0, 0);
            }
        }


        try {


            if (is_subset_questions_required.equalsIgnoreCase("true")) {
                JSONArray json = new JSONArray(condition);

                for (int i = 0; i < json.length(); i++) {

                    JSONObject jsonobject = json.getJSONObject(0);
                    question1 = jsonobject.getString("question");
                    String condtion1 = jsonobject.getString("conditionCheck");

                    jsonobject = json.getJSONObject(1);
                    question2 = jsonobject.getString("question");
                    String condtion2 = jsonobject.getString("conditionCheck");
                }


            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        final String yesQuestion = question1;
        final String noqQuestion = question2;

        //   Prepopulate Yes/No If not answered
        String type, option;
        try {
            JSONObject jsonObject = new JSONObject(displayConfig).getJSONObject("prepopulate");
            type = jsonObject.getString("type");
            option = jsonObject.getString("option").trim();
            JSONArray countryIDs = (JSONArray) jsonObject.get("countryId");
            JSONArray productIDs = (JSONArray) jsonObject.get("productId");
            if (countryIDs.length() > 0 && productIDs.length() > 0) {
                ArrayList<Integer> countryLdList = new ArrayList<>();
                for (int i = 0; i < countryIDs.length(); i++) {
                    countryLdList.add(Integer.parseInt(countryIDs.getString(i)));
                }
                ArrayList<Integer> productIdList = new ArrayList<>();
                for (int i = 0; i < productIDs.length(); i++) {
                    productIdList.add(Integer.parseInt(productIDs.getString(i)));
                }
                if (countryLdList.contains(farmerGeo.getCountryId()) && productIdList.contains(farmerGeo.getProductId())) {
                    if (type.equalsIgnoreCase("Yes/No")) {
                        if (option.equalsIgnoreCase("yes")) {
                            spinner.setSelection(1);
                        } else {
                            spinner.setSelection(2);
                        }
                    }
                }
            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());

        }


        //End


        int finalSwitchId = switchId;
        TextView finalYesNoConstraint = yesNoConstraint;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (id > 0) {
                        //renderRepeaterItemstest(109, 2,  (isRepeaterItem) ? parentLayout : questionWrapper, 183488, "BR-0710-SM41-0003", 6, res);
                        // renderRepeaterItemstest(109, 2, questionWrapper, 183488, "BR-0710-SM41-0003", 6, resources);

                        //renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

                        // If id =2 , pirachana thaan
                        if (id == finalSwitchId) {
                            questionWrapper.addView(finalYesNoConstraint);
                            surveySet.add(questionId);
                            finalYesNoConstraint.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.RIGHT);
                        } else {
                            surveySet.remove(questionId);
                            questionWrapper.removeView(finalYesNoConstraint);
                        }

                        if (is_subset_questions_required.equalsIgnoreCase("true")) {

                            try {


                                if (id == 1) {

                                    //renderRepeaterItemstest(Integer.parseInt(question1), 2, questionWrapper, 183488, "BR-0710-SM41-0003", 6, resources);

                                    //Cursor repeaterQuestions = dl.getConditionallogic(questionId);
                                    // Cursor repeaterQuestions=    dl.getQuestionsAndAnswersBySubModuleId(16, 186862, 4);


                                    // renderRepeaterItemstest(question1, 3, questionWrapper, 183488, "BR-0710-SM41-0003", 6, resources);
                                    dl.deleteDataYesOrONSelect(questionId, Utilities.subModuleId);


                                    renderRepeaterItemstest(yesQuestion, questionId, questionWrapper, Utilities.farmerId, Utilities.stakeholderId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, true);

                                    //renderYesNoQuestion(repeaterQuestions, false, questionWrapper, 0, -1, null, res);


                                } else if (id == 2) {

                                    dl.deleteDataYesOrONSelect(questionId, Utilities.subModuleId);

                                    renderRepeaterItemstest(noqQuestion, questionId, questionWrapper, Utilities.farmerId, Utilities.stakeholderId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, true);

                                }


                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().log(e.getMessage());
                            }

                        }
                        Cursor selecteditem = (Cursor) spinner.getSelectedItem();
                        saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, selecteditem.getString(selecteditem.getColumnIndex("code")), 0, 0, 0);
                        //                    if (questionId == 4333) {
                        //                      showAlertMsg("We recommend you to complete the General Certification Survey before continuing with this Starbucks certification survey");
//                            TextView errorText = (TextView) spinner.getSelectedView();
//                            errorText.setError("");
//                            errorText.setTextColor(Color.RED);//just to highlight that this is an error
//                            errorText.setText("We recommend you to complete the General Certification Survey before continuing with this Starbucks certification survey");
//                          //  errorText.setBackground(context.getResources().getDrawable(R.drawable.rounder_red_edittext));

                        //                       }

                    } else {
                        removeAnswerEntry(questionId, answerId);
                        renderRepeaterItemstest(null, 2, questionWrapper, Utilities.farmerId, Utilities.stakeholderId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, false);

                    }
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().log(e.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (isRepeaterItem) {
            parentLayout.addView(spinner);
        } else {
            questionWrapper.addView(spinner);
            parentLayout.addView(questionWrapper);
        }


        try {


            if (is_subset_questions_required.equalsIgnoreCase("true")) {
                JSONArray json = new JSONArray(condition);

                for (int i = 0; i < json.length(); i++) {

                    JSONObject jsonobject = json.getJSONObject(0);
                    question1 = jsonobject.getString("question");
                    String condtion1 = jsonobject.getString("conditionCheck");

                    jsonobject = json.getJSONObject(1);
                    question2 = jsonobject.getString("question");
                    String condtion2 = jsonobject.getString("conditionCheck");
                }


                Cursor cursor = dl.getParentSubquestionSelect(Utilities.subModuleId, questionId);

                if (cursor.getCount() > 0) {
                    if (currentAnswer == 1) {
                        renderRepeaterItemstest(question1, questionId, questionWrapper, Utilities.farmerId, Utilities.stakeholderId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, true);

                    } else if (currentAnswer == 0) {
                        renderRepeaterItemstest(question2, questionId, questionWrapper, Utilities.farmerId, Utilities.stakeholderId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, true);

                    }

                }


            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }
    }

    void renderYesNoQuestionSubQuestion(final Cursor qCursor, final boolean isRepeaterItem, final LinearLayout parentLayout, final int repeaterItemPosition, final int parentQuestionId, Cursor repeaterAnswers, Resources res) {
        final LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        //renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);
        //qCursor.getString(qCursor.getColumnIndex("question"));
        res = resourcesforyesno;
        String datatypeduplicate = "";
        int questionIddouplicate = 0;
        if (qCursor != null) {
            if (qCursor.moveToFirst()) {
                do {
                    datatypeduplicate = qCursor.getString(qCursor.getColumnIndex("datatype_name"));

                    questionIddouplicate = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
                } while (qCursor.moveToNext());
            }
        }

        final int questionId = questionIddouplicate;
        final String datatype = datatypeduplicate;


        int answerIdTemp = 0;

        /*if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ID));
            } else {
                if (qCursor.getColumnIndex("answer_id") != -1) {


                    if (qCursor.moveToFirst()) {
                        do {
                            answerIdTemp = qCursor.getInt(qCursor.getColumnIndex("answer_id"));


                        } while (qCursor.moveToNext());
                    }


                }
            }
        }*/
        final int answerId = answerIdTemp;

        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        String[] columns = new String[]{"name", "_id", "code"};
        MatrixCursor spinnervalues = new MatrixCursor(columns);
        spinnervalues.addRow(new String[]{res.getString(R.string.ofis_switch_text_yes), "1", "1"});
        spinnervalues.addRow(new String[]{res.getString(R.string.ofis_switch_text_no), "2", "0"});

        dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, null, false);
        /*if (hasAnswers) {
            String currentAnswerString = null;
            Integer currentAnswer = -1;

            currentAnswerString = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));

            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {

            } else {
                if (qCursor.getColumnIndex("answer_" + datatype) != -1) {
                    currentAnswerString = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                }
            }

            if (currentAnswerString != null) {
                currentAnswer = Integer.parseInt(currentAnswerString);
            }

            if (currentAnswer == 1) {
                spinner.setSelection(1, false);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, String.valueOf(currentAnswer));
            } else if (currentAnswer == 0) {
                spinner.setSelection(2, false);
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, String.valueOf(currentAnswer));
            }
        }
*/

        Cursor cursor = dl.getParentSubquestionAnswer(Utilities.subModuleId, parentQuestionId, questionId);


        int position = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    position = Integer.parseInt(cursor.getString(cursor.getColumnIndex("question_answer")));
                    spinner.setSelection(position);

                } while (qCursor.moveToNext());
            }
        }


        Log.i("cursor", cursor + "");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (id > 0) {
                    //renderRepeaterItemstest(109, 2,  (isRepeaterItem) ? parentLayout : questionWrapper, 183488, "BR-0710-SM41-0003", 6, res);
                    // renderRepeaterItemstest(109, 2, questionWrapper, 183488, "BR-0710-SM41-0003", 6, resources);

                    //renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);


                    Cursor selecteditem = (Cursor) spinner.getSelectedItem();
                    int delete = dl.prentidAnswerDelete(parentQuestionId, questionId);
                    long l = id;
                    int i = (int) l;
                    long h = dl.prentidAnswer(Utilities.subModuleId, parentQuestionId, questionId, i + "", position + "", Utilities.olamFarmerId, Utilities.moduleId + "");
                    Log.i(h + "", h + "");

                    //saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, selecteditem.getString(selecteditem.getColumnIndex("code")));
                } else {
                    //removeAnswerEntry(questionId, answerId);
                    int delete = dl.prentidAnswerDelete(parentQuestionId, questionId);

                    Log.i(delete + "", delete + "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (isRepeaterItem) {
            parentLayout.addView(spinner);
        } else {
            questionWrapper.addView(spinner);
            parentLayout.addView(questionWrapper);
        }

    }

    private void parentAnswersave(int i, int parentQuestionId, int questionId, long id) {


    }


    @SuppressLint("SetTextI18n")
    private void recalculateDensity(Resources res) {

        if (densityPlotSection == null) {
            return;
        }

        // get initial average
        int mainAnswerCount = 5, completedAnswerCount = 0, subAnswerCount = densityPlotSection.getChildCount();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        DecimalFormat decimalFormat = (DecimalFormat) nf;
        decimalFormat.applyPattern("#.###");
        double combinedDensity = 0, initialAverageDensity = 0;
        String[] tempSubAnswers = new String[(mainAnswerCount + subAnswerCount)];
        double[] plotDensities = new double[subAnswerCount + 1];

        for (int i = 0; i < mainAnswerCount; i++) {
            View child = densityMainSection.getChildAt(i);
            if (child instanceof Spinner) {
                tempSubAnswers[i] = String.valueOf(((Spinner) child).getSelectedItemPosition());
                if (((Spinner) child).getSelectedItemId() > 0) {
                    Cursor selecteditem = (Cursor) ((Spinner) child).getSelectedItem();
                    String columnName = (selecteditem.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;
                    combinedDensity += selecteditem.getDouble(selecteditem.getColumnIndex(columnName));
                    completedAnswerCount++;
                }
            }
        }

        if (combinedDensity > 0) {
            initialAverageDensity = (combinedDensity / completedAnswerCount);
            plotDensities[0] = initialAverageDensity;
        }

        for (int x = 0; x < subAnswerCount; x++) {
            LinearLayout plotItemWrapper = (LinearLayout) densityPlotSection.getChildAt(x);
            LinearLayout plotItemInner = (LinearLayout) plotItemWrapper.findViewById(R.id.repeater_item);
            Spinner plotItemAnswer = (Spinner) plotItemInner.getChildAt(1);
            String answer = Utilities.getDefaultTextByLocalValue(plotvariants, plotItemAnswer.getSelectedItem().toString(), res);
            tempSubAnswers[(x + mainAnswerCount)] = String.valueOf(plotItemAnswer.getSelectedItemPosition());
            if (answer.equalsIgnoreCase("more")) {
                plotDensities[x + 1] = (initialAverageDensity * 0.9);
            } else if (answer.equalsIgnoreCase("less")) {
                plotDensities[x + 1] = (initialAverageDensity * 1.1);
            } else {
                plotDensities[x + 1] = initialAverageDensity;
            }
        }

        double finalCombinedDensity = 0;

        // get final average answer
        for (double plotDensity : plotDensities) {
            finalCombinedDensity += plotDensity;
        }

        double finalAverageDensity = finalCombinedDensity / plotDensities.length;

        densitySubAnswers = tempSubAnswers;

        densityResult.setText(res.getString(R.string.module_density_plot_question_result_intro) + decimalFormat.format(finalAverageDensity));

        if (initialAverageDensity > 0) {
            saveCalculatedAnswerEntry(densityQuestionId, densityAnswerId, -1, decimalFormat.format(finalAverageDensity), TextUtils.join(":", densitySubAnswers));
        } else {
            removeAnswerEntry(densityQuestionId, densityAnswerId);
        }

    }

    private void renderTreeDensityPlots(int plots, final Resources res) {
        if (plots > 1) {
            plotsHeader.setVisibility(View.VISIBLE);
            final int existingItemsSize = densityPlotSection.getChildCount();
            if (plots > existingItemsSize) {
                for (int i = 1; i < plots; i++) {

                    if (i > existingItemsSize) {
                        LinearLayout plotItemWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_item, densityPlotSection, false);
                        LinearLayout plotItemInner = (LinearLayout) plotItemWrapper.findViewById(R.id.repeater_item);

                        // add a text view with the item number in it
                        TextView tv = (TextView) plotItemWrapper.findViewById(R.id.repeater_item_number);
                        tv.setText(String.valueOf(i + 1));

                        // add a text view with the question in it
                        TextView qu = (TextView) inflater.inflate(R.layout.module_question, plotItemInner, false);
                        qu.setPadding(0, res.getDimensionPixelSize(R.dimen.ofis_large_pad), 0, 0);
                        qu.setText(res.getString(R.string.module_density_plot_question));

                        plotItemInner.addView(qu);

                        // add a spinner with the options array in it
                        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, plotItemInner, false);
                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, res.getStringArray(R.array.plotvariants));
                        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spinner.setAdapter(spinnerAdapter);

                        if (hasAnswers) {
                            if (densitySubAnswers.length > (i + 4)) {
                                spinner.setSelection(Integer.valueOf(densitySubAnswers[(i + 4)]));
                            }
                        }

                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                                recalculateDensity(res);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                        plotItemInner.addView(spinner);

                        densityPlotSection.addView(plotItemWrapper);
                    }
                }
            } else {
                if (plots > 1 && plots <= existingItemsSize) {
                    for (int j = existingItemsSize; j >= plots; j--) {
                        densityPlotSection.removeViewAt(j - 1);
                    }
                }
                recalculateDensity(res);
            }
        } else {
            if (densityPlotSection != null) {
                densityPlotSection.removeAllViews();
            }
            if (plotsHeader != null) {
                plotsHeader.setVisibility(View.GONE);
            }
            recalculateDensity(res);
        }
    }

    @SuppressLint("SetTextI18n")
    public void renderTreeDensityQuestion(Cursor questions, final Resources res) {
        // special case to determine tree density stuff
        setupPlotVariants(res);

        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);

//        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
        final int dataTypeID = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));
        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        }

        if(oldDatatype!=null){
            if (oldDatatype.equals("Farm Tree Density")){
                datatype = "decimal";
            }
        }


        String calcAnswerValue = "0";
        densityQuestionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        String columnName = (questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;

        // create parent layout to append to
        densityMainSection = (LinearLayout) inflater.inflate(R.layout.module_density_layout, questionWrapper, false);

        // get existing calculated answer, if any
        if (viewAnswer){

            calcAnswerValue = questions.getString(questions.getColumnIndex("answer_" + datatype));

            final EditText et = (EditText) inflater.inflate(R.layout.module_edit_text, questionWrapper, false);
            et.setText(calcAnswerValue);
            questionWrapper.addView(et);
            questionsSection.addView(questionWrapper);

        } else {

            if (hasAnswers) {
                calcAnswerValue = questions.getString(questions.getColumnIndex("answer_" + datatype));

                String currentAnswerValue;
                currentAnswerValue = questions.getString(questions.getColumnIndex("answer_text"));

                if (questions.getColumnIndex("answer_id") != -1) {
                    densityAnswerId = questions.getInt(questions.getColumnIndex("answer_id"));
                }

                // convert sub answers to an array from pipe separated
                if (currentAnswerValue != null) {
                    densitySubAnswers = currentAnswerValue.split(":");
                }
            }

            // create first five fields for density measurements
            for (int i = 0; i < 5; i++) {
                final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, densityMainSection, false);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) spinner.getLayoutParams();
                lp.setMargins(0, (i == 0 ? res.getDimensionPixelSize(R.dimen.ofis_small_pad) : 0), 0, res.getDimensionPixelSize(R.dimen.ofis_large_pad));
                spinner.setLayoutParams(lp);
                Cursor spinnervalues = dl.getLookupValuesForQuestion(questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), columnName,dataTypeID);

                dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, columnName, false);

                densityMainSection.addView(spinner);

                if (hasAnswers) {
                    if (densitySubAnswers.length > i) {
                        spinner.setSelection(Integer.valueOf(densitySubAnswers[i]));
                    }
                }

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        recalculateDensity(res);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        recalculateDensity(res);
                    }
                });

            }

            densityResult = (TextView) inflater.inflate(R.layout.module_question, densityMainSection, false);
            densityResult.setPadding(0, res.getDimensionPixelSize(R.dimen.ofis_large_pad), 0, 0);
            densityResult.setTypeface(null, Typeface.BOLD);
            densityResult.setText(res.getString(R.string.module_density_plot_question_result_intro) + " " + calcAnswerValue);
            densityMainSection.addView(densityResult);

            questionWrapper.addView(densityMainSection);

            plotsHeader = (TextView) inflater.inflate(R.layout.module_question, questionWrapper, false);
            plotsHeader.setPadding(0, res.getDimensionPixelSize(R.dimen.ofis_large_pad), 0, 0);
            plotsHeader.setText(res.getString(R.string.module_density_plot_question_intro));
            plotsHeader.setVisibility(View.GONE);

            questionWrapper.addView(plotsHeader);

            densityPlotSection = (LinearLayout) inflater.inflate(R.layout.module_repeater_container, questionWrapper, false);
            questionWrapper.addView(densityPlotSection);
            questionsSection.addView(questionWrapper);
        }

    }

    private void autofill100Percent(EditText currentBox) {
        int total = 0, target = 100, i;

        for (i = 0; i < hundredpercentquestions.size(); i++) {
            EditText hundredpercentquestion = hundredpercentquestions.get(i);
            String answer = hundredpercentquestion.getText().toString();
            if (answer.length() > 0) {
                total += Integer.valueOf(answer);
            } else {
                saveAnswerEntry(
                        (int) hundredpercentquestion.getTag(R.string.hpQId),
                        (int) hundredpercentquestion.getTag(R.string.hpAId),
                        (int) hundredpercentquestion.getTag(R.string.hpPqId),
                        (int) hundredpercentquestion.getTag(R.string.hpRiP),
                        (String) hundredpercentquestion.getTag(R.string.hpD),
                        "0", 0, 0, 0);
            }
        }

        if (total <= target) {
            hundredpercentbalance.setText(String.valueOf(target - total));
        } else if (target < total) {
            if (currentBox != null) {
                updatingHundredPercent = true;
                int currentValue = Integer.valueOf(currentBox.getText().toString());
                int balance = currentValue - (total - target);
                currentBox.setText(String.valueOf(balance));
                hundredpercentbalance.setText("0");
                updatingHundredPercent = false;
            }
        }

    }

    public static void resource(Resources res) {

        resourcesforyesno = res;
    }

    void renderCheckBoxQuestionForSIP(Cursor questions, LinearLayout parentLayout) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(questions, false, questionWrapper);

//        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));
        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        }

        if (oldDatatype != null) {
            if (oldDatatype.equals("Checkbox")) {
                datatype = "int";
            }
        }


        final boolean singleSelect;
        boolean tempSingleSelect = false;

        Cursor checkboxes = dl.getSpinnerData(OFISDBContract.LookupValuesSipEntry.TABLE_NAME, true, OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_POSITION);

        // is this a question we can only select one answer to?
        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting checkbox display config " + e.getMessage());
        }

        singleSelect = tempSingleSelect;

        if (checkboxes != null && checkboxes.getCount() > 0) {
            // add the table layout to the screen
            final TableLayout tl = (TableLayout) inflater.inflate(R.layout.module_checkbox_table, questionWrapper, false);
            tl.setSaveEnabled(true);

            while (checkboxes.moveToNext()) {
                // generate row item
                TableRow tr = (TableRow) inflater.inflate(R.layout.module_checkbox_table_row, tl, false);

                TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                TextView tv = (TextView) tr.findViewById(R.id.cbTitle);

                final int cbId = checkboxes.getInt(checkboxes.getColumnIndex("_id"));

                tvId.setText(String.valueOf(cbId));

                int columnIndex = (checkboxes.getColumnIndex(OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? checkboxes.getColumnIndex(OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : checkboxes.getColumnIndex(OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE);
                tv.setText(checkboxes.getString(columnIndex));

                CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);
                tl.addView(tr);

                // pre-fill checkboxes that have been previously ticked
               /* if (hasAnswers && cbAnswers.getCount() > 0) {
                    while (cbAnswers.moveToNext()) {
                        if (cbAnswers.getInt(cbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == cbId) {
                            cb.setChecked(true);
                        }
                    }
                    cbAnswers.moveToPosition(-1);
                }
*/
                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.cbCheckBox);

                        checkbox.setChecked(!checkbox.isChecked());
                        checkbox.callOnClick();
                    }

                });

                //stick the answer in the question itself
                String finalDatatype = datatype;
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (singleSelect) {
                            int ssTableRows = tl.getChildCount();
                            for (int j = 0; j < ssTableRows; j++) {
                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                ssTcb.setChecked(false);
                            }
                            CheckBox clickedCb = (CheckBox) view;
                            clickedCb.setChecked(true);
                        }
                        saveMultipleAnswerEntry(questionId, 0, finalDatatype, tl, 0, 0, 0, -1);
                    }
                });


            }

            questionWrapper.addView(tl);
            parentLayout.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl, 0, 0, 0, -1);
        }
    }

    public void renderDatePickerQuestionForSip(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

//        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));
        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        }

        if (oldDatatype != null) {
            if (oldDatatype.equals("Date Picker")) {
                datatype = "date";
            }
        }


        int answerIdTemp = 0;
        if (hasAnswers) {
            if (isRepeaterItem && repeaterAnswers != null && repeaterAnswers.getCount() > 0) {
                answerIdTemp = repeaterAnswers.getInt(repeaterAnswers.getColumnIndex(OFISDBContract.AnswerSipEntry.COLUMN_NAME_ID));
            } else {
                if (qCursor.getColumnIndex("answer_id") != -1) {
                    answerIdTemp = qCursor.getInt(qCursor.getColumnIndex("answer_id"));
                }
            }
        }
        final int answerId = answerIdTemp;

        // get display config
        try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                boolean showDay = display.getBoolean("show_day");
                boolean showMonth = display.getBoolean("show_month");
                boolean showyear = display.getBoolean("show_year");

                if ((!showDay) && (!showMonth) && (showyear)) {
                    // just the year, show the fifty year dropdown instead
                    final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
                    // String[] since1950 = Utilities.since1950();
                    String[] since1900 = Utilities.since1900();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1900);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinner.setAdapter(adapter);

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
                            for (int i = 0; i < spinner.getCount(); i++) {
                                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(currentAnswerValue.substring(0, 4))) {
                                    spinner.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    String finalDatatype = datatype;
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype, spinner.getSelectedItem().toString() + "-01-01", 0, 0, 0);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    if (isRepeaterItem) {
                        parentLayout.addView(spinner);
                    } else {
                        questionWrapper.addView(spinner);
                        parentLayout.addView(questionWrapper);
                    }
                } else {
                    Calendar today = Calendar.getInstance();
                    DatePicker dp = (DatePicker) inflater.inflate(R.layout.module_datepicker, questionWrapper, false);
                    final SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

                    if (hasAnswers) {
                        String answerDateString = null;
                        if (isRepeaterItem && repeaterAnswers != null) {
                            if (repeaterAnswers.getCount() > 0) {
                                answerDateString = repeaterAnswers.getString(repeaterAnswers.getColumnIndex("answer_" + datatype));
                            }
                        } else {
                            answerDateString = qCursor.getString(qCursor.getColumnIndex("answer_" + datatype));
                        }
                        if (answerDateString != null) {
                            today.setTime(serverDateFormat.parse(answerDateString));
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, answerDateString, 0, 0, 0);
                        }
                    }

                    String finalDatatype1 = datatype;
                    dp.init(
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH),
                            new DatePicker.OnDateChangedListener() {
                                @Override
                                public void onDateChanged(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                                    // get the date
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(selectedYear, selectedMonth, selectedDay);
                                    saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, finalDatatype1, serverDateFormat.format(cal.getTime()), 0, 0, 0);
                                }
                            }
                    );

                    if (isRepeaterItem) {
                        parentLayout.addView(dp);
                    } else {
                        questionWrapper.addView(dp);
                        parentLayout.addView(questionWrapper);
                    }
                }
            }


        } catch (JSONException | ParseException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error creating date picker display " + e.getMessage());

        }
    }

    public void renderPhotoCheckboxQuestionForSIP(Cursor questions, Resources res, String packageName) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);

//        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
//        final String oldDatatype =  dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        final int dataTypeID = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));

        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        }

        if (oldDatatype != null) {
            if (oldDatatype.equals("Photo Checkbox")) {
                datatype = "int";
            }
        }

        final boolean singleSelect;
        boolean tempSingleSelect = false;

        Cursor photoCheckboxes = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);
//        Cursor photoCbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId, farmerModuleId);

        // is this a question we can only select one answer to?
        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting photo checkbox display config " + e.getMessage());

        }

        singleSelect = tempSingleSelect;

        if (photoCheckboxes.getCount() > 0) {
            // add the table layout to the screen
            final TableLayout tl = (TableLayout) inflater.inflate(R.layout.module_photo_table, questionWrapper, false);

            while (photoCheckboxes.moveToNext()) {
                // generate row item
                TableRow tr = (TableRow) inflater.inflate(R.layout.module_photo_row, tl, false);

                TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                TextView tv = (TextView) tr.findViewById(R.id.cbTitle);
                ImageView iv = (ImageView) tr.findViewById(R.id.photo);

                int cbId = photoCheckboxes.getInt(photoCheckboxes.getColumnIndex("_id"));

                tvId.setText(String.valueOf(cbId));

                int columnIndex = (photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);
                tv.setText(photoCheckboxes.getString(columnIndex));

                int imageId = res.getIdentifier(photoCheckboxes.getString(photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_PHOTO)), "drawable", packageName);
                if (imageId != 0) {
                    iv.setImageDrawable(ContextCompat.getDrawable(context, imageId));
                } else {
                    iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.photo_checkbox_no_image));
                }

                final CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                // pre-fill checkboxes that have been previously ticked
                /*if (hasAnswers && photoCbAnswers.getCount() > 0) {
                    while (photoCbAnswers.moveToNext()) {
                        if (photoCbAnswers.getInt(photoCbAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT)) == cbId) {
                            cb.setChecked(true);
                        }
                    }
                    photoCbAnswers.moveToPosition(-1);
                }*/

                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cb.setChecked(!cb.isChecked());
                        cb.callOnClick();
                    }

                });

                //stick the answer in the question itself
                String finalDatatype = datatype;
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (singleSelect) {
                            int ssTableRows = tl.getChildCount();
                            for (int j = 0; j < ssTableRows; j++) {
                                TableRow ssTr = (TableRow) tl.getChildAt(j);
                                CheckBox ssTcb = (CheckBox) ssTr.findViewById(R.id.cbCheckBox);

                                ssTcb.setChecked(false);
                            }
                            CheckBox clickedCb = (CheckBox) view;
                            clickedCb.setChecked(true);
                        }

                        saveMultipleAnswerEntry(questionId, 0, finalDatatype, tl, 0, 0, 0, -1);
                    }
                });

                tl.addView(tr);
            }

            questionWrapper.addView(tl);
            questionsSection.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl, 0, 0, 0, -1);
        }

    }

    public void renderRepeaterQuestionForSIP(Cursor questions, final Resources res) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);

//        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String displayTypeID = String.valueOf(dl.getDisplayTypeIdByQuestionID(questionId));
        final int dataTypeID = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID));
//        final String old

        String oldDatatype = null;

        String datatype = null;
        if (dataTypeID == 6) {
            oldDatatype = dl.getQuestionTypeValue(Integer.parseInt(displayTypeID));
        } else {
            datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        }

        if (oldDatatype != null) {
            if (oldDatatype.equals("Repeater")) {
                datatype = "int";
            }
        }


        final int answerId = (hasAnswers) ? questions.getInt(questions.getColumnIndex("answer_id")) : 0;
        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, questionWrapper, false);

        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                int maxrepeats = display.getInt("maxrepeats");
                ArrayList<String> repeatOptions = new ArrayList<>(maxrepeats);
                for (int i = 0; i < maxrepeats; i++) {
                    repeatOptions.add(String.valueOf(i));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, repeatOptions);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.setAdapter(adapter);

                final boolean zeroAcceptable = (display.has("zeroacceptable") && (display.getBoolean("zeroacceptable")));

                if (hasAnswers) {
                    int answer = (!questions.isNull(questions.getColumnIndex("answer_" + datatype))) ? questions.getInt(questions.getColumnIndex("answer_" + datatype)) : 0;
                    spinner.setSelection(answer);
                }

                questionWrapper.addView(spinner);
                final LinearLayout repeaterItemsWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_container, questionWrapper, false);
                questionWrapper.addView(repeaterItemsWrapper);
                questionsSection.addView(questionWrapper);

                String finalDatatype = datatype;
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (id > 0 || zeroAcceptable) {
                            saveAnswerEntry(questionId, answerId, 0, -1, finalDatatype, spinner.getSelectedItem().toString(), 0, 0, 0);
                            if (id > 0) {
                                renderRepeaterItemsForSIP(questionId, id, repeaterItemsWrapper, res);
                            } else {
                                removeRepeaterAnswers(questionId);
                                repeaterItemsWrapper.removeAllViews();
                            }
                        } else {
                            removeAnswerEntry(questionId, answerId);
                            removeRepeaterAnswers(questionId);
                            repeaterItemsWrapper.removeAllViews();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().log("DynamicQuestions: Error getting repeater display config " + e.getMessage());
        }
    }


    private void renderRepeaterItemsForSIP(int questionId, long repetitions, LinearLayout repeaterItemsWrapper, Resources res) {
        Cursor repeaterQuestions = dl.getRepeaterItemsForQuestion(questionId);

        if (repetitions > 0) {
            final int existingItemsSize = repeaterItemsWrapper.getChildCount();
            if (repeaterQuestions.getCount() > 0 && repetitions > existingItemsSize) {
                for (int i = 1; i <= repetitions; i++) {

                    if (i > existingItemsSize) {
                        LinearLayout repeateritemWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_item, repeaterItemsWrapper, false);
                        LinearLayout repeaterItemsInner = (LinearLayout) repeateritemWrapper.findViewById(R.id.repeater_item);

                        // add a text view with the item number in it
                        TextView tv = (TextView) repeateritemWrapper.findViewById(R.id.repeater_item_number);
                        tv.setText(String.valueOf(i));

/*
                        while (repeaterQuestions.moveToNext()) {
                            String displayType = repeaterItemDisplayType(repeaterQuestions);

                            Cursor repeaterAnswers = dl.getRepeaterAnswersForQuestionByPosition(repeaterQuestions.getInt(repeaterQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), i, farmerId, farmerModuleId);
                            if (repeaterAnswers.getCount() > 0) {
                                repeaterAnswers.moveToFirst();
                            }

                            if (displayType.toLowerCase().equals("datepicker")) {
                                renderDatePickerQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                            } else if (displayType.toLowerCase().equals("decimal")) {
                                renderTextBoxQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                            } else if (displayType.toLowerCase().equals("dropdown")) {
                                renderDropDownQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                            } else if (displayType.toLowerCase().equals("textbox")) {
                                renderTextBoxQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers);
                            } else if (displayType.toLowerCase().equals("yesno")) {
                                renderYesNoQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                            } else if (displayType.toLowerCase().equals("photoupload")) {
                                renderPhotoUploadQuestion(repeaterQuestions, true, repeaterItemsInner, questionId, i, repeaterAnswers, res);
                            }
                        }

                        repeaterItemsWrapper.addView(repeateritemWrapper);
                        repeaterQuestions.moveToPosition(-1);*/
                    }

                }
            } else {
                if (repetitions > 0 && repetitions < existingItemsSize) {
                    for (int j = existingItemsSize; j > repetitions; j--) {
                        repeaterItemsWrapper.removeViewAt(j - 1);
                        removeRepeaterAnswer(questionId, j);
                    }
                }
            }
        }
    }


    public boolean requiredFieldsSIPAnswered(String uploadId, int moduleId, int subModuleId) {
        Cursor completedAnswers = dl.getAllAnswersSipByUploadId(uploadId);
        Cursor requiredQuestions = dl.getSipRequiredQuestionIds(moduleId, subModuleId);
        Set<Integer> completedQuestionIds = new HashSet<>();
        Set<Integer> requiredQuestionIds = new HashSet<>();

        while (completedAnswers.moveToNext()) {
            completedQuestionIds.add(completedAnswers.getInt(completedAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
        }

        while (requiredQuestions.moveToNext()) {
            requiredQuestionIds.add(requiredQuestions.getInt(requiredQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)));
        }

        return completedQuestionIds.containsAll(requiredQuestionIds);
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

    private int getIndexForValue(int value, Integer[] keys) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == value) {
                return i;
            }
        }
        return 0; // Default to the first item if the value is not found
    }

    public static boolean canParseToDouble(String answerValue) {
        try {
            // Try parsing the string to double
            Double.parseDouble(answerValue);
            // If no exception is thrown, parsing succeeded
            return true;
        } catch (NumberFormatException e) {
            // Parsing failed
            return false;
        }
    }

    private String getColumnName(String assignmentType) {
        String assignmentTypeColumn = "";
        switch (assignmentType) {
            case "1":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID;
                break;
            case "2":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID;
                break;
            case "3":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID;
                break;
            case "4":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID;
                break;
            case "5":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID;
                break;
            case "6":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID;
                break;
            case "7":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID;
                break;
            case "8":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID;
                break;
            case "9":
                assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID;
                break;
        }
        return assignmentTypeColumn;
    }

    public boolean countryAndProductAssignLookup(JSONArray countryAndProductAssign, long farmerId) throws JSONException {
        boolean isFound = false;
        for (int j = 0; j < countryAndProductAssign.length(); j++) {
            JSONObject countryAndProductAssignObj = countryAndProductAssign.getJSONObject(j);
            int productId = countryAndProductAssignObj.getInt("productId");
            JSONArray countryIdArray = countryAndProductAssignObj.getJSONArray("countryId");
            int farmerProductId = dl.getProductByFarmerId(farmerId);
            int farmerCountryId = dl.getCountryByFarmerId(farmerId);
            if (productId == farmerProductId) {
                boolean found = false;
                for (int k = 0; k < countryIdArray.length(); k++) {
                    if (countryIdArray.getInt(k) == farmerCountryId) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    return true;
                }
            }
        }

        return false;
    }

}
