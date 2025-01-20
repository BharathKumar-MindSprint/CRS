package com.olam.digital.ofis.smartnotification;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.BaseListActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.FindFarmerActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.smartnotification.adapter.AllNotificationAdapter;
import com.olam.digital.ofis.smartnotification.dto.ModuleListDTO;
import com.olam.digital.ofis.smartnotification.dto.NotificationDTO;
import com.olam.digital.ofis.smartnotification.listener.OnItemNotificationClickListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AllNotificationActivity extends BaseListActivity {

    private RecyclerView recyclerAllNotification;
    private Context mContext;

    public Resources res;

    public DataLayer dl;
    public OFISRoomDB db;

    private List<ModuleListDTO> notifications;
    private Map<String, Object[]> notificationMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = this;
        res = getResources();
        dl = new DataLayer(res, this);
        db = OFISRoomDB.getDbInstance(mContext);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        recyclerAllNotification = findViewById(R.id.recyclerAllNotification);
        TextView noNotificationText = findViewById(R.id.tv_no_notification);
        createNotificationList();

        if (!notifications.isEmpty()) {
            setRecyclerView();
        }
        else {
            recyclerAllNotification.setVisibility(View.GONE);
            noNotificationText.setVisibility(View.VISIBLE);
        }
    }

    private void setRecyclerView() {
        recyclerAllNotification.setVisibility(View.VISIBLE);
        recyclerAllNotification.setLayoutManager(new LinearLayoutManager(this));
        AllNotificationAdapter adapter = new AllNotificationAdapter(notifications);
        recyclerAllNotification.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemNotificationClickListener() {
            @Override
            public void onNotificationClick(View view, int position, NotificationDTO notificationDTO) {

            }

            @Override
            public void onModuleClick(View view, int position, ModuleListDTO moduleListDTO) {
                String notificationType = moduleListDTO.getModuleName();
                Object[] notificationData = notificationMap.get(notificationType);

                if (notificationData != null) {
                    Class<?> destination = (Class<?>) notificationData[0];
                    String intentValue = (String) notificationData[1];
                    String key = (String) notificationData[2];

                    Intent intent = new Intent(mContext, destination);
                    intent.putExtra(key, intentValue);
                    mContext.startActivity(intent);
                }

            }
        });
    }


    private void createNotificationList() {
        notifications = new ArrayList<>();
        notificationMap = new HashMap<>();

        notificationMap.put(res.getString(R.string.fdp), new Object[]{NotificationActivity.class, Constants.FDP, Utilities.MODULE});
        notificationMap.put(res.getString(R.string.clmrs), new Object[]{NotificationActivity.class, Constants.CLMRS, Utilities.MODULE});
        notificationMap.put(res.getString(R.string.eudr), new Object[]{FindFarmerActivity.class, Constants.EUDR, Utilities.NOTIFICATION_TYPE});
        notificationMap.put(res.getString(R.string.pa), new Object[]{FindFarmerActivity.class, Constants.PA, Utilities.NOTIFICATION_TYPE});

        Map<String, Integer> notificationCounts = new HashMap<>();
        notificationCounts.put(res.getString(R.string.fdp), dl.getNotificationCountByModule(Constants.FDP));
        notificationCounts.put(res.getString(R.string.clmrs), dl.getNotificationCountByModule(Constants.CLMRS));
        notificationCounts.put(res.getString(R.string.eudr), db.onGroundSurveyNotificationDao().getPendingEUDRSurveyCount(Constants.EUDR));
        notificationCounts.put(res.getString(R.string.pa), db.onGroundSurveyNotificationDao().getPendingEUDRSurveyCount(Constants.PA));

        for (Map.Entry<String, Integer> entry : notificationCounts.entrySet()) {
            if (entry.getValue() > 0) {
                ModuleListDTO moduleListDTO = new ModuleListDTO();
                moduleListDTO.setModuleName(entry.getKey());
                moduleListDTO.setTotalCount(entry.getValue());
                notifications.add(moduleListDTO);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notify, menu);
        return true;
    }

}