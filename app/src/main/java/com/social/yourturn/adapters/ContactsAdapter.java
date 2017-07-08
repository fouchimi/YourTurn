package com.social.yourturn.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.social.yourturn.R;
import com.social.yourturn.ContactActivity.MemberQuery;
import com.social.yourturn.data.YourTurnContract;

import org.apache.commons.lang3.text.WordUtils;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by ousma on 4/12/2017.
 */

public class ContactsAdapter extends CursorAdapter implements SectionIndexer {
    private AlphabetIndexer mAlphabetIndexer;
    private Context mContext;
    private SparseBooleanArray selectionArray = new SparseBooleanArray();

    public ContactsAdapter(Context context){
        super(context, null, 0);
        mContext = context;
        final String alphabet = context.getString(R.string.alphabet);
        mAlphabetIndexer = new AlphabetIndexer(null, MemberQuery.DISPLAY_NAME, alphabet);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View itemLayout = LayoutInflater.from(mContext).inflate(R.layout.contact_layout, parent, false);

        final ViewHolder holder = new ViewHolder();
        holder.username = (TextView) itemLayout.findViewById(R.id.username);
        holder.profileUrl = (CircleImageView) itemLayout.findViewById(R.id.profileUrl);
        holder.selected = (ImageView) itemLayout.findViewById(R.id.selected);

        itemLayout.setTag(holder);
        return itemLayout;
    }

    public void setSelected(int position, boolean isSelected) {
        selectionArray.put(position, isSelected);
    }

    public boolean isSelected(int position){
        return selectionArray.get(position);
    }


    public void remove(int key){
        selectionArray.delete(key);
    }

    public void clearSelection(){
        selectionArray.clear();
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        int position = cursor.getPosition();
        boolean isSelected = selectionArray.get(position);
        final ViewHolder holder = (ViewHolder) view.getTag();
        final String displayName = cursor.getString(MemberQuery.DISPLAY_NAME).toUpperCase();
        String thumbnail = cursor.getString(MemberQuery.THUMBNAIL);

        Glide.clear(holder.profileUrl);
        if(thumbnail != null && thumbnail.length() > 0) Glide.with(mContext).load(thumbnail).into(holder.profileUrl);
        else holder.profileUrl.setImageResource(R.drawable.default_profile);
        holder.username.setText(WordUtils.capitalize(displayName.toLowerCase(), null));

        if (isSelected) {
            view.setBackgroundColor(context.getResources().getColor(R.color.colorAccent, mContext.getTheme()));
            holder.username.setTextColor(Color.WHITE);
            holder.selected.setVisibility(View.VISIBLE);
        } else {
            view.setBackgroundColor(Color.TRANSPARENT );
            holder.username.setTextColor(Color.BLACK);
            holder.selected.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getCount() {
        if(getCursor() == null) return 0;
        return super.getCount();
    }

    @Override
    public Object[] getSections() {
        return mAlphabetIndexer.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if(getCursor() == null) return 0;
        return mAlphabetIndexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        if(getCursor() == null) return 0;
        return mAlphabetIndexer.getSectionForPosition(position);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        mAlphabetIndexer.setCursor(newCursor);
        return super.swapCursor(newCursor);
    }

    public class ViewHolder {
        TextView username;
        ImageView  selected;
        CircleImageView profileUrl;
    }

}

