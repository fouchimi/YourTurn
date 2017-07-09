package com.social.yourturn.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.social.yourturn.data.YourTurnContract.MemberEntry;
import com.social.yourturn.data.YourTurnContract.EventEntry;
import com.social.yourturn.data.YourTurnContract.LedgerEntry;
import com.social.yourturn.data.YourTurnContract.MessageEntry;
import com.social.yourturn.data.YourTurnContract.RecentMessageEntry;

/**
 * Created by ousma on 5/3/2017.
 */

public class YourTurnDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "your_turn.db";

    public YourTurnDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MEMBER_TABLE = "CREATE TABLE " + MemberEntry.TABLE_NAME + " (" +
                YourTurnContract.MemberEntry._ID + " INTEGER PRIMARY KEY, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_NAME + " TEXT NULL, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_LOOKUP_KEY + " TEXT, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + " TEXT NOT NULL, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_REGISTERED + " TEXT, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_SCORE + " TEXT, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_SORT_KEY_PRIMARY + " TEXT, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_THUMBNAIL + " TEXT, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_CREATED_DATE + " INTEGER NOT NULL, " +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_UPDATED_DATE + " INTEGER NOT NULL " +
                ");";


        final String SQL_CREATE_EVENT_TABLE = "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
                EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EventEntry.COLUMN_EVENT_ID + " TEXT NOT NULL, " +
                EventEntry.COLUMN_USER_KEY + " TEXT NOT NULL, " +
                EventEntry.COLUMN_EVENT_NAME + " TEXT NOT NULL, " +
                EventEntry.COLUMN_EVENT_URL + " TEXT NULL, " +
                EventEntry.COLUMN_EVENT_CREATED_DATE + " INTEGER NOT NULL, " +
                EventEntry.COLUMN_EVENT_UPDATED_DATE + " INTEGER NOT NULL, " +
                EventEntry.COLUMN_EVENT_CREATOR + " TEXT, " +
                EventEntry.COLUMN_EVENT_FLAG + " TEXT, " +
                "FOREIGN KEY (" + EventEntry.COLUMN_USER_KEY + ") REFERENCES " +
                YourTurnContract.MemberEntry.TABLE_NAME + " (" + MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + ") ON DELETE SET NULL ON UPDATE CASCADE"
                + ");";

        final String SQL_CREATE_LEDGER_TABLE = "CREATE TABLE " + LedgerEntry.TABLE_NAME + " (" +
                LedgerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LedgerEntry.COLUMN_USER_KEY + " TEXT NOT NULL, " +
                LedgerEntry.COLUMN_EVENT_KEY + " TEXT NOT NULL, " +
                LedgerEntry.COLUMN_USER_REQUEST + " TEXT, " +
                LedgerEntry.COLUMN_USER_PAID + " TEXT, " +
                LedgerEntry.COLUMN_TOTAL_AMOUNT + " TEXT NOT NULL," +
                LedgerEntry.COLUMN_GROUP_CREATED_DATE + " INTEGER NOT NULL, " +
                LedgerEntry.COLUMN_GROUP_UPDATED_DATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + LedgerEntry.COLUMN_USER_KEY + ") REFERENCES " +
                YourTurnContract.MemberEntry.TABLE_NAME + " (" + MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + ") ON DELETE SET NULL ON UPDATE CASCADE " +
                "FOREIGN KEY (" + LedgerEntry.COLUMN_EVENT_KEY + ") REFERENCES " +
                EventEntry.TABLE_NAME + " (" + EventEntry.COLUMN_EVENT_ID + ") ON DELETE SET NULL ON UPDATE CASCADE" + ");";

        final String SQL_CREATE_MESSAGE_TABLE = "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MessageEntry.COLUMN_MESSAGE_BODY + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_MESSAGE_TYPE + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_MESSAGE_SENDER_KEY + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_GROUP_CREATED_DATE + " INTEGER NOT NULL, " +
                MessageEntry.COLUMN_GROUP_UPDATED_DATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + MessageEntry.COLUMN_MESSAGE_SENDER_KEY + ") REFERENCES " + MemberEntry.TABLE_NAME + " (" +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + ") ON DELETE SET NULL ON UPDATE CASCADE" +
                " FOREIGN KEY (" + MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY + ") REFERENCES " + MemberEntry.TABLE_NAME + " (" +
                MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + ") ON DELETE SET NULL ON UPDATE CASCADE " + ");";

        final String SQL_CREATE_RECENT_MESSAGE_TABLE = "CREATE TABLE " + RecentMessageEntry.TABLE_NAME + " (" +
                RecentMessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RecentMessageEntry.COLUMN_MESSAGE_BODY + " TEXT NOT NULL, " +
                RecentMessageEntry.COLUMN_MESSAGE_TYPE + " TEXT NOT NULL, " +
                RecentMessageEntry.COLUMN_MESSAGE_USER_KEY + " TEXT NOT NULL, " +
                RecentMessageEntry.COLUMN_MESSAGE_RECEIVER_KEY + " TEXT NOT NULL, " +
                RecentMessageEntry.COLUMN_GROUP_CREATED_DATE + " INTEGER NOT NULL, " +
                RecentMessageEntry.COLUMN_GROUP_UPDATED_DATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + RecentMessageEntry.COLUMN_MESSAGE_USER_KEY + ") REFERENCES " + YourTurnContract.MemberEntry.TABLE_NAME + " (" +
                YourTurnContract.MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + ") ON DELETE SET NULL ON UPDATE CASCADE" + " FOREIGN KEY (" +
                MessageEntry.COLUMN_MESSAGE_RECEIVER_KEY + ") REFERENCES " + MemberEntry.TABLE_NAME + " (" + MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + ") " +
                "ON DELETE SET NULL ON UPDATE CASCADE );";


        db.execSQL(SQL_CREATE_MEMBER_TABLE);
        db.execSQL(SQL_CREATE_EVENT_TABLE);
        db.execSQL(SQL_CREATE_LEDGER_TABLE);
        db.execSQL(SQL_CREATE_MESSAGE_TABLE);
        db.execSQL(SQL_CREATE_RECENT_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MemberEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LedgerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecentMessageEntry.TABLE_NAME);
        onCreate(db);
    }
}
