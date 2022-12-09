package com.olam.digital.ofis.smartnotification.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.smartnotification.TimeUtils;
import com.olam.digital.ofis.smartnotification.dto.NotificationDTO;
import com.olam.digital.ofis.smartnotification.listener.OnItemNotificationClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class SmartNotificationAdapter extends RecyclerView.Adapter<SmartNotificationAdapter.NotificationHolder> {

    private OnItemNotificationClickListener onItemNotificationClickListener;
    private List<NotificationDTO> notificationDTOList;
    public DataLayer dl;
    public Resources res;

    public SmartNotificationAdapter(List<NotificationDTO> notificationDTOList, Context context) {
        this.notificationDTOList=notificationDTOList;
        res = context.getResources();
        dl = new DataLayer(res, context);
    }

    public void setOnItemClickListener(OnItemNotificationClickListener clickListener) {
        this.onItemNotificationClickListener = clickListener;
    }

    public void resetItem(List<NotificationDTO> notificationDTOList){
        this.notificationDTOList=notificationDTOList;
    }

    @NonNull
    @Override
    public SmartNotificationAdapter.NotificationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.smart_notification_row, null);
        return new SmartNotificationAdapter.NotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmartNotificationAdapter.NotificationHolder holder, @SuppressLint("RecyclerView") int position) {
        final NotificationDTO notificationDTO = notificationDTOList.get(position);
        holder.farmerName.setText(dl.getFarmerNamefromDb(Long.valueOf(notificationDTO.getFarmerId())));
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            long time = sdf.parse(notificationDTO.getCreatedAt()).getTime();
            String formattedDate = String.valueOf(TimeUtils.getRelativeTime(time));
            holder.dataTime.setText(formattedDate);
        }catch (ParseException e){
            e.printStackTrace();
        }

        int moduleId = notificationDTO.getModuleId();
        holder.message.setText(dl.getMessageByModuleId(moduleId));
        if(moduleId == 1){
            holder.childName.setVisibility(View.GONE);
            holder.moduleIcon.setImageDrawable(res.getDrawable(R.drawable.ic_fdp_notify));
        }else if (moduleId == 2){
            int childId = notificationDTO.getChildId();
            holder.childName.setText(dl.getChildNamefromDb(Long.valueOf(childId)));
            holder.moduleIcon.setImageDrawable(res.getDrawable(R.drawable.ic_child_notify));
        }
        String status = notificationDTO.getStatus();
        if(status.equalsIgnoreCase(Utilities.READ)){
            holder.rowItem.setBackgroundResource(R.drawable.list_item_disabled);
        }else {
            holder.rowItem.setBackgroundResource(R.drawable.list_item);
        }
        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemNotificationClickListener.onNotificationClick(v,position,notificationDTO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationDTOList.size();
    }

    public class NotificationHolder extends RecyclerView.ViewHolder {
        TextView farmerName,dataTime,message,childName;
        ImageView statusIcon,moduleIcon;
        CardView rowItem;

        public NotificationHolder(View itemView) {
            super(itemView);
            rowItem = (CardView) itemView.findViewById(R.id.rowCardView);
            farmerName = itemView.findViewById(R.id.farmerName);
            childName = itemView.findViewById(R.id.childName);
            message = itemView.findViewById(R.id.message);
            dataTime = itemView.findViewById(R.id.dataTime);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            moduleIcon = itemView.findViewById(R.id.moduleIcon);
        }
    }
}