package com.social.yourturn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.social.yourturn.adapters.CustomAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Place;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;


public class EventActivity extends AppCompatActivity {

    private final static String TAG = EventActivity.class.getSimpleName();
    private static final int NUM_COLUMNS = 5;
    private static final int REQUEST_SEND_SMS_PERMISSION =0 ;
    private ArrayList<Contact> mContactList = null;
    private TextView mGroupTextView;
    private String SEND_SMS_PERMISSION = android.Manifest.permission.SEND_SMS;
    public static final String EVENT_NAME = "event_name";
    private Place mPlace = null;
    private String locationUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.fab);
        fb.setOnClickListener(new SaveDataOnClickListener());
        mGroupTextView = (TextView) findViewById(R.id.groupNameText);
        TextView mParticipantView = (TextView) findViewById(R.id.participantsTextView);
        CircleImageView mEventImageView = (CircleImageView) findViewById(R.id.groupImageView);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.selected_rv);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EventActivity.this, new String[]{SEND_SMS_PERMISSION}, REQUEST_SEND_SMS_PERMISSION);
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));
        Log.d(TAG, "Current User: " + getUsername());

        Intent intent = getIntent();
        if(intent != null) {
            mContactList = intent.getParcelableArrayListExtra(ContactActivity.SELECTED_CONTACT);
            Bundle bundle = intent.getExtras();
            locationUrl = bundle.getString(LocationActivity.PLACE_URL);
            mPlace = bundle.getParcelable(LocationActivity.CURRENT_PLACE);
            Glide.with(this).load(locationUrl).into(mEventImageView);
            int selectedCount = mContactList.size();
            int totalContact = bundle.getInt(ContactActivity.TOTAL_COUNT);
            mParticipantView.setText(mParticipantView.getText().toString() + " " +  selectedCount + "/" + totalContact);
            CustomAdapter mAdapter = new CustomAdapter(this, mContactList);
            mRecyclerView.setAdapter(mAdapter);
        }

    }

    private class SaveDataOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS_PERMISSION) == PackageManager.PERMISSION_GRANTED){
                String eventName = WordUtils.capitalize(mGroupTextView.getText().toString(), null);
                if(eventName.length() > 0){
                    Cursor c = getContentResolver().query(YourTurnContract.EventEntry.CONTENT_URI,
                            new String[]{YourTurnContract.EventEntry.COLUMN_EVENT_NAME},
                            YourTurnContract.EventEntry.COLUMN_EVENT_NAME + "=?",
                            new String[]{eventName},
                            null);

                    if(c != null && c.getCount() > 0){
                        Toast.makeText(EventActivity.this, R.string.duplicate_group_name_err, Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Intent intent = new Intent(EventActivity.this, GroupListActivity.class);
                        intent.putExtra(ContactActivity.SELECTED_CONTACT, mContactList);
                        intent.putExtra(EventActivity.EVENT_NAME, eventName);
                        intent.putExtra(LocationActivity.CURRENT_PLACE, mPlace);
                        intent.putExtra(LocationActivity.PLACE_URL, locationUrl);
                        startActivity(intent);
                    }
                    if(c != null ) c.close();
                }else {
                    Toast.makeText(EventActivity.this, "Event name can't be empty", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(EventActivity.this, "Accept this permission to invite unregistered members", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(EventActivity.this, new String[]{SEND_SMS_PERMISSION}, REQUEST_SEND_SMS_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_SEND_SMS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission granted !", Toast.LENGTH_LONG).show();
                }
        }
    }

    private String getUsername() {
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }

}
