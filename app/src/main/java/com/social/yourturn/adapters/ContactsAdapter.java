package com.social.yourturn.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.text.style.TextAppearanceSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.social.yourturn.ContactActivity;
import com.social.yourturn.R;
import com.social.yourturn.models.Contact;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by ousma on 4/12/2017.
 */

public class ContactsAdapter extends CursorAdapter implements SectionIndexer {
    private LayoutInflater mInflater;
    private AlphabetIndexer mAlphabetIndexer;
    private TextAppearanceSpan highlightTextSpan;
    private Context mContext;
    private final static String TAG = ContactsAdapter.class.getSimpleName();
    private SparseBooleanArray selectionArray = new SparseBooleanArray();
    private List<Contact> contactList = new ArrayList<>();
    private int mPosition;

    public ContactsAdapter(Context context){
        super(context, null, 0);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        final String alphabet = context.getString(R.string.alphabet);
        mAlphabetIndexer = new AlphabetIndexer(null, ContactActivity.ContactsQuery.SORT_KEY, alphabet);
        highlightTextSpan = new TextAppearanceSpan(context, R.style.searchTextHighlight);
        mPosition = 0;
    }


    public List<Contact> getContactList() {
        return contactList;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View itemLayout = mInflater.from(mContext).inflate(R.layout.contact_layout, parent, false);

        final ViewHolder holder = new ViewHolder();
        holder.username = (TextView) itemLayout.findViewById(R.id.username);
        holder.thumbnail = (CircleImageView) itemLayout.findViewById(R.id.thumbnail);
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
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        final String contactId = cursor.getString(ContactActivity.ContactsQuery.ID);
        final String displayName = cursor.getString(ContactActivity.ContactsQuery.DISPLAY_NAME).toUpperCase();
        final String phoneNumber = cursor.getString(ContactActivity.ContactsQuery.PHONE_NUMBER);
        Contact contact = new Contact(contactId, displayName, phoneNumber);
        contact.setPosition((mPosition++) % getCount());
        contactList.add(contact);

        holder.thumbnail.setImageResource(R.drawable.default_profile);
        holder.username.setText(WordUtils.capitalize(displayName.toLowerCase(), null));

        int position = cursor.getPosition();
        boolean isSelected = selectionArray.get(position);
        TextView usernameTextView = (TextView) view.findViewById(R.id.username);
        if (isSelected) {
            view.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            usernameTextView.setTextColor(Color.WHITE);
        } else if (!isSelected){
            view.setBackgroundColor(Color.TRANSPARENT );
            usernameTextView.setTextColor(Color.BLACK);
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

    private class ViewHolder {
        TextView username;
        ImageView  selected;
        CircleImageView thumbnail;
    }

}
