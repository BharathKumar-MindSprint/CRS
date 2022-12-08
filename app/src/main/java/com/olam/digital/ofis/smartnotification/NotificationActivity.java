package com.olam.digital.ofis.smartnotification;

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
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.ViewFarmerActivity;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.smartnotification.adapter.SmartNotificationAdapter;
import com.olam.digital.ofis.smartnotification.dto.NotificationDTO;
import com.olam.digital.ofis.smartnotification.listener.OnItemNotificationClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NotificationActivity extends BaseListActivity {

    private static RecyclerView notificationListRecyclerView;
    private static SmartNotificationAdapter smartNotificationAdapter;
    Spinner moduleSpinner;
    ImageButton sortIcon;
    public DataLayer dl;
    public Resources res;
    boolean ascending = false;
    ArrayList<NotificationDTO> notificationList;
    boolean isChildEnumerator;
    ArrayAdapter<String> spinnerArrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
    }
  
    private void init() {
        res = getResources();
        dl = new DataLayer(res, this);
        moduleSpinner = (Spinner) findViewById(R.id.spinnerModuletype);
        notificationListRecyclerView = (RecyclerView) findViewById(R.id.notificationList);
        sortIcon = (ImageButton) findViewById(R.id.sortingButton);
        isChildEnumerator = Utilities.getChildLabourPref(this, ChildLabourUtils.IS_CHILD_ENUMERATOR);
        if(isChildEnumerator) {
            spinnerArrayAdapter = new ArrayAdapter<String>(NotificationActivity.this, R.layout.spinner_item, res.getStringArray(R.array.notification_modules));
        }else{
            spinnerArrayAdapter = new ArrayAdapter<String>(NotificationActivity.this, R.layout.spinner_item, res.getStringArray(R.array.notification_modules_noclmrs));
        }
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        moduleSpinner.setAdapter(spinnerArrayAdapter);
        notificationList = dl.getSmartNotificationList();
        RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(res.getDrawable(R.drawable.list_seperator_divider));
        notificationListRecyclerView.addItemDecoration(itemDecoration);
        notificationListRecyclerView.setHasFixedSize(true);
        notificationListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ((LinearLayoutManager) reLayoutManager).setReverseLayout(true);
        ((LinearLayoutManager) reLayoutManager).setStackFromEnd(true);
        ((LinearLayoutManager) reLayoutManager).scrollToPosition(0);
        notificationListRecyclerView.setLayoutManager(reLayoutManager);
        smartNotificationAdapter = new SmartNotificationAdapter(notificationList, this);
        notificationListRecyclerView.setAdapter(smartNotificationAdapter);
        loadNotificationData();
        sortIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ascending){
                    ascending = false;
                }else {
                    ascending = true;
                }
                sortNotificationList(ascending);
            }
        });
        moduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    notificationList = dl.getSmartNotificationList();
                }else{
                    notificationList = dl.getSmartNotificationList(position);
                }
                smartNotificationAdapter.resetItem(notificationList);
                smartNotificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void sortNotificationList(boolean ascending) {
        Collections.sort(notificationList, new Comparator<NotificationDTO>() {
            public int compare(NotificationDTO o1, NotificationDTO o2) {
                if (o1.getCreatedAt() == null || o2.getCreatedAt() == null)
                    return 0;
                if(ascending) {
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                }else{
                    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                }
            }
        });
        smartNotificationAdapter.notifyDataSetChanged();
    }

    private void loadNotificationData() {

        smartNotificationAdapter.setOnItemClickListener(new OnItemNotificationClickListener() {
            @Override
            public void onNotificationClick(View view, int position, NotificationDTO notificationDTO) {
                Intent intent = new Intent(NotificationActivity.this, ViewFarmerActivity.class);
                ViewFarmerActivity.farmerId = notificationDTO.getFarmerId();
                dl.updateNotificationModule(notificationDTO.getFarmerId());
                startActivity(intent);
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
        touchHelper.attachToRecyclerView(notificationListRecyclerView);
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
