package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.social.yourturn.adapters.MemberGroupAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.fragments.GroupFragment;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Group;
import com.social.yourturn.broadcast.MyCustomReceiver;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupListActivity extends AppCompatActivity {

    private static final String TAG = GroupListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private MemberGroupAdapter mAdapter;
    private ArrayList<Contact> mContactList = new ArrayList<>();
    private Toolbar mActionBarToolbar;
    private LinearLayoutManager mLinearLayout;
    private boolean isVisible = false;
    private String phoneId, phoneNumber;
    private ParseUser mCurrentUser;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.members_rv);

        login();

        Intent intent = getIntent();
        if(intent != null) {
            Group group = intent.getParcelableExtra(GroupFragment.GROUP_KEY);
            String phoneNumber = intent.getExtras().getString(ParseConstant.USERNAME_COLUMN);
            getSupportActionBar().setTitle(group.getName());
            mContactList = group.getContactList();
            Cursor cursor = getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null, YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null);
            if(cursor != null && cursor.getCount() == 0){
                Contact contact = new Contact();
                contact.setName(getString(R.string.current_user));
                contact.setOwner(true);
                contact.setPhoneNumber(phoneNumber);
                mContactList.add(contact);
            }
            mAdapter = new MemberGroupAdapter(this, mContactList);
            mLinearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLinearLayout);
            mRecyclerView.setAdapter(mAdapter);

            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "On received invoked");
                    Toast.makeText(getApplicationContext(), "On Received invoked !", Toast.LENGTH_SHORT).show();
                }
            };
        }
    }

    private void login(){
        if(mCurrentUser == null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
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
                            Log.d(TAG, "Current User: " + mCurrentUser.getUsername());
                        }else {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                });
            }
        }else {
            Log.d(TAG, "Username: " + mCurrentUser.getUsername());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        MenuItem item = menu.findItem(R.id.validateButton);
        item.setVisible(isVisible);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(MyCustomReceiver.intentAction));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings :
                return true;
            case R.id.splitMenuAction :
                showDialogBox();
                return true;
            case R.id.validateButton :
                // Kick off push notification here
                String recipients ="";
                int i=0;
                String contactArray[] = new String[mContactList.size()];
                for(Contact contact : mContactList){
                    if(!contact.getPhoneNumber().equals(getCurrentPhoneNumber())){
                        recipients += contact.getPhoneNumber()+",";
                        contactArray[i++] = contact.getName();
                    }

                }
                recipients = recipients.substring(0, recipients.length()-1);
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("alert", getCurrentPhoneNumber());
                payload.put("title", recipients);
                payload.put("recipients", contactArray);
                ParseCloud.callFunctionInBackground("pushChannel", payload, new FunctionCallback<Object>() {
                    @Override
                    public void done(Object object, ParseException e) {
                        if(e == null) {
                            Log.d(TAG, "Successfully sent");
                        }else {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                });

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showDialogBox(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog_box, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.totalAmount);

        dialogBuilder.setTitle(R.string.dialog_custom_title);
        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = edt.getText().toString();
                if(value.length() > 0) {
                    if(StringUtils.isNumeric(value)){
                        float floatValue = Float.parseFloat(value);
                        if(floatValue <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.positive_error_validation, Toast.LENGTH_LONG).show();
                        }else {
                            Log.d(TAG, "" + floatValue);
                            for(Contact contact : mContactList){
                                DecimalFormat df = new DecimalFormat("#.00");

                                contact.setShare(df.format((floatValue / mContactList.size())));
                            }
                            mAdapter.notifyDataSetChanged();
                            isVisible = true;
                            invalidateOptionsMenu();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), R.string.custom_dialog_error_validation, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    private String getCurrentPhoneNumber(){
        SharedPreferences sharePref = getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
        return sharePref.getString(ParseConstant.USERNAME_COLUMN, "");
    }

}
