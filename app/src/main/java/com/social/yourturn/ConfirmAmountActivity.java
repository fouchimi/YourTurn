package com.social.yourturn;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.social.yourturn.broadcast.MyCustomReceiver;

public class ConfirmAmountActivity extends AppCompatActivity {

    private static final String TAG = ConfirmAmountActivity.class.getSimpleName();
    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_amount);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mActionBarToolbar.setTitle(getString(R.string.app_name));

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle bundle = intent.getExtras();
        Log.d(TAG, bundle.getString(MyCustomReceiver.TITLE));
        Log.d(TAG, bundle.getString(MyCustomReceiver.MESSAGE));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(bundle.getString(MyCustomReceiver.MESSAGE))
                .setTitle(bundle.getString(MyCustomReceiver.TITLE))
                .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create()
                .show();
    }
}
