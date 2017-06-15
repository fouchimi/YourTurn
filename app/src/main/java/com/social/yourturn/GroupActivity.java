package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.adapters.CustomAdapter;
import com.social.yourturn.broadcast.GroupBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ImagePicker;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class GroupActivity extends AppCompatActivity {

    private final static String TAG = GroupActivity.class.getSimpleName();
    private static final int NUM_COLUMNS = 5, PICK_IMAGE_ID = 12;
    private static final int REQUEST_SEND_SMS_PERMISSION =0 ;
    private FloatingActionButton fb;
    private TextView mGroupTextView;
    private ParseUser mCurrentUser;
    private String friendList = "";
    private CircleImageView mGroupImageView;
    private File mGroupDirectory = null, thumbnailFile = null;
    private ArrayList<Contact> mContactList;
    private Vector<ContentValues> cVVectorUsers = new Vector<>();
    private Bitmap mBitmap = null;
    private Task mTask = null;
    private String groupName;
    private final Object mTaskLock = new Object();
    private BroadcastReceiver mBroadcastReceiver;
    private String SEND_SMS_PERMISSION = android.Manifest.permission.SEND_SMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fb = (FloatingActionButton) findViewById(R.id.fab);
        fb.setOnClickListener(new SaveDataOnClickListener());
        mGroupTextView = (TextView) findViewById(R.id.groupNameText);
        TextView mParticipantView = (TextView) findViewById(R.id.participantsTextView);
        mGroupImageView = (CircleImageView) findViewById(R.id.groupImageView);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.selected_rv);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GroupActivity.this, new String[]{SEND_SMS_PERMISSION}, REQUEST_SEND_SMS_PERMISSION);
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS));

        mCurrentUser = ParseUser.getCurrentUser();

        Intent intent = getIntent();
        if(intent != null) {
            mContactList = intent.getParcelableArrayListExtra(ContactActivity.SELECTED_CONTACT);
            DateTime dayTime = new DateTime();
            for(Contact contact : mContactList) {
                ContentValues userValues = new ContentValues();
                Cursor c = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                        YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(contact.getPhoneNumber()), null, null);

                if(c != null && c.getCount() == 0) {
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, contact.getPhoneNumber());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, contact.getId());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, contact.getName());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
                    cVVectorUsers.add(userValues);
                }

                if(c != null) c.close();

                StringBuilder builder = new StringBuilder();
                builder.append(contact.getId());
                builder.append(":");
                builder.append(contact.getName());
                builder.append(":");
                builder.append(contact.getPhoneNumber());
                builder.append(",");
                friendList +=  builder.toString();
            }
            friendList = friendList.substring(0, friendList.length()-1);
            Log.d(TAG, friendList);
            Bundle bundle = intent.getExtras();
            int selectedCount = mContactList.size();
            int totalContact = bundle.getInt(ContactActivity.TOTAL_COUNT);
            mParticipantView.setText(mParticipantView.getText().toString() + " " +  selectedCount + "/" + totalContact);
            CustomAdapter mAdapter = new CustomAdapter(this, mContactList);
            mRecyclerView.setAdapter(mAdapter);
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "On received invoked");
                Toast.makeText(getApplicationContext(), "On Received invoked !", Toast.LENGTH_SHORT).show();
            }
        };

    }

    private class SaveDataOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS_PERMISSION) == PackageManager.PERMISSION_GRANTED){
                groupName = WordUtils.capitalize(mGroupTextView.getText().toString(), null);
                if(groupName.length() > 0){
                    Cursor c = getContentResolver().query(YourTurnContract.GroupEntry.CONTENT_URI, null,
                            YourTurnContract.GroupEntry.COLUMN_GROUP_NAME + " = " + DatabaseUtils.sqlEscapeString(groupName), null, null);

                    if(c != null && c.getCount() > 0){
                        Toast.makeText(GroupActivity.this, R.string.duplicate_group_name_err, Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        if(!isGroupCreated(groupName)) createGroup();
                        synchronized (mTaskLock){
                            mTask = new Task(GroupActivity.this);
                            mTask.execute();
                            mTaskLock.notifyAll();
                        }

                    }
                    if(c != null ) c.close();
                }else {
                    Toast.makeText(GroupActivity.this, "Group name can't be empty", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(GroupActivity.this, "Accept this permission to invite unregistered members", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(GroupActivity.this, new String[]{SEND_SMS_PERMISSION}, REQUEST_SEND_SMS_PERMISSION);
            }
        }
    }

    private void dumpGroupValuesInContentProvider(final String groupId, final String groupName, final String groupThumbnail){
        if(groupName.length() > 0){
            DateTime dayTime = new DateTime();

            Vector<ContentValues> cVVector = new Vector<>();
            for(Contact contact : mContactList){
                ContentValues groupValues = new ContentValues();
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_ID, groupId);
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_NAME, groupName);

                groupValues.put(YourTurnContract.GroupEntry.COLUMN_USER_KEY, contact.getPhoneNumber());
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATOR, mCurrentUser.getUsername());
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_THUMBNAIL, groupThumbnail);
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());
                cVVector.add(groupValues);
            }
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                GroupActivity.this.getContentResolver().bulkInsert(YourTurnContract.GroupEntry.CONTENT_URI, cvArray);
                Log.d(TAG, "Group Bulk insert successful");
            }
        }else {
            Log.d(TAG, "Group Name cannot be left empty !!!");
        }
    }

    public void snapGroupPhoto(View view){
        if(mBitmap != null) {
            mGroupImageView.setImageResource(R.drawable.ic_group_black_36dp);
        }
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this, getString(R.string.pick_image_intent_text));
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    private boolean isGroupCreated(String groupName){
        mGroupDirectory = new File(Environment.getExternalStorageDirectory(), ParseConstant.YOUR_TURN_FOLDER +  "/" + groupName);
        if(!mGroupDirectory.exists()){
            return false;
        }
        return true;
    }

    private void createGroup(){
        if(mGroupDirectory != null){
            mGroupDirectory.mkdirs();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                mBitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                if(mBitmap == null) {
                    mGroupImageView.setImageResource(R.drawable.ic_group_black_36dp);
                }else {
                    mGroupImageView.setImageBitmap(mBitmap);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(GroupBroadcastReceiver.intentAction));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mTask != null) {
            if(mTask.getStatus().equals(AsyncTask.Status.RUNNING)){
                mTask.cancel(true);
            }
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private class Task extends AsyncTask<Void, Void, Void>{

        private Context mContext;

        public Task(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Task has started !");
        }

        @Override
        protected Void doInBackground(Void... params) {
            ParseFile pFile = null;
            if(mBitmap != null) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMAGE_" + timeStamp + "_";

                String groupThumbnailPath = imageFileName + timeStamp + ".jpg";
                thumbnailFile = new File(mGroupDirectory, groupThumbnailPath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] groupImageByteData = stream.toByteArray();
                pFile = new ParseFile(ParseConstant.GROUP_THUMBNAIL_EXTENSION, groupImageByteData);
            }

            // Save user in user table
            if(cVVectorUsers.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVectorUsers.size()];
                cVVectorUsers.toArray(cvArray);
                Log.d(TAG, "Users bulk insert successful");
                mContext.getContentResolver().bulkInsert(YourTurnContract.UserEntry.CONTENT_URI, cvArray);
            }

            final ParseObject groupTable = new ParseObject(ParseConstant.GROUP_TABLE);
            groupTable.put(ParseConstant.GROUP_NAME, groupName);
            if(pFile != null) groupTable.put(ParseConstant.GROUP_THUMBNAIL_COLUMN, pFile);
            groupTable.put(ParseConstant.USER_ID_COLUMN, mCurrentUser.getUsername());
            groupTable.put(ParseConstant.MEMBERS_COLUMN, friendList);


            groupTable.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.d(TAG, "Group table created!");

                        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.GROUP_TABLE);
                        query.getInBackground(groupTable.getObjectId(), new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject row, ParseException e) {
                                if(e == null){
                                    ParseFile imageFile = (ParseFile) row.get(ParseConstant.GROUP_THUMBNAIL_COLUMN);
                                    if(imageFile != null) {
                                        dumpGroupValuesInContentProvider(groupTable.getObjectId(), groupName, imageFile.getUrl());
                                    }else {
                                        dumpGroupValuesInContentProvider(groupTable.getObjectId(), groupName, "");
                                    }
                                    Toast.makeText(mContext, R.string.new_group_creation, Toast.LENGTH_LONG).show();

                                }else {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });

                        List<ParseObject> list = new ArrayList<>();
                        Contact currentContact = new Contact();
                        currentContact.setPhoneNumber(mCurrentUser.getUsername());

                        mContactList.add(currentContact);
                        HashMap<String, Object> payload = new HashMap<>();
                        String ids = "";
                        payload.put("senderId", mCurrentUser.getUsername());
                        for(Contact contact : mContactList){
                            final ParseObject member_group_table = new ParseObject(ParseConstant.GROUP_MEMBER_TABLE);
                            member_group_table.put(ParseConstant.GROUP_MEMBER_TABLE_ID, groupTable.getObjectId());
                            member_group_table.put(ParseConstant.USER_ID_COLUMN, contact.getPhoneNumber());
                            list.add(member_group_table);

                            ParseQuery<ParseObject> parseUserQuery = ParseQuery.getQuery(ParseConstant.PARSE_USER_TABLE);
                            parseUserQuery.whereEqualTo(ParseConstant.USERNAME_COLUMN, contact.getPhoneNumber());

                            try {
                                if(parseUserQuery.count() > 0) {
                                    // sent push notification here
                                    if(!contact.getPhoneNumber().equals(mCurrentUser.getUsername())) ids += contact.getPhoneNumber() + ",";
                                }else {
                                    // sent sms to the current phone number here
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(contact.getPhoneNumber(), null,
                                            "This number " + mCurrentUser.getUsername() +
                                                    " added you into a group and would like you to install yourturnapp", null, null);
                                }
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        }

                        ParseObject.saveAllInBackground(list, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null) {
                                    Log.d(TAG, "Successfully saved in group member table");
                                }
                            }
                        });

                        if(!ids.isEmpty()){
                            payload.put("targetIds", ids.substring(0, ids.length()-1));
                            payload.put("groupName", groupName);
                            payload.put("groupId", groupTable.getObjectId());
                            ParseCloud.callFunctionInBackground("groupChannel", payload, new FunctionCallback<Object>() {
                                @Override
                                public void done(Object object, ParseException e) {
                                    if(e == null) {
                                        Log.d(TAG, "Group notification successfully sent");
                                    }else {
                                        Log.d(TAG, e.getMessage());
                                    }
                                }
                            });
                        }
                    }else {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(GroupActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            fb.hide();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(TAG, "Background job cancelled");
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
}
