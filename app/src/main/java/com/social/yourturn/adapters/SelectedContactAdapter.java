package com.social.yourturn.adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.social.yourturn.ContactActivity;
import com.social.yourturn.DrawableProvider;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        final String displayName = contact.getName();
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
        public ImageView thumbnailView, deleteIconView;
        public TextView usernameView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            this.thumbnailView = (ImageView) itemView.findViewById(R.id.thumbnail);
            this.usernameView = (TextView) itemView.findViewById(R.id.username);
            this.deleteIconView = (ImageView) itemView.findViewById(R.id.selected);
        }
    }

    public static interface IContactUpdateListener {
        public void onUpdateContactList(Contact contact);
    }
}
