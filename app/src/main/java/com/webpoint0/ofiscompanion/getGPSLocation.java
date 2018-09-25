package com.webpoint0.ofiscompanion;

import android.location.Location;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.location.LocationListener;
import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by ben on 28/11/2016.
 * Handles Google play location updates
 */

class getGPSLocation implements LocationListener {

    private final Handler handler;

    getGPSLocation(Handler handler_) {
        this.handler = handler_;
    }

    @Override
    public void onLocationChanged(Location location) {
        Message msg = handler.obtainMessage(1,location);
        handler.sendMessage(msg);
    }
}
