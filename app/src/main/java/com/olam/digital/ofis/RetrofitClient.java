package com.olam.digital.ofis;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = BuildConfig.HOST;
    public static Retrofit getRetrofit(){
        Interceptor interceptor=new com.olam.digital.ofis.Interceptor();
         OkHttpClient okHttpClient=new OkHttpClient.Builder()
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
