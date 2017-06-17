package com.social.yourturn.fragments;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.yourturn.R;
import com.social.yourturn.adapters.GroupAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Group;
import com.social.yourturn.services.UpdateNameService;
import com.social.yourturn.utils.ParseConstant;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;


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
    private String groupThumbnail = "";

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

    private void addContact(String contactId, String name, String number, String thumbnail, ArrayList<Contact> list) {
        if(contactId == null) contactId = String.valueOf(0);
        if(name == null) name = getString(R.string.current_user);
        Contact contact = new Contact(contactId, name, number);
        if(thumbnail != null) contact.setThumbnailUrl(thumbnail);
        list.add(contact);
    }

    private void loadData(Cursor data) {
        ArrayList<Contact> contactList = new ArrayList<>();
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
                    YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=?", new String[]{userId}, null);

            if(userCursor != null && userCursor.getCount() > 0){
                userCursor.moveToFirst();
                String contactId = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_ID));
                String username = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME));
                String phoneNumber = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER));
                String thumbnail = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL));

                if(lastGroupId.isEmpty() || groupId.equals(lastGroupId)) {
                    lastGroupId = groupId;
                }else {
                    lastGroupId = groupId;
                    contactList = new ArrayList<>();
                }
                addContact(contactId, username, phoneNumber, thumbnail, contactList);
                userCursor.close();

            }else if(userCursor != null && userCursor.getCount() <= 0) {

                ContentValues userValues = new ContentValues();
                DateTime dayTime = new DateTime();

                Cursor memberCursor = getActivity().getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, null,
                        YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(userId), null, null);

                if(memberCursor != null && memberCursor.getCount() > 0) {
                    memberCursor.moveToNext();
                    String contactId = memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry._ID));
                    String username = memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
                    String phoneNumber = memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER));
                    String thumbnail = memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL));

                    if(lastGroupId.isEmpty() || groupId.equals(lastGroupId)) {
                        lastGroupId = groupId;
                    }else {
                        lastGroupId = groupId;
                        contactList = new ArrayList<>();
                    }
                    addContact(contactId, username, phoneNumber, thumbnail, contactList);

                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER, phoneNumber);
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_ID, contactId);
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, username);
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, dayTime.getMillis());
                    userValues.put(YourTurnContract.UserEntry.COLUMN_USER_UPDATED_DATE, dayTime.getMillis());

                    getActivity().getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValues);
                    memberCursor.close();
                }
            }

            if(mGroupList.isEmpty()){
                group = new Group();
                group.setGroupId(groupId);
                group.setName(groupName);
                group.setThumbnail(groupThumbnail);
                group.setGroupCreator(groupCreator);
                group.setDateInMillis(dateInMillis);
                group.setGroupUserRef(userId);
                group.setContactList(contactList);
                mGroupList.add(group);
            }else if (mGroupList.size() > 0 && mGroupList.get(mGroupList.size()-1).getGroupId().equals(groupId)) {
                continue;
            }
            else {
                group = new Group();
                group.setGroupId(groupId);
                group.setName(groupName);
                group.setThumbnail(groupThumbnail);
                group.setGroupCreator(groupCreator);
                group.setDateInMillis(dateInMillis);
                group.setGroupUserRef(userId);
                group.setContactList(contactList);
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
        fetchFriendThumbnail();
        updateName();
    }

    private synchronized void updateName() {
        Cursor userCursor = getActivity().getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null, null, null, null);
        if(userCursor != null && userCursor.getCount() > 0) {
            ArrayList<Contact> list = new ArrayList<>();
            while (userCursor.moveToNext()){
                String id = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry._ID));
                String name = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME));
                String number = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER));
                list.add(new Contact(id, name, number));
            }
            Intent intent = new Intent(getActivity(), UpdateNameService.class);
            intent.putParcelableArrayListExtra(getString(R.string.contact_list), list);
            getActivity().startService(intent);
        }
    }

    private void fetchFriendThumbnail(){
        Cursor memberCursor = getActivity().getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, null, null, null, null);
        if(memberCursor != null && memberCursor.getCount() > 0) {
            while (memberCursor.moveToNext()) {
                final String userId = memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry._ID));
                final String phoneNumber = memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER));
                final String name = memberCursor.getString(memberCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo(ParseConstant.USERNAME_COLUMN, phoneNumber);

                query.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if(e == null) {
                            ParseFile parseFile = (ParseFile) parseUser.get(ParseConstant.USER_THUMBNAIL_COLUMN);
                            if(parseFile != null) {
                                ContentValues imageValue = new ContentValues();
                                imageValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL, parseFile.getUrl());

                                getActivity().getContentResolver().update(YourTurnContract.MemberEntry.CONTENT_URI, imageValue,
                                        YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(phoneNumber), null);

                                Cursor userCursor = getActivity().getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                                        YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER +"=" +DatabaseUtils.sqlEscapeString(phoneNumber), null, null);

                                ContentValues userValue = new ContentValues();
                                userValue.put(YourTurnContract.UserEntry.COLUMN_USER_ID, userId);
                                userValue.put(YourTurnContract.UserEntry.COLUMN_USER_NAME, name);
                                userValue.put(YourTurnContract.UserEntry.COLUMN_USER_THUMBNAIL, parseFile.getUrl());
                                if(userCursor != null && userCursor.getCount() > 0) {
                                    getActivity().getContentResolver().update(YourTurnContract.UserEntry.CONTENT_URI, userValue,
                                            YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + "=" + DatabaseUtils.sqlEscapeString(phoneNumber), null);
                                }else {
                                   // insert here
                                    getActivity().getContentResolver().insert(YourTurnContract.UserEntry.CONTENT_URI, userValue);
                                }
                                userCursor.close();
                            }
                        }else {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                });
            }
        }
        memberCursor.close();
    }

    public Task<List<ParseObject>> fetchGroupsAsync(ParseQuery<ParseObject> query) {
        final TaskCompletionSource<List<ParseObject>> tcs = new TaskCompletionSource<>();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> groups, ParseException e) {
                if (e == null) {
                    tcs.setResult(groups);
                } else {
                    tcs.setError(e);
                }
            }
        });
        return tcs.getTask();
    }

    private Task<ParseObject> fetchGroupAsync(ParseQuery<ParseObject> query, String groupId) {
        final TaskCompletionSource<ParseObject> tcs = new TaskCompletionSource<>();
        query.getInBackground(groupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject row, ParseException e) {
                if(e == null){
                    tcs.setResult(row);
                }else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }


    private void fetchLatestGroup(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.GROUP_MEMBER_TABLE);
        query.whereEqualTo(ParseConstant.USER_ID_COLUMN, getCurrentPhoneNumber());

        fetchGroupsAsync(query).onSuccessTask(new Continuation<List<ParseObject>, Task<ParseObject>>() {
            @Override
            public Task<ParseObject> then(Task<List<ParseObject>> task) throws Exception {
                final ParseQuery<ParseObject> rowQuery = ParseQuery.getQuery(ParseConstant.GROUP_TABLE);
                List<ParseObject> results = task.getResult();
                Task<ParseObject> baseTask = Task.forResult(null);
                for(ParseObject result : results) {
                    final String id = result.getString(ParseConstant.GROUP_MEMBER_TABLE_ID);
                    baseTask = baseTask.continueWithTask(new Continuation<ParseObject, Task<ParseObject>>() {
                        public Task<ParseObject> then(Task<ParseObject> ignored) throws Exception {
                            return fetchGroupAsync(rowQuery, id);
                        }
                    });
                }
                return baseTask;
            }
        }).onSuccess(new Continuation<ParseObject, Task<Void>>() {
            @Override
            public Task<Void> then(Task<ParseObject> task) throws Exception {
                ParseObject groupRow = task.getResult();
                String groupId = groupRow.getObjectId();
                String groupName = groupRow.getString(ParseConstant.GROUP_NAME);
                ParseFile groupImage = (ParseFile) groupRow.get(ParseConstant.GROUP_THUMBNAIL_COLUMN);
                if(groupImage != null) groupThumbnail = groupImage.getUrl();
                String groupCreator  = groupRow.getString(ParseConstant.USER_ID_COLUMN);
                String members = groupRow.getString(ParseConstant.MEMBERS_COLUMN);

                String[] chunks = members.split(",");
                ArrayList<Contact> membersList = new ArrayList<>();
                for(String member : chunks) {
                    String[] contactChunks = member.split(":");
                    Contact contact = new Contact(contactChunks[0], contactChunks[1], contactChunks[2]);
                    membersList.add(contact);
                }

                for(Contact contact : membersList){
                    final Cursor groupCursor = getActivity().getContentResolver().query(YourTurnContract.GroupEntry.CONTENT_URI, null,
                            YourTurnContract.GroupEntry.COLUMN_USER_KEY + " = " +
                                    DatabaseUtils.sqlEscapeString(contact.getPhoneNumber()) + " AND " +
                                    YourTurnContract.GroupEntry.COLUMN_GROUP_ID + " = " +
                                    DatabaseUtils.sqlEscapeString(groupId), null, null);

                    DateTime dayTime = new DateTime();
                    if(groupCursor != null && groupCursor.getCount() <=0) {
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
                            userCursor.close();
                        }
                        // insert individual group here
                        getActivity().getContentResolver().insert(YourTurnContract.GroupEntry.CONTENT_URI, groupValues);
                    }
                }
                return null;
            }
        });

    }

    private String getCurrentPhoneNumber(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
        return sharedPref.getString(ParseConstant.USERNAME_COLUMN, "");
    }
}
