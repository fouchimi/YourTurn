package com.social.yourturn.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.social.yourturn.DrawableProvider;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.CircularImageView;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

/**
 * Created by ousma on 4/22/2017.
 */

public class MemberGroupAdapter extends RecyclerView.Adapter<MemberGroupAdapter.MemberViewHolder>{

    private static final String TAG = MemberGroupAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Contact> mContactList;

    public MemberGroupAdapter(Context context, ArrayList<Contact> contactList){
        this.mContext = context;
        this.mContactList = contactList;
    }


    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.group_members, null);
        final MemberViewHolder viewHolder = new MemberViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MemberViewHolder holder, final int position) {
        Contact contact = mContactList.get(position);
        String displayName = contact.getName();
        if(displayName != null) displayName = displayName.toLowerCase();
        holder.imageView.setImageResource(R.drawable.default_profile);
        holder.nameTextView.setText(WordUtils.capitalize(displayName, null));
        if(contact.getShare() == null || contact.getShare().length() == 0) {
            holder.splitValueEditText.setText(mContext.getString(R.string.zero_default_values));
        }else {
            holder.splitValueEditText.setText(contact.getShare());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }


    public class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        CircularImageView imageView;
        EditText splitValueEditText;

        public MemberViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.member_name);
            imageView = (CircularImageView) itemView.findViewById(R.id.member_thumbnail);
            splitValueEditText = (EditText) itemView.findViewById(R.id.splitValue);
        }
    }
}
