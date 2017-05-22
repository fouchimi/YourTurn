package com.social.yourturn.broadcast;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.social.yourturn.ConfirmAmountActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 5/14/2017.
 */

public class MyCustomReceiver extends BroadcastReceiver {

    private static final String TAG = MyCustomReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "Receiver intent null");
        }else {
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String title = "", message = "";
        String action = intent.getAction();
        Log.d(TAG, "got action " + action);
        if(action.equals(intentAction)){
            String channel = intent.getExtras().getString("com.parse.Channel");
            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            try{
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                // Iterate the parse keys if needed
                Iterator<String> itr = json.keys();
                while(itr.hasNext()){
                    String key = (String) itr.next();
                    if(key.equals("title")) {
                        title = json.getString(key);
                        Log.d(TAG, "Title: " + title);
                    }else if(key.equals("alert")){
                        message = json.getString(key);
                        Log.d(TAG, "Message: " + message);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                createNotification(context, title, message);
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    public static final int NOTIFICATION_ID = 45;

    private void createNotification(Context context, String title, String message) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(TITLE, title);
        intent.putExtra(MESSAGE, message);
        intent.setClass(context, ConfirmAmountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent senderIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(senderIntent);

        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification.build());
    }

}