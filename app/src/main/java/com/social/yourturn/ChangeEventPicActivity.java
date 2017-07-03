package com.social.yourturn;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Event;
import com.social.yourturn.utils.ImagePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChangeEventPicActivity extends AppCompatActivity {

    private static final String TAG = ChangeEventPicActivity.class.getSimpleName();
    private Event mEvent = null;
    private static final int PICK_IMAGE_ID = 32;
    private CircleImageView mEventImageView;
    private Bitmap mBitmap = null;
    private Cloudinary cloudinary;
    private FloatingActionButton delFab, fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_group_pic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEventImageView = (CircleImageView) findViewById(R.id.eventUrl);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        delFab = (FloatingActionButton) findViewById(R.id.delFab);
        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(this));

        Intent intent = getIntent();
        if(intent != null) {
            mEvent = intent.getParcelableExtra(getString(R.string.selected_event));
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mEvent.getName());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            if(mEvent.getEventUrl() != null && mEvent.getEventUrl().length() > 0){
                Glide.with(this).load(mEvent.getEventUrl()).into(mEventImageView);
                delFab.show();
            }
            else {
                delFab.hide();
            }
        }
    }


    public void changeEventPic(View view){
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this, getString(R.string.change_event_thumbnail));
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    public void deleteEventPic(View view){
        ContentValues values = new ContentValues();
        values.put(YourTurnContract.EventEntry.COLUMN_EVENT_URL, "");
       int updated_id = getContentResolver().update(YourTurnContract.EventEntry.CONTENT_URI, values, YourTurnContract.EventEntry.COLUMN_EVENT_ID + "=?", new String[]{mEvent.getEventId()});
        if(updated_id > 0) {
            Toast.makeText(this, "Event url deleted !", Toast.LENGTH_LONG).show();
            mEventImageView.setImageResource(R.drawable.ic_group_black_36dp);
            delFab.hide();
        }
    }

    private void saveEventUrl(String profileUrl, String eventId){
        ContentValues values = new ContentValues();
        values.put(YourTurnContract.EventEntry.COLUMN_EVENT_URL, profileUrl);
        int updated_id = getContentResolver().update(YourTurnContract.EventEntry.CONTENT_URI, values, YourTurnContract.EventEntry.COLUMN_EVENT_ID + "=?", new String[]{eventId});
        if(updated_id > 0){
            Log.d(TAG, "Event url updated successfully");
            Toast.makeText(this, "Event url updated !", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                mBitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                if(mBitmap != null) {
                    new UploadTask(this).execute();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private class UploadTask extends AsyncTask<Void, Void, String> {

        private Context mContext;
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
                saveEventUrl(profileId, mEvent.getEventId());
                mEventImageView.setImageBitmap(mBitmap);
                delFab.show();
            }
        }

    }
}
