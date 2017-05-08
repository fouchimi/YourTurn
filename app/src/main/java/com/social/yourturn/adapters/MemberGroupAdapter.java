package com.social.yourturn.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.social.yourturn.R;
import com.social.yourturn.models.Contact;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

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
        CircleImageView imageView;
        EditText splitValueEditText;

        public MemberViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.member_name);
            imageView = (CircleImageView) itemView.findViewById(R.id.member_thumbnail);
            splitValueEditText = (EditText) itemView.findViewById(R.id.splitValue);
        }
    }
}
