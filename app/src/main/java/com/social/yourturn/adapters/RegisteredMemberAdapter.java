package com.social.yourturn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ousma on 7/4/2017.
 */

public class RegisteredMemberAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Contact> memberList;

    public RegisteredMemberAdapter(Context context, ArrayList<Contact> list){
        mContext = context;
        memberList = list;
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Contact getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        RegisterMemberViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.register_members_layout, null);
            holder = new RegisterMemberViewHolder();
            holder.profileUrlView = (CircleImageView) convertView.findViewById(R.id.userUrl);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.score = (TextView) convertView.findViewById(R.id.score);
            holder.scoreText = (TextView) convertView.findViewById(R.id.scoreText);
        }else {
            holder = (RegisterMemberViewHolder) convertView.getTag();
        }

        Contact contact = getItem(position);

        if(contact.getThumbnailUrl() != null) Glide.with(mContext).load(contact.getThumbnailUrl()).into(holder.profileUrlView);
        holder.username.setText(WordUtils.capitalize(contact.getName().toLowerCase(), null));

        DecimalFormat df = new DecimalFormat("0.00");
        holder.score.setText(mContext.getString(R.string.scoreValue, df.format(Double.parseDouble(contact.getScore()))));
        holder.scoreText.setText(R.string.scoreText);

        return convertView;
    }

    static class RegisterMemberViewHolder {
        private CircleImageView profileUrlView;
        private TextView username, score, scoreText;
    }
}
