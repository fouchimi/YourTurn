package com.social.yourturn.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.social.yourturn.utils.ParseConstant;

/**
 * Created by ousma on 7/5/2017.
 */

@ParseClassName("Message")
public class Message extends ParseObject {

    private boolean isFirstOfTheDay = false;

    public static final String SENDER_KEY = ParseConstant.SENDER_ID;
    public static final String BODY_KEY = ParseConstant.MESSAGE_BODY;
    public static final String RECEIVER_KEY = ParseConstant.RECEIVER_ID;
    public static final String CREATED_DATE_KEY = ParseConstant.CREATED_AT;
    public static final String UPDATED_DATE_KEY = ParseConstant.UPDATED_AT;

    public String getSenderKey() { return getString(SENDER_KEY);}

    public void setSenderId(String userId) {
        put(SENDER_KEY, userId);
    }

    public String getBody() {
        return getString(BODY_KEY);
    }

    public void setBody(String body) {
        put(BODY_KEY, body);
    }

    public void setReceiverKey(String receiverKey) {
        put(RECEIVER_KEY, receiverKey);
    }

    public  String getReceiverKey() {
        return getString(RECEIVER_KEY);
    }

    public  long getCreatedDateKey() {
        return getLong(CREATED_DATE_KEY);
    }

    public void setCreatedDateKey(long createdDateKey) {
        put(CREATED_DATE_KEY, createdDateKey);
    }

    public  long getUpdatedDateKey() {
        return getLong(UPDATED_DATE_KEY);
    }

    public void setUpdatedDateKey(long updatedDateKey){
        put(UPDATED_DATE_KEY, updatedDateKey);
    }

    public void setFirstOfTheDay(boolean firstOfTheDay) {
        isFirstOfTheDay = firstOfTheDay;
    }

    public boolean isFirstOfTheDay() {
        return isFirstOfTheDay;
    }
}
