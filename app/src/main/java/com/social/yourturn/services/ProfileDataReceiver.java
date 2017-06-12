package com.social.yourturn.services;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Created by ousma on 6/12/2017.
 */

public class ProfileDataReceiver extends ResultReceiver {
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */

    private ProfileReceiver receiver;

    public ProfileDataReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(ProfileReceiver receiver) {
        this.receiver = receiver;
    }

    public interface ProfileReceiver {
        void onReceivedResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if(receiver != null) {
            receiver.onReceivedResult(resultCode, resultData);
        }
    }
}
