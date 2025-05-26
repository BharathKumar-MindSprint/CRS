package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.FollowUp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleFollowUp;

import java.util.ArrayList;

public class FollowUpRecyclerAdapter extends RecyclerView.Adapter<FollowUpRecyclerAdapter.ViewHolder> {
    private ArrayList<String> items;
    private Context context;
    int newFollowCount;
    private int selectedPosition = RecyclerView.NO_POSITION;
    Intent in;
    boolean isCompleted;
    String moduleID;

    public FollowUpRecyclerAdapter(ArrayList<String> items , Context context,int newFollowUpCount,Intent in, boolean isCompleted ,String moduleId) {
        this.items = items;
        this.context = context;
        this.newFollowCount = newFollowUpCount;
        this.in = in;
        this.isCompleted = isCompleted;
        this.moduleID = moduleId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.follow_up_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindItem(items.get(position), position);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView itemTextView;
        private ImageView tickImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTextView = itemView.findViewById(R.id.itemTextView);
            tickImageView = itemView.findViewById(R.id.tickImageView);
            itemView.setOnClickListener(this);
        }

        @SuppressLint("ResourceAsColor")
        public void bindItem(String item, int position) {
            itemTextView.setText(item);

            if (position == selectedPosition) {
                tickImageView.setVisibility(View.VISIBLE);

            } else {
                tickImageView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {

            OFISRoomDB db = OFISRoomDB.getDbInstance(context);
            in.putExtra("DM_MODULE_ID",moduleID);
            int previousPosition = selectedPosition;
            selectedPosition = getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (selectedPosition== 0 ) {
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(items.size() == 1 && !isCompleted){
                    Constants.EDITABLE = true;
                    context.startActivity(in);
                }else if (isCompleted && db.complianceIndicatorDao().hasNoActionPlan(String.valueOf(moduleID))){
                    Constants.EDITABLE = false;
                }else if (isCompleted && db.complianceIndicatorDao().hasActionPlan(String.valueOf(moduleID))){
                    Constants.EDITABLE = false;
                }
            }else {
                Intent intent = new Intent(context, DiagnosticModuleFollowUp.class);
                intent.putExtra("DM_MODULE_ID",moduleID);
                intent.putExtra("FollowUpPosition", selectedPosition);
                if(selectedPosition == items.size() - 1 && !isCompleted){
                    Constants.EDITABLE = true;
                    Constants.ATTEMPT = selectedPosition;
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }else{
                    Constants.EDITABLE = false;
                }
            }

        }
    }
}

