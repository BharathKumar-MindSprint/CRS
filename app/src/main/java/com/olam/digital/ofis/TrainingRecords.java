package com.olam.digital.ofis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TrainingRecords extends BaseListActivity implements AdapterView.OnItemClickListener {

    private Context context;
    private Resources res;
    private DataLayer dl;
    private TrainingRecords me;

    private CustomListAdapter_TrainingRecords customListAdapter_trainingRecords;

    private final static String EDIT_TRAININGRECORD_ID = "EDIT_TRAININGRECORD_ID";
    private final static String EDIT_MODE = "EDIT_MODE";
    private final static String READ_ONLY_MODE = "READ_ONLY_MODE";

    private Cursor trainingRecords;
    private TextView noTrainingRecordsText;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_records);
        listView=findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = me = this;
        res = getResources();

        dl = new DataLayer(res, context);

        noTrainingRecordsText = (TextView) findViewById(R.id.notrainingrecordstext);

        getTrainingRecords();
        bindButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getTrainingRecords() {
        trainingRecords = dl.getTrainingRecords();

        if (trainingRecords.getCount() > 0) {
            noTrainingRecordsText.setVisibility(View.GONE);

            String[] adapterCols = new String[]{"_id", "course_date", "course_name", "module_name", OFISDBContract.TrainingRecord.COLUMN_NAME_IN_QUEUE ,"attendee_count"};
            int[] adapterRowViews = new int[]{R.id.trainingRecordId, R.id.trainingDate, R.id.trainingCourse, R.id.trainingModule, R.id.readOnlyMode, R.id.attendeeCount};
            customListAdapter_trainingRecords = new CustomListAdapter_TrainingRecords(context, trainingRecords, adapterCols, adapterRowViews, res, me);
            listView.setAdapter(customListAdapter_trainingRecords);
            //setListAdapter(customListAdapter_trainingRecords);
        } else {
            noTrainingRecordsText.setVisibility(View.VISIBLE);
        }
    }

    public void deleteTrainingRecord(final long trainingRecordId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setCancelable(true)
                .setNegativeButton(res.getString(R.string.app_update_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setMessage(res.getString(R.string.trainingrecord_are_you_sure))
                .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dl.deleteTrainingRecord(trainingRecordId) > 0) {
                            trainingRecords.close();
                            trainingRecords = dl.getTrainingRecords();
                            customListAdapter_trainingRecords.swapCursor(trainingRecords);
                            if (trainingRecords.getCount() > 0) {
                                noTrainingRecordsText.setVisibility(View.GONE);
                            } else {
                                noTrainingRecordsText.setVisibility(View.VISIBLE);
                            }
                            Toast.makeText(context, res.getString(R.string.trainingrecord_deleted), Toast.LENGTH_LONG).show();
                        }

                        // delete training record
                        try {
                             dl.deleteTrainingAttendeeRelations(trainingRecordId);
                        }catch (Exception e){}


                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

//    @Override
//    protected void onListItemClick (ListView l, View v, int position, long id) {
//
//        //we now view this training record
//        Intent intent = new Intent(context, TrainingRecord.class);
//        intent.putExtra(EDIT_TRAININGRECORD_ID, id);
//        intent.putExtra(EDIT_MODE, true);
//
//        TextView readOnlyValue = (TextView) v.findViewById(R.id.readOnlyMode);
//        if (readOnlyValue.getText().equals("1")) {
//            intent.putExtra(READ_ONLY_MODE, true);
//        }
//
//        context.startActivity(intent);
//    }

    private void bindButtons() {
        ImageButton addTrainingRecord = (ImageButton) findViewById(R.id.addTrainingRecord);

        addTrainingRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TrainingRecord.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        //we now view this training record
        Intent intent = new Intent(context, TrainingRecord.class);
        intent.putExtra(EDIT_TRAININGRECORD_ID, id);
        intent.putExtra(EDIT_MODE, true);

        TextView readOnlyValue = (TextView) v.findViewById(R.id.readOnlyMode);
        if (readOnlyValue.getText().equals("1")) {
            intent.putExtra(READ_ONLY_MODE, true);
        }

        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
