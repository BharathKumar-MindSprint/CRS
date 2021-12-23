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

public class UpdateFarmerLeadTask extends DatabaseTask {
    ProgressDialog pd;

    public UpdateFarmerLeadTask(Context context) {
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
            JSONObject flResponse = params[0];

            JSONArray flArray;
            try {
                flArray = flResponse.getJSONArray(OFISDBContract.FarmerLeadDetails.TABLE_NAME);
                fillTable(flArray, OFISDBContract.FarmerLeadDetails.TABLE_NAME, OFISDBContract.FarmerLeadDetails.COLUMN_LIST);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("UpdateFarmerLeadTask: Error filling farmerleads table", e));
                e.printStackTrace();
            }
        } catch (Exception e){}
          return null;
    }
}