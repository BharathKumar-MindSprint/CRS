package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.olam.digital.crs.viewmodel.api.CrsApi;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Certifications;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Partners;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Programme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import timber.log.Timber;

class UpdateCrsProfileListTask extends DatabaseTask {

    ProgressDialog pd;

    public UpdateCrsProfileListTask(Context context, ProgressDialog pd) {
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
            CrsApi.insertAllCrsProfilesBlocking(context,new JSONArray().put(params[0]));
            
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("UpdateSpinnerListsTask: Error getting spinner lists response", e));
            e.printStackTrace();
        }
        return null;
    }
}
