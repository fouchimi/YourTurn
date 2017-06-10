package com.social.yourturn.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.social.yourturn.GroupListActivity;
import com.social.yourturn.R;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.fragments.GroupFragment;
import com.social.yourturn.models.Group;
import com.social.yourturn.utils.ImageLoader;
import com.social.yourturn.utils.ParseConstant;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by ousma on 4/21/2017.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final static String TAG = GroupAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Group> mGroupList;
    private final View.OnClickListener mOnClickListener = new MyOnClickListener();
    private RecyclerView mRecyclerView;
    ImageLoader imageLoader;

    public GroupAdapter(Context context, ArrayList<Group> groupList, RecyclerView rv){
        mContext = context;
        mGroupList = groupList;
        mRecyclerView = rv;
        imageLoader = new ImageLoader(mContext);
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.frag_group_item_layout, null);
        view.setOnClickListener(mOnClickListener);
        GroupViewHolder viewHolder = new GroupViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, int position) {
        Group group = mGroupList.get(position);
        Log.d(TAG, group.getName());
        holder.groupName.setText(group.getName());

        Cursor selfCursor = mContext.getContentResolver().query(YourTurnContract.UserEntry.CONTENT_URI, null,
                YourTurnContract.UserEntry.COLUMN_USER_PHONE_NUMBER + " = " + DatabaseUtils.sqlEscapeString(getCurrentPhoneNumber()), null, null);

        if(selfCursor.getCount() == 0) holder.groupNumber.setText(String.valueOf(group.getContactList().size()+1));
        else holder.groupNumber.setText(String.valueOf(group.getContactList().size()));

        selfCursor.close();

        if(group.getThumbnail() != null && group.getThumbnail().length() > 0) imageLoader.DisplayImage(group.getThumbnail(), holder.groupThumbnail);
        else holder.groupThumbnail.setImageResource(R.drawable.ic_group_black_36dp);

    }

    public Group getGroup(int position){
        return mGroupList.get(position);
    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView groupThumbnail;
        public TextView groupName;
        public TextView groupNumber;

        public GroupViewHolder(View itemView) {
            super(itemView);
            this.groupName = (TextView) itemView.findViewById(R.id.group_name);
            this.groupNumber = (TextView) itemView.findViewById(R.id.group_number);
            this.groupThumbnail = (CircleImageView) itemView.findViewById(R.id.group_thumbnail);
        }
    }

    public class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, GroupListActivity.class);
            int itemPosition = mRecyclerView.getChildLayoutPosition(v);
            Group group = getGroup(itemPosition);
            intent.putExtra(GroupFragment.GROUP_KEY, group);

            intent.putExtra(ParseConstant.USERNAME_COLUMN, getCurrentPhoneNumber());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
    }

    private String getCurrentPhoneNumber(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.user_credentials), Context.MODE_PRIVATE);
        return  sharedPref.getString(ParseConstant.USERNAME_COLUMN, "");
    }
}
