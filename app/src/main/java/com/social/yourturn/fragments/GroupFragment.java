package com.social.yourturn.fragments;


import android.content.Intent;
import android.database.Cursor;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.social.yourturn.GroupListActivity;
import com.social.yourturn.R;
import com.social.yourturn.adapters.GroupAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Group;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = GroupFragment.class.getSimpleName();
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayout;
    private ArrayList<Contact> mContactList = new ArrayList<>();
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
        mLinearLayout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayout);

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if(child != null){
                    int position = mRecyclerView.getChildAdapterPosition(child);
                    Log.d(TAG, "Position: " + position);
                    mGroupAdapter = (GroupAdapter) mRecyclerView.getAdapter();
                    Group group = mGroupAdapter.getGroup(position);
                    Intent intent = new Intent(getActivity(), GroupListActivity.class);
                    intent.putExtra(GroupFragment.GROUP_KEY, group);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == LOADER_ID){
            return new CursorLoader(getActivity(), YourTurnContract.GroupEntry.CONTENT_URI, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_ID){
            if(data == null){
                Log.d(TAG, "Empty cursor");
            }else {
                List<String> groupNameList = new ArrayList<>();
                Group mGroup = null;
                boolean flag = true;
                while (data.moveToNext()){
                    String groupName = data.getString(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_GROUP_NAME));
                    String groupThumbnail = data.getString(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_GROUP_THUMBNAIL));
                    String userId = data.getString(data.getColumnIndex(YourTurnContract.GroupEntry.COLUMN_USER_KEY));
                    Cursor cursor = getActivity().getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null, YourTurnContract.UserEntry.COLUMN_USER_ID + " = " + userId, null, null);
                    cursor.moveToNext();
                    String username = cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_NAME));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER));
                    Contact contact = new Contact(userId, username, phoneNumber);
                    mContactList.add(contact);
                    if(!groupNameList.contains(groupName) && flag){
                        groupNameList.add(groupName);
                        mGroup = new Group();
                        mGroup.setName(groupName);
                        if(groupThumbnail.length() > 0) {
                            mGroup.setThumbnail(groupThumbnail);
                        }
                        flag = false;
                    }else if(groupNameList.contains(groupName) && flag == false){
                        continue;
                    }else if(!groupNameList.contains(groupName) && flag == false){
                        groupNameList.add(groupName);
                        mGroup.setContactList(mContactList);
                        mGroupList.add(mGroup);
                        mContactList = new ArrayList<>();
                        mGroup = new Group();
                        mGroup.setName(groupName);
                        if(groupThumbnail.length() > 0) {
                            mGroup.setThumbnail(groupThumbnail);
                        }
                    }
                }
                if(mContactList.size() > 0){
                    mGroup.setContactList(mContactList);
                    mGroupList.add(mGroup);
                }

                if(mGroupList.size() > 0){
                    mRecyclerView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                    mGroupAdapter = new GroupAdapter(getActivity(), mGroupList);
                    mRecyclerView.setAdapter(mGroupAdapter);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
