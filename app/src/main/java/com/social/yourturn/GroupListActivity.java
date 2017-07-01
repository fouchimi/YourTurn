package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseCloud;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.yourturn.adapters.MemberEventAdapter;
import com.social.yourturn.broadcast.LedgerBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.broadcast.PushSenderBroadcastReceiver;
import com.social.yourturn.models.Event;
import com.social.yourturn.services.ConfirmPaymentIntentService;
import com.social.yourturn.services.ConfirmPaymentReceiver;
import com.social.yourturn.utils.ParseConstant;
import com.social.yourturn.utils.SwipeUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class GroupListActivity extends AppCompatActivity  {

    private static final String TAG = GroupListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private MemberEventAdapter mAdapter;
    private ArrayList<Contact> mContactList = new ArrayList<>();
    private boolean isVisible = false, isValidateVisible = false;
    private BroadcastReceiver mPushSenderBroadcastReceiver;
    private BroadcastReceiver mLedgerBroadcastReceiver;
    private String mSharedAmount, mTotalAmount;
    private int totalCount = 0;
    private PushReplyBroadcastReceiver pReplyBroadcastReceiver = new PushReplyBroadcastReceiver();
    private ConfirmPaymentReceiver mPaymentReceiver;
    private String valueList = "", targetIds = "", currentUserValue="";
    private String eventName, eventUrl;

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
            eventName = intent.getExtras().getString(EventActivity.EVENT_NAME);
            eventUrl = intent.getExtras().getString(LocationActivity.PLACE_URL);
            getSupportActionBar().setTitle(eventName);
            Contact currentUser = new Contact("0", "You", getUsername());
            mContactList.add(currentUser);

            Collections.sort(mContactList, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));

            mAdapter = new MemberEventAdapter(this, mContactList);
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

        setSwipeForRecyclerView();
        setupPaymentReceiver();
    }


    private void setupPaymentReceiver(){
        mPaymentReceiver.setReceiver((resultCode, resultData) -> {
            if(resultCode == RESULT_OK){
                String resultValue = resultData.getString(getString(R.string.result_value));
                final String eventId = resultData.getString(getString(R.string.selected_event_id));
                Toast.makeText(GroupListActivity.this, resultValue, Toast.LENGTH_LONG).show();
                HashMap<String, Object> payload = new HashMap<>();
                valueList += "," + currentUserValue;
                payload.put("eventId", eventId);
                payload.put("eventName", eventName);
                payload.put("totalAmount", mTotalAmount);
                payload.put("sharedValue", valueList);
                payload.put("eventUrl", eventUrl);
                payload.put("targetIds", targetIds);
                payload.put("sender", getUsername());
                ParseCloud.callFunctionInBackground("ledgerChannel", payload, (object, e) -> {
                    if(e == null) {
                        Intent confirmPaymentIntent = new Intent(GroupListActivity.this, EventRecordActivity.class);
                        Event event = new Event();
                        event.setEventId(eventId);
                        event.setName(eventName);
                        event.setEventUrl(eventUrl);
                        event.setContactList(mContactList);
                        confirmPaymentIntent.putExtra(getString(R.string.selected_event), event);
                        confirmPaymentIntent.putExtra(getString(R.string.totalAmount), mTotalAmount);
                        startActivity(confirmPaymentIntent);
                    }else {
                        Log.d(TAG, e.getMessage());
                    }
                });
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
                double totalPaidValue = 0.00;
                double totalRequestedValue = 0.00;
                mAdapter.notifyDataSetChanged();
                if(mAdapter.getContactList().size() < 2){
                    Toast.makeText(GroupListActivity.this, "You need at least two contacts to proceed", Toast.LENGTH_LONG).show();
                    return true;
                }
                targetIds = "";
                valueList = "";
                boolean foundCurrentUser = false;
                for(int pos = 0; pos < mAdapter.getContactList().size(); pos++){
                    String requestedValue = mAdapter.getContactList().get(pos).getRequested();
                    String paidValue = mAdapter.getContactList().get(pos).getPaid();
                    String friend = mAdapter.getContactList().get(pos).getPhoneNumber();
                    //matches positive decimal points.
                    if(requestedValue.matches("\\d*\\.?\\d+")){
                        double rValue = Double.parseDouble(requestedValue);
                        double pValue = Double.parseDouble(paidValue);
                        totalRequestedValue += rValue;
                        totalPaidValue += pValue;
                        if(!friend.equals(getUsername())){
                            targetIds += friend +",";
                            valueList += paidValue + ",";
                        }else {
                            currentUserValue = mAdapter.getContactList().get(pos).getPaid();
                            foundCurrentUser = true;
                        }

                    }else {
                        Toast.makeText(this, "Expecting whole number only", Toast.LENGTH_LONG).show();
                    }
                }

                if(!foundCurrentUser){
                    Toast.makeText(this, "You have to add yourself in order to proceed", Toast.LENGTH_LONG).show();
                    return true;
                }

                totalPaidValue = Math.ceil(totalPaidValue);
                totalRequestedValue = Math.ceil(totalRequestedValue);
                double mTotalParsedAmount = Double.parseDouble(mTotalAmount);
                double diffPaidValue = Math.abs(mTotalParsedAmount - totalPaidValue);
                double diffReqValue = Math.abs(mTotalParsedAmount - totalRequestedValue);
                if(targetIds.length() > 0 && valueList.length() > 0){
                    targetIds = targetIds.substring(0, targetIds.length()-1);
                    valueList = valueList.substring(0, valueList.length()-1);
                }
                if(diffPaidValue <= 1 && diffReqValue <= 1){
                    checkFriendAndValidate(false).onSuccess(new Continuation<List<ParseUser>, Void>() {
                        public Void then(Task<List<ParseUser>> results) throws Exception {
                            payload.put("senderId", getUsername());
                            payload.put("sharedValue", valueList);
                            payload.put("targetIds", targetIds);

                            ParseCloud.callFunctionInBackground("senderChannel", payload, (object, e) -> {
                                if(e == null) {
                                    Log.d(TAG, "Successfully sent");
                                    findContactInList(getUsername());
                                }else {
                                    Log.d(TAG, e.getMessage());
                                }
                            });
                            return null;
                        }
                    });
                }else {
                    Toast.makeText(this, "Make sure edited value adds up to " + mTotalAmount, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.validateBillAction:
                Intent intent = new Intent(GroupListActivity.this, ConfirmPaymentIntentService.class);
                intent.putParcelableArrayListExtra(getString(R.string.friendList), mAdapter.getContactList());
                intent.putExtra(getString(R.string.paymentReceiver), mPaymentReceiver);
                intent.putExtra(getString(R.string.selected_event), eventName);
                intent.putExtra(getString(R.string.totalAmount), mTotalAmount);
                intent.putExtra(LocationActivity.PLACE_URL, eventUrl);
                startService(intent);
                return true;

            case R.id.deleteAction:
                checkFriendAndValidate(true).onSuccess(new Continuation<List<ParseUser>, Void>() {
                    public Void then(Task<List<ParseUser>> ignored) throws Exception {
                        Toast.makeText(GroupListActivity.this, "All invalid users were deleted", Toast.LENGTH_LONG).show();
                        return null;
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private Task<ParseUser> fetchContactAsync(ParseQuery<ParseUser> query, final String name, final String phoneNumber, final boolean deleteAction){
        final TaskCompletionSource<ParseUser> tcs = new TaskCompletionSource<>();
        query.getFirstInBackground((user, e) -> {
            if(e == null) {
                tcs.setResult(user);
            }else {
                if(deleteAction){
                    mAdapter.getContactList().removeIf(contact -> contact.getPhoneNumber().equals(phoneNumber));
                    mAdapter.notifyDataSetChanged();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("User not registered")
                            .setMessage(WordUtils.capitalize(name, null) + " is not a valid yourturn user. Would you like to invite her by sms ?")
                            .setPositiveButton(R.string.YesBtn, (dialog, which) -> {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(phoneNumber, null, "I would like you to install yourturnapp", null, null);
                            })
                            .setNegativeButton(R.string.NoBtn, (dialog, which) -> Toast.makeText(GroupListActivity.this, "You won't be able to save the transaction with invalid user(s)", Toast.LENGTH_LONG).show())
                            .setOnDismissListener(dialog -> Toast.makeText(GroupListActivity.this, "You won't be able to save the transaction with invalid user(s)", Toast.LENGTH_LONG).show());
                    builder.create().show();
                }
                tcs.setError(e);
            }
        });

        return tcs.getTask();
    }

    private Task<List<ParseUser>> checkFriendAndValidate(boolean flag){

        ArrayList<Task<ParseUser>> tasks = new ArrayList<>();

        for(Contact contact : mAdapter.getContactList()) {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(ParseConstant.USERNAME_COLUMN, contact.getPhoneNumber());

            tasks.add(fetchContactAsync(query, contact.getName(), contact.getPhoneNumber(), flag));
        }

        return Task.whenAllResult(tasks);
    }

    private void showDialogBox(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog_box, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.totalAmount);
        edt.setInputType(InputType.TYPE_CLASS_NUMBER);

        dialogBuilder.setTitle(R.string.dialog_custom_title);
        dialogBuilder.setPositiveButton(R.string.done, (dialog, whichButton) -> {
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
                            contact.setPaid(mSharedAmount);
                            contact.setRequested(mSharedAmount);
                        }
                        mAdapter.notifyDataSetChanged();
                        isVisible = true;
                        invalidateOptionsMenu();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), R.string.custom_dialog_error_validation, Toast.LENGTH_LONG).show();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void findContactInList(String userPhoneId){
        int position = 0;
        for(Contact contact : mAdapter.getContactList()) {
            if(contact.getPhoneNumber().equals(userPhoneId)){
                View view = mRecyclerView.getChildAt(position);
                MemberEventAdapter.MemberViewHolder mViewHolder = (MemberEventAdapter.MemberViewHolder) mRecyclerView.getChildViewHolder(view);
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
                    MemberEventAdapter.MemberViewHolder mViewHolder = (MemberEventAdapter.MemberViewHolder) mRecyclerView.getChildViewHolder(view);
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
                        MemberEventAdapter.MemberViewHolder mViewHolder = (MemberEventAdapter.MemberViewHolder) mRecyclerView.getChildViewHolder(view);
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

    private void setSwipeForRecyclerView() {

        SwipeUtil swipeHelper = new SwipeUtil(0, ItemTouchHelper.LEFT, this) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int swipedPosition = viewHolder.getAdapterPosition();
                mAdapter.pendingRemoval(swipedPosition);
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (mAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(swipeHelper);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        //set swipe label
        swipeHelper.setLeftSwipeLable("Archive");
        //set swipe background-Color
        swipeHelper.setLeftcolorCode(ContextCompat.getColor(this, R.color.deep_red));

    }
}
