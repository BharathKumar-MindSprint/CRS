package com.olam.digital.ofis.downloadsurvey.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;
import com.olam.digital.ofis.downloadsurvey.listener.OnItemSurveyClickListener;

import java.util.List;

public class FilterModuleRecyclerAdapter extends RecyclerView.Adapter<FilterModuleRecyclerAdapter.SurveyHolder> {

    private OnItemSurveyClickListener onItemSurveyClickListener;
    private List<ModuleDTO> moduleDTOList;
    private Context mContext;

    public FilterModuleRecyclerAdapter(List<ModuleDTO> moduleDTOList, Context context) {
        this.moduleDTOList=moduleDTOList;
        this.mContext=context;
    }

    public void setOnItemClickListener(OnItemSurveyClickListener clickListener) {
        this.onItemSurveyClickListener = clickListener;
    }

    @NonNull
    @Override
    public FilterModuleRecyclerAdapter.SurveyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.child_survey_row, null);
        return new FilterModuleRecyclerAdapter.SurveyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterModuleRecyclerAdapter.SurveyHolder holder, @SuppressLint("RecyclerView") int position) {
        final ModuleDTO moduleDTO = moduleDTOList.get(position);
        holder.surveyName.setText(moduleDTO.getName());
        holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_new));
        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemSurveyClickListener.onModuleClick(v,position,moduleDTO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moduleDTOList.size();
    }

    public class SurveyHolder extends RecyclerView.ViewHolder {
        TextView surveyName;
        ImageView statusIcon;
        RelativeLayout surveyLayout;
        CardView rowItem;

        public SurveyHolder(View itemView) {
            super(itemView);
            rowItem = (CardView) itemView.findViewById(R.id.rowCardView);
            surveyName = itemView.findViewById(R.id.surveyName);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            surveyLayout = itemView.findViewById(R.id.rl_child_survey_row);


        }
    }
}