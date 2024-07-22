package com.olam.digital.ofis.RetrofitServices;

import com.olam.digital.ofis.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = BuildConfig.HOST;
    public static Retrofit getRetrofit(){
        Interceptor interceptor=new com.olam.digital.ofis.RetrofitServices.Interceptor();
         OkHttpClient okHttpClient=new OkHttpClient.Builder()
                 .connectTimeout(540, TimeUnit.SECONDS)
                 .readTimeout(540, TimeUnit.SECONDS)
                 .writeTimeout(540, TimeUnit.SECONDS)
                 .addInterceptor(interceptor)
                 .build();

        Retrofit retrofit =new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build();
        return retrofit;

    }
}
