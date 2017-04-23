package com.social.yourturn.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.social.yourturn.DrawableProvider;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

        DrawableProvider mProvider = new DrawableProvider(mContext);
        String initials = "";
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(displayName);
        if(m.find()){
            if(displayName.split(" ").length == 1) initials = displayName.substring(0, 2);
            else initials = WordUtils.initials(displayName).substring(0, 2);
            final Drawable drawable = mProvider.getRound(displayName, initials);
            holder.thumbnailView.setImageDrawable(drawable);
            holder.usernameView.setText(WordUtils.capitalize(displayName.toLowerCase(), null));
        }else {
            Pattern pattern = Pattern.compile("[0-9]");
            Matcher matcher = pattern.matcher(displayName);
            if(matcher.find()){
                if(displayName.startsWith("+")){
                    initials = displayName.substring(1, 3);
                }else {
                    initials = displayName.substring(0, 2);
                }
                final Drawable drawable = mProvider.getRound(displayName, initials);
                holder.thumbnailView.setImageDrawable(drawable);
                holder.usernameView.setText(displayName);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView usernameView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, String.valueOf(itemView.getX()));
            Log.d(TAG, String.valueOf(itemView.getY()));
            this.thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail);
            this.usernameView = (TextView) itemView.findViewById(R.id.username);
        }
    }
}
