package com.social.yourturn.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.social.yourturn.R;
import com.social.yourturn.models.Contact;
import com.social.yourturn.utils.ImageLoader;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ousma on 4/22/2017.
 */

public class MemberEventAdapter extends RecyclerView.Adapter<MemberEventAdapter.MemberViewHolder>{

    private static final String TAG = MemberEventAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Contact> mContactList;
    private ImageLoader imageLoader;

    private List<Contact> itemsPendingRemoval;

    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3 second
    private Handler handler = new Handler();
    private HashMap<Contact, Runnable> pendingRunnableMap = new HashMap<>();

    public MemberEventAdapter(Context context, ArrayList<Contact> contactList){
        mContext = context;
        this.mContactList = contactList;
        imageLoader = new ImageLoader(mContext);
        itemsPendingRemoval = new ArrayList<>();
    }


    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.group_members, null);
        final MemberViewHolder viewHolder = new MemberViewHolder(view);
        viewHolder.requestedEditText.addTextChangedListener(new TextWatcher() {
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
                        mContactList.get(i).setRequested(s.toString());
                    }
                }
            }
        });

        viewHolder.paidEditText.addTextChangedListener(new TextWatcher() {
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
                        mContactList.get(i).setPaid(s.toString());
                    }
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MemberViewHolder holder, final int position) {
        final Contact contact = mContactList.get(position);

        if (itemsPendingRemoval.contains(contact)) {
            /** {show swipe layout} and {hide regular layout} */
            holder.regularLayout.setVisibility(View.GONE);
            holder.swipeLayout.setVisibility(View.VISIBLE);
            holder.undoTextView.setOnClickListener(v -> undoOpt(contact));
        } else {
            /** {show regular layout} and {hide swipe layout} */
            String displayName = contact.getName();
            holder.regularLayout.setVisibility(View.VISIBLE);
            holder.swipeLayout.setVisibility(View.GONE);
            holder.nameTextView.setText(WordUtils.capitalize(displayName.toLowerCase(), null));
            if(contact.getRequested() != null && contact.getRequested().length() > 0 &&
                    contact.getPaid() != null && contact.getPaid().length() > 0){
                holder.requestedEditText.setText(contact.getRequested());
                holder.paidEditText.setText(contact.getPaid());
            }
            else {
                holder.requestedEditText.setText(R.string.zero_default_values);
                holder.paidEditText.setText(R.string.zero_default_values);
            }

            holder.checkedIcon.setVisibility(View.INVISIBLE);

            imageLoader.DisplayImage(contact.getThumbnailUrl(), holder.imageView);
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

    public ArrayList<Contact> getContactList() {
        return mContactList;
    }


    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, undoTextView;
        CircleImageView imageView;
        EditText requestedEditText;
        EditText paidEditText;
        ImageView checkedIcon;
        LinearLayout swipeLayout, regularLayout;

        public MemberViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.member_name);
            undoTextView = (TextView) itemView.findViewById(R.id.undo);
            imageView = (CircleImageView) itemView.findViewById(R.id.member_thumbnail);
            requestedEditText = (EditText) itemView.findViewById(R.id.requestValue);
            paidEditText = (EditText) itemView.findViewById(R.id.paidValue);
            checkedIcon = (ImageView) itemView.findViewById(R.id.check_icon);
            swipeLayout = (LinearLayout) itemView.findViewById(R.id.swipeLayout);
            regularLayout = (LinearLayout) itemView.findViewById(R.id.regularLayout);

        }

        public ImageView getCheckedIcon() {
            return checkedIcon;
        }

        public EditText getRequestedEditText() {
            return requestedEditText;
        }

        public EditText getPaidEditText(){
            return paidEditText;
        }
    }

    public void pendingRemoval(int position) {

        final Contact contact = mContactList.get(position);
        if (!itemsPendingRemoval.contains(contact)) {
            itemsPendingRemoval.add(contact);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the data
            Runnable pendingRemovalRunnable = () -> remove(mContactList.indexOf(contact));
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnableMap.put(contact, pendingRemovalRunnable);
        }
    }

    public void remove(int position) {
        Contact contact = mContactList.get(position);
        if (itemsPendingRemoval.contains(contact)) {
            itemsPendingRemoval.remove(contact);
        }
        if (mContactList.contains(contact)) {
            mContactList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isPendingRemoval(int position) {
        Contact contact = mContactList.get(position);
        return itemsPendingRemoval.contains(contact);
    }

    private void undoOpt(Contact contact) {
        Runnable pendingRemovalRunnable = pendingRunnableMap.get(contact);
        pendingRunnableMap.remove(contact);
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable);
        itemsPendingRemoval.remove(contact);
        // this will rebind the row in "normal" state
        notifyItemChanged(mContactList.indexOf(contact));
    }

}
