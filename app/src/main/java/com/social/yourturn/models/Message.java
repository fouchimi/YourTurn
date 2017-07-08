package com.social.yourturn.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.social.yourturn.utils.ParseConstant;

/**
 * Created by ousma on 7/5/2017.
 */

@ParseClassName("Message")
public class Message extends ParseObject {

    public static final String SENDER_KEY = ParseConstant.SENDER_ID;
    public static final String BODY_KEY = ParseConstant.MESSAGE_BODY;
    public static final String RECEIVER_KEY = ParseConstant.RECEIVER_ID;

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
}
