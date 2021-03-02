package com.olam.digital.ofis.logging;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import timber.log.Timber;

/**
 * Created by nitin.kumar35 on 8/20/2017.
 */

public class CrashReportingTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        Throwable t = throwable != null
                ? throwable
                : new Exception(message);
        // Firebase Crash Reporting
        try {
            FirebaseCrashlytics.getInstance().recordException(t);
            FirebaseCrashlytics.getInstance().log(message);
        }catch (Exception e){}
    }
}

