package com.olam.digital.ofis;

import android.content.Context;
import android.content.res.Resources;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olam.digital.ofis.model.TasksDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MyCustomListAdapter_Tasks extends BaseAdapter {
    private int layout;
    private LayoutInflater inflater;
    private Resources res;
    private ArrayList<TasksDTO> tasksDTOArrayList;
    private Context context;
    private boolean iscompleted = true;
    public MyCustomListAdapter_Tasks(Context context, ArrayList<TasksDTO> tasksDTOArrayList, String[] adapterCols, int[] adapterRowViews, Resources res_) {
        this.context = context;
        this.tasksDTOArrayList = tasksDTOArrayList;
        this.res = res_;
    }

    @Override
    public int getCount() {
        return tasksDTOArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return tasksDTOArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tasksDTOArrayList.get(position).getTaskId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.listview_task_list, parent, false);
            }

            // get current item to be displayed
            TasksDTO tasksDTO = (TasksDTO) getItem(position);
            ImageView cbImage = (ImageView) convertView.findViewById(R.id.cbImage);
            LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.rowlin);
            TextView taskText = (TextView) convertView.findViewById(R.id.taskListItemText);
            TextView taskDate = (TextView) convertView.findViewById(R.id.taskListItemDate);
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);


            int completed = Integer.parseInt(tasksDTO.getTaskCompletedStatus());
            taskText.setText(tasksDTO.getTaskName());
            Date dbDate=null;
            try {
                 dbDate = inputFormat.parse(tasksDTO.getTaskDate());
            }catch (Exception e){
                e.printStackTrace();
            }
            linearLayout.setBackgroundResource(R.drawable.list_item);
            taskText.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
            taskText.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.ofis_med_text));
            taskText.setMovementMethod(LinkMovementMethod.getInstance());
            taskText.setLinkTextColor(Color.BLUE);
            taskText.setFocusable(false);
            taskText.setClickable(true);
            taskDate.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));

            if (completed == 1) {
                cbImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box));
                try {
                   String viewDate = outputFormat.format(dbDate);
                   taskDate.setText(viewDate);
               }catch (Exception e){

               }

            } else {
                cbImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_box_outline_blank));
                 Date createdDate = inputFormat.parse(taskDate.getText().toString());
                String creator = tasksDTO.getTaskCreator();
                taskDate.setText(creator + " | " + outputFormat.format(createdDate));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertView;

    }
}
