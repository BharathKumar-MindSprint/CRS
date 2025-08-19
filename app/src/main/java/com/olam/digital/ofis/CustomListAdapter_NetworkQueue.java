package com.olam.digital.ofis;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by ben on 07/12/14.
 * Creates and binds the layouts and functions for an item in the network queue list
 */
class CustomListAdapter_NetworkQueue extends SimpleCursorAdapter {

    private final int layout;
    private final LayoutInflater inflater;
    private final HomeActivity homeActivity;
    private long mLastClickTime = 0;
    public boolean isClickable;
    public Resources res;

    public CustomListAdapter_NetworkQueue(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, HomeActivity ha, boolean isClickable) {
        super(c, R.layout.listview_network_queue, cursor, adapterCols, adapterRowViews, 0);
         this.layout = R.layout.listview_network_queue;
        this.inflater = LayoutInflater.from(c);
        this.homeActivity = ha;
        this.isClickable = isClickable;
        this.res = c.getResources();
    }


    @Override
    public View newView (Context context, final Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(@NonNull View view, final Context context, @NonNull final Cursor cursor) {
        super.bindView(view, context, cursor);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView_pic);
        final TextView networkQueueId = (TextView) view.findViewById(R.id.networkQueueId);

        if (isClickable) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_list));
            imageView.setEnabled(true);
        } else {
            imageView.setEnabled(false);
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_sync_black));
        }

        String itemType = cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE));
        String subType = cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE));
        if (itemType.equalsIgnoreCase("farmpolygon") && (subType != null && subType.equalsIgnoreCase(res.getString(R.string.flag_for_de_activation)))) {
            TextView tvSubGroupType = view.findViewById(R.id.tvSubGroupType);
            tvSubGroupType.setVisibility(View.VISIBLE);
            tvSubGroupType.setText(subType);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemId = Integer.parseInt(String.valueOf(networkQueueId.getText()));
                homeActivity.startItemSyncAuth(itemId);
            }
        });
    }

}
