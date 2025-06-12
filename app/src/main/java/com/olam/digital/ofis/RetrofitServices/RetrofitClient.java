package com.olam.digital.ofis.RetrofitServices;

import com.olam.digital.ofis.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static Retrofit getRetrofit(){
        return buildRetroFit(BuildConfig.BASE_URL);
    }
    public static Retrofit getRetrofitImage() {
        return buildRetroFit(BuildConfig.IMAGE_BASE_URL);
    }
    private static Retrofit buildRetroFit(String baseURL){
        Interceptor interceptor=new com.olam.digital.ofis.RetrofitServices.Interceptor();
        OkHttpClient okHttpClient=new OkHttpClient.Builder()
                .connectTimeout(540, TimeUnit.SECONDS)
                .readTimeout(540, TimeUnit.SECONDS)
                .writeTimeout(540, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseURL)
                .client(okHttpClient)
                .build();
    }
}
