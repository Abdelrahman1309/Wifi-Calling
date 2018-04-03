package com.hti.graduationproject.wificalling.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.hti.graduationproject.wificalling.R;
import com.hti.graduationproject.wificalling.contacts.Logs;
import com.hti.graduationproject.wificalling.contacts.LogsAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;


public class LogsListFragment extends Fragment {
    ListView listView;
    ArrayList<Logs> logs = new ArrayList<>();
    ImageView trash;

    public LogsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_logs_list, container, false);
        v.setBackgroundColor(Color.WHITE);
        listView = v.findViewById(R.id.logs_list_view);
        logs = getLogsList("LOGS");
        try {
            Collections.reverse(logs);
            listView.setAdapter(new LogsAdapter(getActivity(), logs));
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        trash = v.findViewById(R.id.trash);
        trash.setOnClickListener(v1 -> {
            ArrayList<Logs> clearLogs = new ArrayList<>();
            saveLogsList(clearLogs, "LOGS");
            logs = getLogsList("LOGS");
            listView.setAdapter(new LogsAdapter(getActivity(), logs));
        });

        return v;
    }

    public ArrayList<Logs> getLogsList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Logs>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveLogsList(ArrayList<Logs> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }
}
