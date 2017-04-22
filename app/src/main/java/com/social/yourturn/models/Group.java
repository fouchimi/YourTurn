package com.social.yourturn.models;

import java.util.List;

/**
 * Created by ousma on 4/22/2017.
 */

public class Group {
    private String name;
    private String thumbnail;
    private List<Contact> contactList;

    public Group(String name, String thumbnail, List<Contact> contactList){
        this.name = name;
        this.thumbnail = thumbnail;
        this.contactList = contactList;
    }

    public Group(String name, List<Contact> contactList){
        this.name = name;
        this.contactList = contactList;
    }

    public String getName() {
        return name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public List<Contact> getContactList() {
        return contactList;
    }
}
