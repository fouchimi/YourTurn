package com.social.yourturn;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.social.yourturn.adapters.MemberGroupAdapter;
import com.social.yourturn.fragments.GroupFragment;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Group;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.members_rv);

        Intent intent = getIntent();
        if(intent != null) {
            Group group = intent.getParcelableExtra(GroupFragment.GROUP_KEY);
            getSupportActionBar().setTitle(group.getName());
            mContactList = group.getContactList();
            Contact contact = new Contact();
            contact.setName("You");
            contact.setPhoneNumber(ParseUser.getCurrentUser().getUsername());
            mContactList.add(contact);
            mAdapter = new MemberGroupAdapter(this, mContactList);
            mLinearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLinearLayout);
            mRecyclerView.setAdapter(mAdapter);
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
                for(Contact contact : mContactList){
                    recipients += contact.getPhoneNumber()+",";
                }
                recipients = recipients.substring(0, recipients.length()-1);
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("title", ParseUser.getCurrentUser().getUsername());
                payload.put("alert", "First push");
                payload.put("recipients", recipients);
                ParseCloud.callFunctionInBackground("pushChannel", payload, new FunctionCallback<String>() {
                    @Override
                    public void done(String success, ParseException e) {
                        if(e == null){
                            Log.d(TAG, "Push notification sent successfully");
                            Toast.makeText(GroupListActivity.this, "Push sent successfully !!!", Toast.LENGTH_LONG).show();
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

}
