package com.hti.graduationproject.wificalling.contacts;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hti.graduationproject.wificalling.R;

import java.util.ArrayList;
import java.util.List;

public class AvailableContactsAdapter extends ArrayAdapter<AvailableContacts> {

    private List<AvailableContacts> logsList = null;
    private ArrayList<AvailableContacts> logs;

    public AvailableContactsAdapter(Context context, ArrayList<AvailableContacts> contact) {
        super(context, 0, contact);
        logs = contact;
        logsList = contact;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.available_contact_item, parent, false);
        }
        AvailableContacts currentAvailable = getItem(position);

        TextView name = listItemView.findViewById(R.id.available_contacts);
        name.setText(currentAvailable.getContactName());

        return listItemView;
    }

}
