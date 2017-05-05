package com.social.yourturn.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.social.yourturn.R;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.CircularImageView;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;


/**
 * Created by ousma on 4/18/2017.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder>{

    private final static String TAG = CustomAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Contact> mContactList;

    public CustomAdapter(Context context, ArrayList<Contact> cursorList){
        this.mContext = context;
        mContactList = cursorList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.group_item_layout, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Contact contact = mContactList.get(position);
        final String displayName = contact.getName();

        Log.d(TAG, String.valueOf(position));

        holder.thumbnailView.setImageResource(R.drawable.default_profile);
        holder.usernameView.setText(WordUtils.capitalize(displayName.toLowerCase(), null));
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        public CircularImageView thumbnailView;
        public TextView usernameView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, String.valueOf(itemView.getX()));
            Log.d(TAG, String.valueOf(itemView.getY()));
            this.thumbnailView = (CircularImageView) itemView.findViewById(R.id.thumbnail);
            this.usernameView = (TextView) itemView.findViewById(R.id.username);
        }
    }
}
