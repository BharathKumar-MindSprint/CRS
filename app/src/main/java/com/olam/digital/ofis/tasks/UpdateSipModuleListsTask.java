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

class UpdateSipModuleListsTask extends DatabaseTask {
    ProgressDialog pd;
    public UpdateSipModuleListsTask(Context context) {
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

            JSONObject sipModuleResponse = params[0];

            if (sipModuleResponse.getInt("resultcount") > 0) {

                JSONObject response = sipModuleResponse.getJSONObject("outputdata");

                // get the various sub lists
                JSONArray sipGroupArray, sipSubGroupArray, moduelassignmentArray, datatypeArray, lookupvalueArray, questionArray;
                try {
                    sipGroupArray = response.getJSONArray(OFISDBContract.SipGroupEntry.TABLE_NAME);
                    fillTable(sipGroupArray, OFISDBContract.SipGroupEntry.TABLE_NAME, OFISDBContract.SipGroupEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSipModuleListsTask: Error filling modules table", e));
                    e.printStackTrace();
                }
                try {
                    sipSubGroupArray = response.getJSONArray("sipGroup");
                    fillTable(sipSubGroupArray, OFISDBContract.SipSubGroupEntry.TABLE_NAME, OFISDBContract.SipSubGroupEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateSipModuleListsTask: Error filling submodules table", e));
                    e.printStackTrace();
                }
                /*try {
                    moduelassignmentArray = response.getJSONArray(OFISDBContract.ModuleAssignmentsEntry.TABLE_NAME);
                    fillTable(moduelassignmentArray, OFISDBContract.ModuleAssignmentsEntry.TABLE_NAME, OFISDBContract.ModuleAssignmentsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling module assignments table", e));
                    e.printStackTrace();
                }*/
                try {
                    datatypeArray = response.getJSONArray("datatypes");
                    fillTable(datatypeArray, OFISDBContract.DatatypesSipEntry.TABLE_NAME, OFISDBContract.DatatypesSipEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling datatypes table", e));
                    e.printStackTrace();
                }
                try {
                    lookupvalueArray = response.getJSONArray("sipLookupvalues");
                    fillTable(lookupvalueArray, OFISDBContract.LookupValuesSipEntry.TABLE_NAME, OFISDBContract.LookupValuesSipEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling lookup values table", e));
                    e.printStackTrace();
                }
                try {
                    questionArray = response.getJSONArray("sipQuestions");
                    fillTable(questionArray, OFISDBContract.QuestionSipEntry.TABLE_NAME, OFISDBContract.QuestionSipEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling questions table", e));
                    e.printStackTrace();
                }
            }
        } catch(JSONException e){
            FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error getting module lists table response", e));
            e.printStackTrace();
        }

        return null;
    }
}
