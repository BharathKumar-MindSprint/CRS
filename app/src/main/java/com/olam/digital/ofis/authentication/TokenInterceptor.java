package com.olam.digital.ofis.authentication;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by nitin.kumar35 on 8/19/2017.
 */

public class TokenInterceptor implements Interceptor {

    public TokenInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("Content-Type","application/json")
                .header("App-Id", "1");


        Request request = builder.build();
        return chain.proceed(request);
    }
}