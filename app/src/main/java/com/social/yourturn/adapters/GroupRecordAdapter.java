package com.social.yourturn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.social.yourturn.R;
import com.social.yourturn.models.Contact;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ousma on 6/10/2017.
 */

public class GroupRecordAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Contact> mList;

    public GroupRecordAdapter(Context context, ArrayList<Contact> list){
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Contact getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView =  LayoutInflater.from(mContext).inflate(R.layout.record_layout, null);
            holder = new ViewHolder();
            holder.imageView = (CircleImageView) convertView.findViewById(R.id.thumbnail);
            holder.usernameView = (TextView) convertView.findViewById(R.id.username);
            holder.amountView = (TextView) convertView.findViewById(R.id.amount);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = getItem(position);

        holder.imageView.setImageResource(R.drawable.default_profile);
        holder.usernameView.setText(WordUtils.capitalize(contact.getName().toLowerCase(), null));
        if(contact.getShare() != null && !contact.getShare().equals(mContext.getString(R.string.zero_default_values))){
            DecimalFormat df = new DecimalFormat("#.00");
            double doubleValue = Double.parseDouble(contact.getShare());
            holder.amountView.setText(String.valueOf(df.format(doubleValue)));
        }

        return convertView;
    }

    static class ViewHolder {
        private CircleImageView imageView;
        private TextView usernameView;
        private TextView amountView;
    }

}
