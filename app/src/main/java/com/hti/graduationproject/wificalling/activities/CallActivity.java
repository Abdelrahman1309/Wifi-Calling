package com.hti.graduationproject.wificalling.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.hti.graduationproject.wificalling.R;
import com.hti.graduationproject.wificalling.contacts.Contacts;
import com.hti.graduationproject.wificalling.contacts.Logs;
import com.hti.graduationproject.wificalling.fragments.CallProcessFragment;
import com.hti.graduationproject.wificalling.fragments.IncomeCallFragment;
import com.hti.graduationproject.wificalling.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.abtollc.sdk.AbtoApplication;
import org.abtollc.sdk.AbtoPhone;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

//Todo (1) regarding to received intent display (Income call or outcome call)
//Todo (2) Interact with call service

/**
 * There are two components can fire this activity
 *
 * @link {HomeActivity.class}
 * @link SignlingService
 * <p>
 * This activity will manage call events:
 * - Incomming call
 * - Outgoing call
 * - end call
 * At incoming call
 * it will push Incoming call fragment
 * At Accept or reject, This activity must send signal to signaling server
 */
public class CallActivity extends FragmentActivity {

    private static String TAG;
    private static ArrayList<Logs> mLogs;
    long timer = 0; boolean callCheck = true;
    //Fragments
    private IncomeCallFragment mCallFragment;
    private CallProcessFragment mProcessCallFrag;

    //Call process
    private String mDeviceIP = null;
    private String mDevicePhoneNumber = null;
    private String mIncomePhoneNumber = null;
    private String mCallTech = null;
    //Broadcast receiver
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //In case user accept call
            //open call Instance by sending broadcast to calling service
            if (intent.getAction() != null && intent.getAction().equals("CALL_ACCEPTED")) {
                Intent i = new Intent();
                i.setAction(Constants.Calling.CALL_SERVICE_ACTION);
                i.putExtra(Constants.Calling.MAKE_CALL_ACTION_PARAM, mDeviceIP);
                sendBroadcast(i);
                startTimer();
                try {
                    pushCallProcessFragment(mDevicePhoneNumber);
                } catch (IllegalStateException ex) {
                    Log.e(TAG, "There are an error in CallActivity. ", ex);
                }
            } else if (intent.getAction() != null && intent.getAction().equals("CALL_ENDED")) {
                Intent i = new Intent();
                i.setAction(Constants.Calling.CALL_SERVICE_ACTION);
                i.putExtra("END", "END");
                sendBroadcast(i);
                CallActivity.this.finish();
            }
        }
    };

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        mLogs = getLogsList("LOGS");
        if (mLogs == null) mLogs = new ArrayList<>();

        TAG = this.getClass().getSimpleName();
        //register broadcast receiver
        IntentFilter intentFilter = new IntentFilter("CALL_ACCEPTED");
        intentFilter.addAction("CALL_ENDED");
        registerReceiver(mBroadcastReceiver, intentFilter);

        //Get Intent and put it in global intent var
        mIntent = getIntent();
        //Get call tech if D2D
        mCallTech = mIntent.getStringExtra("CALL_TECH");
        //Get Phone Number
        mDevicePhoneNumber = mIntent.getStringExtra("PHONE_NUM");

        Log.i(TAG, String.format("Phone number is %s ", mDevicePhoneNumber));
        //Process Intent, by get call type
        if (mIntent.getStringExtra("CALL_TYPE").equals("OUTGOING")) {
            if (mCallTech.equals("D2D")) {
                //Case D2D outgoing call
                try {
                    mDeviceIP = Constants.getPhoneNumber(mDevicePhoneNumber).second;//Second is device ip
                } catch (Exception ex) {
                    Log.d(TAG, "Device not found please handle it");
                    this.finish();
                }
                mCallTech = "D2D";
                Log.i(TAG, String.format("Device IP is: %s", mDeviceIP));
                //send Signal to recipient device
                String signalMsg = "_invite_##" + Constants.getPhoneNumber();
                sendSignal(signalMsg);
                Intent intent = new Intent();
                intent.setAction(Constants.Calling.CALL_SERVICE_ACTION);
                intent.putExtra("OUTGOING", "OUTGOING");
                sendBroadcast(intent);

                addLogs(mDevicePhoneNumber, R.drawable.forward_call, "D2D");

                // push Call process fragment
                pushCallProcessFragment(mDevicePhoneNumber);
            } else if (mCallTech.equals("VOIP")) {
                AbtoPhone abtoPhone = ((AbtoApplication) getApplication()).getAbtoPhone();
                abtoPhone.setCallConnectedListener(s -> startTimer());
                try {
                    abtoPhone.startCall(mDevicePhoneNumber, abtoPhone.getCurrentAccountId());
                    addLogs(mDevicePhoneNumber, R.drawable.forward_call, "VOIP");
                    pushCallProcessFragment(mDevicePhoneNumber);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    this.finish();
                }
            }
        } else if (mIntent.getStringExtra("CALL_TYPE").equals("INCOMING")) {
            Log.i(TAG, "Incoming call");
            //check if incoming call tech is d2d
            if (mCallTech != null && mIntent.getStringExtra("CALL_TECH").equals("D2D")) {
                mDeviceIP = mIntent.getStringExtra("PHONE_IP");
                mIncomePhoneNumber = mIntent.getStringExtra("PHONE_NUM");
                mIncomePhoneNumber = mIncomePhoneNumber.substring(0, 11);
                Log.i(TAG, String.format("Incoming Phone ip is: %s", mDeviceIP));

            } else if (mCallTech != null && mIntent.getStringExtra("CALL_TECH").equals("VOIP")) {
                mIncomePhoneNumber = mIntent.getStringExtra(AbtoPhone.REMOTE_CONTACT);
                mIncomePhoneNumber = mIncomePhoneNumber.substring(1, 12);
                Log.i(TAG, String.format("Incoming Phone no is:%s", mIncomePhoneNumber));

            }
            //Case incoming call p ush Call incoming call fragment
            if (mIncomePhoneNumber != null) {
                pushIncomingCallFragment(mIncomePhoneNumber, mIntent.getStringExtra("CALL_TECH"));
            }
        }

    }

    public void answerCall() {
        if (mCallTech.equals("D2D")) {
            Log.i(TAG, "Answer call invoked");
            //Start Calling Server By sending broadcast to CallService
            Intent i = new Intent();
            i.setAction(Constants.Calling.CALL_SERVICE_ACTION);
            sendBroadcast(i);
            //Send accept to recipient By send broadcast to signaling server to send it
            i = new Intent();
            i.setAction(Constants.Signaling.SIGNALING_SERVICE_ACTION);
            i.putExtra(Constants.Signaling.SIGNALING_SERVICE_ACTION_MESSAGE, "_accept_");
            sendBroadcast(i);
            pushCallProcessFragment(mDevicePhoneNumber);
            addLogs(mIncomePhoneNumber, R.drawable.income_call, "D2D");

        } else if (mCallTech.equals("VOIP")) {
            AbtoPhone abtoPhone = ((AbtoApplication) getApplication()).getAbtoPhone();
            try {
                abtoPhone.answerCall(200);
                addLogs(mIncomePhoneNumber, R.drawable.income_call, "VOIP");
                abtoPhone.setCallDisconnectedListener((s, i, i1) -> this.finish());
                pushCallProcessFragment(mDevicePhoneNumber);
                startTimer();

            } catch (RemoteException e) {
                e.printStackTrace();
                this.finish();
            }
        }

    }

    public void endCall() {
        if (mCallTech.equals("D2D")) {
            Log.i(TAG, "End call invoked");
            //End Calling Server By sending broadcast to CallService
            Intent i = new Intent();
            i.setAction(Constants.Calling.CALL_SERVICE_ACTION);
            i.putExtra("END", "END");
            sendBroadcast(i);
            //Send end to recipient By send broadcast to signaling server to send it
            i = new Intent();
            i.setAction(Constants.Signaling.SIGNALING_SERVICE_ACTION);
            i.putExtra(Constants.Signaling.SIGNALING_SERVICE_ACTION_MESSAGE, "_end_");
            i.putExtra(Constants.Signaling.SIGNALING_SERVICE_ACTION_IP_ADDRESS, mDeviceIP);
            sendBroadcast(i);
            //finish this activity
            callCheck = false;
            this.finish();
        } else if (mCallTech.equals("VOIP")) {
            AbtoPhone abtoPhone = ((AbtoApplication) getApplication()).getAbtoPhone();
            abtoPhone.setCallDisconnectedListener(null);
            try {
                callCheck = false;
                abtoPhone.hangUp();
                CallActivity.this.finish();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    private void sendSignal(String signal) {
        Intent i = new Intent();
        i.setAction(Constants.Signaling.SIGNALING_SERVICE_ACTION);
        i.putExtra(Constants.Signaling.SIGNALING_SERVICE_ACTION_MESSAGE, signal);
        if (mDeviceIP != null)
            i.putExtra(Constants.Signaling.SIGNALING_SERVICE_ACTION_IP_ADDRESS, mDeviceIP);
        sendBroadcast(i);
        Log.i(TAG, "BroadCast sent");
    }

    private void pushIncomingCallFragment(String phoneNumber, String tech) {
        if (mCallFragment == null) mCallFragment = new IncomeCallFragment();
        mCallFragment.setPhoneNumber(phoneNumber);
        mCallFragment.callTech(tech);
        mCallFragment.setArguments(mIntent.getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragsContainer, mCallFragment)
                .commit();
    }

    private void pushCallProcessFragment(String phoneNumber) {

        if (mProcessCallFrag == null) mProcessCallFrag = new CallProcessFragment();
        mProcessCallFrag.setPhoneNumber(phoneNumber);
        mProcessCallFrag.setArguments(mIntent.getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragsContainer, mProcessCallFrag).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        saveLogsList(mLogs, "LOGS");
        callCheck = false;
        timer = 0;

    }

    private void addLogs(String phoneNumber, int iconResId, String CallTech) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        String time = sdf.format(cal.getTime());
        ArrayList<Contacts> contacts = getContactsList("CONTACTS");
        Log.w("phoneNo", phoneNumber);
        String contactName = phoneNumber;
        try {
            for (int j = 0; j < contacts.size(); j++) {
                Contacts user = contacts.get(j);
                if (user.getContactNumber() != null && user.getContactNumber().equals(phoneNumber)) {
                    contactName = user.getContactName();
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        mLogs.add(new Logs(contactName, iconResId, CallTech, time));
    }

    public ArrayList<Contacts> getContactsList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Contacts>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveLogsList(ArrayList<Logs> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public void startTimer(){
        Thread thread = new Thread(() -> {
            while(callCheck) {
                try {
                    Intent i = new Intent();
                    i.setAction("TIMER");
                    i.putExtra("Timer",timer);
                    sendBroadcast(i);
                    Thread.sleep(1000);
                    timer = timer + 1000;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public ArrayList<Logs> getLogsList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Logs>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

}
