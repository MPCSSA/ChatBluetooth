package com.example.christian.chatbluetooth.view;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;

import java.util.zip.Inflater;

public class MyProfileAdapter extends ArrayAdapter<String>{

    private int layout;

    public MyProfileAdapter(Context context, int resource) {
        super(context, resource);

        layout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout, parent, false);
        }

        ((TextView) view.findViewById(R.id.field)).setText(getItem(position));
        if (position == 0){
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.visibility_cb);
            checkBox.setVisibility(View.INVISIBLE);
            checkBox.setEnabled(false);
        }

        return view;
    }
}
