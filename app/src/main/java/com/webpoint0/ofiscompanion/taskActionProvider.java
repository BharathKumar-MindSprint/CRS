package com.webpoint0.ofiscompanion;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Keep;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Task action provider. Created by ben on 08/11/2016.
 */

@Keep
public class taskActionProvider extends ActionProvider {

    private final Context mContext;
    private LinearLayout view;

    public taskActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = (LinearLayout) inflater.inflate(R.layout.menu_item_tasks, null);
        bindButton(view);
        updateTaskCount();
        return view;
    }

    @Override
    public View onCreateActionView(MenuItem forItem) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = (LinearLayout) inflater.inflate(R.layout.menu_item_tasks, null);
        bindButton(view);
        updateTaskCount();
        return view;
    }

    private void bindButton(LinearLayout view) {
        ImageButton taskIcon = (ImageButton) view.findViewById(R.id.TaskListIcon);
        taskIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TasksActivity.class);
                mContext.startActivity(intent);
            }
        });
    }

    private void updateTaskCount() {
        DataLayer dl = new DataLayer(mContext.getResources(), mContext);
        int taskcount = dl.getActiveTaskCount();
        TextView tasklistcount = (TextView) view.findViewById(R.id.TaskListCount);

        if (taskcount > 0) {
            tasklistcount.setText(String.valueOf(taskcount));
            tasklistcount.setVisibility(View.VISIBLE);
        } else {
            tasklistcount.setVisibility(View.GONE);
        }

    }
}
