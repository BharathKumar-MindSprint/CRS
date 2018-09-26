package com.olam.digital.ofis.authentication;

import android.content.Context;

import com.google.gson.Gson;
import com.olam.digital.ofis.authentication.model.AuthenticationDTO;

public class AuthenticationHelper {
    Context context;
    public static final String AT_PREFS = "ATPREFS";
    public static final String AT_DATA_PREFS = "ATDATAPREFS";
    public static final String AT_USER_PREFS = "ATUSERPREFS";

    public AuthenticationHelper(Context context) {
        this.context = context;
    }

    public AuthenticationDTO getAuthenticationDTO() {
        String s = context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                .getString(AT_DATA_PREFS, "");
        if (s.equals("")) {
            return null;
        } else {
            try {
                return (new Gson().fromJson(s, AuthenticationDTO.class));
            } catch (Exception e) {
                return null;
            }
        }
    }

    public void setUserName(String username) {
        context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                .edit().putString(AT_USER_PREFS, username).apply();
    }

    public String getuserName() {
        String s = context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                .getString(AT_USER_PREFS, "");
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
            context.getSharedPreferences(AT_PREFS, Context.MODE_PRIVATE)
                    .edit().putString(AT_DATA_PREFS, new Gson().toJson(authenticationDTO)).apply();
        }
    }

}

