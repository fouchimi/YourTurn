package com.social.yourturn.fragments;


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
import com.social.yourturn.adapters.LastMessageAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView emptyTextView;
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final int LOADER_ID = 35;
    private RecyclerView mRecyclerView;
    private List<Contact> chatList = new ArrayList<>();
    private LastMessageAdapter lastMessageAdapter;

    public ChatFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_latest_update, container, false);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        emptyTextView = (TextView) view.findViewById(R.id.empty_view);
        emptyTextView.setTypeface(typeface);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.latest_chats);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        chatList = new ArrayList<>();
        lastMessageAdapter = new LastMessageAdapter(getContext(), chatList);
        mRecyclerView.setAdapter(lastMessageAdapter);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == LOADER_ID){
            return new CursorLoader(getActivity(), YourTurnContract.RecentMessageEntry.CONTENT_URI, null, null, null,
                    YourTurnContract.RecentMessageEntry.COLUMN_MESSAGE_UPDATED_DATE + " DESC");
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

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void loadData(Cursor data){

        Contact contact = null;
        chatList.clear();
        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String lastMessage = data.getString(data.getColumnIndex(YourTurnContract.RecentMessageEntry.COLUMN_MESSAGE_BODY));
            String senderId = data.getString(data.getColumnIndex(YourTurnContract.RecentMessageEntry.COLUMN_MESSAGE_USER_KEY));
            String receiverId = data.getString(data.getColumnIndex(YourTurnContract.RecentMessageEntry.COLUMN_MESSAGE_RECEIVER_KEY));
            long createdDate = data.getLong(data.getColumnIndex(YourTurnContract.RecentMessageEntry.COLUMN_MESSAGE_CREATED_DATE));
            long updatedDate = data.getLong(data.getColumnIndex(YourTurnContract.RecentMessageEntry.COLUMN_MESSAGE_UPDATED_DATE));

            if(senderId.equals(getUsername()) && receiverId.equals(getUsername())) {
                contact = getContact(getUsername());
            }else if(!senderId.equals(getUsername()) && receiverId.equals(getUsername())){
                contact = getContact(senderId);
            }else if(senderId.equals(getUsername()) && !receiverId.equals(getUsername())){
                contact = getContact(receiverId);
            }
            if(contact != null){

                contact.setLastMessage(lastMessage);
                contact.setCreatedDate(createdDate);
                contact.setUpdatedDate(updatedDate);

                chatList.add(contact);
            }
        }

        if(chatList.size() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
            lastMessageAdapter.notifyDataSetChanged();
        }
    }

    private String getUsername() {
        SharedPreferences shared = getActivity().getSharedPreferences(getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }

    private Contact getContact(String contactId){

        Contact contact = new Contact();
        Cursor cursor = getActivity().getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI,
                new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME,
                        YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL,
                        YourTurnContract.MemberEntry.COLUMN_MEMBER_SCORE,
                        YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER},
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?",
                new String[]{contactId}, null);

        if(cursor != null && cursor.getCount() > 0){
            cursor.moveToFirst();
            contact.setName(cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME)));
            contact.setThumbnailUrl(cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL)));
            contact.setScore(cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_SCORE)));
            contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER)));
        }

        cursor.close();

        return contact;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
