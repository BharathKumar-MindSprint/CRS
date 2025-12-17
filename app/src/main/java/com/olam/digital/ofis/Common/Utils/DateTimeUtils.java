package com.olam.digital.ofis.Common.Utils;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.olam.digital.ofis.R;

public class DateTimeUtils {

    public static void showDateTimeSettingsPopup(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(com.olam.digital.ofis.R.string.enable_auto_date_time));
        builder.setMessage(context.getString(com.olam.digital.ofis.R.string.enable_auto_date_time_message));

        // Positive button to navigate to Date & Time settings
        builder.setPositiveButton(context.getString(R.string.open_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                context.startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

  public static void checkTime(Context context, Activity activity) {
      Log.e("DateTimeUtils", "checkTime: Checking automatic date and time settings" );
        int auto_time_enabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 0);
        int auto_time_zone_enabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);

        if (auto_time_enabled == 0 || auto_time_zone_enabled == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setMessage(context.getString(R.string.set_auto_time_message))
                    .setPositiveButton(context.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                           startActivityForResult(activity, intent, 0, null);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public static void checkAutoDateTimeSettings(Context context) {
        // Check if automatic date/time is enabled
        boolean isAutoTimeEnabled = Settings.Global.getInt(
                context.getContentResolver(),
                Settings.Global.AUTO_TIME,
                0
        ) == 1;

        // Check if automatic time zone is enabled
        boolean isAutoTimeZoneEnabled = Settings.Global.getInt(
                context.getContentResolver(),
                Settings.Global.AUTO_TIME_ZONE,
                0
        ) == 1;

        if (!isAutoTimeEnabled || !isAutoTimeZoneEnabled) {
            showDateTimeSettingsPopup(context);
        }
    }
}

