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
import com.social.yourturn.adapters.EventAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Event;
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
public class EventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EventFragment.class.getSimpleName();
    private TextView emptyTextView;
    private RecyclerView mRecyclerView;
    private ArrayList<Event> mEventList = new ArrayList<>();
    private EventAdapter mEventAdapter;
    public static final String GROUP_KEY = "Event";
    private static final int LOADER_ID = 0;
    private String groupThumbnail = "";

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
            return new CursorLoader(getActivity(), YourTurnContract.EventEntry.CONTENT_URI,
                    null, null, null, YourTurnContract.EventEntry.COLUMN_EVENT_UPDATED_DATE + " DESC");
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
