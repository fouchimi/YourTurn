package com.social.yourturn;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.adapters.GroupAdapter;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GroupActivity extends AppCompatActivity {

    private final static String TAG = GroupActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private final static int NUMCOLUMS = 5;
    private GroupAdapter mAdapter;
    private TextView mParticipantView ;
    private int selectedCount = 0;
    private int totalContact = 0;
    private FloatingActionButton fb;
    private TextView mGroupTextView;
    private ParseUser mCurrentUser;
    private String friendIds = "";
    private ImageView groupImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fb = (FloatingActionButton) findViewById(R.id.fab);
        mGroupTextView = (TextView) findViewById(R.id.groupTextViewLabel);
        mParticipantView = (TextView) findViewById(R.id.participantsTextView);
        groupImageView = (ImageView) findViewById(R.id.groupImageView);
        mRecyclerView = (RecyclerView) findViewById(R.id.selected_rv);


        mRecyclerView.setLayoutManager(new GridLayoutManager(this, NUMCOLUMS));

        mCurrentUser = ParseUser.getCurrentUser();
        Log.d(TAG, "Username: " + mCurrentUser.getUsername());

        Intent intent = getIntent();
        if(intent != null) {
            ArrayList<Contact> list = intent.getParcelableArrayListExtra(ContactActivity.SELECTED_CONTACT);
            for(Contact contact : list) {
                friendIds += contact.getPhoneNumber() + ",";
            }
            friendIds = friendIds.substring(0, friendIds.length()-1);
            Log.d(TAG, friendIds);
            Bundle bundle = intent.getExtras();
            selectedCount = list.size();
            totalContact = bundle.getInt(ContactActivity.TOTAL_COUNT);
            mParticipantView.setText(mParticipantView.getText().toString() + " " +  selectedCount + "/" + totalContact);
            mAdapter = new GroupAdapter(this, list);
            mRecyclerView.setAdapter(mAdapter);
        }

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String groupName = mGroupTextView.getText().toString();
                Pattern p = Pattern.compile("^[a-zA-Z_ ]*$");
                Matcher m = p.matcher(groupName);
                if(groupName.length() > 0 && m.find()){
                    ParseObject groupTable = new ParseObject(ParseConstant.GROUP_TABLE);
                    groupTable.put(ParseConstant.CREATOR_COLUMN, mCurrentUser.getUsername());
                    groupTable.put(ParseConstant.MEMBERS_COLUMN, friendIds);
                    groupTable.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Log.d(TAG, "Group Table Created !");
                                //mParticipantView.setText("");
                            }else {
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    });
                    // get group thumbnail here
                }else {
                    Toast.makeText(GroupActivity.this, R.string.required_group_name, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
