package com.social.yourturn.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ousma on 5/14/2017.
 */

public class ParsePushBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = ParsePushBroadcastReceiver.class.getSimpleName();
    public static final String INTENT_ACTION = "com.parse.push.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            Log.d(TAG, "Receiver intent null");
        }else {
            processPush(context, intent);
        }
    }

    private void processPush(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "got action " + action);
        String alert= "", title="";
        if(action.equals(INTENT_ACTION)){
            String channel = intent.getExtras().getString("com.parse.Channel");
            try{
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                // Iterate the parse keys if needed
                Iterator<String> itr = json.keys();
                while(itr.hasNext()){
                    String key = (String) itr.next();
                    String value = json.getString(key);
                    Log.d(TAG, "..." + key + " => " + value);
                    if(key.equals("title")){
                        title = value;
                    }else if(key.equals("alert")){
                        alert = value;
                    }
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }
}
