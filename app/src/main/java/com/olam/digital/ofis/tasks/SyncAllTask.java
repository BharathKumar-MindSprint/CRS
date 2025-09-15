package com.olam.digital.ofis.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.Common.Constants.IAPIConstants;
import com.olam.digital.ofis.Common.Constants.StringConstants;
import com.olam.digital.ofis.HomeActivity;
import com.olam.digital.ofis.OFISDBContract;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.agrisupplier.dto.SupplierModuleResponse;
import com.olam.digital.ofis.agrisupplier.dto.SupplierResponse;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSubmittedModuleResponse;
import com.olam.digital.ofis.authentication.AuthenticationClient;
import com.olam.digital.ofis.authentication.AuthenticationHelper;
import com.olam.digital.ofis.authentication.AuthenticationInterface;
import com.olam.digital.ofis.authentication.DeviceIdExtractor;
import com.olam.digital.ofis.authentication.KeyCloakInfo;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.olam.digital.ofis.childlabour.ChildLabourUtils;
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionCategory;
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionGroup;
import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionItem;
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.DistributionModuleResponseDto;
import com.olam.digital.ofis.downloadsurvey.DownloadSurveyData;
import com.olam.digital.ofis.downloadsurvey.dto.AnswerRequestDTO;
import com.olam.digital.ofis.farmplandownload.model.DownloadUrlRequestBody;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.APICall.LocalUnitResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.GlobalUnit;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.ListItems.OutputData;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.LocalUnit;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.followup.FollowUpResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder;
import com.olam.digital.ofis.mvvmmodule.model.photocheckbox.PhotoCheckBoxData;
import com.olam.digital.ofis.mvvmmodule.model.photocheckbox.ResponseModel.PhotoCheckBoxResponseModel;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Currency;
import com.olam.digital.ofis.recordpolygon.SharedPreferenceUtils;

import org.json.JSONArray;
import android.os.Handler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.olam.digital.ofis.Common.Utils.NetworkUtils.isNetworkAvailable;
import static com.olam.digital.ofis.tasks.SyncTaskOperationType.SYNC_DISTRIBUTION_MODULES_DETAILS;

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
    public JSONObject spinnerResponse, moduleResponse, sipModuleResponse, farmerResponse, fmpResponse, trainingResponse, tasksResponse, answerResponse;
    public static JSONObject childResponse,assignmentCheckResponse, notificationResponse, conditionalLogicResponse, distributionModuleResponse = new JSONObject();
    public static JSONObject nationalidresponse,enumnationalidresponse;
    public static  JSONObject multifarmRoleResponse,stakeHolderResponse;
    public static JSONObject localUnitResponse, photoCheckBoxResponse, dmResponseObject, dmFollowUpResponse, supplierResponse, supplierSurveyResponse, supplierSurveySubmittedResponse;
    public static JSONArray notificationArray = new JSONArray();
    public static JSONArray moduleArray = new JSONArray();
    private int syncResultCount;
    public ProgressDialog pd;
    private Resources res;
    ArrayList<String> isoCodes = new ArrayList<>();
    ArrayList<String> countryIds = new ArrayList<>();
    private SyncAllTaskDelegate delegate;
    AnswerRequestDTO answerRequestDTO;
    int notificationModuleId;
    int repeaterCount ;

    OFISRoomDB roomDB = OFISRoomDB.getDbInstance(context);

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
            if (isNetworkAvailable(context)) {

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
        } else if (op == SyncTaskOperationType.SYNC_ENUM_ASSIGNMENT) {
            syncEnumAssignmentCheck();

        }else if (op == SyncTaskOperationType.SYNC_LOCAL_UNIT) {
            try {
                syncLocalUnit();
            }catch (Exception e){
                e.printStackTrace();
            }
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
        }
        else if (op == SyncTaskOperationType.SYNC_MULTIFARM_ROLES) {
            syncMultiFarmRoles();
        }
        else if (op == SyncTaskOperationType.SYNC_EXISTING_STAKEHOLDERS) {
            syncExistingStakeHolders();
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
        } else if ( op == SyncTaskOperationType.SYNC_PHOTO_CHECKBOX) {
            syncPhotoCheckBoxData(SharedPreferenceUtils.getInstance(context).getPhotoCheckBoxSyncDate());
        }
        else if (op == SyncTaskOperationType.SYNC_DM_MODULES) {
            GetAllDiagnosticModule();
        }
        else if (op == SyncTaskOperationType.SYNC_DM_FOLLOW_UP_MODULES) {
            GetAllFollowUpDMData();
        }
        else if (op == SyncTaskOperationType.SYNC_SUPPLIER_DATA) {
            syncSupplierDetails();
        }
        else if (op == SyncTaskOperationType.SYNC_SUPPLIER_SURVEY_DATA) {
            syncSupplierSurveys();
        }
        else if (op == SyncTaskOperationType.SYNC_SUPPLIER_SUBMITTED_MODULE) {
            getAllSubmittedModules();
        }
        else if (op == SyncTaskOperationType.SYNC_CONDITIONAL_DETAILS) {
            syncConditionalLogicDetails();
        }
        else if (op == SYNC_DISTRIBUTION_MODULES_DETAILS) {
            getDistributionModuleData();
        }

        return null;

    }

    // Keycloak check version and Firebase token
    private void syncDeviceInfo() {
        JSONObject inputArgs = new JSONObject();
        try {
            inputArgs.put("deviceId", DeviceIdExtractor.fetchDeviceID(context));
            inputArgs.put("firebaseToken",Utilities.getFirebaseTokenSharedPrefs(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        okHttpPost(context, IAPIConstants.SAVE_DEVICE_INFO, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {

                if (postResponse.isSuccessful()) {
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
            long enumeratorId = Long.parseLong(enumerator_id);
            inputArgs.put("enumeratorId", enumeratorId);
        }
        okHttpPost(context, IAPIConstants.GET_ALL_SIP_QUESTIONNAIRE_DATA, inputArgs, new Callback() {
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
            // Goes directly to the AsyncTask
            new UpdateFarmersTask(context).execute(farmerResponse);

            new UpdateSpinnerListsTask(context, pd).execute(spinnerResponse);
            spinnerResponse = null;

            new UpdateModuleListsTask(context).execute(moduleResponse, conditionalLogicResponse);
            moduleResponse = null;
            conditionalLogicResponse = null;

            // Goes directly to the AsyncTask
            new UpdateSipModuleListsTask(context).execute(sipModuleResponse);
            sipModuleResponse = null;


            new UpdateMultiFarmRoleTask(context).execute(multifarmRoleResponse);
            multifarmRoleResponse = null;

            // Goes directly to the AsyncTask
            new UpdateTrainingListsTask(context).execute(trainingResponse);
            trainingResponse = null;

            // Goes directly to the AsyncTask
            new UpdateTasksListsTask(context).execute(tasksResponse);
            tasksResponse = null;

            new UpdateChildrenTask(context).execute(childResponse);
            childResponse = null;

            new UpdateAssignmentTask(context).execute(assignmentCheckResponse);
            assignmentCheckResponse = null;

            new UpdateNationalIdTask(context).execute(nationalidresponse);
            nationalidresponse = null;

            new UpdateEnumNationalIdTask(context).execute(enumnationalidresponse);
            enumnationalidresponse = null;

            new UpdateStakeHolderResponse(context).execute(stakeHolderResponse);
            StakeHoldersResponse(stakeHolderResponse);
            stakeHolderResponse = null;

            new UpdateNotificationTask(context).execute(notificationResponse);
            notificationResponse = null;

            new UpdateDiagnosticModuleTask(context, dmResponseObject, dmFollowUpResponse, res, farmerResponse).execute(farmerResponse);
            farmerResponse = null;
            dmResponseObject = null;
            dmFollowUpResponse = null;

            new UpdateSupplierTask(context).execute(supplierResponse,supplierSurveyResponse, supplierSurveySubmittedResponse);
            supplierResponse = null;
            supplierSurveyResponse = null;

            LocalUnitResponse(localUnitResponse);
            localUnitResponse = null;

            PhotoCheckBoxResponse(photoCheckBoxResponse);
            photoCheckBoxResponse = null;

            updateDistributionModuleResponse(distributionModuleResponse);
            distributionModuleResponse = null;

            if(Utilities.getBoolFromSharedPrefs(context, "FIRST_RUN", true)) {
                HomeActivity.completeFirstSetup();
            }
            if (!roomDB.photoCheckBoxDataDao().getNoPhotoData(false).isEmpty()){
                photoCheckboxDownload();
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
                String columnname = "responseMessage" + Utilities.getLocaleSuffix(res);
                if (columnname.equalsIgnoreCase("responseMessage_en")) {
                    columnname = "responseMessage";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(response.optString(columnname, res.getString(R.string.app_updated_generic_message)))
                        .setCancelable(true)
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.optString("downloadUrl")));
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(res.getString(R.string.app_update_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                long daysLeftToUpdate = Utilities.daysToUpdate(context);
                                if (daysLeftToUpdate >= 0) {
                                    dialog.cancel();
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
        String failureMessage = context.getString(R.string.item_sync_error);
        if (response != null) {
            failureMessage = response.optString("errors");
        }
        publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
    }

    private void checkVersion() throws JSONException {

        long timestampUpdated = System.currentTimeMillis();
        Utilities.saveLongToSharedPrefs(context, "version_checked", timestampUpdated);
        JSONObject inputArgs = new JSONObject();
        inputArgs.put("enumerator_id", Long.parseLong(Utilities.getFromSharedPrefs(context)));
        inputArgs.put("current_version", BuildConfig.VERSION_NAME);

        okHttpPost(context, IAPIConstants.CHECK_APP_VERSION, inputArgs, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    if (postResponse.code() == 401) {
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

                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.update_firebase_install_id_info));
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_BASIC_DEVICE_INFO);


                            }
                        } else {
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
        okHttpGet(context, IAPIConstants.ENUMERATOR_ASSIGNMENT_CHECK, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
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
                                Utilities.saveToSharedPrefs(context, Utilities.IS_SUPPLIER_ENABLED, null, item.getBoolean(Utilities.IS_SUPPLIER_ENABLED));

                                if(isODOrigin){
                                    String code = item.getString("countryIsoCode").substring(0,2);
                                    isoCodes.add(code);
                                    String countryId = item.getString("countryId");
                                    countryIds.add(countryId);
                                }
                            }
                            assignmentCheckResponse = response;
                            Gson gson = new Gson();
                            List<String> textList = new ArrayList<String>(isoCodes);
                            List<String> cidsList = new ArrayList<String>(countryIds);
                            String jsonText = gson.toJson(textList);
                            String cid = gson.toJson(cidsList);
                            Utilities.saveToSharedPrefs(context, "COUNTRY_CODE", jsonText, null);
                            Utilities.saveToSharedPrefs(context, "COUNTRY_IDS", cid, null);
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_isfl_enumerator));
                            //   publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_farmers));
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_FARMERS);

                        }catch (Exception e){e.printStackTrace();}



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

    private void syncSpinners() {
        okHttpGet(context, IAPIConstants.COUNTRY_SPINNERS, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                            String enumName= response.getJSONObject("outputdata").get(StringConstants.ENUMERATOR_NAME).toString();
                            Utilities.setEnumeratorNameSharedPrefs(context,StringConstants.ENUMERATOR_NAME,enumName);
                        try {
                            JSONArray countryArray = response.getJSONObject("outputdata").getJSONArray("countries");
                            JSONObject item = new JSONObject();
                            int length = countryArray.length();
                            for (int j = 0; j < length; j++) {
                                item = countryArray.getJSONObject(j);
                                String countryCode = item.getString("countryIsoCode");
                                Utilities.saveToSharedPrefs(context, "COUNTRY_CODE", countryCode.substring(0,2), null);
                            }
                        }catch (Exception e){e.printStackTrace();}
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        if (response.getString("status").equals("unchanged")) {

                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.spinners_up_to_date));

                        }
                        spinnerResponse = response;
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.download_nationalidtype_enumerator));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_NATIONALIDS);

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

    private void syncLocalUnit() throws JSONException{
        ManageRecords getLocalUnitMasterData = RetrofitClient.getRetrofit().create(ManageRecords.class);
        retrofit2.Call<LocalUnitResponseModel> call = getLocalUnitMasterData.getAllLocalUnitResponseModel();

        call.enqueue(new retrofit2.Callback<LocalUnitResponseModel>() {
            @Override
            public void onResponse(retrofit2.Call<LocalUnitResponseModel> call, retrofit2.Response<LocalUnitResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        LocalUnitResponseModel responseBody = response.body();
                        Gson gson = new Gson();
                        String jsonString = gson.toJson(responseBody);
                        localUnitResponse = new JSONObject(jsonString);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.survey_data));
                    delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_PHOTO_CHECKBOX);

                }
            }

            @Override
            public void onFailure(retrofit2.Call<LocalUnitResponseModel> call, Throwable t) {

                String failureMessage = res.getString(R.string.networking_error);
                if (t != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing modules", t));
                    failureMessage += (t.getMessage() != null) ? t.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }
        });

    }

    public void syncPhotoCheckBoxData(String syncDate) {
        ManageRecords getPhotoCheckBoxData = RetrofitClient.getRetrofit().create(ManageRecords.class);
        List<PhotoCheckBoxData> photoModule = roomDB.photoCheckBoxDataDao().getPhotoModuleId();

        List<Integer> allModuleId = new ArrayList<>();
        if (!photoModule.isEmpty()) {
            for (int i = 0; i < photoModule.size(); i++) {
                int moduleId = Integer.parseInt(photoModule.get(i).moduleId);
                allModuleId.add(moduleId);
            }
        }

        Map<String, Object> photoModuleList = new HashMap<>();
        photoModuleList.put("lastSyncModuleId", allModuleId);

        String finalJson = new Gson().toJson(photoModuleList);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), finalJson);

        retrofit2.Call<PhotoCheckBoxResponseModel> call = getPhotoCheckBoxData.getAllPhotoCheckBoxData(1, syncDate, requestBody);

        call.enqueue(new retrofit2.Callback<PhotoCheckBoxResponseModel>() {
            @Override
            public void onResponse(retrofit2.Call<PhotoCheckBoxResponseModel> call, retrofit2.Response<PhotoCheckBoxResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getPhotoCheckBoxList() != null ) {
                    try {
                        PhotoCheckBoxResponseModel responseBody = response.body();
                        Gson gson = new Gson();
                        String jsonString = gson.toJson(responseBody);
                        photoCheckBoxResponse = new JSONObject(jsonString);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.downloading_notification));
                    delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SMART_NOTIFICATION);

                }
            }

            @Override
            public void onFailure(retrofit2.Call<PhotoCheckBoxResponseModel> call, Throwable t) {
                String failureMessage = res.getString(R.string.networking_error);
                if (t != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing modules", t));
                    failureMessage += (t.getMessage() != null) ? t.getMessage() : "";
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

        okHttpGet(context, IAPIConstants.GET_ALL_QUESTIONNAIRE_DATA, new Callback() {
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

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.survey_data));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_CONDITIONAL_DETAILS);
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

    private void syncConditionalLogicDetails() {
        conditionalLogicResponse = new JSONObject();
        okHttpGet(context, IAPIConstants.GET_CONDITIONAL_DETAILS, new Callback() {
            @Override
            public void onResponse(Call call, Response postResponse) {
                try {
                    if (postResponse.isSuccessful()) {
                        JSONArray array = new JSONArray(postResponse.body().string());
                        conditionalLogicResponse.put("conditions", array);
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.download_distribution_data));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SYNC_DISTRIBUTION_MODULES_DETAILS);
                    } else {
                        postFailureMessage(null);
                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing conditional details", e));
                    postFailureMessage(null);
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing conditional details", e));
                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }
        });
    }

    private void getDistributionModuleData() {
        okHttpGet(context, IAPIConstants.GET_DISTRIBUTION_MODULE_DATA, new Callback() {
            @Override
            public void onResponse(Call call, Response postResponse) {
                try {
                    if (postResponse.isSuccessful()) {
                        distributionModuleResponse = new JSONObject(postResponse.body().string());
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.survey_data));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_LOCAL_UNIT);
                    } else {
                        postFailureMessage(null);
                    }
                } catch (JSONException | IOException e) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error syncing conditional details", e));
                    postFailureMessage(null);
                    e.printStackTrace();
                } finally {
                    postResponse.close();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String failureMessage = res.getString(R.string.networking_error);
                FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing conditional details", e));
                failureMessage += (e.getMessage() != null) ? e.getMessage() : "";
                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }
        });
    }

    private void syncChildEnumerator() {
        okHttpGet(context, IAPIConstants.IS_CHILD_ENUMERATOR, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
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
        okHttpGet(context, IAPIConstants.IS_FARMER_HH_ENUMERATOR, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    String output = postResponse.body().string();
                    if (postResponse.isSuccessful()) {
                        // store the value in Child labour preferences
                        Boolean ishousehold = Boolean.parseBoolean(output.trim());
                        Utilities.saveToSharedPrefs(context, Utilities.HOUSEHOLD_ENUMERATOR,null, ishousehold);

                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_child_enumerator));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.CHILD_ENUMERATOR);


                    } else {
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

    private void syncMultiFarmRoles() {
        okHttpGet(context, IAPIConstants.GET_ALL_FARM_ROLES, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        multifarmRoleResponse=response;
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "MultiStakeHolder Existing");
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_EXISTING_STAKEHOLDERS);

                    } else {
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

    private void syncExistingStakeHolders() {
        okHttpGet(context, IAPIConstants.GET_STAKEHOLDER_BY_ENUMERATORID, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        stakeHolderResponse = response;
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.updating_sip_training));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SIP_MODULES);
                    } else {
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


    private void syncNationalIDs() {
        okHttpGet(context, IAPIConstants.GET_NATIONALIDS_BY_ENUMERATOR, new Callback() {
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
        okHttpGet(context, IAPIConstants.GET_ENUMERATOR_UNIQUEID, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());

                    if (postResponse.isSuccessful()) {

                        enumnationalidresponse = response;


                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_hhgps_enumerator));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_HHGPS);

                    } else {
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
        okHttpGet(context, IAPIConstants.SYNC_TRAINING_DATA, new Callback() {
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
        okHttpGet(context, IAPIConstants.SYNC_TASKS, new Callback() {

            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        tasksResponse = response;
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.checking_enumerator_assignment));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_ENUM_ASSIGNMENT);
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
        okHttpGet(context, IAPIConstants.GET_FARMER_DETAILS_ENUMERATOR_ID, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());

                    if (postResponse.isSuccessful()) {
                        Utilities.isAppAlive = true;
                        syncResultCount = syncResultCount + response.getInt("resultcount");
                        String progress = res.getString(R.string.importing_string).replace("%%", "0").replace("||", String.valueOf(syncResultCount));

                        if (response.getString("status").equals("unchanged")) {
                            progress = res.getString(R.string.farmers_up_to_date);
                        }
                        farmerResponse = response;

                        deleteRoomDbValues();
                        deleteSupplierDetails();
                        if (Utilities.getBoolFromSharedPrefs(context, Utilities.DM_ENUMERATOR, false)) {
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Downloading Diagnostic Module");
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_DM_MODULES);
                        }
                        else if (Utilities.getBoolFromSharedPrefs(context, Utilities.IS_SUPPLIER_ENABLED, false)) {
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Downloading Supplier Details");
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SUPPLIER_DATA);
                        }
                        else {
                            publishProgress(SyncOperation.START_IMPORT, progress, syncResultCount);
                        }

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

    private void syncChildren() throws JSONException {
        okHttpGet(context, IAPIConstants.FIND_CHILD_SUBMITTEDMODULES, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONArray  response = new JSONArray(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        Utilities.isAppAlive = true;
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("child",response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        childResponse=jsonObject;
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.survey_data));
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_MODULES);
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

        String endpoint = IAPIConstants.SAVE_SURVEY_ANSWERS + "?module_id="+Integer.parseInt(answerRequestDTO.getModule_id())
                +"&farmergroup_id="+Integer.parseInt(answerRequestDTO.getFarmerGroupId())
                +"&section_id="+Integer.parseInt(answerRequestDTO.getSectionId());
        okHttpGet(context, endpoint, new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
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

        String endpoint = IAPIConstants.GET_CHILD_DETAILS
                +ChildLabourUtils.PAGENUMBER_CONSTANT+pageNumber
                +ChildLabourUtils.LIMIT_CONSTANT+Utilities.notificationLimit
                +ChildLabourUtils.MODULE_CONSTANT + notificationModuleId;
        okHttpGet(context, endpoint,  new Callback() {
            @Override
            public void onResponse(Call call, final Response postResponse) {
                try {
                    JSONObject response = new JSONObject(postResponse.body().string());
                    if (postResponse.isSuccessful()) {
                        int totalCount = response.getInt(OFISDBContract.NotificationModuleAssignments.COLUMN_TOTALCOUNT);
                        if (pageNumber == 1) {
                            repeaterCount = totalCount / Utilities.notificationLimit;
                        }
                        try {
                            if (totalCount > 0) {
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, res.getString(R.string.downloading_notification));
                        if (repeaterCount > pageNumber) {
                            int nextPage = pageNumber + 1;
                            syncSmartNotification(notificationModuleId, nextPage);
                        } else {
                            try {
                                if (totalCount > 0) {
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String[] ids = context.getResources().getStringArray(R.array.notification_module_id);
                            String[] withoutClmrsIds = context.getResources().getStringArray(R.array.notifications_noclmrs);
                            boolean isChildEnumerator = Utilities.getChildLabourPref(context, ChildLabourUtils.IS_CHILD_ENUMERATOR);
                            int totalModules;
                            if (isChildEnumerator) {
                                totalModules = ids.length;
                            } else {
                                totalModules = withoutClmrsIds.length;
                            }
                            if (totalModules > notificationModuleId) {
                                int nextModule = notificationModuleId + 1;
                                syncSmartNotification(nextModule, 1);
                            } else {
                                try {
                                    notificationResponse = new JSONObject();
                                    notificationResponse.put("notificationDetailsList", notificationArray);
                                    notificationResponse.put("moduleList", moduleArray);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                notificationArray = new JSONArray();
                                moduleArray = new JSONArray();
                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "MultiFarm Roles ");
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_MULTIFARM_ROLES);
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
    public static void deleteAllInActivePhotoData(OFISRoomDB ofisRoomDB, Context context) {
        List<PhotoCheckBoxData> inActivePhoto = ofisRoomDB.photoCheckBoxDataDao().getInactivePhoto(false);
        if (!inActivePhoto.isEmpty()){
            for (int i = 0; i < inActivePhoto.size(); i++) {
                String fileName = inActivePhoto.get(i).fileName;
                File internalStorageDir = context.getFilesDir();
                File imageFile = new File(internalStorageDir, fileName);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }
        }
    }

    public static void deleteInActiveModuleIdData(OFISRoomDB ofisRoomDB, Context context){
        if (!ofisRoomDB.photoCheckBoxDataDao().getPhotoModuleId().isEmpty()) {
            ArrayList<Integer> moduleNotInUse = SharedPreferenceUtils.getInstance(context).getPhotoCheckBoxModuleNotInUse();
            for (int i=0; i < moduleNotInUse.size(); i++) {
                // delete file
                int moduleId = moduleNotInUse.get(i);
                List<PhotoCheckBoxData> fileNamesModuleId = ofisRoomDB.photoCheckBoxDataDao().getFileNameByModuleId(moduleId);
                if (!fileNamesModuleId.isEmpty()){
                    for (int j=0; j<fileNamesModuleId.size(); j++) {

                        // get list of filenames
                        File internalStorageDir = context.getFilesDir();
                        File imageFile = new File(internalStorageDir,fileNamesModuleId.get(j).fileName);
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                    }
                }

                // delete data in db
                ofisRoomDB.photoCheckBoxDataDao().deleteInActiveModulePhoto(moduleId);
            }
        }

    }
    public void photoCheckboxDownload(){
        deleteAllInActivePhotoData(roomDB, context);
        deleteInActiveModuleIdData(roomDB, context);
        roomDB.photoCheckBoxDataDao().deleteDeactivatedPhoto(false);

        if (!roomDB.photoCheckBoxDataDao().getNoPhotoData(false).isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.common_alert_dialog, null);
            builder.setView(dialogView);
            TextView tvMsg = (TextView) dialogView.findViewById(R.id.tv_msg);
            tvMsg.setText(res.getString(R.string.ready_to_download_photo_checkbox));
            Button btnNo = (Button) dialogView.findViewById(R.id.btn_no);
            Button btnYes = (Button) dialogView.findViewById(R.id.btn_yes);
            AlertDialog alert = builder.create();
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                }
            });

            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    if (!isNetworkAvailable(context)) {
                        Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
                        return;
                    }
                    photoCheckboxThreadCall(new ProgressDialog(context), context, roomDB, new Handler());
                }
            });
            alert.show();
        } else {
            Toast.makeText(context, res.getString(R.string.no_images_to_be_downloaded), Toast.LENGTH_SHORT).show();
        }
    }

    public static void photoCheckboxThreadCall(ProgressDialog progressDialog, Context context, OFISRoomDB ofisRoomDB, Handler handler) {
        progressDialog.setMessage(context.getString(R.string.image_download_start));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<PhotoCheckBoxData> photoData = ofisRoomDB.photoCheckBoxDataDao().getNoPhotoData(false);
                int totalRecords = photoData.size();
                int currentRecord = 0;
                if (!photoData.isEmpty()){
                    for (int i=0; i < photoData.size(); i++) {
                        long id = photoData.get(i).id;
                        long questionId = Long.parseLong(photoData.get(i).questionId);
                        long lookupId = Long.parseLong(photoData.get(i).lookupId);
                        String fileName = photoData.get(i).fileName;
                        currentRecord++;
                        Log.d("PHOTOOOOOOOOO", context.getString(R.string.download) + ": " + currentRecord + " of " + totalRecords);
                        String message = context.getString(R.string.downloading) + currentRecord + " of " + totalRecords;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setMessage(message);
                            }
                        });
                        SyncAllTask.PhotoCheckboxAPICALL(id, questionId, lookupId, fileName, context, ofisRoomDB);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(context,context.getString(R.string.image_downloaded), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        thread.start();
    }

    public static void PhotoCheckboxAPICALL(long id, long questionId, long lookupId, String fileName, Context context, OFISRoomDB roomDBPhoto) {
        ManageRecords getPhotoCheckboxImages = RetrofitClient.getRetrofitImage().create(ManageRecords.class);
        DownloadUrlRequestBody modal = new DownloadUrlRequestBody(SharedPreferenceUtils.getInstance(context).getPhotoCheckBoxContainer(),fileName);

        //Synchronous API call for PhotoCheckbox Data
        try {
            retrofit2.Call<ResponseBody> call = getPhotoCheckboxImages.getDownloadUrl(modal);
            retrofit2.Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                byte[] imageBytes = responseBody.bytes();
                // image compression
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                int quality = 80;
                String imageType = getImageType(imageBytes);
                Bitmap.CompressFormat format;
                switch (imageType) {
                    case "jpeg":
                    case "jpg":
                        format = Bitmap.CompressFormat.JPEG;
                        break;
                    case "png":
                        format = Bitmap.CompressFormat.PNG;
                        break;
                    case "webp":
                        format = Bitmap.CompressFormat.WEBP;
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported image format: " + imageType);
                }
                resizedBitmap.compress(format, quality, outputStream);
                byte[] compressedImageBytes = outputStream.toByteArray();
                File internalStorageDir = context.getFilesDir();

                File imageFile = new File(internalStorageDir, fileName);

                FileOutputStream fileWrite = new FileOutputStream(imageFile);
                fileWrite.write(compressedImageBytes);
                fileWrite.close();

                roomDBPhoto.photoCheckBoxDataDao().updatePhotoCheckboxUrl(imageFile.getAbsolutePath(), true,questionId, lookupId, id);


                Log.d("ImageDownload", "Image downloaded and saved to: " + imageFile.getAbsolutePath());
            } else {
                Log.e("ImageDownload", "Error downloading image: " + response.code());
                Log.d("FailureMessage", "Error response body: " + response.errorBody().string());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private static String getImageType(byte[] imageBytes) {
        String base64EncodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        if (base64EncodedImage.startsWith("iVBORw0KGgoAAA")) {
            return "png";
        } else if (base64EncodedImage.startsWith("/9j/")) {
            return "jpeg";
        } else {
            return "unknown";
        }
    }

    private void LocalUnitResponse(JSONObject localUnitResponse){
        try {
            Gson gson = new Gson();
            LocalUnitResponseModel model = gson.fromJson(String.valueOf(localUnitResponse), LocalUnitResponseModel.class);
            if (localUnitResponse != null && model.getOutputData() != null) {
                OFISRoomDB db = OFISRoomDB.getDbInstance(context);
                OutputData outputData = model.getOutputData();
                db.localUnitDao().deleteAll();
                db.globalUnitDao().deleteAll();
                if (outputData != null) {
                    ArrayList<GlobalUnit> globalUnits = outputData.getGlobalUnits();

                    for (GlobalUnit globalUnit : globalUnits) {
                        // Insert global unit into the database
                        db.globalUnitDao().insertAll(globalUnit);

                        ArrayList<LocalUnit> localUnits = globalUnit.getLocalUnit();
                        if (localUnits != null && !localUnits.isEmpty()) {
                            for (LocalUnit localUnit : localUnits) {
                                // Set the foreign key
                                // localUnit.setGlobalUnitFk(globalUnit.getGlobalUnitId());

                                // store the data in a loop
                                db.localUnitDao().insert(localUnit);
                            }
                            // store in a single stretch
                            // db.localUnitDao().insertAll(localUnits);
                        }
                    }

                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void PhotoCheckBoxResponse(JSONObject photoCheckBoxResponse) {
        try {
            Gson gson = new Gson();
            PhotoCheckBoxResponseModel model = gson.fromJson(photoCheckBoxResponse.toString(), PhotoCheckBoxResponseModel.class);
            if (model != null && model.getPhotoCheckBoxList() != null) {
                OFISRoomDB db = OFISRoomDB.getDbInstance(context);
                SharedPreferenceUtils.getInstance(context.getApplicationContext()).setPhotoCheckBoxContainer(model.getContainer());
                SharedPreferenceUtils.getInstance(context.getApplicationContext()).setPhotoCheckBoxSyncDate(model.getSyncDate());
                SharedPreferenceUtils.getInstance(context.getApplicationContext()).setPhotoCheckBoxModuleNotInUse(model.getModuleNotInUse());

                ArrayList<PhotoCheckBoxData> outputData = model.getPhotoCheckBoxList();

                if (outputData != null) {

                    for (PhotoCheckBoxData photoData : outputData) {
                        db.photoCheckBoxDataDao().insert(photoData);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void StakeHoldersResponse(JSONObject stakeHolderResponse) {

        try {
            Gson gson = new Gson();
            JSONObject multiStakeHolderResponseObject = stakeHolderResponse.has("outputData") ? stakeHolderResponse.getJSONObject("outputData") : new JSONObject();
            JSONArray multiStakeHolderResponse = multiStakeHolderResponseObject.has("multiStakeHolders") ? multiStakeHolderResponseObject.getJSONArray("multiStakeHolders") : new JSONArray();
            Type MultiStakeHolderDtoType = new TypeToken<List<MultiStakeHolder>>(){}.getType();
            List<MultiStakeHolder> multiStakeHolder = gson.fromJson(multiStakeHolderResponse.toString(), MultiStakeHolderDtoType);
            roomDB.multiStakeHolderDao().deleteAll();
            roomDB.multiStakeHolderDao().insert(multiStakeHolder);
        }
        catch (Exception e) {
            FirebaseCrash.report(new Exception("UpdateStakeHolderResponse: Error filling StakeHolders table", e));
            e.printStackTrace();
        }
    }

    private void GetAllDiagnosticModule() {
        if (Utilities.getBoolFromSharedPrefs(context, Utilities.DM_ENUMERATOR, false)) {
            ManageRecords AllDiagnosticModuleData = RetrofitClient.getRetrofit().create(ManageRecords.class);
            retrofit2.Call<ResponseModel> call = AllDiagnosticModuleData.getAllDiagnosticModuleData();
            call.enqueue(new retrofit2.Callback<ResponseModel>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseModel> call, retrofit2.Response<ResponseModel> response) {
                    try {
                        if (response.code() == 200) {
                            ResponseModel dmResponse = response.body();
                            dmResponseObject = new JSONObject(new Gson().toJson(dmResponse));

                            if (dmResponse.getErrorMessage() != null && dmResponse.getErrorMessage().equals(Utilities.DM_ERROR_MESSAGE)) {
                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Downloading Supplier Details");
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SUPPLIER_DATA);
                            }
                            else {
                                publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Downloading Diagnostic Modules");
                                delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_DM_FOLLOW_UP_MODULES);
                            }

                        } else {
                            postFailureMessage(null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        postFailureMessage(null);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ResponseModel> call, Throwable t) {
                    String failureMessage = res.getString(R.string.networking_error);
                    if (t != null) {
                        FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing modules", t));
                        failureMessage += (t.getMessage() != null) ? t.getMessage() : "";
                    }

                    publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
                }
            });
        }
        else {
            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Downloading Supplier Details");
            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SUPPLIER_DATA);
        }
    }

    public void GetAllFollowUpDMData() {
        ManageRecords GetAllFollowUpDMData = RetrofitClient.getRetrofit().create(ManageRecords.class);
        retrofit2.Call<FollowUpResponseModel> call = GetAllFollowUpDMData.getAllFollowUpDMData();
        call.enqueue(new retrofit2.Callback<FollowUpResponseModel>() {
            @Override
            public void onResponse(retrofit2.Call<FollowUpResponseModel> call, retrofit2.Response<FollowUpResponseModel> response) {
                try {
                    if (response.code() == 200) {
                        FollowUpResponseModel followUpResponseModel = response.body();
                        dmFollowUpResponse = new JSONObject(new Gson().toJson(followUpResponseModel));
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Downloading Supplier Details");
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SUPPLIER_DATA);
                    } else if (response.code() == 400){
                        publishProgress(SyncOperation.START_IMPORT, "", syncResultCount);
                    } else {
                        postFailureMessage(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    postFailureMessage(null);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<FollowUpResponseModel> call, Throwable t) {
                String failureMessage = res.getString(R.string.networking_error);
                if (t != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing modules", t));
                    failureMessage += (t.getMessage() != null) ? t.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }
        });

    }

    public void syncSupplierDetails() {
        if (Utilities.getBoolFromSharedPrefs(context, Utilities.IS_SUPPLIER_ENABLED, false)) {
            ManageRecords retrofitClient = RetrofitClient.getRetrofit().create(ManageRecords.class);
            retrofit2.Call<SupplierResponse> call = retrofitClient.getAllSupplierData();
            call.enqueue(new retrofit2.Callback<SupplierResponse>() {
                @Override
                public void onResponse(retrofit2.Call<SupplierResponse> call, retrofit2.Response<SupplierResponse> response) {
                    try {
                        if (response.code() == 200) {
                            supplierResponse = new JSONObject(new Gson().toJson(response.body()));
                            publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Downloading Supplier Data");
                            delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SUPPLIER_SURVEY_DATA);
                        }
                        else {
                            postFailureMessage(null);
                        }
                    } catch (Exception e) {
                        Log.e("RESPONSE", "Error with supplier response");
                        e.printStackTrace();
                        postFailureMessage(null);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<SupplierResponse> call, Throwable t) {
                    String failureMessage = res.getString(R.string.networking_error);
                    if (t != null) {
                        FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing modules", t));
                        failureMessage += (t.getMessage() != null) ? t.getMessage() : "";
                    }

                    publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
                }
            });
        }
        else {
            publishProgress(SyncOperation.START_IMPORT, "", syncResultCount);
        }
    }

    public void syncSupplierSurveys() {
        ManageRecords retrofitClient = RetrofitClient.getRetrofit().create(ManageRecords.class);
        retrofit2.Call<SupplierModuleResponse> call = retrofitClient.getAllSupplierSurveys();

        call.enqueue(new retrofit2.Callback<SupplierModuleResponse>() {
            @Override
            public void onResponse(retrofit2.Call<SupplierModuleResponse> call, retrofit2.Response<SupplierModuleResponse> response) {
                try {
                    if (response.code() == 200) {
                        supplierSurveyResponse = new JSONObject(new Gson().toJson(response.body()));
                        publishProgress(SyncOperation.CONTINUE_DOWNLOADING, "Downloading Supplier Submitted Module Data");
                        delegate.didCompleteSyncOperation(SyncAllTask.this, SyncTaskOperationType.SYNC_SUPPLIER_SUBMITTED_MODULE);
                    }
                    else {
                        postFailureMessage(null);
                    }
                }
                catch (Exception e) {
                    Log.e("RESPONSE", "Error with supplier response");
                    e.printStackTrace();
                    postFailureMessage(null);
                }

            }

            @Override
            public void onFailure(retrofit2.Call<SupplierModuleResponse> call, Throwable t) {
                String failureMessage = res.getString(R.string.networking_error);
                if (t != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing modules", t));
                    failureMessage += (t.getMessage() != null) ? t.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }
        });
    }

    public void getAllSubmittedModules() {
        ManageRecords retrofitClient = RetrofitClient.getRetrofit().create(ManageRecords.class);
        retrofit2.Call<SupplierSubmittedModuleResponse> call = retrofitClient.getSupplierSurveySubmittedModules();
        call.enqueue(new retrofit2.Callback<SupplierSubmittedModuleResponse>() {
            @Override
            public void onResponse(retrofit2.Call<SupplierSubmittedModuleResponse> call, retrofit2.Response<SupplierSubmittedModuleResponse> response) {

                try {
                    if (response.code() == 200) {
                        supplierSurveySubmittedResponse = new JSONObject(new Gson().toJson(response.body()));
                        publishProgress(SyncOperation.START_IMPORT, "", syncResultCount);
                    }
                    else {
                        postFailureMessage(null);
                    }
                }
                catch (Exception e) {
                    Log.d("Error", "Issue in supplier survey submitted module response");
                    e.printStackTrace();
                    postFailureMessage(null);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<SupplierSubmittedModuleResponse> call, Throwable t) {
                String failureMessage = res.getString(R.string.networking_error);
                if (t != null) {
                    FirebaseCrash.report(new Exception("SyncAllTask: Error in response when syncing supplier survey modules", t));
                    failureMessage += (t.getMessage() != null) ? t.getMessage() : "";
                }

                publishProgress(SyncOperation.ERROR_RECEIVED, failureMessage);
            }
        });
    }

    private void updateDistributionModuleResponse(JSONObject distributionModuleResponse) {
        try {
            DistributionModuleResponseDto response = new Gson().fromJson(distributionModuleResponse.toString(), DistributionModuleResponseDto.class);
            deleteDistributionDetails();

            roomDB.distributionCategoryDao().insertDistributionCategory(response.getDistributionCategories());
            roomDB.distributionGroupDao().insertDistributionGroup(response.getDistributionGroups());
            roomDB.distributionItemDao().insertDistributionItem(response.getDistributionItems());
            roomDB.currencyDao().insertCurrency(response.getCurrency());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteRoomDbValues() {
        roomDB.dmSectionsItemDao().deleteAll();
        roomDB.dmQuestionsModuleDao().deleteAll();
        roomDB.dmLookupValuesItemDao().deleteAll();
        roomDB.dmMainModuleDao().deleteAll();
        roomDB.dmAnswerTypesDao().deleteAll();
        roomDB.dmQuestionTypesDao().deleteAll();
        roomDB.dmCurrenciesItemDao().deleteAll();
        roomDB.dmTextBoxModuleQuestionsDao().deleteAll();
        roomDB.dmRangeTextBoxModuleQuestionsTableDao().deleteAll();
        roomDB.dmTextBoxModuleQuestionsDao().deleteAll();
        roomDB.dmRangeTextBoxModuleQuestionsTableDao().deleteAll();
        roomDB.complianceIndicatorDao().deleteAll();
        roomDB.dmCurrencyDao().deleteAll();
        roomDB.dmRangeActionPlansItemsDao().deleteAll();
        roomDB.dmSurveyAnswerTableDao().deleteAll();
        roomDB.dmMasterDataDao().deleteAll();
        roomDB.dmRecurrenceDao().deleteAll();
        roomDB.dmModuleAssignmentsDao().deleteAll();
        roomDB.onGroundSurveyNotificationDao().deleteAll();
        roomDB.dmSurveyReportDao().deleteAll();
        roomDB.followUpItemMainTableDao().deleteAll();
        roomDB.dmComplaintFileTableDao().deleteAll();
    }

    private void deleteSupplierDetails() {
        roomDB.supplierDao().deleteAll();
        roomDB.supplierGroupDao().deleteAll();
        roomDB.supplierTypeDao().deleteAll();
        roomDB.supplierSubmittedModuleDao().deleteAll();
    }

    private void deleteDistributionDetails() {
        roomDB.currencyDao().deleteAll();
        roomDB.distributionGroupDao().deleteAll();
        roomDB.distributionItemDao().deleteAll();
        roomDB.distributionCategoryDao().deleteAll();
    }

}