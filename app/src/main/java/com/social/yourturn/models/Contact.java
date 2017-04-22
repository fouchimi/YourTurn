package com.social.yourturn.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ousma on 4/15/2017.
 */

public class Contact implements Parcelable, Serializable {
    private String id;
    private String name;
    private boolean selected;
    private String thumbnailUrl;
    private String phoneNumber;
    private int position;

    public Contact(String id, String name){
        this.id = id;
        this.name = name;
    }

    public Contact(String id, String name, String phoneNumber){
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public Contact(String id, String name, String phoneNumber, String thumbnailUrl){
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPosition(int position){
        this.position = position;
    }

    public int getPosition(){
        return  position;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Contact(Parcel in){
        id = in.readString();
        name = in.readString();
        thumbnailUrl = in.readString();
        phoneNumber  = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(thumbnailUrl);
        dest.writeString(phoneNumber);
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>(){
        public Contact createFromParcel(Parcel in){
            return new Contact(in);
        }

        public Contact[] newArray(int size){
            return new Contact[size];
        }
    };
}
