package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.matoma.MatomoHelper;
import com.olam.digital.ofis.matoma.ScreenNameConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class TrainingAttendeeScores extends BaseActivity {

    private Context context;
    private Resources res;
    private DataLayer dl;

    private final static String EDIT_TRAININGRECORD_ID = "EDIT_TRAININGRECORD_ID";
    private final static String EDIT_MODE = "EDIT_MODE";

    private long trainingRecordId;
    private JSONObject scoringColumns;
    private boolean editMode;

    private String columnSuffix;

    private LayoutInflater inflater;
    private LinearLayout attendeeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_attendee_scores);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        res = getResources();

        dl = new DataLayer(res, context);

        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        attendeeList = (LinearLayout) findViewById(R.id.attendeeList);

        Intent intent = getIntent();
        trainingRecordId = intent.getLongExtra(EDIT_TRAININGRECORD_ID, 0);
        editMode = intent.getBooleanExtra(EDIT_MODE, true);

        columnSuffix = Utilities.getLocaleSuffix(res);
        if (columnSuffix.equalsIgnoreCase("_en")) {
            columnSuffix = "";
        }

        getTrainingRecord();
        getTrainingAttendees();
        bindButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MatomoHelper.matomoScreenTracker(ScreenNameConstants.SCREEN_TRAINING_RECORD_ACTIVITY, ScreenNameConstants.SCREEN_TRAINING_ATTENDEE_SCORES_ACTIVITY, ScreenNameConstants.SCREEN_TRAINING_ATTENDEE_SCORE);
    }

    private void getTrainingRecord() {
        Cursor trainingRecord = dl.getTrainingRecordById(trainingRecordId, columnSuffix);

        if (trainingRecord.moveToFirst()) {
            try {
                scoringColumns = new JSONObject(trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.CompletionCriteria.COLUMN_NAME_SCORING_COLUMNS)));
            } catch(JSONException e) {
                FirebaseCrash.report(new Exception("TrainingAttendeeScores: Error getting scoring columns from training record", e));
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void getTrainingAttendees() {
        Cursor trainingAttendees = dl.getTrainingAttendeesByTrainingRecordId(trainingRecordId);

        if (trainingAttendees.getCount() > 0) {
            while (trainingAttendees.moveToNext()) {
                LinearLayout attendeeRow = (LinearLayout) inflater.inflate(R.layout.activity_training_attendee_score_row, attendeeList, false);
                TextView attendeeId = (TextView) attendeeRow.findViewById(R.id.trainingAttendeeId);
                TextView attendeeName = (TextView) attendeeRow.findViewById(R.id.attendeeName);

                if (attendeeId != null) {
                    attendeeId.setText(trainingAttendees.getString(trainingAttendees.getColumnIndex("_id")));
                }

                if (attendeeName != null) {
                    attendeeName.setText(trainingAttendees.getString(trainingAttendees.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                }

                // show and label the correct scoring columns
                if (scoringColumns!=null && scoringColumns.length() > 0) {
                    int columnCount = 0;
                    Iterator<String> columnKeys = scoringColumns.keys();
                    while (columnKeys.hasNext()) {
                        String key = columnKeys.next();
                        try {
                            JSONObject value = scoringColumns.getJSONObject(key);
                            String label = value.optString("label" + columnSuffix, "label");
                            LinearLayout scoreWrapper = (LinearLayout) attendeeRow.findViewById(res.getIdentifier("wrapper_"+key, "id", getPackageName()));
                            TextView scoreLabel = (TextView) scoreWrapper.findViewById(res.getIdentifier("label_"+key, "id", getPackageName()));
                            View scoreView = scoreWrapper.getChildAt(1);

                            if (columnCount == 0) {
                                scoreWrapper.setBackgroundResource(0);
                                scoreWrapper.setPadding(0,0,0,0);
                                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) scoreWrapper.getLayoutParams();
                                mlp.setMargins(0, 0, 0, 0);
                            }

                            if (scoreLabel != null) {
                                scoreLabel.setText(label + ": ");
                            }

                            if (scoreView != null) {
                                if (scoreView instanceof CheckBox) {
                                    final CheckBox scoreColumn = (CheckBox) scoreView;
                                    boolean isChecked = (trainingAttendees.getInt(trainingAttendees.getColumnIndex(key)) == 1);
                                    scoreColumn.setChecked(isChecked);
                                } else {
                                    EditText scoreColumn = (EditText) scoreView;
                                    scoreColumn.setText((!trainingAttendees.isNull(trainingAttendees.getColumnIndex(key))) ? trainingAttendees.getString(trainingAttendees.getColumnIndex(key)) : "");

                                    if (key.equalsIgnoreCase("percentage")) {
                                        scoreColumn.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
                                    }
                                }
                            }

                            scoreWrapper.setVisibility(View.VISIBLE);

                            columnCount++;
                        } catch (JSONException e) {
                            FirebaseCrash.report(new Exception("TrainingAttendeeScores: Error getting scoring columns for training attendees", e));
                            e.printStackTrace();
                        }
                    }
                }

                attendeeList.addView(attendeeRow);

            }
        }
    }

    private void bindButtons() {
        Button saveScores = (Button) findViewById(R.id.saveScores);

        saveScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save scores against attendees
                int attendeeCount = attendeeList.getChildCount();
                int updateCount = 0;

                for (int i = 0; i < attendeeCount; i++) {
                    LinearLayout attendeeRow = (LinearLayout) attendeeList.getChildAt(i);
                    TextView attendeeIdView = (TextView) attendeeRow.findViewById(R.id.trainingAttendeeId);
                    String attendeeId = attendeeIdView.getText().toString();
                    ContentValues scoreValues = new ContentValues();

                    // gather the correct scoring column values
                    if (scoringColumns!=null && scoringColumns.length() > 0) {
                        Iterator<String> columnKeys = scoringColumns.keys();
                        while (columnKeys.hasNext()) {
                            String key = columnKeys.next();
                            LinearLayout scoreWrapper = (LinearLayout) attendeeRow.findViewById(res.getIdentifier("wrapper_"+key, "id", getPackageName()));
                            View scoreView = scoreWrapper.getChildAt(1);

                            if (scoreView != null) {
                                if (scoreView instanceof CheckBox) {
                                    scoreValues.put(key, ((CheckBox) scoreView).isChecked());
                                } else {
                                    scoreValues.put(key, ((EditText) scoreView).getText().toString());
                                }
                            }
                        }
                    }

                    updateCount += dl.updateAttendeeScore(scoreValues, attendeeId);
                }

                Toast.makeText(context, updateCount + " " + res.getString(R.string.attendees_scores_updated), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, TrainingRecord.class);
                intent.putExtra(EDIT_TRAININGRECORD_ID, trainingRecordId);
                intent.putExtra(EDIT_MODE, editMode);
                context.startActivity(intent);
            }
        });

    }

    private class InputFilterMinMax implements InputFilter {
        private final double min;
        private final double max;

        public InputFilterMinMax(double min, double max) {
            this.min = min;
            this.max = max;
        }

        private boolean isInRange(double a, double b, double c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                double input = Double.parseDouble(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) {
                FirebaseCrash.report(new Exception("TrainingAttendeeScores: Error filtering min max text on score", nfe));
                nfe.printStackTrace();
            }
            return "";
        }

    }
}
