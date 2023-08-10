package com.olam.digital.ofis.logging;

import android.content.Context;
import android.util.Log;

import com.olam.digital.ofis.authentication.AuthenticationHelper;

public class FabricLogsWriter {

    public static void printCrashLog(String TAG, String serviceName, int statusCode, String request, Context context) {

        Log.e("LOGGER",TAG+" - " + "EnumeratorID : " + new AuthenticationHelper(context).getuserName()+ ", "+ serviceName
                + " --- code : " + statusCode + ", " + " Request/Response : " + request);
    }
}
