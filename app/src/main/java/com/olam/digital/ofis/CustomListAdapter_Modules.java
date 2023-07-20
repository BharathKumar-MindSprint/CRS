package com.olam.digital.ofis;

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

        int currentId = cursor.getInt(cursor.getColumnIndex("_id"));
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.rowlin);
        ImageView imageView = (ImageView) view.findViewById(R.id.moduleListModuleIcon);
        TextView moduleTitle = (TextView) view.findViewById(R.id.moduleListModuleName);
        TextView textView = (TextView) view.findViewById(R.id.moduleListModuleStatus);
        TextView enabledItem = (TextView) view.findViewById(R.id.moduleListEnabled);
        boolean submitted = false;
        String isConsentAvailable = currentFarmer.getString(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ISCONSENTAVAILABLE));
        final boolean isConsentTrue = isConsentAvailable != null && isConsentAvailable.length() > 0 && isConsentAvailable.equalsIgnoreCase("true");
        if (currentId == 0) {

            if(isConsentTrue){
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
            } else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("farmpolygon")) {
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
                //}
                //Ticket-OFISDM-39 newly added
            }else if(cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("diagnostic")){
                int moduleId = cursor.getInt(cursor.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID));
                OFISRoomDB db = OFISRoomDB.getDbInstance(context);
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                }
                SimpleDateFormat outputDateFormatFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                // Set the input date's timezone to UTC
                assert inputDateFormat != null;
                inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date startDateMain = null;
                String outputDateString = null;
                try {
                    startDateMain = inputDateFormat.parse(startDateString);
                    outputDateString = outputDateFormatFormatter.format(startDateMain);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                boolean moduleWithActionPlan = false;
                boolean moduleWithoutActionPlan = false;


                String endDate = RecurrenceUtils.calculateEndDate(outputDateString, recurrence_frequency, recurrence_type);
//                Toast.makeText(context, endDate, Toast.LENGTH_SHORT).show();

                if(db.complianceIndicatorDao().hasNoActionPlan(String.valueOf(moduleId))){
                    moduleWithoutActionPlan = true;
                }

                if (db.complianceIndicatorDao().hasActionPlan(String.valueOf(moduleId))){
                    moduleWithActionPlan = true;
                }

                /*if (recurrence_frequency > 0) {
                    // for recurring
                    try {
                        // get last submitted date
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                        Calendar lastSubmittedDate = Calendar.getInstance();
                        Calendar calculationDate = (Calendar) currentDate.clone();
                        isCompleted = true;
                        try {
                            String startDate = db.dmMainModuleDao().getStartData(moduleId);
                            lastSubmittedDate.setTime(sdf.parse(startDate));
                        } catch (ParseException e) {
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
                                isCompleted = false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                List<FollowUpItemMainTable> followupList = db.followUpItemMainTableDao().getFollowupByFarmerId(farmerId, moduleId);
                if(followupList.size() == 0){
                    List<DmSurveyAnswerTable> dmSurveyAnswerTableList = db.dmSurveyAnswerTableDao().getDataByModuleId(String.valueOf(moduleId), String.valueOf(farmerId), true);
                    for (int i = 0; i < dmSurveyAnswerTableList.size(); i++) {
                        int actionPlanId = Integer.parseInt(dmSurveyAnswerTableList.get(i).actionplanId);
                        String actionPlanText = dmSurveyAnswerTableList.get(i).actionPlanText;
                        if(actionPlanId != 0 || actionPlanText.length() != 0){
                            isCompleted = true;
                        }
                    }
                }else {
                    List<Integer> followupAttempts = db.followUpItemMainTableDao().getFollowupByAttempts(farmerId, moduleId);
                    List<FollowUpItemMainTable> followupUnSyncedItems = db.followUpItemMainTableDao().getFollowupUnSyncedItems(String.valueOf(farmerId), String.valueOf(moduleId), false);
                    if(followupUnSyncedItems.size() == 0) {
                        List<FollowUpItemMainTable> followupUnResolvedItems = db.followUpItemMainTableDao().getFollowupUnResolvedItems(String.valueOf(farmerId), String.valueOf(moduleId), false, Constants.NOTRESOLVED, followupAttempts.size());
                        if(followupUnResolvedItems.size() > 0) {
                            isCompleted = false;
                        }else{
                            isCompleted = true;
                        }
                    }
                }*/

                if(currentDate.after(startDateMain) && moduleWithActionPlan) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_householdgps));
                    enabledItem.setText("true");
                    linearLayout.setBackgroundResource(R.drawable.list_item);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                    textView.setText(res.getString(R.string.ModuleStatus));
                } else if (currentDate.after(startDateMain) && moduleWithoutActionPlan) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_householdgps));
                    enabledItem.setText("true");
                    linearLayout.setBackgroundResource(R.drawable.list_item);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                    textView.setText(res.getString(R.string.ModuleStatus));
                } else{
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_householdgps));
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
                    textView.setText(res.getString(R.string.ModuleStatus));
                }


            } else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(ChildLabourUtils.REG_CHILD)) {

                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_child));
                enabledItem.setText("true");

                linearLayout.setBackgroundResource(R.drawable.list_item);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                textView.setText(res.getString(R.string.update_childlabour_hint));

            } else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(ChildLabourUtils.CHILDLABOUR_SURVEY)) {

                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
                enabledItem.setText("true");

                linearLayout.setBackgroundResource(R.drawable.list_item);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                textView.setText(res.getString(R.string.ModuleStatus));
            } else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("PlotSurvey")) {

                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_childsurvey));
                enabledItem.setText("true");

                linearLayout.setBackgroundResource(R.drawable.list_item);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                textView.setText(res.getString(R.string.ModuleStatus));
            } else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("multifarm")) {
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

            } else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("Signature")) {
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
            } else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("National_ID")) {
                boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("farmer", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                boolean nidupdateWaiting = dl.networkEntryTypeExistsByFarmerId("National_ID", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                long farmerId = currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
                boolean isSynced = dl.getSyncedStatus(farmerId);
                boolean availstatus = dl.isValueavailable(farmerId);

                if (isSynced) {
                    textView.setText(res.getString(R.string.view_tab_hint));
                    moduleTitle.setText(res.getString(R.string.view_nationalid));
                } else {
                    if (!availstatus) {
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
            } else if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals(context.getResources().getString(R.string.household_farmer))) {
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
            } else {
                // Multifarm Location
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

                } else {

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
        }else{
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
        } else if (currentId < 0) {
           
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
            }else{
                linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                enabledItem.setText("false");
                enabledItem.setTag("hasfarm");
            }
        } else {
             if (isAtLeastOnePlotSurveyCaptured && 
                    isConsentAvailable != null && isConsentAvailable.length() > 0 && isConsentAvailable.equalsIgnoreCase("true")) {
                String disabled_months_string = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_DISABLED_MONTHS));
                List<String> disabled_months = Arrays.asList(disabled_months_string.split("\\s*,\\s*"));
                if (hasSubmittedModules) {
                    try {
                        int recurrence_frequency = cursor.getInt(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_FREQUENCY));
                        String recurrence_type = cursor.getString(cursor.getColumnIndex(OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_TYPE));
                        Calendar calculationDate = (Calendar) currentDate.clone();
                        boolean disable_entry = false;
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
                if (MultiFarmUtils.isMultiFarmEnumerator) {

                    try {

                        String farmSurvey = cursor.getString(cursor.getColumnIndex("isFarmSurvey"));
                        boolean isFarmSurvey;
                        String farmerRoleStr = cursor.getString(cursor.getColumnIndex("fkFarmerRoleId"));
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
                                        SubmittedInfo submittedInfo = dl.isAvailableinSubmittedModules2(cursor, currentId, farmerId, multiFarmId);

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

                                                    boolean othersAttneded = dl.isSurveyAttendedByOther(multiFarmId, farmerId, currentId, 0);
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
                                                    boolean othersAttneded = dl.isSurveyAttendedByOther(multiFarmId, farmerId, currentId, 0);
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


}
