package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.JsonArray;
import com.olam.digital.crs.model.mapper.CrsFarmerMapper;
import com.olam.digital.crs.room.FarmerEntity;
import com.olam.digital.crs.viewmodel.api.CrsApi;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

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


                JSONArray farmerArray = null, phonenumbersArray;
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


                    // 2) CRS insert (Java-friendly API)
                    List<FarmerEntity> crsEntities = CrsFarmerMapper.mapJsonArrayToEntities(farmerArray);
                    //delete farmers and then insert into crs library
                    CrsApi.insertFarmers(context,crsEntities);

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
            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateFarmersTask: Error getting farmers table response", e));
            e.printStackTrace();
        }

        return null;
    }
}
