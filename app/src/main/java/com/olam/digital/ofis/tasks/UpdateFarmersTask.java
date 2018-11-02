package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
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
    public UpdateFarmersTask(Context context) {
        super(context);
        pd = Utilities.openProgressDialog(context, context.getString(R.string.data_initialization), false);
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

                JSONArray farmerArray, phonenumbersArray;

                try {
                    farmerArray = response.getJSONArray(OFISDBContract.FarmerEntry.TABLE_NAME);
                    fillTable(farmerArray, OFISDBContract.FarmerEntry.TABLE_NAME, OFISDBContract.FarmerEntry.SYNC_COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateFarmersTask: Error filling farmers table", e));
                    e.printStackTrace();
                }
                try {
                    phonenumbersArray = response.getJSONArray(OFISDBContract.PhoneNumber.TABLE_NAME);
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
