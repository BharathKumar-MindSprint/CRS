package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.crs.viewmodel.api.CrsApi;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
class UpdateAllCommunityProfileListsTask extends DatabaseTask {

    ProgressDialog pd;

    public UpdateAllCommunityProfileListsTask(Context context, ProgressDialog pd) {
        super(context);
        Log.e("UpdateSpinnerListsTask:","inside cons");
        this.pd = pd;
        pd = Utilities.updateProgressDialog(pd, 0, 0, context.getString(R.string.data_initialization));
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Utilities.closeProgressDialog(pd, 2);
    }

    @Override
    protected String doInBackground(JSONObject... params) {

        try {
            JSONObject spinnerResponse = params[0];
            Log.e("UpdateSpinnerListsTask:","inside doin");
            CrsApi.insertAllCrsCommunityProfilesBlocking(context,new JSONArray().put(spinnerResponse));
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error getting spinner lists response", e));
            e.printStackTrace();
        }
        return null;
    }
}
