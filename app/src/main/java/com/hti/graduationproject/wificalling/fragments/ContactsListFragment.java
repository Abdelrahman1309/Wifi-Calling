package com.hti.graduationproject.wificalling.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import com.hti.graduationproject.wificalling.R;
import com.hti.graduationproject.wificalling.activities.HomeActivity;
import com.hti.graduationproject.wificalling.contacts.Contacts;
import com.hti.graduationproject.wificalling.contacts.ContactsAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;


public class ContactsListFragment extends Fragment {
    SearchView editSearch;
    ListView listView;
    ArrayList<Contacts> contacts;
    int numberOfContacts;

    public ContactsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_contact_list, container, false);
        v.setBackgroundColor(Color.WHITE);
        listView = v.findViewById(R.id.contact_list_view);
        contacts = getContactsList("CONTACTS");
        editSearch = getActivity().findViewById(R.id.search);

        if (contacts != null) {
            updateUI(contacts);
            numberOfContacts = contacts.size();
            editSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    query = query.toLowerCase(Locale.getDefault());
                    ArrayList<Contacts> users = new ArrayList<>();
                    contacts = getContactsList("CONTACTS");
                    for (int i = 0; i < numberOfContacts; i++) {
                        Contacts user = contacts.get(i);
                        if (user.getContactName().toLowerCase(Locale.getDefault()).contains(query)) {
                            try {
                                users.add(user);
                                Log.w("Users", user.getContactName());
                            } catch (NullPointerException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    Log.w("Users", contacts.toString());
                    updateUI(users);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    newText = newText.toLowerCase(Locale.getDefault());
                    ArrayList<Contacts> users = new ArrayList<>();
                    contacts = getContactsList("CONTACTS");
                    for (int i = 0; i < numberOfContacts; i++) {
                        Contacts user = contacts.get(i);
                        if (user.getContactName().toLowerCase(Locale.getDefault()).contains(newText)) {
                            try {
                                users.add(user);
                                Log.w("Users", user.getContactName());
                            } catch (NullPointerException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    Log.w("Users", contacts.toString());
                    updateUI(users);
                    return false;
                }
            });

            listView.setOnItemClickListener((parent, view, position, id) -> {
                String num = contacts.get(position).getContactNumber();
                sendData(num);
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Contacts Permission");
            builder.setMessage("Please enable Contacts permission to read contacts");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
            });
            Dialog alertDialog = builder.create();
            alertDialog.show();
        }

        return v;
    }

    private void sendData(String number) {
        //INTENT OBJ
        Intent i = new Intent(getActivity().getBaseContext(), HomeActivity.class);

        //PACK DATA
        i.putExtra("number", number);

        //START ACTIVITY
        getActivity().startActivity(i);
    }

    private void updateUI(ArrayList<Contacts> contact) {
        listView.setAdapter(new ContactsAdapter(getActivity(), contact));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String num = contact.get(position).getContactNumber();
            sendData(num);
        });
    }

    public ArrayList<Contacts> getContactsList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Contacts>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}
