package com.social.yourturn.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ousma on 4/22/2017.
 */

public class Event implements Parcelable{
    private String eventId;
    private String name;
    private String eventUrl;
    private long dateInMillis;
    private String groupCreator;
    private String groupUserRef;
    private ArrayList<Contact> contactList = new ArrayList<>();
    private int size;

    public Event(){

    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getEventUrl() {
        return eventUrl;
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

    private Event(Parcel in){
        eventId = in.readString();
        name = in.readString();
        eventUrl = in.readString();
        dateInMillis = in.readLong();
        groupCreator = in.readString();
        groupUserRef = in.readString();
        in.readTypedList(contactList, Contact.CREATOR);
        size = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventId);
        dest.writeString(name);
        dest.writeString(eventUrl);
        dest.writeLong(dateInMillis);
        dest.writeString(groupCreator);
        dest.writeString(groupUserRef);
        dest.writeTypedList(contactList);
        dest.writeInt(size);
    }

    public static Creator<Event> CREATOR = new Creator<Event>() {

        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }

    };

    public void setContactList(ArrayList<Contact> contactList) {
        this.contactList = contactList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEventUrl(String eventUrl) {
        this.eventUrl = eventUrl;
    }

    public void setGroupUserRef(String groupUserRef) {
        this.groupUserRef = groupUserRef;
    }

    public String getGroupUserRef() {
        return groupUserRef;
    }
}
