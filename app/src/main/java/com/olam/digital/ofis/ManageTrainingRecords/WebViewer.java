package com.olam.digital.ofis.ManageTrainingRecords;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.olam.digital.ofis.BaseActivity;
import com.olam.digital.ofis.ManageTrainingRecords.Services.ManageRecords;
import com.olam.digital.ofis.ManageTrainingRecords.model.ViewFile;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.ManageTrainingRecords.Services.RetrofitClient;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebViewer extends BaseActivity {
    WebView webView;
    Integer Docid;
    RelativeLayout Progressbar;
    String FILENAME="Filename";
    String DOCID="DocId";
    String Url;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_viewer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent mIntent = getIntent();
        int intValue = mIntent.getIntExtra(DOCID, 0);
        String Title=mIntent.getStringExtra(FILENAME);
        if(Title!=null){
            setTitle(Title);
        }else{
            setTitle(getString(R.string.document_viewer));
        }
        Docid=intValue;
        webView = findViewById(R.id.doc_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.requestFocus();
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.getSettings().setLoadsImagesAutomatically(true);
                webView.setVisibility(View.VISIBLE);
                Log.v("after load", view.getUrl());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Progressbar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
                Log.e("error", description);

            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        Progressbar=findViewById(R.id.Progressbar);
        Progressbar.setVisibility(View.VISIBLE);
        if(intValue>0){
            ManageRecords Bytes= RetrofitClient.getRetrofit().create(ManageRecords.class);
            Call<ViewFile> call=Bytes.getBytes(intValue);
           call.enqueue(new Callback<ViewFile>() {
               @Override
               public void onResponse(Call<ViewFile> call, Response<ViewFile> response) {
                   if(response.body().getStatusCode()==200){
                       Url=response.body().getData();
                       String finalUrl = null;
                           try {
                               finalUrl = "https://docs.google.com/viewer?embedded=true&url=" + URLEncoder.encode(Url, "ISO-8859-1");
                           } catch (UnsupportedEncodingException e) {
                               e.printStackTrace();
                           }
                           webView.loadUrl(finalUrl);

                   }

               }

               @Override
               public void onFailure(Call<ViewFile> call, Throwable t) {
                   Progressbar.setVisibility(View.INVISIBLE);
                   showAlert(getString(R.string.failed_to_load_document));
               }
           });
        }
    }
    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle(getString(R.string.Alert_txt));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) (dialog, which) -> {
            Intent intent = new Intent(context, ManagingTrainingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}


