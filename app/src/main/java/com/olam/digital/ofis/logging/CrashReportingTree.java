package com.olam.digital.ofis.logging;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

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
        Crashlytics.log(priority, tag, message);
        Crashlytics.logException(t);
        Crashlytics.getInstance().core.logException(t);
        Crashlytics.getInstance().logException(t);
    }
}

