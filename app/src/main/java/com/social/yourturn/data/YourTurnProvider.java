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

    static final int USER = 100;
    static final int GROUP = 200;
    static final int LEDGER = 300;

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
            case USER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        YourTurnContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case GROUP:{
                retCursor = mOpenHelper.getReadableDatabase().query(true,
                        YourTurnContract.GroupEntry.TABLE_NAME,
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
            case USER:
                return YourTurnContract.UserEntry.CONTENT_TYPE;
            case GROUP:
                return YourTurnContract.GroupEntry.CONTENT_TYPE;
            case LEDGER:
                return YourTurnContract.LedgerEntry.CONTENT_TYPE;
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
            case USER:{
                normalizeDate(values);
                long _id = db.insert(YourTurnContract.UserEntry.TABLE_NAME, null, values);
                if(_id > 0 ){
                    returnUri = YourTurnContract.UserEntry.buildUserUri(_id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case GROUP: {
                normalizeDate(values);
                long _id = db.insert(YourTurnContract.GroupEntry.TABLE_NAME, null, values);
                if( _id > 0 ){
                    returnUri = YourTurnContract.GroupEntry.buildGroupUri(_id);
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
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    private void normalizeDate(ContentValues values){
        if(values.containsKey(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE)){
            long dateValue = values.getAsLong(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE);
            values.put(YourTurnContract.UserEntry.COLUMN_USER_CREATED_DATE, YourTurnContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if(null == selection) selection = "1";
        switch (match){
            case USER :
                rowsDeleted = db.delete(YourTurnContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case GROUP :
                rowsDeleted = db.delete(YourTurnContract.GroupEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LEDGER :
                rowsDeleted = db.delete(YourTurnContract.LedgerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        // Because a null deletes all rows
        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case USER :
                normalizeDate(values);
                rowsUpdated = db.update(YourTurnContract.UserEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case GROUP :
                normalizeDate(values);
                rowsUpdated = db.update(YourTurnContract.GroupEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LEDGER:
                normalizeDate(values);
                rowsUpdated = db.update(YourTurnContract.LedgerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        // Because a null deletes all rows
        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = YourTurnContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, YourTurnContract.PATH_USER, USER);
        matcher.addURI(authority, YourTurnContract.PATH_GROUP, GROUP);
        matcher.addURI(authority, YourTurnContract.PATH_LEDGER, LEDGER);

        return matcher;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match){
            case USER:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for(ContentValues value : values){
                        normalizeDate(value);
                        long _id = db.insert(YourTurnContract.UserEntry.TABLE_NAME, null, value);
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
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case GROUP:
                db.beginTransaction();
                returnCount = 0;

                try{
                    for(ContentValues value : values){
                        normalizeDate(value);
                        long _id = db.insert(YourTurnContract.GroupEntry.TABLE_NAME, null, value);
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
                getContext().getContentResolver().notifyChange(uri, null);
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
