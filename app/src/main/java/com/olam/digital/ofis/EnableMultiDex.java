package com.olam.digital.ofis;
import android.content.Context;
import androidx.multidex.MultiDexApplication;

public class EnableMultiDex extends MultiDexApplication {
    private static EnableMultiDex enableMultiDex;
    public static Context context;

    public EnableMultiDex() {
        enableMultiDex=this;
    }

    public static EnableMultiDex getEnableMultiDexApp() {
        return enableMultiDex;
    }

    static EnableMultiDex instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        context = getApplicationContext();
    }
}

