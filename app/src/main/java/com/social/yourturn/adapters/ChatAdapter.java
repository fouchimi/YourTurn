package com.social.yourturn.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Message;
import com.social.yourturn.utils.ParseConstant;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ousma on 7/5/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final String TAG = ChatAdapter.class.getSimpleName();

    private static final int TYPE_1 = 0;
    private static final int TYPE_2 = 1;
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
        View contactView = null;
        if(viewType == TYPE_1){
            contactView = inflater.inflate(R.layout.chat_item_sent, parent, false);
        }else {
            contactView = inflater.inflate(R.layout.chat_item_rec, parent, false);
        }
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = mMessages.get(position);

        if (holder.getItemViewType() == TYPE_1) {
            holder.senderLayout.setVisibility(View.VISIBLE);
            holder.message.setText(message.getBody());
        } else {
            holder.receiverLayout.setVisibility(View.VISIBLE);
            holder.message.setText(message.getBody());
        }


        Log.d(TAG, "sending messages");
    }

    @Override
    public int getItemViewType(int position) {
        if(mMessages.get(position).getSenderKey().equals(getUsername())) return TYPE_1;
        return TYPE_2;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //CircleImageView imageOther;
        TextView message;
        CardView cardView;
        RelativeLayout senderLayout, receiverLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            //imageOther = (CircleImageView) itemView.findViewById(R.id.profileOtherUrl);
            cardView = (CardView) itemView.findViewById(R.id.message_cardview);
            message = (TextView) itemView.findViewById(R.id.message);
            senderLayout = (RelativeLayout) itemView.findViewById(R.id.sender_layout);
            receiverLayout = (RelativeLayout) itemView.findViewById(R.id.rec_layout);
        }
    }

    private String getUsername() {
        SharedPreferences shared = mContext.getSharedPreferences(mContext.getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }

}
