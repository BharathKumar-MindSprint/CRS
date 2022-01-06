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


public class FindMultiFarmFarmerAdapter extends RecyclerView.Adapter<FindMultiFarmFarmerAdapter.MultiFarmHolder> {

    private OnItemMultifarmFarmerListener onItemMultifarmFarmerListener;
    private List<MultifarmFarmerDTO> flList;
    private Context mContext;

    public FindMultiFarmFarmerAdapter(List<MultifarmFarmerDTO> flList, Context context) {
        this.flList = flList;
        this.mContext = context;
    }

    public void setOnItemMultifarmFarmerListener(OnItemMultifarmFarmerListener clickListener) {
        this.onItemMultifarmFarmerListener = clickListener;
    }

    @NonNull
    @Override
    public MultiFarmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.multifarm_farmer_item_view, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        return new MultiFarmHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiFarmHolder holder, final int position) {
        final MultifarmFarmerDTO multifarmFarmerDTO = flList.get(position);
        holder.farmerNameTV.setText(multifarmFarmerDTO.getFarmerName());
        holder.farmerIdTV.setText(String.valueOf(multifarmFarmerDTO.getFarmerId()));

        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemMultifarmFarmerListener.onMultiFarmFarmerClick(v, position, multifarmFarmerDTO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flList.size();
    }

    public void filterList(ArrayList<MultifarmFarmerDTO> farmerList) {
        flList = farmerList;
        notifyDataSetChanged();
    }

    public class MultiFarmHolder extends RecyclerView.ViewHolder {
        TextView farmerNameTV, farmerIdTV;
        CardView rowItem;

        public MultiFarmHolder(View view) {
            super(view);
            rowItem = (CardView) itemView.findViewById(R.id.itemCardView);
            farmerNameTV = (TextView) view.findViewById(R.id.farmerNameTV);
            farmerIdTV = (TextView) view.findViewById(R.id.farmerIdTV);
        }
    }
}

