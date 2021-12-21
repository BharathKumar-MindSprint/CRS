package com.olam.digital.ofis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemChildClickListener;
import com.olam.digital.ofis.model.FarmerLeadDTO;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

// Child List is populated and handles the active /Inactive child list in this adapter.
public class FindFarmerLeadAdapter extends RecyclerView.Adapter<FindFarmerLeadAdapter.FLHolder>{

    private OnItemFarmerLeadListener onItemFarmerLeadListener;
    private List<FarmerLeadDTO> flList;
    private Context mContext;

    public FindFarmerLeadAdapter(List<FarmerLeadDTO> flList, Context context) {
        this.flList=flList;
        this.mContext=context;
    }

    public void setOnItemFarmerLeadClickListener(OnItemFarmerLeadListener clickListener) {
        this.onItemFarmerLeadListener = clickListener;
    }

    @NonNull
    @Override
    public FLHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.farmerlead_view, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        return new FLHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull FLHolder holder, final int position) {
        final FarmerLeadDTO leadDTO=flList.get(position);
        holder.flName.setText(leadDTO.getFarmerLeadFullName());
        holder.flId.setText(mContext.getResources().getString(R.string.farmerlead_id) +leadDTO.getFarmerLeadDisplayId());
        holder.flPhone.setText(mContext.getResources().getString(R.string.farmerlead_phonenumber) +leadDTO.getContactNumber());



        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemFarmerLeadListener.onFarmerLeadClick(v,position, leadDTO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flList.size();
    }

    public void filterList(ArrayList<FarmerLeadDTO> filteredlist) {
        flList = filteredlist;
        notifyDataSetChanged();
    }

    public class FLHolder extends RecyclerView.ViewHolder {
        TextView flName,flId,flPhone;
        CardView rowItem;
        public FLHolder(View view) {
            super(view);
            rowItem=(CardView)itemView.findViewById(R.id.itemCardView);
            flName = (TextView) view.findViewById(R.id.farmerleadname);
            flId = (TextView) view.findViewById(R.id.farmerleadid);
            flPhone = (TextView) view.findViewById(R.id.phoneno);

        }
    }
}
