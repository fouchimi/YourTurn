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
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.social.yourturn.ChatActivity;
import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 7/10/2017.
 */

public class OfflineChannelReceiver extends BroadcastReceiver {
    private static final String TAG = OfflineChannelReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "empty intent");
        }else {
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String senderId= "", receiverId = "", messageBody="";
        long createdAt = 0L, updatedAt = 0L;
        boolean status = true;
        String action = intent.getAction();
        Log.d(TAG, "got action " + action);
        if(action.equals(intentAction)){
            String channel = intent.getExtras().getString("com.parse.Channel");
            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            try{
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                // Iterate the placeParse keys if needed
                Iterator<String> itr = json.keys();
                while(itr.hasNext()){
                    String key = (String) itr.next();
                    if(key.equals("senderId")) {
                        senderId = json.getString(key);
                        Log.d(TAG, "Title: " + senderId);
                    }else if(key.equals("targetId")){
                        receiverId = json.getString(key);
                        Log.d(TAG, "receiverId: " + receiverId);
                    }else if(key.equals("message")){
                        messageBody = json.getString(key);
                        Log.d(TAG, "message: " + messageBody);
                    }else if(key.equals("createdAt")){
                        createdAt = json.getLong(key);
                        Log.d(TAG, "createdAt: " + createdAt);
                    }else if(key.equals("updatedAt")) {
                        updatedAt = json.getLong(key);
                        Log.d(TAG, "updateAt: " + updatedAt);
                    }else if(key.equals("status")){
                        status = json.getBoolean(key);
                        Log.d(TAG, "status: " + status);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(senderId.length() > 0 && receiverId.length() > 0 && messageBody.length() > 0 && !status) {
                    showNotification(context, senderId, receiverId, messageBody, createdAt, updatedAt);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    private void showNotification(Context context, String senderId, String receiverId, String message,  long createdAt, long updatedAt){

        Cursor cursor = context.getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI,
                new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME, YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?",
                new String[]{senderId},
                null);

        if(cursor != null) cursor.moveToFirst();
        String senderName = cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
        String picUrl = cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL));
        cursor.close();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(context, ChatActivity.class);
        Contact contact = new Contact();
        contact.setName(WordUtils.capitalize(senderName, null));
        contact.setPhoneNumber(senderId);
        contact.setThumbnailUrl(picUrl);
        intent.putExtra(context.getString(R.string.selected_contact), contact);

        int requestID = (int) System.currentTimeMillis();
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        PendingIntent pIntent = PendingIntent.getActivity(context, requestID, intent, flags);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(WordUtils.capitalize(senderName.toLowerCase(), null) + " sent you a message !")
                .setContentText(message)
                .setContentIntent(pIntent)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notification);

        ContentValues values = new ContentValues();
        values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY, contact.getPhoneNumber());
        values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY, receiverId);
        values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_BODY, message);
        values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_TYPE, "text");
        values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_CREATED_DATE, createdAt);
        values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_UPDATED_DATE, updatedAt);

        context.getContentResolver().insert(YourTurnContract.MessageEntry.CONTENT_URI, values);
    }

}
