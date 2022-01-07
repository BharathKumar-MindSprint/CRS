
package com.olam.digital.ofis.multifarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;

import java.util.ArrayList;
import java.util.List;


public class FindMultiFarmFarmAdapter extends RecyclerView.Adapter<FindMultiFarmFarmAdapter.MultiFarmFarmHolder> {

    private OnItemMultiFarmFarmListener onItemMultifarmFarmListener;
    private List<MultiFarmDTO> multiFarmList;
    private Context mContext;

    public FindMultiFarmFarmAdapter(List<MultiFarmDTO> multiFarmList, Context context) {
        this.multiFarmList = multiFarmList;
        this.mContext = context;
    }

    public void setOnItemMultifarmFarmListener(OnItemMultiFarmFarmListener clickListener) {
        this.onItemMultifarmFarmListener = clickListener;
    }

    @NonNull
    @Override
    public MultiFarmFarmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.multifarm_farm_item_view, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        return new MultiFarmFarmHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiFarmFarmHolder holder, final int position) {
        final MultiFarmDTO multiFarmDTO = multiFarmList.get(position);
        holder.farmNameTV.setText(multiFarmDTO.getFarmName());
        holder.farmCodeTV.setText(String.valueOf(multiFarmDTO.getFarmCode()));

        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemMultifarmFarmListener.onMultiFarmFarmClick(v, position, multiFarmDTO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return multiFarmList.size();
    }

    public void filterList(ArrayList<MultiFarmDTO> multiFarmList) {
        this.multiFarmList = multiFarmList;
        notifyDataSetChanged();
    }

    public class MultiFarmFarmHolder extends RecyclerView.ViewHolder {
        TextView farmNameTV, farmCodeTV;
        CardView rowItem;

        public MultiFarmFarmHolder(View view) {
            super(view);
            rowItem = (CardView) itemView.findViewById(R.id.itemCardView);
            farmNameTV = (TextView) view.findViewById(R.id.farmNameTV);
            farmCodeTV = (TextView) view.findViewById(R.id.farmCodeTV);

        }
    }
}


