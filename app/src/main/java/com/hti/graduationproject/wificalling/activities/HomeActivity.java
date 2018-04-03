package com.hti.graduationproject.wificalling.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.hti.graduationproject.wificalling.R;
import com.hti.graduationproject.wificalling.contacts.AvailableContacts;
import com.hti.graduationproject.wificalling.contacts.AvailableContactsAdapter;
import com.hti.graduationproject.wificalling.contacts.Contacts;
import com.hti.graduationproject.wificalling.fragments.ContactsListFragment;
import com.hti.graduationproject.wificalling.fragments.LogsListFragment;
import com.hti.graduationproject.wificalling.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

//Todo (1) Receive New wifi networks
//Todo (2) Send Phone Call Intent to CallActivity
//Todo (3) Display Powers
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    FragmentTransaction transaction;
    Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, star, hash, contact, recents;
    ImageView backSpace, mCall, refresh;
    TextView mPhone,lastUpdate;
    SearchView search;
    FrameLayout searchBar;
    SharedPreferences prefs;
    String devicePhoneNumber,update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        identifyNumbers();
        prefs = getSharedPreferences(Constants.SharedPref.SHARED_PREF, MODE_PRIVATE);
        update = prefs.getString(Constants.SharedPref.LAST_UPDATED, "12:00 am");
        lastUpdate.setText("Last update: "+ update);
        searchBar.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = getIntent();
        String number = i.getStringExtra("number");
        mPhone.setText(number);
        searchBar.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        Constants.clearNearbyDevices();
        super.onPause();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn0:
                mPhone.append("0");
                break;
            case R.id.btn1:
                mPhone.append("1");
                break;
            case R.id.btn2:
                mPhone.append("2");
                break;
            case R.id.btn3:
                mPhone.append("3");
                break;
            case R.id.btn4:
                mPhone.append("4");
                break;
            case R.id.btn5:
                mPhone.append("5");
                break;
            case R.id.btn6:
                mPhone.append("6");
                break;
            case R.id.btn7:
                mPhone.append("7");
                break;
            case R.id.btn8:
                mPhone.append("8");
                break;
            case R.id.btn9:
                mPhone.append("9");
                break;
            case R.id.star:
                mPhone.append("*");
                break;
            case R.id.hash:
                mPhone.append("#");
                break;
            case R.id.back_space:
                try {
                    mPhone.setText(mPhone.getText().toString().substring(0, mPhone.getText().toString().length() - 1));
                } catch (Exception ex) {

                }
                break;
            case R.id.call:
                checkNumber();
                break;
            case R.id.contact:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_contacts, new ContactsListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.search_frame:
                searchBar.setVisibility(View.INVISIBLE);
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_contacts, new ContactsListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.recents:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_logs, new LogsListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            case R.id.refresh:
                ListView listView = findViewById(R.id.available_contacts);
                ArrayList<AvailableContacts> availableContacts = availableContacts();
                listView.setAdapter(new AvailableContactsAdapter(getApplication(), availableContacts));
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, EEE");
                String time = sdf.format(cal.getTime());
                SharedPreferences.Editor editor = getSharedPreferences(Constants.SharedPref.SHARED_PREF, Context.MODE_PRIVATE).edit();
                editor.putString(Constants.SharedPref.LAST_UPDATED, time);
                editor.apply();
                lastUpdate.setText("Last update: "+ time);
        }


    }

    @Override
    public void onBackPressed() {
        searchBar.setVisibility(View.VISIBLE);
        super.onBackPressed();
    }

    private void identifyNumbers() {
        btn0      = findViewById(R.id.btn0);
        btn1      = findViewById(R.id.btn1);
        btn2      = findViewById(R.id.btn2);
        btn3      = findViewById(R.id.btn3);
        btn4      = findViewById(R.id.btn4);
        btn5      = findViewById(R.id.btn5);
        btn6      = findViewById(R.id.btn6);
        btn7      = findViewById(R.id.btn7);
        btn8      = findViewById(R.id.btn8);
        btn9      = findViewById(R.id.btn9);
        star      = findViewById(R.id.star);
        hash      = findViewById(R.id.hash);
        mCall     = findViewById(R.id.call);
        mPhone    = findViewById(R.id.phoneNum);
        refresh   = findViewById(R.id.refresh);
        backSpace = findViewById(R.id.back_space);
        contact   = findViewById(R.id.contact);
        recents   = findViewById(R.id.recents);
        search    = findViewById(R.id.search);
        searchBar = findViewById(R.id.search_frame);
        lastUpdate= findViewById(R.id.last_update);

        btn0.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        star.setOnClickListener(this);
        hash.setOnClickListener(this);
        backSpace.setOnClickListener(this);
        mCall.setOnClickListener(this);
        contact.setOnClickListener(this);
        recents.setOnClickListener(this);
        searchBar.setOnClickListener(this);
        refresh.setOnClickListener(this);
    }

    private void makeCall() {

        String phoneNum = mPhone.getText().toString();
        Intent i = new Intent(this, com.hti.graduationproject.wificalling.activities.CallActivity.class);
        i.putExtra("PHONE_NUM", phoneNum);
        i.putExtra("CALL_TYPE", "OUTGOING");

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Make Call")
                .setMessage("Please Select call technology")
                .setPositiveButton("VOIP", (dialog, which) -> {
                    i.putExtra("CALL_TECH", "VOIP");
                    if(Constants.getSipServerState()) {
                        startActivity(i);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "SIP Server Not Registered", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                })
                .setNegativeButton("D2D", (dialog, which) -> {
                    // do nothing
                    i.putExtra("CALL_TECH", "D2D");
                    String availableDevice = String.valueOf(Constants.getPhoneNumber(mPhone.getText().toString()));
                    if (availableDevice.contains(mPhone.getText().toString())) startActivity(i);
                    else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Number Not Found in this Cell", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                })
                .setNeutralButton("Cellular", (dialog, which) -> {
                    //Todo - make cellular call
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNum, null));
                    startActivity(intent);

                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void checkNumber() {
        prefs = getSharedPreferences(Constants.SharedPref.SHARED_PREF, MODE_PRIVATE);
        devicePhoneNumber = prefs.getString(Constants.SharedPref.SHARED_PREF_PHONE_NUM, "SHARED_PREF_PHONE_NUM");
        if (!mPhone.getText().toString().isEmpty() && !mPhone.getText().toString().equals(devicePhoneNumber))
            makeCall();
        else {
            Toast toast = Toast.makeText(getApplicationContext(), "Invalid number", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private ArrayList<AvailableContacts> availableContacts() {
        ArrayList<Contacts> contacts = getContactsList("CONTACTS");
        Contacts user;
        ArrayList<AvailableContacts> availableContacts = new ArrayList<>();
        try {
            for (int i = 0; i < Constants.getNumberOfNearbyDevice(); i++) {
                String nearbyDevice = Constants.getNearbyDevice(i);
                for (int j = 0; j < contacts.size(); j++) {
                    user = contacts.get(j);
                    if (user.getContactNumber() != null && user.getContactNumber().equals(nearbyDevice)) {
                        availableContacts.add(new AvailableContacts(user.getContactName()));
                    }
                }

            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        return availableContacts;
    }

    public ArrayList<Contacts> getContactsList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Contacts>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}