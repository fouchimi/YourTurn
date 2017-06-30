package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.yourturn.broadcast.PushSenderBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.utils.ParseConstant;

import java.util.HashMap;

public class ConfirmAmountActivity extends AppCompatActivity {

    private static final String TAG = ConfirmAmountActivity.class.getSimpleName();
    private Toolbar mActionBarToolbar;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "On received started!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_amount);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mActionBarToolbar.setTitle(R.string.confirm_price);

        onNewIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(PushSenderBroadcastReceiver.intentAction));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        final Bundle bundle = intent.getExtras();
        String senderPhoneNumber = bundle.getString(PushSenderBroadcastReceiver.TITLE);
        Log.d(TAG, bundle.getString(PushSenderBroadcastReceiver.TITLE));
        Log.d(TAG, bundle.getString(PushSenderBroadcastReceiver.MESSAGE));

        Cursor cursor = getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI,
                new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?",
                new String[]{senderPhoneNumber}, null);
        cursor.moveToFirst();
        String senderName = cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
        cursor.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(senderName + " wants you to confirm the amount below")
                .setMessage("You have been requested to confirm an amount of $ " + bundle.getString(PushSenderBroadcastReceiver.MESSAGE))
                .setPositiveButton(R.string.ok_text, (dialog, id) -> {

                    HashMap<String, Object> payload = new HashMap<>();
                    payload.put("targetId", bundle.getString(PushSenderBroadcastReceiver.SENDER_ID));
                    payload.put("recipientId", getUsername());
                    ParseCloud.callFunctionInBackground("receiverChannel", payload, (object, e) -> {
                        if(e == null) {
                            Log.d(TAG, "Reply confirmation push sent successfully !");
                            Toast.makeText(ConfirmAmountActivity.this, R.string.accepted_payment_message, Toast.LENGTH_LONG).show();
                            Intent intent1 = new Intent(ConfirmAmountActivity.this, MainActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent1);
                        }else {
                            Log.d(TAG, e.getMessage());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {

                    Toast.makeText(ConfirmAmountActivity.this, R.string.refused_payment_message, Toast.LENGTH_LONG).show();
                    Intent intent12 = new Intent(ConfirmAmountActivity.this, MainActivity.class);
                    intent12.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent12);
                });
        builder.create().show();
    }

    private String getUsername(){
        SharedPreferences sharePref = getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
        return sharePref.getString(ParseConstant.USERNAME_COLUMN, "");
    }
}
