package com.olam.digital.ofis.tasks;

import android.content.Context;
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
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
abstract class SyncTask extends AsyncTask<SyncTaskOperationType, Object, String> {

    private static final String BASE_URL = BuildConfig.HOST;
    private static final String ODBASE_URL = BuildConfig.ODHOST;
    final Context context;

    private static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(540, TimeUnit.SECONDS)
            .readTimeout(540, TimeUnit.SECONDS)
            .writeTimeout(540, TimeUnit.SECONDS)
            .sslSocketFactory(getSSLSocketFactory())
            .retryOnConnectionFailure(false)
            .addInterceptor(interceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
            .addNetworkInterceptor(interceptor)
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

//            final SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//            return sslSocketFactory;

            SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            // SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
            final SSLSocketFactory sslSocketFactory = sslcontext.getSocketFactory();
            return sslSocketFactory;


        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }

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
//        String token="";
//        try {
//            token=new AccessTokenTask(context, null).execute().get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        Request.Builder requestBuilder = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id","1")
                .header("Content-Type","application/json")
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

    void okHttpODPost(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String fullUrl = getAbsoluteUrl(url);
        RequestBody body;
        Request.Builder requestBuilder = new Request.Builder()
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
        //okHttpClient.readTimeoutMillis();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);
        RequestBody body;
//        String token="";
//        try {
//            token=new AccessTokenTask(context, null).execute().get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
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


    void okHttpODGet(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String fullUrl = getODAbsoluteUrl(url);
        Request.Builder requestBuilder = new Request.Builder()
                .header("Authorization", "Bearer " +   Utilities.getdatafromSharedPrefs(context, "ODTOKEN"))
                .url(fullUrl);
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
                .header("Authorization", "Bearer " +  new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
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

    private static String getODAbsoluteUrl(String relativeUrl) {
        return ODBASE_URL + relativeUrl;
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

    void okHttpGetChild(Context context, String url, JSONObject params, Callback callback) {
        okHttpClient.readTimeoutMillis();
        String enumeratorId = Utilities.getFromSharedPrefs(context);
        String imeiNumber = Utilities.getIMEI(context);
        String uniqueId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String base64EnumeratorId = Base64.encodeToString(enumeratorId.getBytes(), Base64.NO_WRAP);
        String base64ImeiNumber = Base64.encodeToString(imeiNumber.getBytes(), Base64.NO_WRAP);
        String base64UniqueId = Base64.encodeToString(uniqueId.getBytes(), Base64.NO_WRAP);
        String fullUrl = getAbsoluteUrl(url);
        RequestBody body;
//        String token="";
//        try {
//            token=new AccessTokenTask(context, null).execute().get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        Request.Builder requestBuilder = new Request.Builder()
                .header("enumerator-id", base64EnumeratorId)
                .header("imei-number", base64ImeiNumber)
                .header("unique-id", base64UniqueId)
                .header("app-version", BuildConfig.VERSION_NAME)
                .header("App-Id", "1")
                .header("Authorization", "Bearer " + new AuthenticationHelper(context).getAuthenticationDTO().getAccessToken())
                .url("http://www.mocky.io/v2/5d619ffb32000059008e6134");


//        if (params != null) {
//            body = RequestBody.create(JSONBody, params.toString());
//        } else {
//            body = RequestBody.create(JSONBody, "");
//        }

        requestBuilder.get();

        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(callback);
    }


}
