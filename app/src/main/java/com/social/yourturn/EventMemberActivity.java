package com.social.yourturn;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.social.yourturn.adapters.EventMemberAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.fragments.EventFragment;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Event;
import com.social.yourturn.utils.ParseConstant;

import org.apache.commons.lang3.text.WordUtils;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class EventMemberActivity extends AppCompatActivity {

    private static final String TAG = EventMemberActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_member);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if(intent != null) {
            Event mEvent = intent.getParcelableExtra(EventFragment.EVENT_KEY);
            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mEvent.getName());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            EventMemberAdapter adapter = new EventMemberAdapter(this, mEvent, mEvent.getContactList());
            ListView mListView = (ListView) findViewById(R.id.members_list_view);
            mListView.setAdapter(adapter);

            for(Contact contact : mEvent.getContactList()){
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstant.SCORE_TABLE);
                query.whereEqualTo(ParseConstant.USERNAME_COLUMN, contact.getPhoneNumber());

                fetchScore(query).continueWithTask(new Continuation<ParseObject, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<ParseObject> task) throws Exception {
                        if(task.isFaulted()){
                            Log.d(TAG, "An error occured while fetching score");
                        }else {
                            Log.d(TAG, "Score fetched successfully");
                            // update member entry
                            ParseObject row = task.getResult();
                            ContentValues scoreValue = new ContentValues();
                            String score = row.getString(ParseConstant.USER_SCORE_COLUMN);
                            scoreValue.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_SCORE, score);
                            getContentResolver().update(YourTurnContract.MemberEntry.CONTENT_URI,
                                    scoreValue,
                                    YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?", new String[]{contact.getPhoneNumber()});
                            contact.setScore(score);
                            adapter.notifyDataSetChanged();
                        }
                        return null;
                    }
                });
            }
        }
    }

    private Task<ParseObject> fetchScore(ParseQuery<ParseObject> query) {
        final TaskCompletionSource<ParseObject> tcs = new TaskCompletionSource<>();
        query.getFirstInBackground((row, e) -> {
            if (e == null) {
                tcs.setResult(row);
            } else {
                tcs.setError(e);
            }
        });
        return tcs.getTask();
    }
}
