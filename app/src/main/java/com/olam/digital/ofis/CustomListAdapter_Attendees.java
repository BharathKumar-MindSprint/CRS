package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.util.Hashtable;

/**
 * Created by ben on 06/07/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
class CustomListAdapter_Attendees extends SimpleCursorAdapter {

    private final int layout;
    private final LayoutInflater inflater;
    private final Resources res;
    private final TrainingAttendees trainingAttendees;

    CustomListAdapter_Attendees(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, TrainingAttendees trainingAttendees_) {
        super(c, R.layout.listview_attendee_list, cursor, adapterCols, adapterRowViews, 0);
        this.layout = R.layout.listview_attendee_list;
        this.inflater = LayoutInflater.from(c);
        this.res = res_;
        this.trainingAttendees = trainingAttendees_;
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
            ImageView farmerPhoto = (ImageView) view.findViewById(R.id.farmerListIcon);
            farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_no_photo));
            long id = cursor.getLong(cursor.getColumnIndex("_id"));
            int localColIndex = cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY);

            if (localColIndex != -1 && cursor.getInt(localColIndex) == 1) {
                String farmerPhotoPath = cursor.getString(cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
                if (farmerPhotoPath != null && farmerPhotoPath.length() > 0) {
                    // set our desired width and height for display
                    int baseWidth = 68;
                    int baseHeight = 60;

                    Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                    Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                    Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

                    Bitmap existingBitmap = Utilities.loadSizedBitmap(farmerPhotoPath, outputWidth, outputHeight);

                    farmerPhoto.setImageBitmap(existingBitmap);
                }
            } else {
                farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_no_photo));
            }

            ImageView cbImage = (ImageView) view.findViewById(R.id.cbImage);
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.rowlin);
            TextView state = (TextView) view.findViewById(R.id.attendeeStatus);

            if (trainingAttendees.selectedFarmers.contains(id)) {
                cbImage.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_on_background));
                linearLayout.setBackgroundResource(R.drawable.list_item_selected);
                state.setText("true");
            } else {
                cbImage.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_off_background));
                linearLayout.setBackgroundResource(R.drawable.list_item);
                state.setText("false");
            }
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("CustomListAdapter_Attendees: Error setting row value", e));
            e.printStackTrace();
        }
    }
}
