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
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.HomeActivity;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.authentication.AuthenticationClient;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.authentication.AuthenticationInterface;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.gps.utils.GPSConstants;
import com.olam.digital.ofis.logging.FabricLogsWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

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

        CONTINUE_DOWNLOADING, START_IMPORT, ERROR_RECEIVED, NEW_UPDATE_AVAILABLE
    }

    public static final String TAG = "SyncAllTask";
    public JSONObject spinnerResponse, moduleResponse, sipModuleResponse, farmerResponse, fmpResponse, transactionResponse, trainingResponse, tasksResponse;
    public static JSONObject childResponse;
    public static JSONObject nationalidresponse,enumnationalidresponse;
    private int syncResultCount;
    public ProgressDialog pd;
    private Resources res;
    private SyncAllTaskDelegate delegate;

    public SyncAllTask(Context context) {
        super(context);
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
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }

            }
        } else if (op == SyncTaskOperationType.SYNC_SPINNERS) {
            syncSpinners();

        } else if (op == SyncTaskOperationType.SYNC_MODULES) {
            try {
                syncModules();
            } catch (JSONException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }

        } else if (op == SyncTaskOperationType.SYNC_SIP_MODULES) {
            try {
                syncSipModules();
            } catch (JSONException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }

        } else if (op == SyncTaskOperationType.SYNC_FARMERS) {

            try {
                syncFarmers();
            } catch (JSONException e) {
                FirebaseCrash.report(e);
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

        } else if (op == SyncTaskOperationType.CHILD_ENUMERATOR) {
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

        }

        return null;

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
        String url = "farmer-data/farmerdata/sipquestionnaire/getAllSIPQuestionnaireData";
        okHttpPost(context, url, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
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
        } else if (operation == SyncOperation.START_IMPORT) {
            /*if (pd != null && pd.isShowing()) {
                String pdUpdate = (String) values[1];
                pd = Utilities.updateProgressDialog(pd, 0, 0, pdUpdate);
            }
*/
            // Goes directly to the AsyncTask
            new UpdateSpinnerListsTask(context, pd).execute(spinnerResponse);
            spinnerResponse = null;

            // Goes directly to the AsyncTask
            new UpdateModuleListsTask(context).execute(moduleResponse);
            moduleResponse = null;

            // Goes directly to the AsyncTask
            new UpdateSipModuleListsTask(context).execute(sipModuleResponse);
            sipModuleResponse = null;


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
            childResponse=null;

            new UpdateNationalIdTask(context).execute(nationalidresponse);
            nationalidresponse = null;

            new UpdateEnumNationalIdTask(context).execute(enumnationalidresponse);
            enumnationalidresponse = null;
            /*if (pd != null && pd.isShowing()) {
                String pdUpdate = res.getString(R.string.update_complete);
                pd = Utilities.updateProgressDialog(pd, syncResultCount, syncResultCount, pdUpdate);
                Utilities.closeProgressDialog(pd, 1000);
            }*/
            if (Utilities.getBoolFromSharedPrefs(context, "FIRST_RUN", true)) {
                HomeActivity.completeFirstSetup();
            }

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

    private void postFailureMessage(JSONObject response) {


        try {
            Log.d("Testing Message", "Post Failure-----" + response.getString("errors"));
        } catch (JSONException e) {
            e.printStackTrace();
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
                    if (postResponse.code() == 401 && postResponse.headers().toString().contains("invalid_token")) {
                        FailureGetRefreshToken(null);
                    } else {
                        JSONObject response = new JSONObject(postResponse.body().string());
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

                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.download_nationalidtype_enumerator));
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_NATIONALIDS);


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
        /*try {
            Log.d("Testing Message","Post Failure-----"+response.getString("errors"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String failureMessage=context.getString(R.string.item_sync_error);
        if (response != null) {
            try {
                failureMessage = response.optString("message");
                failureMessage = response.getString("errors") + " : " ;
            } catch (JSONException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
        }*/
        AuthenticationDTO authenticationDTO = new AuthenticationHelper(context).getAuthenticationDTO();
        //
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setGrantType("refreshToken");
        authRequestDTO.setToken(authenticationDTO.getRefreshToken());
        AuthenticationInterface authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, context);
        retrofit2.Call<AuthenticationDTO> authCall = authenticationInterface.getRefreshToken(authRequestDTO);
        retrofit2.Response authResponse = authCall.execute();
        if (authResponse.isSuccessful()) {
            AuthenticationDTO newAuthDTO = (AuthenticationDTO) authResponse.body();
//            newAuthDTO.setWillExpireAt((System.currentTimeMillis()/1000) + newAuthDTO.getExpiresIn() /1000);
            new AuthenticationHelper(context).setAuthenticationDTO(newAuthDTO);
            checkVersion();

        } else {
            postFailureMessage(response);
        }

    }

    private void syncSpinners() {
        JSONObject inputArgs = new JSONObject();
        String endPoint = "";
        //old end point
        //    endPoint="sync_spinners";
        // new end point
        // endPoint="farmer-data/farmerdata/getcountryspinners/1127";
        endPoint = "farmer-data/farmerdata/getcountryspinners/" + Utilities.getFromSharedPrefs(context);
        final String finalEndPoint = endPoint;
        okHttpGet(context, endPoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {

                        try {
                            String enumName= response.getJSONObject("outputdata").get(GPSConstants.COLUMN_ENUMERATOR_NAME).toString();
                            Utilities.setEnumeratorNameSharedPrefs(context,GPSConstants.COLUMN_ENUMERATOR_NAME,enumName);
                        }catch (Exception e){}


                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        if (response.getString("status").equals("unchanged")) {

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.spinners_up_to_date));

                        }
                        spinnerResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));

                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_FARMERS);
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
                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        if (response.getString("status").equals("unchanged")) {

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.modules_up_to_date));

                        }
                        moduleResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_sip_training));

                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SIP_MODULES);
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

    private void syncNationalIDs() {
        String endpoint = "";
        endpoint = "ofismaster/master/nationalId/getNationalIDsByEnumerator?enumeratorLoginId=" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, null, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
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

                    if (postResponse.isSuccessful()) {

                        enumnationalidresponse = response;
                        // store the value in Child labour preferences
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
                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");

                        tasksResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_master_details));

                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SPINNERS);
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
        endpoint = "farmer-data/farmerdata/getfarmerdetailsbyenumeratorid/" + Utilities.getFromSharedPrefs(context);
        final String finalEndpoint = endpoint;
        okHttpGet(context, endpoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
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
                        //                   http://www.mocky.io/v2/5d619ffb32000059008e6134
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
}
