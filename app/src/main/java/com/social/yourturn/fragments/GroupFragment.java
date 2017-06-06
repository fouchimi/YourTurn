package com.social.yourturn.fragments;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.yourturn.R;
import com.social.yourturn.adapters.GroupAdapter;
import com.social.yourturn.broadcast.GroupBroadcastReceiver;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Group;
import com.social.yourturn.utils.ParseConstant;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = GroupFragment.class.getSimpleName();
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private ArrayList<Group> mGroupList = new ArrayList<>();
    private GroupAdapter mGroupAdapter;
    public static final String GROUP_KEY = "Group";
    private static final int LOADER_ID = 0;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        emptyTextView = (TextView) view.findViewById(R.id.empty_view);
        emptyTextView.setTypeface(typeface);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.group_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mGroupList = new ArrayList<>();
        mGroupAdapter = new GroupAdapter(getActivity(), mGroupList, mRecyclerView);
        mRecyclerView.setAdapter(mGroupAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == LOADER_ID){
            return new CursorLoader(getActivity(), YourTurnContract.GroupEntry.CONTENT_URI,
                    null, null, null, YourTurnContract.GroupEntry.COLUMN_GROUP_UPDATED_DATE + " DESC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_ID){
            if(data == null){
                Log.d(TAG, "Empty cursor");
            }else {
                loadData(data);
            }
        }
    }

    private void loadData(Cursor data) {
        ArrayList<Contact> mContactList = new ArrayList<>();
        if(mGroupList != null) mGroupList.clear();
        String groupId = null, groupName = null, groupThumbnail = null, groupCreator = null, userId = null;
        String lastGroupId ="";
        long dateInMillis = 0L;
        Group group = null;

        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
            groupId = data.getString(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_GROUP_ID));
            groupName = data.getString(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_GROUP_NAME));
            groupThumbnail = data.getString(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_GROUP_THUMBNAIL));
            groupCreator = data.getString(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATOR));
            dateInMillis = data.getLong(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_GROUP_UPDATED_DATE));
            userId = data.getString(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_USER_KEY));

            Cursor userCursor = getActivity().getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                    YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(userId), null, null);
            if(userCursor != null && userCursor.getCount() > 0){
                userCursor.moveToFirst();
                String contactId = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_ID));
                String username = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME));
                String phoneNumber = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER));
                Contact contact = new Contact(contactId, username, phoneNumber);
                if(lastGroupId.isEmpty() || groupId.equals(lastGroupId)) {
                    lastGroupId = groupId;
                    mContactList.add(contact);
                }else {
                    lastGroupId = groupId;
                    mContactList = new ArrayList<>();
                    mContactList.add(contact);
                }
                userCursor.close();
            }

            if(mGroupList.isEmpty()){
                group = new Group();
                group.setGroupId(groupId);
                group.setName(groupName);
                group.setThumbnail(groupThumbnail);
                group.setGroupCreator(groupCreator);
                group.setDateInMillis(dateInMillis);
                group.setGroupUserRef(userId);
                group.setContactList(mContactList);
                mGroupList.add(group);
            }else if (mGroupList.size() > 0 && mGroupList.get(mGroupList.size() -1).getGroupId().equals(groupId)) continue;
            else {
                group = new Group();
                group.setGroupId(groupId);
                group.setName(groupName);
                group.setThumbnail(groupThumbnail);
                group.setGroupCreator(groupCreator);
                group.setDateInMillis(dateInMillis);
                group.setGroupUserRef(userId);
                group.setContactList(mContactList);
                mGroupList.add(group);
            }

        }

        if(mGroupList.size() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
            mGroupAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchLatestGroup();
    }

    private void fetchLatestGroup(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.GROUP_MEMBER_TABLE);
        query.whereEqualTo(ParseConstant.USER_ID_COLUMN, getCurrentPhoneNumber());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> rows, ParseException e) {
                if(e == null) {
                    for(ParseObject row : rows) {
                        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery(ParseConstant.GROUP_TABLE);
                        groupQuery.getInBackground(row.getString(ParseConstant.GROUP_MEMBER_TABLE_ID), new GetCallback<ParseObject>() {
                            @Override
                            public void done(final ParseObject groupRow, ParseException e) {
                                if(e == null) {
                                    final String groupId = groupRow.getObjectId();
                                    final String groupName = groupRow.getString(ParseConstant.GROUP_NAME);
                                    final String groupThumbnail = groupRow.getString(ParseConstant.GROUP_THUMBNAIL_COLUMN);
                                    final String groupCreator  = groupRow.getString(ParseConstant.USER_ID_COLUMN);
                                    final String members = groupRow.getString(ParseConstant.MEMBERS_COLUMN);

                                    String[] membersArray = members.split(",");
                                    final ArrayList<Contact> membersList = new ArrayList<>();
                                    for(String member : membersArray) {
                                        String[] contactArray = member.split(":");
                                        Contact contact = new Contact(contactArray[0], contactArray[1], contactArray[2]);
                                        membersList.add(contact);
                                    }

                                    final Cursor groupCursor = getActivity().getContentResolver().query(YourTurnContract.GroupEntry.CONTENT_URI, null,
                                            YourTurnContract.GroupEntry.COLUMN_GROUP_CREATOR + " = " +
                                                    DatabaseUtils.sqlEscapeString(groupCreator) + " AND " +
                                                    YourTurnContract.GroupEntry.COLUMN_GROUP_ID + " = " +
                                                    DatabaseUtils.sqlEscapeString(groupId), null, null);
                                    if(groupCursor != null && groupCursor.getCount() == 0) {

                                        ParseQuery<ParseUser> creatorQuery = ParseUser.getQuery();
                                        creatorQuery.whereEqualTo(ParseConstant.USERNAME_COLUMN, groupCreator);

                                        creatorQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                                            @Override
                                            public void done(ParseUser user, ParseException e) {
                                                if(e == null) {
                                                    List<Contact> finalMemberList = membersList;
                                                    Contact creatorContact = new Contact();
                                                    Cursor creatorCursor = null;
                                                    String creatorName = user.getString(ParseConstant.COLUMN_NAME);
                                                    String creatorId = null;
                                                    if(creatorName != null && creatorName.length() > 0){
                                                        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(PhoneNumberUtils.formatNumber(groupCreator)));
                                                        creatorCursor = getActivity().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
                                                        if(creatorCursor != null && creatorCursor.getCount() > 0){
                                                            creatorCursor.moveToFirst();
                                                            creatorId =  creatorCursor.getString(creatorCursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                                                            creatorName = user.getString(ParseConstant.COLUMN_NAME);
                                                        }
                                                    }else {
                                                        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(PhoneNumberUtils.formatNumber(groupCreator)));
                                                        creatorCursor = getActivity().getContentResolver().query(uri,
                                                                new String[]{ContactsContract.PhoneLookup._ID,
                                                                        ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                                                        if(creatorCursor != null && creatorCursor.getCount() > 0) {
                                                            creatorCursor.moveToFirst();
                                                            creatorId =  creatorCursor.getString(creatorCursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                                                            creatorName = creatorCursor.getString(creatorCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                                                        }
                                                    }

                                                    creatorContact.setId(creatorId);
                                                    creatorContact.setName(creatorName);
                                                    creatorContact.setPhoneNumber(groupCreator);
                                                    finalMemberList.add(creatorContact);

                                                    if(creatorContact != null) creatorCursor.close();

                                                    DateTime dayTime = new DateTime();

                                                    for(Contact contact : finalMemberList){
                                                        ContentValues groupValues = new ContentValues();
                                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_ID, groupId);
                                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_NAME, groupName);
                                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_USER_KEY, contact.getPhoneNumber());
                                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATOR, groupCreator);
                                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_THUMBNAIL, groupThumbnail);
                                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_CREATED_DATE, dayTime.getMillis());
                                                        groupValues.put(YourTurnContract.GroupEntry.COLUMN_GROUP_UPDATED_DATE, dayTime.getMillis());

                                                        // Insert individual contact here
                                                        Cursor userCursor = getActivity().getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null, YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(contact.getPhoneNumber()), null, null);
                                                        if(userCursor != null && userCursor.getCount() == 0){
                                                            ContentValues userValues = new ContentValues();
                                                            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, contact.getPhoneNumber());
                                                            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, contact.getId());
                                                            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, contact.getName());
                                                            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
                                                            userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());
                                                            // Insert individual row here
                                                            getActivity().getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValues);
                                                            if(userCursor != null) userCursor.close();
                                                        }
                                                        // insert individual group here
                                                        getActivity().getContentResolver().insert(YourTurnContract.GroupEntry.CONTENT_URI, groupValues);
                                                    }

                                                    if(groupCursor != null) groupCursor.close();
                                                    getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, GroupFragment.this);

                                                }else {
                                                    Log.d(TAG, e.getMessage());
                                                }
                                            }
                                        });

                                    }else {
                                        Log.d(TAG, "Record was already inserted");
                                    }

                                }else {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });
                    }
                }else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    private String getCurrentPhoneNumber(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
        return sharedPref.getString(ParseConstant.USERNAME_COLUMN, "");
    }
}
