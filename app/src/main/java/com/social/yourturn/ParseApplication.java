package com.social.yourturn;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.social.yourturn.models.Message;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by ousmane on 4/18/17.
 */

public class ParseApplication extends Application {

    //private static final String TAG = ParseApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
        ParseObject.registerSubclass(Message.class);


        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.app_id))
                .server(getString(R.string.server))
                .clientKey(null)
                .clientBuilder(builder)
                .build());

        // Need to register GCM token
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.saveInBackground();
        ParsePush.subscribeInBackground("pushChannel");
    }

}
