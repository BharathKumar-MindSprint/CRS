package com.olam.digital.ofis.authentication;

import android.content.Context;


import com.olam.digital.ofis.R;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by nitin.kumar35 on 8/19/2017.
 */

public class AuthenticationClient {
    public static String API_BASE_URL;

    private AuthenticationClient() {
        API_BASE_URL = "";
    }

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder;

    private static Retrofit retrofit;

    private static void setup(Context context) {
        API_BASE_URL = context.getString(R.string.AUTH_SERVER_URL);
        if (builder == null) {
            builder =
                    new Retrofit.Builder()
                            .baseUrl(API_BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create());
        }
        if (retrofit == null) {
            retrofit = builder.build();
        }
    }

    public static <S> S createService(
            Class<S> serviceClass, Context context) {
        setup(context);
        httpClient.connectTimeout(60, TimeUnit.MINUTES);
        httpClient.readTimeout(60, TimeUnit.MINUTES);
        builder.client(httpClient.build());
        retrofit = builder.build();
        return retrofit.create(serviceClass);
    }
}
