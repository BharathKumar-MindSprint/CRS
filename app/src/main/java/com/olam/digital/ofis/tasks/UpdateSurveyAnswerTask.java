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
class UpdateSurveyAnswerTask extends DatabaseTask {
    ProgressDialog pd;
    public UpdateSurveyAnswerTask(Context context) {
        super(context);
        pd = Utilities.openProgressDialog(context, context.getString(R.string.downloading), false);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Utilities. closeProgressDialog(pd, 2);
    }

    @Override
    protected String doInBackground(JSONObject... params) {
        try {
            JSONObject answerResponse = params[0];

            JSONArray ansArray;
            try {
                 ansArray = answerResponse.getJSONArray("answers");
                 fillTable(ansArray, OFISDBContract.AnswerEntry.TABLE_NAME, OFISDBContract.AnswerEntry.COLUMN_LIST);

            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("UpdateSurveyAnswerTask: Error filling Answer table", e));
                e.printStackTrace();
            }
        } catch (Exception e){e.printStackTrace();}

        return null;
    }
}
