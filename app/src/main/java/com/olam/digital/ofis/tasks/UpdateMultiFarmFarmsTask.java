package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.olam.digital.ofis.DataLayer;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;

import org.json.JSONObject;

public class UpdateMultiFarmFarmsTask extends DatabaseTask {
    ProgressDialog pd;
    DataLayer dl;
    private SQLiteDatabase db;
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

        }

        return null;
    }
}
