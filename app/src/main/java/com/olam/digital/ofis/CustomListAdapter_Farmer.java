package com.olam.digital.ofis;

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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import org.w3c.dom.Text;

import java.util.Hashtable;

/**
 * Created by bengallienne on 03/12/14.
 * Creates and binds the layouts and functions for an item in the find farmer list
 */
class CustomListAdapter_Farmer extends SimpleCursorAdapter {

    private final int layout;
    private final LayoutInflater inflater;
    private final Resources res;
    private boolean transactionMode = false;

    CustomListAdapter_Farmer(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, boolean _transactionMode) {
        super(c, R.layout.listview_farmer_list, cursor, adapterCols, adapterRowViews, 0);
        this.layout = R.layout.listview_farmer_list;
        this.inflater = LayoutInflater.from(c);
        this.res = res_;
        this.transactionMode = _transactionMode;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
        super.bindView(view, context, cursor);
        try {
            ImageView farmerPhoto = (ImageView) view.findViewById(R.id.farmerListIcon);
            TextView ofisID = (TextView) view.findViewById(R.id.farmerListOlamFarmerId);
            TextView odID = (TextView) view.findViewById(R.id.farmerListODFarmerId);
            int statusColIndex = cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED);
            int localColIndex = cursor.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY);


            if (transactionMode) {
                TextView transacteetype = (TextView) view.findViewById(R.id.transacteeType);
                transacteetype.setText(cursor.getString(cursor.getColumnIndex("transactee_type")));
            }
            farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_no_photo));
            if (statusColIndex != -1 && cursor.getInt(statusColIndex) == 1) {
                farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_saved));
            } else if (localColIndex != -1 && cursor.getInt(localColIndex) == 1) {
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
                if (transactionMode && cursor.getString(cursor.getColumnIndex("transactee_type")).equalsIgnoreCase("agent")) {
                    farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_buying_agent));
                } else {
                    farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_no_photo));
                }
            }

            ofisID.setText("OFIS ID: "+cursor.getString(cursor.getColumnIndex("olam_farmer_id")));
            String country_id = cursor.getString(cursor.getColumnIndex("country_id"));
            DataLayer dl = new DataLayer(res,context);

//            boolean isODOrigin = dl.isODOrigin(Integer.parseInt(country_id));
            boolean isODOrigin=false;

            if(isODOrigin) {
                String odidvalue = cursor.getString(cursor.getColumnIndex("odFarmerId"));
                if(odidvalue != null && odidvalue != "null") {
                    odID.setText("OD ID:   " + odidvalue);
                    odID.setVisibility(View.VISIBLE);
                }else
                    odID.setVisibility(View.GONE);
            }else {
                odID.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("CustomListAdapter_Farmer: Error setting row value", e));
            e.printStackTrace();
        }
    }
}
