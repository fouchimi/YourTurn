package com.social.yourturn.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.social.yourturn.LocationActivity;
import com.social.yourturn.models.Place;
import com.social.yourturn.parsers.PlaceParser;
import com.social.yourturn.utils.HttpManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ousma on 6/19/2017.
 */

public class FetchPlaceTask extends AsyncTask<String, Void, Place> {

    private final static String TAG = FetchPlaceTask.class.getSimpleName();

    private Context mContext;

    public FetchPlaceTask(Context context){
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Place doInBackground(String... params) {
        String placeUrl = params[0];
        String result = HttpManager.getData(placeUrl);
        Place place = null;
        try {
            JSONObject resultObject = new JSONObject(result);
            place = PlaceParser.placeParse(resultObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return place;
    }

    @Override
    protected void onPostExecute(Place place) {
        super.onPostExecute(place);
        LocationActivity locationActivity = (LocationActivity) mContext;
        locationActivity.onComplete(place);
    }

    public interface PlaceListener {
        public void onComplete(Place place);
    }
}
