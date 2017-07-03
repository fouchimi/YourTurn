package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.cloudinary.Cloudinary;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.parse.ParseCloud;

import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.yourturn.broadcast.UserThumbnailBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ImagePicker;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private TextView usernameTextView;
    private CircleImageView mImageProfileView;
    private TextView phoneNumberTextView;
    private Bitmap mBitmap = null;
    private BroadcastReceiver mBroadcastReceiver;
    private FloatingActionButton delFab;
    Cloudinary cloudinary;

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

        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(UserThumbnailBroadcastReceiver.intentAction));
        Log.d(TAG, "On Resume");

        phoneNumberTextView.setText(getUsername());

        if(getProfilePic().length() > 0) {
            Glide.with(this).load(getProfilePic()).into(mImageProfileView);
            delFab.setVisibility(View.VISIBLE);
        }else  delFab.setVisibility(View.INVISIBLE);

        if(getProfileName().length() > 0){
            usernameTextView.setText(WordUtils.capitalize(getProfileName().toLowerCase(), null));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    public void launchEditActivity(View view) {
        Intent  intent = new Intent(this, EditProfileNameActivity.class);
        intent.putExtra(ParseConstant.USER_PHONE_NUMBER_COLUMN, getUsername());
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
                .setPositiveButton(R.string.ok_text, (dialog, which) -> {
                    mImageProfileView.setImageResource(R.drawable.ic_account_grey);
                    delFab.hide();
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo(ParseConstant.USERNAME_COLUMN, getUsername());
                    query.getFirstInBackground((user, e) -> {
                        if(e == null){
                           if(!user.isAuthenticated()){
                               user.logInInBackground(getUsername(), getPassword(), (parseUser, e1) -> {
                                   if(e1 == null) {
                                       parseUser.put(ParseConstant.USER_THUMBNAIL_COLUMN, "");
                                       parseUser.saveInBackground(e11 -> {
                                           if(e11 == null) {
                                               Log.d(TAG, "Deleted profile url successfully");
                                               saveOrDeleteProfile("");
                                               Toast.makeText(ProfileActivity.this, "Profile photo deleted successfully", Toast.LENGTH_LONG).show();
                                           }else {
                                               Log.d(TAG, "An error occured while deleting profile photo");
                                               Toast.makeText(ProfileActivity.this, "Couldn't delete profile photo in the server", Toast.LENGTH_LONG).show();
                                           }
                                       });
                                   }else {
                                       Log.d(TAG, "An error occur");
                                   }
                               });
                           }else {
                               user.put(ParseConstant.USER_THUMBNAIL_COLUMN, "");
                               user.saveInBackground(e12 -> {
                                   if(e12 == null) {
                                       Log.d(TAG, "Deleted profile url successfully");
                                       saveOrDeleteProfile("");
                                       Toast.makeText(ProfileActivity.this, "Profile photo deleted successfully", Toast.LENGTH_LONG).show();
                                   }else {
                                       Log.d(TAG, "An error occured while deleting profile photo");
                                       Toast.makeText(ProfileActivity.this, "Couldn't delete profile photo in the server", Toast.LENGTH_LONG).show();
                                   }
                               });
                           }
                        }else {
                            Log.d(TAG, e.getMessage());
                        }
                    });
                }).setNegativeButton(R.string.cancel_text, (dialog, which) -> {
                    Log.d(TAG, "Cancel login");
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
                    new UploadTask(this).execute();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private String getFriendIds(){
        String friendIds = "";
        Cursor registeredCursor = getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI,
                new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_REGISTERED + "=?", new String[]{"1"}, null);
        if(registeredCursor != null && registeredCursor.getCount() > 0) {
            while (registeredCursor.moveToNext()){
                String number = registeredCursor.getString(registeredCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER));
                if(!number.equals(getUsername())) friendIds += number + ",";
            }
        }

        registeredCursor.close();

        return (friendIds.length() > 0) ? friendIds.substring(0, friendIds.length()-1) : "";
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

    private String getPassword(){
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.PASSWORD_COLUMN, ""));
    }

    private String getProfilePic(){
        SharedPreferences shared = getSharedPreferences(getString(R.string.profile_path), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USER_THUMBNAIL_COLUMN, ""));
    }

    private String getProfileName(){
        SharedPreferences shared = getSharedPreferences(getString(R.string.profile_name), MODE_PRIVATE);
        return (shared.getString(ParseConstant.COLUMN_NAME, ""));
    }

    private void saveOrDeleteProfile(String profileId){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.profile_path), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ParseConstant.USER_THUMBNAIL_COLUMN, profileId);
        editor.apply();

        if(getFriendIds().length() > 0) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("senderId", getUsername());
            payload.put("targetIds", getFriendIds());
            payload.put("profileUrl", profileId);
            ParseCloud.callFunctionInBackground("thumbnailChannel", payload, (object, e) -> {
                if(e == null) {
                    Log.d(TAG, "Successfully sent");
                }else {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }

    private void saveProfileInBackground(String profileId) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(ParseConstant.USERNAME_COLUMN, getUsername());
        query.setLimit(1);

        query.getFirstInBackground((user, e) -> {
            if(e == null) {
                user.put(ParseConstant.USER_THUMBNAIL_COLUMN, profileId);
                if(!user.isAuthenticated()){
                    user.logInInBackground(getUsername(), getPassword(), (user1, e13) -> {
                        if(e13 == null){
                            user1.saveInBackground(e12 -> {
                                if(e12 == null){
                                    Log.d(TAG, "Profile saved successfully");
                                    Toast.makeText(ProfileActivity.this, "Profile saved successfully", Toast.LENGTH_LONG).show();
                                }else {
                                    Log.d(TAG, "An error occured");
                                    Log.d(TAG, e12.getMessage());
                                }
                            });
                        }else {
                            Log.d(TAG, "An error occured");
                            Log.d(TAG, e13.getMessage());
                        }
                    });
                }else {
                    user.saveInBackground(e1 -> {
                        if(e1 == null) {
                            Log.d(TAG, "Profile saved successfully");
                            Toast.makeText(ProfileActivity.this, "Profile saved successfully", Toast.LENGTH_LONG).show();
                        }else {
                            Log.d(TAG, "An error occured");
                            Log.d(TAG, e1.getMessage());
                        }
                    });
                }
            }else {
                Log.d(TAG, e.getMessage());
            }
        });
    }

    private class UploadTask extends AsyncTask<Void, Void, String> {

        private  Context mContext;
        public UploadTask(Context context) {
            mContext =context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            InputStream fileInputStream = null;

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "IMAGE_" + timeStamp + "_";

            String image_file = imageFileName + timeStamp + ".jpg";
            File profilePicFile = new File(getCacheDir(), image_file);

            final String profileId = UUID.randomUUID().toString();

            try {
                FileOutputStream out = new FileOutputStream(profilePicFile);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

                File cDir = mContext.getCacheDir();
                File tempFile = new File(cDir.getPath() + "/" + image_file) ;
                Log.d(TAG, "path: " + tempFile.getAbsolutePath());

                fileInputStream = new FileInputStream(tempFile);
                cloudinary.uploader().upload(fileInputStream, ObjectUtils.asMap("public_id", profileId));
                String fileId = cloudinary.url().generate(profileId + ".jpg");

                if(fileId != null) Log.d(TAG, fileId);

                return fileId;
            }catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String profileId) {
            super.onPostExecute(profileId);
            if(profileId != null) {
                Log.d(TAG, profileId);
                mImageProfileView.setImageBitmap(mBitmap);
                saveOrDeleteProfile(profileId);
                saveProfileInBackground(profileId);
            }
        }

    }
}
