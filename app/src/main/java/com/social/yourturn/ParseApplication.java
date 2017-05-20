package com.social.yourturn;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

/**
 * Created by ousmane on 4/18/17.
 */

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.app_id))
                .server(getString(R.string.server))
                .clientKey(null)
                .build());

        // Need to register GCM token
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParsePush.subscribeInBackground("pushChannel");
    }
}
