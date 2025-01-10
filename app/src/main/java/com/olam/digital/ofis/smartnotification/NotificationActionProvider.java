package com.olam.digital.ofis.smartnotification;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Keep;
import androidx.core.view.ActionProvider;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;

import java.util.List;

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
                Intent intent = new Intent(mContext, AllNotificationActivity.class);
                mContext.startActivity(intent);
            }
        });
    }

    private void updateTaskCount() {
        DataLayer dl = new DataLayer(mContext.getResources(), mContext);
        OFISRoomDB db = OFISRoomDB.getDbInstance(mContext);
        int notificationCount = dl.getNotificationCount();
        int EUDRGroundSurveyCount = 0;

        if (notificationCount <= 0) {
            EUDRGroundSurveyCount = db.onGroundSurveyNotificationDao().getOnGroundPendingSurveyFarmers().size();
        }

        ImageView listCount = (ImageView) view.findViewById(R.id.notificationListCount);

        if (notificationCount > 0 || EUDRGroundSurveyCount > 0) {
            listCount.setVisibility(View.VISIBLE);
        } else {
            listCount.setVisibility(View.GONE);
        }
    }
}
