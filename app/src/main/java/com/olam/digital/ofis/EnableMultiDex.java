package com.olam.digital.ofis;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import com.crashlytics.android.Crashlytics;
import com.olam.digital.ofis.logging.CrashReportingTree;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;


public class EnableMultiDex extends MultiDexApplication {
    private static EnableMultiDex enableMultiDex;
    public static Context context;

    public EnableMultiDex() {
        enableMultiDex=this;
    }

    public static EnableMultiDex getEnableMultiDexApp() {
        return enableMultiDex;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        context = getApplicationContext();
        Timber.plant(BuildConfig.DEBUG ? new CrashReportingTree() :  new Timber.DebugTree());
    }
}