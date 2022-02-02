package com.olam.digital.ofis;
import android.content.Context;
import androidx.multidex.MultiDexApplication;

//import com.crashlytics.android.Crashlytics;
//import com.dynatrace.android.agent.Dynatrace;
//import com.microsoft.appcenter.distribute.Distribute;
//import com.olam.digital.ofis.logging.CrashReportingTree;

//import com.olam.inapp.InApp;

import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.dispatcher.DispatchMode;

//import timber.log.Timber;


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
        context = getApplicationContext();
//        Timber.plant(BuildConfig.DEBUG ? new CrashReportingTree() :  new Timber.DebugTree());
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

