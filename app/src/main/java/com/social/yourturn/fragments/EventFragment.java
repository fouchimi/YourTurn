package com.social.yourturn.fragments;


import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.social.yourturn.R;
import com.social.yourturn.adapters.EventAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Event;
import com.social.yourturn.utils.ParseConstant;


import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EventFragment.class.getSimpleName();
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private ArrayList<Event> mEventList = new ArrayList<>();
    private EventAdapter mEventAdapter;
    public static final String GROUP_KEY = "Event";
    private static final int LOADER_ID = 0;
    private String eventUrl = "";

    public EventFragment() {
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
        mEventList = new ArrayList<>();
        mEventAdapter = new EventAdapter(getActivity(), mEventList, mRecyclerView);
        mRecyclerView.setAdapter(mEventAdapter);
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
            if(mEventList.isEmpty())
            return new CursorLoader(getActivity(), YourTurnContract.EventEntry.CONTENT_URI,
                    null, null, null, YourTurnContract.EventEntry.COLUMN_EVENT_UPDATED_DATE + " DESC");
            else {
                // Fetch latest group event with flag of 1
                return new CursorLoader(getActivity(), YourTurnContract.EventEntry.CONTENT_URI,
                        null, YourTurnContract.EventEntry.COLUMN_EVENT_FLAG + "=?", new String[]{"1"},
                        YourTurnContract.EventEntry.COLUMN_EVENT_UPDATED_DATE + " DESC");
            }
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
        String eventId, eventName, eventUrl, eventCreator, userId, lastEventId="";

        Event event = null;

        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            eventId = data.getString(data.getColumnIndex(YourTurnContract.EventEntry.COLUMN_EVENT_ID));
            eventName = data.getString(data.getColumnIndex(YourTurnContract.EventEntry.COLUMN_EVENT_NAME));
            eventUrl = data.getString(data.getColumnIndex(YourTurnContract.EventEntry.COLUMN_EVENT_URL));
            eventCreator = data.getString(data.getColumnIndex(YourTurnContract.EventEntry.COLUMN_EVENT_CREATOR));
            userId = data.getString(data.getColumnIndex(YourTurnContract.EventEntry.COLUMN_USER_KEY));

            Cursor userCursor = getActivity().getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI, null,
                    YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?", new String[]{userId}, null);

            if(userCursor != null && userCursor.getCount() > 0){
                userCursor.moveToFirst();
                String contactId = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.MemberEntry._ID));
                String username = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME));
                String phoneNumber = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER));
                String thumbnail = userCursor.getString(userCursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL));

                if(lastEventId.isEmpty() || eventId.equals(lastEventId)) {
                    lastEventId = eventId;
                }else {
                    lastEventId = eventId;
                    contactList = new ArrayList<>();
                }
                addContact(contactId, username, phoneNumber, thumbnail, contactList);
                userCursor.close();
            }

            if(mEventList.isEmpty()){
                event = new Event();
                event.setEventId(eventId);
                event.setName(eventName);
                event.setEventUrl(eventUrl);
                event.setGroupCreator(eventCreator);
                event.setGroupUserRef(userId);
                event.setContactList(contactList);
                mEventList.add(event);
            }else if (mEventList.size() > 0 && mEventList.get(mEventList.size()-1).getEventId().equals(eventId)) continue;
            else {
                event = new Event();
                event.setEventId(eventId);
                event.setName(eventName);
                event.setEventUrl(eventUrl);
                event.setGroupCreator(eventCreator);
                event.setGroupUserRef(userId);
                event.setContactList(contactList);
                mEventList.add(event);
            }

        }

        if(mEventList.size() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
            mEventAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    private String getUsername(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_credentials), Context.MODE_PRIVATE);
        return sharedPref.getString(ParseConstant.USERNAME_COLUMN, "");
    }
}
