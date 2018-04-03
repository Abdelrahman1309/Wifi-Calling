package com.hti.graduationproject.wificalling.contacts;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.hti.graduationproject.wificalling.R;
import java.util.ArrayList;
import java.util.List;

public class LogsAdapter extends ArrayAdapter<Logs> {
    private List<Logs> logsList = null;
    private ArrayList<Logs> logs;

    public LogsAdapter(Context context, ArrayList<Logs> contact) {
        super(context, 0, contact);
        logs = contact;
        logsList = contact;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.log_item, parent, false);
        }
        Logs currentLog = getItem(position);

        try {
            TextView name = listItemView.findViewById(R.id.log_name);
            name.setText(currentLog.getLogName());

            TextView tech = listItemView.findViewById(R.id.log_technology);
            tech.setText(currentLog.getLogTechnology());

            TextView date = listItemView.findViewById(R.id.log_date);
            date.setText(currentLog.getLogDate());

            ImageView icon = listItemView.findViewById(R.id.log_icon);
            icon.setImageResource(currentLog.getLogIcon());
        }catch (Resources.NotFoundException ex){
            ex.printStackTrace();
        }
        return listItemView;
    }

}
