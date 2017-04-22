package com.social.yourturn;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.social.yourturn.utils.ParseConstant;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ParseUser mCurrentUser;
    private static final int PERMISSION_ALL = 0;
    private String mDeviceMetaData = "";
    private String[] permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE};
    private String phoneId="";
    private String phoneNumber= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        phoneId = sharedPref.getString(ParseConstant.USERNAME, "");
        phoneNumber = sharedPref.getString(ParseConstant.PASSWORD, "");

        Log.d(TAG, "Phone ID from Shared Preferences: " + phoneId);
        Log.d(TAG,  "Phone Number from Shared Preferences: " + phoneNumber);

        if(!phoneId.equals("") && !phoneNumber.equals("")){
            ParseUser.logInInBackground(phoneId, phoneNumber, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){
                        mCurrentUser = user;
                    }else {
                        Log.d(TAG, e.getMessage());
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
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
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "permission granted");
                    mDeviceMetaData = getDeviceMetaData();

                    phoneId = mDeviceMetaData.split(" ")[0];
                    phoneNumber = mDeviceMetaData.split(" ")[1];

                    Log.d(TAG, "Phone Id: " + phoneId);
                    Log.d(TAG, "Phone number: " + phoneNumber);

                    mCurrentUser = new ParseUser();
                    mCurrentUser.setUsername(phoneId);
                    mCurrentUser.setPassword(phoneNumber);

                    //Save login credentials in Shared Preferences document
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(ParseConstant.USERNAME, phoneId);
                    editor.putString(ParseConstant.PASSWORD, phoneNumber);
                    editor.commit();

                    mCurrentUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Log.d(TAG, "USER TABLE CREATED !");
                            }else {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.d(TAG, e.getMessage());
                                Log.d(TAG, "An error occured !");
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getDeviceMetaData(){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId() + " " + telephonyManager.getLine1Number();
    }


}
