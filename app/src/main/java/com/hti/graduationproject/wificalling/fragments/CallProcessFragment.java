package com.hti.graduationproject.wificalling.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hti.graduationproject.wificalling.App;
import com.hti.graduationproject.wificalling.R;
import com.hti.graduationproject.wificalling.activities.CallActivity;
import com.hti.graduationproject.wificalling.contacts.Contacts;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


//Todo (1) Start Call Time Counter
//Todo (2) End calls
public class CallProcessFragment extends Fragment {
    String phoneNumber;
    TextView displayNumber,timer,displayName;
    ImageView speaker,endCall,keypad,mute;
    boolean isOddClicked = true;
    AudioManager audioManager;

    public CallProcessFragment() {
        // Required empty public constructor
    }

    public void setPhoneNumber(String number) {
        phoneNumber = number;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_call_process, container, false);

        endCall       = v.findViewById(R.id.endCallFragBtn);
        keypad        = v.findViewById(R.id.keypad);
        mute          = v.findViewById(R.id.mute);
        displayNumber = v.findViewById(R.id.display_phone_num);
        displayName   = v.findViewById(R.id.display_name);
        speaker       = v.findViewById(R.id.speaker);
        timer         = v.findViewById(R.id.timer);

        IntentFilter intentFilter = new IntentFilter("TIMER");
        intentFilter.addAction("CALL_ENDED");
        getActivity().registerReceiver(mTimerBroadCastReceiver, intentFilter);

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(false);

        //Set VOIP Call disconnected listner
        ((App)getActivity().getApplication()).getAbtoPhone().setCallDisconnectedListener(this::endCall);

        ArrayList<Contacts> contacts = getContactsList("CONTACTS");
        displayNumber.setText(phoneNumber);
        try {
            for (Contacts d : contacts) {
                if (d.getContactNumber() != null && d.getContactNumber().equals(phoneNumber)) {
                    displayName.setText(d.getContactName());
                }
                //something here
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        endCall.setOnClickListener(v1 -> endCall());

        speaker.setOnClickListener(v1 -> {
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);

            if (isOddClicked) {
                audioManager.setSpeakerphoneOn(true);
                speaker.getDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                isOddClicked = false;
            } else {
                audioManager.setSpeakerphoneOn(false);
                speaker.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                isOddClicked = true;
            }


        });
        keypad.setOnClickListener(v1 -> {

        });
        mute.setOnClickListener(v1 -> {

        });
        return v;
    }

    private void endCall(String s, int i, int i1){
        endCall();
    }

    private void endCall() {
        ((CallActivity) getActivity()).endCall();
        displayNumber.clearComposingText();
        isOddClicked = true;
        speaker.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

    }
    public ArrayList<Contacts> getContactsList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Contacts>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private BroadcastReceiver mTimerBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("TIMER")) {
                Date dateObject = new Date(intent.getLongExtra("Timer",0));
                SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
                timer.setText(timeFormat.format(dateObject));
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mTimerBroadCastReceiver);
    }
}