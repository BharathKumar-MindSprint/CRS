package com.olam.digital.ofis.downloadsurvey.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
import com.olam.digital.ofis.downloadsurvey.dto.FarmerDTO;
import com.olam.digital.ofis.downloadsurvey.dto.PlotDTO;
import com.olam.digital.ofis.multifarm.MultiFarmUtils;
import com.olam.digital.ofis.multifarm.SubmittedInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {
    private final int roleId;
    private Context context;
    private List<PlotDTO> expandableListTitle;
    private LinkedHashMap<PlotDTO, List<ChildModuleDto>> expandableListDetail;
    private final boolean hasSubmittedModules;
    private JSONArray submittedModulesArray;
    private final Calendar currentDate;
    private final DataLayer dl;
    private final Resources res;
    long farmerId;
    private String moduleColumnSuffix = "";
    String multiFarmId;

    public CustomExpandableListAdapter(Context context, List<PlotDTO> expandableListTitle,
                                       LinkedHashMap<PlotDTO, List<ChildModuleDto>> expandableListDetail,
                                       JSONObject submittedModules_,
                                       FarmerDTO farmerDTO,
                                       Resources res_,
                                       int roleId,
                                       String multiFarmId) throws JSONException {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        hasSubmittedModules = (submittedModules_.has("modules"));
        if (hasSubmittedModules) {
            submittedModulesArray = submittedModules_.getJSONArray("modules");
        }
        this.currentDate = Calendar.getInstance();
        this.res = res_;
        this.dl = new DataLayer(res, context);
        farmerId = Long.parseLong(farmerDTO.getId());
        this.moduleColumnSuffix = Utilities.getLocaleSuffix(res);
        this.roleId=roleId;
        this.multiFarmId=multiFarmId;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final ChildModuleDto childModuleDto = (ChildModuleDto) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
//        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.moduleListModuleName);
//        TextView status = (TextView) convertView.findViewById(R.id.moduleListModuleStatus);
        LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.rowlin);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.moduleListModuleIcon);
        TextView moduleTitle = (TextView) convertView.findViewById(R.id.moduleListModuleName);
        TextView textView = (TextView) convertView.findViewById(R.id.moduleListModuleStatus);
        TextView enabledItem = (TextView) convertView.findViewById(R.id.moduleListEnabled);
        boolean submitted = false;
//        expandedListTextView.setText(expandedListText);
        moduleTitle.setText(childModuleDto.getName());
        textView.setText("New Task");
        int currentId = Integer.parseInt(childModuleDto.get_id());
        System.out.println("CURRENTID_:EL" +currentId);
        int status_started = 0;
        int status_finished = 0;
        try {
            if(currentId == 164) {
                System.out.println("CURRENTID_EL:COLUMN_NAME_STATUS_STARTED" +  childModuleDto.getStatus_started());
                System.out.println("CURRENTID_EL:COLUMN_NAME_STATUS_STARTED" +  childModuleDto.getStatus_finished());
            }
            PlotDTO plotDTO = expandableListTitle.get(listPosition);
            ChildModuleDto moduleDto = dl.getFarmerModuleDetailsByPlotId(Long.parseLong(childModuleDto.get_id()),farmerId,plotDTO.getPlotId());
//            String statusStarted = childModuleDto.getStatus_started();
//            String statusFinished = childModuleDto.getStatus_finished();
            String statusStarted = moduleDto.getStatus_started();
            String statusFinished = moduleDto.getStatus_finished();
            boolean hasPlotData = dl.getModuledetailsByPlotId(currentId, farmerId, plotDTO.getPlotId());
            if(hasPlotData) {
                if (!statusStarted.equalsIgnoreCase("null")) {
                    status_started = Integer.parseInt(statusStarted);
                }
                if (!statusFinished.equalsIgnoreCase("null")) {
                    status_finished = Integer.parseInt(statusFinished);
                }
            }else{
                status_started = 0;status_finished = 0;
            }
        }catch (NumberFormatException e){
            status_started = 0;status_finished = 0;
        }catch (Exception e){
            status_started = 0;status_finished = 0;
        }
        if (currentId < 0) {
            if (status_finished == 1) {
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));
                enabledItem.setText("false");

                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                textView.setText(res.getString(R.string.review_farm_management_plan_complete));
            } else if (status_started == 1 && status_finished == 0) {
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_saved));
                enabledItem.setText("true");

                linearLayout.setBackgroundResource(R.drawable.list_item);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                textView.setText(res.getString(R.string.review_farm_management_plan_in_progress));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_fmp_available));
                enabledItem.setText("true");

                linearLayout.setBackgroundResource(R.drawable.list_item);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                textView.setText(res.getString(R.string.review_farm_management_plan_hint));
            }
        } else {

            String disabled_months_string = childModuleDto.getDisabled_months();
            List<String> disabled_months = Arrays.asList(disabled_months_string.split("\\s*,\\s*"));
            if(currentId == 164) {
                System.out.println("CURRENTID_EL:disabled_months_string" +  disabled_months_string);
                System.out.println("CURRENTID_EL:disabled_months" + disabled_months.size());
            }
            if(currentId == 164) {
                System.out.println("CURRENTID_EL:hasSubmittedModules" +  hasSubmittedModules);
            }
            if (hasSubmittedModules) {
                try {
                    int recurrence_frequency = Integer.parseInt(childModuleDto.getRecurrence_frequency());
                    String recurrence_type = childModuleDto.getRecurrence_type();
                    Calendar calculationDate = (Calendar) currentDate.clone();
                    boolean disable_entry = false;
                    if(currentId == 164) {
                        System.out.println("CURRENTID_EL:recurrence_frequency" +  recurrence_frequency);
                        System.out.println("CURRENTID_EL:recurrence_type" +  recurrence_type);
                    }
                    for (int i = 0; i < submittedModulesArray.length(); i++) {
                        JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
                        JSONObject submittedModuleDetails = submittedModule.getJSONObject("module");
                        PlotDTO plotDTO = expandableListTitle.get(listPosition);
                        int plotId = plotDTO.getPlotId();

//                        int currentId = Integer.parseInt(childModuleDto.get_id());
                        if (submittedModuleDetails.has("id") &&
                                submittedModuleDetails.getInt("id") == currentId &&
                                submittedModuleDetails.getInt("plotId") == plotId ) {
                        if(currentId == 164) {
                            try {
                                System.out.println("CURRENTID_EL:plotId" + plotId);
                            }catch (Exception e){}
                        }
                            if (recurrence_frequency > 0 && !disabled_months.contains(String.valueOf(currentDate.get(Calendar.MONTH)))) {
                                // for recurring
                                try {
                                    // get last submitted date
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                                    Calendar lastSubmittedDate = Calendar.getInstance();

                                    //O20HS-84 - To enable the annual survey.
                                    calculationDate = (Calendar) currentDate.clone();
                                    //

                                    try {
                                        lastSubmittedDate.setTime(sdf.parse(submittedModuleDetails.getString("created_at")));
                                    } catch (ParseException e) {
                                        FirebaseCrash.report(new Exception("CustomListAdapter_Modules: Error setting recurrence date values", e));
                                        e.printStackTrace();
                                    }
                                    if (recurrence_type != null) {
                                        if (recurrence_type.equalsIgnoreCase("years")) {
                                            calculationDate.add(Calendar.YEAR, -recurrence_frequency);
                                            calculationDate.set(Calendar.MONTH, Calendar.DECEMBER);
                                            calculationDate.set(Calendar.DAY_OF_MONTH, 31);
                                        } else if (recurrence_type.equalsIgnoreCase("months")) {
                                            calculationDate.add(Calendar.MONTH, -recurrence_frequency);
                                            calculationDate.set(Calendar.DAY_OF_MONTH, calculationDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                                        } else if (recurrence_type.equalsIgnoreCase("weeks")) {
                                            calculationDate.add(Calendar.WEEK_OF_YEAR, -recurrence_frequency);
                                            calculationDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                                        } else {
                                            // must be days
                                            calculationDate.add(Calendar.DAY_OF_YEAR, -recurrence_frequency);
                                        }

                                        // set the time
                                        calculationDate.set(Calendar.HOUR_OF_DAY, 23);
                                        calculationDate.set(Calendar.MINUTE, 59);
                                        calculationDate.set(Calendar.SECOND, 59);
                                        calculationDate.set(Calendar.MILLISECOND, 999);

                                        // force the recalculation
                                        calculationDate.get(Calendar.DAY_OF_MONTH);


                                        if (lastSubmittedDate.after(calculationDate)) {
                                            disable_entry = true;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // for non-recurring
                                disable_entry = true;
                            }

                            if (disable_entry) {
                                textView.setText(submittedModuleDetails.getString("created_at"));
                                submittedModuleDetails.put("marked", true);
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));

                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                enabledItem.setText("false");
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
            if(currentId == 164) {
                        System.out.println("CURRENTID_EL:submitted" +  submitted);
            }
            if (!submitted) {
                enabledItem.setText("true");
                linearLayout.setBackgroundResource(R.drawable.list_item);
                /*boolean viewExistingdata = dl.hasExistingAns(String.valueOf(farmerId));
//                viewModule = cursor.getString(cursor.getColumnIndex("view_module"));
                viewModule = cursor.getString(cursor.getColumnIndex("view_module"));

                if(viewExistingdata && viewModule.equalsIgnoreCase("1")) {
                    moduleTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_folder, 0);
                }else{
                    moduleTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                }*/
                long farmerGroupId = dl.getFarmerGroupById(farmerId);
                long sectionId = dl.getSectionByFarmerId(farmerId);
//                int currentId = Integer.parseInt(childModuleDto.get_id());
                boolean value = dl.farmerExistByfarmerGroupSection(farmerGroupId, sectionId, currentId);
                if (value) {
                    boolean viewExistingdata = dl.isSurveyAnsDownloadedByModuleId(currentId, farmerId);
                    if (viewExistingdata) {
                        moduleTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_folder, 0);
                    } else {
                        moduleTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                    }
                } else {
                    moduleTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                }
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                PlotDTO plotDTO = expandableListTitle.get(listPosition);
                int plotId = plotDTO.getPlotId();
                boolean hasPlotData = dl.getModuledetailsByPlotId(currentId, farmerId, plotId);
                if (status_started == 1 && status_finished == 0 && hasPlotData) {
//                    int columnIndex = childModuleDto.getSubmodule_name();
                    textView.setText(res.getString(R.string.module_in_progress) + childModuleDto.getSubmodule_name());
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_saved));
                } else if (status_started == 1 && status_finished == 1 && hasPlotData) {
                    textView.setText(res.getString(R.string.module_finished));
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                } else {
                     imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                     textView.setText(res.getString(R.string.ModuleStatus));
                     if (disabled_months.contains(String.valueOf(currentDate.get(Calendar.MONTH)))) {
                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        enabledItem.setText("false");

                     }
}
            }
            if (MultiFarmUtils.isMultiFarmEnumerator) {

                try {

                    String farmSurvey = childModuleDto.getIsFarmSurvey();
                    boolean isFarmSurvey;
                    String farmerRoleStr = childModuleDto.getFkFarmerRoleId();
                    int farmerRole;
                    if (farmSurvey != null) {
                        try {
                            isFarmSurvey = Boolean.parseBoolean(farmSurvey);
                            try {
                                farmerRole = Integer.parseInt(farmerRoleStr);
                            } catch (Exception e) {
                                farmerRole = 0;
                            }

                            if (!isFarmSurvey) {
                                //// FARM-ER SURVEY CODE
                                if (farmerRole != 0 && roleId != 0) {
                                    if (farmerRole == roleId) {
                                        // Enable
                                        if (!submitted) {
                                            enabledItem.setText("true");
                                            linearLayout.setBackgroundResource(R.drawable.list_item);
                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                        } else {
                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            enabledItem.setText("false");
                                        }
                                    } else {

                                        // Can be completed by other stakeholders
                                        //  showAlertMsg(linearLayout, res.getString(R.string.survey_can_be_completed_by_other_stakeholders));
                                        enabledItem.setTag("farmerS");

                                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                        enabledItem.setText("false");
                                    }
                                }
                            } else {
                                // FARM SURVEY CODE
//                                ------------START ----------------
//
                                try {
                                    //                                  SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules(cursor, currentId, String.valueOf(farmerId));
                                    //     SubmittedInfo submittedInfo =farmSubmittedHashMapList.get(currentId);
//                                    int currentId = Integer.parseInt(childModuleDto.get_id());
                                    PlotDTO plotDTO = expandableListTitle.get(listPosition);
                                    int plotId = plotDTO.getPlotId();
                                    SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules2(childModuleDto, currentId, farmerId, multiFarmId,plotId);

                                    if (farmerRole != 0 && roleId != 0) {
                                        if (farmerRole == roleId) {
                                            // Enable
                                            if (submittedInfo.isSubmitted()) {
                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));


                                                if (submittedInfo.isNetworkTable()) {
                                                    if (submittedInfo.getFarmerId() == farmerId) {
                                                        enabledItem.setText("true");
                                                        linearLayout.setBackgroundResource(R.drawable.list_item);
                                                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                        textView.setText(res.getString(R.string.module_finished));
                                                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));

                                                    } else {
                                                        if (submittedInfo.isAvailableInOtherFarm()) {
                                                            textView.setText(res.getString(R.string.ModuleStatus));
                                                        }
                                                        enabledItem.setText("false");
                                                        enabledItem.setTag("hasfarm");
                                                    }
                                                } else {
                                                    if (submittedInfo.isAvailableInOtherFarm()) {
                                                        textView.setText(res.getString(R.string.ModuleStatus));
                                                    }
                                                    enabledItem.setText("false");
                                                    enabledItem.setTag("hasfarm");
                                                }

                                                // has beed completed by other stakeholders
                                                //    showAlertMsgIdCheck(linearLayout, res.getString(R.string.already_syned_survey_info),submittedInfo);

                                            } else {

                                                boolean othersAttneded = dl.isSurveyAttendedByOther(multiFarmId, farmerId, currentId, plotId);
                                                if (othersAttneded) {
                                                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    enabledItem.setText("false");
                                                    enabledItem.setTag("hasfarm");
                                                } else {
                                                    enabledItem.setText("true");
                                                    textView.setText(res.getString(R.string.ModuleStatus));
                                                    linearLayout.setBackgroundResource(R.drawable.list_item);
                                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                }
                                            }
                                        } else {
                                            // Disable
                                            // Can be completed by other stakeholders
                                            // showAlertMsg(linearLayout, res.getString(R.string.survey_can_be_completed_by_other_stakeholders));

                                            enabledItem.setTag("canfarm");

                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            enabledItem.setText("false");
                                        }
                                    } else {
                                        // Farm Survey - Role is not defined for the farm stakeholders
                                        if (roleId != 0 && farmerRole == 0) {
                                            if (submittedInfo.isSubmitted()) {
                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));


                                                if (submittedInfo.isNetworkTable()) {
                                                    if (submittedInfo.getFarmerId() == farmerId) {
                                                        enabledItem.setText("true");
                                                        linearLayout.setBackgroundResource(R.drawable.list_item);
                                                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                        textView.setText(res.getString(R.string.module_finished));
                                                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                                                    } else {
                                                        if (submittedInfo.isAvailableInOtherFarm()) {
                                                            textView.setText(res.getString(R.string.ModuleStatus));
                                                        }
                                                        enabledItem.setText("false");
                                                        enabledItem.setTag("hasfarm");
                                                    }
                                                } else {
                                                    if (submittedInfo.isAvailableInOtherFarm()) {
                                                        textView.setText(res.getString(R.string.ModuleStatus));
                                                    }
                                                    enabledItem.setText("false");
                                                    enabledItem.setTag("hasfarm");
                                                }


                                                // Put listener call for the long press
                                                //  showAlertMsgIdCheck(linearLayout, res.getString(R.string.already_syned_survey_info),submittedInfo);

                                            } else {
                                                boolean othersAttneded = dl.isSurveyAttendedByOther(multiFarmId, farmerId, currentId, plotId);
                                                if (othersAttneded) {
                                                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    enabledItem.setText("false");
                                                    enabledItem.setTag("hasfarm");
                                                } else {
                                                    enabledItem.setText("true");
                                                    textView.setText(res.getString(R.string.ModuleStatus));
                                                    linearLayout.setBackgroundResource(R.drawable.list_item);
                                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


//                                ------------END ----------------

                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        PlotDTO plotDTO = (PlotDTO) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView plotName = (TextView) convertView.findViewById(R.id.plotName);
        TextView plotId = (TextView) convertView.findViewById(R.id.plotId);
        plotName.setTypeface(null, Typeface.BOLD);
        plotName.setText(plotDTO.getPlotName());
        plotId.setText("Plot ID:"+plotDTO.getPlotId());
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}