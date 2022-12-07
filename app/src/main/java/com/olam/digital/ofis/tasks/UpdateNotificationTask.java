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

public class UpdateNotificationTask extends DatabaseTask {
    ProgressDialog pd;

    public UpdateNotificationTask(Context context) {
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
            JSONObject notifyResponse = params[0];

            JSONArray notificationArray,moduleArray;
            try {
                notificationArray = notifyResponse.getJSONArray("notificationDetailsList");
                fillTable(notificationArray, OFISDBContract.NotificationAssignments.TABLE_NAME, OFISDBContract.NotificationAssignments.COLUMN_LIST);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("NotificationAssignmentsTask: Error filling NotificationAssignments table", e));
                e.printStackTrace();
            }

            try {
                moduleArray = notifyResponse.getJSONArray("moduleList");
                fillTable(moduleArray, OFISDBContract.NotificationModuleAssignments.TABLE_NAME, OFISDBContract.NotificationModuleAssignments.COLUMN_LIST);
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("NotificationModuleAssignments: Error filling NotificationModuleAssignments table", e));
                e.printStackTrace();
            }
        } catch (Exception e){}
          return null;
    }
}