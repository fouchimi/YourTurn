package com.social.yourturn.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ConfirmPaymentIntentService extends IntentService {

    private static final String TAG = ConfirmPaymentIntentService.class.getSimpleName();
    private Vector<ContentValues> ledgerVectors = new Vector<>();

    public ConfirmPaymentIntentService() {
        super("ConfirmPaymentIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(getString(R.string.paymentReceiver));
        String groupId = intent.getStringExtra(getString(R.string.groupId));
        String totalAmount = intent.getStringExtra(getString(R.string.totalAmount));
        ArrayList<Contact> friendList = intent.getParcelableArrayListExtra(getString(R.string.friendList));

        final Bundle bundle = new Bundle();

        DateTime dayTime = new DateTime();
        // Insert into contact provider here
        for(Contact contact: friendList){
            ContentValues ledgerValues = new ContentValues();
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_KEY, groupId);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_USER_KEY, contact.getPhoneNumber());
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_USER_SHARE, contact.getShare());
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_TOTAL_AMOUNT, totalAmount);
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
            ledgerValues.put(YourTurnContract.LedgerEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());

            ledgerVectors.add(ledgerValues);
        }

        List<ParseObject> list = new ArrayList<>();
        for(Contact contact : friendList){
            final ParseObject ledger_group_table = new ParseObject(ParseConstant.LEDGER_GROUP_TABLE);
            ledger_group_table.put(ParseConstant.LEDGER_GROUP_ID, groupId);
            ledger_group_table.put(ParseConstant.LEDGER_SHARED_AMOUNT, contact.getShare());
            ledger_group_table.put(ParseConstant.LEDGER_USER_ID, contact.getPhoneNumber());
            ledger_group_table.put(ParseConstant.LEDGER_TOTAL_AMOUNT, totalAmount);

            list.add(ledger_group_table);
        }

        ParseObject.saveAllInBackground(list, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    if(ledgerVectors.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[ledgerVectors.size()];
                        ledgerVectors.toArray(cvArray);
                        Log.d(TAG, "ledger bulk insert successful");
                        getContentResolver().bulkInsert(YourTurnContract.LedgerEntry.CONTENT_URI, cvArray);
                    }
                    bundle.putString(getString(R.string.result_value), getString(R.string.payment_successful_msg));
                    receiver.send(Activity.RESULT_OK, bundle);
                }else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

}
