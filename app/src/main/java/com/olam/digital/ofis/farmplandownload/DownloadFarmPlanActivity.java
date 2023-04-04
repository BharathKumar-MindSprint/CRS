package com.olam.digital.ofis.farmplandownload;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.ManageTrainingRecords.Services.ManageRecords;
import com.olam.digital.ofis.ManageTrainingRecords.Services.RetrofitClient;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenReqBody;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenResp;
import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;
import com.olam.digital.ofis.farmplandownload.model.DownloadUrlRequestBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownloadFarmPlanActivity extends BaseActivity implements ItemClickListener  {
    private RecyclerView recyclerView;
    DownloadFarmPlanAdapter downloadFarmPlanAdapter;
    List<DownloadFormList.data> data1;
    ItemClickListener itemClickListener;
    String enumeratorId;
    String userID = "ID_OFIS_DO_INTERFACE";
    ArrayList<String> isoCodes = new ArrayList<>();
    private ProgressDialog progressDialog;
    String counrtyCodeNew= "ID_OFIS_DO_INTERFACE";
    String password = "Olam123+";
    String system = "od";
    String odAccessToken;
    String rootobject;
    String filepath;


    private long downloadId;

    TextView tv_down;
    String downloadUrl;
    String fileNameDownload = "";
    File file;
    Uri uri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_farm_plan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.PleaseWait));
        progressDialog.setMessage(getString(R.string.Loading));
        progressDialog.show();


        tv_down = findViewById(R.id.tv_down);
        tv_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getDownloadUrl();

            }
        });

        recyclerView = findViewById(R.id.rvFarmPlanList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

         enumeratorId = Utilities.getFromSharedPrefs(context);

        try {

            Gson gson = new Gson();
            String jsonText = Utilities.getdatafromSharedPrefs(DownloadFarmPlanActivity.this, "COUNTRY_CODE");
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            isoCodes = gson.fromJson(jsonText, type);
            int countrycount = Utilities.getIntFromSharedPrefs(DownloadFarmPlanActivity.this, "COUNTRY_COUNT");
            //String counrtyCode = isoCodes.get(countrycount-1);


            int countryCodeee = Utilities.getIntFromSharedPrefs(context, "COUNTRY_CODENEW");
            String counrtyCode_UserId = String.valueOf(countryCodeee) +"_OFIS_DO_INTERFACE";
            Log.d("PREF_COUNTRYCOde",counrtyCode_UserId);



        } catch (ActivityNotFoundException e) {

        }

         /*OD Access Token API*/
        if(isOnline(getApplicationContext())){
            GetAccessToken();
        }else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
            progressDialog.cancel();

        }

    }
                              /*Get OdAccessToken*/
    private void GetAccessToken() {
        final AccessTokenReqBody accessTokenReqBody = new AccessTokenReqBody(counrtyCodeNew,password,system);

        ManageRecords GetAccessToken= RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<AccessTokenResp> call=GetAccessToken.getAccessToken(accessTokenReqBody);
        call.enqueue(new Callback<AccessTokenResp>() {
            @Override
            public void onResponse(Call<AccessTokenResp> call, Response<AccessTokenResp> response) {
                if(response.code()==200){
                    progressDialog.hide();

                    odAccessToken = response.body().getAccessToken();
                    //Toast.makeText(DownloadFarmPlanActivity.this,odAccessToken, Toast.LENGTH_SHORT).show();
                    Log.e("OD_AccessTOKEN",odAccessToken);

                    if(isOnline(getApplicationContext())){
                        DownloadFormList();
                    }else {
                        Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
                    }

                }else{
                    showAlert(getString(R.string.Alert_message_to_contact_administrator));
                }
            }
            @Override
            public void onFailure(Call<AccessTokenResp> call, Throwable t) {
                progressDialog.hide();
                Toast.makeText(DownloadFarmPlanActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

                              /*Get Down Load Farm Form List*/
    private void DownloadFormList() {
        ManageRecords AllFarmFormList= RetrofitClient.getRetrofit().create(ManageRecords.class);
        Call<DownloadFormList> call=AllFarmFormList.getAllFarmFormList(1,counrtyCodeNew, Integer.valueOf(enumeratorId),odAccessToken);
        call.enqueue(new Callback<DownloadFormList>() {

            @Override
            public void onResponse(Call<DownloadFormList> call, Response<DownloadFormList> response) {
               Log.e("ListOnResponse",response.message());
               Log.e("ListOnRequest",call.toString());

                if(response.code()==200){
                    Toast.makeText(DownloadFarmPlanActivity.this, "Farm Plan is available to download", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    if(response.body()!=null){
                        if(response.body().getData()!=null){
                            data1 =response.body().getData();


                            Log.e("ListRespValue",response.body().getStatusMessage());

                            downloadFarmPlanAdapter=new DownloadFarmPlanAdapter(DownloadFarmPlanActivity.this,response.body().getData(),DownloadFarmPlanActivity.this);
                            recyclerView.setAdapter(downloadFarmPlanAdapter);

                        }else{

                            Toast.makeText(DownloadFarmPlanActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    }else{

                        Toast.makeText(DownloadFarmPlanActivity.this, R.string.no_data_found, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    progressDialog.hide();
                    showAlert(getString(R.string.Alert_message_to_contact_administrator));
                }
            }
            @Override
            public void onFailure(Call<DownloadFormList> call, Throwable t) {
                progressDialog.hide();
                Toast.makeText(DownloadFarmPlanActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getDownloadUrl(){

        Interceptor interceptor=new com.olam.digital.ofis.ManageTrainingRecords.Services.Interceptor();
        OkHttpClient okHttpClient=new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BuildConfig.DOWNLOADURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ManageRecords retrofitAPI = retrofit.create(ManageRecords.class);


        String currentString = filepath;
        String[] separated = currentString.split("/");
        String finalFilepath = separated[1].trim();
        String finalrootobject = rootobject;

        Log.d("FINALRootObj",finalrootobject);
        Log.d("FINALFilePATH",finalFilepath);

        DownloadUrlRequestBody modal = new DownloadUrlRequestBody(finalrootobject,finalFilepath);

        Call<ResponseBody> call = retrofitAPI.getDownloadUrl(modal);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    progressDialog.hide();
                   // Log.d("DownloadServerSuccess", "file download was a success? " + writtenToDisk);
                   // Toast.makeText(DownloadFarmPlanActivity.this, "Data added to API"+response.code(), Toast.LENGTH_SHORT).show();

                    boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                    if (writtenToDisk) {
                        File mediaStorageDir = new File(
                                Environment
                                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                "OFIS");

                        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                                + "Crop_Origin_FarmPlan " + ".pdf");

                        fileNameDownload = mediaFile.getName();

                        //fileNameDownload = "Statement_" + request.getAccountNumber() + "_from" + ".pdf";
                        //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileNameDownload);
                         file = new File(Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                + "/" + fileNameDownload);
                         uri = FileProvider.getUriForFile(DownloadFarmPlanActivity.this, BuildConfig.APPLICATION_ID + ".provider",file);
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(DownloadFarmPlanActivity.this, "No PDF reader found to open this file.", Toast.LENGTH_SHORT).show();
                        }

                    }

                } else {
                    Log.d("DownloadServerFailed", "server contact failed");
                }



            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // setting text to our text view when

            }
        });
    }



         private boolean writeResponseBodyToDisk(ResponseBody body) {
             try {
                 // todo change the file location/name according to your needs
                 File mediaStorageDir = new File(
                         Environment
                                 .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                         "OFIS");

                 File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                         + "Crop_Origin_FarmPlan " + ".pdf");

                 fileNameDownload = mediaFile.getName();

                 //fileNameDownload = "File name" + request.getAccountNumber() + "_from" + time.format("%k : %M : %S") + ".pdf";
                 //File futureStudioIconFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileNameDownload);
                 File futureStudioIconFile = new File(Environment
                         .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                         + "/" + fileNameDownload);
                 InputStream inputStream = null;
                 OutputStream outputStream = null;
                 try {
                     byte[] fileReader = new byte[4096];
                     long fileSize = body.contentLength();
                     long fileSizeDownloaded = 0;
                     inputStream = body.byteStream();
                     outputStream = new FileOutputStream(futureStudioIconFile);
                     while (true) {
                         int read = inputStream.read(fileReader);
                         if (read == -1) {
                             break;
                         }
                         outputStream.write(fileReader, 0, read);
                         fileSizeDownloaded += read;
                         Log.d("FileDownLoad", "file download: " + fileSizeDownloaded + " of " + fileSize);
                     }
                     outputStream.flush();
                     return true;
                 } catch (IOException e) {
                     return false;
                 } finally {
                     if (inputStream != null) {
                         inputStream.close();
                     }
                     if (outputStream != null) {
                         outputStream.close();
                     }
                 }
             } catch (IOException e) {
                 return false;
             }
         }




    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle(R.string.Alert_txt);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public boolean isOnline(Context c){
        ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo ni=cm.getActiveNetworkInfo();
        if(ni !=null && ni.isConnectedOrConnecting()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onClick(String fileUrl, String fileName) {

         rootobject = fileUrl;
         filepath =fileName;
        File fileChk = new File(String.valueOf(uri));
        if (fileChk.isFile()) {
            Toast.makeText(DownloadFarmPlanActivity.this, uri + "/n exists", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(DownloadFarmPlanActivity.this, getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();

        }

        if(isOnline(getApplicationContext())){
            getDownloadUrl();
        }else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }


    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId == id) {
                Toast.makeText(DownloadFarmPlanActivity.this, getResources().getString(R.string.downloading_farm_plan), Toast.LENGTH_SHORT).show();

                Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                pdfOpenintent.setDataAndType(uri, "application/pdf");
                try {
                    startActivity(pdfOpenintent);
                } catch (ActivityNotFoundException e) {

                }

            }

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //checkPermission();
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(onDownloadComplete);
    }

}