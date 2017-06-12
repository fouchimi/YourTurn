package com.social.yourturn;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.social.yourturn.adapters.GroupRecordAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Group;
import com.social.yourturn.utils.ParseConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupRecordActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = GroupRecordActivity.class.getSimpleName();
    private Group mGroup = null;
    private static final int LOADER_ID = 0;
    ArrayList<Contact> contactList = null;
    GroupRecordAdapter recordAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView mListView  = (ListView) findViewById(R.id.group_contact_list);

        Intent intent = getIntent();
        if(intent != null) {
            mGroup = intent.getParcelableExtra(getString(R.string.selected_group));
            if(mGroup != null) {
                Log.d(TAG, "Group name: " + mGroup.getName());
                contactList = mGroup.getContactList();
                recordAdapter = new GroupRecordAdapter(this, contactList);
                mListView.setAdapter(recordAdapter);
            }
        }

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, YourTurnContract.LedgerEntry.CONTENT_URI, null, YourTurnContract.LedgerEntry.COLUMN_GROUP_KEY + "=" + DatabaseUtils.sqlEscapeString(mGroup.getGroupId()), null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "data: " + data.getCount());
        HashMap<String, Double> map = new HashMap<>();
        for(int i=0; i < data.getCount(); i++){
            data.moveToNext();
            String userId = data.getString(data.getColumnIndex(YourTurnContract.LedgerEntry.COLUMN_USER_KEY));
            String userShareAmount = data.getString(data.getColumnIndex(YourTurnContract.LedgerEntry.COLUMN_USER_SHARE));
            Log.d(TAG, "user Id: " + userId + " value: " + userShareAmount);
            if(!map.containsKey(userId)) {
                map.put(userId, Double.parseDouble(userShareAmount));
            }else {
                double value = map.get(userId);
                value += Double.parseDouble(userShareAmount);
                map.put(userId, value);
            }
        }

        Iterator itr = map.entrySet().iterator();
        while (itr.hasNext()){
            Map.Entry pair = (Map.Entry)itr.next();
            for(Contact contact : contactList){
                if(contact.getPhoneNumber().equals(pair.getKey())) {
                    contact.setShare(String.valueOf(pair.getValue()));
                    break;
                }
            }
            Log.d(TAG, "key: " + pair.getKey());
        }

        recordAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
