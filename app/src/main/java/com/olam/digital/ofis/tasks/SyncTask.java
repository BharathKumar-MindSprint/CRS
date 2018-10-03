package com.olam.digital.ofis.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Base64;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.authentication.AuthenticationHelper;

import org.json.JSONArray;
import org.json.JSONObject;

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

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
abstract class SyncTask extends AsyncTask<SyncTaskOperationType, Object, String> {

    private static final String BASE_URL = BuildConfig.HOST;
    final Context context;
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .sslSocketFactory(getSSLSocketFactory())
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
    boolean isPhoneOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    void okHttpPost(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);
        RequestBody body;

        Request.Builder requestBuilder = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id","1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl);

        if (params != null) {
            body = RequestBody.create(JSONBody, params.toString());
        } else {
            body = RequestBody.create(JSONBody, "");
        }

        requestBuilder.post(body);

        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(callback);
    }

    void okHttpGet(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);
        RequestBody body;

        Request.Builder requestBuilder = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id", "1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl);


//        if (params != null) {
//            body = RequestBody.create(JSONBody, params.toString());
//        } else {
//            body = RequestBody.create(JSONBody, "");
//        }

        requestBuilder.get();

        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(callback);
    }

    void okHttpGetConditionalLogic(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        RequestBody body;

        Request.Builder requestBuilder = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id", "1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(url);


//        if (params != null) {
//            body = RequestBody.create(JSONBody, params.toString());
//        } else {
//            body = RequestBody.create(JSONBody, "");
//        }

        requestBuilder.get();

        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(callback);
    }


    void okHttpArrayPost(Context context, String url, JSONArray params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);
        RequestBody body;

        Request.Builder requestBuilder = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id","1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl);

        if (params != null) {
            body = RequestBody.create(JSONBody, params.toString());
        } else {
            body = RequestBody.create(JSONBody, "");
        }

        requestBuilder.post(body);

        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(callback);
    }
    void okHttpGetPost(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
//        String fullUrl = getAbsoluteUrl(url);
        RequestBody body;

        Request.Builder requestBuilder = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .url(url);


//        requestBuilder.get();
        if (params != null) {
            body = RequestBody.create(JSONBody, params.toString());
        } else {
            body = RequestBody.create(JSONBody, "");
        }

        requestBuilder.post(body);

        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(callback);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }


    // use sync farmers & sync spinners in PHP Codes

    private static String getAbsoluteUrlPHP(String relativeUrl) {
        return "https://stageofis.olamgroup.com/api/v1/" + relativeUrl;
    }


    void okHttpPostPHP(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrlPHP(url);
        RequestBody body;

        Request.Builder requestBuilder = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id","1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url(fullUrl);

        if (params != null) {
            body = RequestBody.create(JSONBody, params.toString());
        } else {
            body = RequestBody.create(JSONBody, "");
        }

        requestBuilder.post(body);

        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(callback);
    }

}
