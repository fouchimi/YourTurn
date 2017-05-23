package com.social.yourturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.social.yourturn.broadcast.PushSenderBroadcastReceiver;
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
        Log.d(TAG, bundle.getString(PushSenderBroadcastReceiver.TITLE));
        Log.d(TAG, bundle.getString(PushSenderBroadcastReceiver.MESSAGE));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(bundle.getString(PushSenderBroadcastReceiver.MESSAGE))
                .setTitle(bundle.getString(PushSenderBroadcastReceiver.TITLE))
                .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        HashMap<String, Object> payload = new HashMap<>();
                        payload.put("targetId", bundle.getString(PushSenderBroadcastReceiver.SENDER_ID));
                        payload.put("recipientId", getCurrentPhoneNumber());
                        ParseCloud.callFunctionInBackground("receiverChannel", payload, new FunctionCallback<Object>() {
                            @Override
                            public void done(Object object, ParseException e) {
                                if(e == null) {
                                    Log.d(TAG, "Reply confirmation push sent successfully !");
                                    Toast.makeText(ConfirmAmountActivity.this, R.string.accepted_payment_message, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ConfirmAmountActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }else {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Toast.makeText(ConfirmAmountActivity.this, R.string.refused_payment_message, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ConfirmAmountActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }

    private String getCurrentPhoneNumber(){
        SharedPreferences sharePref = getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
        return sharePref.getString(ParseConstant.USERNAME_COLUMN, "");
    }
}
