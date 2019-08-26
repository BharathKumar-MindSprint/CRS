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

public class UpdateChildrenTask extends DatabaseTask {
    ProgressDialog pd;
    public UpdateChildrenTask(Context context) {
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

            JSONObject ChildResponse = params[0];

            if (ChildResponse.getInt("resultcount") > 0) {

                JSONObject response = ChildResponse.getJSONObject("outputdata");

                // get the various sub lists
                JSONArray sipGroupArray, sipSubGroupArray, moduelassignmentArray, datatypeArray, lookupvalueArray, questionArray, productArray;
                try {
                    sipGroupArray = response.getJSONArray(OFISDBContract.SipGroupEntry.TABLE_NAME);
                    fillTable(sipGroupArray, OFISDBContract.SipGroupEntry.TABLE_NAME, OFISDBContract.SipGroupEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSipModuleListsTask: Error filling modules table", e));
                    e.printStackTrace();
                }
            }
        } catch(JSONException e){
            FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error getting module lists table response", e));
            e.printStackTrace();
        }

        return null;
    }
}
