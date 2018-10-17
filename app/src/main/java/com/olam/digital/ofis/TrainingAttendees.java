package com.olam.digital.ofis;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.olam.digital.ofis.R;

import java.util.ArrayList;

public class TrainingAttendees extends BaseListActivity {

    private Context context;
    private TrainingAttendees me;
    private Resources res;
    private Cursor farmerList;
    private DataLayer dl;
    private CustomListAdapter_Attendees customListAdapter_attendees;

    private final static String EDIT_TRAININGRECORD_ID = "EDIT_TRAININGRECORD_ID";
    private final static String COURSE_ID = "COURSE_ID";
    private final static String TRAINING_MODULE_ID = "TRAINING_MODULE_ID";
    private final static String TRAINING_PHOTO = "TRAINING_PHOTO";
    private final static String EDIT_MODE = "EDIT_MODE";

    private long trainingRecordId, courseId, trainingModuleId;
    private long selectedFarmerGroup = 0, selectedSection = 0;
    private String searchQuery = "", mCurrentPhotoPath = null;

    private EditText ed;
    private TextView attendeeCount;
    private GradientDrawable attendeeCountBackground;
    private Spinner farmerGroupSpinner, sectionSpinner;

    public ArrayList<Long> selectedFarmers;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_attendees);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        context = me = this;
        res = getResources();

        dl = new DataLayer(res, context);

        farmerGroupSpinner = (Spinner) findViewById(R.id.spinner_farmerGroup);
        sectionSpinner = (Spinner) findViewById(R.id.spinner_section);
        ed = (EditText) findViewById(R.id.findFarmerSearchBox);

        Intent intent = this.getIntent();
        trainingRecordId = intent.getLongExtra(EDIT_TRAININGRECORD_ID, 0);
        courseId = intent.getLongExtra(COURSE_ID, 0);
        trainingModuleId = intent.getLongExtra(TRAINING_MODULE_ID, 0);
        boolean editMode = intent.getBooleanExtra(EDIT_MODE, false);
        mCurrentPhotoPath = intent.getStringExtra(TRAINING_PHOTO);

        selectedFarmers = new ArrayList<>();
        attendeeCount = (TextView) findViewById(R.id.attendeeCount);
        attendeeCountBackground = (GradientDrawable) attendeeCount.getBackground();

        if (editMode) {
            getExistingAttendees();
        }

        bindFarmerList();
        showFarmerList();
        setUpSearchFunctionality();
        bindButtons();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        ImageView imageView = (ImageView) v.findViewById(R.id.cbImage);
        TextView state = (TextView) v.findViewById(R.id.attendeeStatus);
        LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.rowlin);
        boolean selectedState = Boolean.parseBoolean(state.getText().toString());

        if (selectedState) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_off_background));
            linearLayout.setBackgroundResource(R.drawable.list_item);
            state.setText("false");
            selectedFarmers.remove(id);
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_on_background));
            linearLayout.setBackgroundResource(R.drawable.list_item_selected);
            state.setText("true");
            selectedFarmers.add(id);
        }
        animateAttendeeSelection();
        attendeeCount.setText(String.valueOf(selectedFarmers.size()));
    }

    private void getExistingAttendees() {
        Cursor existingAttendees = dl.getTrainingAttendeeIdsByTrainingRecordId(trainingRecordId);

        if (existingAttendees.getCount() > 0) {
            while (existingAttendees.moveToNext()) {
                selectedFarmers.add(existingAttendees.getLong(existingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID)));
            }
            attendeeCount.setText(String.valueOf(existingAttendees.getCount()));
        }
    }

    private void bindButtons() {
        Button addSelectedAttendees = (Button) findViewById(R.id.addSelectedAttendees);
        addSelectedAttendees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resultCount = 0;
                if (trainingModuleId > 0 && trainingRecordId == 0) {
                    ContentValues newRecord = new ContentValues();
                    if (mCurrentPhotoPath != null) {
                        newRecord.put(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO, mCurrentPhotoPath);
                    }

                    newRecord.put(OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID, trainingModuleId);
                    trainingRecordId = dl.insertTrainingRecord(newRecord);
                }

                if (selectedFarmers.size() > 0) {
                    resultCount = dl.updateTrainingModuleAttendees(selectedFarmers, trainingRecordId);
                }

                Toast.makeText(context, resultCount + " " + res.getString(R.string.attendees_saved), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, TrainingRecord.class);
                intent.putExtra(EDIT_TRAININGRECORD_ID, trainingRecordId);
                intent.putExtra(COURSE_ID, courseId);
                intent.putExtra(TRAINING_MODULE_ID, trainingModuleId);
                intent.putExtra(TRAINING_PHOTO, mCurrentPhotoPath);
                intent.putExtra(EDIT_MODE, true);
                context.startActivity(intent);
            }
        });
    }

    private void bindFarmerList() {
        farmerList = dl.getTrainingAttendeeList(searchQuery,selectedFarmerGroup,selectedSection, courseId);
        if (farmerList.getCount() == 0) {
            farmerList = dl.getFindFarmerList(searchQuery,selectedFarmerGroup,selectedSection);
        }
    }

    private void showFarmerList() {
        if (farmerList.getCount() > 0) {
            String[] adapterCols = new String[]{
                    "name",
                    "olam_farmer_id"
            };
            int[] adapterRowViews = new int[]{R.id.farmerListFarmerName, R.id.farmerListOlamFarmerId};
            customListAdapter_attendees = new CustomListAdapter_Attendees(context, farmerList, adapterCols, adapterRowViews, res, me);
            setListAdapter(customListAdapter_attendees);

        }
    }

    private void setUpSearchFunctionality()
    {
        Cursor farmerGroups = dl.getSpinnerData(OFISDBContract.FarmerGroupsEntry.TABLE_NAME, false, null, null, true, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME);
        dl.bindDataDrivenSpinner(farmerGroupSpinner, farmerGroups, context, R.string.spinner_farmergroup_default, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME, true);

        farmerGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor sections = dl.getSpinnerData(OFISDBContract.SectionsEntry.TABLE_NAME, true, OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID, ((int) id), true, OFISDBContract.SectionsEntry.COLUMN_NAME_SECTION_NAME);
                dl.bindDataDrivenSpinner(sectionSpinner, sections, context, R.string.spinner_section_default, OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME, true);

                selectedFarmerGroup = id;
                selectedSection = 0;
                doSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSection = id;
                doSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ed.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s) {  }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                //searches and populates the list with the appropriate info
                searchQuery = String.valueOf(ed.getText());
                doSearch();
            }
        });
    }

    //searches and populates the list with the appropriate info
    private void doSearch()
    {
        farmerList.close();
        bindFarmerList();
        runOnUiThread(returnRes);//thread redoes list and notifies etc. so it all updates nicely with results of search
    }

    private final Runnable returnRes = new Runnable() {

        public void run() {
            if (customListAdapter_attendees != null) {
                customListAdapter_attendees.swapCursor(farmerList);
            }

        }
    };

    private void animateAttendeeSelection()
    {
        Integer colorFrom = 0xFFEDE5DC;
        Integer colorTo = 0xFFFFFFFF;
        attendeeCountBackground.setColor(colorFrom);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator)
            {
                Integer colour = (Integer)animator.getAnimatedValue();
                //attendeeCount.setBackgroundColor(colour);
                attendeeCountBackground.setColor(colour);
            }

        });
        colorAnimation.setDuration(820);
        colorAnimation.setInterpolator(new AccelerateInterpolator(0.8f));
        colorAnimation.start();
    }
}
