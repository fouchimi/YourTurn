package com.social.yourturn.broadcast;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.social.yourturn.data.YourTurnContract;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 6/10/2017.
 */

public class LedgerBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LedgerBroadcastReceiver.class.getSimpleName();
    public static final String intentAction = "com.placeParse.push.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "Group Broadcast Receiver intent null");
        }else {
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String sender="", sharedValue = "", friendIds = "", groupId = "", totalAmount="";
        String action = intent.getAction();
        Log.d(TAG, "got action " + action);
        if(action.equals(intentAction)){
            String channel = intent.getExtras().getString("com.placeParse.Channel");
            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            try{
                JSONObject json = new JSONObject(intent.getExtras().getString("com.placeParse.Data"));
                // Iterate the placeParse keys if needed
                Iterator<String> itr = json.keys();
                while(itr.hasNext()){
                    String key = (String) itr.next();
                    if(key.equals("sharedValue")) {
                        sharedValue = json.getString(key);
                        Log.d(TAG, "sharedValue: " + sharedValue);
                    }else if(key.equals("groupId")){
                        groupId = json.getString(key);
                        Log.d(TAG, "group Id: " + groupId);
                    }else if(key.equals("friendIds")) {
                        friendIds = json.getString(key);
                        Log.d(TAG, "friendIds: " + friendIds);
                    }else if(key.equals("totalAmount")){
                        totalAmount = json.getString(key);
                        Log.d(TAG, "total Amount: " + totalAmount);
                    }else if(key.equals("sender")){
                        sender = json.getString(key);
                        Log.d(TAG, "sender: " + sender);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(sender.length() > 0 && groupId.length() > 0 && sharedValue.length() > 0 && sharedValue.length() > 0 && friendIds.length() > 0 && totalAmount.length() > 0) {
                    savedLedgerRecords(context, sender, groupId, sharedValue, friendIds, totalAmount);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }


    private void savedLedgerRecords(Context context, String sender, String groupId, String sharedValue, String recipients, String totalAmount){

        recipients +=","+ sender;
        String[] recipientList = recipients.split(",");
        String[] share = sharedValue.split(",");
        DateTime dayTime = new DateTime();

        for(int i=0; i < recipientList.length; i++){
            ContentValues ledgerValues = new ContentValues();
            Cursor cursor = context.getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null, YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER +"=" + DatabaseUtils.sqlEscapeString(recipientList[i]), null, null);
            Log.d(TAG, "Steve recipient id: " +  recipientList[i]);
            if(cursor.getCount() == 0){
                ContentValues userValue = new ContentValues();
                userValue.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, recipientList[i]);
                userValue.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
                userValue.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
                context.getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValue);
            }
            cursor.close();
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_KEY, groupId);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_USER_KEY, recipientList[i]);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_USER_SHARE, share[i]);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_TOTAL_AMOUNT, totalAmount);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());

            context.getContentResolver().insert(YourTurnContract.LedgerEntry.CONTENT_URI, ledgerValues);
        }

    }
}
