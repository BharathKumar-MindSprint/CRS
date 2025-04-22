package com.olam.digital.ofis.farmstakeholder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StakeholderSurveyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ModulesDTO> modules;
    private OnItemClickListener listener;

    private MultiStakeHolder stakeHolder;

    public interface OnItemClickListener {
        void onItemClick(View view, ModulesDTO item);
    }

    public StakeholderSurveyAdapter(Context context, List<ModulesDTO> modules, MultiStakeHolder stakeHolder, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.stakeHolder = stakeHolder;

        // Filter survey based on MS role list
        this.modules = new ArrayList<>();
        for (ModulesDTO item : modules) {
            String surveyType = item.getSurveyType();
            if (surveyType != null) {
                if (Objects.equals(surveyType, Utilities.SURVEY_TYPE_FLMRS)) {
                    String farmerRoleId = item.farmerRoleId;

                    if (!farmerRoleId.equals("[]") ) {
                        List<Integer> moduleRoleIds = new ArrayList<>();
                        farmerRoleId = farmerRoleId.replaceAll("[\\[\\]]", "");
                        String[] dataArray = farmerRoleId.split(",");

                        for (String data : dataArray) {
                            moduleRoleIds.add(Integer.parseInt(data.trim()));
                        }

                        if (moduleRoleIds.contains(Integer.parseInt(stakeHolder.msRoleId))) {
                            this.modules.add(item);
                        }
                    }

                } else {
                    this.modules.add(item);
                }
            }
        }


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_module_list, parent, false);
        return new SurveyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ModulesDTO item = modules.get(position);
        SurveyViewHolder surveyHolder = (SurveyViewHolder) holder;

        String surveyType = item.getSurveyType();
        if (surveyType != null && Objects.equals(surveyType, Utilities.SURVEY_TYPE_FLMRS)) {
            surveyHolder.moduleName.setText(item.getModule_name());
            surveyHolder.moduleStatus.setText(Utilities.FLMRS);
            surveyHolder.enabledTextView.setText("true");
            surveyHolder.surveyType.setText(item.getSurveyType());

            surveyHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, item);
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return modules.size();
    }

    public static class SurveyViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;
        public ImageView moduleIcon;
        public TextView moduleName;
        public TextView moduleStatus;
        public TextView enabledTextView;
        public TextView surveyType;

        public SurveyViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.rowlin);
            moduleIcon = itemView.findViewById(R.id.moduleListModuleIcon);
            moduleName = itemView.findViewById(R.id.moduleListModuleName);
            moduleStatus = itemView.findViewById(R.id.moduleListModuleStatus);
            enabledTextView = itemView.findViewById(R.id.moduleListEnabled);
            surveyType = itemView.findViewById(R.id.surveyType);
        }
    }

}
