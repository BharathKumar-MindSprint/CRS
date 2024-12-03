package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateStakeHolderResponse extends DatabaseTask {
    ProgressDialog pd;
    DataLayer dl;
    Context context;
    public UpdateStakeHolderResponse(Context context) {
        super(context);
        this.context=context;
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
            JSONObject stakeHolderResponse = params[0];

            if (stakeHolderResponse != null) {
                JSONObject response = stakeHolderResponse.has("outputData") ? stakeHolderResponse.getJSONObject("outputData") : new JSONObject();

                if (response.has("nationalIds")) {
                    JSONArray nationalIdArray = response.getJSONArray("nationalIds");
                    if (nationalIdArray.length() > 0) {
                        fillTable(nationalIdArray, OFISDBContract.FarmerNationalIDs.TABLE_NAME, OFISDBContract.FarmerNationalIDs.COLUMN_LIST);
                    }
                }

                if (response.has("phoneNumbers")) {
                    JSONArray phoneNumberArray = response.getJSONArray("phoneNumbers");
                    if (phoneNumberArray.length() > 0) {
                        fillTable(phoneNumberArray, OFISDBContract.PhoneNumber.TABLE_NAME, OFISDBContract.PhoneNumber.COLUMN_LIST);
                    }
                }
            }
        }
        catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateStakeHolderResponse: Error getting StakeHolder PhoneNumber or NationalId response", e));
            e.printStackTrace();
        }

        return null;
    }
}
