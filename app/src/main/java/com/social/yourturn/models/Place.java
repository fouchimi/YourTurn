package com.social.yourturn.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ousma on 6/26/2017.
 */

public class Place implements Parcelable {
    private String id;
    private String name;
    private String url;
    private String icon;

    public Place(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Place(Parcel in){
        id = in.readString();
        name = in.readString();
        url = in.readString();
        icon = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(icon);
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>(){
        public Place createFromParcel(Parcel in){
            return new Place(in);
        }

        public Place[] newArray(int size){
            return new Place[size];
        }
    };
}
