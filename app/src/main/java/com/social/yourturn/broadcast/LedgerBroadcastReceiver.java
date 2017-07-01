package com.social.yourturn.broadcast;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.social.yourturn.data.YourTurnContract;

import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 6/10/2017.
 */

public class LedgerBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LedgerBroadcastReceiver.class.getSimpleName();
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
        String sender="", sharedValue = "", friendIds = "", eventId = "", totalAmount="", eventName="", eventUrl="";
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
                    if(key.equals("sender")){
                        sender = json.getString(key);
                        Log.d(TAG, "sender: " + sender);
                    }else if(key.equals("eventId")){
                        eventId = json.getString(key);
                        Log.d(TAG, "group Id: " + eventId);
                    }else if(key.equals("eventName")){
                        eventName = json.getString(key);
                        Log.d(TAG, "event name: " + eventName);
                    }else if(key.equals("eventUrl")){
                        eventUrl = json.getString(key);
                        Log.d(TAG, "event url: " + eventUrl);
                    }else if(key.equals("sharedValue")) {
                        sharedValue = json.getString(key);
                        Log.d(TAG, "sharedValue: " + sharedValue);
                    }else if(key.equals("friendIds")) {
                        friendIds = json.getString(key);
                        Log.d(TAG, "targetIds: " + friendIds);
                    }else if(key.equals("totalAmount")){
                        totalAmount = json.getString(key);
                        Log.d(TAG, "total Amount: " + totalAmount);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                }
                if(sender.length() > 0 &&
                        eventId.length() > 0 &&
                        sharedValue.length() > 0 &&
                        sharedValue.length() > 0 &&
                        friendIds.length() > 0 &&
                        totalAmount.length() > 0 &&
                        eventName.length() > 0 &&
                        eventUrl.length() > 0) {
                    savedLedgerRecords(context, sender, eventId, sharedValue, friendIds, totalAmount, eventName, eventUrl);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }


    private void savedLedgerRecords(Context context, String sender, String eventId, String sharedValue, String recipients, String totalAmount, String eventName, String eventUrl){

        recipients +=","+ sender;
        String[] recipientList = recipients.split(",");
        String[] share = sharedValue.split(",");
        DateTime dayTime = new DateTime();

        for(String recipient: recipientList){
            ContentValues eventValues = new ContentValues();
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_ID, eventId);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_NAME, WordUtils.capitalize(eventName, null));
            eventValues.put(YourTurnContract.EventEntry.COLUMN_USER_KEY, recipient);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_URL, eventUrl);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_CREATOR, sender);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_CREATED_DATE, dayTime.getMillis());
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_UPDATED_DATE, dayTime.getMillis());

            context.getContentResolver().insert(YourTurnContract.EventEntry.CONTENT_URI, eventValues);
        }

        for(int i=0; i < recipientList.length; i++){
            ContentValues ledgerValues = new ContentValues();
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_EVENT_KEY, eventId);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_USER_KEY, recipientList[i]);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_USER_SHARE, share[i]);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_TOTAL_AMOUNT, totalAmount);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());

            context.getContentResolver().insert(YourTurnContract.LedgerEntry.CONTENT_URI, ledgerValues);
        }

    }
}
