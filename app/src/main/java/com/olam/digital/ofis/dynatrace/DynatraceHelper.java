package com.olam.digital.ofis.dynatrace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

import com.dynatrace.android.agent.DTXAction;
import com.dynatrace.android.agent.Dynatrace;

public class DynatraceHelper {

    public static boolean isAppClosed =true;

    public static void requestPayload(String tag, String payload, Context context,boolean request){

        // Commenting the payload information
//        try {
//            InternetSpeedModel speedModel = requestNetworkSpeed(context);
//            DTXAction action = Dynatrace.enterAction(tag);
//            if (request) {
//                action.reportValue("Request Payload", payload);
//            } else {
//                action.reportValue("Response Value", payload);
//            }
//            action.leaveAction();
//
//        }catch (Exception e){}

    }

    public static void findNetworkSpeed(Context context){
        try {
            InternetSpeedModel speedModel = requestNetworkSpeed(context);
            DTXAction actionSpeed = Dynatrace.enterAction( " Internet Speed");
            actionSpeed.reportValue("Download Speed", speedModel.getDownSpeed() + " Mbps");
            actionSpeed.reportValue("Upload Speed", speedModel.getUpSpeed()  + " Mbps");
            actionSpeed.leaveAction();
        }catch (Exception e){}
    }


    @SuppressLint("NewApi")
    public static InternetSpeedModel requestNetworkSpeed(Context context){
        InternetSpeedModel internetSpeedModel=null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                int downSpeed = nc.getLinkDownstreamBandwidthKbps();
                int upSpeed = nc.getLinkUpstreamBandwidthKbps();
                double downloadSpeed = (((double) downSpeed / 8) / 1000) / 8;
                double uploadSpeed = (((double) upSpeed / 8) / 1000) / 8;
                internetSpeedModel = new InternetSpeedModel();
                internetSpeedModel.setDownSpeed(downloadSpeed);
                internetSpeedModel.setUpSpeed(uploadSpeed);
                return internetSpeedModel;
            }
        }catch (Exception e){}
           return internetSpeedModel;
    }

}
