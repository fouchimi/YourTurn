package com.social.yourturn.broadcast;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.social.yourturn.data.YourTurnContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 6/15/2017.
 */

public class NameBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = NameBroadcastReceiver.class.getSimpleName();
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
        String senderId = "", name = "";
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
                        Log.d(TAG, "Title: " + senderId);
                    }else if(key.equals("name")){
                        name = json.getString(key);
                        Log.d(TAG, "Message: " + name);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(senderId.length() > 0 && name.length() > 0) {
                    updateName(context, senderId, name);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }


    private void updateName(Context context, String senderId, String name) {

        ContentValues memberValue = new ContentValues();
        memberValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME, name);

        long member_id = context.getContentResolver().update(YourTurnContract.MemberEntry.CONTENT_URI, memberValue,
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(senderId), null);
        if(member_id > 0) {
            Log.d(TAG, "name successfully updated in members table with id: " + member_id);
        }

        ContentValues userValue = new ContentValues();
        userValue.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, name);

        long user_id = context.getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI, userValue,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(senderId), null);

        if(user_id > 0) {
            Log.d(TAG, "name successfully updated in user table with id: " + user_id);
        }else {
            memberValue.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, senderId);
            Cursor nameCursor =  context.getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, null, YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(senderId), null, null);
            if(nameCursor != null && nameCursor.getCount() > 0) {
                nameCursor.moveToNext();
                String contactId = nameCursor.getString(nameCursor.getColumnIndex(YourTurnContract.MemberEntry._ID));
                userValue.put(YourTurnContract.UserEntry.COLUMN_USER_ID, contactId);
            }
            nameCursor.close();
            context.getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, memberValue);
        }
    }
}
