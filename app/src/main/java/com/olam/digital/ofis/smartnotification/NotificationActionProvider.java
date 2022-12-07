package com.olam.digital.ofis.smartnotification;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.core.view.ActionProvider;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;

/**
 * Task action provider. Created by ben on 08/11/2016.
 */

@Keep
public class NotificationActionProvider extends ActionProvider {

    private final Context mContext;
    private LinearLayout view;

    public NotificationActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = (LinearLayout) inflater.inflate(R.layout.menu_item_notification, null);
        bindButton(view);
        updateTaskCount();
        return view;
    }

    @Override
    public View onCreateActionView(MenuItem forItem) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = (LinearLayout) inflater.inflate(R.layout.menu_item_notification, null);
        bindButton(view);
        updateTaskCount();
        return view;
    }

    private void bindButton(LinearLayout view) {
        ImageButton taskIcon = (ImageButton) view.findViewById(R.id.notificationListIcon);
        taskIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NotificationActivity.class);
                mContext.startActivity(intent);
            }
        });
    }

    private void updateTaskCount() {
        DataLayer dl = new DataLayer(mContext.getResources(), mContext);
        int notificationCount = dl.getNotificationCount();
        TextView listCount = (TextView) view.findViewById(R.id.notificationListCount);
        listCount.setMinWidth(70);
        if (notificationCount > 0) {
            if (notificationCount >= 1 && notificationCount < 100) {
                listCount.setText(String.format("%02d",notificationCount));
            }else if (notificationCount >= 100 && notificationCount < 1000){
                listCount.setText("99+");
            }else if (notificationCount >= 1000 && notificationCount < 10000){
                int firstDigit = Integer.parseInt(Integer.toString(notificationCount).substring(0, 1));
                listCount.setText(firstDigit + "k+");
            }else if (notificationCount >= 10000){
                listCount.setText("10k+");
            }
            listCount.setVisibility(View.VISIBLE);
        } else {
            listCount.setVisibility(View.GONE);
        }
    }
}
