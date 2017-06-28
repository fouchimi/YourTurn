package com.social.yourturn.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.social.yourturn.data.YourTurnContract.UserEntry;
import com.social.yourturn.data.YourTurnContract.EventEntry;
import com.social.yourturn.data.YourTurnContract.LedgerEntry;
import com.social.yourturn.data.YourTurnContract.MemberEntry;

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
                MemberEntry._ID + " INTEGER PRIMARY KEY, " +
                MemberEntry.COLUMN_MEMBER_NAME + " TEXT NULL, " +
                MemberEntry.COLUMN_MEMBER_LOOKUP_KEY + " TEXT, " +
                MemberEntry.COLUMN_MEMBER_PHONE_NUMBER + " TEXT NOT NULL, " +
                MemberEntry.COLUMN_MEMBER_REGISTERED + " TEXT, " +
                MemberEntry.COLUMN_MEMBER_SORT_KEY_PRIMARY + " TEXT, " +
                MemberEntry.COLUMN_MEMBER_THUMBNAIL + " TEXT, " +
                MemberEntry.COLUMN_MEMBER_CREATED_DATE + " INTEGER NOT NULL, " +
                MemberEntry.COLUMN_MEMBER_UPDATED_DATE + " INTEGER NOT NULL " +
                ");";

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY, " +
                UserEntry.COLUMN_USER_NAME + " TEXT, " +
                UserEntry.COLUMN_USER_ID + " TEXT, " +
                UserEntry.COLUMN_USER_PASSWORD + " TEXT, " +
                UserEntry.COLUMN_USER_DEVICE_ID + " TEXT, " +
                UserEntry.COLUMN_USER_PHONE_NUMBER + " TEXT NOT NULL, " +
                UserEntry.COLUMN_USER_THUMBNAIL + " TEXT, " +
                UserEntry.COLUMN_USER_CREATED_DATE + " INTEGER NOT NULL, " +
                UserEntry.COLUMN_USER_UPDATED_DATE + " INTEGER NOT NULL" +
                ");";

        final String SQL_CREATE_GROUP_TABLE = "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
                EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EventEntry.COLUMN_GROUP_ID + " TEXT NOT NULL, " +
                EventEntry.COLUMN_USER_KEY + " TEXT NOT NULL, " +
                EventEntry.COLUMN_GROUP_NAME + " TEXT NOT NULL, " +
                EventEntry.COLUMN_GROUP_THUMBNAIL + " TEXT NULL, " +
                EventEntry.COLUMN_GROUP_CREATED_DATE + " INTEGER NOT NULL, " +
                EventEntry.COLUMN_GROUP_UPDATED_DATE + " INTEGER NOT NULL, " +
                EventEntry.COLUMN_GROUP_CREATOR + " TEXT, " +
                "FOREIGN KEY (" + EventEntry.COLUMN_USER_KEY + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + ") ON DELETE SET NULL ON UPDATE CASCADE"
                + ");";

        final String SQL_CREATE_LEDGER_TABLE = "CREATE TABLE " + YourTurnContract.LedgerEntry.TABLE_NAME + " (" +
                LedgerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LedgerEntry.COLUMN_USER_KEY + " TEXT NOT NULL, " +
                LedgerEntry.COLUMN_GROUP_KEY + " TEXT NOT NULL, " +
                LedgerEntry.COLUMN_USER_SHARE + " TEXT, " +
                LedgerEntry.COLUMN_TOTAL_AMOUNT + " TEXT NOT NULL," +
                LedgerEntry.COLUMN_GROUP_CREATED_DATE + " INTEGER NOT NULL, " +
                LedgerEntry.COLUMN_GROUP_UPDATED_DATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + LedgerEntry.COLUMN_USER_KEY + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + ") ON DELETE SET NULL ON UPDATE CASCADE " +
                "FOREIGN KEY (" + LedgerEntry.COLUMN_GROUP_KEY + ") REFERENCES " +
                EventEntry.TABLE_NAME + " (" + EventEntry.COLUMN_GROUP_ID + ") ON DELETE SET NULL ON UPDATE CASCADE" + ");";


        db.execSQL(SQL_CREATE_MEMBER_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_GROUP_TABLE);
        db.execSQL(SQL_CREATE_LEDGER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MemberEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LedgerEntry.TABLE_NAME);
        onCreate(db);
    }
}
