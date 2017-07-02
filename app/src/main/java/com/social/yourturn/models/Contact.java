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
    private boolean selected;
    private String share;
    private String requested;
    private String paid;

    public Contact() {

    }

    public Contact(String id, String name, String phoneNumber){
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getShare() {
        return share;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public String getPaid() {
        return paid;
    }

    public void setRequested(String requested) {
        this.requested = requested;
    }

    public String getRequested() {
        return requested;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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
        requested = in.readString();
        paid = in.readString();
        selected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(thumbnailUrl);
        dest.writeString(phoneNumber);
        dest.writeString(share);
        dest.writeString(requested);
        dest.writeString(paid);
        dest.writeByte((byte) (selected ? 1 : 0));

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
