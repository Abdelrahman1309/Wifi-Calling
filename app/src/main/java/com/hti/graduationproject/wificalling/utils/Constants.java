package com.hti.graduationproject.wificalling.utils;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Constants {
    public static final String TAG = "D2D_Final";

    public static final class SharedPref {
        public static final String SHARED_PREF = "D2D_SHARED_PREF";
        public static final String SHARED_PREF_PHONE_NUM = "SHARED_PREF_PHONE_NUM";
        public static final String ASK_NUMBER_DLG = "ASK_NUMBER_DLG";
        public static final String LAST_UPDATED = "LAST_UPDATE: 12:00 AM";

    }

    public static final class Signaling {

        public static final String SIGNALING_SERVICE_ACTION = "SIGNALING_SERVICE_ACTION";
        public static final String SIGNALING_SERVICE_ACTION_MESSAGE = "SIGNALING_SERVICE_ACTION_MESSAGE";
        public static final String SIGNALING_SERVICE_ACTION_IP_ADDRESS = "ACTION_IP_ADDRESS";
        public static final String SIGNALING_MESSAGE = "SIGNALING_MESSAGE";
        public static final short SIGNALING_SERVER_PORT = 9999;
        public static final int MIN_SIGNALING_BUFFER_SIZE = 512;
        public static final String CALL_INVITATION_SIGNAL_PARAM = "_invite_";
        public static final String USER_REGISTER_REQUEST_SIGNAL_PARAM = "_register_";
        public static final String END_CALL_SIGNAL_PARAM = "_endcall_";
    }

    public static final class Calling {
        public static final String CALL_SERVICE_ACTION = "CALL_SERVICE_ACTION";
        public static final int CALLING_SERVER_PORT = 8975;
        public static final String MAKE_CALL_ACTION_PARAM = "MAKE_CALL_ACTION";
        public static final short SAMPLING_RATE = 8000; // 44100 for music
    }


    public static final String NETWORK_PASSWORD = "123456789";

    public static boolean callState;

    private static Map<String, String> mPhonesMap = new HashMap<>();
    private static List<String> mAvailableDevices = new ArrayList<>();

    private static String mDeviceIP;
    private static String mPhoneNumber;
    private static boolean mVOIP;
    //private static String mOtherPhoneNumber;
    private static String mCurrentWifiSSID;
    private static short currentWifiLevel;
    private static short currentCellularLevel;
    private static String wifiSSIDParam;

    public static String getWifiSSIDParam() {
        return wifiSSIDParam;
    }

    public static short getCurrentWifiLevel() {
        return currentWifiLevel;
    }

    public static void setCurrentWifiLevel(short currentWifiLevel) {
        Constants.currentWifiLevel = currentWifiLevel;
    }

    public static void setCurrentCellularLevel(short currentCellularLevel) {
        Constants.currentCellularLevel = currentCellularLevel;
    }

    public static String getCurrentWifiSSID() {
        return mCurrentWifiSSID;
    }

    public static String getDeviceIP() {
        return mDeviceIP;
    }

    public static void setDeviceIP(String ip) {
        mDeviceIP = ip;
    }

    public static String getPhoneNumber() {
        return mPhoneNumber;
    }

    public static void setPhoneNumber(String mPhoneNumber) {
        Constants.mPhoneNumber = mPhoneNumber;
    }


    public static boolean getSipServerState(){
        return mVOIP;
    }

    public static void sipServerState (boolean state){
        Constants.mVOIP = state;
    }

    public static void addNearbyDevice(String number) {
        if (!mAvailableDevices.contains(number) && !number.equals(mPhoneNumber)) {
            mAvailableDevices.add(number);
        }
    }

    public static int getNumberOfNearbyDevice() {
        return mAvailableDevices.size();
    }

    public static String getNearbyDevice(int index) {
        return mAvailableDevices.get(index);
    }

    public static void clearNearbyDevices() {
        mAvailableDevices.clear();
    }

    public static void addNumber(String number, String ip) {

        mPhonesMap.put(number, ip);
    }

    public static Pair<String, String> getPhoneNumber(String phoneNum) {
        if (mPhonesMap.containsKey(phoneNum)) {
            Pair<String, String> pair = new Pair<>(phoneNum, mPhonesMap.get(phoneNum));
            return pair;
        }
        return null;
    }


}