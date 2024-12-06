package com.olam.digital.ofis.authentication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

public class DeviceIdExtractor {

    @SuppressLint("HardwareIds")
    public static String fetchDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}

