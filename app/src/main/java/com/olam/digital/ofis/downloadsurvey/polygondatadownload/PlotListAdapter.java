package com.olam.digital.ofis.downloadsurvey.polygondatadownload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto.PlotDisplayDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlotListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PlotDisplayDto> plotList;
    private Context context;
    private Map<Long, Long> selectedPlotSizes;


    public PlotListAdapter(List<PlotDisplayDto> plotList, Context context, Map<Long, Long> selectedPlotSizes) {
        this.plotList = plotList;
        this.context = context;
        this.selectedPlotSizes = selectedPlotSizes;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plot_row_layout, parent, false);
        return new PlotListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        PlotDisplayDto item = plotList.get(position);
        PlotListViewHolder plotListViewHolder = (PlotListViewHolder) holder;

        String plot_size = item.getPlotSize();
        plotListViewHolder.plot_name.setText(String.format(context.getString(R.string.plot_number), position+1));
        if (!Objects.equals(plot_size,null) && !Objects.equals(plot_size,"null")) {
            plotListViewHolder.plot_size.setText(String.format(context.getString(R.string.plot_size_polygon), Double.parseDouble(item.getPlotSize())));
        } else {
            plotListViewHolder.plot_size.setText(context.getString(R.string.plot_pin));
        }

        plotListViewHolder.plot_checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                long newSize = calculateTotalSize() + getSizeInBytes(item.getPlotSize());
                if (newSize > Utilities.SIZE_20_MB) {
                    Toast.makeText(buttonView.getContext(), "Total size exceeds 20MB limit", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(false);
                    return;
                }
                selectedPlotSizes.put(item.getPlotId(), getSizeInBytes(item.getPlotSize()));
            } else {
                selectedPlotSizes.remove(item.getPlotId());
            }
            item.setChecked(isChecked);
        });

    }

    @Override
    public int getItemCount() {
        return plotList.size();
    }

    public static class PlotListViewHolder extends RecyclerView.ViewHolder {

        public TextView plot_name;
        public TextView plot_size;
        public CheckBox plot_checkBox;


        public PlotListViewHolder(@NonNull View itemView) {
            super(itemView);
            plot_name = itemView.findViewById(R.id.plot_name);
            plot_size = itemView.findViewById(R.id.plot_size);
            plot_checkBox = itemView.findViewById(R.id.plot_checkBox);
        }
    }

    private long calculateTotalSize() {
        long totalSize = 0;
        for (Long size : selectedPlotSizes.values()) {
            totalSize += size;
        }
        return totalSize;
    }

    private long getSizeInBytes(String plotSize) {
        try {
            double sizeInKB = Double.parseDouble(plotSize);
            return (long) (sizeInKB * 1024);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
