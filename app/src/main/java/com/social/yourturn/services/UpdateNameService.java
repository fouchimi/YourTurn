package com.social.yourturn.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;

import java.util.ArrayList;

/**
 * Created by ousma on 6/15/2017.
 */

public class UpdateNameService extends IntentService {

    private static final String TAG = UpdateNameService.class.getSimpleName();

    public UpdateNameService() {
        super("UpdateNameService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ArrayList<Contact> list = intent.getParcelableArrayListExtra(getString(R.string.contact_list));
        for(Contact contact : list) {
            // Update contact member name here for every contact that has the application installed
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(ParseConstant.USERNAME_COLUMN, contact.getPhoneNumber());
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null) {
                        String name = user.getString(ParseConstant.COLUMN_NAME);
                        String number = user.getUsername();
                        if(name != null && name.length() > 0) {
                            ContentValues values = new ContentValues();
                            values.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME, name);
                            getContentResolver().update(YourTurnContract.MemberEntry.CONTENT_URI, values,
                                    YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(number), null);

                            values = new ContentValues();
                            values.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, name);
                            getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI, values,
                                    YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(number), null);
                        }
                    }else {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
        }

    }
}
