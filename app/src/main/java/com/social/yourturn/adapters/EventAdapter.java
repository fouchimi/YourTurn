package com.social.yourturn.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.social.yourturn.EventMemberActivity;
import com.social.yourturn.GroupListActivity;
import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.fragments.EventFragment;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Event;
import com.social.yourturn.utils.ParseConstant;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by ousma on 4/21/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.GroupViewHolder> {

    private final static String TAG = EventAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Event> mEventList;
    private final View.OnClickListener mOnClickListener = new MyOnClickListener();
    private RecyclerView mRecyclerView;

    public EventAdapter(Context context, ArrayList<Event> eventList, RecyclerView rv){
        mContext = context;
        mEventList = eventList;
        mRecyclerView = rv;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.frag_group_item_layout, null);
        view.setOnClickListener(mOnClickListener);
        GroupViewHolder viewHolder = new GroupViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, int position) {
        Event event = mEventList.get(position);
        Log.d(TAG, event.getName());
        holder.eventName.setText(event.getName());

        String[] selectionArgs = {event.getEventId()};

        Cursor eventCursor = mContext.getContentResolver().query(YourTurnContract.EventEntry.CONTENT_URI, null,
                YourTurnContract.EventEntry.COLUMN_EVENT_ID + "=?", selectionArgs, null);

        holder.eventNumber.setText(String.valueOf(eventCursor.getCount()));

        eventCursor.close();

        Cursor ledgerCursor = mContext.getContentResolver().query(YourTurnContract.LedgerEntry.CONTENT_URI,
                new String[]{YourTurnContract.LedgerEntry.COLUMN_USER_REQUEST, YourTurnContract.LedgerEntry.COLUMN_USER_PAID},
                YourTurnContract.LedgerEntry.COLUMN_EVENT_KEY + "=?" + " AND " + YourTurnContract.LedgerEntry.COLUMN_USER_KEY + "=?",
                new String[]{event.getEventId(), getUsername()}, null);

        if(ledgerCursor != null && ledgerCursor.getCount() > 0) {
            ledgerCursor.moveToFirst();

            String requestValue = ledgerCursor.getString(ledgerCursor.getColumnIndex(YourTurnContract.LedgerEntry.COLUMN_USER_REQUEST));
            String paidValue = ledgerCursor.getString(ledgerCursor.getColumnIndex(YourTurnContract.LedgerEntry.COLUMN_USER_PAID));

            Log.d(TAG, "request Value: " + requestValue);
            Log.d(TAG, "paid Value: " + paidValue);

            holder.requestedText.setText(mContext.getString(R.string.requestedText, requestValue));
            holder.paidText.setText(mContext.getString(R.string.paidText, paidValue));
        }else {
            Log.d(TAG, "Empty ledger cursor");
        }

        ledgerCursor.close();

        Glide.with(mContext).load(event.getEventUrl()).into(holder.eventUrlView);

    }

    public Event getEvent(int position){
        return mEventList.get(position);
    }

    @Override
    public int getItemCount() {
        return mEventList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView eventUrlView;
        public TextView eventName;
        public TextView eventNumber;
        public TextView requestedText, paidText;

        public GroupViewHolder(View itemView) {
            super(itemView);
            this.eventName = (TextView) itemView.findViewById(R.id.group_name);
            this.eventNumber = (TextView) itemView.findViewById(R.id.group_number);
            this.eventUrlView = (CircleImageView) itemView.findViewById(R.id.group_thumbnail);
            this.requestedText = (TextView) itemView.findViewById(R.id.requestText);
            this.paidText = (TextView) itemView.findViewById(R.id.paidText);
        }
    }

    public class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, EventMemberActivity.class);
            int itemPosition = mRecyclerView.getChildLayoutPosition(v);
            Event event = getEvent(itemPosition);

            Contact creatorContact = null;
            boolean creatorFound = false;
            for(Contact contact : event.getContactList()){
                if(contact.getPhoneNumber().equals(getUsername())) {
                    creatorContact = contact;
                    creatorFound = true;
                    break;
                }
            }
            if(!creatorFound){
                Contact contact = new Contact();
                contact.setName(mContext.getString(R.string.current_user));
                contact.setPhoneNumber(getUsername());
                contact.setThumbnailUrl(getProfilePic());
                event.getContactList().add(contact);
            }else {
                creatorContact.setName(mContext.getString(R.string.current_user));
                creatorContact.setThumbnailUrl(getProfilePic());
            }

            intent.putExtra(EventFragment.EVENT_KEY, event);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }

    }

    private String getProfilePic(){
        SharedPreferences shared = mContext.getSharedPreferences(mContext.getString(R.string.profile_path), Context.MODE_PRIVATE);
        return (shared.getString(ParseConstant.USER_THUMBNAIL_COLUMN, ""));
    }

    private String getUsername(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.user_credentials), Context.MODE_PRIVATE);
        return  sharedPref.getString(ParseConstant.USERNAME_COLUMN, "");
    }
}
