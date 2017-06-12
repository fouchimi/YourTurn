package com.social.yourturn.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ParseConstant;

import org.joda.time.DateTime;

/**
 * Created by ousma on 6/12/2017.
 */

public class ProfileDataService extends IntentService {

    private static final String TAG = ProfileDataService.class.getSimpleName();

    public ProfileDataService() {
        super("ProfileDataService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String username = intent.getStringExtra(ParseConstant.USERNAME_COLUMN);
        String friendIds = intent.getStringExtra(ParseConstant.FRIEND_IDS);
        String thumbnail = intent.getStringExtra(ParseConstant.USER_THUMBNAIL_COLUMN);
        ResultReceiver profileDataReceiver = intent.getParcelableExtra(getString(R.string.profileDataReceiver));

        ContentValues userValues = new ContentValues();
        DateTime dayTime = new DateTime();
        Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(username), null, null);
        if(cursor != null && cursor.getCount() <=0 ){
            // Insert
            Log.d(TAG, "Not available in content provider");
            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, 0);
            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, username);
            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, thumbnail);
            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
            getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValues);
        }else {
            // Update
            Log.d(TAG, "Updating content provider");
            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, thumbnail);
            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
            getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI, userValues,
                    YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(username), null);
        }

        if(cursor != null) cursor.close();

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.result_value), "Thumbnail updated successfully");
        bundle.putString(ParseConstant.USERNAME_COLUMN, username);
        bundle.putString(ParseConstant.FRIEND_IDS, friendIds);
        bundle.putString(ParseConstant.USER_THUMBNAIL_COLUMN, thumbnail);

        profileDataReceiver.send(Activity.RESULT_OK, bundle);
    }
}

