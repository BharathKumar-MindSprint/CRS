package com.olam.digital.ofis.childlabour.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemSurveyListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Child Survey is populated and handles the visibility,active /Inactive in this adapter.
public class ChildSurveyRecyclerAdapter extends RecyclerView.Adapter<ChildSurveyRecyclerAdapter.ChildSurveyHolder> {

    private OnItemSurveyListener onItemSurveyListener;
    private List<ChildModuleDto> childModuleList;
    private Context mContext;
    private final Calendar currentDate;
    private  boolean hasSubmittedModules=false;
    private JSONArray submittedModulesArray;
    private ChildrenDTO childrenDTO;
    private JSONArray actionTickedArray;
    private boolean householdFlag;
    private boolean inspectionFlag;
    private boolean followupFlag;


    public ChildSurveyRecyclerAdapter(List<ChildModuleDto> childModuleDtoList, Context context, JSONArray submittedModules,ChildrenDTO childrenDTO,JSONArray actionTickedArray,JSONObject moduleDtoJson) throws JSONException {
        this.childModuleList = childModuleDtoList;
        this.currentDate=Calendar.getInstance();
        this.mContext = context;
        this.childrenDTO=childrenDTO;
        if (submittedModules !=null && submittedModules.length()>0) {
            hasSubmittedModules = true;
            if (hasSubmittedModules) {
                submittedModulesArray = submittedModules;
            }
        }
        if (actionTickedArray !=null && actionTickedArray.length()>0){
            this.actionTickedArray=actionTickedArray;
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
        boolean enabledFollowUp=false;
        int currentId = Integer.parseInt(childModuleDto.get_id());
//        holder.surveyLayout.clearFocus();
//        if (position != 2)
//            holder.surveyLayout.setEnabled(true);
//        else
//            holder.surveyLayout.setEnabled(false);

        if(childModuleDto.getStatus_finished()!=null &&childModuleDto.getStatus_started()!=null) {
            try {
                if (Integer.parseInt(childModuleDto.getStatus_finished()) == 1) {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                     holder.surveyLayout.setEnabled(false);
//                    holder.surveyLayout.setBackgroundResource(R.drawable.list_item_disabled);
//                    holder.surveyName.setTextColor(ContextCompat.getColor(mContext, R.color.ofis_list_item_disabled_text));

                } else if (Integer.parseInt(childModuleDto.getStatus_started()) == 1 && Integer.parseInt(childModuleDto.getStatus_finished()) == 0) {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_saved));
                    holder.surveyLayout.setEnabled(true);
//                    holder.surveyLayout.setBackgroundResource(R.drawable.list_item);
//                    holder.surveyName.setTextColor(ContextCompat.getColor(mContext, R.color.ofis_base_font_dark));
                } else {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_childsurvey));
                }
            } catch (Exception e) {

            }
        }else{
            holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_childsurvey));
        }
//                String disabled_months_string = childModuleDto.getDisabled_months();
//                List<String> disabled_months = Arrays.asList(disabled_months_string.split("\\s*,\\s*"));


        // check after 3 months scenerio
        // Migrating code to above submitted modules
//        if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.FOLLOWUP_SURVEY_CODE)) {
//            if (actionTickedArray != null) {
//                if (actionTickedArray.length() > 0) {
//                    try {
//                        JSONObject actionTick = actionTickedArray.getJSONObject(0);
//                        String actionTickDate = actionTick.getString(ChildLabourUtils.ACTION_DATE);
//                        String after3MonthsDate = ChildLabourUtils.getAfterThreeMonths(actionTickDate);
//                        if (Utilities.dateStringToMilliSeconds(after3MonthsDate) < Utilities.currentDateStringToMilliSeconds()) {
//                            holder.surveyLayout.setEnabled(true);
//                            enabledFollowUp=true;
//                        } else {
//                            holder.surveyLayout.setEnabled(false);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    holder.surveyLayout.setEnabled(false);
//                }
//            } else {
//                holder.surveyLayout.setEnabled(false);
//            }
//
//        }


        if (hasSubmittedModules) {
            try {
//                        int recurrence_frequency =Integer.parseInt(childModuleDto.getRecurrence_frequency());
//                        String recurrence_type = childModuleDto.getRecurrence_type();
//                        Calendar calculationDate = (Calendar) currentDate.clone();
                boolean disable_entry = false;

                for (int i = 0; i < submittedModulesArray.length(); i++) {
                    JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
                 //   JSONObject submittedModuleDetails = submittedModule.getJSONObject("module");
                    if (submittedModule.has("id") && submittedModule.getInt("id") == currentId) {
                        disable_entry = true;

                        if (disable_entry) {
                            holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                            holder.surveyLayout.setEnabled(false);
//                            holder.surveyLayout.setBackgroundResource(R.drawable.list_item_disabled);
//                            holder.surveyName.setTextColor(ContextCompat.getColor(mContext, R.color.ofis_list_item_disabled_text));
                            submitted = true;
                            break;
                        }
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

        // disable in follow up survey while register new child
        try {
            if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.FOLLOWUP_SURVEY_CODE)) {
                if (!submitted && !enabledFollowUp){
                    holder.surveyLayout.setEnabled(false);
                }
            }
        }catch (Exception e){}

        // Module DTO Concepts
        if (childrenDTO.getModuleDTO()!=null) {
            try {
                if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.HOUSEHOLD_SURVEY_CODE)) {
                    if (!householdFlag) {
                        holder.surveyLayout.setEnabled(false);
                    } else {
                        holder.surveyLayout.setEnabled(true);
                    }
                }
            } catch (Exception e) {
            }

            try {
                if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.INSPECTION_SURVEY_CODE)) {
                    if (!inspectionFlag) {
                        holder.surveyLayout.setEnabled(false);
                    } else {
                        holder.surveyLayout.setEnabled(true);
                    }
                }
            } catch (Exception e) {
            }

            try {
                if (childModuleDto.getSurveyCode().equalsIgnoreCase(ChildLabourUtils.FOLLOWUP_SURVEY_CODE)) {
                    if (!followupFlag) {
                        holder.surveyLayout.setEnabled(false);
                    } else {
                        holder.surveyLayout.setEnabled(true);
                    }
                }
            } catch (Exception e) {
            }
        }




        // deal submitted and disabled here

//        holder.surveyLayout.clearFocus();
//        if (position != 2)
//            holder.surveyLayout.setEnabled(true);
//        else
//            holder.surveyLayout.setEnabled(false);

        try{
            if (childrenDTO.getAge() != null) {
                if (Integer.parseInt(childrenDTO.getAge()) >= 18) {
                    holder.surveyLayout.setEnabled(false);
                }
            }
        }catch (Exception e){}


        if (holder.surveyLayout.isEnabled()){
        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!childModuleDto.isEnable() && childModuleDto.getSurveyCode().equalsIgnoreCase("03")) {
                    onItemSurveyListener.onChildClick(v, position, childModuleDto);
//                }
            }
        });}else{
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
