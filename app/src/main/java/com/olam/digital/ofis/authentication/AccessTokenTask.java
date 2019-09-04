package com.olam.digital.ofis.authentication;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.olam.digital.ofis.logging.FabricLogsWriter;


import retrofit2.Call;
import retrofit2.Response;

import static com.olam.digital.ofis.HomeActivity.TAG;

public class AccessTokenTask extends AsyncTask<Object, Object, String> {

    private final AuthRequestDTO authRequestDTO;
    AuthenticationDTO authenticationDTO;
    Context mContext;

    public OnAccessTokenInterface onAccessTokenInterface;

    public AccessTokenTask(Context mContext, AuthRequestDTO authRequestDTO, OnAccessTokenInterface onAccessTokenInterface) {
        this.mContext = mContext;
        this.authRequestDTO = authRequestDTO;
        this.onAccessTokenInterface = onAccessTokenInterface;
    }

    @Override
    protected void onPreExecute() {
        authenticationDTO = new AuthenticationHelper(mContext).getAuthenticationDTO();
    }

    @Override
    protected String doInBackground(Object... objects) {
        try {
            if (authenticationDTO == null) {
                //fetch new AccessToken and update willExpireAt
                AuthenticationInterface authenticationInterface;
                authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, mContext);
                AuthenticationDTO authenticationDTO = null;
                Call<AuthenticationDTO> authCall = authenticationInterface.getCoreAuthToken(authRequestDTO);
                Response authResponse = authCall.execute();
                if (authResponse.isSuccessful()) {
                    authenticationDTO = (AuthenticationDTO) authResponse.body();
                    authenticationDTO.setWillExpireAt((System.currentTimeMillis()/1000) + authenticationDTO.getExpiresIn() /1000);
                    new AuthenticationHelper(mContext).setAuthenticationDTO(authenticationDTO);
                    return authenticationDTO.getAccessToken();
                } else {
                    try {
                        FabricLogsWriter.printCrashLog(TAG, "oAuth/authenticate",authResponse.code(), "Request : "+  new Gson().toJson(authRequestDTO) +"Response  : "+authResponse.raw().networkResponse().toString(),mContext);
                    }catch (Exception e){}
                    int statusCode = authResponse.code();
                    return null;
                }
            }else {
                return "error";
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {

        if (s!=null){

            onAccessTokenInterface.onTokenCompleted(s);
        }else{
            onAccessTokenInterface.onTokenCompleted("false");
        }

    }
}