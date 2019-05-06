package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
    private JSONArray submittedModulesArray;
    private String moduleColumnSuffix = "";
    private final Calendar currentDate;
    private final DataLayer dl;
    private final Context mContext;

    CustomListAdapter_Modules(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, Cursor farmer, JSONObject submittedModules_) throws JSONException {
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
        Log.d("CustomListAdapter", "moduleTitle"+ moduleTitle.getText().toString() + ", SecText" + textView.getText().toString());


        if (currentId == 0) {
            if (cursor.getString(cursor.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)).equals("Phone")) {
                boolean updateWaiting = dl.networkEntryTypeExistsByFarmerId("phonenumbers", currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                if (updateWaiting || currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0) {


                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_completed));
                    enabledItem.setText("false");


                    linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                    try {
                        if (currentFarmer.getInt(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED)) == 0){
                            if(dl.isIdAvailablePhoneDB(currentFarmer.getLong(currentFarmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)))){
                                textView.setText(res.getString(R.string.phone_number_recorded));
                            }else{
                                textView.setText(res.getString(R.string.module_finished));
                            }
                        }else{
                            textView.setText(res.getString(R.string.phone_number_recorded));
                        }
                    }catch (Exception e){
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
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_record_polygon));
                    enabledItem.setText("true");

                    linearLayout.setBackgroundResource(R.drawable.list_item);
                    moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                    textView.setText(res.getString(R.string.update_phone_numbers_hint));
                //}
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
        } else if (currentId < 0) {
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
        } else {
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
                                    } catch (ParseException e){
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
                                } catch (Exception e){
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
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (!submitted) {
                enabledItem.setText("true");
                linearLayout.setBackgroundResource(R.drawable.list_item);
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
        }
    }
}
