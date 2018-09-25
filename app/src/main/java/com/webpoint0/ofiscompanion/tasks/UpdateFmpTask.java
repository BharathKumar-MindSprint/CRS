package com.webpoint0.ofiscompanion.tasks;

import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
import com.webpoint0.ofiscompanion.OFISDBContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
class UpdateFmpTask extends DatabaseTask {

    public UpdateFmpTask(Context context) {
        super(context);
    }

    @Override
    protected String doInBackground(JSONObject... params) {

        try {

                JSONObject fmpResponse = params[0];

                if (fmpResponse.getInt("resultcount") > 0) {

                    JSONObject response = fmpResponse.getJSONObject("outputdata");

                    try {

                        JSONArray fmpArray = response.getJSONArray(OFISDBContract.FarmManagementPlanEntry.TABLE_NAME);
                        fillTable(fmpArray, OFISDBContract.FarmManagementPlanEntry.TABLE_NAME, OFISDBContract.FarmManagementPlanEntry.COLUMN_LIST);

                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateFmpTask: Error filling fmp table", e));
                        e.printStackTrace();
                    }

                    try {
                        JSONArray fmpSectionArray = response.getJSONArray(OFISDBContract.FarmManagementPlanSectionsEntry.TABLE_NAME);
                        fillTable(fmpSectionArray, OFISDBContract.FarmManagementPlanSectionsEntry.TABLE_NAME, OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_LIST);
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateFmpTask: Error filling fmp section table", e));
                        e.printStackTrace();
                    }
                    try {
                        JSONArray fmpEntriesArray = response.getJSONArray(OFISDBContract.FarmManagementPlanEntriesEntry.TABLE_NAME);
                        fillTable(fmpEntriesArray, OFISDBContract.FarmManagementPlanEntriesEntry.TABLE_NAME, OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_LIST);
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateFmpTask: Error filling fmp entries table", e));
                        e.printStackTrace();
                    }
                    try {
                        JSONArray fmpCurrentValuesArray = response.getJSONArray(OFISDBContract.FarmManagementPlanCurrentValuesEntry.TABLE_NAME);
                        fillTable(fmpCurrentValuesArray, OFISDBContract.FarmManagementPlanCurrentValuesEntry.TABLE_NAME, OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_LIST);
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateFmpTask: Error filling fmp values table", e));
                        e.printStackTrace();
                    }
                }
            }

        catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateFmpTask: Error getting fmp table response", e));
            e.printStackTrace();
        }

        return null;
    }
}
