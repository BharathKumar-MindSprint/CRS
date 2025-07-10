package com.olam.digital.ofis.tasks;

import android.content.Context;
import android.os.AsyncTask;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.authentication.AuthenticationHelper;

import org.json.JSONObject;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
abstract class SyncTask extends AsyncTask<SyncTaskOperationType, Object, String> {
    final Context context;

    private static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(540, TimeUnit.SECONDS)
            .readTimeout(540, TimeUnit.SECONDS)
            .writeTimeout(540, TimeUnit.SECONDS)
            .sslSocketFactory(getSSLSocketFactory())
            .retryOnConnectionFailure(false)
            .addInterceptor(interceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
            .addNetworkInterceptor(new StethoInterceptor())
            .build();
    private static final MediaType JSONBody = MediaType.parse("application/json; charset=utf-8");

    SyncTask(Context context) {
        this.context = context;
    }
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

            SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslcontext.getSocketFactory();
            return sslSocketFactory;


        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }

    }

    void okHttpPost(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String fullUrl = getAbsoluteUrl(url);
        Request.Builder requestBuilder = new Request.Builder()
                .header("App-Id","1")
                .header("Content-Type","application/json")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl);
        RequestBody body = RequestBody.create(JSONBody, params != null ? params.toString() : "");
        requestBuilder.post(body);
        Request request = requestBuilder.build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    void okHttpGet(Context context, String url, Callback callback) {
        String fullUrl = getAbsoluteUrl(url);
        Request.Builder requestBuilder = new Request.Builder()
                .header("App-Id", "1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl);
        requestBuilder.get();
        Request request = requestBuilder.build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    void okHttpODGet(Context context, String url, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String fullUrl = getODAbsoluteUrl(url);
        Request.Builder requestBuilder = new Request.Builder()
                .header("Authorization", "Bearer " +   Utilities.getdatafromSharedPrefs(context, "ODTOKEN"))
                .url(fullUrl);
        requestBuilder.get();
        Request request = requestBuilder.build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return BuildConfig.BASE_URL + relativeUrl;
    }

    private  String getODAbsoluteUrl(String relativeUrl) {
        return BuildConfig.ODHOST + relativeUrl;
    }

}
