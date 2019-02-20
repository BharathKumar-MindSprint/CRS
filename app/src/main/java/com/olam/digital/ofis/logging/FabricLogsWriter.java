package com.olam.digital.ofis.logging;

import android.content.Context;

import com.olam.digital.ofis.authentication.AuthenticationHelper;

import timber.log.Timber;


public class FabricLogsWriter {

    public static void printCrashLog(String TAG, String serviceName, int statusCode, String request, Context context) {

        Timber.e(TAG+" - " + "EnumeratorID : " + new AuthenticationHelper(context).getuserName()+ ", "+ serviceName
                + " --- code : " + statusCode + ", " + " Request/Response : " + request);
    }
}
