package com.olam.digital.ofis;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import com.crashlytics.android.Crashlytics;
//import com.dynatrace.android.agent.Dynatrace;
//import com.dynatrace.android.agent.conf.DynatraceConfigurationBuilder;
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
//        Dynatrace.startup(this, new DynatraceConfigurationBuilder("1bd3a5cf-7c8d-49ae-8452-7116cfccb064", "https://52.163.121.203:9999/mbeacon/82203a30-a89c-4afb-8009-c475586c3488")
//        Dynatrace.startup(this, new DynatraceConfigurationBuilder("1bd3a5cf-7c8d-49ae-8452-7116cfccb064", "https://dynatrace-server.olamdigital.com/mbeacon/82203a30-a89c-4afb-8009-c475586c3488")
//            .buildConfiguration());
    }
}

