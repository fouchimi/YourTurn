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

import com.social.yourturn.ConfirmAmountActivity;
import com.social.yourturn.data.YourTurnContract;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 5/14/2017.
 */

public class PushSenderBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = PushSenderBroadcastReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String SENDER_ID = "senderId";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "Sender Broadcast Receiver intent null");
        }else {
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String senderId = "", amount = "";
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
                        Log.d(TAG, "sender Id: " + senderId);
                    }else if(key.equals("amount")){
                        amount = json.getString(key);
                        Log.d(TAG, "Amount: " + amount);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(senderId.length() > 0 && amount.length() > 0){
                    createNotification(context, senderId, amount);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    public static final int NOTIFICATION_ID = 45;

    private void createNotification(Context context, String senderPhoneNumber, String amount) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(TITLE, senderPhoneNumber);
        intent.putExtra(MESSAGE, amount);
        intent.putExtra(SENDER_ID, senderPhoneNumber);
        intent.setClass(context, ConfirmAmountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent senderIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Cursor cursor = context.getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI,
                new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?",
                new String[]{senderPhoneNumber},
                null);

        cursor.moveToFirst();
        String senderName = cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
        cursor.close();

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(WordUtils.capitalize(senderName.toLowerCase(), null) + " sent you a message")
                .setContentText("You have been requested to confirm an amount of $" + amount)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(senderIntent);

        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification.build());

        registered(context, senderPhoneNumber);
    }

    private void registered(Context context, String phoneNumber){
        Cursor memberCursor = context.getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI,
                new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_REGISTERED},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?" + " AND " +
                        YourTurnContract.MemberEntry.COLUMN_MEMBER_REGISTERED + "=?",
                new String[]{phoneNumber, "1"}, null);

        if(memberCursor != null && memberCursor.getCount() > 1) return;
        else {
            ContentValues values = new ContentValues();
            values.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_REGISTERED, "1");
            context.getContentResolver().update(YourTurnContract.MemberEntry.CONTENT_URI, values,
                    YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?", new String[]{phoneNumber});
            return;
        }
    }

}
