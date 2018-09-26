package com.webpoint0.ofiscompanion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;

/**
 * Created by ben on 02/06/2015.
 */
@SuppressWarnings("DefaultFileTemplate")
class DynamicQuestions {
    static Resources resourcesforyesno;
    ArrayList<JSONObject> answerSet;
    ArrayList<EditText> hundredpercentquestions;
    int reportingYear;
    boolean updatingHundredPercent;
    boolean hasAnswers;
    LayoutInflater inflater;
    String questionColumnSuffix;
    LinearLayout questionsSection;
    Context context;
    private final DataLayer dl;
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

    private static DynamicQuestions instance = null;

    DynamicQuestions(Context context, String questionColumnSuffix, DataLayer dl) {
        this.answerSet = new ArrayList<>();
        this.hundredpercentquestions = new ArrayList<>();
        this.context = context;
        this.dl = dl;
        this.updatingHundredPercent = false;
        this.questionColumnSuffix = questionColumnSuffix;
    }


    static DynamicQuestions getInstance(Context context, String questionColumnSuffix, DataLayer dl) {
        if (instance == null) {
            instance = new DynamicQuestions(context, questionColumnSuffix, dl);
        }
        return instance;
    }

    static DynamicQuestions getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    static void clearInstance() {
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

    boolean requiredFieldsAnswered(int farmerModuleId, int moduleId) {
        Cursor completedAnswers = dl.getAllAnswersByFarmerModuleId(farmerModuleId);
        Cursor requiredQuestions = dl.getRequiredQuestionIds(moduleId);
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

    private void saveMultipleAnswerEntry(int questionId, @SuppressWarnings("SameParameterValue") int parentQuestionId, String dataType, TableLayout questionTable) {
        for (int i = 0; i < answerSet.size(); i++) {
            JSONObject currentAnswer = answerSet.get(i);
            if (currentAnswer != null) {
                int currentQuestionId = currentAnswer.optInt("question_id");
                if (currentQuestionId == questionId) {
                    answerSet.remove(i);
                }
            }
        }
        int tableRows = questionTable.getChildCount();
        for (int j = 0; j < tableRows; j++) {
            TableRow tr = (TableRow) questionTable.getChildAt(j);
            TextView cbId = (TextView) tr.findViewById(R.id.cbId);
            CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

            if (cb.isChecked()) {
                saveAnswerEntry(questionId, j, parentQuestionId, -1, dataType, String.valueOf(cbId.getText()));
            }
        }
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
            if (answer.length() > 0) {
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
                    answerSet.add(newAnswer);
                }
            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DynamicQuestions: Error saving calculated answer entry", e));
            e.printStackTrace();
        }
    }

    private void saveAnswerEntry(int questionId, int answerId, int parentQuestionId, int repeaterItemPosition, String datatype, String answer) {
        try {
            if (answer.length() > 0) {
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
                    answerSet.add(newAnswer);
                }
            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DynamicQuestions: Error saving answer entry", e));
            e.printStackTrace();
        }
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

    private void renderQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout) {
        TextView tv;
        tv = (TextView) inflater.inflate((isRepeaterItem) ? R.layout.module_repeater_item_question : R.layout.module_question, parentLayout, false);

        int columnIndex = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) : qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION);

        tv.setText(Html.fromHtml(qCursor.getString(columnIndex)));
        String hh = Html.fromHtml(qCursor.getString(columnIndex)) + "";

        // get disp"lay config to see if question is required
        try {
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
        }

        parentLayout.addView(tv);
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

    void renderPhotoUploadQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
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
        }

        Button modulePhotoUploadButton = (Button) rl.findViewById(R.id.modulePhotoUpload);

        modulePhotoUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;

                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    FirebaseCrash.report(new Exception("DynamicQuestions: Error creating photo image file", e));
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    Uri fileUri = Uri.fromFile(photoFile);
                    setCapturedImageURI(fileUri);
                    setCurrentPhotoPath(fileUri.getPath());
                    setCurrentImageView(iv);
                    setCurrentPhotoMetaData(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype);

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
                    FirebaseCrash.report(new Exception("DynamicQuestions: Error creating photo image file", e));
                    e.printStackTrace();
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
        File storageDir = context.getExternalFilesDir(null);
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error creating photo metadata", e));
            e.printStackTrace();
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

    private String getCurrentPhotoPath() {
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

    void setFullImageFromFilePath(Resources res) {
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
                        getCurrentPhotoFileName()
                );
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("Dynamic Questions: Error getting photo question image", e));
                e.printStackTrace();
            }
        }
    }

    private void displayBitmap(String savedImagePath, ImageView target, Resources res) {
        // set our desired width and height for display
        int baseWidth = target.getWidth();
        int baseHeight = target.getHeight();

        Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
        Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
        Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

        Bitmap bitmap = Utilities.loadSizedBitmap(savedImagePath, outputWidth, outputHeight);

        target.setImageBitmap(bitmap);
    }

    void renderDatePickerQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers) {
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
                    String[] since1950 = Utilities.since1950();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1950);
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

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, spinner.getSelectedItem().toString() + "-01-01");
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
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, answerDateString);
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
                                    saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, serverDateFormat.format(cal.getTime()));
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error creating date picker display", e));
            e.printStackTrace();
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
                    String[] since1950 = Utilities.since1950();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1950);
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error creating date picker display", e));
            e.printStackTrace();
        }
    }

    void renderTextBoxQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers) {
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
        }

        // add tv to array if this is a 100% group question
        // get display config
        boolean isHPQTemp = false;

        try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));

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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting textbox display config", e));
            e.printStackTrace();
        }
        final boolean isHundredPercentQuestion = isHPQTemp;

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
                saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, et.getText().toString());
            }
        });

        if (isRepeaterItem) {
            parentLayout.addView(et);
        } else {
            questionWrapper.addView(et);
            parentLayout.addView(questionWrapper);
        }
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting textbox display config", e));
            e.printStackTrace();
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

    void renderDropDownQuestion(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
        final LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);
        resources = res;
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

        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        Cursor spinnervalues;

        // get display config
        try {
            JSONObject displayConfig = new JSONObject(qCursor.getString(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            final String columnName = (qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;

            if (displayConfig.has("lookuptable")) {
                spinnervalues = dl.getSpinnerData(displayConfig.getString("lookuptable"), false, null, null, true, "name");
            } else {
                spinnervalues = dl.getLookupValuesForQuestion(qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), columnName);
            }

            final boolean isDensityQuestion = (displayConfig.has("densityquestionparent") && (displayConfig.getBoolean("densityquestionparent")));

            dl.bindDataDrivenSpinner(spinner, spinnervalues, context, R.string.ModuleAnswerHint, columnName, false);

            if (hasAnswers) {
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


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {


                    //renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, olamFarmerId, farmerModuleId, res);


                    //renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, olamFarmerId, farmerModuleId, res);
                    if (id > 0) {
                        if (datatype.equals("int") && (!isDensityQuestion)) {
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, String.valueOf(id));

                            long h = Long.parseLong("0026");


                        } else {
                            Cursor selecteditem = (Cursor) spinner.getSelectedItem();
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, selecteditem.getString(selecteditem.getColumnIndex(columnName)));
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error gettign dropdown display config", e));
            e.printStackTrace();
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

        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
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
                spinnervalues = dl.getLookupValuesForQuestion(lookupvalue, columnName);
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error gettign dropdown display config", e));
            e.printStackTrace();
        }

    }


    void renderCheckBoxQuestion(Cursor questions, LinearLayout parentLayout, long farmerId, long farmerModuleId) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(questions, false, questionWrapper);

        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final boolean singleSelect;
        boolean tempSingleSelect = false;

        Cursor checkboxes = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);

        Cursor cbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId, farmerModuleId);

        // is this a question we can only select one answer to?
        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting checkbox display config", e));
            e.printStackTrace();
        }

        singleSelect = tempSingleSelect;

        if (checkboxes.getCount() > 0) {
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
                        saveMultipleAnswerEntry(questionId, 0, datatype, tl);
                    }
                });


            }

            questionWrapper.addView(tl);
            parentLayout.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl);
        }
    }

    void renderCheckBoxQuestionSubQuestion(Cursor questions, LinearLayout parentLayout, long farmerId, long farmerModuleId, final int parentid) {
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

        Cursor cbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId, farmerModuleId);

        // is this a question we can only select one answer to?
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
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting checkbox display config", e));
            e.printStackTrace();
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

                TextView tvId = (TextView) tr.findViewById(R.id.cbId);
                TextView tv = (TextView) tr.findViewById(R.id.cbTitle);

                final int cbId = checkboxes.getInt(checkboxes.getColumnIndex("_id"));


                tvId.setText(String.valueOf(cbId));

                int columnIndex = (checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) != -1) ? checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix) : checkboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE);
                tv.setText(checkboxes.getString(columnIndex));

                CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);
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

            saveMultipleAnswerEntry(questionId, 0, datatype, tl);
        }
    }

    void renderPhotoCheckboxQuestiontest(Cursor questions, final LinearLayout parentLayout, long farmerId, long farmerModuleId, Resources res, String packageName) {
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
        Cursor photoCbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId, farmerModuleId);

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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting photo checkbox display config", e));
            e.printStackTrace();
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

                int imageId = res.getIdentifier("cbimage_" + photoCheckboxes.getString(photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_PHOTO)), "drawable", packageName);
                if (imageId != 0) {
                    iv.setImageDrawable(ContextCompat.getDrawable(context, imageId));
                } else {
                    iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.photo_checkbox_no_image));
                }

                final CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                // pre-fill checkboxes that have been previously ticked
                if (hasAnswers && photoCbAnswers.getCount() > 0) {
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

                        saveMultipleAnswerEntry(questionId, 0, datatype, tl);
                    }
                });

                tl.addView(tr);
            }

            questionWrapper.addView(tl);
            parentLayout.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl);
        }

    }


    void renderPhotoCheckboxQuestion(Cursor questions, long farmerId, long farmerModuleId, Resources res, String packageName) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);

        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final boolean singleSelect;
        boolean tempSingleSelect = false;

        Cursor photoCheckboxes = dl.getSpinnerData(OFISDBContract.LookupValuesEntry.TABLE_NAME, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID, questionId, true, OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION);
        Cursor photoCbAnswers = dl.getAnswersForQuestionByFarmer(questionId, farmerId, farmerModuleId);

        // is this a question we can only select one answer to?
        // get display config
        try {
            JSONObject displayConfig = new JSONObject(questions.getString(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));
            if (displayConfig.has("display")) {
                JSONObject display = displayConfig.getJSONObject("display");
                tempSingleSelect = display.optBoolean("single_select", false);
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting photo checkbox display config", e));
            e.printStackTrace();
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

                int imageId = res.getIdentifier("cbimage_" + photoCheckboxes.getString(photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_PHOTO)), "drawable", packageName);
                if (imageId != 0) {
                    iv.setImageDrawable(ContextCompat.getDrawable(context, imageId));
                } else {
                    iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.photo_checkbox_no_image));
                }

                final CheckBox cb = (CheckBox) tr.findViewById(R.id.cbCheckBox);

                // pre-fill checkboxes that have been previously ticked
                if (hasAnswers && photoCbAnswers.getCount() > 0) {
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

                        saveMultipleAnswerEntry(questionId, 0, datatype, tl);
                    }
                });

                tl.addView(tr);
            }

            questionWrapper.addView(tl);
            questionsSection.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl);
        }

    }


    void renderRepeaterQuestion(Cursor questions, final long farmerId, final String olamFarmerId, final long farmerModuleId, final Resources res) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);
        resources = res;
        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
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


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (id > 0 || zeroAcceptable) {
                            saveAnswerEntry(questionId, answerId, 0, -1, datatype, spinner.getSelectedItem().toString());
                            if (id > 0) {
                                renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, olamFarmerId, farmerModuleId, res);
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting repeater display config", e));
            e.printStackTrace();
        }
    }

    void renderRepeaterQuestionSubQuestion(Cursor questions, final long farmerId, final String olamFarmerId, final long farmerModuleId, final Resources res, LinearLayout parentLayout) {
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
                            saveAnswerEntry(questionId, answerId, 0, -1, datatype, spinner.getSelectedItem().toString());
                            if (id > 0) {
                                renderRepeaterItems(questionId, id, repeaterItemsWrapper, farmerId, olamFarmerId, farmerModuleId, res);
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting repeater display config", e));
            e.printStackTrace();
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting repeater item type display config", e));
            e.printStackTrace();
        }
        return displayType;
    }

    private void renderRepeaterItems(int questionId, long repetitions, LinearLayout repeaterItemsWrapper, long farmerId, String olamFarmerId, long farmerModuleId, Resources res) {
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
        }
    }

    private void renderRepeaterItemstest(String questionId, int parentid, LinearLayout repeaterItemsWrapper, long farmerId, String olamFarmerId, long farmerModuleId, Resources res, boolean selectCondition) {
        //Cursor repeaterQuestions = dl.getRepeaterItemsForQuestion(questionId);

        int i = repeaterItemsWrapper.getChildCount();
        for (; i > 2; i--) {
            repeaterItemsWrapper.removeViewAt(i - 1);

        }

        if (selectCondition) {
            for (String retval : questionId.split(",")) {
                System.out.println(retval);

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


                if (displayType.toLowerCase().equals("datepicker")) {
                    renderDatePickerQuestiontest(repeaterQuestions, false, repeaterItemsWrapper, parentid, i, null);
                } else if (displayType.toLowerCase().equals("decimal")) {

                    renderTextBoxQuestionSubQuestion(repeaterQuestions, false, repeaterItemsWrapper, parentid, -1, null);
                } else if (displayType.toLowerCase().equals("dropdown")) {
                    repeaterQuestions = dl.getQuestionsAndAnswersBySubModuleId1(Utilities.subModuleId, Utilities.farmerId, Utilities.farmerModuleId, question_id);

                    renderDropDownQuestionSubQuestion(repeaterQuestions, true, repeaterItemsWrapper, parentid, 1, null, res);
                } else if (displayType.toLowerCase().equals("textbox")) {
                    renderTextBoxQuestionSubQuestion(repeaterQuestions, false, repeaterItemsWrapper, parentid, -1, null);
                } else if (displayType.toLowerCase().equals("yesno")) {
                    repeaterQuestions = dl.getRepeaterItemsForQuestion1(question_id);
                    renderYesNoQuestionSubQuestion(repeaterQuestions, true, repeaterItemsWrapper, Integer.parseInt(retval), parentid, null, res);
                } else if (displayType.toLowerCase().equals("photoupload")) {

                    renderPhotoUploadQuestionSubQuestion(repeaterQuestions, true, repeaterItemsWrapper, parentid, 0, null, res);
                } else if (displayType.toLowerCase().equals("repeater")) {

                    renderRepeaterQuestionSubQuestion(repeaterQuestions, Utilities.farmerId, Utilities.olamFarmerId, Utilities.farmerModuleId, res, repeaterItemsWrapper);
                } else if (displayType.toLowerCase().equals("checkbox")) {
                    renderCheckBoxQuestionSubQuestion(repeaterQuestions, repeaterItemsWrapper, Utilities.farmerId, Utilities.farmerModuleId, parentid);

                } else if (displayType.toLowerCase().equals("photocheckbox")) {
                    renderPhotoCheckboxQuestiontest(repeaterQuestions, repeaterItemsWrapper, farmerId, farmerModuleId, resourcesforyesno, Utilities.packagename);
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

    void renderYesNoQuestion(final Cursor qCursor, final boolean isRepeaterItem, final LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers, final Resources res) {
        final LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);
        String question1 = "", question2 = "";
        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        final String is_subset_questions_required = qCursor.getString(qCursor.getColumnIndex("is_subset_questions_required"));
        //final String is_subset_questions_required = "true";
        final String condition = qCursor.getString(qCursor.getColumnIndex("conditional_questions"));
       // final String condition = "[{\"question\":\"125,109,162\",\"conditionCheck\":\"==NO\"},{\"question\":\"117,1173,119,67\",\"conditionCheck\":\"==YES\"}]";
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

        final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner, (isRepeaterItem) ? parentLayout : questionWrapper, false);
        String[] columns = new String[]{"name", "_id", "code"};
        MatrixCursor spinnervalues = new MatrixCursor(columns);
        spinnervalues.addRow(new String[]{res.getString(R.string.ofis_switch_text_yes), "1", "1"});
        spinnervalues.addRow(new String[]{res.getString(R.string.ofis_switch_text_no), "2", "0"});

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


        }

        final String yesQuestion = question1;
        final String noqQuestion = question2;


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (id > 0) {
                    //renderRepeaterItemstest(109, 2,  (isRepeaterItem) ? parentLayout : questionWrapper, 183488, "BR-0710-SM41-0003", 6, res);
                    // renderRepeaterItemstest(109, 2, questionWrapper, 183488, "BR-0710-SM41-0003", 6, resources);

                    //renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);


                    if (is_subset_questions_required.equalsIgnoreCase("true")) {

                        try {


                            if (id == 1) {

                                //renderRepeaterItemstest(Integer.parseInt(question1), 2, questionWrapper, 183488, "BR-0710-SM41-0003", 6, resources);

                                //Cursor repeaterQuestions = dl.getConditionallogic(questionId);
                                // Cursor repeaterQuestions=    dl.getQuestionsAndAnswersBySubModuleId(16, 186862, 4);


                                // renderRepeaterItemstest(question1, 3, questionWrapper, 183488, "BR-0710-SM41-0003", 6, resources);
                                dl.deleteDataYesOrONSelect(questionId, Utilities.subModuleId);


                                renderRepeaterItemstest(yesQuestion, questionId, questionWrapper, Utilities.farmerId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, true);

                                //renderYesNoQuestion(repeaterQuestions, false, questionWrapper, 0, -1, null, res);


                            } else if (id == 2) {

                                dl.deleteDataYesOrONSelect(questionId, Utilities.subModuleId);

                                renderRepeaterItemstest(noqQuestion, questionId, questionWrapper, Utilities.farmerId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, true);

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    Cursor selecteditem = (Cursor) spinner.getSelectedItem();
                    saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, selecteditem.getString(selecteditem.getColumnIndex("code")));
                } else {
                    removeAnswerEntry(questionId, answerId);
                    renderRepeaterItemstest(null, 2, questionWrapper, Utilities.farmerId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, false);

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
                        renderRepeaterItemstest(question1, questionId, questionWrapper, Utilities.farmerId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, true);

                    } else if (currentAnswer == 0) {
                        renderRepeaterItemstest(question2, questionId, questionWrapper, Utilities.farmerId, Utilities.olamFarmerId, Utilities.farmerModuleId, resources, true);

                    }

                }


            }
        } catch (Exception e) {


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
                if (plots > 1 && plots < existingItemsSize) {
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
    void renderTreeDensityQuestion(Cursor questions, final Resources res) {
        // special case to determine tree density stuff
        setupPlotVariants(res);

        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);

        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        String calcAnswerValue = "0";
        densityQuestionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
        String columnName = (questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + questionColumnSuffix) != -1) ? OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + questionColumnSuffix : OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE;

        // create parent layout to append to
        densityMainSection = (LinearLayout) inflater.inflate(R.layout.module_density_layout, questionWrapper, false);

        // get existing calculated answer, if any
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
            Cursor spinnervalues = dl.getLookupValuesForQuestion(questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID)), columnName);

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
                        "0");
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

        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID));
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting checkbox display config", e));
            e.printStackTrace();
        }

        singleSelect = tempSingleSelect;

        if (checkboxes.getCount() > 0) {
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
                        saveMultipleAnswerEntry(questionId, 0, datatype, tl);
                    }
                });


            }

            questionWrapper.addView(tl);
            parentLayout.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl);
        }
    }

    public void renderDatePickerQuestionForSip(Cursor qCursor, boolean isRepeaterItem, LinearLayout parentLayout, final int parentQuestionId, final int repeaterItemPosition, Cursor repeaterAnswers) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, parentLayout, false);
        renderQuestion(qCursor, isRepeaterItem, (isRepeaterItem) ? parentLayout : questionWrapper);

        final String datatype = qCursor.getString(qCursor.getColumnIndex("datatype_name"));
        final int questionId = qCursor.getInt(qCursor.getColumnIndex(OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID));
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

                if ( (!showDay) && (!showMonth) && (showyear) ) {
                    // just the year, show the fifty year dropdown instead
                    final Spinner spinner = (Spinner) inflater.inflate(R.layout.module_spinner,(isRepeaterItem) ? parentLayout : questionWrapper,false);
                    String[] since1950 = Utilities.since1950();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, since1950);
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

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, spinner.getSelectedItem().toString()+"-01-01");
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
                            saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, answerDateString);
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
                                    saveAnswerEntry(questionId, answerId, parentQuestionId, repeaterItemPosition, datatype, serverDateFormat.format(cal.getTime()));
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error creating date picker display", e));
            e.printStackTrace();
        }
    }

    public
    void renderPhotoCheckboxQuestionForSIP(Cursor questions, Resources res, String packageName) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);

        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting photo checkbox display config", e));
            e.printStackTrace();
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

                int imageId = res.getIdentifier("cbimage_" + photoCheckboxes.getString(photoCheckboxes.getColumnIndex(OFISDBContract.LookupValuesEntry.COLUMN_NAME_PHOTO)), "drawable", packageName);
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

                        saveMultipleAnswerEntry(questionId, 0, datatype, tl);
                    }
                });

                tl.addView(tr);
            }

            questionWrapper.addView(tl);
            questionsSection.addView(questionWrapper);

            saveMultipleAnswerEntry(questionId, 0, datatype, tl);
        }

    }

    public void renderRepeaterQuestionForSIP(Cursor questions, final Resources res) {
        LinearLayout questionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, questionsSection, false);
        renderQuestion(questions, false, questionWrapper);

        final String datatype = questions.getString(questions.getColumnIndex("datatype_name"));
        final int questionId = questions.getInt(questions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_ID));
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
                    int answer = (!questions.isNull(questions.getColumnIndex("answer_" + datatype))) ? questions.getInt(questions.getColumnIndex("answer_"+datatype)) : 0;
                    spinner.setSelection(answer);
                }

                questionWrapper.addView(spinner);
                final LinearLayout repeaterItemsWrapper = (LinearLayout) inflater.inflate(R.layout.module_repeater_container, questionWrapper, false);
                questionWrapper.addView(repeaterItemsWrapper);
                questionsSection.addView(questionWrapper);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (id > 0 || zeroAcceptable) {
                            saveAnswerEntry(questionId, answerId, 0, -1, datatype, spinner.getSelectedItem().toString());
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
            FirebaseCrash.report(new Exception("DynamicQuestions: Error getting repeater display config", e));
            e.printStackTrace();
        }
    }


    private void renderRepeaterItemsForSIP(int questionId, long repetitions, LinearLayout repeaterItemsWrapper, Resources res) {
        Cursor repeaterQuestions = dl.getRepeaterItemsForQuestion(questionId);

        if (repetitions > 0) {
            final int existingItemsSize = repeaterItemsWrapper.getChildCount();
            if (repeaterQuestions.getCount() > 0 && repetitions > existingItemsSize) {
                for (int i = 1; i<=repetitions; i++) {

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
                    for (int j = existingItemsSize; j>repetitions; j--) {
                        repeaterItemsWrapper.removeViewAt(j-1);
                        removeRepeaterAnswer(questionId, j);
                    }
                }
            }
        }
    }


    public  boolean requiredFieldsSIPAnswered(String uploadId, int moduleId, int subModuleId) {
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

}
