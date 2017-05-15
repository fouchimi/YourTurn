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
import com.parse.ParseUser;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ImagePicker;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private String phoneNumber = "";
    private TextView usernameTextView;
    private CircleImageView mImageProfileView;
    private TextView phoneNumberTextView;
    private Intent chooseImageIntent;
    private Bitmap mBitmap = null;
    private File mProfileDir, profilePicFile;
    private static final String USER_PROFILE_DIR = "user_profile";
    private String groupThumbnailPath;

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

        phoneNumber = ParseUser.getCurrentUser().getUsername();

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
                ContentValues userProfileValues = new ContentValues();
                userProfileValues.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, groupThumbnailPath);
                int id =  getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI, userProfileValues,
                        YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null);
                if(id > 0) {
                    Log.d(TAG, String.valueOf(id));
                    Toast.makeText(ProfileActivity.this, R.string.thumbnail_updated_msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
