package com.social.yourturn.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.social.yourturn.GroupListActivity;
import com.social.yourturn.R;
import com.social.yourturn.fragments.GroupFragment;
import com.social.yourturn.models.Group;
import com.social.yourturn.utils.ParseConstant;
import com.social.yourturn.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
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

    public GroupAdapter(Context context, ArrayList<Group> groupList, RecyclerView rv){
        mContext = context;
        mGroupList = groupList;
        mRecyclerView = rv;
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
        String formattedDate = Utils.formatDate(group.getDateInMillis());
        holder.groupNumber.setText(String.valueOf(group.getContactList().size()));
        holder.createdDate.setText(mContext.getString(R.string.createdOn) + " " + formattedDate);
        if(group.getThumbnail()== null || group.getThumbnail().isEmpty()){
            holder.groupThumbnail.setImageResource(R.drawable.ic_group_black_36dp);
        }else if(group.getThumbnail().length() > 0 && group.getGroupCreator() != null && group.getGroupCreator().length() > 0) {
            File file = new File(Environment.getExternalStorageDirectory()+ "/"+ ParseConstant.YOUR_TURN_FOLDER + "/" + group.getName() + "/" + group.getThumbnail());
            Log.d(TAG, file.getName());
            Glide.with(mContext).load(file).into(holder.groupThumbnail);
        }else {
            holder.groupThumbnail.setImageResource(R.drawable.ic_group_black_36dp);
            ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.GROUP_TABLE);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    ParseFile parseFile = (ParseFile) object.get(ParseConstant.THUMBNAIL_COLUMN);
                    String imageUrl = parseFile.getUrl();
                    Uri imageUri = Uri.parse(imageUrl);
                    Glide.with(mContext).load(imageUri.toString()).into(holder.groupThumbnail);
                }
            });
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
        public CircleImageView groupThumbnail;
        public TextView groupName;
        public TextView groupNumber;
        public TextView createdDate;

        public GroupViewHolder(View itemView) {
            super(itemView);
            this.groupName = (TextView) itemView.findViewById(R.id.group_name);
            this.groupNumber = (TextView) itemView.findViewById(R.id.group_number);
            this.groupThumbnail = (CircleImageView) itemView.findViewById(R.id.group_thumbnail);
            this.createdDate = (TextView) itemView.findViewById(R.id.createdDate);
        }
    }

    public class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, GroupListActivity.class);
            int itemPosition = mRecyclerView.getChildLayoutPosition(v);
            Group group = getGroup(itemPosition);
            intent.putExtra(GroupFragment.GROUP_KEY, group);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
    }
}
