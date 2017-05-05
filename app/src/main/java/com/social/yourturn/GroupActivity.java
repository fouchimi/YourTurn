package com.social.yourturn;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.adapters.CustomAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.CircularImageView;
import com.social.yourturn.utils.ImagePicker;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GroupActivity extends AppCompatActivity {

    private final static String TAG = GroupActivity.class.getSimpleName();
    private static final int PICK_IMAGE_ID = 12;
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
    private CircularImageView mGroupImageView;
    private File mGroupDirectory = null, thumbnailFile = null;
    private ArrayList<Contact> mContactList;
    private String groupThumbnailPath ="";
    private Vector<ContentValues> cVVectorUsers = new Vector<>();
    private Bitmap mBitmap = null;

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
        mGroupImageView = (CircularImageView) findViewById(R.id.groupImageView);
        mRecyclerView = (RecyclerView) findViewById(R.id.selected_rv);


        mRecyclerView.setLayoutManager(new GridLayoutManager(this, NUMCOLUMS));

        mCurrentUser = ParseUser.getCurrentUser();
        Log.d(TAG, "Username: " + mCurrentUser.getUsername());

        Intent intent = getIntent();
        if(intent != null) {
            mContactList = intent.getParcelableArrayListExtra(ContactActivity.SELECTED_CONTACT);
            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();
            for(Contact contact : mContactList) {
                ContentValues userValues = new ContentValues();
                Cursor c = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                        YourTurnContract.UserEntry.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(contact.getId()), null, null);
                if(c.getCount() == 0) {
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, contact.getPhoneNumber());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, contact.getId());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, contact.getName());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_DATE, dayTime.setJulianDay(julianStartDay));
                    cVVectorUsers.add(userValues);
                }
                friendList +=  contact.getId()+ " " + contact.getName() + " " + contact.getPhoneNumber() + ",";
            }
            friendList = friendList.substring(0, friendList.length()-1);
            Log.d(TAG, friendList);
            Bundle bundle = intent.getExtras();
            selectedCount = mContactList.size();
            totalContact = bundle.getInt(ContactActivity.TOTAL_COUNT);
            mParticipantView.setText(mParticipantView.getText().toString() + " " +  selectedCount + "/" + totalContact);
            mAdapter = new CustomAdapter(this, mContactList);
            mRecyclerView.setAdapter(mAdapter);
        }


        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String groupName = WordUtils.capitalize(mGroupTextView.getText().toString(), null);
                Pattern p = Pattern.compile("^[a-zA-Z0-9_\\s]+$");
                Matcher m = p.matcher(groupName);
                if(m.find() && groupName.length() > 0){
                    if(isDuplicateGroupName(groupName)){
                        Log.d(TAG, "Duplicate group name");
                        Toast.makeText(GroupActivity.this, "Duplicate group name", Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        ParseObject groupTable = new ParseObject(ParseConstant.GROUP_TABLE);
                        groupTable.put(ParseConstant.GROUP_NAME, groupName);
                        groupTable.put(ParseConstant.THUMBNAIL_COLUMN, groupThumbnailPath);
                        groupTable.put(ParseConstant.CREATOR_COLUMN, mCurrentUser.getUsername());
                        groupTable.put(ParseConstant.MEMBERS_COLUMN, friendList);
                        if(cVVectorUsers.size() > 0) {
                            ContentValues[] cvArray = new ContentValues[cVVectorUsers.size()];
                            cVVectorUsers.toArray(cvArray);
                            Log.d(TAG, "Users Bulk insert successful");
                            GroupActivity.this.getContentResolver().bulkInsert(YourTurnContract.UserEntry.CONTENT_URI, cvArray);
                        }
                        groupTable.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null){
                                    Log.d(TAG, "Group Table Created !");
                                    Intent intent = new Intent(GroupActivity.this, MainActivity.class);
                                    Time dayTime = new Time();
                                    dayTime.setToNow();

                                    int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                                    dayTime = new Time();
                                    Vector<ContentValues> cVVector = new Vector<>();
                                    int i=0;
                                    for(Contact contact : mContactList){
                                        ContentValues groupValues = new ContentValues();
                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_NAME, groupName);
                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_USER_KEY, contact.getId());
                                        if(groupThumbnailPath != null)
                                            groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_THUMBNAIL, groupThumbnailPath);
                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_DATE, dayTime.setJulianDay(julianStartDay + i));
                                        i++;
                                        cVVector.add(groupValues);
                                    }
                                    if ( cVVector.size() > 0 ) {
                                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                                        cVVector.toArray(cvArray);
                                        GroupActivity.this.getContentResolver().bulkInsert(YourTurnContract.GroupEntry.CONTENT_URI, cvArray);
                                        Log.d(TAG, "Group Bulk insert successful");
                                    }
                                    Toast.makeText(GroupActivity.this, "Your new group have been created !", Toast.LENGTH_LONG).show();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    GroupActivity.this.startActivity(intent);
                                    if(thumbnailFile != null){
                                        saveThumbnailFile(thumbnailFile, mBitmap);
                                    }
                                    mGroupTextView.setText("");
                                }else {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                }else {
                    Toast.makeText(GroupActivity.this, R.string.required_group_name, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void snapGroupPhoto(View view){
        final String groupName = WordUtils.capitalize(mGroupTextView.getText().toString(), null);
        if(groupName.length() <=0 ){
            Log.d(TAG, "Enter group name first");
            Toast.makeText(this, "Please type group name first", Toast.LENGTH_LONG).show();
        }else {
            if(isGroupCreated(groupName)){
                Log.d(TAG, "Choose different group name");
                Toast.makeText(this, "Duplicate group name", Toast.LENGTH_LONG).show();
                if(mGroupDirectory.isDirectory()){
                    if(mGroupDirectory.list().length == 0){
                        mGroupDirectory.delete();
                        mGroupDirectory = null;
                    }
                }
            }else {
                createGroup();
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        }
        return;
    }

    private boolean isGroupCreated(String groupName){
        mGroupDirectory = new File(Environment.getExternalStorageDirectory(), ParseConstant.YOUR_TURN_FOLDER +  "/" + groupName);
        if(mGroupDirectory.exists()) return true;
        return false;
    }

    private void createGroup(){
        if(mGroupDirectory != null){
            mGroupDirectory.mkdirs();
        }
    }

    private boolean isDuplicateGroupName(String groupName){
        Cursor c = getContentResolver().query(YourTurnContract.GroupEntry.CONTENT_URI, null,
                YourTurnContract.GroupEntry.COLUMN_GROUP_NAME + " = " + DatabaseUtils.sqlEscapeString(groupName), null, null);
        if(c.getCount() > 0) return true;
        else return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                mBitmap = ImagePicker.getImageFromResult(this, resultCode, data, mGroupImageView);
                mGroupImageView.setImageBitmap(mBitmap);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMAGE_" + timeStamp + "_";

                groupThumbnailPath = imageFileName + timeStamp + ".jpg";
                thumbnailFile = new File(mGroupDirectory, groupThumbnailPath);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void saveThumbnailFile(File file, Bitmap bitmap){
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
