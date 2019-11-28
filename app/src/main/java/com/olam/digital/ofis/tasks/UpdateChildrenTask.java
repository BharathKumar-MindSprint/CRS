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
            JSONObject childResponse = params[0];

            JSONArray childArray;
            try {
                childArray = childResponse.getJSONArray("child");
                fillTable(childArray, OFISDBContract.ChildEntry.TABLE_NAME, OFISDBContract.ChildEntry.COLUMN_LIST);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("UpdateChildrenTask: Error filling children table", e));
                e.printStackTrace();
            }
        } catch (Exception e){}
          return null;
    }
}