package com.social.yourturn.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.social.yourturn.MainActivity;
import com.social.yourturn.data.YourTurnContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 6/5/2017.
 */

public class GroupBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GroupBroadcastReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "Group Broadcast Receiver intent null");
        }else {
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String senderId = "", recipients = "";
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
                    if(key.equals("senderId")) {
                        senderId = json.getString(key);
                        Log.d(TAG, "sender Id: " + senderId);
                    }else if(key.equals("recipientIds")){
                        recipients = json.getString(key);
                        Log.d(TAG, "recipients Ids: " + recipients);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(senderId.length() > 0 && recipients.length() > 0){
                    createNotification(context, senderId, recipients);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    public static final int NOTIFICATION_ID = 475;

    private void createNotification(Context context, String senderId, String recipients){

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent groupIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Cursor cursor = context.getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, new String[]{YourTurnContract.UserEntry.COLUMN_USER_NAME},
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(senderId), null, null);

        String senderName = "";
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            senderName = cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME));
            cursor.close();
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(senderName + " sent you a message")
                .setContentText("You been added into a new group")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(groupIntent);

        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification.build());

    }
}
