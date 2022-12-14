package com.olam.digital.ofis.authentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Debug;

import com.google.gson.Gson;
import com.olam.digital.ofis.BuildConfig;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class AuthenticationHelper {
    Context context;
    public static final String AT_PREFS = "ATPREFS";
    public static final String AT_DATA_PREFS = "ATDATAPREFS";
    public static final String AT_USER_PREFS = "ATUSERPREFS";
    public static final String AT_USER_PREFS_PASS = "ATUSERPREFS_PASS";

    public AuthenticationHelper(Context context) {
        this.context = context;
    }

    public AuthenticationDTO getAuthenticationDTO() {

        String authMessage = context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                .getString(AT_DATA_PREFS, "");
        String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt(AT_DATA_PREFS, authMessage);
        }catch (GeneralSecurityException e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }
        if (decryptedMsg.equals("")) {
            return null;
        } else {
            try {
                return (new Gson().fromJson(decryptedMsg, AuthenticationDTO.class));
            } catch (Exception e) {
                return null;
            }
        }
    }

    public void setUserName(String username) {
        String encryptedUsername="";
        try {
            encryptedUsername = AESCrypt.encrypt(AT_USER_PREFS, username);
        }catch (GeneralSecurityException e){
            //handle error
        }
        context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                .edit().putString(AT_USER_PREFS, encryptedUsername).apply();
    }

    public String getuserName() {
        String s = context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                .getString(AT_USER_PREFS, "");
       String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt(AT_USER_PREFS, s);
        }catch (GeneralSecurityException e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }
        if (decryptedMsg.equals("")) {
            return null;
        } else {
            try {
                return decryptedMsg;
            } catch (Exception e) {
                return null;
            }
        }
    }

    //    Passcode added - Penetrsation testing
    public void setPassode(String passcode) {
        String encryptedPasscode="";
        try {
            encryptedPasscode = AESCrypt.encrypt(AT_USER_PREFS_PASS, passcode);
        }catch (GeneralSecurityException e){
            //handle error
        }
        context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                .edit().putString(AT_USER_PREFS_PASS, encryptedPasscode).apply();
    }

    public String getPasscode() {
        String s = context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                .getString(AT_USER_PREFS_PASS, "");
        String decryptedMsg="";
        try {
            decryptedMsg = AESCrypt.decrypt(AT_USER_PREFS_PASS, s);
        }catch (GeneralSecurityException e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }

        if (s.equals("")) {
            return null;
        } else {
            try {
                return s;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public void setAuthenticationDTO(AuthenticationDTO authenticationDTO) {
        if (authenticationDTO == null) {
            context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                    .edit().putString(AT_DATA_PREFS, null).apply();
        } else {
            String encryptedAuthentication="";
            try {
                 encryptedAuthentication = AESCrypt.encrypt(AT_DATA_PREFS, new Gson().toJson(authenticationDTO));
            }catch (GeneralSecurityException e){
                //handle error
            }
            context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                    .edit().putString(AT_DATA_PREFS, encryptedAuthentication).apply();
        }
    }

    public void setDataEncryptedFlag(Boolean isEncrypted) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("encrypt",isEncrypted);
        editor.commit();
    }

    public boolean getDataEncryptedFlag(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("encrypt", false);
    }
}

