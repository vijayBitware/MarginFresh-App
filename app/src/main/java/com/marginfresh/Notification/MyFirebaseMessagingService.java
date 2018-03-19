package com.marginfresh.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityNotificationList;
import com.marginfresh.activities.DrawerActivity;
import com.marginfresh.activities.LoginActivity;
import com.marginfresh.domain.Config;

/**
 * Created by bitware on 27/7/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    //Privious Code
    private static final String TAG = "MyFirebaseMsgService";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        System.out.println("Notification Body > " + remoteMessage.getData().toString());
        sendNotification(remoteMessage.getNotification().getBody());

    }

    private void sendNotification(String messageBody) {
        editor.putString("NavigationPosition","0");
        editor.commit();
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.noti_icon)
                .setContentTitle(messageBody)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setColor(getResources().getColor(R.color.colorPrimary));

        updateCounter();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    public  void updateCounter()
    {
        int count = Config.notificationCount+1;
        editor.putString("notiCount", String.valueOf(count));
        editor.commit();
        Intent intent = new Intent("notificationSend");
        sendBroadcast(intent);
    }
}
