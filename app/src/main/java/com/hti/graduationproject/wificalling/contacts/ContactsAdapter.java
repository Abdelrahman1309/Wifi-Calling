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

public class ContactsAdapter extends ArrayAdapter<Contacts> {
    private List<Contacts> contactsList = null;
    private ArrayList<Contacts> contacts;

    public ContactsAdapter(Context context, ArrayList<Contacts> contact) {
        super(context, 0, contact);
        contacts = contact;
        contactsList = contact;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
        }
        Contacts currentContact = getItem(position);

        TextView name = listItemView.findViewById(R.id.contact_name);
        name.setText(currentContact.getContactName());

        TextView number = listItemView.findViewById(R.id.contact_number);
        number.setText(currentContact.getContactNumber());

        return listItemView;
    }

}
