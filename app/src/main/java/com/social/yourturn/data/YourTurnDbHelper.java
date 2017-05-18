package com.social.yourturn.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.social.yourturn.data.YourTurnContract.UserEntry;
import com.social.yourturn.data.YourTurnContract.GroupEntry;

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

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY, " +
                UserEntry.COLUMN_USER_NAME + " TEXT, " +
                UserEntry.COLUMN_USER_ID + " TEXT, " +
                UserEntry.COLUMN_USER_PASSWORD + " TEXT, " +
                UserEntry.COLUMN_USER_DEVICE_ID + " TEXT, " +
                UserEntry.COLUMN_USER_PHONE_NUMBER + " TEXT, " +
                UserEntry.COLUMN_USER_THUMBNAIL + " TEXT, " +
                UserEntry.COLUMN_USER_CREATED_DATE + " INTEGER NOT NULL, " +
                UserEntry.COLUMN_USER_UPDATED_DATE + " INTEGER NOT NULL" +
                ");";

         final String SQL_CREATE_GROUP_TABLE = "CREATE TABLE " + GroupEntry.TABLE_NAME + " (" +
                GroupEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GroupEntry.COLUMN_GROUP_ID + " TEXT NOT NULL, " +
                GroupEntry.COLUMN_USER_KEY + " TEXT NOT NULL, " +
                GroupEntry.COLUMN_GROUP_NAME + " TEXT NOT NULL, " +
                GroupEntry.COLUMN_GROUP_THUMBNAIL + " TEXT NULL, " +
                GroupEntry.COLUMN_GROUP_CREATED_DATE + " INTEGER NOT NULL, " +
                GroupEntry.COLUMN_GROUP_UPDATED_DATE + " INTEGER NOT NULL, " +
                GroupEntry.COLUMN_GROUP_CREATOR + " TEXT, " +
                "FOREIGN KEY (" + GroupEntry.COLUMN_USER_KEY + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + ") ON DELETE CASCADE ON UPDATE NO ACTION"
                + ");";


        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_GROUP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GroupEntry.TABLE_NAME);
        onCreate(db);
    }
}
