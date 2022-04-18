package com.olam.digital.ofis;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;



public class PrivacyStatementWebViewActivity extends  BasePrivacyActivity{

    ProgressDialog progressDialog=null;
    WebView webView;
    SharedPreferences prefs;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_web_view);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading Data...");
            progressDialog.setCancelable(false);

            webView = findViewById(R.id.web_view);
            prefs = getSharedPreferences("prefs",MODE_PRIVATE);
             webViewCode();
        }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkConnected()) {
            String msg;
            if (prefs.getBoolean("firstStart",false)) {
                msg =getResources().getString(R.string.privacy_cache_offline);
            }else{
                msg=getResources().getString(R.string.privacy_no_cache_offline);
            }
            showAlertDialogMsg(msg);
        }
    }


    private void webViewCode() {
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

       //
        if (BuildConfig.FLAVOR.contains("oga")){
           // webView.loadUrl("https://stg-msm.olamagri.com/privacy.html");
            // Livr URL
            webView.loadUrl("https://www.olamagri.com/privacy.html");
        }else {
            webView.loadUrl("https://www.ofi.com/privacy.html");
        }
        webView.setWebChromeClient( new MyWebChromeClient());
        webView.setWebViewClient( new webClient());

    }

    public class MyWebChromeClient extends WebChromeClient {

        public void onProgressChanged(WebView view, int newProgress) {

                   if (newProgress < 100) {
                        progressDialog.show();
                    }
                    if (newProgress == 100) {
                        progressDialog.dismiss();
                    }
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    void showAlertDialogMsg(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.common_alert_info, null);
        builder.setView(dialogView);
        TextView tvMsg=(TextView)dialogView.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_ok);

        AlertDialog alert = builder.create();

        AlertDialog finalAlert = alert;
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalAlert.dismiss();
            }
        });
        alert.show();
    }


    public class webClient extends WebViewClient {
        public boolean  shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (isNetworkConnected()){
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("firstStart", true);
                editor.apply();
            }

        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }
}

