package com.olam.digital.ofis;
import android.content.Context;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
//import com.dynatrace.android.agent.Dynatrace;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.distribute.Distribute;
import com.olam.digital.ofis.logging.CrashReportingTree;

//import com.olam.inapp.InApp;

import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.dispatcher.DispatchMode;

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

    static Tracker tracker;
    static EnableMultiDex instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
       /* Dynatrace.startup(this, new DynatraceConfigurationBuilder("1bd3a5cf-7c8d-49ae-8452-7116cfccb064", "https://eok851.dynatrace-managed.com:9999/mbeacon/82203a30-a89c-4afb-8009-c475586c3488")
                .buildConfiguration());
*/

//        AppCenter.start(this, "983e5261-951a-40fc-b19c-692e856558d3", Distribute.class);

        Distribute.setEnabledForDebuggableBuild(true);
        Fabric.with(this, new Crashlytics());
        context = getApplicationContext();
        Timber.plant(BuildConfig.DEBUG ? new CrashReportingTree() :  new Timber.DebugTree());
//        Dynatrace.identifyUser(Utilities.getFromSharedPrefs(context));
//        Dynatrace.startup(this, new DynatraceConfigurationBuilder("1bd3a5cf-7c8d-49ae-8452-7116cfccb064", "https://52.163.121.203:9999/mbeacon/82203a30-a89c-4afb-8009-c475586c3488")
        /*DTXAction dtxAction = Dynatrace.enterAction("OFIS_UAT_LAUNCH");

        Toast.makeText(this,"Test Dynatrace", Toast.LENGTH_LONG).show();
        dtxAction.leaveAction();*/


        /* Matomo enabled based on the build type*/
        if (BuildConfig.MATOMO_ENABLED) {
            getTracker();
        }


    }

    public static Tracker getTracker() {
        try {
            if (tracker == null) {
                tracker = TrackerBuilder.createDefault(BuildConfig.MATOMO_SERVER_URL, BuildConfig.MATOMO_SERVER_CODE).build(Matomo.getInstance(instance));
                tracker.setDispatchInterval(0);
                tracker.setDispatchMode(DispatchMode.ALWAYS);
            }
        } catch (UnsupportedOperationException ex) {
            ex.getMessage();
        }
        return tracker;
    }


}

