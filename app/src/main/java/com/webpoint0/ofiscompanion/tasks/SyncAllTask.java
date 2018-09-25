package com.webpoint0.ofiscompanion.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.webpoint0.ofiscompanion.BuildConfig;
import com.webpoint0.ofiscompanion.HomeActivity;
import com.webpoint0.ofiscompanion.R;
import com.webpoint0.ofiscompanion.Utilities;
import com.webpoint0.ofiscompanion.authentication.model.AuthenticationDTO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

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

        CONTINUE_DOWNLOADING, START_IMPORT, ERROR_RECEIVED, NEW_UPDATE_AVAILABLE
    }

    public JSONObject spinnerResponse, moduleResponse, farmerResponse, fmpResponse, transactionResponse, trainingResponse, tasksResponse;

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
        } else
            if (op == SyncTaskOperationType.SYNC_SPINNERS) {
            syncSpinners();

        } else

            if (op == SyncTaskOperationType.SYNC_MODULES) {
            try {
                syncModules();
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

        } else if (op == SyncTaskOperationType.SYNC_TRANSACTIONS) {
            //syncTransactions();

        } else if (op == SyncTaskOperationType.SYNC_TRAINING) {
            syncTraining();

        } else if (op == SyncTaskOperationType.SYNC_TASKS) {
            syncTasks();

        } else if (op == SyncTaskOperationType.SYNC_FMP) {
            syncFMP();

        }

        return null;

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
        }
        else if (operation == SyncOperation.START_IMPORT) {
            if (pd != null && pd.isShowing()) {
                String pdUpdate = (String) values[1];
                pd = Utilities.updateProgressDialog(pd, 0, 0, pdUpdate);
            }

            // Goes directly to the AsyncTask
//            new UpdateSpinnerListsTask(context).execute(spinnerResponse);
//            spinnerResponse = null;

            // Goes directly to the AsyncTask
//            new UpdateModuleListsTask(context).execute(moduleResponse);
//            moduleResponse = null;

            // Goes directly to the AsyncTask

            // not in this scope
//            new UpdateFmpTask(context).execute(fmpResponse);
//            fmpResponse = null;

            // Goes directly to the AsyncTask
            //new UpdateTransactionListsTask(context).execute(transactionResponse);
            //transactionResponse = null;

            // Goes directly to the AsyncTask
//            new UpdateTrainingListsTask(context).execute(trainingResponse);
//            trainingResponse = null;

            // Goes directly to the AsyncTask
//            new UpdateTasksListsTask(context).execute(tasksResponse);
//            tasksResponse = null;

            // Goes directly to the AsyncTask
            new UpdateFarmersTask(context).execute(farmerResponse);
            farmerResponse = null;

            if (pd != null && pd.isShowing()) {
                String pdUpdate = res.getString(R.string.update_complete);
                pd = Utilities.updateProgressDialog(pd, syncResultCount, syncResultCount, pdUpdate);
                Utilities.closeProgressDialog(pd, 1000);
            }
            if (Utilities.getBoolFromSharedPrefs(context, "FIRST_RUN", true)) {
                HomeActivity.completeFirstSetup();
            }

        }
        else if (operation == SyncOperation.ERROR_RECEIVED) {
            if (pd != null && pd.isShowing()) {
                String pdUpdate = (String) values[1];
                pd = Utilities.updateProgressDialog(pd, 0, 0, pdUpdate);
                Utilities.closeProgressDialog(pd, 4000);
            }
        }
        else if (operation == SyncOperation.NEW_UPDATE_AVAILABLE) {
            if (pd != null && pd.isShowing()) {
                Utilities.closeProgressDialog(pd, 0);
            }
            try {

                final JSONObject response = new JSONObject((String) values[1]);
                String columnname = "message" + Utilities.getLocaleSuffix(res);
                if (columnname.equalsIgnoreCase("message_en")) {
                    columnname = "message";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(response.optString(columnname, res.getString(R.string.app_updated_generic_message)))
                        .setCancelable(true)
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.optString("download_url", "https://ofis.olamgroup.com/downloads/app-release.apk")));
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(res.getString(R.string.app_update_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                long daysLeftToUpdate = Utilities.daysToUpdate(context);
                                if (daysLeftToUpdate >= 0) {
                                    String reminder = res.getQuantityString(R.plurals.days_left_to_update, (int) daysLeftToUpdate, (int) daysLeftToUpdate);
                                    Toast.makeText(context, reminder, Toast.LENGTH_LONG).show();
                                    dialog.cancel();
                                    if (daysLeftToUpdate > 0) {
                                        pd = Utilities.openProgressDialog(context, res.getString(R.string.Retrieving_data), false);
                                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SPINNERS);
                                    }
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
            Log.d("Testing Message","Post Failure-----"+response.getString("errors"));
        } catch (JSONException e) {
            e.printStackTrace();
        }





        if (response != null) {
            try {
                String failureMessage = response.getString("errors") + " : ";
                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            } catch (JSONException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
        }
    }

    private void checkVersion() throws JSONException {

        long timestampUpdated = System.currentTimeMillis();
        Utilities.saveLongToSharedPrefs(context, "version_checked", timestampUpdated);
        JSONObject inputArgs = new JSONObject();
        inputArgs.put("current_version", BuildConfig.VERSION_NAME);
        okHttpPost(context, "check_version", inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
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

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.Retrieving_data));

                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SPINNERS);

                        }
                    } else {
                        postFailureMessage(response);
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

    private void syncSpinners() {
        JSONObject inputArgs = new JSONObject();
        String endPoint="";
        //old end point
       // endPoint="sync_spinners";
        // new end point
       // endPoint="farmer-data/farmerdata/getcountryspinners/1127";
        endPoint="farmer-data/farmerdata/getcountryspinners/"+Utilities.getFromSharedPrefs(context);

        okHttpGet(context, endPoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        if (response.getString("status").equals("unchanged")) {

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.spinners_up_to_date));

                        }
                        spinnerResponse = response;

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));

                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_FARMERS);
                    } else {
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

        String endPoint="";
        // Old end Point :
        // endPoint= "sync_modules";
        //new End point  :
       // endPoint ="survey/survey/questionnaire/getAllQuestionnaireData/448";
         endPoint ="survey/survey/questionnaire/getAllQuestionnaireData/"+Utilities.getFromSharedPrefs(context);
       // String mUrl = "http://137.116.146.9:8080/survey/questionnaire/getAllQuestionnaireData";
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

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_training));

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
                        delegate.didCompleteSyncOperation(SyncAllTask.this,SyncTaskOperationType.SYNC_TASKS);
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
                        int transactionResultCount = response.optInt("resultcount",0);
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

    private void syncTraining() {

        String endpoint="";
        // Existing PHP end point
         // endpoint="sync_trainingdata";
        // New Java End point MS
       // endpoint="ofistraining/training/synctrainingdata?enumeratorLoginId=100305";
        endpoint="ofistraining/training/synctrainingdata?enumeratorLoginId="+Utilities.getFromSharedPrefs(context);
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
        String endPoint="";
        //old end point
      //  endPoint="sync_tasks";
        // new end point
     //   endPoint="ofistasks/activities/tasks/sync_tasks?enumeraorLoginId=100305";
           endPoint = "ofistasks/activities/tasks/sync_tasks?enumeraorLoginId="+Utilities.getFromSharedPrefs(context);
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

        String endpoint="";
        // Old end point
         // endpoint="farmers/listAll";
        // new endpoint
      //  endpoint = "farmer-data/farmerdata/getfarmerdetailsbyenumeratorid/125";
         endpoint = "farmer-data/farmerdata/getfarmerdetailsbyenumeratorid/"+Utilities.getFromSharedPrefs(context);
        okHttpGet(context, endpoint, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {

                        syncResultCount = syncResultCount + response.getInt("resultcount");

                        if (response.getString("status").equals("unchanged")) {
                            String progress = res.getString(R.string.farmers_up_to_date);

                            publishProgress(SyncOperation.START_IMPORT, progress, syncResultCount);

                        }
                        farmerResponse = response;

                        String progress = res.getString(R.string.importing_string).replace("%%", "0").replace("||", String.valueOf(syncResultCount));

                        publishProgress(SyncOperation.START_IMPORT, progress, syncResultCount);
                    } else {
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

}
