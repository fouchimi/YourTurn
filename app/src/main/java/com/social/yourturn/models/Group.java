package com.social.yourturn.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ousma on 4/22/2017.
 */

public class Group implements Parcelable{
    private String groupId;
    private String name;
    private String thumbnail;
    private long dateInMillis;
    private String groupCreator;
    private String groupUserRef;
    private ArrayList<Contact> contactList = new ArrayList<>();
    private int size;

    public Group(){

    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setGroupCreator(String groupCreator) {
        this.groupCreator = groupCreator;
    }

    public String getGroupCreator() {
        return groupCreator;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public ArrayList<Contact> getContactList() {
        return contactList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Group(Parcel in){
        groupId = in.readString();
        name = in.readString();
        thumbnail = in.readString();
        dateInMillis = in.readLong();
        groupCreator = in.readString();
        groupUserRef = in.readString();
        in.readTypedList(contactList, Contact.CREATOR);
        size = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupId);
        dest.writeString(name);
        dest.writeString(thumbnail);
        dest.writeLong(dateInMillis);
        dest.writeString(groupCreator);
        dest.writeString(groupUserRef);
        dest.writeTypedList(contactList);
        dest.writeInt(size);
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

    public void setDateInMillis(long dateInMillis) {
        this.dateInMillis = dateInMillis;
    }

    public long getDateInMillis() {
        return dateInMillis;
    }

    public void setGroupUserRef(String groupUserRef) {
        this.groupUserRef = groupUserRef;
    }

    public String getGroupUserRef() {
        return groupUserRef;
    }
}
