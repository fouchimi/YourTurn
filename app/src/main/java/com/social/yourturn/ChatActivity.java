package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.social.yourturn.adapters.ChatAdapter;
import com.social.yourturn.broadcast.OfflineChannelReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Message;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import me.leolin.shortcutbadger.ShortcutBadger;

public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private EditText mEditText;
    private RecyclerView rvChat;
    private ArrayList<Message> chatList;
    private ChatAdapter mAdapter;
    private boolean isFriendOnline = false, online = false;
    private TextView emptyTextView;
    private Contact contact = null;
    private MessageBroadcastReceiver messageBroadcastReceiver = new MessageBroadcastReceiver();
    private SenderHandshakeReceiver senderBroadcastReceiver = new SenderHandshakeReceiver();
    private static final int LOADER_ID = 12;
    private BroadcastReceiver offlineBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mEditText = (EditText) findViewById(R.id.etMessage);
        ImageButton mButton = (ImageButton) findViewById(R.id.btSend);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        emptyTextView = (TextView) findViewById(R.id.empty_view);
        emptyTextView.setTypeface(typeface);

        rvChat = (RecyclerView) findViewById(R.id.rvChat);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvChat.setLayoutManager(linearLayoutManager);
        rvChat.setHasFixedSize(true);

        chatList = new ArrayList<>();
        mAdapter = new ChatAdapter(this, chatList);
        rvChat.setAdapter(mAdapter);

        Intent intent = getIntent();

        if(intent != null) {
            contact = intent.getParcelableExtra(getString(R.string.selected_contact));
            boolean flag = intent.getBooleanExtra("clearCount", false);
            if(flag){
                ShortcutBadger.removeCount(this);
                HashMap<String, Object> payload = new HashMap<>();

                payload.put("targetId", getUsername());
                payload.put("clearCounter", flag);
                ParseCloud.callFunctionInBackground("offlineChannel", payload, (object, e) -> {
                    if(e == null) {
                        Log.d(TAG, "Message successfully sent !");
                    }else {
                        Log.d(TAG, e.getMessage());
                    }
                });
            }
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);

            ActionBar actionBar = getSupportActionBar();

            if(actionBar != null && contact != null) actionBar.setTitle(WordUtils.capitalize(contact.getName().toLowerCase(), null));
        }

        offlineBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "On received invoked");
                Toast.makeText(getApplicationContext(), "On Received invoked !", Toast.LENGTH_SHORT).show();
            }
        };

        mButton.setOnClickListener(new ButtonClickListener());

        rvChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                rvChat.post(() -> {
                    if(chatList.size() > 0) rvChat.smoothScrollToPosition(chatList.size()-1);
                });
            }
        });

    }

    public boolean isFriendOnline() {
        return isFriendOnline;
    }

    public void setFriendOnline(boolean friendOnline) {
        isFriendOnline = friendOnline;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginStatus(getUsername());
        LocalBroadcastManager.getInstance(this).registerReceiver(offlineBroadcastReceiver, new IntentFilter(OfflineChannelReceiver.intentAction));
        IntentFilter filter = new IntentFilter("com.parse.push.intent.RECEIVE");
        registerReceiver(messageBroadcastReceiver, filter);
        registerReceiver(senderBroadcastReceiver, filter);

        online = true;
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("connectedStatus", online);
        payload.put("targetId", contact.getPhoneNumber());

        ParseCloud.callFunctionInBackground("handShakeChannel", payload, (object, e) -> {
            if(e == null) {
                Log.d(TAG, "first handshake sent !");
            }else {
                Log.d(TAG, e.getMessage());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        online = false;
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("connectedStatus", online);
        payload.put("targetId", contact.getPhoneNumber());
        ParseCloud.callFunctionInBackground("handShakeChannel", payload, (object, e) -> {
            if(e == null) {
                Log.d(TAG, "Disconnecting ...  !");
                unregisterReceiver(senderBroadcastReceiver);
            }else {
                Log.d(TAG, e.getMessage());
            }
        });
        unregisterReceiver(messageBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(offlineBroadcastReceiver);
    }

    private Task<Void> saveMessageAsync(ParseObject messageObject){
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        messageObject.saveInBackground(e -> {
            if(e == null){
                tcs.setResult(null);
                Log.d(TAG, "Message saved successfully");
            }else {
                tcs.setError(e);
                Log.d(TAG, "Message couldn't be saved successfully");
            }
        });

        return tcs.getTask();
    }

    private String getUsername() {
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }

    private void saveMessageInDb(Message message){
        DateTime dateTime = new DateTime();
        ContentValues messageValues = new ContentValues();
        messageValues.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY, message.getSenderKey());
        messageValues.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY, message.getReceiverKey());
        messageValues.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_BODY, message.getBody());
        messageValues.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_TYPE, "text");
        messageValues.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_CREATED_DATE, dateTime.getMillis());
        messageValues.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_UPDATED_DATE, dateTime.getMillis());

        getContentResolver().insert(YourTurnContract.MessageEntry.CONTENT_URI, messageValues);
    }

    private void updateLoginStatus(String username){
        ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstant.ONLINE_TABLE);
        query.whereEqualTo(ParseConstant.USERNAME_COLUMN, username);

        try {
            if(query.count() <= 0){
                ParseObject onlineTable = new ParseObject(ParseConstant.ONLINE_TABLE);
                onlineTable.put(ParseConstant.USERNAME_COLUMN, username);
                onlineTable.put(ParseConstant.STATUS, true);
                onlineTable.saveInBackground(e -> {
                    if(e == null){
                        Log.d(TAG, "Table created successfully");
                    }else {
                        Log.d(TAG, "An error occur !");
                    }
                });
            }else {
                query.getFirstInBackground((row, e) -> {
                    if(e == null){
                        row.put(ParseConstant.STATUS, true);
                        row.saveInBackground(e1 -> {
                            if(e1 == null){
                                Log.d(TAG, "Online table updated successfully");
                            }else {
                                Log.d(TAG, "Online table couldn't be updated successfully");
                            }
                        });
                    }else {
                        Log.d(TAG, "An error occur !");
                    }
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == LOADER_ID && contact != null){
            return new CursorLoader(this, YourTurnContract.MessageEntry.CONTENT_URI, null,
                    "(" + YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY +"=?" + " AND " + YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY + "=?" + ")" +
            " OR " +  "(" +YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY + "=?" + " AND " + YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY + "=?" + ")",
                    new String[]{getUsername(), contact.getPhoneNumber(), contact.getPhoneNumber(), getUsername()},
                    YourTurnContract.MemberEntry.COLUMN_MEMBER_CREATED_DATE + " ASC LIMIT 400");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() > 0){
            chatList.clear();
            while (data.moveToNext()){
                String senderId = data.getString(data.getColumnIndex(YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY));
                String receiverId = data.getString(data.getColumnIndex(YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY));
                String body = data.getString(data.getColumnIndex(YourTurnContract.MessageEntry.COLUMN_MESSAGE_BODY));
                long createdDate = data.getLong(data.getColumnIndex(YourTurnContract.MessageEntry.COLUMN_MESSAGE_CREATED_DATE));
                long updatedDate = data.getLong(data.getColumnIndex(YourTurnContract.MessageEntry.COLUMN_MESSAGE_UPDATED_DATE));

                Message message = new Message();
                message.setBody(body);
                message.setSenderId(senderId);
                message.setReceiverKey(receiverId);
                message.setCreatedDateKey(createdDate);
                message.setUpdatedDateKey(updatedDate);

                chatList.add(message);

            }

            if(chatList.size() > 0) {
                emptyTextView.setVisibility(View.GONE);
                if(chatList.size() == 1){
                    chatList.get(0).setFirstOfTheDay(true);
                }else {
                    chatList.get(0).setFirstOfTheDay(true);
                    for(int i= 1; i < chatList.size(); i++){
                        Calendar calendarForPrevious = Calendar.getInstance();
                        Calendar calendarForNext = Calendar.getInstance();
                        Date previousMessageDate = new Date(chatList.get(i-1).getCreatedDateKey());
                        Date nextMessageDate = new Date(chatList.get(i).getCreatedDateKey());
                        calendarForPrevious.setTime(previousMessageDate);
                        calendarForNext.setTime(nextMessageDate);
                        if(calendarForNext.get(Calendar.YEAR) == calendarForPrevious.get(Calendar.YEAR) &&
                                calendarForNext.get(Calendar.DAY_OF_YEAR) != calendarForPrevious.get(Calendar.DAY_OF_YEAR)){
                            chatList.get(i).setFirstOfTheDay(true);
                        }
                    }
                }

                mAdapter.notifyDataSetChanged();
                rvChat.scrollToPosition(chatList.size()-1);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private interface ViewListener{
        void updateView(Context context, String sender, String receiver);
    }

    private interface ActionBarSubtitleListener{
        void updateActionBar(boolean flag);
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver implements ViewListener {
        private static final String intentAction = "com.parse.push.intent.RECEIVE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null) {
                Log.d(TAG, "Reply Broadcast Receiver intent null");
            }else {
                processPush(context, intent);
            }
        }

        private void processPush(Context context, Intent intent) {
            String senderId= "", receiverId = "", messageBody="";
            long createdAt = 0L, updatedAt = 0L;
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
                        if(key.equals("senderId")) {
                            senderId = json.getString(key);
                            Log.d(TAG, "senderId: " + senderId);
                        }else if(key.equals("targetId")){
                            receiverId = json.getString(key);
                            Log.d(TAG, "receiverId: " + receiverId);
                        }else if(key.equals("message")){
                            messageBody = json.getString(key);
                            Log.d(TAG, "message: " + messageBody);
                        }else if(key.equals("createdAt")){
                            createdAt = json.getLong(key);
                            Log.d(TAG, "createdAt: " + createdAt);
                        }else if(key.equals("updatedAt")) {
                            updatedAt = json.getLong(key);
                            Log.d(TAG, "updateAt: " + updatedAt);
                        }
                    }
                    if(senderId.length() > 0 && receiverId.length() > 0 && messageBody.length() > 0 ){
                        showNotification(context, senderId, receiverId, messageBody, createdAt, updatedAt);
                    }
                }catch (JSONException ex){
                    ex.printStackTrace();
                    Log.d(TAG, ex.getMessage());
                }
            }
        }

        private void showNotification(Context context, String senderId, String receiverId, String message,  long createdAt, long updatedAt){

            ContentValues values = new ContentValues();
            values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY, senderId);
            values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY, receiverId);
            values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_BODY, message);
            values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_TYPE, "text");
            values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_CREATED_DATE, createdAt);
            values.put(YourTurnContract.MessageEntry.COLUMN_MESSAGE_UPDATED_DATE, updatedAt);

            Message parseMessage = new Message();
            parseMessage.setBody(message);
            parseMessage.setSenderId(senderId);
            parseMessage.setReceiverKey(receiverId);

            context.getContentResolver().insert(YourTurnContract.MessageEntry.CONTENT_URI, values);

            updateView(context, senderId, receiverId);
        }

        @Override
        public void updateView(Context context, String sender, String receiver) {
            // fetch last message
            Cursor messageCursor = context.getContentResolver().query(YourTurnContract.MessageEntry.CONTENT_URI,
                    null, YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY + "=?" + " AND " + YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY + "=?" +
            " OR " + YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY + "=?" + " AND " + YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY + "=?",
                    new String[]{sender, receiver, receiver, sender}, YourTurnContract.MessageEntry.COLUMN_MESSAGE_CREATED_DATE + " DESC LIMIT 1");

            if(messageCursor != null && messageCursor.getCount() > 0){
                messageCursor.moveToFirst();

                String senderId = messageCursor.getString(messageCursor.getColumnIndex(YourTurnContract.MessageEntry.COLUMN_MESSAGE_SENDER_KEY));
                String receiverId = messageCursor.getString(messageCursor.getColumnIndex(YourTurnContract.MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY));
                String body = messageCursor.getString(messageCursor.getColumnIndex(YourTurnContract.MessageEntry.COLUMN_MESSAGE_BODY));

                Message message = new Message();
                message.setBody(body);
                message.setSenderId(senderId);
                message.setReceiverKey(receiverId);

            }

            if(messageCursor != null ) messageCursor.close();
        }
    }

    private class SenderHandshakeReceiver extends BroadcastReceiver implements ActionBarSubtitleListener {
        private static final String intentAction = "com.parse.push.intent.RECEIVE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null) {
                Log.d(TAG, "Reply Broadcast Receiver intent null");
            }else {
                processPush(intent);
            }
        }

        private void processPush(Intent intent) {
            boolean connectedStatus = false;
            String targetId = "";
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
                        if(key.equals("connectedStatus")) {
                            connectedStatus = json.getBoolean(key);
                            Log.d(TAG, "connected Status: " + connectedStatus);
                        }else if(key.equals("targetId")){
                            targetId = json.getString(key);
                            Log.d(TAG, "targetId: " + targetId);
                        }
                    }
                    updateActionBar(connectedStatus);
                }catch (JSONException ex){
                    ex.printStackTrace();
                    Log.d(TAG, ex.getMessage());
                }
            }
        }

        @Override
        public void updateActionBar(boolean isConnected) {
            if(isConnected) {
                getSupportActionBar().setSubtitle(contact.getPhoneNumber());
                setFriendOnline(true);

               if(online){
                   HashMap<String, Object> payload = new HashMap<>();
                   payload.put("connectedStatus", online);
                   payload.put("targetId", contact.getPhoneNumber());

                   ParseCloud.callFunctionInBackground("handShakeChannel", payload, (object, e) -> {
                       if(e == null) {
                           Log.d(TAG, "connection reply !");
                       }else {
                           Log.d(TAG, e.getMessage());
                       }
                   });
               }
            }else {
                getSupportActionBar().setSubtitle("");
                setFriendOnline(false);
            }
        }
    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(mEditText.getText().toString().length() > 0){
                String data = mEditText.getText().toString();
                DateTime dateTime = new DateTime();
                Message message = new Message();
                message.setBody(data.trim());
                message.setSenderId(getUsername());
                message.setReceiverKey(contact.getPhoneNumber());
                message.setCreatedDateKey(dateTime.getMillis());
                message.setUpdatedDateKey(dateTime.getMillis());

                saveMessageInDb(message);
                mEditText.setText("");
                emptyTextView.setVisibility(View.GONE);

                saveMessageAsync(message).continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        if(task.isFaulted()){
                            Toast.makeText(ChatActivity.this, "Failed to save message !", Toast.LENGTH_LONG).show();
                        }else if(task.isCompleted()){
                            Log.d(TAG, "Message successfully saved !");

                            if(contact != null && !message.getSenderKey().equals(message.getReceiverKey())){

                                HashMap<String, Object> payload = new HashMap<>();
                                payload.put("senderId", message.getSenderKey());
                                payload.put("targetId", message.getReceiverKey());
                                payload.put("message", message.getBody());
                                payload.put("createdAt", message.getCreatedDateKey());
                                payload.put("updatedAt", message.getUpdatedDateKey());
                                if(isFriendOnline()){
                                    ParseCloud.callFunctionInBackground("messageChannel", payload, (object, e) -> {
                                        if(e == null) {
                                            Log.d(TAG, "Message successfully sent !");
                                        }else {
                                            Log.d(TAG, e.getMessage());
                                        }
                                    });
                                }else {
                                    payload.put("status", isFriendOnline());
                                    ParseCloud.callFunctionInBackground("offlineChannel", payload, (object, e) -> {
                                        if(e == null) {
                                            Log.d(TAG, "Message successfully sent !");
                                        }else {
                                            Log.d(TAG, e.getMessage());
                                        }
                                    });
                                }
                            }

                        }
                        return null;
                    }
                });

            }else {
                Toast.makeText(ChatActivity.this, "Can't send empty text", Toast.LENGTH_LONG).show();
            }
        }
    }
}
