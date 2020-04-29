package com.olam.digital.ofis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class companionFirebaseMessagingService extends FirebaseMessagingService {
    public companionFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sendNotification(remoteMessage);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, TasksActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String messageBody = remoteMessage.getData().get("body");
        String taskAction = remoteMessage.getData().get("task_action");
        String messageTitle = getResources().getString(R.string.NewTaskReceivedTitle);
        try {
            if (taskAction!= null) {
                switch (taskAction) {
                    case "update":
                    case "updateandreset":
                        messageTitle = getResources().getString(R.string.TaskUpdatedTitle);
                        break;
                    case "delete":
                        messageTitle = getResources().getString(R.string.TaskDeletedTitle);
                        break;
                    default:
                        break;
                }
            }else{
                messageBody=remoteMessage.getNotification().getBody();
            }
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = null;/* = new NotificationCompat.Builder(this,"OFIS_ch_id")
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_ALL);*/


            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", importance);
                notificationManager.createNotificationChannel(notificationChannel);
                notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
            } else {
                notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
            }

            notificationBuilder = notificationBuilder
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_ALL);


            notificationManager.notify(6575, notificationBuilder.build());

            DataLayer dl = new DataLayer(getResources(), getApplicationContext());
            if (taskAction.equalsIgnoreCase("delete")) {
                dl.deleteTask(remoteMessage.getData());
            } else {
                dl.addOrUpdateTask(remoteMessage.getData(), messageBody);
            }

            Intent updTasksTotal = new Intent("TasksUpdated");
            LocalBroadcastManager.getInstance(this).sendBroadcast(updTasksTotal);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
