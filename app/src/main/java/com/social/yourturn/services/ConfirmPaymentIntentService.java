package com.social.yourturn.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.social.yourturn.LocationActivity;
import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.UUID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ConfirmPaymentIntentService extends IntentService {

    private static final String TAG = ConfirmPaymentIntentService.class.getSimpleName();

    public ConfirmPaymentIntentService() {
        super("ConfirmPaymentIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(getString(R.string.paymentReceiver));
        final String eventName = intent.getStringExtra(getString(R.string.selected_event));
        final String placeUrl = intent.getStringExtra(LocationActivity.PLACE_URL);
        String totalAmount = intent.getStringExtra(getString(R.string.totalAmount));
        ArrayList<Contact> friendList = intent.getParcelableArrayListExtra(getString(R.string.friendList));

        final Bundle bundle = new Bundle();
        final String eventId = UUID.randomUUID().toString();

        // update latest event entry flag with "0" string because it will allow me to fetch recent added event in EventFragment
        Cursor cursor = getContentResolver().query(YourTurnContract.EventEntry.CONTENT_URI,
                new String[]{YourTurnContract.EventEntry.COLUMN_EVENT_ID},
                YourTurnContract.EventEntry.COLUMN_EVENT_FLAG + "=?", new String[]{"1"}, null);
        if(cursor != null && cursor.getCount() > 0){
            cursor.moveToFirst();
            String evtId = cursor.getString(cursor.getColumnIndex(YourTurnContract.EventEntry.COLUMN_EVENT_ID));
            ContentValues values  = new ContentValues();
            values.put(YourTurnContract.EventEntry.COLUMN_EVENT_FLAG, "0");
            getContentResolver().update(YourTurnContract.EventEntry.CONTENT_URI,
                    values,
                    YourTurnContract.EventEntry.COLUMN_EVENT_ID + "=?", new String[]{evtId});
        }

        cursor.close();


        DateTime dayTime = new DateTime();

        // save event group locally here
        for(Contact contact : friendList){
            ContentValues eventValues = new ContentValues();
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_ID, eventId);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_NAME, WordUtils.capitalize(eventName, null));
            eventValues.put(YourTurnContract.EventEntry.COLUMN_USER_KEY, contact.getPhoneNumber());
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_URL, placeUrl);
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_CREATOR, getUsername());
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_FLAG, "1");
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_CREATED_DATE, dayTime.getMillis());
            eventValues.put(YourTurnContract.EventEntry.COLUMN_EVENT_UPDATED_DATE, dayTime.getMillis());

            getContentResolver().insert(YourTurnContract.EventEntry.CONTENT_URI, eventValues);
        }

        // Insert into ledger values here
        for(Contact contact: friendList){
            ContentValues ledgerValues = new ContentValues();
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_EVENT_KEY, eventId);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_USER_KEY, contact.getPhoneNumber());
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_USER_SHARE, contact.getPaid());
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_TOTAL_AMOUNT, totalAmount);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());

            getContentResolver().insert(YourTurnContract.LedgerEntry.CONTENT_URI, ledgerValues);
        }

        bundle.putString(getString(R.string.result_value), getString(R.string.payment_successful_msg));
        bundle.putString(getString(R.string.selected_event_id), eventId);
        receiver.send(Activity.RESULT_OK, bundle);
    }

    private String getUsername() {
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }

}
