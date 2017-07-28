package com.social.yourturn.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.social.yourturn.ChatActivity;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ousma on 7/19/2017.
 */

public class LastMessageAdapter extends RecyclerView.Adapter<LastMessageAdapter.LastChatViewHolder>{

    private Context mContext;
    private List<Contact> mChatList;
    private static final String TAG = LastMessageAdapter.class.getSimpleName();

    public LastMessageAdapter(Context context, List<Contact> chatList){
        mContext = context;
        mChatList = chatList;
    }

    @Override
    public LastChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.latest_chats_layout, null);
        return new LastChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LastChatViewHolder holder, int position) {
        Contact contact = mChatList.get(position);
        holder.friendName.setText(WordUtils.capitalize(contact.getName().toLowerCase(), null));
        holder.chatMessageText.setText(contact.getLastMessage());
        if(contact.getThumbnailUrl() != null &&  contact.getThumbnailUrl().length() > 0) Glide.with(mContext).load(contact.getThumbnailUrl()).into(holder.friendUrlView);
        else holder.friendUrlView.setImageResource(R.drawable.default_profile);
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    private Contact getContact(int position){
        return mChatList.get(position);
    }

     public class LastChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
         CircleImageView friendUrlView;
         TextView friendName;
         TextView chatMessageText;

        public LastChatViewHolder(View itemView) {
            super(itemView);
            this.friendName = (TextView) itemView.findViewById(R.id.friend_name);
            this.chatMessageText = (TextView) itemView.findViewById(R.id.last_chat_txt);
            this.friendUrlView = (CircleImageView) itemView.findViewById(R.id.friendPic);
            itemView.setOnClickListener(this);
        }

         @Override
         public void onClick(View v) {
             int position = getLayoutPosition();
             Log.d(TAG, "position: " + position);
             Intent intent = new Intent(mContext, ChatActivity.class);
             Contact contact = getContact(position);
             intent.putExtra(mContext.getString(R.string.selected_contact), contact);
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
             mContext.startActivity(intent);
         }
     }

}
