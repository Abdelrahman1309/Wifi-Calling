package com.hti.graduationproject.wificalling.contacts;


public class Logs {
    private String mName;
    private String mDate;
    private String mTechnology;
    private int mIcon;

    public Logs(String name, int icon, String technology, String date) {
        mName = name;
        mIcon = icon;
        mTechnology = technology;
        mDate = date;
    }

    public String getLogName() {
        return mName;
    }

    public int getLogIcon() {
        return mIcon;
    }

    public String getLogTechnology() {
        return mTechnology;
    }

    public String getLogDate() {
        return mDate;
    }
}
