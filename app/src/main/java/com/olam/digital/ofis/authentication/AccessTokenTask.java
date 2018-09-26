package com.olam.digital.ofis.authentication;

import android.content.Context;
import android.os.AsyncTask;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class AccessTokenTask extends AsyncTask<Object, Object, String> {

    private final AuthRequestDTO authRequestDTO;
    AuthenticationDTO authenticationDTO;
    Context mContext;

    public AccessTokenTask(Context mContext, AuthRequestDTO authRequestDTO) {
        this.mContext = mContext;
        this.authRequestDTO = authRequestDTO;
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
                    authenticationDTO.setWillExpireAt((System.currentTimeMillis()/1000) + authenticationDTO.getExpiresIn() -60);
                    new AuthenticationHelper(mContext).setAuthenticationDTO(authenticationDTO);
                    return authenticationDTO.getAccessToken();
                } else {
                    int statusCode = authResponse.code();
                    return null;
                }
            } else {
                //check is about to expire -> refresh -> update willExpireAt and return auth token
                //if time left for expire -> Simply give the token back
                Long currentTime = System.currentTimeMillis()/1000;
                if(currentTime >= authenticationDTO.getWillExpireAt()) {
                    AuthenticationDTO authenticationDTO = new AuthenticationHelper(mContext).getAuthenticationDTO();
                    //
                    AuthRequestDTO authRequestDTO = new AuthRequestDTO();
                    authRequestDTO.setLanguage(Locale.getDefault().getLanguage());
                    authRequestDTO.setApplication("ttmobile");
                    authRequestDTO.setUsername(new AuthenticationHelper(mContext).getuserName());
                    authRequestDTO.setPassword(DeviceIDxctractor.fetchDeviceID(mContext));
                    authRequestDTO.setGrantType("password");
                    authRequestDTO.setClientId(2L);
                    authRequestDTO.setClientSecret(mContext.getString(R.string.client_secret));
                    AuthenticationInterface authenticationInterface =  AuthenticationClient.createService(AuthenticationInterface.class, mContext);
                    Call<AuthenticationDTO> authCall = authenticationInterface.getCoreAuthToken(authRequestDTO);
                    Response authResponse = authCall.execute();
                    if (authResponse.isSuccessful()) {
                        AuthenticationDTO newAuthDTO = (AuthenticationDTO) authResponse.body();
                        newAuthDTO.setWillExpireAt((System.currentTimeMillis()/1000) + newAuthDTO.getExpiresIn() -60);
                        new AuthenticationHelper(mContext).setAuthenticationDTO(newAuthDTO);
                        return newAuthDTO.getAccessToken();
                    } else {
                        int statusCode = authResponse.code();
                        return null;
                    }
                } else {
                    return authenticationDTO.getAccessToken();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
    }
}

