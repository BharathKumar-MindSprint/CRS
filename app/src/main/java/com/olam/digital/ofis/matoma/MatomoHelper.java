package com.olam.digital.ofis.matoma;


import com.olam.digital.ofis.BuildConfig;

import org.matomo.sdk.extra.TrackHelper;

import static com.olam.digital.ofis.EnableMultiDex.getTracker;


public class MatomoHelper {

    private MatomoHelper() {
    }

    public static void matomoScreenTracker(String fromActivityName,String toActivityName, String titleName) {
        try {
            if (BuildConfig.MATOMO_ENABLED) {
                TrackHelper.track().screen(fromActivityName+"/"+toActivityName).title(titleName).with(getTracker());
            }
        } catch (UnsupportedOperationException ex) {
            ex.getMessage();
        }
    }

    public static void matomoEventTracker(String category, String action, String lable) {
        try {
            if (BuildConfig.MATOMO_ENABLED) {
//                TrackHelper.track().event(category, action).name(lable).value(1000f).with(getTracker());
                TrackHelper.track().event(category, action).name(lable).with(getTracker());
            }
        } catch (UnsupportedOperationException ex) {
            ex.getMessage();
        }
    }
}
