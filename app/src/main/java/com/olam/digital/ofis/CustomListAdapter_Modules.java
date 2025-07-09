package com.olam.digital.ofis;

import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.areDatesEqual;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.calculateEndDate;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.calculateRecurrenceCount;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.findLastAttendedDate;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.getNewEndDate;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.getNewStartDate;
import static org.apache.commons.lang3.time.DateUtils.isSameDay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.multifarm.SubmittedInfo;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by ben on 04/12/14.
 * Creates and binds the layouts and functions for an item in the farmer modules list
 */
class
CustomListAdapter_Modules extends SimpleCursorAdapter {
    private final Cursor currentFarmer;
    private final int layout;
    private final LayoutInflater inflater;
    private final Resources res;
    private final boolean hasSubmittedModules;
    private final Calendar currentDate;
    private final DataLayer dl;
    private JSONArray submittedModulesArray;
    private String moduleColumnSuffix = "";
    int roleId;
    long farmerId;

    boolean ispolygonCaptured;
    boolean isLocationCaptured;
    boolean isAvailableInStakeHolderList;
    HashMap<Integer,SubmittedInfo> farmSubmittedHashMapList;
    boolean isAtLeastOnePlotSurveyCaptured;
    boolean isMandateSurveyCompleted;
    Set<Integer> mandateSurveyList = new HashSet<>();

    OFISRoomDB db;


    CustomListAdapter_Modules(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, Cursor farmer, JSONObject submittedModules_, int roleId, long farmerId, boolean isAtLeastOnePlotSurveyCaptured, boolean isMandateSurveyCompleted, Set<Integer> mandateSurveyList) throws JSONException {
        super(c, R.layout.listview_module_list, cursor, adapterCols, adapterRowViews, 0);
        this.layout = R.layout.listview_module_list;
        this.inflater = LayoutInflater.from(c);
        this.res = res_;
        this.currentFarmer = farmer;
        this.moduleColumnSuffix = Utilities.getLocaleSuffix(res);
        this.currentDate = Calendar.getInstance();
        hasSubmittedModules = (submittedModules_.has("modules"));
        if (hasSubmittedModules) {
            submittedModulesArray = submittedModules_.getJSONArray("modules");
        }
        this.dl = new DataLayer(res, c);
        this.roleId=roleId;
        this.farmerId=farmerId;
        this.isAtLeastOnePlotSurveyCaptured = isAtLeastOnePlotSurveyCaptured;
        this.isMandateSurveyCompleted = isMandateSurveyCompleted;
        this.mandateSurveyList = mandateSurveyList;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
        super.bindView(view, context, cursor);

        db = OFISRoomDB.getDbInstance(context);
        int currentId = cursor.getInt(cursor.getColumnIndex("_id"));
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.rowlin);
        ImageView imageView = (ImageView) view.findViewById(R.id.moduleListModuleIcon);
        TextView moduleTitle = (TextView) view.findViewById(R.id.moduleListModuleName);
        TextView textView = (TextView) view.findViewById(R.id.moduleListModuleStatus);
        TextView enabledItem = (TextView) view.findViewById(R.id.moduleListEnabled);
        boolean submitted = false;
        String isConsentAvailable = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ISCONSENTAVAILABLE));
        final boolean isConsentTrue = isConsentAvailable != null && isConsentAvailable.length() > 0 && isConsentAvailable.equalsIgnoreCase("true");

        boolean disableSurveyForNCF = false;

        boolean isLocalMandateCheck = isMandateSurveyCompleted || mandateSurveyList.contains(currentId);
        boolean localMandateSurveyPlotUpdate = mandateSurveyList.contains(cursor.getInt(cursor.getColumnIndex("_id"))) || isAtLeastOnePlotSurveyCaptured;

        if (currentId == 0) {

            if(isConsentTrue ){
                if(isMandateSurveyCompleted) {
                    if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("Phone")) {
                        boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("phonenumbers", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)), OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID);
                        if (updateWaiting || currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0) {


                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                            if (currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0){
                                enabledItem.setText("false");
                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            } else {
                                enabledItem.setText("true");
                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                            }
                            try {
                                if (currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0) {
                                    if (dl.isIdAvailablePhoneDB(currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)))) {
                                        textView.setText(res.getString(R.string.phone_number_recorded));
                                    } else {
                                        textView.setText(res.getString(R.string.module_finished));
                                    }
                                } else {
                                    textView.setText(res.getString(R.string.phone_number_recorded));
                                }
                            } catch (Exception e) {
                                textView.setText(res.getString(R.string.phone_number_recorded));
                            }
                        } else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_update_phonenumbers));
                            enabledItem.setText("true");

                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                            textView.setText(res.getString(R.string.update_phone_numbers_hint));
                        }
                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("farmpolygon")) {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_record_polygon));
                        enabledItem.setText("true");

                        linearLayout.setBackgroundResource(R.drawable.list_item);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        textView.setText(res.getString(R.string.update_phone_numbers_hint));

                    }
                    else if(cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("diagnostic")){
                        int moduleId = cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID));
                        DmRecurrence dmRecurrence = db.dmRecurrenceDao().getRecurrenceByModuleId(String.valueOf(moduleId));
                        String recurrence_type = dmRecurrence.recurrenceType;
                        int recurrence_frequency = dmRecurrence.recurrenceFrequency;
                        boolean disable_entry = false;
                        boolean isCompleted = false;

                        Calendar calendar = Calendar.getInstance();
                        Date currentDate = calendar.getTime();
                        String startDateString = db.dmMainModuleDao().getStartData(moduleId);
                        SimpleDateFormat inputDateFormat = null;
                        SimpleDateFormat inputDateFormatEndDate = null;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                            inputDateFormatEndDate = new SimpleDateFormat("yyyy-MM-dd");
                        }
                        SimpleDateFormat outputDateFormatFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        assert inputDateFormat != null;
                        inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date startDateMain = null;
                        String outputDateString = null;

                        try {
                            if (startDateString != null){
                                startDateMain = inputDateFormat.parse(startDateString);
                                outputDateString = outputDateFormatFormatter.format(startDateMain);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        boolean moduleWithActionPlan = false;
                        boolean moduleWithoutActionPlan = false;


                        if(db.complianceIndicatorDao().hasNoActionPlan(String.valueOf(moduleId))){
                            moduleWithoutActionPlan = true;
                        }

                        if (db.complianceIndicatorDao().hasActionPlan(String.valueOf(moduleId))){
                            moduleWithActionPlan = true;
                        }

                        int recurrenceCount = 0;
                        Date newStartDate= null;
                        Date newEndDate = null;
                        Date lastReccurenceEndDate = null;

                    if (outputDateString!=null){
                        try {
                            recurrenceCount = calculateRecurrenceCount(startDateString, recurrence_type, recurrence_frequency);
                            newStartDate = getNewStartDate(outputDateString, recurrence_type, recurrence_frequency, recurrenceCount);
                            newEndDate = getNewEndDate(newStartDate, recurrence_type, recurrence_frequency);
                            lastReccurenceEndDate = findLastAttendedDate(startDateMain,newEndDate,recurrence_type,recurrence_frequency,recurrenceCount+1);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                        db.dmMainModuleDao().updateRecurrenceCountStartEndDate(moduleId , String.valueOf(newStartDate) , String.valueOf(newEndDate) , recurrenceCount+1);
                        if (lastReccurenceEndDate!=null){
                            if (currentDate.after(lastReccurenceEndDate) && !areDatesEqual(currentDate,startDateMain) ){
                                db.dmSurveyAnswerTableDao().deleteAllDataForRecurrence(String.valueOf(moduleId) , String.valueOf(farmerId), 0);
                                db.followUpItemMainTableDao().deleteAllDataForRecurrenceFollowUp(String.valueOf(moduleId) , String.valueOf(farmerId));
                            }
                        }

                        List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByModuleId(String.valueOf(moduleId), String.valueOf(farmerId), true);
                        if (!dmSurveyAnswerTableList.isEmpty()) {
                            boolean allEmpty = true;
                            for (DmSurveyAnswerTable answerTable : dmSurveyAnswerTableList) {
                                if (!Objects.equals(answerTable.actionPlanText, "")) {
                                    allEmpty = false;
                                    break;
                                }
                            }

                            if (allEmpty) {
                                moduleWithActionPlan = false;
                                moduleWithoutActionPlan = true;
                            } else {
                                moduleWithActionPlan = true;
                                moduleWithoutActionPlan = false;
                            }
                        }

                        if (startDateMain!=null){
                            if (!disableSurveyForNCF) {
                                if ((currentDate.after(startDateMain) && moduleWithActionPlan) || (currentDate.after(startDateMain) && moduleWithoutActionPlan)) {
                                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                    enabledItem.setText("true");
                                    linearLayout.setBackgroundResource(R.drawable.list_item);
                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                    textView.setText(res.getString(R.string.dm_hint_txt));
                                } else {
                                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                    enabledItem.setText("false");
                                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                    textView.setText(res.getString(R.string.dm_diognostic_module_subtitle));
                                }

                                if(!dmSurveyAnswerTableList.isEmpty() && moduleWithActionPlan){
                                    textView.setText(res.getString(R.string.dm_diognostic_module_subtitle));
                                }else{
                                    textView.setText(res.getString(R.string.dm_hint_txt));
                                }
                            }
                            else {
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                enabledItem.setText("false");

                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setText(res.getString(R.string.dm_hint_txt));
                            }
                        }
                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(ChildLabourUtils.REG_CHILD)) {

                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_child));
                        enabledItem.setText("true");

                        linearLayout.setBackgroundResource(R.drawable.list_item);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        textView.setText(res.getString(R.string.update_childlabour_hint));

                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(ChildLabourUtils.CHILDLABOUR_SURVEY)) {

                        if (!disableSurveyForNCF) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
                            enabledItem.setText("true");

                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                            textView.setText(res.getString(R.string.ModuleStatus));
                        }
                        else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
                            enabledItem.setText("false");

                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setText(res.getString(R.string.ModuleStatus));
                        }

                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("PlotSurvey")) {
                        if(disableSurveyForNCF) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
                            enabledItem.setText("false");

                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setText(res.getString(R.string.ModuleStatus));
                        }
                        else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
                            enabledItem.setText("true");

                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                            textView.setText(res.getString(R.string.ModuleStatus));
                        }
                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("addFarmStakeholders")) {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_update_farm_information));
                        enabledItem.setText("true");
                        linearLayout.setBackgroundResource(R.drawable.list_item);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        textView.setText(res.getString(R.string.update_phone_numbers_hint));
                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("Signature")) {
                        boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("signature", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)), OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID);
                        boolean farmerupdateWaiting = dl.networkEntryTypeExistsByFarmerId("farmer", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)), OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID);
                        long farmerId = currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
                        boolean isSignCaptured = false;
                        if (farmerupdateWaiting) {
                            isSignCaptured = dl.isSignatureCaptured(farmerId);
                        }
                        if (updateWaiting || isSignCaptured) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                            enabledItem.setText("false");

                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        } else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
                            enabledItem.setText("true");

                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                            textView.setText(res.getString(R.string.update_signature_hint));
                        }
                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("National_ID")) {
                        long farmerId = currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
                        boolean availstatus = dl.isValueavailable(farmerId);
                        boolean isDuplicateNationalId = dl.checkNationalIdDuplicateByFarmerId(farmerId);

                        if (!availstatus) {
                            textView.setText(res.getString(R.string.add_tab_hint));
                            moduleTitle.setText(res.getString(R.string.add_nationalid));
                        } else {
                            textView.setText(res.getString(R.string.update_tab_hint));
                            moduleTitle.setText(res.getString(R.string.update_nationalid));
                        }

                        if (currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0){
                            enabledItem.setText("false");
                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        } else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, isDuplicateNationalId ? R.drawable.ic_status_completed : R.drawable.ic_childsurvey));
                            enabledItem.setText("true");
                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        }
                    }
                    else if(cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(context.getResources().getString(R.string.certification))) {
                        boolean availstatus = dl.isCertificationavailable(farmerId);

                        enabledItem.setText("true");

                        linearLayout.setBackgroundResource(R.drawable.list_item);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        moduleTitle.setText(res.getString(R.string.update_certification));
                        if (availstatus) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                            textView.setText(res.getString(R.string.update_tab_hint));
                        }else{
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
                            textView.setText(res.getString(R.string.add_tab_hint));
                        }
                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(context.getResources().getString(R.string.household_farmer))) {
                        String currenthhLat = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_LAT));
                        String currenthhLng = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_LNG));
                        if (currenthhLat != null && currenthhLat.length() > 0 && (!currenthhLat.equalsIgnoreCase("null")) && currenthhLng != null && currenthhLng.length() > 0 && (!currenthhLng.equalsIgnoreCase("null"))) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_householdgps));
                            enabledItem.setText("false");
                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setText(res.getString(R.string.record_farm_location_complete));
                        } else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_householdgps));
                            enabledItem.setText("true");
                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                            textView.setText(res.getString(R.string.record_farm_location_hint));
                        }
                    }
                    else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(res.getString(R.string.dm_plot_survey))) {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                        enabledItem.setText("true");

                        linearLayout.setBackgroundResource(R.drawable.list_item);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        textView.setText(res.getString(R.string.ModuleStatus));
                    }
                    else {
                        // Multifarm Location
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_found));
                        enabledItem.setText("false");

                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setText(res.getString(R.string.record_farm_location_complete));

                    }
                } else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_consentstmts));
                    enabledItem.setText("false");
                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));

                }
            }
            else {
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_consentstmts));
                enabledItem.setText("false");
                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));

            }

            if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(Utilities.CONSENTSTATEMENT)) {
                Log.d("CustomListAdapter", "isConsentavailable? :" + isConsentAvailable);
                if (isConsentTrue) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_consentstmts));
                    enabledItem.setText("false");
                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                } else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_consentstmts));
                    enabledItem.setText("true");
                    linearLayout.setBackgroundResource(R.drawable.list_item);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                }
            }
        }
        else if (currentId < 0) {

            if (isConsentTrue) {
                if(isMandateSurveyCompleted) {
                    if (cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)) == 1) {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));
                        enabledItem.setText("false");

                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setText(res.getString(R.string.review_farm_management_plan_complete));
                    }
                    else if ((cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED)) == 1) && (cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)) == 0)) {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_saved));
                        enabledItem.setText("true");

                        linearLayout.setBackgroundResource(R.drawable.list_item);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        textView.setText(res.getString(R.string.review_farm_management_plan_in_progress));
                    }
                    else {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_fmp_available));
                        enabledItem.setText("true");

                        linearLayout.setBackgroundResource(R.drawable.list_item);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        textView.setText(res.getString(R.string.review_farm_management_plan_hint));
                    }
                } else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_consentstmts));
                    enabledItem.setText("false");
                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));

                }
            }
            else{
                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                enabledItem.setText("false");
                enabledItem.setTag("hasfarm");
            }
        }
        else {
            if (localMandateSurveyPlotUpdate && isLocalMandateCheck &&
                    isConsentAvailable != null && isConsentAvailable.length() > 0 && isConsentAvailable.equalsIgnoreCase("true")) {
                String disabled_months_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_DISABLED_MONTHS));
                List<String> disabled_months = Arrays.asList(disabled_months_string.split("\\s*,\\s*"));
                String module_ID = cursor.getString(cursor.getColumnIndex("_id"));
                String start_date_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_START_DATE));

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

                boolean disable_entry = false;
                if (hasSubmittedModules) {
                    try {
                        int recurrence_frequency = cursor.getInt(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_FREQUENCY));
                        String recurrence_type = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_TYPE));
                        Calendar calculationDate = (Calendar) currentDate.clone();

                        for (int i = 0; i < submittedModulesArray.length(); i++) {
                            JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
                            JSONObject submittedModuleDetails = submittedModule.getJSONObject("module");

                            if (submittedModuleDetails.has("id") && submittedModuleDetails.getInt("id") == currentId) {

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

                                        String farmerAssignmentField = dl.getAssignmentByFarmerTypeID(farmerId , assignmentTypeColumn);

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

                                            // set the time
//                                            calculationDate.set(Calendar.HOUR_OF_DAY, 23);
//                                            calculationDate.set(Calendar.MINUTE, 59);
//                                            calculationDate.set(Calendar.SECOND, 59);
//                                            calculationDate.set(Calendar.MILLISECOND, 999);

                                            // force the recalculation
//                                            calculationDate.get(Calendar.DAY_OF_MONTH);


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
                                    String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));

                                    if (!surveyDuration_string.equals("null") && Integer.parseInt(surveyDuration_string) == surveyDurationId) {
                                        enabledItem.setText("true");
                                        textView.setText(res.getString(R.string.ModuleStatus));
                                        linearLayout.setBackgroundResource(R.drawable.list_item);
                                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                    } else {
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
                        }
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("CustomListAdapter_Modules: Error setting recurrence values", e));
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!submitted) {
                    enabledItem.setText("true");
                    linearLayout.setBackgroundResource(R.drawable.list_item);
                    long farmerGroupId = dl.getFarmerGroupById(farmerId);
                    long sectionId = dl.getSectionByFarmerId(farmerId);
                    boolean value = dl.farmerExistByfarmerGroupSection(farmerGroupId, sectionId, currentId);
                    if (value) {
                        boolean viewExistingdata = dl.isSurveyAnsDownloadedByModuleId(currentId, farmerId, null);
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
                    if ((cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED)) == 1) && (cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)) == 0)) {
                        int columnIndex = (cursor.getColumnIndex("submodule_name" + moduleColumnSuffix) == -1) ? cursor.getColumnIndex("submodule_name") : cursor.getColumnIndex("submodule_name" + moduleColumnSuffix);
                        textView.setText(res.getString(R.string.module_in_progress) + cursor.getString(columnIndex));
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_saved));
                    } else if ((cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED)) == 1) && (cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)) == 1)) {
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

                /*try {
                    String farmSurvey = cursor.getString(cursor.getColumnIndex("isFarmSurvey"));
                    boolean isFarmSurvey;
                    String farmerRoleStr = cursor.getString(cursor.getColumnIndex("fkFarmerRoleId"));
                    // add for new QWR and multiple roles are allowed
                    String farmerRoleStrList = cursor.getString(cursor.getColumnIndex("farmerRoleId"));
                    int farmerRole = 0;

                    if (farmSurvey != null) {
                        try {
                            isFarmSurvey = Boolean.parseBoolean(farmSurvey);
                            try {
                                farmerRole = Integer.parseInt(farmerRoleStr);
                            } catch (Exception e) {
                                farmerRole = 0;
                            }

                            if (!Objects.equals(farmerRoleStrList, "[]") && Objects.equals(farmerRoleStr,"null")){
                                // Remove square brackets and split the string into an array of strings
                                String[] stringArray = farmerRoleStrList.substring(1, farmerRoleStrList.length() - 1).split(",");

                                // Convert the string array to an integer array
                                int[] intArray = new int[stringArray.length];
                                for (int m = 0; m < stringArray.length; m++) {
                                    intArray[m] = Integer.parseInt(stringArray[m]);
                                }

                                for (int newFarmerRole : intArray) {
                                    if (!isFarmSurvey) {
                                        //// FARM-ER SURVEY CODE
                                        if (newFarmerRole != 0 && roleId != 0) {
                                            if (newFarmerRole == roleId) {
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
                                                break;
                                            } else {
                                                enabledItem.setTag("farmerS");
                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                enabledItem.setText("false");
                                            }

                                        }
                                        else if (newFarmerRole != 0) {
                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            enabledItem.setText("false");
                                        }
                                    }
                                    else{
                                        // FARM SURVEY CODE
//                                ------------START ----------------
                                        try {
                                            SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules2(cursor, currentId, farmerId, multiFarmId, Integer.parseInt(module_ID));
                                            if (newFarmerRole != 0 && roleId != 0) {
                                                if (newFarmerRole == roleId) {
                                                    // Enable
                                                    if (submittedInfo.isSubmitted()) {
                                                        boolean surveyDurationBreak = true;
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
                                                                surveyDurationBreak = false;

                                                            } else {
                                                                if (submittedInfo.isAvailableInOtherFarm()) {
                                                                    textView.setText(res.getString(R.string.ModuleStatus));
                                                                }
                                                                enabledItem.setText("false");
                                                                enabledItem.setTag("hasfarm");
                                                                surveyDurationBreak = false;
                                                            }
                                                        } else {
                                                            if (submittedInfo.isAvailableInOtherFarm()) {
                                                                textView.setText(res.getString(R.string.ModuleStatus));
                                                            }
                                                            enabledItem.setText("false");
                                                            enabledItem.setTag("hasfarm");
                                                        }

                                                        int surveyDurationId = db.surveyDurationTypeDao().getSurveyDurationId();
                                                        String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));

                                                        if (surveyDurationBreak){
                                                            if (!surveyDuration_string.equals("null") && Integer.parseInt(surveyDuration_string) == surveyDurationId ) {
                                                                textView.setText(res.getString(R.string.ModuleStatus));
                                                                enabledItem.setText("true");
                                                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                            }
                                                        }
                                                    }
                                                    else {
                                                        boolean othersAttneded = dl.isSurveyAttendedByOtherFarms(multiFarmId, farmerId, currentId, 0, currentDate, cursor);
                                                        if (othersAttneded) {
                                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            enabledItem.setText("false");
                                                            enabledItem.setTag("hasfarm");
                                                        } else {

                                                            int oneTimeSurveyDurationId = db.surveyDurationTypeDao().getOneTimeSurveyDurationId();
                                                            String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));
                                                            if (Objects.equals(String.valueOf(oneTimeSurveyDurationId),surveyDuration_string) && submittedInfo.isSubmitted()){
                                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                                enabledItem.setText("false");
                                                            } else if (disable_entry){
                                                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));
                                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                                enabledItem.setText("false");
                                                            } else {
                                                                enabledItem.setText("true");
                                                                textView.setText(res.getString(R.string.ModuleStatus));
                                                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                            }
                                                        }
                                                    }
                                                    break;
                                                } else {

                                                    enabledItem.setTag("canfarm");
                                                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    enabledItem.setText("false");
                                                }
                                            }
                                            else {
                                                // Farm Survey - Role is not defined for the farm stakeholders
                                                if (roleId != 0 && newFarmerRole == 0) {
                                                    if (submittedInfo.isSubmitted()) {
                                                        boolean surveyDurationBreak = true;
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
                                                                surveyDurationBreak = false;
                                                            } else {
                                                                if (submittedInfo.isAvailableInOtherFarm()) {
                                                                    textView.setText(res.getString(R.string.ModuleStatus));
                                                                }
                                                                enabledItem.setText("false");
                                                                enabledItem.setTag("hasfarm");
                                                                surveyDurationBreak = false;
                                                            }
                                                        } else {
                                                            if (submittedInfo.isAvailableInOtherFarm()) {
                                                                textView.setText(res.getString(R.string.ModuleStatus));
                                                            }
                                                            enabledItem.setText("false");
                                                            enabledItem.setTag("hasfarm");
                                                        }

                                                        int surveyDurationId = db.surveyDurationTypeDao().getSurveyDurationId();
                                                        String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));

                                                        if (surveyDurationBreak){
                                                            if (!surveyDuration_string.equals("null") && Integer.parseInt(surveyDuration_string) == surveyDurationId ) {
                                                                textView.setText(res.getString(R.string.ModuleStatus));
                                                                enabledItem.setText("true");
                                                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                            }
                                                        }
                                                    } else {
                                                        boolean othersAttneded = dl.isSurveyAttendedByOtherFarms(multiFarmId, farmerId, currentId, 0, currentDate, cursor);
                                                        if (othersAttneded) {
                                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            enabledItem.setText("false");
                                                            enabledItem.setTag("hasfarm");
                                                        } else {

                                                            int oneTimeSurveyDurationId = db.surveyDurationTypeDao().getOneTimeSurveyDurationId();
                                                            String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));

                                                            if (Objects.equals(String.valueOf(oneTimeSurveyDurationId),surveyDuration_string) && submittedInfo.isSubmitted()){
                                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                                enabledItem.setText("false");
                                                            } else if (disable_entry){
                                                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));
                                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                                enabledItem.setText("false");
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
                                            }


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
//                                ------------END ----------------
                                    }
                                }
                            }
                            else {
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
                                        }
                                        else {
                                            // Can be completed by other stakeholders
                                            enabledItem.setTag("farmerS");
                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            enabledItem.setText("false");
                                        }
                                    } else if (farmerRole != 0 && roleId == 0) {
                                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                        enabledItem.setText("false");
                                    }
                                }
                                else{
                                    // FARM SURVEY CODE
//                                ------------START ----------------
                                    try {
                                        SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules2(cursor, currentId, farmerId, multiFarmId, Integer.parseInt(module_ID));
                                        if (farmerRole != 0 && roleId != 0) {
                                            if (farmerRole == roleId) {
                                                // Enable
                                                if (submittedInfo.isSubmitted()) {
                                                    boolean surveyDurationBreak = true;
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
                                                            surveyDurationBreak= false;
                                                        } else {
                                                            if (submittedInfo.isAvailableInOtherFarm()) {
                                                                textView.setText(res.getString(R.string.ModuleStatus));
                                                            }
                                                            enabledItem.setText("false");
                                                            enabledItem.setTag("hasfarm");
                                                            surveyDurationBreak = false;
                                                        }
                                                    } else {
                                                        if (submittedInfo.isAvailableInOtherFarm()) {
                                                            textView.setText(res.getString(R.string.ModuleStatus));
                                                        }
                                                        enabledItem.setText("false");
                                                        enabledItem.setTag("hasfarm");
                                                    }

                                                    int surveyDurationId = db.surveyDurationTypeDao().getSurveyDurationId();
                                                    String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));

                                                    if (surveyDurationBreak){
                                                        if (!surveyDuration_string.equals("null") && Integer.parseInt(surveyDuration_string) == surveyDurationId ) {
                                                            textView.setText(res.getString(R.string.ModuleStatus));
                                                            enabledItem.setText("true");
                                                            linearLayout.setBackgroundResource(R.drawable.list_item);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                        }
                                                    }
                                                } else {

                                                    boolean othersAttneded = dl.isSurveyAttendedByOtherFarms(multiFarmId, farmerId, currentId, 0, currentDate, cursor);
                                                    if (othersAttneded) {
                                                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                        enabledItem.setText("false");
                                                        enabledItem.setTag("hasfarm");
                                                    } else {

                                                        int oneTimeSurveyDurationId = db.surveyDurationTypeDao().getOneTimeSurveyDurationId();
                                                        String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));

                                                        if (Objects.equals(String.valueOf(oneTimeSurveyDurationId),surveyDuration_string) && submittedInfo.isSubmitted()){
                                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            enabledItem.setText("false");
                                                        } else if (disable_entry){
                                                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));
                                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            enabledItem.setText("false");
                                                        } else {
                                                            enabledItem.setText("true");
                                                            textView.setText(res.getString(R.string.ModuleStatus));
                                                            linearLayout.setBackgroundResource(R.drawable.list_item);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                        }
                                                    }
                                                }
                                            } else {
                                                enabledItem.setTag("canfarm");
                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                enabledItem.setText("false");
                                            }
                                        }
                                        else {
                                            // Farm Survey - Role is not defined for the farm stakeholders
                                            if (roleId != 0 && farmerRole == 0) {
                                                if (submittedInfo.isSubmitted()) {
                                                    boolean surveyDurationBreak = true;
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
                                                            surveyDurationBreak = false;
                                                        } else {
                                                            if (submittedInfo.isAvailableInOtherFarm()) {
                                                                textView.setText(res.getString(R.string.ModuleStatus));
                                                            }
                                                            enabledItem.setText("false");
                                                            enabledItem.setTag("hasfarm");
                                                            surveyDurationBreak = false;
                                                        }
                                                    } else {
                                                        if (submittedInfo.isAvailableInOtherFarm()) {
                                                            textView.setText(res.getString(R.string.ModuleStatus));
                                                        }
                                                        enabledItem.setText("false");
                                                        enabledItem.setTag("hasfarm");
                                                    }
                                                    int surveyDurationId = db.surveyDurationTypeDao().getSurveyDurationId();
                                                    String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));

                                                    if (surveyDurationBreak){
                                                        if (!surveyDuration_string.equals("null") && Integer.parseInt(surveyDuration_string) == surveyDurationId ) {
                                                            textView.setText(res.getString(R.string.ModuleStatus));
                                                            enabledItem.setText("true");
                                                            linearLayout.setBackgroundResource(R.drawable.list_item);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                                        }
                                                    }
                                                } else {
                                                    boolean othersAttneded = dl.isSurveyAttendedByOtherFarms(multiFarmId, farmerId, currentId, 0, currentDate, cursor);
                                                    if (othersAttneded) {
                                                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                        enabledItem.setText("false");
                                                        enabledItem.setTag("hasfarm");
                                                    } else {
                                                        int oneTimeSurveyDurationId = db.surveyDurationTypeDao().getOneTimeSurveyDurationId();
                                                        String surveyDuration_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION));

                                                        if (Objects.equals(String.valueOf(oneTimeSurveyDurationId),surveyDuration_string) && submittedInfo.isSubmitted()){
                                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            enabledItem.setText("false");
                                                        } else if (disable_entry){
                                                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));
                                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                            enabledItem.setText("false");
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
                                        }


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


//                                ------------END ----------------

                                }
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/


                if (disabled_months.contains(String.valueOf(currentDate.get(Calendar.MONTH)))) {
                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    enabledItem.setText("false");
                }

                Date currentDateNew = new Date();
                if (startDate!=null){
                    if(currentDateNew.before(startDate)){
                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        enabledItem.setText("false");
                    }
                }

            } else {
                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                enabledItem.setText("false");
                enabledItem.setTag("hasfarm");
            }
        }
    }

    public void setValues(int roleId, boolean isPolygonCaptured, boolean isLocationCaptured, boolean isAvailableInStakeHolderList, HashMap<Integer,SubmittedInfo> farmSubmittedHashMapList) {
        this.roleId=roleId;
        this.ispolygonCaptured=isPolygonCaptured;
        this.isLocationCaptured=isLocationCaptured;
        this.isAvailableInStakeHolderList=isAvailableInStakeHolderList;
        this.farmSubmittedHashMapList=farmSubmittedHashMapList;
    }

}
