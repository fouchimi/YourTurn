package com.social.yourturn.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Event;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ousma on 7/1/2017.
 */

public class EventMemberAdapter extends BaseAdapter {

    private static final String TAG = EventMemberAdapter.class.getSimpleName();
    private Context mContext;
    private Event mEvent;
    private ArrayList<Contact> mEventMemberList;

    public EventMemberAdapter(Context context, Event event, ArrayList<Contact> members){
        mContext = context;
        mEvent = event;
        mEventMemberList = members;
    }

    @Override
    public Contact getItem(int position) {
        return mEventMemberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return mEventMemberList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        EventMemberViewHolder holder;

        if (convertView == null) {
            convertView =  LayoutInflater.from(mContext).inflate(R.layout.event_member_layout, null);
            holder = new EventMemberViewHolder();
            holder.userUrlView = (CircleImageView) convertView.findViewById(R.id.userUrl);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.score = (TextView) convertView.findViewById(R.id.score);
            holder.requestedText = (TextView) convertView.findViewById(R.id.requestText);
            holder.paidText = (TextView) convertView.findViewById(R.id.paidText);
            convertView.setTag(holder);
        }
        else {
            holder = (EventMemberViewHolder) convertView.getTag();
        }

        Contact contact = getItem(position);

        holder.username.setText(WordUtils.capitalize(contact.getName().toLowerCase(), null));

        if(contact.getThumbnailUrl() != null && contact.getThumbnailUrl().length() > 0) Glide.with(mContext).load(contact.getThumbnailUrl()).into(holder.userUrlView);

        Cursor ledgerCursor = mContext.getContentResolver().query(YourTurnContract.LedgerEntry.CONTENT_URI,
                new String[]{YourTurnContract.LedgerEntry.COLUMN_USER_REQUEST, YourTurnContract.LedgerEntry.COLUMN_USER_PAID},
                YourTurnContract.LedgerEntry.COLUMN_EVENT_KEY + "=?" + " AND " + YourTurnContract.LedgerEntry.COLUMN_USER_KEY + "=?",
                new String[]{mEvent.getEventId(), contact.getPhoneNumber()}, null);

        DecimalFormat df = new DecimalFormat("#.00");

        if(ledgerCursor != null && ledgerCursor.getCount() > 0) {
            ledgerCursor.moveToFirst();

            String requestValue = ledgerCursor.getString(ledgerCursor.getColumnIndex(YourTurnContract.LedgerEntry.COLUMN_USER_REQUEST));
            String paidValue = ledgerCursor.getString(ledgerCursor.getColumnIndex(YourTurnContract.LedgerEntry.COLUMN_USER_PAID));

            holder.requestedText.setText(mContext.getString(R.string.requestedText, df.format(Double.parseDouble(requestValue))));
            holder.paidText.setText(mContext.getString(R.string.paidText, df.format(Double.parseDouble(paidValue))));
        }

        ledgerCursor.close();

        if(contact.getScore() != null) holder.score.setText(mContext.getString(R.string.scoreValue, df.format(Double.parseDouble(contact.getScore()))));

        return convertView;
    }


    static class EventMemberViewHolder {
        private CircleImageView userUrlView;
        private TextView username, score;
        private TextView requestedText, paidText;
    }
}
