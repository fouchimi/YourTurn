package com.social.yourturn.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by ousma on 5/3/2017.
 */

public class YourTurnProvider extends ContentProvider {

    static final int MEMBER = 100;
    static final int EVENT = 200;
    static final int LEDGER = 300;
    static final int MESSAGE = 400;
    static final int RECENT_MESSAGE = 500;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private YourTurnDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new YourTurnDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            case MEMBER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        YourTurnContract.MemberEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case EVENT:{
                retCursor = mOpenHelper.getReadableDatabase().query(true,
                        YourTurnContract.EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null);
                break;
            }
            case LEDGER:{
                retCursor = mOpenHelper.getReadableDatabase().query(true,
                        YourTurnContract.LedgerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null);
                break;
            }
            case MESSAGE:{
                retCursor = mOpenHelper.getReadableDatabase().query(true,
                        YourTurnContract.MessageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null);
                break;
            }
            case RECENT_MESSAGE: {
                retCursor = mOpenHelper.getReadableDatabase().query(true,
                        YourTurnContract.RecentMessageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match){
            case MEMBER:
                return YourTurnContract.MemberEntry.CONTENT_TYPE;
            case EVENT:
                return YourTurnContract.EventEntry.CONTENT_TYPE;
            case LEDGER:
                return YourTurnContract.LedgerEntry.CONTENT_TYPE;
            case MESSAGE:
                return YourTurnContract.MemberEntry.CONTENT_TYPE;
            case RECENT_MESSAGE:
                return YourTurnContract.RecentMessageEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match){
            case MEMBER:{
                normalizeDate(values);
                long _id = db.insert(YourTurnContract.MemberEntry.TABLE_NAME, null, values);
                if(_id > 0 ){
                    returnUri = YourTurnContract.MemberEntry.buildMemberUri(_id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case EVENT: {
                normalizeDate(values);
                long _id = db.insert(YourTurnContract.EventEntry.TABLE_NAME, null, values);
                if( _id > 0 ){
                    returnUri = YourTurnContract.EventEntry.buildGroupUri(_id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case LEDGER: {
                normalizeDate(values);
                long _id = db.insert(YourTurnContract.LedgerEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = YourTurnContract.LedgerEntry.buildLedgerUri(_id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case MESSAGE:{
                normalizeDate(values);
                long _id = db.insert(YourTurnContract.MessageEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = YourTurnContract.MessageEntry.buildLedgerUri(_id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case RECENT_MESSAGE:{
                long _id = db.insert(YourTurnContract.RecentMessageEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnUri = YourTurnContract.RecentMessageEntry.buildLedgerUri(_id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    private void normalizeDate(ContentValues values){
        if(values.containsKey(YourTurnContract.MemberEntry.COLUMN_MEMBER_CREATED_DATE)){
            long dateValue = values.getAsLong(YourTurnContract.MemberEntry.COLUMN_MEMBER_CREATED_DATE);
            values.put(YourTurnContract.MemberEntry.COLUMN_MEMBER_CREATED_DATE, YourTurnContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if(null == selection) selection = "1";
        switch (match){
            case MEMBER :
                rowsDeleted = db.delete(YourTurnContract.MemberEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EVENT:
                rowsDeleted = db.delete(YourTurnContract.EventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LEDGER :
                rowsDeleted = db.delete(YourTurnContract.LedgerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MESSAGE :
                rowsDeleted = db.delete(YourTurnContract.MessageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RECENT_MESSAGE :
                rowsDeleted = db.delete(YourTurnContract.RecentMessageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        // Because a null deletes all rows
        if(rowsDeleted != 0){
            if(getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case MEMBER :
                normalizeDate(values);
                rowsUpdated = db.update(YourTurnContract.MemberEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case EVENT:
                normalizeDate(values);
                rowsUpdated = db.update(YourTurnContract.EventEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LEDGER:
                normalizeDate(values);
                rowsUpdated = db.update(YourTurnContract.LedgerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MESSAGE:
                normalizeDate(values);
                rowsUpdated = db.update(YourTurnContract.MessageEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case RECENT_MESSAGE:
                normalizeDate(values);
                rowsUpdated = db.update(YourTurnContract.RecentMessageEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        // Because a null deletes all rows
        if(rowsUpdated != 0){
            if(getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = YourTurnContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, YourTurnContract.PATH_MEMBER, MEMBER);
        matcher.addURI(authority, YourTurnContract.PATH_EVENT, EVENT);
        matcher.addURI(authority, YourTurnContract.PATH_LEDGER, LEDGER);
        matcher.addURI(authority, YourTurnContract.PATH_MESSAGE, MESSAGE);
        matcher.addURI(authority, YourTurnContract.PATH_RECENT_MESSAGE, RECENT_MESSAGE);

        return matcher;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match){
            case MEMBER:
                db.beginTransaction();
                try{
                    for(ContentValues value : values){
                        normalizeDate(value);
                        long _id = db.insert(YourTurnContract.MemberEntry.TABLE_NAME, null, value);
                        if(_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    db.endTransaction();
                }
                if(getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case EVENT:
                db.beginTransaction();
                returnCount = 0;
                try{
                    for(ContentValues value : values){
                        normalizeDate(value);
                        long _id = db.insert(YourTurnContract.EventEntry.TABLE_NAME, null, value);
                        if(_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    db.endTransaction();
                }
                if(getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case LEDGER:
                db.beginTransaction();
                returnCount = 0;
                try{
                    for(ContentValues value : values){
                        normalizeDate(value);
                        long _id = db.insert(YourTurnContract.LedgerEntry.TABLE_NAME, null, value);
                        if(_id != -1){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    db.endTransaction();
                }
                if(getContext() != null ) getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case MESSAGE:
                db.beginTransaction();
                returnCount = 0;
                try{
                    for(ContentValues value : values){
                        normalizeDate(value);
                        long _id = db.insert(YourTurnContract.MemberEntry.TABLE_NAME, null, value);
                        if(_id != -1){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    db.endTransaction();
                }
                if(getContext() != null ) getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case RECENT_MESSAGE:
                db.beginTransaction();
                returnCount = 0;
                try{
                    for(ContentValues value : values){
                        normalizeDate(value);
                        long _id = db.insert(YourTurnContract.RecentMessageEntry.TABLE_NAME, null, value);
                        if(_id != -1){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    db.endTransaction();
                }
                if(getContext() != null ) getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
