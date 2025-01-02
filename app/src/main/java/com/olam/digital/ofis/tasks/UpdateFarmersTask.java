package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.JsonArray;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
class UpdateFarmersTask extends DatabaseTask {
    ProgressDialog pd;
    private final SQLiteDatabase db;

    public UpdateFarmersTask(Context context) {
        super(context);
        pd = Utilities.openProgressDialog(context, context.getString(R.string.data_initialization), false);
        db = Utilities.getWriteableDb(context);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Utilities.closeProgressDialog(pd, 2);
    }

    @Override
    protected String doInBackground(JSONObject... params) {

        try {
            JSONObject farmerResponse = params[0];

            if (farmerResponse.getInt("resultcount") > 0) {

                JSONObject response = farmerResponse.getJSONObject("outputData");


                JSONArray farmerArray = null, phonenumbersArray,plotsArray;
                // child visit array start
                try {
                    JSONArray childvisitArray = farmerResponse.getJSONArray("childrenVisit");
                    if (childvisitArray.length()>0) {
                        fillTable(childvisitArray, OFISDBContract.ChildVisitDetails.TABLE_NAME, OFISDBContract.ChildVisitDetails.COLUMN_LIST);
                    }
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateFarmersTask: Error filling farmers table", e));
                    e.printStackTrace();
                }
                //// child visit array End

                try {
                    farmerArray = response.getJSONArray(OFISDBContract.FarmerEntry.TABLE_NAME);
                    fillTable(farmerArray, OFISDBContract.FarmerEntry.TABLE_NAME, OFISDBContract.FarmerEntry.SYNC_COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateFarmersTask: Error filling farmers table", e));
                    e.printStackTrace();
                }
                try {
                    phonenumbersArray = response.getJSONArray(OFISDBContract.PhoneNumber.TABLE_NAME);
                    db.delete(OFISDBContract.PhoneNumber.TABLE_NAME, null, null);
                    fillTable(phonenumbersArray, OFISDBContract.PhoneNumber.TABLE_NAME, OFISDBContract.PhoneNumber.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateFarmersTask: Error filling phone numbers table", e));
                    e.printStackTrace();
                }
                try{
                    plotsArray = new JSONArray();
                    for (int j = 0; j < farmerArray.length(); j++) {
                        JSONObject farmerObject = farmerArray.getJSONObject(j);
                        JSONArray plotArray = farmerObject.getJSONArray(OFISDBContract.PlotEntry.TABLE_NAME);
                        for (int i = 0; i < plotArray.length(); i++) {
                            JSONObject plotObject = plotArray.getJSONObject(i);
                            plotObject.put(OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID,farmerObject.get("id"));
                            plotObject.put(OFISDBContract.PlotEntry.COLUMN_IS_SYNCED,"1");
                            plotObject.put(OFISDBContract.PlotEntry.COLUMN_NAME_IS_EUDR_SURVEY_COMPLETED, "false");
                            plotObject.put(OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_SURVEY_COMPLETED, "false");
                            plotsArray.put(plotObject);
                        }
                    }

                    if(plotsArray.length() > 0 ) {
                        fillTable(plotsArray, OFISDBContract.PlotEntry.TABLE_NAME, OFISDBContract.PlotEntry.COLUMN_LIST);
                    }

                }catch (Exception e){
                  e.printStackTrace();
                }


            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateFarmersTask: Error getting farmers table response", e));
            e.printStackTrace();
        }

        return null;
    }
}
