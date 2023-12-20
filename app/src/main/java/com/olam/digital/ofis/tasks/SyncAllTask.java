package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.HomeActivity;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.SettingsActivity;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.authentication.AuthenticationClient;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.authentication.AuthenticationInterface;
import com.olam.digital.ofis.authentication.DeviceIDxctractor;
import com.olam.digital.ofis.authentication.KeyCloakInfo;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.downloadsurvey.DownloadSurveyData;
import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.dynatrace.DynatraceHelper;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.logging.FabricLogsWriter;
import com.olam.digital.ofis.multifarm.MultiFarmUtils;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by ben on 24/02/16.
 */

@SuppressWarnings("DefaultFileTemplate")
public class SyncAllTask extends SyncTask {

    public interface SyncAllTaskDelegate {
        void didCompleteSyncOperation(SyncAllTask task, SyncTaskOperationType nextOperation);
    }

    enum SyncOperation {
        /*
         * Relates to old msg.what
         * 1 = CONTINUE_DOWNLOADING
         * 2 = START_IMPORT
         * 3 = ERROR_RECEIVED
         * 4 = NEW_UPDATE AVAILABLE
         */

        CONTINUE_DOWNLOADING, START_IMPORT, ERROR_RECEIVED, NEW_UPDATE_AVAILABLE, START_SURVEYANSIMPORT
    }

    public static final String TAG = "SyncAllTask";
    public JSONObject spinnerResponse, moduleResponse, sipModuleResponse, farmerResponse, fmpResponse, transactionResponse, trainingResponse, tasksResponse, answerResponse;
    public static JSONObject childResponse,farmerLeadResponse,assignmentCheckResponse, notificationResponse = new JSONObject();
    public static JSONObject nationalidresponse,enumnationalidresponse;
    public static  JSONObject multifarmRoleResponse,multifarmFarmResponse;
    public static JSONArray notificationArray = new JSONArray();
    public static JSONArray moduleArray = new JSONArray();
    private int syncResultCount;
    public ProgressDialog pd;
    private Resources res;
    String counrtyCode;
    int countrycount = 0;
    ArrayList<String> isoCodes = new ArrayList<>();
    ArrayList<String> countryIds = new ArrayList<>();
    private SyncAllTaskDelegate delegate;
    AnswerRequestDTO answerRequestDTO;
    int notificationModuleId;
    int repeaterCount ;
    public SyncAllTask(Context context) {
        super(context);
    }

    public SyncAllTask(Context context, AnswerRequestDTO answerRequestDTO) {
        super(context);
        this.answerRequestDTO = answerRequestDTO;
    }

    public void setDelegate(SyncAllTaskDelegate delegate) {
        this.delegate = delegate;
    }

    public void setProgressDialog(ProgressDialog pd) {
        this.pd = pd;
    }

    public void setResources(Resources res) {
        this.res = res;
    }

    @Override
    protected String doInBackground(SyncTaskOperationType... params) {

        SyncTaskOperationType op = params[0];

        if (op == SyncTaskOperationType.CHECK_VERSION) {
            if (isPhoneOnline()) {

                try {
                    checkVersion();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        else if (op == SyncTaskOperationType.SYNC_BASIC_DEVICE_INFO) {
          //  syncSpinners();
            syncDeviceInfo();

        } else if (op == SyncTaskOperationType.SYNC_SPINNERS) {
            syncSpinners();
        } else if (op == SyncTaskOperationType.SYNC_OD_FLDETAILS) {
            syncFLDetails();
        } else if (op == SyncTaskOperationType.SYNC_OD_TOKEN) {
            try {
                syncODRefreshToken();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (op == SyncTaskOperationType.SYNC_ENUM_ASSIGNMENT) {
           syncEnumAssignmentCheck();

        } else if (op == SyncTaskOperationType.SYNC_MODULES) {
            try {
                syncModules();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (op == SyncTaskOperationType.SYNC_SIP_MODULES) {
            try {
                syncSipModules();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (op == SyncTaskOperationType.SYNC_FARMERS) {

            try {
                syncFarmers();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (op == SyncTaskOperationType.SYNC_CHILD) {
            try {
                syncChildren();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (op == SyncTaskOperationType.SYNC_TRAINING) {
            syncTraining();

        } else if (op == SyncTaskOperationType.SYNC_TASKS) {
            syncTasks();
//            syncSpinners();
        } else if (op == SyncTaskOperationType.MULTIFARM_ENABLED_ENUMERATOR) {
            syncMultiFarmEnumerator();
         }
        else if (op == SyncTaskOperationType.SYNC_MULTIFARM_ROLES) {
            syncMultiFarmRoles();
        }
        else if (op == SyncTaskOperationType.SYNC_MULTIFARM_EXISTING_FARMS) {
            syncExistingMultiFarms();
        }
        else if (op == SyncTaskOperationType.CHILD_ENUMERATOR) {
            try {
                syncChildEnumerator();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (op == SyncTaskOperationType.SYNC_NATIONALIDS) {
            try {
                syncNationalIDs();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if (op == SyncTaskOperationType.SYNC_ENUM_NID) {
            try {
                syncEnumNationalIDs();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if (op == SyncTaskOperationType.SYNC_HHGPS) {
            try {
                syncHouseholdEnumerator();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (op == SyncTaskOperationType.SYNC_ISFL) {
            try {
                syncFarmerLeadEnumerator();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (op == SyncTaskOperationType.SYNC_SURVEYANSWERS) {
            try {
                syncSurveyAnswers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (op == SyncTaskOperationType.SYNC_SMART_NOTIFICATION) {
            try {
                String[] ids = context.getResources().getStringArray(R.array.notification_module_id);
                notificationModuleId = Integer.parseInt(ids[0]);
                syncSmartNotification(notificationModuleId,1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;

    }

    // Keycloak check version and Firebase token
    private void syncDeviceInfo() {
        String endpoint = "";
        JSONObject inputArgs = new JSONObject();
        try {
            inputArgs.put("deviceId", DeviceIDxctractor.fetchDeviceID(context));
            inputArgs.put("firebaseToken",Utilities.getFirebaseTokenSharedPrefs(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        endpoint = "ofismaster/master/saveDeviceInfo";
        final String finalEndpoint = endpoint;
        okHttpPost(context, endpoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {

                //  JSONObject response = new JSONObject(postResponse.body().string());
                //  DynatraceHelper.requestPayload(finalEndpoint,response.toString(),context,false);
                if (postResponse.isSuccessful()) {
                    // positive response
                   /* publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.download_nationalidtype_enumerator));
                    delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_NATIONALIDS);*/
                    publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_master_details));
                    delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SPINNERS);
                } else {
                    try {
                        JSONObject response = new JSONObject(postResponse.body().string());
                        postFailureMessage(response);
                    } catch (JSONException e) {
                       e.printStackTrace();
                    } catch (IOException e) {
                       e.printStackTrace();
                    }


                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }
                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }

        });
    }



    private void syncSipModules() throws JSONException {
        JSONObject inputArgs = new JSONObject();
        String enumerator_id = Utilities.getFromSharedPrefs(context);
        if (enumerator_id != null) {
            long enumeratorId = Integer.parseInt(enumerator_id);
            inputArgs.put("enumeratorId", enumeratorId);
//            inputArgs.put("enumeratorId", 125);
        }

//        String url = "http://13.76.131.132:8080/farmerdata/sipquestionnaire/getAllSIPQuestionnaireData";
        String url = "ofisfarmer-data/farmerdata/sipquestionnaire/getAllSIPQuestionnaireData";
        final String finalEndpoint = url;
        okHttpPost(context, url, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndpoint,response.toString(),context,false);

                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        if (response.getString("status").equals("unchanged")) {

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.modules_up_to_date));

                        }
                        sipModuleResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_modules));

                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_TRAINING);
                    } else {
                        postFailureMessage(response);
                    }

                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing modules", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing modules", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }

        });
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);

        SyncOperation operation = (SyncOperation) values[0];

        if (operation == SyncOperation.CONTINUE_DOWNLOADING) {
            if (pd != null && pd.isShowing()) {
                String pdUpdate = (String) values[1];
                pd = Utilities.updateProgressDialog(pd, 0, 0, pdUpdate);
            }
        } else if (operation == SyncOperation.START_SURVEYANSIMPORT) {
            int ansCount = 0;
            new UpdateSurveyAnswerTask(context).execute(answerResponse);
            try {
               JSONArray ansArray = answerResponse.getJSONArray("answers");
               ansCount = ansArray.length();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            answerResponse = null;
            if (pd != null && pd.isShowing()) {
                Utilities.closeProgressDialog(pd, 4000);
            }
            DownloadSurveyData.refreshDownloadedSurveyList(answerRequestDTO,ansCount);
        } else if (operation == SyncOperation.START_IMPORT) {
            /*if (pd != null && pd.isShowing()) {
                String pdUpdate = (String) values[1];
                pd = Utilities.updateProgressDialog(pd, 0, 0, pdUpdate);
            }
*/
            Log.e("UpdateSpinnerListsTask:","before async");
            // Goes directly to the AsyncTask
            new UpdateSpinnerListsTask(context, pd).execute(spinnerResponse);
            spinnerResponse = null;
            Log.e("UpdateSpinnerListsTask:","afetr async");

            // Goes directly to the AsyncTask
            new UpdateModuleListsTask(context).execute(moduleResponse);
            moduleResponse = null;

            // Goes directly to the AsyncTask
            new UpdateSipModuleListsTask(context).execute(sipModuleResponse);
            sipModuleResponse = null;


            new UpdateMultiFarmRoleTask(context).execute(multifarmRoleResponse);
            multifarmRoleResponse = null;

            new UpdateMultiFarmFarmsTask(context).execute(multifarmFarmResponse);
            multifarmFarmResponse = null;

            // Goes directly to the AsyncTask

            // not in this scope
//            new UpdateFmpTask(context).execute(fmpResponse);
//            fmpResponse = null;

                // Goes directly to the AsyncTask
                //new UpdateTransactionListsTask(context).execute(transactionResponse);
                //transactionResponse = null;

                // Goes directly to the AsyncTask
                new UpdateTrainingListsTask(context).execute(trainingResponse);
                trainingResponse = null;

                // Goes directly to the AsyncTask
                new UpdateTasksListsTask(context).execute(tasksResponse);
                tasksResponse = null;

            // Goes directly to the AsyncTask
            new UpdateFarmersTask(context).execute(farmerResponse);
            farmerResponse = null;

                new UpdateChildrenTask(context).execute(childResponse);
                childResponse = null;

            new UpdateFarmerLeadTask(context).execute(farmerLeadResponse);
            farmerLeadResponse = null;

            new UpdateAssignmentTask(context).execute(assignmentCheckResponse);
            assignmentCheckResponse = null;

            new UpdateNationalIdTask(context).execute(nationalidresponse);
            nationalidresponse = null;

            new UpdateEnumNationalIdTask(context).execute(enumnationalidresponse);
            enumnationalidresponse = null;

            new UpdateNotificationTask(context).execute(notificationResponse);
            notificationResponse = null;

            if(Utilities.getBoolFromSharedPrefs(context, "FIRST_RUN", true)) {
                deleteRoomDbValues();
                HomeActivity.completeFirstSetup();
            }else{
                deleteRoomDbValues();
                SettingsActivity.completeSetup();
            }

            /*if (pd != null && pd.isShowing()) {
                String pdUpdate = res.getString(R.string.update_complete);
                pd = Utilities.updateProgressDialog(pd, syncResultCount, syncResultCount, pdUpdate);
                Utilities.closeProgressDialog(pd, 1000);
            }*/


        } else if (operation == SyncOperation.ERROR_RECEIVED) {
            if (pd != null && pd.isShowing()) {
                String pdUpdate = (String) values[1];
                pd = Utilities.updateProgressDialog(pd, 0, 0, pdUpdate);
                Utilities.closeProgressDialog(pd, 4000);
            }
        } else if (operation == SyncOperation.NEW_UPDATE_AVAILABLE) {
            if (pd != null && pd.isShowing()) {
                Utilities.closeProgressDialog(pd, 0);
            }
            try {

                // new Code
                JSONObject rawResponse = new JSONObject((String) values[1]);
                String responseVal = rawResponse.getString("app_Data");
                final JSONObject response = new JSONObject(responseVal);

                //    final JSONObject response = new JSONObject((String) values[1]);
                String columnname = "responseMessage" + Utilities.getLocaleSuffix(res);
                if (columnname.equalsIgnoreCase("responseMessage_en")) {
                    columnname = "responseMessage";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(response.optString(columnname, res.getString(R.string.app_updated_generic_message)))
                        .setCancelable(true)
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.optString("downloadUrl", "https://otstgprd1001.blob.core.windows.net/mobileapp-download/OFIS-2.0-release.apk")));
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(res.getString(R.string.app_update_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                long daysLeftToUpdate = Utilities.daysToUpdate(context);
                                if (daysLeftToUpdate >= 0) {
                                    /*String reminder = res.getQuantityString(R.plurals.days_left_to_update, (int) daysLeftToUpdate, (int) daysLeftToUpdate);
                                    Toast.makeText(context, reminder, Toast.LENGTH_LONG).show();*/
                                    dialog.cancel();
                                   /* if (daysLeftToUpdate > 0) {
                                        pd = Utilities.openProgressDialog(context, res.getString(R.string.Retrieving_data), false);
                                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_MODULES);
                                    }*/
                                }
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            } catch (JSONException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
        } else {
            if (pd != null && pd.isShowing()) {
                String pdUpdate = (String) values[1];
                pd = Utilities.updateProgressDialog(pd, 0, 0, pdUpdate);
                Utilities.closeProgressDialog(pd, 4000);
            }
        }

    }

    private void deleteRoomDbValues() {
        try {
            OFISRoomDB db = OFISRoomDB.getDbInstance(context);
            db.dmSectionsItemDao().deleteAll();
            db.dmLookupValuesISubmoduleDao().deleteAll();
            db.dmActionPlansItemSubmoduleDao().deleteAll();
            db.dmQuestionsModuleDao().deleteAll();
            db.dmLookupValuesItemDao().deleteAll();
            db.dmActionPlansItemDao().deleteAll();
            db.dmMainModuleDao().deleteAll();
            db.dmAnswerTypesDao().deleteAll();
            db.dmQuestionTypesDao().deleteAll();
            db.dmCurrenciesItemDao().deleteAll();
            db.dmTextBoxModuleQuestionsDao().deleteAll();
            db.dmRangeTextBoxModuleQuestionsTableDao().deleteAll();
            db.dmTextBoxModuleQuestionsDao().deleteAll();
            db.dmRangeTextBoxModuleQuestionsTableDao().deleteAll();
            db.complianceIndicatorDao().deleteAll();
            db.dmCurrencyDao().deleteAll();
            db.dmRangeActionPlansItemsDao().deleteAll();
            db.dmSurveyAnswerTableDao().deleteAll();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void postFailureMessage(JSONObject response) {


        try {
            Log.d("Testing Message", "Post Failure-----" + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String failureMessage = context.getString(R.string.item_sync_error);
        if (response != null) {
            try {
                failureMessage = response.optString("message");
                failureMessage = response.getString("errors") + " : ";
            } catch (JSONException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
    }

    private void checkVersion() throws JSONException {

        long timestampUpdated = System.currentTimeMillis();
        Utilities.saveLongToSharedPrefs(context, "version_checked", timestampUpdated);
        JSONObject inputArgs = new JSONObject();
        inputArgs.put("enumerator_id", Long.valueOf(Utilities.getFromSharedPrefs(context)));
//        inputArgs.put("enumerator_id",Utilities.getFromSharedPrefs(context));
        inputArgs.put("current_version", BuildConfig.VERSION_NAME);

        String endPoint = "";
        // Old end point
        //   endPoint="check_version";
        // new end point
        endPoint = "ofismaster/master/checkAppVersion";

        final String finalEndPoint = endPoint;
        okHttpPost(context, endPoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    if (postResponse.code() == 401) {
                        FailureGetRefreshToken(null);
                    } else {
                        JSONObject response = new JSONObject(postResponse.body().string());
                        DynatraceHelper.requestPayload(finalEndPoint,response.toString(),context,false);
                        if (postResponse.isSuccessful()) {
                            if (response.getString("status").equals("updated")) {
                                if (Utilities.getLongFromSharedPrefs(context, "version_update_notified_on") == 0) {
                                    Calendar upgradeDate = Calendar.getInstance();
                                    long timenow = upgradeDate.getTimeInMillis();
                                    upgradeDate.add(Calendar.DATE, 31);
                                    long lastUpgradeDate = upgradeDate.getTimeInMillis();
                                    Utilities.saveLongToSharedPrefs(context, "version_update_notified_on", timenow);
                                    Utilities.saveLongToSharedPrefs(context, "upgrade_by", lastUpgradeDate);
                                }

                                // Checked Version
                                publishProgress(SyncOperation.NEW_UPDATE_AVAILABLE, response.toString());

                            } else {
                                Utilities.removeFromSharedPrefs(context, "version_update_notified_on");
                                Utilities.removeFromSharedPrefs(context, "upgrade_by");

                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.update_firebase_install_id_info));
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_BASIC_DEVICE_INFO);


                            }
                        } else {
                            try {
                                FabricLogsWriter.printCrashLog(TAG, finalEndPoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                            } catch (Exception e) {
                            }
                            if (postResponse.code() == 401) {
                                FailureGetRefreshToken(response);
                            } else {
                                postFailureMessage(response);
                            }
                        }
                    }
                } catch (JSONException | IOException e) {

                    FirebaseCrash.report(new Exception("SyncAllTask: Error checking app version", e));
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when checking app version", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }
        });
    }

    private void FailureGetRefreshToken(JSONObject response) throws IOException, JSONException {

        AuthenticationDTO authenticationDTO = new AuthenticationHelper(context).getAuthenticationDTO();
        String clientId=Utilities.getKeyCloakClientDetails(context, KeyCloakInfo.Client_Id);
        String clientSecret=Utilities.getKeyCloakClientDetails(context,KeyCloakInfo.Client_Secret);
        String grantType=context.getResources().getString(R.string.refresh_token);
        String refreshToken=authenticationDTO.getRefreshToken();

        // Auth Call
        AuthenticationInterface authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, context);
        retrofit2.Call<AuthenticationDTO> authCall = authenticationInterface.getKeyCloakRefreshToken(clientId,clientSecret,grantType,refreshToken);
        retrofit2.Response authResponse = authCall.execute();


        // DTO call

        if (authResponse.isSuccessful()) {
            AuthenticationDTO newAuthDTO = (AuthenticationDTO) authResponse.body();
//            newAuthDTO.setWillExpireAt((System.currentTimeMillis()/1000) + newAuthDTO.getExpiresIn() /1000);
            new AuthenticationHelper(context).setAuthenticationDTO(newAuthDTO);
            checkVersion();

        } else {
            postFailureMessage(response);
        }

    }

    private void syncEnumAssignmentCheck() {

        String endpoint = "";
        endpoint = "ofismaster/master/enumeratorAssignmentsCheck?enumeratorId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndpoint,response.toString(),context,false);
                    if (postResponse.isSuccessful()) {
                        try {
                            JSONArray countryArray = response.getJSONArray("assignments");
                            JSONObject item = new JSONObject();
                            int length = countryArray.length();
                            for (int j = 0; j < length; j++) {
                                item = countryArray.getJSONObject(j);
                                boolean isODOrigin = item.getBoolean("isODOrigin");
                                boolean isEnableAgroLearn = item.getBoolean("isEnableAgrolearn");
                                Utilities.saveToSharedPrefs(context, Utilities.AGROLEARN_ENUMERATOR,null, isEnableAgroLearn);
                                Utilities.saveToSharedPrefs(context, Utilities.DM_ENUMERATOR,null, item.getBoolean("isEnableDiagnosticModule"));
                                if(isODOrigin){
                                    String code = item.getString("countryIsoCode").substring(0,2);
                                    isoCodes.add(code);
                                    String countryId = item.getString("countryId");
                                    countryIds.add(countryId);
                                }

//                                Utilities.saveToSharedPrefs(context, "COUNTRY_CODE", counrtyCode.substring(0,2), null);
                            }
                            assignmentCheckResponse = response;
                            Gson gson = new Gson();
                            List<String> textList = new ArrayList<String>(isoCodes);
                            List<String> cidsList = new ArrayList<String>(countryIds);
                            String jsonText = gson.toJson(textList);
                            String cid = gson.toJson(cidsList);
                            Utilities.saveToSharedPrefs(context, "COUNTRY_CODE", jsonText, null);
                            Utilities.saveToSharedPrefs(context, "COUNTRY_IDS", cid, null);
                            Utilities.saveIntToSharedPrefs(context, "COUNTRY_COUNT", 1);
                            countrycount = 1;
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_isfl_enumerator));
                         //   publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_ISFL);

                        }catch (Exception e){e.printStackTrace();}



                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(response);

                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing training data", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing training data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncODRefreshToken() throws JSONException {

        JSONObject inputArgs = new JSONObject();
        Gson gson = new Gson();
        String jsonText = Utilities.getdatafromSharedPrefs(context, "COUNTRY_CODE");
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        isoCodes = gson.fromJson(jsonText, type);
        int countrycount = Utilities.getIntFromSharedPrefs(context, "COUNTRY_COUNT");
        String counrtyCode = isoCodes.get(countrycount-1);
        inputArgs.put("userId", counrtyCode + "_OFIS_DO_INTERFACE");
        inputArgs.put("password", "Olam123+");
        inputArgs.put("system", "od");

        String endPoint = "ofismaster/master/externalLogin";

        final String finalEndPoint = endPoint;
        okHttpPost(context, endPoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        try {
                            String accessToken= response.getString("accessToken");
                            Utilities.saveToSharedPrefs(context,"ODTOKEN",accessToken,null);
                        }catch (Exception e){}

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.downloading_fldetails));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_OD_FLDETAILS);

                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndPoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(response);
                    }

                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing spinners", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }


            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when checking app version", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }
        });
    }

    private void syncSpinners() {
        JSONObject inputArgs = new JSONObject();
        String endPoint = "";
        //old end point
        //    endPoint="sync_spinners";
        // new end point
        // endPoint="ofisfarmer-data/farmerdata/getcountryspinners/1127";
        endPoint = "ofisfarmer-data/farmerdata/getcountryspinners/" + Utilities.getFromSharedPrefs(context);
        final String finalEndPoint = endPoint;
        okHttpGet(context, endPoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndPoint,response.toString(),context,false);
                    if (postResponse.isSuccessful()) {

                        try {
                            String enumName= response.getJSONObject("outputdata").get(GPSConstants.COLUMN_ENUMERATOR_NAME).toString();
                            Utilities.setEnumeratorNameSharedPrefs(context,GPSConstants.COLUMN_ENUMERATOR_NAME,enumName);
                        }catch (Exception e){}
                        try {
                            JSONArray countryArray = response.getJSONObject("outputdata").getJSONArray("countries");
                            JSONObject item = new JSONObject();
                            int length = countryArray.length();
                            for (int j = 0; j < length; j++) {
                                item = countryArray.getJSONObject(j);
                                counrtyCode = item.getString("countryIsoCode");

                                Utilities.saveToSharedPrefs(context, "COUNTRY_CODE", counrtyCode.substring(0,2), null);
                                Utilities.saveToSharedPrefs(context, "CountryUserID", counrtyCode.substring(0,2), null);
                            }
                        }catch (Exception e){e.printStackTrace();}
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        if (response.getString("status").equals("unchanged")) {

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.spinners_up_to_date));

                        }
                        spinnerResponse = response;

                       /* publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_FARMERS);*/
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.download_nationalidtype_enumerator));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_NATIONALIDS);

                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndPoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(response);
                    }

                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing spinners", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing spinners", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }

        });
    }

    private void syncFLDetails() {
        JSONObject inputArgs = new JSONObject();
        String endPoint = "farmer/getAllFlBasicDetails";
        final String finalEndPoint = endPoint;
        okHttpODGet(context, endPoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    Gson gson = new Gson();
                    String jsonText = Utilities.getdatafromSharedPrefs(context, "COUNTRY_CODE");
                    String cids = Utilities.getdatafromSharedPrefs(context, "COUNTRY_IDS");
                    Type type = new TypeToken<ArrayList<String>>() {}.getType();
                    isoCodes = gson.fromJson(jsonText, type);
                    countryIds = gson.fromJson(cids, type);
                    int countrycount = Utilities.getIntFromSharedPrefs(context, "COUNTRY_COUNT");
                    String counrtyCode = isoCodes.get(countrycount-1);
                    String counrtyid = countryIds.get(countrycount-1);
                    JSONArray response = new JSONArray(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndPoint,response.toString(),context,false);
                    if (postResponse.isSuccessful()) {
                        int length = response.length();
                        for (int j = 0; j < length; j++) {
                            response.getJSONObject(j).put("countryIsoCode",  counrtyCode);
                            response.getJSONObject(j).put("countryId",  Integer.parseInt(counrtyid));
                        }

                        // store the value in Farmer lead data
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("farmerLeads", response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        farmerLeadResponse = jsonObject;
                        Log.e("farmerLeadResponse", String.valueOf(farmerLeadResponse));


                        if(countrycount == isoCodes.size()){
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_FARMERS);
                        }else {
                            countrycount = countrycount +1;
                            Utilities.saveIntToSharedPrefs(context, "COUNTRY_COUNT", countrycount);
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_isfl_enumerator));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_OD_TOKEN);
                        }


                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndPoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                       /* String message = postResponse.body().string();
                        if(message.contains("invalid")){
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_FARMERS);
                        }else {*/
                            JSONObject responsedata = new JSONObject(postResponse.body().string());
                            postFailureMessage(responsedata);
//                        }
                    }


                } catch (IOException | JSONException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing spinners", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing spinners", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }

        });
    }

    private void syncModules() throws JSONException {
        JSONObject inputArgs = new JSONObject();
        long enumeratorCountryId = Utilities.getLongFromSharedPrefs(context, "ENUMERATOR_COUNTRY_ID");
        if (enumeratorCountryId != 0) {
            inputArgs.put("ENUMERATOR_COUNTRY_ID", enumeratorCountryId);
        }

        String endPoint = "";
        // Old end Point :
        // endPoint= "sync_modules";
        //new End point  :
        // endPoint ="survey/survey/questionnaire/getAllQuestionnaireData/448";
        endPoint = "ofissurvey/survey/questionnaire/getAllQuestionnaireData/" + Utilities.getFromSharedPrefs(context);
        // String mUrl = "http://137.116.146.9:8080/survey/questionnaire/getAllQuestionnaireData";
        final String finalEndPoint = endPoint;
        okHttpGet(context, endPoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndPoint,response.toString(),context,false);
                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        if (response.getString("status").equals("unchanged")) {

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.modules_up_to_date));

                        }
                        moduleResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.downloading_notification));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SMART_NOTIFICATION);
                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndPoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(response);
                    }

                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing modules", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing modules", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }

        });
    }

    private void syncFMP() {
        okHttpPostPHP(context, "sync_fmps", null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        if (response.getString("status").equals("unchanged")) {

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.fmps_up_to_date));

                        }
                        fmpResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_transaction_data));

                        //delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_TRANSACTIONS);
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_TASKS);
                    } else {
                        postFailureMessage(response);
                    }

                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing FMPs", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing FMPs", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncTransactions() {
        okHttpPost(context, "sync_transactiondata", null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        int transactionResultCount = response.optInt("resultcount", 0);
                        syncResultCount = syncResultCount + transactionResultCount;
                        Utilities.saveToSharedPrefs(context, "can_transact", null, (transactionResultCount > 0));

                        transactionResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_training));

                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_TRAINING);
                    } else {
                        postFailureMessage(response);
                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing transactions", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing transactions", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncChildEnumerator() {
        String endpoint = "";
        endpoint = ChildLabourUtils.CHILD_LABOUR_ENDPOINT + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    // JSONObject response = new JSONObject(postResponse.body().string());
                    String output = postResponse.body().string();
                    DynatraceHelper.requestPayload(finalEndpoint,output,context,false);
                    if (postResponse.isSuccessful()) {
                        // store the value in Child labour preferences
                        Boolean isChild = Boolean.parseBoolean(output.trim());
                        Utilities.setChildLabourPref(context, ChildLabourUtils.IS_CHILD_ENUMERATOR, isChild);

                        if (isChild) {
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.downloading_child_data));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_CHILD);
                        } else {
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.survey_data));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_MODULES);
                        }


                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(null);
                    }
                } catch (IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing Child Enumerator data", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing training data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncHouseholdEnumerator() {
        String endpoint = "";
        endpoint = "ofismaster/master/isFarmerHHEnumerator?enumeratorId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    // JSONObject response = new JSONObject(postResponse.body().string());
                    String output = postResponse.body().string();
                    DynatraceHelper.requestPayload(finalEndpoint,output,context,false);
                    if (postResponse.isSuccessful()) {
                        // store the value in Child labour preferences
                        Boolean ishousehold = Boolean.parseBoolean(output.trim());
                        Utilities.saveToSharedPrefs(context, Utilities.HOUSEHOLD_ENUMERATOR,null, ishousehold);

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_child_enumerator));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.CHILD_ENUMERATOR);


                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(null);
                    }
                } catch (IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing Child Enumerator data", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing training data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncMultiFarmEnumerator() {
        String endpoint = "";
        endpoint = "ofisfarmer/multifarm/enableMultiFarmByEnumerator?enumeratorLoginId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    try {
                        JSONObject  response = new JSONObject(postResponse.body().string());
                        if (postResponse.isSuccessful()) {
                            boolean isMultifarm = response.getBoolean("enableMultiFarm");
                            Utilities.setMultiFarmPref(context, MultiFarmUtils.IS_MULTI_FARM_ENUMERATOR, isMultifarm);
                            if (isMultifarm) {
                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "MultiFarm Roles ");
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_MULTIFARM_ROLES);
                            }else{
                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_sip_training));
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SIP_MODULES);
                            }
                        } else {
                            try {
                                FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                            } catch (Exception e) {
                            }
                            postFailureMessage(null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        postResponse.close();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing training data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncMultiFarmRoles() {
        String endpoint = "";
        endpoint ="ofisfarmer/multifarm/roles";
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    // String output = postResponse.body().string();
                    // DynatraceHelper.requestPayload(finalEndpoint,output,context,false);
                    if (postResponse.isSuccessful()) {
                        multifarmRoleResponse=response;
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "MultiFarm Existing");
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_MULTIFARM_EXISTING_FARMS);

                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncExistingMultiFarms() {
        String endpoint = "";
        endpoint = "ofisfarmer/multifarm/farmerinfo/" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    // JSONObject response = new JSONObject(postResponse.body().string());
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {


                        multifarmFarmResponse=response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_sip_training));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SIP_MODULES);


                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(null);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }



    private void syncFarmerLeadEnumerator() {
        String endpoint = "";
        endpoint = "ofismaster/master/isFLEnumerator?enumeratorId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    String output = postResponse.body().string();
                    DynatraceHelper.requestPayload(finalEndpoint,output,context,false);
                    if (postResponse.isSuccessful()) {
                        Boolean isflenumerator = Boolean.parseBoolean(output.trim());
                        Utilities.saveToSharedPrefs(context, Utilities.FARMERLEAD_ENUMERATOR,null, isflenumerator);
                        if(! isflenumerator) {
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_FARMERS);
                        }else{
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_isfl_enumerator));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_OD_TOKEN);
                        }


                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(null);
                    }
                } catch (IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing Child Enumerator data", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing training data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncNationalIDs() {
        String endpoint = "";
        endpoint = "ofismaster/master/nationalId/getNationalIDsByEnumerator?enumeratorLoginId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndpoint,response.toString(),context,false);
                    if (postResponse.isSuccessful()) {

                        nationalidresponse = response;
                        // store the value in Child labour preferences
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.download_nationalid_enumerator));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_ENUM_NID);


                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(null);
                    }
                }catch (Exception e){ e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing training data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncEnumNationalIDs() {
        String endpoint = "";
        endpoint = "ofisfarmer/farmer/getEnumeratorFarmersUniqueId?enumeratorLoginId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndpoint,response.toString(),context,false);

                    if (postResponse.isSuccessful()) {

                        enumnationalidresponse = response;


                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_hhgps_enumerator));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_HHGPS);

                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(null);
                    }
                }catch (Exception e){ e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing training data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }


    private void syncTraining() {

        String endpoint = "";
        // Existing PHP end point
        // endpoint="sync_trainingdata";
        // New Java End point MS
        // endpoint="ofistraining/training/synctrainingdata?enumeratorLoginId=100305";
        endpoint = "ofistraining/training/synctrainingdata?enumeratorLoginId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndpoint,response.toString(),context,false);
                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");

                        trainingResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_tasks));

                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_TASKS);
                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(response);

                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing training data", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing training data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncTasks() {
        String endPoint = "";
        //old end point
        //  endPoint="sync_tasks";
        // new end point
        //   endPoint="ofistasks/activities/tasks/sync_tasks?enumeraorLoginId=100305";
        endPoint = "ofistasks/activities/tasks/sync_tasks?enumeraorLoginId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndPoint = endPoint;
        okHttpGet(context, endPoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndPoint,response.toString(),context,false);
                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");

                        tasksResponse = response;

                       /* publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_master_details));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SPINNERS);*/
                       /* publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_FARMERS);*/

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_enumerator_assignment));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_ENUM_ASSIGNMENT);
                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndPoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(response);
                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing task data", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing task data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncFarmers() throws JSONException {
        JSONObject inputArgs = new JSONObject();
        long enumeratorCountryId = Utilities.getLongFromSharedPrefs(context, "ENUMERATOR_COUNTRY_ID");
        if (enumeratorCountryId != 0) {
            inputArgs.put("ENUMERATOR_COUNTRY_ID", enumeratorCountryId);
        }

        String endpoint = "";
        endpoint = "ofisfarmer-data/farmerdata/getfarmerdetailsbyenumeratorid/" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndpoint,response.toString(),context,false);

                    if (postResponse.isSuccessful()) {
                        Utilities.isAppAlive = true;
                        syncResultCount = syncResultCount + response.getInt("resultcount");

                        if (response.getString("status").equals("unchanged")) {
                            String progress = res.getString(R.string.farmers_up_to_date);
                            publishProgress(SyncOperation.START_IMPORT, progress, syncResultCount);
                        }
                        farmerResponse = response;
                        String progress = res.getString(R.string.importing_string).replace("%%", "0").replace("||", String.valueOf(syncResultCount));
                        publishProgress(SyncOperation.START_IMPORT, progress, syncResultCount);

                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        postFailureMessage(response);
                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing farmer data", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing farmer data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncChildren() throws JSONException {
        JSONObject inputArgs = new JSONObject();
        long enumeratorCountryId = Utilities.getLongFromSharedPrefs(context, "ENUMERATOR_COUNTRY_ID");
        if (enumeratorCountryId != 0) {
            inputArgs.put("ENUMERATOR_COUNTRY_ID", enumeratorCountryId);
        }

        String endpoint = "";
        endpoint = "ofischild/child/findsubmittedmodulesbyenumeter/" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    // JSONObject response = new JSONObject(postResponse.body().string());
                    JSONArray  response = new JSONArray(postResponse.body().string());
                    DynatraceHelper.requestPayload(finalEndpoint,response.toString(),context,false);
                    if (postResponse.isSuccessful()) {
                        Utilities.isAppAlive = true;
//                        syncResultCount = syncResultCount + response.getInt("resultcount");
//
//                        if (response.getString("status").equals("unchanged")) {
//                            String progress = res.getString(R.string.farmers_up_to_date);
//
//                            publishProgress(SyncOperation.START_IMPORT, progress, syncResultCount);
//
//                        }
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("child",response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        childResponse=jsonObject;
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.survey_data));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_MODULES);
                    } else {
                        try {
                            FabricLogsWriter.printCrashLog(TAG, finalEndpoint, postResponse.code(), postResponse.networkResponse().toString(), context);
                        } catch (Exception e) {
                        }
                        //  postFailureMessage(response);
                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing farmer data", e));
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing farmer data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncSurveyAnswers(){

        String endpoint = "";
        endpoint = "ofissurvey/survey/getRecentSurveyAnswers?module_id="+Integer.parseInt(answerRequestDTO.getModule_id())
                +"&farmergroup_id="+Integer.parseInt(answerRequestDTO.getFarmerGroupId())
                +"&section_id="+Integer.parseInt(answerRequestDTO.getSectionId());
//        endpoint = "ofissurvey/survey/getRecentSurveyAnswers?module_id=3&farmergroup_id=1&section_id=659";
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
//                try {
                try {
                    JSONArray response = new JSONArray(postResponse.body().string());
                    if (postResponse.isSuccessful()) {


                        Utilities.isAppAlive = true;
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("answers", response);
                        answerResponse = jsonObject;
                        publishProgress(SyncOperation.START_SURVEYANSIMPORT, "Downloading Survey Answers", syncResultCount);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing survey answers data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }

    private void syncSmartNotification(int notificationModuleId, int pageNumber){

        String endpoint = "";
        endpoint = ChildLabourUtils.SMARTNOTIFICATION_ENDPOINT+ Utilities.getFromSharedPrefs(context)
                +ChildLabourUtils.PAGENUMBER_CONSTANT+pageNumber
                +ChildLabourUtils.LIMIT_CONSTANT+Utilities.notificationLimit
                +ChildLabourUtils.MODULE_CONSTANT + notificationModuleId;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        int totalCount = response.getInt(OFISDBContract.NotificationModuleAssignments.COLUMN_TOTALCOUNT);
                        if(pageNumber == 1) {
                            repeaterCount = totalCount / Utilities.notificationLimit;
                        }
                        try {
                            if(totalCount > 0) {
                                JSONArray currentArray = response.getJSONArray(Utilities.NOTIFICATIONLIST);
                                if (currentArray.length() > 0) {
                                    for (int i = 0; i < currentArray.length(); i++) {
                                        String moduleName = response.getString(Utilities.MODULE);
                                        if (moduleName.equalsIgnoreCase(Utilities.FDP)) {
                                            currentArray.getJSONObject(i).put(OFISDBContract.NotificationAssignments.COLUMN_MODULEID, 1);
                                        } else {
                                            currentArray.getJSONObject(i).put(OFISDBContract.NotificationAssignments.COLUMN_MODULEID, 2);
                                        }
                                        currentArray.getJSONObject(i).put(OFISDBContract.NotificationAssignments.COLUMN_STATUS, Utilities.UNREAD);
                                        notificationArray.put(currentArray.getJSONObject(i));
                                    }
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.downloading_notification));
                        if(repeaterCount > pageNumber) {
                            int nextPage = pageNumber + 1;
                            syncSmartNotification(notificationModuleId, nextPage);
                        }else{
                            try {
                                if(totalCount > 0) {
                                    JSONObject module = new JSONObject();
                                    module.put(OFISDBContract.NotificationModuleAssignments.COLUMN_TOTALCOUNT, response.getString(OFISDBContract.NotificationModuleAssignments.COLUMN_TOTALCOUNT));
                                    module.put(OFISDBContract.NotificationModuleAssignments.COLUMN_NAME_MSG, response.getString(OFISDBContract.NotificationModuleAssignments.COLUMN_NAME_MSG));
                                    String moduleName = response.getString(Utilities.MODULE);
                                    module.put(OFISDBContract.NotificationModuleAssignments.COLUMN_MODULENAME, moduleName);
                                    if (moduleName.equalsIgnoreCase(Utilities.FDP)) {
                                        module.put(OFISDBContract.NotificationModuleAssignments.COLUMN_NAME_ID, 1);
                                    } else {
                                        module.put(OFISDBContract.NotificationModuleAssignments.COLUMN_NAME_ID, 2);
                                    }
                                    moduleArray.put(module);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            String[] ids = context.getResources().getStringArray(R.array.notification_module_id);
                            String[] withoutClmrsIds = context.getResources().getStringArray(R.array.notifications_noclmrs);
                            boolean isChildEnumerator = Utilities.getChildLabourPref(context, ChildLabourUtils.IS_CHILD_ENUMERATOR);
                            int totalModules;
                            if(isChildEnumerator){
                                totalModules = ids.length;
                            }else{
                                totalModules = withoutClmrsIds.length;
                            }
                            if(totalModules > notificationModuleId){
                                int nextModule = notificationModuleId + 1;
                                syncSmartNotification(nextModule, 1);
                            }else {
                                try {
                                    notificationResponse = new JSONObject();
                                    notificationResponse.put("notificationDetailsList", notificationArray);
                                    notificationResponse.put("moduleList", moduleArray);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                notificationArray = new JSONArray();
                                moduleArray = new JSONArray();
                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Checking MultiFarm enumerator");
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.MULTIFARM_ENABLED_ENUMERATOR);
                            }
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing survey answers data", e));
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);

            }

        });
    }
}
