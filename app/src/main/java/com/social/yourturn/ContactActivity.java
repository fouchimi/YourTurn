package com.social.yourturn;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.parse.ParseUser;
import com.social.yourturn.adapters.ContactsAdapter;
import com.social.yourturn.adapters.SelectedContactAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Place;
import com.social.yourturn.utils.ParseConstant;
import com.social.yourturn.utils.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Locale;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;


public class ContactActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SelectedContactAdapter.IContactUpdateListener {

    private static final String TAG = ContactActivity.class.getSimpleName();
    private ContactsAdapter mAdapter;
    private SelectedContactAdapter mSelectedContactAdapter;
    private String mSearchTerm = null;
    private ArrayList<Contact> mSelectedContactList = new ArrayList<>(), oldList = null;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fb;
    private static final int ITEM_WIDTH = 280;
    public static final String SELECTED_CONTACT = "Selected";
    public static final String TOTAL_COUNT = "TotalCount";
    private ArrayList<Contact> mContactList =  new ArrayList<>(), mList = new ArrayList<>();
    private static final int REQUEST_CODE = 34;
    private Place mPlace = null;
    private String placeUrl = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getSupportLoaderManager().initLoader(MemberQuery.QUERY_ID, null, this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fb = (FloatingActionButton) findViewById(R.id.contact_fb);
        if(mSelectedContactList.size() <= 0) fb.hide();

        if(getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            oldList = bundle.getParcelableArrayList(MainActivity.ALL_CONTACTS);
            mPlace = bundle.getParcelable(LocationActivity.CURRENT_PLACE);
            placeUrl = bundle.getString(LocationActivity.PLACE_URL);
        }

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedContactList.size() > 0){
                    Intent intent = new Intent(ContactActivity.this, GroupActivity.class);
                    intent.putParcelableArrayListExtra(ContactActivity.SELECTED_CONTACT, mSelectedContactList);
                    intent.putExtra(ParseConstant.USERNAME_COLUMN, ParseUser.getCurrentUser().getUsername());
                    intent.putExtra(ContactActivity.TOTAL_COUNT, oldList.size());
                    intent.putExtra(LocationActivity.CURRENT_PLACE, mPlace);
                    intent.putExtra(LocationActivity.PLACE_URL, placeUrl);
                    ContactActivity.this.startActivity(intent);
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);

        mSelectedContactAdapter = new SelectedContactAdapter(this, mSelectedContactList);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false){
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
        ListView mListView = (ListView) findViewById(R.id.contactList);
        mListView.setOnItemClickListener(new MyListViewItemListener());
        mAdapter = new ContactsAdapter(this);
        mListView.setAdapter(mAdapter);
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
                mAdapter.clearSelection();

                getSupportLoaderManager().restartLoader(MemberQuery.QUERY_ID, null, ContactActivity.this);
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK) {
                String DISPLAY_NAME = Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
                Cursor recentCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone._ID,
                                DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null,  ContactsContract.CommonDataKinds.Phone._ID + " DESC LIMIT 1");
                if(recentCursor != null && recentCursor.getCount() > 0) {
                    recentCursor.moveToNext();
                    String id = recentCursor.getString(recentCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                    String displayName = recentCursor.getString(recentCursor.getColumnIndex(DISPLAY_NAME));
                    String phoneNumber = sanitizePhoneNumber(recentCursor.getString(recentCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                    Log.d(TAG, "Id: " + id + " , displayName: " + displayName + ", phoneNumber: " + phoneNumber);
                    DateTime dayTime = new DateTime();
                    ContentValues newContactValues = new ContentValues();
                    newContactValues.put(YourTurnContract.MemberEntry._ID, id);
                    newContactValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME, displayName);
                    newContactValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER, phoneNumber);
                    newContactValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_CREATED_DATE, dayTime.getMillis());
                    newContactValues.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_UPDATED_DATE, dayTime.getMillis());

                    getContentResolver().insert(YourTurnContract.MemberEntry.CONTENT_URI, newContactValues);

                    Toast.makeText(this, "contact saved", Toast.LENGTH_LONG).show();

                    getSupportLoaderManager().restartLoader(MemberQuery.QUERY_ID, null, this);
                }

                recentCursor.close();
            }else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "contact failed to saved", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String sanitizePhoneNumber(String phoneNumber){
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            phoneNumber = phoneUtil.format(phoneUtil.parse(phoneNumber, Locale.getDefault().getCountry()), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return phoneNumber;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Sends a request to the People app to display the create contact screen
            case R.id.menu_add_contact:
                final Intent contactIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                contactIntent.putExtra("finishActivityOnSaveCompleted", true);
                startActivityForResult(contactIntent, REQUEST_CODE);
                break;
            default:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == MemberQuery.QUERY_ID) {
            Uri contentUri = YourTurnContract.MemberEntry.CONTENT_URI;

            if (mSearchTerm == null) {
                return new CursorLoader(this,
                        contentUri,
                        MemberQuery.PROJECTION,
                        null,
                        null,
                        MemberQuery.SORT_ORDER);
            } else {
                return new CursorLoader(this,
                        contentUri,
                        MemberQuery.PROJECTION,
                        YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME + " LIKE ? ",
                        new String[]{"%" + mSearchTerm + "%"},
                        MemberQuery.SORT_ORDER);
            }

        }

        Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId() == MemberQuery.QUERY_ID){
            mContactList.clear();
            while(cursor.moveToNext()){

                String contactId = cursor.getString(MemberQuery.ID);
                String displayName = cursor.getString(MemberQuery.DISPLAY_NAME);
                String phoneNumber = cursor.getString(MemberQuery.PHONE_NUMBER);
                String imageUrl = cursor.getString(MemberQuery.THUMBNAIL);

                Contact contact = new Contact(contactId, displayName, phoneNumber);
                contact.setThumbnailUrl(imageUrl);
                mContactList.add(contact);
            }
        }
        mAdapter.swapCursor(cursor);
        mAdapter.notifyDataSetChanged();
        //Update listview after clearing search box.
        updateListView(cursor);
    }

    private void updateListView(Cursor cursor){
        while (cursor.moveToNext()){
            for(Contact contact : mSelectedContactList){
                if(contact.getId().equals(cursor.getString(MemberQuery.ID)) &&
                        contact.getName().equals(cursor.getString(MemberQuery.DISPLAY_NAME).toUpperCase()) &&
                        contact.getPhoneNumber().equals(cursor.getString(MemberQuery.PHONE_NUMBER))){
                    int position = cursor.getPosition();
                    mAdapter.setSelected(position, true);
                    break;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private Contact getSelectedContact(int position, Cursor cursor){
        cursor.moveToPosition(position);
        for(Contact contact : mContactList){
            if(contact.getId().equals(cursor.getString(MemberQuery.ID)) &&
                    contact.getName().toUpperCase().equals(cursor.getString(MemberQuery.DISPLAY_NAME).toUpperCase()) &&
                    contact.getPhoneNumber().equals(cursor.getString(MemberQuery.PHONE_NUMBER))){
                if(contact.isSelected()) contact.setSelected(false);
                else contact.setSelected(true);
                return contact;
            }
        }
        return null;
    }

    private void showOrHideRecyclerView(){
        if(mList.size() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            fb.show();
        }else {
            mRecyclerView.setVisibility(View.GONE);
            fb.hide();
        }
    }

    private void removeFromListView(Contact contact){
        for(Contact targetedContact : mList){
            if(targetedContact.getId().equals(contact.getId())) {
                mList.remove(targetedContact);
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

    private void removeOnListView(Cursor cursor, Contact contact){
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            final String contactId = cursor.getString(MemberQuery.ID);
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
    public void onUpdateContactList(Contact contact) {
        removeFromRecyclerView(contact);
        removeFromListView(contact);
        removeOnListView(mAdapter.getCursor(), contact);
        showOrHideRecyclerView();
    }

    private class MyListViewItemListener implements ListView.OnItemClickListener {

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
                if(!mList.contains(contact)){
                    mList.add(contact);
                }
                mRecyclerView.smoothScrollToPosition(mSelectedContactAdapter.getItemCount());
            }

            showOrHideRecyclerView();
            mAdapter.notifyDataSetChanged();
            mSelectedContactAdapter.notifyDataSetChanged();
        }
    }

    public interface MemberQuery {
        final static int QUERY_ID = 1;

        @SuppressLint("InlinedApi")
        final static String SORT_ORDER = YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME;

        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                YourTurnContract.MemberEntry._ID,
                YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME,
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER,
                YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL
        };
        final static int ID = 0;
        final static int DISPLAY_NAME = 1;
        final static int PHONE_NUMBER = 2;
        final static int THUMBNAIL = 3;
    }

}
