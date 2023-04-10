package com.olam.digital.ofis.farmplandownload;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;

import java.util.ArrayList;
import java.util.List;


public class DownloadFarmPlanAdapter extends RecyclerView.Adapter<DownloadFarmPlanAdapter.DownloadFarmPlanHolder>{
    public List<DownloadFormList.data> arrayList;
   public ItemClickListener itemClickListener;
    Context mContext;

    public DownloadFarmPlanAdapter(Context mContext, List<DownloadFormList.data> arrayList, ItemClickListener itemClickListener) {
        this.arrayList=arrayList;
        this.itemClickListener=itemClickListener;
        this.mContext=mContext;
    }

    @NonNull
    @Override
    public DownloadFarmPlanHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.download_farm_plan_row, null);
        return new DownloadFarmPlanHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadFarmPlanHolder holder,@SuppressLint("RecyclerView") int position) {
        DownloadFormList.data data =arrayList.get(position);
        holder.farmProductName.setText(data.getProductName() +"-" + data.getCountry());
        holder.downlod_Icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick(data.getFileUrl(),data.getFilename());
            }
        });

        }


  @Override
    public int getItemCount() {
        return arrayList.size();
    }




    public class DownloadFarmPlanHolder extends RecyclerView.ViewHolder {
        TextView farmProductName;
        ImageView downlod_Icon;
        //CardView rowItem;

        public DownloadFarmPlanHolder(@NonNull View itemView) {
            super(itemView);
            this.downlod_Icon = (ImageView) itemView.findViewById(R.id.downlod_Icon);
            this.farmProductName = (TextView) itemView.findViewById(R.id.farmProductName);
        }
    }
   
}
