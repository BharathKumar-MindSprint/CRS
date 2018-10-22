package com.olam.digital.ofis;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.model.TasksDTO;

import java.util.ArrayList;
import java.util.Date;

public class TasksActivity extends BaseListActivity {
    private Context context;
    private static Context staticContext;
    private Resources res;
    private DataLayer dl;
    private Cursor tasks;
//    private CustomListAdapter_Tasks customListAdapter_tasks;
    private MyCustomListAdapter_Tasks myCustomListAdapter_tasks;
    private boolean completed = true;
    private ArrayList<TasksDTO> tasksDTOArrayList = new ArrayList<>();
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTasks();
        }
    };

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static final String ACCOUNT_TYPE = BuildConfig.ACCOUNT_TYPE;
    private static final String ACCOUNT = "ofis_companion";
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SYNC_INTERVAL_IN_MINUTES = 1L;
    private static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        staticContext = this;
        res = getResources();
        setContentView(R.layout.activity_tasks);

        dl = new DataLayer(res, context);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        dl.deleteOldCompletedTasks();
        getTasks();
        Account mAccount = CreateSyncAcount();

        ContentResolver.setSyncAutomatically(
                mAccount,
                AUTHORITY,
                true
        );

        ContentResolver.addPeriodicSync(
                mAccount,
                AUTHORITY,
                Bundle.EMPTY,
                SYNC_INTERVAL
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("TasksUpdated"));

    }



    private static Account CreateSyncAcount() {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) staticContext.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            return newAccount;
        }

        return newAccount;
    }

    private void getTasks() {
        tasks = dl.getTasks();
        bindListAdapter();
      /*  if (tasks.getCount() > 0) {
            bindListAdapter();
        }*/
    }

    private void bindListAdapter() {
        String[] adapterCols = new String[]{"_id", OFISDBContract.Task.COLUMN_NAME_TASKTEXT, OFISDBContract.Task.COLUMN_NAME_CREATED_DATE, OFISDBContract.Task.COLUMN_NAME_COMPLETED};
        int[] adapterRowViews = new int[]{R.id.taskId, R.id.taskListItemText, R.id.taskListItemDate, R.id.taskCompleted};
//        customListAdapter_tasks = new CustomListAdapter_Tasks(context, tasks, adapterCols, adapterRowViews, res);
        tasksDTOArrayList.addAll(generatetasksDTOList(tasks));
        myCustomListAdapter_tasks = new MyCustomListAdapter_Tasks(context, tasksDTOArrayList, adapterCols, adapterRowViews, res);
        setListAdapter(myCustomListAdapter_tasks);
    }

    private ArrayList<TasksDTO> generatetasksDTOList(Cursor tasks) {
        ArrayList<TasksDTO> tasksDTOArrayTemp = new ArrayList<>();
        TasksDTO tasksDTO;
        if(tasks.getCount() > 0){
            while(tasks.moveToNext()){
                tasksDTO = new TasksDTO();
                tasksDTO.setTaskId(tasks.getInt(tasks.getColumnIndex("_id")));
                tasksDTO.setTaskCompletedStatus(String.valueOf(tasks.getInt(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_COMPLETED))));
                tasksDTO.setTaskCreator(tasks.getString(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_TASK_CREATOR)));
                tasksDTO.setTaskDate(tasks.getString(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE)));
                tasksDTO.setTaskName(tasks.getString(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_TASKTEXT)));
                tasksDTOArrayTemp.add(tasksDTO);
            }
        }
        return tasksDTOArrayTemp;
    }

    private void updateTasks() {
        tasks = dl.getTasks();
        if(tasksDTOArrayList != null && !tasksDTOArrayList.isEmpty())
            tasksDTOArrayList.clear();

        tasksDTOArrayList.addAll(generatetasksDTOList(tasks));
        myCustomListAdapter_tasks.notifyDataSetChanged();
       /* if (tasks.getCount() > 0 && customListAdapter_tasks != null) {
            customListAdapter_tasks.changeCursor(tasks);
        } else {
            bindListAdapter();
        }*/
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, final long id) {
        TasksDTO tasksDTO = (TasksDTO) l.getAdapter().getItem(position);
        final ImageView imageView = (ImageView) v.findViewById(R.id.cbImage);
        final TextView completedText = (TextView) v.findViewById(R.id.taskCompleted);
        final LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.rowlin);
        completed = "1".equals(tasksDTO.getTaskCompletedStatus());

        if (completed) {
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);
            dialog.setTitle(R.string.untick_task);
            dialog.setMessage(R.string.untick_task_msg)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_off_background));
                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            completedText.setText("0");
                            completed = false;
                            if (dl.updateTaskStatus(completed, id) > 0) {
//                                myCustomListAdapter_tasks.notifyDataSetChanged();
                                updateTasks();
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        }
                    });
            dialog.show();

            /*imageView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_off_background));
            linearLayout.setBackgroundResource(R.drawable.list_item);
            completedText.setText("0");
            completed = false;*/
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.checkbox_on_background));
            linearLayout.setBackgroundResource(R.drawable.list_item_selected);
            completedText.setText("1");
            completed = true;
            if (dl.updateTaskStatus(completed, id) > 0) {
//                myCustomListAdapter_tasks.notifyDataSetChanged();
                updateTasks();
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tasks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_home) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            onBackPressed();
            return true;
        }

    }
}
