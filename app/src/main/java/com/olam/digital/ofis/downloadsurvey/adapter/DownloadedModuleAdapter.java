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
import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;
import com.olam.digital.ofis.downloadsurvey.listener.OnItemDownloadedSurveyClickListener;
import com.olam.digital.ofis.downloadsurvey.listener.OnItemSurveyClickListener;

import java.util.List;

public class DownloadedModuleAdapter extends RecyclerView.Adapter<DownloadedModuleAdapter.SurveyHolder> {

    private OnItemDownloadedSurveyClickListener onItemSurveyClickListener;
    private List<AnswerRequestDTO> moduleDTOList;
    private Context mContext;

    public DownloadedModuleAdapter(List<AnswerRequestDTO> moduleDTOList, Context context) {
        this.moduleDTOList=moduleDTOList;
        this.mContext=context;
    }

    public void setOnItemClickListener(OnItemDownloadedSurveyClickListener clickListener) {
        this.onItemSurveyClickListener = clickListener;
    }

    public void resetItem(List<AnswerRequestDTO> moduleDTOList){
        this.moduleDTOList=moduleDTOList;
    }

    @NonNull
    @Override
    public DownloadedModuleAdapter.SurveyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.downloaded_survey_row, null);
        return new DownloadedModuleAdapter.SurveyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadedModuleAdapter.SurveyHolder holder, @SuppressLint("RecyclerView") int position) {
        final AnswerRequestDTO moduleDTO = moduleDTOList.get(position);
        holder.surveyName.setText(moduleDTO.getListedName());
        holder.dataTime.setText(moduleDTO.getDateTime());
        holder.statusIcon.setOnClickListener(new View.OnClickListener() {
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
        TextView surveyName,dataTime;
        ImageView statusIcon;
        RelativeLayout surveyLayout;
        CardView rowItem;

        public SurveyHolder(View itemView) {
            super(itemView);
            rowItem = (CardView) itemView.findViewById(R.id.rowCardView);
            surveyName = itemView.findViewById(R.id.surveyName);
            dataTime = itemView.findViewById(R.id.dataTime);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            surveyLayout = itemView.findViewById(R.id.rl_child_survey_row);
        }
    }
}