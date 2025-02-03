package com.olam.digital.ofis.smartnotification.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.smartnotification.dto.ModuleListDTO;
import com.olam.digital.ofis.smartnotification.listener.OnItemNotificationClickListener;

import java.util.List;

public class AllNotificationAdapter extends RecyclerView.Adapter<AllNotificationAdapter.ViewHolder> {

    private final List<ModuleListDTO> notificationList;
    private OnItemNotificationClickListener onItemNotificationClickListener;


    public AllNotificationAdapter(List<ModuleListDTO> notificationList) {
        this.notificationList = notificationList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        TextView content;
        ImageView arrow;
        TextView contentCount;
        LinearLayout cardView;
        TextView notificationType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.logo);
            content = itemView.findViewById(R.id.content);
            arrow = itemView.findViewById(R.id.ic_next_arrow);
            contentCount = itemView.findViewById(R.id.content_count);
            cardView = itemView.findViewById(R.id.card_view);
            notificationType = itemView.findViewById(R.id.notification_type);
        }
    }

    public void setOnItemClickListener(OnItemNotificationClickListener clickListener) {
        this.onItemNotificationClickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleListDTO module = notificationList.get(position);
        String notification = module.getModuleName();
        holder.content.setText(notification);
        int count = module.getTotalCount();

        holder.contentCount.setText(String.valueOf(count));
        holder.cardView.setBackgroundResource(R.drawable.list_item);
        holder.arrow.setVisibility(View.VISIBLE);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemNotificationClickListener.onModuleClick(v, holder.getAdapterPosition(), module);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }


}
