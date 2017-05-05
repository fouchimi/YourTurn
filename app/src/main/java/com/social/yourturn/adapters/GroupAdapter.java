package com.social.yourturn.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.social.yourturn.GroupActivity;
import com.social.yourturn.R;
import com.social.yourturn.models.Group;
import com.social.yourturn.utils.CircularImageView;
import com.social.yourturn.utils.ParseConstant;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by ousma on 4/21/2017.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final static String TAG = GroupAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Group> mGroupList;

    public GroupAdapter(Context context, ArrayList<Group> groupList){
        mContext = context;
        mGroupList = groupList;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.frag_group_item_layout, null);
        GroupViewHolder viewHolder = new GroupViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        Group group = mGroupList.get(position);
        Log.d(TAG, group.getName());
        holder.groupName.setText(group.getName());
        if(group.getThumbnail()== null || group.getThumbnail().isEmpty()){
            holder.groupThumbnail.setImageResource(R.drawable.ic_group_black_36dp);
        }else {
            File file = new File(Environment.getExternalStorageDirectory()+ "/"+ ParseConstant.YOUR_TURN_FOLDER + "/" + group.getName() + "/" + group.getThumbnail());
            Log.d(TAG, file.getName());
            Picasso.with(mContext).load(file).into(holder.groupThumbnail);
        }

    }

    public Group getGroup(int position){
        return mGroupList.get(position);
    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        public ImageView groupThumbnail;
        public TextView groupName;

        public GroupViewHolder(View itemView) {
            super(itemView);
            this.groupName = (TextView) itemView.findViewById(R.id.group_name);
            this.groupThumbnail = (ImageView) itemView.findViewById(R.id.group_thumbnail);
        }
    }
}
