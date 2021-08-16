package com.olam.digital.ofis;

import android.content.Context;
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
    public boolean isClickable = true;
    ImageView imageView;
    Context context;

    public CustomListAdapter_NetworkQueue(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, HomeActivity ha) {
        super(c, R.layout.listview_network_queue, cursor, adapterCols, adapterRowViews, 0);
         this.layout = R.layout.listview_network_queue;
        this.inflater = LayoutInflater.from(c);
        this.homeActivity = ha;
        this.context = c;
    }

    public void onClick(boolean isClickable) {
        if(!isClickable){
            imageView.setEnabled(false);
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_sync_black));
        }else{
            imageView.setEnabled(true);
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_list));
        }
            return;
        // do your click stuff
    }

    @Override
    public View newView (Context context, final Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(@NonNull View view, final Context context, @NonNull final Cursor cursor) {
        super.bindView(view, context, cursor);
        imageView = (ImageView) view.findViewById(R.id.imageView_pic);
        final TextView networkQueueId = (TextView) view.findViewById(R.id.networkQueueId);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setEnabled(false);
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_sync_black));

                int itemId = Integer.parseInt(String.valueOf(networkQueueId.getText()));
                homeActivity.startItemSyncAuth(itemId);
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_list));
                        imageView.setEnabled(true);
                    }
                }, 5000);
            }
        });
    }

}
