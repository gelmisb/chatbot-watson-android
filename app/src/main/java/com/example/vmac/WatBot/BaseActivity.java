package com.example.vmac.WatBot;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class BaseActivity extends Activity{

    String emotion;
    String realUserSentiment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref= getSharedPreferences("mypref", 0);

        emotion = sharedPref.getString("userEmotionAnalysis", "");
        realUserSentiment = sharedPref.getString("userSemanticAnalysis", "");

        notificationCenter();
    }

    public void notificationCenter(){

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // The id of the channel.
        String CHANNEL_ID = "my_channel_01";

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ai_faze)
                        .setContentTitle("Your profile has been assessed")
                        .setContentText("You're " + realUserSentiment.toLowerCase() + ". It seems you are feeling quite " + emotion);

        //Vibration
        mBuilder.setVibrate(new long[300]);

        //LED
        mBuilder.setLights(Color.BLUE, 3000, 3000);

        //Ton
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        mBuilder.setAutoCancel(true);

        mBuilder.setPriority(2);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] events = new String[6];

        // Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Event tracker details:");

        // Moves events into the expanded layout
        for (int i=0; i < events.length; i++) {
            inboxStyle.addLine(events[i]);
        }

        // Moves the expanded layout object into the notification object.
        mBuilder.setStyle(inboxStyle);


        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainScreenTime.class);

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainScreenTime.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        assert mNotificationManager != null;
        mNotificationManager.notify(1, mBuilder.build());

        finish();

    }

}

