package com.social.yourturn.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Message;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ousma on 7/5/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final String TAG = ChatAdapter.class.getSimpleName();

    private List<Message> mMessages;
    private Context mContext;
    private String mUserId;
    private Contact mFriend;

    public ChatAdapter(Context context, String userId, List<Message> messages, Contact friend) {
        mMessages = messages;
        this.mUserId = userId;
        mContext = context;
        mFriend = friend;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = inflater.inflate(R.layout.item_chat, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = mMessages.get(position);
        final boolean isMe = message.getUserId() != null && message.getUserId().equals(mUserId);

        if (isMe) {
            holder.imageOther.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else {
            holder.imageOther.setVisibility(View.VISIBLE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            if(mFriend.getThumbnailUrl() != null && mFriend.getThumbnailUrl().length() > 0) Glide.with(mContext).load(mFriend.getThumbnailUrl()).into(holder.imageOther);
        }

        holder.body.setText(message.getBody());

        Log.d(TAG, "sending messages");
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageOther;
        TextView body;

        public ViewHolder(View itemView) {
            super(itemView);
            imageOther = (CircleImageView)itemView.findViewById(R.id.profileOtherUrl);
            body = (TextView)itemView.findViewById(R.id.tvBody);
        }
    }

}
