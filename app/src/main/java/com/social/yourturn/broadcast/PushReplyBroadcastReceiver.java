package com.social.yourturn.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import android.widget.Toast;

import com.social.yourturn.data.YourTurnContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 5/22/2017.
 */

public class PushReplyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = PushReplyBroadcastReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "Reply Broadcast Receiver intent null");
        }else {
            processReceiverPush(context, intent);
        }
    }

    private void processReceiverPush(Context context, Intent intent) {
        String receiverId = "";
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
                    if(key.equals("rec_id")) {
                        receiverId = json.getString(key);
                        Log.d(TAG, "receiver Phone Number: " + receiverId);
                        displayAcceptanceMessage(context, receiverId);
                        break;
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }


    private void displayAcceptanceMessage(Context context, String rec_id){

        Cursor receiverCursor = context.getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, new String[]{YourTurnContract.UserEntry.COLUMN_USER_NAME},
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(rec_id), null, null);
        if(receiverCursor != null && receiverCursor.getCount() > 0) {
            receiverCursor.moveToFirst();
            String receiverName = receiverCursor.getString(receiverCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME));
            Toast.makeText(context, receiverName + " accepted to pay", Toast.LENGTH_LONG).show();
        }
    }
}
