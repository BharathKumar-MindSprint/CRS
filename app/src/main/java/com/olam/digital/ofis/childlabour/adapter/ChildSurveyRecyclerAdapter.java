package com.olam.digital.ofis.childlabour.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemSurveyListener;

import java.util.List;

public class ChildSurveyRecyclerAdapter extends RecyclerView.Adapter<ChildSurveyRecyclerAdapter.ChildSurveyHolder> {

    private OnItemSurveyListener onItemSurveyListener;
    private List<ChildModuleDto> childModuleList;
    private Context mContext;

    public ChildSurveyRecyclerAdapter(List<ChildModuleDto> childModuleDtoList, Context context) {
        this.childModuleList = childModuleDtoList;
        this.mContext = context;
    }

    public void setOnItemSurveyClickListener(OnItemSurveyListener clickListener) {
        this.onItemSurveyListener = clickListener;
    }

    @NonNull
    @Override
    public ChildSurveyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_survey_row, null);
        return new ChildSurveyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildSurveyHolder holder, final int position) {
        final ChildModuleDto childModuleDto = childModuleList.get(position);
        holder.surveyName.setText(childModuleDto.getName());

        holder.surveyLayout.clearFocus();
        if (position != 2)
            holder.surveyLayout.setEnabled(true);
        else
            holder.surveyLayout.setEnabled(false);

        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemSurveyListener.onChildClick(v, position, childModuleDto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childModuleList.size();
    }

    public class ChildSurveyHolder extends RecyclerView.ViewHolder {
        TextView surveyName;
        ImageView statusIcon;
        RelativeLayout surveyLayout;
        CardView rowItem;

        public ChildSurveyHolder(View itemView) {
            super(itemView);
            rowItem = (CardView) itemView.findViewById(R.id.rowCardView);
            surveyName = itemView.findViewById(R.id.surveyName);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            surveyLayout = itemView.findViewById(R.id.rl_child_survey_row);


        }
    }
}
