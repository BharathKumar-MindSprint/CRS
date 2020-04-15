package com.olam.digital.ofis;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;


public class ReviewFarmManagementPlan extends BaseActivity {

    public static long fmpId, farmerId;
    public static int fmpStartYear, statusStarted, farmId;
    public static String olamFarmerId;
    public static boolean fullReviewMode = false;
    private Context context;
    private Resources res;
    private DataLayer dl;
    private LinearLayout entriesSection;
    private final ArrayList<JSONObject> valueSet = new ArrayList<>();
    private final Hashtable<String, EditText> fmpEntries = new Hashtable<>();
    private final Hashtable<String, String> fmpCurrentValues = new Hashtable<>();
    private LayoutInflater inflater;
    private String entryColumnSuffix = "";
    private DynamicQuestions dynamicQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_farm_management_plan);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        res = getResources();

        dl = new DataLayer(res, context);
        entriesSection = (LinearLayout) findViewById(R.id.entriesSection);
        inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        entryColumnSuffix = Utilities.getLocaleSuffix(res);

        setupSwitch();
        bindSaveButton();


        dynamicQuestions = new DynamicQuestions(this, entryColumnSuffix, dl);

        dynamicQuestions.questionsSection = entriesSection;
        dynamicQuestions.inflater = inflater;
        dynamicQuestions.reportingYear = fmpStartYear;
        dynamicQuestions.hasAnswers = true;


        final ScrollView mainScroll = (ScrollView) findViewById(R.id.scrollView);
        mainScroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainScroll.scrollTo(0,0);
                if (Build.VERSION.SDK_INT < 16) {
                    //noinspection deprecation
                    mainScroll.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mainScroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

    }

    private void checkFullReview(final CompoundButton sw)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(res.getString(R.string.fmp_are_you_sure))
                .setCancelable(false)
                .setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setupFullReview();
                    }
                })
                .setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sw.setChecked(false);
                            dialog.cancel();
                        }
                    }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setupSwitch() {
        final Switch fmpSwitch = (Switch) findViewById(R.id.fmpSwitch);

        if (fullReviewMode) {
            fmpSwitch.setChecked(true);
            setupFullReview();
        } else {
            setupCurrentValues();
            setupSections();
        }

        fmpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fullReviewMode = isChecked;
                if (fullReviewMode) {
                    checkFullReview(buttonView);
                } else {
                    setupCurrentValues();
                    setupSections();
                }
            }
        });
    }

    private void setupCurrentValues() {
        Cursor currentValues = dl.getCurrentValuesByFmpId(fmpId);

        while (currentValues.moveToNext()) {
            fmpCurrentValues.put(currentValues.getString(currentValues.getColumnIndex(OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_NAME)), currentValues.getString(currentValues.getColumnIndex(OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_VALUE)));
        }
    }

    private void setupSections() {
        entriesSection.removeAllViews();
        Cursor sections = dl.getFmpSections();

        // loop through sections and get the right entries for each
        while (sections.moveToNext()) {
            LinearLayout sectionWrapper = (LinearLayout) inflater.inflate(R.layout.module_question_wrapper, entriesSection, false);
            TextView tv = (TextView) inflater.inflate(R.layout.fmp_section_heading, sectionWrapper, false);

            int columnIndex = (sections.getColumnIndex(OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE + entryColumnSuffix) != -1) ? sections.getColumnIndex(OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE + entryColumnSuffix) : sections.getColumnIndex(OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE);

            tv.setText(Html.fromHtml(sections.getString(columnIndex)));

            sectionWrapper.addView(tv);
            setupSectionEntries(sections.getInt(sections.getColumnIndex(OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_ID)), sectionWrapper);
            entriesSection.addView(sectionWrapper);
        }

    }

    private void setupSectionEntries(int sectionId, LinearLayout sectionWrapper) {
        Cursor entries = dl.getFmpEntriesBySectionId(fmpId, sectionId);

        if (entries.getCount() > 0) {
            TableLayout tl = (TableLayout) inflater.inflate(R.layout.fmp_entries_table, sectionWrapper, false);

            while( entries.moveToNext() ) {
                // generate row item
                if (entries.getInt(entries.getColumnIndex(OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_SHOW_IN_APP)) == 1) {
                    TableRow tr = (TableRow) inflater.inflate(R.layout.fmp_entry_row, tl, false);
                    TextView tvId = (TextView) tr.findViewById(R.id.entryId);
                    TextView tv = (TextView) tr.findViewById(R.id.entryTitle);
                    boolean isEditableFieldTemp = true;

                    final int entryId = entries.getInt(entries.getColumnIndex("id"));

                    tvId.setText(String.valueOf(entryId));

                    int columnIndex = (entries.getColumnIndex(OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE + entryColumnSuffix) != -1) ? entries.getColumnIndex(OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE + entryColumnSuffix) : entries.getColumnIndex(OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE);
                    tv.setText(entries.getString(columnIndex));

                    final EditText et = (EditText) tr.findViewById(R.id.entryInputBox);

                    // add existing entry stuff here
                    String existingvalue = entries.getString(entries.getColumnIndex(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_VALUE));
                    if (existingvalue != null) {
                        et.setText(existingvalue);
                        saveReviewValue(fmpId, entryId, existingvalue);
                    }
                    // add existing entry stuff here

                    if (entries.getInt(entries.getColumnIndex(OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_CALCULATED_FIELD)) == 1) {
                        et.setKeyListener(null);
                        et.setAlpha((float) 0.5);
                        tv.setAlpha((float) 0.5);
                        isEditableFieldTemp = false;
                    }

                    final boolean isEditableField = isEditableFieldTemp;
                    final String internal_name = entries.getString(entries.getColumnIndex(OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_NAME));

                    et.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            saveReviewValue(fmpId, entryId, et.getText().toString());
                            if (isEditableField) {
                                runReviewCalculations(internal_name, editable);
                            }
                        }
                    });

                    fmpEntries.put(internal_name, et);

                    tl.addView(tr);
                }
            }
            sectionWrapper.addView(tl);
        }
    }

    private void updateFertiliserPercent(Editable editable) {
        double newPercent = 0, newProductiveTreesToFertilise = 0;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        DecimalFormat percentageFormat = (DecimalFormat)nf;
        percentageFormat.applyPattern("#.##");
        if (editable.length() > 0) {
            Double calcTotal = (fmpCurrentValues.containsKey("fertiliser_recommended_total")) ? Double.valueOf(fmpCurrentValues.get("fertiliser_recommended_total")) : null;
            Double calcPercent = (fmpCurrentValues.containsKey("fertiliser_recommended_percentage")) ? Double.valueOf(fmpCurrentValues.get("fertiliser_recommended_percentage")) : null;

            if (calcTotal == null || calcPercent == null || calcTotal == 0 || calcPercent == 0) {
                return;
            }

            double revisedTotal = Double.valueOf(editable.toString());
            newPercent = (revisedTotal / calcTotal) * calcPercent;

            double productiveTreesToFertilise = (fmpCurrentValues.containsKey("fertiliser_recommended_productive_trees")) ? Double.valueOf(fmpCurrentValues.get("fertiliser_recommended_productive_trees")) : 0;

            if(productiveTreesToFertilise > 0) {
                double calcPercentFraction = calcPercent / 100;
                int totalProductiveTrees = (int) Math.round(productiveTreesToFertilise / calcPercentFraction);
                newProductiveTreesToFertilise = Math.round(totalProductiveTrees * (newPercent / 100));
            }

        }
        EditText outputPercent = (fmpEntries.containsKey("fertiliser_revised_recommended_percentage")) ? fmpEntries.get("fertiliser_revised_recommended_percentage") : null;
        if (outputPercent != null) {
            String outputPercentText = (newPercent > 0) ? percentageFormat.format(newPercent) : "";
            outputPercent.setText(outputPercentText);
        }

        EditText outputProductiveTrees = (fmpEntries.containsKey("fertiliser_revised_productive_trees")) ? fmpEntries.get("fertiliser_revised_productive_trees") : null;
        if (outputProductiveTrees != null) {
            String newproductiveTreesText = (newProductiveTreesToFertilise > 0) ? percentageFormat.format(newProductiveTreesToFertilise) : "";
            outputProductiveTrees.setText(newproductiveTreesText);
        }
    }

    private void updateRehabPercent(Editable editable) {
        double newPercent = 0, newRehabSize = 0;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        DecimalFormat decimalFormat = (DecimalFormat)nf;
        decimalFormat.applyPattern("#.##");
        if (editable.length() > 0) {
            Double calcTotal = (fmpCurrentValues.containsKey("planting_recommended_total")) ? Double.valueOf(fmpCurrentValues.get("planting_recommended_total")) : null;
            Double calcPercent = (fmpCurrentValues.containsKey("planting_recommended_percentage")) ? Double.valueOf(fmpCurrentValues.get("planting_recommended_percentage")) : null;

            if (calcTotal == null || calcPercent == null || calcTotal == 0 || calcPercent == 0) {
                return;
            }

            double revisedTotal = Double.valueOf(editable.toString());
            newPercent = (revisedTotal / calcTotal) * calcPercent;

            double recommendedRehabSize = (fmpCurrentValues.containsKey("planting_recommended_farm_size")) ? Double.valueOf(fmpCurrentValues.get("planting_recommended_farm_size")) : 0;

            if(recommendedRehabSize > 0) {
                double calcPercentFraction = calcPercent / 100;
                double totalFarmSize = recommendedRehabSize / calcPercentFraction;

                newRehabSize = totalFarmSize * (newPercent / 100);
            }

        }
        EditText outputPercent = fmpEntries.get("planting_revised_recommended_percentage");
        String outputPercentText = (newPercent > 0) ? decimalFormat.format(newPercent) : "";
        outputPercent.setText(outputPercentText);

        EditText outputRehabSize = (fmpEntries.containsKey("planting_revised_farm_size")) ? fmpEntries.get("planting_revised_farm_size") : null;
        if (outputRehabSize != null) {
            String outputRehabSizeText = (newRehabSize > 0) ? decimalFormat.format(newRehabSize) : "";
            outputRehabSize.setText(outputRehabSizeText);
        }
    }

    private void runReviewCalculations(String internal_name, Editable editable) {
        // next bit
        switch (internal_name) {
            case "fertiliser_revised_recommended_total":
                updateFertiliserPercent(editable);
                break;
            case "planting_revised_recommended_total":
                updateRehabPercent(editable);
                break;
        }
    }

    private void saveReviewValue(long fmpId, int entryId, String entryValue) {
        try {
            if (entryValue.length() > 0) {
                boolean isNew = true;
                for (int i = 0; i < valueSet.size(); i++) {
                    JSONObject currentValue = valueSet.get(i);
                    if (currentValue != null) {
                        int currentFmpId = currentValue.optInt("fmp_id");
                        int currentEntryId = currentValue.optInt("fmp_entry_id");
                        if (currentFmpId == fmpId && currentEntryId == entryId) {
                            currentValue.put("value", entryValue);
                            isNew = false;
                            break;
                        }

                    }
                }
                if (isNew) {
                    JSONObject newValue = new JSONObject();
                    newValue.put("fmp_id", fmpId);
                    newValue.put("fmp_entry_id", entryId);
                    newValue.put("value", entryValue);
                    valueSet.add(newValue);
                }
            } else {
                removeReviewValue(fmpId, entryId);
            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("ReviewFarmManagementPlan: Error saving review value", e));
            e.printStackTrace();
        }
    }

    private void removeReviewValue(long fmpId, int entryId) {
        for (int i = 0; i < valueSet.size(); i++) {
            JSONObject currentValue = valueSet.get(i);
            if (currentValue != null) {
                int currentFmpId = currentValue.optInt("fmp_id");
                int currentEntryId = currentValue.optInt("fmp_entry_id");
                if (currentFmpId == fmpId && currentEntryId == entryId) {
                    valueSet.remove(i);
                    break;
                }

            }
        }
    }

    private void setupFullReview() {
        entriesSection.removeAllViews();
        Cursor fmpBaselineQuestions = dl.getFmpBaselineQuestions(farmerId, fmpId);

        if (fmpBaselineQuestions.getCount() > 0) {
            while (fmpBaselineQuestions.moveToNext()) {
                String displayType = fmpBaselineQuestions.getString(fmpBaselineQuestions.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE));

                if (displayType.toLowerCase().equals("checkbox")) {
                    dynamicQuestions.renderCheckBoxQuestion(fmpBaselineQuestions, dynamicQuestions.questionsSection, farmerId, 0);
                } else if (displayType.toLowerCase().equals("datepicker")) {
                    dynamicQuestions.renderDatePickerQuestion(fmpBaselineQuestions, false, dynamicQuestions.questionsSection, 0, -1, null);
                } else if (displayType.toLowerCase().equals("decimal")) {
                    dynamicQuestions.renderTextBoxQuestion(fmpBaselineQuestions, false, dynamicQuestions.questionsSection, 0, -1, null);
                } else if (displayType.toLowerCase().equals("dropdown")) {
                    dynamicQuestions.renderDropDownQuestion(fmpBaselineQuestions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                } else if (displayType.toLowerCase().equals("photocheckbox")) {
                    dynamicQuestions.renderPhotoCheckboxQuestion(fmpBaselineQuestions, farmerId, 0, res, getPackageName());
                } else if (displayType.toLowerCase().equals("repeater")) {
                    dynamicQuestions.renderRepeaterQuestion(fmpBaselineQuestions, farmerId, olamFarmerId, 0, res);
                } else if (displayType.toLowerCase().equals("textbox")) {
                    dynamicQuestions.renderTextBoxQuestion(fmpBaselineQuestions, false, dynamicQuestions.questionsSection, 0, -1, null);
                } else if (displayType.toLowerCase().equals("yesno")) {
                    dynamicQuestions.renderYesNoQuestion(fmpBaselineQuestions, false, dynamicQuestions.questionsSection, 0, -1, null, res);
                } else if (displayType.toLowerCase().equals("farmtreedensity")) {
                    dynamicQuestions.renderTreeDensityQuestion(fmpBaselineQuestions, res);
                }

            }
        }
    }

    private void bindSaveButton() {
        ImageButton saveButton = (ImageButton) findViewById(R.id.saveEntries);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fullReviewMode) {
                    saveFullFmpReview();
                } else {
                    saveFmpReview();
                }
            }
        });

        Button baseSaveButton = (Button) findViewById(R.id.saveReview);

        baseSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fullReviewMode) {
                    saveFullFmpReview();
                } else {
                    saveFmpReview();
                }
            }
        });
    }

    private void saveFmpReview() {
        if (dl.hasNqi("fullfmpreview", (int)fmpId)) {
            dl.updateSubModuleAnswers(new ArrayList<JSONObject>(), fmpId, 0, farmerId, olamFarmerId, farmId, "fmp");
            dl.deleteFmpNetworkQueueItem("fullfmpreview", farmerId, fmpId);
        }

        if (!dl.hasNqi("fmpreview", (int)fmpId)) {
            statusStarted = 0;
        }
        // first time saving this module?
        dl.updateFmpReviewValues(valueSet, farmerId, fmpId, olamFarmerId, fmpStartYear, statusStarted);
        statusStarted = 1;

        ViewFarmerActivity.farmerId = farmerId;
        //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);
        Intent intent = new Intent(context, ViewFarmerActivity.class);
        context.startActivity(intent);
    }

    private void saveFullFmpReview() {

        if (dl.hasNqi("fmpreview", (int)fmpId)) {
            dl.deleteFmpReviewValues(fmpId);
            dl.deleteFmpNetworkQueueItem("fmpreview", farmerId, fmpId);
        }

        if (!dl.hasNqi("fullfmpreview", (int)fmpId)) {
            statusStarted = 0;
        }

        if (statusStarted == 0) {
            ContentValues nqiValues = new ContentValues();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "fullfmpreview");
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, res.getString(R.string.fullreview_farm_management_plan));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, res.getString(R.string.fullreview_farm_management_plan_in_progress));
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, farmerId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FMP_ID, fmpId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, farmId);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED, fmpStartYear);
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
            nqiValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
            dl.insertNetworkQueueEntry(nqiValues);
            statusStarted = 1;
        }
        dl.updateSubModuleAnswers(dynamicQuestions.answerSet, fmpId, 0, farmerId, olamFarmerId, farmId, "fmp");

        ViewFarmerActivity.farmerId = farmerId;
        //SharedPreferenceUtils.getInstance(getApplicationContext()).setFarmerID(farmerId);

        Intent intent = new Intent(context, ViewFarmerActivity.class);
        context.startActivity(intent);
    }
}
