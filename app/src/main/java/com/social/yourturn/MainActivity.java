package com.social.yourturn;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.fragments.GroupFragment;
import com.social.yourturn.fragments.LatestUpdateFragment;
import com.social.yourturn.utils.ParseConstant;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ParseUser mCurrentUser;
    private static final int PERMISSION_ALL = 0;
    private String mDeviceMetaData = "";
    private String[] permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String phoneId="", phoneNumber= "";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter((getSupportFragmentManager()));

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        requestAllPermissions(this, permissions);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isConnected()){
            // Load groups here.
        }else {
            //Display dialog box here
        }

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
        phoneId = sharedPref.getString(ParseConstant.USERNAME_COLUMN, "");
        phoneNumber = sharedPref.getString(ParseConstant.PASSWORD_COLUMN, "");

        Log.d(TAG, "Phone ID from Shared Preferences: " + phoneId);
        Log.d(TAG, "Phone Number from Shared Preferences: " + phoneNumber);

        if(!phoneId.equals("") && !phoneNumber.equals("")){
            ParseUser.logInInBackground(phoneId, phoneNumber, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){
                        mCurrentUser = user;
                    }else {
                        Log.d(TAG, e.getMessage());
                        //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() != null) return true;
        return false;
    }

    private void requestAllPermissions(Activity context, String[] permissions){
        if(!hasPermissions(this, permissions)){
            ActivityCompat.requestPermissions(context, permissions, PERMISSION_ALL);
        }
    }

    private boolean hasPermissions(Context context, String... permissions){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null){
            for(String permission : permissions){
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
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
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "permission granted");
                    mDeviceMetaData = getDeviceMetaData();

                    phoneId = mDeviceMetaData.split(" ")[0];
                    phoneNumber = mDeviceMetaData.split(" ")[1];
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        phoneNumber = phoneUtil.format(phoneUtil.parse(phoneNumber, Locale.getDefault().getCountry()), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                    } catch (NumberParseException e) {
                        Log.d(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Phone Id: " + phoneId);
                    Log.d(TAG, "Phone number: " + phoneNumber);

                    mCurrentUser = new ParseUser();
                    mCurrentUser.setUsername(phoneNumber);
                    mCurrentUser.setPassword(phoneId);
                    mCurrentUser.put(ParseConstant.DEVICE_ID_COLUMN, phoneId);
                    mCurrentUser.put(ParseConstant.USER_PHONE_NUMBER_COLUMN, phoneNumber);

                    //Save login credentials in Shared Preferences document
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(ParseConstant.USERNAME_COLUMN, phoneNumber);
                    editor.putString(ParseConstant.PASSWORD_COLUMN, phoneId);
                    editor.apply();

                    mCurrentUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Log.d(TAG, "PARSE USER TABLE CREATED !");
                            }else {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.d(TAG, e.getMessage());
                                Log.d(TAG, "An error occur !");
                            }
                        }
                    });


                }else {
                    Toast.makeText(this, "Permission denied !", Toast.LENGTH_LONG).show();
                }
        }
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
            phoneNumber = sharedPref.getString(ParseConstant.PASSWORD_COLUMN, "");
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

    private String getDeviceMetaData(){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId() + " " + telephonyManager.getLine1Number();
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


}
