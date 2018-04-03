package com.hti.graduationproject.wificalling.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.hti.graduationproject.wificalling.activities.CallActivity;

import org.abtollc.sdk.AbtoApplication;
import org.abtollc.sdk.AbtoPhone;
import org.abtollc.sdk.OnIncomingCallListener;

public class IncomingVIOPCallService extends Service implements OnIncomingCallListener {

    private AbtoPhone abtoPhone;

    private String mTag;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mTag = this.getClass().getSimpleName();

        Log.i(mTag, "IncomingVIOPCallService started");
        abtoPhone = ((AbtoApplication) getApplication()).getAbtoPhone();

        abtoPhone.setIncomingCallListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1,new Notification());
        }

        return START_STICKY;
    }

    @Override
    public void OnIncomingCall(String remoteContact, long arg1) {
        Log.i(mTag, String.format("There are income call from voip server number is: %s", remoteContact));
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("CALL_TYPE", "INCOMING");
        intent.putExtra("CALL_TECH", "VOIP");
        intent.putExtra("CALL_ID", abtoPhone.getActiveCallId());
        intent.putExtra(AbtoPhone.REMOTE_CONTACT, remoteContact);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
