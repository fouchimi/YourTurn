package com.social.yourturn;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.adapters.CustomAdapter;
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
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class GroupActivity extends AppCompatActivity {

    private final static String TAG = GroupActivity.class.getSimpleName();
    private static final int PICK_IMAGE_ID = 12;
    private RecyclerView mRecyclerView;
    private final static int NUM_COLUMS = 5;
    private CustomAdapter mAdapter;
    private TextView mParticipantView ;
    private int selectedCount = 0;
    private int totalContact = 0;
    private FloatingActionButton fb;
    private TextView mGroupTextView;
    private ParseUser mCurrentUser;
    private String friendList = "";
    private CircleImageView mGroupImageView;
    private File mGroupDirectory = null, thumbnailFile = null;
    private ArrayList<Contact> mContactList;
    private String groupThumbnailPath ="";
    private Vector<ContentValues> cVVectorUsers = new Vector<>();
    private Bitmap mBitmap = null;
    private byte[] groupImageByteData;
    private ParseFile pFile = null;
    private Task mTask = null;
    private String groupName;
    private int saveCount = 0;

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
        mGroupImageView = (CircleImageView) findViewById(R.id.groupImageView);
        mRecyclerView = (RecyclerView) findViewById(R.id.selected_rv);


        mRecyclerView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMS));

        mCurrentUser = ParseUser.getCurrentUser();
        Log.d(TAG, "Username: " + mCurrentUser.getUsername());

        Intent intent = getIntent();
        if(intent != null) {
            mContactList = intent.getParcelableArrayListExtra(ContactActivity.SELECTED_CONTACT);
            DateTime dayTime = new DateTime();
            for(Contact contact : mContactList) {
                ContentValues userValues = new ContentValues();
                Cursor c = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                        YourTurnContract.UserEntry.COLUMN_USER_ID + " = " + DatabaseUtils.sqlEscapeString(contact.getId()), null, null);
                if(c.getCount() == 0) {
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, contact.getPhoneNumber());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, contact.getId());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, contact.getName());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
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
                groupName = WordUtils.capitalize(mGroupTextView.getText().toString(), null);
                Pattern p = Pattern.compile("^[a-zA-Z0-9_\\s]+$");
                Matcher m = p.matcher(groupName);
                if(m.find() && groupName.length() > 0){
                    if(isDuplicateGroupName(groupName)){
                        Log.d(TAG, "Duplicate group name");
                        Toast.makeText(GroupActivity.this, R.string.duplicate_group_name_error, Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        if(saveCount == 0){
                            saveCount++;
                            mTask = new Task();
                            mTask.execute(thumbnailFile);
                        }
                    }
                }else {
                    Toast.makeText(GroupActivity.this, R.string.required_group_name, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void dumpGroupValuesInContentProvider(final String groupName){
        if(groupName.length() > 0){
            DateTime dayTime = new DateTime();

            Vector<ContentValues> cVVector = new Vector<>();
            for(Contact contact : mContactList){
                ContentValues groupValues = new ContentValues();
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_NAME, groupName);
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_USER_KEY, contact.getId());
                if(groupThumbnailPath != null)
                    groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_THUMBNAIL, groupThumbnailPath);
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());
                groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATOR, contact.getId());
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
        final String groupName = WordUtils.capitalize(mGroupTextView.getText().toString(), null);
        if(groupName.length() <=0 ){
            Log.d(TAG, "Enter group name first");
            Toast.makeText(this, "Please type group name first", Toast.LENGTH_LONG).show();
        }else {
            if(!isGroupCreated(groupName)){
                createGroup();
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }else if(isGroupCreated(groupName) && mGroupDirectory.list().length == 0){
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
                Log.d(TAG, "Choose different group name");
            }else if(isGroupCreated(groupName) && mGroupDirectory.list().length > 0){
                Toast.makeText(this, R.string.duplicate_group_name_err, Toast.LENGTH_LONG).show();
            }
        }
        return;
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            mBitmap = Compressor.getDefault(this).compressToBitmap(thumbnailFile);
            thumbnailFile = Compressor.getDefault(this).compressToFile(thumbnailFile);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mTask != null) {
            if(mTask.getStatus().equals(AsyncTask.Status.RUNNING)){
                mTask.cancel(true);
            }
        }
    }

    private class Task extends AsyncTask<File, Void, ParseFile>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Task has started !");
        }

        @Override
        protected ParseFile doInBackground(File... params) {
            if(isCancelled()){
                return null;
            }else {
                File thumbnailFile = params[0];
                if(thumbnailFile != null) {
                    saveThumbnailFile(thumbnailFile, mBitmap);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    groupImageByteData = stream.toByteArray();
                    pFile = new ParseFile(ParseConstant.GROUP_THUMBNAIL_EXTENSION, groupImageByteData);
                }
            }
            return pFile;
        }

        @Override
        protected void onPostExecute(ParseFile pFile) {
            super.onPostExecute(pFile);
            final ParseObject groupTable = new ParseObject(ParseConstant.GROUP_TABLE);
            groupTable.put(ParseConstant.GROUP_NAME, groupName);
            if(pFile != null) {
                groupTable.put(ParseConstant.THUMBNAIL_COLUMN, pFile);
            }
            groupTable.put(ParseConstant.USER_ID, mCurrentUser.getUsername());
            groupTable.put(ParseConstant.MEMBERS_COLUMN, friendList);

            if(cVVectorUsers.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVectorUsers.size()];
                cVVectorUsers.toArray(cvArray);
                Log.d(TAG, "Users bulk insert successful");
                GroupActivity.this.getContentResolver().bulkInsert(YourTurnContract.UserEntry.CONTENT_URI, cvArray);
            }

            groupTable.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.d(TAG, "Group table created!");
                        dumpGroupValuesInContentProvider(groupName);
                        Toast.makeText(GroupActivity.this, R.string.new_group_creation, Toast.LENGTH_LONG).show();
                        List<ParseObject> list = new ArrayList<>();
                        for(Contact contact : mContactList){
                            final ParseObject member_group_table = new ParseObject(ParseConstant.GROUP_MEMBER_TABLE);
                            member_group_table.put(ParseConstant.GROUP_MEMBER_TABLE_ID, groupTable.getObjectId());
                            member_group_table.put(ParseConstant.USER_ID, contact.getPhoneNumber());
                            list.add(member_group_table);
                        }

                        ParseObject.saveAllInBackground(list, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null) {
                                    Intent intent = new Intent(GroupActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    GroupActivity.this.startActivity(intent);
                                    fb.hide();
                                    mGroupTextView.setText("");
                                }
                            }
                        });
                    }else {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(TAG, "Background job cancelled");
        }
    }

}
