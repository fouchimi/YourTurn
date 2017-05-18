package com.social.yourturn;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.parse.ParseUser;
import com.social.yourturn.adapters.ContactsAdapter;
import com.social.yourturn.adapters.SelectedContactAdapter;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;


public class ContactActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, SelectedContactAdapter.IContactUpdateListener {

    private static final String TAG = ContactActivity.class.getSimpleName();
    private ContactsAdapter mAdapter;
    private SelectedContactAdapter mSelectedContactAdapter;
    private ListView mListView;
    private String mSearchTerm = null;
    private ArrayList<Contact> mSelectedContactList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton fb;
    private static final int ITEM_WIDTH = 280;
    public static final String SELECTED_CONTACT = "Selected";
    public static final String TOTAL_COUNT = "TotalCount";
    private int mTotalContact = 0;
    private List<Contact> mList = null;
    private List<Contact> mContactList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getSupportLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fb = (FloatingActionButton) findViewById(R.id.contact_fb);
        if(mSelectedContactList.size() <= 0) fb.hide();

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedContactList.size() > 0){
                    Intent intent = new Intent(ContactActivity.this, GroupActivity.class);
                    intent.putParcelableArrayListExtra(ContactActivity.SELECTED_CONTACT, mSelectedContactList);
                    intent.putExtra(ContactActivity.TOTAL_COUNT, mTotalContact);
                    ContactActivity.this.startActivity(intent);
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);

        mSelectedContactAdapter = new SelectedContactAdapter(this, mSelectedContactList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false){
            @Override
            public void smoothScrollToPosition(final RecyclerView recyclerView, RecyclerView.State state, int position) {
                super.smoothScrollToPosition(recyclerView, state, position);
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(ContactActivity.this){
                    private static  final float SPEED = 300f;
                    @Nullable
                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        super.computeScrollVectorForPosition(targetPosition);
                        return new PointF(targetPosition * ITEM_WIDTH, 0);
                    }

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return SPEED / displayMetrics.densityDpi;
                    }
                };
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }
        };

        mRecyclerView.setLayoutManager(mLayoutManager);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mSelectedContactAdapter);
        alphaAdapter.setDuration(1000);
        mRecyclerView.setItemAnimator(new FadeInAnimator());
        mRecyclerView.setAdapter(alphaAdapter);
        mAdapter = new ContactsAdapter(this);
        mListView = (ListView) findViewById(R.id.contactList);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
                if (mSearchTerm == null && newFilter == null) {
                    return true;
                }

                if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
                    return true;
                }

                mSearchTerm = newFilter;
                onSelectionCleared();

                getSupportLoaderManager().restartLoader(ContactsQuery.QUERY_ID, null, ContactActivity.this);
                return true;
            }
        });


        return true;
    }

    private void onSelectionCleared() {
        mAdapter.clearSelection();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == ContactsQuery.QUERY_ID) {
            Uri contentUri;

            if (mSearchTerm == null) {
                contentUri = ContactsQuery.CONTENT_URI;
            } else {
                contentUri =
                        Uri.withAppendedPath(ContactsQuery.FILTER_URI, Uri.encode(mSearchTerm));
            }

            return new CursorLoader(this,
                    contentUri,
                    ContactsQuery.PROJECTION,
                    ContactsQuery.SELECTION,
                    null,
                    ContactsQuery.SORT_ORDER);
        }

        Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId() == ContactsQuery.QUERY_ID){
            //Remove duplicates contacts
            MatrixCursor newCursor = new MatrixCursor(ContactsQuery.PROJECTION);
            if (cursor.moveToFirst()) {
                HashMap<String, String> map = new HashMap<>();
                String lastname = "";
                do {
                    if (!cursor.getString(ContactsQuery.DISPLAY_NAME).equalsIgnoreCase(lastname)) {
                        newCursor.addRow(new Object[]{
                                cursor.getString(ContactsQuery.ID),
                                cursor.getString(ContactsQuery.LOOKUP_KEY),
                                cursor.getString(ContactsQuery.DISPLAY_NAME),
                                cursor.getString(ContactsQuery.PHONE_NUMBER),
                                cursor.getString(ContactsQuery.SORT_KEY)});

                        lastname = cursor.getString(ContactsQuery.DISPLAY_NAME);
                    }
                } while (cursor.moveToNext());
            }
            mAdapter.swapCursor(newCursor);
            mTotalContact = newCursor.getCount();
            updateListView(newCursor);
        }
    }

    private void updateListView(Cursor cursor){
        while (cursor.moveToNext()){
            final String contactId = cursor.getString(ContactsQuery.ID);
            final String displayName = cursor.getString(ContactsQuery.DISPLAY_NAME).toUpperCase();
            final String phoneNumber = cursor.getString(ContactsQuery.PHONE_NUMBER);
            for(Contact contact : mContactList){
                if(contact.getId().equals(contactId) &&
                        contact.getName().equals(displayName) &&
                        contact.getPhoneNumber().equals(phoneNumber)){
                    int position = cursor.getPosition();
                    mAdapter.setSelected(position, true);
                    break;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void removeOnListView(Cursor cursor, Contact contact){
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            final String contactId = cursor.getString(ContactsQuery.ID);
            if(contact.getId().equals(contactId)){
                int position = cursor.getPosition();
                mAdapter.setSelected(position, false);
                mAdapter.remove(position);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == ContactsQuery.QUERY_ID){
            mAdapter.swapCursor(null);
        }
    }

    private Contact getSelectedContact(int position, Cursor cursor){
        mList = mAdapter.getContactList();
        cursor.moveToPosition(position);
        final String contactId = cursor.getString(ContactsQuery.ID);
        final String displayName = cursor.getString(ContactsQuery.DISPLAY_NAME).toUpperCase();
        final String phoneNumber = cursor.getString(ContactsQuery.PHONE_NUMBER);
        Contact targetedContact = new Contact(contactId, displayName, phoneNumber);
        for(Contact contact : mList){
            if(targetedContact.getId().equals(contact.getId()) &&
                    targetedContact.getName().equals(contact.getName()) &&
                    targetedContact.getPhoneNumber().equals(contact.getPhoneNumber())){
                targetedContact.setPosition(contact.getPosition());
                return targetedContact;
            }
        }
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor cursor = mAdapter.getCursor();
        Contact contact = getSelectedContact(position, cursor);
        if(mAdapter.isSelected(position)){
            mAdapter.setSelected(position, false);
            mAdapter.remove(position);
            removeFromListView(contact);
            removeFromRecyclerView(contact);
        }else {
            mAdapter.setSelected(position, true);
            if(!mSelectedContactList.contains(contact)){
                mSelectedContactList.add(contact);
            }
            if(!mContactList.contains(contact)){
                mContactList.add(contact);
            }
            mRecyclerView.smoothScrollToPosition(mSelectedContactAdapter.getItemCount());
        }

        showOrHideRecyclerView();
        mAdapter.notifyDataSetChanged();
        mSelectedContactAdapter.notifyDataSetChanged();
    }

    private void showOrHideRecyclerView(){
        if(mContactList.size() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            fb.show();
        }else {
            mRecyclerView.setVisibility(View.GONE);
            fb.hide();
        }
    }

    private void removeFromListView(Contact contact){
        for(Contact targetedContact : mContactList){
            if(targetedContact.getId().equals(contact.getId())) {
                mContactList.remove(targetedContact);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void removeFromRecyclerView(Contact  contact){
        for(int i=0; i < mSelectedContactList.size(); i++){
            if(mSelectedContactList.get(i).getId().equals(contact.getId())) {
                mSelectedContactList.remove(i);
                mSelectedContactAdapter.notifyItemRemoved(i);
                break;
            }
        }
        mSelectedContactAdapter.notifyDataSetChanged();
    }


    @Override
    public void onUpdateContactList(Contact contact) {
        removeFromRecyclerView(contact);
        removeFromListView(contact);
        removeOnListView(mAdapter.getCursor(), contact);
        showOrHideRecyclerView();
    }

    public interface ContactsQuery {
        final static int QUERY_ID = 1;

        final static Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        final static Uri FILTER_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI;

        @SuppressLint("InlinedApi")
        final static String SELECTION = (Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) +
                "<>''" + " AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + ("1") + "'" + " AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER;

        @SuppressLint("InlinedApi")
        final static String SORT_ORDER = Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                SORT_ORDER
        };
        final static int ID = 0;
        final static int LOOKUP_KEY = 1;
        final static int DISPLAY_NAME = 2;
        final static int PHONE_NUMBER = 3;
        final static int SORT_KEY = 4;
    }

}
