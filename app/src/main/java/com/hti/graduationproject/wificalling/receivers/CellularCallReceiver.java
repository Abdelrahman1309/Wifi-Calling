package com.hti.graduationproject.wificalling.receivers;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.hti.graduationproject.wificalling.utils.Constants;

import java.io.IOException;

//Todo Compare received call phone number with VOIP call number - done
//Todo if phone numbers are equal open call and end voip call - done at any android below 5
public class CellularCallReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Log.i(Constants.TAG, "OnReceive CellularCallReceiver");
        String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
        String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);


        if (stateStr != null && number != null) {
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.i(Constants.TAG, "Phone is Idle");
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.i(Constants.TAG, "Incoming call and the number is: " + number);
                if (Constants.getPhoneNumber().equals(number)) {
                    try {
                        Runtime.getRuntime()
                                .exec("input keyevent " + Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
            }
        }
    }
}
