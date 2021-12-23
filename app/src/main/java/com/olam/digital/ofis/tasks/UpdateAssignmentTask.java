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

public class UpdateAssignmentTask extends DatabaseTask {
    ProgressDialog pd;

    public UpdateAssignmentTask(Context context) {
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

            JSONArray faArray;
            try {
                faArray = flResponse.getJSONArray(OFISDBContract.FarmerAssignments.TABLE_NAME);
                fillTable(faArray, OFISDBContract.FarmerAssignments.TABLE_NAME, OFISDBContract.FarmerAssignments.COLUMN_LIST);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("UpdateFarmerLeadTask: Error filling FarmerAssignments table", e));
                e.printStackTrace();
            }
        } catch (Exception e){}
          return null;
    }
}