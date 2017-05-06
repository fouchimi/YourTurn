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
    private String thumbnailUrl;
    private String phoneNumber;
    private int position;
    private String share;

    public Contact(String id, String name, String phoneNumber){
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
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

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
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
        share = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(thumbnailUrl);
        dest.writeString(phoneNumber);
        dest.writeString(share);
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>(){
        public Contact createFromParcel(Parcel in){
            return new Contact(in);
        }

        public Contact[] newArray(int size){
            return new Contact[size];
        }
    };

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Contact))return false;
        Contact contact = (Contact) other;
        if(this.getId().equals(contact.getId()) && (
                this.getName().equals(contact.getName()))
                && this.getPhoneNumber().equals(contact.getPhoneNumber())) return true;
        else return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + phoneNumber.hashCode();
        return result;
    }
}
