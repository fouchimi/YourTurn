package com.social.yourturn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.fragments.GroupFragment;
import com.social.yourturn.fragments.LatestUpdateFragment;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;
import com.social.yourturn.utils.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = MainActivity.class.getSimpleName();
    private ParseUser mCurrentUser;
    private static final  int PERMISSION_ALL = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
    private static final int LOADER_ID = 1;
    private ArrayList<Contact> mContactList;
    public static final String ALL_CONTACTS = "Contacts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!hasPermissions(MainActivity.this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                }else {
                    Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
                    intent.putExtra(ALL_CONTACTS, mContactList);
                    startActivity(intent);
                }

            }
        });

        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isConnected()){

            SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter((getSupportFragmentManager()));

            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);

            if(hasPermissions(this, PERMISSIONS)){
                // Load groups here.
                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
                String phoneId = sharedPref.getString(ParseConstant.USERNAME_COLUMN, "");
                String phoneNumber = sharedPref.getString(ParseConstant.PASSWORD_COLUMN, "");

                if(!phoneId.equals("") && !phoneNumber.equals("")){
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
        }else {
            //Display dialog box here
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.connection_title_msg)
                    .setMessage(R.string.connection_msg_content)
                    .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() != null) return true;
        return false;
    }


    private boolean hasPermissions(Context context, String... permissions){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null){
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSION_ALL:
                Log.d(TAG, String.valueOf(grantResults.length));
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED){

                    Log.d(TAG, "permission granted");
                    String phoneId="", phoneNumber="";

                    phoneId = getDeviceMetaData().split(" ")[0];
                    phoneNumber = getDeviceMetaData().split(" ")[1];
                    phoneNumber = sanitizePhoneNumber(phoneNumber);

                    Log.d(TAG, "Phone Id: " + phoneId);
                    Log.d(TAG, "Phone number: " + phoneNumber);

                    mCurrentUser = new ParseUser();
                    mCurrentUser.setUsername(phoneNumber);
                    mCurrentUser.setPassword(phoneId);
                    mCurrentUser.put(ParseConstant.DEVICE_ID_COLUMN, phoneId);
                    mCurrentUser.put(ParseConstant.USER_PHONE_NUMBER_COLUMN, phoneNumber);

                    //Save login credentials in Shared Preferences document
                    savedCredentials(phoneId, phoneNumber);

                    mCurrentUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Log.d(TAG, "PARSE USER TABLE CREATED !");
                                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                installation.put("device_id", mCurrentUser.getUsername());
                                installation.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            Log.d(TAG, "device id saved");
                                        }else {
                                            Log.d(TAG, e.getMessage());
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else {
                                //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.d(TAG, e.getMessage());
                                Log.d(TAG, "An error occur !");
                            }
                        }
                    });


                }else {
                    Toast.makeText(this, "Make sure to accept all permissions", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void savedCredentials(String phoneId, String phoneNumber){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ParseConstant.USERNAME_COLUMN, phoneNumber);
        editor.putString(ParseConstant.PASSWORD_COLUMN, phoneId);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String phoneNumber = sharedPref.getString(ParseConstant.PASSWORD_COLUMN, "");
            Cursor c = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                    YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null);
            if(c != null && c.getCount() <= 0) {
                Log.d(TAG, "No records found !");
                intent.putExtra(ParseConstant.USER_PHONE_NUMBER_COLUMN, phoneNumber);
            }
            if(c != null) c.close();
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ParseUser.logOut();
        Log.d(TAG, "Logging out");
    }

    private String getDeviceMetaData(){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId() + " " + telephonyManager.getLine1Number();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                return new CursorLoader(this,
                        ContactsQuery.CONTENT_URI,
                        ContactsQuery.PROJECTION,
                        ContactsQuery.SELECTION,
                        null,
                        ContactsQuery.SORT_ORDER);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mContactList = new ArrayList<>();
        if(loader.getId() == LOADER_ID){
            Vector<ContentValues> contactVector = new Vector<>();
            MatrixCursor newCursor = new MatrixCursor(ContactsQuery.PROJECTION);
            String contactId = "", displayName = "", phoneNumber = "";
            if (cursor.moveToFirst()) {
                do {
                    if (!cursor.getString(ContactsQuery.DISPLAY_NAME).toUpperCase().equals(displayName)) {
                        newCursor.addRow(new Object[]{
                                cursor.getString(ContactsQuery.ID),
                                cursor.getString(ContactsQuery.DISPLAY_NAME),
                                cursor.getString(ContactsQuery.PHONE_NUMBER)});

                        contactId = cursor.getString(ContactsQuery.ID);
                        displayName = cursor.getString(ContactsQuery.DISPLAY_NAME).toUpperCase();
                        phoneNumber = sanitizePhoneNumber(cursor.getString(ContactsQuery.PHONE_NUMBER));

                        if(displayName.matches("\\d+") || displayName.startsWith("+")) continue;

                        DateTime dayTime = new DateTime();

                        ContentValues contactValue = new ContentValues();
                        contactValue.put(YourTurnContract.MemberEntry._ID, contactId);
                        contactValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME, displayName);
                        contactValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER, phoneNumber);
                        contactValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_CREATED_DATE, dayTime.getMillis());
                        contactValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_UPDATED_DATE, dayTime.getMillis());
                        contactVector.add(contactValue);
                        mContactList.add(new Contact(contactId, displayName, phoneNumber));
                    }
                } while (cursor.moveToNext());
            }

            // Dump data into member table here
            if (contactVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[contactVector.size()];
                contactVector.toArray(cvArray);
                Cursor memberCursor = getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, null, null, null, null);
                if(memberCursor.getCount() == 0)
                    getContentResolver().bulkInsert(YourTurnContract.MemberEntry.CONTENT_URI, cvArray);
                if(memberCursor != null) memberCursor.close();
                Log.d(TAG, "Member Bulk insert successful");
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private String sanitizePhoneNumber(String phoneNumber){
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            phoneNumber = phoneUtil.format(phoneUtil.parse(phoneNumber, Locale.getDefault().getCountry()), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return phoneNumber;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new GroupFragment();
                case 1:
                    return new LatestUpdateFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.groups);
                case 1:
                    return getString(R.string.activities);
            }
            return super.getPageTitle(position);
        }
    }

    public interface ContactsQuery {

        final static Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        @SuppressLint("InlinedApi")
        final static String SELECTION = (Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) +
                "<>''" + " AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + ("1") + "'" + " AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER;

        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Phone._ID,
                Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        @SuppressLint("InlinedApi")
        final static String SORT_ORDER = Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        final static int ID = 0;
        final static int DISPLAY_NAME = 1;
        final static int PHONE_NUMBER = 2;
    }

}
