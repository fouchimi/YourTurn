package com.social.yourturn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseUser;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ParseConstant;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private String username = "";
    private String phoneNumber = "";
    private TextView usernameTextView;
    private TextView phoneNumberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.profile_text);

        usernameTextView = (TextView) findViewById(R.id.nameTextField);
        phoneNumberTextView = (TextView) findViewById(R.id.phoneNumberField);

        phoneNumber = ParseUser.getCurrentUser().getUsername();

        phoneNumberTextView.setText(phoneNumber);
        Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            usernameTextView.setText(cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME)));
        }
    }

    public void launchEditActivity(View view) {
        Intent  intent = new Intent(this, EditProfileNameActivity.class);
        intent.putExtra(ParseConstant.USER_PHONE_NUMBER_COLUMN, ParseUser.getCurrentUser().getUsername());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
