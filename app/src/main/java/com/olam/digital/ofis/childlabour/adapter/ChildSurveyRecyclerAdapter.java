package com.olam.digital.ofis.childlabour.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemSurveyListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

// Child Survey is populated and handles the visibility,active /Inactive in this adapter.
public class ChildSurveyRecyclerAdapter extends RecyclerView.Adapter<ChildSurveyRecyclerAdapter.ChildSurveyHolder> {

    private OnItemSurveyListener onItemSurveyListener;
    private List<ChildModuleDto> childModuleList;
    private Context mContext;
    private final Calendar currentDate;
    private  boolean hasSubmittedModules=false;
    private JSONArray submittedModulesArray;
    private ChildrenDTO childrenDTO;
    private boolean householdFlag;
    private boolean inspectionFlag;
    private boolean followupFlag;
    private int followUpCount = 0;
    private int currentFollowUpCount = 0;

    public ChildSurveyRecyclerAdapter(List<ChildModuleDto> childModuleDtoList, Context context, JSONArray submittedModules,ChildrenDTO childrenDTO,JSONObject moduleDtoJson, Integer followUpCount) throws JSONException {
        this.childModuleList = childModuleDtoList;
        this.currentDate=Calendar.getInstance();
        this.mContext = context;
        this.childrenDTO=childrenDTO;
        this.followUpCount = followUpCount == null ? 1 : followUpCount + 1;
        if (submittedModules !=null && submittedModules.length()>0) {
            hasSubmittedModules = true;
            if (hasSubmittedModules) {
                submittedModulesArray = submittedModules;
            }
        }

        try {
            if (moduleDtoJson!=null){
                this.householdFlag=moduleDtoJson.getBoolean(ChildLabourUtils.HOUSEHOLD_ENABLED);
                this.inspectionFlag=moduleDtoJson.getBoolean(ChildLabourUtils.INSPECTION_ENABLED);
                this.followupFlag=moduleDtoJson.getBoolean(ChildLabourUtils.FOLLOWUP_ENABLED);
            }
        }catch (Exception e){}

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
        boolean submitted = false;
        int currentId = Integer.parseInt(childModuleDto.get_id());

        if(childModuleDto.getStatus_finished()!=null &&childModuleDto.getStatus_started()!=null) {
            try {
                if (Integer.parseInt(childModuleDto.getStatus_finished()) == 1) {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                    holder.surveyLayout.setEnabled(false);
                } else if (Integer.parseInt(childModuleDto.getStatus_started()) == 1 && Integer.parseInt(childModuleDto.getStatus_finished()) == 0) {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_saved));
                    holder.surveyLayout.setEnabled(true);
                } else {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_childsurvey));
                }
            } catch (Exception e) {

            }
        }else{
            holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_childsurvey));
        }

        if (hasSubmittedModules) {
            try {
                for (int i = 0; i < submittedModulesArray.length(); i++) {
                    JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
                    if (submittedModule.has("id") && submittedModule.getInt("id") == currentId) {
                        holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                        holder.surveyLayout.setEnabled(false);
                        submitted = true;
                        break;
                    }
                }
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("CustomListAdapter_Modules: Error setting recurrence values", e));
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (!submitted) {
            holder.surveyLayout.setEnabled(true);
            if(childModuleDto.getStatus_started() !=null && childModuleDto.getStatus_finished() !=null){
                if (childModuleDto.getStatus_started().equalsIgnoreCase("1") && childModuleDto.getStatus_finished().equalsIgnoreCase("0")) {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_saved));
                } else if (childModuleDto.getStatus_started().equalsIgnoreCase("1") && childModuleDto.getStatus_finished().equalsIgnoreCase("1")) {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_completed));
                } else {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_childsurvey));
                }
            }else{
                holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_childsurvey));
            }
        }

        try {
            if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.FOLLOWUP_SURVEY_CODE)) {
                if (!submitted){
                    holder.surveyLayout.setEnabled(false);
                    holder.surveyLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    holder.surveyName.setTextColor(mContext.getResources().getColor(R.color.ofis_list_item_disabled_text));
                }
            }
        }catch (Exception e){}

        // Module DTO Concepts
        if (childrenDTO.getModuleDTO()!=null) {
            try {
                if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.HOUSEHOLD_SURVEY_CODE)) {
                    if (submitted){
                        holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                        holder.surveyLayout.setEnabled(false);
                        holder.surveyName.setTextColor(mContext.getResources().getColor(R.color.ofis_list_item_disabled_text));
                    }else {
                        if (!householdFlag) {
                            holder.surveyLayout.setEnabled(false);
                            holder.surveyName.setTextColor(mContext.getResources().getColor(R.color.ofis_list_item_disabled_text));
                        } else {
                            holder.surveyLayout.setEnabled(true);
                        }
                    }
                }

                if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.INSPECTION_SURVEY_CODE)) {
                    if (submitted){
                        holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                        holder.surveyLayout.setEnabled(false);
                        holder.surveyName.setTextColor(mContext.getResources().getColor(R.color.ofis_list_item_disabled_text));
                    }else {
                        if (!inspectionFlag) {
                            holder.surveyName.setTextColor(mContext.getResources().getColor(R.color.ofis_list_item_disabled_text));
                            holder.surveyLayout.setEnabled(false);
                        } else {
                            holder.surveyLayout.setEnabled(true);
                        }
                    }
                }

                if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.FOLLOWUP_SURVEY_CODE)) {
                    currentFollowUpCount++;
                    if (submitted && currentFollowUpCount == followUpCount) {
                        holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                        holder.surveyLayout.setEnabled(false);
                        holder.surveyName.setTextColor(mContext.getResources().getColor(R.color.ofis_list_item_disabled_text));
                    }else{
                        if (followupFlag && currentFollowUpCount == followUpCount) {
                            holder.surveyLayout.setEnabled(true);
                            holder.surveyLayout.setBackgroundResource(R.drawable.sel_survey_item_bg);
                            holder.surveyName.setTextColor(mContext.getResources().getColor(R.color.black));
                        } else {
                            holder.surveyLayout.setEnabled(false);
                            holder.surveyName.setTextColor(mContext.getResources().getColor(R.color.ofis_list_item_disabled_text));
                            if (followUpCount > 1) {
                                holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        try{
            if (childrenDTO.getAge() != null && Integer.parseInt(childrenDTO.getAge()) >= 18) {
                holder.surveyLayout.setEnabled(false);
            }
        }catch (Exception e){}


        if (holder.surveyLayout.isEnabled()){
            holder.rowItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemSurveyListener.onChildClick(v, position, childModuleDto);
                }
            });
        }else{
            holder.rowItem.setOnClickListener(null);
        }
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
