package com.social.yourturn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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

import com.parse.ParseException;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SubscriptionHandling;
import com.social.yourturn.adapters.ChatAdapter;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Message;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private EditText mEditText;
    RecyclerView rvChat;
    ArrayList<Message> mMessages;
    ChatAdapter mAdapter;
    ImageButton mButton;
    boolean mFirstLoad;
    private TextView emptyTextView;
    private Contact contact = null;
    private static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        updateLoginStatus(getUsername());

        mEditText = (EditText) findViewById(R.id.etMessage);
        mButton = (ImageButton) findViewById(R.id.btSend);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        emptyTextView = (TextView) findViewById(R.id.empty_view);
        emptyTextView.setTypeface(typeface);

        rvChat = (RecyclerView) findViewById(R.id.rvChat);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvChat.setLayoutManager(linearLayoutManager);
        rvChat.setHasFixedSize(true);

        mMessages = new ArrayList<>();
        mFirstLoad = true;

        Intent intent = getIntent();

        if(intent != null) {
            contact = intent.getParcelableExtra(getString(R.string.selected_contact));
            mAdapter = new ChatAdapter(this, contact.getPhoneNumber(), mMessages, contact);
            rvChat.setAdapter(mAdapter);

            if(getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle(WordUtils.capitalize(contact.getName().toLowerCase(), null));

                isFriendOnline(contact, getSupportActionBar());
            }

            if(mMessages.size() > 0) {
                emptyTextView.setVisibility(View.GONE);
            }
        }

        mButton.setOnClickListener(v -> {
            if(mEditText.getText().toString().length() > 0){
                String data = mEditText.getText().toString();
                Message message = new Message();
                message.setBody(data.trim());
                message.setSenderId(getUsername());
                message.setReceiverKey(contact.getPhoneNumber());

                mMessages.add(message);
                emptyTextView.setVisibility(View.GONE);
                mAdapter.notifyItemInserted(mMessages.size()-1);
                mEditText.setText("");

                saveMessageAsync(message).continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        if(task.isFaulted()){
                            Toast.makeText(ChatActivity.this, "Failed to save message !", Toast.LENGTH_LONG).show();
                        }else {
                            Log.d(TAG, "Message successfully sent !");
                            ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();

                            ParseQuery<Message> parseQuery =  ParseQuery.getQuery(Message.class);
                            parseQuery.whereEqualTo(ParseConstant.SENDER_ID, contact.getPhoneNumber());


                            // Connect to Parse server
                            SubscriptionHandling<Message> firstSubscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);
                            //SubscriptionHandling<Message> secondSubscriptionHandling = parseLiveQueryClient.subscribe(secondQuery);

                            // Listen for CREATE events
                            firstSubscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (query, newMessage) -> {
                                mMessages.add(newMessage);

                                // RecyclerView updates need to be run on the UI thread
                                runOnUiThread(() -> {
                                    refreshMessages();
                                    if(newMessage != null) Toast.makeText(ChatActivity.this, newMessage.getBody(), Toast.LENGTH_LONG).show();
                                });
                            });
                        }
                        return null;
                    }
                });

            }else {
                Toast.makeText(ChatActivity.this, "Can't send empty text", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMessages();
    }

    private Task<List<Message>> fetchMessagesAsync(ParseQuery<Message> query){
        final TaskCompletionSource<List<Message>> tcs = new TaskCompletionSource<>();

        query.findInBackground((messages, e) -> {
            if(e == null){
                tcs.setResult(messages);
            }else {
                tcs.setError(e);
            }
        });

        return tcs.getTask();
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

    private void isFriendOnline(Contact contact, ActionBar actionBar){
        ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstant.ONLINE_TABLE);
        query.whereEqualTo(ParseConstant.USERNAME_COLUMN, contact.getPhoneNumber());

        try {
            if(query.count() > 0){
                query.getFirstInBackground((row, e) -> {
                    if(e == null){
                        Boolean value = row.getBoolean(ParseConstant.STATUS);
                        if(value) actionBar.setSubtitle("Online");
                    }else {
                        Log.d(TAG, e.getMessage());
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                ParseObject onlineTable = new ParseObject(ParseConstant.ONLINE_TABLE);
                onlineTable.put(ParseConstant.USERNAME_COLUMN, contact.getPhoneNumber());
                onlineTable.put(ParseConstant.STATUS, false);
                onlineTable.saveInBackground(e -> {
                    if(e == null){
                        Log.d(TAG, "Online table updated successfully");
                    }else {
                        Log.d(TAG, "An error occur");
                    }
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return;
    }

    private void refreshMessages(){

        ParseQuery<Message> senderQuery = ParseQuery.getQuery(Message.class);
        senderQuery.whereEqualTo(ParseConstant.SENDER_ID, getUsername());
        senderQuery.whereEqualTo(ParseConstant.RECEIVER_ID, contact.getPhoneNumber());

        ParseQuery<Message> friendQuery = ParseQuery.getQuery(Message.class);
        friendQuery.whereEqualTo(ParseConstant.SENDER_ID, contact.getPhoneNumber());
        friendQuery.whereEqualTo(ParseConstant.RECEIVER_ID, getUsername());

        List<ParseQuery<Message>> queries = new ArrayList<>();
        queries.add(senderQuery);
        queries.add(friendQuery);

        ParseQuery<Message> mainQuery = ParseQuery.or(queries);
        mainQuery.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
        mainQuery.orderByAscending(ParseConstant.CREATED_AT);

        fetchMessagesAsync(mainQuery).onSuccessTask(new Continuation<List<Message>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<Message>> task) throws Exception {

                if(task.isFaulted()){
                    Log.d(TAG, "An error occured !");
                }else if(task.isCompleted()){
                    mMessages.clear();
                    if(task.getResult().size() > 0) emptyTextView.setVisibility(View.GONE);
                    mMessages.addAll(task.getResult());
                    mAdapter.notifyDataSetChanged();
                    rvChat.scrollToPosition(mMessages.size()-1);
                }
                return null;
            }
        });
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
                        Log.d(TAG, "An error occured !");
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
                        Log.d(TAG, "An error occured !");
                    }
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
