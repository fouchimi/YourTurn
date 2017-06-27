package com.social.yourturn.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.social.yourturn.LocationActivity;
import com.social.yourturn.parsers.PlaceParser;
import com.social.yourturn.utils.HttpManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ousma on 6/26/2017.
 */

public class FetchPlaceAddressTask extends AsyncTask<String, Void, String> {
    private final static String TAG = FetchPlaceAddressTask.class.getSimpleName();

    private Context mContext;

    public FetchPlaceAddressTask(Context context){
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "start fetching address ..");
    }

    @Override
    protected String doInBackground(String... params) {
        String addressUrl = params[0];
        String result = HttpManager.getData(addressUrl);
        String place = null;
        try {
            JSONObject resultObject = new JSONObject(result);
            place = PlaceParser.addressParse(resultObject);
        }catch (JSONException ex){

        }
        return place;
    }

    @Override
    protected void onPostExecute(String address) {
        super.onPostExecute(address);
        if(address != null){
            LocationActivity locationActivity = (LocationActivity) mContext;
            locationActivity.onAddressComplete(address);
        }
    }

    public interface AddressListener{
        public void onAddressComplete(String address);
    }
}
