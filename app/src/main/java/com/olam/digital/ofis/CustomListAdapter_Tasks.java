package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ben on 06/07/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
class CustomListAdapter_Tasks extends SimpleCursorAdapter {

    private final int layout;
    private final LayoutInflater inflater;
    private final Resources res;

    CustomListAdapter_Tasks(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_) {
        super(c, R.layout.listview_task_list, cursor, adapterCols, adapterRowViews, 0);
        this.layout = R.layout.listview_task_list;
        this.inflater = LayoutInflater.from(c);
        this.res = res_;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
        super.bindView(view, context, cursor);
        try {
            int completed = cursor.getInt(cursor.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_COMPLETED));
            ImageView cbImage = (ImageView) view.findViewById(R.id.cbImage);
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.rowlin);
            TextView taskText = (TextView) view.findViewById(R.id.taskListItemText);
            TextView taskDate = (TextView) view.findViewById(R.id.taskListItemDate);
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);

            if (completed == 1) {
                cbImage.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_on_background));
                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                taskText.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                taskText.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.ofis_small_text));
                taskDate.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                Date dbDate = inputFormat.parse(cursor.getString(cursor.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE)));
                String viewDate = outputFormat.format(dbDate);
                taskDate.setText(viewDate);
            } else {
                cbImage.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_off_background));
                linearLayout.setBackgroundResource(R.drawable.list_item);
                taskText.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                taskText.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.ofis_med_text));
                taskDate.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                Date createdDate = inputFormat.parse(taskDate.getText().toString());
                String creator = cursor.getString(cursor.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_TASK_CREATOR));
                taskDate.setText(creator + " | " + outputFormat.format(createdDate));
            }
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("CustomListAdapter_Tasks: Error setting row values", e));
            e.printStackTrace();
        }
    }
}
