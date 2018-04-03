package com.hti.graduationproject.wificalling.fragments;


import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.hti.graduationproject.wificalling.R;
import com.hti.graduationproject.wificalling.activities.CallActivity;
import static android.content.Context.POWER_SERVICE;

//Todo (1) Display call from and accept and reject call
//Todo (2) Interact with Service
public class IncomeCallFragment extends Fragment {
    String incomePhoneNumber;
    String callTechnology;
    TextView displayIncomePhoneNum;
    Uri ringTone;
    Ringtone r;

    public IncomeCallFragment() {
        // Required empty public constructor
    }

    public void setPhoneNumber(String number) {
        incomePhoneNumber = number;
    }

    public void callTech(String tech) {
        callTechnology = tech;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_income_call, container, false);
        ImageView mAnswer = v.findViewById(R.id.answerCallFrag);
        ImageView mReject = v.findViewById(R.id.rejectCallFrag);
        displayIncomePhoneNum = v.findViewById(R.id.income_phone_num);

        displayIncomePhoneNum.setText(incomePhoneNumber);
        if (callTechnology.equals("D2D")) {
            ringTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(getContext(), ringTone);
            r.play();
        }

        try {
            PowerManager.WakeLock screenLock = ((PowerManager) getActivity().getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLock.acquire(1000L );
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }catch (NullPointerException ex){
            ex.printStackTrace();

        }

        mAnswer.setOnClickListener(v1 -> {
            onAnswerClicked();
            displayIncomePhoneNum.clearComposingText();
            try {
                r.stop();
            } catch (NullPointerException ex) {

            }
        });

        mReject.setOnClickListener(v1 -> {
            onRejectClicked();
            displayIncomePhoneNum.clearComposingText();
            try {
                r.stop();
            } catch (NullPointerException ex) {

            }
        });

        return v;
    }


    private void onAnswerClicked() {
        Log.i("Incomming Call", "Answer Btn Clicked");
        ((CallActivity) getActivity()).answerCall();
    }

    private void onRejectClicked() {
        Log.i("Incomming Call", "Reject Btn Clicked");
        ((CallActivity) getActivity()).endCall();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            r.stop();
        } catch (NullPointerException ex) {

        }
    }
}
