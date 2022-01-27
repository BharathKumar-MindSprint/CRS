package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateMultiFarmFarmsTask extends DatabaseTask {
    ProgressDialog pd;
    DataLayer dl;
    private SQLiteDatabase db ;
    Context context;
    public UpdateMultiFarmFarmsTask(Context context) {
        super(context);
        this.context=context;
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

        JSONObject farmerResponse = params[0];
        if (farmerResponse!=null) {
            JSONArray farmArray = null;
            try {
                farmArray = farmerResponse.getJSONArray("farmerInfo");
                fillTable(farmArray, OFISDBContract.MultiFarmFarms.TABLE_NAME, OFISDBContract.MultiFarmFarms.COLUMN_LIST);

                db.delete("stakeHolders", null, null);
                for (int i = 0; i < farmArray.length(); i++) {
                    try {
                        JSONArray stakeholderArray = farmArray.getJSONObject(i).getJSONArray("stakeHolders");
                        fillTable(stakeholderArray, OFISDBContract.MultiFarmStakeholderFarms.TABLE_NAME, OFISDBContract.MultiFarmStakeholderFarms.COLUMN_LIST);

                    } catch (Exception e) {
                    }


//                if (stakeholderArray!=null) {
//                    for (int j = 0; j < stakeholderArray.length(); j++) {
//                        ContentValues values=new ContentValues();
//                        values.put(OFISDBContract.MultiFarmStakeholderFarms.COLUMN_NAME_ID,stakeholderArray.getJSONObject(j).get("id").toString());
//                        values.put(OFISDBContract.MultiFarmStakeholderFarms.COLUMN_NAME_NAME_FARM_NAME,stakeholderArray.getJSONObject(j).get("farmName").toString());
//                        db.insert("stakeHolders",null,values);
//                    }
//                }
//                db.insert(OFISDBContract.NetworkQueueEntry.TABLE_NAME, null, values);


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
