package com.webpoint0.ofiscompanion;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
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

public class TasksActivity extends ListActivity {
    private Context context;
    private Resources res;
    private DataLayer dl;
    private Cursor tasks;
    private CustomListAdapter_Tasks customListAdapter_tasks;
    private boolean completed = true;

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTasks();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        res = getResources();
        setContentView(R.layout.activity_tasks);

        dl = new DataLayer(res, context);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        dl.deleteOldCompletedTasks();
        getTasks();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("TasksUpdated"));

    }

    private void getTasks() {
        tasks = dl.getTasks();

        if (tasks.getCount() > 0) {
            bindListAdapter();
        }
    }

    private void bindListAdapter() {
        String[] adapterCols = new String[]{"_id", OFISDBContract.Task.COLUMN_NAME_TASKTEXT, OFISDBContract.Task.COLUMN_NAME_CREATED_DATE, OFISDBContract.Task.COLUMN_NAME_COMPLETED};
        int[] adapterRowViews = new int[]{R.id.taskId, R.id.taskListItemText, R.id.taskListItemDate, R.id.taskCompleted};
        customListAdapter_tasks = new CustomListAdapter_Tasks(context, tasks, adapterCols, adapterRowViews, res);
        setListAdapter(customListAdapter_tasks);
    }

    private void updateTasks() {
        tasks = dl.getTasks();
        if (tasks.getCount() > 0 && customListAdapter_tasks != null) {
            customListAdapter_tasks.changeCursor(tasks);
        } else {
            bindListAdapter();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final ImageView imageView = (ImageView) v.findViewById(R.id.cbImage);
        final TextView completedText = (TextView) v.findViewById(R.id.taskCompleted);
        final LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.rowlin);
        completed = "1".equals(completedText.getText().toString());

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
        }

        if (dl.updateTaskStatus(completed, id) > 0) {
            updateTasks();
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
