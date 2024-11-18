package com.olam.digital.ofis.farmstakeholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.dto.StakeHolderDto;


import java.util.List;

public class StakeHolderRecyclerAdapter extends RecyclerView.Adapter<StakeHolderRecyclerAdapter.StakeHolder> {

    private OnItemClickListener onItemClickListener;
    private List<StakeHolderDto> stakeHolderList;
    private Context mContext;
    private DataLayer dl;

    public StakeHolderRecyclerAdapter(List<StakeHolderDto> stakeHolderList, Context context, DataLayer dl) {
        this.stakeHolderList = stakeHolderList;
        this.mContext = context;
        this.dl = dl;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.onItemClickListener = clickListener;
    }

    @NonNull
    @Override
    public StakeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stake_holder_list, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        return new StakeHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull StakeHolder holder, int position) {
        StakeHolderDto stakeHolderDto = stakeHolderList.get(position);

        holder.stakeHolderName.setText(stakeHolderDto.getMsName());
        holder.farmerRole.setText(dl.getFarmerRole(stakeHolderDto.getMsRoleId()));

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onStakeHolderClick(v,position,stakeHolderDto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stakeHolderList.size();
    }


    public class StakeHolder extends RecyclerView.ViewHolder {
        TextView stakeHolderName, farmerRole;
        CardView item;

        public StakeHolder(View itemView) {
            super(itemView);
            stakeHolderName = (TextView) itemView.findViewById(R.id.tvStakeHolderName);
            farmerRole = (TextView) itemView.findViewById(R.id.tvFarmerRole);
            item = (CardView) itemView.findViewById(R.id.itemCardView);
        }
    }

}
