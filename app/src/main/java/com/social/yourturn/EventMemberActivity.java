package com.social.yourturn;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.social.yourturn.adapters.EventMemberAdapter;
import com.social.yourturn.data.YourTurnContract;
import com.social.yourturn.fragments.EventFragment;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Event;


public class EventMemberActivity extends AppCompatActivity {

    private static final String TAG = EventMemberActivity.class.getSimpleName();
    private Event mEvent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_member);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        if(intent != null) {
            mEvent = intent.getParcelableExtra(EventFragment.EVENT_KEY);
            if(getSupportActionBar() != null) getSupportActionBar().setTitle(mEvent.getName());
            EventMemberAdapter adapter = new EventMemberAdapter(this, mEvent, mEvent.getContactList());
            ListView mListView = (ListView) findViewById(R.id.members_list_view);
            mListView.setAdapter(adapter);

            for(Contact contact : mEvent.getContactList()){
                Cursor cursor = getContentResolver().query(YourTurnContract.MemberEntry.CONTENT_URI,
                        new String[]{YourTurnContract.MemberEntry.COLUMN_MEMBER_SCORE}, YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + "=?",
                        new String[]{contact.getPhoneNumber()}, null);

                if(cursor != null && cursor.getCount() > 0){
                    cursor.moveToFirst();
                    String score = cursor.getString(cursor.getColumnIndex(YourTurnContract.MemberEntry.COLUMN_MEMBER_SCORE));
                    contact.setScore(score);
                    adapter.notifyDataSetChanged();
                }

                cursor.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_members_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteEventAction:
                if(mEvent != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Delete Action")
                            .setMessage("Are you sure you want to delete this event ?")
                            .setPositiveButton(getString(R.string.yes_text), (dialog, which) -> {
                                int deleteId = getContentResolver().delete(YourTurnContract.EventEntry.CONTENT_URI,
                                        YourTurnContract.EventEntry.COLUMN_EVENT_ID + "=?", new String[]{mEvent.getEventId()});
                                Log.d(TAG, "delete Id: " + deleteId);
                                if(deleteId > 0) {
                                    Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }).setNegativeButton(getString(R.string.no_text), (dialog, which) -> {
                                Log.d(TAG, "Deletion declined !");
                            });
                    builder.create().show();
                }
                break;
            case R.id.changeEventPicAction:
                Intent intent = new Intent(this, ChangeEventPicActivity.class);
                intent.putExtra(getString(R.string.selected_event), mEvent);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            default:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
