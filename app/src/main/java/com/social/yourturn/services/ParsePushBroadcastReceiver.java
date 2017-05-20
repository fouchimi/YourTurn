package com.social.yourturn.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.social.yourturn.ConfirmAmountActivity;
import com.social.yourturn.R;

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

    protected ConfirmAmountActivity getActivity(Context context, Intent intent){
        return ConfirmAmountActivity.getInstance();
    }

    private void processPush(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "got action " + action);
        if(action.equals(INTENT_ACTION)){
            String channel = intent.getExtras().getString("com.parse.Channel");
            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            try{
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                // Iterate the parse keys if needed
                Iterator<String> itr = json.keys();
                String value = "";
                while(itr.hasNext()){
                    String key = (String) itr.next();
                    value += json.getString(key) + ",";
                    Log.d(TAG, "..." + key + " => " + value);
                    if(key.equals("receiver")) {
                        createNotification(context, value);
                    }
                }
            }catch (JSONException ex){
                ex.printStackTrace();
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    public static final int NOTIFICATION_ID = 45;

    private void createNotification(Context context, String dataValue) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("Notification: " + dataValue)
                .setContentText(dataValue);

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void launchSomeActivity(Context context, String datavalue) {
        Intent pupInt = new Intent(context, ConfirmAmountActivity.class);
        pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pupInt.putExtra("data", datavalue);
        context.getApplicationContext().startActivity(pupInt);
    }

}
