package com.social.yourturn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.social.yourturn.R;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ImageLoader;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ousma on 6/10/2017.
 */

public class EventRecordAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Contact> mList;
    private ImageLoader imageLoader;

    public EventRecordAdapter(Context context, ArrayList<Contact> list){
        mContext = context;
        mList = list;
        imageLoader = new ImageLoader(mContext);
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
            holder.imageView = (CircleImageView) convertView.findViewById(R.id.eventUrl);
            holder.usernameView = (TextView) convertView.findViewById(R.id.username);
            holder.amountView = (TextView) convertView.findViewById(R.id.amount);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = getItem(position);

        imageLoader.DisplayImage(contact.getThumbnailUrl(), holder.imageView);
        holder.usernameView.setText(WordUtils.capitalize(contact.getName().toLowerCase(), null));
        if(contact.getScore() != null && !contact.getScore().equals(mContext.getString(R.string.zero_default_values))){
            DecimalFormat df = new DecimalFormat("#.00");
            double doubleValue = Double.parseDouble(contact.getScore());
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
