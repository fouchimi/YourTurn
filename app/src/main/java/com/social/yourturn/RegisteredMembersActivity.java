package com.social.yourturn;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.social.yourturn.adapters.RegisteredMemberAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;

import java.util.ArrayList;

public class RegisteredMembersActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = RegisteredMembersActivity.class.getSimpleName();
    private static final int LOADER_ID = 4;
    private ArrayList<Contact> list = new ArrayList<>();
    private RegisteredMemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_members);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        ListView listView = (ListView) findViewById(R.id.members_list_view);
        memberAdapter = new RegisteredMemberAdapter(this, list);
        listView.setAdapter(memberAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.contacts_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == LOADER_ID){
            return new CursorLoader(this,
                    YourTurnContract.MemberEntry.CONTENT_URI,
                    null, YourTurnContract.MemberEntry.COLUMN_MEMBER_REGISTERED + "=?", new String[]{"1"},
                    YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME + " ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.getCount() > 0) {
            for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
                String id = data.getString(data.getColumnIndex(YourTurnContract.MemberEntry._ID));
                String name = data.getString(data.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
                String phoneNumber = data.getString(data.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER));
                String photoUrl = data.getString(data.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL));
                String score = data.getString(data.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_SCORE));
                Contact contact = new Contact(id, name, phoneNumber);
                contact.setScore(score);
                if(photoUrl != null && photoUrl.length() > 0) {
                    contact.setThumbnailUrl(photoUrl);
                }
                list.add(contact);
            }
        }

        memberAdapter.notifyDataSetChanged();
        Log.d(TAG, "Size: " + list.size());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
