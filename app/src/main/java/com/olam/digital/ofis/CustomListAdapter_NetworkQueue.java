package com.olam.digital.ofis;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.olam.digital.ofis.R;

/**
 * Created by ben on 07/12/14.
 * Creates and binds the layouts and functions for an item in the network queue list
 */
class CustomListAdapter_NetworkQueue extends SimpleCursorAdapter {

    private final int layout;
    private final LayoutInflater inflater;
    private final HomeActivity homeActivity;

    public CustomListAdapter_NetworkQueue(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, HomeActivity ha) {
        super(c, R.layout.listview_network_queue, cursor, adapterCols, adapterRowViews, 0);
         this.layout = R.layout.listview_network_queue;
        this.inflater = LayoutInflater.from(c);
        this.homeActivity = ha;
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

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemId = Integer.parseInt(String.valueOf(networkQueueId.getText()));
                homeActivity.startItemSyncAuth(itemId);
            }
        });
    }

}
