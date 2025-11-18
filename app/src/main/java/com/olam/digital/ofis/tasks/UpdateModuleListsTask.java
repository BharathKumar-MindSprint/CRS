package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.mvvmmodule.model.FarmerSurveyAssignmentResponse;
import com.olam.digital.ofis.mvvmmodule.model.qwrdata.SurveyDurationType;
import com.olam.digital.ofis.mvvmmodule.model.qwrdata.ValidationOnRange;
import com.olam.digital.ofis.mvvmmodule.model.assigmenttype.AssignmentType;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.FarmerSurveyAssignment;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ConditionalLogic;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ConditionalType;


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
    private SQLiteDatabase db;
    public UpdateModuleListsTask(Context context) {
        super(context);
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

        try {
            JSONObject moduleResponse = params[0];
            JSONObject conditionalLogicResponse = params[1];
            JSONObject moduleIdResponse = params[2];
            JSONObject assignedFarmerResponse = params[3];

            OFISRoomDB roomDB = OFISRoomDB.getDbInstance(context);
            Gson gson = new Gson();

            if (moduleResponse.getInt("resultcount") > 0) {
                JSONObject response = moduleResponse.getJSONObject("outputdata");
                JSONObject questionnaireData = moduleResponse.getJSONObject("outputdata").getJSONObject("questionnaireData");

                // get the various sub lists
                JSONArray moduleArray, submoduleArray, moduelassignmentArray, datatypeArray, lookupvalueArray, questionArray, countryLookUpValuesArray, questionTypeArray , answerTypeArray, recurrenceTypeArray;
                List<ValidationOnRange> validationOnRangeList;
                List<AssignmentType> assignmentTypeList;
                List<SurveyDurationType> surveyDurationTypeList;
                List<ConditionalType> conditionalTypeList;

                try {
                    moduleArray = response.getJSONArray(OFISDBContract.ModuleEntry.TABLE_NAME);
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
                    JSONArray valiOnRangeArray = questionnaireData.getJSONArray("validationOnRange");
                    Type type = new TypeToken<List<ValidationOnRange>>() {}.getType();
                    validationOnRangeList = gson.fromJson(String.valueOf(valiOnRangeArray), type);
                    roomDB.validationOnRangeDao().insertAll(validationOnRangeList);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling ValidationOnRange table", e));
                    e.printStackTrace();
                }

                try{
                    JSONArray assignmentTypeArray = response.getJSONArray("assignmentTypes");
                    Type type = new TypeToken<List<AssignmentType>>() {}.getType();
                    assignmentTypeList = gson.fromJson(String.valueOf(assignmentTypeArray), type);
                    roomDB.assignmentTypeDao().insertAll(assignmentTypeList);
                }catch (JSONException e){
                    FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error filling AssignmentTypes table", e));
                    e.printStackTrace();
                }

                try {
                    JSONArray surveyDurationTypeArray = questionnaireData.getJSONArray("surveyDurationType");
                    Type type = new TypeToken<List<SurveyDurationType>>() {}.getType();
                    surveyDurationTypeList = gson.fromJson(String.valueOf(surveyDurationTypeArray), type);
                    roomDB.surveyDurationTypeDao().insertAll(surveyDurationTypeList);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("surveyDurationType: Error filling surveyDurationType table", e));
                    e.printStackTrace();
                }

                try {
                    JSONArray conditionalTypeArray = questionnaireData.getJSONArray("conditionalType");
                    Type type = new TypeToken<List<ConditionalType>>() {}.getType();
                    conditionalTypeList = gson.fromJson(String.valueOf(conditionalTypeArray), type);
                    if (!conditionalTypeList.isEmpty()) {
                        roomDB.conditionalTypeDao().deleteAll();
                        roomDB.conditionalTypeDao().insertAll(conditionalTypeList);
                    }
                }
                catch (Exception e) {
                    FirebaseCrash.report(new Exception("Question Type: Error filling question type table", e));
                    e.printStackTrace();
                }
            }

            if (conditionalLogicResponse != null) {
                roomDB.conditionalLogicDao().deleteAll();
                JSONArray conditionsResponseArray = conditionalLogicResponse.getJSONArray("conditions");
                Type type = new TypeToken<List<ConditionalLogic>>() {}.getType( );
                List<ConditionalLogic> conditionalResponse = gson.fromJson(conditionsResponseArray.toString(), type);
                roomDB.conditionalLogicDao().insertAll(conditionalResponse);
            }

            if (moduleIdResponse != null) {
                db.beginTransaction();
                try {
                    JSONArray moduleIdsForNormalSurvey = moduleIdResponse.getJSONArray("modulesWithoutAutoSampling");
                    if (moduleIdsForNormalSurvey.length() > 0) {
                        ContentValues values = new ContentValues();
                        values.put(OFISDBContract.ModuleEntry.COLUMN_NAME_IS_AUTO_SURVEY, "false");

                        StringBuilder whereClause = new StringBuilder(OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " IN (");
                        String[] whereArgs = new String[moduleIdsForNormalSurvey.length()];
                        for (int i = 0; i < moduleIdsForNormalSurvey.length(); i++) {
                            whereArgs[i] = moduleIdsForNormalSurvey.getString(i);
                            whereClause.append("?");
                            if (i < moduleIdsForNormalSurvey.length() - 1) whereClause.append(", ");
                        }
                        whereClause.append(")");

                        db.update(OFISDBContract.ModuleEntry.TABLE_NAME, values, whereClause.toString(), whereArgs);
                    }

                    JSONArray moduleIdsForAutoSampleSurvey = moduleIdResponse.getJSONArray("modulesWithAutoSampling");
                    if (moduleIdsForAutoSampleSurvey.length() > 0) {
                        ContentValues values = new ContentValues();
                        values.put(OFISDBContract.ModuleEntry.COLUMN_NAME_IS_AUTO_SURVEY, "true");

                        StringBuilder whereClause = new StringBuilder(OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " IN (");
                        String[] whereArgs = new String[moduleIdsForAutoSampleSurvey.length()];
                        for (int i = 0; i < moduleIdsForAutoSampleSurvey.length(); i++) {
                            whereArgs[i] = moduleIdsForAutoSampleSurvey.getString(i);
                            whereClause.append("?");
                            if (i < moduleIdsForAutoSampleSurvey.length() - 1) whereClause.append(", ");
                        }
                        whereClause.append(")");

                        db.update(OFISDBContract.ModuleEntry.TABLE_NAME, values, whereClause.toString(), whereArgs);
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            }

            roomDB.assignedFarmerForSurveyDao().deleteAll();
            if (assignedFarmerResponse != null && assignedFarmerResponse.has("assignedFarmers")) {
                JSONArray responseArray = assignedFarmerResponse.getJSONArray("assignedFarmers");
                Type type = new TypeToken<List<FarmerSurveyAssignmentResponse>>() {}.getType( );
                List<FarmerSurveyAssignmentResponse> farmerSurveyAssignmentRespons = gson.fromJson(responseArray.toString(), type);

                List<FarmerSurveyAssignment> farmerSurveyAssignments = new ArrayList<>();
                for (FarmerSurveyAssignmentResponse assignedFarmer : farmerSurveyAssignmentRespons) {
                    int moduleId = assignedFarmer.getModuleId();
                    for (Integer farmerId : assignedFarmer.getFarmerIds()) {
                        farmerSurveyAssignments.add(new FarmerSurveyAssignment(moduleId, farmerId));
                    }
                }

                roomDB.assignedFarmerForSurveyDao().insertAssignedFarmers(farmerSurveyAssignments);
            }

        } catch(JSONException e){
            FirebaseCrash.report(new Exception("UpdateModuleListsTask: Error getting module lists table response", e));
            e.printStackTrace();
        }

        return null;
    }
}
