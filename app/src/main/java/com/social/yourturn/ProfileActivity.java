package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.broadcast.UserThumbnailBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ImagePicker;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
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
    private BroadcastReceiver mBroadcastReceiver;
    private FloatingActionButton delFab;

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

        delFab = (FloatingActionButton) findViewById(R.id.delFab);
        usernameTextView = (TextView) findViewById(R.id.nameTextField);
        phoneNumberTextView = (TextView) findViewById(R.id.phoneNumberField);
        mImageProfileView = (CircleImageView) findViewById(R.id.profile_picture);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "On received invoked");
                Toast.makeText(getApplicationContext(), "On Received invoked !", Toast.LENGTH_SHORT).show();
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(UserThumbnailBroadcastReceiver.intentAction));
        Log.d(TAG, "On Resume");
        phoneNumberTextView.setText(getUsername());
        Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(getUsername()), null, null);
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            String name = cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME));
            if(name != null) {
                usernameTextView.setText(WordUtils.capitalize(name.toLowerCase(), null));
            }
            String thumbnail = cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL));
            if(thumbnail != null && thumbnail.length() > 0) {
                Glide.with(this).load(thumbnail).into(mImageProfileView);
                delFab.setVisibility(View.VISIBLE);
            }else {
                delFab.setVisibility(View.INVISIBLE);
            }
        }

        cursor.close();
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

    public void deleteProfilePic(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Delete Action")
                .setMessage("Are you sure you want to delete profile picture")
                .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mImageProfileView.setImageResource(R.drawable.ic_account_grey);
                        delFab.hide();
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo(ParseConstant.USERNAME_COLUMN, getUsername());
                        query.getFirstInBackground(new GetCallback<ParseUser>() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(e == null){
                                    if(e == null) {
                                        user.remove(ParseConstant.USER_THUMBNAIL_COLUMN);
                                        ContentValues profileValues = new ContentValues();
                                        profileValues.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, "");

                                        getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI, profileValues,
                                                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=?", new String[]{getUsername()});

                                        ContentValues memberValues = new ContentValues();
                                        memberValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL, "");

                                        getContentResolver().update(YourTurnContract.MemberEntry.CONTENT_URI,
                                                memberValues,
                                                YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL + "=?", new String[]{getUsername()});

                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if(e == null) {
                                                    Toast.makeText(ProfileActivity.this, "profile image deleted successfully", Toast.LENGTH_LONG).show();
                                                    HashMap<String, Object> payload = new HashMap<>();
                                                    payload.put("sender", getUsername());
                                                    payload.put("friends", getFriendIds());
                                                    payload.put("url", "");
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
                                            }
                                        });
                                    }
                                }else {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                }).setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                mBitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                if(mBitmap != null) {
                    delFab.show();

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                    String imageFileName = "IMAGE_" + timeStamp + "_";

                    String userThumbnailPath = imageFileName + timeStamp + ".jpg";
                    File profilePicFile = new File(getCacheDir(), userThumbnailPath);

                    ProfileAsyncTask task = new ProfileAsyncTask(this);
                    task.execute(profilePicFile);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private String getFriendIds(){
        String friendIds = "";
        Cursor userCursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null, null, null, null);
        if(userCursor != null && userCursor.getCount() > 0) {
            while (userCursor.moveToNext()){
                String number = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER));
                if(!number.equals(getUsername())) friendIds += number + ",";
            }
        }

        if(userCursor != null) userCursor.close();

        if(friendIds.length() > 0) {
            return friendIds.substring(0, friendIds.length()-1);
        }else return "";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startService(intent);
    }

    private String getUsername() {
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
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
                pFile = new ParseFile(ParseConstant.EVENT_THUMBNAIL_EXTENSION, groupImageByteData);
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
                query.whereEqualTo(ParseConstant.USERNAME_COLUMN, getUsername());
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

                                                    ContentValues memberValue = new ContentValues();
                                                    memberValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL, image.getUrl());

                                                    getApplicationContext().getContentResolver().update(YourTurnContract.MemberEntry.CONTENT_URI,
                                                            memberValue,
                                                            YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?",
                                                            new String[]{getUsername()});

                                                    ContentValues userValue = new ContentValues();
                                                    userValue.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, image.getUrl());

                                                    getApplicationContext().getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI,
                                                            userValue,
                                                            YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=?",
                                                            new String[]{getUsername()});


                                                    HashMap<String, Object> payload = new HashMap<>();
                                                    payload.put("sender", getUsername());
                                                    payload.put("friends", getFriendIds());
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
