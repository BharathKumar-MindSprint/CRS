package com.olam.digital.ofis.authentication;

import android.content.Context;


import com.olam.digital.ofis.R;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by nitin.kumar35 on 8/19/2017.
 */

public class AuthenticationClient {

    public static String API_KEYCLOAK_URL;

    private AuthenticationClient() {
        API_KEYCLOAK_URL = "";
    }

    // Authentication KeyCloak
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder;

    private static Retrofit retrofit;

    private static void setup(Context context) {
        API_KEYCLOAK_URL = context.getString(R.string.AUTH_SERVER_URL);
        if (builder == null) {
            builder =
                    new Retrofit.Builder()
                            .baseUrl(API_KEYCLOAK_URL)
                            .addConverterFactory(GsonConverterFactory.create());
        }
        if (retrofit == null) {
            retrofit = builder.build();
        }
    }

    public static <S> S createService(
            Class<S> serviceClass, Context context) {
        setup(context);
//        TokenInterceptor interceptor =
//                new TokenInterceptor();
        httpClient.connectTimeout(60, TimeUnit.MINUTES);
        httpClient.readTimeout(60, TimeUnit.MINUTES);
        httpClient.addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        httpClient.sslSocketFactory(getSSLSocketFactory());
        httpClient.retryOnConnectionFailure(false);
        builder.client(httpClient.build());
        retrofit = builder.build();
        return retrofit.create(serviceClass);
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
}