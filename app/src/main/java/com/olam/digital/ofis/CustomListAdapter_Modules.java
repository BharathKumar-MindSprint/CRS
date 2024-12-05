package com.olam.digital.ofis;

import static org.apache.commons.lang3.time.DateUtils.isSameDay;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;

import com.olam.digital.ofis.multifarm.MultiFarmLongPressListener;
import com.olam.digital.ofis.multifarm.MultiFarmUtils;
import com.olam.digital.ofis.multifarm.SubmittedInfo;
import com.olam.digital.ofis.mvvmmodule.Constants;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.FollowUp.FollowUpRecyclerAdapter;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils;
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.SurveyDurationTypeDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by ben on 04/12/14.
 * Creates and binds the layouts and functions for an item in the farmer modules list
 */
class CustomListAdapter_Modules extends SimpleCursorAdapter {
    private final Cursor currentFarmer;
    private final int layout;
    private final LayoutInflater inflater;
    private final Resources res;
    private final boolean hasSubmittedModules;
    private final Calendar currentDate;
    private final DataLayer dl;
    private final Context mContext;
    private JSONArray submittedModulesArray;
    private String moduleColumnSuffix = "";
    int roleId;
    String viewModule = "0";
    long farmerId;

    boolean ispolygonCaptured;
    boolean isLocationCaptured;
    boolean isAvailableInStakeHolderList;
    HashMap<Integer,SubmittedInfo> farmSubmittedHashMapList;
    boolean isAtLeastOnePlotSurveyCaptured;
    String multiFarmId;

    OFISRoomDB db;


    CustomListAdapter_Modules(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, Cursor farmer, JSONObject submittedModules_, int roleId, long farmerId, String multiFarmId, boolean isAtLeastOnePlotSurveyCaptured) throws JSONException {
        super(c, R.layout.listview_module_list, cursor, adapterCols, adapterRowViews, 0);
        this.mContext = c;
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
        this.multiFarmId=multiFarmId;
        this.isAtLeastOnePlotSurveyCaptured = isAtLeastOnePlotSurveyCaptured;
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

//        business told to remove the condition based on origin capabilities
//        boolean isCompliantFarmer = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COMPLAINCE_FLAG)).equalsIgnoreCase("true");
//        String polygonCapture = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_POLYGON_CAPTURE_FOR_NCF));
//        String conductSurvey = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CONDUCT_SURVEY_FOR_NCF));
//
//        boolean disablePolygonCaptureForNCF = !isCompliantFarmer && (polygonCapture.equalsIgnoreCase("false") || polygonCapture.isEmpty());
//        boolean disableSurveyForNCF = !isCompliantFarmer && (conductSurvey.equalsIgnoreCase("false") || conductSurvey.isEmpty());

        boolean disablePolygonCaptureForNCF = false;
        boolean disableSurveyForNCF = false;

        if (currentId == 0) {

            if(isConsentTrue ){
                if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("Phone")) {
                    boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("phonenumbers", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                    if (updateWaiting || currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0) {


                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                        enabledItem.setText("false");


                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
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
                    boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("farmpolygon", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));

                    //int farmId = currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID));
                    int farmId = (int) ViewFarmerActivity.farmerId;

                    //boolean isPlotsRecordingFinished = SharedPreferenceUtils.getInstance(mContext).isPlotsRecordingFinished(farmId);
                /*if (isPlotsRecordingFinished) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                    enabledItem.setText("false");
                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setText(res.getString(R.string.plot_recording_module_finished));
                } else {*/

                    if (!disablePolygonCaptureForNCF) {
                        if (MultiFarmUtils.isMultiFarmEnumerator) {
                            boolean ispolygonCaptured = dl.getPolygonInfo1(farmerId);
                            int fetchPlotCountByFarmerID = dl.fetchSyncedPlotCountByFarmerID(String.valueOf(farmerId));
                            if (ispolygonCaptured) {
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_found));
                                enabledItem.setText("false");
                                enabledItem.setTag("poly");

                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setText(res.getString(R.string.record_polygon_location_complete));
                            } else {
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_searching));
                                enabledItem.setText("true");

                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                textView.setText(res.getString(R.string.update_phone_numbers_hint));
                            }
                        } else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_record_polygon));
                            enabledItem.setText("true");

                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                            textView.setText(res.getString(R.string.update_phone_numbers_hint));
                        }

                    }
                    else {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_record_polygon));
                        enabledItem.setText("false");

                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setText(res.getString(R.string.update_phone_numbers_hint));
                    }

                    //}
                }// polygon history
            /*else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("polygon_history")) {
                if (false) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_record_polygon));
                    enabledItem.setText("false");

                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setText(res.getString(R.string.no_polygon_history));

                } else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_record_polygon));
                    enabledItem.setText("true");

                    linearLayout.setBackgroundResource(R.drawable.list_item);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                    textView.setText(res.getString(R.string.view_polygon_history));
                }
            }*/
                //Ticket-OFISDM-39 newly added
                else if(cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("diagnostic")){
                    int moduleId = cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID));
//                    OFISRoomDB db = OFISRoomDB.getDbInstance(context);
                    DmRecurrence dmRecurrence = db.dmRecurrenceDao().getRecurrenceByModuleId(String.valueOf(moduleId));
                    String recurrence_type = dmRecurrence.recurrenceType;
                    int recurrence_frequency = dmRecurrence.recurrenceFrequency;
                    boolean disable_entry = false;
                    boolean isCompleted = false;

                    Calendar calendar = Calendar.getInstance();
                    Date currentDate = calendar.getTime();
                    String startDateString = db.dmMainModuleDao().getStartData(moduleId);
                /*SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date startDateMain = null;
                try {
                    startDateMain = dateFormat.parse(startDateString);
                } catch (android.net.ParseException | java.text.ParseException e) {
                    e.printStackTrace();
                }*/
                    SimpleDateFormat inputDateFormat = null;
                    SimpleDateFormat inputDateFormatEndDate = null;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                        inputDateFormatEndDate = new SimpleDateFormat("yyyy-MM-dd");
                    }
                    SimpleDateFormat outputDateFormatFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                    // Set the input date's timezone to UTC
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


                    String endDate = "";
                    int recurrenceCount = 0;
                    Date newStartDate= null;
                    Date newEndDate = null;
                    Date lastReccurenceEndDate = null;

                    if (outputDateString!=null){
                        endDate = RecurrenceUtils.calculateEndDate(outputDateString, recurrence_frequency, recurrence_type);
                        try {
                            recurrenceCount = calculateRecurrenceCount(outputDateString, recurrence_type, recurrence_frequency);
                            // Get the new start and end dates for the current recurrence
                            newStartDate = getNewStartDate(outputDateString, recurrence_type, recurrence_frequency, recurrenceCount);
                            newEndDate = getNewEndDate(newStartDate, recurrence_type, recurrence_frequency);
                            lastReccurenceEndDate = findLastAttendedDate(startDateMain,newEndDate,recurrence_type,recurrence_frequency,recurrenceCount+1);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Log.d("Module Records" , "Module Id: "+moduleId + " End Date: " + endDate);
                    Log.d("RecurrenceCount" , "Module Id: "+moduleId + "  ----Recurrence Count: " + recurrenceCount
                            + "  ----newStartDate " + newStartDate + " ----newEndDate " + newEndDate +
                            " ----recurrenceType " + recurrence_type + " ----recurrenceFreq " +recurrence_frequency);

                    db.dmMainModuleDao().updateRecurrenceCountStartEndDate(moduleId , String.valueOf(newStartDate) , String.valueOf(newEndDate) , recurrenceCount+1);


                    Date endDateValue = null;
                    try {
//                    assert endDate != null;
                        if (!Objects.equals(endDate, "")){
                            endDateValue = inputDateFormatEndDate.parse(endDate);
                        }


                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }


                    // delete from answer table if the current date crossed recurrence value

                    if (lastReccurenceEndDate!=null){


                        if (currentDate.after(lastReccurenceEndDate) && !areDatesEqual(currentDate,startDateMain) ){


                            // clear the answer table based on module
                            db.dmSurveyAnswerTableDao().deleteAllDataForRecurrence(String.valueOf(moduleId) , String.valueOf(farmerId), 0);
                            db.followUpItemMainTableDao().deleteAllDataForRecurrenceFollowUp(String.valueOf(moduleId) , String.valueOf(farmerId));
                        }

                    }


                    if (startDateMain!=null){
                        if (!disableSurveyForNCF) {
                            if(currentDate.after(startDateMain) && moduleWithActionPlan) {
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                enabledItem.setText("true");
                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                textView.setText(res.getString(R.string.dm_hint_txt));
                            }
                            else if (currentDate.after(startDateMain) && moduleWithoutActionPlan) {
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                enabledItem.setText("true");
                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                textView.setText(res.getString(R.string.dm_hint_txt));
                            }
                            else{
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                enabledItem.setText("false");
                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setText(res.getString(R.string.dm_diognostic_module_subtitle));
                            }
                            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByModuleId(String.valueOf(moduleId), String.valueOf(farmerId), true);
                            if(dmSurveyAnswerTableList.size() > 0){
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

                    if (startDateMain!=null){
                        if (!disableSurveyForNCF) {
                            if(currentDate.after(startDateMain) && moduleWithActionPlan) {
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                enabledItem.setText("true");
                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                textView.setText(res.getString(R.string.dm_hint_txt));
                            } else if (currentDate.after(startDateMain) && moduleWithoutActionPlan) {
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                enabledItem.setText("true");
                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                textView.setText(res.getString(R.string.dm_hint_txt));
                            } else{
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                                enabledItem.setText("false");
                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                textView.setText(res.getString(R.string.dm_diognostic_module_subtitle));
                            }
                            List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByModuleId(String.valueOf(moduleId), String.valueOf(farmerId), true);
                            if(dmSurveyAnswerTableList.size() > 0){
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
                else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("multifarm")) {
                    boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("Multifarm Update", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                    if (updateWaiting || currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0) {


                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                        enabledItem.setText("false");


                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        try {
                            if (currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0) {
                                if (dl.isIdAvailableMultiFarmDB(currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)))) {
                                    textView.setText("Multifarm Details Recorded");
                                } else {
                                    textView.setText(res.getString(R.string.module_finished));
                                }
                            } else {
                                textView.setText("Multifarm Details Recorded");
                            }
                        } catch (Exception e) {
                            textView.setText("Multifarm Details Recorded");
                        }
                    } else {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_update_farm_information));
                        enabledItem.setText("true");

                        linearLayout.setBackgroundResource(R.drawable.list_item);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                        textView.setText("Multifarm update");
                    }

                }
                else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("Signature")) {
                    boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("signature", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                    boolean farmerupdateWaiting = dl.networkEntryTypeExistsByFarmerId("farmer", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
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
                    boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("farmer", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                    boolean nidupdateWaiting = dl.networkEntryTypeExistsByFarmerId("National_ID", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                    long farmerId = currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
                    boolean isSynced = dl.getSyncedStatus(farmerId);
                    boolean availstatus = dl.isValueavailable(farmerId);
                    boolean isUpdateNationalId = dl.isUpdateNationalIdAvailable(farmerId);
                    if (!nidupdateWaiting) {
                        nidupdateWaiting = dl.networkEntryTypeExistsByFarmerId("Update_ID", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                    }

                    if (isSynced && !isUpdateNationalId) {
                        textView.setText(res.getString(R.string.view_tab_hint));
                        moduleTitle.setText(res.getString(R.string.view_nationalid));
                    } else {
                        if (!availstatus && !isUpdateNationalId) {
                            textView.setText(res.getString(R.string.add_tab_hint));
                            moduleTitle.setText(res.getString(R.string.add_nationalid));
                        } else {
                            textView.setText(res.getString(R.string.update_tab_hint));
                            moduleTitle.setText(res.getString(R.string.update_nationalid));
                        }
                    }

                    if (updateWaiting || nidupdateWaiting) {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                        enabledItem.setText("false");
                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        textView.setText(res.getString(R.string.update_tab_hint));
                        moduleTitle.setText(res.getString(R.string.update_nationalid));
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    } else {
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
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

                    //Based on BA requirement record farm location is disabled (PC-265)
/*
                    if (MultiFarmUtils.isMultiFarmEnumerator) {
                        boolean isLocationCaptured = dl.getLocationInfo1(farmerId);
                        if (isLocationCaptured) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_found));
                            enabledItem.setText("false");
                            enabledItem.setTag("loc");


                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setText(res.getString(R.string.record_farm_location_complete));
                        } else {

                            // Multifarm . But Not in the stakeholder list
                            String farmId = dl.getMultifarmfarmId(farmerId);
                            // boolean isAvailableInStakeHolderList= dl.isfarmerIdAvailableMultiFarmDB(farmerId);
                            if (farmId != null) {
                                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_searching));
                                enabledItem.setText("true");

                                linearLayout.setBackgroundResource(R.drawable.list_item);
                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                textView.setText(res.getString(R.string.record_farm_location_hint));
                            } else {

                                // Not available in multifarm Db . Consider as normal farmer
                                String currentLat = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LAT));
                                String currentLng = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LNG));
                                if (currentLat != null && currentLat.length() > 0 && (!currentLat.equalsIgnoreCase("null")) && currentLng != null && currentLng.length() > 0 && (!currentLng.equalsIgnoreCase("null"))) {
                                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_found));
                                    enabledItem.setText("false");

                                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                    textView.setText(res.getString(R.string.record_farm_location_complete));
                                } else {
                                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_searching));
                                    enabledItem.setText("true");

                                    linearLayout.setBackgroundResource(R.drawable.list_item);
                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                                    textView.setText(res.getString(R.string.record_farm_location_hint));
                                }

                            }

                        }

                    }
                    else {

                        String currentLat = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LAT));
                        String currentLng = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LNG));

                        if (currentLat != null && currentLat.length() > 0 && (!currentLat.equalsIgnoreCase("null")) && currentLng != null && currentLng.length() > 0 && (!currentLng.equalsIgnoreCase("null"))) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_found));
                            enabledItem.setText("false");

                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                            textView.setText(res.getString(R.string.record_farm_location_complete));
                        } else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_location_searching));
                            enabledItem.setText("true");

                            linearLayout.setBackgroundResource(R.drawable.list_item);
                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                            textView.setText(res.getString(R.string.record_farm_location_hint));
                        }

                    }
                    */
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
                if (cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)) == 1) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_submitted));
                    enabledItem.setText("false");

                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setText(res.getString(R.string.review_farm_management_plan_complete));
                } else if ((cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED)) == 1) && (cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED)) == 0)) {
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
            if (isAtLeastOnePlotSurveyCaptured &&
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
//                                                    lastSubmittedDate.setTime(sdf.parse("2022-07-23 07:55:12"));

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
                                                calculationDate.add(Calendar.WEEK_OF_YEAR, -recurrence_frequency);
                                                calculationDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                                            } else {
                                                // must be days
                                                calculationDate.add(Calendar.DAY_OF_YEAR, -recurrence_frequency);
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
                    if (!disableSurveyForNCF) {
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
                    else {
                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                        enabledItem.setText("false");
                    }
                }

                if (MultiFarmUtils.isMultiFarmEnumerator) {

                    try {

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
                                                if(disableSurveyForNCF) {
                                                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    enabledItem.setText("false");
                                                }
                                                else {
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

                                                        // Can be completed by other stakeholders
                                                        //  showAlertMsg(linearLayout, res.getString(R.string.survey_can_be_completed_by_other_stakeholders));
                                                        enabledItem.setTag("farmerS");

                                                        linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                        moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                        textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                        enabledItem.setText("false");
                                                    }
                                                }
                                            } else if (newFarmerRole != 0 && roleId == 0) {
                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                enabledItem.setText("false");
                                            }
                                        } else{
                                            // FARM SURVEY CODE
//                                ------------START ----------------
//
                                            try {
                                                //                                  SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules(cursor, currentId, String.valueOf(farmerId));
                                                //     SubmittedInfo submittedInfo =farmSubmittedHashMapList.get(currentId);
                                                SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules2(cursor, currentId, farmerId, multiFarmId, Integer.parseInt(module_ID));

                                                if(disableSurveyForNCF) {
                                                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                    enabledItem.setText("false");
                                                } else {
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

                                                                // has beed completed by other stakeholders
                                                                //    showAlertMsgIdCheck(linearLayout, res.getString(R.string.already_syned_survey_info),submittedInfo);

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
                                                                    /*enabledItem.setText("true");
                                                                    textView.setText(res.getString(R.string.ModuleStatus));
                                                                    linearLayout.setBackgroundResource(R.drawable.list_item);
                                                                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                                                                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));*/
                                                                }
                                                            }
                                                            break;
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

                                                                // Put listener call for the long press
                                                                //  showAlertMsgIdCheck(linearLayout, res.getString(R.string.already_syned_survey_info),submittedInfo);

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
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }


//                                ------------END ----------------

                                        }
                                    }
                                }else {
                                    if (!isFarmSurvey) {
                                        //// FARM-ER SURVEY CODE
                                        if (farmerRole != 0 && roleId != 0) {
                                            if(disableSurveyForNCF) {
                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                enabledItem.setText("false");
                                            } else {
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

                                        } else if (farmerRole != 0 && roleId == 0) {
                                            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                            moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                            enabledItem.setText("false");
                                        }
                                    } else{
                                        // FARM SURVEY CODE
//                                ------------START ----------------
//
                                        try {
                                            //                                  SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules(cursor, currentId, String.valueOf(farmerId));
                                            //     SubmittedInfo submittedInfo =farmSubmittedHashMapList.get(currentId);
                                            SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules2(cursor, currentId, farmerId, multiFarmId, Integer.parseInt(module_ID));

                                            if(disableSurveyForNCF) {
                                                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                                                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                                                enabledItem.setText("false");
                                            } else {
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

                                                            // has beed completed by other stakeholders
                                                            //    showAlertMsgIdCheck(linearLayout, res.getString(R.string.already_syned_survey_info),submittedInfo);

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
                                                        // Disable
                                                        // Can be completed by other stakeholders
                                                        // showAlertMsg(linearLayout, res.getString(R.string.survey_can_be_completed_by_other_stakeholders));

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

                                                            // Put listener call for the long press
                                                            //  showAlertMsgIdCheck(linearLayout, res.getString(R.string.already_syned_survey_info),submittedInfo);

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
                    }
                }
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

    private void showAlertMsgIdCheck(LinearLayout linearLayout, String msg, SubmittedInfo submittedInfo) {
        linearLayout.setOnLongClickListener(v -> {
            if (submittedInfo.getFarmerId() != farmerId) {
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //Getting the View object as defined in the customtoast.xml file
                View layout = li.inflate(R.layout.multifarm_custom_toast_layout, null);
                TextView tv = layout.findViewById(R.id.alert_msg);
                tv.setText(msg);
                //Creating the Toast object
                Toast toast = new Toast(mContext);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                toast.setView(layout);//setting the view of custom toast layout
                toast.show();
            }
            return true;
        });
    }

    public void setValues(int roleId, boolean isPolygonCaptured, boolean isLocationCaptured, boolean isAvailableInStakeHolderList, HashMap<Integer,SubmittedInfo> farmSubmittedHashMapList) {
        this.roleId=roleId;
        this.ispolygonCaptured=isPolygonCaptured;
        this.isLocationCaptured=isLocationCaptured;
        this.isAvailableInStakeHolderList=isAvailableInStakeHolderList;
        this.farmSubmittedHashMapList=farmSubmittedHashMapList;
    }

    private void showAlertMsg(LinearLayout linearLayout,String msg){
        linearLayout.setOnLongClickListener(v -> {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = li.inflate(R.layout.multifarm_custom_toast_layout, null);
            TextView tv = layout.findViewById(R.id.alert_msg);
            tv.setText(msg);
            //Creating the Toast objectn
            Toast toast = new Toast(mContext);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
            toast.setView(layout);//setting the view of custom toast layout
            toast.show();

            //  Toast.makeText(linearLayout.getContext(), mContext.getResources().getString(R.string.already_syned_survey_info), Toast.LENGTH_LONG).show();
            return true;
        });
    }

    private static int calculateRecurrenceCount(String startDateStr, String recurrenceType, int recurrenceFrequency)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(startDateStr);
        Date currentDate = new Date();

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        int recurrenceCount = 0;

        switch (recurrenceType.toLowerCase()) {
            case "days":
                long daysDiff = (currentDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000);
                recurrenceCount = (int) (daysDiff / recurrenceFrequency);
                break;
            case "weeks":
                long weeksDiff = (currentCalendar.getTimeInMillis() - startCalendar.getTimeInMillis())
                        / (7 * 24 * 60 * 60 * 1000);
                recurrenceCount = (int) (weeksDiff / recurrenceFrequency);
                break;
            case "months":
                int monthsDiff = (currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)) * 12
                        + currentCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
                recurrenceCount = monthsDiff / recurrenceFrequency;
                break;
            case "years":
                int yearsDiff = currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                recurrenceCount = yearsDiff / recurrenceFrequency;
                break;
            default:
                System.out.println("Unsupported recurrence frequency");
        }

//        recurrenceCount++;
        return recurrenceCount;
    }

    private static Date getNewStartDate(String startDate, String recurrenceType, int recurrenceFrequency,
                                        int recurrenceCount) throws ParseException {
//        recurrenceCount--;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(sdf.parse(startDate));

        switch (recurrenceType.toLowerCase()) {
            case "days":
                startCalendar.add(Calendar.DAY_OF_MONTH, recurrenceFrequency * recurrenceCount);
                break;
            case "weeks":
                startCalendar.add(Calendar.WEEK_OF_MONTH, recurrenceFrequency * recurrenceCount);
                break;
            case "months":
                startCalendar.add(Calendar.MONTH, recurrenceFrequency * recurrenceCount);
                break;
            case "years":
                startCalendar.add(Calendar.YEAR, recurrenceFrequency * recurrenceCount);
                break;
            default:
                System.out.println("Unsupported recurrence frequency");
        }

        return startCalendar.getTime();
    }

    private static Date getNewEndDate(Date newStartDate, String recurrenceType, int recurrenceFrequency) {
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(newStartDate);

        switch (recurrenceType.toLowerCase()) {
            case "days":
                endCalendar.add(Calendar.DAY_OF_MONTH, recurrenceFrequency);
                break;
            case "weeks":
                endCalendar.add(Calendar.WEEK_OF_MONTH, recurrenceFrequency);
                break;
            case "months":
                endCalendar.add(Calendar.MONTH, recurrenceFrequency);
                break;
            case "years":
                endCalendar.add(Calendar.YEAR, recurrenceFrequency);
                break;
            default:
                System.out.println("Unsupported recurrence frequency");
        }

        return endCalendar.getTime();
    }

    public static Date findLastAttendedDate(Date startDate, Date endDate, String recurrenceType,
                                            int recurrenceFrequency, int recurrenceCount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Increment based on the recurrence type and frequency
        for (int i = 0; i < recurrenceCount; i++) {
            if ("days".equals(recurrenceType)) {
                calendar.add(Calendar.DAY_OF_MONTH, recurrenceFrequency);
            } else if ("months".equals(recurrenceType)) {
                calendar.add(Calendar.MONTH, recurrenceFrequency);
            } else if ("weeks".equals(recurrenceType)) {
                calendar.add(Calendar.WEEK_OF_MONTH, recurrenceFrequency);
            } else if ("years".equals(recurrenceType)) {
                calendar.add(Calendar.YEAR, recurrenceFrequency);
            }

            // Check if the current date is after the old recurrence end date
            if (calendar.getTime().after(endDate)) {
                // Subtract one recurrence interval to get the last attended date
                if ("days".equals(recurrenceType)) {
                    calendar.add(Calendar.DAY_OF_MONTH, -recurrenceFrequency);
                } else if ("months".equals(recurrenceType)) {
                    calendar.add(Calendar.MONTH, -recurrenceFrequency);
                } else if ("weeks".equals(recurrenceType)) {
                    calendar.add(Calendar.WEEK_OF_MONTH, -recurrenceFrequency);
                } else if ("years".equals(recurrenceType)) {
                    calendar.add(Calendar.YEAR, -recurrenceFrequency);
                }

                return calendar.getTime();
            }
        }

        // Return the start date if no recurrence has been attended
        return startDate;
    }

    private static boolean areDatesEqual(Date date1, Date date2) {
        // Check if the dates are equal
        return date1 != null && date2 != null && !date1.equals(date2);
    }

}
