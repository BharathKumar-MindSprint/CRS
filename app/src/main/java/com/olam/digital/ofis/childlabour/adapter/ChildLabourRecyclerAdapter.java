package com.olam.digital.ofis.childlabour.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemChildClickListener;

import java.util.List;

public class ChildLabourRecyclerAdapter extends RecyclerView.Adapter<ChildLabourRecyclerAdapter.ChildHolder>{

    private OnItemChildClickListener onItemChildClickListener;
    private List<ChildrenDTO> childrenList;
    private Context mContext;

    public ChildLabourRecyclerAdapter(List<ChildrenDTO> childrenList, Context context) {
        this.childrenList=childrenList;
        this.mContext=context;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener clickListener) {
        this.onItemChildClickListener = clickListener;
    }

    @NonNull
    @Override
    public ChildHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_list_row,null);
        return new ChildHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildHolder holder, final int position) {
        final ChildrenDTO childrenDTO=childrenList.get(position);
        holder.name.setText(childrenDTO.getName());
        holder.gender.setText(childrenDTO.getGender());
        holder.age.setText(childrenDTO.getAge());

        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemChildClickListener.onChildClick(v,position,childrenDTO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childrenList.size();
    }

    public class ChildHolder extends RecyclerView.ViewHolder {
        TextView name,gender,age;
        LinearLayout rowItem;
        public ChildHolder(View itemView) {
            super(itemView);
            rowItem=(LinearLayout)itemView.findViewById(R.id.itemCardView);
            name = (TextView) itemView.findViewById(R.id.name);
            gender = (TextView) itemView.findViewById(R.id.gender);
            age = (TextView) itemView.findViewById(R.id.age);
        }
    }
}
