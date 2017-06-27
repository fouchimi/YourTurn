package com.social.yourturn.utils;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ousma on 6/26/2017.
 */

public class HttpManager {

    private static final String TAG = HttpManager.class.getSimpleName();

    public static  String getData(String baseUrl){
        String data = "";
        HttpURLConnection httpURLConnection = null;

        try{
            Uri buildUri = Uri.parse(baseUrl).buildUpon().build();

            URL url = new URL(buildUri.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

            data = readStream(in);

        }catch (IOException e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }finally {
            if(httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }

        return data;
    }

    private static String readStream(InputStream in) {
        BufferedReader reader =  null;
        StringBuffer data = new StringBuffer();

        try{
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while((line = reader.readLine()) != null){
                data.append(line + "\n");
            }
        }catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }finally {
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException ex){
                    Log.e(TAG, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        return data.toString();
    }
}
