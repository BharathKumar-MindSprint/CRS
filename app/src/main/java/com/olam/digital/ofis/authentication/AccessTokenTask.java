package com.olam.digital.ofis.authentication;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.olam.digital.ofis.R;
import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.olam.digital.ofis.dynatrace.DynatraceConstants;
import com.olam.digital.ofis.dynatrace.DynatraceHelper;
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
        // Old Approach Start
//        try {
//                //getting new access token for first time
//                AuthenticationInterface authenticationInterface;
//                authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, mContext);
//                AuthenticationDTO authenticationDTO = null;
//
//                // Keycloak parameters - Using Access Token
//                String clientId=mContext.getResources().getString(R.string.clientId);
//                String clientSecret=mContext.getResources().getString(R.string.clientSecret);
//                String grantType=mContext.getResources().getString(R.string.password);
//                String scope=mContext.getResources().getString(R.string.offline_access);
//                Call<AuthenticationDTO> authCall = authenticationInterface.getKeyCloakAuthToken(clientId,
//                    authRequestDTO.getUsername(),authRequestDTO.getPasscode(),grantType,clientSecret,scope);
//
//
//            // Keycloak Code Changes
//                Response authResponse = authCall.execute();
//                if (authResponse.isSuccessful()) {
//                    authenticationDTO = (AuthenticationDTO) authResponse.body();
//                    DynatraceHelper.requestPayload(DynatraceConstants.AUTHENTICATION_RESPONSE,new Gson().toJson(authenticationDTO),mContext,false);
//                    authenticationDTO.setWillExpireAt((System.currentTimeMillis()/1000) + authenticationDTO.getExpiresIn() /1000);
//                    new AuthenticationHelper(mContext).setAuthenticationDTO(authenticationDTO);
//                    new AuthenticationHelper(mContext).setUserName(authRequestDTO.getUsername());
//                    new AuthenticationHelper(mContext).setPassode(authRequestDTO.getPasscode());
//                    Utilities.saveToSharedPrefs(mContext, "ENUMERATOR_ID", authRequestDTO.getUsername(), null);
//                    Utilities.saveToSharedPrefs(mContext, "PASSCODE", authRequestDTO.getPasscode(), null);
//                    return authenticationDTO.getAccessToken();
//                } else {
//                    int statusCode = authResponse.code();
//                    return null;
//                }
//        } catch (Exception e) {
//            return null;
//        }
        //END


        // New Approach Start
        try {
            //getting new access token for first time
            String clientId=mContext.getResources().getString(R.string.clientId);
            String clientSecret=mContext.getResources().getString(R.string.clientSecret);
            String grantType=mContext.getResources().getString(R.string.refresh_token);
            String refreshToken=authenticationDTO.getRefreshToken();

            // Auth Call
            AuthenticationInterface authenticationInterface = AuthenticationClient.createService(AuthenticationInterface.class, mContext);
            retrofit2.Call<AuthenticationDTO> authCall = authenticationInterface.getKeyCloakRefreshToken(clientId,clientSecret,grantType,refreshToken);
            retrofit2.Response authResponse = authCall.execute();
            // DTO call

            if (authResponse.isSuccessful()) {
                AuthenticationDTO newAuthDTO = (AuthenticationDTO) authResponse.body();
                newAuthDTO.setWillExpireAt((System.currentTimeMillis()/1000) + newAuthDTO.getExpiresIn() /1000);
                new AuthenticationHelper(mContext).setAuthenticationDTO(newAuthDTO);
                new AuthenticationHelper(mContext).setUserName(authRequestDTO.getUsername());
                new AuthenticationHelper(mContext).setPassode(authRequestDTO.getPasscode());
                Utilities.saveToSharedPrefs(mContext, "ENUMERATOR_ID", authRequestDTO.getUsername(), null);
                Utilities.saveToSharedPrefs(mContext, "PASSCODE", authRequestDTO.getPasscode(), null);
                return authenticationDTO.getAccessToken();

            } else {
                // auth Response code is 400 for when sync existing enumerator from Old Apk
            //    if (authResponse.code() == 401 || authResponse.code() == 400) {
                    // Keycloak parameters - Using Access Token
                    String grantType1 = mContext.getResources().getString(R.string.password);
                    String scope = mContext.getResources().getString(R.string.offline_access);
                    Call<AuthenticationDTO> authCall1 = authenticationInterface.getKeyCloakAuthToken(clientId,
                            authRequestDTO.getUsername(), authRequestDTO.getPasscode(), grantType1, clientSecret, scope);

                    // Keycloak Code Changes
                    Response authResponse1 = authCall1.execute();
                    if (authResponse1.isSuccessful()) {
                        AuthenticationDTO authenticationDTO = (AuthenticationDTO) authResponse1.body();
                        DynatraceHelper.requestPayload(DynatraceConstants.AUTHENTICATION_RESPONSE, new Gson().toJson(authenticationDTO), mContext, false);
                        authenticationDTO.setWillExpireAt((System.currentTimeMillis()/1000) + authenticationDTO.getExpiresIn() /1000);
                        new AuthenticationHelper(mContext).setAuthenticationDTO(authenticationDTO);
                        new AuthenticationHelper(mContext).setUserName(authRequestDTO.getUsername());
                        new AuthenticationHelper(mContext).setPassode(authRequestDTO.getPasscode());
                        Utilities.saveToSharedPrefs(mContext, "ENUMERATOR_ID", authRequestDTO.getUsername(), null);
                        Utilities.saveToSharedPrefs(mContext, "PASSCODE", authRequestDTO.getPasscode(), null);
                        return authenticationDTO.getAccessToken();
                    } else {
                        int statusCode = authResponse.code();
                        return null;
                    }

              //  } else {
                    // Write a logic if code fails here apart from 401
               //     return null;
              //  }
            }
        } catch (Exception e) {
            return null;
        }
      //   END


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