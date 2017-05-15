package com.social.yourturn;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ImagePicker;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private String phoneNumber, phoneId;
    private TextView usernameTextView;
    private CircleImageView mImageProfileView;
    private TextView phoneNumberTextView;
    private Intent chooseImageIntent;
    private Bitmap mBitmap = null;
    private File mProfileDir, profilePicFile;
    private static final String USER_PROFILE_DIR = "user_profile";
    private String groupThumbnailPath;
    private byte[] groupImageByteData;
    private ParseFile pFile;
    private ParseUser mCurrentUser;

    private static final  int PICK_IMAGE_ID = 2014;

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
        mImageProfileView = (CircleImageView) findViewById(R.id.profile_picture);

        mCurrentUser = ParseUser.getCurrentUser();

        if(mCurrentUser != null) {
            phoneNumber = mCurrentUser.getUsername();
        }else {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            phoneNumber = sharedPref.getString(ParseConstant.USERNAME_COLUMN, "");
            phoneId = sharedPref.getString(ParseConstant.PASSWORD_COLUMN, "");

            ParseUser.logInInBackground(phoneId, phoneNumber, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){
                        mCurrentUser = user;
                    }else {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
        phoneNumberTextView.setText(phoneNumber);
        Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            usernameTextView.setText(cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME)));
            String thumbnail = cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL));
            if(thumbnail != null && thumbnail.length() > 0) {
                Glide.with(this).load(new File(Environment.getExternalStorageDirectory().toString() + "/" + USER_PROFILE_DIR + "/" +  thumbnail)).into(mImageProfileView);
            }
        }
    }

    public void launchEditActivity(View view) {
        Intent  intent = new Intent(this, EditProfileNameActivity.class);
        intent.putExtra(ParseConstant.USER_PHONE_NUMBER_COLUMN, ParseUser.getCurrentUser().getUsername());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void changeProfilePic(View view){
        chooseImageIntent = ImagePicker.getPickImageIntent(this, getString(R.string.choose_profile_image));
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                mBitmap = ImagePicker.getImageFromResult(this, resultCode, data, mImageProfileView);
                mProfileDir = new File(Environment.getExternalStorageDirectory().toString()+ "/" + USER_PROFILE_DIR);
                if(mProfileDir != null && !mProfileDir.exists()){
                    mProfileDir.mkdirs();
                }else {
                    try {
                        FileUtils.cleanDirectory(mProfileDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "File not created");
                    }
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMAGE_" + timeStamp + "_";

                groupThumbnailPath = imageFileName + timeStamp + ".jpg";
                profilePicFile = new File(mProfileDir, groupThumbnailPath);

                new ProfileAsyncTask().execute(profilePicFile);

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private class ProfileAsyncTask extends AsyncTask<File, Void, Bitmap> {

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
                groupImageByteData = stream.toByteArray();
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

                //save in content provider
                ContentValues userValues = new ContentValues();
                DateTime dayTime = new DateTime();
                int id;
                Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null, YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null);
                if(cursor.getCount() <=0 ){
                    // Insert
                    Log.d(TAG, "Not available in content provider");
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, 0);
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, phoneNumber);
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, groupThumbnailPath);
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
                    getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValues);
                }else {
                    // Update
                    Log.d(TAG, "Updating content provider");
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, groupThumbnailPath);
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
                    getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI, userValues,
                            YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=" + phoneNumber, null);
                }

                Toast.makeText(ProfileActivity.this, R.string.thumbnail_updated_msg, Toast.LENGTH_SHORT).show();

                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo(ParseConstant.USERNAME_COLUMN, phoneNumber);
                query.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser currentUser, ParseException e) {
                        if(e == null) {
                            Log.d(TAG, "Found User");
                            if(pFile != null) {
                                currentUser.put(ParseConstant.USER_THUMBNAIL_COLUMN, pFile);
                            }
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null) {
                                        Toast.makeText(ProfileActivity.this, R.string.thumbnail_updated_msg, Toast.LENGTH_SHORT).show();
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
