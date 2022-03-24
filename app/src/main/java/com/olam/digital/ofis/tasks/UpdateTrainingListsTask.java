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
 * Created by ben on 08/07/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
class UpdateTrainingListsTask extends DatabaseTask {
    ProgressDialog pd;
    public UpdateTrainingListsTask(Context context) {
        super(context);
        this.pd = pd;
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

            JSONObject trainingResponse = params[0];
            if (trainingResponse !=null) {
                if (trainingResponse.getInt("resultcount") > 0) {

                    JSONObject response = trainingResponse.getJSONObject("outputdata");

                    // get the various sub lists
                    JSONArray coursesArray, courseAssignmentsArray, trainingModulesArray, completionCriteriaArray,relationShipArray;

                    try {
                        coursesArray = response.getJSONArray(OFISDBContract.Course.TABLE_NAME);
                        fillTable(coursesArray, OFISDBContract.Course.TABLE_NAME, OFISDBContract.Course.COLUMN_LIST);
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateTrainingListsTask: Error filling courses table", e));
                        e.printStackTrace();
                    }
                    try {
                        courseAssignmentsArray = response.getJSONArray(OFISDBContract.CourseAssignment.TABLE_NAME);
                        fillTable(courseAssignmentsArray, OFISDBContract.CourseAssignment.TABLE_NAME, OFISDBContract.CourseAssignment.COLUMN_LIST);
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateTrainingListsTask: Error filling course assignments table", e));
                        e.printStackTrace();
                    }
                    try {
                        trainingModulesArray = response.getJSONArray(OFISDBContract.TrainingModule.TABLE_NAME);
                        fillTable(trainingModulesArray, OFISDBContract.TrainingModule.TABLE_NAME, OFISDBContract.TrainingModule.COLUMN_LIST);
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateTrainingListsTask: Error filling training modules table", e));
                        e.printStackTrace();
                    }
                    try {
                        completionCriteriaArray = response.getJSONArray(OFISDBContract.CompletionCriteria.TABLE_NAME);
                        fillTable(completionCriteriaArray, OFISDBContract.CompletionCriteria.TABLE_NAME, OFISDBContract.CompletionCriteria.COLUMN_LIST);
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateTrainingListsTask: Error filling completion criteria table", e));
                        e.printStackTrace();
                    }

                    try {
                        relationShipArray = response.getJSONArray(OFISDBContract.TrainingAttendeeRelationShip.TABLE_NAME);
                        fillTable(relationShipArray, OFISDBContract.TrainingAttendeeRelationShip.TABLE_NAME, OFISDBContract.TrainingAttendeeRelationShip.COLUMN_LIST);
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("UpdateTrainingListsTask: Error filling completion criteria table", e));
                        e.printStackTrace();
                    }
                }
            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateTrainingListsTask: Error getting training lists response", e));
            e.printStackTrace();
        }

        return null;
    }
}
