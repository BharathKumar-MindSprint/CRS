package com.olam.digital.ofis.tasks;

import static com.olam.digital.ofis.NetworkLayer.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.model.qwrdata.SurveyDurationType;
import com.olam.digital.ofis.mvvmmodule.model.qwrdata.ValidationOnRange;
import com.olam.digital.ofis.mvvmmodule.model.assigmenttype.AssignmentType;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
class UpdateModuleListsTask extends DatabaseTask {
    ProgressDialog pd;
    public UpdateModuleListsTask(Context context) {
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

            JSONObject moduleResponse = params[0];

            if (moduleResponse.getInt("resultcount") > 0) {
                OFISRoomDB roomDB = OFISRoomDB.getDbInstance(context);

                JSONObject response = moduleResponse.getJSONObject("outputdata");
                JSONObject questionnaireData = moduleResponse.getJSONObject("outputdata").getJSONObject("questionnaireData");

                // get the various sub lists
                JSONArray moduleArray, submoduleArray, moduelassignmentArray, datatypeArray, lookupvalueArray, questionArray, countryLookUpValuesArray, questionTypeArray , answerTypeArray, recurrenceTypeArray;
                List<ValidationOnRange> validationOnRangeList;
                List<AssignmentType> assignmentTypeList;
                List<SurveyDurationType> surveyDurationTypeList = new ArrayList<>();
                try {
                    moduleArray = response.getJSONArray(OFISDBContract.ModuleEntry.TABLE_NAME);
/*
                    // Start SIP Filters
          JSONArray sipModuleArray=new JSONArray();

          for (int i=0;i< moduleArray.length();i++){
              if(moduleArray.getJSONObject(i).getString("name").contains("SIP")){
                  sipModuleArray.put(moduleArray.getJSONObject(i));
              }
          }
                    fillTable(sipModuleArray, OFISDBContract.ModuleEntry.TABLE_NAME_SIP, OFISDBContract.ModuleEntry.COLUMN_LIST);
                    // SIP End End*/

                    fillTable(moduleArray, OFISDBContract.ModuleEntry.TABLE_NAME, OFISDBContract.ModuleEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling modules table", e));
                    e.printStackTrace();
                }
                try {
                    submoduleArray = response.getJSONArray(OFISDBContract.SubmoduleEntry.TABLE_NAME);
                    fillTable(submoduleArray, OFISDBContract.SubmoduleEntry.TABLE_NAME, OFISDBContract.SubmoduleEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling submodules table", e));
                    e.printStackTrace();
                }
                try {
                    moduelassignmentArray = response.getJSONArray(OFISDBContract.ModuleAssignmentsEntry.TABLE_NAME);
                    fillTable(moduelassignmentArray, OFISDBContract.ModuleAssignmentsEntry.TABLE_NAME, OFISDBContract.ModuleAssignmentsEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling module assignments table", e));
                    e.printStackTrace();
                }
                try {
                    datatypeArray = response.getJSONArray(OFISDBContract.DatatypesEntry.TABLE_NAME);
                    fillTable(datatypeArray, OFISDBContract.DatatypesEntry.TABLE_NAME, OFISDBContract.DatatypesEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling datatypes table", e));
                    e.printStackTrace();
                }
                try {
                    lookupvalueArray = response.getJSONArray(OFISDBContract.LookupValuesEntry.TABLE_NAME);
                    fillTable(lookupvalueArray, OFISDBContract.LookupValuesEntry.TABLE_NAME, OFISDBContract.LookupValuesEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling lookup values table", e));
                    e.printStackTrace();
                }
                try {
                    questionArray = response.getJSONArray(OFISDBContract.QuestionEntry.TABLE_NAME);
                    fillTable(questionArray, OFISDBContract.QuestionEntry.TABLE_NAME, OFISDBContract.QuestionEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling questions table", e));
                    e.printStackTrace();
                }
                try {
                    countryLookUpValuesArray = response.getJSONArray(OFISDBContract.countryLookUpValuesEntry.TABLE_NAME);
                    fillTable(countryLookUpValuesArray, OFISDBContract.countryLookUpValuesEntry.TABLE_NAME, OFISDBContract.countryLookUpValuesEntry.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling questions table", e));
                    e.printStackTrace();
                }

                try {
                    questionTypeArray = questionnaireData.getJSONArray(OFISDBContract.QuestionType.TABLE_NAME);
                    fillTable(questionTypeArray, OFISDBContract.QuestionType.TABLE_NAME, OFISDBContract.QuestionType.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("QuestionType: Error filling QuestionType table", e));
                    e.printStackTrace();
                }

                try {
                    answerTypeArray = questionnaireData.getJSONArray(OFISDBContract.AnswerType.TABLE_NAME);
                    fillTable(answerTypeArray, OFISDBContract.AnswerType.TABLE_NAME, OFISDBContract.AnswerType.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("AnswerType: Error filling AnswerType table", e));
                    e.printStackTrace();
                }

                try {
                    recurrenceTypeArray = questionnaireData.getJSONArray(OFISDBContract.RecurrenceType.TABLE_NAME);
                    fillTable(recurrenceTypeArray, OFISDBContract.RecurrenceType.TABLE_NAME, OFISDBContract.RecurrenceType.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("RecurrenceType: Error filling RecurrenceType table", e));
                    e.printStackTrace();
                }
                try {
                    Gson gson = new Gson();
                    JSONArray valiOnRangeArray = questionnaireData.getJSONArray("validationOnRange");
                    Log.e(TAG,"Check logs "+valiOnRangeArray);
                    Type type = new TypeToken<List<ValidationOnRange>>() {}.getType();
                    validationOnRangeList = gson.fromJson(String.valueOf(valiOnRangeArray), type);
                    roomDB.validationOnRangeDao().insertAll(validationOnRangeList);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling ValidationOnRange table", e));
                    e.printStackTrace();
                }

                try{
                    Gson gson = new Gson();
                    JSONArray assignmentTypeArray = response.getJSONArray("assignmentTypes");
                    Log.e(TAG,"AssignmentTypes: " + assignmentTypeArray);
                    Type type = new TypeToken<List<AssignmentType>>() {}.getType();
                    assignmentTypeList = gson.fromJson(String.valueOf(assignmentTypeArray), type);
                    roomDB.assignmentTypeDao().insertAll(assignmentTypeList);
                }catch (JSONException e){
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling AssignmentTypes table", e));
                    e.printStackTrace();
                }

                try {
                    Gson gson = new Gson();
                    JSONArray surveyDurationTypeArray = questionnaireData.getJSONArray("surveyDurationType");
                    Log.e(TAG,"Check logs "+surveyDurationTypeArray);
                    Type type = new TypeToken<List<SurveyDurationType>>() {}.getType();
                    surveyDurationTypeList = gson.fromJson(String.valueOf(surveyDurationTypeArray), type);
                    roomDB.surveyDurationTypeDao().insertAll(surveyDurationTypeList);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("surveyDurationType: Error filling surveyDurationType table", e));
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
