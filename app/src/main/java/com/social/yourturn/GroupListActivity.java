package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.social.yourturn.adapters.MemberGroupAdapter;
import com.social.yourturn.broadcast.LedgerBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Group;
import com.social.yourturn.broadcast.PushSenderBroadcastReceiver;
import com.social.yourturn.services.ConfirmPaymentIntentService;
import com.social.yourturn.services.ConfirmPaymentReceiver;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class GroupListActivity extends AppCompatActivity  {

    private static final String TAG = GroupListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private MemberGroupAdapter mAdapter;
    private ArrayList<Contact> mContactList = new ArrayList<>();
    private boolean isVisible = false, isValidateVisible = false;
    private BroadcastReceiver mPushSenderBroadcastReceiver;
    private BroadcastReceiver mLedgerBroadcastReceiver;
    private String mSharedAmount, mTotalAmount;
    private int totalCount = 0;
    private PushReplyBroadcastReceiver pReplyBroadcastReceiver = new PushReplyBroadcastReceiver();
    private ConfirmPaymentReceiver mPaymentReceiver;
    private String shareValueList = "", recipientList= "", currentUserValue="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.members_rv);
        mPaymentReceiver = new ConfirmPaymentReceiver(new Handler());

        Intent intent = getIntent();
        if(intent != null) {
            mContactList = intent.getParcelableArrayListExtra(ContactActivity.SELECTED_CONTACT);
            String eventName = intent.getExtras().getString(GroupActivity.EVENT_NAME);
            getSupportActionBar().setTitle(eventName);
            Contact currentUser = new Contact("0", "You", getUsername());
            mContactList.add(currentUser);

            Collections.sort(mContactList, new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });

            mAdapter = new MemberGroupAdapter(this, mContactList);
            LinearLayoutManager mLinearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLinearLayout);
            mRecyclerView.setAdapter(mAdapter);

            mPushSenderBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "On received invoked");
                    Toast.makeText(getApplicationContext(), "On Received invoked !", Toast.LENGTH_SHORT).show();
                }
            };

            mLedgerBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "On received invoked");
                }
            };
        }

        setupPaymentReceiver();
    }


    private void setupPaymentReceiver(){
        mPaymentReceiver.setReceiver(new ConfirmPaymentReceiver.Receiver() {
            @Override
            public void onReceivedResult(int resultCode, Bundle resultData) {
                if(resultCode == RESULT_OK){
                    String resultValue = resultData.getString(getString(R.string.result_value));
                    final Group group = resultData.getParcelable(getString(R.string.selected_group));
                    Toast.makeText(GroupListActivity.this, resultValue, Toast.LENGTH_LONG).show();
                    HashMap<String, Object> payload = new HashMap<>();
                    shareValueList += "," + currentUserValue;
                    payload.put("groupId", group.getGroupId());
                    payload.put("totalAmount", mTotalAmount);
                    payload.put("sharedValueList", shareValueList);
                    payload.put("friendIds", recipientList);
                    payload.put("sender", getUsername());
                    ParseCloud.callFunctionInBackground("ledgerChannel", payload, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object object, ParseException e) {
                            if(e == null) {
                                Log.d(TAG, "Successfully sent");
                                Intent confirmPaymentIntent = new Intent(GroupListActivity.this, GroupRecordActivity.class);
                                confirmPaymentIntent.putExtra(getString(R.string.selected_group), group);
                                startActivity(confirmPaymentIntent);
                            }else {
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        MenuItem item = menu.findItem(R.id.validateButton);
        item.setVisible(isVisible);
        MenuItem validateBillItem = menu.findItem(R.id.validateBillAction);
        validateBillItem.setVisible(isValidateVisible);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushSenderBroadcastReceiver, new IntentFilter(PushSenderBroadcastReceiver.intentAction));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLedgerBroadcastReceiver, new IntentFilter(LedgerBroadcastReceiver.intentAction));
        IntentFilter filter = new IntentFilter("com.parse.push.intent.RECEIVE");
        registerReceiver(pReplyBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPushSenderBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLedgerBroadcastReceiver);
        unregisterReceiver(pReplyBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.splitMenuAction :
                showDialogBox();
                return true;
            case R.id.validateButton :
                // Kick off push notification here
                HashMap<String, Object> payload = new HashMap<>();
                double totalValue = 0.00;
                mAdapter.notifyDataSetChanged();
                for(int pos = 0; pos < mAdapter.getContactList().size(); pos++){
                    String value = mAdapter.getContactList().get(pos).getShare();
                    String friend = mAdapter.getContactList().get(pos).getPhoneNumber();
                    //matches positive decimal points.
                    if(value.matches("\\d*\\.?\\d+")){
                        double dValue = Double.parseDouble(value);
                        totalValue += dValue;
                        if(!friend.equals(getUsername())){
                            recipientList += friend +",";
                            shareValueList += value + ",";
                        }else {
                            currentUserValue = value;
                        }

                    }else {
                        Toast.makeText(this, "Expecting whole number only", Toast.LENGTH_LONG).show();
                    }
                }

                totalValue = Math.ceil(totalValue);
                double mTotalParsedAmount = Double.parseDouble(mTotalAmount);
                double diff = Math.abs(mTotalParsedAmount - totalValue);
               // Toast.makeText(this, df.format(totalValue), Toast.LENGTH_LONG).show();
                recipientList = recipientList.substring(0, recipientList.length()-1);
                shareValueList = shareValueList.substring(0, shareValueList.length()-1);
                if(diff <= 1){
                    payload.put("senderId", getUsername());
                    payload.put("sharedValueList", shareValueList);
                    payload.put("recipientList", recipientList);

                    ParseCloud.callFunctionInBackground("senderChannel", payload, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object object, ParseException e) {
                            if(e == null) {
                                Log.d(TAG, "Successfully sent");
                                findContactInList(getUsername());
                            }else {
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    });
                }else {
                    Toast.makeText(this, "Make sure edited value adds up to previous total", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.validateBillAction:
                Intent intent = new Intent(this, ConfirmPaymentIntentService.class);
                intent.putParcelableArrayListExtra(getString(R.string.friendList), mAdapter.getContactList());
                //intent.putExtra(getString(R.string.selected_group), mGroup);
                intent.putExtra(getString(R.string.paymentReceiver), mPaymentReceiver);
                intent.putExtra(getString(R.string.totalAmount), mTotalAmount);
                startService(intent);
                return true;
            case R.id.viewGroupAction:
                Intent recordIntent = new Intent(this, GroupRecordActivity.class);
                //recordIntent.putExtra(getString(R.string.selected_group), mGroup);
                startActivity(recordIntent);
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
        edt.setInputType(InputType.TYPE_CLASS_NUMBER);

        dialogBuilder.setTitle(R.string.dialog_custom_title);
        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = edt.getText().toString();
                if(value.length() > 0) {
                    if(StringUtils.isNumeric(value)){
                        DecimalFormat df = new DecimalFormat("#.00");
                        float floatValue = Float.parseFloat(value);
                        mTotalAmount = df.format(floatValue);
                        if(floatValue <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.positive_error_validation, Toast.LENGTH_LONG).show();
                        }else {
                            Log.d(TAG, "" + floatValue);
                            for(Contact contact : mContactList){

                                mSharedAmount = df.format((floatValue / mContactList.size()));
                                contact.setShare(mSharedAmount);
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

    public void findContactInList(String userPhoneId){
        int position = 0;
        for(Contact contact : mAdapter.getContactList()) {
            if(contact.getPhoneNumber().equals(userPhoneId)){
                View view = mRecyclerView.getChildAt(position);
                MemberGroupAdapter.MemberViewHolder mViewHolder = (MemberGroupAdapter.MemberViewHolder) mRecyclerView.getChildViewHolder(view);
                if(mViewHolder != null){
                    mViewHolder.getCheckedIcon().setVisibility(View.VISIBLE);
                }
            }
            position++;
        }
    }

    private String getUsername() {
        SharedPreferences shared = getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }


    private class PushReplyBroadcastReceiver extends BroadcastReceiver  {
        private static final String intentAction = "com.parse.push.intent.RECEIVE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null) {
                Log.d(TAG, "Reply Broadcast Receiver intent null");
            }else {
                processReceiverPush(context, intent);
            }
        }

        private void processReceiverPush(Context context, Intent intent) {
            String receiverId = "";
            String action = intent.getAction();
            Log.d(TAG, "got action " + action);
            if(action.equals(intentAction)){
                String channel = intent.getExtras().getString("com.parse.Channel");
                Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
                try{
                    JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                    // Iterate the placeParse keys if needed
                    Iterator<String> itr = json.keys();
                    while(itr.hasNext()){
                        String key = (String) itr.next();
                        if(key.equals("rec_id")) {
                            receiverId = json.getString(key);
                            Log.d(TAG, "receiver Phone Number: " + receiverId);
                            displayAcceptanceMessage(context, receiverId);
                            break;
                        }
                        Log.d(TAG, "..." + key + " => " + json.getString(key) + ", ");
                    }
                }catch (JSONException ex){
                    ex.printStackTrace();
                    Log.d(TAG, ex.getMessage());
                }
            }
        }

        private void displayAcceptanceMessage(Context context, String rec_id){

            Cursor receiverCursor = context.getContentResolver().query(
                    YourTurnContract.MemberEntry.CONTENT_URI,
                    new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME},
                    YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?",
                    new String[]{rec_id}, null);

            if(receiverCursor != null && receiverCursor.getCount() > 0) {
                receiverCursor.moveToFirst();
                String receiverName = receiverCursor.getString(receiverCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
                Toast.makeText(context, receiverName + " accepted to pay", Toast.LENGTH_LONG).show();
                findContactInList(rec_id);

                for(int i = 0; i < mContactList.size(); i++) {
                    View view = mRecyclerView.getChildAt(i);
                    MemberGroupAdapter.MemberViewHolder mViewHolder = (MemberGroupAdapter.MemberViewHolder) mRecyclerView.getChildViewHolder(view);
                    if( mViewHolder.getCheckedIcon().getVisibility() == View.VISIBLE) {
                        totalCount++;
                    }
                }

                if(totalCount == mContactList.size()) {
                    isVisible = false;
                    isValidateVisible = true;
                    invalidateOptionsMenu();
                    Toast.makeText(context, "All requests answered", Toast.LENGTH_LONG).show();

                    for(int i = 0; i < mContactList.size(); i++) {
                        View view = mRecyclerView.getChildAt(i);
                        MemberGroupAdapter.MemberViewHolder mViewHolder = (MemberGroupAdapter.MemberViewHolder) mRecyclerView.getChildViewHolder(view);
                        mViewHolder.getRequestedEditText().setEnabled(false);
                        mViewHolder.getRequestedEditText().setFocusable(false);
                        mViewHolder.getRequestedEditText().setBackgroundColor(Color.LTGRAY);
                        mViewHolder.getPaidEditText().setEnabled(false);
                        mViewHolder.getPaidEditText().setFocusable(false);
                        mViewHolder.getPaidEditText().setBackgroundColor(Color.LTGRAY);
                    }
                }
            }
        }

    }
}
