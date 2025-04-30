package com.olam.digital.ofis.farmstakeholder;

import static org.apache.commons.lang3.time.DateUtils.isSameDay;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StakeholderSurveyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ModulesDTO> modules;
    private OnItemClickListener listener;

    private MultiStakeHolder stakeHolder;


    private JSONArray submittedModules;
    private final boolean hasSubmittedModules;
    private final DataLayer dl;
    private final OFISRoomDB db;
    private final Resources res;

    public interface OnItemClickListener {
        void onItemClick(View view, ModulesDTO item);
    }

    public StakeholderSurveyAdapter(Context context, List<ModulesDTO> modules, MultiStakeHolder stakeHolder, JSONObject submittedModules, DataLayer dl, OFISRoomDB db, Resources res, OnItemClickListener listener) throws JSONException {
        this.context = context;
        this.listener = listener;
        this.stakeHolder = stakeHolder;
        this.dl = dl;
        this.db = db;
        this.res = res;
        hasSubmittedModules = (submittedModules.has("modules"));
        if (hasSubmittedModules) {
            this.submittedModules = submittedModules.getJSONArray("modules");
        }

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
            surveyHolder.moduleName.setText(getModuleTranslation(item));
            surveyHolder.moduleStatus.setText(Utilities.FLMRS);
            surveyHolder.enabledTextView.setText("true");
            surveyHolder.surveyType.setText(item.getSurveyType());

            boolean submitted = false;
            Calendar currentDate = Calendar.getInstance();
            if (hasSubmittedModules) {

                boolean disable_entry = false;
                try {
                    int recurrence_frequency = Integer.parseInt(item.recurrence_frequency);
                    String recurrence_type =  item.recurrence_type;
                    Calendar calculationDate = (Calendar) currentDate.clone();
                    String disabled_months_string = item.disabled_months;
                    List<String> disabled_months = Arrays.asList(disabled_months_string.split("\\s*,\\s*"));
                    String start_date_string = item.startDate;
                    int month = 0;
                    int day = 0;
                    int year = 0;
                    Calendar recurrenceDate = Calendar.getInstance();
                    Date startDate = null;
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        if (start_date_string != null) {
                            startDate = formatter.parse(start_date_string.split("T")[0]);
                            recurrenceDate.setTime(startDate);
                            month = recurrenceDate.get(Calendar.MONTH);
                            day =  recurrenceDate.get(Calendar.DAY_OF_MONTH);
                            year = recurrenceDate.get(Calendar.YEAR);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < submittedModules.length(); i++) {
                        JSONObject submittedModule = submittedModules.getJSONObject(i);
                        JSONObject submittedModuleDetails = submittedModule.getJSONObject("module");

                        if (submittedModuleDetails.has("id") && submittedModuleDetails.getInt("id") == item.id) {

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

                                    // code for india spices survey
                                    String assignmentTypeColumn = "";
                                    String assignmentType = "1";

                                    switch (assignmentType){
                                        case "1" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID;
                                            break;
                                        case "2" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID;
                                            break;
                                        case "3" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID;
                                            break;
                                        case "4" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID;
                                            break;
                                        case "5" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID;
                                            break;
                                        case "6" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID;
                                            break;
                                        case "7" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID;
                                            break;
                                        case "8" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID;
                                            break;
                                        case "9" :
                                            assignmentTypeColumn = OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID;
                                            break;
                                    }

                                    String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(stakeHolder.farmerId , assignmentTypeColumn);

                                    if (recurrence_type != null) {
                                        if (recurrence_type.equalsIgnoreCase("years")) {

                                            if (Objects.equals(farmerAssignmentField, "48")){

                                                // set the date according to start date
                                                calculationDate.add(Calendar.YEAR, -recurrence_frequency);
                                                calculationDate.set(Calendar.MONTH, Calendar.JULY);
                                                calculationDate.set(Calendar.DAY_OF_MONTH, 31);

                                            }else {
                                                Calendar nextRecurrenceDate = (Calendar) recurrenceDate.clone();
                                                nextRecurrenceDate.add(Calendar.YEAR, recurrence_frequency);

                                                while (!currentDate.before(nextRecurrenceDate)) {
                                                    year += recurrence_frequency;
                                                    recurrenceDate.add(Calendar.YEAR, recurrence_frequency);
                                                    nextRecurrenceDate.add(Calendar.YEAR, recurrence_frequency);
                                                }

                                                calculationDate.set(Calendar.YEAR, year);
                                                calculationDate.set(Calendar.MONTH, month);
                                                calculationDate.set(Calendar.DAY_OF_MONTH, day);
                                            }
                                        } else if (recurrence_type.equalsIgnoreCase("months")) {
                                            Calendar nextRecurrenceDate = (Calendar) recurrenceDate.clone();
                                            Calendar previousRecurrenceDate = (Calendar) recurrenceDate.clone();
                                            while (!currentDate.before(nextRecurrenceDate)) {
                                                previousRecurrenceDate = (Calendar) nextRecurrenceDate.clone();
                                                nextRecurrenceDate.add(Calendar.MONTH, recurrence_frequency);

                                            }
                                            int newYear = previousRecurrenceDate.get(Calendar.YEAR);
                                            int newMonth = previousRecurrenceDate.get(Calendar.MONTH);

                                            calculationDate.set(Calendar.YEAR, newYear);
                                            calculationDate.set(Calendar.MONTH, newMonth);
                                            calculationDate.set(Calendar.DAY_OF_MONTH, day);

                                        } else if (recurrence_type.equalsIgnoreCase("weeks")) {
                                            Calendar nextRecurrenceDate = (Calendar) recurrenceDate.clone();

                                            long daysBetween = (currentDate.getTimeInMillis() - recurrenceDate.getTimeInMillis()) / (1000 * 60 * 60 * 24);

                                            int weekDifference = (int) (daysBetween / 7);

                                            int weeksPassed = weekDifference / recurrence_frequency;

                                            nextRecurrenceDate.add(Calendar.WEEK_OF_YEAR, weeksPassed * recurrence_frequency);

                                            if (currentDate.before(nextRecurrenceDate)) {
                                                nextRecurrenceDate.add(Calendar.WEEK_OF_YEAR, -recurrence_frequency);
                                            }

                                            int newYear = nextRecurrenceDate.get(Calendar.YEAR);
                                            int newMonth = nextRecurrenceDate.get(Calendar.MONTH);
                                            int newDay = nextRecurrenceDate.get(Calendar.DAY_OF_MONTH);
                                            calculationDate.set(Calendar.YEAR, newYear);
                                            calculationDate.set(Calendar.MONTH, newMonth);
                                            calculationDate.set(Calendar.DAY_OF_MONTH, newDay);


                                        } else {
                                            Calendar finalReccurenceDate = (Calendar) recurrenceDate.clone();
                                            Calendar nextRecurrenceDate = (Calendar) finalReccurenceDate.clone();

                                            nextRecurrenceDate.add(Calendar.DAY_OF_YEAR, recurrence_frequency);

                                            while (!currentDate.before(nextRecurrenceDate)) {
                                                finalReccurenceDate.add(Calendar.DAY_OF_YEAR, recurrence_frequency);
                                                nextRecurrenceDate.add(Calendar.DAY_OF_YEAR, recurrence_frequency);
                                            }

                                            int newYear = finalReccurenceDate.get(Calendar.YEAR);
                                            int newMonth = finalReccurenceDate.get(Calendar.MONTH);
                                            int newDay = finalReccurenceDate.get(Calendar.DAY_OF_MONTH);
                                            calculationDate.set(Calendar.YEAR, newYear);
                                            calculationDate.set(Calendar.MONTH, newMonth);
                                            calculationDate.set(Calendar.DAY_OF_MONTH, newDay);
                                        }

                                        if (lastSubmittedDate.after(calculationDate) || isSameDay(lastSubmittedDate, calculationDate)) {
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
                                int surveyDurationId = db.surveyDurationTypeDao().getSurveyDurationId();
                                String surveyDuration_string = item.surveyDuration;

                                if (!surveyDuration_string.equals("null") && Integer.parseInt(surveyDuration_string) == surveyDurationId) {
                                    surveyHolder.enabledTextView.setText("true");
                                    surveyHolder.moduleStatus.setText(res.getString(R.string.ModuleStatus));
                                    surveyHolder.linearLayout.setBackgroundResource(R.drawable.list_item);
                                    surveyHolder.moduleName.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                    surveyHolder.moduleStatus.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                } else {
                                    surveyHolder.moduleStatus.setText(submittedModuleDetails.getString("created_at"));
                                    submittedModuleDetails.put("marked", true);
                                    surveyHolder.moduleIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));

                                    surveyHolder.linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                    surveyHolder.moduleName.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                    surveyHolder.moduleStatus.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                    surveyHolder.enabledTextView.setText("false");
                                    submitted = true;
                                    break;
                                }
                            }

                            if (disabled_months.contains(String.valueOf(currentDate.get(Calendar.MONTH)))) {
                                surveyHolder.linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                surveyHolder.moduleName.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                surveyHolder.moduleStatus.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                surveyHolder.enabledTextView.setText("false");
                            }

                            Date currentDateNew = new Date();
                            if (startDate!=null){
                                if(currentDateNew.before(startDate)){
                                    surveyHolder.linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                    surveyHolder.moduleName.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                    surveyHolder.moduleStatus.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                    surveyHolder.enabledTextView.setText("false");
                                }
                            }

                        }
                    }
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("Stakeholder_Modules: Error setting recurrence values", e));
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (!submitted) {
                surveyHolder.enabledTextView.setText("true");
                surveyHolder.linearLayout.setBackgroundResource(R.drawable.list_item);
                long farmerGroupId = dl.getFarmerGroupById(stakeHolder.farmerId);
                long sectionId = dl.getSectionByFarmerId(stakeHolder.farmerId);
                boolean value = dl.farmerExistByfarmerGroupSection(farmerGroupId, sectionId, item.id);
                if (value) {
                    boolean viewExistingdata = dl.isSurveyAnsDownloadedByModuleId((int) item.id, stakeHolder.farmerId, null);
                    if (viewExistingdata) {
                        surveyHolder.moduleName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_folder, 0);
                    } else {
                        surveyHolder.moduleName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                    }
                } else {
                    surveyHolder.moduleName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                }
                surveyHolder.moduleName.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                surveyHolder.moduleStatus.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));

                if (item.status_started != null && Integer.parseInt(item.status_started) == 1) {
                    if (item.status_finished != null) {
                        if (Integer.parseInt(item.status_finished) == 0) {
                            surveyHolder.moduleStatus.setText(res.getString(R.string.module_in_progress) + getSubmoduleTranslation(item));
                            surveyHolder.moduleIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_saved));
                        } else if (Integer.parseInt(item.status_finished) == 1) {
                            surveyHolder.moduleStatus.setText(res.getString(R.string.module_finished));
                            surveyHolder.moduleIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                        }
                    }
                } else {
                    surveyHolder.moduleIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                    surveyHolder.moduleStatus.setText(res.getString(R.string.ModuleStatus));
                    if (item.disabled_months.contains(String.valueOf(currentDate.get(Calendar.MONTH)))) {
                        surveyHolder.linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        surveyHolder.moduleName.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        surveyHolder.moduleStatus.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        surveyHolder.enabledTextView.setText("false");
                    }
                }

            }
            surveyHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (Objects.equals(surveyHolder.enabledTextView.getText(), "true")) {
                        listener.onItemClick(v, item);
                    }
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

    public String getSubmoduleTranslation(ModulesDTO modulesDTO){
        String language = Locale.getDefault().getLanguage();
        return switch (language) {
            case "es" -> modulesDTO.submodule_name_es;
            case "fr" -> modulesDTO.submodule_name_fr;
            case "id", "in" -> modulesDTO.submodule_name_id;
            case "lo" -> modulesDTO.submodule_name_lo;
            case "pt" -> modulesDTO.submodule_name_pt;
            case "th" -> modulesDTO.submodule_name_th;
            case "tr" -> modulesDTO.submodule_name_tr;
            case "vi" -> modulesDTO.submodule_name_vi;
            default -> modulesDTO.submodule_name;
        };
    }

    public String getModuleTranslation(ModulesDTO modulesDTO){
        String language = Locale.getDefault().getLanguage();
        return switch (language) {
            case "es" -> modulesDTO.module_name_es;
            case "fr" -> modulesDTO.module_name_fr;
            case "id", "in" -> modulesDTO.module_name_id;
            case "lo" -> modulesDTO.module_name_lo;
            case "pt" -> modulesDTO.module_name_pt;
            case "th" -> modulesDTO.module_name_th;
            case "tr" -> modulesDTO.module_name_tr;
            case "vi" -> modulesDTO.module_name_vi;
            default -> modulesDTO.module_name;
        };
    }

}
