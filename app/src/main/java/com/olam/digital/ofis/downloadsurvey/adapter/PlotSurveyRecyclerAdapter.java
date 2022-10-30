package com.olam.digital.ofis.downloadsurvey.adapter;

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

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
import com.olam.digital.ofis.childlabour.listener.OnItemSurveyListener;
import com.olam.digital.ofis.downloadsurvey.dto.FarmerDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

// Child Survey is populated and handles the visibility,active /Inactive in this adapter.
public class PlotSurveyRecyclerAdapter extends RecyclerView.Adapter<PlotSurveyRecyclerAdapter.ChildSurveyHolder> {

    private OnItemSurveyListener onItemSurveyListener;
    private List<ChildModuleDto> childModuleList;
    private Context mContext;
    private final Calendar currentDate;
    private  boolean hasSubmittedModules=false;
    private JSONArray submittedModulesArray;
    private FarmerDTO farmerDTO;


    public PlotSurveyRecyclerAdapter(List<ChildModuleDto> childModuleDtoList, Context context, JSONArray submittedModules, FarmerDTO farmerDTO) throws JSONException {
        this.childModuleList = childModuleDtoList;
        this.currentDate=Calendar.getInstance();
        this.mContext = context;
        this.farmerDTO=farmerDTO;
        if (submittedModules !=null && submittedModules.length()>0) {
            hasSubmittedModules = true;
            if (hasSubmittedModules) {
                submittedModulesArray = submittedModules;
            }
        }




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
        holder.surveyName.setText(childModuleDto.getPlotId()+"_"+childModuleDto.getName());
        boolean submitted = false;
        boolean enabledFollowUp=false;
        int currentId = Integer.parseInt(childModuleDto.get_id());

        if(childModuleDto.getStatus_finished()!=null &&childModuleDto.getStatus_started()!=null) {
            try {
                if (Integer.parseInt(childModuleDto.getStatus_finished()) == 1) {
                    holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_status_submitted));
                     holder.surveyLayout.setEnabled(false);

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

        if (hasSubmittedModules) {
            try {
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
