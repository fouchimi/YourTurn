package com.social.yourturn.parsers;

import android.util.Log;

import com.social.yourturn.models.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ousma on 6/26/2017.
 */

public class PlaceParser {
    private final static String TAG = PlaceParser.class.getSimpleName();

    public static Place placeParse(JSONObject jObject){
        Place place = new Place();
        JSONArray jsonArray = null;
        try {
            jsonArray = jObject.getJSONArray("results");
            String name = ((JSONObject) jsonArray.get(jsonArray.length()-1)).getString("name");
            String placeId = ((JSONObject) jsonArray.get(jsonArray.length()-1)).getString("place_id");
            String icon = ((JSONObject) jsonArray.get(jsonArray.length()-1)).getString("icon");
            JSONArray photos = ((JSONObject) jsonArray.get(jsonArray.length()-1)).getJSONArray("photos");
            String placeUrl = ((JSONObject) photos.get(photos.length()-1)).getString("photo_reference");
            place.setId(placeId);
            place.setName(name);
            place.setUrl(placeUrl);
            place.setIcon(icon);
        }catch (JSONException ex) {
            ex.printStackTrace();
            Log.d(TAG, ex.getMessage());
        }

        return place;
    }

    public static String addressParse(JSONObject jObject){
        String address = null;
        try {
            JSONObject result = jObject.getJSONObject("result");
            address = result.getString("formatted_address");
        }catch (JSONException ex){
            ex.printStackTrace();
            Log.d(TAG, ex.getMessage());
        }
        return address;
    }
}
