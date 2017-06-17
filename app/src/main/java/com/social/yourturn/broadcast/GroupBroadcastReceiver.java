package com.social.yourturn.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.social.yourturn.MainActivity;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
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
        String senderId = "", groupId = "", groupName = "", groupMembers = "", groupUrl="", friendList="";
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
                    }else if(key.equals("groupName")){
                        groupName = json.getString(key);
                        Log.d(TAG, "group Name: " + groupName);
                    }else if(key.equals("groupId")) {
                        groupId = json.getString(key);
                        Log.d(TAG, "group Id: " + groupId);
                    }else if(key.equals("targetIds")) {
                        groupMembers = json.getString(key);
                        Log.d(TAG, "group members" + groupMembers);
                    }else if(key.equals("groupUrl")){
                        groupUrl = json.getString(key);
                        Log.d(TAG, "group Url: " + groupUrl);
                    }else if(key.equals("friendList")){
                        friendList = json.getString(key);
                        Log.d(TAG, "friendList: " + friendList);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(senderId.length() > 0 && groupName.length() > 0 && groupId.length() > 0 && friendList.length() > 0){
                    createNotification(context, senderId,  groupName, groupId, groupUrl, friendList);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    public static final int NOTIFICATION_ID = 475;

    private void insertUserEntry(Context context, String id, Cursor memberCursor) {
        DateTime dayTime = new DateTime();
        ContentValues userValues = new ContentValues();
        userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, id);
        userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry._ID)));
        userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME)));
        userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
        userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());

        context.getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValues);
    }

    private void insertGroupEntry(Context context, String groupId, String groupName, String number, String senderId, String groupUrl){

        DateTime dayTime = new DateTime();
        ContentValues groupValues = new ContentValues();
        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_ID, groupId);
        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_NAME, groupName);
        groupValues.put(YourTurnContract.GroupEntry.COLUMN_USER_KEY, number);
        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATOR, senderId);
        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_THUMBNAIL, groupUrl);
        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());

        context.getContentResolver().insert(YourTurnContract.GroupEntry.CONTENT_URI, groupValues);
    }

    private void createNotification(final Context context, String senderId, String groupName, String groupId, String groupUrl, String friendList){

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent groupIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
                .setContentText("You been added into a new group called " + groupName)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(groupIntent);

        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification.build());

        String[] friendChunks = friendList.split(",");

        DateTime dayTime = new DateTime();
        for(String chunk : friendChunks) {
            String[] contactChunks = chunk.split(":");
            Contact contact = new Contact(contactChunks[0], contactChunks[1], contactChunks[2]);

            Cursor memberCursor = context.getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, null,
                    YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?", new String[]{contact.getPhoneNumber()}, null);

            if(memberCursor != null && memberCursor.getCount() > 0) {
                memberCursor.moveToNext();

                Cursor userCursor = context.getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI,
                        null,
                        YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER +"=?",
                        new String[]{contact.getPhoneNumber()},
                        null);
                if(userCursor != null && userCursor.getCount() <= 0) {
                    insertUserEntry(context, contact.getPhoneNumber(), memberCursor);
                }

            }else if(memberCursor != null && memberCursor.getCount() <=0){
                ContentValues memberValues = new ContentValues();
                memberValues.put(YourTurnContract.MemberEntry._ID, contact.getId());
                memberValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME, contact.getName());
                memberValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER, contact.getPhoneNumber());
                memberValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_CREATED_DATE, dayTime.getMillis());
                memberValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_UPDATED_DATE, dayTime.getMillis());

                context.getContentResolver().insert(YourTurnContract.MemberEntry.CONTENT_URI, memberValues);

                ContentValues userValues = new ContentValues();
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, contact.getPhoneNumber());
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, contact.getId());
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, contact.getName());
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());

                context.getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValues);

            }

            insertGroupEntry(context, groupId, groupName, contact.getPhoneNumber(), senderId, groupUrl);
            memberCursor.close();

        }

        insertGroupEntry(context, groupId, groupName, senderId, senderId, groupUrl);

    }
}
