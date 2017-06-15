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
        String senderId = "", groupId = "", groupName = "", groupMembers = "";
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
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(senderId.length() > 0 && groupName.length() > 0 && groupId.length() > 0 && groupMembers.length() > 0){
                    createNotification(context, senderId,  groupName, groupId, groupMembers);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    public static final int NOTIFICATION_ID = 475;

    private void createNotification(final Context context, String senderId, String groupName, String groupId, String groupMemberIds){

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent groupIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Cursor cursor = context.getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(senderId), null, null);

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

        final Cursor groupCursor = context.getContentResolver().query(YourTurnContract.GroupEntry.CONTENT_URI, null,
                YourTurnContract.GroupEntry.COLUMN_GROUP_CREATOR + " = " +
                        DatabaseUtils.sqlEscapeString(senderId) + " AND " +
                        YourTurnContract.GroupEntry.COLUMN_GROUP_ID + " = " +
                        DatabaseUtils.sqlEscapeString(groupId), null, null);

        if(groupCursor != null && groupCursor.getCount() == 0) {
            groupMemberIds += "," + senderId;
            String[] ids = groupMemberIds.split(",");

            for(String id : ids) {
                Cursor memberCursor = context.getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, null,
                        YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(id), null, null);
                DateTime dayTime = new DateTime();
                if(memberCursor != null && memberCursor.getCount() > 0) {
                    memberCursor.moveToNext();
                    final ContentValues groupValues = new ContentValues();
                    groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_ID, groupId);
                    groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_NAME, groupName);
                    groupValues.put(YourTurnContract.GroupEntry.COLUMN_USER_KEY, id);
                    groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATOR, senderId);
                    groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
                    groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());

                    ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery(ParseConstant.GROUP_TABLE);
                    groupQuery.getInBackground(groupId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject row, ParseException e) {
                            if(e == null) {
                                ParseFile groupImage = (ParseFile) row.get(ParseConstant.GROUP_THUMBNAIL_COLUMN);
                                if(groupImage != null) {
                                    groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_THUMBNAIL, groupImage.getUrl());
                                }
                                context.getContentResolver().insert(YourTurnContract.GroupEntry.CONTENT_URI, groupValues);
                            }else {
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    });
                }
                memberCursor.close();
            }

        }else {
            Log.d(TAG, "Record was already inserted");
        }

        groupCursor.close();

    }
}
