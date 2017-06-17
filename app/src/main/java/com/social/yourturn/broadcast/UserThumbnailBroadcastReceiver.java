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
 * Created by ousma on 6/11/2017.
 */

public class UserThumbnailBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = UserThumbnailBroadcastReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "null intent");
        }else {
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String sender = "", thumbnailUrl = "";
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
                    if(key.equals("sender")) {
                        sender = json.getString(key);
                        Log.d(TAG, "Title: " + sender);
                    }else if(key.equals("imageUrl")){
                        thumbnailUrl = json.getString(key);
                        Log.d(TAG, "Message: " + thumbnailUrl);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(sender.length() > 0 && thumbnailUrl != null) {
                    updateImageUrl(context, sender, thumbnailUrl);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }


    private void updateImageUrl(Context context, String sender, String url){

        ContentValues memberValue = new ContentValues();
        memberValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL, url);

        long member_id = context.getContentResolver().update(YourTurnContract.MemberEntry.CONTENT_URI,
                memberValue,
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?",
                new String[]{sender});
        if(member_id > 0) {
            Log.d(TAG, "imageUrl successfully updated in members table with id: " + member_id);
        }

        ContentValues userValue = new ContentValues();
        userValue.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, url);

        long user_id = context.getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI,
                userValue,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=?",
                new String[]{sender});

        if(user_id > 0) {
            Log.d(TAG, "imageUrl successfully updated in user table with id: " + user_id);
        }
    }
}
