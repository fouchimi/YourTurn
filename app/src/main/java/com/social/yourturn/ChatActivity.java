package com.social.yourturn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseObject;
import com.social.yourturn.adapters.ChatAdapter;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Message;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private EditText mEditText;
    RecyclerView rvChat;
    ArrayList<Message> mMessages;
    ChatAdapter mAdapter;
    // Keep track of initial load to scroll to the bottom of the ListView
    boolean mFirstLoad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());


        mEditText = (EditText) findViewById(R.id.etMessage);
        Button mButton = (Button) findViewById(R.id.btSend);

        rvChat = (RecyclerView) findViewById(R.id.rvChat);
        mMessages = new ArrayList<>();
        mFirstLoad = true;

        Intent intent = getIntent();

        if(intent != null) {
            Contact contact = intent.getParcelableExtra(getString(R.string.selected_contact));
            mAdapter = new ChatAdapter(this, contact.getPhoneNumber(), mMessages, contact);
            rvChat.setAdapter(mAdapter);

            if(getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle(WordUtils.capitalize(contact.getName()));
            }
        }

        mButton.setOnClickListener(v -> {
            if(mEditText.getText() != null){
                String data = mEditText.getText().toString();
                Message message = new Message();
                message.setBody(data);
                message.setUserId(getUsername());

                mEditText.setText(null);

                saveMessageAsync(message).continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        if(task.isFaulted()){
                            Toast.makeText(ChatActivity.this, "Failed to save message !", Toast.LENGTH_LONG).show();
                        }else {
                            Log.d(TAG, "Message successfully sent !");
                        }
                        return null;
                    }
                });
            }else {
                Toast.makeText(ChatActivity.this, "Can't send empty text", Toast.LENGTH_LONG).show();
            }
        });

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
}
