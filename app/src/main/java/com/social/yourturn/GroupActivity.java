package com.social.yourturn;

import android.content.Intent;
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
import com.social.yourturn.adapters.CustomAdapter;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GroupActivity extends AppCompatActivity {

    private final static String TAG = GroupActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private final static int NUMCOLUMS = 5;
    private CustomAdapter mAdapter;
    private TextView mParticipantView ;
    private int selectedCount = 0;
    private int totalContact = 0;
    private FloatingActionButton fb;
    private TextView mGroupTextView;
    private ParseUser mCurrentUser;
    private String friendList = "";
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
        mGroupTextView = (TextView) findViewById(R.id.groupNameText);
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
                friendList +=  contact.getId()+ " " + contact.getName() + " " + contact.getPhoneNumber() + ",";
            }
            friendList = friendList.substring(0, friendList.length()-1);
            Log.d(TAG, friendList);
            Bundle bundle = intent.getExtras();
            selectedCount = list.size();
            totalContact = bundle.getInt(ContactActivity.TOTAL_COUNT);
            mParticipantView.setText(mParticipantView.getText().toString() + " " +  selectedCount + "/" + totalContact);
            mAdapter = new CustomAdapter(this, list);
            mRecyclerView.setAdapter(mAdapter);
        }

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String groupName = mGroupTextView.getText().toString();
                Pattern p = Pattern.compile("^[a-zA-Z0-9_\\s]+$");
                Matcher m = p.matcher(groupName);
                if(m.find() && groupName.length() > 0){
                    ParseObject groupTable = new ParseObject(ParseConstant.GROUP_TABLE);
                    groupTable.put(ParseConstant.GROUP_NAME, groupName);
                    groupTable.put(ParseConstant.THUMBNAIL_COLUMN, "");
                    groupTable.put(ParseConstant.CREATOR_COLUMN, mCurrentUser.getUsername());
                    groupTable.put(ParseConstant.MEMBERS_COLUMN, friendList);
                    groupTable.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Log.d(TAG, "Group Table Created !");
                                Intent intent = new Intent(GroupActivity.this, MainActivity.class);
                                Toast.makeText(GroupActivity.this, "Your new group have been created !", Toast.LENGTH_LONG).show();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                GroupActivity.this.startActivity(intent);
                                mGroupTextView.setText("");
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
