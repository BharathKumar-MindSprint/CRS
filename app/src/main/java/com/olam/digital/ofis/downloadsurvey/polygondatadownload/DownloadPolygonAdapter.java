package com.olam.digital.ofis.downloadsurvey.polygondatadownload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto.FarmerPolygonDto;

import java.util.ArrayList;
import java.util.List;

public class DownloadPolygonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private List<FarmerPolygonDto> getFarmerWithPlots;
    private Context context;

    public DownloadPolygonAdapter(List<FarmerPolygonDto> getFarmerWithPlots, Context context) {
        this.getFarmerWithPlots= new ArrayList<>(getFarmerWithPlots);
        this.context = context;
    }

    public void swapCursor(List<FarmerPolygonDto> newFarmerWithPlots) {
        if (newFarmerWithPlots != null) {
            this.getFarmerWithPlots.clear();
            this.getFarmerWithPlots.addAll(newFarmerWithPlots);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_polygon_farmer, parent, false);
        return new FarmerPlotViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FarmerPolygonDto item = getFarmerWithPlots.get(position);
        FarmerPlotViewHolder farmerPlotViewHolder = (FarmerPlotViewHolder) holder;

        farmerPlotViewHolder.farmerName.setText(item.getName());
        farmerPlotViewHolder.olamFarmerId.setText(item.getOlamFarmerId());
        farmerPlotViewHolder.farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_no_photo));

    }

    @Override
    public int getItemCount() {
        return getFarmerWithPlots.size();
    }

    public static class FarmerPlotViewHolder extends RecyclerView.ViewHolder {
        public TextView farmerName;
        public TextView olamFarmerId;
        public ImageView farmerPhoto;

        public FarmerPlotViewHolder(@NonNull View itemView) {
            super(itemView);
            farmerPhoto = itemView.findViewById(R.id.farmerListIcon);
            farmerName = itemView.findViewById(R.id.farmerName);
            olamFarmerId = itemView.findViewById(R.id.olamFarmerId);
        }
    }
}
