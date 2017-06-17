package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.broadcast.NameBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;

public class EditProfileNameActivity extends AppCompatActivity {

    private final static String TAG = EditProfileNameActivity.class.getSimpleName();
    private EditText editNameText;
    private String phoneNumber;
    private BroadcastReceiver nameBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_name);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_name);

        editNameText = (EditText) findViewById(R.id.editNameField);
        phoneNumber = ParseUser.getCurrentUser().getUsername();
        Log.d(TAG, "Phone Number: " + phoneNumber);
        Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null);
        if(cursor.getCount() > 0){
            cursor.moveToNext();
            String name = cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME));
            if(name != null) editNameText.setText(WordUtils.capitalize(name.toLowerCase(), null));
        }

        nameBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "On received invoked");
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(nameBroadcastReceiver, new IntentFilter(NameBroadcastReceiver.intentAction));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(nameBroadcastReceiver);
    }

    private String getFriendIds(){
        String friendIds = "";
        Cursor userCursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null, null, null, null);
        if(userCursor != null && userCursor.getCount() > 0) {
            ArrayList<String> list = new ArrayList<>();
            while (userCursor.moveToNext()){
                String number = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER));
                list.add(number);
                if(!list.contains(number) && !number.equals(phoneNumber)){
                    list.add(number);
                    friendIds += number + ",";
                }
            }
        }

        if(userCursor != null) userCursor.close();

        if(friendIds.length() > 0) {
            return friendIds.substring(0, friendIds.length()-1);
        }else return null;
    }

    public void confirmChange(View view){
        final String name = editNameText.getText().toString();
        DateTime dayTime = new DateTime();
        if(name.length() > 0) {
            ContentValues userValues = new ContentValues();
            Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                    YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null);
            if(cursor.getCount() <=0 ){
                // Insert
                Log.d(TAG, "Not available in content provider");
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, 0);
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, name.toUpperCase());
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, phoneNumber);
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
                getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValues);
                Toast.makeText(this, "Name Inserted  !", Toast.LENGTH_LONG).show();
            }else {
                // Update
                Log.d(TAG, "Updating content provider");
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, name.toUpperCase());
                userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
                getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI, userValues,
                        YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(phoneNumber), null);
                Toast.makeText(this, "Name updated !", Toast.LENGTH_SHORT).show();
            }

            HashMap<String, Object> payload = new HashMap<>();
            payload.put("senderId", phoneNumber);
            payload.put("targetIds", getFriendIds());
            payload.put("name", name.toUpperCase());
            ParseCloud.callFunctionInBackground("nameChannel", payload, new FunctionCallback<Object>() {
                @Override
                public void done(Object object, ParseException e) {
                    if(e == null) {
                        Log.d(TAG, "name successfully updated");
                    }
                }
            });

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(ParseConstant.USERNAME_COLUMN, phoneNumber);
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser currentUser, ParseException e) {
                    if(e == null) {
                        Log.d(TAG, "Found User");
                        currentUser.put(ParseConstant.COLUMN_NAME, name);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null) {
                                    Log.d(TAG, "name saved successfully !");
                                    Intent intent = new Intent(EditProfileNameActivity.this, ProfileActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }else {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });
                    }else {
                        Log.d(TAG, "No results found !");
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
        }
    }

    public void cancelChange(View view){
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
