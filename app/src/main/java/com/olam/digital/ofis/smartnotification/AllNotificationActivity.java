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
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.smartnotification.adapter.AllNotificationAdapter;
import com.olam.digital.ofis.smartnotification.dto.ModuleListDTO;
import com.olam.digital.ofis.smartnotification.dto.NotificationDTO;
import com.olam.digital.ofis.smartnotification.listener.OnItemNotificationClickListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AllNotificationActivity extends BaseListActivity {

    private RecyclerView recyclerAllNotification;
    private Context mContext;

    public Resources res;

    public DataLayer dl;
    public OFISRoomDB db;

    private List<ModuleListDTO> notifications;

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

            }
        });
    }


    private void createNotificationList() {
        notifications = new ArrayList<>();

        int FDPCount = dl.getNotificationCountByModule("FDP");
        int CLMRSCount = dl.getNotificationCountByModule("CLMRS");
        int EUDRCount = db.onGroundSurveyNotificationDao().getPendingEUDRSurveyCount("EUDR");
        int PACount = db.onGroundSurveyNotificationDao().getPendingEUDRSurveyCount("PA");

        if (FDPCount > 0) {
            ModuleListDTO moduleListDTO = new ModuleListDTO();
            moduleListDTO.setModuleName(res.getString(R.string.fdp));
            moduleListDTO.setTotalCount(FDPCount);
            notifications.add(moduleListDTO);
        }

        if (CLMRSCount > 0) {
            ModuleListDTO moduleListDTO = new ModuleListDTO();
            moduleListDTO.setModuleName(res.getString(R.string.clmrs));
            moduleListDTO.setTotalCount(CLMRSCount);
            notifications.add(moduleListDTO);
        }

        if (EUDRCount > 0) {
            ModuleListDTO moduleListDTO = new ModuleListDTO();
            moduleListDTO.setModuleName(res.getString(R.string.eudr));
            moduleListDTO.setTotalCount(EUDRCount);
            notifications.add(moduleListDTO);
        }

        if (PACount > 0) {
            ModuleListDTO moduleListDTO = new ModuleListDTO();
            moduleListDTO.setModuleName(res.getString(R.string.pa));
            moduleListDTO.setTotalCount(PACount);
            notifications.add(moduleListDTO);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notify, menu);
        return true;
    }
}