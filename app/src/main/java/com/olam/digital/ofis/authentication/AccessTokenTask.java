package com.olam.digital.ofis.authentication;

import android.content.Context;
import android.os.AsyncTask;

import com.olam.digital.ofis.Utilities;
import com.olam.digital.ofis.authentication.model.AuthRequestDTO;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

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

        // New Approach Start
        try {
            //getting new access token for first time
            String clientId=Utilities.getKeyCloakClientDetails(mContext,KeyCloakInfo.Client_Id);
            String clientSecret=Utilities.getKeyCloakClientDetails(mContext,KeyCloakInfo.Client_Secret);
            String grantType=Utilities.SCOPE;
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
                Utilities.saveToSharedPrefs(mContext, "ENUMERATOR_ID", authRequestDTO.getUsername(), null);
                Utilities.saveToSharedPrefs(mContext, "PASSCODE", authRequestDTO.getPasscode(), null);
                return authenticationDTO.getAccessToken();

            } else {
                // auth Response code is 400 for when sync existing enumerator from Old Apk
            //    if (authResponse.code() == 401 || authResponse.code() == 400) {
                    // Keycloak parameters - Using Access Token
                    String grantType1 = Utilities.PASSWORD;
                    String scope = Utilities.SCOPE;
                    Call<AuthenticationDTO> authCall1 = authenticationInterface.getKeyCloakAuthToken(clientId,
                            authRequestDTO.getUsername(), authRequestDTO.getPasscode(), grantType1, clientSecret, scope);

                    // Keycloak Code Changes
                    Response authResponse1 = authCall1.execute();
                    if (authResponse1.isSuccessful()) {
                        AuthenticationDTO authenticationDTO = (AuthenticationDTO) authResponse1.body();
                        authenticationDTO.setWillExpireAt((System.currentTimeMillis()/1000) + authenticationDTO.getExpiresIn() /1000);
                        new AuthenticationHelper(mContext).setAuthenticationDTO(authenticationDTO);
                        new AuthenticationHelper(mContext).setUserName(authRequestDTO.getUsername());
                        Utilities.saveToSharedPrefs(mContext, "ENUMERATOR_ID", authRequestDTO.getUsername(), null);
                        Utilities.saveToSharedPrefs(mContext, "PASSCODE", authRequestDTO.getPasscode(), null);
                        return authenticationDTO.getAccessToken();
                    }

            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {

        onAccessTokenInterface.onTokenCompleted(Objects.requireNonNullElse(s, "false"));

    }
}