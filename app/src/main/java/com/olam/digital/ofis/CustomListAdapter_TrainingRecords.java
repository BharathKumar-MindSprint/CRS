package com.olam.digital.ofis;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ben on 06/07/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
class CustomListAdapter_TrainingRecords extends SimpleCursorAdapter {
    private final int layout;
    private final LayoutInflater inflater;
    private final Resources res;
    private final TrainingRecords trainingRecords;
    private final SimpleDateFormat inputFormat;
    private final SimpleDateFormat outputFormat;

    public CustomListAdapter_TrainingRecords(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, TrainingRecords trainingRecords_) {
        super(c, R.layout.listview_trainingrecords_list, cursor, adapterCols, adapterRowViews, 0);
        this.layout = R.layout.listview_trainingrecords_list;
        this.inflater = LayoutInflater.from(c);
        this.res = res_;
        this.trainingRecords = trainingRecords_;
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        this.outputFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);
    }

    @Override
    public View newView (Context context, final Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(@NonNull View view, final Context context, @NonNull final Cursor cursor) {
        super.bindView(view, context, cursor);
        final TextView trainingRecordId = (TextView) view.findViewById(R.id.trainingRecordId);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.rowlin);
        ImageView imageView = (ImageView) view.findViewById(R.id.deleteTrainingRecord);
        TextView status = (TextView) view.findViewById(R.id.inNetworkQueue);
        TextView trainingDate = (TextView) view.findViewById(R.id.trainingDate);
        try {
            Date dbDate = inputFormat.parse(cursor.getString(cursor.getColumnIndex("course_date")));
            String viewDate = outputFormat.format(dbDate);
            trainingDate.setText(viewDate);
        } catch (ParseException e) {
            FirebaseCrash.report(new Exception("CustomListAdapter_TrainingRecords: Error setting course date values", e));
            e.printStackTrace();
        }

        if (cursor.getInt(cursor.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_IN_QUEUE)) == 1) {
            status.setText(res.getString(R.string.trainingrecord_confirmed));
            linearLayout.setBackgroundResource(R.drawable.list_item);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long itemId = Long.parseLong(String.valueOf(trainingRecordId.getText()));
                trainingRecords.deleteTrainingRecord(itemId);
            }
        });
    }
}
