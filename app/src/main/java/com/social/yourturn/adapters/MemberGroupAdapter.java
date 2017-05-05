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
import com.social.yourturn.utils.CircularImageView;

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
        String displayName = contact.getName();

        holder.imageView.setImageResource(R.drawable.default_profile);
        holder.nameTextView.setText(WordUtils.capitalize(displayName.toLowerCase(), null));
    }


    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder{
        TextView nameTextView;
        CircularImageView imageView;

        public MemberViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.member_name);
            imageView = (CircularImageView) itemView.findViewById(R.id.member_thumbnail);
        }
    }
}
