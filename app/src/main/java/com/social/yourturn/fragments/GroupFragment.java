package com.social.yourturn.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.yourturn.R;
import com.social.yourturn.adapters.GroupAdapter;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;
import com.social.yourturn.models.Group;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    private static final String TAG = GroupFragment.class.getSimpleName();
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayout;
    private ParseUser mCurrentUser;
    private List<Contact> mContactList;
    private List<Group> mGroupList = new ArrayList<>();
    private GroupAdapter mGroupAdapter;
    private String phoneId="", phoneNumber="";

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
        mLinearLayout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayout);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(ParseUser.getCurrentUser() != null){
            mCurrentUser = ParseUser.getCurrentUser();
            fetchGroupList();
            Log.d(TAG, "Not logging in anymore");
        }else {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            phoneId = sharedPref.getString(ParseConstant.USERNAME, "");
            phoneNumber = sharedPref.getString(ParseConstant.PASSWORD, "");

            Log.d(TAG, "Phone ID from Shared Preferences: " + phoneId);
            Log.d(TAG,  "Phone Number from Shared Preferences: " + phoneNumber);

            if(!phoneId.equals("") && !phoneNumber.equals("")){
                ParseUser.logInInBackground(phoneId, phoneNumber, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(e == null){
                            mCurrentUser = user;
                            fetchGroupList();
                        }else {
                            Log.d(TAG, e.getMessage());
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

    }

    private void fetchGroupList(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.GROUP_TABLE);
        query.whereEqualTo(ParseConstant.CREATOR_COLUMN, mCurrentUser.getUsername());
        query.orderByDescending(ParseConstant.UPDATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> groupList, ParseException e) {
                if(e == null){
                    Log.d(TAG, "Size: " + groupList.size());
                    for(ParseObject contactObject : groupList){
                        mContactList = new ArrayList<Contact>();
                        String groupName = contactObject.getString(ParseConstant.GROUP_NAME);
                        String friendList = contactObject.getString(ParseConstant.MEMBERS_COLUMN);
                        String groupThumbnail = contactObject.getString(ParseConstant.THUMBNAIL_COLUMN);
                        String[] chunks = friendList.split(",");
                        for(String chunk : chunks){
                            String[] contactStringArray = chunk.split(" ");
                            Contact  contact = new Contact(contactStringArray[0], contactStringArray[1], contactStringArray[2]);
                            mContactList.add(contact);
                        }
                        Group group = new Group(groupName, mContactList);
                        mGroupList.add(group);
                        Log.d(TAG, friendList);
                    }

                    if(mGroupList.size() > 0){
                        mRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                        mGroupAdapter = new GroupAdapter(getActivity(), mGroupList);
                        mRecyclerView.setAdapter(mGroupAdapter);
                    }

                }else{
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }
}
