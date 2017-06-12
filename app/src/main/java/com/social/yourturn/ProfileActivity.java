package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.broadcast.UserThumbnailBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.services.ProfileDataReceiver;
import com.social.yourturn.services.ProfileDataService;
import com.social.yourturn.utils.ImagePicker;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private TextView usernameTextView;
    private CircleImageView mImageProfileView;
    private TextView phoneNumberTextView;
    private Bitmap mBitmap = null;
    private ParseFile pFile;
    private ParseUser mCurrentUser;
    private BroadcastReceiver mBroadcastReceiver;
    private ProfileDataReceiver profileReceiver;

    private static final  int PICK_IMAGE_ID = 2014;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.profile_text);
        }

        usernameTextView = (TextView) findViewById(R.id.nameTextField);
        phoneNumberTextView = (TextView) findViewById(R.id.phoneNumberField);
        mImageProfileView = (CircleImageView) findViewById(R.id.profile_picture);

        mCurrentUser = ParseUser.getCurrentUser();

        profileReceiver = new ProfileDataReceiver(new Handler());

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "On received invoked");
                Toast.makeText(getApplicationContext(), "On Received invoked !", Toast.LENGTH_SHORT).show();
            }
        };

        setUpProfileReceiver();
    }

    private void setUpProfileReceiver(){
        profileReceiver.setReceiver(new ProfileDataReceiver.ProfileReceiver() {
            @Override
            public void onReceivedResult(int resultCode, Bundle resultData) {
                if(resultCode == RESULT_OK){
                    String resultValue = resultData.getString(getString(R.string.result_value));
                    Toast.makeText(ProfileActivity.this, resultValue, Toast.LENGTH_LONG).show();
                    final String username = resultData.getString(ParseConstant.USERNAME_COLUMN);
                    final String friends = resultData.getString(ParseConstant.FRIEND_IDS);
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo(ParseConstant.USERNAME_COLUMN, username);

                    query.getFirstInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            ParseFile image = (ParseFile) parseUser.get(ParseConstant.USER_THUMBNAIL_COLUMN);
                            HashMap<String, Object> payload = new HashMap<>();
                            payload.put("sender", username);
                            payload.put("friends", friends);
                            payload.put("url", image.getUrl());
                            ParseCloud.callFunctionInBackground("thumbnailChannel", payload, new FunctionCallback<Object>() {
                                @Override
                                public void done(Object object, ParseException e) {
                                    if(e == null) {
                                        Log.d(TAG, "Successfully sent");
                                    }else {
                                        Log.d(TAG, e.getMessage());
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(UserThumbnailBroadcastReceiver.intentAction));
        Log.d(TAG, "On Resume");
        phoneNumberTextView.setText(mCurrentUser.getUsername());
        Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(mCurrentUser.getUsername()), null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            usernameTextView.setText(WordUtils.capitalize(cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME)).toLowerCase(), null));
            String thumbnail = cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL));
            if(thumbnail != null && thumbnail.length() > 0) {
                Glide.with(this).load(thumbnail).into(mImageProfileView);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    public void launchEditActivity(View view) {
        Intent  intent = new Intent(this, EditProfileNameActivity.class);
        intent.putExtra(ParseConstant.USER_PHONE_NUMBER_COLUMN, ParseUser.getCurrentUser().getUsername());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void changeProfilePic(View view){
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this, getString(R.string.choose_profile_image));
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                mBitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                File mProfileDir = new File(Environment.getExternalStorageDirectory().toString()+ "/" + ParseConstant.USER_PROFILE_DIR);
                if(!mProfileDir.exists()){
                    mProfileDir.mkdirs();
                }else {
                    try {
                        FileUtils.cleanDirectory(mProfileDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "File not created");
                    }
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                String imageFileName = "IMAGE_" + timeStamp + "_";

                String userThumbnailPath = imageFileName + timeStamp + ".jpg";
                File profilePicFile = new File(mProfileDir, userThumbnailPath);

                ProfileAsyncTask task = new ProfileAsyncTask(this);
                task.execute(profilePicFile);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private String getFriendIds(){
        ArrayList<String> friendList = new ArrayList<>();
        String friendIds = "";
        Cursor groupCursor = getContentResolver().query(YourTurnContract.GroupEntry.CONTENT_URI, null, null, null, null);
        if(groupCursor != null && groupCursor.getCount() > 0) {
            while (groupCursor.moveToNext()){
                String friendId = groupCursor.getString(groupCursor.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_USER_KEY));
                if(!friendList.contains(friendId) && !friendId.equals(mCurrentUser.getUsername())){
                    friendList.add(friendId);
                    friendIds += friendId + ",";
                }
            }
        }
        if(groupCursor != null) groupCursor.close();

        friendIds = friendIds.substring(0, friendIds.length()-1);
        return friendIds;
    }

    private class ProfileAsyncTask extends AsyncTask<File, Void, Bitmap> {

        private  Context mContext;
        public ProfileAsyncTask(Context context) {
            mContext =context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(File... params) {
            try {
                FileOutputStream out = new FileOutputStream(params[0]);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] groupImageByteData = stream.toByteArray();
                pFile = new ParseFile(ParseConstant.GROUP_THUMBNAIL_EXTENSION, groupImageByteData);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap != null) {
                mImageProfileView.setImageBitmap(bitmap);

                final ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo(ParseConstant.USERNAME_COLUMN, mCurrentUser.getUsername());
                query.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser currentUser, ParseException e) {
                        if(e == null) {
                            Log.d(TAG, "Found User");
                            currentUser.put(ParseConstant.USER_THUMBNAIL_COLUMN, pFile);
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null) {
                                        query.getFirstInBackground(new GetCallback<ParseUser>() {
                                            @Override
                                            public void done(ParseUser currentUser, ParseException e) {
                                                if(e == null) {
                                                    ParseFile image = (ParseFile) currentUser.get(ParseConstant.USER_THUMBNAIL_COLUMN);
                                                    Intent intent = new Intent(mContext, ProfileDataService.class);
                                                    intent.putExtra(ParseConstant.USERNAME_COLUMN, mCurrentUser.getUsername());
                                                    intent.putExtra(ParseConstant.FRIEND_IDS, getFriendIds());
                                                    intent.putExtra(ParseConstant.USER_THUMBNAIL_COLUMN, image.getUrl());
                                                    intent.putExtra(getString(R.string.profileDataReceiver), profileReceiver);
                                                    mContext.startService(intent);
                                                }else {
                                                    Log.d(TAG, e.getMessage());
                                                }
                                            }
                                        });
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
    }
}
