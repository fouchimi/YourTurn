package com.social.yourturn.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        holder.imageView.setImageResource(R.drawable.default_profile);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(ParseConstant.USERNAME_COLUMN, contact.getPhoneNumber());
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e == null){
                    ParseFile parseFile = (ParseFile) user.get(ParseConstant.USER_THUMBNAIL_COLUMN);
                    if(parseFile != null) {
                        String imageUrl = parseFile.getUrl();
                        Uri imageUri = Uri.parse(imageUrl);
                        Glide.with(mContext).load(imageUri.toString()).into(holder.imageView);
                    }
                }else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
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

        public EditText getSplitValueEditText() {
            return splitValueEditText;
        }

        public ImageView getCheckedIcon() {
            return checkedIcon;
        }
    }

}
