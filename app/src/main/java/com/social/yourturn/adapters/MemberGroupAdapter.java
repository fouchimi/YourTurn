package com.social.yourturn.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
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

/**
 * Created by ousma on 4/22/2017.
 */

public class MemberGroupAdapter extends RecyclerView.Adapter<MemberGroupAdapter.MemberViewHolder> {

    private Context mContext;
    private ArrayList<Contact> mContactList;

    public MemberGroupAdapter(Context context, ArrayList<Contact> contactList){
        this.mContext = context;
        this.mContactList = contactList;
    }


    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.group_members, null);
        MemberViewHolder viewHolder = new MemberViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        Contact contact = mContactList.get(position);
        DrawableProvider mProvider = new DrawableProvider(mContext);
        String displayName = contact.getName();
        String initials = "";
        if(displayName.split(" ").length == 1) initials = displayName.substring(0, 2);
        else initials = WordUtils.initials(displayName).substring(0, 2);
        final Drawable drawable = mProvider.getRound(displayName, initials);
        holder.imageView.setImageDrawable(drawable);
        holder.nameTextView.setText(WordUtils.capitalize(displayName.toLowerCase(), null));
    }


    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder{
        TextView nameTextView;
        ImageView imageView;

        public MemberViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.member_name);
            imageView = (ImageView) itemView.findViewById(R.id.member_thumbnail);
        }
    }
}
