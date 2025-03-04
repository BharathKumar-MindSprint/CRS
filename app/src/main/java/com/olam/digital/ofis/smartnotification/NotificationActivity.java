package com.olam.digital.ofis.smartnotification;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.olam.digital.ofis.BaseListActivity;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.ViewFarmerActivity;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.smartnotification.adapter.SmartNotificationAdapter;
import com.olam.digital.ofis.smartnotification.dto.ModuleListDTO;
import com.olam.digital.ofis.smartnotification.dto.NotificationDTO;
import com.olam.digital.ofis.smartnotification.listener.OnItemNotificationClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends BaseListActivity {

    private static RecyclerView notificationListRecyclerView;
    private static SmartNotificationAdapter smartNotificationAdapter;
    public DataLayer dl;
    public Resources res;
    ArrayList<NotificationDTO> notificationList;
    private Context context;
    private String moduleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        init();
    }
  
    private void init() {
        res = getResources();
        dl = new DataLayer(res, this);
        moduleName = getIntent().getExtras().getString(Utilities.MODULE, "FDP");
        notificationListRecyclerView = (RecyclerView) findViewById(R.id.notificationList);
        notificationList = dl.getSmartNotificationList(moduleName);
        RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(res.getDrawable(R.drawable.list_seperator_divider));
        notificationListRecyclerView.addItemDecoration(itemDecoration);
        notificationListRecyclerView.setHasFixedSize(true);
        notificationListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ((LinearLayoutManager) reLayoutManager).setReverseLayout(false);
        notificationListRecyclerView.setLayoutManager(reLayoutManager);
        ((LinearLayoutManager) reLayoutManager).scrollToPosition(0);
        notificationListRecyclerView.scrollToPosition(0);
        smartNotificationAdapter = new SmartNotificationAdapter(notificationList, this);
        notificationListRecyclerView.setAdapter(smartNotificationAdapter);
        loadNotificationData();
    }


    private void loadNotificationData() {

        smartNotificationAdapter.setOnItemClickListener(new OnItemNotificationClickListener() {
            @Override
            public void onNotificationClick(View view, int position, NotificationDTO notificationDTO) {
                Intent intent = new Intent(context, ViewFarmerActivity.class);
                ViewFarmerActivity.farmerId = notificationDTO.getFarmerId();
                dl.updateNotificationModule(notificationDTO.getFarmerId(), notificationDTO.getModuleId(), notificationDTO.getChildId(), new HashMap<>(Map.ofEntries(Map.entry(OFISDBContract.NotificationAssignments.COLUMN_STATUS, Utilities.READ))));
                context.startActivity(intent);
            }

            @Override
            public void onModuleClick(View view, int position, ModuleListDTO moduleListDTO) {

            }
        });


        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                dl.removeNotificationModule(notificationList.get(position).getId());
                notificationList.remove(position);
                smartNotificationAdapter.notifyDataSetChanged();
                Toast.makeText(NotificationActivity.this, getResources().getString(R.string.info_notification_close), Toast.LENGTH_SHORT).show();
            }
        };
        ItemTouchHelper touchHelper =  new ItemTouchHelper(simpleItemTouchCallback);

        if (moduleName.equals(Constants.FDP)) {
            touchHelper.attachToRecyclerView(notificationListRecyclerView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notify, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }
}
