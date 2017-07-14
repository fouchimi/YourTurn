package com.social.yourturn.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.social.yourturn.R;
import com.social.yourturn.models.Message;
import com.social.yourturn.utils.DateUtils;
import com.social.yourturn.utils.ParseConstant;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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

    public ChatAdapter(Context context,  List<Message> messages) {
        mMessages = messages;
        mContext = context;
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

        if (holder.getItemViewType() == TYPE_1) holder.senderLayout.setVisibility(View.VISIBLE);
        else holder.receiverLayout.setVisibility(View.VISIBLE);

        if(message.isFirstOfTheDay()) {
            holder.dateHeaderLayout.setVisibility(View.VISIBLE);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("EEEE", Locale.getDefault());

            if(DateUtils.isSameDay(message.getCreatedDateKey())){
                holder.chatScreenDay.setText(R.string.todayText);
                holder.chatScreenDate.setText(simpleDateFormat.format(message.getCreatedDateKey()));
            }else {
                holder.chatScreenDay.setText(simpleDateFormatDay.format(message.getCreatedDateKey()));
                holder.chatScreenDate.setText(simpleDateFormat.format(message.getCreatedDateKey()));
            }
        }

        holder.message.setText(message.getBody());
        holder.createdTime.setText(DateUtils.getFormattedDate(message.getCreatedDateKey()));

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

        TextView message, createdTime, chatScreenDay, chatScreenDate;
        CardView cardView;
        RelativeLayout senderLayout, receiverLayout;
        LinearLayout dateHeaderLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.message_cardview);
            message = (TextView) itemView.findViewById(R.id.message);
            createdTime = (TextView) itemView.findViewById(R.id.createdAtTime);
            senderLayout = (RelativeLayout) itemView.findViewById(R.id.sender_layout);
            receiverLayout = (RelativeLayout) itemView.findViewById(R.id.receiver_layout);
            dateHeaderLayout = (LinearLayout) itemView.findViewById(R.id.dateHeaderLayout);
            chatScreenDay = (TextView) itemView.findViewById(R.id.chat_screen_day);
            chatScreenDate = (TextView) itemView.findViewById(R.id.chat_screen_date);
        }
    }

    private String getUsername() {
        SharedPreferences shared = mContext.getSharedPreferences(mContext.getString(R.string.user_credentials), MODE_PRIVATE);
        return (shared.getString(ParseConstant.USERNAME_COLUMN, ""));
    }

}
