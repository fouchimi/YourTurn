package com.social.yourturn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.social.yourturn.adapters.EventMemberAdapter;
import com.social.yourturn.fragments.EventFragment;
import com.social.yourturn.models.Event;

import org.apache.commons.lang3.text.WordUtils;

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
            }
            EventMemberAdapter adapter = new EventMemberAdapter(this, mEvent, mEvent.getContactList());
            ListView mListView = (ListView) findViewById(R.id.members_list_view);
            mListView.setAdapter(adapter);
            Log.d(TAG, "Hello");
        }
    }
}
