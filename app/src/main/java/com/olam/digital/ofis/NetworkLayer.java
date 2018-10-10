package com.olam.digital.ofis;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.olam.digital.ofis.authentication.AccessTokenTask;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.model.FarmerRegistrationDTO;
import com.olam.digital.ofis.model.FarmerRegistrationPhoneNumberDTO;
import com.olam.digital.ofis.recordpolygon.RecordPolygonActivity;
import com.olam.digital.ofis.tasks.SyncAllTask;
import com.olam.digital.ofis.tasks.SyncTaskOperationType;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.olam.digital.ofis.R;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by bengallienne on 29/11/14.
 * All network functions carried out by the application
 */
public class NetworkLayer implements SyncAllTask.SyncAllTaskDelegate {
    private final Context context;
    private final Resources res;
    private static String AccessToken;
    private static final String BASE_URL = BuildConfig.HOST;
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(getSSLSocketFactory())
            .build();
    private static final MediaType JSONBody = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
    private static final MediaType MEDIA_TYPE_SQLITE = MediaType.parse("application/x-sqlite3");
    private final DataLayer dl;
    private Cursor networkQueueItems;
    //private Handler handler;
    private ProgressDialog pd = null;

    private static SSLSocketFactory getSSLSocketFactory() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return sslSocketFactory;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }

    }
    // constructor
    public NetworkLayer(Context context_, Resources res_) {
        context = context_;
        res = res_;
        this.dl = new DataLayer(res, context);
    }

    boolean isPhoneOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Array Object created
    private static void okHttpArrayPost(Context context, String url, JSONArray params, Callback callback) {
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);

        RequestBody body = RequestBody.create(JSONBody, params.toString());

        AccessToken = NetworkLayer.getAccessTokenIsAlive(context);

        Log.i("fullUrlAccessToken", AccessToken + "");

        Request request = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id","1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(callback);
    }



    private static void okHttpPost(Context context, String url, JSONObject params, Callback callback) {
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);

        RequestBody body = RequestBody.create(JSONBody, params.toString());

        Request request = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id","1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(callback);
    }

    private static void okHttpPut(Context context, String url, JSONObject params, Callback callback) {
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);

        RequestBody body = RequestBody.create(JSONBody, params.toString());

        Request request = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id","1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl)
                .put(body)
                .build();

        okHttpClient.newCall(request).enqueue(callback);
    }

    private static void okHttpMultiPartPost(Context context, String url, RequestBody requestBody, Callback callback) {
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);
        AccessToken = NetworkLayer.getAccessTokenIsAlive(context);
        Request request = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id", "1")
                .header("AccessToken", AccessToken)
                .url(fullUrl)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(callback);
    }


    private static void okHttpMSPost(Context context, String url, JSONObject params, Callback callback) {
     //   String fullUrl = getMSAbsoluteUrl(url);
        String fullUrl=getAbsoluteUrl(url);
        RequestBody body = RequestBody.create(JSONBody, params.toString());
        Request request = new Request.Builder()
                .header("App-Id", "1")
                .header("Doc-Type","photo")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl)
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    private void recordFailedUpload(long server_item_id, long fully_synced, long networkQueueItemId) {
        try {
            ContentValues failureValues = new ContentValues();
            failureValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, server_item_id);
            failureValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED, fully_synced);
            failureValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)) + 1);
            dl.updateNetworkQueueEntry(failureValues, networkQueueItemId);
        }catch (Exception e){

        }

    }

    private void recordFailedMobileMoneyUpload(long server_item_id, long fully_synced, long transactionItemId) {
        Cursor transactionItem = dl.getTransactionItemById(transactionItemId);
        int uploadAttemptsSoFar = 0;
        if (transactionItem.moveToFirst()) {
            uploadAttemptsSoFar = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ATTEMPTS));
        }
        ContentValues failureValues = new ContentValues();
        failureValues.put(OFISDBContract.TransactionItem.COLUMN_NAME_SERVERITEM_ID, server_item_id);
        failureValues.put(OFISDBContract.TransactionItem.COLUMN_NAME_FULLY_SYNCED, fully_synced);
        failureValues.put(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ATTEMPTS, uploadAttemptsSoFar + 1);
        dl.updateTransactionItem(failureValues, transactionItemId);
    }

    private void postFailureMessage(Handler handler, JSONObject response, Message msg, int msgWhat, int total, int progress, String baseMessage, String failedOperation) {
        String failureMessage = res.getString(R.string.item_sync_error);
        if (baseMessage != null) {
            failureMessage = baseMessage;
        }
        if (response != null) {
            failureMessage = response.optString("errors", res.getString(R.string.item_sync_error));
        }
        msg.what = msgWhat;
        msg.arg1 = total;
        msg.arg2 = progress;
        msg.obj = failureMessage;
        handler.dispatchMessage(msg);
    }

    void postFirebaseToken(final String token) {
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        if (enumeratorId.length() > 0) {
            JSONObject tokenObj = new JSONObject();
            try {
                tokenObj.put("firebasetoken", token);

                okHttpPost(context, "enumerators/updateFBtoken", tokenObj, new Callback() {
                    @Override
                    public void onResponse(Call call, final Response response) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (response.isSuccessful()) {
                                    Toast.makeText(context, res.getString(R.string.registeredForNotifications), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, res.getString(R.string.registerForNotificationsFailed), Toast.LENGTH_LONG).show();
                                    FirebaseCrash.report(new Throwable(res.getString(R.string.registerForNotificationsFailed)));
                                }
                            }
                        });
                        response.close();
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        FirebaseCrash.report(new Exception("NetworkLayer: Error in firebase token response", e));
                    }
                });
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("NetworkLayer: Error in posting firebase token", e));
                e.printStackTrace();
            }
        }
    }

    void syncTasks(final String syncedOrDelivered) {
        final Cursor tasks = (syncedOrDelivered.equalsIgnoreCase(OFISDBContract.Task.COLUMN_NAME_SYNCED)) ? dl.getCompletedUnSyncedTasks() : dl.getNewlyDeliveredTasks();
        JSONObject uploadSyncTask = new JSONObject();
        if (tasks.getCount() > 0) {
            try {
                JSONObject tasksPayload = new JSONObject();
                JSONArray tasksItems = new JSONArray();

                while (tasks.moveToNext()) {
                    JSONObject taskItem = new JSONObject();
//                    taskItem.put("task_id", tasks.getInt(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_ID)));
//                    if (syncedOrDelivered.equalsIgnoreCase(OFISDBContract.Task.COLUMN_NAME_SYNCED)) {
//                        taskItem.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED, tasks.getInt(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_COMPLETED)));
//                        taskItem.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE, tasks.getString(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE)));
//                    }

                    taskItem.put("id", tasks.getInt(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_ID)));
                    if (syncedOrDelivered.equalsIgnoreCase(OFISDBContract.Task.COLUMN_NAME_SYNCED)) {
                        taskItem.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED, tasks.getInt(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_COMPLETED)));
                        taskItem.put(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE, tasks.getString(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE)));
                    }

                    taskItem.put(OFISDBContract.Task.COLUMN_NAME_TASKTEXT, tasks.getString(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_TASKTEXT)));
                    taskItem.put(OFISDBContract.Task.COLUMN_NAME_ENUMERATORS_TASK_ID, tasks.getInt(tasks.getColumnIndex(OFISDBContract.Task.COLUMN_NAME_ENUMERATORS_TASK_ID)));

                    tasksItems.put(taskItem);
                }

                tasksPayload.put("tasks", tasksItems);

                uploadSyncTask.put("status","updated");
                uploadSyncTask.put("outputdata",tasksPayload);
                uploadSyncTask.put("resultcount",tasksItems.length());

                String endPoint="";
                // old endpoint
                // endPoint="tasks/sync_delivered_completed";
                //new end point
                endPoint="ofistasks/activities/tasks/sync_tasks/updates";

                okHttpPut(context, endPoint, uploadSyncTask, new Callback() {
                    @Override
                    public void onResponse(Call call, final Response postResponse) {
                        try {
                            JSONObject response = new JSONObject(postResponse.body().string());
                            JSONObject outputdata = response.optJSONObject("outputdata");
                            if (outputdata != null) {
                                //JSONArray taskIds = outputdata.optJSONArray("task_ids");
                                JSONArray taskIds = outputdata.optJSONArray("tasks");
                                if (taskIds != null && taskIds.length() > 0) {
                                    dl.markTasksAsSyncedOrDelivered(taskIds, syncedOrDelivered);
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
                        FirebaseCrash.report(new Exception("NetworkLayer: Error in syncing tasks response", e));
                    }

                });
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("NetworkLayer: Error posting sync tasks request", e));
                e.printStackTrace();
            }
        }
    }

    void sendForHelp(String helpName, String helpDescription, final Handler handler) {
        String dbName = "ofismobile";
        File dbFile = context.getDatabasePath(dbName);
        RequestBody file = RequestBody.create(MEDIA_TYPE_SQLITE, dbFile);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        RequestBody requestBody;

        final Message msg = handler.obtainMessage();
        String endpoint = "ofismaster/master/requestHelp";

        bodyBuilder.addFormDataPart("username", helpName);
        bodyBuilder.addFormDataPart("faultDescription", helpDescription);
        bodyBuilder.addFormDataPart("SQLiteDb", dbFile.getName(), file);
        bodyBuilder.addFormDataPart("enumeratorId", Utilities.getFromSharedPrefs(context));
        bodyBuilder.addFormDataPart("appVersion", BuildConfig.VERSION_NAME);
        //bodyBuilder.addFormDataPart("zippedDebugLog", dbFile.getName(), file);
        // Get length of file in bytes
        long fileSizeInBytes = dbFile.length();
// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;






        requestBody = bodyBuilder.build();

        okHttpMultiPartPost(context, endpoint, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        msg.what = 1;
                        msg.obj = response;
                        handler.dispatchMessage(msg);
                    } else {
                        postFailureMessage(handler, response, msg, 2, 0, 0, null, "submitting help request");
                        FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting help request failed response"));
                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("NetworkLayer: Error in help request response", e));
                    e.printStackTrace();
                }
                postResponse.close();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                if (e != null) {
                    failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                }
                postFailureMessage(handler, null, msg, 2, 0, 0, failureMessage, "submitting help request");
                FirebaseCrash.report(new Exception(failureMessage, e));
            }

        });

    }

    public void getNetworkQueue(Integer itemId) {
        if (networkQueueItems != null && networkQueueItems.getCount() > 0) {
            networkQueueItems.close();
        }
        networkQueueItems = dl.getNetworkQueueItem(itemId);
    }

    void syncNetworkQueue(Integer itemId, Handler handler) {
        if (networkQueueItems.getCount() > 0 && !networkQueueItems.isAfterLast()) {
            if (networkQueueItems.moveToNext()) {
                syncNetworkQueueItem(itemId, handler);
            } else {
                Message msg = handler.obtainMessage(2, networkQueueItems.getCount(), networkQueueItems.getPosition(), res.getString(R.string.item_synced));
                handler.dispatchMessage(msg);
            }
        } else {
            Message msg = handler.obtainMessage(2, networkQueueItems.getCount(), networkQueueItems.getPosition(), res.getString(R.string.item_synced));
            handler.dispatchMessage(msg);
        }
    }

    private void syncNetworkQueueItem(Integer itemId, Handler handler) {
        Message msg = handler.obtainMessage();
        int total = networkQueueItems.getCount();
        int progress = networkQueueItems.getPosition() + 1;
        String itemType = networkQueueItems
                .getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE));
        int networkQueueItemId = networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID));

        if (itemType.equalsIgnoreCase("infrastructure")) {
            postInfrastructure(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("farmer")) {
            //postFarmer(networkQueueItemId, handler, msg, total, progress, itemId);
            postFarmerJava(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("farmlocation")) {
            postFarmLocation(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("farmpolygon")) {
            postFarmPolygon(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("module")) {
            postModule(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("fmpreview")) {
            postFmpReview(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("fullfmpreview")) {
            postFullFmpReview(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("transaction")) {
            //postTransaction(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("trainingrecord")) {
            //     syncTasks(OFISDBContract.Task.COLUMN_NAME_SYNCED);
            postTrainingRecord(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("phonenumbers")) {
            postPhoneNumbers(networkQueueItemId, handler, msg, total, progress, itemId);
        } else if (itemType.equalsIgnoreCase("farmer_photo")) {
            //JIRA req OW-505
            postFarmerPhoto(networkQueueItemId, handler, msg, total, progress, itemId);
        }
    }

    void initiateMobileMoneyTransaction(final Handler handler, final long transactionItemId, final long farmerId) {
        // create json objects
        final Message msg = handler.obtainMessage();
        final JSONObject transactionItem = new JSONObject();

        Cursor transactionItemRecord = dl.getTransactionItemById(transactionItemId);

        if (transactionItemRecord.getCount() > 0) {
            transactionItemRecord.moveToFirst();
            final String transacteeType = transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE));

            if (transacteeType.equalsIgnoreCase("farmer") && !dl.isFarmerSynced(farmerId)) {
                msg.what = 3;
                msg.obj = String.valueOf(res.getString(R.string.farmer_not_synced));
                handler.dispatchMessage(msg);
            } else if (!transacteeType.equalsIgnoreCase("farmer")) {
                msg.what = 3;
                msg.obj = String.valueOf(res.getString(R.string.cannot_do_mobile_money_non_farmer));
                handler.dispatchMessage(msg);
            } else {
                try {
                    transactionItem.put("buyingagent_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_BUYINGAGENT_ID)));
                    transactionItem.put("transactee_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID)));
                    transactionItem.put("transactee_type", transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE)));
                    transactionItem.put("transaction_date", transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_DATE)));
                    transactionItem.put("transactionitemgroup_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTIONITEMGROUP_ID)));
                    transactionItem.put("transactionitemtype_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTIONITEMTYPE_ID)));
                    transactionItem.put("direction_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_DIRECTION_ID)));
                    transactionItem.put("unit_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_UNIT_ID)));
                    transactionItem.put("product_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRODUCT_ID)));
                    transactionItem.put("productgrade_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRODUCT_GRADE_ID)));
                    transactionItem.put("currency_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_CURRENCY_ID)));
                    transactionItem.put("quantity", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_QUANTITY)));
                    transactionItem.put("price_per_unit", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRICE_PER_UNIT)));
                    transactionItem.put("total_price", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TOTAL_PRICE)));
                    transactionItem.put("amount_mobilemoney", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_MOBILEMONEY)));
                    transactionItem.put("amount_paid", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_PAID)));
                    transactionItem.put("amount_loaned", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_LOANED)));
                    transactionItem.put("amount_gifted", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_GIFTED)));
                    transactionItem.put("transaction_notes", transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_NOTES)));
                    transactionItem.put("transaction_status", transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_STATUS)));
                    transactionItem.put("signal_strength", transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_SIGNAL_STRENGTH)));
                    String diallingCode = transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_DIALLINGCODE));
                    String phoneNumber = transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_NUMBER));
                    transactionItem.put("phone_number", diallingCode + phoneNumber);
                    transactionItem.put(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ATTEMPTS, transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ATTEMPTS)));
                    transactionItem.put(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ID, transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ID)));
                    if (transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_SERVERITEM_ID)) > 0 && transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                        transactionItem.put(OFISDBContract.TransactionItem.COLUMN_NAME_SERVERITEM_ID, transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_SERVERITEM_ID)));
                    }

                    okHttpPost(context, "mobilemoney/submit", transactionItem, new Callback() {
                        @Override
                        public void onResponse(Call call, Response postResponse) {
                            String statusMessage;
                            if (postResponse.isSuccessful()) {
                                JSONObject response = null;
                                try {
                                    response = new JSONObject(postResponse.body().string());
                                    ContentValues trValues = new ContentValues();
                                    if (response.optString("mmpp_requestid", "").length() > 0) {
                                        trValues.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_REQUEST_ID, response.getString("mmpp_requestid"));
                                    }
                                    if (response.optString("mmpp_transactionid", "").length() > 0) {
                                        trValues.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_TRANSACTION_ID, response.getString("mmpp_transactionid"));
                                    }
                                    if (response.optString("transaction_status", "").length() > 0) {
                                        trValues.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_STATUS, response.getString("transaction_status"));
                                    }
                                    dl.updateTransactionItem(trValues, transactionItemId);
                                    startMobileMoneyTimer(response);

                                } catch (JSONException | IOException e) {
                                    FirebaseCrash.report(new Exception("NetworkLayer: Error in mobile money transaction post response", e));
                                    e.printStackTrace();
                                }
                                statusMessage = res.getString(R.string.mm_transaction_initiated);
                                msg.what = 1;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                            } else {
                                try {
                                    JSONObject response = new JSONObject(postResponse.body().string());
                                    long server_item_id = 0, fully_synced = 0;
                                    if (response.optJSONObject("transactionitem") != null) {
                                        JSONObject transactionitem = response.getJSONObject("transactionitem");
                                        server_item_id = transactionitem.getLong("id");
                                        fully_synced = transactionitem.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                    }
                                    recordFailedMobileMoneyUpload(server_item_id, fully_synced, transactionItemId);
                                    postFailureMessage(handler, response, msg, 2, 0, 0, null, "submitting transaction");
                                    FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting mobile money transaction"));
                                } catch (JSONException | IOException e) {
                                    FirebaseCrash.report(new Exception("NetworkLayer: Error getting mobile money transaction post failure response", e));
                                    e.printStackTrace();
                                }
                            }
                            postResponse.close();
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            recordFailedMobileMoneyUpload(0, 0, transactionItemId);
                            String failureMessage = res.getString(R.string.networking_error);
                            if (e != null) {
                                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                            }
                            postFailureMessage(handler, null, msg, 2, 0, 0, failureMessage, "submitting mobile money transaction");
                            FirebaseCrash.report(new Exception(failureMessage, e));
                        }

                    });

                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("NetworkLayer: Error creating transaction post object", e));
                    e.printStackTrace();
                }
            }
        }
    }

    private void startMobileMoneyTimer(JSONObject response) {
        response.remove("transaction_status");
        response.remove("error");
        response.remove("mmpp_message");

        okHttpPost(context, "mobilemoney/canceltimer", response, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    void checkMobileMoneyTransaction(final Handler handler, final long transactionItemId) {
        final Message msg = handler.obtainMessage();
        // create json objects
        JSONObject transactionItem = new JSONObject();
        Cursor transactionItemRecord = dl.getTransactionItemById(transactionItemId);

        if (transactionItemRecord.getCount() > 0) {
            transactionItemRecord.moveToFirst();

            try {
                transactionItem.put(OFISDBContract.TransactionItem.COLUMN_NAME_ID, transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_ID)));
                transactionItem.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_TRANSACTION_ID, transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_TRANSACTION_ID)));
                transactionItem.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_REQUEST_ID, transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_REQUEST_ID)));

                okHttpPost(context, "mobilemoney/check", transactionItem, new Callback() {
                    @Override
                    public void onResponse(Call call, Response postResponse) {
                        String statusMessage;
                        if (postResponse.isSuccessful()) {
                            JSONObject response = null;
                            msg.what = 4;
                            statusMessage = res.getString(R.string.mm_transaction_awaiting_response);
                            try {
                                response = new JSONObject(postResponse.body().string());
                                String transaction_status = response.optString("transaction_status", "");
                                if (transaction_status.equalsIgnoreCase("initiated") || transaction_status.equalsIgnoreCase("confirmed") || transaction_status.equalsIgnoreCase("pending")) {
                                    msg.what = 4;
                                    statusMessage = res.getString(R.string.mm_transaction_inprogress);
                                }
                                if (transaction_status.equalsIgnoreCase("succeeded")) {
                                    msg.what = 5;
                                    statusMessage = res.getString(R.string.mm_transaction_complete);
                                }
                                if (transaction_status.equalsIgnoreCase("failed")) {
                                    msg.what = 2;
                                    statusMessage = res.getString(R.string.mm_transaction_failed);
                                }
                                if (transaction_status.length() > 0) {
                                    ContentValues values = new ContentValues();
                                    values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_STATUS, transaction_status);
                                    dl.updateTransactionItem(values, transactionItemId);
                                }
                            } catch (JSONException | IOException e) {
                                FirebaseCrash.report(new Exception("NetworkLayer: Error in mobile money transaction check response", e));
                                e.printStackTrace();
                            }
                            msg.obj = statusMessage;
                            handler.dispatchMessage(msg);
                        } else {
                            try {
                                JSONObject response = new JSONObject(postResponse.body().string());
                                ContentValues values = new ContentValues();
                                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_STATUS, "failed");
                                dl.updateTransactionItem(values, transactionItemId);
                                postFailureMessage(handler, response, msg, 2, 0, 0, null, "submitting mobile money transaction");
                                FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : checking mobile money transaction"));
                            } catch (JSONException | IOException e) {
                                FirebaseCrash.report(new Exception("NetworkLayer: Error getting mobile money transaction check failure response", e));
                                e.printStackTrace();
                            }
                        }
                        postResponse.close();
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        String failureMessage = res.getString(R.string.networking_error);
                        if (e != null) {
                            failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                        }
                        postFailureMessage(handler, null, msg, 2, 0, 0, failureMessage, "submitting mobile money transaction");
                        FirebaseCrash.report(new Exception(failureMessage, e));
                    }

                });

            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("NetworkLayer: Error creating transaction check object", e));
                e.printStackTrace();
            }
        }
    }

    private void postTrainingRecord(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        RequestBody requestBody = null;

        final long trainingRecordId = networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRAINING_RECORD_ID));
        Cursor trainingRecord = dl.getTrainingRecordById(trainingRecordId, "");

        String endpoint="";
        // old end point
       //  endpoint = "training/submit";
        // new End point
        endpoint = "ofistraining/trainingrecords/create";

        if (trainingRecord.getCount() > 0) {
            trainingRecord.moveToFirst();
            JSONObject jsonObject=new JSONObject();

            // new code
            try {
                jsonObject.put(OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID, trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID)));
                jsonObject.put(OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME, trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME)));
                jsonObject.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
                jsonObject.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));

                jsonObject.put(OFISDBContract.TrainingRecord.COLUMN_NAME_COURSE_ID, trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_COURSE_ID)));

               // jsonObject.put("enumeratorId",100305);
                jsonObject.put("enumeratorId",Utilities.getFromSharedPrefs(context));

                if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                    jsonObject.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
                }
            }catch (Exception e){

                e.printStackTrace();
            }


            // Old code
//            bodyBuilder.addFormDataPart(OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID, trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID)));
//            bodyBuilder.addFormDataPart(OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME, trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME)));
//            bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
//            bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
//
//            if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
//                bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
//            }


            // Sending pic in json format in base 64
            try{
                String trainingPhotoPath = trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO));
                if (trainingPhotoPath != null && trainingPhotoPath.length() > 0) {
                    File photo = new File(trainingRecord.getString(trainingRecord.getColumnIndex(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO)));

 //                   RequestBody file = RequestBody.create(MEDIA_TYPE_JPG, photo);
//                    bodyBuilder.addFormDataPart(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO, photo.getName(), file);
  //                  String encodedImage=getEncodedImage(trainingPhotoPath);
                    jsonObject.put(OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO, photo.getName());
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            Cursor trainingAttendees = dl.getTrainingAttendeesByTrainingRecordId(trainingRecordId);
            if (trainingAttendees.getCount() > 0) {
                // create json objects
                JSONArray attendeesJSON = new JSONArray();

                while (trainingAttendees.moveToNext()) {
                    long farmerId = trainingAttendees.getLong(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID));
                    if (!dl.isFarmerSynced(farmerId)) {
                        msg.what = 3;
                        msg.arg1 = total;
                        msg.arg2 = progress;
                        msg.obj = String.valueOf(res.getString(R.string.farmer_not_synced));
                        handler.dispatchMessage(msg);
                        return;
                    } else {
                        try {
                            JSONObject attendee = new JSONObject();
                            attendee.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID, trainingAttendees.getInt(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID)));
                            attendee.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_ATTENDED, trainingAttendees.getInt(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_ATTENDED)));
                            attendee.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_PASSED, trainingAttendees.getInt(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_PASSED)));
                            attendee.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_PRESCORE, trainingAttendees.getString(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_PRESCORE)));
                            attendee.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_POSTSCORE, trainingAttendees.getString(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_POSTSCORE)));
                            attendee.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_SCORE, trainingAttendees.getString(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_SCORE)));
                            attendee.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_PERCENTAGE, trainingAttendees.getString(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_PERCENTAGE)));
                            attendee.put(OFISDBContract.TrainingAttendee.COLUMN_NAME_GRADE, trainingAttendees.getString(trainingAttendees.getColumnIndex(OFISDBContract.TrainingAttendee.COLUMN_NAME_GRADE)));

                            attendeesJSON.put(attendee);
                        } catch (JSONException e) {
                            FirebaseCrash.report(new Exception("NetworkLayer: Error creating training record attendees object", e));
                            e.printStackTrace();
                        }
                    }
                }

      //          bodyBuilder.addFormDataPart("training_attendees", attendeesJSON.toString());
                try {
                    jsonObject.put("training_attendees", attendeesJSON);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            if (endpoint.length() > 0) {
                okHttpPost(context, endpoint, jsonObject, new Callback() {
                    @Override
                    public void onResponse(Call call, Response postResponse) {
                        String statusMessage;
                        if (postResponse.isSuccessful()) {
                            try {
                                JSONObject response = new JSONObject(postResponse.body().string());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                            }
                            dl.deleteNetworkQueueItem(networkQueueItemId);
                            dl.deleteTrainingRecord(trainingRecordId);
                            statusMessage = res.getString(R.string.item_synced);
                            msg.what = 1;
                            msg.arg1 = total;
                            msg.arg2 = progress;
                            msg.obj = statusMessage;
                            handler.dispatchMessage(msg);
                            syncNetworkQueue(originalSyncRequestItemId, handler);
                        } else {
                            try {
                                JSONObject response = new JSONObject(postResponse.body().string());
                                long server_item_id = 0, fully_synced = 0;
                                if (response.optJSONObject("trainingrecord") != null) {
                                    JSONObject trainingrecord = response.getJSONObject("trainingrecord");
                                    server_item_id = trainingrecord.getLong("id");
                                    fully_synced = trainingrecord.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                }
                                recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting training record");
                                FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting training record failure response"));
                            } catch (JSONException | IOException e) {
                                recordFailedUpload(0, 0, networkQueueItemId);
                                postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting training record");
                                FirebaseCrash.report(new Exception("NetworkLayer: Error getting training record response", e));
                                e.printStackTrace();
                            }
                        }
                        postResponse.close();
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        recordFailedUpload(0, 0, networkQueueItemId);
                        String failureMessage = res.getString(R.string.networking_error);
                        if (e != null) {
                            failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                        }
                        postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting training record");
                        FirebaseCrash.report(new Exception(failureMessage, e));
                    }

                });
            }
        }
    }

    private void postTransaction(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {
        // create json objects
        JSONObject transactionItem = new JSONObject();

        final int transactionItemId = networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRANSACTION_ITEM_ID));
        final long farmerId = networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));

        Cursor transactionItemRecord = dl.getTransactionItemById(transactionItemId);

        if (transactionItemRecord.getCount() > 0) {
            transactionItemRecord.moveToFirst();
            final String transacteeType = transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE));
            final long transactionBalanceId = transactionItemRecord.getLong(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_BALANCE_ID));

            if (transacteeType.equalsIgnoreCase("farmer") && !dl.isFarmerSynced(farmerId)) {
                msg.what = 3;
                msg.arg1 = total;
                msg.arg2 = progress;
                msg.obj = String.valueOf(res.getString(R.string.farmer_not_synced));
                handler.dispatchMessage(msg);
            } else {
                try {
                    transactionItem.put("buyingagent_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_BUYINGAGENT_ID)));
                    transactionItem.put("transactee_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID)));
                    transactionItem.put("transactee_type", transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE)));
                    transactionItem.put("transaction_date", transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_DATE)));
                    transactionItem.put("transactionitemgroup_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTIONITEMGROUP_ID)));
                    transactionItem.put("transactionitemtype_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTIONITEMTYPE_ID)));
                    transactionItem.put("direction_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_DIRECTION_ID)));
                    transactionItem.put("unit_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_UNIT_ID)));
                    transactionItem.put("product_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRODUCT_ID)));
                    transactionItem.put("productgrade_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRODUCT_GRADE_ID)));
                    transactionItem.put("currency_id", transactionItemRecord.getInt(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_CURRENCY_ID)));
                    transactionItem.put("quantity", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_QUANTITY)));
                    transactionItem.put("price_per_unit", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRICE_PER_UNIT)));
                    transactionItem.put("total_price", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TOTAL_PRICE)));
                    transactionItem.put("amount_paid", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_PAID)));
                    transactionItem.put("amount_loaned", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_LOANED)));
                    transactionItem.put("amount_gifted", transactionItemRecord.getDouble(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_GIFTED)));
                    transactionItem.put("transaction_notes", transactionItemRecord.getString(transactionItemRecord.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_NOTES)));
                    transactionItem.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
                    transactionItem.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
                    if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                        transactionItem.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
                    }

                    okHttpPost(context, "transactions/submit", transactionItem, new Callback() {
                        @Override
                        public void onResponse(Call call, Response postResponse) {
                            String statusMessage;
                            if (postResponse.isSuccessful()) {
                                JSONObject response = null;
                                String newBalance = "0.00";
                                try {
                                    response = new JSONObject(postResponse.body().string());
                                    newBalance = response.optString("new_balance");
                                } catch (JSONException | IOException e) {
                                    FirebaseCrash.report(new Exception("NetworkLayer: Error in transaction post response", e));
                                    e.printStackTrace();
                                }
                                dl.deleteNetworkQueueItem(networkQueueItemId);
                                dl.deleteTransactionItem(transactionItemId, true);
                                if (transactionBalanceId > 0) {
                                    dl.updateTransactionBalance(transactionBalanceId, newBalance, 0, 0, null, 0, false, false, true, false);
                                }
                                statusMessage = res.getString(R.string.item_synced);
                                msg.what = 1;
                                msg.arg1 = total;
                                msg.arg2 = progress;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                                syncNetworkQueue(originalSyncRequestItemId, handler);
                            } else {
                                try {
                                    JSONObject response = new JSONObject(postResponse.body().string());
                                    long server_item_id = 0, fully_synced = 0;
                                    if (response.optJSONObject("transactionitem") != null) {
                                        JSONObject transactionitem = response.getJSONObject("transactionitem");
                                        server_item_id = transactionitem.getLong("id");
                                        fully_synced = transactionitem.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                    }
                                    recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                    postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting transaction");
                                    FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting transaction"));
                                } catch (JSONException | IOException e) {
                                    FirebaseCrash.report(new Exception("NetworkLayer: Error getting transaction post failure response", e));
                                    e.printStackTrace();
                                }
                            }
                            postResponse.close();
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            recordFailedUpload(0, 0, networkQueueItemId);
                            String failureMessage = res.getString(R.string.networking_error);
                            if (e != null) {
                                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                            }
                            postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting transaction");
                            FirebaseCrash.report(new Exception(failureMessage, e));
                        }

                    });

                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("NetworkLayer: Error creating transaction post object", e));
                    e.printStackTrace();
                }
            }
        }
    }

    void syncTransactionBalance(long buyingagent_id, long transactee_id, String transactee_type, long currency_id, final Handler handler) {
        JSONObject transactee = new JSONObject();
        try {
            transactee.put("buyingagent_id", buyingagent_id);
            transactee.put("transactee_id", transactee_id);
            transactee.put("transactee_type", transactee_type);
            transactee.put("currency_id", currency_id);
            final Message msg = handler.obtainMessage();

            okHttpPost(context, "transactions/get_transaction_balance", transactee, new Callback() {
                @Override
                public void onResponse(Call call, Response postResponse) {
                    try {
                        if (postResponse.isSuccessful()) {
                            JSONObject response = new JSONObject(postResponse.body().string());
                            if (response.getString("status").equalsIgnoreCase("updated")) {
                                msg.what = 1;
                                msg.obj = Double.parseDouble(response.getString("new_balance"));
                            } else {
                                msg.what = 2;
                            }
                        } else {
                            msg.what = 2;
                        }
                        handler.dispatchMessage(msg);
                    } catch (JSONException | IOException e) {
                        FirebaseCrash.report(new Exception("NetworkLayer: Error getting transaction balance response", e));
                        e.printStackTrace();
                    }
                    postResponse.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    msg.what = 2;
                    handler.dispatchMessage(msg);
                    FirebaseCrash.report(new Exception("NetworkLayer: Error calling transaction balance update", e));
                }

            });

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("NetworkLayer: Error creating transaction balance post object", e));
            e.printStackTrace();
        }

    }

    private void postFmpReview(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {
        // create the JSON object required to sync this review
        JSONObject review = new JSONObject();
        JSONArray entries = new JSONArray();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String IMEI = Utilities.getIMEI(context);
        final int fmpId = networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FMP_ID));
        final int startingYear = networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED));
        final long farmerId = networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));

        try {
            review.put("olam_enumerator_id", enumeratorId);
            review.put("imei_number", IMEI);
            review.put(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_STARTING_YEAR, startingYear);
            review.put(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_FARMER_ID, farmerId);
            review.put(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ID, fmpId);
            review.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
            review.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
            if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                review.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
            }

            Cursor reviewValues = dl.getFmpValuesByFmpId(fmpId);
            while (reviewValues.moveToNext()) {
                JSONObject value = new JSONObject();
                value.put(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ENTRY_ID, reviewValues.getInt(reviewValues.getColumnIndex(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ENTRY_ID)));
                value.put(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_VALUE, reviewValues.getString(reviewValues.getColumnIndex(OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_VALUE)));
                entries.put(value);
            }
            review.put("entries", entries);

            okHttpPost(context, "fmps/submit", review, new Callback() {
                @Override
                public void onResponse(Call call, Response postResponse) {
                    String statusMessage;
                    if (postResponse.isSuccessful()) {
                        dl.syncFmp(fmpId);
                        dl.deleteNetworkQueueItem(networkQueueItemId);
                        statusMessage = res.getString(R.string.item_synced);
                        msg.what = 1;
                        msg.arg1 = total;
                        msg.arg2 = progress;
                        msg.obj = statusMessage;
                        handler.dispatchMessage(msg);
                        syncNetworkQueue(originalSyncRequestItemId, handler);
                    } else {
                        try {
                            JSONObject response = new JSONObject(postResponse.body().string());
                            long server_item_id = 0, fully_synced = 0;
                            if (response.optJSONObject("fmpreview") != null) {
                                JSONObject fmpreview = response.getJSONObject("fmpreview");
                                server_item_id = fmpreview.getLong("id");
                                fully_synced = fmpreview.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                            }
                            recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                            postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting FMP review");
                            FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting FMP review"));
                        } catch (JSONException | IOException e) {
                            FirebaseCrash.report(new Exception("NetworkLayer: Error getting FMP review failure response", e));
                            e.printStackTrace();
                        }
                    }
                    postResponse.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    recordFailedUpload(0, 0, networkQueueItemId);
                    String failureMessage = res.getString(R.string.networking_error);
                    if (e != null) {
                        failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                    }
                    postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting FMP review");
                    FirebaseCrash.report(new Exception(failureMessage, e));
                }

            });

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("NetworkLayer: Error creating fmp review post object", e));
            e.printStackTrace();
        }
    }

    private void postFullFmpReview(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {
        // create the JSON object required to sync this module
        JSONObject submission = new JSONObject();
        JSONArray answers = new JSONArray();
        Set<Integer> questions_included = new TreeSet<>();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String IMEI = Utilities.getIMEI(context);
        final int fmpId = networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FMP_ID));
        final long farmerId = networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
        final String olamFarmerId = networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID));
        final int farmId = networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID));
        Cursor fmpAnswers;

        try {
            final String submissionDate = networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT));
            submission.put(OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID, fmpId);
            submission.put("submission_date", submissionDate);
            submission.put("olam_enumerator_id", enumeratorId);
            submission.put("imei_number", IMEI);
            submission.put("fmpreview", true);
            submission.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, networkQueueItems.getDouble(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
            submission.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, networkQueueItems.getDouble(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID)));
            submission.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
            submission.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
            if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                submission.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
            }

            fmpAnswers = dl.getAnswersByFmpId(fmpId);

            if (fmpAnswers.getCount() > 0) {

                while (fmpAnswers.moveToNext()) {
                    questions_included.add(fmpAnswers.getInt(fmpAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
                    JSONObject answer = new JSONObject();
                    answer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID, fmpAnswers.getInt(fmpAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
                    answer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR, fmpAnswers.getInt(fmpAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR)));
                    String datatype = fmpAnswers.getString(fmpAnswers.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                    if (datatype.equalsIgnoreCase("int") || datatype.equalsIgnoreCase("boolean")) {
                        answer.put("answer", fmpAnswers.getInt(fmpAnswers.getColumnIndex("answer_" + datatype)));
                    } else if (datatype.equalsIgnoreCase("decimal")) {
                        answer.put("answer", fmpAnswers.getDouble(fmpAnswers.getColumnIndex("answer_" + datatype)));
                    } else {
                        answer.put("answer", fmpAnswers.getString(fmpAnswers.getColumnIndex("answer_" + datatype)));
                    }

                    if (fmpAnswers.getString(fmpAnswers.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE)).equalsIgnoreCase("repeater")) {
                        Cursor subAnswers = dl.getAnswersByFmpIdAndParentQuestionId(fmpId, fmpAnswers.getInt(fmpAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));

                        if (subAnswers.getCount() > 0) {
                            JSONArray subAnswersArray = new JSONArray();
                            JSONArray subAnswersAnswerArray = new JSONArray();

                            int repeaterItemPosition = 1;
                            while (subAnswers.moveToNext()) {
                                questions_included.add(subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
                                JSONObject subAnswer = new JSONObject();

                                if (subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION)) > repeaterItemPosition) {
                                    // start new subAnswersAnswerArray
                                    subAnswersArray.put(subAnswersAnswerArray);
                                    subAnswersAnswerArray = new JSONArray();
                                }

                                repeaterItemPosition = subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION));

                                subAnswer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID, subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
                                subAnswer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR, subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR)));
                                String subAnswerDatatype = subAnswers.getString(subAnswers.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                                if (subAnswerDatatype.equalsIgnoreCase("int") || subAnswerDatatype.equalsIgnoreCase("boolean")) {
                                    subAnswer.put("answer", subAnswers.getInt(subAnswers.getColumnIndex("answer_" + subAnswerDatatype)));
                                } else if (subAnswerDatatype.equalsIgnoreCase("decimal")) {
                                    answer.put("answer", subAnswers.getDouble(subAnswers.getColumnIndex("answer_" + datatype)));
                                } else {
                                    subAnswer.put("answer", subAnswers.getString(subAnswers.getColumnIndex("answer_" + subAnswerDatatype)));
                                }
                                subAnswersAnswerArray.put(subAnswer);

                            }
                            subAnswersArray.put(subAnswersAnswerArray);
                            answer.put("sub_answers", subAnswersArray);
                        }
                    }
                    answers.put(answer);
                }
                String questions_included_output = questions_included.toString();
                submission.put("questions_included", questions_included_output.substring(1, questions_included_output.length() - 1));
                submission.put("answers", answers);

                okHttpPost(context, "modules/submit", submission, new Callback() {
                    @Override
                    public void onResponse(Call call, Response postResponse) {
                        String statusMessage;
                        if (postResponse.isSuccessful()) {
                            dl.syncFmp(fmpId);
                            dl.updateSubModuleAnswers(new ArrayList<JSONObject>(), fmpId, 0, farmerId, olamFarmerId, farmId, "fmp");
                            dl.deleteNetworkQueueItem(networkQueueItemId);
                            statusMessage = res.getString(R.string.item_synced);
                            msg.what = 1;
                            msg.arg1 = total;
                            msg.arg2 = progress;
                            msg.obj = statusMessage;
                            handler.dispatchMessage(msg);
                            syncNetworkQueue(originalSyncRequestItemId, handler);
                        } else {
                            try {
                                JSONObject response = new JSONObject(postResponse.body().string());
                                long server_item_id = 0, fully_synced = 0;
                                if (response.optJSONObject("submittedmodule") != null) {
                                    JSONObject submittedModule = response.getJSONObject("submittedmodule");
                                    server_item_id = submittedModule.getLong("id");
                                    fully_synced = submittedModule.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                }
                                recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting FMP review");
                                FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting FMP review"));
                            } catch (JSONException | IOException e) {
                                FirebaseCrash.report(new Exception("NetworkLayer: Error getting module submission failure response", e));
                                e.printStackTrace();
                            }
                        }
                        postResponse.close();
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        recordFailedUpload(0, 0, networkQueueItemId);
                        String failureMessage = res.getString(R.string.networking_error);
                        if (e != null) {
                            failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                        }
                        postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting FMP review");
                        FirebaseCrash.report(new Exception(failureMessage, e));
                    }

                });
                Log.i("POST", "" + submission.toString());
            }
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("NetworkLayer: Error creating full fmp review post object", e));
            e.printStackTrace();
        }
    }

    private void postModule(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {


        // end points
        String endPoint="";
        // old end point
        // endPoint="modules/submit";
        //New endPoint
        endPoint="ofissurvey/survey/submitSurveyAppModule";

        // create the JSON object required to sync this module
        JSONObject submission = new JSONObject();
        JSONArray answers = new JSONArray();
        Set<Integer> questions_included = new TreeSet<>();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String IMEI = Utilities.getIMEI(context);
        final int farmerModuleId = networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID));
        final long farmerId = networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID));
        Cursor moduleAnswers, extraInfo, farmerModule = dl.getFarmerModuleById(farmerModuleId);

        if (farmerModule.getCount() > 0) {
            if (!dl.isFarmerSynced(farmerId)) {
                msg.what = 3;
                msg.arg1 = total;
                msg.arg2 = progress;
                msg.obj = String.valueOf(res.getString(R.string.farmer_not_synced));
                handler.dispatchMessage(msg);
            } else {
                farmerModule.moveToFirst();

                try {
                    final int moduleId = farmerModule.getInt(farmerModule.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID));
                    Utilities.moduleId = moduleId;
                    final String submissionDate = farmerModule.getString(farmerModule.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STARTED_DATE));
                    extraInfo = dl.getExtraInfoByFarmerId(farmerId, moduleId);
                    if(extraInfo.getCount() >0){
                        extraInfo.moveToFirst();
                        submission.put("farmer_name", extraInfo.getString(0));
                        submission.put("country_id", extraInfo.getInt(2));
                        submission.put("country_name", extraInfo.getString(3));
                        submission.put("module_name", extraInfo.getString(4));
                        submission.put("farmer_group_name", extraInfo.getString(5));
                        submission.put("farmer_group_id", extraInfo.getInt(6));
                        submission.put("product_id", extraInfo.getInt(7));
                        submission.put("partner_id", extraInfo.getInt(8));
                    }else{
                        submission.put("country_id","");
                        submission.put("country_name", "");
                        submission.put("farmer_group_id", "");
                        submission.put("farmer_group_name", "");
                        submission.put("module_name", "");
                        submission.put("farmer_name", "");
                        submission.put("product_id", "");
                        submission.put("partner_id", "");
                    }

                    boolean hasPhotos = false;
                    MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    String storageDir = (context.getExternalFilesDir(null) != null) ? context.getExternalFilesDir(null).getPath() + "/" : "/";
                    submission.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID, moduleId);
                    submission.put("submission_date", submissionDate);
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    submission.put("upload_date", timestamp.toString());
                    submission.put("olam_enumerator_id", enumeratorId);
                    submission.put("imei_number", IMEI);
                    submission.put("fmpreview", false);
                    submission.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID, farmerModule.getInt(farmerModule.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID)));
                    submission.put(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARM_ID, farmerModule.getInt(farmerModule.getColumnIndex(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARM_ID)));
                    submission.put("recorded_lat", networkQueueItems.getDouble(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT)));
                    submission.put("recorded_lng", networkQueueItems.getDouble(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)));
                    submission.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
                    submission.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
                    if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                        submission.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
                    }

                    moduleAnswers = dl.getAnswersByFarmerModuleId(farmerModuleId);

                    if (moduleAnswers.getCount() > 0) {

                        while (moduleAnswers.moveToNext()) {
                            questions_included.add(moduleAnswers.getInt(moduleAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
                            JSONObject answer = new JSONObject();
                            answer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID, moduleAnswers.getInt(moduleAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
                            answer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR, moduleAnswers.getInt(moduleAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR)));
                            Utilities.olamFarmerId = moduleAnswers.getString(moduleAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_OLAM_FARMER_ID));

                            answer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_OLAM_FARMER_ID, Utilities.olamFarmerId);


                            int parentid = moduleAnswers.getInt(moduleAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID));

                           Cursor cursor = dl.getSubquestionAnswerSelect(Utilities.moduleId, moduleAnswers.getInt(moduleAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)), Utilities.olamFarmerId + "");
                                Set<Integer> sub_questions_included = new TreeSet<>();

                            if (cursor.getCount() > 0) {

                                answer.put("is_subset", "true");
                                JSONObject subset_section = new JSONObject();
                                JSONArray sub_answers = new JSONArray();
                                if (cursor.moveToFirst()) {
                                    do {
                                        JSONObject sub_answersjson = new JSONObject();
                                        sub_questions_included.add(Integer.parseInt(cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ID))));
                                        sub_answersjson.put("question_id", cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ID)));
                                        sub_answersjson.put("reporting_year", cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_REPORT_YEAR)));

                                        sub_answersjson.put("olam_farmer_id", cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_FARMER_ID)));


                                        if ((cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_POSITION))).equalsIgnoreCase("photo")) {
                                            sub_answersjson.put("answer", Utilities.base64ConversionForImage(cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ANSWER))));
                                            sub_answersjson.put("has_photo_upload",true);
                                        } else
                                            sub_answersjson.put("answer", cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ANSWER)));
                                        sub_answers.put(sub_answersjson);
                                        sub_answersjson.put("has_photo_upload",false);
                                    } while (cursor.moveToNext());
                                }
                                String questions_included_output = sub_questions_included.toString();
                                subset_section.put("sub_questions_included", questions_included_output.substring(1, questions_included_output.length() - 1));
                                subset_section.put("sub_answers", sub_answers);
                                answer.put("subset_section", subset_section);
                            } else {

                                answer.put("is_subset", "false");
                            }


                            String subquestion = "";


                            /*int position = 0;
                            if (cursor != null) {
                                if (cursor.moveToFirst()) {
                                    do {
                                        JSONObject sub_answersjson = new JSONObject();
                                        sub_questions_included.add(Integer.parseInt(cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ID))));
                                        sub_answersjson.put("question_id", cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ID)));
                                        sub_answersjson.put("reporting_year", cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_REPORT_YEAR)));

                                        sub_answersjson.put("olam_farmer_id", cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_FARMER_ID)));


                                        if ((cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_POSITION))).equalsIgnoreCase("photo")) {
                                            sub_answersjson.put("answer", Utilities.base64ConversionForImage(cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ANSWER))));

                                        } else
                                            sub_answersjson.put("answer", cursor.getString(cursor.getColumnIndex(OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ANSWER)));
                                        sub_answers.put(sub_answersjson);
                                    } while (cursor.moveToNext());
                                }
                            }

                            String questions_included_output = sub_questions_included.toString();
                            subset_section.put("sub_questions_included", questions_included_output.substring(1, questions_included_output.length() - 1));
                            subset_section.put("sub_answers", sub_answers);
                            answer.put("subset_section", subset_section);*/
                            String datatype = moduleAnswers.getString(moduleAnswers.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                            if (datatype.equalsIgnoreCase("int") || datatype.equalsIgnoreCase("boolean")) {
                                answer.put("answer", moduleAnswers.getInt(moduleAnswers.getColumnIndex("answer_" + datatype)));
                            } else if (datatype.equalsIgnoreCase("decimal")) {
                                answer.put("answer", moduleAnswers.getDouble(moduleAnswers.getColumnIndex("answer_" + datatype)));
                            } else {
                                answer.put("answer", moduleAnswers.getString(moduleAnswers.getColumnIndex("answer_" + datatype)));
                            }
                            if (moduleAnswers.getString(moduleAnswers.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE)).equalsIgnoreCase("photoupload")) {
                                hasPhotos = true;
                                File photo = new File(storageDir + "/" + moduleAnswers.getString(moduleAnswers.getColumnIndex("answer_" + datatype)));
                                answer.put("answer", Utilities.base64ConversionForImage(
                                        storageDir
                                        + "/"
                                        + moduleAnswers.getString(moduleAnswers.getColumnIndex("answer_" + datatype))));
                                answer.put("has_photo_upload",true);
                            }else{
                                answer.put("has_photo_upload",false);
                            }

                            if (moduleAnswers.getString(moduleAnswers.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE)).equalsIgnoreCase("repeater")) {
                                Cursor subAnswers = dl.getAnswersByModuleIdAndParentQuestionId(farmerModuleId, moduleAnswers.getInt(moduleAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));

                                if (subAnswers.getCount() > 0) {
                                    JSONArray subAnswersArray = new JSONArray();
                                    JSONArray subAnswersAnswerArray = new JSONArray();

                                    int repeaterItemPosition = 1;
                                    while (subAnswers.moveToNext()) {
                                        questions_included.add(subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
                                        JSONObject subAnswer = new JSONObject();
                                        JSONObject displayConfig = new JSONObject(subAnswers.getString(subAnswers.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION)));

                                        if (subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION)) > repeaterItemPosition) {
                                            // start new subAnswersAnswerArray
                                            subAnswersArray.put(subAnswersAnswerArray);
                                            subAnswersAnswerArray = new JSONArray();
                                        }

                                        repeaterItemPosition = subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION));

                                        subAnswer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID, subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID)));
                                        subAnswer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR, subAnswers.getInt(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR)));
                                        subAnswer.put(OFISDBContract.AnswerEntry.COLUMN_NAME_OLAM_FARMER_ID, subAnswers.getString(subAnswers.getColumnIndex(OFISDBContract.AnswerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                                        String subAnswerDatatype = subAnswers.getString(subAnswers.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                                        if (subAnswerDatatype.equalsIgnoreCase("int") || subAnswerDatatype.equalsIgnoreCase("boolean")) {
                                            subAnswer.put("answer", subAnswers.getInt(subAnswers.getColumnIndex("answer_" + subAnswerDatatype)));
                                        } else if (subAnswerDatatype.equalsIgnoreCase("decimal")) {
                                            subAnswer.put("answer", subAnswers.getDouble(subAnswers.getColumnIndex("answer_" + subAnswerDatatype)));
                                        } else {
                                            subAnswer.put("answer", subAnswers.getString(subAnswers.getColumnIndex("answer_" + subAnswerDatatype)));
                                        }

                                        if (displayConfig.has("display")) {
                                            JSONObject display = displayConfig.getJSONObject("display");
                                            if (display.optString("repeateritem_display_type", "").equalsIgnoreCase("photoupload")) {
                                                hasPhotos = true;
                                                subAnswer.put("has_photo_upload",true);
//                                                File photo = new File(storageDir + "/" + subAnswers.getString(subAnswers.getColumnIndex("answer_" + subAnswerDatatype)));
                                                subAnswer.put("answer", Utilities.base64ConversionForImage(
                                                        storageDir + "/" +
                                                                subAnswers.getString(subAnswers.getColumnIndex("answer_" + subAnswerDatatype))));
                                            }else{
                                                subAnswer.put("has_photo_upload",false);
                                            }
                                        }

                                        subAnswersAnswerArray.put(subAnswer);

                                    }
                                    subAnswersArray.put(subAnswersAnswerArray);
                                    answer.put("sub_answers", subAnswersArray);
                                }
                            }
                            answers.put(answer);
                        }
                        String questions_included_output = questions_included.toString();
                        submission.put("questions_included", questions_included_output.substring(1, questions_included_output.length() - 1));
                        submission.put("answers", answers);

                       /* if (hasPhotos) {
                            bodyBuilder.addFormDataPart(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID, submission.getString(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID));
                            bodyBuilder.addFormDataPart("submission_date", submission.getString("submission_date"));
                            bodyBuilder.addFormDataPart("upload_date", submission.getString("upload_date"));
                            bodyBuilder.addFormDataPart("olam_enumerator_id", submission.getString("olam_enumerator_id"));
                            bodyBuilder.addFormDataPart("imei_number", submission.getString("imei_number"));
                            bodyBuilder.addFormDataPart("fmpreview", submission.getString("fmpreview"));
                            bodyBuilder.addFormDataPart(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID, submission.getString(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID));
                            bodyBuilder.addFormDataPart(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARM_ID, submission.getString(OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARM_ID));
                            bodyBuilder.addFormDataPart("recorded_lat", submission.getString("recorded_lat"));
                            bodyBuilder.addFormDataPart("recorded_lng", submission.getString("recorded_lng"));
                            bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, submission.getString(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS));
                            bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));

                            if (submission.optString(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, "").length() > 0) {
                                bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, submission.optString(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID));
                            }

                            bodyBuilder.addFormDataPart("questions_included", submission.getString("questions_included"));
                            bodyBuilder.addFormDataPart("answers", answers.toString());
                            RequestBody requestBody = bodyBuilder.build();

                            okHttpMultiPartPost(context, "modules/submit", requestBody, new Callback() {
                                @Override
                                public void onResponse(Call call, Response postResponse) {
                                    String statusMessage;
                                    if (postResponse.isSuccessful()) {
                                        dl.syncFarmerModule(farmerModuleId, farmerId, moduleId, submissionDate);
                                        dl.deleteNetworkQueueItem(networkQueueItemId);
                                        statusMessage = res.getString(R.string.item_synced);
                                        msg.what = 1;
                                        msg.arg1 = total;
                                        msg.arg2 = progress;
                                        msg.obj = statusMessage;
                                        handler.dispatchMessage(msg);
                                        syncNetworkQueue(originalSyncRequestItemId, handler);
                                    } else {
                                        try {
                                            JSONObject response = new JSONObject(postResponse.body().string());
                                            long server_item_id = 0, fully_synced = 0;

                                            if (response.optJSONObject("submittedmodule") != null) {
                                                JSONObject submittedModule = response.getJSONObject("submittedmodule");
                                                server_item_id = submittedModule.getLong("id");
                                                fully_synced = submittedModule.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                            }

                                            recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);

                                            postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting survey module");
                                            FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting survey module"));
                                        } catch (JSONException | IOException e) {
                                            FirebaseCrash.report(new Exception("NetworkLayer: Error getting module with photos posting failure response", e));
                                            e.printStackTrace();
                                        }
                                    }
                                    postResponse.close();
                                }

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    recordFailedUpload(0, 0, networkQueueItemId);

                                    String failureMessage = res.getString(R.string.networking_error);
                                    if (e != null) {
                                        failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                                    }
                                    postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting survey module with photos");
                                    FirebaseCrash.report(new Exception(failureMessage, e));
                                }

                            });

                        } else */{

                            okHttpPost(context, endPoint, submission, new Callback() {
                                @Override
                                public void onResponse(Call call, Response postResponse) {
                                    String statusMessage;
                                    if (postResponse.isSuccessful()) {
                                        dl.syncFarmerModule(farmerModuleId, farmerId, moduleId, submissionDate);
                                        dl.deleteNetworkQueueItem(networkQueueItemId);
                                        statusMessage = res.getString(R.string.item_synced);
                                        msg.what = 1;
                                        msg.arg1 = total;
                                        msg.arg2 = progress;
                                        msg.obj = statusMessage;
                                        handler.dispatchMessage(msg);
                                        syncNetworkQueue(originalSyncRequestItemId, handler);
                                    } else {
                                        try {
                                            JSONObject response = new JSONObject(postResponse.body().string());
                                            long server_item_id = 0, fully_synced = 0;

                                            if (response.optJSONObject("submittedmodule") != null) {
                                                JSONObject submittedModule = response.getJSONObject("submittedmodule");
                                                server_item_id = submittedModule.getLong("id");
                                                fully_synced = submittedModule.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                            }

                                            recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);

                                            postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting survey module");
                                            FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting survey module"));
                                        } catch (JSONException | IOException e) {
                                            recordFailedUpload(0, 0, networkQueueItemId);
                                            postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting survey module");
                                            FirebaseCrash.report(new Exception("NetworkLayer: Error getting module posting failure response", e));
                                            e.printStackTrace();
                                        }
                                    }
                                    postResponse.close();
                                }

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    recordFailedUpload(0, 0, networkQueueItemId);

                                    String failureMessage = res.getString(R.string.networking_error);
                                    if (e != null) {
                                        failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                                    }
                                    postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting survey module");
                                    FirebaseCrash.report(new Exception(failureMessage, e));
                                }
                            });
                        }

                        Log.i("POST", "" + submission.toString());
                    }
                } catch (JSONException e) {
                    recordFailedUpload(0, 0, networkQueueItemId);
                    postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting survey module");
                    FirebaseCrash.report(new Exception("NetworkLayer: Error creating module post object", e));
                    e.printStackTrace();
                }
            }
        }

    }

    private void postFarmPolygon(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {
        String endpoint="";
        // Old end point
        // endpoint = "poi/add/farm";
        // new End point
        endpoint = "ofisfarmer/farmer/addfarm";
        JSONObject params = new JSONObject();

        if (!dl.isFarmerSynced(networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)))) {
            msg.what = 3;
            msg.arg1 = total;
            msg.arg2 = progress;
            msg.obj = String.valueOf(res.getString(R.string.farmer_not_synced));
            handler.dispatchMessage(msg);
        } else {
            try {
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID)));
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                params.put("farm_plots", networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_PLOTS)));
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
                if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                    params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
                    params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
                }

                if (endpoint.length() > 0) {
                    okHttpPost(context, endpoint, params, new Callback() {
                        @Override
                        public void onResponse(Call call, Response postResponse) throws IOException {
                            String statusMessage;
                            if (postResponse.isSuccessful()) {

                                String postResponseStr = postResponse.body().string();

                                try {
                                    JSONObject jsonServerRespObj = new JSONObject(postResponseStr.toString());

                                    // Getting JSON Array node
                                    JSONArray locationJsonArray = null;
                                    locationJsonArray = jsonServerRespObj.getJSONArray("location");

                                    // looping through All locationJsonArray
                                    for (int i = 0; i < locationJsonArray.length(); i++) {
                                        JSONObject plotDataObj = locationJsonArray.getJSONObject(i);
                                        dl.updatePlotGeoDataAfterSync(plotDataObj.getString("plotid"), Integer.toString(plotDataObj.getInt("serverid")));
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                dl.deleteNetworkQueueItem(networkQueueItemId);
                                statusMessage = res.getString(R.string.item_synced);
                                msg.what = 1;
                                msg.arg1 = total;
                                msg.arg2 = progress;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                                syncNetworkQueue(originalSyncRequestItemId, handler);
                            } else {
                                try {
                                    JSONObject response = new JSONObject(postResponse.body().string());
                                    long server_item_id = 0, fully_synced = 0;
                                    if (response.optJSONObject("farm") != null) {
                                        JSONObject farm = response.getJSONObject("farm");
                                        server_item_id = farm.getLong("id");
                                        fully_synced = farm.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                    }
                                    recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                    postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting farm polygon");
                                    FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting farm polygon"));
                                } catch (JSONException | IOException e) {
                                    recordFailedUpload(0, 0, networkQueueItemId);
                                    postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting farm polygon");
                                    FirebaseCrash.report(new Exception("NetworkLayer: Error getting farm polygon failure response", e));
                                    e.printStackTrace();
                                }
                            }
                            postResponse.close();
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            recordFailedUpload(0, 0, networkQueueItemId);
                            String failureMessage = res.getString(R.string.networking_error);
                            if (e != null) {
                                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                            }
                            postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting farm polygon");
                            FirebaseCrash.report(new Exception(failureMessage, e));
                        }

                    });
                }
            } catch (JSONException e) {
                recordFailedUpload(0, 0, networkQueueItemId);
                postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting farm polygon");
                FirebaseCrash.report(new Exception("NetworkLayer: Error creating farm polygon post object", e));
                e.printStackTrace();
            }
        }
    }

    private void postFarmLocation(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {

        String endpoint="";
        // old
        // endpoint = "poi/add/farm";
        // new
        endpoint = "ofisfarmer/farmer/addfarm";

        JSONObject params = new JSONObject();

        if (!dl.isFarmerSynced(networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)))) {
            msg.what = 3;
            msg.arg1 = total;
            msg.arg2 = progress;
            msg.obj = String.valueOf(res.getString(R.string.farmer_not_synced));
            handler.dispatchMessage(msg);
        } else {

            try {
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID)));
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                params.put("primary_lat", networkQueueItems.getDouble(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT)));
                params.put("primary_long", networkQueueItems.getDouble(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)));
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
                if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                    params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
                    params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
                }


                if (endpoint.length() > 0) {
                    okHttpPost(context, endpoint, params, new Callback() {
                        @Override
                        public void onResponse(Call call, Response postResponse) {
                            String statusMessage;
                            if (postResponse.isSuccessful()) {
                                dl.deleteNetworkQueueItem(networkQueueItemId);
                                statusMessage = res.getString(R.string.item_synced);
                                msg.what = 1;
                                msg.arg1 = total;
                                msg.arg2 = progress;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                                syncNetworkQueue(originalSyncRequestItemId, handler);
                            } else {
                                try {
                                    JSONObject response = new JSONObject(postResponse.body().string());
                                    long server_item_id = 0, fully_synced = 0;
                                    if (response.optJSONObject("farm") != null) {
                                        JSONObject farm = response.getJSONObject("farm");
                                        server_item_id = farm.getLong("id");
                                        fully_synced = farm.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                    }
                                    recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                    postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting farm location");
                                    FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting farm location"));
                                } catch (JSONException | IOException e) {
                                    recordFailedUpload(0, 0, networkQueueItemId);
                                    postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting farm location");
                                    FirebaseCrash.report(new Exception("NetworkLayer: Error getting farm location failure response", e));
                                    e.printStackTrace();
                                }
                            }
                            postResponse.close();
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            recordFailedUpload(0, 0, networkQueueItemId);
                            String failureMessage = res.getString(R.string.networking_error);
                            if (e != null) {
                                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                            }
                            postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting farm location");
                            FirebaseCrash.report(new Exception(failureMessage, e));
                        }

                    });
                }
            } catch (JSONException e) {
                recordFailedUpload(0, 0, networkQueueItemId);
                postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting farm location");
                FirebaseCrash.report(new Exception("NetworkLayer: Error creating farm location post object", e));
                e.printStackTrace();
            }
        }
    }

    // get farmer phone numbers
    private JSONArray farmerPhoneNumbers(long farmerId) {
        Cursor phonenumbers = dl.getFarmerPhoneNumbers(farmerId, false);
        JSONArray numbersarray = new JSONArray();

        // New Code
//        List<FarmerRegistrationPhoneNumberDTO> farmerRegistrationPhoneNumberDTOList=new ArrayList<FarmerRegistrationPhoneNumberDTO>();

        try {
            while (phonenumbers.moveToNext()) {
                JSONObject phonenumber = new JSONObject();

                // old
//                phonenumber.put(OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID, phonenumbers.getInt(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID)));
//                phonenumber.put(OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID, phonenumbers.getInt(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID)));
//                phonenumber.put(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER, phonenumbers.getString(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER)));
//                phonenumber.put(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY, phonenumbers.getInt(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY)));

                //New
                phonenumber.put("farmerId", new Long(phonenumbers.getInt(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID))));
                phonenumber.put("countryId", new Long(phonenumbers.getInt(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID))));
                phonenumber.put("phoneNumber", phonenumbers.getString(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER)));
                phonenumber.put("primaryNumber", Utilities.convertDT(String.valueOf(phonenumbers.getInt(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY)))));

                numbersarray.put(phonenumber);

//                FarmerRegistrationPhoneNumberDTO farmerRegistrationPhoneNumberDTO=new FarmerRegistrationPhoneNumberDTO();
//                farmerRegistrationPhoneNumberDTO.setFarmerId(new Long(phonenumbers.getInt(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID))));
//                farmerRegistrationPhoneNumberDTO.setCountryId(new Long(phonenumbers.getInt(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID))));
//                farmerRegistrationPhoneNumberDTO.setPhoneNumber(phonenumbers.getString(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER)));
//                farmerRegistrationPhoneNumberDTO.setPrimaryNumber(Utilities.convertDT(phonenumbers.getString(phonenumbers.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER))));
//                farmerRegistrationPhoneNumberDTOList.add(farmerRegistrationPhoneNumberDTO);
            }
            return numbersarray;
        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("NetworkLayer: Error creating farmer phone numbers object", e));
            e.printStackTrace();
        }
        return null;
    }

    private void postPhoneNumbers(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {

        String endpoint="";
        //old end point
        // endpoint = "updatephonenumbers";
        // new end point
        endpoint="ofisfarmer/farmer/updatephonenumbers";
        JSONObject params = new JSONObject();
        JSONArray numberstosend = farmerPhoneNumbers(networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));

//        List<FarmerRegistrationPhoneNumberDTO> farmerRegistrationPhoneNumberDTOList = farmerPhoneNumbers(networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));

        if (!dl.isFarmerSynced(networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)))) {
            msg.what = 3;
            msg.arg1 = total;
            msg.arg2 = progress;
            msg.obj = String.valueOf(res.getString(R.string.farmer_not_synced));
            handler.dispatchMessage(msg);
        } else {

            try {
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                params.put("phoneNumbers", numberstosend);
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
                if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                    params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
                }

                if (numberstosend != null && endpoint.length() > 0) {
                    okHttpPost(context, endpoint, params, new Callback() {
                        @Override
                        public void onResponse(Call call, Response postResponse) {
                            String statusMessage;
                            if (postResponse.isSuccessful()) {
                                dl.deleteNetworkQueueItem(networkQueueItemId);
                                statusMessage = res.getString(R.string.item_synced);
                                msg.what = 1;
                                msg.arg1 = total;
                                msg.arg2 = progress;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                                syncNetworkQueue(originalSyncRequestItemId, handler);
                            } else {
                                try {
                                    JSONObject response = new JSONObject(postResponse.body().string());
                                    long server_item_id = 0, fully_synced = 0;
                                    if (response.optJSONObject("phonenumbers") != null) {
                                        JSONObject phonenumbers = response.getJSONObject("phonenumbers");
                                        server_item_id = phonenumbers.getLong("id");
                                        fully_synced = phonenumbers.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                    }
                                    recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                    postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting farm location");
                                    FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting farm location"));
                                } catch (JSONException | IOException e) {
                                    recordFailedUpload(0, 0, networkQueueItemId);
                                    postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting farm location");
                                    FirebaseCrash.report(new Exception("NetworkLayer: Error getting phone numbers post failure response", e));
                                    e.printStackTrace();
                                }
                            }
                            postResponse.close();
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            recordFailedUpload(0, 0, networkQueueItemId);
                            String failureMessage = res.getString(R.string.networking_error);
                            if (e != null) {
                                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                            }
                            postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting farm location");
                            FirebaseCrash.report(new Exception(failureMessage, e));
                        }

                    });
                }
            } catch (JSONException e) {
                recordFailedUpload(0, 0, networkQueueItemId);
                postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting farm location");
                FirebaseCrash.report(new Exception("NetworkLayer: Error creating phone numbers post object", e));
                e.printStackTrace();
            }
        }

    }

    private void postFarmer(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        RequestBody requestBody;
        Cursor farmer = dl.getFarmerById(networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
        String endpoint = "farmers/add";

        if (farmer.getCount() > 0) {
            farmer.moveToFirst();

            bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
            bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
            bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB)));
            bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER)));
            bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)));
            bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID)));
            bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_FARMER, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_FARMER)));
            bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_OTHERS, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_OTHERS)));
            bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
            bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
            if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                bodyBuilder.addFormDataPart(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
            }

            // test for existence of new place name
            final int localPlaceId = farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID));
            Cursor place = dl.getPlaceById(localPlaceId);
            if (place != null) {
                place.moveToFirst();
                if (place.getString(place.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE)).equalsIgnoreCase("localinsert")) {
                    bodyBuilder.addFormDataPart("new_place", "true");
                    bodyBuilder.addFormDataPart("new_place_name", place.getString(place.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME)));
                    bodyBuilder.addFormDataPart("new_place_district_id", place.getString(place.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID)));
                }
            }

            String farmerPhotoPath = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
            if (farmerPhotoPath != null && farmerPhotoPath.length() > 0) {
                File photo = new File(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO)));

                RequestBody file = RequestBody.create(MEDIA_TYPE_JPG, photo);
                bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO, photo.getName(), file);

            }

            String farmerSignaturePath = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE));
            if (farmerSignaturePath != null && farmerSignaturePath.length() > 0) {
                File signature = new File(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE)));

                RequestBody file = RequestBody.create(MEDIA_TYPE_JPG, signature);
                bodyBuilder.addFormDataPart(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE, signature.getName(), file);
            }

            final int localFarmerId = farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));

//            JSONArray numberstosend = farmerPhoneNumbers(networkQueueItems
//                    .getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
            JsonArray numberstosend=null;

            if (numberstosend != null) {
                bodyBuilder.addFormDataPart("phoneNumbers", numberstosend.toString());
            }

            requestBody = bodyBuilder.build();

            if (endpoint.length() > 0) {
                okHttpMultiPartPost(context, endpoint, requestBody, new Callback() {
                    @Override
                    public void onResponse(Call call, Response postResponse) {
                        try {
                            String statusMessage;
                            JSONObject response = new JSONObject(postResponse.body().string());
                            if (postResponse.isSuccessful()) {
                                JSONObject farmerResponse = response.getJSONObject("farmer");
                                dl.markFarmerAsSynced(localFarmerId, farmerResponse.getInt("id"), response.getInt("farm_id"), localPlaceId, response.getInt("new_place_id"));
                                dl.deleteNetworkQueueItem(networkQueueItemId);
                                statusMessage = res.getString(R.string.item_synced);
                                msg.what = 1;
                                msg.arg1 = total;
                                msg.arg2 = progress;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                                // update the cursor if this is part of a multi-sync
                                if (originalSyncRequestItemId == null) {
                                    getNetworkQueue(null);
                                }
                                syncNetworkQueue(originalSyncRequestItemId, handler);
                            } else {
                                long server_item_id = 0, fully_synced = 0;
                                if (response.optJSONObject("farmer") != null) {
                                    JSONObject farmer = response.getJSONObject("farmer");
                                    server_item_id = farmer.optLong("id", 0);
                                    fully_synced = farmer.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                }
                                recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting farmer");
                                FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting submitting farmer"));
                            }
                            postResponse.close();
                        } catch (JSONException | IOException e) {

                            // OW-752 - fixed
                            //Same farmer ID on different devices
                            recordFailedUpload(0, 0, networkQueueItemId);
                            String failureMessage = res.getString(R.string.item_sync_error);
                            postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting submitting farmer");

                            FirebaseCrash.report(new Exception("NetworkLayer: Error getting farmer post failure response", e));
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        recordFailedUpload(0, 0, networkQueueItemId);
                        String failureMessage = res.getString(R.string.networking_error);
                        if (e != null) {
                            failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                        }
                        postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting submitting farmer");
                        FirebaseCrash.report(new Exception(failureMessage, e));
                    }

                });
            }
        }
    }

    private void postInfrastructure(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {
        Hashtable<String, String> poitypes = new Hashtable<>();

        JSONObject params = new JSONObject();
        JSONArray answers = new JSONArray();
        Set<Integer> questions_included = new TreeSet<>();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String IMEI = Utilities.getIMEI(context);
        boolean hasPhotos = false;
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        String storageDir = (context.getExternalFilesDir(null) != null) ? context.getExternalFilesDir(null).getPath() + "/" : "/";

        final String uploadId = networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID));
        Cursor moduleAnswers= dl.getAnswersSipByUploadId(uploadId);
        try {
            if(enumeratorId != null) {
                long enumerator_Id = Integer.parseInt(enumeratorId);
                params.put("enumerator_id", enumeratorId);
            }
            params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLACE_ID, 0);
            params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID)));
            params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID)));
            params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID)));
            params.put("recorded_lat", networkQueueItems.getDouble(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT)));
            params.put("recorded_lng", networkQueueItems.getDouble(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG)));
            params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
            params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));
            if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
            }
            params.put("sip_group_id", networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID)));
            params.put("sip_group", networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE)));
            params.put("sip_submodule_id", networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBMODULE_ID)));
            params.put("sip_submodule", networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE)));

//            if (!networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE)).equalsIgnoreCase("Clean water supply")) {
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME)));
//            }
/*

            if (!networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE)).equalsIgnoreCase("Buying centre")) {
                params.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FUNDED, networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FUNDED)));
            }
*/

            params.put("farm_id", 0);
//            params.put("Capacity", 0);
//            params.put("Product_id", 0);

            if (moduleAnswers.getCount() > 0) {

                while (moduleAnswers.moveToNext()) {
                    questions_included.add(moduleAnswers.getInt(moduleAnswers.getColumnIndex(OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID)));
                    JSONObject answer = new JSONObject();
                    answer.put(OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID, moduleAnswers.getInt(moduleAnswers.getColumnIndex(OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID)));
                    answer.put("reporting_date", moduleAnswers.getString(moduleAnswers.getColumnIndex(OFISDBContract.AnswerSipEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR)));
                    String datatype = moduleAnswers.getString(moduleAnswers.getColumnIndex(OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME));
                    if (datatype.equalsIgnoreCase("int") || datatype.equalsIgnoreCase("boolean")) {
                        answer.put("answer", moduleAnswers.getInt(moduleAnswers.getColumnIndex("answer_" + datatype)));
                    } else if (datatype.equalsIgnoreCase("decimal")) {
                        answer.put("answer", moduleAnswers.getDouble(moduleAnswers.getColumnIndex("answer_" + datatype)));
                    } else {
                        answer.put("answer", moduleAnswers.getString(moduleAnswers.getColumnIndex("answer_" + datatype)));
                    }

                    if (moduleAnswers.getString(moduleAnswers.getColumnIndex(OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE)).equalsIgnoreCase("photoupload")) {
                        hasPhotos = true;
                        File photo = new File(storageDir + "/" + moduleAnswers.getString(moduleAnswers.getColumnIndex("answer_" + datatype)));
                        answer.put("answer", Utilities.base64ConversionForImage(storageDir + "/" + moduleAnswers.getString(moduleAnswers.getColumnIndex("answer_" + datatype))));
//                        RequestBody file = RequestBody.create(MEDIA_TYPE_JPG, photo);
//                        bodyBuilder.addFormDataPart(FilenameUtils.removeExtension(photo.getName()), photo.getName(), file);
                    }

                    answers.put(answer);
                }

                String questions_included_output = questions_included.toString();
                params.put("questions_included", questions_included_output.substring(1, questions_included_output.length() - 1));
                params.put("answers", answers);

            }

//                    String url = "http://13.76.2.201:8080/farmerdata/socialinfra/saveAllSipAnswers/";
                    String url = "farmer-data/farmerdata/socialinfra/saveAllSipAnswers/";

                    okHttpPost(context, url, params, new Callback() {
                        @Override
                        public void onResponse(Call call, Response postResponse) {
                            String statusMessage;
                            if (postResponse.isSuccessful()) {
//                                dl.syncFarmerModule(farmerModuleId, farmerId, moduleId, submissionDate);

                                dl.deleteSipNetworkQueueItem(networkQueueItemId);

                                statusMessage = res.getString(R.string.item_synced);
                                msg.what = 1;
                                msg.arg1 = total;
                                msg.arg2 = progress;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                                syncNetworkQueue(originalSyncRequestItemId, handler);
                            } else {
                                try {
                                    JSONObject response = new JSONObject(postResponse.body().string());
                                    long server_item_id = 0, fully_synced = 0;

                                    if (response.optJSONObject("submittedmodule") != null) {
                                        JSONObject submittedModule = response.getJSONObject("submittedmodule");
                                        server_item_id = submittedModule.getLong("id");
                                        fully_synced = submittedModule.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                    }

                                    recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);

                                    postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting survey module");
                                    FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting survey module"));
                                } catch (JSONException | IOException e) {
                                    recordFailedUpload(0, 0, networkQueueItemId);
                                    postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting survey module");
                                    FirebaseCrash.report(new Exception("NetworkLayer: Error getting module posting failure response", e));
                                    e.printStackTrace();
                                }
                            }
                            postResponse.close();
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            recordFailedUpload(0, 0, networkQueueItemId);

                            String failureMessage = res.getString(R.string.networking_error);
                            if (e != null) {
                                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                            }
                            postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting survey module");
                            FirebaseCrash.report(new Exception(failureMessage, e));
                        }
                    });

                Log.i("POST", "" + params.toString());
        } catch (JSONException e) {
            recordFailedUpload(0, 0, networkQueueItemId);
            postFailureMessage(handler, null, msg, 2, total, progress, null, "submitting survey module");
            FirebaseCrash.report(new Exception("NetworkLayer: Error creating module post object", e));
            e.printStackTrace();
        }
    }

    // inbound sync functions
    void checkVersion(final Handler handler) throws JSONException {
        long timestampUpdated = Utilities.getLongFromSharedPrefs(context, "version_checked");
        if ((timestampUpdated + (24 * 60 * 60 * 1000)) < System.currentTimeMillis()) {
            timestampUpdated = System.currentTimeMillis();
            Utilities.saveLongToSharedPrefs(context, "version_checked", timestampUpdated);
            JSONObject inputArgs = new JSONObject();
            inputArgs.put("current_version", BuildConfig.VERSION_NAME);
            okHttpPost(context, "check_version", inputArgs, new Callback() {
                @Override
                public void onResponse(Call call, Response postResponse) {
                    try {
                        Message msg = handler.obtainMessage();
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
                                msg.what = 4;
                                msg.obj = response;
                                handler.dispatchMessage(msg);
                            } else {
                                Utilities.removeFromSharedPrefs(context, "version_update_notified_on");
                                Utilities.removeFromSharedPrefs(context, "upgrade_by");
                            }
                        } else {
                            postFailureMessage(handler, response, msg, 3, 0, 0, null, "checking version");
                            FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : checking version"));
                        }
                        postResponse.close();

                    } catch (JSONException | IOException e) {
                        FirebaseCrash.report(new Exception("NetworkLayer: Error getting version check post response", e));
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    String failureMessage = res.getString(R.string.networking_error);
                    if (e != null) {
                        failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                    }
                    postFailureMessage(handler, null, handler.obtainMessage(), 3, 0, 0, failureMessage, "checking version");
                    FirebaseCrash.report(new Exception(failureMessage, e));
                }

            });
        } else {
            Message msg = handler.obtainMessage();
            msg.what = 5;
            handler.dispatchMessage(msg);
        }
    }

    @Override
    public void didCompleteSyncOperation(SyncAllTask task, SyncTaskOperationType nextOperation) {

        SyncAllTask syncAllTask = new SyncAllTask(context);
        syncAllTask.setProgressDialog(pd);
        syncAllTask.setResources(res);
        syncAllTask.setDelegate(this);

        syncAllTask.pd = task.pd;
        syncAllTask.spinnerResponse = task.spinnerResponse;
        syncAllTask.moduleResponse = task.moduleResponse;
        syncAllTask.sipModuleResponse = task.sipModuleResponse;
        syncAllTask.farmerResponse = task.farmerResponse;
        syncAllTask.fmpResponse = task.fmpResponse;
        syncAllTask.transactionResponse = task.transactionResponse;
        syncAllTask.trainingResponse = task.trainingResponse;
        syncAllTask.tasksResponse = task.tasksResponse;
        syncAllTask.execute(nextOperation);

    }

    void syncData() {
        pd = Utilities.openProgressDialog(context, res.getString(R.string.Retrieving_data), false);
        SyncAllTask syncAllTask = new SyncAllTask(context);
        syncAllTask.setProgressDialog(pd);
        syncAllTask.setResources(res);
        syncAllTask.setDelegate(this);
        syncAllTask.execute(SyncTaskOperationType.CHECK_VERSION);

    }

    void showDialog(String s) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Title");
        alert.setMessage("Message");
        // Create TextView
        final TextView input = new TextView(context);
        ScrollView scroller = new ScrollView(context);
        scroller.addView(input);
        alert.setView(scroller);
        input.setText(s);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }


    //JIRA req OW-505
    private void postFarmerPhoto(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {

        String endpoint="";
        // old end point
        // endpoint = "uploadFarmerFile";
         // new end point
        endpoint = "utility/uploadFarmerFile";
        JSONObject params = new JSONObject();

            try {

                Cursor farmer = dl.getFarmerById(networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
                if (farmer.getCount() > 0) {
                    farmer.moveToFirst();
                    params.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                    params.put(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                    params.put("displayFarmerId", farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)));
                    String farmerPhotoPath = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
                    if (farmerPhotoPath != null && farmerPhotoPath.length() > 0) {
                        File photo = new File(farmerPhotoPath);
                        Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 20, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        params.put("photo", encoded);
                    }

                }
                if (endpoint.length() > 0) {
                    okHttpMSPost(context, endpoint, params, new Callback() {
                        @Override
                        public void onResponse(Call call, Response postResponse) {
                            String statusMessage;
                            if (postResponse.isSuccessful()) {
                                dl.deleteNetworkQueueItem(networkQueueItemId);
                                statusMessage = res.getString(R.string.item_synced);
                                msg.what = 1;
                                msg.arg1 = total;
                                msg.arg2 = progress;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                                syncNetworkQueue(originalSyncRequestItemId, handler);
                            } else {
                                try {
                                    JSONObject response = new JSONObject(postResponse.body().string());
                                    recordFailedUpload(0, 0, networkQueueItemId);
                                    String failureMessage = res.getString(R.string.networking_error);
                                    postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting farmer photo failed");
                                    FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting farmer photo"));

                                    /*      long server_item_id = 0, fully_synced = 0;
                                    if (response.optJSONObject("farm") != null) {
                                        JSONObject farm = response.getJSONObject("farm");
                                        server_item_id = farm.getLong("id");
                                        fully_synced = farm.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                    }
                                    recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                    postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting farm location");
                                    FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting farmer photo"));
                              */
                                } catch (JSONException | IOException e) {
                                    recordFailedUpload(0, 0, networkQueueItemId);
                                    String failureMessage = res.getString(R.string.networking_error);
                                    if (e != null) {
                                        failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                                    }
                                    postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting farmer photo failed");

                                    FirebaseCrash.report(new Exception("NetworkLayer: Error getting farmer photo  failure response", e));
                                    e.printStackTrace();
                                }
                            }
                            postResponse.close();
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            recordFailedUpload(0, 0, networkQueueItemId);
                            String failureMessage = res.getString(R.string.networking_error);
                            if (e != null) {
                                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                            }
                            postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting farmer photo failed");
                            FirebaseCrash.report(new Exception(failureMessage, e));
                        }

                    });
                }
            } catch (JSONException e) {
                recordFailedUpload(0, 0, networkQueueItemId);
                String failureMessage = res.getString(R.string.networking_error);
                postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting farmer photo failed");
                FirebaseCrash.report(new Exception("NetworkLayer: Error creating farm location post object", e));
                e.printStackTrace();
            }

    }

    private static String getMSAbsoluteUrl(String relativeUrl) {
        return BuildConfig.MICROSERVICE_HOST + relativeUrl;
    }

    // Farmer Registration Code
    private void postFarmerJava(final int networkQueueItemId, final Handler handler, final Message msg, final int total, final int progress, final Integer originalSyncRequestItemId) {
        Cursor farmer = dl.getFarmerById(networkQueueItems.getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));
        //old end point
         // String endpoint = "farmers/add";
        // New End point
        String endpoint="ofisfarmer/farmer/registerFarmer";

        if (farmer.getCount() > 0) {
            farmer.moveToFirst();
            JSONObject jsonObject=new JSONObject();
            try {

                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERNAME,farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID_DISPLAY, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)));
//                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB)));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_DATE_OF_BIRTH, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DOB)));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER, farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER)));

                // converting to Long value
              //  long countryID= Long.valueOf(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRYID, Long.valueOf(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRYID))));
                long placeID= Long.valueOf(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACEID, placeID);
                long sectionID=Long.valueOf(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID)));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SECTIONID, sectionID);
                long districtID=Long.valueOf(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID)));
                jsonObject.put("districtId",districtID);
                long regionID=Long.valueOf(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID)));
                jsonObject.put("regionId",regionID);
                long farmerGroupID=Long.valueOf(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID)));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID,farmerGroupID);


                // cnvert the consent to boolean
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENTFARMER,Utilities.convertDT(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_FARMER))));
                jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENTOTHERS, Utilities.convertDT(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_OTHERS))));

                jsonObject.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS)));
                jsonObject.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID)));


                if (networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)) > 0 && networkQueueItems.getInt(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED)) == 0) {
                    jsonObject.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID, networkQueueItems.getString(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID)));
                }

                jsonObject.put("enumeratorId",Long.valueOf(Utilities.getFromSharedPrefs(context)));

            }catch (Exception e){
                e.printStackTrace();
            }

            // test for existence of new place name
            final int localPlaceId = farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID));
            Cursor place = dl.getPlaceById(localPlaceId);
            if (place != null) {
                place.moveToFirst();
                if (place.getString(place.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE)).equalsIgnoreCase("localinsert")) {
                    try {
                        jsonObject.put("new_place", "true");
                        jsonObject.put("new_place_name", place.getString(place.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME)));
                        jsonObject.put("new_place_district_id", place.getString(place.getColumnIndex(OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID)));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            String farmerPhotoPath = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
            if (farmerPhotoPath != null && farmerPhotoPath.length() > 0) {
                File photo = new File(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO)));
                //  RequestBody file = RequestBody.create(MEDIA_TYPE_JPG, photo);
                // convert base 64 string

                try {
                    String encodedImage=Utilities.base64ConversionForImage(farmerPhotoPath);
                    jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO, encodedImage);

  //                  jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO, photo.getName());
                }catch (Exception e){
                    e.printStackTrace();
                }


            }

            String farmerSignaturePath = farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE));
            if (farmerSignaturePath != null && farmerSignaturePath.length() > 0) {
                File signature = new File(farmer.getString(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE)));
                try {
                    String encodedImage=Utilities.base64ConversionForImage(farmerSignaturePath);
                    jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE, encodedImage);
//                   jsonObject.put(OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE, signature.getName());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }



            final int localFarmerId = farmer.getInt(farmer.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_ID));
            JSONArray numberstosend = farmerPhoneNumbers(networkQueueItems
                    .getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));

//            JSONArray farmerRegistrationPhoneNumberDTOList = farmerPhoneNumbers(networkQueueItems
//                    .getLong(networkQueueItems.getColumnIndex(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID)));

            try {
                if (numberstosend != null) {
                  jsonObject.put("phoneNumbers", numberstosend);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            JSONArray jsonArray=new JSONArray();
            jsonArray.put(jsonObject);
            // String requestData=jsonArray.toString();
            if (endpoint.length() > 0) {

                okHttpArrayPost(context, endpoint, jsonArray, new Callback() {
                    @Override
                    public void onResponse(Call call, Response postResponse) {
                        try {
                            String statusMessage;

                       //     JSONObject response = new JSONObject(postResponse.body().string());
                            if (postResponse.isSuccessful()) {
                               // JSONObject farmerResponse = response.getJSONObject("farmer");

                                //new
                                JSONArray jsonResponse=new JSONArray(postResponse.body().string());
                                JSONObject jsonObject1=jsonResponse.getJSONObject(0);

                                dl.markFarmerAsSynced(localFarmerId, jsonObject1.getInt("id"), jsonObject1.getInt("farmId"), localPlaceId, jsonObject1.getInt("placeId"));
                                dl.deleteNetworkQueueItem(networkQueueItemId);
                                statusMessage = res.getString(R.string.item_synced);
                                msg.what = 1;
                                msg.arg1 = total;
                                msg.arg2 = progress;
                                msg.obj = statusMessage;
                                handler.dispatchMessage(msg);
                                // update the cursor if this is part of a multi-sync
                                if (originalSyncRequestItemId == null) {
                                    getNetworkQueue(null);
                                }
                                syncNetworkQueue(originalSyncRequestItemId, handler);
                            } else {
                                long server_item_id = 0, fully_synced = 0;

                                // Added
                                JSONArray jsonResponse=new JSONArray(postResponse.body().string());
                                JSONObject response=jsonResponse.getJSONObject(0);

                                if (response != null) {
                                    server_item_id = response.optLong("id", 0);
                                    fully_synced = response.getLong(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED);
                                }
                                recordFailedUpload(server_item_id, fully_synced, networkQueueItemId);
                                postFailureMessage(handler, response, msg, 2, total, progress, null, "submitting farmer");
                                FirebaseCrash.report(new Exception(response.optString("errors", res.getString(R.string.item_sync_error)) + " : submitting submitting farmer"));
                            }
                            postResponse.close();
                        } catch (JSONException | IOException e) {

                            // OW-752 - fixed
                            //Same farmer ID on different devices
                            recordFailedUpload(0, 0, networkQueueItemId);
                            String failureMessage = res.getString(R.string.item_sync_error);
                            postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting submitting farmer");

                            FirebaseCrash.report(new Exception("NetworkLayer: Error getting farmer post failure response", e));
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        recordFailedUpload(0, 0, networkQueueItemId);
                        String failureMessage = res.getString(R.string.networking_error);
                        if (e != null) {
                            failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                        }
                        postFailureMessage(handler, null, msg, 2, total, progress, failureMessage, "submitting submitting farmer");
                        FirebaseCrash.report(new Exception(failureMessage, e));
                    }

                });
            }
        }
    }

    private String getEncodedImage(String farmerPhotoPath) {
        File photo = new File(farmerPhotoPath);
        Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }


    public static String getAccessTokenIsAlive(Context context) {
        String key = null;
        try {
            key = new AccessTokenTask(context, null).execute(" ").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return key;
    }

}
