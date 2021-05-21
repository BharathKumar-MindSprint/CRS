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
class UpdateNationalIdTask extends DatabaseTask {
    ProgressDialog pd;
    Context context;
    public UpdateNationalIdTask(Context context) {
        super(context);
        this.context = context;
        pd = Utilities.openProgressDialog(context, context.getString(R.string.data_initialization), false);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Utilities.closeProgressDialog(pd, 2);
    }

    @Override
    protected String doInBackground(JSONObject... params) {

            JSONObject nationalidResponse = params[0];
                System.out.println("getNationalIDsByEnumerator:" + nationalidResponse.toString());
                try {
                    JSONArray nationalidArray = nationalidResponse.getJSONArray("nationalids");
                    if (nationalidArray.length()>0) {
                        fillTable(nationalidArray, OFISDBContract.NationalIDEntry.TABLE_NAME, OFISDBContract.NationalIDEntry.COLUMN_LIST);
                        Utilities.saveToSharedPrefs(context, "NationalID", null, true);
                    }else{
                        Utilities.saveToSharedPrefs(context, "NationalID", null, false);
                    }
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdatenationalIdTask: Error filling nationalids table", e));
                    e.printStackTrace();
                }


        return null;
    }
}
