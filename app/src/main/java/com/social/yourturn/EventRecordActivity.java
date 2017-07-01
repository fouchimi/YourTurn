package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseCloud;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.social.yourturn.adapters.EventRecordAdapter;
import com.social.yourturn.broadcast.EventBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Event;
import com.social.yourturn.utils.ParseConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class EventRecordActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EventRecordActivity.class.getSimpleName();
    private Event mEvent = null;
    private static final int LOADER_ID = 0;
    ArrayList<Contact> contactList = null;
    EventRecordAdapter recordAdapter = null;
    private BroadcastReceiver eventBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView mListView  = (ListView) findViewById(R.id.group_contact_list);

        Intent intent = getIntent();
        if(intent != null) {
            mEvent = intent.getParcelableExtra(getString(R.string.selected_event));
            String totalAmount = intent.getStringExtra(getString(R.string.totalAmount));
            if(mEvent != null) {
                Log.d(TAG, "Event name: " + mEvent.getName());
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mEvent.getName());
                }
                contactList = mEvent.getContactList();
                recordAdapter = new EventRecordAdapter(this, contactList);
                mListView.setAdapter(recordAdapter);

                savedAllInBackground(mEvent, contactList, totalAmount);
            }
        }

        eventBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "event broadcast invoked !");
            }
        };

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private Task<ParseObject> savedEventAsync(List<ParseObject> list) {
        final TaskCompletionSource<ParseObject> tcs = new TaskCompletionSource<>();
        ParseObject.saveAllInBackground(list, e -> {
            if (e == null) {
                tcs.setResult(null);
            } else {
                tcs.setError(e);
            }
        });
        return tcs.getTask();
    }

    private Task<ParseObject> savedLedgerAsync(List<ParseObject> rows){
        final TaskCompletionSource<ParseObject> tcs = new TaskCompletionSource<>();
        ParseObject.saveAllInBackground(rows, e -> {
            if(e == null) {
                tcs.setResult(null);
            }else {
                tcs.setError(e);
            }
        });

        return tcs.getTask();
    }

    private Task<ParseObject> savedScoreAsync(ParseQuery<ParseObject> query, final String score, final String phoneNumber) {
        final TaskCompletionSource<ParseObject> tcs = new TaskCompletionSource<>();
        query.getFirstInBackground((row, e) -> {
            if(e == null){
                tcs.setResult(row);
                String parsedScore = row.getString(ParseConstant.USER_SCORE_COLUMN);
                if(parsedScore == null) {
                    row.put(ParseConstant.USERNAME_COLUMN, phoneNumber);
                    row.put(ParseConstant.USER_SCORE_COLUMN, String.valueOf(Double.parseDouble(parsedScore) + Double.parseDouble(score)));
                }
                else{
                    double scoredValue = Double.parseDouble(parsedScore);
                    row.put(ParseConstant.USER_SCORE_COLUMN, String.valueOf(scoredValue + Double.parseDouble(score)));
                    row.put(ParseConstant.USERNAME_COLUMN, phoneNumber);
                }
                row.saveInBackground(ex -> {
                    if(ex == null){
                        Log.d(TAG, "Score saved successfully");
                    }else {
                        Log.d(TAG, "Error occur!");
                    }
                });
            }else {
                tcs.setError(null);
                // will create score table if not existing
                ParseObject scoreTable = new ParseObject(ParseConstant.SCORE_TABLE);
                scoreTable.put(ParseConstant.USERNAME_COLUMN, phoneNumber);
                scoreTable.put(ParseConstant.USER_SCORE_COLUMN, score);

                scoreTable.saveInBackground(e1 -> {
                    if(e1 == null){
                        Log.d(TAG, "Score saved successfully");
                    }else {
                        Log.d(TAG, e1.getMessage());
                    }
                });
            }
        });
        return tcs.getTask();
    }

    private void savedAllInBackground(final Event event, final ArrayList<Contact> contactList, final String totalAmount){
        List<ParseObject> eventList = new ArrayList<>();

        for(Contact contact : contactList) {
            final ParseObject eventTable = new ParseObject(ParseConstant.EVENT_TABLE);

            eventTable.put(ParseConstant.EVENT_NAME, event.getName());
            eventTable.put(ParseConstant.EVENT_THUMBNAIL_COLUMN, event.getEventUrl());
            eventTable.put(ParseConstant.USER_ID_COLUMN, getUsername());
            eventTable.put(ParseConstant.FRIEND_ID, contact.getPhoneNumber());
            eventTable.put(ParseConstant.EVENT_ID, event.getEventId());

            eventList.add(eventTable);
        }

        savedEventAsync(eventList).onSuccessTask(task -> {
            List<ParseObject> ledgerList = new ArrayList<>();
            for(Contact contact : contactList){
                final ParseObject ledger_group_table = new ParseObject(ParseConstant.LEDGER_EVENT_TABLE);
                ledger_group_table.put(ParseConstant.LEDGER_EVENT_ID, event.getEventId());
                ledger_group_table.put(ParseConstant.LEDGER_SHARED_AMOUNT, contact.getShare());
                ledger_group_table.put(ParseConstant.LEDGER_USER_ID, contact.getPhoneNumber());
                ledger_group_table.put(ParseConstant.LEDGER_TOTAL_AMOUNT, totalAmount);

                ledgerList.add(ledger_group_table);

            }

            return savedLedgerAsync(ledgerList);

        }).onSuccessTask(task -> {

            // Create a trivial completed task as a base case.
            ArrayList<Task<ParseObject>> tasks = new ArrayList<>();

            for(Contact contact : contactList){
                final ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstant.SCORE_TABLE);
                query.whereEqualTo(ParseConstant.USERNAME_COLUMN, contact.getPhoneNumber());

                double paidValue = Double.parseDouble(contact.getPaid());
                double requestedValue = Double.parseDouble(contact.getRequested());
                if(paidValue > requestedValue) contact.setShare(String.valueOf(paidValue-requestedValue));
                else if(paidValue < requestedValue) contact.setShare(String.valueOf(paidValue-requestedValue));
                else contact.setShare(getString(R.string.zero_default_values));
                tasks.add(savedScoreAsync(query, contact.getShare(), contact.getPhoneNumber()));
            }
            return Task.whenAllResult(tasks);
        }).continueWith(new Continuation<List<ParseObject>, Void>() {
            public Void then(Task<List<ParseObject>> task) throws Exception {
                // Every user score saved
                if(task.isFaulted()){
                    Toast.makeText(EventRecordActivity.this, "Couldn't save data successfully", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(EventRecordActivity.this, "Payment successfully saved", Toast.LENGTH_LONG).show();

                    String userIds = "";

                    for(Contact contact : contactList ){
                        if(!contact.getPhoneNumber().equals(getUsername())){
                            userIds += contact.getPhoneNumber() + ",";
                        }
                    }

                    if(userIds != null && userIds.length() > 0){
                        userIds = userIds.substring(0, userIds.length()-1);
                    }

                    // Send Event creation here
                    HashMap<String, Object> payload = new HashMap<>();
                    payload.put("senderId", getUsername());
                    payload.put("eventId", event.getEventId());
                    payload.put("eventName", event.getName());
                    payload.put("eventUrl", event.getEventUrl());
                    payload.put("targetIds", userIds);
                    ParseCloud.callFunctionInBackground("eventChannel", payload, (object, e) -> {
                        if(e == null) {
                            Log.d(TAG, "Event creation was successful");
                        }else {
                            Log.d(TAG, e.getMessage());
                        }
                    });
                }
                return null;
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, YourTurnContract.LedgerEntry.CONTENT_URI, null,
                YourTurnContract.LedgerEntry.COLUMN_EVENT_KEY + "=?", new String[]{mEvent.getEventId()}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "data: " + data.getCount());
        HashMap<String, Double> map = new HashMap<>();
        for(int i=0; i < data.getCount(); i++){
            data.moveToNext();
            String userId = data.getString(data.getColumnIndex(YourTurnContract.LedgerEntry.COLUMN_USER_KEY));
            String userShareAmount = data.getString(data.getColumnIndex(YourTurnContract.LedgerEntry.COLUMN_USER_SHARE));
            Log.d(TAG, "user Id: " + userId + " value: " + userShareAmount);
            if(!map.containsKey(userId)) {
                map.put(userId, Double.parseDouble(userShareAmount));
            }else {
                double value = map.get(userId);
                value += Double.parseDouble(userShareAmount);
                map.put(userId, value);
            }
        }

        Iterator itr = map.entrySet().iterator();
        while (itr.hasNext()){
            Map.Entry pair = (Map.Entry)itr.next();
            for(Contact contact : contactList){
                if(contact.getPhoneNumber().equals(pair.getKey())) {
                    contact.setShare(String.valueOf(pair.getValue()));
                    break;
                }
            }
            Log.d(TAG, "key: " + pair.getKey());
        }

        recordAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(eventBroadcastReceiver, new IntentFilter(EventBroadcastReceiver.intentAction));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(eventBroadcastReceiver);
    }

    private String getUsername() {
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }
}
