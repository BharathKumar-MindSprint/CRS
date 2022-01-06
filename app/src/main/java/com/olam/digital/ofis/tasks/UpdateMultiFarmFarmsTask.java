package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateMultiFarmFarmsTask extends DatabaseTask {
    ProgressDialog pd;
    public UpdateMultiFarmFarmsTask(Context context) {
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

        JSONObject farmerResponse = params[0];
        
        JSONArray farmArray = null;
        try {
            farmArray = farmerResponse.getJSONArray("farmerInfo");
            fillTable(farmArray, OFISDBContract.MultiFarmFarms.TABLE_NAME, OFISDBContract.MultiFarmFarms.COLUMN_LIST);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
