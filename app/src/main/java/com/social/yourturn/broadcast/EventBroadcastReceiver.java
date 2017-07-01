package com.social.yourturn.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.social.yourturn.MainActivity;
import com.social.yourturn.data.YourTurnContract;

import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 6/5/2017.
 */

public class EventBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = EventBroadcastReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "Event Broadcast Receiver intent null");
        }else {
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String senderId = "", eventId = "", eventName = "", targetIds = "", eventUrl="";
        String action = intent.getAction();
        Log.d(TAG, "got action " + action);
        if(action.equals(intentAction)){
            String channel = intent.getExtras().getString("com.parse.Channel");
            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            try{
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                Iterator<String> itr = json.keys();
                while(itr.hasNext()){
                    String key = (String) itr.next();
                    if(key.equals("senderId")) {
                        senderId = json.getString(key);
                        Log.d(TAG, "sender Id: " + senderId);
                    }else if(key.equals("eventName")){
                        eventName = json.getString(key);
                        Log.d(TAG, "event Name: " + eventName);
                    }else if(key.equals("eventId")) {
                        eventId = json.getString(key);
                        Log.d(TAG, "event Id: " + eventId);
                    }else if(key.equals("targetIds")) {
                        targetIds = json.getString(key);
                        Log.d(TAG, "event members" + targetIds);
                    }else if(key.equals("eventUrl")){
                        eventUrl = json.getString(key);
                        Log.d(TAG, "event Url: " + eventUrl);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(senderId.length() > 0 && eventName.length() > 0 && eventId.length() > 0 && targetIds.length() > 0){
                    createNotification(context, senderId,  eventName, eventId, eventUrl, targetIds);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    public static final int NOTIFICATION_ID = 475;


    private void insertEventEntry(Context context, String eventId, String eventName, String senderId, String eventUrl, String targetIds){


        String[] ids = targetIds.split(",");
        for(String id: ids){
            DateTime dayTime = new DateTime();
            ContentValues eventValues = new ContentValues();
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_ID, eventId);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_NAME, eventName);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_USER_KEY, id);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_CREATOR, senderId);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_URL, eventUrl);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_FLAG, "1");
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_CREATED_DATE, dayTime.getMillis());
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_UPDATED_DATE, dayTime.getMillis());

            context.getContentResolver().insert(YourTurnContract.EventEntry.CONTENT_URI, eventValues);
        }
    }

    private void createNotification(final Context context, String senderId, String eventName, String eventId, String eventUrl, String targetIds){

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent eventIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Cursor cursor = context.getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?", new String[]{senderId}, null);

        String senderName = "";
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            senderName = cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
            cursor.close();
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(WordUtils.capitalize(senderName.toLowerCase(), null) + " added you in a new group")
                .setContentText("You been added into a new group called " + eventName)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(eventIntent);

        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification.build());

        insertEventEntry(context, eventId, eventName, senderId, eventUrl, targetIds + "," + senderId);

    }
}
