package com.hti.graduationproject.wificalling.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * This class will handle getting phone number
 */

public class PhoneUtils {


    /**
     * This method will try to get cashed phone number,
     * if not available it will get it from sim card
     * if not available it will try to get it from contacts.
     * if all of above not available it will display dialog to tell user he must add phone number,
     * then it will cashed it for future
     *
     * @param activity Activity needs phone number
     * @return phone number string
     */
    public static String getPhoneNumber(Activity activity) {
        //String phoneNum = getCashedPhoneNumber(activity);

        try {
            String phoneNum = getPhoneNumberFromSim(activity);
            if (phoneNum != null && !phoneNum.isEmpty()) {
                return phoneNum;
            }
            phoneNum = getPhoneNumberFromContacts(activity);

            if (phoneNum != null && !phoneNum.isEmpty()) {
                return phoneNum;
            }
            phoneNum = getCashedPhoneNumber(activity);
            if (!phoneNum.equals("")) {
                return phoneNum;
            }
        } catch (Exception ex) {
            Log.i("Get phone number", "Failed");
        }
        return null;
    }

    private static String getCashedPhoneNumber(Activity activity) {
        return activity.getSharedPreferences(Constants.SharedPref.SHARED_PREF, Context.MODE_PRIVATE).getString(Constants.SharedPref.SHARED_PREF_PHONE_NUM, "");
    }

    private static String getPhoneNumberFromContacts(Activity activity) {
        String s1 = "";
        String main_data[] = {"data1", "is_primary", "data3", "data2", "data1", "is_primary", "photo_uri", "mimetype"};
        Object object = activity.getContentResolver().query(Uri.withAppendedPath(android.provider.ContactsContract.Profile.CONTENT_URI, "data"),
                main_data, "mimetype=?",
                new String[]{"vnd.android.cursor.item/phone_v2"},
                "is_primary DESC");
        if (object != null) {
            do {
                if (!((Cursor) (object)).moveToNext())
                    break;
                s1 = ((Cursor) (object)).getString(4);
            } while (true);
            ((Cursor) (object)).close();
        }
        Log.i(Constants.TAG, "Phone Number: " + s1);
        return s1;

    }

    private static String getPhoneNumberFromSim(Activity activity) {
        TelephonyManager tMgr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }


}
