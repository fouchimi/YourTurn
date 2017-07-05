package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseCloud;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.yourturn.broadcast.NameBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;

public class EditProfileNameActivity extends AppCompatActivity {

    private final static String TAG = EditProfileNameActivity.class.getSimpleName();
    private EditText editNameText;
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

        if(getProfileName().length() > 0) editNameText.setText(WordUtils.capitalize(getProfileName().toLowerCase(), null));

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

    private String getProfileName(){
        SharedPreferences shared = getSharedPreferences(getString(R.string.profile_name), MODE_PRIVATE);
        return (shared.getString(ParseConstant.COLUMN_NAME, ""));
    }

    private String getFriendIds(){
        String friendIds = "";
        Cursor registeredCursor = getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI,
                new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_REGISTERED + "=?", new String[]{"1"}, null);
        if(registeredCursor != null && registeredCursor.getCount() > 0) {
            while (registeredCursor.moveToNext()){
                String number = registeredCursor.getString(registeredCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER));
                if(!number.equals(getUsername())) friendIds += number + ",";
            }
        }

        registeredCursor.close();

        return (friendIds.length() > 0) ? friendIds.substring(0, friendIds.length()-1) : "";
    }

    public void confirmChange(View view){
        String name = editNameText.getText().toString();
        if(name.length() > 0) {

            saveName(name);
            saveNameInBackground(name);

            if(getFriendIds().length() > 0) {
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("senderId", getUsername());
                payload.put("targetIds", getFriendIds());
                payload.put("name", name.toUpperCase());
                ParseCloud.callFunctionInBackground("nameChannel", payload, (object, e) -> {
                    if(e == null) {
                        Log.d(TAG, "name successfully updated");
                    }
                });
            }

        }
    }

    public void cancelChange(View view){
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String getUsername() {
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }

    private String getPassword(){
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.PASSWORD_COLUMN, ""));
    }

    private void saveName(String name){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.profile_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ParseConstant.COLUMN_NAME, name);
        editor.apply();
    }

    private void saveNameInBackground(String name){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(ParseConstant.USERNAME_COLUMN, getUsername());
        query.getFirstInBackground((currentUser, e) -> {
            if(e == null) {
                Log.d(TAG, "Found User");
                if(!currentUser.isAuthenticated()){
                    ParseUser.logInInBackground(getUsername(), getPassword(), (user, e12) -> {
                        if(e12 == null){
                            Log.d(TAG, "name saved successfully !");
                            user.put(ParseConstant.COLUMN_NAME, name);
                            Toast.makeText(EditProfileNameActivity.this, "Name saved successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(EditProfileNameActivity.this, ProfileActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else {
                            Log.d(TAG, e12.getMessage());
                        }
                    });
                }else {
                    currentUser.saveInBackground(e1 -> {
                        if(e1 == null) {
                            Log.d(TAG, "name saved successfully !");
                            Toast.makeText(EditProfileNameActivity.this, "Name saved successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(EditProfileNameActivity.this, ProfileActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else {
                            Log.d(TAG, e1.getMessage());
                        }
                    });
                }
            }else {
                Log.d(TAG, "No results found !");
                Log.d(TAG, e.getMessage());
            }
        });
    }
}
