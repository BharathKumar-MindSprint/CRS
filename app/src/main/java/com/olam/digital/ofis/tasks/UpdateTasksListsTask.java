package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ben on 08/07/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
class UpdateTasksListsTask extends DatabaseTask {
    ProgressDialog pd;
    public UpdateTasksListsTask(Context context) {
        super(context);
        pd = Utilities.openProgressDialog(context, "Data initialization", false);

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Utilities.closeProgressDialog(pd, 2);
    }

    @Override
    protected String doInBackground(JSONObject... params) {

        try {

            JSONObject tasksResponse = params[0];

            if (tasksResponse.getInt("resultcount") > 0) {

                JSONObject response = tasksResponse.getJSONObject("outputdata");

                // get the various sub lists
                JSONArray tasksArray;

                try {
                    tasksArray = response.getJSONArray(OFISDBContract.Task.TABLE_NAME);
                    updateTasks(tasksArray);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTasksListsTask: Error filling tasks table", e));
                    e.printStackTrace();
                }

            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateTasksListsTask: Error getting tasks lists response", e));
            e.printStackTrace();
        }

        return null;
    }
}
