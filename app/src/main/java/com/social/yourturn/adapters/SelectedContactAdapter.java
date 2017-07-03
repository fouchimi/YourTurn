package com.social.yourturn.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.social.yourturn.ContactActivity;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ImageLoader;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by ousma on 4/15/2017.
 */

public class SelectedContactAdapter extends RecyclerView.Adapter<SelectedContactAdapter.CustomViewHolder> {

    private ContactActivity mContext;
    private ArrayList<Contact> mContactList;
    private final static String TAG = SelectedContactAdapter.class.getSimpleName();

    public SelectedContactAdapter(ContactActivity context, ArrayList<Contact> contactList){
        this.mContext = context;
        this.mContactList = contactList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
        final CustomViewHolder viewHolder = new CustomViewHolder(view);

        Log.d(TAG, "");
        viewHolder.deleteIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = viewHolder.usernameView.getText().toString();
                int index = -1;
                for(int i=0; i < mContactList.size(); i++){
                    if(mContactList.get(i).getName().toLowerCase().equals(username.toLowerCase())){
                        index = i;
                        break;
                    }
                }
                if(index > -1) {
                    mContext.onUpdateContactList(mContactList.get(index));
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Contact contact = mContactList.get(position);
        if(contact != null) {
            final String displayName = contact.getName();
            holder.usernameView.setText(WordUtils.capitalize(displayName.toLowerCase(), null));

            if(contact.getPhoneNumber() != null &&  contact.getPhoneNumber().length() > 0) Glide.with(mContext).load(contact.getThumbnailUrl()).into(holder.thumbnailView);
        }
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView  deleteIconView;
        public TextView usernameView;
        public CircleImageView thumbnailView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            this.thumbnailView = (CircleImageView) itemView.findViewById(R.id.eventUrl);
            this.usernameView = (TextView) itemView.findViewById(R.id.username);
            this.deleteIconView = (ImageView) itemView.findViewById(R.id.selected);
        }
    }

    public static interface IContactUpdateListener {
        public void onUpdateContactList(Contact contact);
    }
}
