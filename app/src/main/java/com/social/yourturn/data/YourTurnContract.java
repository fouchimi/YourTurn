package com.social.yourturn.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ousma on 5/3/2017.
 */

public class YourTurnContract {

    public static final String CONTENT_AUTHORITY = "com.social.yourturn";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MEMBER = "member";
    public static final String PATH_EVENT = "event";
    public static final String PATH_LEDGER = "ledger";
    public static final String PATH_RECENT_MESSAGE = "recent";
    public static final String PATH_MESSAGE = "message";

    public static long normalizeDate(long startDate) {
        return startDate;
    }

    public static final class MemberEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMBER).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBER;

        public static final String TABLE_NAME = "members";
        public static final String COLUMN_MEMBER_NAME = "display_name";
        public static final String COLUMN_MEMBER_PHONE_NUMBER = "data1";
        public static final String COLUMN_MEMBER_REGISTERED = "is_registered";
        public static final String COLUMN_MEMBER_THUMBNAIL = "thumbnail";
        public static final String COLUMN_MEMBER_SORT_KEY_PRIMARY = "sort_key";
        public static final String COLUMN_MEMBER_LOOKUP_KEY = "lookup";
        public static final String COLUMN_MEMBER_SCORE = "score";
        public static final String COLUMN_MEMBER_CREATED_DATE = "created_date";
        public static final String COLUMN_MEMBER_UPDATED_DATE = "updated_date";

        public static Uri buildMemberUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class EventEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;

        public static final String TABLE_NAME = "groups";

        public static final String COLUMN_EVENT_ID = "group_id";
        public static final String COLUMN_USER_KEY = "usr_contact_id";
        public static final String COLUMN_EVENT_NAME = "group_name";
        public static final String COLUMN_EVENT_URL = "group_thumbnail";
        public static final String COLUMN_EVENT_CREATED_DATE = "created_date";
        public static final String COLUMN_EVENT_UPDATED_DATE = "updated_date";
        public static final String COLUMN_EVENT_CREATOR = "group_creator";
        public static final String COLUMN_EVENT_FLAG = "flag_latest";

        public static Uri buildGroupUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class LedgerEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LEDGER).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LEDGER;

        public static final String TABLE_NAME = "ledger";

        public static final String COLUMN_EVENT_KEY = "group_id";
        public static final String COLUMN_USER_KEY = "usr_contact_id";
        public static final String COLUMN_USER_REQUEST = "usr_request";
        public static final String COLUMN_USER_PAID = "usr_paid";
        public static final String COLUMN_TOTAL_AMOUNT = "total_amount";
        public static final String COLUMN_GROUP_CREATED_DATE = "created_date";
        public static final String COLUMN_GROUP_UPDATED_DATE = "updated_date";

        public static Uri buildLedgerUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class RecentMessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECENT_MESSAGE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECENT_MESSAGE;

        public static final String TABLE_NAME = "recent_message";

        public static final String COLUMN_MESSAGE_BODY = "msg_body";
        public static final String COLUMN_MESSAGE_TYPE = "msg_type";
        public static final String COLUMN_MESSAGE_USER_KEY = "msg_ct_id";
        public static final String COLUMN_MESSAGE_RECEIVER_KEY = "msg_rc_id";
        public static final String COLUMN_MESSAGE_CREATED_DATE = "created_date";
        public static final String COLUMN_MESSAGE_UPDATED_DATE = "updated_date";

        public static Uri buildLedgerUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class MessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGE;

        public static final String TABLE_NAME = "message";

        public static final String COLUMN_MESSAGE_BODY = "msg_body";
        public static final String COLUMN_MESSAGE_TYPE = "msg_type";
        public static final String COLUMN_MESSAGE_SENDER_KEY = "msg_ct_id";
        public static final String COLUMN_MESSAGE_RECEIVER_KEY = "msg_rc_id";
        public static final String COLUMN_MESSAGE_CREATED_DATE = "created_date";
        public static final String COLUMN_MESSAGE_UPDATED_DATE = "updated_date";

        public static Uri buildLedgerUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
