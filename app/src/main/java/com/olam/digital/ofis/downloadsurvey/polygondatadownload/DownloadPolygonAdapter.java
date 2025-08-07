package com.olam.digital.ofis.downloadsurvey.polygondatadownload;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto.FarmerPolygonDto;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto.PlotCursorToDto;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto.PlotDisplayDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadPolygonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private List<FarmerPolygonDto> getFarmerWithPlots;
    private Context context;
    private RecyclerView.RecycledViewPool viewPool;
    private DataLayer dl;
    private Map<Long, Long> selectedPlotSizes = new HashMap<>();
    private OnSelectedPlotSizesChangeListener listener;

    public DownloadPolygonAdapter(List<FarmerPolygonDto> getFarmerWithPlots, Context context, DataLayer dl) {
        this.getFarmerWithPlots= new ArrayList<>(getFarmerWithPlots);
        this.context = context;
        this.viewPool = new RecyclerView.RecycledViewPool();
        this.dl = dl;
    }

    public void swapCursor(List<FarmerPolygonDto> newFarmerWithPlots) {
        if (newFarmerWithPlots != null) {
            this.getFarmerWithPlots.clear();
            this.getFarmerWithPlots.addAll(newFarmerWithPlots);
            notifyDataSetChanged();
        }
    }

    public interface OnSelectedPlotSizesChangeListener {
        void onSelectedPlotSizesChanged(Map<Long, Long> updatedPlotSizes);
    }

    public void setOnSelectedPlotSizesChangeListener(OnSelectedPlotSizesChangeListener listener) {
        this.listener = listener;
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
        farmerPlotViewHolder.olamFarmerId.setText(context.getString(R.string.ofis_olam_id) + item.getOlamFarmerId());
        farmerPlotViewHolder.farmerPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_no_photo));

        List<PlotDisplayDto> plotList = getAllplotList(item);

        PlotListAdapter childAdapter = new PlotListAdapter(plotList, context, selectedPlotSizes);
        farmerPlotViewHolder.listPolygonCheckbox.setLayoutManager(new LinearLayoutManager(context));
        farmerPlotViewHolder.listPolygonCheckbox.setAdapter(childAdapter);
        farmerPlotViewHolder.listPolygonCheckbox.setRecycledViewPool(viewPool);
        childAdapter.setOnSelectedPlotSizesChangeListener(new PlotListAdapter.OnSelectedPlotSizesChangeListener() {
            @Override
            public void onSelectedPlotSizesChanged(Map<Long, Long> updatedPlotSizes) {
                selectedPlotSizes = updatedPlotSizes;
                notifySelectedPlotSizesChanged();
            }
        });
    }

    private void notifySelectedPlotSizesChanged() {
        if (listener != null) {
            listener.onSelectedPlotSizesChanged(selectedPlotSizes);
        }
    }
    private List<PlotDisplayDto> getAllplotList(FarmerPolygonDto item) {

        Cursor getAllPlot = dl.fetchAllPlotIds(String.valueOf(item.getId()), true, false);
        return PlotCursorToDto.cursorToDtoFarmerList(getAllPlot, item.getName(), item.getId());
    }

    @Override
    public int getItemCount() {
        return getFarmerWithPlots.size();
    }
    public Map<Long, Long> getSelectedPlotSizes() {
        return selectedPlotSizes;
    }

    public static class FarmerPlotViewHolder extends RecyclerView.ViewHolder {
        public TextView farmerName;
        public TextView olamFarmerId;
        public ImageView farmerPhoto;
        public RecyclerView listPolygonCheckbox;

        public FarmerPlotViewHolder(@NonNull View itemView) {
            super(itemView);
            farmerPhoto = itemView.findViewById(R.id.farmerListIcon);
            farmerName = itemView.findViewById(R.id.farmerName);
            olamFarmerId = itemView.findViewById(R.id.olamFarmerId);
            listPolygonCheckbox = itemView.findViewById(R.id.listPolygonCheckbox);
        }
    }
}
