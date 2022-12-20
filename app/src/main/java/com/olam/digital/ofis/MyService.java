package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

public class MyService extends Service {
    @Override
    public void onCreate() {
          super.onCreate();
          Log.e("inside","oncreate");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        handler.post(periodicUpdate);
        return START_STICKY;
    }

    public boolean isOnline(Context c){
        ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo ni=cm.getActiveNetworkInfo();

        if(ni !=null && ni.isConnectedOrConnecting()){
            return true;
        }else{
            return false;
        }
    }

    Handler handler=new Handler();
    private Runnable periodicUpdate=new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(periodicUpdate,1*1000- SystemClock.elapsedRealtime()%1000);
            Intent broadcastIntent=new Intent();
            broadcastIntent.setAction("checkinternet");
            broadcastIntent.putExtra("online_status",""+isOnline(MyService.this));
            sendBroadcast(broadcastIntent);
        }
    };

}
