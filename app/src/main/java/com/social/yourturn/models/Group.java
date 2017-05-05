package com.social.yourturn.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ousma on 4/22/2017.
 */

public class Group implements Parcelable{
    private String name;
    private String thumbnail;
    private ArrayList<Contact> contactList = new ArrayList<>();

    public Group(){

    }

    public Group(String name, String thumbnail, ArrayList<Contact> contactList){
        this.name = name;
        this.thumbnail = thumbnail;
        this.contactList = contactList;
    }

    public Group(String name, ArrayList<Contact> contactList){
        this.name = name;
        this.contactList = contactList;
    }

    public String getName() {
        return name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public ArrayList<Contact> getContactList() {
        return contactList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Group(Parcel in){
        name = in.readString();
        thumbnail = in.readString();
        in.readTypedList(contactList, Contact.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(thumbnail);
        dest.writeTypedList(contactList);
    }

    public static Creator<Group> CREATOR = new Creator<Group>() {

        @Override
        public Group createFromParcel(Parcel source) {
            return new Group(source);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }

    };

    public void setContactList(ArrayList<Contact> contactList) {
        this.contactList = contactList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
