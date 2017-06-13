package com.social.yourturn.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.social.yourturn.R;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ImageLoader;

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
    private ImageLoader imageLoader;

    public MemberGroupAdapter(Context context, ArrayList<Contact> contactList){
        mContext = context;
        this.mContactList = contactList;
        imageLoader = new ImageLoader(mContext);
    }


    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.group_members, null);
        final MemberViewHolder viewHolder = new MemberViewHolder(view);
        viewHolder.splitValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                TextView txt = viewHolder.nameTextView;
                for(int i=0; i < mContactList.size(); i++){
                    String name = mContactList.get(i).getName().toLowerCase();
                    name = WordUtils.capitalize(name, null);
                    if(name.equals(txt.getText().toString())){
                        mContactList.get(i).setShare(s.toString());
                    }
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MemberViewHolder holder, final int position) {
        final Contact contact = mContactList.get(position);
        String displayName = contact.getName();
        holder.nameTextView.setText(WordUtils.capitalize(displayName.toLowerCase(), null));
        if(contact.getShare() != null && contact.getShare().length() > 0)
            holder.splitValueEditText.setText(contact.getShare());
        else {
            holder.splitValueEditText.setText(R.string.zero_default_values);
        }

        holder.checkedIcon.setVisibility(View.INVISIBLE);

        imageLoader.DisplayImage(contact.getThumbnailUrl(), holder.imageView);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    public ArrayList<Contact> getContactList() {
        return mContactList;
    }


    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        CircleImageView imageView;
        EditText splitValueEditText;
        ImageView checkedIcon;

        public MemberViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.member_name);
            imageView = (CircleImageView) itemView.findViewById(R.id.member_thumbnail);
            splitValueEditText = (EditText) itemView.findViewById(R.id.splitValue);
            checkedIcon = (ImageView) itemView.findViewById(R.id.check_icon);
        }

        public ImageView getCheckedIcon() {
            return checkedIcon;
        }

        public EditText getSplitValueEditText() {
            return splitValueEditText;
        }
    }

}
