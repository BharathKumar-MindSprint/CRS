package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
class UpdateEnumNationalIdTask extends DatabaseTask {
    ProgressDialog pd;
    private SQLiteDatabase db;
    public UpdateEnumNationalIdTask(Context context) {
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

            JSONObject nationalidResponse = params[0];

                try {
                    db.delete(OFISDBContract.FarmerNationalIDs.TABLE_NAME, null, null);
                    JSONArray nationalidArray = nationalidResponse.getJSONArray("nationalids");
                    if (nationalidArray.length()>0) {
                        fillTable(nationalidArray, OFISDBContract.FarmerNationalIDs.TABLE_NAME, OFISDBContract.FarmerNationalIDs.COLUMN_LIST);
                    }
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdatenationalIdTask: Error filling nationalids table", e));
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }


        return null;
    }
}
