package com.hti.graduationproject.wificalling.contacts;


public class Contacts {
    private String mName;
    private String mNumber;

    public Contacts(String name, String number) {
        mName = name;
        mNumber = number;
    }

    public String getContactName() {
        return mName;
    }

    public String getContactNumber() {
        return mNumber;
    }
}
