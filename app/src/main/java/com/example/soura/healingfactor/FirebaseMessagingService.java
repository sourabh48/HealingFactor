package com.example.soura.healingfactor;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;


/**
 * Created by soura on 31-12-2017.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService
{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title =remoteMessage.getNotification().getTitle();
        String notification_message =remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        Notification.Builder builder =new Notification.Builder(this);

        long arr[]={500,1000,500,1000,500,1000};

        Notification notification = builder
                .setCategory(Notification.CATEGORY_EVENT)
                .setContentTitle(notification_title)
                .setContentText(notification_message)
                .setSmallIcon(R.drawable.logolauncher)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(arr)
                .build();

        notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
        notification.defaults = Notification.DEFAULT_LIGHTS ;

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id", from_user_id);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(resultPendingIntent);
        builder.addAction(android.R.drawable.ic_menu_view, "View details", resultPendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, builder.build());
    }

}



