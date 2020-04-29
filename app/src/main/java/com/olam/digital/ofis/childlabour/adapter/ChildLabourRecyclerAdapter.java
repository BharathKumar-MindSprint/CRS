package com.olam.digital.ofis.childlabour.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemChildClickListener;

import java.util.List;

// Child List is populated and handles the active /Inactive child list in this adapter.
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
        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_list_row,null);
        return new ChildHolder(view);*/
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_list_row, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        return new ChildHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildHolder holder, final int position) {
        final ChildrenDTO childrenDTO=childrenList.get(position);
        holder.name.setText(childrenDTO.getName());
        holder.gender.setText(ChildLabourUtils.getTranslatedGender(mContext,childrenDTO.getGender()));
        holder.age.setText(childrenDTO.getAge() + " "+mContext.getResources().getString(R.string.yrs) );

        if(childrenDTO.getSynced().equalsIgnoreCase("1")){
            holder.arrow.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_verified));
        }else{
            holder.arrow.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_right_arrow));
        }

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
        CardView rowItem;
        ImageView arrow;
        public ChildHolder(View itemView) {
            super(itemView);
            rowItem=(CardView)itemView.findViewById(R.id.itemCardView);
            name = (TextView) itemView.findViewById(R.id.name);
            gender = (TextView) itemView.findViewById(R.id.gender);
            age = (TextView) itemView.findViewById(R.id.age);
            arrow = (ImageView) itemView.findViewById(R.id.arrow);
        }
    }
}
